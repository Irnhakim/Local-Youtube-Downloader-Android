# 📱 Android Compatibility

Panduan lengkap kompatibilitas Local YouTube Downloader Android across different Android versions, devices, dan configurations.

## 🎯 Overview

Aplikasi ini dirancang untuk mendukung wide range of Android devices dan versions, dengan special attention pada compatibility issues, version-specific features, dan device-specific optimizations.

## 📊 Supported Android Versions

### Version Support Matrix

| Android Version | API Level | Support Status | Notes |
|----------------|-----------|----------------|-------|
| Android 14 | API 34 | ✅ Full Support | Target SDK, latest features |
| Android 13 | API 33 | ✅ Full Support | Themed app icons, per-app languages |
| Android 12 | API 31-32 | ✅ Full Support | Material You, splash screen API |
| Android 11 | API 30 | ✅ Full Support | Scoped storage, package visibility |
| Android 10 | API 29 | ✅ Full Support | Scoped storage (optional) |
| Android 9 | API 28 | ✅ Full Support | Network security config |
| Android 8.1 | API 27 | ✅ Full Support | Notification channels |
| Android 8.0 | API 26 | ✅ Full Support | Background execution limits |
| Android 7.1 | API 25 | ✅ Full Support | App shortcuts |
| Android 7.0 | API 24 | ✅ Minimum Support | Minimum supported version |
| Android 6.0 | API 23 | ❌ Not Supported | Runtime permissions complexity |
| Android 5.x | API 21-22 | ❌ Not Supported | Material Design limitations |

### Version-Specific Features

```kotlin
object AndroidVersionSupport {
    
    fun isAndroid11Plus(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.R
    fun isAndroid10Plus(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
    fun isAndroid9Plus(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.P
    fun isAndroid8Plus(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
    fun isAndroid7Plus(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N
    
    fun getSupportedFeatures(): List<String> {
        val features = mutableListOf<String>()
        
        if (isAndroid7Plus()) features.add("File Provider")
        if (isAndroid8Plus()) features.add("Notification Channels")
        if (isAndroid9Plus()) features.add("Network Security Config")
        if (isAndroid10Plus()) features.add("Scoped Storage")
        if (isAndroid11Plus()) features.add("Package Visibility")
        
        return features
    }
}
```

## 🏗️ Architecture Compatibility

### Minimum Requirements

```kotlin
android {
    compileSdk = 34
    
    defaultConfig {
        applicationId = "com.irnhakim.ytmp3"
        minSdk = 24        // Android 7.0
        targetSdk = 34     // Android 14
        versionCode = 1
        versionName = "1.0"
        
        // ABI support
        ndk {
            abiFilters += listOf("arm64-v8a", "armeabi-v7a", "x86", "x86_64")
        }
    }
}
```

### Device Architecture Support

```
✅ Supported Architectures:
- ARM64 (arm64-v8a) - Modern 64-bit ARM devices
- ARM32 (armeabi-v7a) - Older 32-bit ARM devices  
- x86_64 - 64-bit Intel/AMD (emulators, some tablets)
- x86 - 32-bit Intel/AMD (older emulators)

📱 Device Categories:
- Smartphones (5" - 7")
- Tablets (7" - 13")
- Foldable devices
- Android TV (limited support)
- Chromebooks with Android support
```

## 🔧 Version-Specific Implementations

### Storage Compatibility

#### Android 11+ (Scoped Storage)
```kotlin
class ScopedStorageManager {
    
    fun getDownloadDirectory(context: Context): File {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 11+: Use MediaStore or app-specific directory
            if (Environment.isExternalStorageManager()) {
                // Has MANAGE_EXTERNAL_STORAGE permission
                File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "YTMP3Downloads")
            } else {
                // Use app-specific external directory
                File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "YTMP3Downloads")
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Android 10: Scoped storage with legacy support
            if (Environment.isExternalStorageLegacy()) {
                File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "YTMP3Downloads")
            } else {
                File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "YTMP3Downloads")
            }
        } else {
            // Android 9 and below: Traditional external storage
            File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "YTMP3Downloads")
        }
    }
    
    fun requestStoragePermission(activity: Activity) {
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
                // Android 11+: Request MANAGE_EXTERNAL_STORAGE
                if (!Environment.isExternalStorageManager()) {
                    val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                        data = Uri.parse("package:${activity.packageName}")
                    }
                    activity.startActivity(intent)
                }
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> {
                // Android 6-10: Request runtime permissions
                ActivityCompat.requestPermissions(
                    activity,
                    arrayOf(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ),
                    STORAGE_PERMISSION_REQUEST_CODE
                )
            }
            else -> {
                // Android 5 and below: Permissions granted at install time
                // No runtime request needed
            }
        }
    }
}
```

#### Android 10 (Scoped Storage Transition)
```kotlin
class Android10StorageCompat {
    
    fun setupScopedStorage(context: Context) {
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q) {
            // Android 10 specific handling
            val appOpsManager = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
            val mode = appOpsManager.checkOpNoThrow(
                AppOpsManager.OPSTR_LEGACY_STORAGE,
                Process.myUid(),
                context.packageName
            )
            
            val hasLegacyStorage = mode == AppOpsManager.MODE_ALLOWED
            android.util.Log.d("StorageCompat", "Legacy storage available: $hasLegacyStorage")
        }
    }
    
    fun createMediaStoreEntry(context: Context, fileName: String, mimeType: String): Uri? {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
                put(MediaStore.MediaColumns.RELATIVE_PATH, "Download/YTMP3Downloads")
            }
            
            val collection = when {
                mimeType.startsWith("video/") -> MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                mimeType.startsWith("audio/") -> MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                else -> MediaStore.Files.getContentUri("external")
            }
            
            return context.contentResolver.insert(collection, values)
        }
        return null
    }
}
```

### Notification Compatibility

#### Android 8+ (Notification Channels)
```kotlin
class NotificationCompatManager(private val context: Context) {
    
    companion object {
        const val DOWNLOAD_CHANNEL_ID = "download_channel"
        const val PROGRESS_CHANNEL_ID = "progress_channel"
    }
    
    fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            
            // Download completion channel
            val downloadChannel = NotificationChannel(
                DOWNLOAD_CHANNEL_ID,
                "Download Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications for download completion"
                enableVibration(true)
                setShowBadge(true)
            }
            
            // Progress channel
            val progressChannel = NotificationChannel(
                PROGRESS_CHANNEL_ID,
                "Download Progress",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows download progress"
                enableVibration(false)
                setShowBadge(false)
            }
            
            notificationManager.createNotificationChannels(listOf(downloadChannel, progressChannel))
        }
    }
    
    fun createDownloadNotification(title: String, message: String): Notification {
        val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationCompat.Builder(context, DOWNLOAD_CHANNEL_ID)
        } else {
            NotificationCompat.Builder(context)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        }
        
        return builder
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_download)
            .setAutoCancel(true)
            .build()
    }
}
```

### Network Security

#### Android 9+ (Network Security Config)
```xml
<!-- res/xml/network_security_config.xml -->
<?xml version="1.0" encoding="utf-8"?>
<network-security-config>
    <domain-config cleartextTrafficPermitted="false">
        <domain includeSubdomains="true">youtube.com</domain>
        <domain includeSubdomains="true">googlevideo.com</domain>
        <domain includeSubdomains="true">ytimg.com</domain>
    </domain-config>
    
    <!-- Allow cleartext for localhost (development) -->
    <domain-config cleartextTrafficPermitted="true">
        <domain includeSubdomains="true">localhost</domain>
        <domain includeSubdomains="true">127.0.0.1</domain>
        <domain includeSubdomains="true">10.0.2.2</domain>
    </domain-config>
</network-security-config>
```

```xml
<!-- AndroidManifest.xml -->
<application
    android:networkSecurityConfig="@xml/network_security_config"
    android:usesCleartextTraffic="false">
    <!-- App components -->
</application>
```

## 🎨 UI Compatibility

### Theme Compatibility

#### Android 12+ (Material You)
```kotlin
@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
```

#### Android 8+ (Adaptive Icons)
```xml
<!-- res/mipmap-anydpi-v26/ic_launcher.xml -->
<?xml version="1.0" encoding="utf-8"?>
<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
    <background android:drawable="@drawable/ic_launcher_background" />
    <foreground android:drawable="@drawable/ic_launcher_foreground" />
    <monochrome android:drawable="@drawable/ic_launcher_monochrome" />
</adaptive-icon>
```

### Display Compatibility

#### Screen Size Support
```kotlin
class DisplayCompatManager(private val context: Context) {
    
    fun getScreenSizeCategory(): ScreenSize {
        val metrics = context.resources.displayMetrics
        val widthDp = metrics.widthPixels / metrics.density
        val heightDp = metrics.heightPixels / metrics.density
        
        return when {
            widthDp >= 840 -> ScreenSize.EXTRA_LARGE  // Tablets
            widthDp >= 600 -> ScreenSize.LARGE        // Large phones, small tablets
            widthDp >= 480 -> ScreenSize.MEDIUM       // Normal phones
            else -> ScreenSize.SMALL                  // Compact phones
        }
    }
    
    fun isTablet(): Boolean {
        return (context.resources.configuration.screenLayout and 
                Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE
    }
    
    fun supportsFoldable(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.R &&
                context.packageManager.hasSystemFeature("android.hardware.type.foldable")
    }
}

enum class ScreenSize {
    SMALL, MEDIUM, LARGE, EXTRA_LARGE
}
```

#### Density Support
```kotlin
class DensityCompatManager {
    
    fun getOptimalImageSize(context: Context, baseSize: Int): Int {
        val density = context.resources.displayMetrics.density
        return when {
            density >= 4.0 -> baseSize * 4  // XXXHDPI
            density >= 3.0 -> baseSize * 3  // XXHDPI
            density >= 2.0 -> baseSize * 2  // XHDPI
            density >= 1.5 -> (baseSize * 1.5).toInt()  // HDPI
            else -> baseSize  // MDPI
        }
    }
    
    fun getDensityCategory(context: Context): String {
        return when (context.resources.displayMetrics.densityDpi) {
            DisplayMetrics.DENSITY_LOW -> "LDPI"
            DisplayMetrics.DENSITY_MEDIUM -> "MDPI"
            DisplayMetrics.DENSITY_HIGH -> "HDPI"
            DisplayMetrics.DENSITY_XHIGH -> "XHDPI"
            DisplayMetrics.DENSITY_XXHIGH -> "XXHDPI"
            DisplayMetrics.DENSITY_XXXHIGH -> "XXXHDPI"
            else -> "UNKNOWN"
        }
    }
}
```

## 🔒 Permission Compatibility

### Runtime Permissions (Android 6+)
```kotlin
class PermissionCompatManager(private val activity: Activity) {
    
    fun requestStoragePermissions(callback: (Boolean) -> Unit) {
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
                // Android 11+: All files access
                requestAllFilesAccess(callback)
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> {
                // Android 6-10: Runtime permissions
                requestRuntimePermissions(callback)
            }
            else -> {
                // Android 5 and below: Install-time permissions
                callback(true)
            }
        }
    }
    
    private fun requestAllFilesAccess(callback: (Boolean) -> Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (Environment.isExternalStorageManager()) {
                callback(true)
            } else {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                    data = Uri.parse("package:${activity.packageName}")
                }
                activity.startActivity(intent)
                // Note: Callback handling would need to be done in onResume
            }
        }
    }
    
    private fun requestRuntimePermissions(callback: (Boolean) -> Unit) {
        val permissions = arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        
        val missingPermissions = permissions.filter {
            ContextCompat.checkSelfPermission(activity, it) != PackageManager.PERMISSION_GRANTED
        }
        
        if (missingPermissions.isEmpty()) {
            callback(true)
        } else {
            ActivityCompat.requestPermissions(
                activity,
                missingPermissions.toTypedArray(),
                STORAGE_PERMISSION_REQUEST_CODE
            )
        }
    }
    
    fun handlePermissionResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
        callback: (Boolean) -> Unit
    ) {
        if (requestCode == STORAGE_PERMISSION_REQUEST_CODE) {
            val allGranted = grantResults.all { it == PackageManager.PERMISSION_GRANTED }
            callback(allGranted)
        }
    }
    
    companion object {
        const val STORAGE_PERMISSION_REQUEST_CODE = 1001
    }
}
```

## 🏭 Manufacturer Compatibility

### Device-Specific Optimizations

#### Samsung Devices
```kotlin
class SamsungCompatManager {
    
    fun isSamsungDevice(): Boolean {
        return Build.MANUFACTURER.equals("samsung", ignoreCase = true)
    }
    
    fun handleSamsungSpecificFeatures(context: Context) {
        if (isSamsungDevice()) {
            // Samsung-specific optimizations
            handleOneUIFeatures(context)
            handleSamsungBatteryOptimization(context)
        }
    }
    
    private fun handleOneUIFeatures(context: Context) {
        // One UI specific adaptations
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Handle One UI 2.0+ features
            val isOneUI = try {
                val semPlatformVersion = Build::class.java.getDeclaredField("SEM_PLATFORM_INT")
                semPlatformVersion.isAccessible = true
                semPlatformVersion.getInt(null) >= 90000
            } catch (e: Exception) {
                false
            }
            
            if (isOneUI) {
                android.util.Log.d("SamsungCompat", "One UI detected, applying optimizations")
            }
        }
    }
    
    private fun handleSamsungBatteryOptimization(context: Context) {
        // Guide user to disable battery optimization for Samsung devices
        val intent = Intent().apply {
            action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
            data = Uri.parse("package:${context.packageName}")
        }
        
        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
        }
    }
}
```

#### Xiaomi Devices
```kotlin
class XiaomiCompatManager {
    
    fun isXiaomiDevice(): Boolean {
        return Build.MANUFACTURER.equals("xiaomi", ignoreCase = true) ||
               Build.BRAND.equals("xiaomi", ignoreCase = true) ||
               Build.BRAND.equals("redmi", ignoreCase = true)
    }
    
    fun handleMIUIPermissions(context: Context) {
        if (isXiaomiDevice()) {
            // MIUI specific permission handling
            try {
                val intent = Intent("miui.intent.action.APP_PERM_EDITOR").apply {
                    setClassName("com.miui.securitycenter", "com.miui.permcenter.permissions.PermissionsEditorActivity")
                    putExtra("extra_pkgname", context.packageName)
                }
                
                if (intent.resolveActivity(context.packageManager) != null) {
                    context.startActivity(intent)
                }
            } catch (e: Exception) {
                // Fallback to standard settings
                openAppSettings(context)
            }
        }
    }
    
    private fun openAppSettings(context: Context) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.parse("package:${context.packageName}")
        }
        context.startActivity(intent)
    }
}
```

#### Huawei Devices
```kotlin
class HuaweiCompatManager {
    
    fun isHuaweiDevice(): Boolean {
        return Build.MANUFACTURER.equals("huawei", ignoreCase = true) ||
               Build.BRAND.equals("huawei", ignoreCase = true) ||
               Build.BRAND.equals("honor", ignoreCase = true)
    }
    
    fun hasGooglePlayServices(): Boolean {
        return try {
            val packageManager = context.packageManager
            packageManager.getPackageInfo("com.google.android.gms", 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }
    
    fun handleEMUIFeatures(context: Context) {
        if (isHuaweiDevice()) {
            // EMUI specific handling
            handleHuaweiBatteryOptimization(context)
            handleHuaweiAutoStart(context)
        }
    }
    
    private fun handleHuaweiBatteryOptimization(context: Context) {
        try {
            val intent = Intent().apply {
                component = ComponentName(
                    "com.huawei.systemmanager",
                    "com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity"
                )
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            // Fallback to standard battery optimization
            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                data = Uri.parse("package:${context.packageName}")
            }
            context.startActivity(intent)
        }
    }
}
```

## 🧪 Compatibility Testing

### Device Testing Matrix

```kotlin
class CompatibilityTestSuite {
    
    data class TestDevice(
        val manufacturer: String,
        val model: String,
        val androidVersion: Int,
        val apiLevel: Int,
        val architecture: String,
        val screenSize: String,
        val density: String
    )
    
    val testDevices = listOf(
        // Samsung
        TestDevice("Samsung", "Galaxy S23", 13, 33, "arm64-v8a", "6.1\"", "XXHDPI"),
        TestDevice("Samsung", "Galaxy A54", 13, 33, "arm64-v8a", "6.4\"", "XHDPI"),
        TestDevice("Samsung", "Galaxy Tab S8", 12, 31, "arm64-v8a", "11\"", "XHDPI"),
        
        // Google Pixel
        TestDevice("Google", "Pixel 7", 13, 33, "arm64-v8a", "6.3\"", "XXHDPI"),
        TestDevice("Google", "Pixel 6a", 13, 33, "arm64-v8a", "6.1\"", "XHDPI"),
        
        // Xiaomi
        TestDevice("Xiaomi", "Mi 11", 12, 31, "arm64-v8a", "6.81\"", "XXHDPI"),
        TestDevice("Xiaomi", "Redmi Note 11", 11, 30, "arm64-v8a", "6.43\"", "XHDPI"),
        
        // OnePlus
        TestDevice("OnePlus", "OnePlus 10 Pro", 12, 31, "arm64-v8a", "6.7\"", "XXHDPI"),
        
        // Older devices for compatibility
        TestDevice("Samsung", "Galaxy S9", 10, 29, "arm64-v8a", "5.8\"", "XXHDPI"),
        TestDevice("Google", "Pixel 3", 9, 28, "arm64-v8a", "5.5\"", "XXHDPI"),
        
        // Budget devices
        TestDevice("Nokia", "Nokia 5.4", 11, 30, "arm64-v8a", "6.39\"", "HDPI"),
        TestDevice("Motorola", "Moto G Power", 10, 29, "arm64-v8a", "6.4\"", "HDPI")
    )
    
    @Test
    fun testCompatibilityAcrossDevices() {
        testDevices.forEach { device ->
            android.util.Log.d("CompatTest", "Testing on ${device.manufacturer} ${device.model}")
            
            // Test core functionality
            testBasicFunctionality(device)
            testStorageAccess(device)
            testNetworkOperations(device)
            testUIRendering(device)
        }
    }
    
    private fun testBasicFunctionality(device: TestDevice) {
        // Test URL validation
        assertTrue("URL validation failed on ${device.model}", 
            FileUtils.isValidYouTubeUrl("https://youtube.com/watch?v=test"))
        
        // Test file operations
        val testFile = File.createTempFile("test", ".mp4")
        assertTrue("File operations failed on ${device.model}", testFile.exists())
        testFile.delete()
    }
    
    private fun testStorageAccess(device: TestDevice) {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val downloadDir = FileUtils.getDownloadDirectory(context)
        
        assertTrue("Storage access failed on ${device.model}", 
            downloadDir.exists() || downloadDir.mkdirs())
    }
    
    private fun testNetworkOperations(device: TestDevice) {
        // Test network connectivity
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        
        assertNotNull("Network not available on ${device.model}", networkInfo)
        assertTrue("Network not connected on ${device.model}", networkInfo?.isConnected == true)
    }
    
    private fun testUIRendering(device: TestDevice) {
        // Test UI components render correctly
        val composeTestRule = createComposeRule()
        
        composeTestRule.setContent {
            DownloadScreen()
        }
        
        composeTestRule.onNodeWithText("YouTube Downloader")
            .assertIsDisplayed()
    }
}
```

### Automated Compatibility Checks

```kotlin
class CompatibilityChecker(private val context: Context) {
    
    data class CompatibilityReport(
        val isCompatible: Boolean,
        val warnings: List<String>,
        val recommendations: List<String>,
        val deviceInfo: DeviceInfo
    )
    
    fun checkCompatibility(): CompatibilityReport {
        val warnings = mutableListOf<String>()
        val recommendations = mutableListOf<String>()
        var isCompatible = true
        
        // Check Android version
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            isCompatible = false
            warnings.add("Android version ${Build.VERSION.RELEASE} is not supported. Minimum required: Android 7.0")
        }
        
        // Check available RAM
        val memoryInfo = getMemoryInfo()
        if (memoryInfo.totalMem < 2L * 1024 * 1024 * 1024) { // 2GB
            warnings.add("Low RAM detected (${formatBytes(memoryInfo.totalMem)}). App may experience performance issues.")
            recommendations.add("Close other apps before downloading large files")
        }
        
        // Check storage space
        val availableSpace = getAvailableStorageSpace()
        if (availableSpace < 500L * 1024 * 1024) { // 500MB
            warnings.add("Low storage space (${formatBytes(availableSpace)})")
            recommendations.add("Free up storage space before downloading")
        }
        
        // Check network capabilities
        if (!hasNetworkConnection()) {
            warnings.add("No network connection detected")
            recommendations.add("Connect to WiFi or mobile data")
        }
        
        // Check permissions
        if (!hasStoragePermissions()) {
            warnings.add("Storage permissions not granted")
            recommendations.add("Grant storage permissions in app settings")
        }
        
        // Check manufacturer-specific issues
        checkManufacturerCompatibility(warnings, recommendations)
        
        return CompatibilityReport(
            isCompatible = isCompatible,
            warnings = warnings,
            recommendations = recommendations,
            deviceInfo = getDeviceInfo()
        )
    }
    
    private fun checkManufacturerCompatibility(
        warnings: MutableList<String>,
        recommendations: MutableList<String>
    ) {
        when (Build.MANUFACTURER.lowercase()) {
            "xiaomi" -> {
                warnings.add("MIUI detected - may require additional permissions")
                recommendations.add("Enable 'Display pop-up windows while running in the background' in app settings")
            }
            "huawei" -> {
                warnings.add("EMUI detected - may require battery optimization settings")
                recommendations.add("Add app to protected apps list in battery settings")
            }
            "oppo", "realme" -> {
                warnings.add("ColorOS detected - may require auto-start permissions")
                recommendations.add("Enable auto-start for this app in settings")
            }
            "vivo" -> {
                warnings.add("FunTouch OS detected - may require background app permissions")
                recommendations.add("Allow app to run in background in settings")
            }
        }
    }
    
    private fun getDeviceInfo(): DeviceInfo {
        return DeviceInfo(
            manufacturer = Build.MANUFACTURER,
            model = Build.MODEL,
            androidVersion = Build.VERSION.RELEASE,
            apiLevel = Build.VERSION.SDK_INT,
            architecture = Build.SUPPORTED_ABIS.firstOrNull() ?: "unknown",
            totalRam = getMemoryInfo().totalMem,
            availableStorage = getAvailableStorageSpace(),
            screenDensity = context.resources.displayMetrics.densityDpi,
            screenSize = "${context.resources.displayMetrics.widthPixels}x${context.resources.displayMetrics.heightPixels}"
        )
    }
}

data class DeviceInfo(
    val manufacturer: String,
    val model: String,
    val androidVersion: String,
    val apiLevel: Int,
    val architecture: String,
    val totalRam: Long,
    val availableStorage: Long,
    val screenDensity: Int,
    val screenSize: String
)
```

## 📋 Compatibility Checklist

### Pre-Release Testing

```
✅ Android Version Testing:
- [ ] Android 7.0 (API 24) - Minimum support
- [ ] Android 8.0 (API 26) - Notification channels
- [ ] Android 9.0 (API 28) - Network security
- [ ] Android 10 (API 29) - Scoped storage
- [ ] Android 11 (API 30) - Package visibility
- [ ] Android 12 (API 31) - Material You
- [ ] Android 13 (API 33) -
