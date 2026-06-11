package com.example.findway.location

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TrailRecordingController @Inject constructor(
  @param:ApplicationContext private val context: Context,
) {
  fun start() {
    ContextCompat.startForegroundService(
      context,
      Intent(context, TrailRecordingService::class.java).setAction(TrailRecordingService.ACTION_START),
    )
  }

  fun stop() {
    context.startService(
      Intent(context, TrailRecordingService::class.java).setAction(TrailRecordingService.ACTION_STOP),
    )
  }
}
