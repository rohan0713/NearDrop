package com.drop.near.data.mapper

import com.drop.near.data.model.DeviceDto
import com.drop.near.domain.model.Device
import com.drop.near.domain.model.DeviceType

/**
 * Concrete mapper translating between DeviceDto and Device entity.
 * Keeps data representation separate from domain representation.
 */
class DeviceMapper : BiDirectionalMapper<DeviceDto, Device> {

    override fun map(from: DeviceDto): Device {
        return Device(
            id = from.id,
            name = from.name,
            type = mapDeviceType(from.type),
            ipAddress = from.ipAddress,
            port = from.port,
            rssi = from.rssi,
            isFavorite = false,
            lastSeenEpochMs = from.lastSeenEpochMs
        )
    }

    override fun mapBack(to: Device): DeviceDto {
        return DeviceDto(
            id = to.id,
            name = to.name,
            type = to.type.name,
            ipAddress = to.ipAddress,
            port = to.port,
            rssi = to.rssi,
            lastSeenEpochMs = to.lastSeenEpochMs
        )
    }

    private fun mapDeviceType(rawType: String): DeviceType {
        return try {
            DeviceType.valueOf(rawType.uppercase())
        } catch (_: Exception) {
            DeviceType.UNKNOWN
        }
    }
}
