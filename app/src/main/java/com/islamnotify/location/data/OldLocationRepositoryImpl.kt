//package com.islamnotify.location.data
//
//import android.Manifest
//import android.content.Context
//import android.content.SharedPreferences
//import android.content.pm.PackageManager
//import android.location.Address
//import android.location.Geocoder
//import android.location.Location
//import android.location.LocationManager
//import android.os.Looper
//import android.util.Log
//import androidx.core.content.ContextCompat
//import androidx.core.content.edit
//import com.google.android.gms.location.FusedLocationProviderClient
//import com.google.android.gms.location.LocationCallback
//import com.google.android.gms.location.LocationRequest
//import com.google.android.gms.location.Priority
//import com.islamnotify.common.AppUtils.getLocalizedContext
//import com.islamnotify.location.domain.model.LocationData
//import com.islamnotify.location.domain.LocationRepository
//import com.islamnotify.location.domain.model.LocationFailureCause
//import com.islamnotify.location.domain.model.LocationResult
//import dagger.hilt.android.qualifiers.ApplicationContext
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.channels.awaitClose
//import kotlinx.coroutines.delay
//import kotlinx.coroutines.flow.Flow
//import kotlinx.coroutines.flow.callbackFlow
//import kotlinx.coroutines.flow.firstOrNull
//import kotlinx.coroutines.launch
//import kotlinx.coroutines.withContext
//import java.util.Locale
//import javax.inject.Inject
//
//
//private const val LOCATION_TIMEOUT_MS = 30_000L
//private const val TAG = "LocationRepository"
//
//class LocationRepositoryImpl @Inject constructor(
//    @param:ApplicationContext private val context: Context,
//    private val fusedLocationProvider: FusedLocationProviderClient,
//    private val locationDataStore: LocationDataStore
//) : LocationRepository {
//
//    val cacheLocation: SharedPreferences by lazy {
//        context.getSharedPreferences("location_shared_prefs", Context.MODE_PRIVATE)
//    }
//
//    override fun getCurrentLocation(shouldRequestUpdates: Boolean): Flow<LocationResult> =
//        callbackFlow {
//
//            // Check For Permission And Gps Disabled
//            if (ContextCompat.checkSelfPermission(
//                    context,
//                    Manifest.permission.ACCESS_FINE_LOCATION
//                ) != PackageManager.PERMISSION_GRANTED
//                && ContextCompat.checkSelfPermission(
//                    context,
//                    Manifest.permission.ACCESS_COARSE_LOCATION
//                ) != PackageManager.PERMISSION_GRANTED
//            ) {
//                val locationResult: LocationResult =
//                    cachedLocation(LocationFailureCause.PERMISSION_DENIED)
//                trySend(locationResult)
//                Log.e(TAG, "PermissionDenied")
//                close()
//                return@callbackFlow
//            }
//
//
//            if (isGpsDisabled()) {
//                val locationResult: LocationResult =
//                    cachedLocation(LocationFailureCause.GPS_DISABLED)
//                trySend(locationResult)
//                Log.e(TAG, "Gps Disabled")
//                close()
//                return@callbackFlow
//            }
//
//
//            val timeoutJob = launch {
//                delay(LOCATION_TIMEOUT_MS)
//                val result = cachedLocation(LocationFailureCause.GENERIC_ERROR)
//                this@callbackFlow.trySend(result)
//                Log.e(TAG, "getLocation: " + " failed to get location due to timeout")
//                close()
//            }
//
//
//            // location updates request and callback
//            val locationRequest: LocationRequest =
//                LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 0L).setMaxUpdates(1)
//                    .build()
//            val locationCallback: LocationCallback = object : LocationCallback() {
//                override fun onLocationResult(result: com.google.android.gms.location.LocationResult) {
//                    val location = result.lastLocation
//                    timeoutJob.cancel()
//
//                    this@callbackFlow.launch {
//
//                        if (location != null) {
//                            val locationData = processLocation(location)
//                            trySend(LocationResult.Success(locationData))
//                        } else {
//                            val locationData = cachedLocation(LocationFailureCause.GENERIC_ERROR)
//                            trySend(locationData)
//                        }
//
//                        close()
//                    }
//                }
//            }
//
//
//            // fetch last location
//            fusedLocationProvider.lastLocation
//                .addOnSuccessListener { location ->
//                    this@callbackFlow.launch {
//                        if (location != null) {
//                            val locationData = processLocation(location)
//                            trySend(LocationResult.Success(locationData))
//                            timeoutJob.cancel()
//                            close()
//                        } else if (shouldRequestUpdates) {
//                            Log.d(
//                                TAG,
//                                "getLocation: " + "last Location is null, Requesting new update"
//                            )
//                            fusedLocationProvider.requestLocationUpdates(
//                                locationRequest,
//                                locationCallback,
//                                Looper.getMainLooper()
//                            )
//                        } else {
//                            timeoutJob.cancel()
//                            val locationData = cachedLocation(LocationFailureCause.GENERIC_ERROR)
//                            trySend(locationData)
//                        }
//                    }
//                }
//                .addOnFailureListener {
//                    this@callbackFlow.launch {
//                        if (shouldRequestUpdates) {
//                            Log.e(
//                                TAG,
//                                "getLocation: " + "last Location error, Requesting new update"
//                            )
//                            fusedLocationProvider.requestLocationUpdates(
//                                locationRequest,
//                                locationCallback,
//                                Looper.getMainLooper()
//                            )
//                        } else {
//                            timeoutJob.cancel()
//                            val locationData = cachedLocation(LocationFailureCause.GENERIC_ERROR)
//                            trySend(locationData)
//                        }
//                    }
//                }
//
//
//            // cleanup
//            awaitClose {
//                fusedLocationProvider.removeLocationUpdates(locationCallback)
//                timeoutJob.cancel()
//            }
//
//        }
//
//
//    override fun getCachedLocation(): LocationData? {
//        if (!cacheLocation.contains("latitude") || !cacheLocation.contains("longitude")) {
//            return null
//        }
//
//        return LocationData(
//            locationName = cacheLocation.getString("location_name", null),
//            countryCode = cacheLocation.getString("country_code", null),
//            // 3. Convert Long bits back to Double for precision
//            latitude = Double.fromBits(cacheLocation.getLong("latitude", 0L)),
//            longitude = Double.fromBits(cacheLocation.getLong("longitude", 0L)),
//            timestamp = cacheLocation.getLong("timestamp", 0L)
//        )
//    }
//
//    private fun saveCachedLocation(locationData: LocationData) {
//        cacheLocation.edit {
//            putString("location_name", locationData.locationName)
//            putString("country_code", locationData.countryCode)
//            // 3. Store Double as Long bits
//            putLong("latitude", locationData.latitude.toRawBits())
//            putLong("longitude", locationData.longitude.toRawBits())
//            putLong("timestamp", locationData.timestamp)
//        }
//    }
//
//    private suspend fun processLocation(location: Location): LocationData {
//        val locationInfo: LocationInfo = getLocationInfo(
//            latitude = location.latitude,
//            longitude = location.longitude
//        )
//
//        val locationData = LocationData(
//            latitude = location.latitude,
//            longitude = location.longitude,
//            locationName = locationInfo.locationName,
//            countryCode = locationInfo.countryCode,
//            timestamp = System.currentTimeMillis()
//        )
//
//        locationDataStore.saveLocation(locationData)
//        saveCachedLocation(locationData)
//        return locationData
//    }
//
//
//    @Suppress("DEPRECATION")
//    private suspend fun getLocationInfo(latitude: Double, longitude: Double): LocationInfo {
//        return withContext(Dispatchers.IO) {
//            try {
//                val locale: Locale = context.getLocalizedContext().resources.configuration.locales[0]
//                val geocoder = Geocoder(context, locale)
//
//                val addresses = geocoder.getFromLocation(latitude, longitude, 1)
//                if (!addresses.isNullOrEmpty()) {
//                    parseAddress(addresses[0])
//                } else {
//                    Log.e(TAG, "getLocationName: location name is null")
//                    LocationInfo()
//                }
//            } catch (e: Exception) {
//                Log.e(TAG, "getLocationName: ", e)
//                LocationInfo()
//            }
//        }
//    }
//
//    private suspend fun parseAddress(address: Address): LocationInfo {
//        val locationName = address.locality ?: address.subAdminArea ?: address.adminArea
//        val countryCode: String? =
//            address.countryCode ?: locationDataStore.getCountryCode().firstOrNull()
//
//        return LocationInfo(locationName = locationName, countryCode = countryCode)
//    }
//
//    private fun isGpsDisabled(): Boolean {
//        val locationManager: LocationManager =
//            context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
//        val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
//        val isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
//
//        return !isGpsEnabled && !isNetworkEnabled
//    }
//
//
//    private suspend fun cachedLocation(cause: LocationFailureCause): LocationResult {
//        return try {
//            val location = locationDataStore.getLocation().firstOrNull()
//            if (location != null && location.latitude in -90.0..90.0 && location.longitude in -180.0..180.0) {
//                LocationResult.Stale(location, cause)
//            } else {
//                LocationResult.Error(cause)
//            }
//        } catch (e: Exception) {
//            Log.d(TAG, "getLocation: cached location error", e)
//            LocationResult.Error(cause)
//        }
//    }
//
//
//    private data class LocationInfo(
//        val locationName: String? = null,
//        val countryCode: String? = null
//    )
//}
