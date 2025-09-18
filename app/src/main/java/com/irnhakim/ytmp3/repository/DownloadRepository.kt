package com.irnhakim.ytmp3.repository

import android.content.Context
import com.irnhakim.ytmp3.data.DownloadFormat
import com.irnhakim.ytmp3.data.DownloadRequest
import com.irnhakim.ytmp3.data.DownloadState
import com.irnhakim.ytmp3.data.VideoFormat
import com.irnhakim.ytmp3.data.VideoInfo
import com.irnhakim.ytmp3.service.YouTubeExtractorService
import com.irnhakim.ytmp3.utils.FileUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DownloadRepository(private val context: Context) {
    
    private val youtubeExtractor = YouTubeExtractorService(context)
    private var isInitialized = false
    
    private suspend fun ensureInitialized() {
        if (!isInitialized) {
            val result = youtubeExtractor.initializeYoutubeDL()
            if (result.isSuccess) {
                isInitialized = true
            } else {
                throw Exception("Gagal menginisialisasi YouTube-DL: ${result.exceptionOrNull()?.message}")
            }
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
            
            // Ekstrak info video menggunakan YouTube-DL
            val extractResult = youtubeExtractor.extractVideoInfo(url)
            
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
            Result.failure(e)
        }
    }
    
    fun downloadVideo(request: DownloadRequest): Flow<DownloadState> = callbackFlow {
        trySend(DownloadState.Loading)

        // Jalankan pekerjaan pada IO dispatcher agar bisa memanggil suspend functions
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
                ensureInitialized()

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

                // Handler progress dari YoutubeDL
                val progressHandler: (Float, Long, String) -> Unit = { prog, _, status ->
                    val clamped = prog.coerceIn(0f, 100f)
                    val percent = (20 + (clamped * 0.8f)).toInt().coerceIn(20, 100)
                    val message = if (status.isBlank()) "Mengunduh..." else status
                    trySend(DownloadState.Progress(percent, message))
                }

                // Download berdasarkan format yang diminta
                val downloadResult = when (request.format) {
                    DownloadFormat.MP4 -> {
                        youtubeExtractor.downloadVideo(
                            url = request.url,
                            outputDir = downloadDir,
                            format = "best[ext=mp4]/best",
                            onProgress = progressHandler
                        )
                    }

                    DownloadFormat.MP3 -> {
                        youtubeExtractor.downloadAudio(
                            url = request.url,
                            outputDir = downloadDir,
                            onProgress = progressHandler
                        )
                    }
                }

                if (downloadResult.isSuccess) {
                    val filePath = downloadResult.getOrNull()!!
                    val fileName = java.io.File(filePath).name
                    trySend(DownloadState.Progress(100, "Download selesai!"))
                    trySend(DownloadState.Success(filePath, fileName))
                    close()
                } else {
                    trySend(
                        DownloadState.Error(
                            "Download gagal: ${downloadResult.exceptionOrNull()?.message}"
                        )
                    )
                    close(downloadResult.exceptionOrNull())
                }
            } catch (e: Exception) {
                trySend(DownloadState.Error("Error: ${e.message}"))
                close(e)
            }
        }

        awaitClose { job.cancel() }
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
}
