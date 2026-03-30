package com.example.map.data.model

import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem

// Lớp bọc (Wrapper) giúp chuyển đổi model TouristLocation thành một đối tượng tương thích với thư viện Google Maps Clustering
class LocationClusterItem(
    // Đối tượng chứa dữ liệu gốc của địa điểm du lịch
    val location: TouristLocation
) : ClusterItem {
    // Hàm quan trọng nhất: Cung cấp tọa độ LatLng để hệ thống tính toán khoảng cách và gộp (cluster) các marker lại với nhau khi người dùng zoom out bản đồ
    override fun getPosition(): LatLng {
        return LatLng(location.latitude, location.longitude)
    }
    // Trả về tiêu đề của marker (sẽ hiển thị chữ in đậm trên InfoWindow khi người dùng click vào một marker đơn lẻ)
    override fun getTitle(): String {
        return location.name
    }
    // Trả về đoạn mô tả ngắn (hiển thị ngay bên dưới tiêu đề trên InfoWindow)
    override fun getSnippet(): String {
        return location.description
    }
    // Xác định thứ tự lớp hiển thị (Z-Index) của marker này so với các marker khác trên bản đồ. 0f là mức cơ bản mặc định.
    override fun getZIndex(): Float {
        return 0f
    }
}