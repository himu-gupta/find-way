package com.example.findway.di

import android.content.Context
import androidx.room.Room
import com.example.findway.data.RoomTrailRepository
import com.example.findway.data.TrailRepository
import com.example.findway.data.local.FindWayDatabase
import com.example.findway.data.local.TrailDao
import com.example.findway.domain.BreadcrumbAcceptancePolicy
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
  @Binds
  @Singleton
  abstract fun bindTrailRepository(repository: RoomTrailRepository): TrailRepository
}

@Module
@InstallIn(SingletonComponent::class)
object DataModule {
  @Provides
  @Singleton
  fun provideDatabase(@ApplicationContext context: Context): FindWayDatabase =
    Room.databaseBuilder(context, FindWayDatabase::class.java, "find-way.db").build()

  @Provides
  fun provideTrailDao(database: FindWayDatabase): TrailDao = database.trailDao()

  @Provides
  @Singleton
  fun provideBreadcrumbAcceptancePolicy(): BreadcrumbAcceptancePolicy = BreadcrumbAcceptancePolicy()
}
