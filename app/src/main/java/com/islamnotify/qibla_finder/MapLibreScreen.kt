package com.islamnotify.qibla_finder

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import org.maplibre.android.MapLibre
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.location.LocationComponentActivationOptions
import org.maplibre.android.location.engine.LocationEngineCallback
import org.maplibre.android.location.engine.LocationEngineRequest
import org.maplibre.android.location.engine.LocationEngineResult
import org.maplibre.android.location.modes.CameraMode
import org.maplibre.android.location.modes.RenderMode
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style
import org.maplibre.android.style.layers.LineLayer
import org.maplibre.android.style.layers.PropertyFactory.*
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.geojson.Feature
import org.maplibre.geojson.FeatureCollection
import org.maplibre.geojson.LineString
import org.maplibre.geojson.Point
import kotlin.math.cos
import kotlin.math.sin
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import kotlin.math.cos
import kotlin.math.sin

private val KAABA_LAT_LNG = LatLng(21.422487, 39.826206)
private const val QIBLA_LINE_SOURCE_ID = "qibla-line-source"
private const val QIBLA_LINE_LAYER_ID = "qibla-line-layer"
private const val GUIDELINES_SOURCE_ID = "guidelines-source"
private const val GUIDELINES_LAYER_ID = "guidelines-layer"


@Composable
fun GoogleMapsCanvas(
    context: Context,
    modifier: Modifier = Modifier
) {
    // 1. State for User Location
    var userLocation by remember { mutableStateOf<LatLng?>(null) }

    // 2. Camera State
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(0.0, 0.0), 2f)
    }

    // 3. Fetch Location Once on Start
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    LaunchedEffect(Unit) {
        getUserLocation(fusedLocationClient) { latLng ->
            userLocation = latLng
            cameraPositionState.position = CameraPosition.fromLatLngZoom(latLng, 15f)
        }
    }

    // 4. The Map UI
    GoogleMap(
        modifier = modifier,
        cameraPositionState = cameraPositionState,
        properties = MapProperties(
            isMyLocationEnabled = userLocation != null, // Shows the blue dot
        ),
        uiSettings = MapUiSettings(
            myLocationButtonEnabled = true,
            compassEnabled = true
        )
    ) {
        userLocation?.let { userPos ->

            // Feature 1: Qibla Line (Blue)
            Polyline(
                points = listOf(userPos, KAABA_LAT_LNG),
                color = Color.Blue,
                width = 8f
            )

        }
    }
}


@SuppressLint("MissingPermission")
private fun getUserLocation(
    client: com.google.android.gms.location.FusedLocationProviderClient,
    onSuccess: (LatLng) -> Unit
) {
    client.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
        .addOnSuccessListener { location ->
            if (location != null) {
                onSuccess(LatLng(location.latitude, location.longitude))
            }
        }
}

//@Composable
//fun MapLibreCanvas(
//    context: Context,
//    modifier: Modifier = Modifier,
//    onMapReady: (MapLibreMap) -> Unit = {}
//) {
//    remember { MapLibre.getInstance(context) }
//    val mapView = remember { MapView(context) }
//
//    val lifecycle = LocalLifecycleOwner.current.lifecycle
//    DisposableEffect(lifecycle, mapView) {
//        val observer = LifecycleEventObserver { _, event ->
//            when (event) {
//                Lifecycle.Event.ON_CREATE -> mapView.onCreate(null)
//                Lifecycle.Event.ON_START -> mapView.onStart()
//                Lifecycle.Event.ON_RESUME -> mapView.onResume()
//                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
//                Lifecycle.Event.ON_STOP -> mapView.onStop()
//                Lifecycle.Event.ON_DESTROY -> mapView.onDestroy()
//                else -> {}
//            }
//        }
//        lifecycle.addObserver(observer)
//        onDispose { lifecycle.removeObserver(observer) }
//    }
//
//    AndroidView(
//        factory = { mapView },
//        modifier = modifier,
//        update = { mv ->
//            mv.getMapAsync { map ->
//                map.setStyle("https://tiles.openfreemap.org/styles/liberty") { style ->
//                    setupMapLayers(style)
//                    enableLocationAndDraw(context, map, style)
//                    onMapReady(map)
//                }
//            }
//        }
//    )
//}
//
//@SuppressLint("MissingPermission")
//private fun enableLocationAndDraw(context: Context, map: MapLibreMap, style: Style) {
//    val locationComponent = map.locationComponent
//    val options = LocationComponentActivationOptions.builder(context, style)
//        .useDefaultLocationEngine(true)
//        .build()
//
//    locationComponent.activateLocationComponent(options)
//    locationComponent.isLocationComponentEnabled = true
//    locationComponent.renderMode = RenderMode.COMPASS
//    locationComponent.cameraMode = CameraMode.TRACKING
//
//    val engine = locationComponent.locationEngine
//    val request = LocationEngineRequest.Builder(1000L)
//        .setPriority(LocationEngineRequest.PRIORITY_HIGH_ACCURACY)
//        .build()
//
//    val callback = object : LocationEngineCallback<LocationEngineResult> {
//        override fun onSuccess(result: LocationEngineResult?) {
//            val location = result?.lastLocation ?: return
//            val userLatLng = LatLng(location.latitude, location.longitude)
//            updateMapVisuals(style, userLatLng)
//            map.animateCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 15.0))
//            engine?.removeLocationUpdates(this)
//        }
//        override fun onFailure(exception: Exception) { exception.printStackTrace() }
//    }
//    engine?.requestLocationUpdates(request, callback, context.mainLooper)
//}
//
//private fun setupMapLayers(style: Style) {
//    if (style.getSource(QIBLA_LINE_SOURCE_ID) == null) {
//        style.addSource(GeoJsonSource(QIBLA_LINE_SOURCE_ID))
//        style.addLayer(LineLayer(QIBLA_LINE_LAYER_ID, QIBLA_LINE_SOURCE_ID).apply {
//            setProperties(lineColor(Color.Blue), lineWidth(3f), lineCap("round"))
//        })
//    }
//
//    if (style.getSource(GUIDELINES_SOURCE_ID) == null) {
//        style.addSource(GeoJsonSource(GUIDELINES_SOURCE_ID))
//        style.addLayer(LineLayer(GUIDELINES_LAYER_ID, GUIDELINES_SOURCE_ID).apply {
//            setProperties(
//                lineColor(Color.Gray),
//                lineWidth(1f),
//                lineDasharray(arrayOf(4f, 4f)) // Dashed effect
//            )
//        })
//    }
//}
//
//private fun updateMapVisuals(style: Style, userLoc: LatLng) {
//    // 1. Update the main Blue Line to Kaaba
//    val qiblaSource = style.getSourceAs<GeoJsonSource>(QIBLA_LINE_SOURCE_ID)
//    val qiblaLine = LineString.fromLngLats(listOf(
//        Point.fromLngLat(userLoc.longitude, userLoc.latitude),
//        Point.fromLngLat(KAABA_LAT_LNG.longitude, KAABA_LAT_LNG.latitude)
//    ))
//    qiblaSource?.setGeoJson(Feature.fromGeometry(qiblaLine))
//
//    // 2. Update Guidelines (Intersecting lines)
//    val guidelinesSource = style.getSourceAs<GeoJsonSource>(GUIDELINES_SOURCE_ID)
//    val features = mutableListOf<Feature>()
//
//    // length determines how far the guidelines extend from the center
//    val length = 0.05
//
//    // Define the 4 angles for the intersecting lines: 0 (Vert), 45 (Diag1), 90 (Horiz), 135 (Diag2)
//    val angles = listOf(0.0, 45.0, 90.0, 135.0)
//
//    for (angleDeg in angles) {
//        val rad = Math.toRadians(angleDeg)
//
//        // Calculate point on one side of the user
//        val latA = userLoc.latitude + (length * cos(rad))
//        val lngA = userLoc.longitude + (length * sin(rad))
//
//        // Calculate point on the opposite side of the user
//        val latB = userLoc.latitude - (length * cos(rad))
//        val lngB = userLoc.longitude - (length * sin(rad))
//
//        // Create a single line string that passes through the user location
//        features.add(Feature.fromGeometry(
//            LineString.fromLngLats(listOf(
//                Point.fromLngLat(lngA, latA),
//                Point.fromLngLat(lngB, latB)
//            ))
//        ))
//    }
//
//    guidelinesSource?.setGeoJson(FeatureCollection.fromFeatures(features))
//}
//package com.islamnotify.qibla_finder
//
//import android.annotation.SuppressLint
//import android.content.Context
//import android.graphics.Color
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.DisposableEffect
//import androidx.compose.runtime.remember
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.viewinterop.AndroidView
//import androidx.lifecycle.Lifecycle
//import androidx.lifecycle.LifecycleEventObserver
//import androidx.lifecycle.compose.LocalLifecycleOwner
//import org.maplibre.android.MapLibre
//import org.maplibre.android.camera.CameraUpdateFactory
//import org.maplibre.android.geometry.LatLng
//import org.maplibre.android.location.LocationComponentActivationOptions
//import org.maplibre.android.location.engine.LocationEngineCallback
//import org.maplibre.android.location.engine.LocationEngineRequest
//import org.maplibre.android.location.engine.LocationEngineResult
//import org.maplibre.android.location.modes.CameraMode
//import org.maplibre.android.location.modes.RenderMode
//import org.maplibre.android.maps.MapLibreMap
//import org.maplibre.android.maps.MapView
//import org.maplibre.android.maps.Style
//import org.maplibre.android.style.layers.LineLayer
//import org.maplibre.android.style.layers.PropertyFactory.*
//import org.maplibre.android.style.sources.GeoJsonSource
//import org.maplibre.geojson.Feature
//import org.maplibre.geojson.FeatureCollection
//import org.maplibre.geojson.LineString
//import org.maplibre.geojson.Point
//import kotlin.math.cos
//import kotlin.math.sin
//
//private val KAABA_LAT_LNG = LatLng(21.422487, 39.826206)
//private const val QIBLA_LINE_SOURCE_ID = "qibla-line-source"
//private const val QIBLA_LINE_LAYER_ID = "qibla-line-layer"
//private const val GUIDELINES_SOURCE_ID = "guidelines-source"
//private const val GUIDELINES_LAYER_ID = "guidelines-layer"
//
//@Composable
//fun MapLibreCanvas(
//    context: Context,
//    modifier: Modifier = Modifier,
//    onMapReady: (MapLibreMap) -> Unit = {}
//) {
//    // Initialize MapLibre
//    remember { MapLibre.getInstance(context) }
//
//    val mapView = remember { MapView(context) }
//
//    // Lifecycle handling
//    val lifecycle = LocalLifecycleOwner.current.lifecycle
//    DisposableEffect(lifecycle, mapView) {
//        val observer = LifecycleEventObserver { _, event ->
//            when (event) {
//                Lifecycle.Event.ON_CREATE -> mapView.onCreate(null)
//                Lifecycle.Event.ON_START -> mapView.onStart()
//                Lifecycle.Event.ON_RESUME -> mapView.onResume()
//                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
//                Lifecycle.Event.ON_STOP -> mapView.onStop()
//                Lifecycle.Event.ON_DESTROY -> mapView.onDestroy()
//                else -> {}
//            }
//        }
//        lifecycle.addObserver(observer)
//        onDispose { lifecycle.removeObserver(observer) }
//    }
//
//    AndroidView(
//        factory = { mapView },
//        modifier = modifier,
//        update = { mv ->
//            mv.getMapAsync { map ->
//                // Set style (Ensure this URL is valid or use a default one)
//                map.setStyle("https://tiles.openfreemap.org/styles/liberty") { style ->
//                    setupMapLayers(style)
//                    enableLocationAndDraw(context, map, style)
//                    onMapReady(map)
//                }
//            }
//        }
//    )
//}
//
//@SuppressLint("MissingPermission")
//private fun enableLocationAndDraw(context: Context, map: MapLibreMap, style: Style) {
//    val locationComponent = map.locationComponent
//
//    // 1. Activate Location Component with default engine
//    val options = LocationComponentActivationOptions.builder(context, style)
//        .useDefaultLocationEngine(true) // MapLibre will find the best engine automatically
//        .build()
//
//    locationComponent.activateLocationComponent(options)
//    locationComponent.isLocationComponentEnabled = true
//    locationComponent.renderMode = RenderMode.COMPASS
//    locationComponent.cameraMode = CameraMode.TRACKING
//
//    // 2. Access the engine created by the component to get a one-time fix
//    val engine = locationComponent.locationEngine
//    val request = LocationEngineRequest.Builder(1000L)
//        .setPriority(LocationEngineRequest.PRIORITY_HIGH_ACCURACY)
//        .build()
//
//    val callback = object : LocationEngineCallback<LocationEngineResult> {
//        override fun onSuccess(result: LocationEngineResult?) {
//            val location = result?.lastLocation ?: return
//            val userLatLng = LatLng(location.latitude, location.longitude)
//
//            // Update visuals
//            updateMapVisuals(style, userLatLng)
//
//            // Center camera
//            map.animateCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 15.0))
//
//            // Once we have a high-accuracy fix, stop updates to save battery
//            engine?.removeLocationUpdates(this)
//        }
//
//        override fun onFailure(exception: Exception) {
//            exception.printStackTrace()
//        }
//    }
//
//    engine?.requestLocationUpdates(request, callback, context.mainLooper)
//}
//
//private fun setupMapLayers(style: Style) {
//    // Qibla Line
//    if (style.getSource(QIBLA_LINE_SOURCE_ID) == null) {
//        style.addSource(GeoJsonSource(QIBLA_LINE_SOURCE_ID))
//        style.addLayer(LineLayer(QIBLA_LINE_LAYER_ID, QIBLA_LINE_SOURCE_ID).apply {
//            setProperties(
//                lineColor(Color.BLUE),
//                lineWidth(3f),
//                lineCap("round")
//            )
//        })
//    }
//
//    // Guidelines
//    if (style.getSource(GUIDELINES_SOURCE_ID) == null) {
//        style.addSource(GeoJsonSource(GUIDELINES_SOURCE_ID))
//        style.addLayer(LineLayer(GUIDELINES_LAYER_ID, GUIDELINES_SOURCE_ID).apply {
//            setProperties(
//                lineColor(Color.GRAY),
//                lineWidth(1.2f),
//                lineDasharray(arrayOf(3f, 3f))
//            )
//        })
//    }
//}
//
//private fun updateMapVisuals(style: Style, userLoc: LatLng) {
//    // Update Blue Line
//    val qiblaSource = style.getSourceAs<GeoJsonSource>(QIBLA_LINE_SOURCE_ID)
//    val qiblaLine = LineString.fromLngLats(listOf(
//        Point.fromLngLat(userLoc.longitude, userLoc.latitude),
//        Point.fromLngLat(KAABA_LAT_LNG.longitude, KAABA_LAT_LNG.latitude)
//    ))
//    qiblaSource?.setGeoJson(Feature.fromGeometry(qiblaLine))
//
//    // Update Guidelines (8 directions)
//    val guidelinesSource = style.getSourceAs<GeoJsonSource>(GUIDELINES_SOURCE_ID)
//    val features = mutableListOf<Feature>()
//    val length = 0.02
//
//    for (i in 0 until 8) {
//        val angleRad = Math.toRadians(i * 45.0)
//        val endLat = userLoc.latitude + (length * cos(angleRad))
//        val endLng = userLoc.longitude + (length * sin(angleRad))
//
//        features.add(Feature.fromGeometry(
//            LineString.fromLngLats(listOf(
//                Point.fromLngLat(userLoc.longitude, userLoc.latitude),
//                Point.fromLngLat(endLng, endLat)
//            ))
//        ))
//    }
//    guidelinesSource?.setGeoJson(FeatureCollection.fromFeatures(features))
//}
//package com.islamnotify.qibla_finder
//
//import android.annotation.SuppressLint
//import android.graphics.Color
//import android.os.Looper
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.DisposableEffect
//import androidx.compose.runtime.remember
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.viewinterop.AndroidView
//import androidx.lifecycle.Lifecycle
//import androidx.lifecycle.LifecycleEventObserver
//import androidx.lifecycle.compose.LocalLifecycleOwner
//import org.maplibre.android.MapLibre
//import org.maplibre.android.camera.CameraUpdateFactory
//import org.maplibre.android.geometry.LatLng
//import org.maplibre.android.location.LocationComponentActivationOptions
//import org.maplibre.android.location.engine.LocationEngineCallback
//import org.maplibre.android.location.engine.LocationEngineRequest
//import org.maplibre.android.location.engine.LocationEngineResult
//import org.maplibre.android.location.modes.CameraMode
//import org.maplibre.android.location.modes.RenderMode
//import org.maplibre.android.maps.MapLibreMap
//import org.maplibre.android.maps.MapView
//import org.maplibre.android.maps.Style
//import org.maplibre.android.style.layers.LineLayer
//import org.maplibre.android.style.layers.PropertyFactory.*
//import org.maplibre.android.style.sources.GeoJsonSource
//import org.maplibre.geojson.Feature
//import org.maplibre.geojson.FeatureCollection
//import org.maplibre.geojson.LineString
//import org.maplibre.geojson.Point
//import kotlin.math.cos
//import kotlin.math.sin
//
//private val KAABA_LAT_LNG = LatLng(21.422487, 39.826206)
//private const val QIBLA_LINE_SOURCE_ID = "qibla-line-source"
//private const val QIBLA_LINE_LAYER_ID = "qibla-line-layer"
//private const val GUIDELINES_SOURCE_ID = "guidelines-source"
//private const val GUIDELINES_LAYER_ID = "guidelines-layer"
//
//@Composable
//fun MapLibreCanvas(
//    modifier: Modifier = Modifier,
//    onMapReady: (MapLibreMap) -> Unit = {}
//) {
//    val context = LocalContext.current
//    remember { MapLibre.getInstance(context) }
//
//    val mapView = remember {
//        MapView(context).apply { onCreate(null) }
//    }
//
//    // Lifecycle handling for the MapView
//    val lifecycle = LocalLifecycleOwner.current.lifecycle
//    DisposableEffect(lifecycle, mapView) {
//        val observer = LifecycleEventObserver { _, event ->
//            when (event) {
//                Lifecycle.Event.ON_START -> mapView.onStart()
//                Lifecycle.Event.ON_RESUME -> mapView.onResume()
//                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
//                Lifecycle.Event.ON_STOP -> mapView.onStop()
//                Lifecycle.Event.ON_DESTROY -> mapView.onDestroy()
//                else -> {}
//            }
//        }
//        lifecycle.addObserver(observer)
//        onDispose { lifecycle.removeObserver(observer) }
//    }
//
//    AndroidView(
//        factory = {
//            mapView.apply {
//                getMapAsync { map ->
//                    map.setStyle("https://tiles.openfreemap.org/styles/liberty") { style ->
//
//                        // 1. Setup Visual Layers (Feature 2 & 3)
//                        setupMapLayers(style)
//
//                        // 2. Enable User Location Dot & Heading (Feature 1)
//                        enableUserLocation(map, style)
//
//                        // 3. Get current location and draw everything once
//                        fetchLocationAndDraw(map, style)
//
//                        onMapReady(map)
//                    }
//                }
//            }
//        },
//        modifier = modifier
//    )
//}
//
//@SuppressLint("MissingPermission")
//private fun enableUserLocation(map: MapLibreMap, style: Style) {
//    val locationComponent = map.locationComponent
//    val options = LocationComponentActivationOptions.builder(map.context, style)
//        .useDefaultLocationEngine(true)
//        .build()
//
//    locationComponent.activateLocationComponent(options)
//    locationComponent.isLocationComponentEnabled = true
//
//    // Feature 1: COMPASS mode shows the user's heading (the V-shaped beam)
//    locationComponent.renderMode = RenderMode.COMPASS
//    locationComponent.cameraMode = CameraMode.TRACKING
//}
//
//private fun setupMapLayers(style: Style) {
//    // Feature 2: Blue Line Source
//    style.addSource(GeoJsonSource(QIBLA_LINE_SOURCE_ID))
//    style.addLayer(LineLayer(QIBLA_LINE_LAYER_ID, QIBLA_LINE_SOURCE_ID).apply {
//        setProperties(
//            lineColor(Color.BLUE),
//            lineWidth(3f),
//            lineCap("round")
//        )
//    })
//
//    // Feature 3: Gray Guidelines Source
//    style.addSource(GeoJsonSource(GUIDELINES_SOURCE_ID))
//    style.addLayer(LineLayer(GUIDELINES_LAYER_ID, GUIDELINES_SOURCE_ID).apply {
//        setProperties(
//            lineColor(Color.parseColor("#808080")), // Gray
//            lineWidth(1.2f),
//            lineDasharray(arrayOf(3f, 3f)) // Makes the line look dashed/guide-like
//        )
//    })
//}
//
//@SuppressLint("MissingPermission")
//private fun fetchLocationAndDraw(map: MapLibreMap, style: Style) {
//    val engine = LocationEngineProvider.getBestLocationEngine(map.context)
//    val request = LocationEngineRequest.Builder(1000L)
//        .setPriority(LocationEngineRequest.PRIORITY_HIGH_ACCURACY)
//        .build()
//
//    val callback = object : LocationEngineCallback<LocationEngineResult> {
//        override fun onSuccess(result: LocationEngineResult?) {
//            val location = result?.lastLocation ?: return
//            val userLatLng = LatLng(location.latitude, location.longitude)
//
//            // Draw the features on the map
//            updateMapVisuals(style, userLatLng)
//
//            // Center camera on user
//            map.animateCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 15.0))
//
//            // We have the location, stop the engine to save battery
//            engine.removeLocationUpdates(this)
//        }
//
//        override fun onFailure(exception: Exception) {}
//    }
//
//    engine.requestLocationUpdates(request, callback, Looper.getMainLooper())
//}
//
//private fun updateMapVisuals(style: Style, userLoc: LatLng) {
//    // Feature 2: Update the Blue Line to Kaaba
//    val qiblaSource = style.getSourceAs<GeoJsonSource>(QIBLA_LINE_SOURCE_ID)
//    val qiblaLine = LineString.fromLngLats(listOf(
//        Point.fromLngLat(userLoc.longitude, userLoc.latitude),
//        Point.fromLngLat(KAABA_LAT_LNG.longitude, KAABA_LAT_LNG.latitude)
//    ))
//    qiblaSource?.setGeoJson(Feature.fromGeometry(qiblaLine))
//
//    // Feature 3: Update the 8 Direction Guidelines
//    val guidelinesSource = style.getSourceAs<GeoJsonSource>(GUIDELINES_SOURCE_ID)
//    val features = mutableListOf<Feature>()
//    val length = 0.02 // Length of guidelines (approx 2km)
//
//    for (i in 0 until 8) {
//        val angleRad = Math.toRadians(i * 45.0)
//        val endLat = userLoc.latitude + (length * cos(angleRad))
//        val endLng = userLoc.longitude + (length * sin(angleRad))
//
//        features.add(Feature.fromGeometry(
//            LineString.fromLngLats(listOf(
//                Point.fromLngLat(userLoc.longitude, userLoc.latitude),
//                Point.fromLngLat(endLng, endLat)
//            ))
//        ))
//    }
//    guidelinesSource?.setGeoJson(FeatureCollection.fromFeatures(features))
//}
//package com.islamnotify.qibla_finder
//
//import android.annotation.SuppressLint
//import android.content.Context
//import android.graphics.Color
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.DisposableEffect
//import androidx.compose.runtime.remember
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.viewinterop.AndroidView
//import androidx.lifecycle.Lifecycle
//import androidx.lifecycle.LifecycleEventObserver
//import androidx.lifecycle.compose.LocalLifecycleOwner
//import org.maplibre.android.MapLibre
//import org.maplibre.android.camera.CameraUpdateFactory
//import org.maplibre.android.geometry.LatLng
//import org.maplibre.android.location.LocationComponentActivationOptions
//import org.maplibre.android.location.modes.CameraMode
//import org.maplibre.android.location.modes.RenderMode
//import org.maplibre.android.maps.MapLibreMap
//import org.maplibre.android.maps.MapView
//import org.maplibre.android.maps.Style
//import org.maplibre.android.style.layers.LineLayer
//import org.maplibre.android.style.layers.PropertyFactory.*
//import org.maplibre.android.style.sources.GeoJsonSource
//import org.maplibre.geojson.Feature
//import org.maplibre.geojson.FeatureCollection
//import org.maplibre.geojson.LineString
//import org.maplibre.geojson.Point
//import kotlin.math.cos
//import kotlin.math.sin
//
//// Constants
//private val KAABA_LAT_LNG = LatLng(21.422487, 39.826206)
//private const val QIBLA_LINE_SOURCE_ID = "qibla-line-source"
//private const val QIBLA_LINE_LAYER_ID = "qibla-line-layer"
//private const val GUIDELINES_SOURCE_ID = "guidelines-source"
//private const val GUIDELINES_LAYER_ID = "guidelines-layer"
//
//@Composable
//fun MapLibreCanvas(
//    context: Context,
//    modifier: Modifier = Modifier,
//    onMapReady: (MapLibreMap) -> Unit = {}
//) {
//    val context = LocalContext.current
//    remember { MapLibre.getInstance(context) }
//
//    val mapView = remember {
//        MapView(context).apply { onCreate(null) }
//    }
//
//    val lifecycle = LocalLifecycleOwner.current.lifecycle
//    DisposableEffect(lifecycle, mapView) {
//        val observer = LifecycleEventObserver { _, event ->
//            when (event) {
//                Lifecycle.Event.ON_START -> mapView.onStart()
//                Lifecycle.Event.ON_RESUME -> mapView.onResume()
//                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
//                Lifecycle.Event.ON_STOP -> mapView.onStop()
//                Lifecycle.Event.ON_DESTROY -> mapView.onDestroy()
//                else -> {}
//            }
//        }
//        lifecycle.addObserver(observer)
//        onDispose { lifecycle.removeObserver(observer) }
//    }
//
//    AndroidView(
//        factory = {
//            mapView.apply {
//                getMapAsync { map ->
//                    map.setStyle("https://tiles.openfreemap.org/styles/liberty") { style ->
//                        enableLocationComponent(context, map, style)
//                        setupMapLayers(style)
//                        // Listen for location changes to update lines
//                        map.locationComponent.addOnLocationChangeListener { location ->
//                            val userLatLng = LatLng(location.latitude, location.longitude)
//                            updateLines(style, userLatLng)
//
//                            // Move camera to user location on first fix
//                            val cameraUpdate = CameraUpdateFactory.newLatLngZoom(userLatLng, 15.0)
//                            map.animateCamera(cameraUpdate)
//                        }
//
//                        onMapReady(map)
//                    }
//                }
//            }
//        },
//        modifier = modifier,
//        update = { /* UI updates handled by listeners */ }
//    )
//}
//
//@SuppressLint("MissingPermission")
//private fun enableLocationComponent(context: Context, map: MapLibreMap, style: Style) {
//    val locationComponent = map.locationComponent
//
//    val options = LocationComponentActivationOptions.builder(context, style)
//        .useDefaultLocationEngine(true)
//        .build()
//
//    locationComponent.activateLocationComponent(options)
//    locationComponent.isLocationComponentEnabled = true
//
//    // Feature 1: Show Heading (Compass)
//    locationComponent.renderMode = RenderMode.COMPASS
//    locationComponent.cameraMode = CameraMode.TRACKING
//}
//
//private fun setupMapLayers(style: Style) {
//    // Feature 2: Blue Line Source & Layer
//    style.addSource(GeoJsonSource(QIBLA_LINE_SOURCE_ID))
//    style.addLayer(LineLayer(QIBLA_LINE_LAYER_ID, QIBLA_LINE_SOURCE_ID).apply {
//        setProperties(
//            lineColor(Color.BLUE),
//            lineWidth(3f),
//            lineCap("round"),
//            lineJoin("round")
//        )
//    })
//
//    // Feature 3: Gray Guidelines Source & Layer
//    style.addSource(GeoJsonSource(GUIDELINES_SOURCE_ID))
//    style.addLayer(LineLayer(GUIDELINES_LAYER_ID, GUIDELINES_SOURCE_ID).apply {
//        setProperties(
//            lineColor(Color.LTGRAY),
//            lineWidth(1.5f),
//            lineDasharray(arrayOf(2f, 2f)) // Dashed lines look better for guides
//        )
//    })
//}
//
//private fun updateLines(style: Style, userLoc: LatLng) {
//    // Update Qibla Line
//    val qiblaSource = style.getSourceAs<GeoJsonSource>(QIBLA_LINE_SOURCE_ID)
//    val qiblaLine = LineString.fromLngLats(listOf(
//        Point.fromLngLat(userLoc.longitude, userLoc.latitude),
//        Point.fromLngLat(KAABA_LAT_LNG.longitude, KAABA_LAT_LNG.latitude)
//    ))
//    qiblaSource?.setGeoJson(Feature.fromGeometry(qiblaLine))
//
//    // Update Guidelines (8 directions)
//    val guidelinesSource = style.getSourceAs<GeoJsonSource>(GUIDELINES_SOURCE_ID)
//    val guideLines = mutableListOf<Feature>()
//    val distance = 0.01 // Roughly 1km in decimal degrees (approximation)
//
//    for (i in 0 until 8) {
//        val angleDeg = i * 45.0
//        val angleRad = Math.toRadians(angleDeg)
//
//        // Simple planar approximation for guidelines
//        val endLat = userLoc.latitude + (distance * cos(angleRad))
//        val endLng = userLoc.longitude + (distance * sin(angleRad))
//
//        val line = LineString.fromLngLats(listOf(
//            Point.fromLngLat(userLoc.longitude, userLoc.latitude),
//            Point.fromLngLat(endLng, endLat)
//        ))
//        guideLines.add(Feature.fromGeometry(line))
//    }
//    guidelinesSource?.setGeoJson(FeatureCollection.fromFeatures(guideLines))
//}
////
////package com.islamnotify.qibla_finder
////
////import androidx.compose.runtime.Composable
////import androidx.compose.runtime.DisposableEffect
////import androidx.compose.runtime.remember
////import androidx.compose.ui.Modifier
////import androidx.compose.ui.platform.LocalContext
////import androidx.compose.ui.viewinterop.AndroidView
////import androidx.lifecycle.Lifecycle
////import androidx.lifecycle.LifecycleEventObserver
////import androidx.lifecycle.compose.LocalLifecycleOwner
////import org.maplibre.android.MapLibre
////import org.maplibre.android.camera.CameraPosition
////import org.maplibre.android.camera.CameraUpdateFactory
////import org.maplibre.android.geometry.LatLng
////import org.maplibre.android.maps.MapView
////
////@Composable
////fun MapLibreCanvas(
////    modifier: Modifier = Modifier,
////    onMapReady: (org.maplibre.android.maps.MapLibreMap) -> Unit = {}
////) {
////    val context = LocalContext.current
////
////    // 1. Initialize MapLibre once
////    remember { MapLibre.getInstance(context) }
////
////    // 2. Create and Remember the MapView
////    val mapView = remember {
////        MapView(context).apply {
////            // CRITICAL: Must call onCreate
////            onCreate(null)
////        }
////    }
////
////    // 3. Proper Lifecycle Handling
////    val lifecycle = LocalLifecycleOwner.current.lifecycle
////    DisposableEffect(lifecycle, mapView) {
////        val observer = LifecycleEventObserver { _, event ->
////            when (event) {
////                Lifecycle.Event.ON_START -> mapView.onStart()
////                Lifecycle.Event.ON_RESUME -> mapView.onResume()
////                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
////                Lifecycle.Event.ON_STOP -> mapView.onStop()
////                Lifecycle.Event.ON_DESTROY -> mapView.onDestroy()
////                else -> {}
////            }
////        }
////        lifecycle.addObserver(observer)
////        onDispose {
////            lifecycle.removeObserver(observer)
////        }
////    }
////
////    // 4. The View Bridge
////    AndroidView(
////        factory = {
////            // Configure the map ONLY ONCE here
////            mapView.apply {
////                getMapAsync { map ->
////                    map.setStyle("https://tiles.openfreemap.org/styles/liberty") { _ ->
////                        val initialPosition = CameraPosition.Builder()
////                            .target(LatLng(30.1400064, 31.6540821))
////                            .zoom(17.0)
////                            .build()
////
////                        map.moveCamera(CameraUpdateFactory.newCameraPosition(initialPosition))
////
////                        onMapReady(map)
////                    }
////                }
////            }
////        },
////        modifier = modifier,
////        update = { /* Leave empty for basic maps to avoid flickering */ }
////    )
////}
////
//////@Composable
//////fun MapLibreCanvas(modifier: Modifier = Modifier) {
//////    val context = LocalContext.current
//////
//////    // 1. Initialize MapLibre (must be done once)
//////    remember {
//////        MapLibre.getInstance(context)
//////    }
//////
//////    // 2. Manage the MapView lifecycle
//////    val mapView = remember { MapView(context) }
//////
//////    val lifecycleObserver = remember {
//////        LifecycleEventObserver { _, event ->
//////            when (event) {
//////                Lifecycle.Event.ON_START -> mapView.onStart()
//////                Lifecycle.Event.ON_RESUME -> mapView.onResume()
//////                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
//////                Lifecycle.Event.ON_STOP -> mapView.onStop()
//////                Lifecycle.Event.ON_DESTROY -> mapView.onDestroy()
//////                else -> {}
//////            }
//////        }
//////    }
//////
//////    val lifecycle = LocalLifecycleOwner.current.lifecycle
//////    DisposableEffect(lifecycle) {
//////        lifecycle.addObserver(lifecycleObserver)
//////        onDispose {
//////            lifecycle.removeObserver(lifecycleObserver)
//////        }
//////    }
//////
//////    // 3. The actual View bridge
//////    AndroidView(
//////        factory = { mapView },
//////        modifier = modifier,
//////        update = { view ->
//////            view.getMapAsync { map ->
//////                // Set the map style (Demo style from MapLibre)
//////                map.setStyle("https://tiles.openfreemap.org/styles/positron") { _ ->
//////                    // Map is loaded and ready!
//////                }
//////            }
//////        }
//////    )
//////}