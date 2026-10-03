package com.drop.near.domain.usecase

import com.drop.near.core.base.BaseUseCase
import com.drop.near.core.base.None
import com.drop.near.core.coroutines.CoroutineDispatchers
import com.drop.near.core.result.AppResult
import com.drop.near.domain.repository.DeviceRepository

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
