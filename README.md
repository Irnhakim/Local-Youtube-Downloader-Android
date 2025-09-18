# 📱 Local YouTube Downloader Android

Aplikasi Android modern untuk mendownload video YouTube dalam format MP4 atau MP3 menggunakan Kotlin dan Jetpack Compose. Aplikasi ini menggunakan yt-dlp dan FFmpeg untuk download dan konversi yang handal.

## ✨ Fitur Utama

- **🎥 Download Video & Audio**: Mendukung format MP4 (video) dan MP3 (audio)
- **🎨 UI Modern**: Jetpack Compose dengan Material Design 3
- **⚡ Proses Lokal**: Download dan konversi dilakukan sepenuhnya di perangkat
- **📊 Progress Real-time**: Tracking progress download dengan detail
- **✅ Validasi URL**: Validasi otomatis URL YouTube dengan berbagai format
- **📁 File Management**: Penyimpanan terorganisir dengan FileProvider
- **🔄 Fallback System**: Sistem fallback otomatis untuk kompatibilitas maksimal
- **📱 Open File**: Buka file hasil download langsung dengan aplikasi default

## 🛠️ Teknologi yang Digunakan

### Core Technologies
- **Kotlin**: Bahasa pemrograman utama
- **Jetpack Compose**: UI toolkit modern
- **Material Design 3**: Design system terbaru
- **MVVM Architecture**: Clean architecture pattern

### Libraries & Dependencies
- **yt-dlp Android**: YouTube video extraction dan download
- **FFmpeg Android**: Audio/video processing dan konversi
- **Coroutines & Flow**: Asynchronous programming
- **StateFlow**: Reactive state management
- **Coil**: Efficient image loading
- **FileProvider**: Secure file sharing

## 📱 Screenshots

### Tampilan Utama
- ✅ Input field untuk URL YouTube dengan validasi
- ✅ Pilihan format download (MP4/MP3)
- ✅ Preview informasi video (thumbnail, title, duration)
- ✅ Progress indicator dengan persentase dan status
- ✅ Notifikasi "Download selesai" dengan lokasi file
- ✅ Tombol "Buka File" untuk membuka dengan aplikasi default

## 🏗️ Arsitektur Aplikasi

```
app/src/main/java/com/irnhakim/ytmp3/
├── data/
│   ├── VideoInfo.kt              # Data models
│   ├── DownloadState.kt          # Download state sealed class
│   └── DownloadRequest.kt        # Request data class
├── repository/
│   ├── DownloadRepository.kt     # Main repository
│   └── CrashSafeDownloadRepository.kt # Crash-safe wrapper
├── service/
│   ├── YouTubeExtractorService.kt # yt-dlp integration
│   └── CrashSafeYouTubeExtractorService.kt # Crash-safe wrapper
├── viewmodel/
│   ├── DownloadViewModel.kt      # Main view model
│   └── CrashSafeDownloadViewModel.kt # Crash-safe wrapper
├── ui/
│   └── screen/
│       └── DownloadScreen.kt     # Main UI screen
└── utils/
    └── FileUtils.kt              # File operations & utilities
```

## 🔧 Setup dan Instalasi

### Prerequisites
- **Android Studio**: Flamingo atau lebih baru
- **Android SDK**: API 24 (Android 7.0) atau lebih tinggi
- **Kotlin**: 1.9.0 atau lebih baru
- **Gradle**: 8.0 atau lebih baru

### Langkah Instalasi

1. **Clone Repository**
   ```bash
   git clone https://github.com/irnhakim/Local-Youtube-Downloader-Android.git
   cd Local-Youtube-Downloader-Android
   ```

2. **Buka di Android Studio**
   - Buka Android Studio
   - Pilih "Open an existing project"
   - Navigasi ke folder project

3. **Sync Dependencies**
   ```bash
   ./gradlew build
   ```

4. **Run Aplikasi**
   - Pilih device/emulator (API 24+)
   - Klik tombol Run

## 📋 Dependencies

### Core Dependencies
```kotlin
// Jetpack Compose BOM
implementation(platform("androidx.compose:compose-bom:2023.10.01"))
implementation("androidx.compose.ui:ui")
implementation("androidx.compose.material3:material3")
implementation("androidx.activity:activity-compose:1.8.1")

// ViewModel & Lifecycle
implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")

// Coroutines
implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

// Image Loading
implementation("io.coil-kt:coil-compose:2.5.0")

// YouTube Downloader
implementation("com.github.yausername.youtubedl-android:library:0.14.+")
implementation("com.github.yausername.youtubedl-android:ffmpeg:0.14.+")

// File Provider
implementation("androidx.core:core-ktx:1.12.0")
```

## 🎯 Cara Penggunaan

### 1. Input URL YouTube
- Paste URL video YouTube di input field
- Mendukung format: `youtube.com/watch?v=`, `youtu.be/`, `youtube.com/embed/`
- Klik tombol search untuk mendapatkan info video

### 2. Pilih Format Download
- **MP4**: Download video lengkap dengan audio
- **MP3**: Download audio saja (ekstraksi dari video)

### 3. Download Process
- Klik tombol "Download"
- Monitor progress real-time dengan persentase
- Tunggu notifikasi "Download selesai"
- Klik "Buka File" untuk membuka dengan aplikasi default

### 4. File Location
File tersimpan di: `Downloads/YTMP3Downloads/`

## 🔐 Permissions

```xml
<!-- Network -->
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />

<!-- Storage -->
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />

<!-- File Provider -->
<provider
    android:name="androidx.core.content.FileProvider"
    android:authorities="${applicationId}.fileprovider"
    android:exported="false"
    android:grantUriPermissions="true">
    <meta-data
        android:name="android.support.FILE_PROVIDER_PATHS"
        android:resource="@xml/provider_paths" />
</provider>
```

## 📁 File Structure

```
Downloads/YTMP3Downloads/
├── Video_Title_20231201_143022.mp4
├── Audio_Title_20231201_143055.mp3
├── Another_Video_20231201_150030.webm
└── Music_Track_20231201_151245.m4a
```

## 🧪 Testing

### Unit Tests
```bash
./gradlew test
```

### Instrumentation Tests
```bash
./gradlew connectedAndroidTest
```

### Test Coverage
- ✅ URL validation tests
- ✅ File utility tests
- ✅ Download repository tests
- ✅ ViewModel state tests
- ✅ Integration tests

## 🚀 Fitur Teknis

### Download Engine
- **yt-dlp Integration**: Library terbaru untuk ekstraksi YouTube
- **FFmpeg Support**: Konversi audio/video berkualitas tinggi
- **Fallback System**: Otomatis mencoba format alternatif jika gagal
- **Progress Tracking**: Real-time progress dengan ETA

### File Management
- **FileProvider**: Secure file sharing antar aplikasi
- **MIME Type Detection**: Otomatis detect dan set MIME type
- **File Validation**: Validasi file hasil download
- **Directory Management**: Otomatis buat dan kelola folder download

### Error Handling
- **Crash-Safe Wrappers**: Wrapper untuk semua komponen kritis
- **Graceful Degradation**: Fallback ke format alternatif
- **User-Friendly Messages**: Pesan error yang mudah dipahami
- **Retry Mechanisms**: Otomatis retry untuk kasus tertentu

## 🔧 Configuration

### Supported Formats
**Video Output:**
- MP4 (primary)
- WebM (fallback)
- MKV (fallback)
- 3GP (fallback)

**Audio Output:**
- MP3 (primary, requires FFmpeg)
- M4A (fallback)
- OPUS (fallback)
- WebM Audio (fallback)
- AAC (fallback)

### Quality Settings
- **Video**: Otomatis pilih kualitas terbaik yang tersedia
- **Audio**: Kualitas maksimal (320kbps untuk MP3)
- **Progressive**: Prioritas format progressive (audio+video dalam satu file)

## 🐛 Troubleshooting

### Common Issues

**1. "Format tidak tersedia"**
- Aplikasi otomatis mencoba format fallback
- Cek koneksi internet
- Pastikan URL YouTube valid

**2. "File audio tidak ditemukan"**
- FFmpeg mungkin tidak tersedia di perangkat
- Aplikasi otomatis fallback ke M4A/OPUS
- File tetap bisa dibuka dengan pemutar audio

**3. Download gagal**
- Cek storage space
- Pastikan permissions diberikan
- Restart aplikasi jika perlu

## 🤝 Contributing

1. Fork repository
2. Buat feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit changes (`git commit -m 'Add some AmazingFeature'`)
4. Push ke branch (`git push origin feature/AmazingFeature`)
5. Buat Pull Request

### Development Guidelines
- Ikuti Kotlin coding conventions
- Gunakan Jetpack Compose best practices
- Tambahkan unit tests untuk fitur baru
- Update dokumentasi jika diperlukan

## 📄 License

```
MIT License

Copyright (c) 2024 Local YouTube Downloader Android

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

## 👨‍💻 Developer

Dibuat dengan ❤️ menggunakan Kotlin dan Jetpack Compose

**Tech Stack:**
- Kotlin + Jetpack Compose
- MVVM + Clean Architecture
- yt-dlp + FFmpeg
- Material Design 3

## 📞 Support

- 🐛 **Bug Reports**: [Create an Issue](https://github.com/irnhakim/Local-Youtube-Downloader-Android/issues)
- 💡 **Feature Requests**: [Create an Issue](https://github.com/irnhakim/Local-Youtube-Downloader-Android/issues)
- 📖 **Documentation**: Check the [Wiki](https://github.com/irnhakim/Local-Youtube-Downloader-Android/wiki)

## ⭐ Star History

Jika project ini membantu Anda, berikan ⭐ untuk mendukung development!

---

## ⚠️ Disclaimer

**Aplikasi ini dibuat untuk tujuan edukasi dan penggunaan pribadi.**

- Pastikan mematuhi Terms of Service YouTube
- Hormati hak cipta konten creator
- Gunakan hanya untuk konten yang Anda miliki atau memiliki izin
- Developer tidak bertanggung jawab atas penyalahgunaan aplikasi

**Educational Purpose Only** - This app is created for educational purposes and personal use. Please respect YouTube's Terms of Service and content creators' rights.
