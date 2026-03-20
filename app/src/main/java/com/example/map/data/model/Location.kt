package com.example.map.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class TouristLocation(
    val id: Int,
    val name: String,
    val description: String,
    val latitude: Double,
    val longitude: Double,
    val imageRes: Int = 0,        // resource id ảnh thumbnail (tuỳ chọn)
    val imageUrl: String = "",    // URL ảnh từ internet (tuỳ chọn)
    val address: String = "Ninh Bình, Việt Nam",
    val rating: Float = 4.8f,
    val reviewCount: Int = 0
) : Parcelable