package com.example.findway.ui.model

import com.example.findway.domain.TrailPoint

data class ReadinessItem(
  val label: String,
  val value: String,
  val isReady: Boolean,
)

data class TrailMapState(
  val breadcrumbs: List<TrailPoint>,
  val distanceLabel: String,
  val accuracyLabel: String,
  val breadcrumbCount: Int,
)
