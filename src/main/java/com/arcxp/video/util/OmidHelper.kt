package com.arcxp.video.util

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.TextView
import com.arcxp.sdk.R
import com.arcxp.video.ArcMediaPlayerConfig
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
    val config: ArcMediaPlayerConfig,
    val layout: VideoFrameLayout?,
    val videoPlayer: VideoPlayer?
) {

    private var adSession: AdSession? = null
    private var adEvents: AdEvents? = null
    private var mediaEvents: MediaEvents? = null
    private val buildVersionProvider = DependencyProvider.buildVersionUtil()

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

    public fun runOmidTest(activity: Activity, omOutput: TextView) {

        Handler(Looper.getMainLooper()).post {
            try {
                val omOutputString: StringBuilder = java.lang.StringBuilder()

                omOutputString.append("OM Test Start\n")
                omOutput?.text = omOutputString.toString()

                omOutputString.clear()
                adSession = OmidAdSessionUtil.getNativeAdSession(context, config,
                    listOf(
                        AdVerification(listOf(JavascriptResource(
                            "omid",
                            "https://pubads.g.doubleclick.net/gampad/ads?iu=%2F124319096%2Fexternal%2Fomid_google_samples&env=vp&gdfp_req=1&output=xml_vast4&sz=640x480&description_url=http:%2F%2Ftest_site.com%2Fhomepage&tfcd=0&npa=0&vpmute=0&vpa=0&vad_format=linear&url=http:%2F%2Ftest_site.com&vpos=preroll&unviewed_position_start=1&paln=AQzzBGQEBQFeLs7Fd70fmApmYWR_4HjMJmtdho1F4t4eMXJdwPsHJ8zo_HbBnu11jOPFfutlkUG22l6UbNlun47p8kvFoa4M_TXMYrrBfOts-0XD_Xw_w8_5mA0InEw6xUkL_ep0_sbP5BcN63PEHBCyqTcza-IZ7xycX57RAnRFAH_tcP8jJveNYrl4V3xApEAEXzujpKPdbsbyBThsm7cK183FmtE7HqEU77iphe8BcIXYPowSLCj7Pm9uJtAsQwuwZxaWUq0n-i2ou8Njid-luGFmsJPQxS4-A4cDuN12DY0k7l0ZcGekeArcqnWEsm3ILFkJjegdsuBAF0hRyM1c7QqfV4Woex40KpYiT_XGv6J8Exk5280GDX91Z0DxtXVGvnLA0W14e5jzbWiI0oq_YXqaiNccXIVVuRw-9tx0HHu1DORHz4nTovAtGNqqHwTS6fLwv0QaquvHKcsbOLFoUIHCJ7qjBBniP6bicDz_7VJfHKJ3TUn6QV2FEMr7iKZmryZBqrmX3SMoPG0H4kw-JGcTvRq8CHvDLthZQck-wlat-6Xw3QZyk-yUcCV8pfKaS1ONsBn_MVyC-HJ0W7ZQ94MLe2t5xhzjuRt412LMCc77qKC1o0lRhISRmmq3nxoK4qKC-kFa02kyjErTvz7rYOyLhycwEVrNbfac2QYIyCaCgOvtku1KdXDJzRwcSR5fA5_6KxmIjak1fxG7WsAqIDg5Wow7-tQKMz8o-ALpYBeVA6hgZHbWJytmKKmmQ_WZgfo3Lwt2nHelICUbFYGB-xvdzoMtUyw4irFhYeZ9IkJfiWCvBLUwoFtRwI4Fh1ep3oXJSKpHgMThzIRGwSdG07nSOHW-GMbDwqGsLQClNIATbLbQ1KbnpUigcPDyx4Ix4AOPa_zrGg..&correlator=",
                        )), "", "")))
                adSession?.registerAdView(layout)
                mediaEvents = MediaEvents.createMediaEvents(adSession)
                adSession?.start()
                adEvents = AdEvents.createAdEvents(adSession)
                val properties = VastProperties.createVastPropertiesForNonSkippableMedia(false, Position.STANDALONE)
                adEvents?.loaded(properties)

                omOutputString.append("OM Initialized\n")
                omOutput?.text = omOutputString.toString()

                //Thread.sleep(1000)
                mediaEvents?.start(1000.0f, 1.0f)
                omOutputString.append("Media Event: Start()\n")
                omOutput?.text = omOutputString.toString()

                //Thread.sleep(1000)
                adEvents?.impressionOccurred()
                omOutputString.append("Ad Events: ImpressionOccurred()\n")
                omOutput?.text = omOutputString.toString()

                //Thread.sleep(1000)
                mediaEvents?.firstQuartile()
                omOutputString.append("Media Event: First Quartile()\n")
                omOutput?.text = omOutputString.toString()

                //Thread.sleep(1000)
                mediaEvents?.midpoint()
                omOutputString.append("Media Event: Midpoint()\n")
                omOutput?.text = omOutputString.toString()

                //Thread.sleep(1000)
                mediaEvents?.thirdQuartile()
                omOutputString.append("Media Event: Third Quartile()\n")
                omOutput?.text = omOutputString.toString()

                //Thread.sleep(1000)
                mediaEvents?.pause()
                omOutputString.append("Media Events: Pause()\n")
                omOutput?.text = omOutputString.toString()

                //Thread.sleep(1000)
                mediaEvents?.resume()
                omOutputString.append("Media Events: Resume()\n")
                omOutput?.text = omOutputString.toString()

                //Thread.sleep(1000)
                mediaEvents?.playerStateChange(PlayerState.FULLSCREEN)
                omOutputString.append("Media Events: Fullscreen()\n")
                omOutput?.text = omOutputString.toString()

                //Thread.sleep(1000)
                mediaEvents?.playerStateChange(PlayerState.NORMAL)
                omOutputString.append("Media Events: Normal Screen()\n")
                omOutput?.text = omOutputString.toString()

                //Thread.sleep(1000)
                mediaEvents?.adUserInteraction(InteractionType.CLICK)
                omOutputString.append("Media Events: On Touch()\n")
                omOutput?.text = omOutputString.toString()

                //Thread.sleep(1000)
                mediaEvents?.volumeChange(0f)
                omOutputString.append("Media Events: volumeChange()\n")
                omOutput?.text = omOutputString.toString()

                //Thread.sleep(1000)
                mediaEvents?.complete()
                omOutputString.append("Media Events: Complete()\n")
                omOutput?.text = omOutputString.toString()

                //Thread.sleep(1000)
                mediaEvents?.complete()
                adSession?.finish()
                adSession = null
                omOutputString.append("Test Complete()\n")
                omOutput?.text = omOutputString.toString()

            } catch (e: Exception) {
                Log.e("ArcVideoSDK", "Exception: ${e.message}")
            }
        }
    }
}