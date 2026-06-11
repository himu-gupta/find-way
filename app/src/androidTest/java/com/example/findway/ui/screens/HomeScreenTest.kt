package com.example.findway.ui.screens

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.example.findway.theme.FindWayTheme
import com.example.findway.ui.model.HomeUiState
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class HomeScreenTest {
  @get:Rule val composeTestRule = createAndroidComposeRule<ComponentActivity>()

  @Before
  fun setup() {
    composeTestRule.setContent {
      FindWayTheme(dynamicColor = false) {
        HomeScreen(
          state = HomeUiState(batteryPercent = 82, availableStorageBytes = 2L * 1024 * 1024 * 1024),
          hasLocationPermission = true,
          onStartTrail = {},
          onOpenSavedTrails = {},
          onOpenSos = {},
          onOpenSettings = {},
        )
      }
    }
  }

  @Test
  fun homeScreen_showsPrimarySafetyActions() {
    composeTestRule.onNodeWithText("Trail readiness").assertExists()
    composeTestRule.onNodeWithText("Location access").assertExists()
    composeTestRule.onNodeWithText("Start Trail").assertExists()
    composeTestRule.onNodeWithText("Saved Trails").assertExists()
    composeTestRule.onNodeWithText("SOS").assertExists()
  }
}
