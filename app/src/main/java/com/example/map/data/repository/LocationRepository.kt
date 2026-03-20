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
                description = "Khu du lịch sinh thái Tràng An - Di sản thế giới UNESCO với hệ thống hang động và sông nước tuyệt đẹp.",
                latitude = 20.2525,
                longitude = 105.9000,
                imageUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/d/d3/Trang_An_Scenic_Landscape_Complex.jpg/640px-Trang_An_Scenic_Landscape_Complex.jpg",
                address = "Tràng An, Ninh Bình, Việt Nam",
                rating = 4.9f,
                reviewCount = 25430
            ),
            TouristLocation(
                id = 2,
                name = "Tam Cốc - Bích Động",
                description = "Vịnh Hạ Long trên cạn, nổi tiếng với 3 hang động xuyên núi đá vôi và dòng sông Ngô Đồng thơ mộng.",
                latitude = 20.2167,
                longitude = 105.9333,
                imageUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/c/c3/NinhBinh_TamCoc.jpg/640px-NinhBinh_TamCoc.jpg",
                address = "Ninh Hải, Hoa Lư, Ninh Bình, Việt Nam",
                rating = 4.8f,
                reviewCount = 18750
            ),
            TouristLocation(
                id = 3,
                name = "Cố đô Hoa Lư",
                description = "Kinh đô đầu tiên của nhà nước phong kiến Việt Nam thống nhất, thờ Đinh Tiên Hoàng và Lê Đại Hành.",
                latitude = 20.2803,
                longitude = 105.9058,
                imageUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/5/5f/Hoa_Lu_ancient_capital.jpg/640px-Hoa_Lu_ancient_capital.jpg",
                address = "Trường Yên, Hoa Lư, Ninh Bình, Việt Nam",
                rating = 4.7f,
                reviewCount = 12340
            ),
            TouristLocation(
                id = 4,
                name = "Chùa Bái Đính",
                description = "Chùa lớn nhất Đông Nam Á với nhiều kỷ lục Việt Nam. Khu vực rộng hơn 539 ha với nhiều công trình hoành tráng.",
                latitude = 20.3014,
                longitude = 105.8639,
                imageUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/4/43/Ch%C3%B9a_B%C3%A1i_%C4%90%C3%ADnh_-_Ninh_B%C3%ACnh.jpg/640px-Ch%C3%B9a_B%C3%A1i_%C4%90%C3%ADnh_-_Ninh_B%C3%ACnh.jpg",
                address = "Gia Sinh, Gia Viễn, Ninh Bình, Việt Nam",
                rating = 4.8f,
                reviewCount = 22100
            ),
            TouristLocation(
                id = 5,
                name = "Vườn Quốc gia Cúc Phương",
                description = "Vườn quốc gia đầu tiên của Việt Nam (1962), là nơi cư trú của nhiều loài động thực vật quý hiếm.",
                latitude = 20.3500,
                longitude = 105.5833,
                imageUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/7/75/Cucphuong.jpg/640px-Cucphuong.jpg",
                address = "Nho Quan, Ninh Bình, Việt Nam",
                rating = 4.6f,
                reviewCount = 9870
            ),
            TouristLocation(
                id = 6,
                name = "Kênh Gà - Vân Trình",
                description = "Khu du lịch sinh thái nổi tiếng với suối khoáng nóng tự nhiên và hệ thống hang động, sông nước xanh mát.",
                latitude = 20.3892,
                longitude = 105.8681,
                imageUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/5/50/Van_Long_Ninh_Binh.jpg/640px-Van_Long_Ninh_Binh.jpg",
                address = "Gia Thịnh, Gia Viễn, Ninh Bình, Việt Nam",
                rating = 4.5f,
                reviewCount = 5430
            ),
            TouristLocation(
                id = 7,
                name = "Nhà thờ đá Phát Diệm",
                description = "Công trình kiến trúc độc đáo kết hợp phong cách Đông - Tây, xây dựng từ cuối thế kỷ 19 hoàn toàn bằng đá.",
                latitude = 20.0922,
                longitude = 106.0917,
                imageUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/6/6a/Phat_Diem_Cathedral_01.jpg/640px-Phat_Diem_Cathedral_01.jpg",
                address = "Phát Diệm, Kim Sơn, Ninh Bình, Việt Nam",
                rating = 4.7f,
                reviewCount = 8920
            ),
            TouristLocation(
                id = 8,
                name = "Hồ Đồng Chương",
                description = "Hồ nước ngọt đẹp như mơ giữa vùng núi đá vôi xanh biếc, lý tưởng cho chèo thuyền kayak và cắm trại.",
                latitude = 20.2700,
                longitude = 105.8100,
                imageUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/5/50/Van_Long_Ninh_Binh.jpg/640px-Van_Long_Ninh_Binh.jpg",
                address = "Nho Quan, Ninh Bình, Việt Nam",
                rating = 4.6f,
                reviewCount = 4210
            )
        )
    }
}