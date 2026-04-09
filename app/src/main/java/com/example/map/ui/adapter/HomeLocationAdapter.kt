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

class HomeLocationAdapter(
    private val onCardClick: (TouristLocation) -> Unit,
    private val onAddPlanClick: (TouristLocation) -> Unit
) : ListAdapter<TouristLocation, HomeLocationAdapter.LocationViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_home_location, parent, false)
        return LocationViewHolder(view, onCardClick, onAddPlanClick)
    }

    override fun onBindViewHolder(holder: LocationViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class LocationViewHolder(
        itemView: View,
        private val onCardClick: (TouristLocation) -> Unit,
        private val onAddPlanClick: (TouristLocation) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val tvName: TextView = itemView.findViewById(R.id.tvName)
        private val tvDesc: TextView = itemView.findViewById(R.id.tvDescription)
        private val tvCategoryTag: TextView = itemView.findViewById(R.id.tvCategoryTag)
        private val tvAddress: TextView = itemView.findViewById(R.id.tvAddress)
        private val tvRating: TextView = itemView.findViewById(R.id.tvRating)
        private val imgPreview: ImageView = itemView.findViewById(R.id.imgPreview)
        private val btnMap: View? = itemView.findViewById(R.id.btnOpenMap)

        fun bind(item: TouristLocation) {
            val context = itemView.context

            tvName.text = item.name
            tvDesc.text = item.description
            tvAddress.text = context.getString(R.string.home_default_address)
            tvRating.text = context.getString(R.string.home_default_rating)
            tvCategoryTag.text = when (detectCategory(item)) {
                "spiritual" -> context.getString(R.string.home_category_spiritual)
                "nature" -> context.getString(R.string.home_category_nature)
                else -> context.getString(R.string.home_category_photo)
            }

            val previewResId = when {
                item.imageRes != 0 -> item.imageRes
                else -> resolveImageRes(item.imageName)
            }
            imgPreview.setImageResource(previewResId)
            imgPreview.contentDescription = item.name

            itemView.setOnClickListener { onCardClick(item) }
            btnMap?.setOnClickListener { onAddPlanClick(item) }
        }

        private fun resolveImageRes(imageName: String): Int {
            return when (imageName) {
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

        private fun detectCategory(item: TouristLocation): String {
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
                else -> "photo"
            }
        }
    }

    private object DiffCallback : DiffUtil.ItemCallback<TouristLocation>() {
        override fun areItemsTheSame(oldItem: TouristLocation, newItem: TouristLocation): Boolean {
            return oldItem.name == newItem.name
        }

        override fun areContentsTheSame(oldItem: TouristLocation, newItem: TouristLocation): Boolean {
            return oldItem == newItem
        }
    }
}