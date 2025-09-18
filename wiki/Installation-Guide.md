# 📦 Installation Guide

Panduan lengkap untuk menginstal dan menjalankan Local YouTube Downloader Android.

## 📋 Prerequisites

### System Requirements
- **Operating System**: Windows 10/11, macOS 10.14+, atau Linux
- **RAM**: Minimum 8GB (16GB recommended)
- **Storage**: Minimum 4GB free space
- **Internet**: Koneksi internet stabil

### Software Requirements
- **Android Studio**: Flamingo (2022.3.1) atau lebih baru
- **Java Development Kit**: JDK 11 atau lebih baru
- **Android SDK**: API 24 (Android 7.0) hingga API 34 (Android 14)
- **Git**: Untuk cloning repository

## 🚀 Installation Steps

### 1. Install Android Studio

#### Windows
1. Download Android Studio dari [developer.android.com](https://developer.android.com/studio)
2. Jalankan installer dan ikuti wizard instalasi
3. Pilih "Standard" installation type
4. Tunggu hingga SDK components terdownload

#### macOS
```bash
# Using Homebrew
brew install --cask android-studio

# Or download from official website
# https://developer.android.com/studio
```

#### Linux (Ubuntu/Debian)
```bash
# Download dan extract
wget https://redirector.gvt1.com/edgedl/android/studio/ide-zips/2022.3.1.21/android-studio-2022.3.1.21-linux.tar.gz
tar -xzf android-studio-*.tar.gz
sudo mv android-studio /opt/
sudo ln -sf /opt/android-studio/bin/studio.sh /usr/local/bin/android-studio

# Install dependencies
sudo apt update
sudo apt install libc6:i386 libncurses5:i386 libstdc++6:i386 lib32z1 libbz2-1.0:i386
```

### 2. Setup Android SDK

1. Buka Android Studio
2. Go to **File > Settings** (Windows/Linux) atau **Android Studio > Preferences** (macOS)
3. Navigate to **Appearance & Behavior > System Settings > Android SDK**
4. Install required SDK platforms:
   - Android 14 (API 34) - Target
   - Android 7.0 (API 24) - Minimum
5. Install SDK Tools:
   - Android SDK Build-Tools
   - Android Emulator
   - Android SDK Platform-Tools
   - Intel x86 Emulator Accelerator (HAXM installer)

### 3. Clone Repository

```bash
# Clone the repository
git clone https://github.com/yourusername/Local-Youtube-Downloader-Android.git

# Navigate to project directory
cd Local-Youtube-Downloader-Android
```

### 4. Open Project in Android Studio

1. Launch Android Studio
2. Click **"Open an existing project"**
3. Navigate to cloned repository folder
4. Select the project folder and click **"OK"**
5. Wait for Gradle sync to complete

### 5. Configure Project

#### Gradle Sync
```bash
# Manual sync if needed
./gradlew build --refresh-dependencies
```

#### SDK Configuration
1. Go to **File > Project Structure**
2. Under **Project Settings > Project**:
   - Set **Project SDK** to Android API 34
   - Set **Project language level** to 11
3. Under **Modules > app**:
   - Set **Compile Sdk Version** to 34
   - Set **Build Tools Version** to latest

## 📱 Device Setup

### Physical Device
1. Enable **Developer Options**:
   - Go to **Settings > About Phone**
   - Tap **Build Number** 7 times
2. Enable **USB Debugging**:
   - Go to **Settings > Developer Options**
   - Enable **USB Debugging**
3. Connect device via USB
4. Accept debugging permission on device

### Emulator Setup
1. Open **AVD Manager** in Android Studio
2. Click **"Create Virtual Device"**
3. Select device (recommended: Pixel 6)
4. Choose system image:
   - **API 34** (Android 14) for testing latest features
   - **API 24** (Android 7.0) for minimum compatibility
5. Configure AVD settings:
   - **RAM**: 2048MB minimum
   - **Internal Storage**: 2GB minimum
   - **SD Card**: 1GB (optional)

## 🔧 Build Configuration

### Debug Build
```bash
# Build debug APK
./gradlew assembleDebug

# Install on connected device
./gradlew installDebug
```

### Release Build
```bash
# Build release APK
./gradlew assembleRelease

# Build signed APK (requires keystore)
./gradlew assembleRelease -Pandroid.injected.signing.store.file=keystore.jks
```

## 🧪 Verify Installation

### Run Tests
```bash
# Unit tests
./gradlew test

# Instrumentation tests
./gradlew connectedAndroidTest

# All tests
./gradlew check
```

### Run Application
1. Select target device/emulator
2. Click **Run** button (▶️) or press **Shift+F10**
3. Wait for app to install and launch
4. Verify app opens without crashes

## 🐛 Troubleshooting

### Common Issues

#### Gradle Sync Failed
```bash
# Clear Gradle cache
./gradlew clean
rm -rf ~/.gradle/caches/

# Re-sync project
./gradlew build --refresh-dependencies
```

#### SDK Not Found
1. Open **SDK Manager**
2. Install missing SDK components
3. Update **local.properties** file:
```properties
sdk.dir=/path/to/Android/Sdk
```

#### Build Tools Version Mismatch
Update `app/build.gradle.kts`:
```kotlin
android {
    compileSdk = 34
    buildToolsVersion = "34.0.0"
}
```

#### Emulator Performance Issues
1. Enable **Hardware Acceleration**:
   - Install Intel HAXM (Windows/macOS)
   - Enable KVM (Linux)
2. Increase **RAM allocation** in AVD settings
3. Use **x86_64** system images instead of ARM

#### Permission Denied (Linux/macOS)
```bash
# Make gradlew executable
chmod +x gradlew

# Fix Android SDK permissions
sudo chown -R $USER:$USER $ANDROID_HOME
```

## 📊 Performance Optimization

### IDE Settings
1. **Increase heap size**:
   - Go to **Help > Edit Custom VM Options**
   - Add: `-Xmx4096m`
2. **Enable parallel builds**:
   - **Settings > Build > Compiler**
   - Check **"Build project automatically"**
3. **Configure Gradle**:
   - **Settings > Build > Build Tools > Gradle**
   - Set **Gradle JVM** to JDK 11+

### Project Settings
Add to `gradle.properties`:
```properties
# Enable parallel builds
org.gradle.parallel=true
org.gradle.configureondemand=true

# Increase heap size
org.gradle.jvmargs=-Xmx4096m -XX:MaxPermSize=512m

# Enable build cache
org.gradle.caching=true
```

## ✅ Installation Checklist

- [ ] Android Studio installed and configured
- [ ] Android SDK (API 24-34) installed
- [ ] Repository cloned successfully
- [ ] Project opens without errors
- [ ] Gradle sync completes successfully
- [ ] Device/emulator configured
- [ ] App builds and runs successfully
- [ ] Tests pass without failures

## 🆘 Getting Help

If you encounter issues during installation:

1. **Check Prerequisites**: Ensure all requirements are met
2. **Search Issues**: Look for similar problems in [GitHub Issues](https://github.com/yourusername/Local-Youtube-Downloader-Android/issues)
3. **Create Issue**: If problem persists, create a new issue with:
   - Operating system and version
   - Android Studio version
   - Error messages and logs
   - Steps to reproduce

## 📚 Next Steps

After successful installation:
- Read the **[Quick Start Guide](Quick-Start)** to begin using the app
- Check **[Development Setup](Development-Setup)** for development environment
- Review **[User Guide](User-Guide)** for detailed usage instructions

---

**Installation complete!** 🎉 You're ready to start using Local YouTube Downloader Android.
