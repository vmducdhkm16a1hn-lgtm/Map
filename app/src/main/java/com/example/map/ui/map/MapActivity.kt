package com.example.map.ui.map
// IMPORTS - Thư viện cần thiết cho app
// Android Core - Các class cơ bản của Android
import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Toast
// AndroidX - Thư viện hỗ trợ modern Android
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.toColorInt
// App modules - Các class của project
import com.example.map.R
import com.example.map.data.model.LocationClusterItem
import com.example.map.data.model.TouristLocation
import com.example.map.databinding.ActivityMapBinding
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.google.maps.android.clustering.ClusterManager

// MAP ACTIVITY - Activity chính hiển thị bản đồ Ninh Bình
/**
 * MapActivity - Activity chính của app
 *
 * Chức năng:
 * 1. Hiển thị bản đồ Google Maps tập trung vào Ninh Bình
 * 2. Hiển thị 12 địa điểm du lịch với marker clustering
 * 3. Lấy vị trí hiện tại của user
 * 4. Vẽ đường đi (polyline) từ vị trí hiện tại đến địa điểm
 * 5. Vẽ route trong app khi user bấm nút mở bản đồ
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

    // Lưu tạm địa điểm cần vẽ route khi đang chờ cấp quyền vị trí
    private var pendingRouteDestination: TouristLocation? = null

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

            // Nếu trước đó user bấm "Mở bản đồ" nhưng chưa có quyền, tiếp tục vẽ route ngay
            pendingRouteDestination?.let { destination ->
                getCurrentLocationAndDrawRoute(destination)
                pendingRouteDestination = null
            }
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

        // Khởi tạo Google Map
        setupMap()

        // Setup các sự kiện click cho buttons
        setupClickListeners()

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

        // NÚT VẼ TUYẾN - Nút duy nhất để vẽ line từ vị trí hiện tại đến địa điểm
        binding.btnOpenGoogleMaps.setOnClickListener {
            val destination = viewModel.selectedLocation.value
            if (destination == null) {
                Toast.makeText(this, "Chưa chọn địa điểm!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val destinationLatLng = LatLng(destination.latitude, destination.longitude)
            val currentLoc = viewModel.currentLocation.value
            if (currentLoc != null) {
                viewModel.fetchDirections(currentLoc, destinationLatLng)
            } else {
                // Chưa có current location trong ViewModel -> lấy nhanh từ FusedLocation rồi vẽ route
                getCurrentLocationAndDrawRoute(destination)
            }
        }
    }
    // OBSERVE VIEWMODEL - Lắng nghe thay đổi từ ViewModel
    // Sử dụng LiveData để UI tự động update khi data thay đổi
    private fun observeViewModel() {

        // OBSERVE LOCATIONS - Danh sách địa điểm
        // Khi danh sách địa điểm được load, thêm markers lên map
        viewModel.locations.observe(this) { locations ->
            if (googleMap != null) {
                addMarkersToMap(locations)
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
                        .addAll(points)
                        .color("#2196F3".toColorInt())
                        .width(12f)
                        .geodesic(true)
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
            addMarkersToMap(locations)
        }

        // LOCATION PERMISSION - Kiểm tra và bật My Location
        if (hasLocationPermission()) {
            enableMyLocation()
        }
    }

    // SETUP CLUSTER MANAGER - Cấu hình marker clustering
    @Suppress("PotentialBehaviorOverride")
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
    private fun addMarkersToMap(locations: List<TouristLocation>) {
        clusterManager?.clearItems()

        locations.forEach { location ->
            val clusterItem = LocationClusterItem(location)
            clusterManager?.addItem(clusterItem)
        }

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

    // Lấy vị trí hiện tại rồi vẽ route trong app; không tự mở Google Maps
    private fun getCurrentLocationAndDrawRoute(destination: TouristLocation) {
        if (!hasLocationPermission()) {
            pendingRouteDestination = destination
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
            return
        }

        try {
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    val current = LatLng(location.latitude, location.longitude)
                    viewModel.updateCurrentLocation(current)

                    // Vẽ route line trong app
                    viewModel.fetchDirections(current, LatLng(destination.latitude, destination.longitude))
                } else {
                    Toast.makeText(this, "Chưa lấy được vị trí chính xác, vui lòng thử lại", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener {
                Toast.makeText(this, "Không lấy được vị trí hiện tại, vui lòng thử lại", Toast.LENGTH_SHORT).show()
            }
        } catch (_: SecurityException) {
            Toast.makeText(this, "Lỗi quyền vị trí, vui lòng thử lại", Toast.LENGTH_SHORT).show()
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
        ) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
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
                googleMap?.isMyLocationEnabled = true
                googleMap?.uiSettings?.isMyLocationButtonEnabled = false
            } catch (_: SecurityException) {
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
        } catch (_: SecurityException) {
            Toast.makeText(this, "Lỗi quyền vị trí!", Toast.LENGTH_SHORT).show()
        }
    }

    // Đưa bản đồ về vùng địa điểm gợi ý ban đầu của Ninh Bình
    private fun returnToSuggestedArea() {
        val map = googleMap ?: return


        // Xóa route hiện tại
        currentPolyline?.remove()
        currentPolyline = null
        viewModel.clearRoute()

        // Bỏ chọn địa điểm để ẩn info card
        binding.cardLocationInfo.visibility = View.GONE

        // Nạp lại marker gợi ý ban đầu
        viewModel.locations.value?.let { locations ->
            addMarkersToMap(locations)
        }

        // Đưa camera về khu vực Ninh Bình như lúc mở app
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(ninhBinhCenter, 10f))
    }
}
