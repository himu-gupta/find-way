package com.example.findway.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TrailDaoTest {
  private lateinit var database: FindWayDatabase
  private lateinit var dao: TrailDao

  @Before
  fun createDatabase() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    database = Room.inMemoryDatabaseBuilder(context, FindWayDatabase::class.java).build()
    dao = database.trailDao()
  }

  @After
  fun closeDatabase() {
    database.close()
  }

  @Test
  fun activeTrailPersistsOrderedBreadcrumbsAndMovesToSavedTrails() = runTest {
    val trailId = dao.insertTrail(TrailEntity(name = "Test trail", startedAtMillis = 1_000L))
    dao.insertBreadcrumb(
      BreadcrumbEntity(
        trailId = trailId,
        sequence = 1,
        latitude = 25.2,
        longitude = 51.2,
        accuracyMeters = 6f,
        timestampMillis = 3_000L,
      ),
    )
    dao.insertBreadcrumb(
      BreadcrumbEntity(
        trailId = trailId,
        sequence = 0,
        latitude = 25.1,
        longitude = 51.1,
        accuracyMeters = 5f,
        timestampMillis = 2_000L,
      ),
    )

    val active = dao.observeActiveTrail().first()
    assertEquals(trailId, active?.trail?.id)
    assertEquals(listOf(0, 1), active?.breadcrumbs?.sortedBy { it.sequence }?.map { it.sequence })

    assertEquals(1, dao.completeTrail(trailId, endedAtMillis = 4_000L))
    assertNull(dao.observeActiveTrail().first())
    assertEquals(trailId, dao.observeCompletedTrails().first().single().trail.id)
  }
}
