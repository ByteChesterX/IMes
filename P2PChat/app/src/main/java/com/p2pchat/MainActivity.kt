package com.p2pchat

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.p2pchat.ui.screens.MainScreen
import com.p2pchat.ui.theme.P2PChatTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        val application = applicationContext as P2PChatApplication
        
        setContent {
            P2PChatTheme {
                MainScreen(
                    networkManager = application.networkManager,
                    localDataSource = application.localDataSource
                )
            }
        }
    }
}
