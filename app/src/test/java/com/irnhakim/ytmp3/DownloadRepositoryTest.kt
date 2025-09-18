package com.irnhakim.ytmp3

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.irnhakim.ytmp3.data.DownloadFormat
import com.irnhakim.ytmp3.data.DownloadRequest
import com.irnhakim.ytmp3.data.DownloadState
import com.irnhakim.ytmp3.repository.DownloadRepository
import com.irnhakim.ytmp3.utils.FileUtils
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class DownloadRepositoryTest {

    private lateinit var context: Context
    private lateinit var repository: DownloadRepository

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        repository = DownloadRepository(context)
    }

    @Test
    fun `test URL validation works correctly`() {
        // Test valid URLs
        assertTrue("YouTube watch URL should be valid", 
            FileUtils.isValidYouTubeUrl("https://www.youtube.com/watch?v=dQw4w9WgXcQ"))
        assertTrue("YouTube short URL should be valid", 
            FileUtils.isValidYouTubeUrl("https://youtu.be/dQw4w9WgXcQ"))
        
        // Test invalid URLs
        assertFalse("Google URL should be invalid", 
            FileUtils.isValidYouTubeUrl("https://www.google.com"))
        assertFalse("Empty URL should be invalid", 
            FileUtils.isValidYouTubeUrl(""))
    }

    @Test
    fun `test getVideoInfo with invalid URL returns failure`() = runTest {
        val invalidUrl = "https://www.google.com"
        
        val result = repository.getVideoInfo(invalidUrl)
        
        assertTrue("Should return failure for invalid URL", result.isFailure)
        val exception = result.exceptionOrNull()
        assertNotNull("Exception should not be null", exception)
        assertEquals("Should have correct error message", "URL YouTube tidak valid", exception?.message)
    }

    @Test
    fun `test downloadVideo with invalid URL emits error state`() = runTest {
        val request = DownloadRequest(
            url = "https://www.google.com",
            format = DownloadFormat.MP4
        )
        
        val states = repository.downloadVideo(request).take(2).toList()
        
        assertTrue("Should emit Loading state first", states[0] is DownloadState.Loading)
        assertTrue("Should emit Error state for invalid URL", states[1] is DownloadState.Error)
        
        val errorState = states[1] as DownloadState.Error
        assertTrue("Error message should mention invalid URL", 
            errorState.message.contains("URL YouTube tidak valid"))
    }

    @Test
    fun `test file utilities work correctly`() {
        // Test video ID extraction
        val videoId = FileUtils.extractVideoId("https://www.youtube.com/watch?v=dQw4w9WgXcQ")
        assertEquals("Should extract correct video ID", "dQw4w9WgXcQ", videoId)
        
        // Test file name generation
        val fileName = FileUtils.generateFileName("Test Video Title", "mp4")
        assertTrue("File name should contain sanitized title", fileName.contains("Test_Video_Title"))
        assertTrue("File name should have correct extension", fileName.endsWith(".mp4"))
        
        // Test download directory
        val downloadDir = FileUtils.getDownloadDirectory(context)
        assertTrue("Download directory should exist", downloadDir.exists())
        assertTrue("Should be a directory", downloadDir.isDirectory)
    }
}
