package com.example.findway.ui.screens

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.example.findway.theme.FindWayTheme
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class TrackingScreenTest {
  @get:Rule val composeTestRule = createAndroidComposeRule<ComponentActivity>()

  @Before
  fun setup() {
    composeTestRule.setContent {
      FindWayTheme {
        TrackingScreen(onBack = {}, onTakeMeBack = {}, onOpenSos = {})
      }
    }
  }

  @Test
  fun trackingScreen_showsRecordedBreadcrumbState() {
    composeTestRule.onNodeWithText("Live breadcrumb trail").assertExists()
    composeTestRule.onNodeWithText("186 points recorded").assertExists()
    composeTestRule.onNode(hasContentDescription("Recorded breadcrumb route with 10 points")).assertExists()
    composeTestRule.onNodeWithText("Take Me Back").assertExists()
  }
}
