package com.example.findway.domain

import kotlin.math.min
import kotlin.math.roundToInt

data class TrailPoint(
  val latitude: Double,
  val longitude: Double,
  val accuracyMeters: Float? = null,
  val timestampMillis: Long = 0L,
)

data class ReturnProgress(
  val nextPoint: TrailPoint?,
  val distanceToNextMeters: Int,
  val bearingToNextDegrees: Int?,
  val remainingDistanceMeters: Int,
  val offRouteDistanceMeters: Int,
  val isOffRoute: Boolean,
)

class TrailProgressCalculator(
  private val offRouteThresholdMeters: Double = 35.0,
) {
  fun calculateReturnProgress(
    currentLocation: TrailPoint,
    recordedRoute: List<TrailPoint>,
  ): ReturnProgress {
    if (recordedRoute.isEmpty()) {
      return ReturnProgress(
        nextPoint = null,
        distanceToNextMeters = 0,
        bearingToNextDegrees = null,
        remainingDistanceMeters = 0,
        offRouteDistanceMeters = 0,
        isOffRoute = false,
      )
    }

    val returnRoute = recordedRoute.asReversed()
    val nearestIndex =
      returnRoute.indices.minBy { index ->
        distanceMeters(currentLocation, returnRoute[index])
      }
    val nextIndex = min(nearestIndex + 1, returnRoute.lastIndex)
    val nextPoint = returnRoute[nextIndex]
    val offRouteDistance = distanceMeters(currentLocation, returnRoute[nearestIndex])
    val remainingDistance =
      distanceMeters(currentLocation, nextPoint) +
        returnRoute.drop(nextIndex).zipWithNext().sumOf { (from, to) -> distanceMeters(from, to) }

    return ReturnProgress(
      nextPoint = nextPoint,
      distanceToNextMeters = distanceMeters(currentLocation, nextPoint).roundToInt(),
      bearingToNextDegrees = initialBearingDegrees(currentLocation, nextPoint).roundToInt() % 360,
      remainingDistanceMeters = remainingDistance.roundToInt(),
      offRouteDistanceMeters = offRouteDistance.roundToInt(),
      isOffRoute = offRouteDistance > offRouteThresholdMeters,
    )
  }

}
