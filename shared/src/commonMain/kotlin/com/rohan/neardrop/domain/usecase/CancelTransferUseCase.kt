package com.rohan.neardrop.domain.usecase

import com.rohan.neardrop.core.base.BaseUseCase
import com.rohan.neardrop.core.coroutines.CoroutineDispatchers
import com.rohan.neardrop.core.result.AppError
import com.rohan.neardrop.core.result.AppResult
import com.rohan.neardrop.domain.repository.TransferRepository

/**
 * Single Responsibility: Cancels an active or pending transfer.
 */
class CancelTransferUseCase(
    private val repository: TransferRepository,
    dispatchers: CoroutineDispatchers
) : BaseUseCase<String, Unit>(dispatchers) {

    override suspend fun execute(input: String): AppResult<Unit> {
        if (input.isBlank()) {
            return AppResult.Error(AppError.Validation.EmptyField("transferId"))
        }
        return repository.cancelTransfer(input)
    }
}
