package com.montanhajr.pointgame.ui.components

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.util.Log
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.montanhajr.pointgame.BuildConfig
import com.montanhajr.pointgame.logic.BillingManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

object AdManager {
    private var interstitialAd: InterstitialAd? = null
    private var isLoading = false
    private const val TAG = "AdManager"

    fun loadInterstitial(context: Context) {
        // Se já for premium, não carrega anúncio
        val billingManager = BillingManager(context)
        CoroutineScope(Dispatchers.Main).launch {
            if (billingManager.isPremium.value) return@launch
            
            if (interstitialAd != null || isLoading) return@launch

            isLoading = true
            val adRequest = AdRequest.Builder().build()
            
            InterstitialAd.load(
                context,
                BuildConfig.INTERSTITIAL_AD_UNIT_ID,
                adRequest,
                object : InterstitialAdLoadCallback() {
                    override fun onAdFailedToLoad(adError: LoadAdError) {
                        interstitialAd = null
                        isLoading = false
                    }

                    override fun onAdLoaded(ad: InterstitialAd) {
                        interstitialAd = ad
                        isLoading = false
                    }
                }
            )
        }
    }

    fun showInterstitial(activity: Activity, onAdDismissed: () -> Unit) {
        val billingManager = BillingManager(activity)
        
        // Verifica se é premium antes de mostrar
        if (billingManager.isPremium.value) {
            onAdDismissed()
            return
        }

        if (interstitialAd != null) {
            interstitialAd?.fullScreenContentCallback = object : com.google.android.gms.ads.FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    interstitialAd = null
                    loadInterstitial(activity)
                    onAdDismissed()
                }

                override fun onAdFailedToShowFullScreenContent(adError: com.google.android.gms.ads.AdError) {
                    interstitialAd = null
                    onAdDismissed()
                }
            }
            interstitialAd?.show(activity)
        } else {
            loadInterstitial(activity)
            onAdDismissed()
        }
    }
}
