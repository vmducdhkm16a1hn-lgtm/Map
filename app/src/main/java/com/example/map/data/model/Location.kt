package com.example.map.data.model

data class TouristLocation(
    val id: Int,
    val name: String,
    val description: String,
    val latitude: Double,
    val longitude: Double,
    val imageRes: Int = 0,
    val imageName: String = ""
)
