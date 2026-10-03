package com.drop.near.domain.usecase

import com.drop.near.core.base.BaseUseCase
import com.drop.near.core.coroutines.CoroutineDispatchers
import com.drop.near.core.result.AppError
import com.drop.near.core.result.AppResult
import com.drop.near.domain.model.Device
import com.drop.near.domain.model.TransferItem
import com.drop.near.domain.repository.TransferRepository

/**
 * Validates transfer parameters and initiates file transfer to a target device.
 */
class InitiateTransferUseCase(
    private val repository: TransferRepository,
    dispatchers: CoroutineDispatchers
) : BaseUseCase<InitiateTransferUseCase.Params, TransferItem>(dispatchers) {

    data class Params(
        val targetDevice: Device,
        val fileName: String,
        val fileSizeBytes: Long
    )

    override suspend fun execute(input: Params): AppResult<TransferItem> {
        if (input.fileName.isBlank()) {
            return AppResult.Error(AppError.Validation.EmptyField("fileName"))
        }
        if (input.fileSizeBytes <= 0) {
            return AppResult.Error(AppError.Validation.InvalidPayload("File size must be greater than 0"))
        }

        return repository.initiateTransfer(
            targetDevice = input.targetDevice,
            fileName = input.fileName,
            fileSizeBytes = input.fileSizeBytes
        )
    }
}
