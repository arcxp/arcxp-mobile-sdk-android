package com.arcxp.video.util

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import com.arcxp.commons.util.Constants.SDK_TAG
import com.arcxp.commons.util.DependencyFactory.createBuildVersionProvider
import com.arcxp.sdk.R
import com.arcxp.video.ArcXPVideoConfig
import com.arcxp.video.listeners.VideoPlayer
import com.arcxp.video.model.AdVerification
import com.arcxp.video.model.JavascriptResource
import com.arcxp.video.views.VideoFrameLayout
import com.iab.omid.library.washpost.adsession.AdEvents
import com.iab.omid.library.washpost.adsession.AdSession
import com.iab.omid.library.washpost.adsession.FriendlyObstructionPurpose
import com.iab.omid.library.washpost.adsession.media.*

/**
 * @suppress
 */
public class OmidHelper(
    val context: Context,
    val config: ArcXPVideoConfig,
    val layout: VideoFrameLayout?,
    val videoPlayer: VideoPlayer?
) {

    private var adSession: AdSession? = null
    private var adEvents: AdEvents? = null
    private var mediaEvents: MediaEvents? = null
    private val buildVersionProvider = createBuildVersionProvider()

    public fun init(verifications: List<AdVerification>) {
        clear()
        if (config.isEnableOmid) {
            Handler(Looper.getMainLooper()).post {
                try {
                    adSession = OmidAdSessionUtil.getNativeAdSession(
                        context, config,
                        verifications
                    )
                    adSession?.let { session ->
                        session.registerAdView(layout)
                        if (buildVersionProvider.sdkInt() >= 24) {
                            config.overlays.iterator().forEach { (key, value) ->
                                session.addFriendlyObstruction(
                                    value,
                                    FriendlyObstructionPurpose.OTHER,
                                    key
                                )
                            }
                        }
                        val controller =
                            videoPlayer?.playControls?.findViewById<View>(R.id.exo_controller)
                        if (controller != null) {
                            session.addFriendlyObstruction(
                                controller,
                                FriendlyObstructionPurpose.VIDEO_CONTROLS,
                                "controls"
                            )
                        }

                        mediaEvents = MediaEvents.createMediaEvents(adSession)
                        session.start()
                        adEvents = AdEvents.createAdEvents(adSession)
                        val properties = VastProperties.createVastPropertiesForNonSkippableMedia(
                            false,
                            Position.STANDALONE
                        )
                        adEvents?.loaded(properties)
                        if (config.isLoggingEnabled) {
                            Log.d(SDK_TAG, "OM Ad session started")
                        }
                    }
                } catch (e: Exception) {
                    if (config.isLoggingEnabled) {
                        Log.e(SDK_TAG, "OM Exception: ${e.message}")
                    }
                }
            }
        }
    }

    public fun clear() {
        adSession?.let {
            Handler(Looper.getMainLooper()).post {
                try {
                    it.finish()
                    adSession = null
                    if (config.isLoggingEnabled) {
                        Log.d(SDK_TAG, "OM Ad session stopped")
                    }
                } catch (e: Exception) {
                    if (config.isLoggingEnabled) {
                        Log.e(SDK_TAG, "OM Exception: ${e.message}")
                    }
                }
            }
        }
    }

    fun mediaEventsStart(length: Float, volume: Float) {
        mediaEvents?.let {
            Handler(Looper.getMainLooper()).post {
                try {
                    it.start(length, volume)
                    if (config.isLoggingEnabled) {
                        Log.d(SDK_TAG, "OM mediaEvents.start() called")
                    }
                } catch (e: Exception) {
                    if (config.isLoggingEnabled) {
                        Log.e(SDK_TAG, "OM Exception: ${e.message}")
                    }
                }
            }
        }
    }

    fun mediaEventsFirstQuartile() {
        mediaEvents?.let {
            Handler(Looper.getMainLooper()).post {
                try {
                    it.firstQuartile()
                    if (config.isLoggingEnabled) {
                        Log.d(SDK_TAG, "OM mediaEvents.firstQuartile() called")
                    }
                } catch (e: Exception) {
                    if (config.isLoggingEnabled) {
                        Log.e(SDK_TAG, "OM Exception: ${e.message}")
                    }
                }
            }
        }
    }

    fun mediaEventsMidpoint() {
        mediaEvents?.let {
            Handler(Looper.getMainLooper()).post {
                try {
                    it.midpoint()
                    if (config.isLoggingEnabled) {
                        Log.d(SDK_TAG, "OM mediaEvents.midpoint() called")
                    }
                } catch (e: Exception) {
                    if (config.isLoggingEnabled) {
                        Log.e(SDK_TAG, "OM Exception: ${e.message}")
                    }
                }
            }
        }
    }

    fun mediaEventsThirdQuartile() {
        mediaEvents?.let {
            Handler(Looper.getMainLooper()).post {
                try {
                    it.thirdQuartile()
                    if (config.isLoggingEnabled) {
                        Log.d(SDK_TAG, "OM mediaEvents.thirdQuartile() called")
                    }
                } catch (e: Exception) {
                    if (config.isLoggingEnabled) {
                        Log.e(SDK_TAG, "OM Exception: ${e.message}")
                    }
                }
            }
        }
    }

    fun mediaEventsComplete() {
        mediaEvents?.let {
            Handler(Looper.getMainLooper()).post {
                try {
                    it.complete()
                    if (config.isLoggingEnabled) {
                        Log.d(SDK_TAG, "OM mediaEvents.complete() called")
                    }
                } catch (e: Exception) {
                    if (config.isLoggingEnabled) {
                        Log.e(SDK_TAG, "OM Exception: ${e.message}")
                    }
                }
            }
        }
    }

    fun adEventsImpressionOccurred() {
        adEvents?.let {
            Handler(Looper.getMainLooper()).post {
                try {
                    it.impressionOccurred()
                    if (config.isLoggingEnabled) {
                        Log.d(SDK_TAG, "OM adEvents.impressionOccurred() called")
                    }
                } catch (e: Exception) {
                    if (config.isLoggingEnabled) {
                        Log.e(SDK_TAG, "OM Exception: ${e.message}")
                    }
                }
            }
        }
    }

    fun mediaEventsPause() {
        mediaEvents?.let {
            Handler(Looper.getMainLooper()).post {
                try {
                    it.pause()
                    if (config.isLoggingEnabled) {
                        Log.d(SDK_TAG, "OM mediaEvents.pause() called")
                    }
                } catch (e: Exception) {
                    if (config.isLoggingEnabled) {
                        Log.e(SDK_TAG, "OM Exception: ${e.message}")
                    }
                }
            }
        }
    }

    fun mediaEventsResume() {
        mediaEvents?.let {
            Handler(Looper.getMainLooper()).post {
                try {
                    it.resume()
                    if (config.isLoggingEnabled) {
                        Log.d(SDK_TAG, "OM mediaEvents.resume() called")
                    }
                } catch (e: Exception) {
                    if (config.isLoggingEnabled) {
                        Log.e(SDK_TAG, "Exception: ${e.message}")
                    }
                }
            }
        }
    }

    fun mediaEventsFullscreen() {
        mediaEvents?.let {
            Handler(Looper.getMainLooper()).post {
                try {
                    it.playerStateChange(PlayerState.FULLSCREEN)
                    if (config.isLoggingEnabled) {
                        Log.d(SDK_TAG, "OM mediaEvents.fullscreen() called")
                    }
                } catch (e: Exception) {
                    if (config.isLoggingEnabled) {
                        Log.e(SDK_TAG, "Exception: ${e.message}")
                    }
                }
            }
        }
    }

    fun mediaEventsNormalScreen() {
        mediaEvents?.let {
            Handler(Looper.getMainLooper()).post {
                try {
                    it.playerStateChange(PlayerState.NORMAL)
                    if (config.isLoggingEnabled) {
                        Log.d(SDK_TAG, "OM mediaEvents.normalScreen() called")
                    }
                } catch (e: Exception) {
                    if (config.isLoggingEnabled) {
                        Log.e(SDK_TAG, "Exception: ${e.message}")
                    }
                }
            }
        }
    }

    fun mediaEventsOnTouch() {
        mediaEvents?.let {
            Handler(Looper.getMainLooper()).post {
                try {
                    it.adUserInteraction(InteractionType.CLICK)
                    if (config.isLoggingEnabled) {
                        Log.d(SDK_TAG, "OM mediaEvents.onTouch() called")
                    }
                } catch (e: Exception) {
                    if (config.isLoggingEnabled) {
                        Log.e(SDK_TAG, "Exception: ${e.message}")
                    }
                }
            }
        }
    }

    fun mediaEventsVolumeChange(volume: Float) {
        mediaEvents?.let {
            Handler(Looper.getMainLooper()).post {
                try {
                    it.volumeChange(volume)
                    if (config.isLoggingEnabled) {
                        Log.d(SDK_TAG, "OM mediaEvents.volumeChange() called")
                    }
                } catch (e: Exception) {
                    if (config.isLoggingEnabled) {
                        Log.e(SDK_TAG, "Exception: ${e.message}")
                    }
                }
            }
        }
    }

    public fun onDestroy() {
        clear()
    }
}