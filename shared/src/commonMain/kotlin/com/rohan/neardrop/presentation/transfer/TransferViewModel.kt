package com.rohan.neardrop.presentation.transfer

import com.rohan.neardrop.core.base.BaseViewModel
import com.rohan.neardrop.core.base.None
import com.rohan.neardrop.core.coroutines.CoroutineDispatchers
import com.rohan.neardrop.domain.repository.TransferRepository
import com.rohan.neardrop.domain.usecase.CancelTransferUseCase
import com.rohan.neardrop.domain.usecase.ObserveTransfersUseCase
import com.rohan.neardrop.core.result.onError
import com.rohan.neardrop.core.result.onSuccess
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

/**
 * Shared ViewModel managing active and completed transfer sessions.
 */
class TransferViewModel(
    private val observeTransfersUseCase: ObserveTransfersUseCase,
    private val cancelTransferUseCase: CancelTransferUseCase,
    private val transferRepository: TransferRepository,
    dispatchers: CoroutineDispatchers
) : BaseViewModel<TransferState, TransferIntent, TransferEffect>(
    initialState = TransferState(),
    dispatchers = dispatchers
) {

    init {
        onIntent(TransferIntent.LoadTransfers)
    }

    override fun onIntent(intent: TransferIntent) {
        when (intent) {
            is TransferIntent.LoadTransfers -> loadTransfers()
            is TransferIntent.CancelTransfer -> handleCancel(intent.transferId)
            is TransferIntent.ClearFinished -> handleClearFinished()
        }
    }

    private fun loadTransfers() {
        updateState { copy(isLoading = true) }
        viewModelScope.launch {
            observeTransfersUseCase(None)
                .catch { error ->
                    updateState {
                        copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Failed to observe transfers"
                        )
                    }
                }
                .collect { transferList ->
                    updateState {
                        copy(
                            isLoading = false,
                            transfers = transferList
                        )
                    }
                }
        }
    }

    private fun handleCancel(transferId: String) {
        viewModelScope.launch {
            cancelTransferUseCase(transferId)
                .onSuccess {
                    emitEffect(TransferEffect.ShowMessage("Transfer cancelled"))
                }
                .onError { error ->
                    emitEffect(TransferEffect.ShowMessage(error.message))
                }
        }
    }

    private fun handleClearFinished() {
        viewModelScope.launch {
            transferRepository.clearFinishedTransfers()
                .onSuccess {
                    emitEffect(TransferEffect.ShowMessage("Cleared completed transfers"))
                }
        }
    }
}
