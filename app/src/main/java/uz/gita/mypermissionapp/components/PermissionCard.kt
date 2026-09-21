package uz.gita.mypermissionapp.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import uz.gita.mypermissionapp.permission.PermissionRequestState
import uz.gita.mypermissionapp.permission.PermissionStatus

/**
 * One "real app" demo: the scenario, its permission, the current status and a button
 * that follows the recommended flow: check → rationale → request → Settings.
 */
@Composable
fun PermissionCard(
    icon: ImageVector,
    title: String,
    appExample: String,
    rationale: String,
    actionText: String,
    state: PermissionRequestState,
    onGranted: () -> Unit,
    modifier: Modifier = Modifier,
    note: String? = null,
    content: @Composable ColumnScope.() -> Unit = {}
) {
    var showRationale by remember { mutableStateOf(false) }

    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(28.dp))
                Spacer(Modifier.size(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(title, style = MaterialTheme.typography.titleMedium)
                    Text(
                        appExample,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                StatusChip(state.status)
            }

            Text(
                text = state.permissions.ifEmpty { listOf("(not required on this Android version)") }
                    .joinToString("\n") { it.substringAfterLast('.') },
                style = MaterialTheme.typography.labelMedium,
                fontFamily = FontFamily.Monospace
            )

            if (note != null) {
                Text(note, style = MaterialTheme.typography.bodySmall)
            }

            content()

            when (state.status) {
                PermissionStatus.PermanentlyDenied -> {
                    Text(
                        "The system won't show the dialog again. Enable it in Settings → Permissions.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                    OutlinedButton(onClick = state::openSettings) { Text("Open settings") }
                }

                else -> Button(
                    onClick = {
                        when (state.status) {
                            PermissionStatus.Granted -> onGranted()
                            PermissionStatus.Denied -> showRationale = true
                            else -> state.launch()
                        }
                    }
                ) { Text(actionText) }
            }
        }
    }

    if (showRationale) {
        RationaleDialog(
            icon = icon,
            title = "Permission needed",
            text = rationale,
            onConfirm = {
                showRationale = false
                state.launch()
            },
            onDismiss = { showRationale = false }
        )
    }
}

@Composable
fun StatusChip(status: PermissionStatus) {
    val color = when (status) {
        PermissionStatus.Granted -> Color(0xFF2E7D32)
        PermissionStatus.Denied -> Color(0xFFEF6C00)
        PermissionStatus.PermanentlyDenied -> MaterialTheme.colorScheme.error
        PermissionStatus.NotRequested -> MaterialTheme.colorScheme.outline
    }
    StatusChip(text = status.label, color = color)
}

@Composable
fun StatusChip(text: String, color: Color) {
    Surface(
        color = color.copy(alpha = 0.12f),
        contentColor = color,
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold
        )
    }
}
