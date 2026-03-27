package com.example.mapasygeolocalizacion

import android.Manifest
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MainScreen(viewModel: MapsViewModel) {
    val context = LocalContext.current
    val locationPermissionState = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)
    val currentLocation by viewModel.currentLocation.collectAsState()
    val routePoints by viewModel.routePoints.collectAsState()
    val logs = viewModel.consoleLogs

    // TU API KEY DE OPENROUTE SERVICE
    val apiKey = "eyJvcmciOiI1YjNjZTM1OTc4NTExMTAwMDFjZjYyNDgiLCJpZCI6IjBlMzg0MzAyNWY4MTQ1YzFiMzhhNzM5YWEwZGY2YjIyIiwiaCI6Im11cm11cjY0In0="

    LaunchedEffect(locationPermissionState.status.isGranted) {
        if (locationPermissionState.status.isGranted) {
            viewModel.updateCurrentLocation(context)
        } else {
            locationPermissionState.launchPermissionRequest()
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.calculateRoute(apiKey) }) {
                Icon(Icons.Default.Directions, contentDescription = "Calcular Ruta")
            }
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(16.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextField(
                        value = viewModel.destinationLat,
                        onValueChange = { viewModel.destinationLat = it },
                        label = { Text("Lat Casa") },
                        modifier = Modifier.weight(1f)
                    )
                    TextField(
                        value = viewModel.destinationLng,
                        onValueChange = { viewModel.destinationLng = it },
                        label = { Text("Lng Casa") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            // Mapa
            AndroidView(
                factory = { ctx ->
                    MapView(ctx).apply {
                        setTileSource(TileSourceFactory.MAPNIK)
                        setMultiTouchControls(true)
                        controller.setZoom(15.0)
                    }
                },
                update = { mapView ->
                    mapView.overlays.clear()
                    
                    currentLocation?.let {
                        mapView.controller.setCenter(it)
                        val marker = Marker(mapView)
                        marker.position = it
                        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        marker.title = "Mi Ubicación"
                        mapView.overlays.add(marker)
                    }

                    if (routePoints.isNotEmpty()) {
                        val polyline = Polyline()
                        polyline.setPoints(routePoints)
                        polyline.outlinePaint.color = android.graphics.Color.BLUE
                        polyline.outlinePaint.strokeWidth = 10f
                        mapView.overlays.add(polyline)
                    }
                    
                    mapView.invalidate()
                },
                modifier = Modifier.fillMaxSize()
            )

            /* // Consola de errores (Oculta)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .padding(8.dp)
                    .align(Alignment.TopCenter),
                color = Color.Black.copy(alpha = 0.7f),
                shape = RoundedCornerShape(8.dp)
            ) {
                LazyColumn(
                    modifier = Modifier.padding(8.dp),
                    reverseLayout = false
                ) {
                    item {
                        Text("--- CONSOLA DE ESTADO ---", color = Color.Green, fontSize = 10.sp)
                    }
                    items(logs) { log ->
                        Text(
                            text = "> $log",
                            color = if (log.contains("ERROR")) Color.Red else Color.White,
                            fontSize = 11.sp,
                            lineHeight = 14.sp
                        )
                    }
                }
            }
            */
        }
    }
}
