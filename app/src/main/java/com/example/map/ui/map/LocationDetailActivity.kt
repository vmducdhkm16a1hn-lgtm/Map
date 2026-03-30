package com.example.map.ui.map

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.map.R
import com.example.map.data.model.TouristLocation
import com.example.map.databinding.ActivityLocationBinding

// Màn hình hiển thị thông tin chi tiết của một địa điểm du lịch
class LocationDetailActivity : AppCompatActivity() {

    // ViewBinding giúp truy xuất các view an toàn và nhanh chóng từ activity_location.xml
    private lateinit var binding: ActivityLocationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLocationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Lấy dữ liệu được truyền từ HomeActivity sang thông qua Intent
        // Sử dụng .orEmpty() và giá trị mặc định để tránh lỗi NullPointerException nếu không có dữ liệu
        val name = intent.getStringExtra(EXTRA_LOCATION_NAME).orEmpty()
        val desc = intent.getStringExtra(EXTRA_LOCATION_DESC).orEmpty()
        val id = intent.getIntExtra(EXTRA_LOCATION_ID, -1)
        val lat = intent.getDoubleExtra(EXTRA_LOCATION_LAT, 0.0)
        val lng = intent.getDoubleExtra(EXTRA_LOCATION_LNG, 0.0)
        val imageRes = intent.getIntExtra(EXTRA_LOCATION_IMAGE_RES, R.drawable.ic_launcher_background)

        // Tái tạo lại đối tượng TouristLocation từ các dữ liệu vừa nhận được
        val location = TouristLocation(
            id = id,
            name = name,
            description = desc,
            latitude = lat,
            longitude = lng,
            imageRes = imageRes
        )

        // Gọi các hàm để hiển thị dữ liệu lên UI và thiết lập sự kiện click
        bindData(location)
        setupActions(location)
    }

    // Hàm gắn dữ liệu của địa điểm vào các View tương ứng trên giao diện
    private fun bindData(location: TouristLocation) {
        // Lấy thêm các thông tin phụ (Metadata) được tạo tự động dựa trên tên/mô tả
        val meta = buildMeta(location)

        // Kiểm tra xem địa điểm có ảnh không, nếu không có thì dùng ảnh mặc định
        val headerImageRes = if (location.imageRes != 0) location.imageRes else R.drawable.ic_launcher_background

        // Gắn dữ liệu vào các TextView và ImageView
        binding.ivDetailImage.setImageResource(headerImageRes)
        binding.tvDetailName.text = location.name
        binding.tvDetailRating.text = "⭐ ${meta.rating} • ${meta.reviewCount} lượt đánh giá"
        binding.tvDetailIntro.text = "Giới thiệu: ${location.description}"
        binding.tvDetailAddress.text = "📍 Địa chỉ: ${meta.address}"
        binding.tvDetailOpenClose.text = "🕒 Mở cửa: ${meta.openTime} - ${meta.closeTime}"
        binding.tvDetailCost.text = "💸 Vé tham quan: ${meta.ticketCost} | Di chuyển: ${meta.transportCost}"

        // Format tọa độ để chỉ hiển thị 6 chữ số thập phân cho gọn gàng
        binding.tvDetailCoordinates.text = "🧭 Tọa độ: ${"%.6f".format(location.latitude)}, ${"%.6f".format(location.longitude)}"
        binding.tvDetailDuration.text = "⏱ Thời gian tham quan gợi ý: ${meta.recommendedDuration}"
        binding.tvDetailBestTime.text = "🌤 Thời điểm phù hợp: ${meta.bestTimeToVisit}"
        binding.tvDetailHighlights.text = "✨ Điểm nổi bật: ${meta.highlights}"
        binding.tvDetailTips.text = "📝 Lưu ý: ${meta.travelTips}"
    }

    // Hàm thiết lập sự kiện click cho các nút bấm trên giao diện
    private fun setupActions(location: TouristLocation) {
        // Nút Back: Đóng màn hình hiện tại và quay lại màn hình trước đó
        binding.btnBack.setOnClickListener { finish() }

        // Nút Thêm vào lịch trình: Hiện tại chỉ hiển thị một Toast thông báo (có thể phát triển thêm logic lưu Database sau)
        binding.btnAddToPlan.setOnClickListener {
            Toast.makeText(
                this,
                "Đã thêm '${location.name}' vào lịch trình",
                Toast.LENGTH_SHORT
            ).show()
        }

        // Nút Chỉ đường: Mở lại MapActivity và truyền theo thông tin địa điểm cùng cờ EXTRA_FOCUS_DESTINATION để bản đồ tự động zoom đến đó
        binding.btnDetailDirections.setOnClickListener {
            val intent = Intent(this, MapActivity::class.java).apply {
                putExtra(HomeActivity.EXTRA_LOCATION_NAME, location.name)
                putExtra(HomeActivity.EXTRA_LOCATION_DESC, location.description)
                putExtra(HomeActivity.EXTRA_LOCATION_LAT, location.latitude)
                putExtra(HomeActivity.EXTRA_LOCATION_LNG, location.longitude)
                putExtra(HomeActivity.EXTRA_FOCUS_DESTINATION, true)
            }
            startActivity(intent)
        }
    }

    // Data class nội bộ dùng để nhóm các thông tin chi tiết giả lập (Metadata) của địa điểm
    private data class LocationMeta(
        val address: String,
        val openTime: String,
        val closeTime: String,
        val ticketCost: String,
        val transportCost: String,
        val rating: String,
        val reviewCount: String,
        val recommendedDuration: String,
        val bestTimeToVisit: String,
        val highlights: String,
        val travelTips: String
    )

    // Hàm tạo dữ liệu giả (Mock data) chi tiết dựa trên phân loại của địa điểm (Tâm linh, Thiên nhiên, Khác)
    // Việc này giúp giao diện nhìn phong phú hơn mà không cần phải có sẵn một cục Database khổng lồ
    private fun buildMeta(location: TouristLocation): LocationMeta {
        val text = "${location.name} ${location.description}".lowercase()
        val isSpiritual = text.contains("chùa") || text.contains("đền") || text.contains("cố đô")
        val isNature = text.contains("vườn") || text.contains("hồ") || text.contains("vân long") || text.contains("kênh gà")

        return when {
            isSpiritual -> LocationMeta(
                address = "${location.name}, Ninh Bình",
                openTime = "06:00",
                closeTime = "18:00",
                ticketCost = "50.000đ - 150.000đ",
                transportCost = "30.000đ - 120.000đ",
                rating = "4.7",
                reviewCount = "2.100+",
                recommendedDuration = "2 - 3 giờ",
                bestTimeToVisit = "Sáng sớm hoặc chiều mát",
                highlights = "Không gian tâm linh cổ kính, kiến trúc đẹp, phù hợp tham quan và chụp ảnh",
                travelTips = "Nên ăn mặc lịch sự, đi giày dễ di chuyển và tránh giờ cao điểm ngày lễ"
            )
            isNature -> LocationMeta(
                address = "${location.name}, Ninh Bình",
                openTime = "07:00",
                closeTime = "17:30",
                ticketCost = "80.000đ - 250.000đ",
                transportCost = "40.000đ - 180.000đ",
                rating = "4.8",
                reviewCount = "3.400+",
                recommendedDuration = "3 - 5 giờ",
                bestTimeToVisit = "Tháng 11 đến tháng 4, trời khô ráo",
                highlights = "Cảnh quan thiên nhiên rộng, nhiều góc check-in, trải nghiệm thuyền hoặc trekking",
                travelTips = "Chuẩn bị nước uống, mũ/nón, kem chống nắng; nên đi sớm để có ánh sáng đẹp"
            )
            else -> LocationMeta(
                address = "${location.name}, Ninh Bình",
                openTime = "08:00",
                closeTime = "22:00",
                ticketCost = "Miễn phí - 120.000đ",
                transportCost = "20.000đ - 100.000đ",
                rating = "4.6",
                reviewCount = "1.200+",
                recommendedDuration = "1 - 2 giờ",
                bestTimeToVisit = "Cuối chiều đến tối",
                highlights = "Dễ tiếp cận, nhiều dịch vụ ăn uống và tiện ích xung quanh",
                travelTips = "Kiểm tra thời tiết trước khi đi; ưu tiên gửi xe gần cổng chính để tiết kiệm thời gian"
            )
        }
    }

    // Nơi định nghĩa các key (khóa) để truyền dữ liệu qua lại giữa các Activity một cách đồng nhất, tránh gõ sai
    companion object {
        const val EXTRA_LOCATION_ID = "extra_location_id"
        const val EXTRA_LOCATION_NAME = "extra_location_name"
        const val EXTRA_LOCATION_DESC = "extra_location_desc"
        const val EXTRA_LOCATION_LAT = "extra_location_lat"
        const val EXTRA_LOCATION_LNG = "extra_location_lng"
        const val EXTRA_LOCATION_IMAGE_RES = "extra_location_image_res"
    }
}