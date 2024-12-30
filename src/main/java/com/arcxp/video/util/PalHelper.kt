package com.arcxp.video.util

import android.content.Context
import android.util.Log
import android.view.MotionEvent
import androidx.annotation.VisibleForTesting
import com.arcxp.commons.util.Constants.SDK_TAG
import com.arcxp.video.ArcXPVideoConfig
import com.arcxp.video.listeners.VideoListener
import com.arcxp.video.model.ArcAd
import com.arcxp.video.model.TrackingType
import com.arcxp.video.views.VideoFrameLayout
import com.google.ads.interactivemedia.pal.NonceLoader
import com.google.ads.interactivemedia.pal.NonceManager

/**
 * PalHelper is a utility class for managing PAL (Publisher Ad Library) ad interactions within the ArcXP Video module.
 * It provides methods to initialize video ads, handle touch events, and track various ad-related events using the Google Ads Interactive Media PAL library.
 *
 * The class includes methods to start, end, and track ad impressions and playback events.
 * It ensures the PAL API is properly initialized and handles the creation and management of nonce data for ad tracking.
 *
 * Usage:
 * - Instantiate the PalHelper class with the required context, configuration, layout, utilities, and listener.
 * - Use the `initVideo` method to initialize the video ad with the specified description URL.
 * - Use the provided methods to handle touch events and track ad impressions and playback events.
 *
 * Example:
 *
 * val palHelper = PalHelper(context, config, layout, utils, listener)
 * palHelper.initVideo(descriptionUrl)
 * palHelper.sendAdImpression()
 *
 * Note: Ensure that the required parameters such as context, config, layout, utils, and listener are properly initialized before creating an instance of PalHelper.
 *
 * @property context The application context.
 * @property config Configuration settings for the video module.
 * @property layout The layout containing the video player.
 * @property utils Utility class for creating nonce data and loaders.
 * @property mListener The listener for handling video tracking events.
 * @property nonceManager The current nonce manager.
 * @property nonceLoader The current nonce loader.
 * @property nonceData The current nonce data.
 */
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