package com.example.map.data.remote

import com.example.map.data.model.DirectionsResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface DirectionsApiService {

    @GET("maps/api/directions/json")
    suspend fun getDirections(
        @Query("origin") origin: String,         // "lat,lng"
        @Query("destination") destination: String, // "lat,lng"
        @Query("key") apiKey: String,
        @Query("mode") mode: String = "driving"
    ): DirectionsResponse
}