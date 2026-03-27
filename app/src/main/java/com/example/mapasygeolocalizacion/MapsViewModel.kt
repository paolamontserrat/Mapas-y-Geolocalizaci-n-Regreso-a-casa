package com.example.mapasygeolocalizacion

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.osmdroid.util.GeoPoint
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MapsViewModel : ViewModel() {
    //ITSUR
    /*var destinationLat by mutableStateOf("20.139431259239895")
    var destinationLng by mutableStateOf("-101.15075602647381")*/
     //Casa America
       var destinationLat by mutableStateOf("20.1366974701443")
        var destinationLng by mutableStateOf("-101.19251489713973")

    /*  //Casa Paola
        var destinationLat by mutableStateOf("20.139431259239895")
        var destinationLng by mutableStateOf("-101.15075602647381")
    */
    // Consola de errores en pantalla
    val consoleLogs = mutableStateListOf<String>()

    private val _routePoints = MutableStateFlow<List<GeoPoint>>(emptyList())
    val routePoints: StateFlow<List<GeoPoint>> = _routePoints

    private val _currentLocation = MutableStateFlow<GeoPoint?>(null)
    val currentLocation: StateFlow<GeoPoint?> = _currentLocation

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api.openrouteservice.org/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val apiService = retrofit.create(ORSApiservice::class.java)

    private fun addLog(message: String) {
        Log.d("MapsApp", message)
        consoleLogs.add(0, message) // Añadir al inicio para ver lo más reciente arriba
        if (consoleLogs.size > 10) consoleLogs.removeLast()
    }

    @SuppressLint("MissingPermission")
    fun updateCurrentLocation(context: Context) {
        addLog("Obteniendo ubicación actual...")
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                val point = GeoPoint(location.latitude, location.longitude)
                addLog("Ubicación obtenida: ${point.latitude}, ${point.longitude}")
                _currentLocation.value = point
            } else {
                addLog("ERROR: Ubicación nula. ¿Tienes el GPS activo?")
            }
        }.addOnFailureListener {
            addLog("ERROR GPS: ${it.message}")
        }
    }

    fun calculateRoute(apiKey: String) {
        val origin = _currentLocation.value
        if (origin == null) {
            addLog("ERROR: No hay ubicación de origen.")
            return
        }
        
        val destLat = destinationLat.toDoubleOrNull()
        val destLng = destinationLng.toDoubleOrNull()
        if (destLat == null || destLng == null) {
            addLog("ERROR: Coordenadas de destino inválidas.")
            return
        }

        addLog("Solicitando ruta a OpenRouteService...")
        viewModelScope.launch {
            try {
                val start = "${origin.longitude},${origin.latitude}"
                val end = "$destLng,$destLat"
                
                val response = apiService.getDirections(start, end, apiKey)
                
                if (response.features.isNotEmpty()) {
                    val coords = response.features[0].geometry.coordinates
                    val points = coords.map { GeoPoint(it[1], it[0]) }
                    _routePoints.value = points
                    addLog("ÉXITO: Ruta trazada con ${points.size} puntos.")
                } else {
                    addLog("AVISO: La API no encontró rutas terrestres.")
                }
            } catch (e: Exception) {
                addLog("ERROR API: ${e.localizedMessage}")
                e.printStackTrace()
            }
        }
    }
}
