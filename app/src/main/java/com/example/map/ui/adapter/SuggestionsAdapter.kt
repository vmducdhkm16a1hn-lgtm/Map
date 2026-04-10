package com.example.map.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.map.R
import com.example.map.data.model.TouristLocation

/**
 * SuggestionsAdapter - Adapter cho RecyclerView hiển thị gợi ý tìm kiếm
 */
class SuggestionsAdapter(
    private val onItemClick: (TouristLocation) -> Unit
) : RecyclerView.Adapter<SuggestionsAdapter.SuggestionViewHolder>() {

    // Danh sách gợi ý
    private val suggestions = mutableListOf<TouristLocation>()

    // ViewHolder cho mỗi item
    class SuggestionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgSuggestion: ImageView = itemView.findViewById(R.id.imgSuggestion)
        val tvSuggestionTitle: TextView = itemView.findViewById(R.id.tvSuggestionTitle)
        val tvSuggestionDesc: TextView = itemView.findViewById(R.id.tvSuggestionDesc)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SuggestionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_suggestion, parent, false)
        return SuggestionViewHolder(view)
    }

    override fun onBindViewHolder(holder: SuggestionViewHolder, position: Int) {
        val suggestion = suggestions[position]
        holder.tvSuggestionTitle.text = suggestion.name
        holder.tvSuggestionDesc.text = suggestion.description

        val imageResId = if (suggestion.imageRes != 0) {
            suggestion.imageRes
        } else {
            resolveImageRes(suggestion.imageName)
        }
        holder.imgSuggestion.setImageResource(imageResId)
        holder.imgSuggestion.contentDescription = suggestion.name

        // Xử lý click vào suggestion
        holder.itemView.setOnClickListener {
            onItemClick(suggestion)
        }
    }

    override fun getItemCount(): Int = suggestions.size

    // Update danh sách gợi ý
    fun updateSuggestions(newSuggestions: List<TouristLocation>) {
        val oldSize = suggestions.size
        suggestions.clear()
        if (oldSize > 0) {
            notifyItemRangeRemoved(0, oldSize)
        }

        if (newSuggestions.isNotEmpty()) {
            suggestions.addAll(newSuggestions)
            notifyItemRangeInserted(0, newSuggestions.size)
        }
    }

    // Clear tất cả suggestions
    fun clearSuggestions() {
        val oldSize = suggestions.size
        if (oldSize == 0) return

        suggestions.clear()
        notifyItemRangeRemoved(0, oldSize)
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
}