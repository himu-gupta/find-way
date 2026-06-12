package com.example.findway.location

import com.example.findway.domain.TrailPoint
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@Singleton
class CurrentLocationStore @Inject constructor() {
  private val mutableLocation = MutableStateFlow<TrailPoint?>(null)

  val location: StateFlow<TrailPoint?> = mutableLocation.asStateFlow()

  fun update(point: TrailPoint) {
    mutableLocation.value = point
  }
}
