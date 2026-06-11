package com.example.findway

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable data object Home : NavKey

@Serializable data object Tracking : NavKey

@Serializable data object ReturnMode : NavKey

@Serializable data object SavedTrails : NavKey

@Serializable data class TrailDetail(val trailId: Long) : NavKey

@Serializable data object Sos : NavKey

@Serializable data object Settings : NavKey
