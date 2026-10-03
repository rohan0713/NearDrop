package com.drop.near.presentation.devicelist

import com.drop.near.core.base.UiEffect
import com.drop.near.core.base.UiIntent
import com.drop.near.core.base.UiState
import com.drop.near.domain.model.Device
import com.drop.near.domain.model.DeviceType

/**
 * Immutable UI State representing the Device Discovery Screen.
 */
data class DeviceListState(
    val isLoading: Boolean = true,
    val isScanning: Boolean = false,
    val rawDevices: List<Device> = emptyList(),
    val displayedDevices: List<Device> = emptyList(),
    val searchQuery: String = "",
    val selectedType: DeviceType? = null,
    val favoritesOnly: Boolean = false,
    val errorMessage: String? = null
) : UiState {
    val hasDevices: Boolean get() = displayedDevices.isNotEmpty()
    val totalDeviceCount: Int get() = rawDevices.size
    val favoriteCount: Int get() = rawDevices.count { it.isFavorite }
}

/**
 * User intents and events dispatched to [DeviceListViewModel].
 */
sealed interface DeviceListIntent : UiIntent {
    data object Load : DeviceListIntent
    data object Scan : DeviceListIntent
    data class UpdateSearch(val query: String) : DeviceListIntent
    data class SelectTypeFilter(val type: DeviceType?) : DeviceListIntent
    data class ToggleFavoritesOnly(val enabled: Boolean) : DeviceListIntent
    data class ToggleFavorite(val deviceId: String) : DeviceListIntent
    data class SelectDevice(val device: Device) : DeviceListIntent
    data class SendFile(val device: Device, val fileName: String, val sizeBytes: Long) : DeviceListIntent
    data object DismissError : DeviceListIntent
}

/**
 * One-time side effects dispatched to the native UI (Android Compose / iOS SwiftUI).
 */
sealed interface DeviceListEffect : UiEffect {
    data class ShowToast(val message: String) : DeviceListEffect
    data class OpenSendDialog(val device: Device) : DeviceListEffect
    data class NavigateToTransfer(val transferId: String, val deviceName: String) : DeviceListEffect
}
