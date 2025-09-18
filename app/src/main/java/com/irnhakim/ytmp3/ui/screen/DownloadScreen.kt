package com.irnhakim.ytmp3.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.irnhakim.ytmp3.data.DownloadFormat
import com.irnhakim.ytmp3.data.DownloadState
import com.irnhakim.ytmp3.viewmodel.DownloadViewModel
import com.irnhakim.ytmp3.utils.FileUtils
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadScreen(
    modifier: Modifier = Modifier,
    viewModel: DownloadViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val downloadState by viewModel.downloadState.collectAsState()
    val context = LocalContext.current
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Text(
            text = "YouTube Downloader",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        
        // URL Input
        OutlinedTextField(
            value = uiState.url,
            onValueChange = viewModel::updateUrl,
            label = { Text("Masukkan URL YouTube") },
            placeholder = { Text("https://youtube.com/watch?v=...") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            trailingIcon = {
                IconButton(
                    onClick = viewModel::getVideoInfo,
                    enabled = !uiState.isLoading && uiState.url.isNotBlank()
                ) {
                    Icon(Icons.Default.Search, contentDescription = "Cari Video")
                }
            }
        )
        
        // Format Selection
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Pilih Format",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        onClick = { viewModel.updateFormat(DownloadFormat.MP4) },
                        label = { Text("MP4 (Video)") },
                        selected = uiState.selectedFormat == DownloadFormat.MP4,
                        modifier = Modifier.weight(1f)
                    )
                    
                    FilterChip(
                        onClick = { viewModel.updateFormat(DownloadFormat.MP3) },
                        label = { Text("MP3 (Audio)") },
                        selected = uiState.selectedFormat == DownloadFormat.MP3,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
        
        // Loading State
        if (uiState.isLoading) {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Mengambil informasi video...")
                }
            }
        }
        
        // Error Message
        uiState.errorMessage?.let { error ->
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Text(
                    text = error,
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
        
        // Video Info
        if (uiState.showVideoInfo && uiState.videoInfo != null) {
            VideoInfoCard(
                videoInfo = uiState.videoInfo!!,
                onDownload = viewModel::startDownload
            )
        }
        
        // Download Progress
        when (val currentDownloadState = downloadState) {
            is DownloadState.Loading -> {
                DownloadProgressCard(
                    status = "Mempersiapkan download...",
                    progress = 0,
                    downloadDirPath = FileUtils.getDownloadDirectory(context).absolutePath
                )
            }
            is DownloadState.Progress -> {
                DownloadProgressCard(
                    status = currentDownloadState.status,
                    progress = currentDownloadState.progress,
                    downloadDirPath = FileUtils.getDownloadDirectory(context).absolutePath
                )
            }
            is DownloadState.Success -> {
                DownloadSuccessCard(
                    fileName = currentDownloadState.fileName,
                    filePath = currentDownloadState.filePath,
                    onOpenFile = { FileUtils.openFile(context, currentDownloadState.filePath) },
                    onReset = viewModel::resetDownload
                )
            }
            is DownloadState.Error -> {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Download Gagal",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Text(
                            text = currentDownloadState.message,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = viewModel::resetDownload,
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("Coba Lagi")
                        }
                    }
                }
            }
            else -> {}
        }
    }
}

@Composable
private fun VideoInfoCard(
    videoInfo: com.irnhakim.ytmp3.data.VideoInfo,
    onDownload: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Thumbnail
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(videoInfo.thumbnail)
                    .crossfade(true)
                    .build(),
                contentDescription = "Video Thumbnail",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Video Info
            Text(
                text = videoInfo.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = "Channel: ${videoInfo.uploader}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Text(
                text = "Durasi: ${videoInfo.duration}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Download Button
            Button(
                onClick = onDownload,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("📥 Download")
            }
        }
    }
}

@Composable
private fun DownloadProgressCard(
    status: String,
    progress: Int,
    downloadDirPath: String = ""
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Download Progress",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            LinearProgressIndicator(
                progress = { progress / 100f },
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = status,
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = "$progress%",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (progress >= 100) {
                Spacer(modifier = Modifier.height(8.dp))
                Divider()
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Download selesai",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "File tersimpan pada: $downloadDirPath",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun DownloadSuccessCard(
    fileName: String,
    filePath: String,
    onOpenFile: () -> Unit,
    onReset: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Download selesai",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "File: $fileName",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            Text(
                text = "File tersimpan pada: ${File(filePath).parent ?: filePath}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                OutlinedButton(
                    onClick = onOpenFile
                ) {
                    Text("Buka File")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = onReset
                ) {
                    Text("Download Lagi")
                }
            }
        }
    }
}
