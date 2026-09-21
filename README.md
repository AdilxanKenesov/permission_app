<div align="center">

# 🧭 My Permission App

**A Jetpack Compose sample app with a Material 3 bottom navigation bar, built on Navigation Compose**

![Kotlin](https://img.shields.io/badge/Kotlin-2.2-7F52FF?style=flat&logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-Material%203-4285F4?style=flat&logo=jetpackcompose&logoColor=white)
![Navigation](https://img.shields.io/badge/Navigation%20Compose-2.9.8-3DDC84?style=flat&logo=android&logoColor=white)
![Android](https://img.shields.io/badge/Android-24%2B-3DDC84?style=flat&logo=android&logoColor=white)

</div>

---

## ✨ Overview

The app has three tabs, **Home**, **Profile** and **Settings**, switched from a bottom `NavigationBar`. It shows the standard way to build bottom navigation in Compose: tab state is saved and restored, tapping a tab twice doesn't create duplicate screens, and the back stack stays short.

```
┌───────────────────────────┐
│                           │
│        Home Screen        │   ← NavHost content (blue / gray / yellow)
│                           │
├───────────────────────────┤
│  Home   Profile  Settings │   ← Material 3 NavigationBar
└───────────────────────────┘
```

## 🚀 Features

- 📱 **Three destinations** (`Home`, `Profile` and `Settings`), each a separate composable screen.
- 🧭 **Material 3 `NavigationBar`**: icons from Material Icons Extended, labels, and the selected-tab highlight.
- 🔁 **Tab state saving**: `saveState` / `restoreState` keep each tab's state when you switch away and back.
- 🚫 **No duplicate screens**: `launchSingleTop` together with a route check skips navigation when the tab is already open.
- ↩️ **Short back stack**: `popUpTo(startDestination)` means Back from any tab returns to Home and then exits the app.
- 📐 **Edge-to-edge**: `enableEdgeToEdge()` with `Scaffold` handling the insets.

## ⚙️ How It Works

### 1. Destinations as an enum

All tabs are described in one place, so the bar and the `NavHost` can never get out of sync:

```kotlin
enum class Destination(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val contentDescription: String
) {
    Home("home", "Home", Icons.Default.Home, "Home Screen"),
    Profile("profile", "Profile", Icons.Default.Person, "Profile Screen"),
    Settings("settings", "Settings", Icons.Default.Settings, "Settings Screen")
}
```

### 2. Selected tab from the back stack

The selected item comes from the **current back stack entry**, not from a separate variable. This keeps it correct after the Back button or a configuration change:

```kotlin
val navBackStackEntry by navController.currentBackStackEntryAsState()
val currentRoute = navBackStackEntry?.destination?.route
```

### 3. Navigating between tabs

```kotlin
NavigationBarItem(
    selected = currentRoute == destination.route,
    onClick = {
        if (currentRoute != destination.route) {
            navController.navigate(destination.route) {
                popUpTo(navController.graph.startDestinationId) { saveState = true }
                launchSingleTop = true
                restoreState = true
            }
        }
    },
    icon = { Icon(destination.icon, contentDescription = destination.contentDescription) },
    label = { Text(destination.label) }
)
```

| Option | What it does |
|--------|--------------|
| `popUpTo(startDestinationId)` | Removes the screens above Home, so tabs don't pile up in the back stack |
| `saveState = true` | Saves the state of the screens being removed |
| `restoreState = true` | Restores a tab's saved state when you return to it |
| `launchSingleTop = true` | Doesn't create a second copy of a screen that is already on top |

### 4. The NavHost

```kotlin
Scaffold(bottomBar = { /* NavigationBar */ }) { contentPadding ->
    NavHost(
        navController = navController,
        startDestination = Destination.Home.route,
        modifier = Modifier.padding(contentPadding)
    ) {
        composable(Destination.Home.route) { HomeScreen() }
        composable(Destination.Profile.route) { ProfileScreen() }
        composable(Destination.Settings.route) { SettingsScreen() }
    }
}
```

The `contentPadding` from `Scaffold` keeps the screen content from being hidden behind the navigation bar.

## ➕ Adding a New Tab

1. Add an entry to the enum:
   ```kotlin
   Search("search", "Search", Icons.Default.Search, "Search Screen")
   ```
2. Create the screen composable (`SearchScreen()`).
3. Register it in the `NavHost`:
   ```kotlin
   composable(Destination.Search.route) { SearchScreen() }
   ```

The navigation bar picks up the new tab automatically, because it loops over `Destination.entries`.

## 🛠 Tech Stack

| Category | Library |
|----------|---------|
| Language | Kotlin `2.2` |
| UI | Jetpack Compose (BOM `2026.02.01`), Material 3 |
| Navigation | Navigation Compose `2.9.8` |
| Icons | Material Icons Extended |
| SDK | min `24` · target `36` · compile `37` · AGP `9.2.1` · Java 11 |

## 📂 Project Structure

```
app/src/main/java/uz/gita/mypermissionapp/
├── MainActivity.kt          # Edge-to-edge + theme + MainScreen
├── screen/
│   ├── MainScreen.kt        # Scaffold, NavigationBar, NavHost, Destination enum
│   ├── HomeScreen.kt
│   ├── ProfileScreen.kt
│   └── SettingsScreen.kt
└── ui/theme/                # Color, Theme, Type
```

## 🚀 Getting Started

**Requirements:** a recent Android Studio, JDK 11+, and an Android 7.0+ device or emulator.

```bash
git clone <repository-url>
cd MyPermissionApp
./gradlew installDebug
```

No runtime permissions are needed.
