package uz.gita.mypermissionapp.screen

import android.Manifest
import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.provider.ContactsContract
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import uz.gita.mypermissionapp.components.PermissionCard
import uz.gita.mypermissionapp.permission.clearPermissionRequestHistory
import uz.gita.mypermissionapp.permission.rememberPermissionRequest

/** Five runtime ("dangerous") permissions, each requested the way a real app would. */
@Composable
fun RuntimeScreen(modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                "Runtime permissions are asked in a system dialog, right when the user starts " +
                    "a feature that needs them. Deny a permission twice to see the " +
                    "\"permanently denied\" state.",
                style = MaterialTheme.typography.bodyMedium
            )
        }
        item { CameraDemo() }
        item { LocationDemo() }
        item { MicrophoneDemo() }
        item { ContactsDemo() }
        item { NotificationsDemo() }
        item { ResetDemo() }
    }
}

/** 1. Single permission + rationale dialog: a document scanner opening the camera. */
@Composable
private fun CameraDemo() {
    val context = LocalContext.current
    var photo by remember { mutableStateOf<Bitmap?>(null) }
    val takePicture = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicturePreview()
    ) { bitmap -> if (bitmap != null) photo = bitmap }

    val openCamera = {
        try {
            takePicture.launch(null)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(context, "No camera app found", Toast.LENGTH_SHORT).show()
        }
    }
    val state = rememberPermissionRequest(listOf(Manifest.permission.CAMERA)) { granted ->
        if (granted) openCamera()
    }

    PermissionCard(
        icon = Icons.Default.CameraAlt,
        title = "Scan a document",
        appExample = "Like a scanner or banking app",
        rationale = "The camera is used only to take a photo of your document. " +
            "Nothing is recorded in the background.",
        actionText = "Scan document",
        state = state,
        onGranted = openCamera
    ) {
        photo?.let {
            Image(
                bitmap = it.asImageBitmap(),
                contentDescription = "Scanned document",
                modifier = Modifier.fillMaxWidth().height(160.dp),
                contentScale = ContentScale.Crop
            )
        }
    }
}

/** 2. Several permissions at once: the user may allow only approximate location. */
@Composable
private fun LocationDemo() {
    var result by remember { mutableStateOf<String?>(null) }
    val state = rememberPermissionRequest(
        listOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
    )

    val showAccess = {
        result = if (Manifest.permission.ACCESS_FINE_LOCATION in state.grantedPermissions) {
            "✅ Precise location: showing places within 100 m"
        } else {
            "📍 Approximate location only: showing places in your area (~3 km)"
        }
    }
    LaunchedEffect(state.grantedPermissions) { if (state.isGranted) showAccess() else result = null }

    PermissionCard(
        icon = Icons.Default.LocationOn,
        title = "Find nearby places",
        appExample = "Like a maps or delivery app",
        rationale = "Your location is used to show places near you. " +
            "You can choose \"Approximate\" if you prefer.",
        actionText = "Find nearby",
        state = state,
        onGranted = showAccess,
        note = "Uses RequestMultiplePermissions. On Android 12+ the dialog lets the user " +
            "pick Precise or Approximate."
    ) {
        result?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }
    }
}

/** 3. A permission that supports "Only this time" (one-time permission, Android 11+). */
@Composable
private fun MicrophoneDemo() {
    var secondsLeft by remember { mutableIntStateOf(0) }
    val startRecording = { secondsLeft = 3 }
    val state = rememberPermissionRequest(listOf(Manifest.permission.RECORD_AUDIO)) { granted ->
        if (granted) startRecording()
    }

    LaunchedEffect(secondsLeft) {
        if (secondsLeft > 0) {
            delay(1_000)
            secondsLeft--
        }
    }

    PermissionCard(
        icon = Icons.Default.Mic,
        title = "Record a voice message",
        appExample = "Like a messenger app",
        rationale = "The microphone is needed to record your voice message. " +
            "Recording starts only when you tap Record.",
        actionText = if (secondsLeft > 0) "Recording…" else "Record voice",
        state = state,
        onGranted = startRecording,
        note = "Try \"Only this time\": the permission is revoked soon after you leave the app."
    ) {
        if (secondsLeft > 0) {
            Text("🔴 Recording (simulated)… 0:0$secondsLeft", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

/** 4. Reading private data: the real number of contacts on the device. */
@Composable
private fun ContactsDemo() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var result by remember { mutableStateOf<String?>(null) }

    val loadContacts: () -> Unit = {
        scope.launch {
            val count = withContext(Dispatchers.IO) { countContacts(context) }
            result = "👥 Found $count contacts you can invite"
        }
    }
    val state = rememberPermissionRequest(listOf(Manifest.permission.READ_CONTACTS)) { granted ->
        if (granted) loadContacts()
    }

    PermissionCard(
        icon = Icons.Default.Contacts,
        title = "Invite friends",
        appExample = "Like a social or messaging app",
        rationale = "Your contacts are used to find friends to invite. " +
            "They are never uploaded.",
        actionText = "Find friends",
        state = state,
        onGranted = loadContacts
    ) {
        result?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }
    }
}

private fun countContacts(context: Context): Int =
    context.contentResolver.query(
        ContactsContract.Contacts.CONTENT_URI,
        arrayOf(ContactsContract.Contacts._ID),
        null, null, null
    )?.use { it.count } ?: 0

/** 5. A permission that exists only on newer Android (13+). Older versions allow it by default. */
@Composable
private fun NotificationsDemo() {
    val context = LocalContext.current
    val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        listOf(Manifest.permission.POST_NOTIFICATIONS)
    } else {
        emptyList()
    }
    val sendReminder = { showReminder(context) }
    val state = rememberPermissionRequest(permissions) { granted ->
        if (granted) sendReminder()
    }

    PermissionCard(
        icon = Icons.Default.Notifications,
        title = "Enable reminders",
        appExample = "Like a to-do or calendar app",
        rationale = "Notifications are used only to remind you about your tasks.",
        actionText = "Send test reminder",
        state = state,
        onGranted = sendReminder,
        note = "POST_NOTIFICATIONS became a runtime permission in Android 13 (API 33)."
    )
}

private const val REMINDER_CHANNEL_ID = "reminders"

@SuppressLint("MissingPermission") // only called after the permission is granted
private fun showReminder(context: Context) {
    val manager = NotificationManagerCompat.from(context)
    manager.createNotificationChannel(
        NotificationChannelCompat.Builder(REMINDER_CHANNEL_ID, NotificationManagerCompat.IMPORTANCE_DEFAULT)
            .setName("Reminders")
            .build()
    )
    val notification = NotificationCompat.Builder(context, REMINDER_CHANNEL_ID)
        .setSmallIcon(android.R.drawable.ic_popup_reminder)
        .setContentTitle("Reminder")
        .setContentText("Notifications permission works! 🎉")
        .setAutoCancel(true)
        .build()
    manager.notify(1, notification)
}

/** Android 13+: an app can give its own permissions back. Handy for resetting this demo. */
@Composable
private fun ResetDemo() {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
    val context = LocalContext.current

    OutlinedButton(
        onClick = {
            context.revokeSelfPermissionsOnKill(
                listOf(
                    Manifest.permission.CAMERA,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.READ_CONTACTS,
                    Manifest.permission.POST_NOTIFICATIONS
                )
            )
            clearPermissionRequestHistory(context)
            Toast.makeText(
                context,
                "Permissions will be revoked when the app process is killed",
                Toast.LENGTH_LONG
            ).show()
        },
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("Reset demo (revokeSelfPermissionsOnKill)")
    }
}
