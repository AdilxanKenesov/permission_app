package uz.gita.mypermissionapp.screen

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import uz.gita.mypermissionapp.components.RationaleDialog
import uz.gita.mypermissionapp.components.StatusChip
import uz.gita.mypermissionapp.overlay.OverlayBubble

/**
 * Special permission demo: SYSTEM_ALERT_WINDOW ("Display over other apps").
 * There is no dialog for it. The app sends the user to a Settings page instead,
 * and checks the result with its own API when the user comes back.
 */
@Composable
fun SpecialScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var canDrawOverlays by remember { mutableStateOf(Settings.canDrawOverlays(context)) }
    var bubbleShowing by remember { mutableStateOf(OverlayBubble.isShowing) }
    var showRationale by remember { mutableStateOf(false) }

    // Special permissions have no result callback: re-check every time the screen resumes
    LifecycleResumeEffect(Unit) {
        canDrawOverlays = Settings.canDrawOverlays(context)
        bubbleShowing = OverlayBubble.isShowing
        onPauseOrDispose { }
    }

    val openOverlaySettings = {
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:${context.packageName}")
        )
        context.startActivity(intent)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            "Special permissions guard powerful system features. They are never shown in a " +
                "dialog: the user turns them on in Settings → Special app access.",
            style = MaterialTheme.typography.bodyMedium
        )

        Card(Modifier.fillMaxWidth()) {
            Column(
                Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Layers, contentDescription = null, modifier = Modifier.size(28.dp))
                    Spacer(Modifier.size(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Chat bubbles", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "Like Messenger chat heads or a screen recorder",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    StatusChip(
                        text = if (canDrawOverlays) "Allowed" else "Not allowed",
                        color = if (canDrawOverlays) Color(0xFF2E7D32) else MaterialTheme.colorScheme.outline
                    )
                }

                Text(
                    "SYSTEM_ALERT_WINDOW\ncheck: Settings.canDrawOverlays()\n" +
                        "request: ACTION_MANAGE_OVERLAY_PERMISSION",
                    style = MaterialTheme.typography.labelMedium,
                    fontFamily = FontFamily.Monospace
                )

                if (!canDrawOverlays) {
                    Button(onClick = { showRationale = true }) { Text("Allow display over other apps") }
                } else if (!bubbleShowing) {
                    Button(onClick = {
                        bubbleShowing = OverlayBubble.show(context)
                        Toast.makeText(context, "Press Home: the bubble stays on top", Toast.LENGTH_LONG).show()
                    }) { Text("Show floating bubble") }
                } else {
                    OutlinedButton(onClick = {
                        OverlayBubble.hide(context)
                        bubbleShowing = false
                    }) { Text("Hide bubble") }
                }

                if (canDrawOverlays) {
                    Text(
                        "To test the request again, turn it off in Settings.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    OutlinedButton(onClick = openOverlaySettings) { Text("Open overlay settings") }
                }
            }
        }
    }

    if (showRationale) {
        RationaleDialog(
            icon = Icons.Default.Layers,
            title = "Display over other apps",
            text = "To show chat bubbles on top of other apps, turn on " +
                "\"Allow display over other apps\" for My Permission App on the next screen, " +
                "then come back.",
            confirmText = "Open settings",
            onConfirm = {
                showRationale = false
                openOverlaySettings()
            },
            onDismiss = { showRationale = false }
        )
    }
}
