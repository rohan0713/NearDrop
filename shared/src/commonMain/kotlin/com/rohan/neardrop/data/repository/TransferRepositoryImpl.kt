package com.rohan.neardrop.data.repository

import com.rohan.neardrop.core.coroutines.CoroutineDispatchers
import com.rohan.neardrop.core.result.AppError
import com.rohan.neardrop.core.result.AppResult
import com.rohan.neardrop.domain.model.Device
import com.rohan.neardrop.domain.model.TransferDirection
import com.rohan.neardrop.domain.model.TransferItem
import com.rohan.neardrop.domain.model.TransferStatus
import com.rohan.neardrop.domain.repository.TransferRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Concrete implementation of [TransferRepository] managing in-flight transfers and progress.
 */
class TransferRepositoryImpl(
    private val dispatchers: CoroutineDispatchers
) : TransferRepository {

    private val mutex = Mutex()
    private val scope = CoroutineScope(dispatchers.default)
    private val activeJobs = mutableMapOf<String, Job>()
    private val _transfers = MutableStateFlow<List<TransferItem>>(emptyList())

    override fun observeTransfers(): Flow<List<TransferItem>> {
        return _transfers.asStateFlow()
    }

    override suspend fun initiateTransfer(
        targetDevice: Device,
        fileName: String,
        fileSizeBytes: Long
    ): AppResult<TransferItem> = mutex.withLock {
        val transferId = "tx-${generateTransferId()}"
        val initialItem = TransferItem(
            id = transferId,
            device = targetDevice,
            fileName = fileName,
            fileSizeBytes = fileSizeBytes,
            bytesTransferred = 0L,
            status = TransferStatus.Queued,
            direction = TransferDirection.OUTGOING,
            createdAtEpochMs = currentTimeEpoch()
        )

        _transfers.update { listOf(initialItem) + it }

        // Start progressive transfer simulation
        val transferJob = scope.launch(dispatchers.io) {
            simulateTransferProgress(transferId, fileSizeBytes)
        }
        activeJobs[transferId] = transferJob

        AppResult.Success(initialItem)
    }

    override suspend fun cancelTransfer(transferId: String): AppResult<Unit> = mutex.withLock {
        activeJobs[transferId]?.cancel()
        activeJobs.remove(transferId)

        _transfers.update { list ->
            list.map { item ->
                if (item.id == transferId) {
                    item.copy(status = TransferStatus.Cancelled)
                } else {
                    item
                }
            }
        }
        AppResult.Success(Unit)
    }

    override suspend fun clearFinishedTransfers(): AppResult<Unit> = mutex.withLock {
        _transfers.update { list ->
            list.filterNot { it.status.isFinished }
        }
        AppResult.Success(Unit)
    }

    private suspend fun simulateTransferProgress(transferId: String, totalBytes: Long) {
        val chunkSize = (totalBytes / 10).coerceAtLeast(1024L)
        var transferred = 0L

        while (transferred < totalBytes) {
            delay(300)
            transferred = (transferred + chunkSize).coerceAtMost(totalBytes)
            val progress = (transferred.toFloat() / totalBytes.toFloat()).coerceIn(0f, 1f)

            _transfers.update { list ->
                list.map { item ->
                    if (item.id == transferId) {
                        item.copy(
                            bytesTransferred = transferred,
                            status = if (transferred >= totalBytes) {
                                TransferStatus.Completed(currentTimeEpoch())
                            } else {
                                TransferStatus.InProgress(progress, speedBytesPerSec = chunkSize * 3)
                            }
                        )
                    } else {
                        item
                    }
                }
            }
        }
    }

    private var idCounter = 1000L
    private fun generateTransferId(): Long = idCounter++
    private fun currentTimeEpoch(): Long = 1727913600000L + (idCounter * 1000)
}
