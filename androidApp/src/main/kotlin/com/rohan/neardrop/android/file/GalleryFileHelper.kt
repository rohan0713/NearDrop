package com.rohan.neardrop.android.file

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import java.text.DecimalFormat

data class SelectedFile(
    val uri: Uri,
    val name: String,
    val sizeBytes: Long,
    val mimeType: String?,
    val isImage: Boolean,
    val isVideo: Boolean
) {
    val formattedSize: String
        get() = formatBytes(sizeBytes)
}

object GalleryFileHelper {

    fun resolveFile(context: Context, uri: Uri): SelectedFile {
        var fileName = "Selected_File"
        var fileSize = 0L

        try {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (nameIndex != -1) {
                        fileName = cursor.getString(nameIndex) ?: fileName
                    }
                    val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                    if (sizeIndex != -1) {
                        fileSize = cursor.getLong(sizeIndex)
                    }
                }
            }
        } catch (_: Exception) {
            // Fallback to last path segment if query fails
            uri.lastPathSegment?.let { segment ->
                fileName = segment.substringAfterLast('/')
            }
        }

        val mimeType = try {
            context.contentResolver.getType(uri)
        } catch (_: Exception) {
            null
        }

        val isImage = mimeType?.startsWith("image/") == true ||
                fileName.endsWith(".jpg", ignoreCase = true) ||
                fileName.endsWith(".jpeg", ignoreCase = true) ||
                fileName.endsWith(".png", ignoreCase = true) ||
                fileName.endsWith(".webp", ignoreCase = true)

        val isVideo = mimeType?.startsWith("video/") == true ||
                fileName.endsWith(".mp4", ignoreCase = true) ||
                fileName.endsWith(".mkv", ignoreCase = true) ||
                fileName.endsWith(".mov", ignoreCase = true)

        return SelectedFile(
            uri = uri,
            name = fileName,
            sizeBytes = fileSize.coerceAtLeast(0L),
            mimeType = mimeType,
            isImage = isImage,
            isVideo = isVideo
        )
    }
}

fun formatBytes(bytes: Long): String {
    if (bytes <= 0) return "0 B"
    val units = arrayOf("B", "KB", "MB", "GB", "TB")
    val digitGroups = (Math.log10(bytes.toDouble()) / Math.log10(1024.0)).toInt().coerceIn(0, units.size - 1)
    val df = DecimalFormat("#,##0.#")
    return "${df.format(bytes / Math.pow(1024.0, digitGroups.toDouble()))} ${units[digitGroups]}"
}
