package com.rohan.neardrop.android.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.rohan.neardrop.android.R
import com.rohan.neardrop.android.file.formatBytes
import com.rohan.neardrop.android.service.BluetoothTransferService
import com.rohan.neardrop.android.ui.MainActivity

object TransferNotificationHelper {

    const val CHANNEL_ID = "neardrop_bluetooth_transfers"
    const val CHANNEL_NAME = "Bluetooth File Transfers"
    const val NOTIFICATION_ID = 4040

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = "Shows real-time progress and status of Bluetooth file transfers"
                enableLights(false)
                enableVibration(false)
                setShowBadge(true)
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun buildProgressNotification(
        context: Context,
        transferId: String,
        fileName: String,
        deviceName: String,
        transferredBytes: Long,
        totalBytes: Long,
        speedBytesPerSec: Long = 0L
    ): Notification {
        val progressPercent = if (totalBytes > 0) {
            ((transferredBytes.toDouble() / totalBytes.toDouble()) * 100).toInt().coerceIn(0, 100)
        } else {
            0
        }

        val contentIntent = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val cancelIntent = PendingIntent.getService(
            context,
            transferId.hashCode(),
            Intent(context, BluetoothTransferService::class.java).apply {
                action = BluetoothTransferService.ACTION_CANCEL_TRANSFER
                putExtra(BluetoothTransferService.EXTRA_TRANSFER_ID, transferId)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val transferredStr = formatBytes(transferredBytes)
        val totalStr = formatBytes(totalBytes)
        val speedStr = if (speedBytesPerSec > 0) " • ${formatBytes(speedBytesPerSec)}/s" else ""

        val iconRes = R.drawable.ic_notification_share

        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(iconRes)
            .setContentTitle("Sharing $fileName")
            .setContentText("$transferredStr of $totalStr ($progressPercent%)$speedStr")
            .setSubText("Bluetooth • $deviceName")
            .setProgress(100, progressPercent, false)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setContentIntent(contentIntent)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Cancel", cancelIntent)
            .setCategory(NotificationCompat.CATEGORY_PROGRESS)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    fun buildCompletedNotification(
        context: Context,
        fileName: String,
        deviceName: String
    ): Notification {
        val contentIntent = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val iconRes = R.drawable.ic_notification_share

        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(iconRes)
            .setContentTitle("File Transfer Complete")
            .setContentText("$fileName sent to $deviceName")
            .setSubText("Bluetooth")
            .setOngoing(false)
            .setAutoCancel(true)
            .setContentIntent(contentIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
    }

    fun buildFailedNotification(
        context: Context,
        fileName: String,
        deviceName: String,
        reason: String
    ): Notification {
        val contentIntent = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val iconRes = R.drawable.ic_notification_share

        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(iconRes)
            .setContentTitle("Transfer Failed")
            .setContentText("Could not send $fileName to $deviceName: $reason")
            .setSubText("Bluetooth")
            .setOngoing(false)
            .setAutoCancel(true)
            .setContentIntent(contentIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
    }

    fun buildCancelledNotification(
        context: Context,
        fileName: String,
        deviceName: String
    ): Notification {
        val iconRes = R.drawable.ic_notification_share

        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(iconRes)
            .setContentTitle("Transfer Cancelled")
            .setContentText("$fileName transfer to $deviceName was cancelled")
            .setSubText("Bluetooth")
            .setOngoing(false)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    fun notifyCompleted(context: Context, fileName: String, deviceName: String) {
        val notification = buildCompletedNotification(context, fileName, deviceName)
        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID + 1, notification)
        } catch (_: SecurityException) {
            // Permission might be denied
        }
    }

    fun notifyFailed(context: Context, fileName: String, deviceName: String, reason: String) {
        val notification = buildFailedNotification(context, fileName, deviceName, reason)
        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID + 2, notification)
        } catch (_: SecurityException) {
            // Permission might be denied
        }
    }

    fun notifyCancelled(context: Context, fileName: String, deviceName: String) {
        val notification = buildCancelledNotification(context, fileName, deviceName)
        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID + 3, notification)
        } catch (_: SecurityException) {
            // Permission might be denied
        }
    }

    fun cancelNotification(context: Context, notificationId: Int = NOTIFICATION_ID) {
        try {
            NotificationManagerCompat.from(context).cancel(notificationId)
        } catch (_: Exception) {}
    }
}
