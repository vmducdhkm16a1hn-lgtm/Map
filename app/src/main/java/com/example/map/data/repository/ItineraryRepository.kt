package com.example.map.data.repository

import android.content.Context
import com.example.map.data.model.TouristLocation
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object ItineraryRepository {

    private const val PREFS_NAME = "itinerary_prefs"
    private const val KEY_ITEMS = "itinerary_items"

    fun getAll(context: Context): List<TouristLocation> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val raw = prefs.getString(KEY_ITEMS, null) ?: return emptyList()
        return try {
            val listType = object : TypeToken<List<TouristLocation>>() {}.type
            Gson().fromJson<List<TouristLocation>>(raw, listType) ?: emptyList()
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun add(context: Context, location: TouristLocation): Boolean {
        val current = getAll(context).toMutableList()
        if (current.any { it.id == location.id }) return false
        current.add(location)
        save(context, current)
        return true
    }

    fun remove(context: Context, locationId: Int) {
        val updated = getAll(context).filterNot { it.id == locationId }
        save(context, updated)
    }

    private fun save(context: Context, items: List<TouristLocation>) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_ITEMS, Gson().toJson(items)).apply()
    }
}

