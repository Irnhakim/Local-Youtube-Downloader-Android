# 📁 File Management System

Dokumentasi lengkap sistem manajemen file Local YouTube Downloader Android.

## 🎯 Overview

File Management System menangani semua aspek penyimpanan, organisasi, dan akses file dalam aplikasi. Sistem ini dirancang untuk memberikan pengalaman yang aman, terorganisir, dan mudah digunakan.

## 🏗️ Architecture

### Core Components

```
┌─────────────────────────────────────────┐
│         File Management System          │
├─────────────────────────────────────────┤
│  ┌─────────────┐  ┌─────────────────┐   │
│  │ FileUtils   │  │ FileProvider    │   │
│  │ Operations  │  │ Integration     │   │
│  └─────────────┘  └─────────────────┘   │
├─────────────────────────────────────────┤
│  ┌─────────────┐  ┌─────────────────┐   │
│  │ Directory   │  │ MIME Type       │   │
│  │ Management  │  │ Detection       │   │
│  └─────────────┘  └─────────────────┘   │
├─────────────────────────────────────────┤
│  ┌─────────────┐  ┌─────────────────┐   │
│  │ File        │  │ Security &      │   │
│  │ Validation  │  │ Permissions     │   │
│  └─────────────┘  └─────────────────┘   │
└─────────────────────────────────────────┘
```

### FileUtils Class

```kotlin
object FileUtils {
    // Directory management
    fun getDownloadDirectory(context: Context): File
    fun createDirectoryIfNotExists(directory: File): Boolean
    
    // File operations
    fun generateFileName(title: String, extension: String): String
    fun getFileSize(file: File): String
    fun deleteFile(filePath: String): Boolean
    
    // MIME type and content URI
    fun getMimeType(filePath: String): String
    fun getContentUri(context: Context, file: File): Uri
    fun openFile(context: Context, filePath: String): Boolean
    
    // URL validation
    fun isValidYouTubeUrl(url: String): Boolean
    fun extractVideoId(url: String): String?
}
```

## 📂 Directory Structure

### Default Directory Layout

```
/storage/emulated/0/Download/YTMP3Downloads/
├── Amazing_Video_20240101_143022.mp4
├── Great_Song_20240101_143055.mp3
├── Tutorial_Part1_20240101_150030.webm
├── Music_Track_20240101_151245.m4a
└── .nomedia                              # Prevents media scanner
```

### Directory Management

```kotlin
private const val DOWNLOAD_FOLDER = "YTMP3Downloads"

fun getDownloadDirectory(context: Context): File {
    val downloadsDir = File(
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
        DOWNLOAD_FOLDER
    )
    
    if (!downloadsDir.exists()) {
        val created = downloadsDir.mkdirs()
        if (created) {
            createNoMediaFile(downloadsDir)
        }
    }
    
    return downloadsDir
}

private fun createNoMediaFile(directory: File) {
    try {
        val noMediaFile = File(directory, ".nomedia")
        if (!noMediaFile.exists()) {
            noMediaFile.createNewFile()
        }
    } catch (e: Exception) {
        android.util.Log.w("FileUtils", "Could not create .nomedia file: ${e.message}")
    }
}
```

### Alternative Storage Locations

```kotlin
fun getAlternativeStorageLocations(context: Context): List<File> {
    val locations = mutableListOf<File>()
    
    // Primary external storage
    locations.add(getDownloadDirectory(context))
    
    // Secondary external storage (SD Card)
    context.getExternalFilesDirs(Environment.DIRECTORY_DOWNLOADS)?.forEach { dir ->
        if (dir != null && dir.exists()) {
            locations.add(File(dir, DOWNLOAD_FOLDER))
        }
    }
    
    // Internal app storage (fallback)
    locations.add(File(context.filesDir, DOWNLOAD_FOLDER))
    
    return locations
}
```

## 📝 File Naming System

### Naming Convention

```kotlin
fun generateFileName(title: String, extension: String): String {
    // Sanitize title
    val sanitizedTitle = title
        .replace(Regex("[^a-zA-Z0-9\\s\\-_]"), "")  // Remove special chars
        .replace(Regex("\\s+"), "_")                 // Replace spaces with underscores
        .take(50)                                    // Limit length
        .trim('_')                                   // Remove leading/trailing underscores
    
    // Generate timestamp
    val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
        .format(Date())
    
    // Combine components
    return "${sanitizedTitle}_${timestamp}.${extension}"
}
```

### Naming Examples

```kotlin
// Input: "Amazing Video Tutorial - Part 1 [HD]", "mp4"
// Output: "Amazing_Video_Tutorial_Part_1_HD_20240101_143022.mp4"

// Input: "Song Title (Official Music Video)", "mp3"
// Output: "Song_Title_Official_Music_Video_20240101_143055.mp3"

// Input: "Very Long Video Title That Exceeds The Maximum Length Limit", "webm"
// Output: "Very_Long_Video_Title_That_Exceeds_The_Maximum_20240101_150030.webm"
```

### Duplicate Handling

```kotlin
fun generateUniqueFileName(directory: File, title: String, extension: String): String {
    var fileName = generateFileName(title, extension)
    var counter = 1
    
    while (File(directory, fileName).exists()) {
        val baseName = generateFileName(title, extension).substringBeforeLast('.')
        fileName = "${baseName}_${counter}.${extension}"
        counter++
    }
    
    return fileName
}
```

## 🔍 MIME Type Detection

### MIME Type Mapping

```kotlin
fun getMimeType(filePath: String): String {
    val extension = filePath.substringAfterLast('.', "").lowercase(Locale.getDefault())
    
    // Try Android's built-in MIME type map first
    val fromMap = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
    if (!fromMap.isNullOrBlank()) {
        return fromMap
    }
    
    // Fallback to manual mapping
    return when (extension) {
        // Video formats
        "mp4" -> "video/mp4"
        "webm" -> "video/webm"
        "mkv" -> "video/x-matroska"
        "m4v" -> "video/x-m4v"
        "3gp" -> "video/3gpp"
        "avi" -> "video/x-msvideo"
        
        // Audio formats
        "mp3" -> "audio/mpeg"
        "m4a", "aac" -> "audio/mp4"
        "opus" -> "audio/opus"
        "wav" -> "audio/wav"
        "flac" -> "audio/flac"
        "m4b" -> "audio/mp4"
        
        // Default
        else -> "*/*"
    }
}
```

### Content Type Validation

```kotlin
fun isValidMediaFile(file: File): Boolean {
    val mimeType = getMimeType(file.absolutePath)
    return mimeType.startsWith("video/") || mimeType.startsWith("audio/")
}

fun getMediaType(filePath: String): MediaType {
    val mimeType = getMimeType(filePath)
    return when {
        mimeType.startsWith("video/") -> MediaType.VIDEO
        mimeType.startsWith("audio/") -> MediaType.AUDIO
        else -> MediaType.UNKNOWN
    }
}

enum class MediaType {
    VIDEO, AUDIO, UNKNOWN
}
```

## 🔐 FileProvider Integration

### FileProvider Configuration

```xml
<!-- AndroidManifest.xml -->
<provider
    android:name="androidx.core.content.FileProvider"
    android:authorities="${applicationId}.fileprovider"
    android:exported="false"
    android:grantUriPermissions="true">
    <meta-data
        android:name="android.support.FILE_PROVIDER_PATHS"
        android:resource="@xml/provider_paths" />
</provider>
```

### Provider Paths Configuration

```xml
<!-- res/xml/provider_paths.xml -->
<?xml version="1.0" encoding="utf-8"?>
<paths xmlns:android="http://schemas.android.com/apk/res/android">
    <!-- External Downloads directory -->
    <external-path 
        name="external_downloads" 
        path="Download/" />
    
    <!-- External app-specific directory -->
    <external-files-path 
        name="external_files" 
        path="Download/" />
    
    <!-- Internal app directory -->
    <files-path 
        name="internal_files" 
        path="Download/" />
</paths>
```

### Content URI Generation

```kotlin
fun getContentUri(context: Context, file: File): Uri {
    return try {
        FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    } catch (e: IllegalArgumentException) {
        // File not in configured paths
        android.util.Log.e("FileUtils", "File not in FileProvider paths: ${file.absolutePath}")
        throw SecurityException("File not accessible via FileProvider")
    }
}
```

## 📱 File Opening System

### Intent-Based File Opening

```kotlin
fun openFile(context: Context, filePath: String): Boolean {
    return try {
        val file = File(filePath)
        
        // Validate file exists and is readable
        if (!file.exists() || !file.canRead()) {
            showToast(context, "File tidak ditemukan atau tidak dapat dibaca")
            return false
        }
        
        // Get content URI via FileProvider
        val uri = getContentUri(context, file)
        val mimeType = getMimeType(filePath)
        
        // Create intent
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, mimeType)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        
        // Check if there's an app to handle this intent
        val packageManager = context.packageManager
        if (intent.resolveActivity(packageManager) != null) {
            context.startActivity(intent)
            true
        } else {
            showToast(context, "Tidak ada aplikasi untuk membuka file ini")
            false
        }
        
    } catch (e: Exception) {
        android.util.Log.e("FileUtils", "Error opening file: ${e.message}", e)
        showToast(context, "Gagal membuka file: ${e.message}")
        false
    }
}
```

### App Recommendations

```kotlin
fun getRecommendedApps(mediaType: MediaType): List<AppRecommendation> {
    return when (mediaType) {
        MediaType.VIDEO -> listOf(
            AppRecommendation("VLC for Android", "org.videolan.vlc", "Universal video player"),
            AppRecommendation("MX Player", "com.mxtech.videoplayer.ad", "Popular video player"),
            AppRecommendation("Google Photos", "com.google.android.apps.photos", "Built-in viewer")
        )
        MediaType.AUDIO -> listOf(
            AppRecommendation("VLC for Android", "org.videolan.vlc", "Universal media player"),
            AppRecommendation("Google Play Music", "com.google.android.music", "Music player"),
            AppRecommendation("Poweramp", "com.maxmpz.audioplayer", "Advanced audio player")
        )
        MediaType.UNKNOWN -> emptyList()
    }
}

data class AppRecommendation(
    val name: String,
    val packageName: String,
    val description: String
)
```

## 📊 File Information and Statistics

### File Size Formatting

```kotlin
fun getFileSize(file: File): String {
    val bytes = file.length()
    return formatFileSize(bytes)
}

fun formatFileSize(bytes: Long): String {
    return when {
        bytes >= 1024 * 1024 * 1024 -> {
            String.format("%.1f GB", bytes / (1024.0 * 1024.0 * 1024.0))
        }
        bytes >= 1024 * 1024 -> {
            String.format("%.1f MB", bytes / (1024.0 * 1024.0))
        }
        bytes >= 1024 -> {
            String.format("%.1f KB", bytes / 1024.0)
        }
        else -> "$bytes B"
    }
}
```

### File Metadata

```kotlin
data class FileMetadata(
    val name: String,
    val path: String,
    val size: Long,
    val sizeFormatted: String,
    val mimeType: String,
    val mediaType: MediaType,
    val createdDate: Date,
    val modifiedDate: Date,
    val isReadable: Boolean,
    val isWritable: Boolean
)

fun getFileMetadata(file: File): FileMetadata {
    return FileMetadata(
        name = file.name,
        path = file.absolutePath,
        size = file.length(),
        sizeFormatted = formatFileSize(file.length()),
        mimeType = getMimeType(file.absolutePath),
        mediaType = getMediaType(file.absolutePath),
        createdDate = Date(file.lastModified()), // Android doesn't track creation time
        modifiedDate = Date(file.lastModified()),
        isReadable = file.canRead(),
        isWritable = file.canWrite()
    )
}
```

### Directory Statistics

```kotlin
data class DirectoryStats(
    val totalFiles: Int,
    val totalSize: Long,
    val totalSizeFormatted: String,
    val videoFiles: Int,
    val audioFiles: Int,
    val otherFiles: Int,
    val oldestFile: Date?,
    val newestFile: Date?
)

fun getDirectoryStats(directory: File): DirectoryStats {
    val files = directory.listFiles()?.filter { it.isFile } ?: emptyList()
    
    var videoCount = 0
    var audioCount = 0
    var otherCount = 0
    var totalSize = 0L
    var oldestTime = Long.MAX_VALUE
    var newestTime = Long.MIN_VALUE
    
    files.forEach { file ->
        totalSize += file.length()
        
        val modTime = file.lastModified()
        if (modTime < oldestTime) oldestTime = modTime
        if (modTime > newestTime) newestTime = modTime
        
        when (getMediaType(file.absolutePath)) {
            MediaType.VIDEO -> videoCount++
            MediaType.AUDIO -> audioCount++
            MediaType.UNKNOWN -> otherCount++
        }
    }
    
    return DirectoryStats(
        totalFiles = files.size,
        totalSize = totalSize,
        totalSizeFormatted = formatFileSize(totalSize),
        videoFiles = videoCount,
        audioFiles = audioCount,
        otherFiles = otherCount,
        oldestFile = if (oldestTime != Long.MAX_VALUE) Date(oldestTime) else null,
        newestFile = if (newestTime != Long.MIN_VALUE) Date(newestTime) else null
    )
}
```

## 🗑️ File Cleanup and Management

### Safe File Deletion

```kotlin
fun deleteFile(filePath: String): Boolean {
    return try {
        val file = File(filePath)
        if (file.exists()) {
            val deleted = file.delete()
            if (deleted) {
                android.util.Log.i("FileUtils", "Successfully deleted: $filePath")
            } else {
                android.util.Log.w("FileUtils", "Failed to delete: $filePath")
            }
            deleted
        } else {
            android.util.Log.w("FileUtils", "File does not exist: $filePath")
            true // Consider non-existent file as "deleted"
        }
    } catch (e: Exception) {
        android.util.Log.e("FileUtils", "Error deleting file: $filePath", e)
        false
    }
}
```

### Batch Operations

```kotlin
fun deleteMultipleFiles(filePaths: List<String>): BatchOperationResult {
    var successCount = 0
    var failureCount = 0
    val failures = mutableListOf<String>()
    
    filePaths.forEach { path ->
        if (deleteFile(path)) {
            successCount++
        } else {
            failureCount++
            failures.add(path)
        }
    }
    
    return BatchOperationResult(
        totalFiles = filePaths.size,
        successCount = successCount,
        failureCount = failureCount,
        failures = failures
    )
}

data class BatchOperationResult(
    val totalFiles: Int,
    val successCount: Int,
    val failureCount: Int,
    val failures: List<String>
)
```

### Cleanup Strategies

```kotlin
object FileCleanupManager {
    
    fun cleanupOldFiles(directory: File, maxAgeMs: Long): BatchOperationResult {
        val cutoffTime = System.currentTimeMillis() - maxAgeMs
        val oldFiles = directory.listFiles()
            ?.filter { it.isFile && it.lastModified() < cutoffTime }
            ?.map { it.absolutePath }
            ?: emptyList()
        
        return deleteMultipleFiles(oldFiles)
    }
    
    fun cleanupLargeFiles(directory: File, maxSizeBytes: Long): BatchOperationResult {
        val largeFiles = directory.listFiles()
            ?.filter { it.isFile && it.length() > maxSizeBytes }
            ?.map { it.absolutePath }
            ?: emptyList()
        
        return deleteMultipleFiles(largeFiles)
    }
    
    fun cleanupByCount(directory: File, maxFiles: Int): BatchOperationResult {
        val files = directory.listFiles()
            ?.filter { it.isFile }
            ?.sortedByDescending { it.lastModified() }
            ?: emptyList()
        
        if (files.size <= maxFiles) {
            return BatchOperationResult(0, 0, 0, emptyList())
        }
        
        val filesToDelete = files.drop(maxFiles).map { it.absolutePath }
        return deleteMultipleFiles(filesToDelete)
    }
}
```

## 🔒 Security and Permissions

### Permission Management

```kotlin
object PermissionManager {
    
    fun hasStoragePermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 11+ uses scoped storage
            Environment.isExternalStorageManager()
        } else {
            // Android 10 and below
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }
    
    fun requestStoragePermission(activity: Activity, requestCode: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Request manage external storage permission
            val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                data = Uri.parse("package:${activity.packageName}")
            }
            activity.startActivityForResult(intent, requestCode)
        } else {
            // Request traditional storage permission
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ),
                requestCode
            )
        }
    }
}
```

### File Access Validation

```kotlin
fun validateFileAccess(file: File): FileAccessResult {
    return when {
        !file.exists() -> FileAccessResult.NOT_EXISTS
        !file.canRead() -> FileAccessResult.NO_READ_PERMISSION
        file.isDirectory -> FileAccessResult.IS_DIRECTORY
        file.length() == 0L -> FileAccessResult.EMPTY_FILE
        else -> FileAccessResult.ACCESSIBLE
    }
}

enum class FileAccessResult {
    ACCESSIBLE,
    NOT_EXISTS,
    NO_READ_PERMISSION,
    IS_DIRECTORY,
    EMPTY_FILE
}
```

## 📱 Android Version Compatibility

### Scoped Storage Handling (Android 10+)

```kotlin
object ScopedStorageManager {
    
    fun isUsingLegacyStorage(): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.Q ||
                Environment.isExternalStorageLegacy()
    }
    
    fun getCompatibleDownloadDirectory(context: Context): File {
        return if (isUsingLegacyStorage()) {
            // Use public Downloads directory
            File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                DOWNLOAD_FOLDER
            )
        } else {
            // Use app-specific external directory
            File(
                context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),
                DOWNLOAD_FOLDER
            )
        }
    }
}
```

### MediaStore Integration (Android 10+)

```kotlin
object MediaStoreManager {
    
    fun addToMediaStore(context: Context, file: File): Uri? {
        return try {
            val values = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, file.name)
                put(MediaStore.MediaColumns.MIME_TYPE, getMimeType(file.absolutePath))
                put(MediaStore.MediaColumns.RELATIVE_PATH, "Download/$DOWNLOAD_FOLDER")
            }
            
            val collection = when (getMediaType(file.absolutePath)) {
                MediaType.VIDEO -> MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                MediaType.AUDIO -> MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                else -> MediaStore.Files.getContentUri("external")
            }
            
            context.contentResolver.insert(collection, values)
        } catch (e: Exception) {
            android.util.Log.e("MediaStore", "Failed to add file to MediaStore", e)
            null
        }
    }
}
```

## 🧪 Testing File Management

### Unit Tests

```kotlin
class FileUtilsTest {
    
    @Test
    fun `generateFileName creates valid filename`() {
        val result = FileUtils.generateFileName("Test Video [HD]", "mp4")
        
        assertTrue(result.endsWith(".mp4"))
        assertTrue(result.contains("Test_Video_HD"))
        assertFalse(result.contains("["))
        assertFalse(result.contains("]"))
    }
    
    @Test
    fun `getMimeType returns correct MIME type`() {
        assertEquals("video/mp4", FileUtils.getMimeType("video.mp4"))
        assertEquals("audio/mpeg", FileUtils.getMimeType("audio.mp3"))
        assertEquals("video/webm", FileUtils.getMimeType("video.webm"))
    }
    
    @Test
    fun `formatFileSize formats correctly`() {
        assertEquals("1.0 KB", FileUtils.formatFileSize(1024))
        assertEquals("1.0 MB", FileUtils.formatFileSize(1024 * 1024))
        assertEquals("1.0 GB", FileUtils.formatFileSize(1024 * 1024 * 1024))
    }
}
```

### Integration Tests

```kotlin
@RunWith(AndroidJUnit4::class)
class FileManagementIntegrationTest {
    
    @Test
    fun `file creation and opening works end to end`() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val downloadDir = FileUtils.getDownloadDirectory(context)
        
        // Create test file
        val testFile = File(downloadDir, "test_video.mp4")
        testFile.writeText("test content")
        
        // Verify file operations
        assertTrue(testFile.exists())
        assertTrue(FileUtils.validateFileAccess(testFile) == FileAccessResult.ACCESSIBLE)
        
        // Test content URI generation
        val contentUri = FileUtils.getContentUri(context, testFile)
        assertNotNull(contentUri)
        
        // Cleanup
        testFile.delete()
    }
}
```

---

**📁 File Management System** - Comprehensive file handling for secure, organized, and user-friendly file operations.

**Key Features:**
- ✅ **Organized Storage** - Structured directory management
- ✅ **FileProvider Integration** - Secure file sharing
- ✅ **MIME Type Detection** - Proper file type handling
- ✅ **Cross-Platform Compatibility** - Android version compatibility
- ✅ **Security First** - Permission-aware operations
- ✅ **User-Friendly** - Easy file access and management
