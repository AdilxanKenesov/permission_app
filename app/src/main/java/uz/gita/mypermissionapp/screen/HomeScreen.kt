package uz.gita.mypermissionapp.screen

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

class HomeScreen {
    @Composable
    fun NewFun(){
        Scaffold(modifier = Modifier.fillMaxSize()) {paddingValues ->
            Text(text = "Hello", modifier = Modifier.padding(paddingValues))
        }
    }
// Rustam


}