package com.arcxp.video.util

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.TextView
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
                    adSession?.registerAdView(layout)
                    if (buildVersionProvider.sdkInt() >= 24) {
                        config.overlays.iterator().forEach { (key, value) ->
                            adSession?.addFriendlyObstruction(
                                value,
                                FriendlyObstructionPurpose.OTHER,
                                key
                            )
                        }
                    }
                    val controller =
                        videoPlayer?.playControls?.findViewById<View>(R.id.exo_controller)
                    if (controller != null) {
                        adSession?.addFriendlyObstruction(
                            controller,
                            FriendlyObstructionPurpose.VIDEO_CONTROLS,
                            "controls"
                        )
                    }

                    mediaEvents = MediaEvents.createMediaEvents(adSession)
                    adSession?.start()
                    adEvents = AdEvents.createAdEvents(adSession)
                    val properties = VastProperties.createVastPropertiesForNonSkippableMedia(
                        false,
                        Position.STANDALONE
                    )
                    adEvents?.loaded(properties)
                    if (config.isLoggingEnabled) {
                        Log.d("ArcVideoSDK", "OM Ad session started")
                    }
                } catch (e: Exception) {
                    if (config.isLoggingEnabled) {
                        Log.e("ArcVideoSDK", "OM Exception: ${e.message}")
                    }
                }
            }
        }
    }

    public fun clear() {
        if (adSession != null) {
            Handler(Looper.getMainLooper()).post {
                try {
                    adSession?.finish()
                    adSession = null
                    if (config.isLoggingEnabled) {
                        Log.d("ArcVideoSDK", "OM Ad session stopped")
                    }
                } catch (e: Exception) {
                    if (config.isLoggingEnabled) {
                        Log.e("ArcVideoSDK", "OM Exception: ${e.message}")
                    }
                }
            }
        }
    }

    fun mediaEventsStart(length: Float, volume: Float) {
        Handler(Looper.getMainLooper()).post {
            try {
                mediaEvents?.start(length, volume)
                if (config.isLoggingEnabled) {
                    Log.d("ArcVideoSDK", "OM mediaEvents.start() called")
                }
            } catch (e: Exception) {
                if (config.isLoggingEnabled) {
                    Log.e("ArcVideoSDK", "OM Exception: ${e.message}")
                }
            }
        }
    }

    fun mediaEventsFirstQuartile() {
        Handler(Looper.getMainLooper()).post {
            try {
                mediaEvents?.firstQuartile()
                if (config.isLoggingEnabled) {
                    Log.d("ArcVideoSDK", "OM mediaEvents.firstQuartile() called")
                }
            } catch (e: Exception) {
                if (config.isLoggingEnabled) {
                    Log.e("ArcVideoSDK", "OM Exception: ${e.message}")
                }
            }
        }
    }

    fun mediaEventsMidpoint() {
        Handler(Looper.getMainLooper()).post {
            try {
                mediaEvents?.midpoint()
                if (config.isLoggingEnabled) {
                    Log.d("ArcVideoSDK", "OM mediaEvents.midpoint() called")
                }
            } catch (e: Exception) {
                if (config.isLoggingEnabled) {
                    Log.e("ArcVideoSDK", "OM Exception: ${e.message}")
                }
            }
        }
    }

    fun mediaEventsThirdQuartile() {
        Handler(Looper.getMainLooper()).post {
            try {
                mediaEvents?.thirdQuartile()
                if (config.isLoggingEnabled) {
                    Log.d("ArcVideoSDK", "OM mediaEvents.thirdQuartile() called")
                }
            } catch (e: Exception) {
                if (config.isLoggingEnabled) {
                    Log.e("ArcVideoSDK", "OM Exception: ${e.message}")
                }
            }
        }
    }

    fun mediaEventsComplete() {
        Handler(Looper.getMainLooper()).post {
            try {
                mediaEvents?.complete()
                if (config.isLoggingEnabled) {
                    Log.d("ArcVideoSDK", "OM mediaEvents.complete() called")
                }
            } catch (e: Exception) {
                if (config.isLoggingEnabled) {
                    Log.e("ArcVideoSDK", "OM Exception: ${e.message}")
                }
            }
        }
    }

    fun adEventsImpressionOccurred() {
        Handler(Looper.getMainLooper()).post {
            try {
                adEvents?.impressionOccurred()
                if (config.isLoggingEnabled) {
                    Log.d("ArcVideoSDK", "OM adEvents.impressionOccurred() called")
                }
            } catch (e: Exception) {
                if (config.isLoggingEnabled) {
                    Log.e("ArcVideoSDK", "OM Exception: ${e.message}")
                }
            }
        }
    }

    fun mediaEventsPause() {
        Handler(Looper.getMainLooper()).post {
            try {
                mediaEvents?.pause()
                if (config.isLoggingEnabled) {
                    Log.d("ArcVideoSDK", "OM mediaEvents.pause() called")
                }
            } catch (e: Exception) {
                if (config.isLoggingEnabled) {
                    Log.e("ArcVideoSDK", "OM Exception: ${e.message}")
                }
            }
        }
    }

    fun mediaEventsResume() {
        Handler(Looper.getMainLooper()).post {
            try {
                mediaEvents?.resume()
                if (config.isLoggingEnabled) {
                    Log.d("ArcVideoSDK", "OM mediaEvents.resume() called")
                }
            } catch (e: Exception) {
                if (config.isLoggingEnabled) {
                    Log.e("ArcVideoSDK", "Exception: ${e.message}")
                }
            }
        }
    }

    fun mediaEventsFullscreen() {
        Handler(Looper.getMainLooper()).post {
            try {
                mediaEvents?.playerStateChange(PlayerState.FULLSCREEN)
                if (config.isLoggingEnabled) {
                    Log.d("ArcVideoSDK", "OM mediaEvents.fullscreen() called")
                }
            } catch (e: Exception) {
                if (config.isLoggingEnabled) {
                    Log.e("ArcVideoSDK", "Exception: ${e.message}")
                }
            }
        }
    }

    fun mediaEventsNormalScreen() {
        Handler(Looper.getMainLooper()).post {
            try {
                mediaEvents?.playerStateChange(PlayerState.NORMAL)
                if (config.isLoggingEnabled) {
                    Log.d("ArcVideoSDK", "OM mediaEvents.normalScreen() called")
                }
            } catch (e: Exception) {
                if (config.isLoggingEnabled) {
                    Log.e("ArcVideoSDK", "Exception: ${e.message}")
                }
            }
        }
    }

    fun mediaEventsOnTouch() {
        Handler(Looper.getMainLooper()).post {
            try {
                mediaEvents?.adUserInteraction(InteractionType.CLICK)
                if (config.isLoggingEnabled) {
                    Log.d("ArcVideoSDK", "OM mediaEvents.onTouch() called")
                }
            } catch (e: Exception) {
                if (config.isLoggingEnabled) {
                    Log.e("ArcVideoSDK", "Exception: ${e.message}")
                }
            }
        }
    }

    fun mediaEventsVolumeChange(volume: Float) {
        Handler(Looper.getMainLooper()).post {
            try {
                mediaEvents?.volumeChange(volume)
                if (config.isLoggingEnabled) {
                    Log.d("ArcVideoSDK", "OM mediaEvents.volumeChange() called")
                }
            } catch (e: Exception) {
                if (config.isLoggingEnabled) {
                    Log.e("ArcVideoSDK", "Exception: ${e.message}")
                }
            }
        }
    }

    public fun onDestroy() {
        clear()
    }
}