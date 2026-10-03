import SwiftUI
import SharedKit

@main
struct iOSApp: App {
    init() {
        NearDropSdk.shared.initialize(customContainer: nil)
    }
    
    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
