# 🐛 Common Issues & Solutions

Panduan troubleshooting untuk masalah yang sering ditemui saat menggunakan Local YouTube Downloader Android.

## 📋 Quick Troubleshooting Checklist

Sebelum mencari solusi spesifik, coba langkah-langkah dasar ini:

- [ ] **Restart aplikasi** - tutup dan buka kembali
- [ ] **Periksa koneksi internet** - pastikan stabil
- [ ] **Periksa storage space** - minimal 1GB free space
- [ ] **Coba URL lain** - isolasi masalah pada video tertentu
- [ ] **Update aplikasi** - jika ada versi terbaru

## 🔗 URL & Video Issues

### ❌ "URL tidak valid"

**Symptoms:**
- Error message saat memasukkan URL
- Tombol search tidak aktif
- URL tidak dikenali sistem

**Causes:**
- Format URL tidak didukung
- URL bukan video YouTube
- URL playlist atau channel

**Solutions:**

1. **Periksa format URL yang didukung:**
   ```
   ✅ https://www.youtube.com/watch?v=VIDEO_ID
   ✅ https://youtu.be/VIDEO_ID
   ✅ https://youtube.com/embed/VIDEO_ID
   ✅ https://m.youtube.com/watch?v=VIDEO_ID
   ❌ https://youtube.com/playlist?list=...
   ❌ https://youtube.com/channel/...
   ❌ https://youtube.com/user/...
   ```

2. **Copy URL yang benar:**
   - Buka video di YouTube app/browser
   - Tap tombol **Share**
   - Pilih **Copy Link**
   - Paste di aplikasi

3. **Remove extra parameters:**
   ```
   ✅ https://youtube.com/watch?v=dQw4w9WgXcQ
   ❌ https://youtube.com/watch?v=dQw4w9WgXcQ&t=30s&list=...
   ```

### ❌ "Video tidak ditemukan"

**Symptoms:**
- URL valid tapi tidak bisa mengambil info video
- Error saat loading video information
- Timeout saat mengakses video

**Causes:**
- Video private atau deleted
- Video region-blocked
- Video age-restricted
- Network connectivity issues

**Solutions:**

1. **Verify video accessibility:**
   - Buka video di browser/YouTube app
   - Pastikan video bisa diputar normal
   - Check apakah video public

2. **Try different network:**
   - Switch dari WiFi ke mobile data (atau sebaliknya)
   - Restart router jika menggunakan WiFi
   - Check firewall/proxy settings

3. **Wait and retry:**
   - YouTube server mungkin temporary down
   - Coba lagi setelah beberapa menit
   - Check YouTube status di downdetector.com

### ❌ "Video terlalu panjang"

**Symptoms:**
- Download gagal untuk video panjang (>1 jam)
- Out of memory errors
- App crash saat download video besar

**Solutions:**

1. **Check available storage:**
   ```
   Video 1080p: ~100MB per 10 menit
   Video 720p: ~50MB per 10 menit
   Audio MP3: ~10MB per 10 menit
   ```

2. **Free up storage space:**
   - Delete old downloads
   - Move files to external storage
   - Clear app cache

3. **Use lower quality (future feature):**
   - Saat ini otomatis pilih best quality
   - Quality selection akan ditambahkan

## 📥 Download Issues

### ❌ "Download gagal"

**Symptoms:**
- Download stops unexpectedly
- Progress stuck at certain percentage
- Generic download error

**Causes:**
- Network interruption
- Insufficient storage
- Server-side issues
- App killed by system

**Solutions:**

1. **Check network stability:**
   - Use stable WiFi connection
   - Avoid switching networks during download
   - Close bandwidth-heavy apps

2. **Ensure sufficient storage:**
   ```bash
   # Check available space
   Settings > Storage > Available space
   
   # Recommended minimum:
   - Video downloads: 500MB free
   - Audio downloads: 100MB free
   ```

3. **Keep app in foreground:**
   - Don't switch to other apps during download
   - Disable battery optimization for app
   - Keep screen on during download

4. **Retry with different format:**
   - If MP4 fails, try MP3
   - If MP3 fails, try MP4
   - App will auto-fallback to available formats

### ❌ "Format tidak tersedia"

**Symptoms:**
- Specific format (MP4/MP3) not available
- Download fails with format error
- Only certain formats work

**Causes:**
- Video doesn't have requested format
- Regional restrictions on certain formats
- YouTube encoding issues

**Solutions:**

1. **Try alternative format:**
   - If MP4 fails → try MP3
   - If MP3 fails → try MP4
   - App automatically tries fallback formats

2. **Understand fallback system:**
   ```
   MP4 Request:
   1. Progressive MP4 (video+audio)
   2. Best video + Best audio (merged)
   3. Best MP4 available
   4. Best format available (WebM/MKV)
   
   MP3 Request:
   1. Extract to MP3 (requires FFmpeg)
   2. Best audio M4A
   3. Best audio available (OPUS/WebM)
   ```

3. **Check final file format:**
   - File might be saved as WebM/M4A instead of MP4/MP3
   - Still playable with compatible players
   - Check file extension in download folder

### ❌ "File audio tidak ditemukan atau kosong"

**Symptoms:**
- MP3 download completes but file is 0 bytes
- Audio file not created
- Error message about empty audio file

**Causes:**
- FFmpeg not available on device
- Audio extraction failed
- Codec compatibility issues

**Solutions:**

1. **Automatic fallback:**
   - App automatically tries M4A format
   - Check download folder for .m4a files
   - M4A files are compatible with most players

2. **Install compatible audio player:**
   - VLC Media Player (supports all formats)
   - Google Play Music
   - Any player that supports M4A/OPUS

3. **Restart app and retry:**
   - Close and reopen app
   - Try downloading same video again
   - FFmpeg might initialize properly on retry

## 📁 File & Storage Issues

### ❌ "File tidak ditemukan"

**Symptoms:**
- Download completes but file not found
- "Buka File" button doesn't work
- File not in expected location

**Causes:**
- File saved with different name/extension
- Permission issues
- File moved/deleted by system

**Solutions:**

1. **Check download directory:**
   ```
   Primary location: /storage/emulated/0/Download/YTMP3Downloads/
   Alternative: Internal Storage > Download > YTMP3Downloads
   ```

2. **Search for files with different extensions:**
   - Look for .webm, .m4a, .opus files
   - Check file timestamps
   - Use file manager search function

3. **Check file naming pattern:**
   ```
   Format: [Title]_[Timestamp].[Extension]
   Example: Amazing_Video_20240101_143022.mp4
   ```

4. **Verify permissions:**
   - Go to App Settings > Permissions
   - Ensure Storage permission is granted
   - Try revoking and granting permission again

### ❌ "Tidak bisa membuka file"

**Symptoms:**
- File exists but won't open
- "No app found to handle file" error
- File opens in wrong app

**Causes:**
- No compatible player installed
- File association issues
- Corrupted file

**Solutions:**

1. **Install recommended players:**
   ```
   Video files (.mp4, .webm, .mkv):
   - VLC Media Player
   - MX Player
   - Google Photos
   
   Audio files (.mp3, .m4a, .opus):
   - VLC Media Player
   - Google Play Music
   - Any music player app
   ```

2. **Manual file opening:**
   - Open file manager
   - Navigate to download folder
   - Long-press file → Open with → Choose app

3. **Check file integrity:**
   - Verify file size > 0 bytes
   - Try playing in different player
   - Re-download if file seems corrupted

### ❌ "Storage penuh"

**Symptoms:**
- Download fails immediately
- "Insufficient storage" error
- App crashes during download

**Solutions:**

1. **Free up storage space:**
   - Delete old downloads
   - Clear app caches
   - Move photos/videos to cloud storage
   - Uninstall unused apps

2. **Move existing downloads:**
   - Copy files to external SD card
   - Upload to cloud storage
   - Delete local copies after backup

3. **Monitor storage during download:**
   - Check available space before starting
   - Estimate file size based on video length
   - Keep at least 500MB free for video downloads

## 🔧 App Performance Issues

### ❌ App crash atau force close

**Symptoms:**
- App suddenly closes
- "App has stopped" error
- App won't start

**Causes:**
- Memory issues
- Corrupted app data
- System resource constraints
- Bug in app code

**Solutions:**

1. **Restart device:**
   - Complete device reboot
   - Clears memory and system resources
   - Often resolves temporary issues

2. **Clear app data (if available):**
   ```
   Settings > Apps > YTMP3 > Storage > Clear Data
   Warning: This will reset app settings
   ```

3. **Free up device memory:**
   - Close other running apps
   - Restart device to clear RAM
   - Ensure sufficient storage space

4. **Update or reinstall app:**
   - Check for app updates
   - Uninstall and reinstall if necessary
   - Download latest version from source

### ❌ Download sangat lambat

**Symptoms:**
- Download progress very slow
- Takes hours for short videos
- Progress bar barely moves

**Causes:**
- Slow internet connection
- Network congestion
- YouTube server limitations
- Device performance issues

**Solutions:**

1. **Optimize network connection:**
   - Use WiFi instead of mobile data
   - Move closer to WiFi router
   - Restart router/modem
   - Close other apps using internet

2. **Download during off-peak hours:**
   - Early morning or late night
   - Avoid peak internet usage times
   - Check local network congestion

3. **Optimize device performance:**
   - Close unnecessary apps
   - Restart device before download
   - Ensure device not overheating
   - Charge device during download

### ❌ UI tidak responsif

**Symptoms:**
- App interface freezes
- Buttons don't respond
- Screen doesn't update

**Causes:**
- Main thread blocked
- Heavy processing
- Memory pressure
- UI rendering issues

**Solutions:**

1. **Wait for processing to complete:**
   - Video info extraction takes time
   - Don't tap buttons repeatedly
   - Wait for UI to respond

2. **Restart app:**
   - Force close and reopen
   - Clear recent apps and restart
   - Give app fresh start

3. **Reduce system load:**
   - Close other apps
   - Free up RAM
   - Avoid multitasking during use

## 🌐 Network & Connectivity Issues

### ❌ "Tidak ada koneksi internet"

**Symptoms:**
- Network error messages
- Can't load video info
- Download fails immediately

**Solutions:**

1. **Check internet connectivity:**
   - Open browser and visit website
   - Try other apps requiring internet
   - Check WiFi/mobile data settings

2. **Reset network settings:**
   - Turn WiFi off and on
   - Switch to mobile data temporarily
   - Restart network adapter

3. **Check firewall/proxy:**
   - Disable VPN temporarily
   - Check corporate firewall settings
   - Try different network if available

### ❌ "Connection timeout"

**Symptoms:**
- Long loading times
- Timeout errors
- Intermittent connectivity

**Solutions:**

1. **Improve connection stability:**
   - Move closer to WiFi router
   - Use 5GHz WiFi if available
   - Avoid network congestion

2. **Retry with patience:**
   - Wait longer for responses
   - Don't cancel operations too quickly
   - YouTube servers may be slow

## 🔒 Permissions Issues

### ❌ "Permission denied"

**Symptoms:**
- Can't save files
- Storage access denied
- File operations fail

**Solutions:**

1. **Grant storage permissions:**
   ```
   Settings > Apps > YTMP3 > Permissions > Storage > Allow
   ```

2. **Check Android version compatibility:**
   - Android 11+ has scoped storage
   - App handles permissions automatically
   - May need to grant additional permissions

3. **Restart app after granting permissions:**
   - Close app completely
   - Reopen to refresh permissions
   - Try operation again

## 🆘 Emergency Troubleshooting

### When Nothing Works

1. **Complete app reset:**
   - Uninstall app completely
   - Restart device
   - Reinstall latest version
   - Test with simple video

2. **Device compatibility check:**
   - Verify Android version (7.0+)
   - Check available RAM (2GB+)
   - Ensure sufficient storage (1GB+)

3. **Report persistent issues:**
   - Create GitHub issue with details
   - Include device info and logs
   - Provide steps to reproduce

### Getting Help

1. **Search existing issues:**
   - Check [GitHub Issues](https://github.com/yourusername/Local-Youtube-Downloader-Android/issues)
   - Look for similar problems
   - Check if solution already exists

2. **Create detailed bug report:**
   - Device model and Android version
   - App version
   - Exact error message
   - Steps to reproduce
   - Screenshots if helpful

3. **Community support:**
   - Join [GitHub Discussions](https://github.com/yourusername/Local-Youtube-Downloader-Android/discussions)
   - Ask questions in community
   - Help others with similar issues

---

## 📊 Issue Priority Guide

### 🔴 Critical Issues (Report Immediately)
- App crashes on startup
- Data loss or corruption
- Security vulnerabilities
- Complete feature breakdown

### 🟡 High Priority Issues
- Download failures for most videos
- Major UI problems
- Performance degradation
- Compatibility issues

### 🟢 Medium Priority Issues
- Specific video failures
- Minor UI glitches
- Feature enhancement requests
- Documentation improvements

### ⚪ Low Priority Issues
- Cosmetic issues
- Nice-to-have features
- Minor inconveniences

---

**Still having issues?** 🤔 

1. Check our [FAQ](FAQ) for more answers
2. Search [GitHub Issues](https://github.com/yourusername/Local-Youtube-Downloader-Android/issues)
3. Create a new issue with detailed information
4. Join our [community discussions](https://github.com/yourusername/Local-Youtube-Downloader-Android/discussions)

**Remember:** Most issues can be resolved with basic troubleshooting steps. Don't hesitate to ask for help if you're stuck!
