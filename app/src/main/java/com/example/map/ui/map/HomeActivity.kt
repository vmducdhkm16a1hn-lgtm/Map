package com.example.map.ui.map

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.map.data.model.TouristLocation
import com.example.map.data.repository.LocationRepository
import com.example.map.databinding.ActivityHomeBinding
import com.example.map.ui.adapter.HomeLocationAdapter

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var adapter: HomeLocationAdapter

    private var allLocations: List<TouristLocation> = emptyList()
    private var selectedCategory: String = CATEGORY_ALL
    private var searchKeyword: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupSearch()
        setupCategoryChips()
        loadData()
    }

    private fun setupRecyclerView() {
        adapter = HomeLocationAdapter(
            onCardClick = { showLocationDetail(it) },
            onMapClick = { openMapWithLocation(it) }
        )
        binding.rvLocations.layoutManager = LinearLayoutManager(this)
        binding.rvLocations.adapter = adapter
    }

    private fun setupSearch() {
        binding.searchView.apply {
            isIconified = false
            queryHint = "Tìm kiếm địa điểm..."
            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    searchKeyword = query.orEmpty().trim()
                    applyFilter()
                    clearFocus()
                    return true
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    searchKeyword = newText.orEmpty().trim()
                    applyFilter()
                    return true
                }
            })
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

    private fun showLocationDetail(location: TouristLocation) {
        LocationDetailBottomSheet.newInstance(location)
            .show(supportFragmentManager, LocationDetailBottomSheet.TAG)
    }

    private fun openMapWithLocation(location: TouristLocation) {
        val intent = Intent(this, MapActivity::class.java).apply {
            putExtra(EXTRA_LOCATION_NAME, location.name)
            putExtra(EXTRA_LOCATION_DESC, location.description)
            putExtra(EXTRA_LOCATION_LAT, location.latitude)
            putExtra(EXTRA_LOCATION_LNG, location.longitude)
        }
        startActivity(intent)
    }

    companion object {
        private const val CATEGORY_ALL = "all"
        private const val CATEGORY_SPIRITUAL = "spiritual"
        private const val CATEGORY_PHOTO = "photo"
        private const val CATEGORY_NATURE = "nature"

        const val EXTRA_LOCATION_NAME = "extra_location_name"
        const val EXTRA_LOCATION_DESC = "extra_location_desc"
        const val EXTRA_LOCATION_LAT = "extra_location_lat"
        const val EXTRA_LOCATION_LNG = "extra_location_lng"
    }
}
