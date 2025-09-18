# 🧪 Testing Guide

Panduan lengkap untuk testing Local YouTube Downloader Android - unit tests, integration tests, dan manual testing.

## 🎯 Overview

Testing strategy aplikasi ini menggunakan **Testing Pyramid** approach dengan fokus pada unit tests, integration tests, dan UI tests untuk memastikan kualitas dan reliability aplikasi.

## 🏗️ Testing Architecture

### Testing Pyramid

```
┌─────────────────────────────────────────┐
│              UI Tests                   │
│         (Instrumentation)               │
│    ┌─────────────────────────────┐      │
│    │ End-to-End Scenarios        │      │
│    │ User Journey Testing        │      │
│    └─────────────────────────────┘      │
├─────────────────────────────────────────┤
│           Integration Tests             │
│      ┌─────────────────────────────┐    │
│      │ Repository + Service        │    │
│      │ ViewModel + Repository      │    │
│      │ Component Integration       │    │
│      └─────────────────────────────┘    │
├─────────────────────────────────────────┤
│              Unit Tests                 │
│  ┌─────────────────────────────────┐    │
│  │ Individual Components           │    │
│  │ Business Logic                  │    │
│  │ Utility Functions               │    │
│  │ Error Handling                  │    │
│  └─────────────────────────────────┘    │
└─────────────────────────────────────────┘
```

### Test Structure

```
app/src/test/java/com/irnhakim/ytmp3/
├── unit/
│   ├── repository/
│   │   ├── DownloadRepositoryTest.kt
│   │   └── CrashSafeDownloadRepositoryTest.kt
│   ├── viewmodel/
│   │   ├── DownloadViewModelTest.kt
│   │   └── CrashSafeDownloadViewModelTest.kt
│   ├── service/
│   │   ├── YouTubeExtractorServiceTest.kt
│   │   └── CrashSafeYouTubeExtractorServiceTest.kt
│   └── utils/
│       └── FileUtilsTest.kt
├── integration/
│   ├── DownloadFlowIntegrationTest.kt
│   ├── ErrorHandlingIntegrationTest.kt
│   └── FileManagementIntegrationTest.kt
└── mock/
    ├── MockVideoInfo.kt
    ├── MockDownloadRequest.kt
    └── TestUtils.kt

app/src/androidTest/java/com/irnhakim/ytmp3/
├── ui/
│   ├── DownloadScreenTest.kt
│   ├── ErrorDisplayTest.kt
│   └── ProgressIndicatorTest.kt
├── integration/
│   ├── EndToEndDownloadTest.kt
│   ├── PermissionTest.kt
│   └── FileProviderTest.kt
└── performance/
    ├── MemoryLeakTest.kt
    └── PerformanceTest.kt
```

## 🔧 Unit Testing

### Test Configuration

```kotlin
// app/build.gradle.kts
dependencies {
    // Unit testing
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.mockito:mockito-core:5.1.1")
    testImplementation("org.mockito.kotlin:mockito-kotlin:4.1.0")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    testImplementation("app.cash.turbine:turbine:1.0.0")
    testImplementation("io.mockk:mockk:1.13.4")
    
    // Android testing
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    androidTestImplementation("androidx.test:runner:1.5.2")
    androidTestImplementation("androidx.test:rules:1.5.0")
}

android {
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            isReturnDefaultValues = true
        }
    }
}
```

### Repository Testing

```kotlin
class DownloadRepositoryTest {
    
    @Mock
    private lateinit var mockContext: Context
    
    @Mock
    private lateinit var mockYouTubeExtractor: YouTubeExtractorService
    
    private lateinit var repository: DownloadRepository
    
    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        repository = DownloadRepository(mockContext)
        
        // Inject mock extractor via reflection or dependency injection
        val extractorField = DownloadRepository::class.java.getDeclaredField("youtubeExtractor")
        extractorField.isAccessible = true
        extractorField.set(repository, mockYouTubeExtractor)
    }
    
    @Test
    fun `getVideoInfo returns success for valid URL`() = runTest {
        // Given
        val validUrl = "https://youtube.com/watch?v=dQw4w9WgXcQ"
        val expectedVideoInfo = TestUtils.createMockVideoInfo()
        
        whenever(mockYouTubeExtractor.extractVideoInfo(validUrl))
            .thenReturn(Result.success(expectedVideoInfo))
        
        // When
        val result = repository.getVideoInfo(validUrl)
        
        // Then
        assertTrue(result.isSuccess)
        assertEquals(expectedVideoInfo.id, result.getOrNull()?.id)
        verify(mockYouTubeExtractor).extractVideoInfo(validUrl)
    }
    
    @Test
    fun `getVideoInfo returns failure for invalid URL`() = runTest {
        // Given
        val invalidUrl = "invalid-url"
        
        // When
        val result = repository.getVideoInfo(invalidUrl)
        
        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("tidak valid") == true)
        verify(mockYouTubeExtractor, never()).extractVideoInfo(any())
    }
    
    @Test
    fun `downloadVideo emits correct state sequence`() = runTest {
        // Given
        val request = DownloadRequest("https://youtube.com/watch?v=test", DownloadFormat.MP4)
        val mockVideoInfo = TestUtils.createMockVideoInfo()
        
        whenever(mockYouTubeExtractor.extractVideoInfo(any()))
            .thenReturn(Result.success(mockVideoInfo))
        whenever(mockYouTubeExtractor.downloadVideo(any(), any(), any(), any()))
            .thenReturn(Result.success("/path/to/file.mp4"))
        
        // When & Then
        repository.downloadVideo(request).test {
            assertEquals(DownloadState.Loading, awaitItem())
            
            val progressItem = awaitItem()
            assertTrue(progressItem is DownloadState.Progress)
            assertEquals(5, (progressItem as DownloadState.Progress).percentage)
            
            val successItem = awaitItem()
            assertTrue(successItem is DownloadState.Success)
            assertEquals("file.mp4", (successItem as DownloadState.Success).fileName)
            
            awaitComplete()
        }
    }
    
    @Test
    fun `downloadVideo handles network error gracefully`() = runTest {
        // Given
        val request = DownloadRequest("https://youtube.com/watch?v=test", DownloadFormat.MP4)
        
        whenever(mockYouTubeExtractor.extractVideoInfo(any()))
            .thenReturn(Result.failure(Exception("Network error")))
        
        // When & Then
        repository.downloadVideo(request).test {
            assertEquals(DownloadState.Loading, awaitItem())
            
            val errorItem = awaitItem()
            assertTrue(errorItem is DownloadState.Error)
            assertTrue((errorItem as DownloadState.Error).message.contains("Network error"))
            
            awaitComplete()
        }
    }
}
```

### ViewModel Testing

```kotlin
class DownloadViewModelTest {
    
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()
    
    @Mock
    private lateinit var mockApplication: Application
    
    @Mock
    private lateinit var mockRepository: DownloadRepository
    
    private lateinit var viewModel: DownloadViewModel
    
    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        viewModel = DownloadViewModel(mockApplication)
        
        // Inject mock repository
        val repositoryField = DownloadViewModel::class.java.getDeclaredField("repository")
        repositoryField.isAccessible = true
        repositoryField.set(viewModel, mockRepository)
    }
    
    @Test
    fun `updateUrl updates UI state correctly`() = runTest {
        // Given
        val testUrl = "https://youtube.com/watch?v=test"
        
        // When
        viewModel.updateUrl(testUrl)
        
        // Then
        assertEquals(testUrl, viewModel.uiState.value.url)
        assertNull(viewModel.uiState.value.errorMessage)
    }
    
    @Test
    fun `getVideoInfo updates state on success`() = runTest {
        // Given
        val testUrl = "https://youtube.com/watch?v=test"
        val mockVideoInfo = TestUtils.createMockVideoInfo()
        
        viewModel.updateUrl(testUrl)
        whenever(mockRepository.getVideoInfo(testUrl))
            .thenReturn(Result.success(mockVideoInfo))
        
        // When
        viewModel.getVideoInfo()
        
        // Then
        viewModel.uiState.test {
            val finalState = expectMostRecentItem()
            assertFalse(finalState.isLoading)
            assertEquals(mockVideoInfo, finalState.videoInfo)
            assertTrue(finalState.showVideoInfo)
            assertNull(finalState.errorMessage)
        }
    }
    
    @Test
    fun `startDownload triggers repository call`() = runTest {
        // Given
        val testUrl = "https://youtube.com/watch?v=test"
        val mockVideoInfo = TestUtils.createMockVideoInfo()
        val request = DownloadRequest(testUrl, DownloadFormat.MP4)
        
        viewModel.updateUrl(testUrl)
        viewModel.uiState.value = viewModel.uiState.value.copy(
            videoInfo = mockVideoInfo,
            showVideoInfo = true
        )
        
        val downloadFlow = flowOf(
            DownloadState.Loading,
            DownloadState.Progress(50, "Downloading..."),
            DownloadState.Success("/path/to/file.mp4", "file.mp4")
        )
        
        whenever(mockRepository.downloadVideo(any<DownloadRequest>(), any()))
            .thenReturn(downloadFlow)
        
        // When
        viewModel.startDownload()
        
        // Then
        verify(mockRepository).downloadVideo(any<DownloadRequest>(), eq(mockVideoInfo))
        
        viewModel.downloadState.test {
            assertEquals(DownloadState.Loading, awaitItem())
            assertEquals(DownloadState.Progress(50, "Downloading..."), awaitItem())
            assertEquals(DownloadState.Success("/path/to/file.mp4", "file.mp4"), awaitItem())
        }
    }
}
```

### Service Testing

```kotlin
class YouTubeExtractorServiceTest {
    
    @Mock
    private lateinit var mockContext: Context
    
    private lateinit var service: YouTubeExtractorService
    
    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        service = YouTubeExtractorService(mockContext)
    }
    
    @Test
    fun `initializeYoutubeDL returns success in test environment`() = runTest {
        // When
        val result = service.initializeYoutubeDL()
        
        // Then
        assertTrue(result.isSuccess)
    }
    
    @Test
    fun `extractVideoInfo returns mock data in test environment`() = runTest {
        // Given
        val testUrl = "https://youtube.com/watch?v=dQw4w9WgXcQ"
        
        // When
        val result = service.extractVideoInfo(testUrl)
        
        // Then
        assertTrue(result.isSuccess)
        val videoInfo = result.getOrNull()!!
        assertEquals("dQw4w9WgXcQ", videoInfo.id)
        assertEquals("Test Video Title", videoInfo.title)
    }
    
    @Test
    fun `downloadVideo simulates progress in test environment`() = runTest {
        // Given
        val testUrl = "https://youtube.com/watch?v=test"
        val outputDir = File.createTempFile("test", "dir").apply { 
            delete()
            mkdirs() 
        }
        val progressUpdates = mutableListOf<Float>()
        
        // When
        val result = service.downloadVideo(
            url = testUrl,
            outputDir = outputDir,
            onProgress = { progress, _, _ -> progressUpdates.add(progress) }
        )
        
        // Then
        assertTrue(result.isSuccess)
        assertTrue(progressUpdates.contains(0f))
        assertTrue(progressUpdates.contains(25f))
        assertTrue(progressUpdates.contains(50f))
        assertTrue(progressUpdates.contains(75f))
        assertTrue(progressUpdates.contains(100f))
        
        // Cleanup
        outputDir.deleteRecursively()
    }
}
```

### Utility Testing

```kotlin
class FileUtilsTest {
    
    @Test
    fun `isValidYouTubeUrl validates URLs correctly`() {
        // Valid URLs
        assertTrue(FileUtils.isValidYouTubeUrl("https://www.youtube.com/watch?v=dQw4w9WgXcQ"))
        assertTrue(FileUtils.isValidYouTubeUrl("https://youtu.be/dQw4w9WgXcQ"))
        assertTrue(FileUtils.isValidYouTubeUrl("https://youtube.com/embed/dQw4w9WgXcQ"))
        assertTrue(FileUtils.isValidYouTubeUrl("https://m.youtube.com/watch?v=dQw4w9WgXcQ"))
        
        // Invalid URLs
        assertFalse(FileUtils.isValidYouTubeUrl("https://youtube.com/playlist?list=123"))
        assertFalse(FileUtils.isValidYouTubeUrl("https://youtube.com/channel/UC123"))
        assertFalse(FileUtils.isValidYouTubeUrl("https://google.com"))
        assertFalse(FileUtils.isValidYouTubeUrl("invalid-url"))
    }
    
    @Test
    fun `generateFileName creates valid filename`() {
        // Given
        val title = "Amazing Video [HD] - Part 1"
        val extension = "mp4"
        
        // When
        val result = FileUtils.generateFileName(title, extension)
        
        // Then
        assertTrue(result.endsWith(".mp4"))
        assertTrue(result.contains("Amazing_Video_HD_Part_1"))
        assertFalse(result.contains("["))
        assertFalse(result.contains("]"))
        assertFalse(result.contains("-"))
        assertTrue(result.matches(Regex(".*_\\d{8}_\\d{6}\\.mp4")))
    }
    
    @Test
    fun `getMimeType returns correct MIME types`() {
        assertEquals("video/mp4", FileUtils.getMimeType("video.mp4"))
        assertEquals("audio/mpeg", FileUtils.getMimeType("audio.mp3"))
        assertEquals("video/webm", FileUtils.getMimeType("video.webm"))
        assertEquals("audio/mp4", FileUtils.getMimeType("audio.m4a"))
        assertEquals("*/*", FileUtils.getMimeType("unknown.xyz"))
    }
    
    @Test
    fun `getFileSize formats sizes correctly`() {
        assertEquals("1.0 KB", FileUtils.formatFileSize(1024))
        assertEquals("1.0 MB", FileUtils.formatFileSize(1024 * 1024))
        assertEquals("1.0 GB", FileUtils.formatFileSize(1024 * 1024 * 1024))
        assertEquals("500 B", FileUtils.formatFileSize(500))
    }
    
    @Test
    fun `extractVideoId extracts IDs correctly`() {
        assertEquals("dQw4w9WgXcQ", FileUtils.extractVideoId("https://www.youtube.com/watch?v=dQw4w9WgXcQ"))
        assertEquals("dQw4w9WgXcQ", FileUtils.extractVideoId("https://youtu.be/dQw4w9WgXcQ"))
        assertEquals("dQw4w9WgXcQ", FileUtils.extractVideoId("https://youtube.com/embed/dQw4w9WgXcQ"))
        assertNull(FileUtils.extractVideoId("invalid-url"))
    }
}
```

## 🔗 Integration Testing

### Repository-Service Integration

```kotlin
class DownloadFlowIntegrationTest {
    
    private lateinit var context: Context
    private lateinit var repository: DownloadRepository
    private lateinit var tempDir: File
    
    @Before
    fun setup() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        repository = DownloadRepository(context)
        tempDir = File.createTempFile("test", "dir").apply {
            delete()
            mkdirs()
        }
    }
    
    @After
    fun cleanup() {
        tempDir.deleteRecursively()
    }
    
    @Test
    fun `complete download flow works end to end`() = runTest {
        // Given
        val request = DownloadRequest(
            url = "https://youtube.com/watch?v=dQw4w9WgXcQ",
            format = DownloadFormat.MP4
        )
        
        val states = mutableListOf<DownloadState>()
        
        // When
        repository.downloadVideo(request).collect { state ->
            states.add(state)
        }
        
        // Then
        assertTrue(states.any { it is DownloadState.Loading })
        assertTrue(states.any { it is DownloadState.Progress })
        assertTrue(states.any { it is DownloadState.Success })
        
        val successState = states.filterIsInstance<DownloadState.Success>().first()
        assertTrue(File(successState.filePath).exists())
        assertTrue(successState.fileName.isNotEmpty())
    }
    
    @Test
    fun `error handling works across layers`() = runTest {
        // Given
        val request = DownloadRequest(
            url = "invalid-url",
            format = DownloadFormat.MP4
        )
        
        val states = mutableListOf<DownloadState>()
        
        // When
        repository.downloadVideo(request).collect { state ->
            states.add(state)
        }
        
        // Then
        assertTrue(states.any { it is DownloadState.Error })
        val errorState = states.filterIsInstance<DownloadState.Error>().first()
        assertTrue(errorState.message.contains("tidak valid"))
    }
}
```

### ViewModel-Repository Integration

```kotlin
class ViewModelRepositoryIntegrationTest {
    
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()
    
    private lateinit var viewModel: DownloadViewModel
    private lateinit var mockApplication: Application
    
    @Before
    fun setup() {
        mockApplication = mock()
        viewModel = DownloadViewModel(mockApplication)
    }
    
    @Test
    fun `complete user flow from URL input to download completion`() = runTest {
        // Given
        val testUrl = "https://youtube.com/watch?v=dQw4w9WgXcQ"
        
        // When - User enters URL
        viewModel.updateUrl(testUrl)
        
        // Then - URL is updated
        assertEquals(testUrl, viewModel.uiState.value.url)
        
        // When - User gets video info
        viewModel.getVideoInfo()
        
        // Then - Video info is loaded
        viewModel.uiState.test {
            val state = expectMostRecentItem()
            assertNotNull(state.videoInfo)
            assertTrue(state.showVideoInfo)
        }
        
        // When - User starts download
        viewModel.startDownload()
        
        // Then - Download progresses to completion
        viewModel.downloadState.test {
            val states = mutableListOf<DownloadState>()
            repeat(10) { // Collect multiple states
                states.add(awaitItem())
            }
            
            assertTrue(states.any { it is DownloadState.Loading })
            assertTrue(states.any { it is DownloadState.Progress })
            assertTrue(states.any { it is DownloadState.Success })
        }
    }
}
```

## 📱 UI Testing (Instrumentation)

### Screen Testing

```kotlin
@RunWith(AndroidJUnit4::class)
class DownloadScreenTest {
    
    @get:Rule
    val composeTestRule = createComposeRule()
    
    @Test
    fun downloadScreen_displaysInitialState() {
        composeTestRule.setContent {
            DownloadScreen()
        }
        
        composeTestRule.onNodeWithText("YouTube Downloader").assertIsDisplayed()
        composeTestRule.onNodeWithText("Masukkan URL YouTube").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Cari Video").assertIsDisplayed()
    }
    
    @Test
    fun downloadScreen_urlInputAndSearch() {
        composeTestRule.setContent {
            DownloadScreen()
        }
        
        val testUrl = "https://youtube.com/watch?v=dQw4w9WgXcQ"
        
        // Input URL
        composeTestRule.onNodeWithText("Masukkan URL YouTube")
            .performTextInput(testUrl)
        
        // Click search
        composeTestRule.onNodeWithContentDescription("Cari Video")
            .performClick()
        
        // Verify loading state
        composeTestRule.onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate))
            .assertIsDisplayed()
    }
    
    @Test
    fun downloadScreen_showsVideoInfoAfterSearch() {
        composeTestRule.setContent {
            DownloadScreen()
        }
        
        val testUrl = "https://youtube.com/watch?v=dQw4w9WgXcQ"
        
        // Input URL and search
        composeTestRule.onNodeWithText("Masukkan URL YouTube")
            .performTextInput(testUrl)
        composeTestRule.onNodeWithContentDescription("Cari Video")
            .performClick()
        
        // Wait for video info to load
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodesWithText("Test Video Title").fetchSemanticsNodes().isNotEmpty()
        }
        
        // Verify video info is displayed
        composeTestRule.onNodeWithText("Test Video Title").assertIsDisplayed()
        composeTestRule.onNodeWithText("Test Channel").assertIsDisplayed()
        composeTestRule.onNodeWithText("Download").assertIsDisplayed()
    }
    
    @Test
    fun downloadScreen_downloadProgressAndCompletion() {
        composeTestRule.setContent {
            DownloadScreen()
        }
        
        // Setup and start download
        val testUrl = "https://youtube.com/watch?v=dQw4w9WgXcQ"
        composeTestRule.onNodeWithText("Masukkan URL YouTube")
            .performTextInput(testUrl)
        composeTestRule.onNodeWithContentDescription("Cari Video")
            .performClick()
        
        // Wait for video info and start download
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodesWithText("Download").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("Download").performClick()
        
        // Verify progress is shown
        composeTestRule.onNodeWithText("Download Progress").assertIsDisplayed()
        
        // Wait for completion
        composeTestRule.waitUntil(timeoutMillis = 10000) {
            composeTestRule.onAllNodesWithText("Download Berhasil!").fetchSemanticsNodes().isNotEmpty()
        }
        
        // Verify success state
        composeTestRule.onNodeWithText("Download Berhasil!").assertIsDisplayed()
        composeTestRule.onNodeWithText("Buka File").assertIsDisplayed()
        composeTestRule.onNodeWithText("Download Lagi").assertIsDisplayed()
    }
}
```

### End-to-End Testing

```kotlin
@RunWith(AndroidJUnit4::class)
class EndToEndDownloadTest {
    
    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)
    
    @Test
    fun completeDownloadJourney_MP4() {
        // Launch app
        onView(withText("YouTube Downloader")).check(matches(isDisplayed()))
        
        // Input URL
        onView(withHint("Masukkan URL YouTube"))
            .perform(typeText("https://youtube.com/watch?v=dQw4w9WgXcQ"))
        
        // Search for video
        onView(withContentDescription("Cari Video"))
            .perform(click())
        
        // Wait for video info
        onView(withText("Test Video Title"))
            .check(matches(isDisplayed()))
        
        // Select MP4 format (default)
        onView(withText("MP4")).check(matches(isSelected()))
        
        // Start download
        onView(withText("Download")).perform(click())
        
        // Wait for download completion
        onView(withText("Download Berhasil!"))
            .check(matches(isDisplayed()))
        
        // Verify file info
        onView(withText(containsString("test_video")))
            .check(matches(isDisplayed()))
        
        // Test open file
        onView(withText("Buka File")).perform(click())
        
        // Verify intent was sent (file opening)
        intended(hasAction(Intent.ACTION_VIEW))
    }
    
    @Test
    fun completeDownloadJourney_MP3() {
        // Similar to MP4 test but select MP3 format
        onView(withHint("Masukkan URL YouTube"))
            .perform(typeText("https://youtube.com/watch?v=dQw4w9WgXcQ"))
        
        onView(withContentDescription("Cari Video")).perform(click())
        
        onView(withText("Test Video Title"))
            .check(matches(isDisplayed()))
        
        // Select MP3 format
        onView(withText("MP3")).perform(click())
        
        onView(withText("Download")).perform(click())
        
        onView(withText("Download Berhasil!"))
            .check(matches(isDisplayed()))
        
        // Verify audio file
        onView(withText(containsString(".mp3")))
            .check(matches(isDisplayed()))
    }
}
```

## 🚀 Performance Testing

### Memory Leak Testing

```kotlin
@RunWith(AndroidJUnit4::class)
class MemoryLeakTest {
    
    @Test
    fun downloadViewModel_doesNotLeakMemory() {
        val scenario = ActivityScenario.launch(MainActivity::class.java)
        
        scenario.use {
            // Perform multiple download operations
            repeat(5) {
                onView(withHint("Masukkan URL YouTube"))
                    .perform(clearText(), typeText("https://youtube.com/watch?v=test$it"))
                
                onView(withContentDescription("Cari Video")).perform(click())
                
                // Wait and reset
                Thread.sleep(1000)
                onView(withText("Download Lagi")).perform(click())
            }
            
            // Force garbage collection
            System.gc()
            Thread.sleep(1000)
            
            // Memory should be stable (this is a basic check)
            val runtime = Runtime.getRuntime()
            val usedMemory = runtime.totalMemory() - runtime.freeMemory()
            val maxMemory = runtime.maxMemory()
            
            assertTrue("Memory usage too high", usedMemory < maxMemory * 0.8)
        }
    }
}
```

### Performance Benchmarking

```kotlin
@RunWith(AndroidJUnit4::class)
class PerformanceTest {
    
    @Test
    fun urlValidation_performance() {
        val urls = listOf(
            "https://youtube.com/watch?v=dQw4w9WgXcQ",
            "https://youtu.be/dQw4w9WgXcQ",
            "invalid-url",
            "https://youtube.com/playlist?list=123"
        )
        
        val startTime = System.nanoTime()
        
        repeat(1000) {
            urls.forEach { url ->
                FileUtils.isValidYouTubeUrl(url)
            }
        }
        
        val endTime = System.nanoTime()
        val durationMs = (endTime - startTime) / 1_000_000
        
        // Should complete within reasonable time
        assertTrue("URL validation too slow: ${durationMs}ms", durationMs < 100)
    }
}
```

## 🎯 Test Utilities

### Mock Data Creation

```kotlin
object TestUtils {
    
    fun createMockVideoInfo(
        id: String = "dQw4w9WgXcQ",
        title: String = "Test Video Title",
        duration: String = "3:32",
        uploader: String = "Test Channel"
    ): VideoInfo {
        return VideoInfo(
            id = id,
            title = title,
            duration = duration,
            thumbnail = "https://i.ytimg.com/vi/$id/maxresdefault.jpg",
            uploader = uploader,
            url = "https://youtube.com/watch?v=$id",
            formats = createMockFormats()
        )
    }
    
    fun createMockFormats(): List<VideoFormat> {
        return listOf(
            VideoFormat(
                formatId = "18",
                ext = "mp4",
                quality = "360p",
                filesize = 10_000_000L,
                url = "https://example.com/video.mp4",
                acodec = "aac",
                vcodec = "h264"
            ),
            VideoFormat(
                formatId = "140",
                ext = "m4a",
                quality = "128kbps",
                filesize = 3_000_000L,
                url = "https://example.com/audio.m4a",
                acodec = "aac",
                vcodec = "none"
            )
        )
    }
    
    fun createMockDownloadRequest(
        url: String = "https://youtube.com/watch?v=dQw4w9WgXcQ",
        format
