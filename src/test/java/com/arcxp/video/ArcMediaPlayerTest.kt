package com.arcxp.video

import android.app.Activity
import android.app.Application
import android.content.Context
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.arcxp.video.listeners.ArcKeyListener
import com.arcxp.video.listeners.ArcVideoEventsListener
import com.arcxp.video.listeners.ArcVideoSDKErrorListener
import com.arcxp.video.model.ArcVideo
import com.arcxp.video.model.ArcVideoStream
import com.arcxp.video.views.ArcVideoFrame
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.verify
import io.mockk.verifySequence
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ArcMediaPlayerTest {

    @MockK
    lateinit var video: ArcVideo

    @RelaxedMockK
    lateinit var mContext: Context
    @RelaxedMockK
    lateinit var application: Application

    @MockK
    lateinit var mConfig: ArcXPVideoConfig

    @RelaxedMockK
    lateinit var arcVideoManager: ArcVideoManager

    @RelaxedMockK
    lateinit var builder: ArcXPVideoConfig.Builder

    private lateinit var testObject: ArcMediaPlayer

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        mockkObject(VideoPackageUtils)
        every { mContext.applicationContext} returns application
        every { VideoPackageUtils.createArcVideoManager(mContext = application) } returns arcVideoManager
        every { VideoPackageUtils.createArcMediaPlayerConfigBuilder() } returns builder
        every { builder.build() } returns mConfig

        testObject = ArcMediaPlayer.instantiate(context = mContext)
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `setActivity sets Activity on mConfigBuilder`() {
        val activity = mockk<Activity>()

        testObject
            .setActivity(activity)

        verify { builder.setActivity(activity) }

    }

    @Test
    fun `setActivity sets AppCompatActivity on mConfigBuilder`() {
        val activityCompat = mockk<AppCompatActivity>()

        testObject
            .setActivity(activityCompat)
    }

    @Test
    fun `setVideoFrame sets frame on builder`() {
        val videoFrame = mockk<ArcVideoFrame>()
        ArcMediaPlayer.createPlayer(mContext).setVideoFrame(videoFrame)

        verify { builder.setVideoFrame(videoFrame) }
    }

    @Test
    fun `initMedia, when mConfig not null, calls release and initializes mediaPlayer and media`() {
        testObject
            .configureMediaPlayer(mConfig)
            .initMedia(video)

        verifySequence {
            arcVideoManager.initMediaPlayer(mConfig)
            arcVideoManager.release()
            arcVideoManager.initMediaPlayer(mConfig)
            arcVideoManager.initMedia(video)
        }
    }

    @Test
    fun `initMedia, when mConfig null, calls release and initializes mediaPlayer and media`() {
        testObject
            .initMedia(video)

        verifySequence {
            arcVideoManager.release()
            builder.build()
            arcVideoManager.initMediaPlayer(mConfig)
            arcVideoManager.initMedia(video)
        }
    }

    @Test
    fun `initMedia from url, when mConfig not null, calls release and initializes mediaPlayer and media`() {
        val arcVideoStreamVirtualChannel = mockk<ArcVideoStream>()
        testObject
            .configureMediaPlayer(mConfig)
            .initMedia(arcVideoStreamVirtualChannel)

        verifySequence {
            arcVideoManager.initMediaPlayer(mConfig)
            arcVideoManager.release()
            arcVideoManager.initMediaPlayer(mConfig)
            arcVideoManager.initMedia(arcVideoStreamVirtualChannel)
        }
    }

    @Test
    fun `initMedia from url, when mConfig null, calls release and initializes mediaPlayer and media`() {
        val arcVideoStreamVirtualChannel = mockk<ArcVideoStream>()
        testObject
            .initMedia(arcVideoStreamVirtualChannel)

        verifySequence {
            arcVideoManager.release()
            builder.build()
            arcVideoManager.initMediaPlayer(mConfig)
            arcVideoManager.initMedia(arcVideoStreamVirtualChannel)
        }
    }

    @Test
    fun `initMedia with stream calls release, config not null, initializes media player and media`() {
        val video = mockk<ArcVideoStream>()

        testObject
            .configureMediaPlayer(mConfig)
            .initMedia(video)

        verifySequence {
            arcVideoManager.initMediaPlayer(mConfig)
            arcVideoManager.release()
            arcVideoManager.initMediaPlayer(mConfig)
            arcVideoManager.initMedia(video)
        }
    }

    @Test
    fun `initMedia with stream calls release, config null, initializes media player and media`() {
        val video = mockk<ArcVideoStream>()

        testObject
            .initMedia(video)

        verifySequence {
            arcVideoManager.release()
            builder.build()
            arcVideoManager.initMediaPlayer(mConfig)
            arcVideoManager.initMedia(video)
        }
    }

    @Test
    fun `initMedia with stream, adUrl, and config not null, calls release and initializes media player and media`() {
        val video = mockk<ArcVideoStream>()

        testObject
            .configureMediaPlayer(mConfig)
            .initMedia(video, "adUrl")

        verifySequence {
            arcVideoManager.initMediaPlayer(mConfig)
            arcVideoManager.release()
            arcVideoManager.initMediaPlayer(mConfig)
            arcVideoManager.initMedia(video, "adUrl")
        }
    }

    @Test
    fun `initMedia with stream, adUrl, and config null, calls release and initializes media player and media`() {
        val video = mockk<ArcVideoStream>()
        testObject
            .initMedia(video, "adUrl")

        verifySequence {
            arcVideoManager.release()
            builder.build()
            arcVideoManager.initMediaPlayer(mConfig)
            arcVideoManager.initMedia(video, "adUrl")
        }
    }

    @Test
    fun `initMedia with stream list and config not null, calls release and initializes media player and media`() {
        val list = listOf(mockk<ArcVideoStream>())

        testObject
            .configureMediaPlayer(mConfig)
            .initMedia(list)

        verifySequence {
            arcVideoManager.initMediaPlayer(mConfig)
            arcVideoManager.release()
            arcVideoManager.initMediaPlayer(mConfig)
            arcVideoManager.initMedia(list)
        }
    }

    @Test
    fun `initMedia with stream list and config null, calls release and initializes media player and media`() {
        val list = listOf(mockk<ArcVideoStream>())

        testObject
            .initMedia(list)

        verifySequence {
            arcVideoManager.release()
            arcVideoManager.initMediaPlayer(mConfig)
            arcVideoManager.initMedia(list)
        }
    }

    @Test
    fun `initMedia with stream list, adUrl, and config not null, calls release and initializes media player and media`() {
        val videos = listOf(mockk<ArcVideoStream>())

        val adUrls = listOf("adUrl")
        testObject
            .configureMediaPlayer(mConfig)
            .initMedia(videos, adUrls)

        verifySequence {
            arcVideoManager.initMediaPlayer(mConfig)
            arcVideoManager.release()
            arcVideoManager.initMediaPlayer(mConfig)
            arcVideoManager.initMedia(videos, adUrls)
        }
    }

    @Test
    fun `initMedia with stream list, adUrl, and config null, calls release and initializes media player and media`() {
        val videos = listOf(mockk<ArcVideoStream>())
        val adUrls = listOf("adUrl")

        testObject
            .initMedia(videos, adUrls)

        verifySequence {
            arcVideoManager.release()
            arcVideoManager.initMediaPlayer(mConfig)
            arcVideoManager.initMedia(videos, adUrls)
        }
    }

    @Test
    fun `addVideo adds video to manager`() {
        val video = mockk<ArcVideoStream>()
        testObject.addVideo(video)
        verify(exactly = 1) { arcVideoManager.addVideo(video) }
    }

    @Test
    fun `addVideo with ad url adds video and ad to manager`() {
        val video = mockk<ArcVideoStream>()
        testObject.addVideo(video, "ad")
        verify(exactly = 1) { arcVideoManager.addVideo(video, "ad") }
    }

    @Test
    fun `initMediaEvents initializes track events with manager`() {
        val events = mockk<ArcVideoEventsListener>()
        testObject.initMediaEvents(events)
        verify(exactly = 1) { arcVideoManager.initEvents(events) }
    }

    @Test
    fun `trackMediaEvents initializes track events with manager`() {
        val events = mockk<ArcVideoEventsListener>()
        testObject.trackMediaEvents(events)
        verify(exactly = 1) { arcVideoManager.initEvents(events) }
    }

    @Test
    fun `setErrorListener sets listener on manager`() {
        val listener = mockk<ArcVideoSDKErrorListener>()
        testObject.setErrorListener(listener)
        verify(exactly = 1) { arcVideoManager.setErrorListener(listener) }
    }

    @Test
    fun `trackErrors sets listener on manager`() {
        val listener = mockk<ArcVideoSDKErrorListener>()
        testObject.trackErrors(listener)
        verify(exactly = 1) { arcVideoManager.setErrorListener(listener) }
    }

    @Test
    fun `playVideo plays video `() {
        testObject.playVideo()
        verify(exactly = 1) { arcVideoManager.displayVideo() }
    }

    @Test
    fun `displayVideo plays video `() {
        testObject.displayVideo()
        verify(exactly = 1) { arcVideoManager.displayVideo() }
    }

    @Test
    fun `finish calls release on manager`() {
        testObject.finish()
        verify(exactly = 1) { arcVideoManager.release() }
    }

    @Test
    fun `onBackPressed calls release on manager`() {
        testObject.onBackPressed()
        verify(exactly = 1) { arcVideoManager.onBackPressed() }
    }

    @Test
    fun `enablePip calls method on mConfigBuilder`() {
        testObject.enablePip(true)
        verify(exactly = 1) { builder.enablePip(enable = true) }
    }

    @Test
    fun `stopPip calls methods on manager and finishes current activity`() {
        val arcVideoActivity = mockk<Activity>(relaxed = true)
        every { arcVideoManager.currentActivity } returns arcVideoActivity
        testObject.exitAppFromPip()
        verifySequence {
            arcVideoManager.setIsInPIP(false)
            arcVideoManager.release()
            arcVideoManager.currentActivity
            arcVideoActivity.finish()
        }
    }

    @Test
    fun `showControls calls through to manager`() {
        testObject.showControls()
        verify(exactly = 1) { arcVideoManager.showControls() }
    }

    @Test
    fun `hideControls calls through to manager`() {
        testObject.hideControls()
        verify(exactly = 1) { arcVideoManager.hideControls() }
    }

    @Test
    fun `isControlsVisible arcVideoManager boolean is null`() {
        assertFalse(testObject.isControlsVisible)
    }

    @Test
    fun `isControlsVisible arcVideoManager boolean is populated`() {
        every { arcVideoManager.isControlsVisible } returns true
        assertTrue(testObject.isControlsVisible)
    }

    @Test
    fun `isClosedCaptionAvailable arcVideoManager boolean is null`() {
        assertFalse(testObject.isClosedCaptionAvailable)
    }

    @Test
    fun `isClosedCaptionAvailable arcVideoManager boolean is populated`() {
        every { arcVideoManager.isClosedCaptionAvailable } returns true
        assertTrue(testObject.isClosedCaptionAvailable)
    }

    @Test
    fun `isFullScreen arcVideoManager boolean is null`() {
        assertFalse(testObject.isFullScreen)
    }

    @Test
    fun `isFullScreen arcVideoManager boolean is populated`() {
        every { arcVideoManager.isFullScreen } returns true
        assertTrue(testObject.isFullScreen)
    }

    @Test
    fun `setFullScreen calls through to manager with value`() {
        testObject.setFullscreen(true)
        verify(exactly = 1) { arcVideoManager.setFullscreen(true) }
    }

    @Test
    fun `setFullscreenKeyListener calls through to manager with value`() {
        val listener = mockk<ArcKeyListener>()
        testObject.setFullscreenKeyListener(listener)
        verify(exactly = 1) { arcVideoManager.setFullscreenListener(listener) }
    }

    @Test
    fun `setPlayerKeyListener calls through to manager with value`() {
        val listener = mockk<ArcKeyListener>()
        testObject.setPlayerKeyListener(listener)
        verify(exactly = 1) { arcVideoManager.setPlayerKeyListener(listener) }
    }

    @Test
    fun `stop calls through to manager`() {
        testObject.stop()
        verify(exactly = 1) { arcVideoManager.stopPlay() }
    }

    @Test
    fun `start calls through to manager`() {
        testObject.start()
        verify(exactly = 1) { arcVideoManager.startPlay() }
    }

    @Test
    fun `pause calls through to manager`() {
        testObject.pause()
        verify(exactly = 1) { arcVideoManager.pausePlay() }
    }

    @Test
    fun `resume calls through to manager`() {
        testObject.resume()
        verify(exactly = 1) { arcVideoManager.resumePlay() }
    }

    @Test
    fun `seekTo calls through to manager with value`() {
        val seekToTime = 234
        testObject.seekTo(seekToTime)
        verify(exactly = 1) { arcVideoManager.seekTo(seekToTime) }
    }

    @Test
    fun `setVolume calls through to manager with value`() {
        val volume = 234.3f
        testObject.setVolume(volume)
        verify(exactly = 1) { arcVideoManager.setVolume(volume) }
    }

    @Test
    fun `playbackState returns 0 if value not populated in manager `() {
        assertEquals(0, testObject.playbackState)
    }

    @Test
    fun `playbackState returns value if populated in manager `() {
        val playBackState = 543
        every { arcVideoManager.playbackState } returns playBackState
        assertEquals(playBackState, testObject.playbackState)
    }

    @Test
    fun `playWhenReadyState returns false if value not populated in manager `() {
        assertFalse(testObject.playWhenReadyState)
    }

    @Test
    fun `playWhenReadyState returns value if populated in manager`() {
        every { arcVideoManager.playWhenReadyState } returns true
        assertTrue(testObject.playWhenReadyState)
    }

    @Test
    fun `playerPosition returns value populated in manager`() {
        val playHeadPosition = 543324L
        every { arcVideoManager.playheadPosition } returns playHeadPosition
        assertEquals(playHeadPosition, testObject.playerPosition)
    }

    @Test
    fun `currentTimelinePosition returns value populated in manager`() {
        val currentTimelinePosition = 543324L
        every { arcVideoManager.currentTimelinePosition } returns currentTimelinePosition
        assertEquals(
            currentTimelinePosition,
            testObject.currentTimelinePosition
        )
    }

    @Test
    fun `currentVideoDuration returns value populated in manager`() {
        val currentVideoDuration = 543324L
        every { arcVideoManager.currentVideoDuration } returns currentVideoDuration
        assertEquals(
            currentVideoDuration,
            testObject.currentVideoDuration
        )
    }

    @Test
    fun `addOverlay calls builder with values`() {
        val view = mockk<View>()
        testObject.addOverlay("tag", view)
        verify(exactly = 1) { builder.addOverlay("tag", view) }
    }

    @Test
    fun `getOverlay returns value from manager`() {
        val view = mockk<View>()
        every { arcVideoManager.getOverlay("tag") } returns view
        assertEquals(view, testObject.getOverlay("tag"))
    }

    @Test
    fun `sdkVersion returns value from resources`() {
        assertEquals("1.5.0", testObject.sdkVersion)
    }

    @Test
    fun `setViewsToHide sets views to hide in builder`() {
        val view1 = mockk<View>()
        val view2 = mockk<View>()
        val view3 = mockk<View>()
        testObject.setViewsToHide(view1, view2, view3)
        verify(exactly = 1) { builder.setViewsToHide(view1, view2, view3) }
    }

    @Test
    fun `setEnableAds calls builder with value`() {
        testObject.setEnableAds(true)
        verify(exactly = 1) { builder.setEnableAds(true) }
    }

    @Test
    fun `setAdConfigUrl calls builder with value`() {
        testObject.setAdConfigUrl("url")
        verify(exactly = 1) { builder.setAdConfigUrl("url") }
    }

    @Test
    fun `setPreferredStreamType calls builder with value`() {
        val preferredStreamType = ArcXPVideoConfig.PreferredStreamType.HLS
        testObject.setPreferredStreamType(preferredStreamType)
        verify(exactly = 1) { builder.setPreferredStreamType(preferredStreamType) }
    }

    @Test
    fun `setMaxBitRate calls builder with value`() {
        val maxBitRate = 123
        testObject.setMaxBitRate(maxBitRate)
        verify(exactly = 1) { builder.setMaxBitRate(maxBitRate) }
    }

    @Test
    fun `showClosedCaption calls builder with value`() {
        testObject.showClosedCaption(true)
        verify(exactly = 1) { builder.showClosedCaption(true) }
    }

    @Test
    fun `toggleClosedCaption returns value from manager`() {
        every { arcVideoManager.enableClosedCaption(true) } returns true
        assertTrue(testObject.toggleClosedCaption(true))
    }


    @Test
    fun `setCcButtonDrawable sets value in manager, returns true if successful`() {
        val drawableRes = 3423245
        every { arcVideoManager.setCcButtonDrawable(drawableRes) } returns true
        assertTrue(testObject.setCcButtonDrawable(drawableRes))
    }

    @Test
    fun `setCcButtonDrawable sets value in manager, returns false if unsuccessful`() {
        val drawableRes = 3423245
        every { arcVideoManager.setCcButtonDrawable(drawableRes) } returns false
        assertFalse(testObject.setCcButtonDrawable(drawableRes))
    }

    @Test
    fun `showCountdown sets value in builder`() {
        testObject.showCountdown(true)
        verify(exactly = 1) { builder.showCountdown(true) }
    }

    @Test
    fun `showProgressBar sets value in builder`() {
        testObject.showProgressBar(true)
        verify(exactly = 1) { builder.showProgressBar(true) }
    }

    @Test
    fun `setServerSideAds sets value in builder`() {
        testObject.setServerSideAds(true)
        verify(exactly = 1) { builder.setServerSideAds(true) }
    }

    @Test
    fun `setClientSideAds sets value in builder`() {
        testObject.setClientSideAds(true)
        verify(exactly = 1) { builder.setClientSideAds(true) }
    }

    @Test
    fun `setAutoStartPlay sets value in builder`() {
        testObject.setAutoStartPlay(true)
        verify(exactly = 1) { builder.setAutoStartPlay(true) }
    }

    @Test
    fun `showSeekButton sets value in builder`() {
        testObject.showSeekButton(true)
        verify(exactly = 1) { builder.showSeekButton(true) }
    }

    @Test
    fun `setStartMuted sets value in builder`() {
        testObject.setStartMuted(true)
        verify(exactly = 1) { builder.setStartMuted(true) }
    }

    @Test
    fun `setFocusSkipButton sets value in builder`() {
        testObject.setFocusSkipButton(true)
        verify(exactly = 1) { builder.setFocusSkipButton(true) }
    }

    @Test
    fun `setCcStartMode sets value in builder`() {
        val expectedCcStartMode = ArcXPVideoConfig.CCStartMode.ON
        testObject.setCcStartMode(expectedCcStartMode)
        verify(exactly = 1) { builder.setCcStartMode(expectedCcStartMode) }
    }

    @Test
    fun `setAutoShowControls sets value in builder`() {
        testObject.setAutoShowControls(true)
        verify(exactly = 1) { builder.setAutoShowControls(true) }
    }

    @Test
    fun `setShowClosedCaptionTrackSelection sets value in builder`() {
        testObject.setShowClosedCaptionTrackSelection(true)
        verify(exactly = 1) { builder.setShowClosedCaptionTrackSelection(true) }
    }

    @Test
    fun `addAdParam sets values in builder`() {
        testObject.addAdParam("key", "value")
        verify(exactly = 1) { builder.addAdParam("key", "value") }
    }

    @Test
    fun `onPause releases manager and calls manager onPause`() {
        testObject.onPause()
        verifySequence {
            arcVideoManager.release()
            arcVideoManager.onPause()
        }
    }

    @Test
    fun `onStop calls manager onStop`() {
        testObject.onStop()
        verify(exactly = 1) { arcVideoManager.onStop() }
    }

    @Test
    fun `onDestroy calls manager onDestroy`() {
        testObject.onDestroy()
        verify(exactly = 1) { arcVideoManager.onDestroy() }
    }

    @Test
    fun `onResume calls manager onResume`() {
        testObject.onResume()
        verify(exactly = 1) { arcVideoManager.onResume() }
    }

    @Test
    fun `onPictureInPictureModeChanged when arcVideoManger isPipStopRequest stops pip `() {
        val currentActivity = mockk<Activity>(relaxed = true)
        every { arcVideoManager.currentActivity } returns currentActivity
        every { arcVideoManager.isPipStopRequest } returns true

        testObject.onPictureInPictureModeChanged(true, null)

        verifySequence {
            arcVideoManager.isPipStopRequest
            arcVideoManager.setIsInPIP(false)
            arcVideoManager.release()
            arcVideoManager.currentActivity
            currentActivity.finish()
        }
    }

    @Test
    fun `onPictureInPictureModeChanged hides controls and disables auto show`() {
        val currentActivity = mockk<Activity>(relaxed = true)
        every { arcVideoManager.currentActivity } returns currentActivity
        every { arcVideoManager.isPipStopRequest } returns false

        testObject.onPictureInPictureModeChanged(true, null)

        verifySequence {
            arcVideoManager.isPipStopRequest
            arcVideoManager.hideControls()
            arcVideoManager.toggleAutoShow(false)
        }
    }

    @Test
    fun `onPictureInPictureModeChanged shows controls and re-enables auto show`() {
        val currentActivity = mockk<Activity>(relaxed = true)
        every { arcVideoManager.currentActivity } returns currentActivity
        every { arcVideoManager.isPipStopRequest } returns false

        testObject.onPictureInPictureModeChanged(false, null)

        verifySequence {
            arcVideoManager.isPipStopRequest
            arcVideoManager.showControls()
            arcVideoManager.toggleAutoShow(true)
        }
    }

    @Test
    fun `setControlsShowTimeoutMs sets value in builder`() {
        val ms = 45378
        testObject.setControlsShowTimeoutMs(ms)
        verify(exactly = 1) { builder.setControlsShowTimeoutMs(ms) }
    }

    @Test
    fun `enableLogging sets value in builder`() {
        testObject.enableLogging()
        verify(exactly = 1) { builder.enableLogging() }
    }

    @Test
    fun `useDialogForFullscreen sets value in builder`() {
        testObject.useDialogForFullscreen(true)
        verify(exactly = 1) { builder.useDialogForFullscreen(true) }
    }

    @Test
    fun `keepControlsSpaceOnHide sets value in builder`() {
        testObject.keepControlsSpaceOnHide(true)
        verify(exactly = 1) { builder.setKeepControlsSpaceOnHide(true) }
    }

    @Test
    fun `isPipEnabled returns arcVideoManger Value when true`() {
        every { arcVideoManager.isPipEnabled } returns true
        assertTrue(testObject.isPipEnabled())
    }

    @Test
    fun `isPipEnabled returns arcVideoManger Value when false`() {
        every { arcVideoManager.isPipEnabled } returns false
        assertFalse(testObject.isPipEnabled())
    }
}