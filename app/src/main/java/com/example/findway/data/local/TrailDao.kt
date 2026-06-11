package com.example.findway.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface TrailDao {
  @Transaction
  @Query("SELECT * FROM trails WHERE endedAtMillis IS NULL ORDER BY startedAtMillis DESC LIMIT 1")
  fun observeActiveTrail(): Flow<TrailWithBreadcrumbs?>

  @Transaction
  @Query("SELECT * FROM trails WHERE endedAtMillis IS NOT NULL ORDER BY startedAtMillis DESC")
  fun observeCompletedTrails(): Flow<List<TrailWithBreadcrumbs>>

  @Transaction
  @Query("SELECT * FROM trails WHERE id = :trailId LIMIT 1")
  fun observeTrail(trailId: Long): Flow<TrailWithBreadcrumbs?>

  @Transaction
  @Query("SELECT * FROM trails WHERE endedAtMillis IS NULL ORDER BY startedAtMillis DESC LIMIT 1")
  suspend fun getActiveTrail(): TrailWithBreadcrumbs?

  @Insert
  suspend fun insertTrail(trail: TrailEntity): Long

  @Insert
  suspend fun insertBreadcrumb(breadcrumb: BreadcrumbEntity)

  @Query("UPDATE trails SET endedAtMillis = :endedAtMillis WHERE id = :trailId AND endedAtMillis IS NULL")
  suspend fun completeTrail(
    trailId: Long,
    endedAtMillis: Long,
  ): Int
}
