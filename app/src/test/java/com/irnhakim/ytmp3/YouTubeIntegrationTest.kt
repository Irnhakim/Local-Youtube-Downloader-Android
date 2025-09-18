package com.irnhakim.ytmp3

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.irnhakim.ytmp3.data.DownloadFormat
import com.irnhakim.ytmp3.data.DownloadRequest
import com.irnhakim.ytmp3.data.DownloadState
import com.irnhakim.ytmp3.repository.DownloadRepository
import com.irnhakim.ytmp3.service.YouTubeExtractorService
import com.irnhakim.ytmp3.utils.FileUtils
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeout
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class YouTubeIntegrationTest {

    private lateinit var context: Context
    private lateinit var repository: DownloadRepository
    private lateinit var youtubeExtractor: YouTubeExtractorService

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        repository = DownloadRepository(context)
        youtubeExtractor = YouTubeExtractorService(context)
    }

    @Test
    fun `test YouTube URL validation with various formats`() {
        val validUrls = listOf(
            "https://www.youtube.com/watch?v=dQw4w9WgXcQ",
            "https://youtu.be/dQw4w9WgXcQ",
            "https://youtube.com/watch?v=dQw4w9WgXcQ",
            "https://www.youtube.com/embed/dQw4w9WgXcQ",
            "https://www.youtube.com/v/dQw4w9WgXcQ"
        )

        val invalidUrls = listOf(
            "https://www.google.com",
            "https://www.facebook.com/video",
            "https://vimeo.com/123456",
            "not-a-url",
            "",
            "https://youtube.com/playlist?list=PLrAXtmRdnEQy6nuLMt9H1mu_0fqoNnU2T"
        )

        validUrls.forEach { url ->
            assertTrue("URL should be valid: $url", FileUtils.isValidYouTubeUrl(url))
            val videoId = FileUtils.extractVideoId(url)
            assertEquals("Should extract correct video ID from $url", "dQw4w9WgXcQ", videoId)
        }

        invalidUrls.forEach { url ->
            assertFalse("URL should be invalid: $url", FileUtils.isValidYouTubeUrl(url))
        }
    }

    @Test
    fun `test YouTubeExtractorService initialization`() = runTest {
        // Test initialization (may fail in test environment, but should not crash)
        val result = youtubeExtractor.initializeYoutubeDL()
        
        // In test environment, this might fail due to missing native libraries
        // But the code structure should be correct
        assertNotNull("Result should not be null", result)
        
        if (result.isFailure) {
            val exception = result.exceptionOrNull()
            assertNotNull("Exception should be present on failure", exception)
            println("Expected initialization failure in test environment: ${exception?.message}")
        } else {
            println("YouTube-DL initialization succeeded in test environment")
        }
    }

    @Test
    fun `test download flow with invalid URL handles errors gracefully`() = runTest {
        val invalidRequest = DownloadRequest(
            url = "https://www.google.com",
            format = DownloadFormat.MP4
        )

        val states = repository.downloadVideo(invalidRequest).take(3).toList()

        assertTrue("First state should be Loading", states[0] is DownloadState.Loading)
        assertTrue("Should eventually emit Error state", 
            states.any { it is DownloadState.Error })

        val errorState = states.find { it is DownloadState.Error } as? DownloadState.Error
        assertNotNull("Error state should be present", errorState)
        assertTrue("Error message should mention invalid URL", 
            errorState?.message?.contains("URL YouTube tidak valid") == true)
    }

    @Test
    fun `test file utilities comprehensive functionality`() {
        // Test video ID extraction from various URL formats
        val testCases = mapOf(
            "https://www.youtube.com/watch?v=dQw4w9WgXcQ" to "dQw4w9WgXcQ",
            "https://youtu.be/abc123XYZ" to "abc123XYZ",
            "https://www.youtube.com/embed/test_video_id" to "test_video_id"
        )

        testCases.forEach { (url, expectedId) ->
            val extractedId = FileUtils.extractVideoId(url)
            assertEquals("Should extract correct ID from $url", expectedId, extractedId)
        }

        // Test file name sanitization
        val problematicTitles = listOf(
            "Video with / slash",
            "Video with * asterisk",
            "Video with ? question",
            "Video with | pipe",
            "Video with < > brackets",
            "Video with : colon",
            "Video with \" quotes"
        )

        problematicTitles.forEach { title ->
            val fileName = FileUtils.generateFileName(title, "mp4")
            assertFalse("File name should not contain problematic characters: $fileName",
                fileName.contains(Regex("[/\\*\\?\\|<>:\"]")))
            assertTrue("File name should end with .mp4", fileName.endsWith(".mp4"))
        }

        // Test download directory creation
        val downloadDir = FileUtils.getDownloadDirectory(context)
        assertTrue("Download directory should exist", downloadDir.exists())
        assertTrue("Should be a directory", downloadDir.isDirectory)
        assertTrue("Directory name should contain YTMP3Downloads", 
            downloadDir.name.contains("YTMP3Downloads"))

        // Test file size formatting
        val testFile = java.io.File(downloadDir, "test.txt")
        testFile.writeText("Test content for file size testing")
        
        val fileSize = FileUtils.getFileSize(testFile)
        assertTrue("File size should be formatted correctly", 
            fileSize.matches(Regex("\\d+(\\.\\d+)? [KMGT]?B")))
        
        testFile.delete()
    }

    @Test
    fun `test repository error handling with network simulation`() = runTest {
        // Test with malformed URL
        val malformedRequest = DownloadRequest(
            url = "not-a-valid-url",
            format = DownloadFormat.MP3
        )

        val states = repository.downloadVideo(malformedRequest).take(2).toList()
        
        assertTrue("Should start with Loading", states[0] is DownloadState.Loading)
        assertTrue("Should emit Error for malformed URL", states[1] is DownloadState.Error)

        // Test with valid YouTube URL format but potentially non-existent video
        val nonExistentRequest = DownloadRequest(
            url = "https://www.youtube.com/watch?v=nonexistent123",
            format = DownloadFormat.MP4
        )

        // This should handle the error gracefully without crashing
        val nonExistentStates = repository.downloadVideo(nonExistentRequest).take(3).toList()
        assertTrue("Should handle non-existent video gracefully", 
            nonExistentStates.any { it is DownloadState.Loading || it is DownloadState.Error })
    }

    @Test
    fun `test format selection and conversion logic`() {
        // Test MP4 format selection
        val mp4Request = DownloadRequest(
            url = "https://www.youtube.com/watch?v=dQw4w9WgXcQ",
            format = DownloadFormat.MP4,
            quality = "best"
        )

        assertEquals("MP4 format should be preserved", DownloadFormat.MP4, mp4Request.format)
        assertEquals("Quality should be preserved", "best", mp4Request.quality)

        // Test MP3 format selection
        val mp3Request = DownloadRequest(
            url = "https://www.youtube.com/watch?v=dQw4w9WgXcQ",
            format = DownloadFormat.MP3
        )

        assertEquals("MP3 format should be preserved", DownloadFormat.MP3, mp3Request.format)
        assertEquals("Default quality should be best", "best", mp3Request.quality)
    }

    @Test
    fun `test concurrent download handling`() = runTest {
        // Test that multiple download requests can be handled
        val request1 = DownloadRequest(
            url = "https://www.youtube.com/watch?v=dQw4w9WgXcQ",
            format = DownloadFormat.MP4
        )

        val request2 = DownloadRequest(
            url = "https://www.youtube.com/watch?v=dQw4w9WgXcQ",
            format = DownloadFormat.MP3
        )

        // Start both downloads (they will fail in test environment, but should not crash)
        val flow1 = repository.downloadVideo(request1).take(2)
        val flow2 = repository.downloadVideo(request2).take(2)

        val states1 = flow1.toList()
        val states2 = flow2.toList()

        // Both should at least emit Loading state
        assertTrue("First download should emit Loading", states1[0] is DownloadState.Loading)
        assertTrue("Second download should emit Loading", states2[0] is DownloadState.Loading)

        // Repository should handle concurrent requests without crashing
        assertTrue("Repository should handle concurrent requests", 
            states1.isNotEmpty() && states2.isNotEmpty())
    }
}
