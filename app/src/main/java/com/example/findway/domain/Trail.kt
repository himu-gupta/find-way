package com.example.findway.domain

data class TrailPoint(
  val latitude: Double,
  val longitude: Double,
  val accuracyMeters: Float? = null,
  val timestampMillis: Long = 0L,
)

data class Trail(
  val id: Long,
  val name: String,
  val startedAtMillis: Long,
  val endedAtMillis: Long?,
  val breadcrumbs: List<TrailPoint>,
) {
  val isActive: Boolean
    get() = endedAtMillis == null
}

fun routeDistanceMeters(points: List<TrailPoint>): Double =
  points.zipWithNext().sumOf { (from, to) -> distanceMeters(from, to) }
