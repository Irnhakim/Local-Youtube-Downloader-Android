# 🛡️ Error Handling & Fallback System

Dokumentasi lengkap sistem penanganan error dan fallback Local YouTube Downloader Android.

## 🎯 Overview

Error Handling & Fallback System dirancang untuk memberikan pengalaman yang robust dan user-friendly dengan menangani berbagai skenario kegagalan secara graceful dan menyediakan alternatif otomatis ketika operasi utama gagal.

## 🏗️ Architecture

### Error Handling Layers

```
┌─────────────────────────────────────────┐
│            UI Layer                     │
│  ┌─────────────────────────────────┐    │
│  │ User-Friendly Error Messages    │    │
│  │ Retry Actions & Suggestions     │    │
│  └─────────────────────────────────┘    │
├─────────────────────────────────────────┤
│           ViewModel Layer               │
│  ┌─────────────────────────────────┐    │
│  │ Error State Management          │    │
│  │ User Action Coordination        │    │
│  └─────────────────────────────────┘    │
├─────────────────────────────────────────┤
│          Repository Layer               │
│  ┌─────────────────────────────────┐    │
│  │ Business Logic Error Handling   │    │
│  │ Service Error Translation       │    │
│  └─────────────────────────────────┘    │
├─────────────────────────────────────────┤
│           Service Layer                 │
│  ┌─────────────────────────────────┐    │
│  │ Technical Error Detection       │    │
│  │ Automatic Fallback Execution    │    │
│  └─────────────────────────────────┘    │
└─────────────────────────────────────────┘
```

## 🚨 Error Classification

### Error Hierarchy

```kotlin
sealed class AppError(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause) {
    
    // Network-related errors
    sealed class NetworkError(message: String, cause: Throwable? = null) : AppError(message, cause) {
        class ConnectionTimeout(cause: Throwable? = null) : NetworkError("Connection timeout", cause)
        class NoInternet(cause: Throwable? = null) : NetworkError("No internet connection", cause)
        class ServerError(message: String, cause: Throwable? = null) : NetworkError(message, cause)
        class RateLimited(cause: Throwable? = null) : NetworkError("Rate limited by server", cause)
    }
    
    // YouTube-specific errors
    sealed class YouTubeError(message: String, cause: Throwable? = null) : AppError(message, cause) {
        class VideoNotFound(cause: Throwable? = null) : YouTubeError("Video not found or private", cause)
        class VideoUnavailable(region: String? = null) : YouTubeError("Video unavailable${region?.let { " in $it" } ?: ""}")
        class AgeRestricted(cause: Throwable? = null) : YouTubeError("Video is age-restricted", cause)
        class LiveStream(cause: Throwable? = null) : YouTubeError("Live streams not supported", cause)
    }
    
    // Format-related errors
    sealed class FormatError(message: String, cause: Throwable? = null) : AppError(message, cause) {
        class FormatNotAvailable(format: String) : FormatError("Format $format not available")
        class QualityNotAvailable(quality: String) : FormatError("Quality $quality not available")
        class ExtractionFailed(cause: Throwable? = null) : FormatError("Failed to extract formats", cause)
    }
    
    // Storage-related errors
    sealed class StorageError(message: String, cause: Throwable? = null) : AppError(message, cause) {
        class InsufficientSpace(required: Long, available: Long) : StorageError("Insufficient storage: need ${formatBytes(required)}, have ${formatBytes(available)}")
        class PermissionDenied(cause: Throwable? = null) : StorageError("Storage permission denied", cause)
        class FileNotFound(path: String) : StorageError("File not found: $path")
        class FileCorrupted(path: String) : StorageError("File corrupted: $path")
    }
    
    // Processing errors
    sealed class ProcessingError(message: String, cause: Throwable? = null) : AppError(message, cause) {
        class FFmpegNotAvailable(cause: Throwable? = null) : ProcessingError("FFmpeg not available", cause)
        class ConversionFailed(cause: Throwable? = null) : ProcessingError("Audio conversion failed", cause)
        class MergeFailed(cause: Throwable? = null) : ProcessingError("Video merge failed", cause)
    }
    
    // Validation errors
    sealed class ValidationError(message: String, cause: Throwable? = null) : AppError(message, cause) {
        class InvalidUrl(url: String) : ValidationError("Invalid YouTube URL: $url")
        class UnsupportedUrl(url: String) : ValidationError("Unsupported URL format: $url")
        class EmptyInput(cause: Throwable? = null) : ValidationError("Input cannot be empty", cause)
    }
    
    // System errors
    sealed class SystemError(message: String, cause: Throwable? = null) : AppError(message, cause) {
        class InitializationFailed(component: String, cause: Throwable? = null) : SystemError("Failed to initialize $component", cause)
        class OutOfMemory(cause: Throwable? = null) : SystemError("Out of memory", cause)
        class UnknownError(cause: Throwable? = null) : SystemError("Unknown error occurred", cause)
    }
}
```

### Error Severity Levels

```kotlin
enum class ErrorSeverity {
    LOW,        // Minor issues, app continues normally
    MEDIUM,     // Significant issues, some features affected
    HIGH,       // Major issues, core functionality affected
    CRITICAL    // App-breaking issues, requires immediate attention
}

fun AppError.getSeverity(): ErrorSeverity {
    return when (this) {
        is AppError.ValidationError -> ErrorSeverity.LOW
        is AppError.FormatError.QualityNotAvailable -> ErrorSeverity.LOW
        is AppError.NetworkError.ConnectionTimeout -> ErrorSeverity.MEDIUM
        is AppError.FormatError.FormatNotAvailable -> ErrorSeverity.MEDIUM
        is AppError.StorageError.InsufficientSpace -> ErrorSeverity.HIGH
        is AppError.SystemError.InitializationFailed -> ErrorSeverity.CRITICAL
        else -> ErrorSeverity.MEDIUM
    }
}
```

## 🔄 Fallback System

### Multi-Level Fallback Strategy

```kotlin
class FallbackManager {
    
    suspend fun executeWithFallback<T>(
        primary: suspend () -> T,
        fallbacks: List<suspend () -> T>,
        onFallback: (Int, Exception) -> Unit = { _, _ -> }
    ): Result<T> {
        
        // Try primary operation
        try {
            return Result.success(primary())
        } catch (e: Exception) {
            android.util.Log.w("FallbackManager", "Primary operation failed: ${e.message}")
        }
        
        // Try fallback operations
        fallbacks.forEachIndexed { index, fallback ->
            try {
                onFallback(index, Exception("Attempting fallback ${index + 1}"))
                return Result.success(fallback())
            } catch (e: Exception) {
                android.util.Log.w("FallbackManager", "Fallback ${index + 1} failed: ${e.message}")
            }
        }
        
        return Result.failure(AppError.SystemError.UnknownError(Exception("All operations failed")))
    }
}
```

### Download Format Fallback

```kotlin
class DownloadFallbackManager {
    
    suspend fun downloadWithFallback(
        url: String,
        requestedFormat: DownloadFormat,
        outputDir: File,
        onProgress: (Float, Long, String) -> Unit
    ): Result<String> {
        
        val fallbackChain = when (requestedFormat) {
            DownloadFormat.MP4 -> createVideoFallbackChain(url, outputDir, onProgress)
            DownloadFormat.MP3 -> createAudioFallbackChain(url, outputDir, onProgress)
        }
        
        return FallbackManager().executeWithFallback(
            primary = fallbackChain.first(),
            fallbacks = fallbackChain.drop(1),
            onFallback = { index, _ ->
                onProgress(0f, 0L, "Trying alternative format ${index + 1}...")
            }
        )
    }
    
    private fun createVideoFallbackChain(
        url: String,
        outputDir: File,
        onProgress: (Float, Long, String) -> Unit
    ): List<suspend () -> String> {
        return listOf(
            // 1. Progressive MP4 (best quality, single file)
            { downloadWithFormat(url, outputDir, "best[ext=mp4][acodec!=none][vcodec!=none]", onProgress) },
            
            // 2. Best video + Best audio (requires merging)
            { downloadWithFormat(url, outputDir, "bestvideo[ext=mp4]+bestaudio[ext=m4a]", onProgress) },
            
            // 3. Any MP4 format
            { downloadWithFormat(url, outputDir, "best[ext=mp4]", onProgress) },
            
            // 4. Best WebM format
            { downloadWithFormat(url, outputDir, "best[ext=webm]", onProgress) },
            
            // 5. Any best format
            { downloadWithFormat(url, outputDir, "best", onProgress) }
        )
    }
    
    private fun createAudioFallbackChain(
        url: String,
        outputDir: File,
        onProgress: (Float, Long, String) -> Unit
    ): List<suspend () -> String> {
        return listOf(
            // 1. Extract to MP3 (requires FFmpeg)
            { extractToMp3(url, outputDir, onProgress) },
            
            // 2. Best M4A audio
            { downloadWithFormat(url, outputDir, "bestaudio[ext=m4a]", onProgress) },
            
            // 3. Best OPUS audio
            { downloadWithFormat(url, outputDir, "bestaudio[ext=opus]", onProgress) },
            
            // 4. Any best audio
            { downloadWithFormat(url, outputDir, "bestaudio", onProgress) }
        )
    }
}
```

### Network Retry Strategy

```kotlin
class NetworkRetryManager {
    
    suspend fun <T> executeWithRetry(
        maxRetries: Int = 3,
        initialDelayMs: Long = 1000,
        maxDelayMs: Long = 10000,
        backoffMultiplier: Double = 2.0,
        operation: suspend () -> T
    ): Result<T> {
        
        var currentDelay = initialDelayMs
        var lastException: Exception? = null
        
        repeat(maxRetries) { attempt ->
            try {
                return Result.success(operation())
            } catch (e: Exception) {
                lastException = e
                
                // Don't retry certain types of errors
                if (!shouldRetry(e)) {
                    return Result.failure(e)
                }
                
                if (attempt < maxRetries - 1) {
                    android.util.Log.w("NetworkRetry", "Attempt ${attempt + 1} failed, retrying in ${currentDelay}ms: ${e.message}")
                    delay(currentDelay)
                    currentDelay = (currentDelay * backoffMultiplier).toLong().coerceAtMost(maxDelayMs)
                }
            }
        }
        
        return Result.failure(lastException ?: Exception("Max retries exceeded"))
    }
    
    private fun shouldRetry(exception: Exception): Boolean {
        return when (exception) {
            is AppError.NetworkError.ConnectionTimeout -> true
            is AppError.NetworkError.ServerError -> true
            is AppError.YouTubeError.VideoNotFound -> false
            is AppError.ValidationError -> false
            is AppError.StorageError.PermissionDenied -> false
            else -> true
        }
    }
}
```

## 🔧 Error Recovery Mechanisms

### Automatic Recovery

```kotlin
class ErrorRecoveryManager {
    
    suspend fun recoverFromError(error: AppError, context: RecoveryContext): RecoveryResult {
        return when (error) {
            is AppError.NetworkError.NoInternet -> {
                RecoveryResult.WaitAndRetry(
                    message = "Waiting for internet connection...",
                    retryDelayMs = 5000
                )
            }
            
            is AppError.StorageError.InsufficientSpace -> {
                val cleaned = cleanupOldFiles(context.outputDir)
                if (cleaned.freedBytes > error.required) {
                    RecoveryResult.Recovered("Freed ${formatBytes(cleaned.freedBytes)} of storage")
                } else {
                    RecoveryResult.Failed("Unable to free sufficient storage")
                }
            }
            
            is AppError.ProcessingError.FFmpegNotAvailable -> {
                RecoveryResult.Fallback(
                    message = "Using alternative audio format",
                    fallbackAction = { downloadBestAudio(context.url, context.outputDir) }
                )
            }
            
            is AppError.FormatError.FormatNotAvailable -> {
                RecoveryResult.Fallback(
                    message = "Trying alternative format",
                    fallbackAction = { tryAlternativeFormat(context.url, context.outputDir) }
                )
            }
            
            else -> RecoveryResult.Failed("No automatic recovery available")
        }
    }
}

sealed class RecoveryResult {
    data class Recovered(val message: String) : RecoveryResult()
    data class WaitAndRetry(val message: String, val retryDelayMs: Long) : RecoveryResult()
    data class Fallback(val message: String, val fallbackAction: suspend () -> String) : RecoveryResult()
    data class Failed(val message: String) : RecoveryResult()
}

data class RecoveryContext(
    val url: String,
    val outputDir: File,
    val requestedFormat: DownloadFormat,
    val userPreferences: UserPreferences
)
```

### User-Guided Recovery

```kotlin
class UserRecoveryManager {
    
    fun getRecoveryOptions(error: AppError): List<RecoveryOption> {
        return when (error) {
            is AppError.NetworkError.NoInternet -> listOf(
                RecoveryOption.Retry("Check connection and retry"),
                RecoveryOption.Cancel("Cancel download")
            )
            
            is AppError.StorageError.InsufficientSpace -> listOf(
                RecoveryOption.Action("Free up storage") { openStorageSettings() },
                RecoveryOption.Action("Choose different location") { selectAlternativeStorage() },
                RecoveryOption.Cancel("Cancel download")
            )
            
            is AppError.YouTubeError.VideoUnavailable -> listOf(
                RecoveryOption.Action("Try different URL") { clearUrlAndFocus() },
                RecoveryOption.Action("Check video in browser") { openInBrowser() },
                RecoveryOption.Cancel("Cancel")
            )
            
            is AppError.FormatError.FormatNotAvailable -> listOf(
                RecoveryOption.Action("Try different format") { switchFormat() },
                RecoveryOption.Retry("Retry with automatic format"),
                RecoveryOption.Cancel("Cancel download")
            )
            
            else -> listOf(
                RecoveryOption.Retry("Try again"),
                RecoveryOption.Cancel("Cancel")
            )
        }
    }
}

sealed class RecoveryOption {
    data class Retry(val label: String) : RecoveryOption()
    data class Action(val label: String, val action: suspend () -> Unit) : RecoveryOption()
    data class Cancel(val label: String) : RecoveryOption()
}
```

## 🎨 UI Error Handling

### Error State Management

```kotlin
data class ErrorUiState(
    val error: AppError? = null,
    val isRecovering: Boolean = false,
    val recoveryMessage: String? = null,
    val recoveryOptions: List<RecoveryOption> = emptyList(),
    val canRetry: Boolean = false,
    val retryCount: Int = 0,
    val maxRetries: Int = 3
) {
    val hasError: Boolean get() = error != null
    val canShowRetry: Boolean get() = canRetry && retryCount < maxRetries
    val shouldShowRecoveryOptions: Boolean get() = recoveryOptions.isNotEmpty()
}
```

### Error Display Components

```kotlin
@Composable
fun ErrorDisplay(
    errorState: ErrorUiState,
    onRetry: () -> Unit,
    onRecoveryAction: (RecoveryOption) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (errorState.hasError) {
        Card(
            modifier = modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Error icon and title
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Text(
                        text = getErrorTitle(errorState.error!!),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
                
                // Error message
                Text(
                    text = getErrorMessage(errorState.error!!),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
                
                // Recovery message
                if (errorState.recoveryMessage != null) {
                    Text(
                        text = errorState.recoveryMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        fontStyle = FontStyle.Italic
                    )
                }
                
                // Action buttons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (errorState.canShowRetry) {
                        Button(
                            onClick = onRetry,
                            enabled = !errorState.isRecovering
                        ) {
                            if (errorState.isRecovering) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text("Retry (${errorState.retryCount + 1}/${errorState.maxRetries})")
                            }
                        }
                    }
                    
                    OutlinedButton(onClick = onDismiss) {
                        Text("Dismiss")
                    }
                }
                
                // Recovery options
                if (errorState.shouldShowRecoveryOptions) {
                    Divider()
                    Text(
                        text = "Recovery Options:",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    
                    errorState.recoveryOptions.forEach { option ->
                        when (option) {
                            is RecoveryOption.Action -> {
                                TextButton(
                                    onClick = { onRecoveryAction(option) }
                                ) {
                                    Text(option.label)
                                }
                            }
                            is RecoveryOption.Retry -> {
                                TextButton(
                                    onClick = { onRecoveryAction(option) }
                                ) {
                                    Text(option.label)
                                }
                            }
                            is RecoveryOption.Cancel -> {
                                TextButton(
                                    onClick = { onRecoveryAction(option) }
                                ) {
                                    Text(option.label)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
```

### User-Friendly Error Messages

```kotlin
fun getErrorTitle(error: AppError): String {
    return when (error) {
        is AppError.NetworkError -> "Connection Problem"
        is AppError.YouTubeError -> "Video Issue"
        is AppError.FormatError -> "Format Problem"
        is AppError.StorageError -> "Storage Issue"
        is AppError.ProcessingError -> "Processing Problem"
        is AppError.ValidationError -> "Input Error"
        is AppError.SystemError -> "System Error"
    }
}

fun getErrorMessage(error: AppError): String {
    return when (error) {
        is AppError.NetworkError.NoInternet -> 
            "No internet connection. Please check your network settings and try again."
        
        is AppError.NetworkError.ConnectionTimeout -> 
            "Connection timed out. The server might be busy or your connection is slow."
        
        is AppError.YouTubeError.VideoNotFound -> 
            "Video not found. It might be private, deleted, or the URL is incorrect."
        
        is AppError.YouTubeError.VideoUnavailable -> 
            "Video is not available in your region or has been restricted."
        
        is AppError.FormatError.FormatNotAvailable -> 
            "The requested format is not available for this video. We'll try an alternative format."
        
        is AppError.StorageError.InsufficientSpace -> 
            "Not enough storage space. Please free up some space and try again."
        
        is AppError.StorageError.PermissionDenied -> 
            "Storage permission denied. Please grant storage access in app settings."
        
        is AppError.ProcessingError.FFmpegNotAvailable -> 
            "Audio conversion not available. We'll download in the best available audio format."
        
        is AppError.ValidationError.InvalidUrl -> 
            "Invalid YouTube URL. Please check the URL and try again."
        
        else -> error.message ?: "An unexpected error occurred. Please try again."
    }
}
```

## 📊 Error Monitoring and Analytics

### Error Tracking

```kotlin
class ErrorTracker {
    
    private val errorLog = mutableListOf<ErrorEvent>()
    
    fun trackError(
        error: AppError,
        context: String,
        userId: String? = null,
        additionalData: Map<String, Any> = emptyMap()
    ) {
        val event = ErrorEvent(
            timestamp = System.currentTimeMillis(),
            error = error,
            context = context,
            severity = error.getSeverity(),
            userId = userId,
            deviceInfo = getDeviceInfo(),
            appVersion = getAppVersion(),
            additionalData = additionalData
        )
        
        errorLog.add(event)
        
        // Log to system
        when (event.severity) {
            ErrorSeverity.LOW -> android.util.Log.i("ErrorTracker", event.toString())
            ErrorSeverity.MEDIUM -> android.util.Log.w("ErrorTracker", event.toString())
            ErrorSeverity.HIGH, ErrorSeverity.CRITICAL -> android.util.Log.e("ErrorTracker", event.toString())
        }
        
        // Send to analytics (if enabled and user consented)
        if (shouldSendAnalytics()) {
            sendToAnalytics(event)
        }
    }
    
    fun getErrorStatistics(): ErrorStatistics {
        return ErrorStatistics(
            totalErrors = errorLog.size,
            errorsByType = errorLog.groupBy { it.error::class.simpleName }.mapValues { it.value.size },
            errorsBySeverity = errorLog.groupBy { it.severity }.mapValues { it.value.size },
            mostCommonErrors = errorLog.groupBy { it.error::class.simpleName }
                .toList()
                .sortedByDescending { it.second.size }
                .take(5)
                .map { it.first to it.second.size }
        )
    }
}

data class ErrorEvent(
    val timestamp: Long,
    val error: AppError,
    val context: String,
    val severity: ErrorSeverity,
    val userId: String?,
    val deviceInfo: DeviceInfo,
    val appVersion: String,
    val additionalData: Map<String, Any>
)

data class ErrorStatistics(
    val totalErrors: Int,
    val errorsByType: Map<String?, Int>,
    val errorsBySeverity: Map<ErrorSeverity, Int>,
    val mostCommonErrors: List<Pair<String?, Int>>
)
```

### Crash-Safe Wrappers

```kotlin
class CrashSafeWrapper<T>(
    private val delegate: T,
    private val errorTracker: ErrorTracker
) {
    
    inline fun <R> safeCall(
        context: String,
        fallback: R? = null,
        operation: T.() -> R
    ): R? {
        return try {
            delegate.operation()
        } catch (e: Exception) {
            val appError = when (e) {
                is AppError -> e
                else -> AppError.SystemError.UnknownError(e)
            }
            
            errorTracker.trackError(
                error = appError,
                context = context,
                additionalData = mapOf(
                    "stackTrace" to e.stackTraceToString(),
                    "fallbackUsed" to (fallback != null)
                )
            )
            
            fallback
        }
    }
    
    inline fun <R> safeCallSuspend(
        context: String,
        fallback: R? = null,
        crossinline operation: suspend T.() -> R
    ): R? {
        return runBlocking {
            try {
                delegate.operation()
            } catch (e: Exception) {
                val appError = when (e) {
                    is AppError -> e
                    else -> AppError.SystemError.UnknownError(e)
                }
                
                errorTracker.trackError(
                    error = appError,
                    context = context,
                    additionalData = mapOf(
                        "stackTrace" to e.stackTraceToString(),
                        "fallbackUsed" to (fallback != null)
                    )
                )
                
                fallback
            }
        }
    }
}

// Usage example
class CrashSafeDownloadRepository(
    private val delegate: DownloadRepository,
    private val errorTracker: ErrorTracker
) {
    private val wrapper = CrashSafeWrapper(delegate, errorTracker)
    
    suspend fun getVideoInfo(url: String): Result<VideoInfo> {
        return wrapper.safeCallSuspend(
            context = "getVideoInfo",
            fallback = Result.failure(AppError.SystemError.UnknownError())
        ) {
            getVideoInfo(url)
        } ?: Result.failure(AppError.SystemError.UnknownError())
    }
}
```

## 🧪 Testing Error Handling

### Error Simulation

```kotlin
class ErrorSimulator {
    
    fun simulateNetworkError(): AppError.NetworkError {
        return AppError.NetworkError.ConnectionTimeout()
    }
    
    fun simulateStorageError(): AppError.StorageError {
        return AppError.StorageError.InsufficientSpace(
            required = 100_000_000L,
            available = 50_000_000L
        )
    }
    
    fun simulateYouTubeError(): AppError.YouTubeError {
        return AppError.YouTubeError.VideoNotFound()
    }
}
```

### Error Handling Tests

```kotlin
class ErrorHandlingTest {
    
    @Test
    fun `error recovery manager handles network errors correctly`() = runTest {
        val errorRecovery = ErrorRecoveryManager()
        val context = RecoveryContext(
            url = "https://youtube.com/watch?v=test",
            outputDir = tempDir,
            requestedFormat = DownloadFormat.MP4,
            userPreferences = UserPreferences()
        )
        
        val result = errorRecovery.recoverFromError(
            AppError.NetworkError.NoInternet(),
            context
        )
        
        assertTrue(result is RecoveryResult.WaitAndRetry)
        assertEquals(5000L, (result as RecoveryResult.WaitAndRetry).retryDelayMs)
    }
    
    @Test
    fun `fallback manager tries all fallbacks before failing`() = runTest {
        val fallbackManager = FallbackManager()
        var attemptCount = 0
        
        val result = fallbackManager.executeWithFallback(
            primary = { 
                attemptCount++
                throw Exception("Primary failed") 
            },
            fallbacks = listOf(
                { 
                    attemptCount++
                    throw Exception("Fallback 1 failed") 
                },
                { 
                    attemptCount++
                    throw Exception("Fallback 2 failed") 
                }
            )
        )
        
        assertTrue(result.isFailure)
        assertEquals(3, attemptCount) // Primary + 2 fallbacks
    }
}
```

---

**🛡️ Error Handling & Fallback System** - Comprehensive error management for robust and user-friendly application behavior.

**Key Features:**
- ✅ **Hierarchical Error Classification** - Structured error types and severity levels
- ✅ **Multi-Level Fallback** - Automatic alternative strategies
- ✅ **Intelligent Recovery** - Context-aware error recovery
- ✅ **User-Friendly Messages** - Clear, actionable error communication
- ✅ **Crash-Safe Operations** - Wrapper-based error containment
- ✅ **Comprehensive Monitoring** - Error tracking and analytics
