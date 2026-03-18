package com.example.map.ui.map
// IMPORTS - Thư viện cần thiết cho app
// Android Core - Các class cơ bản của Android
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
// AndroidX - Thư viện hỗ trợ modern Android
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
// App modules - Các class của project
import com.example.map.R
import com.example.map.data.model.LocationClusterItem
import com.example.map.data.model.TouristLocation
import com.example.map.databinding.ActivityMapBinding
// Google Play Services - Location và Maps
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*

// Google Maps Utils - Clustering
import com.google.maps.android.clustering.ClusterManager

// Google Places API - Search địa điểm
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.net.PlacesClient

// Geocoder - Để search địa điểm (fallback cho Places API)
import android.location.Geocoder
import com.example.map.ui.adater.SuggestionsAdapter
import java.util.Locale

// MAP ACTIVITY - Activity chính hiển thị bản đồ Ninh Bình
/**
 * MapActivity - Activity chính của app
 *
 * Chức năng:
 * 1. Hiển thị bản đồ Google Maps tập trung vào Ninh Bình
 * 2. Hiển thị 12 địa điểm du lịch với marker clustering
 * 3. Lấy vị trí hiện tại của user
 * 4. Vẽ đường đi (polyline) từ vị trí hiện tại đến địa điểm
 * 5. Mở Google Maps app để navigation
 *
 * Kiến trúc: MVVM (Model-View-ViewModel)
 */
class MapActivity : AppCompatActivity(), OnMapReadyCallback {

    // VIEW BINDING - Truy cập views an toàn không cần findViewById
    private lateinit var binding: ActivityMapBinding
    // VIEWMODEL - Quản lý data và business logic (MVVM pattern)
    private val viewModel: MapViewModel by viewModels()

    // GOOGLE MAP - Instance của Google Map
    private var googleMap: GoogleMap? = null

    // CLUSTER MANAGER - Quản lý việc gộp markers thành clusters
    // Giúp optimize performance khi có nhiều markers
    private var clusterManager: ClusterManager<LocationClusterItem>? = null

    // POLYLINE - Đường vẽ trên map (route từ A đến B)
    // Lưu để có thể xóa khi vẽ đường mới
    private var currentPolyline: Polyline? = null

    // TỌA ĐỘ TRUNG TÂM NINH BÌNH
    // Latitude: 20.2506, Longitude: 105.9745
    private val ninhBinhCenter = LatLng(20.2506, 105.9745)

    // PLACES CLIENT - Để search địa điểm qua Google Places API
    private lateinit var placesClient: PlacesClient

    // GEOCODER - Fallback cho Places API (không cần billing)
    private lateinit var geocoder: Geocoder

    // SEARCH MARKER - Marker của địa điểm tìm kiếm
    private var searchMarker: Marker? = null

    // SUGGESTIONS ADAPTER - Adapter cho RecyclerView gợi ý
    private lateinit var suggestionsAdapter: SuggestionsAdapter

    // LOCATION PERMISSION LAUNCHER
    // Xử lý request quyền truy cập vị trí từ user
    // Sử dụng Activity Result API (modern way)
    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        // Kiểm tra xem có ít nhất 1 trong 2 quyền được cấp không
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
                || permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

        if (granted) {
            // Đã có quyền → Bật My Location trên map
            enableMyLocation()
        } else {
            // Bị từ chối → Thông báo user
            Toast.makeText(this, "Cần quyền vị trí để sử dụng tính năng chỉ đường!", Toast.LENGTH_SHORT).show()
        }
    }

    // LIFECYCLE METHOD - onCreate()
    // Được gọi khi Activity được tạo ra
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inflate layout sử dụng View Binding
        binding = ActivityMapBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Khởi tạo Places API
        initializePlaces()

        // Khởi tạo Suggestions RecyclerView
        setupSuggestionsRecyclerView()

        // Khởi tạo Google Map
        setupMap()

        // Setup các sự kiện click cho buttons
        setupClickListeners()

        // Setup search functionality
        setupSearch()

        // Observe (lắng nghe) các thay đổi từ ViewModel
        observeViewModel()
    }

    // SETUP MAP - Khởi tạo SupportMapFragment
    // Fragment này chứa Google Map
    private fun setupMap() {
        // Tìm MapFragment từ layout
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.mapFragment) as SupportMapFragment

        // Yêu cầu khởi tạo map bất đồng bộ
        // Khi map sẵn sàng, onMapReady() sẽ được gọi
        mapFragment.getMapAsync(this)
    }

    // INITIALIZE PLACES API - Khởi tạo Google Places API
    private fun initializePlaces() {
        try {
            // Khởi tạo Geocoder (Miễn phí, không cần billing)
            geocoder = Geocoder(this, Locale("vi", "VN"))
            Toast.makeText(this, "✓ Search đã sẵn sàng (Geocoder)", Toast.LENGTH_SHORT).show()

            // Vẫn giữ Places API nếu có billing
            try {
                val ai = packageManager.getApplicationInfo(
                    packageName,
                    PackageManager.GET_META_DATA
                )
                val apiKey = ai.metaData?.getString("com.google.android.geo.API_KEY")

                if (!apiKey.isNullOrEmpty() && !Places.isInitialized()) {
                    Places.initialize(applicationContext, apiKey)
                    placesClient = Places.createClient(this)
                }
            } catch (e: Exception) {
                // Ignore - sẽ dùng Geocoder thay thế
            }

        } catch (e: Exception) {
            Toast.makeText(this, "Lỗi khởi tạo Search: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    // ══════════════════════════════════════════════════════════════
    // SETUP SUGGESTIONS RECYCLERVIEW - Khởi tạo RecyclerView gợi ý
    // ══════════════════════════════════════════════════════════════
    private fun setupSuggestionsRecyclerView() {
        // Khởi tạo adapter với callback khi click vào suggestion
        suggestionsAdapter = SuggestionsAdapter { suggestion ->
            // Khi user click vào 1 suggestion
            binding.searchView.setQuery(suggestion, true)  // Set text và submit search
            hideSuggestions()  // Ẩn dropdown
        }

        // Setup RecyclerView
        binding.suggestionsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@MapActivity)
            adapter = suggestionsAdapter
        }
    }

    // ══════════════════════════════════════════════════════════════
    // SHOW/HIDE SUGGESTIONS - Hiển thị/ẩn dropdown gợi ý
    // ══════════════════════════════════════════════════════════════
    private fun showSuggestions() {
        binding.suggestionsCard.visibility = View.VISIBLE
    }

    private fun hideSuggestions() {
        binding.suggestionsCard.visibility = View.GONE
    }

    // SETUP MAP - Khởi tạo SupportMapFragment
    private fun setupSearch() {
        binding.searchView.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let {
                    if (it.isNotEmpty()) {
                        searchPlaces(it)
                        hideSuggestions()  // Ẩn dropdown khi search
                    } else {
                        Toast.makeText(this@MapActivity, "Vui lòng nhập từ khóa tìm kiếm", Toast.LENGTH_SHORT).show()
                    }
                }
                // Ẩn keyboard
                binding.searchView.clearFocus()
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                // Autocomplete suggestions khi user gõ
                newText?.let {
                    if (it.length >= 2) {
                        // Chỉ search khi nhập ít nhất 2 ký tự
                        showAutocompleteSuggestions(it)
                    } else {
                        // Ít hơn 2 ký tự → Ẩn dropdown
                        hideSuggestions()
                    }
                } ?: run {
                    // Text rỗng → Ẩn dropdown
                    hideSuggestions()
                }
                return true
            }
        })
    }

    // ══════════════════════════════════════════════════════════════
    // SHOW AUTOCOMPLETE SUGGESTIONS - Hiển thị gợi ý tự động
    // Hiển thị trong RecyclerView dropdown dưới SearchView
    // ══════════════════════════════════════════════════════════════
    private fun showAutocompleteSuggestions(query: String) {
        try {
            // Sử dụng Geocoder để tìm gợi ý (miễn phí)
            Thread {
                try {
                    val addresses = geocoder.getFromLocationName(
                        "$query, Ninh Bình, Việt Nam",
                        5  // Lấy tối đa 5 kết quả
                    )

                    runOnUiThread {
                        if (!addresses.isNullOrEmpty()) {
                            // Lấy tên địa điểm từ kết quả
                            val suggestions = addresses
                                .mapNotNull { it.featureName ?: it.locality ?: it.getAddressLine(0) }
                                .distinct()  // Loại bỏ duplicate
                                .take(5)     // Lấy tối đa 5 gợi ý

                            if (suggestions.isNotEmpty()) {
                                // Cập nhật adapter và hiển thị dropdown
                                suggestionsAdapter.updateSuggestions(suggestions)
                                showSuggestions()
                            } else {
                                // Không có gợi ý → Ẩn dropdown
                                hideSuggestions()
                            }
                        } else {
                            // Không có kết quả → Ẩn dropdown
                            hideSuggestions()
                        }
                    }
                } catch (e: Exception) {
                    runOnUiThread {
                        hideSuggestions()
                    }
                }
            }.start()

        } catch (e: Exception) {
            hideSuggestions()
        }
    }

    // SEARCH PLACES - Tìm kiếm địa điểm qua Geocoder API
    // Sử dụng Geocoder thay vì Places API (miễn phí, không cần billing)
    private fun searchPlaces(query: String) {
        try {
            // Hiển thị loading
            binding.progressBar.visibility = View.VISIBLE

            Toast.makeText(this, "Đang tìm: $query", Toast.LENGTH_SHORT).show()

            // Tìm kiếm bất đồng bộ trên thread khác
            Thread {
                try {
                    // Sử dụng Geocoder để tìm địa điểm
                    val addresses = geocoder.getFromLocationName(
                        "$query, Ninh Bình, Việt Nam",
                        5  // Lấy tối đa 5 kết quả
                    )

                    runOnUiThread {
                        binding.progressBar.visibility = View.GONE

                        if (!addresses.isNullOrEmpty()) {
                            val firstResult = addresses[0]
                            val locationName = firstResult.featureName
                                ?: firstResult.locality
                                ?: query

                            Toast.makeText(
                                this,
                                "✓ Tìm thấy: $locationName",
                                Toast.LENGTH_SHORT
                            ).show()

                            // Lấy tọa độ
                            val latLng = LatLng(firstResult.latitude, firstResult.longitude)

                            // Xóa marker search cũ
                            searchMarker?.remove()

                            // Thêm marker mới
                            searchMarker = googleMap?.addMarker(
                                MarkerOptions()
                                    .position(latLng)
                                    .title(locationName)
                                    .snippet(firstResult.getAddressLine(0) ?: "")
                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                            )

                            // Zoom đến địa điểm
                            googleMap?.animateCamera(
                                CameraUpdateFactory.newLatLngZoom(latLng, 15f)
                            )

                            // Hiển thị info window
                            searchMarker?.showInfoWindow()

                        } else {
                            Toast.makeText(
                                this,
                                "Không tìm thấy '$query' ở Ninh Bình",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                } catch (e: Exception) {
                    runOnUiThread {
                        binding.progressBar.visibility = View.GONE
                        Toast.makeText(
                            this,
                            "Lỗi tìm kiếm: ${e.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }.start()

        } catch (e: Exception) {
            binding.progressBar.visibility = View.GONE
            Toast.makeText(this, "Lỗi search: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    // SETUP CLICK LISTENERS - Xử lý các sự kiện click
    private fun setupClickListeners() {

        // FAB BUTTON - Lấy vị trí hiện tại
        // Floating Action Button ở góc dưới bên trái
        binding.fabMyLocation.setOnClickListener {
            // Kiểm tra quyền và lấy vị trí GPS
            checkLocationPermissionAndGetLocation()
        }

        // NÚT ĐƯA VỀ KHU VỰC GỢI Ý BAN ĐẦU (giống hành vi "Explore this area" của Google Maps)
        binding.fabResetMap.setOnClickListener {
            returnToSuggestedArea()
        }

        // NÚT CHỈ ĐƯỜNG - Vẽ route từ vị trí hiện tại đến địa điểm
        binding.btnGetDirections.setOnClickListener {
            // Lấy vị trí hiện tại và địa điểm được chọn từ ViewModel
            val currentLoc = viewModel.currentLocation.value
            val selectedLoc = viewModel.selectedLocation.value

            when {
                // Case 1: Chưa có vị trí hiện tại
                currentLoc == null -> {
                    Toast.makeText(this, "Chưa lấy được vị trí của bạn!", Toast.LENGTH_SHORT).show()
                    // Tự động yêu cầu lấy vị trí
                    checkLocationPermissionAndGetLocation()
                }

                // Case 2: Chưa chọn địa điểm
                selectedLoc == null -> {
                    Toast.makeText(this, "Chưa chọn địa điểm!", Toast.LENGTH_SHORT).show()
                }

                // Case 3: Đã có đủ thông tin → Gọi Directions API
                else -> {
                    val destination = LatLng(selectedLoc.latitude, selectedLoc.longitude)
                    viewModel.fetchDirections(currentLoc, destination)
                }
            }
        }

        // NÚT MỞ GOOGLE MAPS - Chuyển sang Google Maps app
        // Sử dụng deep link để mở navigation
        binding.btnOpenGoogleMaps.setOnClickListener {
            viewModel.selectedLocation.value?.let { location ->
                openGoogleMapsNavigation(location)
            }
        }
    }
    // OBSERVE VIEWMODEL - Lắng nghe thay đổi từ ViewModel
    // Sử dụng LiveData để UI tự động update khi data thay đổi
    private fun observeViewModel() {

        // OBSERVE LOCATIONS - Danh sách địa điểm
        // Khi danh sách địa điểm được load, thêm markers lên map
        viewModel.locations.observe(this) { locations ->
            googleMap?.let { map ->
                addMarkersToMap(map, locations)
            }
        }

        // OBSERVE CURRENT LOCATION - Vị trí hiện tại
        // Khi vị trí được update, zoom map đến vị trí đó
        viewModel.currentLocation.observe(this) { latLng ->
            // Zoom camera đến vị trí hiện tại (zoom level 14)
            googleMap?.animateCamera(
                CameraUpdateFactory.newLatLngZoom(latLng, 14f)
            )
        }

        // OBSERVE POLYLINE POINTS - Điểm vẽ đường
        // Khi có dữ liệu route từ Directions API, vẽ polyline
        viewModel.polylinePoints.observe(this) { points ->
            // Xóa đường cũ (nếu có)
            currentPolyline?.remove()

            if (points.isNotEmpty()) {
                // Vẽ polyline mới
                currentPolyline = googleMap?.addPolyline(
                    PolylineOptions()
                        .addAll(points)                    // Thêm tất cả điểm
                        .color(Color.parseColor("#2196F3")) // Màu xanh Material Blue
                        .width(12f)                         // Độ rộng 12px
                        .geodesic(true)                     // Đường cong theo bề mặt trái đất
                )

                // Zoom camera để thấy toàn bộ route
                zoomToFitRoute(points)

                // Thông báo thành công
                Toast.makeText(this, "Đã vẽ đường đi!", Toast.LENGTH_SHORT).show()
            }
        }

        // OBSERVE LOADING STATE - Trạng thái loading
        // Hiển thị/ẩn progress bar khi đang fetch data
        viewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        // OBSERVE ERROR MESSAGE - Thông báo lỗi
        // Hiển thị toast khi có lỗi xảy ra
        viewModel.errorMessage.observe(this) { error ->
            if (error.isNotEmpty()) {
                Toast.makeText(this, error, Toast.LENGTH_LONG).show()
            }
        }

        // OBSERVE SELECTED LOCATION - Địa điểm được chọn
        // Hiển thị bottom card với thông tin địa điểm
        viewModel.selectedLocation.observe(this) { location ->
            if (location != null) {
                // Có địa điểm được chọn → Hiện bottom card
                showLocationInfoCard(location)
            } else {
                // Không có địa điểm nào → Ẩn bottom card
                binding.cardLocationInfo.visibility = View.GONE
            }
        }
    }

    // ON MAP READY CALLBACK - Được gọi khi Google Map sẵn sàng
    override fun onMapReady(map: GoogleMap) {
        googleMap = map

        // CAMERA MOVEMENT - Zoom về Ninh Bình khi mở app
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(ninhBinhCenter, 10f))

        // MAP UI SETTINGS - Thiết lập giao diện map
        map.uiSettings.apply {
            isZoomControlsEnabled = true      // Hiện nút zoom (+/-)
            isCompassEnabled = true           // Hiện la bàn
            isMapToolbarEnabled = false       // Ẩn toolbar mặc định
        }

        // SETUP CLUSTERING - Khởi tạo Cluster Manager
        setupClusterManager(map)
        // ADD MARKERS - Thêm markers nếu data đã sẵn sàng
        viewModel.locations.value?.let { locations ->
            addMarkersToMap(map, locations)
        }

        // LOCATION PERMISSION - Kiểm tra và bật My Location
        if (hasLocationPermission()) {
            enableMyLocation()
        }
    }

    // SETUP CLUSTER MANAGER - Cấu hình marker clustering
    private fun setupClusterManager(map: GoogleMap) {
        // Khởi tạo ClusterManager
        clusterManager = ClusterManager<LocationClusterItem>(this, map)

        // CLUSTER ITEM CLICK - Xử lý khi click vào marker
        clusterManager?.setOnClusterItemClickListener { clusterItem ->
            val location = clusterItem.location

            // Chọn địa điểm này
            viewModel.selectLocation(location)

            // Xóa đường đi cũ (nếu có)
            currentPolyline?.remove()
            viewModel.clearRoute()

            // Cập nhật lại selection
            viewModel.selectLocation(location)

            // Return true để không hiện info window mặc định
            true
        }

        // MAP EVENT LISTENERS - Gắn ClusterManager vào Map
        map.setOnCameraIdleListener(clusterManager)  // Update clusters khi map dừng di chuyển
        map.setOnMarkerClickListener(clusterManager)  // Xử lý click marker

        // MAP CLICK - Xử lý khi click vào vùng trống trên map
        map.setOnMapClickListener {
            // Xóa route
            viewModel.clearRoute()

            // Ẩn bottom card
            binding.cardLocationInfo.visibility = View.GONE

            // Xóa polyline
            currentPolyline?.remove()
        }
    }

    // ADD MARKERS TO MAP - Thêm markers lên map với clustering
    private fun addMarkersToMap(map: GoogleMap, locations: List<TouristLocation>) {
        // Xóa tất cả markers cũ
        clusterManager?.clearItems()

        // Thêm từng địa điểm vào ClusterManager
        locations.forEach { location ->
            val clusterItem = LocationClusterItem(location)
            clusterManager?.addItem(clusterItem)
        }

        // Trigger clustering algorithm
        clusterManager?.cluster()
    }

    // SHOW LOCATION INFO CARD - Hiển thị bottom card với thông tin
    private fun showLocationInfoCard(location: TouristLocation) {
        binding.apply {
            tvLocationName.text = location.name           // Tên địa điểm
            tvLocationDesc.text = location.description    // Mô tả
            cardLocationInfo.visibility = View.VISIBLE    // Hiện card
        }
    }
    // ZOOM TO FIT ROUTE - Zoom camera để thấy toàn bộ đường đi
    private fun zoomToFitRoute(points: List<LatLng>) {
        if (points.isEmpty()) return

        // Tạo LatLngBounds builder
        val builder = LatLngBounds.Builder()

        // Thêm tất cả điểm trên route
        points.forEach { builder.include(it) }

        // Thêm cả vị trí hiện tại
        viewModel.currentLocation.value?.let { builder.include(it) }

        // Build bounds
        val bounds = builder.build()

        // Animate camera với padding 150px
        googleMap?.animateCamera(
            CameraUpdateFactory.newLatLngBounds(bounds, 150)
        )
    }

    // OPEN GOOGLE MAPS NAVIGATION - Mở Google Maps app
    // Sử dụng Intent với deep link để mở navigation mode
    private fun openGoogleMapsNavigation(location: TouristLocation) {
        // Tạo URI cho Google Maps navigation
        val uri = Uri.parse(
            "google.navigation:q=${location.latitude},${location.longitude}&mode=d"
        )

        // Tạo Intent với package Google Maps
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            setPackage("com.google.android.apps.maps")
        }

        // Kiểm tra xem có Google Maps app không
        if (intent.resolveActivity(packageManager) != null) {
            // Có app → Mở Google Maps
            startActivity(intent)
        } else {
            // Không có app → Fallback sang browser
            val webUri = Uri.parse(
                "https://www.google.com/maps/dir/?api=1&destination=${location.latitude},${location.longitude}"
            )
            startActivity(Intent(Intent.ACTION_VIEW, webUri))
        }
    }

    // LOCATION PERMISSION METHODS - Xử lý quyền vị trí

    /**
     * Kiểm tra xem đã có quyền vị trí chưa
     */
    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Kiểm tra quyền và lấy vị trí hiện tại
     */
    private fun checkLocationPermissionAndGetLocation() {
        if (hasLocationPermission()) {
            // Đã có quyền → Lấy vị trí
            enableMyLocation()
            getCurrentLocation()
        } else {
            // Chưa có quyền → Request
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    /**
     * Bật My Location dot (chấm xanh) trên map
     */
    private fun enableMyLocation() {
        if (hasLocationPermission()) {
            try {
                googleMap?.isMyLocationEnabled = true           // Bật My Location
                googleMap?.uiSettings?.isMyLocationButtonEnabled = false  // Ẩn nút mặc định (dùng FAB)
            } catch (e: SecurityException) {
                Toast.makeText(this, "Lỗi quyền vị trí!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // GET CURRENT LOCATION - Lấy vị trí GPS hiện tại
    // Sử dụng FusedLocationProviderClient (recommended way)
    private fun getCurrentLocation() {
        if (!hasLocationPermission()) return

        try {
            // Khởi tạo FusedLocationProviderClient
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

            // Lấy vị trí cuối cùng đã biết (nhanh nhất)
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    // Có vị trí → Convert sang LatLng
                    val latLng = LatLng(it.latitude, it.longitude)

                    // Cập nhật vào ViewModel
                    viewModel.updateCurrentLocation(latLng)

                    // Zoom đến vị trí hiện tại
                    googleMap?.animateCamera(
                        CameraUpdateFactory.newLatLngZoom(latLng, 14f)
                    )

                    // Thông báo thành công
                    Toast.makeText(
                        this,
                        "Đã lấy vị trí: ${it.latitude}, ${it.longitude}",
                        Toast.LENGTH_SHORT
                    ).show()
                } ?: run {
                    // Không có vị trí → Yêu cầu thử lại
                    Toast.makeText(this, "Chưa lấy được vị trí, thử lại!", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: SecurityException) {
            Toast.makeText(this, "Lỗi quyền vị trí!", Toast.LENGTH_SHORT).show()
        }
    }

    // Đưa bản đồ về vùng địa điểm gợi ý ban đầu của Ninh Bình
    private fun returnToSuggestedArea() {
        val map = googleMap ?: return

        // Xóa marker tìm kiếm tạm thời nếu có
        searchMarker?.remove()
        searchMarker = null

        // Xóa route hiện tại
        currentPolyline?.remove()
        currentPolyline = null
        viewModel.clearRoute()

        // Bỏ chọn địa điểm để ẩn info card
        binding.cardLocationInfo.visibility = View.GONE

        // Nạp lại marker gợi ý ban đầu
        viewModel.locations.value?.let { locations ->
            addMarkersToMap(map, locations)
        }

        // Đưa camera về khu vực Ninh Bình như lúc mở app
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(ninhBinhCenter, 10f))
    }
}