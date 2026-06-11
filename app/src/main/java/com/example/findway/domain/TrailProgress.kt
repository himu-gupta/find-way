package com.example.findway.domain

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

data class TrailPoint(
  val latitude: Double,
  val longitude: Double,
  val accuracyMeters: Float? = null,
  val timestampMillis: Long = 0L,
)

data class ReturnProgress(
  val nextPoint: TrailPoint?,
  val distanceToNextMeters: Int,
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
      remainingDistanceMeters = remainingDistance.roundToInt(),
      offRouteDistanceMeters = offRouteDistance.roundToInt(),
      isOffRoute = offRouteDistance > offRouteThresholdMeters,
    )
  }

  private fun distanceMeters(from: TrailPoint, to: TrailPoint): Double {
    val earthRadiusMeters = 6_371_000.0
    val fromLat = Math.toRadians(from.latitude)
    val toLat = Math.toRadians(to.latitude)
    val deltaLat = Math.toRadians(to.latitude - from.latitude)
    val deltaLon = Math.toRadians(to.longitude - from.longitude)
    val a =
      sin(deltaLat / 2) * sin(deltaLat / 2) +
        cos(fromLat) * cos(toLat) * sin(deltaLon / 2) * sin(deltaLon / 2)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))
    return earthRadiusMeters * c
  }
}
