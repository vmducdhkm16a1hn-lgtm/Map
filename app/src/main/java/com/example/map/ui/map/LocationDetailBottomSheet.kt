package com.example.map.ui.map

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.example.map.R
import com.example.map.data.model.TouristLocation
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton

class LocationDetailBottomSheet : BottomSheetDialogFragment() {

    private lateinit var location: TouristLocation

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, com.google.android.material.R.style.ThemeOverlay_Material3_BottomSheetDialog)
        @Suppress("DEPRECATION")
        location = arguments?.getParcelable(ARG_LOCATION)
            ?: throw IllegalStateException("Location argument is required")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_location_detail, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val imgDetail = view.findViewById<ImageView>(R.id.imgDetail)
        val tvName = view.findViewById<TextView>(R.id.tvDetailName)
        val tvRating = view.findViewById<TextView>(R.id.tvDetailRating)
        val tvAddress = view.findViewById<TextView>(R.id.tvDetailAddress)
        val tvDescription = view.findViewById<TextView>(R.id.tvDetailDescription)
        val tvCategoryTag = view.findViewById<TextView>(R.id.tvDetailCategoryTag)
        val btnDirections = view.findViewById<MaterialButton>(R.id.btnDetailDirections)
        val btnOpenMap = view.findViewById<MaterialButton>(R.id.btnDetailOpenMap)

        // Load image
        if (location.imageUrl.isNotEmpty()) {
            Glide.with(this)
                .load(location.imageUrl)
                .transition(DrawableTransitionOptions.withCrossFade())
                .placeholder(R.drawable.ic_launcher_background)
                .error(R.drawable.ic_launcher_background)
                .centerCrop()
                .into(imgDetail)
        } else if (location.imageRes != 0) {
            Glide.with(this)
                .load(location.imageRes)
                .centerCrop()
                .into(imgDetail)
        }

        tvName.text = location.name
        val reviewText = if (location.reviewCount > 0) " (${formatCount(location.reviewCount)} đánh giá)" else ""
        tvRating.text = "⭐ ${"%.1f".format(location.rating)}$reviewText"
        tvAddress.text = "📍 ${location.address}"
        tvDescription.text = location.description

        tvCategoryTag.text = when (detectCategory(location)) {
            "spiritual" -> "🏛 Du lịch tâm linh"
            "nature" -> "🌿 Thiên nhiên"
            else -> "📸 Điểm chụp ảnh"
        }

        btnDirections.setOnClickListener {
            dismiss()
            openGoogleMapsDirections()
        }

        btnOpenMap.setOnClickListener {
            dismiss()
            openMapWithLocation()
        }
    }

    private fun openMapWithLocation() {
        val intent = Intent(requireContext(), MapActivity::class.java).apply {
            putExtra(HomeActivity.EXTRA_LOCATION_NAME, location.name)
            putExtra(HomeActivity.EXTRA_LOCATION_DESC, location.description)
            putExtra(HomeActivity.EXTRA_LOCATION_LAT, location.latitude)
            putExtra(HomeActivity.EXTRA_LOCATION_LNG, location.longitude)
        }
        startActivity(intent)
    }

    private fun openGoogleMapsDirections() {
        val uri = Uri.parse("google.navigation:q=${location.latitude},${location.longitude}&mode=d")
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            setPackage("com.google.android.apps.maps")
        }
        if (intent.resolveActivity(requireContext().packageManager) != null) {
            startActivity(intent)
        } else {
            // Fallback: open in browser if Google Maps not installed
            val browserUri = Uri.parse(
                "https://www.google.com/maps/dir/?api=1&destination=${location.latitude},${location.longitude}"
            )
            startActivity(Intent(Intent.ACTION_VIEW, browserUri))
        }
    }

    private fun formatCount(count: Int): String {
        return if (count >= 1000) "${"%.1f".format(count / 1000.0)}k" else count.toString()
    }

    private fun detectCategory(item: TouristLocation): String {
        val text = "${item.name} ${item.description}".lowercase()
        return when {
            text.contains("chua") || text.contains("chùa") ||
                    text.contains("den") || text.contains("đền") ||
                    text.contains("nha tho") || text.contains("nhà thờ") ||
                    text.contains("co do") || text.contains("cố đô") -> "spiritual"
            text.contains("vuon") || text.contains("vườn") ||
                    text.contains("ho ") || text.contains("hồ") ||
                    text.contains("van long") || text.contains("vân long") ||
                    text.contains("kenh ga") || text.contains("kênh gà") -> "nature"
            else -> "photo"
        }
    }

    companion object {
        const val TAG = "LocationDetailBottomSheet"
        private const val ARG_LOCATION = "arg_location"

        fun newInstance(location: TouristLocation): LocationDetailBottomSheet {
            return LocationDetailBottomSheet().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_LOCATION, location)
                }
            }
        }
    }
}
