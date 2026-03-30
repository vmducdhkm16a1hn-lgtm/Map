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

// Màn hình chính (Home) của ứng dụng, hiển thị danh sách các địa điểm và xử lý các thao tác tìm kiếm, lọc danh mục
class HomeActivity : AppCompatActivity() {

    // Sử dụng ViewBinding để tương tác trực tiếp với các view trong file XML (activity_home.xml) một cách an toàn, không cần dùng findViewById
    private lateinit var binding: ActivityHomeBinding
    private lateinit var adapter: HomeLocationAdapter

    // Các biến lưu trữ trạng thái (State) hiện tại của màn hình
    private var allLocations: List<TouristLocation> = emptyList() // Lưu toàn bộ dữ liệu gốc
    private var selectedCategory: String = CATEGORY_ALL           // Lưu trạng thái danh mục đang được chọn
    private var searchKeyword: String = ""                        // Lưu từ khóa người dùng đang nhập vào ô tìm kiếm

    // Hàm vòng đời đầu tiên được gọi khi Activity được tạo
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Khởi tạo ViewBinding và thiết lập giao diện
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Gọi các hàm khởi tạo giao diện và nạp dữ liệu tuần tự
        setupRecyclerView()
        setupSearch()
        setupCategoryChips()
        loadData()
    }

    // Cấu hình danh sách RecyclerView
    private fun setupRecyclerView() {
        adapter = HomeLocationAdapter(
            // Truyền hàm callback xử lý sự kiện click vào từng item: mở màn hình chi tiết
            onCardClick = { openLocationDetail(it) }
        )
        // Hiển thị danh sách theo dạng cuộn dọc (Vertical)
        binding.rvLocations.layoutManager = LinearLayoutManager(this)
        binding.rvLocations.adapter = adapter
    }

    // Cấu hình thanh tìm kiếm (SearchView)
    private fun setupSearch() {
        binding.searchView.apply {
            isIconified = false // Hiển thị sẵn ô nhập text thay vì chỉ hiện icon kính lúp
            queryHint = "Tìm kiếm địa điểm..."

            // Lắng nghe sự kiện người dùng gõ phím hoặc bấm nút tìm kiếm
            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                // Khi người dùng bấm nút Submit (Enter/Tìm kiếm trên bàn phím)
                override fun onQueryTextSubmit(query: String?): Boolean {
                    searchKeyword = query.orEmpty().trim()
                    applyFilter() // Gọi hàm lọc lại danh sách
                    clearFocus()  // Ẩn bàn phím đi
                    return true
                }

                // Lọc realtime (thời gian thực): Khi người dùng đang gõ từng chữ
                override fun onQueryTextChange(newText: String?): Boolean {
                    searchKeyword = newText.orEmpty().trim()
                    applyFilter() // Gọi hàm lọc lại danh sách tương ứng với từ khóa mới
                    return true
                }
            })
        }
    }

    // Cấu hình các nút lọc danh mục (Chips)
    private fun setupCategoryChips() {
        // Mỗi khi click vào một Chip, cập nhật lại biến selectedCategory, đổi màu Chip và gọi hàm lọc danh sách
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
        updateChipSelection() // Set trạng thái UI ban đầu cho các Chips
    }

    // Cập nhật giao diện (màu sắc, trạng thái checked) của các Chips dựa trên biến selectedCategory hiện tại
    private fun updateChipSelection() {
        binding.chipAll.isChecked = selectedCategory == CATEGORY_ALL
        binding.chipSpiritual.isChecked = selectedCategory == CATEGORY_SPIRITUAL
        binding.chipPhoto.isChecked = selectedCategory == CATEGORY_PHOTO
        binding.chipNature.isChecked = selectedCategory == CATEGORY_NATURE
    }

    // Lấy dữ liệu từ Repository (đọc từ file JSON)
    private fun loadData() {
        allLocations = LocationRepository(this).getNinhBinhLocations()
        applyFilter() // Áp dụng bộ lọc ban đầu để hiển thị toàn bộ danh sách lên Adapter
    }

    // Hàm cực kỳ quan trọng: Xử lý logic lọc danh sách kết hợp cả 2 điều kiện (Danh mục VÀ Từ khóa tìm kiếm)
    private fun applyFilter() {
        val filtered = allLocations.filter { location ->
            val category = detectCategory(location)

            // Điều kiện 1: Khớp danh mục (Nếu đang chọn "Tất cả" thì luôn đúng, nếu không thì phải so sánh xem có trùng thể loại không)
            val matchCategory = selectedCategory == CATEGORY_ALL || category == selectedCategory

            // Điều kiện 2: Khớp từ khóa (Nếu ô tìm kiếm trống thì luôn đúng, nếu có chữ thì kiểm tra xem Tên hoặc Mô tả có chứa từ khóa đó không, bỏ qua viết hoa/thường)
            val matchKeyword = searchKeyword.isBlank() ||
                    location.name.contains(searchKeyword, ignoreCase = true) ||
                    location.description.contains(searchKeyword, ignoreCase = true)

            // Chỉ giữ lại những địa điểm thỏa mãn CẢ HAI điều kiện trên
            matchCategory && matchKeyword
        }
        // Gửi danh sách đã lọc (hoặc danh sách rỗng nếu không có kết quả) vào Adapter để cập nhật UI mượt mà qua DiffUtil
        adapter.submitList(filtered)
    }

    // Hàm tự động phân loại địa điểm dựa vào từ khóa trong tên và mô tả (tương tự như trong Adapter)
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

    // Chuyển sang màn hình Bản đồ (MapActivity) và truyền theo tọa độ để focus (zoom) thẳng vào địa điểm đó
    private fun openMapWithLocation(location: TouristLocation) {
        val intent = Intent(this, MapActivity::class.java).apply {
            putExtra(EXTRA_LOCATION_NAME, location.name)
            putExtra(EXTRA_LOCATION_DESC, location.description)
            putExtra(EXTRA_LOCATION_LAT, location.latitude)
            putExtra(EXTRA_LOCATION_LNG, location.longitude)
            putExtra(EXTRA_FOCUS_DESTINATION, true) // Cờ báo hiệu cho MapActivity biết cần tự động di chuyển camera đến đây
        }
        startActivity(intent)
    }

    // Chuyển sang màn hình Chi tiết (LocationDetailActivity) và truyền toàn bộ thông tin của địa điểm qua Intent
    private fun openLocationDetail(location: TouristLocation) {
        val intent = Intent(this, LocationDetailActivity::class.java).apply {
            putExtra(LocationDetailActivity.EXTRA_LOCATION_ID, location.id)
            putExtra(LocationDetailActivity.EXTRA_LOCATION_NAME, location.name)
            putExtra(LocationDetailActivity.EXTRA_LOCATION_DESC, location.description)
            putExtra(LocationDetailActivity.EXTRA_LOCATION_LAT, location.latitude)
            putExtra(LocationDetailActivity.EXTRA_LOCATION_LNG, location.longitude)
            putExtra(LocationDetailActivity.EXTRA_LOCATION_IMAGE_RES, location.imageRes)
        }
        startActivity(intent)
    }

    // Khối companion object lưu trữ các hằng số (Constants) dùng chung, tương đương với static final trong Java
    companion object {
        // Các key định nghĩa cho bộ lọc Category
        private const val CATEGORY_ALL = "all"
        private const val CATEGORY_SPIRITUAL = "spiritual"
        private const val CATEGORY_PHOTO = "photo"
        private const val CATEGORY_NATURE = "nature"

        // Các key an toàn dùng để đóng gói dữ liệu truyền qua Intent, giúp tránh gõ sai chính tả khi nhận dữ liệu ở Activity khác
        const val EXTRA_LOCATION_NAME = "extra_location_name"
        const val EXTRA_LOCATION_DESC = "extra_location_desc"
        const val EXTRA_LOCATION_LAT = "extra_location_lat"
        const val EXTRA_LOCATION_LNG = "extra_location_lng"
        const val EXTRA_FOCUS_DESTINATION = "extra_focus_destination"
    }
}