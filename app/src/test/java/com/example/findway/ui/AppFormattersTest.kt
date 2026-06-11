package com.example.findway.ui

import org.junit.Assert.assertEquals
import org.junit.Test

class AppFormattersTest {
  @Test
  fun formatDistance_usesMetersBelowOneKilometer() {
    assertEquals("842 m", formatDistance(842.9))
  }

  @Test
  fun formatDistance_usesKilometersAtOneKilometer() {
    assertEquals("1.25 km", formatDistance(1_250.0))
  }

  @Test
  fun formatElapsed_includesHoursOnlyWhenNeeded() {
    assertEquals("02:05", formatElapsed(125_000L))
    assertEquals("1:02:05", formatElapsed(3_725_000L))
  }
}
