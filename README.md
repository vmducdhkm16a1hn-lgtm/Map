# Ninh Bình Map App

Ứng dụng Android hiển thị các địa điểm du lịch tại Ninh Bình với Google Maps.

## Tính năng đã triển khai

### ✅ 1. Hiển thị Google Map
- Tích hợp Google Maps SDK
- Zoom vào khu vực Ninh Bình khi khởi động
- UI controls: zoom, compass

### ✅ 2. Hiển thị nhiều địa điểm (Markers)
- **12 địa điểm du lịch** nổi tiếng tại Ninh Bình:
  - Tràng An (UNESCO)
  - Tam Cốc - Bích Động
  - Cố đô Hoa Lư
  - Chùa Bái Đính
  - Vườn Quốc gia Cúc Phương
  - Kênh Gà - Vân Trình
  - Nhà thờ đá Phát Diệm
  - Hồ Đồng Chương
  - Động Thiên Hà
  - Vân Long
  - Chùa Bảo Tháp
  - Đền Trần Thương

### ✅ 3. Lấy vị trí hiện tại
- Request runtime permissions cho location
- FusedLocationProviderClient để lấy vị trí
- FAB button để lấy vị trí hiện tại
- Hiển thị "My Location" dot trên map

### ✅ 4. Vẽ đường đi (Polyline)
- Tích hợp Google Directions API
- Vẽ polyline từ vị trí hiện tại đến địa điểm
- Auto zoom để hiển thị toàn bộ route
- Xóa đường đi cũ khi chọn địa điểm mới

### ✅ 5. Đọc dữ liệu từ JSON
- File `locations.json` trong folder `assets`
- Parse JSON bằng Gson
- Fallback data nếu JSON bị lỗi

### ✅ 6. Google Maps Clustering
- Sử dụng `android-maps-utils` library
- Tự động group markers khi zoom out
- Mở rộng clusters khi zoom in
- Click vào marker hiển thị thông tin

### ✅ 7. Chuyển sang Google Maps App
- Nút "Mở Google Maps" để chỉ đường
- Deep link vào Google Maps Navigation
- Fallback sang browser nếu không có app

## Cấu trúc dự án

```
app/
├── src/main/
│   ├── assets/
│   │   └── locations.json          # Dữ liệu địa điểm
│   ├── java/com/example/map/
│   │   ├── data/
│   │   │   ├── model/
│   │   │   │   ├── Location.kt              # Model TouristLocation
│   │   │   │   ├── LocationClusterItem.kt   # ClusterItem wrapper
│   │   │   │   └── DirectionsResponse.kt    # API response model
│   │   │   ├── remote/
│   │   │   │   ├── DirectionsApiService.kt
│   │   │   │   └── RetrofitClient.kt
│   │   │   └── repository/
│   │   │       └── LocationRepository.kt    # Đọc JSON từ assets
│   │   ├── ui/
│   │   │   └── map/
│   │   │       ├── MapActivity.kt           # Main activity
│   │   │       └── MapViewModel.kt          # MVVM ViewModel
│   │   └── utils/
│   │       └── PolylineUtils.kt             # Decode polyline
│   └── res/
│       ├── layout/
│       │   └── activity_map.xml
│       └── values/
│           └── themes.xml
```

## Cài đặt

### 1. Thêm Google Maps API Key

Tạo file `local.properties` trong thư mục root:

```properties
MAPS_API_KEY=YOUR_GOOGLE_MAPS_API_KEY_HERE
```

### 2. Enable APIs trên Google Cloud Console

- Maps SDK for Android
- Directions API
- Geolocation API

### 3. Build & Run

```bash
./gradlew assembleDebug
```

## Dependencies

- **Google Maps**: `com.google.android.gms:play-services-maps:18.2.0`
- **Location Services**: `com.google.android.gms:play-services-location:21.3.0`
- **Maps Clustering**: `com.google.maps.android:android-maps-utils:3.8.2`
- **Retrofit**: `com.squareup.retrofit2:retrofit:2.11.0`
- **Gson**: `com.google.code.gson:gson:2.10.1`
- **Coroutines**: `org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1`
- **ViewModel & LiveData**: `androidx.lifecycle:lifecycle-*`

## Cách sử dụng

1. **Khởi động app**: Map sẽ zoom vào khu vực Ninh Bình với tất cả các markers
2. **Xem các địa điểm**: Zoom in/out để xem markers (tự động clustering)
3. **Chọn địa điểm**: Click vào marker để xem thông tin chi tiết
4. **Lấy vị trí**: Nhấn FAB button (biểu tượng vị trí) ở góc phải dưới
5. **Chỉ đường**: 
   - Nhấn "🗺️ Chỉ đường" để vẽ route trên map
   - Nhấn "📍 Mở Google Maps" để chuyển sang Google Maps app

## Marker Clustering

App sử dụng **Google Maps Android Marker Clustering Utility** để:
- Tự động nhóm markers gần nhau thành clusters
- Hiển thị số lượng markers trong mỗi cluster
- Mở rộng cluster khi zoom in hoặc click vào
- Tối ưu hiệu năng khi có nhiều markers

## Kiến trúc

- **MVVM (Model-View-ViewModel)**: Tách biệt logic và UI
- **Repository Pattern**: Quản lý data source (JSON assets)
- **LiveData**: Observe data changes
- **Coroutines**: Async operations (API calls)
- **ViewBinding**: Type-safe view access

## Lưu ý

- Cần Internet để load Google Maps và Directions API
- Cần cấp quyền Location để lấy vị trí hiện tại
- API Key cần enable billing trên Google Cloud (miễn phí $200/tháng)

## Tác giả

Ninh Bình Tourism Map - 2026

