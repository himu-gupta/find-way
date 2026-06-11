@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.findway.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.findway.domain.TrailPoint
import com.example.findway.theme.FindWayTheme
import com.example.findway.ui.formatDistance
import com.example.findway.ui.formatElapsed
import com.example.findway.ui.model.HomeUiState
import com.example.findway.ui.model.ReadinessItem
import com.example.findway.ui.model.ReturnUiState
import com.example.findway.ui.model.SavedTrailUiItem
import com.example.findway.ui.model.TrackingUiState
import com.example.findway.ui.model.TrailDetailUiState
import com.example.findway.ui.model.TrailMapState
import kotlin.math.max

@Composable
fun HomeScreen(
  state: HomeUiState,
  hasLocationPermission: Boolean,
  onStartTrail: () -> Unit,
  onOpenSavedTrails: () -> Unit,
  onOpenSos: () -> Unit,
  onOpenSettings: () -> Unit,
) {
  FindWayScaffold(title = "Find Way", actions = { TextButton(onClick = onOpenSettings) { Text("Settings") } }) { innerPadding ->
    LazyColumn(
      modifier = Modifier.fillMaxSize().padding(innerPadding).consumeWindowInsets(innerPadding),
      contentPadding = PaddingValues(20.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
      item {
        Text(
          text = "Ready for the trail?",
          style = MaterialTheme.typography.headlineMedium,
          fontWeight = FontWeight.Bold,
        )
      }
      item {
        TrailReadinessCard(
          items =
            listOf(
              ReadinessItem(
                label = "Location access",
                value = if (hasLocationPermission) "Granted" else "Required",
                isReady = hasLocationPermission,
              ),
              ReadinessItem(label = "Offline recording", value = "On-device", isReady = true),
              ReadinessItem(
                label = "Battery",
                value = state.batteryPercent?.let { "$it%" } ?: "Unavailable",
                isReady = state.batteryPercent == null || state.batteryPercent >= 20,
              ),
              ReadinessItem(
                label = "Trail storage",
                value = formatStorage(state.availableStorageBytes),
                isReady = state.availableStorageBytes >= 100L * 1024 * 1024,
              ),
            ),
        )
      }
      item {
        Button(modifier = Modifier.fillMaxWidth().height(56.dp), onClick = onStartTrail) {
          Text(if (state.hasActiveTrail) "Resume Trail" else "Start Trail")
        }
      }
      item {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
          OutlinedButton(modifier = Modifier.weight(1f), onClick = onOpenSavedTrails) { Text("Saved Trails") }
          OutlinedButton(modifier = Modifier.weight(1f), onClick = onOpenSos) { Text("SOS") }
        }
      }
      item {
        SafetyNote(
          title = "Start before you leave",
          body = "Tracking begins only after you tap Start Trail. Confirm the recording indicator before moving away from your starting point.",
        )
      }
    }
  }
}

@Composable
fun TrackingScreen(
  state: TrackingUiState,
  onBack: () -> Unit,
  onTakeMeBack: () -> Unit,
  onOpenSos: () -> Unit,
  onStop: () -> Unit,
) {
  FindWayScaffold(title = "Tracking", onBack = onBack, actions = { TextButton(onClick = onOpenSos) { Text("SOS") } }) { innerPadding ->
    LazyColumn(
      modifier = Modifier.fillMaxSize().padding(innerPadding).consumeWindowInsets(innerPadding),
      contentPadding = PaddingValues(20.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
      item {
        ActiveTrailCard(
          state =
            TrailMapState(
              breadcrumbs = state.breadcrumbs,
              distanceLabel = formatDistance(state.distanceMeters),
              accuracyLabel = state.accuracyMeters?.let { "GPS ±${it.toInt()} m" } ?: "Waiting for GPS",
              breadcrumbCount = state.breadcrumbs.size,
            ),
          isLive = true,
          statusLabel = if (state.isRecording) "Recording" else "Starting",
        )
      }
      item {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
          TrailMetric(label = "Elapsed", value = formatElapsed(state.elapsedMillis), modifier = Modifier.weight(1f))
          TrailMetric(label = "Points", value = state.breadcrumbs.size.toString(), modifier = Modifier.weight(1f))
          TrailMetric(label = "Accuracy", value = state.accuracyMeters?.let { "${it.toInt()} m" } ?: "--", modifier = Modifier.weight(1f))
        }
      }
      item {
        Button(
          modifier = Modifier.fillMaxWidth().height(56.dp),
          enabled = state.breadcrumbs.size >= 2,
          onClick = onTakeMeBack,
        ) {
          Text("Take Me Back")
        }
      }
      item {
        OutlinedButton(modifier = Modifier.fillMaxWidth().height(52.dp), onClick = onStop) {
          Text("Stop Tracking")
        }
      }
    }
  }
}

@Composable
fun ReturnModeScreen(
  state: ReturnUiState,
  onBack: () -> Unit,
  onOpenSos: () -> Unit,
) {
  FindWayScaffold(title = "Return Mode", onBack = onBack, actions = { TextButton(onClick = onOpenSos) { Text("SOS") } }) { innerPadding ->
    LazyColumn(
      modifier = Modifier.fillMaxSize().padding(innerPadding).consumeWindowInsets(innerPadding),
      contentPadding = PaddingValues(20.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      item { Text("Take Me Back", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold) }
      item {
        if (state.targetBearingDegrees != null && state.deviceHeadingDegrees != null) {
          DirectionCompassCard(
            targetBearingDegrees = state.targetBearingDegrees,
            deviceHeadingDegrees = state.deviceHeadingDegrees,
            distanceMeters = state.distanceToNextMeters,
            gpsAccuracy = state.accuracyMeters?.let { "GPS ±${it.toInt()} m" } ?: "GPS unavailable",
          )
        } else {
          SafetyNote(
            title = "Preparing return guidance",
            body =
              if (state.breadcrumbs.size < 2) {
                "At least two recorded GPS breadcrumbs are needed before Find Way can calculate the route back."
              } else {
                "Move the phone gently while Find Way waits for a compass heading."
              },
          )
        }
      }
      item {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
          TrailMetric(
            label = "Next point",
            value = if (state.targetBearingDegrees == null) "--" else "${state.distanceToNextMeters} m",
            modifier = Modifier.weight(1f),
          )
          TrailMetric(label = "Remaining", value = formatDistance(state.remainingDistanceMeters.toDouble()), modifier = Modifier.weight(1f))
        }
      }
      item {
        SafetyNote(
          title = if (state.isOffRoute) "Off recorded path" else "On recorded path",
          body =
            if (state.isOffRoute) {
              "You are about ${state.offRouteDistanceMeters} m from the nearest saved breadcrumb. Move carefully toward the arrow."
            } else {
              "Find Way is guiding you through the recorded breadcrumbs in reverse order."
            },
        )
      }
      item {
        Button(modifier = Modifier.fillMaxWidth().height(56.dp), onClick = onOpenSos) { Text("Open SOS") }
      }
    }
  }
}

@Composable
fun SavedTrailsScreen(
  trails: List<SavedTrailUiItem>,
  onBack: () -> Unit,
  onOpenTrail: (Long) -> Unit,
) {
  FindWayScaffold(title = "Saved Trails", onBack = onBack) { innerPadding ->
    LazyColumn(
      modifier = Modifier.fillMaxSize().padding(innerPadding).consumeWindowInsets(innerPadding),
      contentPadding = PaddingValues(20.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      if (trails.isEmpty()) {
        item {
          SafetyNote(
            title = "No saved trails",
            body = "A trail appears here after you stop an active recording.",
          )
        }
      }
      items(trails.size) { index ->
        val trail = trails[index]
        TrailRow(name = trail.name, detail = trail.detail, onClick = { onOpenTrail(trail.id) })
      }
    }
  }
}

@Composable
fun TrailDetailScreen(
  state: TrailDetailUiState?,
  onBack: () -> Unit,
) {
  FindWayScaffold(title = "Trail Detail", onBack = onBack) { innerPadding ->
    Column(
      modifier = Modifier.fillMaxSize().padding(innerPadding).consumeWindowInsets(innerPadding).padding(20.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
      if (state == null) {
        Text("Loading saved trail…", color = MaterialTheme.colorScheme.onSurfaceVariant)
      } else {
        Text(state.name, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        ActiveTrailCard(
          state =
            TrailMapState(
              breadcrumbs = state.breadcrumbs,
              distanceLabel = formatDistance(state.distanceMeters),
              accuracyLabel = "Saved route",
              breadcrumbCount = state.breadcrumbs.size,
            ),
          isLive = false,
          statusLabel = "Saved",
        )
        SafetyNote(
          title = "Saved route review",
          body = "Starting guidance from a saved trail will be enabled after current-location matching is implemented.",
        )
      }
    }
  }
}

@Composable
fun SosScreen(
  trackingState: TrackingUiState,
  onBack: () -> Unit,
  onShareLocation: () -> Unit,
  onEmergencyCall: () -> Unit,
) {
  val currentPoint = trackingState.breadcrumbs.lastOrNull()
  FindWayScaffold(title = "SOS", onBack = onBack) { innerPadding ->
    Column(
      modifier = Modifier.fillMaxSize().padding(innerPadding).consumeWindowInsets(innerPadding).padding(20.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
      Text("Emergency Info", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
      SafetyNote(
        title = "Current coordinates",
        body =
          currentPoint?.let { point ->
            "%.6f, %.6f\nAccuracy: ±%d m".format(point.latitude, point.longitude, point.accuracyMeters?.toInt() ?: 0)
          } ?: "Waiting for a recorded GPS position.",
      )
      SafetyNote(title = "Distance from start", body = "${formatDistance(trackingState.distanceMeters)} along the recorded route")
      Button(
        modifier = Modifier.fillMaxWidth().height(56.dp),
        enabled = currentPoint != null,
        onClick = onShareLocation,
      ) {
        Text("Share Location")
      }
      OutlinedButton(modifier = Modifier.fillMaxWidth().height(56.dp), onClick = onEmergencyCall) { Text("Emergency Call") }
    }
  }
}

@Composable
fun SettingsScreen(onBack: () -> Unit) {
  FindWayScaffold(title = "Settings", onBack = onBack) { innerPadding ->
    Column(
      modifier = Modifier.fillMaxSize().padding(innerPadding).consumeWindowInsets(innerPadding).padding(20.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
      SafetyNote(
        title = "No configurable settings yet",
        body = "Find Way currently uses high-accuracy recording and metric units. Controls will appear here only when they are functional.",
      )
    }
  }
}

@Composable
private fun FindWayScaffold(
  title: String,
  onBack: (() -> Unit)? = null,
  actions: @Composable () -> Unit = {},
  content: @Composable (PaddingValues) -> Unit,
) {
  Scaffold(
    contentWindowInsets = WindowInsets.safeDrawing,
    topBar = {
      if (onBack == null) {
        CenterAlignedTopAppBar(title = { Text(title, fontWeight = FontWeight.SemiBold) }, actions = { actions() })
      } else {
        TopAppBar(
          title = { Text(title, fontWeight = FontWeight.SemiBold) },
          navigationIcon = { TextButton(onClick = onBack) { Text("Back") } },
          actions = { actions() },
        )
      }
    },
    content = content,
  )
}

@Composable
private fun TrailReadinessCard(
  items: List<ReadinessItem>,
) {
  val isReady = items.all(ReadinessItem::isReady)
  Card(
    shape = RoundedCornerShape(8.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    modifier = Modifier.fillMaxWidth(),
  ) {
    Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
          Text("Trail readiness", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
          Text("Core checks before recording", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text(
          if (isReady) "READY" else "CHECK",
          color = if (isReady) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary,
          fontWeight = FontWeight.Bold,
        )
      }
      items.forEachIndexed { index, item ->
        ReadinessRow(item)
        if (index != items.lastIndex) {
          Box(Modifier.fillMaxWidth().height(1.dp).background(MaterialTheme.colorScheme.outline.copy(alpha = 0.18f)))
        }
      }
    }
  }
}

@Composable
private fun ReadinessRow(item: ReadinessItem) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
      Box(
        Modifier.size(10.dp)
          .background(
            if (item.isReady) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary,
            CircleShape,
          ),
      )
      Text(item.label, fontWeight = FontWeight.Medium)
    }
    Text(item.value, color = MaterialTheme.colorScheme.onSurfaceVariant)
  }
}

@Composable
private fun ActiveTrailCard(
  state: TrailMapState,
  isLive: Boolean,
  statusLabel: String,
) {
  Card(
    shape = RoundedCornerShape(8.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    modifier = Modifier.fillMaxWidth(),
  ) {
    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
      Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth(),
      ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
          Text(
            if (isLive) "Live breadcrumb trail" else "Recorded breadcrumb trail",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
          )
          Text("${state.breadcrumbCount} points recorded", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text(state.distanceLabel, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
      }
      BreadcrumbMap(points = state.breadcrumbs, modifier = Modifier.fillMaxWidth().height(220.dp))
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
          Box(
            Modifier.size(9.dp)
              .background(if (statusLabel == "Recording") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline, CircleShape),
          )
          Text(
            statusLabel,
            color = if (statusLabel == "Recording") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.SemiBold,
          )
        }
        Text(state.accuracyLabel, color = MaterialTheme.colorScheme.onSurfaceVariant)
      }
    }
  }
}

@Composable
private fun BreadcrumbMap(
  points: List<TrailPoint>,
  modifier: Modifier = Modifier,
) {
  val routeColor = MaterialTheme.colorScheme.tertiary
  val startColor = MaterialTheme.colorScheme.primary
  val currentColor = MaterialTheme.colorScheme.tertiary
  val background = MaterialTheme.colorScheme.surface
  Canvas(
    modifier =
      modifier
        .background(background, RoundedCornerShape(8.dp))
        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.18f), RoundedCornerShape(8.dp))
        .semantics { contentDescription = "Recorded breadcrumb route with ${points.size} points" },
  ) {
    val horizontalPadding = size.width * 0.12f
    val verticalPadding = size.height * 0.14f
    val mapWidth = size.width - horizontalPadding * 2
    val mapHeight = size.height - verticalPadding * 2

    repeat(3) { index ->
      val y = verticalPadding + mapHeight * (index + 1) / 4f
      drawLine(
        color = startColor.copy(alpha = 0.10f),
        start = Offset(horizontalPadding, y),
        end = Offset(size.width - horizontalPadding, y),
        strokeWidth = 1.dp.toPx(),
      )
    }

    if (points.isEmpty()) return@Canvas

    val minLat = points.minOf { it.latitude }
    val maxLat = points.maxOf { it.latitude }
    val minLon = points.minOf { it.longitude }
    val maxLon = points.maxOf { it.longitude }
    val latSpan = max(maxLat - minLat, 0.000001)
    val lonSpan = max(maxLon - minLon, 0.000001)
    val offsets =
      points.map { point ->
        Offset(
          x = horizontalPadding + (((point.longitude - minLon) / lonSpan).toFloat() * mapWidth),
          y = verticalPadding + ((1f - ((point.latitude - minLat) / latSpan).toFloat()) * mapHeight),
        )
      }
    val path =
      Path().apply {
        moveTo(offsets.first().x, offsets.first().y)
        offsets.drop(1).forEach { lineTo(it.x, it.y) }
      }

    drawPath(path = path, color = routeColor, style = Stroke(width = 5.dp.toPx(), cap = StrokeCap.Round))
    offsets.dropLast(1).forEach { offset ->
      drawCircle(color = routeColor.copy(alpha = 0.55f), radius = 3.dp.toPx(), center = offset)
    }
    drawCircle(color = startColor, radius = 9.dp.toPx(), center = offsets.first())
    drawCircle(color = background, radius = 11.dp.toPx(), center = offsets.last())
    drawCircle(color = currentColor, radius = 8.dp.toPx(), center = offsets.last())
  }
}

@Composable
private fun TrailMetric(
  label: String,
  value: String,
  modifier: Modifier = Modifier,
) {
  Card(shape = RoundedCornerShape(8.dp), modifier = modifier) {
    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
      Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
      Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
    }
  }
}

@Composable
private fun DirectionCompassCard(
  targetBearingDegrees: Float,
  deviceHeadingDegrees: Float,
  distanceMeters: Int,
  gpsAccuracy: String,
) {
  val relativeBearing = (targetBearingDegrees - deviceHeadingDegrees + 360f) % 360f
  val cardinalDirection = cardinalDirection(targetBearingDegrees)
  Card(
    shape = RoundedCornerShape(8.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    modifier = Modifier.fillMaxWidth(),
  ) {
    Column(
      modifier = Modifier.padding(20.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      Text("Follow the arrow", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
      Box(
        modifier =
          Modifier.size(220.dp)
            .background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
            .semantics {
              contentDescription =
                "Direction arrow pointing $cardinalDirection at ${targetBearingDegrees.toInt()} degrees"
            },
        contentAlignment = Alignment.Center,
      ) {
        DirectionArrow(relativeBearingDegrees = relativeBearing, modifier = Modifier.fillMaxSize().padding(26.dp))
        Text("N", modifier = Modifier.align(Alignment.TopCenter).padding(top = 8.dp), fontWeight = FontWeight.Bold)
        Text("E", modifier = Modifier.align(Alignment.CenterEnd).padding(end = 10.dp), fontWeight = FontWeight.SemiBold)
        Text("S", modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 8.dp), fontWeight = FontWeight.SemiBold)
        Text("W", modifier = Modifier.align(Alignment.CenterStart).padding(start = 8.dp), fontWeight = FontWeight.SemiBold)
      }
      Text(
        "$distanceMeters m to next breadcrumb",
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center,
      )
      Text(
        "${targetBearingDegrees.toInt()}° $cardinalDirection · $gpsAccuracy",
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
      Text(
        "Turn with your phone until the arrow points straight ahead, then walk toward the next breadcrumb.",
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
    }
  }
}

@Composable
private fun DirectionArrow(
  relativeBearingDegrees: Float,
  modifier: Modifier = Modifier,
) {
  val arrowColor = MaterialTheme.colorScheme.tertiary
  val arrowOutline = MaterialTheme.colorScheme.onTertiary
  Canvas(modifier = modifier) {
    val center = this.center
    val arrowLength = size.minDimension * 0.42f
    val headWidth = size.minDimension * 0.13f
    val headHeight = size.minDimension * 0.18f

    drawCircle(
      color = arrowColor.copy(alpha = 0.14f),
      radius = size.minDimension * 0.46f,
      center = center,
      style = Stroke(width = 2.dp.toPx()),
    )
    rotate(degrees = relativeBearingDegrees, pivot = center) {
      drawLine(
        color = arrowColor,
        start = Offset(center.x, center.y + arrowLength * 0.45f),
        end = Offset(center.x, center.y - arrowLength + headHeight * 0.45f),
        strokeWidth = 14.dp.toPx(),
        cap = StrokeCap.Round,
      )
      val arrowHead =
        Path().apply {
          moveTo(center.x, center.y - arrowLength)
          lineTo(center.x - headWidth, center.y - arrowLength + headHeight)
          lineTo(center.x + headWidth, center.y - arrowLength + headHeight)
          close()
        }
      drawPath(path = arrowHead, color = arrowColor)
      drawPath(path = arrowHead, color = arrowOutline.copy(alpha = 0.35f), style = Stroke(width = 1.dp.toPx()))
    }
    drawCircle(color = arrowColor, radius = 10.dp.toPx(), center = center)
  }
}

private fun cardinalDirection(bearingDegrees: Float): String {
  val directions = listOf("N", "NE", "E", "SE", "S", "SW", "W", "NW")
  val normalized = (bearingDegrees % 360f + 360f) % 360f
  return directions[((normalized + 22.5f) / 45f).toInt() % directions.size]
}

@Composable
private fun SafetyNote(
  title: String,
  body: String,
) {
  Card(
    shape = RoundedCornerShape(8.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
    modifier = Modifier.fillMaxWidth(),
  ) {
    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
      Text(title, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSecondaryContainer)
      Text(body, color = MaterialTheme.colorScheme.onSecondaryContainer)
    }
  }
}

@Composable
private fun TrailRow(
  name: String,
  detail: String,
  onClick: () -> Unit,
) {
  Card(shape = RoundedCornerShape(8.dp), onClick = onClick, modifier = Modifier.fillMaxWidth()) {
    Row(
      modifier = Modifier.padding(16.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Text(detail, color = MaterialTheme.colorScheme.onSurfaceVariant)
      }
      Text("Open", color = MaterialTheme.colorScheme.primary)
    }
  }
}

private fun formatStorage(bytes: Long): String {
  if (bytes <= 0L) return "Unavailable"
  val gigabytes = bytes.toDouble() / (1024 * 1024 * 1024)
  return if (gigabytes >= 1) "%.1f GB free".format(gigabytes) else "${bytes / (1024 * 1024)} MB free"
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
  FindWayTheme(dynamicColor = false) {
    HomeScreen(
      state = HomeUiState(),
      hasLocationPermission = false,
      onStartTrail = {},
      onOpenSavedTrails = {},
      onOpenSos = {},
      onOpenSettings = {},
    )
  }
}

@Preview(showBackground = true)
@Composable
private fun ReturnModeScreenPreview() {
  FindWayTheme(dynamicColor = false) {
    ReturnModeScreen(state = ReturnUiState(), onBack = {}, onOpenSos = {})
  }
}
