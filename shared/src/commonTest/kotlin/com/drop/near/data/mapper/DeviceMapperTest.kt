package com.drop.near.data.mapper

import com.drop.near.data.model.DeviceDto
import com.drop.near.domain.model.Device
import com.drop.near.domain.model.DeviceType
import kotlin.test.Test
import kotlin.test.assertEquals

class DeviceMapperTest {

    private val mapper = DeviceMapper()

    @Test
    fun map_converts_dto_to_domain_entity() {
        val dto = DeviceDto(
            id = "d1",
            name = "Test Phone",
            type = "PHONE",
            ipAddress = "10.0.0.1",
            port = 4500,
            rssi = -55,
            lastSeenEpochMs = 123456789L
        )

        val entity = mapper.map(dto)

        assertEquals("d1", entity.id)
        assertEquals("Test Phone", entity.name)
        assertEquals(DeviceType.PHONE, entity.type)
        assertEquals("10.0.0.1", entity.ipAddress)
        assertEquals(4500, entity.port)
        assertEquals(-55, entity.rssi)
        assertEquals(false, entity.isFavorite)
    }

    @Test
    fun map_handles_unknown_device_type_gracefully() {
        val dto = DeviceDto(
            id = "d2",
            name = "Unknown Smart TV",
            type = "SMART_TV_999",
            ipAddress = "10.0.0.2",
            port = 8080
        )

        val entity = mapper.map(dto)
        assertEquals(DeviceType.UNKNOWN, entity.type)
    }

    @Test
    fun mapBack_converts_domain_entity_to_dto() {
        val entity = Device(
            id = "d3",
            name = "Laptop Node",
            type = DeviceType.LAPTOP,
            ipAddress = "192.168.0.5",
            port = 3000,
            rssi = -60
        )

        val dto = mapper.mapBack(entity)

        assertEquals("d3", dto.id)
        assertEquals("Laptop Node", dto.name)
        assertEquals("LAPTOP", dto.type)
        assertEquals("192.168.0.5", dto.ipAddress)
    }
}
