package com.montanhajr.pointgame.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.*
import com.montanhajr.pointgame.BuildConfig
import com.montanhajr.pointgame.R
import com.montanhajr.pointgame.logic.BillingManager
import kotlinx.coroutines.delay

@Composable
fun AdBanner(onPremiumClick: (() -> Unit)? = null) {
    val context = LocalContext.current
    val billingManager = remember { BillingManager(context) }
    val isPremium by billingManager.isPremium.collectAsState()
    
    if (isPremium) {
        return
    }

    var adFailedCompletely by remember { mutableStateOf(false) }
    var retryCount by remember { mutableIntStateOf(0) }
    val maxRetries = 2 // Tenta a primeira + 2 re-tentativas
    
    // Trigger para forçar o recarregamento do AdView
    var loadTrigger by remember { mutableIntStateOf(0) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .height(60.dp),
        contentAlignment = Alignment.Center
    ) {
        if (adFailedCompletely) {
            // Banner de fallback para Premium
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f))
                    .border(1.dp, Color(0xFFFFD700).copy(alpha = 0.3f))
                    .clickable { onPremiumClick?.invoke() }
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .background(Color(0xFFFFD700), RoundedCornerShape(2.dp))
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "PREMIUM",
                        color = Color(0xFF1A1A2E),
                        fontSize = 8.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = stringResource(R.string.premium_title),
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = stringResource(R.string.premium_desc),
                        color = Color.LightGray,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = Color(0xFFFFD700),
                    modifier = Modifier.height(32.dp)
                ) {
                    Box(
                        modifier = Modifier.padding(horizontal = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "GET",
                            color = Color(0xFF1A1A2E),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        } else {
            // Tentativa de carregar AdMob
            key(loadTrigger) {
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { ctx ->
                        AdView(ctx).apply {
                            setAdSize(AdSize.BANNER)
                            adUnitId = BuildConfig.AD_UNIT_ID
                            adListener = object : AdListener() {
                                override fun onAdFailedToLoad(error: LoadAdError) {
                                    if (retryCount < maxRetries) {
                                        // Agenda uma nova tentativa em 20 segundos
                                        postDelayed({
                                            retryCount++
                                            loadTrigger++
                                        }, 20000)
                                    } else {
                                        // Falhou todas as vezes
                                        adFailedCompletely = true
                                    }
                                }
                                
                                override fun onAdLoaded() {
                                    // Reset caso carregue com sucesso
                                    retryCount = 0
                                }
                            }
                            loadAd(AdRequest.Builder().build())
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun Surface(
    modifier: Modifier = Modifier,
    shape: androidx.compose.ui.graphics.Shape,
    color: Color,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .background(color, shape)
            .padding(0.dp)
    ) {
        content()
    }
}
