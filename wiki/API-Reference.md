# 📚 API Reference

Dokumentasi lengkap API dan komponen untuk Local YouTube Downloader Android.

## 📋 Table of Contents

- [Core Components](#core-components)
- [Data Models](#data-models)
- [Repository Layer](#repository-layer)
- [Service Layer](#service-layer)
- [ViewModel Layer](#viewmodel-layer)
- [Utility Classes](#utility-classes)
- [UI Components](#ui-components)

## 🧩 Core Components

### DownloadRepository

Main repository class untuk mengelola download operations.

```kotlin
class DownloadRepository(private val context: Context)
```

#### Methods

##### getVideoInfo
```kotlin
suspend fun getVideoInfo(url: String): Result<VideoInfo>
```
**Description**: Mengekstrak informasi video dari URL YouTube.

**Parameters**:
- `url: String` - YouTube video URL

**Returns**: `Result<VideoInfo>` - Video information atau error

**Example**:
```kotlin
val repository = DownloadRepository(context)
val result = repository.getVideoInfo("https://youtube.com/watch?v=dQw4w9WgXcQ")
result.fold(
    onSuccess = { videoInfo -> 
        println("Title: ${videoInfo.title}")
    },
    onFailure = { error -> 
        println("Error: ${error.message}")
    }
)
```

##### downloadVideo
```kotlin
fun downloadVideo(request: DownloadRequest): Flow<DownloadState>
```
**Description**: Memulai download video dengan progress tracking.

**Parameters**:
- `request: DownloadRequest` - Download request configuration

**Returns**: `Flow<DownloadState>` - Stream of download states

**Example**:
```kotlin
val request = DownloadRequest(
    url = "https://youtube.com/watch?v=dQw4w9WgXcQ",
    format = DownloadFormat.MP4
)

repository.downloadVideo(request).collect { state ->
    when (state) {
        is DownloadState.Progress -> {
            println("Progress: ${state.percentage}%")
        }
        is DownloadState.Success -> {
            println("Downloaded: ${state.filePath}")
        }
        is DownloadState.Error -> {
            println("Error: ${state.message}")
        }
    }
}
```

##### getDownloadedFiles
```kotlin
fun getDownloadedFiles(): List<File>
```
**Description**: Mendapatkan daftar file yang sudah didownload.

**Returns**: `List<File>` - List of downloaded files

---

### YouTubeExtractorService

Service class untuk integrasi dengan yt-dlp dan FFmpeg.

```kotlin
class YouTubeExtractorService(private val context: Context)
```

#### Methods

##### initializeYoutubeDL
```kotlin
suspend fun initializeYoutubeDL(): Result<Unit>
```
**Description**: Inisialisasi yt-dlp dan FFmpeg libraries.

**Returns**: `Result<Unit>` - Success atau error

##### extractVideoInfo
```kotlin
suspend fun extractVideoInfo(url: String): Result<VideoInfo>
```
**Description**: Ekstrak informasi video menggunakan yt-dlp.

**Parameters**:
- `url: String` - YouTube video URL

**Returns**: `Result<VideoInfo>` - Video information

##### downloadVideo
```kotlin
suspend fun downloadVideo(
    url: String,
    outputDir: File,
    format: String = "best",
    onProgress: (Float, Long, String) -> Unit
): Result<String>
```
**Description**: Download video dengan format tertentu.

**Parameters**:
- `url: String` - YouTube video URL
- `outputDir: File` - Output directory
- `format: String` - Format selector (default: "best")
- `onProgress: (Float, Long, String) -> Unit` - Progress callback

**Returns**: `Result<String>` - File path atau error

##### downloadAudio
```kotlin
suspend fun downloadAudio(
    url: String,
    outputDir: File,
    onProgress: (Float, Long, String) -> Unit
): Result<String>
```
**Description**: Download dan ekstrak audio dari video.

**Parameters**:
- `url: String` - YouTube video URL
- `outputDir: File` - Output directory
- `onProgress: (Float, Long, String) -> Unit` - Progress callback

**Returns**: `Result<String>` - File path atau error

---

## 📊 Data Models

### VideoInfo
```kotlin
data class VideoInfo(
    val id: String,
    val title: String,
    val duration: String,
    val thumbnail: String,
    val uploader: String,
    val url: String,
    val formats: List<VideoFormat>
)
```

**Properties**:
- `id: String` - YouTube video ID
- `title: String` - Video title
- `duration: String` - Formatted duration (e.g., "3:45")
- `thumbnail: String` - Thumbnail image URL
- `uploader: String` - Channel name
- `url: String` - Original video URL
- `formats: List<VideoFormat>` - Available formats

### VideoFormat
```kotlin
data class VideoFormat(
    val formatId: String,
    val ext: String,
    val quality: String,
    val filesize: Long?,
    val url: String,
    val acodec: String?,
    val vcodec: String?
)
```

**Properties**:
- `formatId: String` - Format identifier
- `ext: String` - File extension
- `quality: String` - Quality description
- `filesize: Long?` - File size in bytes (nullable)
- `url: String` - Direct download URL
- `acodec: String?` - Audio codec (nullable)
- `vcodec: String?` - Video codec (nullable)

### DownloadRequest
```kotlin
data class DownloadRequest(
    val url: String,
    val format: DownloadFormat
)
```

**Properties**:
- `url: String` - YouTube video URL
- `format: DownloadFormat` - Desired output format

### DownloadFormat
```kotlin
enum class DownloadFormat {
    MP4, MP3
}
```

**Values**:
- `MP4` - Video format (video + audio)
- `MP3` - Audio format (audio only)

### DownloadState
```kotlin
sealed class DownloadState {
    object Idle : DownloadState()
    object Loading : DownloadState()
    data class Progress(val percentage: Int, val status: String) : DownloadState()
    data class Success(val filePath: String, val fileName: String) : DownloadState()
    data class Error(val message: String) : DownloadState()
}
```

**States**:
- `Idle` - No download in progress
- `Loading` - Initializing download
- `Progress(percentage, status)` - Download in progress
- `Success(filePath, fileName)` - Download completed
- `Error(message)` - Download failed

---

## 🧠 ViewModel Layer

### DownloadViewModel
```kotlin
class DownloadViewModel(application: Application) : AndroidViewModel(application)
```

#### Properties

##### uiState
```kotlin
val uiState: StateFlow<DownloadUiState>
```
**Description**: Current UI state observable.

##### downloadState
```kotlin
val downloadState: StateFlow<DownloadState>
```
**Description**: Current download state observable.

#### Methods

##### updateUrl
```kotlin
fun updateUrl(url: String)
```
**Description**: Update URL input.

**Parameters**:
- `url: String` - New URL value

##### updateFormat
```kotlin
fun updateFormat(format: DownloadFormat)
```
**Description**: Update selected format.

**Parameters**:
- `format: DownloadFormat` - New format selection

##### getVideoInfo
```kotlin
fun getVideoInfo()
```
**Description**: Fetch video information for current URL.

##### startDownload
```kotlin
fun startDownload()
```
**Description**: Start download process.

##### resetDownload
```kotlin
fun resetDownload()
```
**Description**: Reset download state to idle.

##### clearError
```kotlin
fun clearError()
```
**Description**: Clear error message from UI state.

### DownloadUiState
```kotlin
data class DownloadUiState(
    val url: String = "",
    val selectedFormat: DownloadFormat = DownloadFormat.MP4,
    val isLoading: Boolean = false,
    val videoInfo: VideoInfo? = null,
    val showVideoInfo: Boolean = false,
    val errorMessage: String? = null
)
```

---

## 🛠️ Utility Classes

### FileUtils
```kotlin
object FileUtils
```

#### Methods

##### getDownloadDirectory
```kotlin
fun getDownloadDirectory(context: Context): File
```
**Description**: Get download directory path.

**Parameters**:
- `context: Context` - Android context

**Returns**: `File` - Download directory

##### generateFileName
```kotlin
fun generateFileName(title: String, extension: String): String
```
**Description**: Generate unique filename.

**Parameters**:
- `title: String` - Video title
- `extension: String` - File extension

**Returns**: `String` - Generated filename

**Example**:
```kotlin
val filename = FileUtils.generateFileName("Amazing Video", "mp4")
// Result: "Amazing_Video_20240101_143022.mp4"
```

##### getFileSize
```kotlin
fun getFileSize(file: File): String
```
**Description**: Get human-readable file size.

**Parameters**:
- `file: File` - File object

**Returns**: `String` - Formatted file size

**Example**:
```kotlin
val size = FileUtils.getFileSize(File("/path/to/video.mp4"))
// Result: "15.2 MB"
```

##### deleteFile
```kotlin
fun deleteFile(filePath: String): Boolean
```
**Description**: Delete file safely.

**Parameters**:
- `filePath: String` - Path to file

**Returns**: `Boolean` - Success status

##### isValidYouTubeUrl
```kotlin
fun isValidYouTubeUrl(url: String): Boolean
```
**Description**: Validate YouTube URL format.

**Parameters**:
- `url: String` - URL to validate

**Returns**: `Boolean` - Validation result

**Supported Formats**:
```kotlin
// Valid formats
"https://www.youtube.com/watch?v=VIDEO_ID"
"https://youtu.be/VIDEO_ID"
"https://youtube.com/embed/VIDEO_ID"
"https://m.youtube.com/watch?v=VIDEO_ID"
```

##### extractVideoId
```kotlin
fun extractVideoId(url: String): String?
```
**Description**: Extract video ID from YouTube URL.

**Parameters**:
- `url: String` - YouTube URL

**Returns**: `String?` - Video ID atau null

##### getMimeType
```kotlin
fun getMimeType(filePath: String): String
```
**Description**: Get MIME type for file.

**Parameters**:
- `filePath: String` - File path

**Returns**: `String` - MIME type

##### getContentUri
```kotlin
fun getContentUri(context: Context, file: File): Uri
```
**Description**: Get content URI using FileProvider.

**Parameters**:
- `context: Context` - Android context
- `file: File` - File object

**Returns**: `Uri` - Content URI

##### openFile
```kotlin
fun openFile(context: Context, filePath: String): Boolean
```
**Description**: Open file with default application.

**Parameters**:
- `context: Context` - Android context
- `filePath: String` - Path to file

**Returns**: `Boolean` - Success status

---

## 🎨 UI Components

### DownloadScreen
```kotlin
@Composable
fun DownloadScreen(
    modifier: Modifier = Modifier,
    viewModel: DownloadViewModel = viewModel()
)
```
**Description**: Main download screen composable.

**Parameters**:
- `modifier: Modifier` - Compose modifier
- `viewModel: DownloadViewModel` - ViewModel instance

### VideoInfoCard
```kotlin
@Composable
private fun VideoInfoCard(
    videoInfo: VideoInfo,
    selectedFormat: DownloadFormat,
    onFormatChange: (DownloadFormat) -> Unit,
    onDownload: () -> Unit
)
```
**Description**: Video information display card.

### DownloadProgressCard
```kotlin
@Composable
private fun DownloadProgressCard(
    status: String,
    progress: Int
)
```
**Description**: Download progress indicator card.

### DownloadSuccessCard
```kotlin
@Composable
private fun DownloadSuccessCard(
    fileName: String,
    filePath: String,
    onReset: () -> Unit,
    onOpenFile: () -> Unit
)
```
**Description**: Download success notification card.

---

## 🔧 Configuration

### Build Configuration
```kotlin
// app/build.gradle.kts
android {
    compileSdk = 34
    
    defaultConfig {
        applicationId = "com.irnhakim.ytmp3"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }
}
```

### Dependencies
```kotlin
dependencies {
    // Jetpack Compose
    implementation(platform("androidx.compose:compose-bom:2023.10.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    
    // ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    
    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    
    // YouTube Downloader
    implementation("com.github.yausername.youtubedl-android:library:0.14.+")
    implementation("com.github.yausername.youtubedl-android:ffmpeg:0.14.+")
}
```

---

## 🧪 Testing APIs

### Test Utilities
```kotlin
// Test helper functions
object TestUtils {
    fun createMockVideoInfo(): VideoInfo
    fun createMockDownloadRequest(): DownloadRequest
    fun mockProgressCallback(): (Float, Long, String) -> Unit
}
```

### Repository Testing
```kotlin
class DownloadRepositoryTest {
    @Test
    fun `getVideoInfo returns success for valid URL`()
    
    @Test
    fun `downloadVideo emits progress states`()
    
    @Test
    fun `downloadVideo handles errors gracefully`()
}
```

### ViewModel Testing
```kotlin
class DownloadViewModelTest {
    @Test
    fun `updateUrl updates UI state`()
    
    @Test
    fun `startDownload triggers repository call`()
    
    @Test
    fun `error state is handled correctly`()
}
```

---

## 📝 Usage Examples

### Basic Download Flow
```kotlin
// 1. Create repository
val repository = DownloadRepository(context)

// 2. Get video info
val videoInfo = repository.getVideoInfo(url).getOrNull()

// 3. Create download request
val request = DownloadRequest(url, DownloadFormat.MP4)

// 4. Start download
repository.downloadVideo(request).collect { state ->
    when (state) {
        is DownloadState.Progress -> updateProgress(state.percentage)
        is DownloadState.Success -> showSuccess(state.filePath)
        is DownloadState.Error -> showError(state.message)
    }
}
```

### File Operations
```kotlin
// Get download directory
val downloadDir = FileUtils.getDownloadDirectory(context)

// Validate URL
if (FileUtils.isValidYouTubeUrl(url)) {
    // Process URL
}

// Open downloaded file
FileUtils.openFile(context, filePath)
```

### ViewModel Integration
```kotlin
// In Composable
val viewModel: DownloadViewModel = viewModel()
val uiState by viewModel.uiState.collectAsState()
val downloadState by viewModel.downloadState.collectAsState()

// Update URL
viewModel.updateUrl("https://youtube.com/watch?v=dQw4w9WgXcQ")

// Start download
viewModel.startDownload()
```

---

**API Reference** 📚 Complete documentation of all public APIs and components.
