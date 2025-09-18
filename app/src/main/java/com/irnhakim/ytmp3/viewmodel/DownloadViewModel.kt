package com.irnhakim.ytmp3.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.irnhakim.ytmp3.data.*
import com.irnhakim.ytmp3.repository.DownloadRepository
import com.irnhakim.ytmp3.utils.FileUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DownloadViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository = DownloadRepository(application)
    
    private val _uiState = MutableStateFlow(DownloadUiState())
    val uiState: StateFlow<DownloadUiState> = _uiState.asStateFlow()
    
    private val _downloadState = MutableStateFlow<DownloadState>(DownloadState.Idle)
    val downloadState: StateFlow<DownloadState> = _downloadState.asStateFlow()
    
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
        // Validasi URL secara sinkron agar UI langsung menampilkan error pada URL tidak valid
        if (!FileUtils.isValidYouTubeUrl(currentUrl)) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                errorMessage = "URL YouTube tidak valid"
            )
            return
        }
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            repository.getVideoInfo(currentUrl).fold(
                onSuccess = { videoInfo ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        videoInfo = videoInfo,
                        showVideoInfo = true
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Terjadi kesalahan"
                    )
                }
            )
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
                }
            } catch (e: Exception) {
                android.util.Log.e("DownloadViewModel", "Download error", e)
                _downloadState.value = DownloadState.Error("Error saat download: ${e.message}")
            }
        }
    }
    
    fun resetDownload() {
        _downloadState.value = DownloadState.Idle
        _uiState.value = _uiState.value.copy(
            showVideoInfo = false,
            videoInfo = null
        )
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}

data class DownloadUiState(
    val url: String = "",
    val selectedFormat: DownloadFormat = DownloadFormat.MP4,
    val isLoading: Boolean = false,
    val videoInfo: VideoInfo? = null,
    val showVideoInfo: Boolean = false,
    val errorMessage: String? = null
)
