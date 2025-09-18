package com.irnhakim.ytmp3

import com.irnhakim.ytmp3.data.DownloadFormat
import com.irnhakim.ytmp3.data.DownloadRequest
import com.irnhakim.ytmp3.utils.FileUtils
import org.junit.Test
import org.junit.Assert.*

/**
 * Basic functionality tests for core components
 */
class BasicFunctionalityTest {

    @Test
    fun testYouTubeUrlValidation() {
        // Valid URLs
        assertTrue(FileUtils.isValidYouTubeUrl("https://www.youtube.com/watch?v=dQw4w9WgXcQ"))
        assertTrue(FileUtils.isValidYouTubeUrl("https://youtu.be/dQw4w9WgXcQ"))
        
        // Invalid URLs
        assertFalse(FileUtils.isValidYouTubeUrl("https://www.google.com"))
        assertFalse(FileUtils.isValidYouTubeUrl("not a url"))
        assertFalse(FileUtils.isValidYouTubeUrl(""))
    }

    @Test
    fun testVideoIdExtraction() {
        val url = "https://www.youtube.com/watch?v=dQw4w9WgXcQ"
        val videoId = FileUtils.extractVideoId(url)
        assertEquals("dQw4w9WgXcQ", videoId)
        
        val shortUrl = "https://youtu.be/dQw4w9WgXcQ"
        val videoId2 = FileUtils.extractVideoId(shortUrl)
        assertEquals("dQw4w9WgXcQ", videoId2)
    }

    @Test
    fun testFileNameGeneration() {
        val title = "Test Video Title"
        val extension = "mp4"
        val fileName = FileUtils.generateFileName(title, extension)
        
        assertTrue(fileName.contains("Test_Video_Title"))
        assertTrue(fileName.endsWith(".mp4"))
    }

    @Test
    fun testDownloadRequestCreation() {
        val request = DownloadRequest(
            url = "https://www.youtube.com/watch?v=test123",
            format = DownloadFormat.MP4,
            quality = "720p"
        )
        
        assertEquals("https://www.youtube.com/watch?v=test123", request.url)
        assertEquals(DownloadFormat.MP4, request.format)
        assertEquals("720p", request.quality)
    }

    @Test
    fun testDownloadFormatEnum() {
        val formats = DownloadFormat.values()
        assertEquals(2, formats.size)
        assertTrue(formats.contains(DownloadFormat.MP4))
        assertTrue(formats.contains(DownloadFormat.MP3))
    }
}
