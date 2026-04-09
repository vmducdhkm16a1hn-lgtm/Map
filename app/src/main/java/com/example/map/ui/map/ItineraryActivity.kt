package com.example.map.ui.map

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.map.R
import com.example.map.data.model.TouristLocation
import com.example.map.data.repository.ItineraryRepository
import com.example.map.databinding.ActivityItineraryBinding
import com.example.map.ui.adapter.ItineraryAdapter

class ItineraryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityItineraryBinding
    private lateinit var adapter: ItineraryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityItineraryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupRecyclerView()
    }

    override fun onResume() {
        super.onResume()
        loadItinerary()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupRecyclerView() {
        adapter = ItineraryAdapter { location ->
            removeFromItinerary(location)
        }
        binding.rvItinerary.layoutManager = LinearLayoutManager(this)
        binding.rvItinerary.adapter = adapter
    }

    private fun loadItinerary() {
        val items = ItineraryRepository.getAll(this)
        adapter.submitList(items)
        binding.tvEmpty.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun removeFromItinerary(location: TouristLocation) {
        ItineraryRepository.remove(this, location.id)
        Toast.makeText(this, getString(R.string.itinerary_removed, location.name), Toast.LENGTH_SHORT).show()
        loadItinerary()
    }
}

