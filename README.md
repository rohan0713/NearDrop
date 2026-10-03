# NearDrop - Kotlin Multiplatform (Clean Architecture & OOP)

A modern, production-grade Kotlin Multiplatform (KMP) application designed with **No Shared UI**. The business logic, state machines, domain models, and data pipelines are 100% shared in Kotlin, while the user interfaces are implemented purely natively using **Jetpack Compose on Android** and **SwiftUI on iOS**.

---

## 🏛 Architecture Overview

The codebase adheres strictly to **Clean Architecture** (Robert C. Martin) and core **Object-Oriented Programming (OOP)** principles:

```
┌────────────────────────────────────────────────────────────────────────┐
│                        Native UI (No Shared UI)                        │
│   Android (Jetpack Compose)                 iOS (Native SwiftUI)       │
└───────────────────▲──────────────────────────────────▲─────────────────┘
                    │                                  │
                    │         Presentation Layer       │
                    │   - BaseViewModel (MVI pattern)  │
                    │   - Observable StateFlow / Flow  │
                    │   - Swift ViewModels (Adapters)  │
                    │                                  │
                    │            Domain Layer          │
                    │   - Pure Business Entities       │
                    │   - Repository Interfaces        │
                    │   - Interactors / Use Cases      │
                    │                                  │
                    │             Data Layer           │
                    │   - Repository Implementations   │
                    │   - Remote & Local DataSources   │
                    │   - Two-Way Data Mappers         │
                    └──────────────────────────────────┘
```

---

## 🧩 Clean Architecture Layers

### 1. `core`
- **Result & Error Handling**: Polymorphic sealed hierarchies [`AppResult<T>`](shared/src/commonMain/kotlin/com/drop/near/core/result/AppResult.kt) (`Success`, `Error`) and [`AppError`](shared/src/commonMain/kotlin/com/drop/near/core/result/AppError.kt) (`Network`, `Storage`, `Validation`, `DeviceTransfer`).
- **Concurrency**: [`CoroutineDispatchers`](shared/src/commonMain/kotlin/com/drop/near/core/coroutines/CoroutineDispatchers.kt) interface enabling Dependency Inversion and seamless test swapping.
- **Base Components**:
  - [`BaseUseCase<Input, Output>`](shared/src/commonMain/kotlin/com/drop/near/core/base/BaseUseCase.kt) & [`BaseFlowUseCase`](shared/src/commonMain/kotlin/com/drop/near/core/base/BaseUseCase.kt): Encapsulates background thread execution and exception recovery.
  - [`BaseViewModel<State, Intent, Effect>`](shared/src/commonMain/kotlin/com/drop/near/core/base/BaseViewModel.kt): MVI state machine with private mutable state and public immutable observation. Includes `watchState` and `watchEffect` for native Swift interop.

### 2. `domain` (Pure Kotlin, zero platform imports)
- **Entities**: [`Device`](shared/src/commonMain/kotlin/com/drop/near/domain/model/Device.kt), [`TransferItem`](shared/src/commonMain/kotlin/com/drop/near/domain/model/Transfer.kt), [`TransferStatus`](shared/src/commonMain/kotlin/com/drop/near/domain/model/Transfer.kt). Includes domain-level business validation and signal calculations.
- **Repository Contracts**: [`DeviceRepository`](shared/src/commonMain/kotlin/com/drop/near/domain/repository/DeviceRepository.kt), [`TransferRepository`](shared/src/commonMain/kotlin/com/drop/near/domain/repository/TransferRepository.kt).
- **Use Cases (Interactors)**:
  - [`ObserveDevicesUseCase`](shared/src/commonMain/kotlin/com/drop/near/domain/usecase/ObserveDevicesUseCase.kt)
  - [`RefreshDiscoveryUseCase`](shared/src/commonMain/kotlin/com/drop/near/domain/usecase/RefreshDiscoveryUseCase.kt)
  - [`FilterDevicesUseCase`](shared/src/commonMain/kotlin/com/drop/near/domain/usecase/FilterDevicesUseCase.kt)
  - [`ToggleFavoriteUseCase`](shared/src/commonMain/kotlin/com/drop/near/domain/usecase/ToggleFavoriteUseCase.kt)
  - [`InitiateTransferUseCase`](shared/src/commonMain/kotlin/com/drop/near/domain/usecase/InitiateTransferUseCase.kt)
  - [`ObserveTransfersUseCase`](shared/src/commonMain/kotlin/com/drop/near/domain/usecase/ObserveTransfersUseCase.kt)
  - [`CancelTransferUseCase`](shared/src/commonMain/kotlin/com/drop/near/domain/usecase/CancelTransferUseCase.kt)

### 3. `data`
- **Data Transfer Objects (DTOs)**: [`DeviceDto`](shared/src/commonMain/kotlin/com/drop/near/data/model/DeviceDto.kt), [`TransferDto`](shared/src/commonMain/kotlin/com/drop/near/data/model/DeviceDto.kt).
- **Data Mappers**: [`DeviceMapper`](shared/src/commonMain/kotlin/com/drop/near/data/mapper/DeviceMapper.kt), [`TransferMapper`](shared/src/commonMain/kotlin/com/drop/near/data/mapper/TransferMapper.kt) implementing generic [`BiDirectionalMapper`](shared/src/commonMain/kotlin/com/drop/near/data/mapper/Mapper.kt).
- **Data Sources**:
  - Remote: [`DeviceRemoteDataSource`](shared/src/commonMain/kotlin/com/drop/near/data/datasource/remote/DeviceRemoteDataSource.kt) with [`MockDeviceRemoteDataSource`](shared/src/commonMain/kotlin/com/drop/near/data/datasource/remote/DeviceRemoteDataSource.kt).
  - Local: [`DeviceLocalDataSource`](shared/src/commonMain/kotlin/com/drop/near/data/datasource/local/DeviceLocalDataSource.kt) with [`InMemoryDeviceLocalDataSource`](shared/src/commonMain/kotlin/com/drop/near/data/datasource/local/DeviceLocalDataSource.kt).
- **Repository Implementations**:
  - [`DeviceRepositoryImpl`](shared/src/commonMain/kotlin/com/drop/near/data/repository/DeviceRepositoryImpl.kt)
  - [`TransferRepositoryImpl`](shared/src/commonMain/kotlin/com/drop/near/data/repository/TransferRepositoryImpl.kt)

### 4. `presentation` (Shared State Holders)
- [`DeviceListViewModel`](shared/src/commonMain/kotlin/com/drop/near/presentation/devicelist/DeviceListViewModel.kt) & [`DeviceListContract`](shared/src/commonMain/kotlin/com/drop/near/presentation/devicelist/DeviceListContract.kt)
- [`TransferViewModel`](shared/src/commonMain/kotlin/com/drop/near/presentation/transfer/TransferViewModel.kt) & [`TransferContract`](shared/src/commonMain/kotlin/com/drop/near/presentation/transfer/TransferContract.kt)

### 5. `di` (Dependency Injection / Composition Root)
- [`AppContainer`](shared/src/commonMain/kotlin/com/drop/near/di/AppContainer.kt) & [`DefaultAppContainer`](shared/src/commonMain/kotlin/com/drop/near/di/AppContainer.kt)
- [`NearDropSdk`](shared/src/commonMain/kotlin/com/drop/near/di/AppContainer.kt) global initialization point.

---

## 🎨 Native Platforms (No Shared UI)

### Android (`androidApp`) - Native Jetpack Compose
- Modern Material 3 theming ([`Theme.kt`](androidApp/src/main/kotlin/com/drop/near/ui/theme/Theme.kt), [`Color.kt`](androidApp/src/main/kotlin/com/drop/near/ui/theme/Color.kt)).
- Native UI screens:
  - [`DeviceListScreen.kt`](androidApp/src/main/kotlin/com/drop/near/ui/devicelist/DeviceListScreen.kt)
  - [`TransferHistoryScreen.kt`](androidApp/src/main/kotlin/com/drop/near/ui/transfer/TransferHistoryScreen.kt)
- Lifecycle-aware flow collection via `collectAsStateWithLifecycle()`.
- Single-event effect handling with `LaunchedEffect`.

### iOS (`iosApp`) - Native SwiftUI
- Native SwiftUI views:
  - [`DeviceListView.swift`](iosApp/iosApp/DeviceListView.swift)
  - [`TransferHistoryView.swift`](iosApp/iosApp/TransferHistoryView.swift)
  - [`ContentView.swift`](iosApp/iosApp/ContentView.swift)
- Adapter Pattern: [`ObservableDeviceListViewModel.swift`](iosApp/iosApp/ObservableDeviceListViewModel.swift) bridges Kotlin MVI StateFlow to `@Published` SwiftUI states.
- Xcode project integrates automated Gradle build phase: `embedAndSignAppleFrameworkForXcode`.

---

## 💡 Applied OOP Principles

| OOP Concept | Implementation in Code |
| :--- | :--- |
| **Encapsulation** | `_state: MutableStateFlow` and `_effect: Channel` are `private`; exposed exclusively as read-only `state: StateFlow` and `effect: Flow`. |
| **Abstraction** | Interfaces for `DeviceRepository`, `TransferRepository`, `DeviceRemoteDataSource`, `CoroutineDispatchers`, `AppContainer`. |
| **Polymorphism** | `AppResult` (`Success` vs `Error`), `TransferStatus` (`Queued`, `InProgress`, `Completed`, `Failed`, `Cancelled`), `Mapper<F, T>`. |
| **Inheritance** | `BaseViewModel`, `BaseUseCase`, `BaseFlowUseCase` implementing Template Method design pattern. |
| **SOLID - SRP** | Distinct Use Cases (`FilterDevicesUseCase`, `InitiateTransferUseCase`, etc.) each with a single business responsibility. |
| **SOLID - OCP** | Data sources and repositories are open for extension without modifying domain consumers. |
| **SOLID - LSP** | `DeviceRepositoryImpl` and test mocks can substitute `DeviceRepository` transparently. |
| **SOLID - ISP** | Granular data source interfaces (`DeviceRemoteDataSource`, `DeviceLocalDataSource`). |
| **SOLID - DIP** | High-level Use Cases depend only on repository abstractions; implementations depend on abstractions. |

---

## 🚀 Building & Testing

### Run All Unit Tests
```bash
JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home ./gradlew test
```

### Build Android Debug APK
```bash
JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home ./gradlew :androidApp:assembleDebug
```
Output: `androidApp/build/outputs/apk/debug/androidApp-debug.apk`

### Build iOS Shared Framework
```bash
JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home ./gradlew :shared:linkDebugFrameworkIosSimulatorArm64
```
Output: `shared/build/bin/iosSimulatorArm64/debugFramework/SharedKit.framework`

### Build iOS Application
```bash
xcodebuild -project iosApp/iosApp.xcodeproj -scheme iosApp -destination 'generic/platform=iOS Simulator' build CODE_SIGN_IDENTITY="" CODE_SIGNING_REQUIRED=NO CODE_SIGNING_ALLOWED=NO
```
