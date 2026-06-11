package com.example.findway.domain

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

fun distanceMeters(from: TrailPoint, to: TrailPoint): Double {
  val earthRadiusMeters = 6_371_000.0
  val fromLat = Math.toRadians(from.latitude)
  val toLat = Math.toRadians(to.latitude)
  val deltaLat = Math.toRadians(to.latitude - from.latitude)
  val deltaLon = Math.toRadians(to.longitude - from.longitude)
  val a =
    sin(deltaLat / 2) * sin(deltaLat / 2) +
      cos(fromLat) * cos(toLat) * sin(deltaLon / 2) * sin(deltaLon / 2)
  return earthRadiusMeters * 2 * atan2(sqrt(a), sqrt(1 - a))
}

fun initialBearingDegrees(from: TrailPoint, to: TrailPoint): Double {
  val fromLat = Math.toRadians(from.latitude)
  val toLat = Math.toRadians(to.latitude)
  val deltaLon = Math.toRadians(to.longitude - from.longitude)
  val y = sin(deltaLon) * cos(toLat)
  val x = cos(fromLat) * sin(toLat) - sin(fromLat) * cos(toLat) * cos(deltaLon)
  return (Math.toDegrees(atan2(y, x)) + 360.0) % 360.0
}
