package com.example.map.ui.adater

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.map.R

/**
 * SuggestionsAdapter - Adapter cho RecyclerView hiển thị gợi ý tìm kiếm
 */
class SuggestionsAdapter(
    private val onItemClick: (String) -> Unit
) : RecyclerView.Adapter<SuggestionsAdapter.SuggestionViewHolder>() {

    // Danh sách gợi ý
    private val suggestions = mutableListOf<String>()

    // ViewHolder cho mỗi item
    class SuggestionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvSuggestion: TextView = itemView.findViewById(R.id.tvSuggestion)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SuggestionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_suggestion, parent, false)
        return SuggestionViewHolder(view)
    }

    override fun onBindViewHolder(holder: SuggestionViewHolder, position: Int) {
        val suggestion = suggestions[position]
        holder.tvSuggestion.text = suggestion

        // Xử lý click vào suggestion
        holder.itemView.setOnClickListener {
            onItemClick(suggestion)
        }
    }

    override fun getItemCount(): Int = suggestions.size

    // Update danh sách gợi ý
    fun updateSuggestions(newSuggestions: List<String>) {
        suggestions.clear()
        suggestions.addAll(newSuggestions)
        notifyDataSetChanged()
    }

    // Clear tất cả suggestions
    fun clearSuggestions() {
        suggestions.clear()
        notifyDataSetChanged()
    }
}