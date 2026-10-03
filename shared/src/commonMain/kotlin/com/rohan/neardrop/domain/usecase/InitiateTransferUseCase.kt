package com.rohan.neardrop.domain.usecase

import com.rohan.neardrop.core.base.BaseUseCase
import com.rohan.neardrop.core.coroutines.CoroutineDispatchers
import com.rohan.neardrop.core.result.AppError
import com.rohan.neardrop.core.result.AppResult
import com.rohan.neardrop.domain.model.Device
import com.rohan.neardrop.domain.model.TransferItem
import com.rohan.neardrop.domain.repository.TransferRepository

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
