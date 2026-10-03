package com.drop.near.data.repository

import com.drop.near.core.result.AppError
import com.drop.near.core.result.AppResult
import com.drop.near.data.datasource.local.DeviceLocalDataSource
import com.drop.near.data.datasource.remote.DeviceRemoteDataSource
import com.drop.near.data.mapper.DeviceMapper
import com.drop.near.domain.model.Device
import com.drop.near.domain.repository.DeviceRepository
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
