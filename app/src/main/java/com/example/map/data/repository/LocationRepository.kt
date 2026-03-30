package com.example.map.data.repository

import android.content.Context
import com.example.map.R
import com.example.map.data.model.TouristLocation
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

// Lớp Repository đóng vai trò là nguồn cung cấp dữ liệu (Data Source) cho ứng dụng, tách biệt logic lấy dữ liệu khỏi UI
class LocationRepository(private val context: Context) {

    /**
     * Đọc danh sách địa điểm từ file JSON trong assets
     */
    // Hàm chính để lấy danh sách địa điểm, ưu tiên lấy từ file JSON trước
    fun getNinhBinhLocations(): List<TouristLocation> {
        return try {
            // Mở và đọc toàn bộ dữ liệu từ file "locations.json" nằm trong thư mục 'assets' của project thành một chuỗi văn bản
            val jsonString = context.assets.open("locations.json").bufferedReader().use { it.readText() }

            // Khai báo kiểu dữ liệu List<TouristLocation> để Gson biết cách parse chuỗi JSON
            val listType = object : TypeToken<List<TouristLocation>>() {}.type

            // Yêu cầu Gson chuyển đổi chuỗi JSON thành danh sách các đối tượng TouristLocation
            val parsedLocations: List<TouristLocation> = Gson().fromJson(jsonString, listType)

            // Duyệt qua danh sách vừa tạo, sao chép (copy) và gán thêm ID của file ảnh (imageRes) tương ứng với từng địa điểm
            parsedLocations.map { location ->
                location.copy(imageRes = mapImageRes(location.id, location.name))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback: trả về danh sách mặc định nếu có lỗi
            // Cơ chế an toàn (Fallback): Nếu việc đọc file JSON thất bại (không tìm thấy file, sai định dạng...), sẽ trả về danh sách cứng để app không bị crash
            getDefaultLocations()
        }
    }

    /**
     * Danh sách mặc định (fallback)
     */
    // Hàm chứa dữ liệu dự phòng được code cứng (hardcode) trực tiếp
    private fun getDefaultLocations(): List<TouristLocation> {
        return listOf(
            TouristLocation(
                id = 1,
                name = "Tràng An",
                description = "Khu du lịch sinh thái Tràng An - Di sản thế giới UNESCO",
                latitude = 20.2525,
                longitude = 105.9000,
                imageRes = R.drawable.trang_an
            ),
            TouristLocation(
                id = 2,
                name = "Tam Cốc - Bích Động",
                description = "Vịnh Hạ Long trên cạn, nổi tiếng với 3 hang động",
                latitude = 20.2167,
                longitude = 105.9333,
                imageRes = R.drawable.tam_coc_dong
            ),
            TouristLocation(
                id = 3,
                name = "Cố đô Hoa Lư",
                description = "Kinh đô đầu tiên của nhà nước phong kiến Việt Nam",
                latitude = 20.2803,
                longitude = 105.9058,
                imageRes = R.drawable.co_do_hoa_lu
            ),
            TouristLocation(
                id = 4,
                name = "Chùa Bái Đính",
                description = "Chùa lớn nhất Đông Nam Á với nhiều kỷ lục Việt Nam",
                latitude = 20.3014,
                longitude = 105.8639,
                imageRes = R.drawable.chua_bai_dinh
            ),
            TouristLocation(
                id = 5,
                name = "Vườn Quốc gia Cúc Phương",
                description = "Vườn quốc gia đầu tiên của Việt Nam",
                latitude = 20.3500,
                longitude = 105.5833,
                imageRes = R.drawable.cuc_phuong
            ),
            TouristLocation(
                id = 6,
                name = "Kênh Gà - Vân Trình",
                description = "Khu du lịch sinh thái suối khoáng nổi tiếng",
                latitude = 20.3892,
                longitude = 105.8681,
                imageRes = R.drawable.kenh_ga
            ),
            TouristLocation(
                id = 7,
                name = "Nhà thờ đá Phát Diệm",
                description = "Công trình kiến trúc độc đáo kết hợp Đông - Tây",
                latitude = 20.0922,
                longitude = 106.0917,
                imageRes = R.drawable.phat_diem
            ),
            TouristLocation(
                id = 8,
                name = "Hồ Đồng Chương",
                description = "Hồ nước ngọt đẹp giữa vùng núi đá vôi",
                latitude = 20.2700,
                longitude = 105.8100,
                imageRes = R.drawable.ho_dong_chuong
            )
        )
    }

    // Hàm tiện ích để tìm và gán resource file ảnh local (R.drawable...) dựa vào ID hoặc Tên của địa điểm
    private fun mapImageRes(id: Int, name: String): Int {
        return when (id) {
            // Ưu tiên kiểm tra đối chiếu (map) theo ID trước vì độ chính xác cao nhất
            1 -> R.drawable.trang_an
            2 -> R.drawable.tam_coc_dong
            3 -> R.drawable.co_do_hoa_lu
            4 -> R.drawable.chua_bai_dinh
            5 -> R.drawable.cuc_phuong
            6 -> R.drawable.kenh_ga
            7 -> R.drawable.phat_diem
            8 -> R.drawable.ho_dong_chuong
            9 -> R.drawable.dong_thien_ha
            10 -> R.drawable.dam_van_long
            11 -> R.drawable.chua_bao_thap
            12 -> R.drawable.tran_thuong
            else -> {
                // Nếu ID không khớp (có thể do dữ liệu JSON mới thêm vào nhưng code chưa cập nhật), chuyển sang kiểm tra chứa chuỗi dựa vào Tên (fallback)
                val normalizedName = name.lowercase()
                when {
                    normalizedName.contains("tràng an") || normalizedName.contains("trang an") -> R.drawable.trang_an
                    normalizedName.contains("tam cốc") || normalizedName.contains("tam coc") -> R.drawable.tam_coc_dong
                    normalizedName.contains("hoa lư") || normalizedName.contains("hoa lu") -> R.drawable.co_do_hoa_lu
                    normalizedName.contains("bái đính") || normalizedName.contains("bai dinh") -> R.drawable.chua_bai_dinh
                    normalizedName.contains("cúc phương") || normalizedName.contains("cuc phuong") -> R.drawable.cuc_phuong
                    normalizedName.contains("kênh gà") || normalizedName.contains("kenh ga") -> R.drawable.kenh_ga
                    normalizedName.contains("phát diệm") || normalizedName.contains("phat diem") -> R.drawable.phat_diem
                    normalizedName.contains("đồng chương") || normalizedName.contains("dong chuong") -> R.drawable.ho_dong_chuong
                    normalizedName.contains("thiên hà") || normalizedName.contains("thien ha") -> R.drawable.dong_thien_ha
                    normalizedName.contains("vân long") || normalizedName.contains("van long") -> R.drawable.dam_van_long
                    normalizedName.contains("bảo tháp") || normalizedName.contains("bao thap") -> R.drawable.chua_bao_thap
                    normalizedName.contains("trần thương") || normalizedName.contains("tran thuong") -> R.drawable.tran_thuong
                    // Trả về ảnh nền mặc định nếu không khớp với bất kỳ điều kiện nào
                    else -> R.drawable.ic_launcher_background
                }
            }
        }
    }
}