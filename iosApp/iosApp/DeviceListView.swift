import SwiftUI
import SharedKit

struct DeviceListView: View {
    @StateObject private var viewModel = ObservableDeviceListViewModel()
    @State private var showingSendSheet = false
    @State private var fileName = "DocumentArchive.zip"
    @State private var fileSizeMb = "30"
    
    var body: some View {
        NavigationStack {
            VStack(spacing: 0) {
                // Filter bar
                filterHeader
                    .padding(.horizontal)
                    .padding(.vertical, 8)
                
                // Device list
                if viewModel.state.isLoading {
                    Spacer()
                    ProgressView("Searching for nearby devices...")
                    Spacer()
                } else if viewModel.state.displayedDevices.isEmpty {
                    emptyStateView
                } else {
                    List {
                        ForEach(viewModel.state.displayedDevices, id: \.id) { device in
                            DeviceRow(
                                device: device,
                                onSelect: {
                                    viewModel.selectedDeviceForSend = device
                                    showingSendSheet = true
                                },
                                onToggleFavorite: {
                                    viewModel.onToggleFavorite(deviceId: device.id)
                                }
                            )
                        }
                    }
                    .listStyle(.insetGrouped)
                }
            }
            .navigationTitle("NearDrop")
            .searchable(
                text: Binding(
                    get: { viewModel.state.searchQuery },
                    set: { viewModel.onSearchQueryChanged($0) }
                ),
                prompt: "Search by name or IP"
            )
            .toolbar {
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button(action: { viewModel.onScanTapped() }) {
                        if viewModel.state.isScanning {
                            ProgressView()
                        } else {
                            Image(systemName: "arrow.clockwise")
                        }
                    }
                    .disabled(viewModel.state.isScanning)
                }
            }
            .alert(item: Binding(
                get: { viewModel.alertMessage.map { AlertWrapper(message: $0) } },
                set: { viewModel.alertMessage = $0?.message }
            )) { wrapper in
                Alert(title: Text("Notice"), message: Text(wrapper.message), dismissButton: .default(Text("OK")))
            }
            .sheet(isPresented: $showingSendSheet) {
                if let target = viewModel.selectedDeviceForSend {
                    sendSheetContent(target: target)
                }
            }
        }
    }
    
    private var filterHeader: some View {
        HStack(spacing: 8) {
            Button(action: {
                viewModel.onFilterTypeSelected(nil)
            }) {
                Text("All (\(viewModel.state.totalDeviceCount))")
                    .font(.caption.bold())
                    .padding(.horizontal, 12)
                    .padding(.vertical, 6)
                    .background(viewModel.state.selectedType == nil ? Color.blue : Color(.systemGray5))
                    .foregroundColor(viewModel.state.selectedType == nil ? .white : .primary)
                    .cornerRadius(12)
            }
            
            Button(action: {
                viewModel.onFilterTypeSelected(DeviceType.phone)
            }) {
                Label("Phones", systemImage: "iphone")
                    .font(.caption.bold())
                    .padding(.horizontal, 10)
                    .padding(.vertical, 6)
                    .background(viewModel.state.selectedType == DeviceType.phone ? Color.blue : Color(.systemGray5))
                    .foregroundColor(viewModel.state.selectedType == DeviceType.phone ? .white : .primary)
                    .cornerRadius(12)
            }
            
            Button(action: {
                viewModel.onFilterTypeSelected(DeviceType.laptop)
            }) {
                Label("Laptops", systemImage: "laptopcomputer")
                    .font(.caption.bold())
                    .padding(.horizontal, 10)
                    .padding(.vertical, 6)
                    .background(viewModel.state.selectedType == DeviceType.laptop ? Color.blue : Color(.systemGray5))
                    .foregroundColor(viewModel.state.selectedType == DeviceType.laptop ? .white : .primary)
                    .cornerRadius(12)
            }
            
            Spacer()
        }
    }
    
    private var emptyStateView: some View {
        VStack(spacing: 12) {
            Spacer()
            Image(systemName: "bonjour")
                .font(.system(size: 48))
                .foregroundColor(.secondary)
            Text("No Devices Found")
                .font(.headline)
            Text("Make sure target devices are connected to the same Wi-Fi network.")
                .font(.subheadline)
                .foregroundColor(.secondary)
                .multilineTextAlignment(.center)
                .padding(.horizontal, 32)
            Button("Scan Again") {
                viewModel.onScanTapped()
            }
            .buttonStyle(.borderedProminent)
            .padding(.top, 8)
            Spacer()
        }
    }
    
    private func sendSheetContent(target: Device) -> some View {
        NavigationStack {
            Form {
                Section("Target Device") {
                    LabeledContent("Device Name", value: target.name)
                    LabeledContent("IP Address", value: "\(target.ipAddress):\(target.port)")
                }
                
                Section("Payload Simulation") {
                    TextField("File Name", text: $fileName)
                    TextField("Size (MB)", text: $fileSizeMb)
                        .keyboardType(.numberPad)
                }
            }
            .navigationTitle("Send File")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Cancel") { showingSendSheet = false }
                }
                ToolbarItem(placement: .confirmationAction) {
                    Button("Transfer") {
                        let sizeBytes = (Int64(fileSizeMb) ?? 20) * 1024 * 1024
                        viewModel.onSendFile(to: target, fileName: fileName, sizeBytes: sizeBytes)
                        showingSendSheet = false
                    }
                    .bold()
                }
            }
        }
        .presentationDetents([.medium])
    }
}

struct DeviceRow: View {
    let device: Device
    let onSelect: () -> Void
    let onToggleFavorite: () -> Void
    
    var body: some View {
        HStack(spacing: 14) {
            ZStack {
                Circle()
                    .fill(Color.blue.opacity(0.12))
                    .frame(width: 44, height: 44)
                Image(systemName: iconName(for: device.type))
                    .foregroundColor(.blue)
                    .font(.system(size: 20))
            }
            
            VStack(alignment: .leading, spacing: 3) {
                Text(device.name)
                    .font(.headline)
                HStack(spacing: 6) {
                    Text("\(device.ipAddress):\(device.port)")
                        .font(.caption)
                        .foregroundColor(.secondary)
                    signalPill(signal: device.signalStrength)
                }
            }
            
            Spacer()
            
            Button(action: onToggleFavorite) {
                Image(systemName: device.isFavorite ? "star.fill" : "star")
                    .foregroundColor(device.isFavorite ? .yellow : .secondary)
            }
            .buttonStyle(.borderless)
            
            Button(action: onSelect) {
                Image(systemName: "paperplane.fill")
                    .foregroundColor(.blue)
            }
            .buttonStyle(.borderless)
        }
        .padding(.vertical, 4)
    }
    
    private func iconName(for type: DeviceType) -> String {
        switch type {
        case .phone: return "iphone"
        case .tablet: return "ipad"
        case .laptop: return "laptopcomputer"
        case .desktop: return "desktopcomputer"
        default: return "network"
        }
    }
    
    @ViewBuilder
    private func signalPill(signal: SignalStrength) -> some View {
        let (label, color): (String, Color) = {
            switch signal {
            case .excellent: return ("Strong", .green)
            case .good: return ("Good", .blue)
            case .fair: return ("Fair", .orange)
            case .poor: return ("Weak", .red)
            default: return ("--", .gray)
            }
        }()
        
        Text(label)
            .font(.system(size: 9, weight: .bold))
            .foregroundColor(color)
            .padding(.horizontal, 5)
            .padding(.vertical, 2)
            .background(color.opacity(0.15))
            .cornerRadius(4)
    }
}

struct AlertWrapper: Identifiable {
    let id = UUID()
    let message: String
}
