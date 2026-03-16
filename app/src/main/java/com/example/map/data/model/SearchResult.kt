package com.example.map.data.model

import com.google.android.gms.maps.model.LatLng

/**
 * SearchResult - Kết quả tìm kiếm từ Places API
 *
 * Data class chứa thông tin địa điểm tìm được
 */
data class SearchResult(
    val placeId: String,              // ID của địa điểm từ Places API
    val name: String,                  // Tên địa điểm
    val address: String,               // Địa chỉ đầy đủ
    val latLng: LatLng,               // Tọa độ
    val types: List<String> = emptyList()  // Loại địa điểm (restaurant, hotel, etc.)
)

