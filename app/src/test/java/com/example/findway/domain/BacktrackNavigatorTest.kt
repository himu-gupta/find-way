package com.example.findway.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BacktrackNavigatorTest {
  private val route =
    listOf(
      TrailPoint(25.0000, 51.0000),
      TrailPoint(25.0000, 51.0010),
      TrailPoint(25.0010, 51.0010),
      TrailPoint(25.0010, 51.0020),
    )

  @Test
  fun startsByTargetingPreviousRecordedBreadcrumb() {
    val progress = BacktrackNavigator(route).update(route.last())

    assertEquals(2, progress.targetIndex)
    assertEquals(route[2], progress.nextPoint)
    assertFalse(progress.isComplete)
  }

  @Test
  fun reachingBreadcrumbAdvancesToNextOlderBreadcrumb() {
    val navigator = BacktrackNavigator(route)

    val progress = navigator.update(route[2])

    assertEquals(1, progress.targetIndex)
    assertEquals(route[1], progress.nextPoint)
  }

  @Test
  fun reachingStartCompletesBacktrack() {
    val navigator = BacktrackNavigator(route)
    navigator.update(route[2])
    navigator.update(route[1])

    val progress = navigator.update(route[0])

    assertTrue(progress.isComplete)
    assertNull(progress.nextPoint)
    assertEquals(0, progress.remainingDistanceMeters)
  }

  @Test
  fun positionNearRecordedSegmentIsNotMarkedOffRoute() {
    val current = TrailPoint(25.0000, 51.0005)

    val progress = BacktrackNavigator(route).update(current)

    assertFalse(progress.isOffRoute)
  }

  @Test
  fun positionFarFromRecordedSegmentsIsMarkedOffRoute() {
    val current = TrailPoint(25.0030, 51.0030)

    val progress = BacktrackNavigator(route).update(current)

    assertTrue(progress.isOffRoute)
  }
}
