package com.rohan.neardrop.domain.repository

import com.rohan.neardrop.core.result.AppResult
import com.rohan.neardrop.domain.model.Device
import com.rohan.neardrop.domain.model.TransferItem
import kotlinx.coroutines.flow.Flow

/**
 * Domain contract for orchestrating file transfers across network boundaries.
 */
interface TransferRepository {

    /**
     * Observes real-time list of transfers and their ongoing progress.
     */
    fun observeTransfers(): Flow<List<TransferItem>>

    /**
     * Initiates a new outbound file transfer to the specified target device.
     */
    suspend fun initiateTransfer(
        targetDevice: Device,
        fileName: String,
        fileSizeBytes: Long
    ): AppResult<TransferItem>

    /**
     * Cancels an active or pending transfer.
     */
    suspend fun cancelTransfer(transferId: String): AppResult<Unit>

    /**
     * Clears finished (completed, failed, cancelled) transfers from session history.
     */
    suspend fun clearFinishedTransfers(): AppResult<Unit>
}
