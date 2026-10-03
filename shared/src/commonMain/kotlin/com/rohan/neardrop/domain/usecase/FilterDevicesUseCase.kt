package com.rohan.neardrop.domain.usecase

import com.rohan.neardrop.domain.model.Device
import com.rohan.neardrop.domain.model.DeviceType

/**
 * Pure domain interactor encapsulating filtering and searching logic.
 * Independent of any platform or framework.
 */
class FilterDevicesUseCase {

    data class Params(
        val devices: List<Device>,
        val query: String,
        val typeFilter: DeviceType? = null,
        val favoritesOnly: Boolean = false
    )

    operator fun invoke(params: Params): List<Device> {
        val trimmedQuery = params.query.trim().lowercase()

        return params.devices.filter { device ->
            val matchesQuery = trimmedQuery.isEmpty() ||
                device.name.lowercase().contains(trimmedQuery) ||
                device.ipAddress.contains(trimmedQuery)

            val matchesType = params.typeFilter == null || device.type == params.typeFilter

            val matchesFavorites = !params.favoritesOnly || device.isFavorite

            matchesQuery && matchesType && matchesFavorites
        }.sortedWith(
            compareByDescending<Device> { it.isFavorite }
                .thenByDescending { it.rssi }
                .thenBy { it.name }
        )
    }
}
