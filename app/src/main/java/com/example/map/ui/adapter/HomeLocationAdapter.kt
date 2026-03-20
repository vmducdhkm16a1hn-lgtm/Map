package com.example.map.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.example.map.R
import com.example.map.data.model.TouristLocation

class HomeLocationAdapter(
    private val onCardClick: (TouristLocation) -> Unit,
    private val onMapClick: (TouristLocation) -> Unit
) : ListAdapter<TouristLocation, HomeLocationAdapter.LocationViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_home_location, parent, false)
        return LocationViewHolder(view, onCardClick, onMapClick)
    }

    override fun onBindViewHolder(holder: LocationViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class LocationViewHolder(
        itemView: View,
        private val onCardClick: (TouristLocation) -> Unit,
        private val onMapClick: (TouristLocation) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val tvName: TextView = itemView.findViewById(R.id.tvName)
        private val tvDesc: TextView = itemView.findViewById(R.id.tvDescription)
        private val tvCategoryTag: TextView = itemView.findViewById(R.id.tvCategoryTag)
        private val tvAddress: TextView = itemView.findViewById(R.id.tvAddress)
        private val tvRating: TextView = itemView.findViewById(R.id.tvRating)
        private val imgPreview: ImageView = itemView.findViewById(R.id.imgPreview)
        private val btnMap: View? = itemView.findViewById(R.id.btnOpenMap)

        fun bind(item: TouristLocation) {
            tvName.text = item.name
            tvDesc.text = item.description
            tvAddress.text = "📍 ${item.address}"
            val reviewText = if (item.reviewCount > 0) " (${formatCount(item.reviewCount)})" else ""
            tvRating.text = "⭐ ${"%.1f".format(item.rating)}$reviewText"
            tvCategoryTag.text = when (detectCategory(item)) {
                "spiritual" -> "🏛 Du lịch tâm linh"
                "nature" -> "🌿 Thiên nhiên"
                else -> "📸 Điểm chụp ảnh"
            }

            // Load image using Glide
            if (item.imageUrl.isNotEmpty()) {
                Glide.with(itemView.context)
                    .load(item.imageUrl)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .placeholder(R.drawable.ic_launcher_background)
                    .error(R.drawable.ic_launcher_background)
                    .centerCrop()
                    .into(imgPreview)
            } else if (item.imageRes != 0) {
                Glide.with(itemView.context)
                    .load(item.imageRes)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .centerCrop()
                    .into(imgPreview)
            } else {
                imgPreview.setImageResource(R.drawable.ic_launcher_background)
            }
            imgPreview.contentDescription = item.name

            itemView.setOnClickListener { onCardClick(item) }
            btnMap?.setOnClickListener { onMapClick(item) }
        }

        private fun formatCount(count: Int): String {
            return if (count >= 1000) "${"%.1f".format(count / 1000.0)}k" else count.toString()
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