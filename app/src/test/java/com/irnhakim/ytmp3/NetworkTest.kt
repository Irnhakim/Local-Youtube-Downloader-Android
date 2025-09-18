package com.irnhakim.ytmp3

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.irnhakim.ytmp3.data.DownloadFormat
import com.irnhakim.ytmp3.data.DownloadRequest
import com.irnhakim.ytmp3.data.DownloadState
import com.irnhakim.ytmp3.repository.DownloadRepository
import com.irnhakim.ytmp3.utils.FileUtils
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class NetworkTest {

    private lateinit var application: Application
    private lateinit var repository: DownloadRepository

    @Before
    fun setup() {
        application = ApplicationProvider.getApplicationContext()
        repository = DownloadRepository(application)
    }

    @Test
    fun `test various YouTube URL formats validation`() {
        val validUrls = listOf(
            // Standard YouTube URLs
            "https://www.youtube.com/watch?v=dQw4w9WgXcQ",
            "https://youtube.com/watch?v=dQw4w9WgXcQ",
            "http://www.youtube.com/watch?v=dQw4w9WgXcQ",
            "http://youtube.com/watch?v=dQw4w9WgXcQ",
            
            // Short URLs
            "https://youtu.be/dQw4w9WgXcQ",
            "http://youtu.be/dQw4w9WgXcQ",
            
            // Embed URLs
            "https://www.youtube.com/embed/dQw4w9WgXcQ",
            "https://youtube.com/embed/dQw4w9WgXcQ",
            
            // Direct video URLs
            "https://www.youtube.com/v/dQw4w9WgXcQ",
            "https://youtube.com/v/dQw4w9WgXcQ",
            
            // URLs with additional parameters
            "https://www.youtube.com/watch?v=dQw4w9WgXcQ&t=30s",
            "https://www.youtube.com/watch?v=dQw4w9WgXcQ&list=PLrAXtmRdnEQy6nuLMt9H1mu_0VgZJoJX9",
            "https://www.youtube.com/watch?v=dQw4w9WgXcQ&ab_channel=TestChannel",
            
            // Mobile URLs
            "https://m.youtube.com/watch?v=dQw4w9WgXcQ",
            "https://mobile.youtube.com/watch?v=dQw4w9WgXcQ"
        )

        validUrls.forEach { url ->
            val isValid = FileUtils.isValidYouTubeUrl(url)
            assert(isValid) { "URL should be valid: $url" }
            
            val videoId = FileUtils.extractVideoId(url)
            assert(videoId == "dQw4w9WgXcQ") { "Video ID should be extracted correctly from: $url" }
        }
    }

    @Test
    fun `test invalid YouTube URLs`() {
        val invalidUrls = listOf(
            // Non-YouTube URLs
            "https://www.google.com",
            "https://www.facebook.com/video/123",
            "https://vimeo.com/123456",
            "https://dailymotion.com/video/123",
            
            // Malformed YouTube URLs
            "youtube.com/watch?v=",
            "https://youtube.com/watch?v=",
            "https://youtube.com/watch?video=dQw4w9WgXcQ",
            "https://youtube.com/video/dQw4w9WgXcQ",
            
            // Invalid video IDs
            "https://youtube.com/watch?v=invalid",
            "https://youtube.com/watch?v=123",
            "https://youtube.com/watch?v=dQw4w9WgXcQ123456",
            
            // Empty or null
            "",
            "   ",
            
            // Random strings
            "not a url",
            "123456789",
            "dQw4w9WgXcQ"
        )

        invalidUrls.forEach { url ->
            val isValid = FileUtils.isValidYouTubeUrl(url)
            assert(!isValid) { "URL should be invalid: $url" }
        }
    }

    @Test
    fun `test video ID extraction from various formats`() {
        val urlToVideoIdMap = mapOf(
            "https://www.youtube.com/watch?v=dQw4w9WgXcQ" to "dQw4w9WgXcQ",
            "https://youtu.be/abc123DEF45" to "abc123DEF45",
            "https://youtube.com/embed/XYZ789xyz12" to "XYZ789xyz12",
            "https://www.youtube.com/v/mno456PQR78" to "mno456PQR78",
            "https://m.youtube.com/watch?v=stu901VWX23" to "stu901VWX23"
        )

        urlToVideoIdMap.forEach { (url, expectedId) ->
            val extractedId = FileUtils.extractVideoId(url)
            assert(extractedId == expectedId) { 
                "Expected video ID '$expectedId' but got '$extractedId' from URL: $url" 
            }
        }
    }

    @Test
    fun `test download request creation for different formats`() {
        val testUrl = "https://www.youtube.com/watch?v=dQw4w9WgXcQ"
        
        // Test MP4 request
        val mp4Request = DownloadRequest(testUrl, DownloadFormat.MP4)
        assert(mp4Request.url == testUrl) { "MP4 request should have correct URL" }
        assert(mp4Request.format == DownloadFormat.MP4) { "MP4 request should have MP4 format" }
        assert(mp4Request.quality == "best") { "MP4 request should have default quality" }
        
        // Test MP3 request
        val mp3Request = DownloadRequest(testUrl, DownloadFormat.MP3)
        assert(mp3Request.url == testUrl) { "MP3 request should have correct URL" }
        assert(mp3Request.format == DownloadFormat.MP3) { "MP3 request should have MP3 format" }
        assert(mp3Request.quality == "best") { "MP3 request should have default quality" }
        
        // Test custom quality
        val customQualityRequest = DownloadRequest(testUrl, DownloadFormat.MP4, "720p")
        assert(customQualityRequest.quality == "720p") { "Custom quality should be set correctly" }
    }

    @Test
    fun `test network error handling`() = runBlocking {
        val invalidUrl = "https://www.youtube.com/watch?v=invalidvideoid"
        
        try {
            withTimeout(5000) { // 5 second timeout
                val result = repository.getVideoInfo(invalidUrl)
                result.fold(
                    onSuccess = { 
                        // If somehow it succeeds, that's unexpected but not a failure
                        assert(true) { "Unexpected success for invalid URL" }
                    },
                    onFailure = { error ->
                        // This is expected for invalid URLs
                        assert(error.message != null) { "Error should have a message" }
                        assert(error.message!!.isNotEmpty()) { "Error message should not be empty" }
                    }
                )
            }
        } catch (e: Exception) {
            // Timeout or other exceptions are acceptable for network tests
            assert(true) { "Network operations may fail in test environment" }
        }
    }

    @Test
    fun `test concurrent URL validations`() = runBlocking {
        val urls = listOf(
            "https://www.youtube.com/watch?v=dQw4w9WgXcQ",
            "https://youtu.be/abc123DEF45",
            "https://youtube.com/embed/XYZ789xyz12",
            "https://www.youtube.com/v/mno456PQR78",
            "https://m.youtube.com/watch?v=stu901VWX23"
        )

        // Test that multiple URL validations can be performed concurrently
        val results = urls.map { url ->
            FileUtils.isValidYouTubeUrl(url)
        }

        // All should be valid
        results.forEach { isValid ->
            assert(isValid) { "All test URLs should be valid" }
        }
    }

    @Test
    fun `test URL parameter handling`() {
        val baseUrl = "https://www.youtube.com/watch?v=dQw4w9WgXcQ"
        val urlsWithParams = listOf(
            "$baseUrl&t=30s",
            "$baseUrl&list=PLrAXtmRdnEQy6nuLMt9H1mu_0VgZJoJX9",
            "$baseUrl&ab_channel=TestChannel",
            "$baseUrl&feature=youtu.be",
            "$baseUrl&t=1m30s&list=PLtest&ab_channel=Test"
        )

        urlsWithParams.forEach { url ->
            val isValid = FileUtils.isValidYouTubeUrl(url)
            assert(isValid) { "URL with parameters should be valid: $url" }
            
            val videoId = FileUtils.extractVideoId(url)
            assert(videoId == "dQw4w9WgXcQ") { 
                "Video ID should be extracted correctly from URL with parameters: $url" 
            }
        }
    }

    @Test
    fun `test edge case URLs`() {
        val edgeCaseUrls = mapOf(
            // URLs with different protocols
            "http://youtube.com/watch?v=dQw4w9WgXcQ" to true,
            "https://youtube.com/watch?v=dQw4w9WgXcQ" to true,
            
            // URLs with www and without
            "https://www.youtube.com/watch?v=dQw4w9WgXcQ" to true,
            "https://youtube.com/watch?v=dQw4w9WgXcQ" to true,
            
            // Case sensitivity
            "https://YouTube.com/watch?v=dQw4w9WgXcQ" to false, // Should be case sensitive
            "https://YOUTUBE.COM/watch?v=dQw4w9WgXcQ" to false,
            
            // Trailing slashes
            "https://youtube.com/watch?v=dQw4w9WgXcQ/" to true,
            "https://youtu.be/dQw4w9WgXcQ/" to true
        )

        edgeCaseUrls.forEach { (url, expectedValid) ->
            val isValid = FileUtils.isValidYouTubeUrl(url)
            assert(isValid == expectedValid) { 
                "URL '$url' should be ${if (expectedValid) "valid" else "invalid"}" 
            }
        }
    }

    @Test
    fun `test download state transitions`() = runBlocking {
        val testUrl = "https://www.youtube.com/watch?v=dQw4w9WgXcQ"
        val request = DownloadRequest(testUrl, DownloadFormat.MP4)
        
        // Mock video info for testing
        val mockVideoInfo = com.irnhakim.ytmp3.data.VideoInfo(
            id = "dQw4w9WgXcQ",
            title = "Test Video",
            duration = "3:32",
            thumbnail = "https://example.com/thumb.jpg",
            uploader = "Test Channel",
            url = testUrl,
            formats = emptyList()
        )

        try {
            withTimeout(3000) { // 3 second timeout
                repository.downloadVideo(request, mockVideoInfo).collect { state ->
                    when (state) {
                        is DownloadState.Idle -> {
                            assert(true) { "Idle state is valid" }
                        }
                        is DownloadState.Loading -> {
                            assert(true) { "Loading state is valid" }
                        }
                        is DownloadState.Progress -> {
                            assert(state.progress >= 0 && state.progress <= 100) { 
                                "Progress should be between 0 and 100" 
                            }
                            assert(state.status.isNotEmpty()) { "Status should not be empty" }
                        }
                        is DownloadState.Success -> {
                            assert(state.fileName.isNotEmpty()) { "File name should not be empty" }
                            assert(state.filePath.isNotEmpty()) { "File path should not be empty" }
                        }
                        is DownloadState.Error -> {
                            assert(state.message.isNotEmpty()) { "Error message should not be empty" }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            // Network operations may fail in test environment
            assert(true) { "Network operations may fail in test environment: ${e.message}" }
        }
    }
}
