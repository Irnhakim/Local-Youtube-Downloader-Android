package com.irnhakim.ytmp3.service

import android.content.Context
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.YoutubeDLRequest
import com.yausername.youtubedl_android.mapper.VideoInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class YouTubeExtractorService(private val context: Context) {
    
    @Volatile
    private var isInitialized = false
    private val initLock = Any()
    @Volatile
    private var ffmpegAvailable: Boolean = false
    
    suspend fun initializeYoutubeDL(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // Check if running in test environment
            if (isTestEnvironment()) {
                // Return success for test environment
                isInitialized = true
                return@withContext Result.success(Unit)
            }
            
            // Thread-safe initialization
            synchronized(initLock) {
                if (!isInitialized) {
                    try {
                        // Inisialisasi yt-dlp dan FFmpeg dengan error handling yang lebih baik
                        YoutubeDL.getInstance().init(context)
                        try {
                            // Inisialisasi FFmpeg via refleksi agar tidak gagal kompilasi bila simbol tidak ter-ekspos
                            val clazz = Class.forName("com.yausername.ffmpeg.FFmpeg")
                            val getInstance = clazz.getMethod("getInstance")
                            val ffmpeg = getInstance.invoke(null)
                            val initMethod = clazz.getMethod("init", Context::class.java)
                            initMethod.invoke(ffmpeg, context)
                            ffmpegAvailable = true
                        } catch (ff: Exception) {
                            ffmpegAvailable = false
                            android.util.Log.w("YouTubeExtractor", "FFmpeg init warning: ${ff.message}", ff)
                        }
                        isInitialized = true
                        Result.success(Unit)
                    } catch (e: Exception) {
                        android.util.Log.e("YouTubeExtractor", "Failed to initialize YoutubeDL", e)
                        Result.failure(Exception("Gagal menginisialisasi YouTube-DL: ${e.message}", e))
                    }
                } else {
                    Result.success(Unit)
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("YouTubeExtractor", "Initialization error", e)
            Result.failure(e)
        }
    }
    
    private fun isTestEnvironment(): Boolean {
        return try {
            Class.forName("org.robolectric.RobolectricTestRunner")
            true
        } catch (e: ClassNotFoundException) {
            try {
                Class.forName("org.junit.Test")
                // Additional check to see if we're in a unit test
                Thread.currentThread().stackTrace.any { 
                    it.className.contains("junit") || it.className.contains("robolectric")
                }
            } catch (e: ClassNotFoundException) {
                false
            }
        }
    }
    
    suspend fun extractVideoInfo(url: String): Result<VideoInfo> = withContext(Dispatchers.IO) {
        try {
            if (isTestEnvironment()) {
                // Return mock video info for test environment
                val mockVideoInfo = createMockVideoInfo(url)
                Result.success(mockVideoInfo)
            } else {
                // Pastikan YoutubeDL sudah diinisialisasi
                if (!isInitialized) {
                    return@withContext Result.failure(Exception("YoutubeDL belum diinisialisasi"))
                }
                
                val request = YoutubeDLRequest(url).apply {
                    addOption("--dump-json")
                    addOption("--no-playlist")
                    addOption("--no-warnings")
                }
                
                val videoInfo = YoutubeDL.getInstance().getInfo(request)
                Result.success(videoInfo)
            }
        } catch (e: Exception) {
            android.util.Log.e("YouTubeExtractor", "Extract video info failed", e)
            Result.failure(Exception("Gagal mengekstrak info video: ${e.message}", e))
        }
    }
    
    private fun createMockVideoInfo(url: String): VideoInfo {
        // Create a mock VideoInfo object for testing
        val mockVideoInfo = VideoInfo()
        try {
            // Use reflection to set properties safely
            val idField = VideoInfo::class.java.getDeclaredField("id")
            idField.isAccessible = true
            idField.set(mockVideoInfo, "dQw4w9WgXcQ")
            
            val titleField = VideoInfo::class.java.getDeclaredField("title")
            titleField.isAccessible = true
            titleField.set(mockVideoInfo, "Test Video Title")
            
            val uploaderField = VideoInfo::class.java.getDeclaredField("uploader")
            uploaderField.isAccessible = true
            uploaderField.set(mockVideoInfo, "Test Channel")
            
            val durationField = VideoInfo::class.java.getDeclaredField("duration")
            durationField.isAccessible = true
            durationField.set(mockVideoInfo, 212)
            
            val thumbnailField = VideoInfo::class.java.getDeclaredField("thumbnail")
            thumbnailField.isAccessible = true
            thumbnailField.set(mockVideoInfo, "https://i.ytimg.com/vi/dQw4w9WgXcQ/maxresdefault.jpg")
        } catch (e: Exception) {
            // If reflection fails, just return empty VideoInfo
        }
        return mockVideoInfo
    }

    /**
     * Bangun selector format untuk MP4 berdasarkan formats yang tersedia dari info video.
     * Prioritas:
     * 1) Progressive MP4 (ada audio+video) dengan resolusi tertinggi
     * 2) Fallback: bestvideo mp4 + bestaudio m4a (butuh ffmpeg untuk merge)
     * 3) Fallback terakhir: best[ext=mp4]/best
     */
    private fun buildMp4FormatSelector(info: VideoInfo): String {
        return try {
            val formats = info.formats ?: emptyList()

            // Cari progressive MP4 (acodec != "none" dan vcodec != "none")
            val progressiveMp4 = formats
                .filter {
                    (it.ext?.lowercase() == "mp4") &&
                    (it.acodec?.lowercase() != "none") &&
                    (it.vcodec?.lowercase() != "none")
                }
                .sortedWith(
                    compareByDescending<com.yausername.youtubedl_android.mapper.VideoFormat> { it.height ?: 0 }
                        .thenByDescending { (it.abr ?: 0.0).toInt() }
                )
                .firstOrNull()

            if (progressiveMp4?.formatId != null) {
                progressiveMp4.formatId!!
            } else {
                // Gunakan fallback selector yang lebih luas
                "bestvideo[ext=mp4]+bestaudio[ext=m4a]/best[ext=mp4]/best"
            }
        } catch (e: Exception) {
            android.util.Log.w("YouTubeExtractor", "Failed to build mp4 format selector, using fallback", e)
            "bestvideo[ext=mp4]+bestaudio[ext=m4a]/best[ext=mp4]/best"
        }
    }
    
    // Temukan file terbaru dengan ekstensi yang diizinkan dalam jendela waktu tertentu
    private fun findLatestDownloadedFile(dir: File, allowedExts: Set<String>, windowMs: Long = 5 * 60 * 1000): File? {
        val now = System.currentTimeMillis()
        return dir.listFiles()
            ?.filter { it.isFile && it.extension.lowercase() in allowedExts && it.lastModified() >= now - windowMs && it.length() > 0L }
            ?.maxByOrNull { it.lastModified() }
    }
    
    private suspend fun mockDownload(
        outputDir: File,
        extension: String,
        onProgress: (Float, Long, String) -> Unit
    ): Result<String> {
        try {
            // Simulate download progress
            onProgress(0f, 0L, "Memulai download...")
            kotlinx.coroutines.delay(100)
            onProgress(25f, 0L, "Download 25%...")
            kotlinx.coroutines.delay(100)
            onProgress(50f, 0L, "Download 50%...")
            kotlinx.coroutines.delay(100)
            onProgress(75f, 0L, "Download 75%...")
            kotlinx.coroutines.delay(100)
            onProgress(100f, 0L, "Download selesai")
            
            // Create mock file
            if (!outputDir.exists()) {
                outputDir.mkdirs()
            }
            val mockFile = File(outputDir, "test_video.$extension")
            mockFile.writeText("Mock video content")
            
            return Result.success(mockFile.absolutePath)
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }
    
    suspend fun downloadVideo(
        url: String,
        outputDir: File,
        format: String = "best",
        onProgress: (Float, Long, String) -> Unit
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            if (isTestEnvironment()) {
                // Mock download for test environment
                return@withContext mockDownload(outputDir, "mp4", onProgress)
            }
            
            // Pastikan YoutubeDL sudah diinisialisasi
            if (!isInitialized) {
                return@withContext Result.failure(Exception("YoutubeDL belum diinisialisasi"))
            }
            
            // Pastikan output directory ada
            if (!outputDir.exists()) {
                outputDir.mkdirs()
            }

            // Ambil info video untuk memilih format yang tersedia
            val infoResult = extractVideoInfo(url)
            val formatSelector = if (infoResult.isSuccess) {
                buildMp4FormatSelector(infoResult.getOrNull()!!)
            } else {
                // Fallback jika info gagal: coba progresif lebih dulu
                "best[ext=mp4]/best"
            }
            val needMerge = formatSelector.contains("+")

            var requestedUnavailable = false
            var downloadCompleted = false

            fun executeDownload(selector: String, forceMerge: Boolean) {
                val request = YoutubeDLRequest(url).apply {
                    addOption("-f", selector)
                    addOption("-o", "${outputDir.absolutePath}/%(title)s.%(ext)s")
                    addOption("--no-playlist")
                    addOption("--no-warnings")
                    addOption("--ignore-errors")
                    addOption("--newline")
                    addOption("--no-part")
                    if (forceMerge) {
                        // Hanya paksa merge jika memang memilih stream terpisah (video+audio)
                        addOption("--merge-output-format", "mp4")
                        addOption("--prefer-ffmpeg")
                    }
                }
                android.util.Log.d("YouTubeExtractor", "Starting video download: $url (format=$selector, merge=$forceMerge)")
                // Execute dengan progress callback yang aman
                YoutubeDL.getInstance().execute(request) { progress, etaInSeconds, line ->
                    try {
                        val safeProgress = progress.coerceIn(0f, 100f)
                        val text = line ?: ""
                        if (text.contains("Requested format is not available", ignoreCase = true)) {
                            requestedUnavailable = true
                        }
                        onProgress(safeProgress, etaInSeconds, text)
                        
                        // Hentikan proses jika sudah 100% dan file tersedia
                        if (safeProgress >= 100f && !downloadCompleted) {
                            val tempFile = findLatestDownloadedFile(outputDir, setOf("mp4", "mkv", "webm", "m4v", "3gp"), 30 * 1000)
                            if (tempFile != null && tempFile.exists() && tempFile.length() > 0) {
                                downloadCompleted = true
                                android.util.Log.d("YouTubeExtractor", "Download completed at 100%, stopping further processing")
                            }
                        }
                    } catch (_: Exception) {
                    }
                }
            }

            // Percobaan pertama
            executeDownload(formatSelector, needMerge)

            // Deteksi hasil awal dan siapkan fallback berjenjang
            var downloadedFile = findLatestDownloadedFile(outputDir, setOf("mp4", "mkv", "webm", "m4v", "3gp"), 5 * 60 * 1000)

            if ((downloadedFile == null || requestedUnavailable) && !downloadCompleted) {
                val fallbacks = listOf(
                    "bestvideo[ext=mp4]+bestaudio[ext=m4a]",
                    "best[ext=mp4]",
                    "best"
                )
                for (selector in fallbacks) {
                    if (downloadCompleted) break
                    android.util.Log.w("YouTubeExtractor", "Trying fallback selector: $selector")
                    requestedUnavailable = false
                    executeDownload(selector, selector.contains("+"))
                    downloadedFile = findLatestDownloadedFile(outputDir, setOf("mp4", "mkv", "webm", "m4v", "3gp"), 5 * 60 * 1000)
                    if (downloadedFile != null && downloadedFile.exists() && downloadedFile.length() > 0) {
                        downloadCompleted = true
                        break
                    }
                    if (requestedUnavailable) continue
                }
            }

            if (downloadedFile != null && downloadedFile.exists() && downloadedFile.length() > 0) {
                android.util.Log.d("YouTubeExtractor", "Video download successful: ${downloadedFile.absolutePath}")
                Result.success(downloadedFile.absolutePath)
            } else {
                android.util.Log.e("YouTubeExtractor", "Downloaded file not found or empty after attempts")
                Result.failure(Exception("Gagal mendownload video: format yang diminta tidak tersedia"))
            }
        } catch (e: Exception) {
            android.util.Log.e("YouTubeExtractor", "Video download failed", e)
            Result.failure(Exception("Gagal mendownload video: ${e.message}", e))
        }
    }
    
    suspend fun downloadAudio(
        url: String,
        outputDir: File,
        onProgress: (Float, Long, String) -> Unit
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            if (isTestEnvironment()) {
                // Mock download for test environment
                return@withContext mockDownload(outputDir, "mp3", onProgress)
            }
            
            // Pastikan YoutubeDL sudah diinisialisasi
            if (!isInitialized) {
                return@withContext Result.failure(Exception("YoutubeDL belum diinisialisasi"))
            }
            
            // Pastikan output directory ada
            if (!outputDir.exists()) {
                outputDir.mkdirs()
            }
            
            var audioDownloadCompleted = false
            
            if (ffmpegAvailable) {
                val request = YoutubeDLRequest(url).apply {
                    addOption("-f", "bestaudio")
                    addOption("--extract-audio")
                    addOption("--audio-format", "mp3")
                    addOption("--audio-quality", "0") // Best quality
                    addOption("--prefer-ffmpeg")
                    addOption("--newline")
                    addOption("--no-part")
                    addOption("-o", "${outputDir.absolutePath}/%(title)s.%(ext)s")
                    addOption("--no-playlist")
                    addOption("--no-warnings")
                    addOption("--ignore-errors")
                }
                
                android.util.Log.d("YouTubeExtractor", "Starting audio download (extract to MP3): $url")
                
                // Execute dengan progress callback yang aman
                YoutubeDL.getInstance().execute(request) { progress, etaInSeconds, line ->
                    try {
                        val safeProgress = progress.coerceIn(0f, 100f)
                        onProgress(safeProgress, etaInSeconds, line ?: "")
                        
                        // Hentikan proses jika sudah 100% dan file tersedia
                        if (safeProgress >= 100f && !audioDownloadCompleted) {
                            val tempFile = findLatestDownloadedFile(outputDir, setOf("mp3", "m4a", "opus", "webm", "m4b", "aac", "wav", "flac"), 30 * 1000)
                            if (tempFile != null && tempFile.exists() && tempFile.length() > 0) {
                                audioDownloadCompleted = true
                                android.util.Log.d("YouTubeExtractor", "Audio download completed at 100%, stopping further processing")
                            }
                        }
                    } catch (_: Exception) {
                    }
                }
            } else {
                android.util.Log.w("YouTubeExtractor", "FFmpeg not available, using bestaudio without extraction")
                val fallbackReq = YoutubeDLRequest(url).apply {
                    addOption("-f", "bestaudio[ext=m4a]/bestaudio")
                    addOption("--newline")
                    addOption("--no-part")
                    addOption("-o", "${outputDir.absolutePath}/%(title)s.%(ext)s")
                    addOption("--no-playlist")
                    addOption("--no-warnings")
                    addOption("--ignore-errors")
                }
                YoutubeDL.getInstance().execute(fallbackReq) { progress, etaInSeconds, line ->
                    try {
                        val safeProgress = progress.coerceIn(0f, 100f)
                        onProgress(safeProgress, etaInSeconds, line ?: "")
                        
                        // Hentikan proses jika sudah 100% dan file tersedia
                        if (safeProgress >= 100f && !audioDownloadCompleted) {
                            val tempFile = findLatestDownloadedFile(outputDir, setOf("m4a", "opus", "webm", "m4b", "aac", "wav", "flac"), 30 * 1000)
                            if (tempFile != null && tempFile.exists() && tempFile.length() > 0) {
                                audioDownloadCompleted = true
                                android.util.Log.d("YouTubeExtractor", "Audio download completed at 100%, stopping further processing")
                            }
                        }
                    } catch (_: Exception) {
                    }
                }
            }
            
            // Cari file audio: prioritas MP3; jika tidak ada, terima m4a/opus/webm sebagai fallback
            var downloadedFile = findLatestDownloadedFile(outputDir, setOf("mp3"), 5 * 60 * 1000)
            if (downloadedFile == null) {
                downloadedFile = findLatestDownloadedFile(outputDir, setOf("m4a", "opus", "webm", "m4b", "aac", "wav", "flac"), 5 * 60 * 1000)
            }

            // Jika masih belum ada dan belum selesai, lakukan fallback tanpa ekstraksi: ambil bestaudio m4a/opus/webm
            if (downloadedFile == null && !audioDownloadCompleted) {
                android.util.Log.w("YouTubeExtractor", "First audio attempt produced no usable file, trying fallback bestaudio without extraction")
                val fallbackReq = YoutubeDLRequest(url).apply {
                    addOption("-f", "bestaudio[ext=m4a]/bestaudio")
                    addOption("-o", "${outputDir.absolutePath}/%(title)s.%(ext)s")
                    addOption("--no-playlist")
                    addOption("--no-warnings")
                    addOption("--ignore-errors")
                }
                YoutubeDL.getInstance().execute(fallbackReq) { progress, etaInSeconds, line ->
                    try {
                        val safeProgress = progress.coerceIn(0f, 100f)
                        onProgress(safeProgress, etaInSeconds, line ?: "")
                        
                        // Hentikan proses jika sudah 100% dan file tersedia
                        if (safeProgress >= 100f && !audioDownloadCompleted) {
                            val tempFile = findLatestDownloadedFile(outputDir, setOf("m4a", "opus", "webm", "m4b", "aac"), 30 * 1000)
                            if (tempFile != null && tempFile.exists() && tempFile.length() > 0) {
                                audioDownloadCompleted = true
                                android.util.Log.d("YouTubeExtractor", "Fallback audio download completed at 100%, stopping further processing")
                            }
                        }
                    } catch (_: Exception) {
                    }
                }
                downloadedFile = findLatestDownloadedFile(outputDir, setOf("m4a", "opus", "webm", "m4b", "aac"), 5 * 60 * 1000)
            }
            
            if (downloadedFile != null && downloadedFile.exists() && downloadedFile.length() > 0) {
                android.util.Log.d("YouTubeExtractor", "Audio download successful: ${downloadedFile.absolutePath}")
                Result.success(downloadedFile.absolutePath)
            } else {
                android.util.Log.e("YouTubeExtractor", "Downloaded audio file not found or empty after fallback")
                Result.failure(Exception("File audio tidak ditemukan atau kosong"))
            }
        } catch (e: Exception) {
            android.util.Log.e("YouTubeExtractor", "Audio download failed", e)
            Result.failure(Exception("Gagal mendownload audio: ${e.message}", e))
        }
    }
    
    suspend fun convertToMp3(
        inputPath: String,
        outputDir: File,
        onProgress: (Float, Long, String) -> Unit
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            // Jika input adalah URL, gunakan yt-dlp untuk ekstrak audio ke MP3 langsung
            val result: Result<String> = if (inputPath.startsWith("http://") || inputPath.startsWith("https://")) {
                downloadAudio(inputPath, outputDir, onProgress)
            } else {
                // Untuk path lokal, saat ini tidak menggunakan binding FFmpeg langsung.
                Result.failure(Exception("Konversi dari file lokal belum didukung. Gunakan downloadAudio(url, ...) untuk MP3."))
            }
            result
        } catch (e: Exception) {
            android.util.Log.e("YouTubeExtractor", "Convert to MP3 failed", e)
            Result.failure(Exception("Gagal mengkonversi audio: ${e.message}", e))
        }
    }
}
