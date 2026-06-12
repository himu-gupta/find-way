package com.example.findway.ui.screens

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollToIndex
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
              breadcrumbs =
                listOf(
                  TrailPoint(25.0, 51.0),
                  TrailPoint(25.001, 51.001),
                  TrailPoint(25.002, 51.002),
                ),
              currentLocation = TrailPoint(25.002, 51.002, accuracyMeters = 6f),
              deviceHeadingDegrees = 0f,
              targetBearingDegrees = 42f,
              targetBreadcrumbIndex = 1,
              totalBreadcrumbs = 3,
              distanceToNextMeters = 120,
              remainingDistanceMeters = 5_600,
              accuracyMeters = 6f,
              isBacktracking = true,
            ),
          onBack = {},
          onOpenSos = {},
          onFinish = {},
        )
      }
    }
  }

  @Test
  fun returnMode_showsDirectionalRetraceGuidance() {
    composeTestRule.onNodeWithText("Follow the recorded route").assertExists()
    composeTestRule.onNodeWithText("Next breadcrumb 2 of 3").assertExists()
    composeTestRule.onNodeWithText("120 m to the next saved point").assertExists()
    composeTestRule.onNode(hasContentDescription("Backtrack arrow to breadcrumb 2 of 3")).assertExists()
    composeTestRule.onNode(hasScrollAction()).performScrollToIndex(2)
    composeTestRule.onNode(hasContentDescription("Backtrack route targeting breadcrumb 2 of 3")).assertExists()
  }
}
