package com.example.map.data.model

import com.google.gson.annotations.SerializedName

// Class gốc hứng toàn bộ dữ liệu trả về từ API Google Directions
data class DirectionsResponse(
    // Danh sách các tuyến đường có thể đi (thường phần tử [0] là tuyến đường tối ưu nhất)
    val routes: List<Route>
)

data class Route(
    // Danh sách các chặng đường (chứa thông tin chi tiết của từng đoạn trên tuyến đường)
    val legs: List<Leg>,

    // Annotation quan trọng: Ánh xạ chính xác key "overview_polyline" từ JSON API trả về vào biến Kotlin
    @SerializedName("overview_polyline")
    // Chứa thông tin tổng quan để vẽ toàn bộ tuyến đường
    val overviewPolyline: OverviewPolyline
)

data class Leg(
    // Tổng khoảng cách của chặng đường
    val distance: TextValue,
    // Tổng thời gian dự kiến di chuyển
    val duration: TextValue
)

data class TextValue(
    // Dữ liệu dạng chữ để hiển thị lên UI cho user đọc (Ví dụ: "15 km", "20 mins")
    val text: String,
    // Dữ liệu dạng số nguyên để tính toán logic trong app (Ví dụ: 15000 (mét), 1200 (giây))
    val value: Int
)

data class OverviewPolyline(
    // Chuỗi tọa độ đã bị mã hóa. Bạn sẽ cần decode chuỗi này để lấy ra List<LatLng> vẽ lên bản đồ.
    val points: String   // Encoded polyline string
)