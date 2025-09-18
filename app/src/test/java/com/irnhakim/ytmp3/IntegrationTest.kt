package com.irnhakim.ytmp3

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.irnhakim.ytmp3.data.DownloadFormat
import com.irnhakim.ytmp3.data.DownloadRequest
import com.irnhakim.ytmp3.data.DownloadState
import com.irnhakim.ytmp3.repository.DownloadRepository
import com.irnhakim.ytmp3.utils.FileUtils
import com.irnhakim.ytmp3.viewmodel.DownloadViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class IntegrationTest {

    private lateinit var application: Application
    private lateinit var repository: DownloadRepository
    private lateinit var viewModel: DownloadViewModel

    @Before
    fun setup() {
        application = ApplicationProvider.getApplicationContext()
        repository = DownloadRepository(application)
        viewModel = DownloadViewModel(application)
    }

    @Test
    fun `test complete download flow - URL validation to UI state`() = runBlocking {
        // Test URL validation
        val validUrl = "https://www.youtube.com/watch?v=dQw4w9WgXcQ"
        val isValid = FileUtils.isValidYouTubeUrl(validUrl)
        assert(isValid) { "Valid YouTube URL should be recognized" }

        // Test video ID extraction
        val videoId = FileUtils.extractVideoId(validUrl)
        assert(videoId == "dQw4w9WgXcQ") { "Video ID should be extracted correctly" }

        // Test ViewModel URL update
        viewModel.updateUrl(validUrl)
        val uiState = viewModel.uiState.first()
        assert(uiState.url == validUrl) { "URL should be updated in UI state" }

        // Test format selection
        viewModel.updateFormat(DownloadFormat.MP3)
        val updatedState = viewModel.uiState.first()
        assert(updatedState.selectedFormat == DownloadFormat.MP3) { "Format should be updated" }
    }

    @Test
    fun `test error handling flow`() = runBlocking {
        // Test invalid URL
        val invalidUrl = "not-a-youtube-url"
        val isValid = FileUtils.isValidYouTubeUrl(invalidUrl)
        assert(!isValid) { "Invalid URL should be rejected" }

        // Test empty URL in ViewModel
        viewModel.updateUrl("")
        viewModel.getVideoInfo()
        val uiState = viewModel.uiState.first()
        assert(uiState.errorMessage != null) { "Error message should be set for empty URL" }
    }

    @Test
    fun `test download state transitions`() = runBlocking {
        // Initial state should be Idle
        val initialState = viewModel.downloadState.first()
        assert(initialState is DownloadState.Idle) { "Initial download state should be Idle" }

        // Test reset functionality
        viewModel.resetDownload()
        val resetState = viewModel.downloadState.first()
        assert(resetState is DownloadState.Idle) { "Reset should return to Idle state" }
    }

    @Test
    fun `test file operations integration`() {
        // Test file name generation
        val title = "Test Video Title with Special Characters!@#"
        val fileName = FileUtils.generateFileName(title, "mp4")
        assert(fileName.contains("Test_Video_Title")) { "File name should be sanitized" }
        assert(fileName.endsWith(".mp4")) { "File name should have correct extension" }

        // Test download directory creation
        val downloadDir = FileUtils.getDownloadDirectory(application)
        assert(downloadDir.exists() || downloadDir.mkdirs()) { "Download directory should be created" }
    }

    @Test
    fun `test various YouTube URL formats`() {
        val urlFormats = listOf(
            "https://www.youtube.com/watch?v=dQw4w9WgXcQ",
            "https://youtu.be/dQw4w9WgXcQ",
            "https://youtube.com/watch?v=dQw4w9WgXcQ",
            "http://www.youtube.com/watch?v=dQw4w9WgXcQ",
            "https://www.youtube.com/embed/dQw4w9WgXcQ",
            "https://www.youtube.com/v/dQw4w9WgXcQ"
        )

        urlFormats.forEach { url ->
            val isValid = FileUtils.isValidYouTubeUrl(url)
            assert(isValid) { "URL format $url should be valid" }

            val videoId = FileUtils.extractVideoId(url)
            assert(videoId == "dQw4w9WgXcQ") { "Video ID should be extracted from $url" }
        }
    }

    @Test
    fun `test download request creation`() {
        val url = "https://www.youtube.com/watch?v=dQw4w9WgXcQ"
        val format = DownloadFormat.MP3
        
        val request = DownloadRequest(url, format)
        
        assert(request.url == url) { "Download request should contain correct URL" }
        assert(request.format == format) { "Download request should contain correct format" }
        assert(request.quality == "best") { "Download request should have default quality" }
    }

    @Test
    fun `test UI state management`() = runBlocking {
        // Test initial state
        val initialState = viewModel.uiState.first()
        assert(initialState.url.isEmpty()) { "Initial URL should be empty" }
        assert(initialState.selectedFormat == DownloadFormat.MP4) { "Default format should be MP4" }
        assert(!initialState.isLoading) { "Initial loading state should be false" }
        assert(initialState.videoInfo == null) { "Initial video info should be null" }
        assert(!initialState.showVideoInfo) { "Initial show video info should be false" }
        assert(initialState.errorMessage == null) { "Initial error message should be null" }

        // Test error clearing
        viewModel.updateUrl("invalid")
        viewModel.getVideoInfo()
        var stateWithError = viewModel.uiState.first()
        assert(stateWithError.errorMessage != null) { "Error should be set" }

        viewModel.clearError()
        val stateWithoutError = viewModel.uiState.first()
        assert(stateWithoutError.errorMessage == null) { "Error should be cleared" }
    }
}
