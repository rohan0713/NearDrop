package com.drop.near.bluetooth

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothClass
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.core.content.ContextCompat
import com.drop.near.domain.model.Device
import com.drop.near.domain.model.DeviceType
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

object BluetoothManagerHelper {

    @Suppress("DEPRECATION")
    fun getBluetoothAdapter(context: Context): BluetoothAdapter? {
        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
        return bluetoothManager?.adapter ?: BluetoothAdapter.getDefaultAdapter()
    }

    fun isBluetoothSupported(context: Context): Boolean {
        return getBluetoothAdapter(context) != null
    }

    fun isBluetoothEnabled(context: Context): Boolean {
        return getBluetoothAdapter(context)?.isEnabled == true
    }

    fun getRequiredPermissions(): Array<String> {
        val permissions = mutableListOf<String>()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissions.add(Manifest.permission.BLUETOOTH_SCAN)
            permissions.add(Manifest.permission.BLUETOOTH_CONNECT)
            permissions.add(Manifest.permission.BLUETOOTH_ADVERTISE)
        } else {
            permissions.add(Manifest.permission.BLUETOOTH)
            permissions.add(Manifest.permission.BLUETOOTH_ADMIN)
            permissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        return permissions.toTypedArray()
    }

    fun hasRequiredPermissions(context: Context): Boolean {
        return getRequiredPermissions().all { perm ->
            ContextCompat.checkSelfPermission(context, perm) == PackageManager.PERMISSION_GRANTED
        }
    }

    @SuppressLint("MissingPermission")
    fun getPairedDevices(context: Context): List<Device> {
        val adapter = getBluetoothAdapter(context) ?: return emptyList()
        if (!hasRequiredPermissions(context) || !adapter.isEnabled) return emptyList()

        return try {
            adapter.bondedDevices?.map { device ->
                mapBluetoothDeviceToDomain(device)
            } ?: emptyList()
        } catch (_: SecurityException) {
            emptyList()
        }
    }

    @SuppressLint("MissingPermission")
    fun discoverDevices(context: Context): Flow<List<Device>> = callbackFlow {
        val adapter = getBluetoothAdapter(context)
        if (adapter == null || !hasRequiredPermissions(context) || !adapter.isEnabled) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val discoveredMap = mutableMapOf<String, Device>()

        // Seed with currently paired devices
        val paired = getPairedDevices(context)
        paired.forEach { discoveredMap[it.id] = it }
        trySend(discoveredMap.values.toList())

        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                when (intent.action) {
                    BluetoothDevice.ACTION_FOUND -> {
                        val device = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java)
                        } else {
                            @Suppress("DEPRECATION")
                            intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                        }

                        if (device != null) {
                            val rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, (-65).toShort()).toInt()
                            val domainDevice = mapBluetoothDeviceToDomain(device, rssi)
                            discoveredMap[domainDevice.id] = domainDevice
                            trySend(discoveredMap.values.toList())
                        }
                    }
                    BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                        // Finished scan iteration
                    }
                }
            }
        }

        val filter = IntentFilter().apply {
            addAction(BluetoothDevice.ACTION_FOUND)
            addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        }
        context.registerReceiver(receiver, filter)

        try {
            if (adapter.isDiscovering) {
                adapter.cancelDiscovery()
            }
            adapter.startDiscovery()
        } catch (_: SecurityException) {
            // Permission denied
        }

        awaitClose {
            try {
                if (adapter.isDiscovering) {
                    adapter.cancelDiscovery()
                }
                context.unregisterReceiver(receiver)
            } catch (_: Exception) {}
        }
    }

    @SuppressLint("MissingPermission")
    fun mapBluetoothDeviceToDomain(device: BluetoothDevice, rssi: Int = -60): Device {
        val deviceName = try {
            device.name?.takeIf { it.isNotBlank() } ?: "Bluetooth Device (${device.address.takeLast(5)})"
        } catch (_: SecurityException) {
            "Bluetooth Device (${device.address.takeLast(5)})"
        }

        val deviceType = try {
            when (device.bluetoothClass?.majorDeviceClass) {
                BluetoothClass.Device.Major.PHONE -> DeviceType.PHONE
                BluetoothClass.Device.Major.COMPUTER -> DeviceType.LAPTOP
                BluetoothClass.Device.Major.AUDIO_VIDEO,
                BluetoothClass.Device.Major.WEARABLE -> DeviceType.PHONE
                else -> DeviceType.UNKNOWN
            }
        } catch (_: SecurityException) {
            DeviceType.UNKNOWN
        }

        return Device(
            id = device.address,
            name = deviceName,
            type = deviceType,
            ipAddress = device.address,
            port = 1,
            rssi = rssi,
            isFavorite = false,
            lastSeenEpochMs = System.currentTimeMillis()
        )
    }

    fun createBluetoothShareIntent(fileUri: Uri, mimeType: String?): Intent {
        return Intent(Intent.ACTION_SEND).apply {
            type = mimeType ?: "*/*"
            putExtra(Intent.EXTRA_STREAM, fileUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            setPackage("com.android.bluetooth")
        }
    }
}
