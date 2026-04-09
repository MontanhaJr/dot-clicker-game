package com.montanhajr.pointgame.ui.components

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.montanhajr.pointgame.BuildConfig
import com.montanhajr.pointgame.logic.BillingManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object AdManager {
    private var interstitialAd: InterstitialAd? = null
    private var isLoading = false
    private const val TAG = "AdManager"

    fun loadInterstitial(context: Context) {
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
                        Log.e(TAG, "Ad failed to load: ${adError.message}")
                        interstitialAd = null
                        isLoading = false
                    }

                    override fun onAdLoaded(ad: InterstitialAd) {
                        Log.d(TAG, "Ad loaded successfully")
                        interstitialAd = ad
                        isLoading = false
                    }
                }
            )
        }
    }

    fun showInterstitial(
        activity: Activity, 
        onAdDismissed: () -> Unit,
        onShowFallback: () -> Unit
    ) {
        val billingManager = BillingManager(activity)
        
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
                    Log.e(TAG, "Ad failed to show: ${adError.message}")
                    interstitialAd = null
                    onShowFallback()
                }
            }
            interstitialAd?.show(activity)
        } else {
            Log.d(TAG, "No ad available, triggering fallback")
            loadInterstitial(activity)
            onShowFallback()
        }
    }
}
