package com.example.map.data.model

// Class đại diện cho một địa điểm du lịch, dùng để chứa dữ liệu hiển thị thông tin và cắm cờ lên bản đồ
data class TouristLocation(
    // Định danh duy nhất cho mỗi địa điểm (dùng để truy vấn trong Database hoặc phân biệt các địa điểm)
    val id: Int,
    // Tên của địa điểm du lịch
    val name: String,
    // Đoạn text mô tả chi tiết về địa điểm đó để hiển thị trên UI
    val description: String,
    // Vĩ độ của địa điểm - Dữ liệu quan trọng bắt buộc phải có để xác định vị trí trên Google Maps
    val latitude: Double,
    // Kinh độ của địa điểm - Kết hợp cùng vĩ độ để tạo thành một tọa độ LatLng hoàn chỉnh
    val longitude: Double,
    // ID của file ảnh lưu trong thư mục res/drawable. Việc gán = 0 nghĩa là mặc định sẽ không có ảnh nếu bạn không truyền vào.
    val imageRes: Int = 0
)