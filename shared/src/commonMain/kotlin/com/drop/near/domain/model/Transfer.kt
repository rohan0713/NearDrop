package com.drop.near.domain.model

enum class TransferDirection {
    INCOMING,
    OUTGOING
}

/**
 * Algebraic data type representing current status of a file transfer session.
 */
sealed interface TransferStatus {
    data object Queued : TransferStatus
    data class InProgress(val progress: Float, val speedBytesPerSec: Long = 0L) : TransferStatus
    data class Completed(val completedAtEpochMs: Long) : TransferStatus
    data class Failed(val reason: String) : TransferStatus
    data object Cancelled : TransferStatus

    val isFinished: Boolean
        get() = this is Completed || this is Failed || this is Cancelled
}

/**
 * Core domain entity representing a file transfer session.
 */
data class TransferItem(
    val id: String,
    val device: Device,
    val fileName: String,
    val fileSizeBytes: Long,
    val bytesTransferred: Long = 0L,
    val status: TransferStatus = TransferStatus.Queued,
    val direction: TransferDirection = TransferDirection.OUTGOING,
    val createdAtEpochMs: Long = 0L
) {
    init {
        require(id.isNotBlank()) { "Transfer ID cannot be blank" }
        require(fileName.isNotBlank()) { "File name cannot be blank" }
        require(fileSizeBytes >= 0) { "File size must be non-negative" }
    }

    val progressFraction: Float
        get() = if (fileSizeBytes > 0) {
            (bytesTransferred.toFloat() / fileSizeBytes.toFloat()).coerceIn(0f, 1f)
        } else {
            0f
        }

    val progressPercent: Int
        get() = (progressFraction * 100).toInt()

    fun isCompleted(): Boolean = status is TransferStatus.Completed
}
