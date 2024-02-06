package com.arcxp.video.util

import android.content.Context
import android.util.Log
import android.view.MotionEvent
import androidx.annotation.VisibleForTesting
import androidx.media3.common.util.UnstableApi
import com.arcxp.commons.util.Constants.SDK_TAG
import com.arcxp.video.ArcXPVideoConfig
import com.arcxp.video.listeners.VideoListener
import com.arcxp.video.model.ArcAd
import com.arcxp.video.model.TrackingType
import com.arcxp.video.views.VideoFrameLayout
import com.google.ads.interactivemedia.pal.NonceLoader
import com.google.ads.interactivemedia.pal.NonceManager

/**
 * @suppress
 */
@UnstableApi
class PalHelper(
    val context: Context,
    val config: ArcXPVideoConfig,
    val layout: VideoFrameLayout?,
    val utils: Utils,
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
        nonceLoader?.let {
            val request = utils.createNonceRequest(config, descriptionUrl, layout)
            it.loadNonceManager(request)
                .addOnSuccessListener { manager: NonceManager -> run {
                    nonceManager = manager
                    nonceData.data = manager.nonce
                    mListener.onTrackingEvent(TrackingType.PAL_NONCE, nonceData)
                } }
                .addOnFailureListener { error: Exception? -> }
        }
    }

    fun onTouch(event: MotionEvent, currentAd: ArcAd?) {
        nonceManager?.let {
            if (config.isLoggingEnabled) Log.e(SDK_TAG,"Pal Event sendTouch")
            it.sendAdTouch(event)
            if (currentAd != null) {
                if (config.isLoggingEnabled) Log.e(SDK_TAG,"Pal Event sendAdClick")
                it.sendAdClick()
            }
        }
    }

    fun sendAdImpression() {
        nonceManager?.let {
            if (config.isLoggingEnabled) Log.e(SDK_TAG,"Pal Event sendAdImpression")
            it.sendAdImpression()
        }
    }

    fun sendPlaybackStart() {
        nonceManager?.let {
            if (config.isLoggingEnabled) Log.e(SDK_TAG, "Pal Event sendPlaybackStart")
            it.sendPlaybackStart()

            mListener.onTrackingEvent(TrackingType.PAL_VIDEO_START, nonceData)
        }
    }

    fun sendPlaybackEnd() {
        nonceManager?.let {
            if (config.isLoggingEnabled) Log.e(SDK_TAG, "Pal Event sendPlaybackEnd")
            it.sendPlaybackEnd()

            mListener.onTrackingEvent(TrackingType.PAL_VIDEO_END, nonceData)
        }
    }

    @VisibleForTesting
    fun getNonceManager() = nonceManager

    @VisibleForTesting
    fun getNonceLoader() = nonceLoader
}