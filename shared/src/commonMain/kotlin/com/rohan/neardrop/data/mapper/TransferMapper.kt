package com.rohan.neardrop.data.mapper

import com.rohan.neardrop.data.model.TransferDto
import com.rohan.neardrop.domain.model.TransferDirection
import com.rohan.neardrop.domain.model.TransferItem
import com.rohan.neardrop.domain.model.TransferStatus

/**
 * Concrete mapper translating between TransferDto and TransferItem entity.
 */
class TransferMapper(
    private val deviceMapper: DeviceMapper
) : Mapper<TransferDto, TransferItem> {

    override fun map(from: TransferDto): TransferItem {
        return TransferItem(
            id = from.id,
            device = deviceMapper.map(from.targetDevice),
            fileName = from.fileName,
            fileSizeBytes = from.fileSizeBytes,
            bytesTransferred = from.bytesTransferred,
            status = parseStatus(from.status),
            direction = parseDirection(from.direction),
            createdAtEpochMs = from.createdAtEpochMs
        )
    }

    private fun parseStatus(raw: String): TransferStatus {
        return when {
            raw.equals("QUEUED", ignoreCase = true) -> TransferStatus.Queued
            raw.startsWith("IN_PROGRESS", ignoreCase = true) -> TransferStatus.InProgress(0.5f)
            raw.equals("COMPLETED", ignoreCase = true) -> TransferStatus.Completed(0L)
            raw.startsWith("FAILED", ignoreCase = true) -> TransferStatus.Failed("Transfer failed")
            raw.equals("CANCELLED", ignoreCase = true) -> TransferStatus.Cancelled
            else -> TransferStatus.Queued
        }
    }

    private fun parseDirection(raw: String): TransferDirection {
        return try {
            TransferDirection.valueOf(raw.uppercase())
        } catch (_: Exception) {
            TransferDirection.OUTGOING
        }
    }
}
