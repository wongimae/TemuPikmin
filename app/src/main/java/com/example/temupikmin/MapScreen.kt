package com.example.temupikmin

import android.graphics.Color
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.temupikmin.data.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.Marker

@Composable
fun MapScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var points by remember { mutableStateOf<List<GeoPoint>>(emptyList()) }

    // Load points from Room and refresh every 10 seconds while screen is visible
    LaunchedEffect(Unit) {
        while (true) {
            val db = AppDatabase.getInstance(context.applicationContext)
            val locationPoints = withContext(Dispatchers.IO) {
                db.locationPointDao().getAllPoints()
            }
            points = locationPoints.map { GeoPoint(it.latitude, it.longitude) }
            kotlinx.coroutines.delay(10_000L)
        }
    }


    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                MapView(ctx).apply {
                    setTileSource(TileSourceFactory.MAPNIK)
                    setMultiTouchControls(true)
                    controller.setZoom(16.0)
                }
            },
            update = { mapView ->
                mapView.overlays.clear()

                if (points.isNotEmpty()) {
                    val polyline = Polyline().apply {
                        setPoints(points)
                        outlinePaint.color = Color.BLUE
                        outlinePaint.strokeWidth = 8f
                    }
                    mapView.overlays.add(polyline)

                    val currentLocationMarker = Marker(mapView).apply {
                        position = points.last()
                        title = "Current location"
                    }
                    mapView.overlays.add(currentLocationMarker)

                    mapView.controller.setCenter(points.last())
                }

                mapView.invalidate()
            }
        )

        Button(
            onClick = onBack,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)
        ) {
            Text("←")
        }
    }
}