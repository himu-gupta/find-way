package com.example.findway.ui.screens

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.example.findway.theme.FindWayTheme
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
    composeTestRule.onNodeWithText("Precise location").assertExists()
    composeTestRule.onNodeWithText("Start Trail").assertExists()
    composeTestRule.onNodeWithText("Saved Trails").assertExists()
    composeTestRule.onNodeWithText("SOS").assertExists()
  }
}
