# 🚀 Download Engine

Dokumentasi lengkap tentang cara kerja download engine Local YouTube Downloader Android.

## 🎯 Overview

Download engine adalah inti dari aplikasi yang menangani ekstraksi informasi video YouTube dan proses download. Engine ini dibangun di atas **yt-dlp** dan **FFmpeg** untuk memberikan kompatibilitas maksimal dan kualitas terbaik.

## 🏗️ Architecture

### Core Components

```
┌─────────────────────────────────────────┐
│           Download Engine               │
├─────────────────────────────────────────┤
│  ┌─────────────┐  ┌─────────────────┐   │
│  │ yt-dlp      │  │ FFmpeg          │   │
│  │ Integration │  │ Integration     │   │
│  └─────────────┘  └─────────────────┘   │
├─────────────────────────────────────────┤
│  ┌─────────────┐  ┌─────────────────┐   │
│  │ Format      │  │ Progress        │   │
│  │ Selection   │  │ Tracking        │   │
│  └─────────────┘  └─────────────────┘   │
├─────────────────────────────────────────┤
│  ┌─────────────┐  ┌─────────────────┐   │
│  │ Fallback    │  │ Error           │   │
│  │ System      │  │ Recovery        │   │
│  └─────────────┘  └─────────────────┘   │
└─────────────────────────────────────────┘
```

### Service Layer

```kotlin
class YouTubeExtractorService(private val context: Context) {
    // Core functionality
    suspend fun initializeYoutubeDL(): Result<Unit>
    suspend fun extractVideoInfo(url: String): Result<VideoInfo>
    suspend fun downloadVideo(url: String, outputDir: File, format: String, onProgress: (Float, Long, String) -> Unit): Result<String>
    suspend fun downloadAudio(url: String, outputDir: File, onProgress: (Float, Long, String) -> Unit): Result<String>
}
```

## 🔧 yt-dlp Integration

### Initialization Process

```kotlin
suspend fun initializeYoutubeDL(): Result<Unit> = withContext(Dispatchers.IO) {
    try {
        // Initialize yt-dlp library
        YoutubeDL.getInstance().init(context)
        
        // Initialize FFmpeg (optional)
        try {
            val clazz = Class.forName("com.yausername.ffmpeg.FFmpeg")
            val getInstance = clazz.getMethod("getInstance")
            val ffmpeg = getInstance.invoke(null)
            val initMethod = clazz.getMethod("init", Context::class.java)
            initMethod.invoke(ffmpeg, context)
            ffmpegAvailable = true
        } catch (e: Exception) {
            ffmpegAvailable = false
            // Continue without FFmpeg
        }
        
        isInitialized = true
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(Exception("Failed to initialize: ${e.message}"))
    }
}
```

### Video Information Extraction

```kotlin
suspend fun extractVideoInfo(url: String): Result<VideoInfo> = withContext(Dispatchers.IO) {
    try {
        val request = YoutubeDLRequest(url).apply {
            addOption("--dump-json")
            addOption("--no-playlist")
            addOption("--no-warnings")
        }
        
        val videoInfo = YoutubeDL.getInstance().getInfo(request)
        Result.success(convertToInternalFormat(videoInfo))
    } catch (e: Exception) {
        Result.failure(Exception("Failed to extract video info: ${e.message}"))
    }
}
```

### yt-dlp Options

#### Common Options
```kotlin
val request = YoutubeDLRequest(url).apply {
    // Basic options
    addOption("--no-playlist")        // Single video only
    addOption("--no-warnings")        // Suppress warnings
    addOption("--ignore-errors")      // Continue on errors
    addOption("--newline")            // Progress on new lines
    addOption("--no-part")            // Don't use .part files
    
    // Output options
    addOption("-o", "${outputDir.absolutePath}/%(title)s.%(ext)s")
    
    // Format selection
    addOption("-f", formatSelector)
}
```

#### Video-Specific Options
```kotlin
// For video downloads
addOption("-f", "bestvideo[ext=mp4]+bestaudio[ext=m4a]/best[ext=mp4]/best")
addOption("--merge-output-format", "mp4")
addOption("--prefer-ffmpeg")
```

#### Audio-Specific Options
```kotlin
// For audio downloads (with FFmpeg)
addOption("-f", "bestaudio")
addOption("--extract-audio")
addOption("--audio-format", "mp3")
addOption("--audio-quality", "0")  // Best quality
addOption("--prefer-ffmpeg")

// For audio downloads (without FFmpeg)
addOption("-f", "bestaudio[ext=m4a]/bestaudio")
```

## 🎵 FFmpeg Integration

### Availability Detection

```kotlin
@Volatile
private var ffmpegAvailable: Boolean = false

private fun detectFFmpegAvailability() {
    try {
        val clazz = Class.forName("com.yausername.ffmpeg.FFmpeg")
        val getInstance = clazz.getMethod("getInstance")
        val ffmpeg = getInstance.invoke(null)
        val initMethod = clazz.getMethod("init", Context::class.java)
        initMethod.invoke(ffmpeg, context)
        ffmpegAvailable = true
    } catch (e: Exception) {
        ffmpegAvailable = false
    }
}
```

### Audio Processing

```kotlin
suspend fun downloadAudio(url: String, outputDir: File, onProgress: (Float, Long, String) -> Unit): Result<String> {
    return if (ffmpegAvailable) {
        // Extract to MP3 using FFmpeg
        downloadWithExtraction(url, outputDir, onProgress)
    } else {
        // Download best audio format without extraction
        downloadBestAudio(url, outputDir, onProgress)
    }
}
```

### Format Conversion

```kotlin
private suspend fun downloadWithExtraction(url: String, outputDir: File, onProgress: (Float, Long, String) -> Unit): Result<String> {
    val request = YoutubeDLRequest(url).apply {
        addOption("-f", "bestaudio")
        addOption("--extract-audio")
        addOption("--audio-format", "mp3")
        addOption("--audio-quality", "0")
        addOption("--prefer-ffmpeg")
        addOption("-o", "${outputDir.absolutePath}/%(title)s.%(ext)s")
    }
    
    YoutubeDL.getInstance().execute(request) { progress, etaInSeconds, line ->
        onProgress(progress, etaInSeconds, line ?: "")
    }
    
    return findDownloadedFile(outputDir, setOf("mp3"))
}
```

## 📊 Format Selection System

### Format Priority Matrix

```kotlin
/**
 * Format selection priority:
 * 1. Progressive formats (audio+video in single file)
 * 2. Best video + Best audio (requires merging)
 * 3. Best available format
 */
private fun buildFormatSelector(info: VideoInfo, requestedFormat: DownloadFormat): String {
    return when (requestedFormat) {
        DownloadFormat.MP4 -> buildVideoFormatSelector(info)
        DownloadFormat.MP3 -> buildAudioFormatSelector(info)
    }
}
```

### Video Format Selection

```kotlin
private fun buildVideoFormatSelector(info: VideoInfo): String {
    val formats = info.formats ?: emptyList()
    
    // Find progressive MP4 (has both audio and video)
    val progressiveMp4 = formats
        .filter { format ->
            format.ext?.lowercase() == "mp4" &&
            format.acodec?.lowercase() != "none" &&
            format.vcodec?.lowercase() != "none"
        }
        .sortedWith(
            compareByDescending<VideoFormat> { it.height ?: 0 }
                .thenByDescending { it.abr ?: 0.0 }
        )
        .firstOrNull()
    
    return if (progressiveMp4?.formatId != null) {
        progressiveMp4.formatId!!
    } else {
        // Fallback to separate video+audio streams
        "bestvideo[ext=mp4]+bestaudio[ext=m4a]/best[ext=mp4]/best"
    }
}
```

### Audio Format Selection

```kotlin
private fun buildAudioFormatSelector(ffmpegAvailable: Boolean): String {
    return if (ffmpegAvailable) {
        // Can extract to MP3
        "bestaudio"
    } else {
        // Use best audio format directly
        "bestaudio[ext=m4a]/bestaudio[ext=opus]/bestaudio"
    }
}
```

## 🔄 Fallback System

### Multi-Level Fallback

```kotlin
suspend fun downloadVideo(url: String, outputDir: File, format: String, onProgress: (Float, Long, String) -> Unit): Result<String> {
    val fallbackChain = listOf(
        format,                                    // Requested format
        "bestvideo[ext=mp4]+bestaudio[ext=m4a]",  // MP4 video + M4A audio
        "best[ext=mp4]",                          // Best MP4
        "best"                                    // Best available
    )
    
    for (selector in fallbackChain) {
        val result = attemptDownload(url, outputDir, selector, onProgress)
        if (result.isSuccess) {
            return result
        }
    }
    
    return Result.failure(Exception("All download attempts failed"))
}
```

### Error Detection and Recovery

```kotlin
private fun executeDownload(selector: String, onProgress: (Float, Long, String) -> Unit) {
    var requestedUnavailable = false
    
    YoutubeDL.getInstance().execute(request) { progress, etaInSeconds, line ->
        val text = line ?: ""
        
        // Detect format unavailability
        if (text.contains("Requested format is not available", ignoreCase = true)) {
            requestedUnavailable = true
        }
        
        // Detect other errors
        if (text.contains("ERROR:", ignoreCase = true)) {
            // Log error for debugging
            android.util.Log.e("DownloadEngine", "Download error: $text")
        }
        
        onProgress(progress, etaInSeconds, text)
    }
    
    // Trigger fallback if format unavailable
    if (requestedUnavailable) {
        throw FormatUnavailableException("Requested format not available")
    }
}
```

## 📈 Progress Tracking

### Progress Callback System

```kotlin
interface ProgressCallback {
    fun onProgress(percentage: Float, etaSeconds: Long, statusMessage: String)
}

private fun createProgressHandler(callback: ProgressCallback): (Float, Long, String) -> Unit {
    return { progress, eta, message ->
        try {
            val safeProgress = progress.coerceIn(0f, 100f)
            callback.onProgress(safeProgress, eta, message)
        } catch (e: Exception) {
            // Handle callback errors gracefully
            android.util.Log.w("DownloadEngine", "Progress callback error: ${e.message}")
        }
    }
}
```

### Progress States

```kotlin
sealed class DownloadProgress {
    data class Initializing(val message: String) : DownloadProgress()
    data class Extracting(val percentage: Float) : DownloadProgress()
    data class Downloading(val percentage: Float, val speed: String, val eta: String) : DownloadProgress()
    data class Processing(val message: String) : DownloadProgress()
    data class Completed(val filePath: String) : DownloadProgress()
    data class Failed(val error: String) : DownloadProgress()
}
```

### Progress Parsing

```kotlin
private fun parseProgressMessage(message: String): DownloadProgress {
    return when {
        message.contains("[download]") && message.contains("%") -> {
            val percentage = extractPercentage(message)
            val speed = extractSpeed(message)
            val eta = extractETA(message)
            DownloadProgress.Downloading(percentage, speed, eta)
        }
        message.contains("[ffmpeg]") -> {
            DownloadProgress.Processing("Converting audio format...")
        }
        message.contains("Extracting") -> {
            DownloadProgress.Extracting(0f)
        }
        else -> {
            DownloadProgress.Initializing(message)
        }
    }
}
```

## 🔍 File Detection and Validation

### File Discovery

```kotlin
private fun findLatestDownloadedFile(
    dir: File, 
    allowedExts: Set<String>, 
    windowMs: Long = 5 * 60 * 1000
): File? {
    val now = System.currentTimeMillis()
    return dir.listFiles()
        ?.filter { file ->
            file.isFile &&
            file.extension.lowercase() in allowedExts &&
            file.lastModified() >= now - windowMs &&
            file.length() > 0L
        }
        ?.maxByOrNull { it.lastModified() }
}
```

### File Validation

```kotlin
private fun validateDownloadedFile(file: File): Boolean {
    return try {
        file.exists() &&
        file.isFile &&
        file.length() > 0L &&
        file.canRead()
    } catch (e: Exception) {
        false
    }
}
```

### Supported Extensions

```kotlin
object SupportedFormats {
    val VIDEO_EXTENSIONS = setOf("mp4", "webm", "mkv", "m4v", "3gp", "avi")
    val AUDIO_EXTENSIONS = setOf("mp3", "m4a", "opus", "webm", "aac", "wav", "flac", "m4b")
    
    fun isVideoFormat(extension: String): Boolean {
        return extension.lowercase() in VIDEO_EXTENSIONS
    }
    
    fun isAudioFormat(extension: String): Boolean {
        return extension.lowercase() in AUDIO_EXTENSIONS
    }
}
```

## ⚡ Performance Optimizations

### Memory Management

```kotlin
class DownloadEngine {
    private val downloadScope = CoroutineScope(
        Dispatchers.IO + SupervisorJob()
    )
    
    // Use bounded channels to prevent memory buildup
    private val progressChannel = Channel<DownloadProgress>(capacity = 10)
    
    fun cleanup() {
        downloadScope.cancel()
        progressChannel.close()
    }
}
```

### Concurrent Downloads Prevention

```kotlin
class DownloadEngine {
    private val downloadMutex = Mutex()
    
    suspend fun downloadVideo(request: DownloadRequest): Flow<DownloadState> {
        return downloadMutex.withLock {
            // Only one download at a time
            performDownload(request)
        }
    }
}
```

### Resource Cleanup

```kotlin
private suspend fun performDownload(request: DownloadRequest): Flow<DownloadState> = callbackFlow {
    val job = launch(Dispatchers.IO) {
        try {
            // Download logic
        } finally {
            // Cleanup temporary files
            cleanupTempFiles(outputDir)
        }
    }
    
    awaitClose { 
        job.cancel()
        cleanup()
    }
}
```

## 🛡️ Error Handling

### Exception Hierarchy

```kotlin
sealed class DownloadException(message: String, cause: Throwable? = null) : Exception(message, cause) {
    class NetworkException(message: String, cause: Throwable? = null) : DownloadException(message, cause)
    class FormatException(message: String, cause: Throwable? = null) : DownloadException(message, cause)
    class StorageException(message: String, cause: Throwable? = null) : DownloadException(message, cause)
    class InitializationException(message: String, cause: Throwable? = null) : DownloadException(message, cause)
}
```

### Error Recovery Strategies

```kotlin
private suspend fun downloadWithRetry(
    request: DownloadRequest,
    maxRetries: Int = 3
): Result<String> {
    repeat(maxRetries) { attempt ->
        try {
            return performDownload(request)
        } catch (e: NetworkException) {
            if (attempt == maxRetries - 1) throw e
            delay(1000 * (attempt + 1)) // Exponential backoff
        } catch (e: FormatException) {
            // Don't retry format errors
            throw e
        }
    }
    throw Exception("Max retries exceeded")
}
```

## 🧪 Testing the Download Engine

### Unit Tests

```kotlin
class DownloadEngineTest {
    @Test
    fun `extractVideoInfo returns valid info for valid URL`() = runTest {
        val engine = YouTubeExtractorService(mockContext)
        val result = engine.extractVideoInfo("https://youtube.com/watch?v=dQw4w9WgXcQ")
        
        assertTrue(result.isSuccess)
        val videoInfo = result.getOrNull()!!
        assertNotNull(videoInfo.title)
        assertNotNull(videoInfo.id)
    }
    
    @Test
    fun `downloadVideo handles format fallback correctly`() = runTest {
        val engine = YouTubeExtractorService(mockContext)
        // Test with unavailable format
        val result = engine.downloadVideo(
            url = "https://youtube.com/watch?v=test",
            outputDir = tempDir,
            format = "nonexistent[ext=fake]",
            onProgress = { _, _, _ -> }
        )
        
        // Should fallback to available format
        assertTrue(result.isSuccess)
    }
}
```

### Integration Tests

```kotlin
class DownloadEngineIntegrationTest {
    @Test
    fun `complete download flow works end to end`() = runTest {
        val repository = DownloadRepository(context)
        val request = DownloadRequest(
            url = "https://youtube.com/watch?v=dQw4w9WgXcQ",
            format = DownloadFormat.MP4
        )
        
        val states = mutableListOf<DownloadState>()
        repository.downloadVideo(request).collect { state ->
            states.add(state)
        }
        
        // Verify state progression
        assertTrue(states.any { it is DownloadState.Loading })
        assertTrue(states.any { it is DownloadState.Progress })
        assertTrue(states.any { it is DownloadState.Success })
    }
}
```

## 📊 Monitoring and Debugging

### Logging System

```kotlin
object DownloadLogger {
    private const val TAG = "DownloadEngine"
    
    fun logInfo(message: String) {
        if (BuildConfig.DEBUG) {
            android.util.Log.i(TAG, message)
        }
    }
    
    fun logError(message: String, throwable: Throwable? = null) {
        android.util.Log.e(TAG, message, throwable)
    }
    
    fun logProgress(url: String, progress: Float, message: String) {
        if (BuildConfig.DEBUG) {
            android.util.Log.d(TAG, "[$url] $progress% - $message")
        }
    }
}
```

### Performance Metrics

```kotlin
class DownloadMetrics {
    data class DownloadStats(
        val startTime: Long,
        val endTime: Long,
        val fileSize: Long,
        val downloadSpeed: Double, // bytes per second
        val format: String,
        val fallbacksUsed: Int
    )
    
    fun calculateStats(
        startTime: Long,
        endTime: Long,
        file: File,
        format: String,
        fallbacksUsed: Int
    ): DownloadStats {
        val duration = (endTime - startTime) / 1000.0 // seconds
        val speed = if (duration > 0) file.length() / duration else 0.0
        
        return DownloadStats(
            startTime = startTime,
            endTime = endTime,
            fileSize = file.length(),
            downloadSpeed = speed,
            format = format,
            fallbacksUsed = fallbacksUsed
        )
    }
}
```

---

**🚀 Download Engine** - The heart of Local YouTube Downloader Android, providing robust, efficient, and reliable video downloading capabilities.

**Key Features:**
- ✅ **yt-dlp Integration** - Latest YouTube extraction technology
- ✅ **FFmpeg Support** - High-quality audio/video processing
- ✅ **Multi-level Fallback** - Maximum compatibility
- ✅ **Progress Tracking** - Real-time download progress
- ✅ **Error Recovery** - Graceful error handling
- ✅ **Performance Optimized** - Memory and resource efficient
