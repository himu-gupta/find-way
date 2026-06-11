package com.example.findway

import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.dropUnlessResumed
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
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

  NavDisplay(
    backStack = backStack,
    onBack = { backStack.removeLastOrNull() },
    entryProvider =
      entryProvider {
        entry<Home> {
          HomeScreen(
            onStartTrail = dropUnlessResumed { backStack.add(Tracking) },
            onOpenSavedTrails = dropUnlessResumed { backStack.add(SavedTrails) },
            onOpenSos = dropUnlessResumed { backStack.add(Sos) },
            onOpenSettings = dropUnlessResumed { backStack.add(Settings) },
          )
        }
        entry<Tracking> {
          TrackingScreen(
            onBack = { backStack.removeLastOrNull() },
            onTakeMeBack = dropUnlessResumed { backStack.add(ReturnMode) },
            onOpenSos = dropUnlessResumed { backStack.add(Sos) },
          )
        }
        entry<ReturnMode> {
          ReturnModeScreen(onBack = { backStack.removeLastOrNull() }, onOpenSos = dropUnlessResumed { backStack.add(Sos) })
        }
        entry<SavedTrails> {
          SavedTrailsScreen(
            onBack = { backStack.removeLastOrNull() },
            onOpenTrail = { trailId -> backStack.add(TrailDetail(trailId)) },
          )
        }
        entry<TrailDetail> { route ->
          TrailDetailScreen(
            trailId = route.trailId,
            onBack = { backStack.removeLastOrNull() },
            onRetrace = dropUnlessResumed { backStack.add(ReturnMode) },
          )
        }
        entry<Sos> {
          SosScreen(onBack = { backStack.removeLastOrNull() })
        }
        entry<Settings> {
          SettingsScreen(onBack = { backStack.removeLastOrNull() })
        }
      },
  )
}
