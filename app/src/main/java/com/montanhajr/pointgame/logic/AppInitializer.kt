package com.montanhajr.pointgame.logic

import android.content.Context
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.games.PlayGamesSdk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object AppInitializer {
    private var isInitialized = false

    suspend fun initialize(context: Context, onProgress: (Float) -> Unit) {
        if (isInitialized) return

        // Usamos Dispatchers.IO para tudo o que for possível
        withContext(Dispatchers.IO) {
            // 1. Inicializa Play Games (O SDK v2 é leve, mas roda em BG)
            onProgress(0.2f)
            try {
                // PlayGamesSdk.initialize deve ser chamado na Main Thread, 
                // mas vamos envolver apenas o estritamente necessário.
                withContext(Dispatchers.Main) {
                    PlayGamesSdk.initialize(context)
                }
            } catch (e: Exception) { e.printStackTrace() }

            // 2. Inicializa Mobile Ads (Lento, por isso rodamos em IO)
            onProgress(0.6f)
            try {
                MobileAds.initialize(context) {}
            } catch (e: Exception) { e.printStackTrace() }

            // 3. Finalizando
            onProgress(1.0f)
            isInitialized = true
        }
    }
}
