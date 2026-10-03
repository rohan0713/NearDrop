import SwiftUI

struct ContentView: View {
    var body: some View {
        TabView {
            DeviceListView()
                .tabItem {
                    Label("Nearby", systemImage: "wave.3.forward")
                }
            
            TransferHistoryView()
                .tabItem {
                    Label("Transfers", systemImage: "arrow.up.arrow.down")
                }
        }
    }
}
