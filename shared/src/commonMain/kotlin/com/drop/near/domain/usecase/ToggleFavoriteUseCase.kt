package com.drop.near.domain.usecase

import com.drop.near.core.base.BaseUseCase
import com.drop.near.core.coroutines.CoroutineDispatchers
import com.drop.near.core.result.AppError
import com.drop.near.core.result.AppResult
import com.drop.near.domain.repository.DeviceRepository

/**
 * Single Responsibility: Toggles favorite bookmark status of a device.
 */
class ToggleFavoriteUseCase(
    private val repository: DeviceRepository,
    dispatchers: CoroutineDispatchers
) : BaseUseCase<String, Boolean>(dispatchers) {

    override suspend fun execute(input: String): AppResult<Boolean> {
        if (input.isBlank()) {
            return AppResult.Error(AppError.Validation.EmptyField("deviceId"))
        }
        return repository.toggleFavorite(input)
    }
}
