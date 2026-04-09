# Ninh Binh Tourism App (Map)

## 1) Tong quan du an
Ung dung Android gioi thieu dia diem du lich Ninh Binh, gom 3 man chinh:
- `Home`: danh sach dia diem, bo loc theo danh muc, tim kiem, them vao lich trinh.
- `Location Detail`: xem thong tin chi tiet dia diem.
- `Map`: hien marker, lay vi tri hien tai, ve tuyen duong den dia diem da chon.
- `Itinerary`: quan ly danh sach dia diem da them vao lich trinh.

Du an su dung Kotlin + ViewBinding + MVVM (cho man hinh Map) + Google Maps + Directions API.

---

## 2) Chuc nang da hoan thien

### 2.1 Home (`HomeActivity`)
- Hien thi danh sach dia diem du lich tu `assets/locations.json`.
- Tim kiem theo ten/mo ta.
- Loc theo chip danh muc (`Tat ca`, `Tam linh`, `Diem chup anh`, `Thien nhien`).
- Bam vao card de mo trang chi tiet.
- Bam nut `+` tren card de them vao lich trinh.
- Nut `Lich trinh (n)` mo man hinh lich trinh, dong bo so luong theo du lieu luu.

### 2.2 Chi tiet dia diem (`LocationDetailActivity`)
- Hien thi anh, ten, mo ta, dia chi, rating, gio mo cua, chi phi, tips.
- Nut mo map noi bo (`MapActivity`) voi thong tin dia diem dang xem.
- Nut mo Google Maps de dan duong ngoai app.

### 2.3 Ban do (`MapActivity` + `MapViewModel`)
- Hien Google Map tap trung khu vuc Ninh Binh.
- Hien marker cac dia diem du lich (co clustering).
- Lay vi tri hien tai (Fused Location Provider).
- Ve polyline tu vi tri hien tai den dia diem da chon.
- Nhanh `fabResetMap` de dua map ve trang thai goi y ban dau.
- UI hien tai chi con **1 nut ve tuyen** trong card thong tin dia diem.

### 2.4 Lich trinh (`ItineraryActivity`)
- Hien danh sach dia diem da them.
- Trang thai rong (empty state) neu chua co du lieu.
- Xoa tung dia diem khoi lich trinh.
- Du lieu lich trinh duoc luu bang `SharedPreferences` (JSON/Gson).

---

## 3) Cau truc code theo tinh nang

## 3.1 UI / Screen
- `app/src/main/java/com/example/map/ui/map/HomeActivity.kt`
- `app/src/main/java/com/example/map/ui/map/LocationDetailActivity.kt`
- `app/src/main/java/com/example/map/ui/map/MapActivity.kt`
- `app/src/main/java/com/example/map/ui/map/MapViewModel.kt`
- `app/src/main/java/com/example/map/ui/map/ItineraryActivity.kt`

## 3.2 Adapter
- `app/src/main/java/com/example/map/ui/adapter/HomeLocationAdapter.kt`
- `app/src/main/java/com/example/map/ui/adapter/ItineraryAdapter.kt`

## 3.3 Data / Repository / API
- `app/src/main/java/com/example/map/data/model/Location.kt` (data class `TouristLocation`)
- `app/src/main/java/com/example/map/data/model/LocationClusterItem.kt`
- `app/src/main/java/com/example/map/data/model/DirectionsResponse.kt`
- `app/src/main/java/com/example/map/data/repository/LocationRepository.kt`
- `app/src/main/java/com/example/map/data/repository/ItineraryRepository.kt`
- `app/src/main/java/com/example/map/data/remote/DirectionsApiService.kt`
- `app/src/main/java/com/example/map/data/remote/RetrofitClient.kt`
- `app/src/main/java/com/example/map/utils/PolylineUtils.kt`

## 3.4 Layout / Resource
- `app/src/main/res/layout/activity_home.xml`
- `app/src/main/res/layout/item_home_location.xml`
- `app/src/main/res/layout/activity_location_detail.xml`
- `app/src/main/res/layout/activity_map.xml`
- `app/src/main/res/layout/activity_itinerary.xml`
- `app/src/main/res/layout/item_itinerary_location.xml`
- `app/src/main/res/values/strings.xml`
- `app/src/main/assets/locations.json`

## 3.5 Cau hinh app
- `app/src/main/AndroidManifest.xml`
- `app/build.gradle.kts`
- `local.properties` (khai bao `MAPS_API_KEY`)

---

## 4) Luong su dung chinh
1. Mo app vao `HomeActivity`.
2. Chon dia diem:
   - Bam card -> `LocationDetailActivity`.
   - Bam `+` -> them vao lich trinh.
3. Tu `Home`, bam `Lich trinh (n)` -> `ItineraryActivity`.
4. Tu `Detail`, mo `MapActivity` de xem marker va ve tuyen den diem den.

---

## 5) Huong dan setup nhanh

### 5.1 Yeu cau
- Android Studio (ban moi)
- JDK 11
- Android SDK (minSdk 24, targetSdk 35)
- Google Maps API key (Directions + Maps Android SDK)

### 5.2 Khai bao API key
Trong `local.properties`:

```properties
MAPS_API_KEY=YOUR_REAL_API_KEY
```

### 5.3 Chay du an
```powershell
.\gradlew.bat :app:assembleDebug --console=plain
```

Sau do chay app bang Android Studio (Run `app`).

---

## 6) Thu vien chinh dang dung
- `com.google.android.gms:play-services-maps`
- `com.google.android.gms:play-services-location`
- `com.google.maps.android:android-maps-utils`
- `com.squareup.retrofit2:retrofit`
- `com.squareup.retrofit2:converter-gson`
- `com.google.code.gson:gson`
- `androidx.lifecycle` (ViewModel + LiveData)
- `org.jetbrains.kotlinx:kotlinx-coroutines-android`

---

## 7) Luu y quan trong
- Quyen vi tri (`ACCESS_FINE_LOCATION`, `ACCESS_COARSE_LOCATION`) bat buoc cho chuc nang ve route.
- Neu key sai/het quota, route va map API co the loi.
- Du lieu lich trinh hien tai luu local tren may (SharedPreferences), chua dong bo cloud.

---

## 8) Backlog de nang cap tiep
- Them nut `Xoa tat ca` trong man hinh lich trinh.
- Sap xep lich trinh theo thu tu/nhom danh muc.
- Them test tu dong cho `ItineraryRepository`.
- Polish UI theo Material 3 (icon, spacing, color consistency).

---

## 9) Tinh trang hien tai
- Build debug da pass.
- Chuc nang cot loi theo yeu cau hien tai da co.
- Co the tiep tuc vao pha polish UI + test release.

