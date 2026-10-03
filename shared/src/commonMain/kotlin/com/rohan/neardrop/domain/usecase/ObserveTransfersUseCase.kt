package com.rohan.neardrop.domain.usecase

import com.rohan.neardrop.core.base.BaseFlowUseCase
import com.rohan.neardrop.core.base.None
import com.rohan.neardrop.core.coroutines.CoroutineDispatchers
import com.rohan.neardrop.domain.model.TransferItem
import com.rohan.neardrop.domain.repository.TransferRepository
import kotlinx.coroutines.flow.Flow

/**
 * Single Responsibility: Observes the live stream of active and completed transfers.
 */
class ObserveTransfersUseCase(
    private val repository: TransferRepository,
    dispatchers: CoroutineDispatchers
) : BaseFlowUseCase<None, List<TransferItem>>(dispatchers) {

    override fun createFlow(input: None): Flow<List<TransferItem>> {
        return repository.observeTransfers()
    }
}
