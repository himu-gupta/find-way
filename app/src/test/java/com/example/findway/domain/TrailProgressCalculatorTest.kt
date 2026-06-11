package com.example.findway.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TrailProgressCalculatorTest {
  private val calculator = TrailProgressCalculator(offRouteThresholdMeters = 35.0)

  @Test
  fun calculateReturnProgress_emptyRoute_hasNoNextPoint() {
    val progress =
      calculator.calculateReturnProgress(
        currentLocation = TrailPoint(latitude = 25.0, longitude = 51.0),
        recordedRoute = emptyList(),
      )

    assertEquals(null, progress.nextPoint)
    assertEquals(0, progress.remainingDistanceMeters)
    assertFalse(progress.isOffRoute)
  }

  @Test
  fun calculateReturnProgress_fromEndOfRecordedRoute_targetsPreviousBreadcrumb() {
    val start = TrailPoint(latitude = 25.0000, longitude = 51.0000)
    val middle = TrailPoint(latitude = 25.0000, longitude = 51.0010)
    val end = TrailPoint(latitude = 25.0000, longitude = 51.0020)

    val progress =
      calculator.calculateReturnProgress(
        currentLocation = end,
        recordedRoute = listOf(start, middle, end),
      )

    assertNotNull(progress.nextPoint)
    assertEquals(middle, progress.nextPoint)
    assertTrue(progress.remainingDistanceMeters in 190..220)
    assertFalse(progress.isOffRoute)
  }

  @Test
  fun calculateReturnProgress_whenFarFromRoute_marksOffRoute() {
    val route =
      listOf(
        TrailPoint(latitude = 25.0000, longitude = 51.0000),
        TrailPoint(latitude = 25.0000, longitude = 51.0010),
      )

    val progress =
      calculator.calculateReturnProgress(
        currentLocation = TrailPoint(latitude = 25.0020, longitude = 51.0010),
        recordedRoute = route,
      )

    assertTrue(progress.offRouteDistanceMeters > 35)
    assertTrue(progress.isOffRoute)
  }
}
