package com.example.map.ui.map

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.map.R
import com.example.map.data.model.TouristLocation
import com.example.map.data.repository.ItineraryRepository
import com.example.map.data.repository.LocationRepository
import com.example.map.databinding.ActivityHomeBinding
import com.example.map.ui.adapter.HomeLocationAdapter
import com.example.map.ui.adapter.SuggestionsAdapter

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var adapter: HomeLocationAdapter
    private lateinit var suggestionsAdapter: SuggestionsAdapter

    private var allLocations: List<TouristLocation> = emptyList()
    private var selectedCategory: String = CATEGORY_ALL
    private var searchKeyword: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupItineraryButton()
        setupSearch()
        setupCategoryChips()
        loadData()
        updateItineraryButtonCount()
    }

    override fun onResume() {
        super.onResume()
        // Đồng bộ lại số lượng lịch trình khi quay về Home
        updateItineraryButtonCount()
    }

    private fun setupRecyclerView() {
        adapter = HomeLocationAdapter(
            onCardClick = { openLocationDetail(it) },
            onAddPlanClick = { addToItinerary(it) }
        )
        binding.rvLocations.layoutManager = LinearLayoutManager(this)
        binding.rvLocations.adapter = adapter

        suggestionsAdapter = SuggestionsAdapter { selectedLocation ->
            binding.searchView.setQuery(selectedLocation.name, false)
            searchKeyword = selectedLocation.name
            applyFilter()
            showSuggestions(false)
            binding.searchView.clearFocus()
            openLocationDetail(selectedLocation)
        }
        binding.rvSuggestions.layoutManager = LinearLayoutManager(this)
        binding.rvSuggestions.adapter = suggestionsAdapter
    }

    private fun setupItineraryButton() {
        binding.btnItinerary.setOnClickListener {
            startActivity(Intent(this, ItineraryActivity::class.java))
        }
    }

    private fun updateItineraryButtonCount() {
        val count = ItineraryRepository.getAll(this).size
        binding.btnItinerary.text = getString(R.string.home_itinerary_count, count)
    }

    private fun setupSearch() {
        binding.searchView.apply {
            isIconified = false
            queryHint = getString(R.string.home_search_hint)
            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    searchKeyword = query.orEmpty().trim()
                    applyFilter()
                    updateSuggestions()
                    showSuggestions(false)
                    clearFocus()
                    return true
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    searchKeyword = newText.orEmpty().trim()
                    applyFilter()
                    updateSuggestions()
                    return true
                }
            })

            setOnQueryTextFocusChangeListener { _, hasFocus ->
                if (!hasFocus && searchKeyword.isBlank()) {
                    showSuggestions(false)
                } else if (hasFocus && searchKeyword.isNotBlank()) {
                    showSuggestions(true)
                }
            }
        }
    }

    private fun setupCategoryChips() {
        binding.chipAll.setOnClickListener {
            selectedCategory = CATEGORY_ALL
            updateChipSelection()
            applyFilter()
        }
        binding.chipSpiritual.setOnClickListener {
            selectedCategory = CATEGORY_SPIRITUAL
            updateChipSelection()
            applyFilter()
        }
        binding.chipPhoto.setOnClickListener {
            selectedCategory = CATEGORY_PHOTO
            updateChipSelection()
            applyFilter()
        }
        binding.chipNature.setOnClickListener {
            selectedCategory = CATEGORY_NATURE
            updateChipSelection()
            applyFilter()
        }
        updateChipSelection()
    }

    private fun updateChipSelection() {
        binding.chipAll.isChecked = selectedCategory == CATEGORY_ALL
        binding.chipSpiritual.isChecked = selectedCategory == CATEGORY_SPIRITUAL
        binding.chipPhoto.isChecked = selectedCategory == CATEGORY_PHOTO
        binding.chipNature.isChecked = selectedCategory == CATEGORY_NATURE
    }

    private fun loadData() {
        allLocations = LocationRepository(this).getNinhBinhLocations()
        applyFilter()
        updateSuggestions()
    }

    private fun applyFilter() {
        val filtered = allLocations.filter { location ->
            val category = detectCategory(location)
            val matchCategory = selectedCategory == CATEGORY_ALL || category == selectedCategory
            val matchKeyword = searchKeyword.isBlank() ||
                    location.name.contains(searchKeyword, ignoreCase = true) ||
                    location.description.contains(searchKeyword, ignoreCase = true)
            matchCategory && matchKeyword
        }
        adapter.submitList(filtered)
    }

    private fun updateSuggestions() {
        if (searchKeyword.isBlank()) {
            suggestionsAdapter.clearSuggestions()
            return
        }

        val suggestions = allLocations.filter { location ->
            location.name.contains(searchKeyword, ignoreCase = true) ||
                location.description.contains(searchKeyword, ignoreCase = true)
        }.take(8)

        suggestionsAdapter.updateSuggestions(suggestions)
        showSuggestions(suggestions.isNotEmpty())
    }

    private fun showSuggestions(show: Boolean) {
        binding.rvSuggestions.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun detectCategory(location: TouristLocation): String {
        val text = "${location.name} ${location.description}".lowercase()
        return when {
            text.contains("chua") || text.contains("chùa") ||
                    text.contains("den") || text.contains("đền") ||
                    text.contains("nha tho") || text.contains("nhà thờ") ||
                    text.contains("co do") || text.contains("cố đô") -> CATEGORY_SPIRITUAL

            text.contains("vuon") || text.contains("vườn") ||
                    text.contains("ho ") || text.contains("hồ") ||
                    text.contains("van long") || text.contains("vân long") ||
                    text.contains("kenh ga") || text.contains("kênh gà") -> CATEGORY_NATURE

            else -> CATEGORY_PHOTO
        }
    }

    private fun openLocationDetail(location: TouristLocation) {
        val category = detectCategory(location)
        val intent = Intent()
        intent.setClassName(this, "com.example.map.ui.map.LocationDetailActivity")
        intent.putExtra(EXTRA_LOCATION_ID, location.id)
        intent.putExtra(EXTRA_LOCATION_NAME, location.name)
        intent.putExtra(EXTRA_LOCATION_DESC, location.description)
        intent.putExtra(EXTRA_LOCATION_LAT, location.latitude)
        intent.putExtra(EXTRA_LOCATION_LNG, location.longitude)
        intent.putExtra(EXTRA_LOCATION_IMAGE_RES, location.imageRes)
        intent.putExtra(EXTRA_LOCATION_IMAGE_NAME, location.imageName)
        intent.putExtra(EXTRA_LOCATION_CATEGORY, category)
        startActivity(intent)
    }

    private fun addToItinerary(location: TouristLocation) {
        val added = ItineraryRepository.add(this, location)
        val messageRes = if (added) {
            R.string.home_added_to_itinerary
        } else {
            R.string.home_already_in_itinerary
        }
        Toast.makeText(this, getString(messageRes, location.name), Toast.LENGTH_SHORT).show()
        updateItineraryButtonCount()
    }

    companion object {
        private const val CATEGORY_ALL = "all"
        private const val CATEGORY_SPIRITUAL = "spiritual"
        private const val CATEGORY_PHOTO = "photo"
        private const val CATEGORY_NATURE = "nature"

        const val EXTRA_LOCATION_ID = "extra_location_id"
        const val EXTRA_LOCATION_NAME = "extra_location_name"
        const val EXTRA_LOCATION_DESC = "extra_location_desc"
        const val EXTRA_LOCATION_LAT = "extra_location_lat"
        const val EXTRA_LOCATION_LNG = "extra_location_lng"
        const val EXTRA_LOCATION_IMAGE_RES = "extra_location_image_res"
        const val EXTRA_LOCATION_IMAGE_NAME = "extra_location_image_name"
        const val EXTRA_LOCATION_CATEGORY = "extra_location_category"
    }
}
