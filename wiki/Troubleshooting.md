# 🔧 Troubleshooting

Panduan lengkap troubleshooting untuk mengatasi masalah umum di Local YouTube Downloader Android.

## 🎯 Quick Troubleshooting Checklist

### ⚡ First Steps (Try These First)
```
1. ✅ Check internet connection
2. ✅ Restart the app
3. ✅ Clear app cache
4. ✅ Check storage space (need 500MB+ free)
5. ✅ Verify YouTube URL is valid
6. ✅ Try a different video
7. ✅ Update the app if available
```

### 🔍 Diagnostic Information
Before reporting issues, gather this information:
```
📱 Device: [Manufacturer] [Model]
🤖 Android Version: [Version] (API [Level])
📱 App Version: [Version Code]
🌐 Network: WiFi/Mobile Data
💾 Storage: [Free Space] available
🔗 URL: [YouTube URL that failed]
❌ Error: [Exact error message]
```

## 🚨 Common Issues & Solutions

### 1. Download Issues

#### ❌ "Download Failed" Error
**Symptoms:**
- Download starts but fails partway through
- Error message appears after some progress

**Solutions:**
```kotlin
// Check these in order:
1. Network Connection
   - Switch between WiFi and mobile data
   - Test with other apps
   - Restart router if using WiFi

2. Storage Space
   - Free up at least 1GB space
   - Check Downloads/YTMP3Downloads folder
   - Clear app cache: Settings > Apps > YTMP3 > Storage > Clear Cache

3. Video Availability
   - Try the video in YouTube app
   - Check if video is region-blocked
   - Verify video is not private/deleted

4. App Permissions
   - Grant storage permissions
   - Android 11+: Enable "All files access"
   - Check notification permissions
```

**Advanced Solutions:**
```kotlin
// If basic solutions don't work:
1. Clear App Data (will reset settings)
   Settings > Apps > YTMP3 > Storage > Clear Data

2. Reinstall App
   - Uninstall app
   - Download latest version
   - Install and setup again

3. Try Different Format
   - Switch from MP4 to MP3 or vice versa
   - Let app auto-select format
```

#### ❌ "Invalid URL" Error
**Symptoms:**
- Error appears immediately after entering URL
- URL looks correct but app rejects it

**Solutions:**
```kotlin
// URL Format Check:
✅ Valid URLs:
- https://www.youtube.com/watch?v=dQw4w9WgXcQ
- https://youtu.be/dQw4w9WgXcQ
- https://youtube.com/watch?v=dQw4w9WgXcQ
- https://m.youtube.com/watch?v=dQw4w9WgXcQ

❌ Invalid URLs:
- Playlist URLs (contains "list=")
- Channel URLs (contains "/channel/" or "/c/")
- Live stream URLs
- YouTube Shorts URLs (some may not work)
- URLs with extra parameters

// Fix Steps:
1. Copy URL directly from YouTube app/browser
2. Remove extra parameters after video ID
3. Ensure URL starts with https://
4. Try shortening with youtu.be format
```

#### ❌ "Video Not Available" Error
**Symptoms:**
- Video exists but can't be downloaded
- Works in YouTube but not in app

**Solutions:**
```kotlin
// Common Causes & Fixes:
1. Age-Restricted Content
   - Some age-restricted videos can't be downloaded
   - Try different video to test app functionality

2. Region Restrictions
   - Video blocked in your country
   - Use VPN to test (if legally allowed)

3. Copyright Protection
   - Some videos have download restrictions
   - Try user-uploaded content instead

4. Live Streams
   - Live streams not supported
   - Wait for stream to end and try again

5. Premium Content
   - YouTube Premium exclusive content
   - Not downloadable through third-party apps
```

### 2. Storage Issues

#### ❌ "Insufficient Storage" Error
**Symptoms:**
- Download fails with storage error
- Device shows low storage warning

**Solutions:**
```kotlin
// Immediate Fixes:
1. Free Up Space
   - Delete old downloads: Downloads/YTMP3Downloads/
   - Clear other app caches
   - Move photos/videos to cloud storage
   - Uninstall unused apps

2. Change Storage Location
   - Go to app settings
   - Select different storage location
   - Choose SD card if available
   - Use app-specific storage

3. Clean Downloads Folder
   - Open file manager
   - Navigate to Downloads/YTMP3Downloads/
   - Delete old/unwanted files
   - Empty trash/recycle bin
```

**Storage Management:**
```kotlin
// Automatic Cleanup Setup:
1. Enable Auto-Cleanup
   - App Settings > Storage > Auto-Cleanup
   - Set to delete files older than 30 days
   - Enable cleanup when storage low

2. Manual Cleanup
   - App Settings > Storage > Manage Files
   - Review large files (100MB+)
   - Delete duplicates
   - Archive old downloads
```

#### ❌ "Permission Denied" Error
**Symptoms:**
- Can't save files to storage
- Permission error during download

**Solutions:**
```kotlin
// Android Version Specific:

Android 11+ (API 30+):
1. Grant "All Files Access"
   - Settings > Apps > YTMP3 > Permissions
   - Enable "All files access"
   - Or use app-specific storage

Android 6-10 (API 23-29):
1. Grant Storage Permissions
   - Settings > Apps > YTMP3 > Permissions
   - Enable "Storage" permission
   - Allow both read and write

Android 5 and below:
1. Check SD Card
   - Ensure SD card is properly mounted
   - Try internal storage instead
```

### 3. Network Issues

#### ❌ "No Internet Connection" Error
**Symptoms:**
- Error appears despite having internet
- Other apps work fine

**Solutions:**
```kotlin
// Network Troubleshooting:
1. Basic Checks
   - Toggle airplane mode on/off
   - Switch between WiFi and mobile data
   - Restart device
   - Check if YouTube.com loads in browser

2. WiFi Issues
   - Forget and reconnect to WiFi
   - Restart router
   - Check router firewall settings
   - Try different WiFi network

3. Mobile Data Issues
   - Check data plan limits
   - Verify mobile data is enabled for app
   - Contact carrier if persistent issues

4. DNS Issues
   - Change DNS to 8.8.8.8 or 1.1.1.1
   - Clear DNS cache
   - Restart network adapter
```

#### ❌ "Connection Timeout" Error
**Symptoms:**
- Download starts but times out
- Slow or unstable connection

**Solutions:**
```kotlin
// Connection Optimization:
1. Improve Signal
   - Move closer to WiFi router
   - Check mobile signal strength
   - Avoid network congestion times

2. App Settings
   - Reduce concurrent downloads to 1
   - Enable "Retry on failure"
   - Increase timeout values

3. Network Settings
   - Disable VPN temporarily
   - Check firewall/antivirus settings
   - Whitelist app in security software
```

### 4. App Performance Issues

#### ❌ App Crashes or Force Closes
**Symptoms:**
- App suddenly closes
- "App has stopped" message
- App becomes unresponsive

**Solutions:**
```kotlin
// Immediate Fixes:
1. Restart App
   - Force close app completely
   - Clear from recent apps
   - Reopen app

2. Restart Device
   - Power off device completely
   - Wait 10 seconds
   - Power on and try again

3. Clear App Cache
   - Settings > Apps > YTMP3 > Storage
   - Clear Cache (not Clear Data)
   - Restart app

// Advanced Fixes:
1. Update App
   - Check for app updates
   - Install latest version
   - Restart after update

2. Check Device Resources
   - Close other apps
   - Free up RAM
   - Ensure 2GB+ RAM available

3. Clear App Data (Last Resort)
   - Settings > Apps > YTMP3 > Storage
   - Clear Data (will reset all settings)
   - Reconfigure app
```

#### ❌ App Runs Slowly
**Symptoms:**
- UI is laggy or unresponsive
- Downloads are very slow
- App takes long to start

**Solutions:**
```kotlin
// Performance Optimization:
1. Device Optimization
   - Close background apps
   - Restart device weekly
   - Clear device cache partition
   - Free up storage space (keep 20%+ free)

2. App Settings
   - Disable animations if available
   - Reduce download quality
   - Limit concurrent operations

3. Network Optimization
   - Use WiFi instead of mobile data
   - Avoid peak usage times
   - Check for network interference
```

### 5. File Access Issues

#### ❌ "File Not Found" Error
**Symptoms:**
- Download completes but file missing
- Can't open downloaded file

**Solutions:**
```kotlin
// File Location Check:
1. Check Download Locations
   Primary: /storage/emulated/0/Download/YTMP3Downloads/
   App-specific: /Android/data/com.irnhakim.ytmp3/files/Downloads/
   SD Card: /storage/[sdcard]/Download/YTMP3Downloads/

2. File Manager Search
   - Open file manager
   - Search for file name
   - Check all storage locations
   - Look in Downloads folder

3. Media Scanner Refresh
   - Restart device
   - Use media scanner app
   - Check gallery/music apps
```

#### ❌ "Can't Open File" Error
**Symptoms:**
- File exists but won't open
- "No app found" message

**Solutions:**
```kotlin
// File Opening Solutions:
1. Install Media Player
   Recommended apps:
   - VLC Media Player (universal)
   - MX Player (video)
   - Google Play Music (audio)
   - Default system players

2. Check File Format
   - Verify file extension (.mp4, .mp3, etc.)
   - Try different player app
   - Check if file is corrupted

3. File Permissions
   - Check file permissions
   - Try copying to different location
   - Use different file manager
```

## 🔧 Advanced Troubleshooting

### Debug Mode

```kotlin
// Enable Debug Logging:
1. Go to App Settings
2. Tap version number 7 times
3. Enable "Debug Mode"
4. Reproduce issue
5. Check logs in Settings > Debug > View Logs
```

### Network Diagnostics

```kotlin
// Network Testing:
1. Speed Test
   - Test download speed: speedtest.net
   - Minimum recommended: 5 Mbps
   - Check latency: should be <100ms

2. DNS Test
   - Try different DNS servers
   - Google DNS: 8.8.8.8, 8.8.4.4
   - Cloudflare DNS: 1.1.1.1, 1.0.0.1

3. Firewall Check
   - Temporarily disable firewall
   - Check if app works
   - Add app to firewall exceptions
```

### Storage Diagnostics

```kotlin
// Storage Health Check:
1. Check File System
   - Run disk check utility
   - Scan for errors
   - Defragment if needed (rare on Android)

2. SD Card Issues
   - Remove and reinsert SD card
   - Format SD card (backup first)
   - Try different SD card

3. Permissions Audit
   - Review all app permissions
   - Reset permissions if needed
   - Check system-level restrictions
```

## 📱 Device-Specific Issues

### Samsung Devices

```kotlin
// Samsung-Specific Solutions:
1. Battery Optimization
   - Settings > Device Care > Battery
   - App Power Management
   - Add YTMP3 to "Never sleeping apps"

2. One UI Issues
   - Disable "Put unused apps to sleep"
   - Check "Auto-optimize daily" settings
   - Review Samsung security settings

3. Knox Security
   - Check if Knox is blocking app
   - Review security policies
   - Temporarily disable if needed
```

### Xiaomi/MIUI Devices

```kotlin
// MIUI-Specific Solutions:
1. Autostart Management
   - Security > Autostart
   - Enable autostart for YTMP3
   - Allow background activity

2. Battery Saver
   - Settings > Battery & Performance
   - App Battery Saver
   - Set YTMP3 to "No restrictions"

3. MIUI Permissions
   - Settings > Apps > Permissions
   - Grant all required permissions
   - Check "Display pop-up windows"
```

### Huawei/EMUI Devices

```kotlin
// EMUI-Specific Solutions:
1. Protected Apps
   - Settings > Battery > Protected Apps
   - Enable protection for YTMP3
   - Allow background running

2. Power Management
   - Settings > Battery > Launch
   - Set YTMP3 to manual management
   - Enable all options

3. HMS vs GMS
   - Check if Google Play Services available
   - Use Huawei AppGallery version if needed
   - Verify service compatibility
```

## 🆘 Getting Help

### Before Contacting Support

```kotlin
// Information to Gather:
1. Device Information
   - Manufacturer and model
   - Android version
   - Available storage space
   - RAM amount

2. App Information
   - App version
   - Installation source (Play Store, APK)
   - When issue started
   - Frequency of issue

3. Network Information
   - Connection type (WiFi/Mobile)
   - Internet speed
   - ISP/Carrier
   - Location/Country

4. Error Details
   - Exact error message
   - Steps to reproduce
   - Screenshots if possible
   - Log files if available
```

### Support Channels

```kotlin
// How to Get Help:
1. GitHub Issues
   - Create detailed issue report
   - Include device/app information
   - Attach logs if possible
   - Follow issue template

2. Community Forums
   - Search existing discussions
   - Post in appropriate category
   - Be specific about problem
   - Help others when possible

3. Email Support
   - Use for sensitive issues
   - Include diagnostic information
   - Be patient for response
   - Follow up if needed
```

### Self-Help Resources

```kotlin
// Additional Resources:
1. Documentation
   - Read full user guide
   - Check FAQ section
   - Review compatibility list
   - Study architecture docs

2. Community
   - Join user groups
   - Follow social media
   - Participate in discussions
   - Share solutions

3. Updates
   - Subscribe to release notes
   - Follow development blog
   - Test beta versions
   - Provide feedback
```

## 🔄 Recovery Procedures

### Complete App Reset

```kotlin
// Nuclear Option (Last Resort):
1. Backup Important Data
   - Export download history
   - Note custom settings
   - Save important files

2. Complete Uninstall
   - Uninstall app
   - Clear all app data
   - Remove app folders manually
   - Restart device

3. Fresh Installation
   - Download latest version
   - Install cleanly
   - Setup from scratch
   - Test basic functionality

4. Restore Data
   - Import settings if possible
   - Reconfigure preferences
   - Test all features
```

### System-Level Recovery

```kotlin
// If App Issues Persist:
1. Android System Issues
   - Check for system updates
   - Clear system cache partition
   - Consider factory reset (backup first)

2. Hardware Issues
   - Test storage with other apps
   - Check network with other devices
   - Run hardware diagnostics

3. Account Issues
   - Sign out and back in
   - Clear Google Play Store cache
   - Check account permissions
```

---

**🔧 Troubleshooting Guide** - Comprehensive solutions for all common issues and problems.

**Quick Reference:**
- ✅ **Start with basics** - Internet, restart, storage space
- ✅ **Check permissions** - Storage, notifications, network
- ✅ **Try different videos** - Isolate the problem
- ✅ **Update everything** - App, Android, device drivers
- ✅ **Contact support** - When all else fails

**Remember:** Most issues are solved by the first 3 steps in the quick checklist!
