package com.rohan.neardrop.domain.usecase

import com.rohan.neardrop.core.base.BaseUseCase
import com.rohan.neardrop.core.base.None
import com.rohan.neardrop.core.coroutines.CoroutineDispatchers
import com.rohan.neardrop.core.result.AppResult
import com.rohan.neardrop.domain.repository.DeviceRepository

/**
 * Single Responsibility: Manually triggers network device discovery scan.
 */
class RefreshDiscoveryUseCase(
    private val repository: DeviceRepository,
    dispatchers: CoroutineDispatchers
) : BaseUseCase<None, Unit>(dispatchers) {

    override suspend fun execute(input: None): AppResult<Unit> {
        return repository.refreshDiscovery()
    }
}
