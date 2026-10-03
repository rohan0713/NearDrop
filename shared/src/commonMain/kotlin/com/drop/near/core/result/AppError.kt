package com.drop.near.core.result

/**
 * Root domain error hierarchy demonstrating OOP encapsulation and polymorphism.
 */
sealed interface AppError {

    val message: String

    sealed interface Network : AppError {
        data class ConnectionLost(override val message: String = "Network connection lost") : Network
        data class Timeout(override val message: String = "Network request timed out") : Network
        data class HostUnreachable(override val message: String = "Host device is unreachable") : Network
    }

    sealed interface Storage : AppError {
        data class ReadFailed(override val message: String = "Failed to read data from local storage") : Storage
        data class WriteFailed(override val message: String = "Failed to write data to local storage") : Storage
        data class DiskFull(override val message: String = "Insufficient storage space") : Storage
    }

    sealed interface Validation : AppError {
        data class EmptyField(val fieldName: String, override val message: String = "$fieldName cannot be empty") : Validation
        data class InvalidPayload(override val message: String = "Transfer payload is invalid or empty") : Validation
    }

    sealed interface DeviceTransfer : AppError {
        data class DeviceNotFound(val deviceId: String, override val message: String = "Device $deviceId not found") : DeviceTransfer
        data class TransferDeclined(val deviceName: String, override val message: String = "$deviceName declined the transfer") : DeviceTransfer
        data class TransferFailed(val reason: String, override val message: String = "Transfer failed: $reason") : DeviceTransfer
    }

    data class Unknown(override val message: String, val cause: Throwable? = null) : AppError
}
