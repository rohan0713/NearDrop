package com.rohan.neardrop.data.repository

import com.rohan.neardrop.core.result.AppError
import com.rohan.neardrop.core.result.AppResult
import com.rohan.neardrop.data.datasource.local.DeviceLocalDataSource
import com.rohan.neardrop.data.datasource.remote.DeviceRemoteDataSource
import com.rohan.neardrop.data.mapper.DeviceMapper
import com.rohan.neardrop.domain.model.Device
import com.rohan.neardrop.domain.repository.DeviceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first

/**
 * Concrete implementation of [DeviceRepository].
 * Coordinates remote network discovery with local caching and data mapping.
 */
class DeviceRepositoryImpl(
    private val remoteDataSource: DeviceRemoteDataSource,
    private val localDataSource: DeviceLocalDataSource,
    private val mapper: DeviceMapper
) : DeviceRepository {

    override fun observeDevices(): Flow<List<Device>> {
        return combine(
            remoteDataSource.observeDiscoveredDevices(),
            localDataSource.observeFavoriteDeviceIds()
        ) { remoteList, favoriteIds ->
            remoteList.map { dto ->
                val entity = mapper.map(dto)
                entity.copy(isFavorite = favoriteIds.contains(entity.id))
            }
        }
    }

    override suspend fun refreshDiscovery(): AppResult<Unit> {
        return when (val result = remoteDataSource.triggerDiscoveryScan()) {
            is AppResult.Success -> AppResult.Success(Unit)
            is AppResult.Error -> result
        }
    }

    override suspend fun getDeviceById(id: String): AppResult<Device> {
        val currentDevices = observeDevices().first()
        val found = currentDevices.find { it.id == id }
        return if (found != null) {
            AppResult.Success(found)
        } else {
            AppResult.Error(AppError.DeviceTransfer.DeviceNotFound(id))
        }
    }

    override suspend fun toggleFavorite(deviceId: String): AppResult<Boolean> {
        val isNowFavorite = localDataSource.toggleFavorite(deviceId)
        return AppResult.Success(isNowFavorite)
    }
}
