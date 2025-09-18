package com.irnhakim.ytmp3.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.irnhakim.ytmp3.data.*
import com.irnhakim.ytmp3.repository.CrashSafeDownloadRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CrashSafeDownloadViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository = CrashSafeDownloadRepository(application)
    
    private val _uiState = MutableStateFlow(DownloadUiState())
    val uiState: StateFlow<DownloadUiState> = _uiState.asStateFlow()
    
    private val _downloadState = MutableStateFlow<DownloadState>(DownloadState.Idle)
    val downloadState: StateFlow<DownloadState> = _downloadState.asStateFlow()
    
    private val _serviceStatus = MutableStateFlow("Unknown")
    val serviceStatus: StateFlow<String> = _serviceStatus.asStateFlow()
    
    init {
        // Update service status on initialization
        updateServiceStatus()
    }
    
    fun updateUrl(url: String) {
        _uiState.value = _uiState.value.copy(url = url, errorMessage = null)
    }
    
    fun updateFormat(format: DownloadFormat) {
        _uiState.value = _uiState.value.copy(selectedFormat = format)
    }
    
    fun getVideoInfo() {
        val currentUrl = _uiState.value.url
        if (currentUrl.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Masukkan URL YouTube")
            return
        }
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            try {
                repository.getVideoInfo(currentUrl).fold(
                    onSuccess = { videoInfo ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            videoInfo = videoInfo,
                            showVideoInfo = true
                        )
                    },
                    onFailure = { error ->
                        android.util.Log.e("CrashSafeDownloadVM", "Get video info failed", error)
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Terjadi kesalahan"
                        )
                    }
                )
            } catch (e: Exception) {
                android.util.Log.e("CrashSafeDownloadVM", "Get video info exception", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Error: ${e.message}"
                )
            }
        }
    }
    
    fun startDownload() {
        val videoInfo = _uiState.value.videoInfo ?: return
        val format = _uiState.value.selectedFormat
        
        val request = DownloadRequest(
            url = _uiState.value.url,
            format = format
        )
        
        viewModelScope.launch {
            try {
                repository.downloadVideo(request, videoInfo).collect { state ->
                    _downloadState.value = state
                    
                    // Update service status after download completion or error
                    if (state is DownloadState.Success || state is DownloadState.Error) {
                        updateServiceStatus()
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("CrashSafeDownloadVM", "Download error", e)
                _downloadState.value = DownloadState.Error("Error saat download: ${e.message}")
                updateServiceStatus()
            }
        }
    }
    
    fun resetDownload() {
        _downloadState.value = DownloadState.Idle
        _uiState.value = _uiState.value.copy(
            showVideoInfo = false,
            videoInfo = null
        )
        updateServiceStatus()
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
    
    fun resetCrashCount() {
        viewModelScope.launch {
            try {
                repository.resetCrashCount()
                updateServiceStatus()
                android.util.Log.i("CrashSafeDownloadVM", "Crash count reset")
            } catch (e: Exception) {
                android.util.Log.w("CrashSafeDownloadVM", "Failed to reset crash count", e)
            }
        }
    }
    
    private fun updateServiceStatus() {
        viewModelScope.launch {
            try {
                val status = repository.getServiceStatus()
                _serviceStatus.value = status
            } catch (e: Exception) {
                _serviceStatus.value = "Status unavailable: ${e.message}"
            }
        }
    }
    
    fun refreshServiceStatus() {
        updateServiceStatus()
    }
    
    // For debugging purposes
    fun getDownloadedFiles(): List<java.io.File> {
        return repository.getDownloadedFiles()
    }
}
