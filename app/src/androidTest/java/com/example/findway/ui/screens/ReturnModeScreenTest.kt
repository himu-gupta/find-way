package com.example.findway.ui.screens

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.example.findway.theme.FindWayTheme
import com.example.findway.domain.TrailPoint
import com.example.findway.ui.model.ReturnUiState
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class ReturnModeScreenTest {
  @get:Rule val composeTestRule = createAndroidComposeRule<ComponentActivity>()

  @Before
  fun setup() {
    composeTestRule.setContent {
      FindWayTheme(dynamicColor = false) {
        ReturnModeScreen(
          state =
            ReturnUiState(
              breadcrumbs = listOf(TrailPoint(25.0, 51.0), TrailPoint(25.001, 51.001)),
              deviceHeadingDegrees = 0f,
              targetBearingDegrees = 42f,
              distanceToNextMeters = 120,
              remainingDistanceMeters = 5_600,
              accuracyMeters = 6f,
            ),
          onBack = {},
          onOpenSos = {},
        )
      }
    }
  }

  @Test
  fun returnMode_showsDirectionalRetraceGuidance() {
    composeTestRule.onNodeWithText("Follow the arrow").assertExists()
    composeTestRule.onNodeWithText("120 m to next breadcrumb").assertExists()
    composeTestRule.onNode(hasContentDescription("Direction arrow pointing NE at 42 degrees")).assertExists()
  }
}
