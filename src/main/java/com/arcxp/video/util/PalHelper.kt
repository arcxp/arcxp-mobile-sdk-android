package com.arcxp.video.util

import android.content.Context
import android.util.Log
import android.view.MotionEvent
import androidx.annotation.VisibleForTesting
import com.arcxp.video.ArcException
import com.arcxp.video.ArcMediaPlayerConfig
import com.arcxp.video.listeners.VideoListener
import com.arcxp.video.model.ArcAd
import com.arcxp.video.model.ArcVideoSDKErrorType
import com.arcxp.video.model.TrackingType
import com.arcxp.video.model.TrackingTypeData
import com.arcxp.video.views.VideoFrameLayout
import com.google.ads.interactivemedia.pal.NonceLoader
import com.google.ads.interactivemedia.pal.NonceManager
import com.google.ads.interactivemedia.pal.NonceRequest

/**
 * @suppress
 */
class PalHelper(
    val context: Context,
    val config: ArcMediaPlayerConfig,
    val layout: VideoFrameLayout?,
    val utils: Utils = Utils(),
    val mListener: VideoListener) {

    private var nonceManager: NonceManager? = null
    private var nonceLoader: NonceLoader? = null
    private val nonceData = utils.createNonceData()

    public fun clear() {
        nonceManager = null
    }

    fun initVideo(descriptionUrl: String) {
        clear()
        nonceLoader = utils.createNonceLoader(context)
        if (nonceLoader != null) {
            val request = utils.createNonceRequest(config, descriptionUrl, layout)
            nonceLoader!!
                    .loadNonceManager(request)
                    .addOnSuccessListener { manager: NonceManager -> run {
                        nonceManager = manager
                        nonceData.data = manager.nonce
                        mListener.onTrackingEvent(TrackingType.PAL_NONCE, nonceData)
                    } }
                    .addOnFailureListener { error: Exception? -> }
        }
    }

    fun onTouch(event: MotionEvent, currentAd: ArcAd?) {
        if (nonceManager != null) {
            if (config.isLoggingEnabled) Log.e("ArcVideoSDK","Pal Event sendTouch")
            nonceManager?.sendAdTouch(event)
            if (currentAd != null) {
                if (config.isLoggingEnabled) Log.e("ArcVideoSDK","Pal Event sendAdClick")
                nonceManager?.sendAdClick()
            }
        }
    }

    fun sendAdImpression() {
        if (config.isLoggingEnabled) Log.e("ArcVideoSDK","Pal Event sendAdImpression")
        nonceManager?.sendAdImpression()
    }

    fun sendPlaybackStart() {
        if (config.isLoggingEnabled) Log.e("ArcVideoSDK","Pal Event sendPlaybackStart")
        nonceManager?.sendPlaybackStart()

        mListener.onTrackingEvent(TrackingType.PAL_VIDEO_START, nonceData)
    }

    fun sendPlaybackEnd() {
        if (config.isLoggingEnabled) Log.e("ArcVideoSDK","Pal Event sendPlaybackEnd")
        nonceManager?.sendPlaybackEnd()

        mListener.onTrackingEvent(TrackingType.PAL_VIDEO_END, nonceData)
    }

    @VisibleForTesting
    fun getNonceManager() = nonceManager

    @VisibleForTesting
    fun getNonceLoader() = nonceLoader
}