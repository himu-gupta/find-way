package com.example.findway.ui.screens

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.example.findway.theme.FindWayTheme
import com.example.findway.domain.TrailPoint
import com.example.findway.ui.model.TrackingUiState
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class TrackingScreenTest {
  @get:Rule val composeTestRule = createAndroidComposeRule<ComponentActivity>()

  @Before
  fun setup() {
    composeTestRule.setContent {
      FindWayTheme {
        TrackingScreen(
          state =
            TrackingUiState(
              isRecording = true,
              breadcrumbs =
                listOf(
                  TrailPoint(25.0, 51.0, 5f, 1_000L),
                  TrailPoint(25.001, 51.001, 6f, 6_000L),
                ),
              elapsedMillis = 42_000L,
              distanceMeters = 150.0,
              accuracyMeters = 6f,
            ),
          onBack = {},
          onTakeMeBack = {},
          onOpenSos = {},
          onStop = {},
        )
      }
    }
  }

  @Test
  fun trackingScreen_showsRecordedBreadcrumbState() {
    composeTestRule.onNodeWithText("Live breadcrumb trail").assertExists()
    composeTestRule.onNodeWithText("2 points recorded").assertExists()
    composeTestRule.onNode(hasContentDescription("Recorded breadcrumb route with 2 points")).assertExists()
    composeTestRule.onNodeWithText("Take Me Back").assertExists()
  }
}
