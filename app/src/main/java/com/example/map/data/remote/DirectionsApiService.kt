package com.example.map.data.remote

import com.example.map.data.model.DirectionsResponse
import retrofit2.http.GET
import retrofit2.http.Query
// Interface dùng để định nghĩa các API endpoint cho Retrofit
interface DirectionsApiService {
    // Khai báo phương thức HTTP GET và đường dẫn (endpoint) của API
    // Đường dẫn này sẽ được nối vào BASE_URL (ví dụ: https://maps.googleapis.com/)
    @GET("maps/api/directions/json")
    // Từ khóa 'suspend' để chạy hàm này bất đồng bộ bằng Kotlin Coroutines, giúp gọi mạng mà không làm đơ giao diện (UI Thread)
    suspend fun getDirections(
        // @Query sẽ tự động nối tham số vào URL theo định dạng ?origin=...
        // Tọa độ điểm bắt đầu
        @Query("origin") origin: String,
        // Tọa độ điểm kết thúc
        @Query("destination") destination: String,
        // API Key bắt buộc của Google Cloud để xác thực quyền gọi API
        @Query("key") apiKey: String,
        // Phương tiện di chuyển. Gán giá trị mặc định là "driving" (ô tô), có thể truyền "walking" (đi bộ), "transit" (phương tiện công cộng)...
        @Query("mode") mode: String = "driving"
        // Kiểu dữ liệu trả về: Retrofit kết hợp Gson sẽ tự động parse cục JSON từ server thành class DirectionsResponse
    ): DirectionsResponse
}