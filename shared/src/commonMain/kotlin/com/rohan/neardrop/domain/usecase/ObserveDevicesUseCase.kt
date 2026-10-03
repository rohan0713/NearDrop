package com.rohan.neardrop.domain.usecase

import com.rohan.neardrop.core.base.BaseFlowUseCase
import com.rohan.neardrop.core.base.None
import com.rohan.neardrop.core.coroutines.CoroutineDispatchers
import com.rohan.neardrop.domain.model.Device
import com.rohan.neardrop.domain.repository.DeviceRepository
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
