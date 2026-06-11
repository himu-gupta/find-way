package com.example.findway.data

import com.example.findway.domain.Trail
import com.example.findway.domain.TrailPoint
import kotlinx.coroutines.flow.Flow

interface TrailRepository {
  fun observeActiveTrail(): Flow<Trail?>

  fun observeSavedTrails(): Flow<List<Trail>>

  fun observeTrail(trailId: Long): Flow<Trail?>

  suspend fun startTrail(startedAtMillis: Long): Long

  suspend fun appendBreadcrumb(point: TrailPoint): Boolean

  suspend fun stopActiveTrail(endedAtMillis: Long): Boolean
}
