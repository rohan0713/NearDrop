package com.drop.near.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DeviceDto(
    @SerialName("id") val id: String,
    @SerialName("name") val name: String,
    @SerialName("type") val type: String,
    @SerialName("ip_address") val ipAddress: String,
    @SerialName("port") val port: Int,
    @SerialName("rssi") val rssi: Int = -65,
    @SerialName("last_seen") val lastSeenEpochMs: Long = 0L
)

@Serializable
data class TransferDto(
    @SerialName("id") val id: String,
    @SerialName("target_device") val targetDevice: DeviceDto,
    @SerialName("file_name") val fileName: String,
    @SerialName("file_size_bytes") val fileSizeBytes: Long,
    @SerialName("bytes_transferred") val bytesTransferred: Long,
    @SerialName("status") val status: String,
    @SerialName("direction") val direction: String,
    @SerialName("created_at") val createdAtEpochMs: Long
)
