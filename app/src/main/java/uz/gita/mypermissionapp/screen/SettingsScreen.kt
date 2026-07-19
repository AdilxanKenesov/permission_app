package uz.gita.mypermissionapp.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
fun SettingsScreen() {
    Box(modifier = Modifier.fillMaxSize().background(Color.Yellow),
        contentAlignment = Alignment.Center
    ) {

        Text(text = "Setting Screen", modifier = Modifier.align(Alignment.Center))
    }
}