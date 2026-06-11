package com.example.findway.domain

class BreadcrumbAcceptancePolicy(
  private val maximumAccuracyMeters: Float = 50f,
  private val minimumDistanceMeters: Double = 3.0,
  private val maximumStationaryIntervalMillis: Long = 15_000L,
) {
  fun shouldAccept(
    candidate: TrailPoint,
    previous: TrailPoint?,
  ): Boolean {
    val accuracy = candidate.accuracyMeters ?: return false
    if (accuracy > maximumAccuracyMeters) return false
    if (previous == null) return true

    val movedEnough = distanceMeters(previous, candidate) >= minimumDistanceMeters
    val waitedLongEnough = candidate.timestampMillis - previous.timestampMillis >= maximumStationaryIntervalMillis
    return movedEnough || waitedLongEnough
  }
}
