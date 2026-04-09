package com.example.map.ui.map

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import com.example.map.R
import com.example.map.databinding.ActivityLocationDetailBinding
import java.util.Locale

class LocationDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLocationDetailBinding

    private var locationId: Int = -1
    private var locationName: String = ""
    private var locationDesc: String = ""
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0
    private var imageRes: Int = 0
    private var imageName: String = ""
    private var category: String = "photo"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLocationDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        readExtras()
        bindLocationData()
        setupActions()
    }

    private fun readExtras() {
        locationId = intent.getIntExtra(HomeActivity.EXTRA_LOCATION_ID, -1)
        locationName = intent.getStringExtra(HomeActivity.EXTRA_LOCATION_NAME).orEmpty()
        locationDesc = intent.getStringExtra(HomeActivity.EXTRA_LOCATION_DESC).orEmpty()
        latitude = intent.getDoubleExtra(HomeActivity.EXTRA_LOCATION_LAT, 0.0)
        longitude = intent.getDoubleExtra(HomeActivity.EXTRA_LOCATION_LNG, 0.0)
        imageRes = intent.getIntExtra(HomeActivity.EXTRA_LOCATION_IMAGE_RES, 0)
        imageName = intent.getStringExtra(HomeActivity.EXTRA_LOCATION_IMAGE_NAME).orEmpty()
        category = intent.getStringExtra(HomeActivity.EXTRA_LOCATION_CATEGORY).orEmpty().ifBlank { "photo" }
    }

    private fun bindLocationData() {
        binding.tvName.text = locationName
        binding.tvDescription.text = locationDesc
        binding.tvCategory.text = categoryLabel(category)
        binding.tvAddress.text = getString(R.string.detail_address_format, locationName)
        binding.tvRating.text = ratingForLocation(locationId)
        binding.tvOpenTime.text = openTimeForCategory(category)
        binding.tvCostInfo.text = costInfoForCategory(category)
        binding.tvTips.text = tipsForCategory(category)

        val headerRes = when {
            imageRes != 0 -> imageRes
            else -> resolveImageRes(imageName)
        }
        binding.imgHeader.setImageResource(headerRes)
    }

    private fun setupActions() {
        binding.btnBack.setOnClickListener { finish() }

        binding.btnOpenMap.setOnClickListener {
            val mapIntent = Intent(this, MapActivity::class.java).apply {
                putExtra(HomeActivity.EXTRA_LOCATION_ID, locationId)
                putExtra(HomeActivity.EXTRA_LOCATION_NAME, locationName)
                putExtra(HomeActivity.EXTRA_LOCATION_DESC, locationDesc)
                putExtra(HomeActivity.EXTRA_LOCATION_LAT, latitude)
                putExtra(HomeActivity.EXTRA_LOCATION_LNG, longitude)
                putExtra(HomeActivity.EXTRA_LOCATION_IMAGE_RES, imageRes)
                putExtra(HomeActivity.EXTRA_LOCATION_IMAGE_NAME, imageName)
                putExtra(HomeActivity.EXTRA_LOCATION_CATEGORY, category)
            }
            startActivity(mapIntent)
        }

        binding.btnDirections.setOnClickListener {
            val navUri = "google.navigation:q=$latitude,$longitude&mode=d".toUri()
            val navIntent = Intent(Intent.ACTION_VIEW, navUri).apply {
                setPackage("com.google.android.apps.maps")
            }
            if (navIntent.resolveActivity(packageManager) != null) {
                startActivity(navIntent)
            } else {
                val webUri = "https://www.google.com/maps/dir/?api=1&destination=$latitude,$longitude".toUri()
                startActivity(Intent(Intent.ACTION_VIEW, webUri))
            }
        }
    }

    private fun resolveImageRes(name: String): Int {
        return when (name) {
            "trang_an" -> R.drawable.trang_an
            "tam_coc" -> R.drawable.tam_coc
            "co_do" -> R.drawable.co_do
            "chua_bai_dinh" -> R.drawable.chua_bai_dinh
            "cuc_phuong" -> R.drawable.cuc_phuong
            "kenh_ga" -> R.drawable.kenh_ga
            "phat_diem" -> R.drawable.phat_diem
            "ho_dong_chuong" -> R.drawable.ho_dong_chuong
            "dong_thien_ha" -> R.drawable.dong_thien_ha
            "dam_van_long" -> R.drawable.dam_van_long
            "chua_bao_thap" -> R.drawable.chua_bao_thap
            "den_tran_huong" -> R.drawable.den_tran_huong
            else -> R.drawable.ic_launcher_background
        }
    }

    private fun categoryLabel(value: String): String {
        return when (value) {
            "spiritual" -> getString(R.string.home_category_spiritual)
            "nature" -> getString(R.string.home_category_nature)
            else -> getString(R.string.home_category_photo)
        }
    }

    private fun ratingForLocation(id: Int): String {
        val rating = when (id % 5) {
            0 -> 4.9
            1 -> 4.8
            2 -> 4.7
            3 -> 4.6
            else -> 4.5
        }
        val reviews = 10000 + (id * 987)
        val ratingText = String.format(Locale.US, "%.1f", rating)
        return "⭐ $ratingText ($reviews đánh giá)"
    }

    private fun openTimeForCategory(value: String): String {
        return when (value) {
            "spiritual" -> "🕒 05:00 - 21:00"
            "nature" -> "🕒 06:00 - 18:30"
            else -> "🕒 06:00 - 20:00"
        }
    }

    private fun costInfoForCategory(value: String): String {
        return when (value) {
            "spiritual" -> "• Vé tham quan: Miễn phí\n• Xe điện: 60.000đ/người\n• Dâng hương: 50.000đ - 200.000đ"
            "nature" -> "• Vé tham quan: 100.000đ/người\n• Thuyền: 150.000đ/chuyến\n• Bãi xe: 10.000đ"
            else -> "• Vé vào cổng: 120.000đ/người\n• Thuê trang phục: 80.000đ\n• Gửi xe: 10.000đ"
        }
    }

    private fun tipsForCategory(value: String): String {
        return when (value) {
            "spiritual" -> "• Mặc lịch sự khi vào khu tâm linh.\n• Đi sớm để tránh đông và nắng gắt.\n• Mang nước uống và giày mềm để đi bộ lâu."
            "nature" -> "• Nên mang mũ, kem chống nắng và thuốc côn trùng.\n• Chuẩn bị giày chống trơn khi đi thuyền hoặc leo dốc.\n• Không xả rác để bảo vệ cảnh quan thiên nhiên."
            else -> "• Chọn khung giờ sáng sớm hoặc chiều muộn để chụp ảnh đẹp.\n• Mang pin dự phòng vì khu tham quan rộng.\n• Luôn kiểm tra thời tiết trước khi đi."
        }
    }
}
