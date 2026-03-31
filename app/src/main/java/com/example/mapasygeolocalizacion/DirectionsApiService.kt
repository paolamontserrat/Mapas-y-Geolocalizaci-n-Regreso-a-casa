package com.example.mapasygeolocalizacion

import retrofit2.http.GET
import retrofit2.http.Query

// Interfaz para pedir direcciones (archivo de ejemplo o alternativo)
interface DirectionsApiService {
    @GET("maps/api/directions/json")
    suspend fun getDirections(
        @Query("origin") origin: String,
        @Query("destination") destination: String,
        @Query("key") apiKey: String
    ): DirectionsResponse
}

// Datos de la respuesta de direcciones
data class DirectionsResponse(
    val routes: List<Route>
)

// Datos de la ruta
data class Route(
    val overview_polyline: OverviewPolyline,
    val bounds: Bounds
)

// Puntos de la linea en el mapa
data class OverviewPolyline(
    val points: String
)

// Limites del mapa
data class Bounds(
    val northeast: Location,
    val southwest: Location
)

// Coordenadas simples
data class Location(
    val lat: Double,
    val lng: Double
)
