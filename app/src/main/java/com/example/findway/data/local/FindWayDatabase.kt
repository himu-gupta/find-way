package com.example.findway.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
  entities = [TrailEntity::class, BreadcrumbEntity::class],
  version = 1,
  exportSchema = false,
)
abstract class FindWayDatabase : RoomDatabase() {
  abstract fun trailDao(): TrailDao
}
