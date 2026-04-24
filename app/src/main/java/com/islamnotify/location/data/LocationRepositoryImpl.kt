package com.islamnotify.location.data

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.LocationManager
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.islamnotify.common.AppUtils.getLocalizedContext
import com.islamnotify.location.domain.LocationRepository
import com.islamnotify.location.domain.model.LocationData
import com.islamnotify.location.domain.model.LocationFailureCause
import com.islamnotify.location.domain.model.LocationResult
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import javax.inject.Inject
import kotlin.coroutines.resume

class LocationRepositoryImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val fusedLocationProvider: FusedLocationProviderClient,
    private val locationDataStore: LocationDataStore
) : LocationRepository {

    companion object {
        const val TAG = "LocationRepository"
    }


    override suspend fun getLastKnownLocation(): LocationResult {
        Log.d(TAG, "getLastKnownLocation: called")
        val error = checkLocationRequirements()
        if (error != null) return getCachedLocation(error)

        val lastLocation = try {
            fusedLocationProvider.lastLocation.await()
        } catch (e: Exception) {
            Log.w(TAG, "getLastKnownLocation: last location is null", e)
            null
        }

        return if (lastLocation != null) {
            Log.d(TAG, "getLastKnownLocation: last location fetching success")
            processNewLocation(lastLocation.latitude, lastLocation.longitude)
        } else {
            getCachedLocation(LocationFailureCause.GENERIC_ERROR)
        }
    }


    override suspend fun getCurrentLocation(): LocationResult {
        Log.d(TAG, "getCurrentLocation: called")
        val error = checkLocationRequirements()
        if (error != null) return getCachedLocation(error)

        // Try last location first for speed
        val lastLocation = try {
            fusedLocationProvider.lastLocation.await()
        } catch (e: Exception) {
            Log.w(TAG, "getCurrentLocation: location is null", e)
            null
        }

        if (lastLocation != null) {
            Log.d(TAG, "getCurrentLocation: last location fetching success")
            return processNewLocation(lastLocation.latitude, lastLocation.longitude)
        }

        // Otherwise, request a fresh one
        val newLocationFetch = getNewLocation()
        val latitude = newLocationFetch.latitude
        val longitude = newLocationFetch.longitude

        return if (latitude != null && longitude != null) {
            Log.d(TAG, "getCurrentLocation: current location fetching success")
            processNewLocation(latitude, longitude)
        } else {
            Log.w(TAG, "getCurrentLocation: current location fetching failed")
            getCachedLocation(newLocationFetch.failureCause ?: LocationFailureCause.GENERIC_ERROR)
        }
    }


    override suspend fun getCachedLocation(): LocationData? {
        return locationDataStore.getLocation().first()
    }


    private suspend fun getNewLocation(): LocationFetchResult {
        val cts = CancellationTokenSource()
        return try {
            withTimeout(20_000L) {

                val location = fusedLocationProvider.getCurrentLocation(
                    Priority.PRIORITY_HIGH_ACCURACY,
                    cts.token
                ).await()

                Log.d(TAG, "getCurrentLocation: new location fetched successfully")
                LocationFetchResult(location?.latitude, location?.longitude, null)
            }
        } catch (_: TimeoutCancellationException) {
            Log.e(TAG, "getNewLocation(): failed to get the location ue to timeout")
            LocationFetchResult(null, null, LocationFailureCause.GENERIC_ERROR)
        } catch (e: Exception) {
            Log.e(TAG, "getNewLocation(): failed to get the location due to general exception", e)
            LocationFetchResult(null, null, LocationFailureCause.GENERIC_ERROR)
        } finally {
            cts.cancel()
        }
    }


    private suspend fun getCachedLocation(cause: LocationFailureCause): LocationResult {
        return try {
            val location = locationDataStore.getLocation().first()
            if (location != null && location.latitude in -90.0..90.0 && location.longitude in -180.0..180.0) {
                Log.d(TAG, "getCachedLocation: fetching a stale location")
                LocationResult.Stale(location, cause)
            } else {
                Log.w(TAG, "getLocation: cached location is not valid")
                LocationResult.Error(cause)
            }
        } catch (e: Exception) {
            Log.e(TAG, "getLocation: cached location error", e)
            LocationResult.Error(cause)
        }
    }


    private suspend fun processNewLocation(latitude: Double, longitude: Double): LocationResult {
        val addressData: AddressData = getLocationInfo(
            latitude = latitude,
            longitude = longitude
        )

        Log.d(TAG, "processNewLocation: location name and country code = ${addressData.locationName}, ${addressData.countryCode}")

        val locationData = LocationData(
            latitude = latitude,
            longitude = longitude,
            locationName = addressData.locationName,
            countryCode = addressData.countryCode,
            timestamp = System.currentTimeMillis()
        )

        locationDataStore.saveLocation(locationData)
        return LocationResult.Success(locationData)
    }


    private suspend fun getLocationInfo(latitude: Double, longitude: Double): AddressData {
        if (!Geocoder.isPresent()) {
            return AddressData(null, locationDataStore.getCountryCode().first())
        }

        val locale = context.getLocalizedContext().resources.configuration.locales.get(0)
        val geocoder = Geocoder(context, locale)

        return try {
            withTimeout(5_000L) {
                val address: Address? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    suspendCancellableCoroutine { continuation ->
                        geocoder.getFromLocation(latitude, longitude, 1) { addresses ->
                            continuation.resume(addresses.firstOrNull())
                        }
                    }
                } else {
                    withContext(Dispatchers.IO) {
                        @Suppress("DEPRECATION")
                        geocoder.getFromLocation(latitude, longitude, 1)?.firstOrNull()
                    }
                }

                Log.d(TAG, "getLocationInfo: location name success")
                address?.let { parseAddress(it) } ?: AddressData()
            }
        }catch (_: TimeoutCancellationException){
            Log.e(TAG, "getLocationName failed to get the location due to timeout")
            AddressData(null, locationDataStore.getCountryCode().first())
        } catch (e: Exception) {
            Log.e(TAG, "getLocationName Error", e)
            AddressData(null, locationDataStore.getCountryCode().first())
        }
    }


    private suspend fun parseAddress(address: Address): AddressData {
        val locationName = address.locality ?: address.subAdminArea ?: address.adminArea
        val countryCode: String? =
            address.countryCode ?: locationDataStore.getCountryCode().first()
        Log.d(TAG, "parseAddress: country code value = $countryCode")
        return AddressData(locationName = locationName, countryCode = countryCode)
    }


    private fun checkLocationRequirements(): LocationFailureCause? {
        val hasFine = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val hasCoarse = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasFine && !hasCoarse) return LocationFailureCause.PERMISSION_DENIED
        if (isGpsDisabled()) return LocationFailureCause.GPS_DISABLED
        return null
    }


    private fun isGpsDisabled(): Boolean {
        val locationManager: LocationManager =
            context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        val isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

        return !isGpsEnabled && !isNetworkEnabled
    }
}

private data class AddressData(
    val locationName: String? = null,
    val countryCode: String? = null
)

private data class LocationFetchResult(
    var latitude: Double?,
    var longitude: Double?,
    var failureCause: LocationFailureCause?
)