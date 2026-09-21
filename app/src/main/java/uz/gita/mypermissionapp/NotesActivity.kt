package uz.gita.mypermissionapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import uz.gita.mypermissionapp.ui.theme.MyPermissionAppTheme

/**
 * An exported screen protected by the custom READ_NOTES permission (see AndroidManifest.xml).
 * Other apps can start it only if they hold that permission. Because it is "signature",
 * only apps signed with our key can get it.
 */
class NotesActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyPermissionAppTheme {
                Surface(Modifier.fillMaxSize()) {
                    Column(
                        modifier = Modifier.safeDrawingPadding().padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text("🔒 Protected notes", style = MaterialTheme.typography.headlineSmall)
                        Text(
                            "You opened this screen because this app holds " +
                                "uz.gita.mypermissionapp.permission.READ_NOTES.\n\n" +
                                "Another app starting it without that permission gets a SecurityException.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Button(onClick = ::finish) { Text("Close") }
                    }
                }
            }
        }
    }
}
