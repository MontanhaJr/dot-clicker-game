package com.montanhajr.pointgame.ui.components

import android.app.Activity
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.OnUserEarnedRewardListener
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.montanhajr.pointgame.BuildConfig
import com.montanhajr.pointgame.logic.BillingManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object AdManager {
    private var interstitialAd: InterstitialAd? = null
    private var rewardedAd: RewardedAd? = null
    private var isInterstitialLoading = false
    private var isRewardedLoading = false
    private const val TAG = "AdManager"

    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false
        return when {
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            else -> false
        }
    }

    fun isRewardedAdReady(): Boolean {
        return rewardedAd != null
    }

    fun loadInterstitial(context: Context) {
        val billingManager = BillingManager(context)
        CoroutineScope(Dispatchers.Main).launch {
            if (billingManager.isPremium.value) return@launch
            if (interstitialAd != null || isInterstitialLoading) return@launch

            isInterstitialLoading = true
            val adRequest = AdRequest.Builder().build()
            
            InterstitialAd.load(
                context,
                BuildConfig.INTERSTITIAL_AD_UNIT_ID,
                adRequest,
                object : InterstitialAdLoadCallback() {
                    override fun onAdFailedToLoad(adError: LoadAdError) {
                        interstitialAd = null
                        isInterstitialLoading = false
                    }
                    override fun onAdLoaded(ad: InterstitialAd) {
                        interstitialAd = ad
                        isInterstitialLoading = false
                    }
                }
            )
        }
    }

    fun showInterstitial(activity: Activity, onAdDismissed: () -> Unit, onShowFallback: () -> Unit) {
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
                    interstitialAd = null
                    onShowFallback()
                }
            }
            interstitialAd?.show(activity)
        } else {
            loadInterstitial(activity)
            onShowFallback()
        }
    }

    // Lógica para Ad Premiado (Rewarded)
    fun loadRewardedAd(context: Context) {
        if (rewardedAd != null || isRewardedLoading) return

        isRewardedLoading = true
        val adRequest = AdRequest.Builder().build()
        // Use um ID de teste para rewarded se ainda não tiver um real no local.properties
        val adUnitId = "ca-app-pub-3940256099942544/5224354917" 

        RewardedAd.load(context, adUnitId, adRequest, object : RewardedAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                rewardedAd = null
                isRewardedLoading = false
            }
            override fun onAdLoaded(ad: RewardedAd) {
                rewardedAd = ad
                isRewardedLoading = false
            }
        })
    }

    fun showRewardedAd(activity: Activity, onRewardEarned: () -> Unit, onAdFailed: () -> Unit) {
        if (rewardedAd != null) {
            rewardedAd?.fullScreenContentCallback = object : com.google.android.gms.ads.FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    rewardedAd = null
                    loadRewardedAd(activity)
                }
                override fun onAdFailedToShowFullScreenContent(adError: com.google.android.gms.ads.AdError) {
                    rewardedAd = null
                    onAdFailed()
                }
            }
            rewardedAd?.show(activity, OnUserEarnedRewardListener {
                onRewardEarned()
            })
        } else {
            loadRewardedAd(activity)
            onAdFailed()
        }
    }
}
