package com.example.findway.location

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.PI
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate

@Singleton
class HeadingProvider @Inject constructor(
  @ApplicationContext context: Context,
) {
  private val sensorManager = context.getSystemService(SensorManager::class.java)
  private val rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)

  val headings: Flow<Float?> =
    callbackFlow {
      if (rotationSensor == null) {
        trySend(null)
        close()
        return@callbackFlow
      }

      val listener =
        object : SensorEventListener {
          override fun onSensorChanged(event: SensorEvent) {
            val rotationMatrix = FloatArray(9)
            val orientation = FloatArray(3)
            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
            SensorManager.getOrientation(rotationMatrix, orientation)
            val heading = ((orientation[0] * 180f / PI.toFloat()) + 360f) % 360f
            trySend(heading)
          }

          override fun onAccuracyChanged(
            sensor: Sensor?,
            accuracy: Int,
          ) = Unit
        }

      sensorManager.registerListener(listener, rotationSensor, SensorManager.SENSOR_DELAY_UI)
      awaitClose { sensorManager.unregisterListener(listener) }
    }.conflate()
}
