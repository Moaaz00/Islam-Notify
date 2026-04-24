package com.islamnotify.location.domain.model

sealed interface LocationResult {
    data class Success(val locationData: LocationData): LocationResult
    data class Stale(val locationData: LocationData, val failureCause: LocationFailureCause): LocationResult
    data class Error(val failureCause: LocationFailureCause): LocationResult
}