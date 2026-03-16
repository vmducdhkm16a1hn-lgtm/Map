package com.example.map.data.model

import com.google.gson.annotations.SerializedName

data class DirectionsResponse(
    val routes: List<Route>
)

data class Route(
    val legs: List<Leg>,
    @SerializedName("overview_polyline")
    val overviewPolyline: OverviewPolyline
)

data class Leg(
    val distance: TextValue,
    val duration: TextValue
)

data class TextValue(
    val text: String,
    val value: Int
)

data class OverviewPolyline(
    val points: String   // Encoded polyline string
)