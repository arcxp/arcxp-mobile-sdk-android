package com.arcxp.video.util

import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import com.arcxp.commons.util.BuildVersionProviderImpl
import com.arcxp.commons.util.DependencyFactory
import com.arcxp.sdk.R
import com.arcxp.video.ArcMediaPlayerConfig
import com.arcxp.video.listeners.VideoPlayer
import com.arcxp.video.model.AdVerification
import com.arcxp.video.model.JavascriptResource
import com.arcxp.video.views.VideoFrameLayout
import com.google.android.exoplayer2.ui.StyledPlayerView
import com.iab.omid.library.washpost.adsession.AdEvents
import com.iab.omid.library.washpost.adsession.AdSession
import com.iab.omid.library.washpost.adsession.FriendlyObstructionPurpose
import com.iab.omid.library.washpost.adsession.media.*
import io.mockk.*
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import org.junit.After
import org.junit.Before
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runners.MethodSorters
import java.util.concurrent.CountDownLatch

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class OmidHelperTest {
    @RelaxedMockK lateinit var context: Context
    @RelaxedMockK lateinit var config: ArcMediaPlayerConfig
    @RelaxedMockK lateinit var layout: VideoFrameLayout
    @RelaxedMockK lateinit var videoPlayer: VideoPlayer
    @RelaxedMockK lateinit var adSession: AdSession
    @RelaxedMockK lateinit var adEvents: AdEvents
    @RelaxedMockK lateinit var mediaEvents: MediaEvents

    @MockK lateinit var view1: View
    @MockK lateinit var view2: View
    @MockK lateinit var view3: View
    @MockK lateinit var controller: View
    @MockK lateinit var playerView: StyledPlayerView
    @MockK lateinit var properties: VastProperties
    @MockK lateinit var buildVersionProvider: BuildVersionProviderImpl

    private lateinit var overlays: HashMap<String, View>
    private val latch = CountDownLatch(2)
    private val key1 = "key1"
    private val key2 = "key2"
    private val key3 = "key3"
    private val error = "exception message"

    private lateinit var testObject: OmidHelper

    var adVerifications: List<AdVerification> = listOf(AdVerification(listOf(JavascriptResource("omid", "http://omid.com")), "vendor", "params"))

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxUnitFun = true)
        mockkObject(DependencyFactory)
        every { DependencyFactory.createBuildVersionProvider() } returns buildVersionProvider
        every { buildVersionProvider.sdkInt() } returns Build.VERSION_CODES.N
        overlays = HashMap()
        overlays[key1] = view1
        overlays[key2] = view2
        overlays[key3] = view3

        mockkConstructor(Handler::class)
        every { anyConstructed<Handler>().post(any()) } answers {
            try {
                val runnable = invocation.args[0] as Runnable?
                runnable?.run()
            } finally {
                latch.countDown()
            }
            true
        }

        mockkStatic(Looper::class)
        every { Looper.getMainLooper() } returns mockk()
        mockkStatic(Log::class)
        every { Log.e(any(), any()) } returns 1
        every { Log.d(any(), any()) } returns 1
        mockkStatic(OmidAdSessionUtil::class)
        every {
            OmidAdSessionUtil
                    .getNativeAdSession(
                            context,
                            config,
                        adVerifications)
        } returns adSession
        mockkStatic(MediaEvents::class)
        every { MediaEvents.createMediaEvents(adSession) } returns mediaEvents
        mockkStatic(AdEvents::class)
        every { AdEvents.createAdEvents(adSession) } returns adEvents
        mockkStatic(VastProperties::class)
        every { VastProperties.createVastPropertiesForNonSkippableMedia(false, Position.STANDALONE) } returns properties

        every { config.isEnableOmid } returns true
        every { config.isLoggingEnabled } returns true
        every { config.overlays } returns overlays
        every { videoPlayer.playControls } returns playerView
        every { playerView.findViewById<View>(R.id.exo_controller) } returns controller

        testObject = OmidHelper(context, config, layout, videoPlayer)
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `init given adSession is null `() {
        testObject.init(verifications = adVerifications)
        verifySequence {
            config.isEnableOmid
            OmidAdSessionUtil
                    .getNativeAdSession(
                            context,
                            config,
                        adVerifications)
            adSession.registerAdView(layout)
            config.overlays
            adSession.addFriendlyObstruction(view1, FriendlyObstructionPurpose.OTHER, "key1")
            adSession.addFriendlyObstruction(view2, FriendlyObstructionPurpose.OTHER, "key2")
            adSession.addFriendlyObstruction(view3, FriendlyObstructionPurpose.OTHER, "key3")
            videoPlayer.playControls
            playerView.findViewById<View>(R.id.exo_controller)
            adSession.addFriendlyObstruction(controller, FriendlyObstructionPurpose.VIDEO_CONTROLS, "controls")
            MediaEvents.createMediaEvents(adSession)
            adSession.start()
            AdEvents.createAdEvents(adSession)
            VastProperties.createVastPropertiesForNonSkippableMedia(false, Position.STANDALONE)
            adEvents.loaded(properties)
            config.isLoggingEnabled
            Log.d("ArcVideoSDK", "OM Ad session started")
        }
        verify(exactly = 0) { Log.e(any(), any()) }
    }

    @Test
    fun `init given adSession is non null `() {
        testObject.init(adVerifications)
        clearAllMocks(answers = false)

        testObject.init(adVerifications)

        verifySequence {
            adSession.finish()
            config.isLoggingEnabled
            Log.d("ArcVideoSDK", "OM Ad session stopped")
            config.isEnableOmid
            OmidAdSessionUtil
                    .getNativeAdSession(
                            context,
                            config,
                        adVerifications)
            adSession.registerAdView(layout)
            config.overlays
            adSession.addFriendlyObstruction(view1, FriendlyObstructionPurpose.OTHER, "key1")
            adSession.addFriendlyObstruction(view2, FriendlyObstructionPurpose.OTHER, "key2")
            adSession.addFriendlyObstruction(view3, FriendlyObstructionPurpose.OTHER, "key3")
            videoPlayer.playControls
            playerView.findViewById<View>(R.id.exo_controller)
            adSession.addFriendlyObstruction(controller, FriendlyObstructionPurpose.VIDEO_CONTROLS, "controls")
            MediaEvents.createMediaEvents(adSession)
            adSession.start()
            AdEvents.createAdEvents(adSession)
            VastProperties.createVastPropertiesForNonSkippableMedia(false, Position.STANDALONE)
            adEvents.loaded(properties)
            config.isLoggingEnabled
            Log.d("ArcVideoSDK", "OM Ad session started")
        }
        verify(exactly = 0) { Log.e(any(), any()) }
    }


    @Test
    fun `init throws exception and is logged `() {
        every { OmidAdSessionUtil.getNativeAdSession(any(), any(), any()) } throws Exception(error)

        testObject.init(adVerifications)

        verifySequence {
            config.isEnableOmid
            config.isLoggingEnabled
            Log.e("ArcVideoSDK", "OM Exception: $error")
        }
    }

    @Test
    fun `clear throws exception and is logged `() {

        testObject.init(adVerifications)
        clearAllMocks(answers = false)
        every { adSession.finish() } throws Exception(error)

        testObject.clear()

        verifySequence {
            config.isLoggingEnabled
            Log.e("ArcVideoSDK", "OM Exception: $error")
        }
    }

    @Test
    fun `clear given adSession not null `() {
        testObject.init(adVerifications)
        clearAllMocks(answers = false)

        testObject.clear()

        verifySequence {
            adSession.finish()
            config.isLoggingEnabled
            Log.d("ArcVideoSDK", "OM Ad session stopped")
        }
        verify(exactly = 0) { Log.e(any(), any()) }
    }

    @Test
    fun `destroy calls clear, throws exception and is logged `() {

        testObject.init(adVerifications)
        clearAllMocks(answers = false)
        every { adSession.finish() } throws Exception(error)

        testObject.clear()

        verifySequence {
            config.isLoggingEnabled
            Log.e("ArcVideoSDK", "OM Exception: $error")
        }
    }

    @Test
    fun `destroy given adSession not null calls clear`() {
        testObject.init(adVerifications)
        clearAllMocks(answers = false)

        testObject.clear()

        verifySequence {
            adSession.finish()
            config.isLoggingEnabled
            Log.d("ArcVideoSDK", "OM Ad session stopped")
        }
        verify(exactly = 0) { Log.e(any(), any()) }
    }

    @Test
    fun `mediaEventsStart starts media events, logs event`() {
        val length = 123.3f
        val volume = 0.67f
        testObject.init(adVerifications)
        clearAllMocks(answers = false)

        testObject.mediaEventsStart(length, volume)

        verifySequence {
            mediaEvents.start(length, volume)
            config.isLoggingEnabled
            Log.d("ArcVideoSDK", "OM mediaEvents.start() called")
        }
        verify(exactly = 0) { Log.e(any(), any()) }
    }

    @Test
    fun `mediaEventsStart throws exception, logs event`() {
        val length = 123.3f
        val volume = 0.67f

        every { mediaEvents.start(any(), any()) } throws Exception(error)
        testObject.init(adVerifications)
        clearAllMocks(answers = false)

        testObject.mediaEventsStart(length, volume)

        verifySequence {
            config.isLoggingEnabled
            Log.e("ArcVideoSDK", "OM Exception: $error")
        }
    }

    @Test
    fun `mediaEventsFirstQuartile calls mediaEvents method, logs event`() {
        testObject.init(adVerifications)
        clearAllMocks(answers = false)

        testObject.mediaEventsFirstQuartile()

        verifySequence {
            mediaEvents.firstQuartile()
            config.isLoggingEnabled
            Log.d("ArcVideoSDK", "OM mediaEvents.firstQuartile() called")
        }
        verify(exactly = 0) { Log.e(any(), any()) }
    }

    @Test
    fun `mediaEventsFirstQuartile throws exception, logs event`() {
        every { mediaEvents.firstQuartile() } throws Exception(error)
        testObject.init(adVerifications)
        clearAllMocks(answers = false)

        testObject.mediaEventsFirstQuartile()

        verifySequence {
            config.isLoggingEnabled
            Log.e("ArcVideoSDK", "OM Exception: $error")
        }
    }

    @Test
    fun `mediaEventsMidpoint calls mediaEvents method, logs event`() {
        testObject.init(adVerifications)
        clearAllMocks(answers = false)

        testObject.mediaEventsMidpoint()

        verifySequence {
            mediaEvents.midpoint()
            config.isLoggingEnabled
            Log.d("ArcVideoSDK", "OM mediaEvents.midpoint() called")
        }
        verify(exactly = 0) { Log.e(any(), any()) }
    }

    @Test
    fun `mediaEventsMidpoint throws exception, logs event`() {
        every { mediaEvents.midpoint() } throws Exception(error)
        testObject.init(adVerifications)
        clearAllMocks(answers = false)

        testObject.mediaEventsMidpoint()

        verifySequence {
            config.isLoggingEnabled
            Log.e("ArcVideoSDK", "OM Exception: $error")
        }
    }

    @Test
    fun `mediaEventsThirdQuartile calls mediaEvents method, logs event`() {
        testObject.init(adVerifications)
        clearAllMocks(answers = false)

        testObject.mediaEventsThirdQuartile()

        verifySequence {
            mediaEvents.thirdQuartile()
            config.isLoggingEnabled
            Log.d("ArcVideoSDK", "OM mediaEvents.thirdQuartile() called")
        }
        verify(exactly = 0) { Log.e(any(), any()) }
    }

    @Test
    fun `mediaEventsThirdQuartile throws exception, logs event`() {
        every { mediaEvents.thirdQuartile() } throws Exception(error)
        testObject.init(adVerifications)
        clearAllMocks(answers = false)

        testObject.mediaEventsThirdQuartile()

        verifySequence {

            config.isLoggingEnabled
            Log.e("ArcVideoSDK", "OM Exception: $error")
        }
    }

    @Test
    fun `mediaEventsComplete calls mediaEvents method, logs event`() {
        testObject.init(adVerifications)
        clearAllMocks(answers = false)

        testObject.mediaEventsComplete()

        verifySequence {
            mediaEvents.complete()
            config.isLoggingEnabled
            Log.d("ArcVideoSDK", "OM mediaEvents.complete() called")
        }
        verify(exactly = 0) { Log.e(any(), any()) }
    }

    @Test
    fun `mediaEventsComplete throws exception, logs event`() {
        every { mediaEvents.complete() } throws Exception(error)
        testObject.init(adVerifications)
        clearAllMocks(answers = false)

        testObject.mediaEventsComplete()

        verifySequence {
            config.isLoggingEnabled
            Log.e("ArcVideoSDK", "OM Exception: $error")
        }
    }

    @Test
    fun `adEventsImpressionOccurred calls mediaEvents method, logs event`() {
        testObject.init(adVerifications)
        clearAllMocks(answers = false)

        testObject.adEventsImpressionOccurred()

        verifySequence {
            adEvents.impressionOccurred()
            config.isLoggingEnabled
            Log.d("ArcVideoSDK", "OM adEvents.impressionOccurred() called")
        }
        verify(exactly = 0) { Log.e(any(), any()) }
    }

    @Test
    fun `adEventsImpressionOccurred throws exception, logs event`() {
        every { adEvents.impressionOccurred() } throws Exception(error)
        testObject.init(adVerifications)
        clearAllMocks(answers = false)

        testObject.adEventsImpressionOccurred()

        verifySequence {
            config.isLoggingEnabled
            Log.e("ArcVideoSDK", "OM Exception: $error")
        }
    }

    @Test
    fun `mediaEventsPause calls mediaEvents method, logs event`() {
        testObject.init(adVerifications)
        clearAllMocks(answers = false)

        testObject.mediaEventsPause()

        verifySequence {
            mediaEvents.pause()
            config.isLoggingEnabled
            Log.d("ArcVideoSDK", "OM mediaEvents.pause() called")
        }
        verify(exactly = 0) { Log.e(any(), any()) }
    }

    @Test
    fun `mediaEventsPause throws exception, logs event`() {
        every { mediaEvents.pause() } throws Exception(error)
        testObject.init(adVerifications)
        clearAllMocks(answers = false)

        testObject.mediaEventsPause()

        verifySequence {
            config.isLoggingEnabled
            Log.e("ArcVideoSDK", "OM Exception: $error")
        }
    }

    @Test
    fun `mediaEventsResume calls mediaEvents method, logs event`() {
        testObject.init(adVerifications)
        clearAllMocks(answers = false)

        testObject.mediaEventsResume()

        verifySequence {
            mediaEvents.resume()
            config.isLoggingEnabled
            Log.d("ArcVideoSDK", "OM mediaEvents.resume() called")
        }
        verify(exactly = 0) { Log.e(any(), any()) }
    }

    @Test
    fun `mediaEventsResume throws exception, logs event`() {
        every { mediaEvents.resume() } throws Exception(error)
        testObject.init(adVerifications)
        clearAllMocks(answers = false)

        testObject.mediaEventsResume()

        verifySequence {
            config.isLoggingEnabled
            Log.e("ArcVideoSDK", "Exception: $error")
        }
    }

    @Test
    fun `mediaEventsFullscreen calls mediaEvents method, logs event`() {
        testObject.init(adVerifications)
        clearAllMocks(answers = false)

        testObject.mediaEventsFullscreen()

        verifySequence {
            mediaEvents.playerStateChange(PlayerState.FULLSCREEN)
            config.isLoggingEnabled
            Log.d("ArcVideoSDK", "OM mediaEvents.fullscreen() called")
        }
        verify(exactly = 0) { Log.e(any(), any()) }
    }

    @Test
    fun `mediaEventsFullscreen throws exception, logs event`() {
        every { mediaEvents.playerStateChange(any()) } throws Exception(error)
        testObject.init(adVerifications)
        clearAllMocks(answers = false)

        testObject.mediaEventsFullscreen()

        verifySequence {
            config.isLoggingEnabled
            Log.e("ArcVideoSDK", "Exception: $error")
        }
    }

    @Test
    fun `mediaEventsNormalScreen calls mediaEvents method, logs event`() {
        testObject.init(adVerifications)
        clearAllMocks(answers = false)

        testObject.mediaEventsNormalScreen()

        verifySequence {
            mediaEvents.playerStateChange(PlayerState.NORMAL)
            config.isLoggingEnabled
            Log.d("ArcVideoSDK", "OM mediaEvents.normalScreen() called")
        }
        verify(exactly = 0) { Log.e(any(), any()) }
    }

    @Test
    fun `mediaEventsNormalScreen throws exception, logs event`() {
        every { mediaEvents.playerStateChange(any()) } throws Exception(error)
        testObject.init(adVerifications)
        clearAllMocks(answers = false)

        testObject.mediaEventsNormalScreen()

        verifySequence {
            config.isLoggingEnabled
            Log.e("ArcVideoSDK", "Exception: $error")
        }
    }

    @Test
    fun `mediaEventsOnTouch calls mediaEvents method, logs event`() {
        testObject.init(adVerifications)
        clearAllMocks(answers = false)

        testObject.mediaEventsOnTouch()

        verifySequence {
            mediaEvents.adUserInteraction(InteractionType.CLICK)
            config.isLoggingEnabled
            Log.d("ArcVideoSDK", "OM mediaEvents.onTouch() called")
        }
        verify(exactly = 0) { Log.e(any(), any()) }
    }

    @Test
    fun `mediaEventsOnTouch throws exception, logs event`() {
        every { mediaEvents.adUserInteraction(InteractionType.CLICK) } throws Exception(error)
        testObject.init(adVerifications)
        clearAllMocks(answers = false)

        testObject.mediaEventsOnTouch()

        verifySequence {
            config.isLoggingEnabled
            Log.e("ArcVideoSDK", "Exception: $error")
        }
    }

    @Test
    fun `mediaEventsVolumeChange calls mediaEvents method, logs event`() {
        val volume = 0.67f
        testObject.init(adVerifications)
        clearAllMocks(answers = false)

        testObject.mediaEventsVolumeChange(volume)

        verifySequence {
            mediaEvents.volumeChange(volume)
            config.isLoggingEnabled
            Log.d("ArcVideoSDK", "OM mediaEvents.volumeChange() called")
        }
        verify(exactly = 0) { Log.e(any(), any()) }
    }

    @Test
    fun `mediaEventsVolumeChange throws exception, logs event`() {
        val volume = 0.67f

        every { mediaEvents.volumeChange(volume) } throws Exception(error)
        testObject.init(adVerifications)
        clearAllMocks(answers = false)

        testObject.mediaEventsVolumeChange(volume)

        verifySequence {
            config.isLoggingEnabled
            Log.e("ArcVideoSDK", "Exception: $error")
        }
    }
}