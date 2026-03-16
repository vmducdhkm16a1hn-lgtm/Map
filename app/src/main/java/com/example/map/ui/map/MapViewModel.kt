package com.example.map.ui.map

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.map.BuildConfig
import com.example.map.data.model.TouristLocation
import com.example.map.data.remote.RetrofitClient
import com.example.map.data.repository.LocationRepository
import com.example.map.utils.PolylineUtils
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.launch

class MapViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = LocationRepository(application.applicationContext)

    // Danh sách địa điểm
    private val _locations = MutableLiveData<List<TouristLocation>>()
    val locations: LiveData<List<TouristLocation>> = _locations

    // Vị trí hiện tại của user
    private val _currentLocation = MutableLiveData<LatLng>()
    val currentLocation: LiveData<LatLng> = _currentLocation

    //  Điểm đến được chọn
    private val _selectedLocation = MutableLiveData<TouristLocation?>()
    val selectedLocation: LiveData<TouristLocation?> = _selectedLocation

    //  Danh sách điểm vẽ polyline
    private val _polylinePoints = MutableLiveData<List<LatLng>>()
    val polylinePoints: LiveData<List<LatLng>> = _polylinePoints

    // Thông báo lỗi
    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    // Trạng thái loading
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    init {
        loadLocations()
    }

    /** Load danh sách địa điểm Ninh Bình */
    private fun loadLocations() {
        _locations.value = repository.getNinhBinhLocations()
    }

    /** Cập nhật vị trí hiện tại */
    fun updateCurrentLocation(latLng: LatLng) {
        _currentLocation.value = latLng
    }

    /** User chọn 1 địa điểm → lưu vào selectedLocation */
    fun selectLocation(location: TouristLocation) {
        _selectedLocation.value = location
    }

    /**
     * Gọi Google Directions API để lấy đường đi
     * từ [origin] đến [destination], rồi decode polyline
     */
    fun fetchDirections(origin: LatLng, destination: LatLng) {
        _isLoading.value = true
        _polylinePoints.value = emptyList() // xoá đường cũ

        viewModelScope.launch {
            try {
                val originStr = "${origin.latitude},${origin.longitude}"
                val destinationStr = "${destination.latitude},${destination.longitude}"

                val response = RetrofitClient.directionsApi.getDirections(
                    origin = originStr,
                    destination = destinationStr,
                    apiKey = BuildConfig.MAPS_API_KEY
                )

                if (response.routes.isNotEmpty()) {
                    val encodedPolyline = response.routes[0].overviewPolyline.points
                    val points = PolylineUtils.decodePolyline(encodedPolyline)
                    _polylinePoints.value = points
                } else {
                    _errorMessage.value = "Không tìm thấy đường đi!"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Lỗi: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /** Xoá đường đi đang hiển thị */
    fun clearRoute() {
        _polylinePoints.value = emptyList()
        _selectedLocation.value = null
    }
}