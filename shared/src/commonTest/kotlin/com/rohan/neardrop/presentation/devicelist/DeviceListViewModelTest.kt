package com.rohan.neardrop.presentation.devicelist

import com.rohan.neardrop.core.base.None
import com.rohan.neardrop.core.result.AppResult
import com.rohan.neardrop.domain.model.Device
import com.rohan.neardrop.domain.model.DeviceType
import com.rohan.neardrop.domain.repository.DeviceRepository
import com.rohan.neardrop.domain.repository.TransferRepository
import com.rohan.neardrop.domain.usecase.FilterDevicesUseCase
import com.rohan.neardrop.domain.usecase.InitiateTransferUseCase
import com.rohan.neardrop.domain.usecase.ObserveDevicesUseCase
import com.rohan.neardrop.domain.usecase.RefreshDiscoveryUseCase
import com.rohan.neardrop.domain.usecase.ToggleFavoriteUseCase
import com.rohan.neardrop.test.TestCoroutineDispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DeviceListViewModelTest {

    private val dispatchers = TestCoroutineDispatchers()

    private val fakeDevices = MutableStateFlow(
        listOf(
            Device("d1", "MacBook Pro", DeviceType.LAPTOP, "192.168.1.10", 5000, rssi = -50, isFavorite = false),
            Device("d2", "Pixel 9", DeviceType.PHONE, "192.168.1.11", 5000, rssi = -60, isFavorite = true)
        )
    )

    private val fakeDeviceRepository = object : DeviceRepository {
        override fun observeDevices(): Flow<List<Device>> = fakeDevices.asStateFlow()

        override suspend fun refreshDiscovery(): AppResult<Unit> = AppResult.Success(Unit)

        override suspend fun getDeviceById(id: String): AppResult<Device> {
            val dev = fakeDevices.value.find { it.id == id }
            return if (dev != null) AppResult.Success(dev) else AppResult.Error(com.rohan.neardrop.core.result.AppError.DeviceTransfer.DeviceNotFound(id))
        }

        override suspend fun toggleFavorite(deviceId: String): AppResult<Boolean> {
            var isFav = false
            fakeDevices.value = fakeDevices.value.map {
                if (it.id == deviceId) {
                    isFav = !it.isFavorite
                    it.copy(isFavorite = isFav)
                } else it
            }
            return AppResult.Success(isFav)
        }
    }

    private val fakeTransferRepository = object : TransferRepository {
        override fun observeTransfers(): Flow<List<com.rohan.neardrop.domain.model.TransferItem>> =
            MutableStateFlow<List<com.rohan.neardrop.domain.model.TransferItem>>(emptyList()).asStateFlow()

        override suspend fun initiateTransfer(
            targetDevice: Device,
            fileName: String,
            fileSizeBytes: Long
        ): AppResult<com.rohan.neardrop.domain.model.TransferItem> {
            return AppResult.Success(
                com.rohan.neardrop.domain.model.TransferItem(
                    id = "test-tx-1",
                    device = targetDevice,
                    fileName = fileName,
                    fileSizeBytes = fileSizeBytes
                )
            )
        }

        override suspend fun cancelTransfer(transferId: String): AppResult<Unit> = AppResult.Success(Unit)

        override suspend fun clearFinishedTransfers(): AppResult<Unit> = AppResult.Success(Unit)
    }

    private fun createViewModel(): DeviceListViewModel {
        return DeviceListViewModel(
            observeDevicesUseCase = ObserveDevicesUseCase(fakeDeviceRepository, dispatchers),
            refreshDiscoveryUseCase = RefreshDiscoveryUseCase(fakeDeviceRepository, dispatchers),
            filterDevicesUseCase = FilterDevicesUseCase(),
            toggleFavoriteUseCase = ToggleFavoriteUseCase(fakeDeviceRepository, dispatchers),
            initiateTransferUseCase = InitiateTransferUseCase(fakeTransferRepository, dispatchers),
            dispatchers = dispatchers
        )
    }

    @Test
    fun initial_load_populates_devices_and_stops_loading() = runTest {
        val viewModel = createViewModel()
        val state = viewModel.state.value

        assertFalse(state.isLoading)
        assertEquals(2, state.displayedDevices.size)
        // Favorite item should be ordered first
        assertTrue(state.displayedDevices.first().isFavorite)
        assertEquals("Pixel 9", state.displayedDevices.first().name)
    }

    @Test
    fun search_query_filters_displayed_devices() = runTest {
        val viewModel = createViewModel()
        viewModel.onIntent(DeviceListIntent.UpdateSearch("macbook"))

        val state = viewModel.state.value
        assertEquals(1, state.displayedDevices.size)
        assertEquals("MacBook Pro", state.displayedDevices.first().name)
    }

    @Test
    fun type_filter_restricts_devices() = runTest {
        val viewModel = createViewModel()
        viewModel.onIntent(DeviceListIntent.SelectTypeFilter(DeviceType.PHONE))

        val state = viewModel.state.value
        assertEquals(1, state.displayedDevices.size)
        assertEquals(DeviceType.PHONE, state.displayedDevices.first().type)
    }

    @Test
    fun send_file_intent_emits_navigation_effect() = runTest {
        val viewModel = createViewModel()
        val target = fakeDevices.value.first()

        viewModel.onIntent(DeviceListIntent.SendFile(target, "sample.jpg", 1024L))

        val effect = viewModel.effect.first()
        assertTrue(effect is DeviceListEffect.NavigateToTransfer)
        assertEquals("test-tx-1", effect.transferId)
    }
}
