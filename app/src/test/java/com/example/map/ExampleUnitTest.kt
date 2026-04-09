package com.example.map

import com.example.map.data.model.TouristLocation
import com.example.map.utils.PolylineUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun touristLocation_defaultsAreApplied() {
        val location = TouristLocation(
            id = 1,
            name = "Tràng An",
            description = "Di sản UNESCO",
            latitude = 20.2525,
            longitude = 105.9000
        )

        assertEquals(0, location.imageRes)
        assertEquals("", location.imageName)
    }

    @Test
    fun decodePolyline_returnsExpectedPoints() {
        // Example from Google Encoded Polyline Algorithm Format docs
        val encoded = "_p~iF~ps|U_ulLnnqC_mqNvxq`@"

        val points = PolylineUtils.decodePolyline(encoded)

        assertEquals(3, points.size)
        assertEquals(38.5, points[0].latitude, 0.00001)
        assertEquals(-120.2, points[0].longitude, 0.00001)
        assertEquals(40.7, points[1].latitude, 0.00001)
        assertEquals(-120.95, points[1].longitude, 0.00001)
        assertEquals(43.252, points[2].latitude, 0.00001)
        assertEquals(-126.453, points[2].longitude, 0.00001)
    }

    @Test
    fun decodePolyline_emptyString_returnsEmptyList() {
        val points = PolylineUtils.decodePolyline("")
        assertTrue(points.isEmpty())
    }
}