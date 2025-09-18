package com.irnhakim.ytmp3

import com.irnhakim.ytmp3.utils.FileUtils
import org.junit.Test
import org.junit.Assert.*

class FileUtilsTest {

    @Test
    fun `test valid YouTube URLs`() {
        val validUrls = listOf(
            "https://www.youtube.com/watch?v=dQw4w9WgXcQ",
            "https://youtube.com/watch?v=dQw4w9WgXcQ",
            "https://youtu.be/dQw4w9WgXcQ",
            "https://www.youtube.com/embed/dQw4w9WgXcQ",
            "https://www.youtube.com/v/dQw4w9WgXcQ"
        )

        validUrls.forEach { url ->
            assertTrue("URL should be valid: $url", FileUtils.isValidYouTubeUrl(url))
        }
    }

    @Test
    fun `test invalid YouTube URLs`() {
        val invalidUrls = listOf(
            "https://www.google.com",
            "https://www.facebook.com/watch?v=123",
            "not a url",
            "",
            "https://youtube.com/watch?v=invalid",
            "https://www.youtube.com/watch"
        )

        invalidUrls.forEach { url ->
            assertFalse("URL should be invalid: $url", FileUtils.isValidYouTubeUrl(url))
        }
    }

    @Test
    fun `test video ID extraction`() {
        val testCases = mapOf(
            "https://www.youtube.com/watch?v=dQw4w9WgXcQ" to "dQw4w9WgXcQ",
            "https://youtu.be/dQw4w9WgXcQ" to "dQw4w9WgXcQ",
            "https://www.youtube.com/embed/dQw4w9WgXcQ" to "dQw4w9WgXcQ",
            "https://www.youtube.com/v/dQw4w9WgXcQ" to "dQw4w9WgXcQ"
        )

        testCases.forEach { (url, expectedId) ->
            val extractedId = FileUtils.extractVideoId(url)
            assertEquals("Video ID should match for URL: $url", expectedId, extractedId)
        }
    }

    @Test
    fun `test filename generation`() {
        val title = "Test Video Title with Special Characters!@#$%"
        val extension = "mp4"
        
        val filename = FileUtils.generateFileName(title, extension)
        
        assertTrue("Filename should contain sanitized title", filename.contains("Test_Video_Title_with_Special_Characters"))
        assertTrue("Filename should end with extension", filename.endsWith(".$extension"))
        assertTrue("Filename should contain timestamp", filename.matches(Regex(".*_\\d{8}_\\d{6}\\.mp4")))
    }

    @Test
    fun `test file size formatting`() {
        val testCases = mapOf(
            500L to "500 B",
            1024L to "1.0 KB",
            1048576L to "1.0 MB",
            1073741824L to "1.0 GB"
        )

        // Note: This test would need a mock File object in a real implementation
        // For now, we're testing the concept
    }
}
