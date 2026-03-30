package com.example.map.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// Khai báo dạng Singleton (object) để đảm bảo toàn bộ app chỉ sử dụng chung một phiên bản (instance) Retrofit duy nhất, giúp tiết kiệm tài nguyên.
object RetrofitClient {
    // Địa chỉ gốc của Google Maps API. Tất cả các endpoint ở interface sẽ được nối vào sau chuỗi này.
    private const val BASE_URL = "https://maps.googleapis.com/"
    // Khởi tạo lười (lazy initialization): Biến directionsApi sẽ không được cấp phát bộ nhớ ngay, mà chỉ được tạo ra ở lần đầu tiên bạn gọi đến nó.
    val directionsApi: DirectionsApiService by lazy {
        Retrofit.Builder()
            // Truyền Base URL vào Retrofit
            .baseUrl(BASE_URL)
            // Tích hợp thư viện Gson để tự động chuyển đổi (parse) cục dữ liệu JSON phức tạp trả về từ API thành các Data Class
            .addConverterFactory(GsonConverterFactory.create())
            // Tiến hành khởi tạo đối tượng Retrofit
            .build()
            // Dựa vào interface DirectionsApiService, Retrofit sẽ tự động sinh ra đoạn code thực thi để gọi API thực tế.
            .create(DirectionsApiService::class.java)
    }
}