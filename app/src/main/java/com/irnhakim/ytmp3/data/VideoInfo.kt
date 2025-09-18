package com.irnhakim.ytmp3.data

data class VideoInfo(
    val id: String,
    val title: String,
    val duration: String,
    val thumbnail: String,
    val uploader: String,
    val url: String,
    val formats: List<VideoFormat>
)

data class VideoFormat(
    val formatId: String,
    val ext: String,
    val quality: String,
    val filesize: Long?,
    val url: String,
    val acodec: String?,
    val vcodec: String?
)

sealed class DownloadState {
    object Idle : DownloadState()
    object Loading : DownloadState()
    data class Progress(val progress: Int, val status: String) : DownloadState()
    data class Success(val filePath: String, val fileName: String) : DownloadState()
    data class Error(val message: String) : DownloadState()
}

enum class DownloadFormat {
    MP4, MP3
}

data class DownloadRequest(
    val url: String,
    val format: DownloadFormat,
    val quality: String = "best"
)
