package com.drop.near.domain.usecase

import com.drop.near.core.base.BaseFlowUseCase
import com.drop.near.core.base.None
import com.drop.near.core.coroutines.CoroutineDispatchers
import com.drop.near.domain.model.Device
import com.drop.near.domain.repository.DeviceRepository
import kotlinx.coroutines.flow.Flow

/**
 * Single Responsibility: Observes the live stream of discoverable nearby devices.
 */
class ObserveDevicesUseCase(
    private val repository: DeviceRepository,
    dispatchers: CoroutineDispatchers
) : BaseFlowUseCase<None, List<Device>>(dispatchers) {

    override fun createFlow(input: None): Flow<List<Device>> {
        return repository.observeDevices()
    }
}
