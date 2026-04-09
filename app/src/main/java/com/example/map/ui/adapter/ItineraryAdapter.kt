package com.example.map.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.map.data.model.TouristLocation
import com.example.map.databinding.ItemItineraryLocationBinding

class ItineraryAdapter(
    private val onRemoveClick: (TouristLocation) -> Unit
) : ListAdapter<TouristLocation, ItineraryAdapter.ItineraryViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItineraryViewHolder {
        val binding = ItemItineraryLocationBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ItineraryViewHolder(binding, onRemoveClick)
    }

    override fun onBindViewHolder(holder: ItineraryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ItineraryViewHolder(
        private val binding: ItemItineraryLocationBinding,
        private val onRemoveClick: (TouristLocation) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: TouristLocation) {
            binding.tvName.text = item.name
            binding.tvDescription.text = item.description
            binding.btnRemove.setOnClickListener { onRemoveClick(item) }
        }
    }

    private object DiffCallback : DiffUtil.ItemCallback<TouristLocation>() {
        override fun areItemsTheSame(oldItem: TouristLocation, newItem: TouristLocation): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: TouristLocation, newItem: TouristLocation): Boolean {
            return oldItem == newItem
        }
    }
}

