package com.rohan.neardrop.di

import com.rohan.neardrop.core.coroutines.CoroutineDispatchers
import com.rohan.neardrop.core.coroutines.DefaultCoroutineDispatchers
import com.rohan.neardrop.data.datasource.local.DeviceLocalDataSource
import com.rohan.neardrop.data.datasource.local.InMemoryDeviceLocalDataSource
import com.rohan.neardrop.data.datasource.remote.DeviceRemoteDataSource
import com.rohan.neardrop.data.datasource.remote.MockDeviceRemoteDataSource
import com.rohan.neardrop.data.mapper.DeviceMapper
import com.rohan.neardrop.data.repository.DeviceRepositoryImpl
import com.rohan.neardrop.data.repository.TransferRepositoryImpl
import com.rohan.neardrop.domain.repository.DeviceRepository
import com.rohan.neardrop.domain.repository.TransferRepository
import com.rohan.neardrop.domain.usecase.CancelTransferUseCase
import com.rohan.neardrop.domain.usecase.FilterDevicesUseCase
import com.rohan.neardrop.domain.usecase.InitiateTransferUseCase
import com.rohan.neardrop.domain.usecase.ObserveDevicesUseCase
import com.rohan.neardrop.domain.usecase.ObserveTransfersUseCase
import com.rohan.neardrop.domain.usecase.RefreshDiscoveryUseCase
import com.rohan.neardrop.domain.usecase.ToggleFavoriteUseCase
import com.rohan.neardrop.presentation.devicelist.DeviceListViewModel
import com.rohan.neardrop.presentation.transfer.TransferViewModel

/**
 * Dependency Inversion Principle & Composition Root:
 * High-level interface exposing core application dependencies.
 */
interface AppContainer {
    val dispatchers: CoroutineDispatchers
    val deviceRepository: DeviceRepository
    val transferRepository: TransferRepository

    // Use cases
    val observeDevicesUseCase: ObserveDevicesUseCase
    val refreshDiscoveryUseCase: RefreshDiscoveryUseCase
    val filterDevicesUseCase: FilterDevicesUseCase
    val toggleFavoriteUseCase: ToggleFavoriteUseCase
    val initiateTransferUseCase: InitiateTransferUseCase
    val observeTransfersUseCase: ObserveTransfersUseCase
    val cancelTransferUseCase: CancelTransferUseCase

    // ViewModel Factory methods
    fun createDeviceListViewModel(): DeviceListViewModel
    fun createTransferViewModel(): TransferViewModel
}

/**
 * Default production implementation of [AppContainer] managing dependency graph lifecycles.
 */
class DefaultAppContainer(
    override val dispatchers: CoroutineDispatchers = DefaultCoroutineDispatchers(),
    remoteDataSource: DeviceRemoteDataSource? = null,
    localDataSource: DeviceLocalDataSource? = null
) : AppContainer {

    private val deviceMapper: DeviceMapper by lazy {
        DeviceMapper()
    }

    private val actualRemoteDataSource: DeviceRemoteDataSource by lazy {
        remoteDataSource ?: MockDeviceRemoteDataSource()
    }

    private val actualLocalDataSource: DeviceLocalDataSource by lazy {
        localDataSource ?: InMemoryDeviceLocalDataSource()
    }

    override val deviceRepository: DeviceRepository by lazy {
        DeviceRepositoryImpl(
            remoteDataSource = actualRemoteDataSource,
            localDataSource = actualLocalDataSource,
            mapper = deviceMapper
        )
    }

    override val transferRepository: TransferRepository by lazy {
        TransferRepositoryImpl(
            dispatchers = dispatchers
        )
    }

    override val observeDevicesUseCase: ObserveDevicesUseCase by lazy {
        ObserveDevicesUseCase(deviceRepository, dispatchers)
    }

    override val refreshDiscoveryUseCase: RefreshDiscoveryUseCase by lazy {
        RefreshDiscoveryUseCase(deviceRepository, dispatchers)
    }

    override val filterDevicesUseCase: FilterDevicesUseCase by lazy {
        FilterDevicesUseCase()
    }

    override val toggleFavoriteUseCase: ToggleFavoriteUseCase by lazy {
        ToggleFavoriteUseCase(deviceRepository, dispatchers)
    }

    override val initiateTransferUseCase: InitiateTransferUseCase by lazy {
        InitiateTransferUseCase(transferRepository, dispatchers)
    }

    override val observeTransfersUseCase: ObserveTransfersUseCase by lazy {
        ObserveTransfersUseCase(transferRepository, dispatchers)
    }

    override val cancelTransferUseCase: CancelTransferUseCase by lazy {
        CancelTransferUseCase(transferRepository, dispatchers)
    }

    override fun createDeviceListViewModel(): DeviceListViewModel {
        return DeviceListViewModel(
            observeDevicesUseCase = observeDevicesUseCase,
            refreshDiscoveryUseCase = refreshDiscoveryUseCase,
            filterDevicesUseCase = filterDevicesUseCase,
            toggleFavoriteUseCase = toggleFavoriteUseCase,
            initiateTransferUseCase = initiateTransferUseCase,
            dispatchers = dispatchers
        )
    }

    override fun createTransferViewModel(): TransferViewModel {
        return TransferViewModel(
            observeTransfersUseCase = observeTransfersUseCase,
            cancelTransferUseCase = cancelTransferUseCase,
            transferRepository = transferRepository,
            dispatchers = dispatchers
        )
    }
}

/**
 * Global singleton access point for cross-platform consumption.
 */
object NearDropSdk {
    private var containerInstance: AppContainer? = null

    val container: AppContainer
        get() = containerInstance ?: DefaultAppContainer().also { containerInstance = it }

    fun initialize(customContainer: AppContainer? = null) {
        containerInstance = customContainer ?: DefaultAppContainer()
    }
}
