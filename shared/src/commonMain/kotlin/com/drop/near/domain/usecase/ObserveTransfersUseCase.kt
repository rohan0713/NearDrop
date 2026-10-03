package com.drop.near.domain.usecase

import com.drop.near.core.base.BaseFlowUseCase
import com.drop.near.core.base.None
import com.drop.near.core.coroutines.CoroutineDispatchers
import com.drop.near.domain.model.TransferItem
import com.drop.near.domain.repository.TransferRepository
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
