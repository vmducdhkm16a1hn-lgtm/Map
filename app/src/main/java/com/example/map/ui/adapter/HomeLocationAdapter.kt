package com.example.map.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.map.R
import com.example.map.data.model.TouristLocation

// Adapter sử dụng ListAdapter thay vì RecyclerView.Adapter thông thường.
// ListAdapter kết hợp với DiffUtil giúp tối ưu hiệu năng: tự động tính toán sự khác biệt giữa list cũ và list mới để chỉ cập nhật những item thay đổi thay vì reload lại toàn bộ list.
class HomeLocationAdapter(
    // Lambda function để truyền sự kiện click từ Adapter ra ngoài Activity xử lý
    private val onCardClick: (TouristLocation) -> Unit
) : ListAdapter<TouristLocation, HomeLocationAdapter.LocationViewHolder>(DiffCallback) {

    // Hàm này được gọi khi RecyclerView cần tạo một View mới (chỉ tạo đủ số lượng View hiển thị trên màn hình + một vài view dự phòng)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocationViewHolder {
        // Nạp (inflate) giao diện của từng item từ file XML (item_home_location.xml)
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_home_location, parent, false)
        return LocationViewHolder(view, onCardClick)
    }

    // Hàm này được gọi khi RecyclerView cần đổ dữ liệu (bind) vào một ViewHolder đã được tạo sẵn khi người dùng cuộn danh sách
    override fun onBindViewHolder(holder: LocationViewHolder, position: Int) {
        // Lấy item tại vị trí hiện tại và truyền vào hàm bind của ViewHolder
        holder.bind(getItem(position))
    }

    // Lớp ViewHolder giữ nhiệm vụ tìm và lưu trữ (cache) các tham chiếu đến các view con  bên trong item, giúp tránh việc gọi findViewById nhiều lần gây giật lag.
    class LocationViewHolder(
        itemView: View,
        private val onCardClick: (TouristLocation) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        // Ánh xạ các view từ XML
        private val tvName: TextView = itemView.findViewById(R.id.tvName)
        private val tvDesc: TextView = itemView.findViewById(R.id.tvDescription)
        private val tvCategoryTag: TextView = itemView.findViewById(R.id.tvCategoryTag)
        private val tvAddress: TextView = itemView.findViewById(R.id.tvAddress)
        private val tvRating: TextView = itemView.findViewById(R.id.tvRating)
        private val imgPreview: ImageView = itemView.findViewById(R.id.imgPreview)

        // Hàm gán dữ liệu từ model TouristLocation lên các View tương ứng
        fun bind(item: TouristLocation) {
            tvName.text = item.name
            tvDesc.text = item.description

            // Dữ liệu đang được hardcode tạm thời cho mục đích hiển thị UI
            tvAddress.text = "📍 Ninh Bình"
            tvRating.text = "⭐ 4.8"

            // Gán nhãn thể loại dựa trên hàm detectCategory
            tvCategoryTag.text = when (detectCategory(item)) {
                "spiritual" -> "🏛 Du lịch tâm linh"
                "nature" -> "🌿 Thiên nhiên"
                else -> "📸 Điểm chụp ảnh"
            }

            // Kiểm tra nếu item có hình ảnh thì hiển thị, nếu bằng 0 (chưa có) thì hiển thị ảnh mặc định
            val imageRes = if (item.imageRes != 0) item.imageRes else R.drawable.ic_launcher_background
            imgPreview.setImageResource(imageRes)
            imgPreview.contentDescription = item.name // Hỗ trợ trợ năng cho người khiếm thị đọc nội dung ảnh

            // Gán sự kiện click cho toàn bộ thẻ (item)
            itemView.setOnClickListener { onCardClick(item) }
        }

        // Hàm tự động phân loại địa điểm (Tâm linh, Thiên nhiên, Chụp ảnh) bằng cách phân tích từ khóa trong Tên và Mô tả
        private fun detectCategory(item: TouristLocation): String {
            // Chuyển tất cả về chữ thường để dễ dàng so sánh
            val text = "${item.name} ${item.description}".lowercase()
            return when {
                text.contains("chua") || text.contains("chùa") ||
                        text.contains("den") || text.contains("đền") ||
                        text.contains("nha tho") || text.contains("nhà thờ") ||
                        text.contains("co do") || text.contains("cố đô") -> "spiritual"
                text.contains("vuon") || text.contains("vườn") ||
                        text.contains("ho ") || text.contains("hồ") ||
                        text.contains("van long") || text.contains("vân long") ||
                        text.contains("kenh ga") || text.contains("kênh gà") -> "nature"
                else -> "photo" // Thể loại mặc định nếu không khớp từ khóa nào
            }
        }
    }

    // Object này cung cấp quy tắc để ListAdapter so sánh các phần tử trong danh sách khi có sự thay đổi
    private object DiffCallback : DiffUtil.ItemCallback<TouristLocation>() {
        // So sánh xem 2 item có phải là CÙNG MỘT đối tượng không (thường dùng ID để so sánh, ở đây bạn đang dùng name)
        override fun areItemsTheSame(oldItem: TouristLocation, newItem: TouristLocation): Boolean {
            return oldItem.name == newItem.name
        }

        // Nếu 2 item là cùng một đối tượng, kiểm tra xem nội dung bên trong của chúng có bị thay đổi không. Data class trong Kotlin tự động overide hàm equals() nên so sánh rất chính xác.
        override fun areContentsTheSame(oldItem: TouristLocation, newItem: TouristLocation): Boolean {
            return oldItem == newItem
        }
    }
}