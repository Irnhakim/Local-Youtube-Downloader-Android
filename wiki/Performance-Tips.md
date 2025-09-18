# ⚡ Performance Tips

Panduan optimasi performa untuk Local YouTube Downloader Android - tips untuk developer dan pengguna.

## 🎯 Overview

Performance optimization mencakup berbagai aspek mulai dari memory management, network efficiency, storage optimization, hingga user experience improvements untuk memastikan aplikasi berjalan smooth dan responsive.

## 📱 User Performance Tips

### 🌐 Network Optimization

#### WiFi vs Mobile Data
```
✅ Recommended for Downloads:
- WiFi connection (stable, faster, unlimited)
- 5GHz WiFi band (less congested)
- Close to router (strong signal)

⚠️ Mobile Data Considerations:
- Check data plan limits
- Monitor data usage
- Use for small files only
```

#### Network Settings
```kotlin
// Optimal network conditions
Signal Strength: > -70 dBm
Download Speed: > 5 Mbps for HD videos
Upload Speed: > 1 Mbps (for app functionality)
Latency: < 100ms
```

#### Connection Tips
- **Close bandwidth-heavy apps** (streaming, cloud sync)
- **Pause automatic updates** during downloads
- **Use airplane mode trick**: Turn on airplane mode for 10 seconds, then turn off to reset connection
- **Restart router** if WiFi is slow
- **Switch DNS** to 8.8.8.8 or 1.1.1.1 for faster resolution

### 💾 Storage Optimization

#### Storage Management
```
Recommended Free Space:
- Video downloads: 1GB+ free space
- Audio downloads: 500MB+ free space
- App operation: 200MB+ free space

Storage Locations by Priority:
1. Internal storage (fastest)
2. High-speed SD card (Class 10+)
3. External storage (slower but more space)
```

#### Cleanup Strategies
```kotlin
// Regular maintenance
Weekly: Delete unwanted downloads
Monthly: Clear app cache
Quarterly: Move old files to cloud storage

// Automated cleanup
Settings > Storage > Smart cleanup
- Delete files older than 30 days
- Remove duplicates
- Compress large files
```

### 🔋 Battery Optimization

#### Power Management
```
Battery Optimization Settings:
- Disable battery optimization for YTMP3
- Keep screen on during downloads
- Use power saving mode after download starts
- Close unnecessary background apps
```

#### Charging Recommendations
```
✅ Best Practices:
- Charge device to 80%+ before large downloads
- Use original charger during long downloads
- Keep device cool (avoid direct sunlight)
- Enable adaptive battery in Android settings
```

### 📊 Device Performance

#### RAM Management
```
Minimum RAM Requirements:
- 2GB RAM: Basic functionality
- 4GB RAM: Smooth operation
- 6GB+ RAM: Optimal performance

RAM Optimization:
- Close unused apps before downloading
- Restart device weekly
- Clear RAM using device settings
- Avoid multitasking during downloads
```

#### CPU Optimization
```
CPU Performance Tips:
- Close CPU-intensive apps (games, video editors)
- Reduce screen brightness during downloads
- Disable live wallpapers
- Turn off unnecessary animations
```

## 🛠️ Developer Performance Tips

### 🏗️ Architecture Optimization

#### Memory Management
```kotlin
class DownloadViewModel : ViewModel() {
    
    // Use weak references for large objects
    private var videoInfoCache: WeakReference<VideoInfo>? = null
    
    // Limit collection sizes
    private val maxCacheSize = 50
    private val downloadHistory = LRUCache<String, DownloadState>(maxCacheSize)
    
    // Clean up resources
    override fun onCleared() {
        super.onCleared()
        downloadScope.cancel()
        videoInfoCache?.clear()
    }
}
```

#### Coroutine Optimization
```kotlin
class DownloadRepository {
    
    // Use appropriate dispatchers
    suspend fun downloadVideo(request: DownloadRequest): Flow<DownloadState> = flow {
        // IO operations
        withContext(Dispatchers.IO) {
            val videoInfo = extractVideoInfo(request.url)
            emit(DownloadState.Progress(10, "Info extracted"))
        }
        
        // CPU-intensive operations
        withContext(Dispatchers.Default) {
            val processedData = processVideoData(videoInfo)
            emit(DownloadState.Progress(50, "Processing"))
        }
        
        // Main thread updates
        withContext(Dispatchers.Main) {
            emit(DownloadState.Success(filePath, fileName))
        }
    }.flowOn(Dispatchers.IO) // Set default context
}
```

#### Flow Optimization
```kotlin
// Efficient flow operations
fun downloadVideo(request: DownloadRequest): Flow<DownloadState> = callbackFlow {
    
    // Buffer emissions to prevent backpressure
    val channel = Channel<DownloadState>(capacity = Channel.BUFFERED)
    
    // Use conflated flow for progress updates
    val progressFlow = callbackFlow<Float> {
        // Progress callback
        trySend(progress)
    }.conflate() // Only keep latest progress
    
    // Combine flows efficiently
    progressFlow
        .map { progress -> DownloadState.Progress(progress.toInt(), "Downloading") }
        .collect { state -> trySend(state) }
    
    awaitClose { channel.close() }
}
```

### 🎨 UI Performance

#### Compose Optimization
```kotlin
@Composable
fun DownloadScreen(
    viewModel: DownloadViewModel = viewModel()
) {
    // Collect state efficiently
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val downloadState by viewModel.downloadState.collectAsStateWithLifecycle()
    
    // Avoid unnecessary recompositions
    val stableVideoInfo = remember(uiState.videoInfo?.id) { uiState.videoInfo }
    
    // Use derivedStateOf for computed values
    val isDownloadEnabled by remember {
        derivedStateOf { 
            uiState.videoInfo != null && downloadState !is DownloadState.Loading 
        }
    }
    
    // Optimize list rendering
    LazyColumn {
        items(
            items = downloadHistory,
            key = { it.id } // Stable keys for better performance
        ) { item ->
            DownloadHistoryItem(
                item = item,
                modifier = Modifier.animateItemPlacement() // Smooth animations
            )
        }
    }
}

@Composable
fun VideoInfoCard(
    videoInfo: VideoInfo,
    modifier: Modifier = Modifier
) {
    // Avoid creating new objects in composition
    val cardColors = remember {
        CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
    
    Card(
        colors = cardColors,
        modifier = modifier
    ) {
        // Content
    }
}
```

#### Image Loading Optimization
```kotlin
@Composable
fun VideoThumbnail(
    thumbnailUrl: String,
    modifier: Modifier = Modifier
) {
    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(thumbnailUrl)
            .memoryCachePolicy(CachePolicy.ENABLED) // Enable memory cache
            .diskCachePolicy(CachePolicy.ENABLED)   // Enable disk cache
            .crossfade(true)                        // Smooth transitions
            .size(300, 200)                         // Resize for efficiency
            .build(),
        contentDescription = "Video thumbnail",
        modifier = modifier,
        contentScale = ContentScale.Crop
    )
}
```

### 🗄️ Data Management

#### Caching Strategy
```kotlin
class VideoInfoCache {
    
    private val memoryCache = LRUCache<String, VideoInfo>(50)
    private val diskCache = DiskLruCache.create(cacheDir, 1, 1, 10 * 1024 * 1024) // 10MB
    
    suspend fun getVideoInfo(url: String): VideoInfo? {
        // Check memory cache first
        memoryCache.get(url)?.let { return it }
        
        // Check disk cache
        return withContext(Dispatchers.IO) {
            diskCache.get(url)?.let { snapshot ->
                val videoInfo = Json.decodeFromString<VideoInfo>(snapshot.getString(0))
                memoryCache.put(url, videoInfo) // Update memory cache
                videoInfo
            }
        }
    }
    
    suspend fun putVideoInfo(url: String, videoInfo: VideoInfo) {
        memoryCache.put(url, videoInfo)
        
        withContext(Dispatchers.IO) {
            diskCache.edit(url)?.let { editor ->
                editor.setString(0, Json.encodeToString(videoInfo))
                editor.commit()
            }
        }
    }
}
```

#### Database Optimization
```kotlin
@Entity(tableName = "download_history")
data class DownloadHistoryEntity(
    @PrimaryKey val id: String,
    val title: String,
    val url: String,
    val filePath: String,
    val downloadDate: Long,
    val fileSize: Long
)

@Dao
interface DownloadHistoryDao {
    
    // Use indexed queries
    @Query("SELECT * FROM download_history WHERE url = :url")
    suspend fun getByUrl(url: String): DownloadHistoryEntity?
    
    // Limit results for performance
    @Query("SELECT * FROM download_history ORDER BY downloadDate DESC LIMIT :limit")
    fun getRecentDownloads(limit: Int = 50): Flow<List<DownloadHistoryEntity>>
    
    // Batch operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(downloads: List<DownloadHistoryEntity>)
    
    // Cleanup old entries
    @Query("DELETE FROM download_history WHERE downloadDate < :cutoffDate")
    suspend fun deleteOldEntries(cutoffDate: Long)
}
```

### 🌐 Network Performance

#### HTTP Client Optimization
```kotlin
class OptimizedHttpClient {
    
    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .connectionPool(ConnectionPool(5, 5, TimeUnit.MINUTES))
        .addInterceptor(createCacheInterceptor())
        .addNetworkInterceptor(createProgressInterceptor())
        .build()
    
    private fun createCacheInterceptor(): Interceptor {
        return Interceptor { chain ->
            val request = chain.request()
            val cacheControl = CacheControl.Builder()
                .maxAge(5, TimeUnit.MINUTES) // Cache for 5 minutes
                .build()
            
            val cachedRequest = request.newBuilder()
                .cacheControl(cacheControl)
                .build()
            
            chain.proceed(cachedRequest)
        }
    }
    
    private fun createProgressInterceptor(): Interceptor {
        return Interceptor { chain ->
            val originalRequest = chain.request()
            val originalResponse = chain.proceed(originalRequest)
            
            originalResponse.newBuilder()
                .body(ProgressResponseBody(originalResponse.body!!) { bytesRead, contentLength ->
                    val progress = (bytesRead * 100 / contentLength).toFloat()
                    // Update progress
                })
                .build()
        }
    }
}
```

#### Request Optimization
```kotlin
class YouTubeApiClient {
    
    // Batch requests when possible
    suspend fun getMultipleVideoInfo(urls: List<String>): List<VideoInfo> {
        return urls.chunked(5) { batch -> // Process in batches of 5
            batch.map { url ->
                async { getVideoInfo(url) }
            }.awaitAll()
        }.flatten()
    }
    
    // Use request deduplication
    private val ongoingRequests = mutableMapOf<String, Deferred<VideoInfo>>()
    
    suspend fun getVideoInfo(url: String): VideoInfo {
        return ongoingRequests[url] ?: run {
            val deferred = async { fetchVideoInfo(url) }
            ongoingRequests[url] = deferred
            
            try {
                deferred.await()
            } finally {
                ongoingRequests.remove(url)
            }
        }
    }
}
```

### 📁 File System Performance

#### Efficient File Operations
```kotlin
class OptimizedFileManager {
    
    // Use buffered streams for large files
    suspend fun copyFile(source: File, destination: File) = withContext(Dispatchers.IO) {
        source.inputStream().buffered(8192).use { input ->
            destination.outputStream().buffered(8192).use { output ->
                input.copyTo(output)
            }
        }
    }
    
    // Batch file operations
    suspend fun deleteMultipleFiles(files: List<File>) = withContext(Dispatchers.IO) {
        files.forEach { file ->
            try {
                file.delete()
            } catch (e: Exception) {
                // Log but continue with other files
                android.util.Log.w("FileManager", "Failed to delete ${file.name}: ${e.message}")
            }
        }
    }
    
    // Use memory-mapped files for large file operations
    suspend fun processLargeFile(file: File) = withContext(Dispatchers.IO) {
        RandomAccessFile(file, "r").use { randomAccessFile ->
            randomAccessFile.channel.use { channel ->
                val buffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size())
                // Process buffer efficiently
            }
        }
    }
}
```

#### Storage Monitoring
```kotlin
class StorageMonitor(private val context: Context) {
    
    fun getAvailableSpace(): Long {
        val downloadDir = FileUtils.getDownloadDirectory(context)
        return downloadDir.usableSpace
    }
    
    fun isSpaceSufficient(requiredBytes: Long): Boolean {
        val availableSpace = getAvailableSpace()
        val bufferSpace = 100 * 1024 * 1024 // 100MB buffer
        return availableSpace > (requiredBytes + bufferSpace)
    }
    
    suspend fun cleanupIfNeeded(requiredBytes: Long) {
        if (!isSpaceSufficient(requiredBytes)) {
            // Clean up old files
            val downloadDir = FileUtils.getDownloadDirectory(context)
            val oldFiles = downloadDir.listFiles()
                ?.filter { it.lastModified() < System.currentTimeMillis() - TimeUnit.DAYS.toMillis(30) }
                ?.sortedBy { it.lastModified() }
            
            var freedSpace = 0L
            oldFiles?.forEach { file ->
                if (freedSpace < requiredBytes) {
                    freedSpace += file.length()
                    file.delete()
                }
            }
        }
    }
}
```

## 📊 Performance Monitoring

### 🔍 Performance Metrics

#### Memory Monitoring
```kotlin
class MemoryMonitor {
    
    fun getCurrentMemoryUsage(): MemoryInfo {
        val runtime = Runtime.getRuntime()
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)
        
        return MemoryInfo(
            usedMemory = runtime.totalMemory() - runtime.freeMemory(),
            totalMemory = runtime.totalMemory(),
            maxMemory = runtime.maxMemory(),
            availableSystemMemory = memoryInfo.availMem,
            totalSystemMemory = memoryInfo.totalMem,
            isLowMemory = memoryInfo.lowMemory
        )
    }
    
    fun logMemoryUsage(tag: String) {
        val memInfo = getCurrentMemoryUsage()
        android.util.Log.d("MemoryMonitor", 
            "$tag - Used: ${formatBytes(memInfo.usedMemory)}, " +
            "Available: ${formatBytes(memInfo.availableSystemMemory)}, " +
            "Low Memory: ${memInfo.isLowMemory}"
        )
    }
}

data class MemoryInfo(
    val usedMemory: Long,
    val totalMemory: Long,
    val maxMemory: Long,
    val availableSystemMemory: Long,
    val totalSystemMemory: Long,
    val isLowMemory: Boolean
)
```

#### Performance Profiling
```kotlin
class PerformanceProfiler {
    
    private val measurements = mutableMapOf<String, MutableList<Long>>()
    
    inline fun <T> measureTime(operation: String, block: () -> T): T {
        val startTime = System.nanoTime()
        val result = block()
        val endTime = System.nanoTime()
        val duration = endTime - startTime
        
        measurements.getOrPut(operation) { mutableListOf() }.add(duration)
        
        if (BuildConfig.DEBUG) {
            android.util.Log.d("Performance", "$operation took ${duration / 1_000_000}ms")
        }
        
        return result
    }
    
    fun getAverageTime(operation: String): Double {
        return measurements[operation]?.average() ?: 0.0
    }
    
    fun getPerformanceReport(): String {
        return measurements.entries.joinToString("\n") { (operation, times) ->
            val avgMs = times.average() / 1_000_000
            val minMs = times.minOrNull()?.div(1_000_000) ?: 0.0
            val maxMs = times.maxOrNull()?.div(1_000_000) ?: 0.0
            "$operation: avg=${avgMs.format(2)}ms, min=${minMs.format(2)}ms, max=${maxMs.format(2)}ms"
        }
    }
}

// Usage
val profiler = PerformanceProfiler()

val videoInfo = profiler.measureTime("extractVideoInfo") {
    youtubeExtractor.extractVideoInfo(url)
}
```

### 📈 Performance Testing

#### Automated Performance Tests
```kotlin
@RunWith(AndroidJUnit4::class)
class PerformanceTest {
    
    @Test
    fun urlValidation_performanceTest() {
        val urls = generateTestUrls(1000)
        val profiler = PerformanceProfiler()
        
        val results = profiler.measureTime("urlValidation") {
            urls.map { FileUtils.isValidYouTubeUrl(it) }
        }
        
        val avgTimeMs = profiler.getAverageTime("urlValidation") / 1_000_000
        assertTrue("URL validation too slow: ${avgTimeMs}ms", avgTimeMs < 10)
    }
    
    @Test
    fun memoryUsage_downloadFlow() {
        val memoryMonitor = MemoryMonitor()
        val initialMemory = memoryMonitor.getCurrentMemoryUsage()
        
        // Simulate multiple downloads
        repeat(5) {
            simulateDownload()
            System.gc() // Force garbage collection
            Thread.sleep(100)
        }
        
        val finalMemory = memoryMonitor.getCurrentMemoryUsage()
        val memoryIncrease = finalMemory.usedMemory - initialMemory.usedMemory
        
        assertTrue(
            "Memory leak detected: ${formatBytes(memoryIncrease)} increase",
            memoryIncrease < 50 * 1024 * 1024 // 50MB threshold
        )
    }
}
```

## 🎯 Performance Best Practices

### ✅ Do's

#### Code Optimization
```kotlin
// ✅ Use efficient data structures
val videoCache = LRUCache<String, VideoInfo>(50)

// ✅ Avoid creating objects in loops
val stringBuilder = StringBuilder()
for (item in items) {
    stringBuilder.append(item.toString())
}

// ✅ Use lazy initialization
val expensiveObject by lazy { createExpensiveObject() }

// ✅ Cache expensive computations
private val mimeTypeCache = mutableMapOf<String, String>()
fun getMimeType(extension: String): String {
    return mimeTypeCache.getOrPut(extension) {
        computeMimeType(extension)
    }
}
```

#### Resource Management
```kotlin
// ✅ Use try-with-resources
file.inputStream().use { input ->
    // Process file
}

// ✅ Cancel coroutines properly
class DownloadViewModel : ViewModel() {
    private val downloadJob = SupervisorJob()
    private val downloadScope = CoroutineScope(Dispatchers.IO + downloadJob)
    
    override fun onCleared() {
        downloadJob.cancel()
        super.onCleared()
    }
}

// ✅ Use appropriate thread pools
val downloadDispatcher = Dispatchers.IO.limitedParallelism(3)
```

### ❌ Don'ts

#### Performance Anti-patterns
```kotlin
// ❌ Don't create objects in tight loops
for (i in 0..1000) {
    val list = mutableListOf<String>() // Creates 1000 lists
    // Process
}

// ❌ Don't block main thread
fun loadData() {
    runBlocking { // Blocks UI thread
        repository.getData()
    }
}

// ❌ Don't ignore memory leaks
class LeakyViewModel : ViewModel() {
    private val context: Context // Holds reference to Activity
    
    // Should use Application context instead
}

// ❌ Don't use inefficient string operations
var result = ""
for (item in items) {
    result += item.toString() // Creates new string each time
}
```

## 🔧 Performance Tools

### 📱 Android Profiler
```
Memory Profiler:
- Monitor memory allocation
- Detect memory leaks
- Analyze heap dumps

CPU Profiler:
- Identify performance bottlenecks
- Analyze method execution time
- Monitor thread activity

Network Profiler:
- Track network requests
- Monitor data usage
- Analyze request/response times
```

### 🛠️ Development Tools
```kotlin
// LeakCanary for memory leak detection
dependencies {
    debugImplementation 'com.squareup.leakcanary:leakcanary-android:2.12'
}

// Flipper for debugging
dependencies {
    debugImplementation 'com.facebook.flipper:flipper:0.182.0'
    debugImplementation 'com.facebook.flipper:flipper-network-plugin:0.182.0'
}

// Chucker for network monitoring
dependencies {
    debugImplementation 'com.github.chuckerteam.chucker:library:4.0.0'
    releaseImplementation 'com.github.chuckerteam.chucker:library-no-op:4.0.0'
}
```

---

**⚡ Performance Optimization** - Comprehensive guide for optimal app performance across all aspects.

**Key Areas:**
- ✅ **User Experience** - Network, storage, and battery optimization
- ✅ **Code Efficiency** - Memory management and algorithm optimization
- ✅ **UI Performance** - Smooth animations and responsive interface
- ✅ **Data Management** - Efficient caching and storage strategies
- ✅ **Monitoring** - Performance metrics and profiling tools
- ✅ **Best Practices** - Do's and don'ts for optimal performance
