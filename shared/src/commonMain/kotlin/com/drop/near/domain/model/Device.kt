package com.drop.near.domain.model

/**
 * Categorization of discoverable devices.
 */
enum class DeviceType {
    PHONE,
    TABLET,
    LAPTOP,
    DESKTOP,
    UNKNOWN
}

/**
 * Signal strength tiers calculated via RSSI values.
 */
enum class SignalStrength {
    EXCELLENT,
    GOOD,
    FAIR,
    POOR
}

/**
 * Core domain entity representing a nearby network device.
 * Encapsulates properties and domain-specific business calculations.
 */
data class Device(
    val id: String,
    val name: String,
    val type: DeviceType,
    val ipAddress: String,
    val port: Int,
    val rssi: Int = -60,
    val isFavorite: Boolean = false,
    val lastSeenEpochMs: Long = 0L
) {
    init {
        require(id.isNotBlank()) { "Device ID cannot be blank" }
        require(name.isNotBlank()) { "Device name cannot be blank" }
        require(port in 1..65535) { "Port must be in valid TCP/UDP range (1-65535)" }
    }

    /**
     * Domain business calculation for signal tier based on RSSI (in dBm).
     */
    val signalStrength: SignalStrength
        get() = when {
            rssi >= -55 -> SignalStrength.EXCELLENT
            rssi >= -67 -> SignalStrength.GOOD
            rssi >= -80 -> SignalStrength.FAIR
            else -> SignalStrength.POOR
        }

    /**
     * Business logic for checking device availability.
     */
    fun isAvailable(currentTimeMs: Long, timeoutMs: Long = 60_000L): Boolean {
        if (lastSeenEpochMs <= 0L) return true
        return (currentTimeMs - lastSeenEpochMs) < timeoutMs
    }
}
