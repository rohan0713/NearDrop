package com.rohan.neardrop.domain.usecase

import com.rohan.neardrop.domain.model.Device
import com.rohan.neardrop.domain.model.DeviceType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FilterDevicesUseCaseTest {

    private val useCase = FilterDevicesUseCase()

    private val sampleDevices = listOf(
        Device("1", "MacBook Pro", DeviceType.LAPTOP, "192.168.1.10", 5000, rssi = -50, isFavorite = true),
        Device("2", "Pixel 9", DeviceType.PHONE, "192.168.1.11", 5000, rssi = -60, isFavorite = false),
        Device("3", "Galaxy Tab", DeviceType.TABLET, "192.168.1.12", 5000, rssi = -80, isFavorite = false),
        Device("4", "Workstation", DeviceType.DESKTOP, "192.168.1.13", 5000, rssi = -40, isFavorite = false)
    )

    @Test
    fun filter_by_empty_query_returns_all_devices_sorted() {
        val result = useCase(
            FilterDevicesUseCase.Params(
                devices = sampleDevices,
                query = ""
            )
        )
        assertEquals(4, result.size)
        // Favorite should be sorted to top
        assertTrue(result.first().isFavorite)
    }

    @Test
    fun filter_by_name_query_matches_correctly() {
        val result = useCase(
            FilterDevicesUseCase.Params(
                devices = sampleDevices,
                query = "pixel"
            )
        )
        assertEquals(1, result.size)
        assertEquals("Pixel 9", result.first().name)
    }

    @Test
    fun filter_by_device_type_restricts_results() {
        val result = useCase(
            FilterDevicesUseCase.Params(
                devices = sampleDevices,
                query = "",
                typeFilter = DeviceType.LAPTOP
            )
        )
        assertEquals(1, result.size)
        assertEquals(DeviceType.LAPTOP, result.first().type)
    }

    @Test
    fun filter_by_favorites_only_returns_favorited_devices() {
        val result = useCase(
            FilterDevicesUseCase.Params(
                devices = sampleDevices,
                query = "",
                favoritesOnly = true
            )
        )
        assertEquals(1, result.size)
        assertEquals("MacBook Pro", result.first().name)
    }
}
