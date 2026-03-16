package com.example.map.data.model

import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem

/**
 * ClusterItem wrapper cho TouristLocation để sử dụng với Marker Clustering
 */
class LocationClusterItem(
    val location: TouristLocation
) : ClusterItem {

    override fun getPosition(): LatLng {
        return LatLng(location.latitude, location.longitude)
    }

    override fun getTitle(): String {
        return location.name
    }

    override fun getSnippet(): String {
        return location.description
    }

    override fun getZIndex(): Float {
        return 0f
    }
}

