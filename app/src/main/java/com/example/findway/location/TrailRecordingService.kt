package com.example.findway.location

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import com.example.findway.MainActivity
import com.example.findway.R
import com.example.findway.data.TrailRepository
import com.example.findway.domain.TrailPoint
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TrailRecordingService : Service() {
  @Inject lateinit var fusedLocationClient: FusedLocationProviderClient
  @Inject lateinit var trailRepository: TrailRepository

  private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
  private var recordingJob: Job? = null
  private var requestingUpdates = false

  private val locationRequest =
    LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, LOCATION_INTERVAL_MILLIS)
      .setMinUpdateIntervalMillis(MIN_LOCATION_INTERVAL_MILLIS)
      .setMinUpdateDistanceMeters(MIN_LOCATION_DISTANCE_METERS)
      .build()

  private val locationCallback =
    object : LocationCallback() {
      override fun onLocationResult(result: LocationResult) {
        result.locations.forEach { location ->
          serviceScope.launch {
            trailRepository.appendBreadcrumb(
              TrailPoint(
                latitude = location.latitude,
                longitude = location.longitude,
                accuracyMeters = location.accuracy,
                timestampMillis = location.time,
              ),
            )
          }
        }
      }
    }

  override fun onCreate() {
    super.onCreate()
    createNotificationChannel()
  }

  override fun onStartCommand(
    intent: Intent?,
    flags: Int,
    startId: Int,
  ): Int {
    when (intent?.action) {
      ACTION_STOP -> stopRecording()
      else -> startRecording()
    }
    return START_STICKY
  }

  override fun onBind(intent: Intent?): IBinder? = null

  override fun onDestroy() {
    fusedLocationClient.removeLocationUpdates(locationCallback)
    serviceScope.cancel()
    super.onDestroy()
  }

  private fun startRecording() {
    if (recordingJob?.isActive == true || requestingUpdates) return
    startInForeground()
    recordingJob =
      serviceScope.launch {
        trailRepository.startTrail(System.currentTimeMillis())
        requestLocationUpdates()
      }
  }

  private fun requestLocationUpdates() {
    val fineGranted =
      ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    val coarseGranted =
      ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
    if (!fineGranted && !coarseGranted) {
      stopSelf()
      return
    }

    requestingUpdates = true
    fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
  }

  private fun stopRecording() {
    requestingUpdates = false
    fusedLocationClient.removeLocationUpdates(locationCallback)
    recordingJob?.cancel()
    recordingJob =
      serviceScope.launch {
        trailRepository.stopActiveTrail(System.currentTimeMillis())
        ServiceCompat.stopForeground(this@TrailRecordingService, ServiceCompat.STOP_FOREGROUND_REMOVE)
        stopSelf()
      }
  }

  private fun startInForeground() {
    val openAppIntent =
      PendingIntent.getActivity(
        this,
        0,
        Intent(this, MainActivity::class.java),
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
      )
    val stopIntent =
      PendingIntent.getService(
        this,
        1,
        Intent(this, TrailRecordingService::class.java).setAction(ACTION_STOP),
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
      )
    val notification =
      NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
        .setSmallIcon(R.drawable.ic_stat_location)
        .setContentTitle(getString(R.string.recording_notification_title))
        .setContentText(getString(R.string.recording_notification_body))
        .setContentIntent(openAppIntent)
        .setOngoing(true)
        .setOnlyAlertOnce(true)
        .addAction(0, getString(R.string.stop_tracking), stopIntent)
        .build()

    ServiceCompat.startForeground(
      this,
      NOTIFICATION_ID,
      notification,
      ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION,
    )
  }

  private fun createNotificationChannel() {
    val channel =
      NotificationChannel(
        NOTIFICATION_CHANNEL_ID,
        getString(R.string.recording_channel_name),
        NotificationManager.IMPORTANCE_LOW,
      ).apply {
        description = getString(R.string.recording_channel_description)
      }
    getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
  }

  companion object {
    const val ACTION_START = "com.example.findway.action.START_RECORDING"
    const val ACTION_STOP = "com.example.findway.action.STOP_RECORDING"

    private const val NOTIFICATION_CHANNEL_ID = "trail_recording"
    private const val NOTIFICATION_ID = 1001
    private const val LOCATION_INTERVAL_MILLIS = 5_000L
    private const val MIN_LOCATION_INTERVAL_MILLIS = 2_000L
    private const val MIN_LOCATION_DISTANCE_METERS = 2f
  }
}
