package com.montanhajr.pointgame

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.montanhajr.pointgame.ui.screens.GameModeScreen
import com.montanhajr.pointgame.ui.screens.SplashScreen
import com.montanhajr.pointgame.ui.theme.DotConnectTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // 1. Instala a splash nativa - O Android agora sabe que deve liberar a tela assim que o Compose desenhar
        installSplashScreen()
        
        super.onCreate(savedInstanceState)
        
        // 2. Configura a tela cheia imediatamente
        enableEdgeToEdge()

        setContent {
            DotConnectTheme {
                var showComposeSplash by remember { mutableStateOf(true) }

                if (showComposeSplash) {
                    SplashScreen(onLoadingComplete = { showComposeSplash = false })
                } else {
                    GameModeScreen()
                }
            }
        }
    }
}
