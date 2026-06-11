package com.example.findway

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.dropUnlessResumed
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.findway.ui.AppViewModel
import com.example.findway.ui.screens.HomeScreen
import com.example.findway.ui.screens.ReturnModeScreen
import com.example.findway.ui.screens.SavedTrailsScreen
import com.example.findway.ui.screens.SettingsScreen
import com.example.findway.ui.screens.SosScreen
import com.example.findway.ui.screens.TrackingScreen
import com.example.findway.ui.screens.TrailDetailScreen

@Composable
fun MainNavigation() {
  val backStack = rememberNavBackStack(Home)
  val viewModel = hiltViewModel<AppViewModel>()
  val context = LocalContext.current
  val homeUiState by viewModel.homeUiState.collectAsStateWithLifecycle()
  val trackingUiState by viewModel.trackingUiState.collectAsStateWithLifecycle()
  val returnUiState by viewModel.returnUiState.collectAsStateWithLifecycle()
  val savedTrails by viewModel.savedTrails.collectAsStateWithLifecycle()
  val selectedTrail by viewModel.selectedTrail.collectAsStateWithLifecycle()
  var hasLocationPermission by remember { mutableStateOf(context.hasLocationPermission()) }

  fun beginRecording() {
    viewModel.startRecording()
    if (backStack.lastOrNull() != Tracking) backStack.add(Tracking)
  }

  val permissionLauncher =
    rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
      hasLocationPermission = context.hasLocationPermission()
      viewModel.refreshDeviceStatus()
      if (hasLocationPermission) beginRecording()
    }

  LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
    hasLocationPermission = context.hasLocationPermission()
    viewModel.refreshDeviceStatus()
  }

  val requestAndStartRecording = {
    if (hasLocationPermission) {
      beginRecording()
    } else {
      permissionLauncher.launch(
        buildList {
          add(Manifest.permission.ACCESS_FINE_LOCATION)
          add(Manifest.permission.ACCESS_COARSE_LOCATION)
          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) add(Manifest.permission.POST_NOTIFICATIONS)
        }.toTypedArray(),
      )
    }
  }

  NavDisplay(
    backStack = backStack,
    onBack = { backStack.removeLastOrNull() },
    entryProvider =
      entryProvider {
        entry<Home> {
          HomeScreen(
            state = homeUiState,
            hasLocationPermission = hasLocationPermission,
            onStartTrail = dropUnlessResumed { requestAndStartRecording() },
            onOpenSavedTrails = dropUnlessResumed { backStack.add(SavedTrails) },
            onOpenSos = dropUnlessResumed { backStack.add(Sos) },
            onOpenSettings = dropUnlessResumed { backStack.add(Settings) },
          )
        }
        entry<Tracking> {
          TrackingScreen(
            state = trackingUiState,
            onBack = { backStack.removeLastOrNull() },
            onTakeMeBack = dropUnlessResumed { backStack.add(ReturnMode) },
            onOpenSos = dropUnlessResumed { backStack.add(Sos) },
            onStop = {
              viewModel.stopRecording()
              backStack.removeLastOrNull()
            },
          )
        }
        entry<ReturnMode> {
          ReturnModeScreen(
            state = returnUiState,
            onBack = { backStack.removeLastOrNull() },
            onOpenSos = dropUnlessResumed { backStack.add(Sos) },
          )
        }
        entry<SavedTrails> {
          SavedTrailsScreen(
            trails = savedTrails,
            onBack = { backStack.removeLastOrNull() },
            onOpenTrail = { trailId -> backStack.add(TrailDetail(trailId)) },
          )
        }
        entry<TrailDetail> { route ->
          LaunchedEffect(route.trailId) { viewModel.selectTrail(route.trailId) }
          TrailDetailScreen(
            state = selectedTrail,
            onBack = { backStack.removeLastOrNull() },
          )
        }
        entry<Sos> {
          SosScreen(
            trackingState = trackingUiState,
            onBack = { backStack.removeLastOrNull() },
            onShareLocation = {
              trackingUiState.breadcrumbs.lastOrNull()?.let { point ->
                val text = "My location: https://maps.google.com/?q=${point.latitude},${point.longitude}"
                context.startActivity(
                  Intent.createChooser(
                    Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, text),
                    "Share location",
                  ),
                )
              }
            },
            onEmergencyCall = {
              context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:112")))
            },
          )
        }
        entry<Settings> {
          SettingsScreen(onBack = { backStack.removeLastOrNull() })
        }
      },
  )
}

private fun android.content.Context.hasLocationPermission(): Boolean =
  ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
    ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
