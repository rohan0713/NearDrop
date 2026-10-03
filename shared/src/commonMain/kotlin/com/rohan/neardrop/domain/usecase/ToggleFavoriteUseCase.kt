package com.rohan.neardrop.domain.usecase

import com.rohan.neardrop.core.base.BaseUseCase
import com.rohan.neardrop.core.coroutines.CoroutineDispatchers
import com.rohan.neardrop.core.result.AppError
import com.rohan.neardrop.core.result.AppResult
import com.rohan.neardrop.domain.repository.DeviceRepository

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
