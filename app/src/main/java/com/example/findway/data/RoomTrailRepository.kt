package com.example.findway.data

import com.example.findway.data.local.BreadcrumbEntity
import com.example.findway.data.local.TrailDao
import com.example.findway.data.local.TrailEntity
import com.example.findway.data.local.toDomain
import com.example.findway.domain.BreadcrumbAcceptancePolicy
import com.example.findway.domain.Trail
import com.example.findway.domain.TrailPoint
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

@Singleton
class RoomTrailRepository @Inject constructor(
  private val trailDao: TrailDao,
  private val acceptancePolicy: BreadcrumbAcceptancePolicy,
) : TrailRepository {
  private val writeMutex = Mutex()

  override fun observeActiveTrail(): Flow<Trail?> = trailDao.observeActiveTrail().map { it?.toDomain() }

  override fun observeSavedTrails(): Flow<List<Trail>> =
    trailDao.observeCompletedTrails().map { trails -> trails.map { it.toDomain() } }

  override fun observeTrail(trailId: Long): Flow<Trail?> = trailDao.observeTrail(trailId).map { it?.toDomain() }

  override suspend fun startTrail(startedAtMillis: Long): Long =
    writeMutex.withLock {
      trailDao.getActiveTrail()?.trail?.id
        ?: trailDao.insertTrail(
          TrailEntity(
            name = "Trail",
            startedAtMillis = startedAtMillis,
          ),
        )
    }

  override suspend fun appendBreadcrumb(point: TrailPoint): Boolean =
    writeMutex.withLock {
      val activeTrail = trailDao.getActiveTrail() ?: return@withLock false
      val orderedBreadcrumbs = activeTrail.breadcrumbs.sortedBy(BreadcrumbEntity::sequence)
      val previous = orderedBreadcrumbs.lastOrNull()?.let { breadcrumb ->
        TrailPoint(
          latitude = breadcrumb.latitude,
          longitude = breadcrumb.longitude,
          accuracyMeters = breadcrumb.accuracyMeters,
          timestampMillis = breadcrumb.timestampMillis,
        )
      }
      if (!acceptancePolicy.shouldAccept(point, previous)) return@withLock false

      trailDao.insertBreadcrumb(
        BreadcrumbEntity(
          trailId = activeTrail.trail.id,
          sequence = orderedBreadcrumbs.size,
          latitude = point.latitude,
          longitude = point.longitude,
          accuracyMeters = requireNotNull(point.accuracyMeters),
          timestampMillis = point.timestampMillis,
        ),
      )
      true
    }

  override suspend fun stopActiveTrail(endedAtMillis: Long): Boolean =
    writeMutex.withLock {
      val activeTrailId = trailDao.getActiveTrail()?.trail?.id ?: return@withLock false
      trailDao.completeTrail(activeTrailId, endedAtMillis) == 1
    }
}
