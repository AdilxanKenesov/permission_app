package uz.gita.mypermissionapp.permission

import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.edit
import androidx.lifecycle.compose.LifecycleResumeEffect

/**
 * A small, self-made alternative to Accompanist Permissions.
 *
 * It wraps RequestMultiplePermissions, works out the [PermissionStatus]
 * (including "permanently denied", which Android doesn't report directly)
 * and refreshes itself every time the screen resumes, e.g. after the user
 * comes back from Settings.
 */
@Stable
class PermissionRequestState internal constructor(
    val permissions: List<String>,
    private val context: Context
) {
    internal var launcher: ActivityResultLauncher<Array<String>>? = null
    internal var onResult: (granted: Boolean) -> Unit = {}

    var status by mutableStateOf(PermissionStatus.NotRequested)
        private set

    /** Which of [permissions] are granted right now (e.g. only COARSE location). */
    var grantedPermissions by mutableStateOf(emptySet<String>())
        private set

    val isGranted: Boolean get() = status == PermissionStatus.Granted

    /** Shows the system dialog (or reports "granted" right away if nothing needs asking). */
    fun launch() {
        if (permissions.isEmpty()) {
            refresh()
            onResult(true)
            return
        }
        markRequested()
        launcher?.launch(permissions.toTypedArray())
    }

    fun openSettings() = context.openAppSettings()

    internal fun refresh() {
        grantedPermissions = permissions.filter { context.isGranted(it) }.toSet()

        val activity = context.findActivity()
        status = when {
            // An empty list means "not needed on this Android version" (e.g. notifications below 13)
            permissions.isEmpty() || grantedPermissions.isNotEmpty() -> PermissionStatus.Granted
            permissions.any { ActivityCompat.shouldShowRequestPermissionRationale(activity, it) } ->
                PermissionStatus.Denied
            // Not granted, no rationale, but we asked before → the system won't show the dialog again
            wasRequested() -> PermissionStatus.PermanentlyDenied
            else -> PermissionStatus.NotRequested
        }
    }

    // Android has no API for "was this ever requested?", so we remember it ourselves
    private val prefs = context.requestHistory()

    private fun wasRequested() = permissions.any { prefs.getBoolean(it, false) }

    private fun markRequested() = prefs.edit { permissions.forEach { putBoolean(it, true) } }
}

private fun Context.requestHistory() =
    getSharedPreferences("permission_requests", Context.MODE_PRIVATE)

/** Forgets which permissions were asked, so a fresh install state can be simulated. */
fun clearPermissionRequestHistory(context: Context) =
    context.requestHistory().edit { clear() }

@Composable
fun rememberPermissionRequest(
    permissions: List<String>,
    onResult: (granted: Boolean) -> Unit = {}
): PermissionRequestState {
    val context = LocalContext.current
    val state = remember(permissions) { PermissionRequestState(permissions, context) }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        state.refresh()
        state.onResult(results.values.any { it })
    }

    SideEffect {
        state.launcher = launcher
        state.onResult = onResult
    }

    LifecycleResumeEffect(state) {
        state.refresh()
        onPauseOrDispose { }
    }

    return state
}
