package com.example.map.data.repository

import android.content.Context
import com.example.map.data.model.TouristLocation
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class LocationRepository(private val context: Context) {

    /**
     * Đọc danh sách địa điểm từ file JSON trong assets
     */
    fun getNinhBinhLocations(): List<TouristLocation> {
        return try {
            val jsonString = context.assets.open("locations.json").bufferedReader().use { it.readText() }
            val listType = object : TypeToken<List<TouristLocation>>() {}.type
            Gson().fromJson(jsonString, listType)
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback: trả về danh sách mặc định nếu có lỗi
            getDefaultLocations()
        }
    }

    /**
     * Danh sách mặc định (fallback)
     */
    private fun getDefaultLocations(): List<TouristLocation> {
        return listOf(
            TouristLocation(
                id = 1,
                name = "Tràng An",
                description = "Khu du lịch sinh thái Tràng An - Di sản thế giới UNESCO",
                latitude = 20.2525,
                longitude = 105.9000
            ),
            TouristLocation(
                id = 2,
                name = "Tam Cốc - Bích Động",
                description = "Vịnh Hạ Long trên cạn, nổi tiếng với 3 hang động",
                latitude = 20.2167,
                longitude = 105.9333
            ),
            TouristLocation(
                id = 3,
                name = "Cố đô Hoa Lư",
                description = "Kinh đô đầu tiên của nhà nước phong kiến Việt Nam",
                latitude = 20.2803,
                longitude = 105.9058
            ),
            TouristLocation(
                id = 4,
                name = "Chùa Bái Đính",
                description = "Chùa lớn nhất Đông Nam Á với nhiều kỷ lục Việt Nam",
                latitude = 20.3014,
                longitude = 105.8639
            ),
            TouristLocation(
                id = 5,
                name = "Vườn Quốc gia Cúc Phương",
                description = "Vườn quốc gia đầu tiên của Việt Nam",
                latitude = 20.3500,
                longitude = 105.5833
            ),
            TouristLocation(
                id = 6,
                name = "Kênh Gà - Vân Trình",
                description = "Khu du lịch sinh thái suối khoáng nổi tiếng",
                latitude = 20.3892,
                longitude = 105.8681
            ),
            TouristLocation(
                id = 7,
                name = "Nhà thờ đá Phát Diệm",
                description = "Công trình kiến trúc độc đáo kết hợp Đông - Tây",
                latitude = 20.0922,
                longitude = 106.0917
            ),
            TouristLocation(
                id = 8,
                name = "Hồ Đồng Chương",
                description = "Hồ nước ngọt đẹp giữa vùng núi đá vôi",
                latitude = 20.2700,
                longitude = 105.8100
            )
        )
    }
}