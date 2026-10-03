package com.drop.near.domain.repository

import com.drop.near.core.result.AppResult
import com.drop.near.domain.model.Device
import com.drop.near.domain.model.TransferItem
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

    /**
     * Updates ongoing transfer progress and throughput speed.
     */
    suspend fun updateTransferProgress(
        transferId: String,
        bytesTransferred: Long,
        speedBytesPerSec: Long = 0L
    ): AppResult<Unit> = AppResult.Success(Unit)

    /**
     * Marks a transfer as successfully completed.
     */
    suspend fun completeTransfer(transferId: String): AppResult<Unit> = AppResult.Success(Unit)

    /**
     * Marks a transfer as failed with an explanatory reason.
     */
    suspend fun failTransfer(transferId: String, reason: String): AppResult<Unit> = AppResult.Success(Unit)
}
