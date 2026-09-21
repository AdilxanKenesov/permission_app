<div align="center">

# 🔐 My Permission App

**A hands-on guide to Android permissions: runtime, special, normal and custom, with working examples in Jetpack Compose**

![Kotlin](https://img.shields.io/badge/Kotlin-2.2-7F52FF?style=flat&logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-Material%203-4285F4?style=flat&logo=jetpackcompose&logoColor=white)
![Android](https://img.shields.io/badge/Android-24%2B-3DDC84?style=flat&logo=android&logoColor=white)
![No libraries](https://img.shields.io/badge/Permission%20libs-none-lightgrey?style=flat)

</div>

---

## 📖 Table of Contents

1. [About the App](#-about-the-app)
2. [Permission Types](#-permission-types)
3. [All Runtime Permissions](#-all-runtime-permissions)
4. [Common Special Permissions](#-common-special-permissions)
5. [The Request Flow](#-the-request-flow)
6. [Ways to Request Permissions](#-ways-to-request-permissions)
7. [Custom Permissions](#-custom-permissions)
8. [Things Every Developer Should Know](#-things-every-developer-should-know)
9. [Testing with adb](#-testing-with-adb)
10. [Best Practices](#-best-practices)
11. [Project Structure & Getting Started](#-project-structure)

---

## 📱 About the App

The app **simulates five real apps asking for runtime permissions** and **one app asking for a special permission**. It also shows every permission type live, with a real status check for each.

| Tab | What it shows |
|-----|---------------|
| **Runtime** | 5 real-app scenarios, each with a status chip, a rationale dialog and an "Open settings" fallback |
| **Special** | `SYSTEM_ALERT_WINDOW`: the user grants it in Settings, then a floating bubble appears on top of other apps |
| **Types** | Normal, signature/custom, runtime and special permissions, each with its current status |

### The 5 runtime demos

| # | Scenario | Permission | What it demonstrates | Result |
|---|----------|------------|----------------------|--------|
| 1 | 📷 **Scan a document** (scanner app) | `CAMERA` | Single request and a **rationale dialog** after the first denial | Opens the camera and shows the photo |
| 2 | 📍 **Find nearby places** (maps app) | `ACCESS_FINE_LOCATION` + `ACCESS_COARSE_LOCATION` | `RequestMultiplePermissions`, **precise vs approximate** | Shows which accuracy the user allowed |
| 3 | 🎤 **Record a voice message** (messenger) | `RECORD_AUDIO` | **One-time permission** ("Only this time") | Simulated recording |
| 4 | 👥 **Invite friends** (social app) | `READ_CONTACTS` | Reading private data | Real number of contacts on the device |
| 5 | 🔔 **Enable reminders** (to-do app) | `POST_NOTIFICATIONS` | A permission that exists **only on Android 13+** | Posts a real notification |

### The special permission demo

| Scenario | Permission | Check | Request |
|----------|------------|-------|---------|
| 💬 **Chat bubbles** (like chat heads) | `SYSTEM_ALERT_WINDOW` | `Settings.canDrawOverlays()` | `Settings.ACTION_MANAGE_OVERLAY_PERMISSION` |

Every card follows the same logic:

```
Not requested ──tap──► system dialog ──Allow──► Granted ✅ (feature runs)
                              │
                            Deny
                              ▼
                          Denied ──tap──► our rationale dialog ──► system dialog again
                                                                        │
                                                                      Deny
                                                                        ▼
                                                        Permanently denied ──► "Open settings"
```

---

## 🧩 Permission Types

Android has **four kinds** of permissions. The kind is set by the permission's `protectionLevel`.

| Type | `protectionLevel` | Who grants it | How | Examples |
|------|-------------------|---------------|-----|----------|
| **Install-time: normal** | `normal` | System | Automatically at install | `INTERNET`, `ACCESS_NETWORK_STATE`, `VIBRATE`, `SET_ALARM`, `FOREGROUND_SERVICE` |
| **Install-time: signature** | `signature` | System | Only if the app is signed with the **same certificate** as the declaring app | Your own custom permissions, system-internal permissions |
| **Runtime (dangerous)** | `dangerous` | **User** | A system dialog, while the app is running | `CAMERA`, `ACCESS_FINE_LOCATION`, `READ_CONTACTS` |
| **Special** | `appop` | **User** | A switch in **Settings → Special app access** | `SYSTEM_ALERT_WINDOW`, `MANAGE_EXTERNAL_STORAGE`, `SCHEDULE_EXACT_ALARM` |

All of them must be declared in `AndroidManifest.xml`:

```xml
<!-- normal: nothing else to do -->
<uses-permission android:name="android.permission.INTERNET" />

<!-- dangerous: must also be requested at runtime -->
<uses-permission android:name="android.permission.CAMERA" />

<!-- special: the user must enable it in Settings -->
<uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW" />
```

> Runtime permissions were introduced in **Android 6.0 (API 23)**. Before that, every permission was granted at install time.

---

## 📋 All Runtime Permissions

Grouped the way Android shows them in Settings. Group membership can change between releases, so always request the **exact** permission you need.

| Group | Permissions | Notes |
|-------|-------------|-------|
| 📅 **Calendar** | `READ_CALENDAR`, `WRITE_CALENDAR` | |
| 📷 **Camera** | `CAMERA` | Supports one-time grants (Android 11+) |
| 👥 **Contacts** | `READ_CONTACTS`, `WRITE_CONTACTS`, `GET_ACCOUNTS` | |
| 📍 **Location** | `ACCESS_COARSE_LOCATION`, `ACCESS_FINE_LOCATION`, `ACCESS_BACKGROUND_LOCATION` | Background: API 29+, requested **separately**. Approximate option: Android 12+ |
| 🎤 **Microphone** | `RECORD_AUDIO` | Supports one-time grants |
| 📞 **Phone** | `READ_PHONE_STATE`, `READ_PHONE_NUMBERS`, `CALL_PHONE`, `ANSWER_PHONE_CALLS`, `ADD_VOICEMAIL`, `USE_SIP` | |
| 🧾 **Call logs** | `READ_CALL_LOG`, `WRITE_CALL_LOG` | Google Play restricts these |
| ✉️ **SMS** | `SEND_SMS`, `RECEIVE_SMS`, `READ_SMS`, `RECEIVE_MMS`, `RECEIVE_WAP_PUSH` | Google Play restricts these |
| 🖼 **Photos & videos** | `READ_MEDIA_IMAGES`, `READ_MEDIA_VIDEO` (API 33+), `READ_MEDIA_VISUAL_USER_SELECTED` (API 34+) | API 34+: the user can grant access to **selected photos only** |
| 🎵 **Music & audio** | `READ_MEDIA_AUDIO` (API 33+) | |
| 📁 **Storage (legacy)** | `READ_EXTERNAL_STORAGE` (≤ API 32), `WRITE_EXTERNAL_STORAGE` (≤ API 28) | Replaced by the media permissions above and scoped storage |
| 🗺 **Media location** | `ACCESS_MEDIA_LOCATION` (API 29+) | GPS data inside photos |
| 🏃 **Physical activity** | `ACTIVITY_RECOGNITION` (API 29+) | Step counters, activity detection |
| 📡 **Nearby devices** | `BLUETOOTH_SCAN`, `BLUETOOTH_CONNECT`, `BLUETOOTH_ADVERTISE`, `UWB_RANGING` (API 31+), `NEARBY_WIFI_DEVICES` (API 33+) | Replace the old location requirement for Bluetooth/Wi-Fi scanning |
| ❤️ **Body sensors** | `BODY_SENSORS`, `BODY_SENSORS_BACKGROUND` (API 33+) | Android 16 moves apps to granular `android.permission.health.*` permissions |
| 🔔 **Notifications** | `POST_NOTIFICATIONS` (API 33+) | Below Android 13, notifications are allowed by default |

> 🛈 Full, always up-to-date list: [`Manifest.permission`](https://developer.android.com/reference/android/Manifest.permission). Look for "Protection level: dangerous".

---

## ⭐ Common Special Permissions

Special permissions **never** use `checkSelfPermission()` or the permission dialog. Each has its own check method and its own Settings screen:

| Permission | Check | Settings intent | Since |
|------------|-------|-----------------|-------|
| `SYSTEM_ALERT_WINDOW` (display over other apps) | `Settings.canDrawOverlays(ctx)` | `Settings.ACTION_MANAGE_OVERLAY_PERMISSION` | API 23 |
| `WRITE_SETTINGS` (modify system settings) | `Settings.System.canWrite(ctx)` | `Settings.ACTION_MANAGE_WRITE_SETTINGS` | API 23 |
| `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS` | `PowerManager.isIgnoringBatteryOptimizations(pkg)` | `Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS` | API 23 |
| `ACCESS_NOTIFICATION_POLICY` (Do Not Disturb) | `NotificationManager.isNotificationPolicyAccessGranted()` | `Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS` | API 23 |
| `REQUEST_INSTALL_PACKAGES` (install unknown apps) | `packageManager.canRequestPackageInstalls()` | `Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES` | API 26 |
| `MANAGE_EXTERNAL_STORAGE` (all files access) | `Environment.isExternalStorageManager()` | `Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION` | API 30 |
| `SCHEDULE_EXACT_ALARM` | `AlarmManager.canScheduleExactAlarms()` | `Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM` | API 31 |
| `USE_FULL_SCREEN_INTENT` | `NotificationManager.canUseFullScreenIntent()` | `Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT` | API 34 |
| `PACKAGE_USAGE_STATS` (usage access) | `AppOpsManager` → `OPSTR_GET_USAGE_STATS` | `Settings.ACTION_USAGE_ACCESS_SETTINGS` | API 21 |
| Notification listener | `NotificationManagerCompat.getEnabledListenerPackages(ctx)` | `Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS` | API 18 |

### How this app requests `SYSTEM_ALERT_WINDOW`

```kotlin
// 1. Check with the permission's own API
var canDrawOverlays by remember { mutableStateOf(Settings.canDrawOverlays(context)) }

// 2. There is no result callback: re-check whenever the user comes back
LifecycleResumeEffect(Unit) {
    canDrawOverlays = Settings.canDrawOverlays(context)
    onPauseOrDispose { }
}

// 3. After explaining why, send the user to the Settings page
val intent = Intent(
    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
    Uri.parse("package:${context.packageName}")
)
context.startActivity(intent)
```

---

## 🔄 The Request Flow

The flow recommended by Google, which every demo in this app follows:

```
            ┌──────────────────────────┐
            │  User taps a feature     │
            └────────────┬─────────────┘
                         ▼
         checkSelfPermission() == GRANTED ? ──Yes──► ✅ Run the feature
                         │ No
                         ▼
     shouldShowRequestPermissionRationale() ? ──Yes──► Show YOUR explanation dialog
                         │ No                                 │ "Continue"
                         ▼                                    ▼
               launcher.launch(permission)  ◄─────────────────┘
                         │
                 ┌───────┴────────┐
              Allowed           Denied
                 ▼                 ▼
          ✅ Run feature   Degrade gracefully (the feature stays off,
                            and the rest of the app works)
```

**`shouldShowRequestPermissionRationale()` returns:**

| Situation | Returns |
|-----------|---------|
| Never asked | `false` |
| Denied once | **`true`**: show your explanation |
| Denied twice / "Don't ask again" | `false`: the dialog **won't appear anymore** |
| Granted | `false` |

"Never asked" and "permanently denied" both return `false`. To tell them apart, an app has to remember whether it has already asked. This app stores that in `SharedPreferences` (see `PermissionRequest.kt`).

---

## 🛠 Ways to Request Permissions

### 1. Activity Result API: one permission ✅ recommended

```kotlin
class CameraActivity : ComponentActivity() {

    private val requestCamera = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) openCamera() else showCameraUnavailable()
    }

    private fun onScanClicked() {
        when {
            checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED ->
                openCamera()
            shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) ->
                showRationaleDialog { requestCamera.launch(Manifest.permission.CAMERA) }
            else ->
                requestCamera.launch(Manifest.permission.CAMERA)
        }
    }
}
```

> `registerForActivityResult` must be called **before** the Activity/Fragment is created (as a property), not inside a click listener.

### 2. Activity Result API: several permissions

```kotlin
private val requestLocation = registerForActivityResult(
    ActivityResultContracts.RequestMultiplePermissions()
) { result: Map<String, Boolean> ->
    when {
        result[Manifest.permission.ACCESS_FINE_LOCATION] == true -> usePreciseLocation()
        result[Manifest.permission.ACCESS_COARSE_LOCATION] == true -> useApproximateLocation()
        else -> showLocationUnavailable()
    }
}

requestLocation.launch(
    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
)
```

### 3. Jetpack Compose

```kotlin
@Composable
fun CameraButton() {
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted -> if (isGranted) { /* open camera */ } }

    Button(onClick = { launcher.launch(Manifest.permission.CAMERA) }) {
        Text("Scan document")
    }
}
```

### 4. Legacy API (older code) ⚠️ deprecated

```kotlin
ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), REQUEST_CAMERA)

override fun onRequestPermissionsResult(
    requestCode: Int, permissions: Array<String>, grantResults: IntArray
) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    if (requestCode == REQUEST_CAMERA &&
        grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED
    ) openCamera()
}
```

You'll still see this in older projects. The Activity Result API replaces it: no request codes, and the callback lives next to the request.

### 5. Accompanist Permissions (Compose library)

```kotlin
// implementation("com.google.accompanist:accompanist-permissions:<latest>")
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraFeature() {
    val cameraPermission = rememberPermissionState(Manifest.permission.CAMERA)

    when {
        cameraPermission.status.isGranted -> CameraPreview()
        cameraPermission.status.shouldShowRationale -> RationaleUi { cameraPermission.launchPermissionRequest() }
        else -> Button(onClick = { cameraPermission.launchPermissionRequest() }) { Text("Allow camera") }
    }
}
```

The API is still **experimental**.

### 6. PermissionX (View-based library)

```kotlin
// implementation("com.guolindev.permissionx:permissionx:<latest>")
PermissionX.init(activity)
    .permissions(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
    .onExplainRequestReason { scope, deniedList ->
        scope.showRequestReasonDialog(deniedList, "Needed to record video", "OK", "Cancel")
    }
    .onForwardToSettings { scope, deniedList ->
        scope.showForwardToSettingsDialog(deniedList, "Enable it in Settings", "OK", "Cancel")
    }
    .request { allGranted, grantedList, deniedList -> /* ... */ }
```

The rationale and "go to Settings" dialogs are built in.

### 7. This app's custom helper: `rememberPermissionRequest`

A small alternative to Accompanist with no extra libraries. It wraps `RequestMultiplePermissions`, works out all four states (including **permanently denied**), and refreshes itself when the user returns from Settings:

```kotlin
val camera = rememberPermissionRequest(listOf(Manifest.permission.CAMERA)) { granted ->
    if (granted) openCamera()
}

when (camera.status) {
    PermissionStatus.Granted            -> openCamera()
    PermissionStatus.Denied             -> showRationale = true   // then camera.launch()
    PermissionStatus.PermanentlyDenied  -> camera.openSettings()
    PermissionStatus.NotRequested       -> camera.launch()
}
```

`PermissionCard` wraps this flow into a reusable UI component. See [`permission/PermissionRequest.kt`](app/src/main/java/uz/gita/mypermissionapp/permission/PermissionRequest.kt).

### Which one should I use?

| Situation | Use |
|-----------|-----|
| New View/Fragment code | Activity Result API (1, 2) |
| Compose | `rememberLauncherForActivityResult` (3), or a small helper like (7) |
| Want ready-made dialogs | PermissionX (6) |
| Maintaining old code | You'll meet (4). Migrate it when you can |

---

## 🏷 Custom Permissions

An app can **define its own permission** to protect its components (Activities, Services, BroadcastReceivers, ContentProviders) from other apps.

**Declare it** (in the app that owns the component):

```xml
<permission
    android:name="uz.gita.mypermissionapp.permission.READ_NOTES"
    android:label="@string/permission_read_notes_label"
    android:description="@string/permission_read_notes_description"
    android:protectionLevel="signature" />

<activity
    android:name=".NotesActivity"
    android:exported="true"
    android:permission="uz.gita.mypermissionapp.permission.READ_NOTES" />
```

**Use it** (in any app that wants access):

```xml
<uses-permission android:name="uz.gita.mypermissionapp.permission.READ_NOTES" />
```

| `protectionLevel` | Who gets the custom permission |
|-------------------|--------------------------------|
| `normal` | Any app that declares `<uses-permission>` |
| `dangerous` | Apps that declare it **and** get user approval at runtime |
| `signature` ✅ | Only apps signed with **your** certificate. Best for sharing data between your own apps |

Tips:
- Prefix the name with your package name, so it can't clash with another app's permission.
- Other protected components use the same attribute (`<service android:permission=…>`, `<receiver android:permission=…>`). A `<provider>` can also set separate `android:readPermission` and `android:writePermission`.
- In this app, the **Types** tab shows `READ_NOTES` as granted, because the app holds its own signature permission, and opens the protected `NotesActivity`.

---

## 💡 Things Every Developer Should Know

| Topic | What happens |
|-------|--------------|
| **Two denials = permanent** (Android 11+) | After the user denies twice, the dialog no longer appears. The only way back is Settings. |
| **One-time permissions** (Android 11+) | For camera, microphone and location, the user can choose "Only this time". The permission is revoked soon after the app goes to the background. |
| **Approximate location** (Android 12+) | The user can grant `COARSE` even if you asked for `FINE`. Always request both together. |
| **Background location** (Android 10+) | Request foreground location first, then `ACCESS_BACKGROUND_LOCATION` separately. On Android 11+ the user must choose "Allow all the time" in Settings. |
| **Auto-reset** (Android 11+) | Permissions of apps unused for a few months are revoked automatically. |
| **Revoking in Settings kills the app** | If the user revokes a runtime permission while the app runs, the process is restarted. Always re-check permissions and never cache the result. |
| **Give permissions back** (Android 13+) | `context.revokeSelfPermissionsOnKill(listOf(...))` revokes permissions you no longer need. This app uses it for its **Reset demo** button. |
| **Partial photo access** (Android 14+) | The user can allow only selected photos (`READ_MEDIA_VISUAL_USER_SELECTED`). |
| **Photo Picker needs no permission** | To let the user choose images, use `PickVisualMedia`. No storage permission is required. |

---

## 🧪 Testing with adb

```bash
PKG=uz.gita.mypermissionapp

# Grant / revoke a runtime permission
adb shell pm grant  $PKG android.permission.CAMERA
adb shell pm revoke $PKG android.permission.CAMERA

# Undo "permanently denied" so the dialog shows again
adb shell pm clear-permission-flags $PKG android.permission.CAMERA user-set user-fixed

# See what the app has been granted
adb shell dumpsys package $PKG | grep -A1 "permission"

# Special permission (app op): allow / deny "display over other apps"
adb shell appops set $PKG SYSTEM_ALERT_WINDOW allow
adb shell appops set $PKG SYSTEM_ALERT_WINDOW deny

# Start completely fresh
adb shell pm clear $PKG
```

---

## ✅ Best Practices

1. **Ask in context.** Request a permission when the user taps the feature, not on app launch.
2. **Ask for the minimum.** Before adding a permission, check for a no-permission alternative (Photo Picker, `ACTION_IMAGE_CAPTURE`, the contact picker).
3. **Explain why.** Show your own rationale when `shouldShowRequestPermissionRationale()` is `true`.
4. **Degrade gracefully.** A denied permission should turn off one feature, not the whole app.
5. **Respect "no".** Don't nag. After a permanent denial, offer a single "Open settings" button.
6. **Check every time.** Permissions can be revoked at any moment (Settings, one-time grants, auto-reset).
7. **Declare hardware as optional:** `<uses-feature android:name="android.hardware.camera" android:required="false" />`.

---

## 📂 Project Structure

```
app/src/main/java/uz/gita/mypermissionapp/
├── MainActivity.kt
├── NotesActivity.kt               # Exported, protected by the custom READ_NOTES permission
├── permission/
│   ├── PermissionStatus.kt        # NotRequested · Granted · Denied · PermanentlyDenied
│   ├── PermissionRequest.kt       # rememberPermissionRequest(): custom Compose helper
│   └── PermissionUtils.kt         # isGranted(), findActivity(), openAppSettings()
├── components/
│   ├── PermissionCard.kt          # Reusable card: status chip, flow, Settings fallback
│   └── RationaleDialog.kt         # "Why we need this" dialog
├── overlay/
│   └── OverlayBubble.kt           # WindowManager bubble (needs SYSTEM_ALERT_WINDOW)
├── screen/
│   ├── MainScreen.kt              # Bottom navigation (Runtime · Special · Types)
│   ├── RuntimeScreen.kt           # The 5 runtime demos + reset
│   ├── SpecialScreen.kt           # Special permission demo
│   └── TypesScreen.kt             # Live overview of all permission types
└── ui/theme/
```

## 🛠 Tech Stack

| Category | Details |
|----------|---------|
| Language | Kotlin `2.2` |
| UI | Jetpack Compose (BOM `2026.02.01`), Material 3, Material Icons Extended |
| Navigation | Navigation Compose `2.9.8` |
| Lifecycle | `lifecycle-runtime-compose` (`LifecycleResumeEffect`) |
| Permissions | Plain AndroidX Activity Result API, with no third-party permission library |
| SDK | min `24` · target `36` · compile `37` · AGP `9.2.1` · Java 11 |

## 🚀 Getting Started

**Requirements:** a recent Android Studio, JDK 11+, and an Android 7.0+ device or emulator. Android 13+ is recommended, to see every behavior (notifications, reset).

```bash
git clone <repository-url>
cd MyPermissionApp
./gradlew installDebug
```

**Try this:**
1. **Runtime** tab → tap **Scan document** → deny → tap again: the rationale appears → deny again: now it's **Permanently denied**, and **Open settings** is the only way back.
2. **Find nearby** → choose **Approximate**: the card shows approximate access.
3. **Special** tab → allow **Display over other apps** → **Show floating bubble** → press Home: the bubble stays on top.
4. **Types** tab → see all four permission types with their live status.

## 📚 References

- [Permissions on Android](https://developer.android.com/guide/topics/permissions/overview)
- [Request runtime permissions](https://developer.android.com/training/permissions/requesting)
- [Request special permissions](https://developer.android.com/training/permissions/requesting-special)
- [Define a custom app permission](https://developer.android.com/guide/topics/permissions/defining)
- [App permissions best practices](https://developer.android.com/training/permissions/usage-notes)
- [`Manifest.permission` reference](https://developer.android.com/reference/android/Manifest.permission)
