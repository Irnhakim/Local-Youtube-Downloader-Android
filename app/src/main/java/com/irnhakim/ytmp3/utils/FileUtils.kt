package com.irnhakim.ytmp3.utils

import android.content.Context
import android.os.Environment
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

object FileUtils {
    
    private const val DOWNLOAD_FOLDER = "YTMP3Downloads"
    
    fun getDownloadDirectory(context: Context): File {
        val downloadsDir = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            DOWNLOAD_FOLDER
        )
        if (!downloadsDir.exists()) {
            downloadsDir.mkdirs()
        }
        return downloadsDir
    }
    
    fun generateFileName(title: String, extension: String): String {
        val sanitizedTitle = title.replace(Regex("[^a-zA-Z0-9\\s]"), "")
            .replace(Regex("\\s+"), "_")
            .take(50)
        
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
            .format(Date())
        
        return "${sanitizedTitle}_${timestamp}.${extension}"
    }
    
    fun getFileSize(file: File): String {
        val bytes = file.length()
        return when {
            bytes >= 1024 * 1024 * 1024 -> String.format("%.1f GB", bytes / (1024.0 * 1024.0 * 1024.0))
            bytes >= 1024 * 1024 -> String.format("%.1f MB", bytes / (1024.0 * 1024.0))
            bytes >= 1024 -> String.format("%.1f KB", bytes / 1024.0)
            else -> "$bytes B"
        }
    }
    
    fun deleteFile(filePath: String): Boolean {
        return try {
            File(filePath).delete()
        } catch (e: Exception) {
            false
        }
    }
    
    // Validasi URL YouTube mencakup subdomain umum: www, m, mobile dan format watch/embed/v serta youtu.be
    fun isValidYouTubeUrl(url: String): Boolean {
        val youtubePattern = Regex(
            pattern = "^(https?://)?((www|m|mobile)\\.)?(youtube\\.com/(watch\\?v=|embed/|v/)|youtu\\.be/)[a-zA-Z0-9_-]{11}([&/?#].*)?\$"
        )
        return youtubePattern.matches(url)
    }
    
    fun extractVideoId(url: String): String? {
        val patterns = listOf(
            // youtu.be/<id>
            Regex("(?:^https?://)?(?:youtu\\.be/)([a-zA-Z0-9_-]{6,64})(?:[&/?#].*)?\$"),
            // youtube.com/watch?v=<id> (dengan subdomain www|m|mobile opsional)
            Regex("(?:^https?://)?(?:(?:www|m|mobile)\\.)?youtube\\.com/watch\\?v=([a-zA-Z0-9_-]{6,64})(?:[&?#/].*)?\$"),
            // youtube.com/embed/<id>
            Regex("(?:^https?://)?(?:(?:www|m|mobile)\\.)?youtube\\.com/embed/([a-zA-Z0-9_-]{6,64})(?:[&/?#].*)?\$"),
            // youtube.com/v/<id>
            Regex("(?:^https?://)?(?:(?:www|m|mobile)\\.)?youtube\\.com/v/([a-zA-Z0-9_-]{6,64})(?:[&/?#].*)?\$")
        )
        
        for (pattern in patterns) {
            val match = pattern.find(url)
            if (match != null) {
                return match.groupValues[1]
            }
        }
        return null
    }

    // Tentukan MIME type dari filePath
    fun getMimeType(filePath: String): String {
        val extension = filePath.substringAfterLast('.', "").lowercase(Locale.getDefault())
        val fromMap = android.webkit.MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
        if (!fromMap.isNullOrBlank()) return fromMap
        return when (extension) {
            "mp3" -> "audio/mpeg"
            "m4a", "aac" -> "audio/mp4"
            "wav" -> "audio/wav"
            "flac" -> "audio/flac"
            "mp4" -> "video/mp4"
            "mkv" -> "video/x-matroska"
            "webm" -> "video/webm"
            "3gp" -> "video/3gpp"
            else -> "*/*"
        }
    }

    // Buat content:// URI melalui FileProvider
    fun getContentUri(context: Context, file: File): android.net.Uri {
        return androidx.core.content.FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }

    // Buka file dengan aplikasi default perangkat (tanpa chooser)
    fun openFile(context: Context, filePath: String): Boolean {
        return try {
            val file = File(filePath)
            if (!file.exists()) {
                android.widget.Toast.makeText(context, "File tidak ditemukan", android.widget.Toast.LENGTH_SHORT).show()
                return false
            }
            val uri = getContentUri(context, file)
            val mime = getMimeType(filePath)
            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                setDataAndType(uri, mime)
                addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            val pm = context.packageManager
            if (intent.resolveActivity(pm) != null) {
                context.startActivity(intent)
                true
            } else {
                android.widget.Toast.makeText(context, "Tidak ada aplikasi untuk membuka file ini", android.widget.Toast.LENGTH_SHORT).show()
                false
            }
        } catch (e: Exception) {
            android.widget.Toast.makeText(context, "Gagal membuka file: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
            false
        }
    }
}
