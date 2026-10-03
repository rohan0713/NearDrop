package com.drop.near.di

import com.drop.near.core.coroutines.CoroutineDispatchers
import com.drop.near.core.coroutines.DefaultCoroutineDispatchers
import com.drop.near.data.datasource.local.DeviceLocalDataSource
import com.drop.near.data.datasource.local.InMemoryDeviceLocalDataSource
import com.drop.near.data.datasource.remote.DeviceRemoteDataSource
import com.drop.near.data.datasource.remote.MockDeviceRemoteDataSource
import com.drop.near.data.mapper.DeviceMapper
import com.drop.near.data.repository.DeviceRepositoryImpl
import com.drop.near.data.repository.TransferRepositoryImpl
import com.drop.near.domain.repository.DeviceRepository
import com.drop.near.domain.repository.TransferRepository
import com.drop.near.domain.usecase.CancelTransferUseCase
import com.drop.near.domain.usecase.FilterDevicesUseCase
import com.drop.near.domain.usecase.InitiateTransferUseCase
import com.drop.near.domain.usecase.ObserveDevicesUseCase
import com.drop.near.domain.usecase.ObserveTransfersUseCase
import com.drop.near.domain.usecase.RefreshDiscoveryUseCase
import com.drop.near.domain.usecase.ToggleFavoriteUseCase
import com.drop.near.presentation.devicelist.DeviceListViewModel
import com.drop.near.presentation.transfer.TransferViewModel

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
