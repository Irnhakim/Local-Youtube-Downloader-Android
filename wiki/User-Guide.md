# 👤 User Guide

Panduan lengkap untuk menggunakan Local YouTube Downloader Android.

## 🎯 Overview

Local YouTube Downloader Android adalah aplikasi yang memungkinkan Anda mendownload video YouTube dalam format MP4 (video) atau MP3 (audio) langsung ke perangkat Android Anda.

## 🚀 Getting Started

### First Launch
1. **Install aplikasi** dari APK atau build dari source code
2. **Grant permissions** yang diperlukan:
   - Storage access untuk menyimpan file
   - Network access untuk download
3. **Aplikasi siap digunakan** - tidak perlu registrasi atau login

### Interface Overview
Aplikasi memiliki interface sederhana dengan komponen utama:
- **URL Input Field**: Tempat memasukkan link YouTube
- **Format Selector**: Pilihan MP4 (video) atau MP3 (audio)
- **Video Info Card**: Preview informasi video
- **Download Progress**: Progress bar dan status
- **Success Card**: Informasi file hasil download

## 📥 How to Download

### Step 1: Input YouTube URL

#### Supported URL Formats
```
✅ https://www.youtube.com/watch?v=VIDEO_ID
✅ https://youtube.com/watch?v=VIDEO_ID
✅ https://youtu.be/VIDEO_ID
✅ https://m.youtube.com/watch?v=VIDEO_ID
✅ https://mobile.youtube.com/watch?v=VIDEO_ID
✅ https://www.youtube.com/embed/VIDEO_ID
✅ https://youtube.com/v/VIDEO_ID
```

#### How to Get YouTube URL
1. **From YouTube App**:
   - Tap **Share** button on video
   - Select **Copy Link**
   - Paste in aplikasi

2. **From Browser**:
   - Copy URL from address bar
   - Paste in aplikasi

3. **From Share Menu**:
   - Share video to aplikasi (jika tersedia)

### Step 2: Get Video Information
1. **Paste URL** di input field
2. **Tap Search button** (🔍)
3. **Wait for video info** to load
4. **Review video details**:
   - Title
   - Duration
   - Uploader
   - Thumbnail

### Step 3: Select Format

#### MP4 (Video)
- **Contains**: Video + Audio
- **Quality**: Best available (auto-selected)
- **File Size**: Larger (varies by video length/quality)
- **Use Case**: Watch offline, share video content

#### MP3 (Audio)
- **Contains**: Audio only
- **Quality**: 320kbps (best available)
- **File Size**: Smaller
- **Use Case**: Music, podcasts, audio content

### Step 4: Start Download
1. **Select desired format** (MP4/MP3)
2. **Tap Download button**
3. **Monitor progress**:
   - Progress percentage (0-100%)
   - Current status message
   - Estimated time (if available)

### Step 5: Access Downloaded File
1. **Wait for "Download selesai" message**
2. **Note file location**: `Downloads/YTMP3Downloads/`
3. **Tap "Buka File"** to open with default app
4. **Or navigate manually** to file location

## 📁 File Management

### File Location
All downloads are saved to:
```
/storage/emulated/0/Download/YTMP3Downloads/
```

### File Naming Convention
```
[Title]_[Timestamp].[Extension]

Examples:
- Amazing_Video_20240101_143022.mp4
- Great_Song_20240101_143055.mp3
- Tutorial_Video_20240101_150030.webm
```

### Supported Output Formats

#### Video Formats
| Format | Extension | Description |
|--------|-----------|-------------|
| MP4 | `.mp4` | Primary format, widely supported |
| WebM | `.webm` | Fallback format |
| MKV | `.mkv` | Fallback format |
| 3GP | `.3gp` | Fallback for compatibility |

#### Audio Formats
| Format | Extension | Description |
|--------|-----------|-------------|
| MP3 | `.mp3` | Primary format (requires FFmpeg) |
| M4A | `.m4a` | Fallback format |
| OPUS | `.opus` | High-quality fallback |
| WebM | `.webm` | Audio-only WebM |
| AAC | `.aac` | Fallback format |

### File Operations

#### Open File
- **Tap "Buka File"** in success card
- **Or use file manager** to navigate to download folder
- **Default apps** will handle file opening based on MIME type

#### Share File
1. Open file manager
2. Navigate to `Downloads/YTMP3Downloads/`
3. Long-press file
4. Select **Share**
5. Choose target app

#### Delete File
1. Open file manager
2. Navigate to download folder
3. Select file(s) to delete
4. Confirm deletion

## ⚙️ Settings & Configuration

### Storage Settings
- **Default location**: `Downloads/YTMP3Downloads/`
- **Cannot be changed** in current version
- **Auto-create folder** if not exists

### Quality Settings
- **Video**: Automatically selects best available quality
- **Audio**: Maximum quality (320kbps for MP3)
- **Progressive preferred**: Single file with audio+video

### Network Settings
- **No proxy support** in current version
- **Uses system network settings**
- **Requires stable internet connection**

## 🔧 Advanced Features

### Fallback System
Aplikasi secara otomatis menggunakan fallback jika format utama tidak tersedia:

#### Video Download Fallback Chain
1. **Progressive MP4** (audio+video in one file)
2. **Best video + Best audio** (merged)
3. **Best MP4 available**
4. **Best format available** (any extension)

#### Audio Download Fallback Chain
1. **MP3 extraction** (if FFmpeg available)
2. **Best audio M4A**
3. **Best audio available** (OPUS/WebM/AAC)

### Error Recovery
- **Automatic retry** for network errors
- **Format fallback** for unavailable formats
- **Graceful degradation** when components fail
- **User-friendly error messages**

## 📊 Progress Tracking

### Progress Indicators
- **Percentage**: 0-100% completion
- **Status Messages**: Current operation description
- **ETA**: Estimated time remaining (when available)

### Progress Stages
1. **0-5%**: Initializing download engine
2. **5-10%**: Extracting video information
3. **10-20%**: Preparing download
4. **20-100%**: Actual download progress
5. **100%**: Processing and finalizing

### Completion Detection
- **Progress reaches 100%**
- **File successfully created**
- **File size > 0 bytes**
- **Process automatically stops**

## 🐛 Troubleshooting

### Common Issues

#### "URL tidak valid"
**Cause**: Invalid or unsupported YouTube URL
**Solution**:
- Check URL format
- Ensure it's a YouTube video (not playlist/channel)
- Try copying URL again

#### "Video tidak ditemukan"
**Cause**: Video is private, deleted, or region-blocked
**Solution**:
- Check if video is accessible in browser
- Try different video
- Check internet connection

#### "Download gagal"
**Cause**: Network issues or format unavailable
**Solution**:
- Check internet connection
- Try different format (MP4 ↔ MP3)
- Restart app and try again

#### "File tidak ditemukan"
**Cause**: Download completed but file not found
**Solution**:
- Check `Downloads/YTMP3Downloads/` folder
- Look for files with different extensions (.webm, .m4a)
- Check storage permissions

#### "Tidak bisa membuka file"
**Cause**: No app installed to handle file type
**Solution**:
- Install video player (VLC, MX Player)
- Install music player for audio files
- Use file manager to open with specific app

### Performance Issues

#### Slow Download
- **Check internet speed**
- **Close other apps** using network
- **Try downloading smaller/shorter videos**

#### App Crashes
- **Restart app**
- **Clear app cache** (if available)
- **Check available storage space**
- **Update app** if newer version available

## 💡 Tips & Best Practices

### For Better Experience
1. **Use WiFi** for large video downloads
2. **Ensure sufficient storage** before downloading
3. **Close unnecessary apps** during download
4. **Don't switch apps** during download process
5. **Wait for completion** before starting new download

### Storage Management
1. **Regularly clean** download folder
2. **Move files** to external storage if needed
3. **Delete unwanted files** to free space
4. **Monitor storage usage**

### Quality Optimization
1. **MP4 for videos** you want to watch
2. **MP3 for music** and audio content
3. **Check video length** before downloading
4. **Consider file size** vs available storage

## 🔒 Privacy & Security

### Data Collection
- **No personal data** collected
- **No analytics** or tracking
- **No account required**
- **Local processing only**

### File Security
- **Files stored locally** on device
- **No cloud upload**
- **Standard Android permissions**
- **FileProvider** for secure sharing

### Network Security
- **HTTPS connections** for YouTube API
- **No proxy or VPN** interference
- **Standard network protocols**

## 📞 Support

### Getting Help
1. **Check this guide** for common solutions
2. **Review [FAQ](FAQ)** for quick answers
3. **Search [Issues](https://github.com/yourusername/Local-Youtube-Downloader-Android/issues)** for similar problems
4. **Create new issue** if problem persists

### Reporting Bugs
Include in bug report:
- **Device model** and Android version
- **App version**
- **YouTube URL** that caused issue
- **Error message** (if any)
- **Steps to reproduce**

---

**Happy downloading!** 🎉 Enjoy using Local YouTube Downloader Android.
