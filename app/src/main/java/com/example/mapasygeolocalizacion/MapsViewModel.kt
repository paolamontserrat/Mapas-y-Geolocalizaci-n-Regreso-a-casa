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

// Clase que controla los datos del mapa y la ubicacion
class MapsViewModel : ViewModel() {
    ///ITSUR
    /*var destinationLat by mutableStateOf("20.139431259239895")
    var destinationLng by mutableStateOf("-101.15075602647381")*/

    //Casa America
    //var destinationLat by mutableStateOf("20.1356374701443")
    //var destinationLng by mutableStateOf("-101.19211489713973")

    //Casa Paola
    var destinationLat by mutableStateOf("20.139431259239895")
    var destinationLng by mutableStateOf("-101.15075602647381")

    // Lista para guardar los mensajes de error o avisos que se ven en la pantalla
    val consoleLogs = mutableStateListOf<String>()

    // Lista de puntos para dibujar la linea del camino en el mapa
    private val _routePoints = MutableStateFlow<List<GeoPoint>>(emptyList())
    val routePoints: StateFlow<List<GeoPoint>> = _routePoints

    // Aqui guardamos la posicion actual del usuario
    private val _currentLocation = MutableStateFlow<GeoPoint?>(null)
    val currentLocation: StateFlow<GeoPoint?> = _currentLocation

    // Configuracion para conectarse al servicio de rutas en internet
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api.openrouteservice.org/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    // Servicio para pedir las direcciones a la API de OpenRouteService
    private val apiService = retrofit.create(ORSApiservice::class.java)

    // Funcion para mostrar mensajes en la consola de la pantalla y en el sistema
    private fun addLog(message: String) {
        Log.d("MapsApp", message)
        consoleLogs.add(0, message)
        if (consoleLogs.size > 10) consoleLogs.removeLast()
    }

    // Funcion para buscar la posicion actual del celular usando el GPS
    @SuppressLint("MissingPermission")
    fun updateCurrentLocation(context: Context) {
        addLog("Obteniendo ubicacion actual...")
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                val point = GeoPoint(location.latitude, location.longitude)
                addLog("Ubicacion obtenida: ${point.latitude}, ${point.longitude}")
                _currentLocation.value = point
            } else {
                addLog("ERROR: Ubicacion nula. Revisar GPS")
            }
        }.addOnFailureListener {
            addLog("ERROR GPS: ${it.message}")
        }
    }

    // Funcion para calcular el camino desde el usuario hasta el destino
    fun calculateRoute(apiKey: String) {
        val origin = _currentLocation.value
        if (origin == null) {
            addLog("ERROR: No hay ubicacion de origen.")
            return
        }
        
        val destLat = destinationLat.toDoubleOrNull()
        val destLng = destinationLng.toDoubleOrNull()
        if (destLat == null || destLng == null) {
            addLog("ERROR: Coordenadas de destino invalidas.")
            return
        }

        addLog("Solicitando ruta a internet...")
        viewModelScope.launch {
            try {
                val start = "${origin.longitude},${origin.latitude}"
                val end = "$destLng,$destLat"
                
                // Pedimos la ruta a la API
                val response = apiService.getDirections(start, end, apiKey)
                
                if (response.features.isNotEmpty()) {
                    // Si hay respuesta, guardamos los puntos para dibujarlos
                    val coords = response.features[0].geometry.coordinates
                    val points = coords.map { GeoPoint(it[1], it[0]) }
                    _routePoints.value = points
                    addLog("EXITO: Ruta trazada correctamente.")
                } else {
                    addLog("AVISO: No se encontro un camino terrestre.")
                }
            } catch (e: Exception) {
                addLog("ERROR API: ${e.localizedMessage}")
                e.printStackTrace()
            }
        }
    }
}
