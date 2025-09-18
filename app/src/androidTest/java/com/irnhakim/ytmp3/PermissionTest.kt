package com.irnhakim.ytmp3

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import com.irnhakim.ytmp3.utils.FileUtils
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class PermissionTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    @get:Rule
    val permissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE
    )

    @Test
    fun testStoragePermissionsGranted() {
        val writePermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        val readPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )

        assert(writePermission == PackageManager.PERMISSION_GRANTED) {
            "Write external storage permission should be granted"
        }
        assert(readPermission == PackageManager.PERMISSION_GRANTED) {
            "Read external storage permission should be granted"
        }
    }

    @Test
    fun testDownloadDirectoryCreation_withPermissions() {
        // Test that download directory can be created when permissions are granted
        val downloadDir = FileUtils.getDownloadDirectory(context)
        
        assert(downloadDir.exists() || downloadDir.mkdirs()) {
            "Download directory should be created successfully with permissions"
        }
        
        assert(downloadDir.canWrite()) {
            "Download directory should be writable with permissions"
        }
        
        assert(downloadDir.canRead()) {
            "Download directory should be readable with permissions"
        }
    }

    @Test
    fun testFileOperations_withPermissions() {
        val downloadDir = FileUtils.getDownloadDirectory(context)
        val testFile = File(downloadDir, "test_file.txt")
        
        try {
            // Test file creation
            val created = testFile.createNewFile()
            assert(created || testFile.exists()) {
                "Test file should be created successfully with permissions"
            }
            
            // Test file writing
            testFile.writeText("Test content")
            val content = testFile.readText()
            assert(content == "Test content") {
                "File content should be written and read correctly"
            }
            
            // Test file size calculation
            val fileSize = FileUtils.getFileSize(testFile)
            assert(fileSize.isNotEmpty()) {
                "File size should be calculated correctly"
            }
            
            // Test file deletion
            val deleted = FileUtils.deleteFile(testFile.absolutePath)
            assert(deleted) {
                "File should be deleted successfully with permissions"
            }
            
        } catch (e: Exception) {
            assert(false) { "File operations should work with proper permissions: ${e.message}" }
        }
    }

    @Test
    fun testDownloadDirectoryStructure() {
        val downloadDir = FileUtils.getDownloadDirectory(context)
        
        // Check if directory is in the expected location
        assert(downloadDir.absolutePath.contains("Downloads")) {
            "Download directory should be in Downloads folder"
        }
        
        assert(downloadDir.absolutePath.contains("YTMP3Downloads")) {
            "Download directory should have YTMP3Downloads subfolder"
        }
        
        // Test directory permissions
        assert(downloadDir.canRead()) { "Download directory should be readable" }
        assert(downloadDir.canWrite()) { "Download directory should be writable" }
        assert(downloadDir.canExecute()) { "Download directory should be executable" }
    }

    @Test
    fun testFileNameGeneration_withSpecialCharacters() {
        val problematicTitles = listOf(
            "Video with / slash",
            "Video with \\ backslash",
            "Video with : colon",
            "Video with * asterisk",
            "Video with ? question",
            "Video with \" quote",
            "Video with < less than",
            "Video with > greater than",
            "Video with | pipe",
            "Video with multiple    spaces",
            "Very long video title that exceeds the normal length limit and should be truncated properly to avoid filesystem issues"
        )

        problematicTitles.forEach { title ->
            val fileName = FileUtils.generateFileName(title, "mp4")
            
            // Check that filename doesn't contain problematic characters
            val problematicChars = listOf('/', '\\', ':', '*', '?', '"', '<', '>', '|')
            problematicChars.forEach { char ->
                assert(!fileName.contains(char)) {
                    "Generated filename should not contain '$char': $fileName"
                }
            }
            
            // Check that filename has reasonable length
            assert(fileName.length <= 100) {
                "Generated filename should not be too long: $fileName"
            }
            
            // Check that filename ends with correct extension
            assert(fileName.endsWith(".mp4")) {
                "Generated filename should end with .mp4: $fileName"
            }
        }
    }

    @Test
    fun testMultipleFileCreation() {
        val downloadDir = FileUtils.getDownloadDirectory(context)
        val testFiles = mutableListOf<File>()
        
        try {
            // Create multiple test files
            repeat(5) { index ->
                val fileName = FileUtils.generateFileName("Test Video $index", "mp4")
                val file = File(downloadDir, fileName)
                file.createNewFile()
                file.writeText("Test content for file $index")
                testFiles.add(file)
            }
            
            // Verify all files were created
            testFiles.forEach { file ->
                assert(file.exists()) { "File ${file.name} should exist" }
                assert(file.length() > 0) { "File ${file.name} should have content" }
            }
            
            // Test file size calculation for all files
            testFiles.forEach { file ->
                val size = FileUtils.getFileSize(file)
                assert(size.isNotEmpty()) { "File size should be calculated for ${file.name}" }
            }
            
        } finally {
            // Clean up test files
            testFiles.forEach { file ->
                FileUtils.deleteFile(file.absolutePath)
            }
        }
    }
}
