package com.example.mapasygeolocalizacion

import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

// Interfaz para definir como nos comunicamos con el servidor de rutas
interface ORSApiservice {
    // Funcion para pedir el camino entre dos puntos manejando el carro
    @GET("v2/directions/driving-car")
    suspend fun getDirections(
        @Query("start") start: String, // Punto de inicio
        @Query("end") end: String,     // Punto de llegada
        @Header("Authorization") apiKey: String // Llave secreta de permiso
    ): ORSResponse
}

// Estructura de la respuesta que nos da el servidor
data class ORSResponse(
    val features: List<Feature>
)

// Representa una caracteristica de la respuesta (como la ruta)
data class Feature(
    val geometry: Geometry,
    val properties: Properties
)

// Aqui se guardan las coordenadas de la linea del camino
data class Geometry(
    val coordinates: List<List<Double>>
)

// Datos extras como distancia y tiempo
data class Properties(
    val summary: Summary
)

// Resumen de la ruta: cuanto mide y cuanto tarda
data class Summary(
    val distance: Double,
    val duration: Double
)
