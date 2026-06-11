@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.findway.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.material3.Switch
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
import com.example.findway.theme.FindWayTheme

@Composable
fun HomeScreen(
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
          text = "Your way back, saved before you need it.",
          style = MaterialTheme.typography.headlineMedium,
          fontWeight = FontWeight.Bold,
        )
      }
      item {
        RoutePreviewCard(status = "Offline ready", distance = "0.0 km", accuracy = "GPS waiting")
      }
      item {
        Button(modifier = Modifier.fillMaxWidth().height(56.dp), onClick = onStartTrail) {
          Text("Start Trail")
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
          title = "Safety first",
          body = "Find Way will record breadcrumbs while tracking is active. It is a backup aid, not a replacement for maps or emergency judgment.",
        )
      }
    }
  }
}

@Composable
fun TrackingScreen(
  onBack: () -> Unit,
  onTakeMeBack: () -> Unit,
  onOpenSos: () -> Unit,
) {
  FindWayScaffold(title = "Tracking", onBack = onBack, actions = { TextButton(onClick = onOpenSos) { Text("SOS") } }) { innerPadding ->
    LazyColumn(
      modifier = Modifier.fillMaxSize().padding(innerPadding).consumeWindowInsets(innerPadding),
      contentPadding = PaddingValues(20.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
      item { RoutePreviewCard(status = "Recording active", distance = "2.4 km", accuracy = "GPS good") }
      item {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
          TrailMetric(label = "Elapsed", value = "42m", modifier = Modifier.weight(1f))
          TrailMetric(label = "Points", value = "186", modifier = Modifier.weight(1f))
          TrailMetric(label = "Battery", value = "82%", modifier = Modifier.weight(1f))
        }
      }
      item {
        Button(modifier = Modifier.fillMaxWidth().height(56.dp), onClick = onTakeMeBack) {
          Text("Take Me Back")
        }
      }
      item {
        OutlinedButton(modifier = Modifier.fillMaxWidth().height(52.dp), onClick = onBack) {
          Text("Stop Tracking")
        }
      }
    }
  }
}

@Composable
fun ReturnModeScreen(
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
        DirectionCompassCard(
          targetBearingDegrees = 42f,
          deviceHeadingDegrees = 0f,
          distanceMeters = 120,
          gpsAccuracy = "GPS good",
        )
      }
      item {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
          TrailMetric(label = "Next point", value = "120 m", modifier = Modifier.weight(1f))
          TrailMetric(label = "Remaining", value = "5.6 km", modifier = Modifier.weight(1f))
        }
      }
      item {
        SafetyNote(title = "On path", body = "Stay close to the orange breadcrumb line. The app will warn you when you drift away.")
      }
      item {
        Button(modifier = Modifier.fillMaxWidth().height(56.dp), onClick = onOpenSos) { Text("Open SOS") }
      }
    }
  }
}

@Composable
fun SavedTrailsScreen(
  onBack: () -> Unit,
  onOpenTrail: (String) -> Unit,
) {
  FindWayScaffold(title = "Saved Trails", onBack = onBack) { innerPadding ->
    LazyColumn(
      modifier = Modifier.fillMaxSize().padding(innerPadding).consumeWindowInsets(innerPadding),
      contentPadding = PaddingValues(20.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      items(sampleTrails.size) { index ->
        val trail = sampleTrails[index]
        TrailRow(name = trail.name, detail = trail.detail, onClick = { onOpenTrail(trail.id) })
      }
    }
  }
}

@Composable
fun TrailDetailScreen(
  trailId: String,
  onBack: () -> Unit,
  onRetrace: () -> Unit,
) {
  FindWayScaffold(title = "Trail Detail", onBack = onBack) { innerPadding ->
    Column(
      modifier = Modifier.fillMaxSize().padding(innerPadding).consumeWindowInsets(innerPadding).padding(20.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
      Text("Trail $trailId", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
      RoutePreviewCard(status = "Saved route", distance = "5.6 km", accuracy = "342 breadcrumbs")
      Button(modifier = Modifier.fillMaxWidth().height(56.dp), onClick = onRetrace) { Text("Retrace Route") }
    }
  }
}

@Composable
fun SosScreen(onBack: () -> Unit) {
  FindWayScaffold(title = "SOS", onBack = onBack) { innerPadding ->
    Column(
      modifier = Modifier.fillMaxSize().padding(innerPadding).consumeWindowInsets(innerPadding).padding(20.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
      Text("Emergency Info", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
      SafetyNote(title = "Current coordinates", body = "25.2854, 51.5310\nAccuracy: waiting for GPS")
      SafetyNote(title = "Distance from start", body = "2.4 km along recorded route")
      Button(modifier = Modifier.fillMaxWidth().height(56.dp), onClick = {}) { Text("Share Location") }
      OutlinedButton(modifier = Modifier.fillMaxWidth().height(56.dp), onClick = {}) { Text("Emergency Call") }
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
      SettingRow(title = "Low battery tracking", checked = true)
      SettingRow(title = "Off-route vibration", checked = true)
      SettingRow(title = "Use kilometers", checked = true)
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
private fun RoutePreviewCard(
  status: String,
  distance: String,
  accuracy: String,
) {
  Card(
    shape = RoundedCornerShape(8.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    modifier = Modifier.fillMaxWidth(),
  ) {
    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
      Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
        AssistChip(onClick = {}, label = { Text(status) })
        Text(distance, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
      }
      BreadcrumbMap(modifier = Modifier.fillMaxWidth().height(180.dp))
      Text(accuracy, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
  }
}

@Composable
private fun BreadcrumbMap(modifier: Modifier = Modifier) {
  val routeColor = MaterialTheme.colorScheme.tertiary
  val pointColor = MaterialTheme.colorScheme.primary
  val background = MaterialTheme.colorScheme.surface
  Canvas(modifier = modifier.background(background, RoundedCornerShape(8.dp)).semantics { contentDescription = "Breadcrumb route preview" }) {
    val path =
      Path().apply {
        moveTo(size.width * 0.14f, size.height * 0.78f)
        cubicTo(size.width * 0.32f, size.height * 0.62f, size.width * 0.28f, size.height * 0.28f, size.width * 0.50f, size.height * 0.38f)
        cubicTo(size.width * 0.70f, size.height * 0.48f, size.width * 0.66f, size.height * 0.14f, size.width * 0.86f, size.height * 0.22f)
      }
    drawLine(
      color = pointColor.copy(alpha = 0.15f),
      start = Offset(size.width * 0.1f, size.height * 0.3f),
      end = Offset(size.width * 0.92f, size.height * 0.3f),
      strokeWidth = 2.dp.toPx(),
    )
    drawLine(
      color = pointColor.copy(alpha = 0.15f),
      start = Offset(size.width * 0.1f, size.height * 0.58f),
      end = Offset(size.width * 0.92f, size.height * 0.58f),
      strokeWidth = 2.dp.toPx(),
    )
    drawPath(path = path, color = routeColor, style = Stroke(width = 5.dp.toPx(), cap = StrokeCap.Round))
    drawCircle(color = pointColor, radius = 8.dp.toPx(), center = Offset(size.width * 0.14f, size.height * 0.78f))
    drawCircle(color = routeColor, radius = 8.dp.toPx(), center = Offset(size.width * 0.86f, size.height * 0.22f))
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

@Composable
private fun SettingRow(
  title: String,
  checked: Boolean,
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Text(title, style = MaterialTheme.typography.titleMedium)
    Switch(checked = checked, onCheckedChange = {})
  }
}

private data class SampleTrail(
  val id: String,
  val name: String,
  val detail: String,
)

private val sampleTrails =
  listOf(
    SampleTrail("morning-ridge", "Morning Ridge", "5.6 km, 342 breadcrumbs"),
    SampleTrail("camp-loop", "Camp Loop", "1.8 km, 104 breadcrumbs"),
    SampleTrail("wadi-lookout", "Wadi Lookout", "3.2 km, 218 breadcrumbs"),
  )

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
  FindWayTheme(dynamicColor = false) {
    HomeScreen(onStartTrail = {}, onOpenSavedTrails = {}, onOpenSos = {}, onOpenSettings = {})
  }
}

@Preview(showBackground = true)
@Composable
private fun ReturnModeScreenPreview() {
  FindWayTheme(dynamicColor = false) {
    ReturnModeScreen(onBack = {}, onOpenSos = {})
  }
}
