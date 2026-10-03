package com.rohan.neardrop.presentation.transfer

import com.rohan.neardrop.core.base.UiEffect
import com.rohan.neardrop.core.base.UiIntent
import com.rohan.neardrop.core.base.UiState
import com.rohan.neardrop.domain.model.TransferItem

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
