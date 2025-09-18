# 🔐 Permissions Guide

Panduan lengkap tentang permissions yang dibutuhkan Local YouTube Downloader Android dan cara mengelolanya.

## 🎯 Overview

Aplikasi ini memerlukan beberapa permissions untuk berfungsi dengan baik. Guide ini menjelaskan setiap permission, mengapa dibutuhkan, dan cara mengaturnya di berbagai versi Android.

## 📋 Required Permissions

### Core Permissions

```xml
<!-- AndroidManifest.xml -->
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" 
    android:maxSdkVersion="29" />
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" 
    android:maxSdkVersion="32" />
<uses-permission android:name="android.permission.MANAGE_EXTERNAL_STORAGE" 
    tools:ignore="ScopedStorage" />
<uses-permission android:name="android.permission.REQUEST_INSTALL_PACKAGES" />
<uses-permission android:name="android.permission.WAKE_LOCK" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" 
    android:minSdkVersion="33" />
```

### Permission Details

| Permission | Required | Purpose | Android Version |
|------------|----------|---------|-----------------|
| `INTERNET` | ✅ Essential | Download videos from YouTube | All versions |
| `ACCESS_NETWORK_STATE` | ✅ Essential | Check network connectivity | All versions |
| `WRITE_EXTERNAL_STORAGE` | ✅ Essential | Save downloaded files | Android 6-10 |
| `READ_EXTERNAL_STORAGE` | ✅ Essential | Access downloaded files | Android 6-12 |
| `MANAGE_EXTERNAL_STORAGE` | ⚠️ Conditional | Full storage access | Android 11+ |
| `REQUEST_INSTALL_PACKAGES` | ❌ Optional | Install APK updates | All versions |
| `WAKE_LOCK` | ⚠️ Conditional | Keep device awake during download | All versions |
| `FOREGROUND_SERVICE` | ⚠️ Conditional | Background download service | Android 8+ |
| `POST_NOTIFICATIONS` | ⚠️ Conditional | Show download notifications | Android 13+ |

## 🔧 Permission Implementation

### Permission Manager

```kotlin
class PermissionManager(private val context: Context) {
    
    companion object {
        const val STORAGE_PERMISSION_REQUEST = 1001
        const val NOTIFICATION_PERMISSION_REQUEST = 1002
        const val ALL_FILES_ACCESS_REQUEST = 1003
    }
    
    // Check if all required permissions are granted
    fun hasAllRequiredPermissions(): Boolean {
        return hasInternetPermission() && 
               hasStoragePermissions() && 
               hasNotificationPermission()
    }
    
    // Internet permission (granted at install time)
    fun hasInternetPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context, 
            Manifest.permission.INTERNET
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    // Storage permissions (version-dependent)
    fun hasStoragePermissions(): Boolean {
        return when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
                // Android 11+: Check for all files access
                Environment.isExternalStorageManager()
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> {
                // Android 6-10: Check runtime permissions
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
            }
            else -> {
                // Android 5 and below: Permissions granted at install
                true
            }
        }
    }
    
    // Notification permission (Android 13+)
    fun hasNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true // Not required for older versions
        }
    }
    
    // Network state permission (granted at install time)
    fun hasNetworkStatePermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_NETWORK_STATE
        ) == PackageManager.PERMISSION_GRANTED
    }
}
```

### Permission Request Handler

```kotlin
class PermissionRequestHandler(private val activity: Activity) {
    
    private val permissionManager = PermissionManager(activity)
    
    fun requestAllPermissions(callback: (Boolean) -> Unit) {
        when {
            permissionManager.hasAllRequiredPermissions() -> {
                callback(true)
            }
            else -> {
                requestMissingPermissions(callback)
            }
        }
    }
    
    private fun requestMissingPermissions(callback: (Boolean) -> Unit) {
        // Request storage permissions first
        if (!permissionManager.hasStoragePermissions()) {
            requestStoragePermissions { storageGranted ->
                if (storageGranted) {
                    // Then request notification permissions
                    requestNotificationPermissions(callback)
                } else {
                    callback(false)
                }
            }
        } else {
            requestNotificationPermissions(callback)
        }
    }
    
    fun requestStoragePermissions(callback: (Boolean) -> Unit) {
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
                requestAllFilesAccess(callback)
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> {
                requestRuntimeStoragePermissions(callback)
            }
            else -> {
                callback(true) // Permissions granted at install time
            }
        }
    }
    
    private fun requestAllFilesAccess(callback: (Boolean) -> Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (Environment.isExternalStorageManager()) {
                callback(true)
            } else {
                showAllFilesAccessDialog { granted ->
                    if (granted) {
                        val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                            data = Uri.parse("package:${activity.packageName}")
                        }
                        activity.startActivityForResult(intent, PermissionManager.ALL_FILES_ACCESS_REQUEST)
                    }
                    callback(granted)
                }
            }
        }
    }
    
    private fun requestRuntimeStoragePermissions(callback: (Boolean) -> Unit) {
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
                PermissionManager.STORAGE_PERMISSION_REQUEST
            )
        }
    }
    
    fun requestNotificationPermissions(callback: (Boolean) -> Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    activity,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                callback(true)
            } else {
                ActivityCompat.requestPermissions(
                    activity,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    PermissionManager.NOTIFICATION_PERMISSION_REQUEST
                )
            }
        } else {
            callback(true) // Not required for older versions
        }
    }
    
    private fun showAllFilesAccessDialog(callback: (Boolean) -> Unit) {
        AlertDialog.Builder(activity)
            .setTitle("Storage Permission Required")
            .setMessage("This app needs access to manage files on your device to save downloaded videos. Please grant 'All files access' permission in the next screen.")
            .setPositiveButton("Grant Permission") { _, _ -> callback(true) }
            .setNegativeButton("Cancel") { _, _ -> callback(false) }
            .setCancelable(false)
            .show()
    }
}
```

### Permission Result Handler

```kotlin
class PermissionResultHandler(private val activity: Activity) {
    
    private val permissionManager = PermissionManager(activity)
    private var pendingCallback: ((Boolean) -> Unit)? = null
    
    fun handlePermissionResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
        callback: (Boolean) -> Unit
    ) {
        when (requestCode) {
            PermissionManager.STORAGE_PERMISSION_REQUEST -> {
                handleStoragePermissionResult(grantResults, callback)
            }
            PermissionManager.NOTIFICATION_PERMISSION_REQUEST -> {
                handleNotificationPermissionResult(grantResults, callback)
            }
        }
    }
    
    fun handleActivityResult(requestCode: Int, resultCode: Int, callback: (Boolean) -> Unit) {
        when (requestCode) {
            PermissionManager.ALL_FILES_ACCESS_REQUEST -> {
                val granted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    Environment.isExternalStorageManager()
                } else {
                    false
                }
                callback(granted)
            }
        }
    }
    
    private fun handleStoragePermissionResult(grantResults: IntArray, callback: (Boolean) -> Unit) {
        val allGranted = grantResults.isNotEmpty() && 
                        grantResults.all { it == PackageManager.PERMISSION_GRANTED }
        
        if (!allGranted) {
            showPermissionDeniedDialog("Storage", callback)
        } else {
            callback(true)
        }
    }
    
    private fun handleNotificationPermissionResult(grantResults: IntArray, callback: (Boolean) -> Unit) {
        val granted = grantResults.isNotEmpty() && 
                     grantResults[0] == PackageManager.PERMISSION_GRANTED
        
        if (!granted) {
            // Notification permission is optional, continue anyway
            showNotificationPermissionInfo()
        }
        callback(true) // Continue even if notification permission denied
    }
    
    private fun showPermissionDeniedDialog(permissionType: String, callback: (Boolean) -> Unit) {
        AlertDialog.Builder(activity)
            .setTitle("Permission Required")
            .setMessage("$permissionType permission is required for the app to function properly. Please grant the permission in app settings.")
            .setPositiveButton("Open Settings") { _, _ ->
                openAppSettings()
                callback(false)
            }
            .setNegativeButton("Cancel") { _, _ -> callback(false) }
            .setCancelable(false)
            .show()
    }
    
    private fun showNotificationPermissionInfo() {
        Toast.makeText(
            activity,
            "Notification permission denied. You won't receive download completion notifications.",
            Toast.LENGTH_LONG
        ).show()
    }
    
    private fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.parse("package:${activity.packageName}")
        }
        activity.startActivity(intent)
    }
}
```

## 📱 Version-Specific Permission Handling

### Android 11+ (API 30+) - Scoped Storage

```kotlin
class Android11PermissionHandler(private val context: Context) {
    
    fun requestStorageAccess(activity: Activity, callback: (Boolean) -> Unit) {
        when {
            Environment.isExternalStorageManager() -> {
                callback(true)
            }
            canRequestAllFilesAccess() -> {
                showAllFilesAccessRationale(activity) { shouldRequest ->
                    if (shouldRequest) {
                        requestAllFilesAccess(activity)
                    } else {
                        // Fallback to scoped storage
                        callback(false)
                    }
                }
            }
            else -> {
                // Use scoped storage
                callback(false)
            }
        }
    }
    
    private fun canRequestAllFilesAccess(): Boolean {
        // Check if app is eligible for MANAGE_EXTERNAL_STORAGE
        return try {
            val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
            intent.data = Uri.parse("package:${context.packageName}")
            intent.resolveActivity(context.packageManager) != null
        } catch (e: Exception) {
            false
        }
    }
    
    private fun showAllFilesAccessRationale(activity: Activity, callback: (Boolean) -> Unit) {
        AlertDialog.Builder(activity)
            .setTitle("Storage Access")
            .setMessage("To save downloads to your preferred location, this app needs access to manage files on your device.\n\nYou can choose:\n• Grant full access (recommended)\n• Use app-specific folder only")
            .setPositiveButton("Grant Full Access") { _, _ -> callback(true) }
            .setNegativeButton("Use App Folder") { _, _ -> callback(false) }
            .setCancelable(false)
            .show()
    }
    
    private fun requestAllFilesAccess(activity: Activity) {
        val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
            data = Uri.parse("package:${activity.packageName}")
        }
        activity.startActivity(intent)
    }
}
```

### Android 10 (API 29) - Scoped Storage Transition

```kotlin
class Android10PermissionHandler(private val context: Context) {
    
    fun handleScopedStorageTransition(): Boolean {
        return if (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q) {
            // Check if legacy external storage is available
            Environment.isExternalStorageLegacy()
        } else {
            false
        }
    }
    
    fun requestStoragePermissions(activity: Activity, callback: (Boolean) -> Unit) {
        val permissions = mutableListOf<String>()
        
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.
