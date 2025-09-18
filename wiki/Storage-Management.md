# 💾 Storage Management

Panduan lengkap manajemen penyimpanan untuk Local YouTube Downloader Android - lokasi file, organisasi, dan optimasi storage.

## 🎯 Overview

Storage Management System menangani semua aspek penyimpanan file dalam aplikasi, termasuk pemilihan lokasi, organisasi direktori, monitoring space, dan cleanup otomatis untuk memberikan pengalaman storage yang optimal.

## 📂 Storage Locations

### Default Storage Structure

```
📱 Device Storage
├── 📁 /storage/emulated/0/Download/
│   └── 📁 YTMP3Downloads/                    # Main download folder
│       ├── 🎥 Amazing_Video_20240101_143022.mp4
│       ├── 🎵 Great_Song_20240101_143055.mp3
│       ├── 🎬 Tutorial_Part1_20240101_150030.webm
│       ├── 🎼 Music_Track_20240101_151245.m4a
│       └── 📄 .nomedia                       # Hide from gallery
├── 📁 /Android/data/com.irnhakim.ytmp3/files/
│   └── 📁 Downloads/                         # App-specific storage
│       └── 📁 YTMP3Downloads/
└── 📁 /storage/[sdcard]/Download/            # External SD card
    └── 📁 YTMP3Downloads/
```

### Storage Location Manager

```kotlin
class StorageLocationManager(private val context: Context) {
    
    companion object {
        const val DOWNLOAD_FOLDER_NAME = "YTMP3Downloads"
        const val NOMEDIA_FILE = ".nomedia"
    }
    
    fun getAvailableStorageLocations(): List<StorageLocation> {
        val locations = mutableListOf<StorageLocation>()
        
        // Primary external storage (Downloads folder)
        val primaryLocation = getPrimaryStorageLocation()
        if (primaryLocation.isAvailable) {
            locations.add(primaryLocation)
        }
        
        // App-specific external storage
        val appSpecificLocation = getAppSpecificStorageLocation()
        if (appSpecificLocation.isAvailable) {
            locations.add(appSpecificLocation)
        }
        
        // Secondary external storage (SD Card)
        getSecondaryStorageLocations().forEach { location ->
            if (location.isAvailable) {
                locations.add(location)
            }
        }
        
        // Internal storage (fallback)
        val internalLocation = getInternalStorageLocation()
        locations.add(internalLocation)
        
        return locations
    }
    
    private fun getPrimaryStorageLocation(): StorageLocation {
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val ytmp3Dir = File(downloadsDir, DOWNLOAD_FOLDER_NAME)
        
        return StorageLocation(
            path = ytmp3Dir.absolutePath,
            type = StorageType.PRIMARY_EXTERNAL,
            displayName = "Downloads",
            isAvailable = isLocationAvailable(ytmp3Dir),
            freeSpace = getFreeSpace(ytmp3Dir),
            totalSpace = getTotalSpace(ytmp3Dir),
            isWritable = isLocationWritable(ytmp3Dir),
            requiresPermission = requiresStoragePermission()
        )
    }
    
    private fun getAppSpecificStorageLocation(): StorageLocation {
        val appDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
        val ytmp3Dir = File(appDir, DOWNLOAD_FOLDER_NAME)
        
        return StorageLocation(
            path = ytmp3Dir.absolutePath,
            type = StorageType.APP_SPECIFIC,
            displayName = "App Storage",
            isAvailable = isLocationAvailable(ytmp3Dir),
            freeSpace = getFreeSpace(ytmp3Dir),
            totalSpace = getTotalSpace(ytmp3Dir),
            isWritable = true, // Always writable for app-specific storage
            requiresPermission = false
        )
    }
    
    private fun getSecondaryStorageLocations(): List<StorageLocation> {
        val locations = mutableListOf<StorageLocation>()
        
        // Get all external file directories (includes SD cards)
        context.getExternalFilesDirs(Environment.DIRECTORY_DOWNLOADS)?.forEach { dir ->
            if (dir != null && !Environment.isExternalStorageEmulated(dir)) {
                val ytmp3Dir = File(dir, DOWNLOAD_FOLDER_NAME)
                locations.add(
                    StorageLocation(
                        path = ytmp3Dir.absolutePath,
                        type = StorageType.SECONDARY_EXTERNAL,
                        displayName = "SD Card",
                        isAvailable = isLocationAvailable(ytmp3Dir),
                        freeSpace = getFreeSpace(ytmp3Dir),
                        totalSpace = getTotalSpace(ytmp3Dir),
                        isWritable = isLocationWritable(ytmp3Dir),
                        requiresPermission = false
                    )
                )
            }
        }
        
        return locations
    }
    
    private fun getInternalStorageLocation(): StorageLocation {
        val internalDir = File(context.filesDir, DOWNLOAD_FOLDER_NAME)
        
        return StorageLocation(
            path = internalDir.absolutePath,
            type = StorageType.INTERNAL,
            displayName = "Internal Storage",
            isAvailable = true,
            freeSpace = getFreeSpace(internalDir),
            totalSpace = getTotalSpace(internalDir),
            isWritable = true,
            requiresPermission = false
        )
    }
    
    private fun isLocationAvailable(directory: File): Boolean {
        return try {
            directory.exists() || directory.mkdirs()
        } catch (e: Exception) {
            false
        }
    }
    
    private fun isLocationWritable(directory: File): Boolean {
        return try {
            if (!directory.exists()) {
                directory.mkdirs()
            }
            
            val testFile = File(directory, ".test_write")
            testFile.createNewFile()
            val canWrite = testFile.exists()
            testFile.delete()
            canWrite
        } catch (e: Exception) {
            false
        }
    }
    
    private fun getFreeSpace(directory: File): Long {
        return try {
            directory.usableSpace
        } catch (e: Exception) {
            0L
        }
    }
    
    private fun getTotalSpace(directory: File): Long {
        return try {
            directory.totalSpace
        } catch (e: Exception) {
            0L
        }
    }
    
    private fun requiresStoragePermission(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && 
               Build.VERSION.SDK_INT < Build.VERSION_CODES.R
    }
}

data class StorageLocation(
    val path: String,
    val type: StorageType,
    val displayName: String,
    val isAvailable: Boolean,
    val freeSpace: Long,
    val totalSpace: Long,
    val isWritable: Boolean,
    val requiresPermission: Boolean
) {
    val freeSpaceFormatted: String get() = formatBytes(freeSpace)
    val totalSpaceFormatted: String get() = formatBytes(totalSpace)
    val usagePercentage: Float get() = if (totalSpace > 0) ((totalSpace - freeSpace) * 100f / totalSpace) else 0f
}

enum class StorageType {
    PRIMARY_EXTERNAL,    // /storage/emulated/0/Download/
    APP_SPECIFIC,        // /Android/data/app/files/
    SECONDARY_EXTERNAL,  // SD Card
    INTERNAL            // Internal app storage
}
```

## 📊 Storage Monitoring

### Space Monitoring

```kotlin
class StorageMonitor(private val context: Context) {
    
    private val storageLocationManager = StorageLocationManager(context)
    
    fun getStorageStatus(): StorageStatus {
        val locations = storageLocationManager.getAvailableStorageLocations()
        val currentLocation = getCurrentStorageLocation()
        
        return StorageStatus(
            currentLocation = currentLocation,
            availableLocations = locations,
            totalFreeSpace = locations.sumOf { it.freeSpace },
            recommendedLocation = getRecommendedLocation(locations),
            warnings = generateStorageWarnings(locations),
            needsCleanup = needsStorageCleanup(currentLocation)
        )
    }
    
    fun checkSpaceRequirement(requiredBytes: Long): SpaceCheckResult {
        val currentLocation = getCurrentStorageLocation()
        val availableSpace = currentLocation.freeSpace
        val bufferSpace = 100 * 1024 * 1024L // 100MB buffer
        
        return when {
            availableSpace >= (requiredBytes + bufferSpace) -> {
                SpaceCheckResult.SUFFICIENT
            }
            availableSpace >= requiredBytes -> {
                SpaceCheckResult.TIGHT
            }
            else -> {
                SpaceCheckResult.INSUFFICIENT
            }
        }
    }
    
    fun getSpaceRecommendations(requiredBytes: Long): List<SpaceRecommendation> {
        val recommendations = mutableListOf<SpaceRecommendation>()
        val currentLocation = getCurrentStorageLocation()
        
        when (checkSpaceRequirement(requiredBytes)) {
            SpaceCheckResult.INSUFFICIENT -> {
                recommendations.add(
                    SpaceRecommendation(
                        type = RecommendationType.CLEANUP_REQUIRED,
                        title = "Free up space",
                        description = "Need ${formatBytes(requiredBytes - currentLocation.freeSpace)} more space",
                        action = { cleanupOldFiles() }
                    )
                )
                
                // Suggest alternative locations
                val alternativeLocations = storageLocationManager.getAvailableStorageLocations()
                    .filter { it.path != currentLocation.path && it.freeSpace >= requiredBytes }
                
                alternativeLocations.forEach { location ->
                    recommendations.add(
                        SpaceRecommendation(
                            type = RecommendationType.ALTERNATIVE_LOCATION,
                            title = "Use ${location.displayName}",
                            description = "${location.freeSpaceFormatted} available",
                            action = { switchStorageLocation(location) }
                        )
                    )
                }
            }
            SpaceCheckResult.TIGHT -> {
                recommendations.add(
                    SpaceRecommendation(
                        type = RecommendationType.CLEANUP_SUGGESTED,
                        title = "Consider cleanup",
                        description = "Low space remaining after download",
                        action = { cleanupOldFiles() }
                    )
                )
            }
            SpaceCheckResult.SUFFICIENT -> {
                // No recommendations needed
            }
        }
        
        return recommendations
    }
    
    private fun getCurrentStorageLocation(): StorageLocation {
        val currentPath = PreferenceManager.getDefaultSharedPreferences(context)
            .getString("storage_location", null)
        
        return if (currentPath != null) {
            storageLocationManager.getAvailableStorageLocations()
                .find { it.path == currentPath }
                ?: storageLocationManager.getAvailableStorageLocations().first()
        } else {
            storageLocationManager.getAvailableStorageLocations().first()
        }
    }
    
    private fun getRecommendedLocation(locations: List<StorageLocation>): StorageLocation {
        return locations
            .filter { it.isAvailable && it.isWritable }
            .maxByOrNull { 
                when (it.type) {
                    StorageType.PRIMARY_EXTERNAL -> 100 + it.freeSpace / 1024 / 1024 // Prefer primary + free space
                    StorageType.SECONDARY_EXTERNAL -> 80 + it.freeSpace / 1024 / 1024 // SD card
                    StorageType.APP_SPECIFIC -> 60 + it.freeSpace / 1024 / 1024 // App-specific
                    StorageType.INTERNAL -> 40 + it.freeSpace / 1024 / 1024 // Internal (last resort)
                }
            } ?: locations.first()
    }
    
    private fun generateStorageWarnings(locations: List<StorageLocation>): List<String> {
        val warnings = mutableListOf<String>()
        
        locations.forEach { location ->
            when {
                !location.isAvailable -> {
                    warnings.add("${location.displayName} is not available")
                }
                !location.isWritable -> {
                    warnings.add("${location.displayName} is not writable")
                }
                location.freeSpace < 500 * 1024 * 1024L -> { // Less than 500MB
                    warnings.add("${location.displayName} is running low on space (${location.freeSpaceFormatted} remaining)")
                }
                location.usagePercentage > 90 -> {
                    warnings.add("${location.displayName} is ${location.usagePercentage.toInt()}% full")
                }
            }
        }
        
        return warnings
    }
    
    private fun needsStorageCleanup(location: StorageLocation): Boolean {
        return location.freeSpace < 1024 * 1024 * 1024L || // Less than 1GB
               location.usagePercentage > 85 // More than 85% full
    }
}

data class StorageStatus(
    val currentLocation: StorageLocation,
    val availableLocations: List<StorageLocation>,
    val totalFreeSpace: Long,
    val recommendedLocation: StorageLocation,
    val warnings: List<String>,
    val needsCleanup: Boolean
)

enum class SpaceCheckResult {
    SUFFICIENT, TIGHT, INSUFFICIENT
}

data class SpaceRecommendation(
    val type: RecommendationType,
    val title: String,
    val description: String,
    val action: suspend () -> Unit
)

enum class RecommendationType {
    CLEANUP_REQUIRED,
    CLEANUP_SUGGESTED,
    ALTERNATIVE_LOCATION
}
```

### Storage Analytics

```kotlin
class StorageAnalytics(private val context: Context) {
    
    fun getStorageAnalytics(): StorageAnalyticsData {
        val downloadDir = FileUtils.getDownloadDirectory(context)
        val files = downloadDir.listFiles()?.filter { it.isFile } ?: emptyList()
        
        return StorageAnalyticsData(
            totalFiles = files.size,
            totalSize = files.sumOf { it.length() },
            videoFiles = files.count { isVideoFile(it) },
            audioFiles = files.count { isAudioFile(it) },
            averageFileSize = if (files.isNotEmpty()) files.sumOf { it.length() } / files.size else 0L,
            largestFile = files.maxByOrNull { it.length() },
            oldestFile = files.minByOrNull { it.lastModified() },
            newestFile = files.maxByOrNull { it.lastModified() },
            filesByMonth = groupFilesByMonth(files),
            sizeByFormat = groupSizeByFormat(files),
            downloadTrends = calculateDownloadTrends(files)
        )
    }
    
    fun getCleanupRecommendations(): List<CleanupRecommendation> {
        val recommendations = mutableListOf<CleanupRecommendation>()
        val downloadDir = FileUtils.getDownloadDirectory(context)
        val files = downloadDir.listFiles()?.filter { it.isFile } ?: emptyList()
        
        // Old files (30+ days)
        val oldFiles = files.filter { 
            System.currentTimeMillis() - it.lastModified() > TimeUnit.DAYS.toMillis(30) 
        }
        if (oldFiles.isNotEmpty()) {
            recommendations.add(
                CleanupRecommendation(
                    type = CleanupType.OLD_FILES,
                    title = "Delete old files",
                    description = "${oldFiles.size} files older than 30 days (${formatBytes(oldFiles.sumOf { it.length() })})",
                    files = oldFiles,
                    potentialSavings = oldFiles.sumOf { it.length() }
                )
            )
        }
        
        // Large files (100MB+)
        val largeFiles = files.filter { it.length() > 100 * 1024 * 1024L }
        if (largeFiles.isNotEmpty()) {
            recommendations.add(
                CleanupRecommendation(
                    type = CleanupType.LARGE_FILES,
                    title = "Review large files",
                    description = "${largeFiles.size} files larger than 100MB (${formatBytes(largeFiles.sumOf { it.length() })})",
                    files = largeFiles,
                    potentialSavings = largeFiles.sumOf { it.length() }
                )
            )
        }
        
        // Duplicate files (same name pattern)
        val duplicates = findDuplicateFiles(files)
        if (duplicates.isNotEmpty()) {
            recommendations.add(
                CleanupRecommendation(
                    type = CleanupType.DUPLICATES,
                    title = "Remove duplicates",
                    description = "${duplicates.size} potential duplicate files (${formatBytes(duplicates.sumOf { it.length() })})",
                    files = duplicates,
                    potentialSavings = duplicates.sumOf { it.length() }
                )
            )
        }
        
        return recommendations.sortedByDescending { it.potentialSavings }
    }
    
    private fun groupFilesByMonth(files: List<File>): Map<String, Int> {
        val formatter = SimpleDateFormat("yyyy-MM", Locale.getDefault())
        return files.groupBy { 
            formatter.format(Date(it.lastModified())) 
        }.mapValues { it.value.size }
    }
    
    private fun groupSizeByFormat(files: List<File>): Map<String, Long> {
        return files.groupBy { 
            it.extension.lowercase(Locale.getDefault()) 
        }.mapValues { entry -> 
            entry.value.sumOf { it.length() } 
        }
    }
    
    private fun calculateDownloadTrends(files: List<File>): DownloadTrends {
        val now = System.currentTimeMillis()
        val last7Days = files.filter { now - it.lastModified() <= TimeUnit.DAYS.toMillis(7) }
        val last30Days = files.filter { now - it.lastModified() <= TimeUnit.DAYS.toMillis(30) }
        
        return DownloadTrends(
            last7Days = last7Days.size,
            last30Days = last30Days.size,
            averagePerDay = if (files.isNotEmpty()) {
                val oldestTime = files.minOfOrNull { it.lastModified() } ?: now
                val daysSinceFirst = ((now - oldestTime) / TimeUnit.DAYS.toMillis(1)).coerceAtLeast(1)
                files.size.toFloat() / daysSinceFirst
            } else 0f,
            peakDownloadDay = findPeakDownloadDay(files)
        )
    }
    
    private fun findPeakDownloadDay(files: List<File>): String? {
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return files.groupBy { 
            formatter.format(Date(it.lastModified())) 
        }.maxByOrNull { it.value.size }?.key
    }
    
    private fun findDuplicateFiles(files: List<File>): List<File> {
        val duplicates = mutableListOf<File>()
        val seenNames = mutableSetOf<String>()
        
        files.forEach { file ->
            val baseName = file.nameWithoutExtension
                .replace(Regex("_\\d{8}_\\d{6}$"), "") // Remove timestamp
            
            if (seenNames.contains(baseName)) {
                duplicates.add(file)
            } else {
                seenNames.add(baseName)
            }
        }
        
        return duplicates
    }
    
    private fun isVideoFile(file: File): Boolean {
        val videoExtensions = setOf("mp4", "webm", "mkv", "avi", "mov", "m4v")
        return file.extension.lowercase(Locale.getDefault()) in videoExtensions
    }
    
    private fun isAudioFile(file: File): Boolean {
        val audioExtensions = setOf("mp3", "m4a", "opus", "aac", "wav", "flac")
        return file.extension.lowercase(Locale.getDefault()) in audioExtensions
    }
}

data class StorageAnalyticsData(
    val totalFiles: Int,
    val totalSize: Long,
    val videoFiles: Int,
    val audioFiles: Int,
    val averageFileSize: Long,
    val largestFile: File?,
    val oldestFile: File?,
    val newestFile: File?,
    val filesByMonth: Map<String, Int>,
    val sizeByFormat: Map<String, Long>,
    val downloadTrends: DownloadTrends
) {
    val totalSizeFormatted: String get() = formatBytes(totalSize)
    val averageFileSizeFormatted: String get() = formatBytes(averageFileSize)
}

data class DownloadTrends(
    val last7Days: Int,
    val last30Days: Int,
    val averagePerDay: Float,
    val peakDownloadDay: String?
)

data class CleanupRecommendation(
    val type: CleanupType,
    val title: String,
    val description: String,
    val files: List<File>,
    val potentialSavings: Long
) {
    val potentialSavingsFormatted: String get() = formatBytes(potentialSavings)
}

enum class CleanupType {
    OLD_FILES, LARGE_FILES, DUPLICATES
}
```

## 🧹 Storage Cleanup

### Automatic Cleanup

```kotlin
class StorageCleanupManager(private val context: Context) {
    
    private val preferences = PreferenceManager.getDefaultSharedPreferences(context)
    
    fun performAutomaticCleanup(): CleanupResult {
        val settings = getCleanupSettings()
        val downloadDir = FileUtils.getDownloadDirectory(context)
        val files = downloadDir.listFiles()?.filter { it.isFile } ?: emptyList()
        
        var deletedFiles = 0
        var freedSpace = 0L
        val errors = mutableListOf<String>()
        
        // Clean old files
        if (settings.deleteOldFiles) {
            val oldFiles = files.filter { 
                System.currentTimeMillis() - it.lastModified() > TimeUnit.DAYS.toMillis(settings.oldFileDays.toLong()) 
            }
            
            oldFiles.forEach { file ->
                try {
                    val fileSize = file.length()
                    if (file.delete()) {
                        deletedFiles++
                        freedSpace += fileSize
                    }
                } catch (e: Exception) {
                    errors.add("Failed to delete ${file.name}: ${e.message}")
                }
            }
        }
        
        // Clean large files if space is low
        if (settings.deleteLargeFilesWhenLowSpace && isStorageLow()) {
            val largeFiles = files
                .filter { it.length() > settings.largeFileThreshold }
                .sortedByDescending { it.length() }
                .take(5) // Limit to 5 largest files
            
            largeFiles.forEach { file ->
                try {
                    val fileSize = file.length()
                    if (file.delete()) {
                        deletedFiles++
                        freedSpace += fileSize
                    }
                } catch (e: Exception) {
                    errors.add("Failed to delete ${file.name}: ${e.message}")
                }
            }
        }
        
        // Update last cleanup time
        preferences.edit()
            .putLong("last_cleanup_time", System.currentTimeMillis())
            .apply()
        
        return CleanupResult(
            deletedFiles = deletedFiles,
            freedSpace = freedSpace,
            errors = errors,
            timestamp = System.currentTimeMillis()
        )
    }
    
    fun scheduleAutomaticCleanup() {
        val settings = getCleanupSettings()
        if (!settings.enableAutoCleanup) return
        
        val workRequest = PeriodicWorkRequestBuilder<CleanupWorker>(
            settings.cleanupInterval.toLong(), TimeUnit.DAYS
        ).setConstraints(
            Constraints.Builder()
                .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                .setRequiresBatteryNotLow(true)
                .build()
        ).build()
        
        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(
                "storage_cleanup",
                ExistingPeriodicWorkPolicy.REPLACE,
                workRequest
            )
    }
    
    fun performManualCleanup(recommendations: List<CleanupRecommendation>): CleanupResult {
        var deletedFiles = 0
        var freedSpace = 0L
        val errors = mutableListOf<String>()
        
        recommendations.forEach { recommendation ->
            recommendation.files.forEach { file ->
                try {
                    val fileSize = file.length()
                    if (file.delete()) {
                        deletedFiles++
                        freedSpace += fileSize
                    }
                } catch (e: Exception) {
                    errors.add("Failed to delete ${file.name}: ${e.message}")
                }
            }
        }
        
        return CleanupResult(
            deletedFiles = deletedFiles,
            freedSpace = freedSpace,
            errors = errors,
            timestamp = System.currentTimeMillis()
        )
    }
    
    private fun getCleanupSettings(): CleanupSettings {
        return CleanupSettings(
            enableAutoCleanup = preferences.getBoolean("auto_cleanup_enabled", false),
            deleteOldFiles = preferences.getBoolean("delete_old_files", true),
            oldFileDays = preferences.getInt("old_file_days", 30),
            deleteLargeFilesWhenLowSpace = preferences.getBoolean("delete_large_files_low_space", false),
            largeFileThreshold = preferences.getLong("large_file_threshold", 500 * 1024 * 1024L), // 500MB
            cleanupInterval = preferences.getInt("cleanup_interval_days", 7)
        )
    }
    
    private fun isStorageLow(): Boolean {
        val downloadDir = FileUtils.getDownloadDirectory(context)
        val freeSpace = downloadDir.usableSpace
        val totalSpace = downloadDir.totalSpace
        
        return freeSpace < totalSpace * 0.1 || // Less than 10% free
               freeSpace < 1024 * 1024 * 1024L   // Less than 1GB free
    }
}

data class CleanupSettings(
    val enableAutoCleanup: Boolean,
    val deleteOldFiles: Boolean,
    val oldFileDays: Int,
    val deleteLargeFilesWhenLowSpace: Boolean,
    val largeFileThreshold: Long,
    val cleanupInterval: Int
)

data class CleanupResult(
    val deletedFiles: Int,
    val freedSpace: Long,
    val errors: List<String>,
    val timestamp: Long
) {
    val freedSpaceFormatted: String get() = formatBytes(freedSpace)
    val isSuccessful: Boolean get() = errors.isEmpty()
}

class CleanupWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    
    override suspend fun doWork(): Result {
        return try {
            val cleanupManager = StorageCleanupManager(applicationContext)
            val result = cleanupManager.performAutomaticCleanup()
            
            if (result.isSuccessful) {
                Result.success()
            } else {
                Result.failure()
            }
        } catch (e: Exception) {
            Result.failure()
        }
    }
}
```

## 🎨 Storage UI Components

### Storage Selection Dialog

```kotlin
@Composable
fun StorageLocationDialog(
    locations: List<StorageLocation>,
    currentLocation: StorageLocation,
    onLocationSelected: (StorageLocation) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Choose Storage Location")
        },
        text = {
            LazyColumn {
                items(locations) { location ->
                    StorageLocationItem(
                        location = location,
                        isSelected = location.path == currentLocation.path,
                        onClick = { onLocationSelected(location) }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun StorageLocationItem(
    location: StorageLocation,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = location.displayName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                    Text(
                        text = location.path,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Selected",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Storage info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Free: ${location.freeSpaceFormatted}",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "Total: ${location.totalSpaceFormatted}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            
            // Progress bar
            LinearProgressIndicator(
                progress = { (location.usagePercentage / 100f).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                color = when {
                    location.usagePercentage > 90 -> MaterialTheme.colorScheme.error
                    location.
