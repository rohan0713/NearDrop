import SwiftUI
import SharedKit

@MainActor
final class ObservableTransferViewModel: ObservableObject {
    @Published private(set) var state: TransferState = TransferState(
        isLoading: false,
        transfers: [],
        errorMessage: nil
    )
    
    private let sharedViewModel: TransferViewModel
    private var stateWatcher: Closeable?
    
    init(sharedViewModel: TransferViewModel = NearDropSdk.shared.container.createTransferViewModel()) {
        self.sharedViewModel = sharedViewModel
        observeState()
    }
    
    deinit {
        stateWatcher?.close()
        sharedViewModel.onCleared()
    }
    
    func onCancelTransfer(id: String) {
        sharedViewModel.onIntent(intent: TransferIntentCancelTransfer(transferId: id))
    }
    
    func onClearFinished() {
        sharedViewModel.onIntent(intent: TransferIntentClearFinished.shared)
    }
    
    private func observeState() {
        stateWatcher = sharedViewModel.watchState { [weak self] newState in
            self?.state = newState
        }
    }
}

struct TransferHistoryView: View {
    @StateObject private var viewModel = ObservableTransferViewModel()
    
    var body: some View {
        NavigationStack {
            Group {
                if viewModel.state.transfers.isEmpty {
                    VStack(spacing: 8) {
                        Image(systemName: "tray")
                            .font(.system(size: 40))
                            .foregroundColor(.secondary)
                        Text("No Transfers Yet")
                            .font(.headline)
                        Text("Initiate a transfer from the NearDrop discovery tab.")
                            .font(.subheadline)
                            .foregroundColor(.secondary)
                    }
                } else {
                    List {
                        ForEach(viewModel.state.transfers, id: \.id) { item in
                            TransferRow(item: item, onCancel: {
                                viewModel.onCancelTransfer(id: item.id)
                            })
                        }
                    }
                    .listStyle(.insetGrouped)
                }
            }
            .navigationTitle("Transfer History")
            .toolbar {
                if viewModel.state.completedCount > 0 {
                    ToolbarItem(placement: .navigationBarTrailing) {
                        Button("Clear Finished") {
                            viewModel.onClearFinished()
                        }
                    }
                }
            }
        }
    }
}

struct TransferRow: View {
    let item: TransferItem
    let onCancel: () -> Void
    
    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            HStack {
                VStack(alignment: .leading, spacing: 2) {
                    Text(item.fileName)
                        .font(.headline)
                    Text("To: \(item.device.name)")
                        .font(.subheadline)
                        .foregroundColor(.secondary)
                }
                Spacer()
                statusBadge(status: item.status)
                
                if !item.status.isFinished {
                    Button(action: onCancel) {
                        Image(systemName: "xmark.circle.fill")
                            .foregroundColor(.red)
                    }
                    .buttonStyle(.borderless)
                }
            }
            
            if let inProgress = item.status as? TransferStatusInProgress {
                ProgressView(value: Double(inProgress.progress), total: 1.0)
                    .tint(.blue)
                HStack {
                    Text("\(item.progressPercent)%")
                        .font(.caption.bold())
                    Spacer()
                    Text("\(formatBytes(item.bytesTransferred)) / \(formatBytes(item.fileSizeBytes))")
                        .font(.caption)
                        .foregroundColor(.secondary)
                }
            }
        }
        .padding(.vertical, 4)
    }
    
    @ViewBuilder
    private func statusBadge(status: TransferStatus) -> some View {
        let (label, color): (String, Color) = {
            if status is TransferStatusQueued {
                return ("Queued", .gray)
            } else if status is TransferStatusInProgress {
                return ("Sending", .blue)
            } else if status is TransferStatusCompleted {
                return ("Completed", .green)
            } else if status is TransferStatusFailed {
                return ("Failed", .red)
            } else {
                return ("Cancelled", .gray)
            }
        }()
        
        Text(label)
            .font(.caption2.bold())
            .foregroundColor(color)
            .padding(.horizontal, 6)
            .padding(.vertical, 3)
            .background(color.opacity(0.15))
            .cornerRadius(6)
    }
    
    private func formatBytes(_ bytes: Int64) -> String {
        let mb = Double(bytes) / (1024.0 * 1024.0)
        return String(format: "%.1f MB", mb)
    }
}
