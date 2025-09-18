package com.irnhakim.ytmp3.ui

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.irnhakim.ytmp3.data.DownloadFormat
import com.irnhakim.ytmp3.data.DownloadState
import com.irnhakim.ytmp3.data.VideoInfo
import com.irnhakim.ytmp3.ui.screen.DownloadScreen
import com.irnhakim.ytmp3.ui.theme.YTMP3Theme
import com.irnhakim.ytmp3.viewmodel.DownloadViewModel
import io.mockk.mockk
import io.mockk.every
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DownloadScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun downloadScreen_displaysInitialState() {
        composeTestRule.setContent {
            YTMP3Theme {
                DownloadScreen()
            }
        }

        // Check if main components are displayed
        composeTestRule.onNodeWithText("YouTube Downloader").assertIsDisplayed()
        composeTestRule.onNodeWithText("Masukkan URL YouTube").assertIsDisplayed()
        composeTestRule.onNodeWithText("Pilih Format").assertIsDisplayed()
        composeTestRule.onNodeWithText("MP4 (Video)").assertIsDisplayed()
        composeTestRule.onNodeWithText("MP3 (Audio)").assertIsDisplayed()
    }

    @Test
    fun downloadScreen_urlInputWorks() {
        composeTestRule.setContent {
            YTMP3Theme {
                DownloadScreen()
            }
        }

        val testUrl = "https://www.youtube.com/watch?v=dQw4w9WgXcQ"
        
        // Find and interact with URL input field
        composeTestRule.onNodeWithText("Masukkan URL YouTube")
            .performTextInput(testUrl)

        // Verify text was entered
        composeTestRule.onNodeWithText(testUrl).assertIsDisplayed()
    }

    @Test
    fun downloadScreen_formatSelectionWorks() {
        composeTestRule.setContent {
            YTMP3Theme {
                DownloadScreen()
            }
        }

        // Initially MP4 should be selected
        composeTestRule.onNodeWithText("MP4 (Video)").assertIsSelected()
        composeTestRule.onNodeWithText("MP3 (Audio)").assertIsNotSelected()

        // Click MP3 format
        composeTestRule.onNodeWithText("MP3 (Audio)").performClick()

        // Now MP3 should be selected
        composeTestRule.onNodeWithText("MP3 (Audio)").assertIsSelected()
        composeTestRule.onNodeWithText("MP4 (Video)").assertIsNotSelected()
    }

    @Test
    fun downloadScreen_searchButtonEnabled_whenUrlNotEmpty() {
        composeTestRule.setContent {
            YTMP3Theme {
                DownloadScreen()
            }
        }

        // Initially search button should be disabled (URL is empty)
        composeTestRule.onNodeWithContentDescription("Cari Video").assertIsNotEnabled()

        // Enter URL
        composeTestRule.onNodeWithText("Masukkan URL YouTube")
            .performTextInput("https://www.youtube.com/watch?v=test")

        // Now search button should be enabled
        composeTestRule.onNodeWithContentDescription("Cari Video").assertIsEnabled()
    }

    @Test
    fun downloadScreen_displaysLoadingState() {
        val mockViewModel = mockk<DownloadViewModel>()
        val loadingUiState = com.irnhakim.ytmp3.viewmodel.DownloadUiState(
            url = "https://www.youtube.com/watch?v=test",
            isLoading = true
        )
        
        every { mockViewModel.uiState } returns MutableStateFlow(loadingUiState)
        every { mockViewModel.downloadState } returns MutableStateFlow(DownloadState.Idle)

        composeTestRule.setContent {
            YTMP3Theme {
                DownloadScreen(viewModel = mockViewModel)
            }
        }

        // Check if loading indicator is displayed
        composeTestRule.onNodeWithText("Mengambil informasi video...").assertIsDisplayed()
        composeTestRule.onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate)).assertIsDisplayed()
    }

    @Test
    fun downloadScreen_displaysErrorMessage() {
        val mockViewModel = mockk<DownloadViewModel>()
        val errorUiState = com.irnhakim.ytmp3.viewmodel.DownloadUiState(
            url = "invalid-url",
            errorMessage = "URL tidak valid"
        )
        
        every { mockViewModel.uiState } returns MutableStateFlow(errorUiState)
        every { mockViewModel.downloadState } returns MutableStateFlow(DownloadState.Idle)

        composeTestRule.setContent {
            YTMP3Theme {
                DownloadScreen(viewModel = mockViewModel)
            }
        }

        // Check if error message is displayed
        composeTestRule.onNodeWithText("URL tidak valid").assertIsDisplayed()
    }

    @Test
    fun downloadScreen_displaysVideoInfo() {
        val mockViewModel = mockk<DownloadViewModel>()
        val videoInfo = VideoInfo(
            id = "test123",
            title = "Test Video Title",
            duration = "3:45",
            thumbnail = "https://example.com/thumb.jpg",
            uploader = "Test Channel",
            url = "https://www.youtube.com/watch?v=test123",
            formats = emptyList()
        )
        
        val videoInfoUiState = com.irnhakim.ytmp3.viewmodel.DownloadUiState(
            url = "https://www.youtube.com/watch?v=test123",
            videoInfo = videoInfo,
            showVideoInfo = true
        )
        
        every { mockViewModel.uiState } returns MutableStateFlow(videoInfoUiState)
        every { mockViewModel.downloadState } returns MutableStateFlow(DownloadState.Idle)

        composeTestRule.setContent {
            YTMP3Theme {
                DownloadScreen(viewModel = mockViewModel)
            }
        }

        // Check if video info is displayed
        composeTestRule.onNodeWithText("Test Video Title").assertIsDisplayed()
        composeTestRule.onNodeWithText("Channel: Test Channel").assertIsDisplayed()
        composeTestRule.onNodeWithText("Durasi: 3:45").assertIsDisplayed()
        composeTestRule.onNodeWithText("📥 Download").assertIsDisplayed()
    }

    @Test
    fun downloadScreen_displaysDownloadProgress() {
        val mockViewModel = mockk<DownloadViewModel>()
        val progressState = DownloadState.Progress(progress = 50, status = "Downloading...")
        
        every { mockViewModel.uiState } returns MutableStateFlow(com.irnhakim.ytmp3.viewmodel.DownloadUiState())
        every { mockViewModel.downloadState } returns MutableStateFlow(progressState)

        composeTestRule.setContent {
            YTMP3Theme {
                DownloadScreen(viewModel = mockViewModel)
            }
        }

        // Check if download progress is displayed
        composeTestRule.onNodeWithText("Download Progress").assertIsDisplayed()
        composeTestRule.onNodeWithText("Downloading...").assertIsDisplayed()
        composeTestRule.onNodeWithText("50%").assertIsDisplayed()
    }

    @Test
    fun downloadScreen_displaysDownloadSuccess() {
        val mockViewModel = mockk<DownloadViewModel>()
        val successState = DownloadState.Success(
            filePath = "/storage/downloads/test.mp4",
            fileName = "test.mp4"
        )
        
        every { mockViewModel.uiState } returns MutableStateFlow(com.irnhakim.ytmp3.viewmodel.DownloadUiState())
        every { mockViewModel.downloadState } returns MutableStateFlow(successState)

        composeTestRule.setContent {
            YTMP3Theme {
                DownloadScreen(viewModel = mockViewModel)
            }
        }

        // Check if success message and actions are displayed
        composeTestRule.onNodeWithText("Download selesai").assertIsDisplayed()
        composeTestRule.onNodeWithText("File: test.mp4").assertIsDisplayed()
        composeTestRule.onNode(hasText("File tersimpan pada:", substring = true)).assertIsDisplayed()
        composeTestRule.onNodeWithText("Buka File").assertIsDisplayed()
        composeTestRule.onNodeWithText("Download Lagi").assertIsDisplayed()
    }

    @Test
    fun downloadScreen_displaysDownloadError() {
        val mockViewModel = mockk<DownloadViewModel>()
        val errorState = DownloadState.Error("Network error occurred")
        
        every { mockViewModel.uiState } returns MutableStateFlow(com.irnhakim.ytmp3.viewmodel.DownloadUiState())
        every { mockViewModel.downloadState } returns MutableStateFlow(errorState)

        composeTestRule.setContent {
            YTMP3Theme {
                DownloadScreen(viewModel = mockViewModel)
            }
        }

        // Check if error message is displayed
        composeTestRule.onNodeWithText("Download Gagal").assertIsDisplayed()
        composeTestRule.onNodeWithText("Network error occurred").assertIsDisplayed()
        composeTestRule.onNodeWithText("Coba Lagi").assertIsDisplayed()
    }
}
