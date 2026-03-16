package com.montanhajr.pointgame.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.montanhajr.pointgame.BuildConfig

@Composable
fun AdBanner() {
    AndroidView(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        factory = { context ->
            AdView(context).apply {
                setAdSize(AdSize.BANNER)
                adUnitId = BuildConfig.AD_UNIT_ID
                loadAd(AdRequest.Builder().build())
            }
        }
    )
}
