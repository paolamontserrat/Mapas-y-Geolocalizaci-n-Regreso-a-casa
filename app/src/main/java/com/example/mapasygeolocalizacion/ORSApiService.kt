package com.example.mapasygeolocalizacion

import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface ORSApiservice {
    @GET("v2/directions/driving-car")
    suspend fun getDirections(
        @Query("start") start: String,
        @Query("end") end: String,
        @Header("Authorization") apiKey: String
    ): ORSResponse
}

data class ORSResponse(
    val features: List<Feature>
)

data class Feature(
    val geometry: Geometry,
    val properties: Properties
)

data class Geometry(
    val coordinates: List<List<Double>>
)

data class Properties(
    val summary: Summary
)

data class Summary(
    val distance: Double,
    val duration: Double
)
