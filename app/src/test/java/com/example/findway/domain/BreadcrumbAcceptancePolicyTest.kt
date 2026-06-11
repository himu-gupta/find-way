package com.example.findway.domain

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BreadcrumbAcceptancePolicyTest {
  private val policy = BreadcrumbAcceptancePolicy()

  @Test
  fun firstAccuratePoint_isAccepted() {
    assertTrue(policy.shouldAccept(point(accuracy = 8f), previous = null))
  }

  @Test
  fun inaccuratePoint_isRejected() {
    assertFalse(policy.shouldAccept(point(accuracy = 80f), previous = null))
  }

  @Test
  fun nearbyPointArrivingImmediately_isRejected() {
    val previous = point(latitude = 25.0, timestamp = 1_000L)
    val candidate = point(latitude = 25.000001, timestamp = 2_000L)

    assertFalse(policy.shouldAccept(candidate, previous))
  }

  @Test
  fun stationaryPointAfterInterval_isAccepted() {
    val previous = point(latitude = 25.0, timestamp = 1_000L)
    val candidate = point(latitude = 25.000001, timestamp = 16_000L)

    assertTrue(policy.shouldAccept(candidate, previous))
  }

  private fun point(
    latitude: Double = 25.0,
    accuracy: Float = 8f,
    timestamp: Long = 1_000L,
  ) =
    TrailPoint(
      latitude = latitude,
      longitude = 51.0,
      accuracyMeters = accuracy,
      timestampMillis = timestamp,
    )
}
