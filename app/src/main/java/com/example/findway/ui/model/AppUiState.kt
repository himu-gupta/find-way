package com.example.findway.ui.model

import com.example.findway.domain.TrailPoint

data class HomeUiState(
  val hasActiveTrail: Boolean = false,
  val batteryPercent: Int? = null,
  val availableStorageBytes: Long = 0L,
)

data class TrackingUiState(
  val isRecording: Boolean = false,
  val breadcrumbs: List<TrailPoint> = emptyList(),
  val elapsedMillis: Long = 0L,
  val distanceMeters: Double = 0.0,
  val accuracyMeters: Float? = null,
)

data class ReturnUiState(
  val breadcrumbs: List<TrailPoint> = emptyList(),
  val deviceHeadingDegrees: Float? = null,
  val targetBearingDegrees: Float? = null,
  val distanceToNextMeters: Int = 0,
  val remainingDistanceMeters: Int = 0,
  val accuracyMeters: Float? = null,
  val offRouteDistanceMeters: Int = 0,
  val isOffRoute: Boolean = false,
)

data class SavedTrailUiItem(
  val id: Long,
  val name: String,
  val detail: String,
)

data class TrailDetailUiState(
  val id: Long = 0,
  val name: String = "",
  val breadcrumbs: List<TrailPoint> = emptyList(),
  val distanceMeters: Double = 0.0,
  val startedAtMillis: Long = 0L,
  val endedAtMillis: Long? = null,
)
