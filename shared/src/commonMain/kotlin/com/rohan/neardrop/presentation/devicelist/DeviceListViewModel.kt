package com.rohan.neardrop.presentation.devicelist

import com.rohan.neardrop.core.base.BaseViewModel
import com.rohan.neardrop.core.base.None
import com.rohan.neardrop.core.coroutines.CoroutineDispatchers
import com.rohan.neardrop.domain.usecase.FilterDevicesUseCase
import com.rohan.neardrop.domain.usecase.InitiateTransferUseCase
import com.rohan.neardrop.domain.usecase.ObserveDevicesUseCase
import com.rohan.neardrop.domain.usecase.RefreshDiscoveryUseCase
import com.rohan.neardrop.domain.usecase.ToggleFavoriteUseCase
import com.rohan.neardrop.core.result.onError
import com.rohan.neardrop.core.result.onSuccess
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

/**
 * Shared ViewModel orchestrating the device discovery screen.
 * Consumed natively by Jetpack Compose on Android and SwiftUI on iOS.
 *
 * Implements MVI pattern with strict encapsulation.
 */
class DeviceListViewModel(
    private val observeDevicesUseCase: ObserveDevicesUseCase,
    private val refreshDiscoveryUseCase: RefreshDiscoveryUseCase,
    private val filterDevicesUseCase: FilterDevicesUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
    private val initiateTransferUseCase: InitiateTransferUseCase,
    dispatchers: CoroutineDispatchers
) : BaseViewModel<DeviceListState, DeviceListIntent, DeviceListEffect>(
    initialState = DeviceListState(),
    dispatchers = dispatchers
) {

    init {
        onIntent(DeviceListIntent.Load)
    }

    override fun onIntent(intent: DeviceListIntent) {
        when (intent) {
            is DeviceListIntent.Load -> loadDevices()
            is DeviceListIntent.Scan -> performScan()
            is DeviceListIntent.UpdateSearch -> handleSearch(intent.query)
            is DeviceListIntent.SelectTypeFilter -> handleFilter(intent.type)
            is DeviceListIntent.ToggleFavoritesOnly -> handleFavoritesOnly(intent.enabled)
            is DeviceListIntent.ToggleFavorite -> handleToggleFavorite(intent.deviceId)
            is DeviceListIntent.SelectDevice -> emitEffect(DeviceListEffect.OpenSendDialog(intent.device))
            is DeviceListIntent.SendFile -> handleSendFile(intent)
            is DeviceListIntent.DismissError -> updateState { copy(errorMessage = null) }
        }
    }

    private fun loadDevices() {
        viewModelScope.launch {
            observeDevicesUseCase(None)
                .catch { error ->
                    updateState {
                        copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Failed to observe devices"
                        )
                    }
                }
                .collect { devices ->
                    updateState {
                        val filtered = filterDevicesUseCase(
                            FilterDevicesUseCase.Params(
                                devices = devices,
                                query = searchQuery,
                                typeFilter = selectedType,
                                favoritesOnly = favoritesOnly
                            )
                        )
                        copy(
                            isLoading = false,
                            rawDevices = devices,
                            displayedDevices = filtered
                        )
                    }
                }
        }
    }

    private fun performScan() {
        updateState { copy(isScanning = true) }
        viewModelScope.launch {
            refreshDiscoveryUseCase(None)
                .onSuccess {
                    updateState { copy(isScanning = false) }
                    emitEffect(DeviceListEffect.ShowToast("Discovery scan completed"))
                }
                .onError { error ->
                    updateState {
                        copy(
                            isScanning = false,
                            errorMessage = error.message
                        )
                    }
                }
        }
    }

    private fun handleSearch(query: String) {
        updateState {
            val filtered = filterDevicesUseCase(
                FilterDevicesUseCase.Params(
                    devices = rawDevices,
                    query = query,
                    typeFilter = selectedType,
                    favoritesOnly = favoritesOnly
                )
            )
            copy(searchQuery = query, displayedDevices = filtered)
        }
    }

    private fun handleFilter(type: com.rohan.neardrop.domain.model.DeviceType?) {
        updateState {
            val filtered = filterDevicesUseCase(
                FilterDevicesUseCase.Params(
                    devices = rawDevices,
                    query = searchQuery,
                    typeFilter = type,
                    favoritesOnly = favoritesOnly
                )
            )
            copy(selectedType = type, displayedDevices = filtered)
        }
    }

    private fun handleFavoritesOnly(enabled: Boolean) {
        updateState {
            val filtered = filterDevicesUseCase(
                FilterDevicesUseCase.Params(
                    devices = rawDevices,
                    query = searchQuery,
                    typeFilter = selectedType,
                    favoritesOnly = enabled
                )
            )
            copy(favoritesOnly = enabled, displayedDevices = filtered)
        }
    }

    private fun handleToggleFavorite(deviceId: String) {
        viewModelScope.launch {
            toggleFavoriteUseCase(deviceId)
                .onSuccess { isFavorite ->
                    val message = if (isFavorite) "Added to favorites" else "Removed from favorites"
                    emitEffect(DeviceListEffect.ShowToast(message))
                }
                .onError { error ->
                    emitEffect(DeviceListEffect.ShowToast(error.message))
                }
        }
    }

    private fun handleSendFile(intent: DeviceListIntent.SendFile) {
        viewModelScope.launch {
            initiateTransferUseCase(
                InitiateTransferUseCase.Params(
                    targetDevice = intent.device,
                    fileName = intent.fileName,
                    fileSizeBytes = intent.sizeBytes
                )
            ).onSuccess { transferItem ->
                emitEffect(
                    DeviceListEffect.NavigateToTransfer(
                        transferId = transferItem.id,
                        deviceName = intent.device.name
                    )
                )
            }.onError { error ->
                emitEffect(DeviceListEffect.ShowToast("Transfer failed: ${error.message}"))
            }
        }
    }
}
