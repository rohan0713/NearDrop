package com.rohan.neardrop.data.datasource.local

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Abstraction for local device data caching and user preferences.
 */
interface DeviceLocalDataSource {
    fun observeFavoriteDeviceIds(): Flow<Set<String>>
    suspend fun isFavorite(deviceId: String): Boolean
    suspend fun toggleFavorite(deviceId: String): Boolean
    suspend fun clearFavorites()
}

/**
 * Thread-safe in-memory cache implementation of [DeviceLocalDataSource].
 */
class InMemoryDeviceLocalDataSource : DeviceLocalDataSource {

    private val mutex = Mutex()
    private val _favoriteIds = MutableStateFlow<Set<String>>(setOf("dev-macbook-pro"))

    override fun observeFavoriteDeviceIds(): Flow<Set<String>> {
        return _favoriteIds.asStateFlow()
    }

    override suspend fun isFavorite(deviceId: String): Boolean = mutex.withLock {
        _favoriteIds.value.contains(deviceId)
    }

    override suspend fun toggleFavorite(deviceId: String): Boolean = mutex.withLock {
        val current = _favoriteIds.value
        val newState = if (current.contains(deviceId)) {
            current - deviceId
        } else {
            current + deviceId
        }
        _favoriteIds.value = newState
        newState.contains(deviceId)
    }

    override suspend fun clearFavorites() = mutex.withLock {
        _favoriteIds.value = emptySet()
    }
}
