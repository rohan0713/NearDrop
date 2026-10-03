package com.drop.near.file

import kotlin.test.Test
import kotlin.test.assertEquals

class GalleryFileHelperTest {

    @Test
    fun formatBytes_handlesZeroAndNegative() {
        assertEquals("0 B", formatBytes(0L))
        assertEquals("0 B", formatBytes(-500L))
    }

    @Test
    fun formatBytes_formatsKilobytesCorrectly() {
        assertEquals("1 KB", formatBytes(1024L))
        assertEquals("1.5 KB", formatBytes(1536L))
    }

    @Test
    fun formatBytes_formatsMegabytesCorrectly() {
        assertEquals("1 MB", formatBytes(1024L * 1024L))
        assertEquals("10.5 MB", formatBytes((10.5 * 1024L * 1024L).toLong()))
    }

    @Test
    fun formatBytes_formatsGigabytesCorrectly() {
        assertEquals("1 GB", formatBytes(1024L * 1024L * 1024L))
        assertEquals("2.4 GB", formatBytes((2.4 * 1024L * 1024L * 1024L).toLong()))
    }
}
