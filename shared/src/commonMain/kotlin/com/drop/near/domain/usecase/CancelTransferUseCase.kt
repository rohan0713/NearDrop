package com.drop.near.domain.usecase

import com.drop.near.core.base.BaseUseCase
import com.drop.near.core.coroutines.CoroutineDispatchers
import com.drop.near.core.result.AppError
import com.drop.near.core.result.AppResult
import com.drop.near.domain.repository.TransferRepository

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
