package com.drop.near.presentation.transfer

import com.drop.near.core.base.UiEffect
import com.drop.near.core.base.UiIntent
import com.drop.near.core.base.UiState
import com.drop.near.domain.model.TransferItem

data class TransferState(
    val isLoading: Boolean = false,
    val transfers: List<TransferItem> = emptyList(),
    val errorMessage: String? = null
) : UiState {
    val activeCount: Int get() = transfers.count { !it.status.isFinished }
    val completedCount: Int get() = transfers.count { it.isCompleted() }
}

sealed interface TransferIntent : UiIntent {
    data object LoadTransfers : TransferIntent
    data class CancelTransfer(val transferId: String) : TransferIntent
    data object ClearFinished : TransferIntent
}

sealed interface TransferEffect : UiEffect {
    data class ShowMessage(val text: String) : TransferEffect
}
