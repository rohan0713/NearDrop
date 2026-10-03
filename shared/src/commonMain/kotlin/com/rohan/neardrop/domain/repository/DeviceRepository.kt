package com.rohan.neardrop.domain.repository

import com.rohan.neardrop.core.result.AppResult
import com.rohan.neardrop.domain.model.Device
import kotlinx.coroutines.flow.Flow

/**
 * Interface Segregation Principle & Dependency Inversion Principle:
 * Domain contract for discovery and persistence of nearby devices.
 */
interface DeviceRepository {

    /**
     * Observes real-time list of discovered devices on the local network.
     */
    fun observeDevices(): Flow<List<Device>>

    /**
     * Manually triggers a network discovery broadcast.
     */
    suspend fun refreshDiscovery(): AppResult<Unit>

    /**
     * Retrieves a specific device by its unique identifier.
     */
    suspend fun getDeviceById(id: String): AppResult<Device>

    /**
     * Marks or unmarks a device as a favorite.
     */
    suspend fun toggleFavorite(deviceId: String): AppResult<Boolean>
}
