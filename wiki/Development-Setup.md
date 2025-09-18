# 🛠️ Development Setup

Panduan lengkap untuk setup environment development Local YouTube Downloader Android.

## 📋 Prerequisites

### System Requirements
- **OS**: Windows 10/11, macOS 10.14+, atau Linux (Ubuntu 18.04+)
- **RAM**: Minimum 8GB (16GB recommended untuk emulator)
- **Storage**: Minimum 10GB free space
- **CPU**: Intel i5/AMD Ryzen 5 atau lebih baik

### Required Software
- **Android Studio**: Flamingo (2022.3.1) atau lebih baru
- **JDK**: OpenJDK 11 atau Oracle JDK 11+
- **Git**: Latest version untuk version control
- **Node.js**: (Optional) untuk documentation tools

## 🚀 Environment Setup

### 1. Install Java Development Kit

#### Windows
```powershell
# Using Chocolatey
choco install openjdk11

# Or download from Oracle/OpenJDK website
# Set JAVA_HOME environment variable
setx JAVA_HOME "C:\Program Files\Java\jdk-11.0.x"
```

#### macOS
```bash
# Using Homebrew
brew install openjdk@11

# Add to PATH
echo 'export PATH="/opt/homebrew/opt/openjdk@11/bin:$PATH"' >> ~/.zshrc
source ~/.zshrc
```

#### Linux (Ubuntu/Debian)
```bash
# Install OpenJDK 11
sudo apt update
sudo apt install openjdk-11-jdk

# Verify installation
java -version
javac -version
```

### 2. Install Android Studio

#### Download and Install
1. **Download** dari [developer.android.com/studio](https://developer.android.com/studio)
2. **Run installer** dan pilih "Standard" installation
3. **Wait for SDK download** (akan download ~3GB)
4. **Launch Android Studio** setelah instalasi selesai

#### Initial Configuration
```bash
# Set Android Studio preferences
# File > Settings (Windows/Linux) atau Android Studio > Preferences (macOS)

# Configure:
- Appearance & Behavior > System Settings > Memory Settings
  - IDE max heap size: 4096 MB
  - Gradle max heap size: 4096 MB
```

### 3. Configure Android SDK

#### SDK Platforms
Install SDK platforms yang diperlukan:
```
✅ Android 14 (API 34) - Target SDK
✅ Android 13 (API 33) - Recent version
✅ Android 11 (API 30) - Common version
✅ Android 7.0 (API 24) - Minimum SDK
```

#### SDK Tools
Install tools yang diperlukan:
```
✅ Android SDK Build-Tools (latest)
✅ Android Emulator
✅ Android SDK Platform-Tools
✅ Intel x86 Emulator Accelerator (HAXM) - Windows/macOS
✅ Google Play services
✅ Google Repository
```

#### SDK Path Configuration
```bash
# Set ANDROID_HOME environment variable

# Windows
setx ANDROID_HOME "C:\Users\%USERNAME%\AppData\Local\Android\Sdk"

# macOS/Linux
export ANDROID_HOME=$HOME/Android/Sdk
export PATH=$PATH:$ANDROID_HOME/emulator
export PATH=$PATH:$ANDROID_HOME/tools
export PATH=$PATH:$ANDROID_HOME/tools/bin
export PATH=$PATH:$ANDROID_HOME/platform-tools
```

### 4. Install Git and Configure

#### Install Git
```bash
# Windows (using Chocolatey)
choco install git

# macOS (using Homebrew)
brew install git

# Linux (Ubuntu/Debian)
sudo apt install git
```

#### Configure Git
```bash
# Set global configuration
git config --global user.name "Your Name"
git config --global user.email "your.email@example.com"

# Configure line endings
git config --global core.autocrlf input  # macOS/Linux
git config --global core.autocrlf true   # Windows

# Set default branch name
git config --global init.defaultBranch main
```

## 📱 Project Setup

### 1. Clone Repository

#### Fork and Clone
```bash
# Fork repository di GitHub UI, kemudian:
git clone https://github.com/YOUR_USERNAME/Local-Youtube-Downloader-Android.git
cd Local-Youtube-Downloader-Android

# Add upstream remote
git remote add upstream https://github.com/ORIGINAL_OWNER/Local-Youtube-Downloader-Android.git

# Verify remotes
git remote -v
```

#### Branch Strategy
```bash
# Create development branch
git checkout -b develop
git push -u origin develop

# Create feature branches from develop
git checkout -b feature/your-feature-name
```

### 2. Open Project in Android Studio

#### Import Project
1. **Launch Android Studio**
2. **Click "Open"** atau "Open an existing project"
3. **Navigate** to cloned repository folder
4. **Select project folder** dan click "OK"
5. **Wait for Gradle sync** to complete

#### Project Structure Verification
```
Local-Youtube-Downloader-Android/
├── app/                          # Main application module
│   ├── src/main/java/           # Kotlin source code
│   ├── src/test/java/           # Unit tests
│   ├── src/androidTest/java/    # Instrumentation tests
│   └── build.gradle.kts         # App-level build script
├── gradle/                      # Gradle wrapper files
├── build.gradle.kts            # Project-level build script
├── settings.gradle.kts         # Project settings
└── local.properties           # Local SDK path (auto-generated)
```

### 3. Configure Build Environment

#### Gradle Configuration
Update `gradle.properties`:
```properties
# Performance optimizations
org.gradle.jvmargs=-Xmx4096m -XX:MaxPermSize=512m -XX:+HeapDumpOnOutOfMemoryError -Dfile.encoding=UTF-8
org.gradle.parallel=true
org.gradle.configureondemand=true
org.gradle.caching=true

# Android optimizations
android.useAndroidX=true
android.enableJetifier=true
android.enableR8.fullMode=true

# Kotlin optimizations
kotlin.code.style=official
kotlin.incremental=true
kotlin.incremental.android=true
```

#### Local Properties
Create/update `local.properties`:
```properties
# SDK location (auto-generated, verify path)
sdk.dir=/path/to/Android/Sdk

# Optional: NDK location if needed
# ndk.dir=/path/to/Android/Sdk/ndk/version
```

### 4. Verify Setup

#### Build Project
```bash
# Clean and build
./gradlew clean
./gradlew build

# Build debug APK
./gradlew assembleDebug

# Run tests
./gradlew test
./gradlew connectedAndroidTest
```

#### Expected Output
```
BUILD SUCCESSFUL in 2m 30s
45 actionable tasks: 45 executed
```

## 🧪 Testing Setup

### 1. Unit Testing Configuration

#### Test Dependencies
Verify in `app/build.gradle.kts`:
```kotlin
dependencies {
    // Unit testing
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.mockito:mockito-core:4.11.0")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    
    // Android testing
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
}
```

#### Test Configuration
```kotlin
// app/build.gradle.kts
android {
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            isReturnDefaultValues = true
        }
    }
}
```

### 2. Run Tests

#### Unit Tests
```bash
# Run all unit tests
./gradlew test

# Run specific test class
./gradlew test --tests "DownloadRepositoryTest"

# Run with coverage
./gradlew testDebugUnitTestCoverage
```

#### Instrumentation Tests
```bash
# Run on connected device/emulator
./gradlew connectedAndroidTest

# Run specific test
./gradlew connectedAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.irnhakim.ytmp3.ui.DownloadScreenTest
```

## 🔧 IDE Configuration

### 1. Android Studio Settings

#### Code Style
```
File > Settings > Editor > Code Style > Kotlin
- Use Kotlin official style guide
- Import scheme from kotlin-coding-conventions.xml
```

#### Inspections
```
File > Settings > Editor > Inspections
- Enable Kotlin inspections
- Enable Android Lint checks
- Configure severity levels
```

#### Live Templates
```
File > Settings > Editor > Live Templates
- Add custom templates for common patterns
- Import Android/Kotlin templates
```

### 2. Useful Plugins

#### Recommended Plugins
```
✅ Kotlin Multiplatform Mobile
✅ Android APK Support
✅ Database Navigator
✅ GitToolBox
✅ Rainbow Brackets
✅ String Manipulation
✅ Key Promoter X
```

#### Install Plugins
```
File > Settings > Plugins > Marketplace
Search and install recommended plugins
```

### 3. Debugging Configuration

#### Debug Build Variant
```kotlin
// app/build.gradle.kts
android {
    buildTypes {
        debug {
            isDebuggable = true
            isMinifyEnabled = false
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
        }
    }
}
```

#### Logging Configuration
```kotlin
// Enable detailed logging in debug builds
if (BuildConfig.DEBUG) {
    android.util.Log.d("YTMP3", "Debug logging enabled")
}
```

## 📱 Device/Emulator Setup

### 1. Physical Device Setup

#### Enable Developer Options
```
1. Go to Settings > About Phone
2. Tap "Build Number" 7 times
3. Go back to Settings > Developer Options
4. Enable "USB Debugging"
5. Enable "Stay Awake" (optional)
```

#### Connect Device
```bash
# Verify device connection
adb devices

# Expected output:
# List of devices attached
# DEVICE_ID    device
```

### 2. Emulator Setup

#### Create AVD (Android Virtual Device)
```
1. Open AVD Manager in Android Studio
2. Click "Create Virtual Device"
3. Select device: Pixel 6 (recommended)
4. Select system image: API 34 (Android 14)
5. Configure AVD:
   - Name: Pixel_6_API_34
   - RAM: 4096 MB
   - Internal Storage: 4 GB
   - SD Card: 1 GB (optional)
```

#### Emulator Performance
```
Advanced Settings:
- Graphics: Hardware - GLES 2.0
- Boot option: Cold Boot
- Multi-Core CPU: 4 cores
- Enable Hardware Keyboard: Yes
```

## 🚀 Development Workflow

### 1. Daily Development

#### Start Development Session
```bash
# Update from upstream
git fetch upstream
git checkout main
git merge upstream/main

# Create/switch to feature branch
git checkout -b feature/new-feature

# Start Android Studio
# Open project
# Select device/emulator
# Run app (Shift+F10)
```

#### Code-Test-Debug Cycle
```bash
# Make changes
# Run unit tests
./gradlew test

# Run app on device
./gradlew installDebug

# Debug if needed
# Use Android Studio debugger
```

### 2. Code Quality

#### Pre-commit Checks
```bash
# Format code
./gradlew ktlintFormat

# Run static analysis
./gradlew lint

# Run tests
./gradlew test

# Build project
./gradlew assembleDebug
```

#### Continuous Integration
```yaml
# .github/workflows/ci.yml (example)
name: CI
on: [push, pull_request]
jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - uses: actions/setup-java@v3
        with:
          java-version: '11'
      - run: ./gradlew test
      - run: ./gradlew assembleDebug
```

## 🐛 Troubleshooting Development Issues

### Common Setup Issues

#### Gradle Sync Failed
```bash
# Clear Gradle cache
./gradlew clean
rm -rf ~/.gradle/caches/

# Invalidate caches in Android Studio
File > Invalidate Caches and Restart
```

#### SDK Not Found
```bash
# Check ANDROID_HOME
echo $ANDROID_HOME  # macOS/Linux
echo %ANDROID_HOME% # Windows

# Update local.properties
sdk.dir=/correct/path/to/Android/Sdk
```

#### Build Tools Version Mismatch
```kotlin
// Update app/build.gradle.kts
android {
    compileSdk = 34
    buildToolsVersion = "34.0.0"
}
```

#### Emulator Issues
```bash
# Cold boot emulator
emulator -avd Pixel_6_API_34 -cold-boot

# Wipe emulator data
emulator -avd Pixel_6_API_34 -wipe-data
```

## 📚 Additional Resources

### Documentation
- **[Architecture Overview](Architecture-Overview)** - Understand app structure
- **[API Reference](API-Reference)** - Component documentation
- **[Contributing Guide](Contributing-Guide)** - Contribution workflow

### External Resources
- **[Android Developer Docs](https://developer.android.com/docs)**
- **[Kotlin Documentation](https://kotlinlang.org/docs/home.html)**
- **[Jetpack Compose Docs](https://developer.android.com/jetpack/compose/documentation)**

### Community
- **[GitHub Discussions](https://github.com/irnhakim/Local-Youtube-Downloader-Android/discussions)**
- **[Stack Overflow](https://stackoverflow.com/questions/tagged/android+kotlin)**

---

**🎉 Development Environment Ready!** You're now set up for productive Android development with this project.

**Next Steps:**
1. **[Quick Start](Quick-Start)** - Build and run the app
2. **[Contributing Guide](Contributing-Guide)** - Make your first contribution
3. **[Architecture Overview](Architecture-Overview)** - Understand the codebase
