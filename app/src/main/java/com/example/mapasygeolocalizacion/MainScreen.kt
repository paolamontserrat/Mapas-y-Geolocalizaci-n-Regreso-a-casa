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

// Esta es la pantalla principal de la aplicacion
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MainScreen(viewModel: MapsViewModel) {
    val context = LocalContext.current
    // Revisamos si tenemos permiso para usar el GPS
    val locationPermissionState = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)
    
    // Obtenemos los datos desde el ViewModel
    val currentLocation by viewModel.currentLocation.collectAsState()
    val routePoints by viewModel.routePoints.collectAsState()
    val logs = viewModel.consoleLogs
    
    // Llave para usar el servicio de mapas (OpenRouteService)
    val apiKey = "eyJvcmciOiI1YjNjZTM1OTc4NTExMTAwMDFjZjYyNDgiLCJpZCI6IjBlMzg0MzAyNWY4MTQ1YzFiMzhhNzM5YWEwZGY2YjIyIiwiaCI6Im11cm11cjY0In0="

    // Si el permiso esta concedido, buscamos la ubicacion. Si no, lo pedimos.
    LaunchedEffect(locationPermissionState.status.isGranted) {
        if (locationPermissionState.status.isGranted) {
            viewModel.updateCurrentLocation(context)
        } else {
            locationPermissionState.launchPermissionRequest()
        }
    }

    Scaffold(
        // Boton flotante para calcular la ruta
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.calculateRoute(apiKey) }) {
                Icon(Icons.Default.Directions, contentDescription = "Calcular Ruta")
            }
        },
        // Barra inferior para escribir el destino
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
            // Aqui mostramos el mapa real
            AndroidView(
                factory = { ctx ->
                    MapView(ctx).apply {
                        setTileSource(TileSourceFactory.MAPNIK) // Estilo del mapa
                        setMultiTouchControls(true) // Permite usar dos dedos para zoom
                        controller.setZoom(15.0) // Nivel de acercamiento inicial
                    }
                },
                update = { mapView ->
                    mapView.overlays.clear() // Limpiamos marcas anteriores
                    
                    // Si sabemos donde esta el usuario, ponemos un marcador
                    currentLocation?.let {
                        mapView.controller.setCenter(it)
                        val marker = Marker(mapView)
                        marker.position = it
                        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        marker.title = "Mi Ubicacion"
                        mapView.overlays.add(marker)
                    }

                    // Si hay una ruta calculada, dibujamos la linea azul
                    if (routePoints.isNotEmpty()) {
                        val polyline = Polyline()
                        polyline.setPoints(routePoints)
                        polyline.outlinePaint.color = android.graphics.Color.BLUE
                        polyline.outlinePaint.strokeWidth = 10f
                        mapView.overlays.add(polyline)
                    }
                    
                    mapView.invalidate() // Refresca el mapa
                },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
