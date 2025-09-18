package com.irnhakim.ytmp3

import com.irnhakim.ytmp3.data.DownloadFormat
import com.irnhakim.ytmp3.data.DownloadState
import org.junit.Test
import org.junit.Assert.*

class DownloadViewModelTest {

    @Test
    fun testDownloadFormatEnum() {
        assertEquals("MP4", DownloadFormat.MP4.name)
        assertEquals("MP3", DownloadFormat.MP3.name)
    }

    @Test
    fun testDownloadStateSealed() {
        val idleState = DownloadState.Idle
        val loadingState = DownloadState.Loading
        val progressState = DownloadState.Progress(50, "Downloading...")
        val successState = DownloadState.Success("/path/to/file", "video.mp4")
        val errorState = DownloadState.Error("Download failed")

        assertTrue(idleState is DownloadState.Idle)
        assertTrue(loadingState is DownloadState.Loading)
        assertTrue(progressState is DownloadState.Progress)
        assertTrue(successState is DownloadState.Success)
        assertTrue(errorState is DownloadState.Error)

        assertEquals(50, progressState.progress)
        assertEquals("Downloading...", progressState.status)
        assertEquals("/path/to/file", successState.filePath)
        assertEquals("video.mp4", successState.fileName)
        assertEquals("Download failed", errorState.message)
    }
}
