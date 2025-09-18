package com.irnhakim.ytmp3.repository

import android.content.Context
import com.irnhakim.ytmp3.data.DownloadFormat
import com.irnhakim.ytmp3.data.DownloadRequest
import com.irnhakim.ytmp3.data.DownloadState
import com.irnhakim.ytmp3.data.VideoFormat
import com.irnhakim.ytmp3.data.VideoInfo
import com.irnhakim.ytmp3.service.CrashSafeYouTubeExtractorService
import com.irnhakim.ytmp3.utils.FileUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CrashSafeDownloadRepository(private val context: Context) {
    
    private suspend fun getYoutubeExtractor(): CrashSafeYouTubeExtractorService {
        return CrashSafeYouTubeExtractorService.getInstance(context)
    }
    
    private suspend fun ensureInitialized() {
        val extractor = getYoutubeExtractor()
        val result = extractor.initializeYoutubeDL()
        if (result.isFailure) {
            throw Exception("Gagal menginisialisasi YouTube-DL: ${result.exceptionOrNull()?.message}")
        }
    }
    
    suspend fun getVideoInfo(url: String): Result<VideoInfo> = withContext(Dispatchers.IO) {
        try {
            // Validasi URL
            if (!FileUtils.isValidYouTubeUrl(url)) {
                return@withContext Result.failure(Exception("URL YouTube tidak valid"))
            }
            
            // Pastikan YouTube-DL sudah diinisialisasi
            ensureInitialized()
            
            val extractor = getYoutubeExtractor()
            
            // Ekstrak info video menggunakan crash-safe service
            val extractResult = extractor.extractVideoInfo(url)
            
            if (extractResult.isFailure) {
                return@withContext Result.failure(extractResult.exceptionOrNull()!!)
            }
            
            val ytVideoInfo = extractResult.getOrNull()!!
            
            // Konversi ke format internal
            val videoInfo = VideoInfo(
                id = ytVideoInfo.id ?: "unknown",
                title = ytVideoInfo.title ?: "Unknown Title",
                duration = formatDuration(ytVideoInfo.duration ?: 0),
                thumbnail = ytVideoInfo.thumbnail ?: "",
                uploader = ytVideoInfo.uploader ?: "Unknown",
                url = url,
                formats = convertFormats(ytVideoInfo.formats ?: emptyList())
            )
            
            Result.success(videoInfo)
        } catch (e: Exception) {
            android.util.Log.e("CrashSafeDownloadRepo", "Get video info failed", e)
            Result.failure(e)
        }
    }
    
    fun downloadVideo(request: DownloadRequest): Flow<DownloadState> = callbackFlow {
        trySend(DownloadState.Loading)

        val job = launch(Dispatchers.IO) {
            try {
                // Validasi URL
                if (!FileUtils.isValidYouTubeUrl(request.url)) {
                    trySend(DownloadState.Error("URL YouTube tidak valid"))
                    close()
                    return@launch
                }

                // Pastikan YouTube-DL sudah diinisialisasi
                trySend(DownloadState.Progress(5, "Menginisialisasi..."))
                
                try {
                    ensureInitialized()
                } catch (e: Exception) {
                    trySend(DownloadState.Error("Gagal menginisialisasi: ${e.message}"))
                    close()
                    return@launch
                }

                // Dapatkan info video
                trySend(DownloadState.Progress(10, "Mengekstrak informasi video..."))
                val videoInfoResult = getVideoInfo(request.url)
                if (videoInfoResult.isFailure) {
                    trySend(
                        DownloadState.Error(
                            "Gagal mendapatkan info video: ${videoInfoResult.exceptionOrNull()?.message}"
                        )
                    )
                    close()
                    return@launch
                }

                // Buat direktori download
                val downloadDir = FileUtils.getDownloadDirectory(context)
                trySend(DownloadState.Progress(20, "Memulai download..."))

                val extractor = getYoutubeExtractor()

                // Handler progress dari YoutubeDL dengan error handling yang lebih baik
                val progressHandler: (Float, Long, String) -> Unit = { prog, _, status ->
                    try {
                        val clamped = prog.coerceIn(0f, 100f)
                        val percent = (20 + (clamped * 0.8f)).toInt().coerceIn(20, 100)
                        val message = if (status.isBlank()) "Mengunduh..." else status.take(100)
                        trySend(DownloadState.Progress(percent, message))
                    } catch (e: Exception) {
                        android.util.Log.w("CrashSafeDownloadRepo", "Progress handler error", e)
                    }
                }

                // Download berdasarkan format yang diminta dengan crash protection
                val downloadResult = try {
                    when (request.format) {
                        DownloadFormat.MP4 -> {
                            extractor.downloadVideo(
                                url = request.url,
                                outputDir = downloadDir,
                                format = "best[ext=mp4]/best",
                                onProgress = progressHandler
                            )
                        }

                        DownloadFormat.MP3 -> {
                            extractor.downloadAudio(
                                url = request.url,
                                outputDir = downloadDir,
                                onProgress = progressHandler
                            )
                        }
                    }
                } catch (e: Exception) {
                    android.util.Log.e("CrashSafeDownloadRepo", "Download execution failed", e)
                    Result.failure(Exception("Download gagal: ${e.message}", e))
                }

                if (downloadResult.isSuccess) {
                    val filePath = downloadResult.getOrNull()!!
                    val fileName = java.io.File(filePath).name
                    trySend(DownloadState.Progress(100, "Download selesai!"))
                    trySend(DownloadState.Success(filePath, fileName))
                    close()
                } else {
                    val error = downloadResult.exceptionOrNull()
                    android.util.Log.e("CrashSafeDownloadRepo", "Download failed", error)
                    trySend(
                        DownloadState.Error(
                            "Download gagal: ${error?.message ?: "Unknown error"}"
                        )
                    )
                    close(error)
                }
            } catch (e: Exception) {
                android.util.Log.e("CrashSafeDownloadRepo", "Download job failed", e)
                trySend(DownloadState.Error("Error: ${e.message}"))
                close(e)
            }
        }

        awaitClose { 
            job.cancel()
            android.util.Log.d("CrashSafeDownloadRepo", "Download flow closed")
        }
    }
    
    // Overload untuk backward compatibility
    fun downloadVideo(request: DownloadRequest, videoInfo: VideoInfo): Flow<DownloadState> {
        return downloadVideo(request)
    }
    
    private fun formatDuration(seconds: Int): String {
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        val secs = seconds % 60
        
        return if (hours > 0) {
            String.format("%d:%02d:%02d", hours, minutes, secs)
        } else {
            String.format("%d:%02d", minutes, secs)
        }
    }
    
    private fun convertFormats(ytFormats: List<com.yausername.youtubedl_android.mapper.VideoFormat>): List<VideoFormat> {
        return ytFormats.map { ytFormat ->
            VideoFormat(
                formatId = ytFormat.formatId ?: "unknown",
                ext = ytFormat.ext ?: "unknown",
                quality = ytFormat.height?.let { "${it}p" } ?: ytFormat.abr?.let { "${it}kbps" } ?: "unknown",
                filesize = null,
                url = ytFormat.url ?: "",
                acodec = ytFormat.acodec,
                vcodec = ytFormat.vcodec
            )
        }
    }
    
    fun getDownloadedFiles(): List<java.io.File> {
        val downloadDir = FileUtils.getDownloadDirectory(context)
        return downloadDir.listFiles()?.toList() ?: emptyList()
    }
    
    suspend fun getServiceStatus(): String {
        return try {
            val extractor = getYoutubeExtractor()
            extractor.getStatus()
        } catch (e: Exception) {
            "Service unavailable: ${e.message}"
        }
    }
    
    suspend fun resetCrashCount() {
        try {
            val extractor = getYoutubeExtractor()
            extractor.resetCrashCount()
        } catch (e: Exception) {
            android.util.Log.w("CrashSafeDownloadRepo", "Failed to reset crash count", e)
        }
    }
}
