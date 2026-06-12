package com.example.findway.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.findway.data.TrailRepository
import com.example.findway.device.DeviceStatusProvider
import com.example.findway.domain.BacktrackNavigator
import com.example.findway.domain.Trail
import com.example.findway.domain.TrailPoint
import com.example.findway.domain.routeDistanceMeters
import com.example.findway.location.CurrentLocationStore
import com.example.findway.location.HeadingProvider
import com.example.findway.location.TrailRecordingController
import com.example.findway.ui.model.HomeUiState
import com.example.findway.ui.model.ReturnUiState
import com.example.findway.ui.model.SavedTrailUiItem
import com.example.findway.ui.model.TrackingUiState
import com.example.findway.ui.model.TrailDetailUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import java.text.DateFormat
import java.util.Date
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.ExperimentalCoroutinesApi

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class AppViewModel @Inject constructor(
  repository: TrailRepository,
  private val recordingController: TrailRecordingController,
  private val deviceStatusProvider: DeviceStatusProvider,
  headingProvider: HeadingProvider,
  currentLocationStore: CurrentLocationStore,
) : ViewModel() {
  private val activeTrail =
    repository.observeActiveTrail().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
  private val deviceStatus = MutableStateFlow(deviceStatusProvider.currentStatus())
  private val selectedTrailId = MutableStateFlow<Long?>(null)
  private val backtrackRoute = MutableStateFlow<List<TrailPoint>>(emptyList())
  private val isBacktracking = MutableStateFlow(false)
  private var backtrackNavigator: BacktrackNavigator? = null
  private val now = tickerFlow()

  val homeUiState: StateFlow<HomeUiState> =
    combine(activeTrail, deviceStatus) { trail, status ->
        HomeUiState(
          hasActiveTrail = trail != null,
          batteryPercent = status.batteryPercent,
          availableStorageBytes = status.availableStorageBytes,
        )
      }
      .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())

  val trackingUiState: StateFlow<TrackingUiState> =
    combine(activeTrail, now) { trail, currentTime -> trail.toTrackingUiState(currentTime) }
      .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TrackingUiState())

  val returnUiState: StateFlow<ReturnUiState> =
    combine(backtrackRoute, currentLocationStore.location, headingProvider.headings, isBacktracking) { route, location, heading, active ->
        toReturnUiState(route, location, heading, active)
      }
      .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ReturnUiState())

  val savedTrails: StateFlow<List<SavedTrailUiItem>> =
    repository.observeSavedTrails().map { trails -> trails.map { it.toSavedTrailUiItem() } }
      .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

  val selectedTrail: StateFlow<TrailDetailUiState?> =
    selectedTrailId.flatMapLatest { trailId ->
        if (trailId == null) flowOf(null) else repository.observeTrail(trailId).map { it?.toDetailUiState() }
      }
      .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

  fun refreshDeviceStatus() {
    deviceStatus.value = deviceStatusProvider.currentStatus()
  }

  fun startRecording() = recordingController.start()

  fun stopRecording() {
    resetBacktracking()
    recordingController.stop()
  }

  fun startBacktracking() {
    val route = activeTrail.value?.breadcrumbs.orEmpty()
    if (route.size < 2) return
    backtrackRoute.value = route.toList()
    backtrackNavigator = BacktrackNavigator(route)
    isBacktracking.value = true
    recordingController.startBacktracking()
  }

  fun cancelBacktracking() {
    resetBacktracking()
    recordingController.start()
  }

  fun selectTrail(trailId: Long) {
    selectedTrailId.value = trailId
  }

  private fun Trail?.toTrackingUiState(currentTimeMillis: Long): TrackingUiState {
    if (this == null) return TrackingUiState()
    return TrackingUiState(
      isRecording = true,
      breadcrumbs = breadcrumbs,
      elapsedMillis = (currentTimeMillis - startedAtMillis).coerceAtLeast(0L),
      distanceMeters = routeDistanceMeters(breadcrumbs),
      accuracyMeters = breadcrumbs.lastOrNull()?.accuracyMeters,
    )
  }

  private fun toReturnUiState(
    route: List<TrailPoint>,
    currentLocation: TrailPoint?,
    deviceHeading: Float?,
    active: Boolean,
  ): ReturnUiState {
    if (!active || route.size < 2 || currentLocation == null) {
      return ReturnUiState(
        breadcrumbs = route,
        currentLocation = currentLocation,
        deviceHeadingDegrees = deviceHeading,
        totalBreadcrumbs = route.size,
        accuracyMeters = currentLocation?.accuracyMeters,
        isBacktracking = active,
      )
    }
    val progress = backtrackNavigator?.update(currentLocation) ?: return ReturnUiState()
    return ReturnUiState(
      breadcrumbs = route,
      currentLocation = currentLocation,
      deviceHeadingDegrees = deviceHeading,
      targetBearingDegrees = progress.bearingToNextDegrees?.toFloat(),
      targetBreadcrumbIndex = progress.targetIndex,
      totalBreadcrumbs = progress.totalBreadcrumbs,
      distanceToNextMeters = progress.distanceToNextMeters,
      remainingDistanceMeters = progress.remainingDistanceMeters,
      accuracyMeters = currentLocation.accuracyMeters,
      offRouteDistanceMeters = progress.offRouteDistanceMeters,
      isOffRoute = progress.isOffRoute,
      isBacktracking = true,
      isComplete = progress.isComplete,
    )
  }

  private fun resetBacktracking() {
    isBacktracking.value = false
    backtrackRoute.value = emptyList()
    backtrackNavigator = null
  }

  private fun Trail.toSavedTrailUiItem(): SavedTrailUiItem =
    SavedTrailUiItem(
      id = id,
      name = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(Date(startedAtMillis)),
      detail = "${formatDistance(routeDistanceMeters(breadcrumbs))} · ${breadcrumbs.size} breadcrumbs",
    )

  private fun Trail.toDetailUiState(): TrailDetailUiState =
    TrailDetailUiState(
      id = id,
      name = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(Date(startedAtMillis)),
      breadcrumbs = breadcrumbs,
      distanceMeters = routeDistanceMeters(breadcrumbs),
      startedAtMillis = startedAtMillis,
      endedAtMillis = endedAtMillis,
    )

  private fun tickerFlow(): Flow<Long> =
    flow {
      while (true) {
        emit(System.currentTimeMillis())
        delay(1_000L)
      }
    }
}

fun formatDistance(distanceMeters: Double): String =
  if (distanceMeters < 1_000) "${distanceMeters.toInt()} m" else String.format("%.2f km", distanceMeters / 1_000)

fun formatElapsed(elapsedMillis: Long): String {
  val totalSeconds = elapsedMillis / 1_000
  val hours = totalSeconds / 3_600
  val minutes = (totalSeconds % 3_600) / 60
  val seconds = totalSeconds % 60
  return if (hours > 0) "%d:%02d:%02d".format(hours, minutes, seconds) else "%02d:%02d".format(minutes, seconds)
}
