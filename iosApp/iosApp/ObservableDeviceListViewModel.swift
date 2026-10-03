import Foundation
import Combine
import SharedKit

/**
 * Adapter Pattern (OOP):
 * Adapts the shared Kotlin [DeviceListViewModel] to SwiftUI's [ObservableObject] paradigm.
 * Encapsulates Kotlin coroutine flows and exposes native Swift @Published properties.
 */
@MainActor
final class ObservableDeviceListViewModel: ObservableObject {
    
    @Published private(set) var state: DeviceListState = DeviceListState(
        isLoading: true,
        isScanning: false,
        rawDevices: [],
        displayedDevices: [],
        searchQuery: "",
        selectedType: nil,
        favoritesOnly: false,
        errorMessage: nil
    )
    
    @Published var alertMessage: String? = nil
    @Published var selectedDeviceForSend: Device? = nil
    
    private let sharedViewModel: DeviceListViewModel
    private var stateWatcher: Closeable?
    private var effectWatcher: Closeable?
    
    init(sharedViewModel: DeviceListViewModel = NearDropSdk.shared.container.createDeviceListViewModel()) {
        self.sharedViewModel = sharedViewModel
        observeState()
        observeEffects()
    }
    
    deinit {
        stateWatcher?.close()
        effectWatcher?.close()
        sharedViewModel.onCleared()
    }
    
    // MARK: - Intent Dispatchers (Forwarding to KMP)
    
    func onScanTapped() {
        sharedViewModel.onIntent(intent: DeviceListIntentScan.shared)
    }
    
    func onSearchQueryChanged(_ query: String) {
        sharedViewModel.onIntent(intent: DeviceListIntentUpdateSearch(query: query))
    }
    
    func onFilterTypeSelected(_ type: DeviceType?) {
        sharedViewModel.onIntent(intent: DeviceListIntentSelectTypeFilter(type: type))
    }
    
    func onToggleFavoritesOnly(_ enabled: Bool) {
        sharedViewModel.onIntent(intent: DeviceListIntentToggleFavoritesOnly(enabled: enabled))
    }
    
    func onToggleFavorite(deviceId: String) {
        sharedViewModel.onIntent(intent: DeviceListIntentToggleFavorite(deviceId: deviceId))
    }
    
    func onDeviceSelected(_ device: Device) {
        sharedViewModel.onIntent(intent: DeviceListIntentSelectDevice(device: device))
    }
    
    func onSendFile(to device: Device, fileName: String, sizeBytes: Int64) {
        sharedViewModel.onIntent(
            intent: DeviceListIntentSendFile(device: device, fileName: fileName, sizeBytes: sizeBytes)
        )
    }
    
    // MARK: - State & Effect Observation
    
    private func observeState() {
        stateWatcher = sharedViewModel.watchState { [weak self] newState in
            self?.state = newState
        }
    }
    
    private func observeEffects() {
        effectWatcher = sharedViewModel.watchEffect { [weak self] effect in
            guard let self = self else { return }
            if let toast = effect as? DeviceListEffectShowToast {
                self.alertMessage = toast.message
            } else if let openDialog = effect as? DeviceListEffectOpenSendDialog {
                self.selectedDeviceForSend = openDialog.device
            }
        }
    }
}
