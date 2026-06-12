package com.example.findway.domain

import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

data class BacktrackProgress(
  val nextPoint: TrailPoint?,
  val targetIndex: Int?,
  val totalBreadcrumbs: Int,
  val distanceToNextMeters: Int,
  val bearingToNextDegrees: Int?,
  val remainingDistanceMeters: Int,
  val offRouteDistanceMeters: Int,
  val isOffRoute: Boolean,
  val isComplete: Boolean,
)

class BacktrackNavigator(
  route: List<TrailPoint>,
  private val reachedThresholdMeters: Double = 10.0,
  private val offRouteThresholdMeters: Double = 35.0,
) {
  val route: List<TrailPoint> = route.toList()
  private var targetIndex = route.lastIndex - 1

  fun update(currentLocation: TrailPoint): BacktrackProgress {
    if (route.size < 2) return emptyProgress()

    while (targetIndex > 0 && distanceMeters(currentLocation, route[targetIndex]) <= reachedThresholdMeters) {
      targetIndex--
    }

    val reachedStart = targetIndex == 0 && distanceMeters(currentLocation, route.first()) <= reachedThresholdMeters
    if (reachedStart) {
      return BacktrackProgress(
        nextPoint = null,
        targetIndex = null,
        totalBreadcrumbs = route.size,
        distanceToNextMeters = 0,
        bearingToNextDegrees = null,
        remainingDistanceMeters = 0,
        offRouteDistanceMeters = 0,
        isOffRoute = false,
        isComplete = true,
      )
    }

    val nextPoint = route[targetIndex]
    val remainingRoute = route.subList(0, targetIndex + 1)
    val remainingDistance =
      distanceMeters(currentLocation, nextPoint) + remainingRoute.zipWithNext().sumOf { (from, to) -> distanceMeters(from, to) }
    val relevantRoute = route.subList(0, min(targetIndex + 2, route.size))
    val offRouteDistance = distanceToRouteMeters(currentLocation, relevantRoute)

    return BacktrackProgress(
      nextPoint = nextPoint,
      targetIndex = targetIndex,
      totalBreadcrumbs = route.size,
      distanceToNextMeters = distanceMeters(currentLocation, nextPoint).roundToInt(),
      bearingToNextDegrees = initialBearingDegrees(currentLocation, nextPoint).roundToInt() % 360,
      remainingDistanceMeters = remainingDistance.roundToInt(),
      offRouteDistanceMeters = offRouteDistance.roundToInt(),
      isOffRoute = offRouteDistance > offRouteThresholdMeters,
      isComplete = false,
    )
  }

  private fun emptyProgress() =
    BacktrackProgress(
      nextPoint = null,
      targetIndex = null,
      totalBreadcrumbs = route.size,
      distanceToNextMeters = 0,
      bearingToNextDegrees = null,
      remainingDistanceMeters = 0,
      offRouteDistanceMeters = 0,
      isOffRoute = false,
      isComplete = false,
    )
}

private fun distanceToRouteMeters(
  point: TrailPoint,
  route: List<TrailPoint>,
): Double {
  if (route.isEmpty()) return 0.0
  if (route.size == 1) return distanceMeters(point, route.first())
  return route.zipWithNext().minOf { (start, end) -> distanceToSegmentMeters(point, start, end) }
}

private fun distanceToSegmentMeters(
  point: TrailPoint,
  start: TrailPoint,
  end: TrailPoint,
): Double {
  val latitudeScale = 111_320.0
  val longitudeScale = latitudeScale * cos(Math.toRadians(point.latitude))
  val pointX = point.longitude * longitudeScale
  val pointY = point.latitude * latitudeScale
  val startX = start.longitude * longitudeScale
  val startY = start.latitude * latitudeScale
  val endX = end.longitude * longitudeScale
  val endY = end.latitude * latitudeScale
  val segmentX = endX - startX
  val segmentY = endY - startY
  val lengthSquared = segmentX * segmentX + segmentY * segmentY
  if (lengthSquared == 0.0) return distanceMeters(point, start)

  val projection = ((pointX - startX) * segmentX + (pointY - startY) * segmentY) / lengthSquared
  val clampedProjection = max(0.0, min(1.0, projection))
  val closestX = startX + clampedProjection * segmentX
  val closestY = startY + clampedProjection * segmentY
  val deltaX = pointX - closestX
  val deltaY = pointY - closestY
  return kotlin.math.sqrt(deltaX * deltaX + deltaY * deltaY)
}
