package com.rohan.neardrop.data.datasource.remote

import com.rohan.neardrop.core.result.AppResult
import com.rohan.neardrop.data.model.DeviceDto
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Abstraction for network discovery and communications.
 */
interface DeviceRemoteDataSource {
    fun observeDiscoveredDevices(): Flow<List<DeviceDto>>
    suspend fun triggerDiscoveryScan(): AppResult<List<DeviceDto>>
}

/**
 * Realistic network simulation implementation of [DeviceRemoteDataSource].
 * Discovers nearby multiplatform devices (Android, Mac, Windows, Linux, iPad).
 */
class MockDeviceRemoteDataSource : DeviceRemoteDataSource {

    private val _devicesFlow = MutableSharedFlow<List<DeviceDto>>(replay = 1)
    private val seedDevices = listOf(
        DeviceDto(
            id = "dev-pixel-9",
            name = "Rohan's Pixel 9 Pro",
            type = "PHONE",
            ipAddress = "192.168.1.105",
            port = 52184,
            rssi = -48,
            lastSeenEpochMs = 1727913600000L
        ),
        DeviceDto(
            id = "dev-macbook-pro",
            name = "MacBook Pro M3 Max",
            type = "LAPTOP",
            ipAddress = "192.168.1.110",
            port = 52184,
            rssi = -55,
            lastSeenEpochMs = 1727913605000L
        ),
        DeviceDto(
            id = "dev-ipad-air",
            name = "Living Room iPad Air",
            type = "TABLET",
            ipAddress = "192.168.1.120",
            port = 52184,
            rssi = -68,
            lastSeenEpochMs = 1727913590000L
        ),
        DeviceDto(
            id = "dev-workstation",
            name = "Linux Studio Workstation",
            type = "DESKTOP",
            ipAddress = "192.168.1.200",
            port = 52184,
            rssi = -72,
            lastSeenEpochMs = 1727913500000L
        ),
        DeviceDto(
            id = "dev-galaxy-tab",
            name = "Galaxy Tab S9 Ultra",
            type = "TABLET",
            ipAddress = "192.168.1.135",
            port = 52184,
            rssi = -82,
            lastSeenEpochMs = 1727913400000L
        )
    )

    init {
        _devicesFlow.tryEmit(seedDevices)
    }

    override fun observeDiscoveredDevices(): Flow<List<DeviceDto>> {
        return _devicesFlow.asSharedFlow()
    }

    override suspend fun triggerDiscoveryScan(): AppResult<List<DeviceDto>> {
        // Simulate network broadcast latency
        delay(400)
        _devicesFlow.emit(seedDevices)
        return AppResult.Success(seedDevices)
    }
}
