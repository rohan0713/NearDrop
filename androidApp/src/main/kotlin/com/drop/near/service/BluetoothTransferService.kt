package com.drop.near.service

import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.net.Uri
import android.os.Build
import android.os.IBinder
import androidx.core.content.ContextCompat
import com.drop.near.notification.TransferNotificationHelper
import com.drop.near.core.result.AppResult
import com.drop.near.di.NearDropSdk
import com.drop.near.domain.model.Device
import com.drop.near.domain.model.DeviceType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class BluetoothTransferService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var transferJob: Job? = null
    private var activeTransferId: String? = null
    private var activeFileName: String = ""
    private var activeDeviceName: String = ""

    override fun onCreate() {
        super.onCreate()
        TransferNotificationHelper.createNotificationChannel(this)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_TRANSFER -> {
                val targetDeviceId = intent.getStringExtra(EXTRA_TARGET_DEVICE_ID) ?: "unknown_device"
                val targetDeviceName = intent.getStringExtra(EXTRA_TARGET_DEVICE_NAME) ?: "Bluetooth Device"
                val fileName = intent.getStringExtra(EXTRA_FILE_NAME) ?: "SharedFile.bin"
                val fileSizeBytes = intent.getLongExtra(EXTRA_FILE_SIZE, 1024L * 1024L)
                val fileUriString = intent.getStringExtra(EXTRA_FILE_URI)

                startTransferSession(
                    targetDeviceId = targetDeviceId,
                    targetDeviceName = targetDeviceName,
                    fileName = fileName,
                    fileSizeBytes = fileSizeBytes,
                    fileUri = fileUriString?.let { Uri.parse(it) }
                )
            }
            ACTION_CANCEL_TRANSFER -> {
                val transferId = intent.getStringExtra(EXTRA_TRANSFER_ID) ?: activeTransferId
                cancelTransferSession(transferId)
            }
        }
        return START_NOT_STICKY
    }

    private fun startTransferSession(
        targetDeviceId: String,
        targetDeviceName: String,
        fileName: String,
        fileSizeBytes: Long,
        fileUri: Uri?
    ) {
        transferJob?.cancel()
        activeFileName = fileName
        activeDeviceName = targetDeviceName

        val initialNotification = TransferNotificationHelper.buildProgressNotification(
            context = this,
            transferId = "init",
            fileName = fileName,
            deviceName = targetDeviceName,
            transferredBytes = 0L,
            totalBytes = fileSizeBytes,
            speedBytesPerSec = 0L
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val foregroundServiceType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE or ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            } else {
                ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE
            }
            startForeground(TransferNotificationHelper.NOTIFICATION_ID, initialNotification, foregroundServiceType)
        } else {
            startForeground(TransferNotificationHelper.NOTIFICATION_ID, initialNotification)
        }

        transferJob = serviceScope.launch(Dispatchers.IO) {
            val targetDevice = Device(
                id = targetDeviceId,
                name = targetDeviceName,
                type = DeviceType.PHONE,
                ipAddress = targetDeviceId,
                port = 1
            )

            val initResult = NearDropSdk.container.transferRepository.initiateTransfer(
                targetDevice = targetDevice,
                fileName = fileName,
                fileSizeBytes = fileSizeBytes
            )

            val transferItem = when (initResult) {
                is AppResult.Success -> initResult.data
                is AppResult.Error -> {
                    TransferNotificationHelper.notifyFailed(
                        context = this@BluetoothTransferService,
                        fileName = fileName,
                        deviceName = targetDeviceName,
                        reason = "Could not initialize session"
                    )
                    stopServiceForeground()
                    return@launch
                }
            }

            val transferId = transferItem.id
            activeTransferId = transferId

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Transfer simulation / byte streaming loop
            val totalBytes = fileSizeBytes.coerceAtLeast(1024L)
            val chunkSize = (totalBytes / 12).coerceIn(4096L, 1024L * 512L)
            var transferred = 0L
            val startTimeMs = System.currentTimeMillis()

            try {
                while (transferred < totalBytes && isActive) {
                    delay(350)
                    transferred = (transferred + chunkSize).coerceAtMost(totalBytes)

                    val elapsedSec = ((System.currentTimeMillis() - startTimeMs) / 1000.0).coerceAtLeast(0.1)
                    val speedBytesSec = (transferred / elapsedSec).toLong()

                    NearDropSdk.container.transferRepository.updateTransferProgress(
                        transferId = transferId,
                        bytesTransferred = transferred,
                        speedBytesPerSec = speedBytesSec
                    )

                    val updatedNotification = TransferNotificationHelper.buildProgressNotification(
                        context = this@BluetoothTransferService,
                        transferId = transferId,
                        fileName = fileName,
                        deviceName = targetDeviceName,
                        transferredBytes = transferred,
                        totalBytes = totalBytes,
                        speedBytesPerSec = speedBytesSec
                    )
                    notificationManager.notify(TransferNotificationHelper.NOTIFICATION_ID, updatedNotification)
                }

                if (isActive && transferred >= totalBytes) {
                    NearDropSdk.container.transferRepository.completeTransfer(transferId)
                    TransferNotificationHelper.notifyCompleted(
                        context = this@BluetoothTransferService,
                        fileName = fileName,
                        deviceName = targetDeviceName
                    )
                }
            } catch (e: Exception) {
                NearDropSdk.container.transferRepository.failTransfer(
                    transferId = transferId,
                    reason = e.message ?: "Transfer encountered an error"
                )
                TransferNotificationHelper.notifyFailed(
                    context = this@BluetoothTransferService,
                    fileName = fileName,
                    deviceName = targetDeviceName,
                    reason = e.message ?: "Transfer failure"
                )
            } finally {
                stopServiceForeground()
            }
        }
    }

    private fun cancelTransferSession(transferId: String?) {
        transferJob?.cancel()
        serviceScope.launch(Dispatchers.IO) {
            transferId?.let { id ->
                NearDropSdk.container.transferRepository.cancelTransfer(id)
            }
            TransferNotificationHelper.notifyCancelled(
                context = this@BluetoothTransferService,
                fileName = activeFileName,
                deviceName = activeDeviceName
            )
            stopServiceForeground()
        }
    }

    private fun stopServiceForeground() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            @Suppress("DEPRECATION")
            stopForeground(true)
        }
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    companion object {
        const val ACTION_START_TRANSFER = "com.drop.near.action.START_TRANSFER"
        const val ACTION_CANCEL_TRANSFER = "com.drop.near.action.CANCEL_TRANSFER"

        const val EXTRA_TARGET_DEVICE_ID = "extra_target_device_id"
        const val EXTRA_TARGET_DEVICE_NAME = "extra_target_device_name"
        const val EXTRA_FILE_NAME = "extra_file_name"
        const val EXTRA_FILE_SIZE = "extra_file_size"
        const val EXTRA_FILE_URI = "extra_file_uri"
        const val EXTRA_MIME_TYPE = "extra_mime_type"
        const val EXTRA_TRANSFER_ID = "extra_transfer_id"

        fun startTransfer(
            context: Context,
            targetDevice: Device,
            fileName: String,
            fileSizeBytes: Long,
            fileUri: Uri?,
            mimeType: String?
        ) {
            val intent = Intent(context, BluetoothTransferService::class.java).apply {
                action = ACTION_START_TRANSFER
                putExtra(EXTRA_TARGET_DEVICE_ID, targetDevice.id)
                putExtra(EXTRA_TARGET_DEVICE_NAME, targetDevice.name)
                putExtra(EXTRA_FILE_NAME, fileName)
                putExtra(EXTRA_FILE_SIZE, fileSizeBytes)
                fileUri?.let { putExtra(EXTRA_FILE_URI, it.toString()) }
                mimeType?.let { putExtra(EXTRA_MIME_TYPE, it) }
            }
            ContextCompat.startForegroundService(context, intent)
        }

        fun cancelTransfer(context: Context, transferId: String) {
            val intent = Intent(context, BluetoothTransferService::class.java).apply {
                action = ACTION_CANCEL_TRANSFER
                putExtra(EXTRA_TRANSFER_ID, transferId)
            }
            context.startService(intent)
        }
    }
}
