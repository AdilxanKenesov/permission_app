package uz.gita.mypermissionapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import uz.gita.mypermissionapp.screen.MainScreen
import uz.gita.mypermissionapp.ui.theme.MyPermissionAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyPermissionAppTheme {
                MainScreen()
            }
        }
    }
}
