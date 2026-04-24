package com.islamnotify.location.domain.model

data class LocationData(
    val latitude: Double,
    val longitude: Double,
    val locationName: String?,
    val countryCode: String?,
    val timestamp: Long
)
