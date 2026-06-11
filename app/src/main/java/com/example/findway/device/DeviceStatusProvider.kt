package com.example.findway.device

import android.content.Context
import android.os.BatteryManager
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

data class DeviceStatus(
  val batteryPercent: Int?,
  val availableStorageBytes: Long,
)

@Singleton
class DeviceStatusProvider @Inject constructor(
  @param:ApplicationContext private val context: Context,
) {
  fun currentStatus(): DeviceStatus {
    val batteryManager = context.getSystemService(BatteryManager::class.java)
    val batteryPercent = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY).takeIf { it in 0..100 }
    return DeviceStatus(
      batteryPercent = batteryPercent,
      availableStorageBytes = File(context.filesDir.absolutePath).usableSpace,
    )
  }
}
