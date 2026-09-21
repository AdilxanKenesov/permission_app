package uz.gita.mypermissionapp.screen

import android.Manifest
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import uz.gita.mypermissionapp.NotesActivity
import uz.gita.mypermissionapp.components.StatusChip
import uz.gita.mypermissionapp.permission.isGranted

const val READ_NOTES_PERMISSION = "uz.gita.mypermissionapp.permission.READ_NOTES"

private val runtimePermissions = buildList {
    add(Manifest.permission.CAMERA)
    add(Manifest.permission.ACCESS_FINE_LOCATION)
    add(Manifest.permission.ACCESS_COARSE_LOCATION)
    add(Manifest.permission.RECORD_AUDIO)
    add(Manifest.permission.READ_CONTACTS)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) add(Manifest.permission.POST_NOTIFICATIONS)
}

/** A live overview of the four permission types, with the real status of each one. */
@Composable
fun TypesScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    // Bumped on every resume so the checks below re-run after visiting Settings
    var refreshKey by mutableIntStateOf(0)
    LifecycleResumeEffect(Unit) {
        refreshKey++
        onPauseOrDispose { }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            refreshKey // read, so this list recomposes on resume
            TypeCard(
                title = "1. Install-time: normal",
                protectionLevel = "normal",
                description = "Low-risk access. Granted automatically when the app is installed, with no dialog.",
                rows = listOf(Manifest.permission.INTERNET to context.isGranted(Manifest.permission.INTERNET))
            )
        }
        item {
            refreshKey
            TypeCard(
                title = "2. Install-time: signature (custom)",
                protectionLevel = "signature",
                description = "Granted only to apps signed with the same key as the app that declares it. " +
                    "This app declares its own READ_NOTES permission and protects NotesActivity with it.",
                rows = listOf(READ_NOTES_PERMISSION to context.isGranted(READ_NOTES_PERMISSION))
            ) {
                OutlinedButton(onClick = {
                    context.startActivity(Intent(context, NotesActivity::class.java))
                }) { Text("Open protected NotesActivity") }
            }
        }
        item {
            refreshKey
            TypeCard(
                title = "3. Runtime (dangerous)",
                protectionLevel = "dangerous",
                description = "Private data or hardware. The user approves each one in a system dialog " +
                    "and can revoke it at any time. See the Runtime tab.",
                rows = runtimePermissions.map { it to context.isGranted(it) }
            )
        }
        item {
            refreshKey
            TypeCard(
                title = "4. Special (appop)",
                protectionLevel = "appop",
                description = "Powerful system features. Turned on in Settings → Special app access, " +
                    "each with its own check API. See the Special tab.",
                rows = listOf(Manifest.permission.SYSTEM_ALERT_WINDOW to Settings.canDrawOverlays(context))
            )
        }
    }
}

@Composable
private fun TypeCard(
    title: String,
    protectionLevel: String,
    description: String,
    rows: List<Pair<String, Boolean>>,
    extra: @Composable () -> Unit = {}
) {
    Card(Modifier.fillMaxWidth()) {
        Column(
            Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(
                "protectionLevel=\"$protectionLevel\"",
                style = MaterialTheme.typography.labelMedium,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.primary
            )
            Text(description, style = MaterialTheme.typography.bodySmall)
            rows.forEach { (permission, granted) ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        permission.substringAfterLast('.'),
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.labelMedium,
                        fontFamily = FontFamily.Monospace
                    )
                    StatusChip(
                        text = if (granted) "Granted" else "Not granted",
                        color = if (granted) Color(0xFF2E7D32) else MaterialTheme.colorScheme.outline
                    )
                }
            }
            extra()
        }
    }
}
