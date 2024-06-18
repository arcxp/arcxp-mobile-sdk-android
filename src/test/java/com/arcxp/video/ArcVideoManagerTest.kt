package com.arcxp.video

import android.app.ActionBar
import android.app.Activity
import android.app.Application
import android.app.PictureInPictureParams
import android.content.Context
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import android.os.Handler
import android.util.DisplayMetrics
import android.util.Log
import android.util.TypedValue
import android.view.Display
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.arcxp.ArcXPMobileSDK
import com.arcxp.commons.testutils.TestUtils
import com.arcxp.commons.throwables.ArcXPException
import com.arcxp.commons.util.BuildVersionProviderImpl
import com.arcxp.commons.util.DependencyFactory
import com.arcxp.sdk.R
import com.arcxp.video.cast.ArcCastManager
import com.arcxp.video.listeners.ArcKeyListener
import com.arcxp.video.listeners.ArcVideoEventsListener
import com.arcxp.video.listeners.ArcVideoSDKErrorListener
import com.arcxp.video.model.AdConfig
import com.arcxp.video.model.ArcVideo
import com.arcxp.video.model.ArcVideoSDKErrorType
import com.arcxp.video.model.ArcVideoStream
import com.arcxp.video.model.ArcVideoStreamVirtualChannel
import com.arcxp.video.model.AvailList
import com.arcxp.video.model.Stream
import com.arcxp.video.model.TrackingType
import com.arcxp.video.model.TrackingTypeData
import com.arcxp.video.model.VideoAdData
import com.arcxp.video.players.PlayerContract
import com.arcxp.video.service.AdUtils
import com.arcxp.video.service.AdUtils.Companion.enableServerSideAds
import com.arcxp.video.service.AdUtils.Companion.getVideoManifest
import com.arcxp.video.util.TrackingHelper
import com.arcxp.video.util.Utils
import com.arcxp.video.views.ArcVideoFrame
import com.arcxp.video.views.VideoFrameLayout
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.spyk
import io.mockk.verify
import io.mockk.verifySequence
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.FixMethodOrder
import org.junit.Rule
import org.junit.Test
import org.junit.runners.MethodSorters
import java.util.Timer
import java.util.TimerTask
import java.util.concurrent.CountDownLatch

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class ArcVideoManagerTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @RelaxedMockK
    private lateinit var mContext: Context

    @RelaxedMockK
    private lateinit var application: Application

    @RelaxedMockK
    private lateinit var mockResources: Resources

    @RelaxedMockK
    private lateinit var utils: Utils

    @RelaxedMockK
    private lateinit var videoFrameLayout: VideoFrameLayout

    @RelaxedMockK
    private lateinit var trackingHelper: TrackingHelper

    @RelaxedMockK
    private lateinit var postTvPlayerImpl: PlayerContract

    @RelaxedMockK
    private lateinit var videoAdData: VideoAdData

    @RelaxedMockK
    private lateinit var videoAdData2: VideoAdData

    @RelaxedMockK
    private lateinit var configInfo: ArcXPVideoConfig

    @RelaxedMockK
    private lateinit var mMessageText: TextView

    @RelaxedMockK
    private lateinit var mMessageOverlay: RelativeLayout

    @RelaxedMockK
    private lateinit var params: RelativeLayout.LayoutParams

    @RelaxedMockK
    private lateinit var videoStream: ArcVideoStream

    @RelaxedMockK
    private lateinit var videoStream2: ArcVideoStream

    @MockK
    private lateinit var mockBestStream: Stream

    @MockK
    private lateinit var builder: ArcVideo.Builder

    @MockK
    private lateinit var builder2: ArcVideo.Builder

    @RelaxedMockK
    private lateinit var eventTracker: ArcVideoEventsListener

    @RelaxedMockK
    private lateinit var errorListener: ArcVideoSDKErrorListener

    @RelaxedMockK
    private lateinit var mockAdConfig: AdConfig

    @RelaxedMockK
    private lateinit var pictureInPictureParams: PictureInPictureParams

    @RelaxedMockK
    private lateinit var pictureInPictureParamsBuilder: PictureInPictureParams.Builder

    @RelaxedMockK
    private lateinit var availList: AvailList

    @RelaxedMockK
    private lateinit var mTimer: Timer

    @MockK
    private lateinit var buildVersionProvider: BuildVersionProviderImpl

    private val expectedUrl = "a url"
    private val expectedAdUrl = "adUrl"
    private val expectedManifestUrl = "manifestUrl"
    private val expectedTrackingUrl = "trackingUrl"
    private val expectedPollingDelay = 3000L
    private val expectedAdError = "error message from exception"
    private val expectedPosition = 123L
    private val videoPlayerError = "Cannot add a video to if the video player is not initialized."
    private val mediaPlayerError =
        "Media Player has not been initialized.  Please call initMediaPlayer first."
    private val latch = CountDownLatch(2)


    private lateinit var testObject: ArcVideoManager

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        mockkConstructor(PictureInPictureParams.Builder::class)
        every { anyConstructed<PictureInPictureParams.Builder>().setAspectRatio(any()) } returns pictureInPictureParamsBuilder
        every { anyConstructed<PictureInPictureParams.Builder>().build() } returns pictureInPictureParams
        every { pictureInPictureParamsBuilder.build() } returns pictureInPictureParams
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
        utils.apply {
            every { createVideoFrameLayout(mContext) } returns videoFrameLayout
            every { createTextView(mContext) } returns mMessageText
            every { createRelativeLayout(mContext) } returns mMessageOverlay
            every {
                createRelativeLayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            } returns params
            every { createTimer() } returns mTimer
        }
        every { mContext.resources } returns mockResources
        every { mockResources.getInteger(R.integer.ad_polling_delay_ms) } returns expectedPollingDelay.toInt()


        mockkConstructor(ArcVideo.Builder::class)
        every {
            anyConstructed<ArcVideo.Builder>().setVideoStream(videoStream, configInfo)
        } returns builder
        every {
            anyConstructed<ArcVideo.Builder>().setVideoStream(videoStream2, configInfo)
        } returns builder2
        videoAdData.apply {
            every { error } returns mockk {
                every { message } returns expectedAdError
            }
            every { manifestUrl } returns expectedManifestUrl
            every { sessionId } returns "sessionId"
            every { trackingUrl } returns expectedTrackingUrl
        }
        videoAdData2.apply {
            every { error } returns mockk {
                every { message } returns expectedAdError
            }
            every { manifestUrl } returns expectedManifestUrl
            every { sessionId } returns "sessionId"
            every { trackingUrl } returns expectedTrackingUrl
        }
        mockkObject(AdUtils)
        every {
            getVideoManifest(
                videoStream,
                mockBestStream,
                configInfo
            )
        } returns videoAdData
        every {
            getVideoManifest(
                expectedUrl,
                configInfo
            )
        } returns videoAdData
        every {
            getVideoManifest(
                videoStream2,
                mockBestStream,
                configInfo
            )
        } returns videoAdData2
        every { enableServerSideAds(any(), any()) } returns false
        every { AdUtils.getAvails(any()) } returns availList
        every { availList.avails } returns listOf(mockk())
        mockkObject(ArcXPMobileSDK)
        every { ArcXPMobileSDK.application() } returns application
        every { application.getString(R.string.media_player_uninitialized_error) } returns mediaPlayerError
        every { application.getString(R.string.video_player_uninitialized_error) } returns videoPlayerError
        mockkConstructor(Timer::class)
        every { mTimer.schedule(any(), 2000, any()) } just Runs
        configInfo.apply {
            every { isEnableClientSideAds } returns true
            every { isEnableServerSideAds } returns true
            every { isEnableAds } returns true
            every { isAutoStartPlay } returns true
            every { isStartMuted } returns false
            every { adConfigUrl } returns expectedAdUrl
            every { isLoggingEnabled } returns true
            every { adConfig } returns null
            every { isEnableOmid } returns false
        }
        mockAdConfig.apply {
            every { getAdConfigUrl() } returns expectedAdUrl
            every { isAdEnabled } returns true
        }
        testObject = ArcVideoManager(mContext, utils)

        every {
            utils.createTrackingHelper(
                any(),
                testObject,
                configInfo,
                mContext,
                videoFrameLayout,
                testObject
            )
        } returns trackingHelper
        every {
            utils.createPostTvPlayerImpl(
                configInfo,
                testObject,
                trackingHelper
            )
        } returns postTvPlayerImpl
        mockkObject(DependencyFactory)
        every { DependencyFactory.createBuildVersionProvider() } returns buildVersionProvider
        every { buildVersionProvider.sdkInt() } returns Build.VERSION_CODES.O
    }

    @Test
    fun `initMediaPlayer sets configInfo`() {
        testObject.initMediaPlayer(configInfo)
        assertEquals(configInfo, testObject.configInfo)
    }

    @Test
    fun `initMedia(video) throws exception when configInfo is null`() {
        assertThrows(ArcXPException::class.java) {
            testObject.initMedia(mockk<ArcVideo>())
        }
    }

    @Test
    fun `initMedia(video) given adConfig is null, mVideoPlayer null, not youtube, not legacy player`() {
        val video = TestUtils.createDefaultVideo()

        testObject.initMediaPlayer(configInfo)
        clearAllMocks(answers = false)
        testObject.initMedia(video)

        assertEquals(expectedAdUrl, video.adTagUrl)
        assertTrue(video.shouldPlayAds)
        assertTrue(video.autoStartPlay)
        assertFalse(video.startMuted)
        verify {
            postTvPlayerImpl.playVideo(video)
        }
        assertEquals(123L, testObject.getSavedPosition("id"))
        assertTrue(testObject.mIsPlaying())
    }

    @Test
    fun `initMedia(video) given adConfig is not null`() {
        val video = TestUtils.createDefaultVideo()
        every { configInfo.adConfig } returns mockAdConfig

        testObject.initMediaPlayer(configInfo)
        clearAllMocks(answers = false)
        testObject.initMedia(video)

        assertEquals(expectedAdUrl, video.adTagUrl)
        assertTrue(video.shouldPlayAds)
    }

    @Test
    fun `initMedia(video) given player not null, is pip calls release on player`() {
        val video = TestUtils.createDefaultVideo()
        every { configInfo.adConfig } returns mockAdConfig


        testObject.initMediaPlayer(configInfo)
        clearAllMocks(answers = false)
        testObject.initMedia(video)
        testObject.setIsInPIP(true)
        testObject.initMedia(video)

        verify {
            postTvPlayerImpl.release()
        }
    }

    @Test
    fun `initMedia(video) given player not null, is not pip calls release`() {
        val video = TestUtils.createDefaultVideo()
        every { configInfo.adConfig } returns mockAdConfig



        testObject.initMediaPlayer(configInfo)
        clearAllMocks(answers = false)
        testObject.initMedia(video)
        testObject.initMedia(video)

        verify {
            postTvPlayerImpl.release()
        }
    }

    @Test
    fun `initMedia(videoStream) throws exception when configInfo is null`() {
        assertThrows(ArcXPException::class.java) {
            testObject.initMedia(videoStream)
        }
    }

    @Test
    fun `initMedia(videoStream), misLive false`() {


        val video = mockk<ArcVideo>(relaxed = true) {
            every { id } returns "url"
            every { bestStream } returns mockBestStream
            every { isLive } returns false
        }
        val errorListener = mockk<ArcVideoSDKErrorListener>(relaxed = true)
        every { videoAdData.error } returns mockk {
            every { message } returns "message"
        }
        mockkObject(AdUtils)
        every {
            getVideoManifest(
                videoStream,
                mockBestStream,
                configInfo
            )
        } returns videoAdData
        every { enableServerSideAds(any(), any()) } returns false

        every { builder.build() } returns video
        every {
            utils.createTrackingHelper(
                "url",
                testObject,
                configInfo,
                mContext,
                videoFrameLayout,
                testObject
            )
        } returns trackingHelper

        testObject.initMediaPlayer(configInfo)
        testObject.setErrorListener(errorListener)
        testObject.initMedia(videoStream)

        verify {
            video.autoStartPlay = true
            postTvPlayerImpl.playVideo(video)
        }

    }

    @Test
    fun `initMedia(videoStream), misLive true calls error since doesn't have all ad properties`() {


        val video =
            TestUtils.createDefaultVideo(id = "url", isLive = true, bestStream = mockBestStream)
        val errorListener = mockk<ArcVideoSDKErrorListener>(relaxed = true)
        mockkObject(AdUtils)
        every {
            getVideoManifest(
                videoStream,
                mockBestStream,
                configInfo
            )
        } returns videoAdData


        every { builder.build() } returns video
        every {
            utils.createTrackingHelper(
                "url",
                testObject,
                configInfo,
                mContext,
                videoFrameLayout,
                testObject
            )
        } returns trackingHelper
        testObject.initMediaPlayer(configInfo)
        testObject.setErrorListener(errorListener)
        testObject.initMedia(videoStream)
        clearAllMocks(answers = false)
        testObject.initMedia(videoStream)

        assertEquals(expectedManifestUrl, video.id)
        verify(exactly = 1) {
            errorListener.onError(
                ArcVideoSDKErrorType.VIDEO_STREAM_DATA_ERROR,
                expectedAdError,
                videoStream
            )
        }
    }

    @Test
    fun `initMedia(arcVideoStreamVirtualChannel) throws exception when configInfo is null`() {
        assertThrows(ArcXPException::class.java) {
            testObject.initMedia(mockk<ArcVideoStreamVirtualChannel>())
        }
    }

    @Test
    fun `initMedia(arcVideoStreamVirtualChannel) plays video from virtual channel (ads not enabled)`() {
        val arcVideoStreamVirtualChannel = mockk<ArcVideoStreamVirtualChannel> {
            every { url } returns expectedUrl
        }
        val video = TestUtils.createDefaultVideo(id = expectedUrl)
        mockkConstructor(ArcVideo.Builder::class)
        every {
            anyConstructed<ArcVideo.Builder>().setVideoStreamVirtual(expectedUrl, configInfo)
        } returns builder
        every { configInfo.isEnableAds } returns false
        every { builder.build() } returns video
        every {
            utils.createTrackingHelper(
                expectedUrl,
                testObject,
                configInfo,
                mContext,
                videoFrameLayout,
                testObject
            )
        } returns trackingHelper



        testObject.initMediaPlayer(configInfo)
        testObject.initMedia(arcVideoStreamVirtualChannel)
        verify { postTvPlayerImpl.playVideo(video) }
    }

    @Test
    fun `initMedia(arcVideoStreamVirtualChannel) plays video from virtual channel (ads enabled)`() {
        val video = TestUtils.createDefaultVideo(id = expectedUrl)
        mockkConstructor(ArcVideo.Builder::class)
        every {
            anyConstructed<ArcVideo.Builder>().setVideoStreamVirtual(expectedAdUrl, configInfo)
        } returns builder
        every { configInfo.isEnableAds } returns true
        every { builder.build() } returns video
        every {
            utils.createTrackingHelper(
                expectedUrl,
                testObject,
                configInfo,
                mContext,
                videoFrameLayout,
                testObject
            )
        } returns trackingHelper
        mockkObject(AdUtils.Companion)
        every {
            getVideoManifest(
                expectedAdUrl,
                configInfo
            )
        } returns mockk {
            every { manifestUrl } returns "expected new url"
        }
        val inputStream = mockk<ArcVideoStreamVirtualChannel> {
            every { adSettings } returns mockk {
                every { url } returns expectedAdUrl
            }
        }


        testObject.initMediaPlayer(configInfo)
        testObject.initMedia(inputStream)
        verifySequence {
            getVideoManifest(expectedAdUrl, configInfo)
            postTvPlayerImpl.playVideo(video)
        }
        assertEquals("expected new url", video.id)
    }

    @Test
    fun `initMedia (stream, adurl) throws exception if configInfo is null`() {
        assertThrows(ArcXPException::class.java) {
            testObject.initMedia(mockk(), "ad url")
        }
    }

    @Test
    fun `initMedia (stream, adurl) when misLive is false`() {


        val video = mockk<ArcVideo>(relaxed = true)
        every { video.id } returns "id"
        every { video.bestStream } returns mockBestStream
        every { video.isLive } returns false
        every { enableServerSideAds(any(), any()) } returns false

        every { videoAdData.error } returns mockk {
            every { message } returns "message"
        }

        every { builder.build() } returns video


        testObject.initMediaPlayer(configInfo)
        testObject.initMedia(videoStream, "ad url")

        verify {
            video.autoStartPlay = true
            videoStream.adTagUrl = "ad url"
            postTvPlayerImpl.playVideo(video)
            enableServerSideAds(videoStream, mockBestStream)
        }


    }

    @Test
    fun `initMedia (stream, adurl) when misLive is true`() {
        val video =
            TestUtils.createDefaultVideo(id = "url", isLive = true, bestStream = mockBestStream)
        val errorListener = mockk<ArcVideoSDKErrorListener>(relaxed = true)
        mockkObject(AdUtils.Companion)
        every {
            getVideoManifest(
                videoStream,
                mockBestStream,
                configInfo
            )
        } returns videoAdData
        every { enableServerSideAds(any(), any()) } returns false

        every { builder.build() } returns video
        every {
            utils.createTrackingHelper(
                any(),
                testObject,
                configInfo,
                mContext,
                videoFrameLayout,
                testObject
            )
        } returns trackingHelper

        testObject.setErrorListener(errorListener)
        testObject.initMediaPlayer(configInfo)
        testObject.initMedia(videoStream, "ad url")
        clearAllMocks(answers = false)
        testObject.initMedia(videoStream, "ad url")

        verify {
            video.autoStartPlay = true
            videoStream.adTagUrl = "ad url"
            postTvPlayerImpl.playVideo(video)
            enableServerSideAds(videoStream, mockBestStream)
            errorListener.onError(
                ArcVideoSDKErrorType.VIDEO_STREAM_DATA_ERROR,
                expectedAdError,
                videoStream
            )

        }
        assertEquals(expectedManifestUrl, video.id)


    }


    @Test
    fun `toggleOptionViews given true with activity`() {
        val view1 = mockk<View>(relaxed = true)
        val view2 = mockk<View>(relaxed = true)
        val mockActionBar = mockk<ActionBar>(relaxed = true)
        val mockActivity = mockk<Activity> {
            every { actionBar } returns mockActionBar
        }
        configInfo.apply {
            every { viewsToHide } returns listOf(view1, view2)
            every { activity } returns mockActivity
        }
        testObject.initMediaPlayer(configInfo)

        testObject.toggleOptionalViews(true)

        verifySequence {
            view1.visibility = View.VISIBLE
            view2.visibility = View.VISIBLE
            mockActionBar.show()
        }
    }

    @Test
    fun `toggleOptionViews given true with support activity`() {
        val view1 = mockk<View>(relaxed = true)
        val view2 = mockk<View>(relaxed = true)
        val mockSupportActionBar = mockk<androidx.appcompat.app.ActionBar>(relaxed = true)
        val mockSupportActivity = mockk<AppCompatActivity> {
            every { actionBar } returns null
            every { supportActionBar } returns mockSupportActionBar
        }
        configInfo.apply {
            every { viewsToHide } returns listOf(view1, view2)
            every { activity } returns mockSupportActivity
        }
        testObject.initMediaPlayer(configInfo)

        testObject.toggleOptionalViews(true)

        verifySequence {
            view1.visibility = View.VISIBLE
            view2.visibility = View.VISIBLE
            mockSupportActionBar.show()
        }
    }

    @Test
    fun `toggleOptionViews given false with activity`() {
        val view1 = mockk<View>(relaxed = true)
        val view2 = mockk<View>(relaxed = true)
        val mockActionBar = mockk<ActionBar>(relaxed = true)
        val mockActivity = mockk<Activity> {
            every { actionBar } returns mockActionBar
        }
        configInfo.apply {
            every { viewsToHide } returns listOf(view1, view2)
            every { activity } returns mockActivity
        }
        testObject.initMediaPlayer(configInfo)

        testObject.toggleOptionalViews(false)

        verifySequence {
            view1.visibility = View.GONE
            view2.visibility = View.GONE
            mockActionBar.hide()
        }
    }

    @Test
    fun `toggleOptionViews given false with support activity`() {
        val view1 = mockk<View>(relaxed = true)
        val view2 = mockk<View>(relaxed = true)
        val mockSupportActionBar = mockk<androidx.appcompat.app.ActionBar>(relaxed = true)
        val mockSupportActivity = mockk<AppCompatActivity> {
            every { actionBar } returns null
            every { supportActionBar } returns mockSupportActionBar
        }
        configInfo.apply {
            every { viewsToHide } returns listOf(view1, view2)
            every { activity } returns mockSupportActivity
        }
        testObject.initMediaPlayer(configInfo)

        testObject.toggleOptionalViews(false)

        verifySequence {
            view1.visibility = View.GONE
            view2.visibility = View.GONE
            mockSupportActionBar.hide()
        }
    }

    @Test
    fun `onResume resumes castManager if not null`() {
        val castManager = mockk<ArcCastManager> {
            every { onResume() } just Runs
        }
        every { configInfo.arcCastManager } returns castManager

        testObject.initMediaPlayer(configInfo)

        testObject.onResume()

        verifySequence {
            castManager.onResume()
        }
    }

    @Test
    fun `onDestroy call cast manager and tracking helper when populated`() {
        val castManager = mockk<ArcCastManager>(relaxed = true)
        every { configInfo.arcCastManager } returns castManager

        testObject.initMediaPlayer(configInfo)
        testObject.initMedia(TestUtils.createDefaultVideo())
        clearAllMocks(answers = false)

        testObject.onDestroy()

        verifySequence {
            castManager.onDestroy()
            trackingHelper.onDestroy()
        }
    }

    @Test
    fun `onStop sets isPIPStopRequest to true`() {
        testObject.onStop()
        assertTrue(testObject.isPipStopRequest)
    }

    @Test
    fun `onPause sets pause on castManger`() {
        val castManager = mockk<ArcCastManager>(relaxed = true)
        every { configInfo.arcCastManager } returns castManager
        testObject.initMediaPlayer(configInfo)

        testObject.onPause()

        verifySequence {
            castManager.onPause()
        }
    }

    @Test
    fun `getCastManager returns castManager from configInfo`() {
        val castManager = mockk<ArcCastManager>(relaxed = true)
        every { configInfo.arcCastManager } returns castManager
        testObject.initMediaPlayer(configInfo)

        assertEquals(castManager, testObject.castManager)
    }

    @Test
    fun `getOverlay returns null when no videoPlayer`() {
        assertNull(testObject.getOverlay("tag"))
    }

    @Test
    fun `getOverlay calls videoPlayer with tag, returns item when found by player`() {
        val expectedView = mockk<View>()
        every {
            postTvPlayerImpl.getOverlay("tag")
        } returns expectedView
        initPlayer()

        assertEquals(expectedView, testObject.getOverlay("tag"))
    }

    @Test
    fun `getCurrentTimelinePosition returns -1 when no videoPlayer`() {
        assertEquals(-1, testObject.currentTimelinePosition)
    }

    @Test
    fun `getCurrentTimelinePosition calls videoPlayer, returns current timeline position from player`() {
        every {
            postTvPlayerImpl.getCurrentTimelinePosition()
        } returns 123L
        initPlayer()

        assertEquals(123L, testObject.currentTimelinePosition)
    }

    @Test
    fun `getPlayheadPosition returns -1 when no videoPlayer`() {
        assertEquals(-1, testObject.playheadPosition)
    }

    @Test
    fun `getPlayheadPosition calls videoPlayer, returns current position from player`() {
        every {
            postTvPlayerImpl.getCurrentPosition()
        } returns 1234L
        initPlayer()

        assertEquals(1234L, testObject.playheadPosition)
    }

    @Test
    fun `getPlaybackState returns 0 when no videoPlayer`() {
        assertEquals(0, testObject.playbackState)
    }

    @Test
    fun `getPlaybackState calls videoPlayer, returns playback state from player`() {
        every {
            postTvPlayerImpl.getPlaybackState()
        } returns 333

        initPlayer()

        assertEquals(333, testObject.playbackState)
    }

    @Test
    fun `setVolume sets volume on player when populated`() {
        initPlayer()

        testObject.setVolume(0.123f)

        verifySequence { postTvPlayerImpl.setVolume(0.123f) }
    }

    @Test
    fun `seekTo seeks to position on player when populated`() {
        initPlayer()

        testObject.seekTo(565656)

        verifySequence { postTvPlayerImpl.seekTo(565656) }
    }

    @Test
    fun `resumePlay calls resume on player when populated`() {
        initPlayer()

        testObject.resumePlay()

        verifySequence { postTvPlayerImpl.resume() }
    }

    @Test
    fun `pausePlay calls pause on player when populated`() {
        initPlayer()

        testObject.pausePlay()

        verifySequence { postTvPlayerImpl.pause() }
    }

    @Test
    fun `pausePlay(boolean) calls pause on player with value when populated`() {
        initPlayer()

        testObject.pausePlay(true)

        verifySequence { postTvPlayerImpl.pausePlay(true) }
    }

    @Test
    fun `stopPlay calls stop on player when populated`() {
        initPlayer()

        testObject.stopPlay()

        verifySequence { postTvPlayerImpl.stop() }
    }

    @Test
    fun `startPlay calls start on player when populated`() {
        initPlayer()

        testObject.startPlay()

        verifySequence { postTvPlayerImpl.start() }
    }

    @Test
    fun `setPlayerKeyListener sets listener on player when populated`() {
        initPlayer()
        val arcKeyListener = mockk<ArcKeyListener>()
        every { postTvPlayerImpl.setPlayerKeyListener(any()) } just Runs
        testObject.setPlayerKeyListener(arcKeyListener)

        verifySequence { postTvPlayerImpl.setPlayerKeyListener(arcKeyListener) }
    }

    @Test
    fun `setFullscreenListener sets listener on player when populated`() {
        initPlayer()
        val arcKeyListener = mockk<ArcKeyListener>()
        every { postTvPlayerImpl.setFullscreenListener(any()) } just Runs
        testObject.setFullscreenListener(arcKeyListener)

        verifySequence { postTvPlayerImpl.setFullscreenListener(arcKeyListener) }
    }

    @Test
    fun `setFullScreen when using full screen dialog`() {
        initPlayer()
        every { configInfo.isUseFullScreenDialog } returns true

        testObject.setFullscreen(true)
        verifySequence { postTvPlayerImpl.setFullscreen(true) }
    }

    @Test
    fun `setFullScreen given false, when using not full screen dialog`() {
        initPlayer()
        every { configInfo.isUseFullScreenDialog } returns false
        val mockDecorView = mockk<View>(relaxed = true)
        val mockLayoutParams = ViewGroup.LayoutParams(22, 23)
        val layoutParamSlot = slot<ViewGroup.LayoutParams>()
        val mockActivity = mockk<Activity>(relaxed = true) {
            every { window } returns mockk {
                every { decorView } returns mockDecorView
            }
        }
        val mockVideoFrame = mockk<ArcVideoFrame>(relaxed = true) {
            every { layoutParams } returns mockLayoutParams
        }
        every { configInfo.activity } returns mockActivity
        every { configInfo.videoFrame } returns mockVideoFrame

        testObject.setFullscreen(false)
        verifySequence {
            mockActivity.window
            mockDecorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
            mockActivity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR
            mockVideoFrame.layoutParams
            mockVideoFrame.layoutParams = capture(layoutParamSlot)
        }
        assertEquals(0, layoutParamSlot.captured.width)
        assertEquals(0, layoutParamSlot.captured.height)
    }

    @Test
    fun `setFullScreen given true, when using not full screen dialog`() {
        initPlayer()
        every { configInfo.isUseFullScreenDialog } returns false
        val mockDecorView = mockk<View>(relaxed = true)
        val mockLayoutParams = ViewGroup.LayoutParams(22, 23)
        val layoutParamSlot = slot<ViewGroup.LayoutParams>()
        val mockActivity = mockk<Activity>(relaxed = true) {
            every { window } returns mockk {
                every { decorView } returns mockDecorView
            }
        }
        val mockVideoFrame = mockk<ArcVideoFrame>(relaxed = true) {
            every { layoutParams } returns mockLayoutParams
        }
        every { configInfo.activity } returns mockActivity
        every { configInfo.videoFrame } returns mockVideoFrame

        testObject.setFullscreen(true)
        verifySequence {
            mockActivity.window
            mockDecorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)
            mockActivity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            mockVideoFrame.layoutParams
            mockVideoFrame.layoutParams = capture(layoutParamSlot)
        }
        assertEquals(ViewGroup.LayoutParams.MATCH_PARENT, layoutParamSlot.captured.width)
        assertEquals(
            ViewGroup.LayoutParams.MATCH_PARENT,
            layoutParamSlot.captured.height
        )
    }

    @Test
    fun `setFullscreen sets oldHeight and oldWidth, uses it to set layout parameters`() {
        initPlayer()
        every { configInfo.isUseFullScreenDialog } returns false
        val mockDecorView = mockk<View>(relaxed = true)
        val mockLayoutParams = ViewGroup.LayoutParams(22, 23)
        mockLayoutParams.height = 23
        mockLayoutParams.width = 22

        val layoutParamSlot = slot<ViewGroup.LayoutParams>()
        val mockActivity = mockk<Activity>(relaxed = true) {
            every { window } returns mockk {
                every { decorView } returns mockDecorView
            }
        }
        val mockVideoFrame = mockk<ArcVideoFrame>(relaxed = true) {
            every { layoutParams } returns mockLayoutParams
        }
        every { configInfo.activity } returns mockActivity
        every { configInfo.videoFrame } returns mockVideoFrame
        testObject.setFullscreen(true)
        clearAllMocks(answers = false)
        testObject.setFullscreen(false)
        verify {
            mockVideoFrame.layoutParams = capture(layoutParamSlot)
        }
        assertEquals(22, layoutParamSlot.captured.width)
        assertEquals(23, layoutParamSlot.captured.height)
    }

    private fun initPlayer() {
        every { configInfo.adConfig } returns mockAdConfig
        testObject.initMediaPlayer(configInfo)
        testObject.initMedia(TestUtils.createDefaultVideo())
        clearAllMocks(answers = false)
    }

    @Test
    fun `isFullScreen returns false when mVideoPlayer is null`() {
        assertFalse(testObject.isFullScreen)
    }

    @Test
    fun `isFullScreen returns value from player`() {
        initPlayer()
        assertFalse(testObject.isFullScreen)
        every { postTvPlayerImpl.isFullScreen() } returns true
        assertTrue(testObject.isFullScreen)
    }

    @Test
    fun `isShowClosedCaptionDialog returns value from configInfo`() {
        initPlayer()
        assertFalse(testObject.isShowClosedCaptionDialog)
        every { configInfo.isShowClosedCaptionTrackSelection } returns true
        assertTrue(testObject.isShowClosedCaptionDialog)
    }

    @Test
    fun `setCcButtonDrawable returns false if player null`() {
        assertFalse(testObject.setCcButtonDrawable(123))
    }

    @Test
    fun `setCcButtonDrawable returns player result`() {
        initPlayer()
        assertFalse(testObject.setCcButtonDrawable(123))
        every { postTvPlayerImpl.setCcButtonDrawable(123) } returns true
        assertTrue(testObject.setCcButtonDrawable(123))
    }

    @Test
    fun `enableClosedCaption returns false if player null`() {
        assertFalse(testObject.enableClosedCaption(true))
        assertFalse(testObject.enableClosedCaption(false))
    }

    @Test
    fun `enableClosedCaption returns player result`() {
        initPlayer()
        assertFalse(testObject.enableClosedCaption(false))
        every { postTvPlayerImpl.enableClosedCaption(false) } returns true
        assertTrue(testObject.enableClosedCaption(false))
    }

    @Test
    fun `isClosedCaptionAvailable returns false if player null`() {
        assertFalse(testObject.isClosedCaptionAvailable)
    }

    @Test
    fun `isClosedCaptionAvailable returns player result`() {
        initPlayer()
        assertFalse(testObject.isClosedCaptionAvailable)
        every { postTvPlayerImpl.isClosedCaptionAvailable() } returns true
        assertTrue(testObject.isClosedCaptionAvailable)
    }

    @Test
    fun `isClosedCaptionTurnedOn returns false if player null`() {
        assertFalse(testObject.isClosedCaptionTurnedOn)
    }

    @Test
    fun `isClosedCaptionTurnedOn returns player result`() {
        initPlayer()
        assertFalse(testObject.isClosedCaptionTurnedOn)
        every { postTvPlayerImpl.isClosedCaptionVisibleAndOn() } returns true
        assertTrue(testObject.isClosedCaptionTurnedOn)
    }

    @Test
    fun `isClosedCaptionVisible returns false if player null`() {
        assertFalse(testObject.isClosedCaptionVisible)
    }

    @Test
    fun `isClosedCaptionVisible returns player result`() {
        initPlayer()
        assertFalse(testObject.isClosedCaptionVisible)
        every { postTvPlayerImpl.isClosedCaptionVisibleAndOn() } returns true
        assertTrue(testObject.isClosedCaptionVisible)
    }

    @Test
    fun `getCurrentVideoDuration returns 0 if player null`() {
        assertEquals(0, testObject.currentVideoDuration)
    }

    @Test
    fun `getCurrentVideoDuration returns player result`() {
        initPlayer()
        assertEquals(0, testObject.currentVideoDuration)
        every { postTvPlayerImpl.getCurrentVideoDuration() } returns 777L
        assertEquals(777L, testObject.currentVideoDuration)
    }

    @Test
    fun `isControlsVisible returns false if player null`() {
        assertFalse(testObject.isControlsVisible)
    }

    @Test
    fun `isControlsVisible returns player result`() {
        initPlayer()
        assertFalse(testObject.isControlsVisible)
        every { postTvPlayerImpl.isControllerFullyVisible() } returns true
        assertTrue(testObject.isControlsVisible)
    }

    @Test
    fun `hideControls sets showControls to false on player`() {
        initPlayer()
        testObject.hideControls()
        verifySequence {
            postTvPlayerImpl.showControls(false)
        }
    }

    @Test
    fun `showControls sets showControls to true on player`() {
        initPlayer()
        testObject.showControls()
        verifySequence {
            postTvPlayerImpl.showControls(true)
        }
    }

    @Test
    fun `toggleAutoShow sets toggleAutoShow to true on player`() {
        initPlayer()
        testObject.toggleAutoShow(true)
        verifySequence {
            postTvPlayerImpl.toggleAutoShow(true)
        }
    }

    @Test
    fun `null player for method calls`() {
        testObject.toggleAutoShow(true)
        testObject.hideControls()
        testObject.showControls()
    }

    @Test
    fun `getAdType returns value from player`() {
        initPlayer()
        every { postTvPlayerImpl.getAdType() } returns 555L
        assertEquals(555L, testObject.adType)
    }

    @Test
    fun `isInPip returns previously set boolean`() {
        testObject.setIsInPIP(true)
        assertTrue(testObject.isInPIP)
        testObject.setIsInPIP(false)
        assertFalse(testObject.isInPIP)
    }

    @Test
    fun `getVideo returns video from player`() {
        val expected = mockk<ArcVideo>()
        every { postTvPlayerImpl.getVideo() } returns expected
        initPlayer()
        assertEquals(expected, testObject.video)
    }

    @Test
    fun `getVideoURl returns video id from player`() {
        val expected = "23789"
        val video = TestUtils.createDefaultVideo(id = expected)
        every { postTvPlayerImpl.getVideo() } returns video

        initPlayer()

        assertEquals(expected, testObject.videoURl)
    }

    @Test
    fun `onActivityResume calls same function in player if populated`() {
        initPlayer()

        testObject.onActivityResume()

        verifySequence {
            postTvPlayerImpl.onActivityResume()
        }
    }

    @Test
    fun `getId returns null if player is null`() {
        assertNull(testObject.id)
    }

    @Test
    fun `getId returns value from player if populated`() {
        val expected = "id101"
        initPlayer()
        every { postTvPlayerImpl.getId() } returns expected
        assertEquals(expected, testObject.id)
    }

    @Test
    fun `onError given not sticky, not pip, frame layout is view group`() {
        val errorListener = mockk<ArcVideoSDKErrorListener>(relaxed = true)
        val videoFrameParent = mockk<ViewGroup>(relaxed = true)
        every { videoFrameLayout.parent } returns videoFrameParent
        initPlayer()
        testObject.setErrorListener(errorListener)
        val value = Exception(expectedAdError)
        val viewGroup = mockk<ViewGroup>(relaxed = true)
        every { mMessageOverlay.parent } returns viewGroup
        every { mContext.getString(R.string.source_error) } returns "error from resource"

        testObject.onError(ArcVideoSDKErrorType.SERVER_ERROR, "errorMessage", value)

        verify {
            mMessageText.text = "errorMessage"
            viewGroup.removeView(mMessageOverlay)
            videoFrameParent.addView(mMessageOverlay)
            errorListener.onError(
                ArcVideoSDKErrorType.SERVER_ERROR,
                expectedAdError,
                value
            )
        }
    }

    @Test
    fun `onError with error overlay disabled`() {
        val errorListener = mockk<ArcVideoSDKErrorListener>(relaxed = true)
        val videoFrameParent = mockk<ViewGroup>(relaxed = true)
        every { videoFrameLayout.parent } returns videoFrameParent
        every { configInfo.disableErrorOverlay } returns true
        initPlayer()
        testObject.setErrorListener(errorListener)
        val value = Exception(expectedAdError)
        val viewGroup = mockk<ViewGroup>(relaxed = true)
        every { mMessageOverlay.parent } returns viewGroup
        every { mContext.getString(R.string.source_error) } returns "error from resource"

        testObject.onError(ArcVideoSDKErrorType.SERVER_ERROR, "errorMessage", value)

        verify(exactly = 0) {
            mMessageText.text = "errorMessage"
            viewGroup.removeView(mMessageOverlay)
            videoFrameParent.addView(mMessageOverlay)
        }
        verifySequence {
            configInfo.disableErrorOverlay
            errorListener.onError(
                ArcVideoSDKErrorType.SERVER_ERROR,
                expectedAdError,
                value
            )
        }
    }

    @Test
    fun `addVideoView adds input to frame layout`() {
        initPlayer()
        val viewGroup = mockk<ViewGroup>(relaxed = true)
        val view = mockk<View> {
            every { parent } returns viewGroup
        }
        testObject.addVideoView(view)
        verifySequence {
            viewGroup.removeView(view)
            testObject.playerFrame.addView(view)
        }
    }

    @Test
    fun `getPlayWhenReadyState returns false if player null`() {
        assertFalse(testObject.playWhenReadyState)
    }

    @Test
    fun `getPlayWhenReadyState returns value from player if populated`() {
        initPlayer()
        assertFalse(testObject.playWhenReadyState)
        every { postTvPlayerImpl.getPlayWhenReadyState() } returns true
        assertTrue(testObject.playWhenReadyState)
    }

    @Test
    fun `onTrackingEvent ON_PLAY_STARTED`() {
        testObject = spyk(testObject)
        val timerTask = mockk<TimerTask>()
        every { testObject.createTimerTask() } returns timerTask
        val type = TrackingType.ON_PLAY_STARTED
        val trackingData = mockk<TrackingTypeData.TrackingVideoTypeData>(relaxed = true)


        val video =
            TestUtils.createDefaultVideo(isLive = true, bestStream = mockBestStream)



        every { builder.build() } returns video
        every {
            utils.createTrackingHelper(
                any(),
                testObject,
                configInfo,
                mContext,
                videoFrameLayout,
                testObject
            )
        } returns trackingHelper
        every {
            utils.createPostTvPlayerImpl(
                configInfo,
                testObject,
                trackingHelper
            )
        } returns postTvPlayerImpl
        every { postTvPlayerImpl.getVideo() } returns video
        testObject.setErrorListener(errorListener)
        testObject.initMediaPlayer(configInfo)
        testObject.initMedia(videoStream, "ad url")

        testObject.initEvents(eventTracker)
        clearAllMocks(answers = false)

        testObject.onTrackingEvent(type, trackingData)

        verify(exactly = 1) {
            testObject.onTrackingEvent(type, trackingData)
            Log.d("ArcVideoSDK", "onTrackingEvent ON_PLAY_STARTED at 0")
            testObject.createTimerTask()
            mTimer.schedule(timerTask, 2000L, expectedPollingDelay.toLong())
            trackingData.arcVideo = video
            trackingData.sessionId = "sessionId"
            eventTracker.onVideoTrackingEvent(type, trackingData)
        }
        assertTrue(testObject.isPlayStarted)
        assertTrue(testObject.mIsPlaying())
    }

    @Test
    fun `release cancels timer and purges`() {
        testObject = spyk(testObject)
        val timerTask = mockk<TimerTask>()
        every { testObject.createTimerTask() } returns timerTask
        val type = TrackingType.ON_PLAY_STARTED
        val trackingData = mockk<TrackingTypeData.TrackingVideoTypeData>(relaxed = true)

        val video =
            TestUtils.createDefaultVideo(isLive = true, bestStream = mockBestStream)

        every { builder.build() } returns video
        every {
            utils.createTrackingHelper(
                any(),
                testObject,
                configInfo,
                mContext,
                videoFrameLayout,
                testObject
            )
        } returns trackingHelper
        every {
            utils.createPostTvPlayerImpl(
                configInfo,
                testObject,
                trackingHelper
            )
        } returns postTvPlayerImpl
        every { postTvPlayerImpl.getVideo() } returns video
        testObject.setErrorListener(errorListener)
        testObject.initMediaPlayer(configInfo)
        testObject.initMedia(videoStream, "ad url")
        testObject.initEvents(eventTracker)
        testObject.onTrackingEvent(type, trackingData)
        clearAllMocks(answers = false)

        testObject.release()

        verify(exactly = 1) {

            mTimer.cancel()
            mTimer.purge()
        }
    }

    @Test
    fun `onTrackingEvent ON_PLAY_COMPLETED`() {
        testObject = spyk(testObject)

        val timerTask = mockk<TimerTask>()
        every { testObject.createTimerTask() } returns timerTask
        val type = TrackingType.ON_PLAY_COMPLETED
        val trackingData = mockk<TrackingTypeData.TrackingVideoTypeData>(relaxed = true) {
            every { position } returns expectedPosition
        }

        val video =
            TestUtils.createDefaultVideo(isLive = true, bestStream = mockBestStream)
        every { builder.build() } returns video
        every {
            utils.createTrackingHelper(
                expectedManifestUrl,
                testObject,
                configInfo,
                mContext,
                videoFrameLayout,
                testObject
            )
        } returns trackingHelper
        every {
            utils.createPostTvPlayerImpl(
                configInfo,
                testObject,
                trackingHelper
            )
        } returns postTvPlayerImpl
        every { postTvPlayerImpl.getVideo() } returns video
        every { postTvPlayerImpl.getId() } returns "id"
        testObject.setErrorListener(errorListener)
        testObject.initMediaPlayer(configInfo)
        testObject.initMedia(videoStream, "ad url")
        testObject.initEvents(eventTracker)


        testObject.onTrackingEvent(TrackingType.ON_PLAY_STARTED, trackingData)
        clearAllMocks(answers = false)

        testObject.onTrackingEvent(type, trackingData)

        verify {
            testObject.onTrackingEvent(type, trackingData)
            Log.d("ArcVideoSDK", "onTrackingEvent ON_PLAY_COMPLETED at 0")
            trackingData.position
            testObject.setSavedPosition("id", expectedPosition)
            mTimer.cancel()
            mTimer.purge()
            eventTracker.onVideoTrackingEvent(type, trackingData)
        }
        assertFalse(testObject.isPlayStarted)
        assertFalse(testObject.mIsPlaying())
        assertEquals(expectedPosition, testObject.getSavedPosition("id"))
    }

    @Test
    fun `onTrackingEvent VIDEO_PERCENTAGE_WATCHED is 25 percent`() {

        val type = TrackingType.VIDEO_PERCENTAGE_WATCHED
        val trackingData = mockk<TrackingTypeData.TrackingVideoTypeData>(relaxed = true) {
            every { position } returns expectedPosition
            every { percentage } returns 25
        }
        val video =
            TestUtils.createDefaultVideo(bestStream = mockBestStream)
        every { builder.build() } returns video
        every { postTvPlayerImpl.getVideo() } returns video
        every { postTvPlayerImpl.getId() } returns "id"
        testObject.setErrorListener(errorListener)
        testObject.initMediaPlayer(configInfo)
        testObject.initMedia(videoStream)
        testObject.initEvents(eventTracker)
        clearAllMocks(answers = false)

        testObject.onTrackingEvent(type, trackingData)

        verify {
            Log.d("ArcVideoSDK", "onTrackingEvent VIDEO_PERCENTAGE_WATCHED at 0")
            trackingData.percentage
            eventTracker.onVideoTrackingEvent(TrackingType.VIDEO_25_WATCHED, trackingData)
        }
    }

    @Test
    fun `onTrackingEvent VIDEO_PERCENTAGE_WATCHED is 50 percent`() {

        val type = TrackingType.VIDEO_PERCENTAGE_WATCHED
        val trackingData = mockk<TrackingTypeData.TrackingVideoTypeData>(relaxed = true) {
            every { position } returns expectedPosition
            every { percentage } returns 50
        }
        val video =
            TestUtils.createDefaultVideo(bestStream = mockBestStream)
        every { builder.build() } returns video
        every { postTvPlayerImpl.getVideo() } returns video
        every { postTvPlayerImpl.getId() } returns "id"
        testObject.setErrorListener(errorListener)
        testObject.initMediaPlayer(configInfo)
        testObject.initMedia(videoStream)
        testObject.initEvents(eventTracker)
        clearAllMocks(answers = false)

        testObject.onTrackingEvent(type, trackingData)

        verify {
            Log.d("ArcVideoSDK", "onTrackingEvent VIDEO_PERCENTAGE_WATCHED at 0")
            trackingData.percentage
            eventTracker.onVideoTrackingEvent(TrackingType.VIDEO_50_WATCHED, trackingData)
        }
    }

    @Test
    fun `onTrackingEvent VIDEO_PERCENTAGE_WATCHED is 75 percent`() {
        val type = TrackingType.VIDEO_PERCENTAGE_WATCHED
        val trackingData = mockk<TrackingTypeData.TrackingVideoTypeData>(relaxed = true) {
            every { position } returns expectedPosition
            every { percentage } returns 75
        }
        val video =
            TestUtils.createDefaultVideo(bestStream = mockBestStream)
        every { builder.build() } returns video
        every { postTvPlayerImpl.getVideo() } returns video
        every { postTvPlayerImpl.getId() } returns "id"
        testObject.setErrorListener(errorListener)
        testObject.initMediaPlayer(configInfo)
        testObject.initMedia(videoStream)
        testObject.initEvents(eventTracker)
        clearAllMocks(answers = false)

        testObject.onTrackingEvent(type, trackingData)

        verify {
            Log.d("ArcVideoSDK", "onTrackingEvent VIDEO_PERCENTAGE_WATCHED at 0")
            trackingData.percentage
            eventTracker.onVideoTrackingEvent(TrackingType.VIDEO_75_WATCHED, trackingData)
        }
    }

    @Test
    fun `onTrackingEvent AD_PLAY_STARTED is 0 percent`() {
        val type = TrackingType.AD_PLAY_STARTED
        val trackingData = mockk<TrackingTypeData.TrackingVideoTypeData>(relaxed = true) {
            every { position } returns expectedPosition
            every { percentage } returns 0
        }
        val video =
            TestUtils.createDefaultVideo(bestStream = mockBestStream)
        every { builder.build() } returns video
        every { postTvPlayerImpl.getVideo() } returns video
        every { postTvPlayerImpl.getId() } returns "id"
        testObject.setErrorListener(errorListener)
        testObject.initMediaPlayer(configInfo)
        testObject.initMedia(videoStream)
        testObject.initEvents(eventTracker)
        clearAllMocks(answers = false)

        testObject.onTrackingEvent(type, trackingData)

        verify {
            Log.d("ArcVideoSDK", "onTrackingEvent AD_PLAY_STARTED at 0")
            trackingData.percentage
            eventTracker.onVideoTrackingEvent(TrackingType.PREROLL_AD_STARTED, trackingData)
        }
    }

    @Test
    fun `onTrackingEvent AD_PLAY_STARTED is between 1 and 89 percent`() {
        val type = TrackingType.AD_PLAY_STARTED
        val trackingData = mockk<TrackingTypeData.TrackingVideoTypeData>(relaxed = true) {
            every { position } returns expectedPosition
            every { percentage } returns 89
        }
        val video =
            TestUtils.createDefaultVideo(bestStream = mockBestStream)
        every { builder.build() } returns video
        every { postTvPlayerImpl.getVideo() } returns video
        every { postTvPlayerImpl.getId() } returns "id"
        testObject.setErrorListener(errorListener)
        testObject.initMediaPlayer(configInfo)
        testObject.initMedia(videoStream)
        testObject.initEvents(eventTracker)
        clearAllMocks(answers = false)

        testObject.onTrackingEvent(type, trackingData)

        verify {
            Log.d("ArcVideoSDK", "onTrackingEvent AD_PLAY_STARTED at 0")
            trackingData.percentage
            eventTracker.onVideoTrackingEvent(TrackingType.MIDROLL_AD_STARTED, trackingData)
        }
    }

    @Test
    fun `onTrackingEvent AD_PLAY_STARTED is 90 percent`() {
        val type = TrackingType.AD_PLAY_STARTED
        val trackingData = mockk<TrackingTypeData.TrackingVideoTypeData>(relaxed = true) {
            every { position } returns expectedPosition
            every { percentage } returns 90
        }
        val video =
            TestUtils.createDefaultVideo(bestStream = mockBestStream)
        val errorListener = mockk<ArcVideoSDKErrorListener>(relaxed = true)
        every { builder.build() } returns video
        every { postTvPlayerImpl.getVideo() } returns video
        every { postTvPlayerImpl.getId() } returns "id"
        testObject.setErrorListener(errorListener)
        testObject.initMediaPlayer(configInfo)
        testObject.initMedia(videoStream)
        testObject.initEvents(eventTracker)
        clearAllMocks(answers = false)

        testObject.onTrackingEvent(type, trackingData)

        verify {
            Log.d("ArcVideoSDK", "onTrackingEvent AD_PLAY_STARTED at 0")
            trackingData.percentage
            eventTracker.onVideoTrackingEvent(TrackingType.POSTROLL_AD_STARTED, trackingData)
        }
    }

    @Test
    fun `onTrackingEvent AD_PLAY_COMPLETED is 0 percent`() {
        val type = TrackingType.AD_PLAY_COMPLETED
        val trackingData = mockk<TrackingTypeData.TrackingVideoTypeData>(relaxed = true) {
            every { position } returns expectedPosition
            every { percentage } returns 0
        }
        val video =
            TestUtils.createDefaultVideo(bestStream = mockBestStream)
        every { builder.build() } returns video
        every { postTvPlayerImpl.getVideo() } returns video
        every { postTvPlayerImpl.getId() } returns "id"
        testObject.setErrorListener(errorListener)
        testObject.initMediaPlayer(configInfo)
        testObject.initMedia(videoStream)
        testObject.initEvents(eventTracker)
        clearAllMocks(answers = false)

        testObject.onTrackingEvent(type, trackingData)

        verify {
            Log.d("ArcVideoSDK", "onTrackingEvent AD_PLAY_COMPLETED at 0")
            trackingData.percentage
            eventTracker.onVideoTrackingEvent(TrackingType.PREROLL_AD_COMPLETED, trackingData)
        }
    }

    @Test
    fun `onTrackingEvent AD_PLAY_COMPLETED is between 1 and 99 percent`() {
        val type = TrackingType.AD_PLAY_COMPLETED
        val trackingData = mockk<TrackingTypeData.TrackingVideoTypeData>(relaxed = true) {
            every { position } returns expectedPosition
            every { percentage } returns 1
        }
        val video =
            TestUtils.createDefaultVideo(bestStream = mockBestStream)
        every { builder.build() } returns video
        every { postTvPlayerImpl.getVideo() } returns video
        every { postTvPlayerImpl.getId() } returns "id"
        testObject.setErrorListener(errorListener)
        testObject.initMediaPlayer(configInfo)
        testObject.initMedia(videoStream)
        testObject.initEvents(eventTracker)
        clearAllMocks(answers = false)

        testObject.onTrackingEvent(type, trackingData)

        verify {
            Log.d("ArcVideoSDK", "onTrackingEvent AD_PLAY_COMPLETED at 0")
            trackingData.percentage
            eventTracker.onVideoTrackingEvent(TrackingType.MIDROLL_AD_COMPLETED, trackingData)
        }
    }

    @Test
    fun `onTrackingEvent AD_PLAY_COMPLETED is 100 percent`() {
        val type = TrackingType.AD_PLAY_COMPLETED
        val trackingData = mockk<TrackingTypeData.TrackingVideoTypeData>(relaxed = true) {
            every { position } returns expectedPosition
            every { percentage } returns 100
        }
        val eventTracker = mockk<ArcVideoEventsListener>(relaxed = true)
        val video =
            TestUtils.createDefaultVideo(bestStream = mockBestStream)
        every { builder.build() } returns video
        every { postTvPlayerImpl.getVideo() } returns video
        every { postTvPlayerImpl.getId() } returns "id"
        testObject.setErrorListener(errorListener)
        testObject.initMediaPlayer(configInfo)
        testObject.initMedia(videoStream)
        testObject.initEvents(eventTracker)
        clearAllMocks(answers = false)

        testObject.onTrackingEvent(type, trackingData)

        verify {
            Log.d("ArcVideoSDK", "onTrackingEvent AD_PLAY_COMPLETED at 0")
            trackingData.percentage
            eventTracker.onVideoTrackingEvent(TrackingType.POSTROLL_AD_COMPLETED, trackingData)
        }
    }

    @Test
    fun `isPipEnabled returns config value true when pip is supported`() {
        initPlayer()
        assertFalse(testObject.isPipEnabled)
        every { configInfo.isEnablePip } returns true
        assertTrue(testObject.isPipEnabled)
    }

    @Test
    fun `isPipEnabled returns config value false when pip is supported`() {
        initPlayer()
        assertFalse(testObject.isPipEnabled)
        every { configInfo.isEnablePip } returns false
        assertFalse(testObject.isPipEnabled)
    }

    @Test
    fun `isPipEnabled returns false when config true but pip is not supported`() {
        every { buildVersionProvider.sdkInt() } returns Build.VERSION_CODES.M
        initPlayer()
        assertFalse(testObject.isPipEnabled)
        every { configInfo.isEnablePip } returns true
        assertTrue(testObject.isPipEnabled)
    }

//    @Test
//    fun `enableClosedCaption returns config value`() {
//        initPlayer()
//        assertFalse(testObject.enableClosedCaption())
//        every { configInfo.enableClosedCaption() } returns true
//        assertTrue(testObject.enableClosedCaption())
//    }//TODO update

    @Test
    fun `isPlaying returns false if mVideoPlayer is null`() {
        assertFalse(testObject.isPlaying)
    }

    @Test
    fun `isPlaying returns value from player if populated`() {
        initPlayer()
        every { postTvPlayerImpl.isPlaying() } returns true
        assertTrue(testObject.isPlaying)
    }

    @Test
    fun `addVideo(stream) throws exception if config is null`() {
        assertThrows(ArcXPException::class.java) {
            testObject.addVideo(mockk())
        }
    }

    @Test
    fun `addVideo(stream, string) throws exception if config is null`() {
        assertThrows(ArcXPException::class.java) {
            testObject.addVideo(mockk(), "")
        }
    }

    @Test
    fun `addVideo(stream) throws exception if mVideoPlayer is null`() {
        testObject.initMediaPlayer(configInfo)
        assertThrows(ArcXPException::class.java) {
            testObject.addVideo(mockk())
        }
    }

    @Test
    fun `addVideo(stream, string) throws mVideoPlayer if config is null`() {
        testObject.initMediaPlayer(configInfo)
        assertThrows(ArcXPException::class.java) {
            testObject.addVideo(mockk(), "")
        }
    }

    @Test
    fun `addVideo(stream, string) adds video to player`() {
        val video = mockk<ArcVideo>()

        every { builder.build() } returns video
        initPlayer()

        testObject.addVideo(videoStream, expectedAdUrl)

        verify(exactly = 1) {
            videoStream.adTagUrl = expectedAdUrl
            postTvPlayerImpl.addVideo(video)
        }
    }

    @Test
    fun `addVideo(stream) adds video to player`() {

        val video = mockk<ArcVideo>()

        every { builder.build() } returns video
        initPlayer()

        testObject.addVideo(videoStream)

        verifySequence {
            postTvPlayerImpl.addVideo(video)
        }
    }

    @Test
    fun `toggleCaptions toggles captions on player if populated`() {
        initPlayer()
        testObject.toggleCaptions()
        verifySequence {
            postTvPlayerImpl.toggleCaptions()
        }
    }

    @Test
    fun `getSessionId returns null if videoAdData is null`() {
        assertNull(testObject.sessionId)
    }

    @Test
    fun `getSessionId returns value from videoAdData is it is populated`() {
        val video =
            TestUtils.createDefaultVideo(isLive = true, bestStream = mockBestStream)
        every { builder.build() } returns video
        testObject.setErrorListener(errorListener)
        testObject.initMediaPlayer(configInfo)
        testObject.initMedia(videoStream, "ad url")

        assertEquals("sessionId", testObject.sessionId)
    }


    @Test
    fun `onBackPressed starts pip if supported, not casting, config video frame is null`() {
        configInfo.apply {
            every { activity } returns mockk {
                every { isInPictureInPictureMode } returns false
            }
            every { isEnablePip } returns true
            every { videoFrame } returns null
        }
        every {
            postTvPlayerImpl.isCasting()
        } returns false
        initPlayer()
        assertTrue(testObject.onBackPressed())
        verifySequence {
            postTvPlayerImpl.isCasting()
            postTvPlayerImpl.onPipEnter()
        }
        assertTrue(testObject.isInPIP)
    }

    @Test
    fun `onBackPressed returns false when not supported`() {
        every { buildVersionProvider.sdkInt() } returns Build.VERSION_CODES.N
        assertFalse(testObject.onBackPressed())
    }

    @Test
    fun `onBackPressed returns false when configInfo is null`() {
        assertFalse(testObject.onBackPressed())
    }

    @Test
    fun `onBackPressed returns false when configInfo activity is null`() {
        initPlayer()
        assertFalse(testObject.onBackPressed())
    }

    @Test
    fun `onBackPressed returns false when configInfo activity is in pip`() {
        configInfo.apply {
            every { activity } returns mockk {
                every { isInPictureInPictureMode } returns true
            }
        }
        initPlayer()
        assertFalse(testObject.onBackPressed())
    }

    @Test
    fun `onBackPressed returns false when configInfo pip is not enabled`() {
        configInfo.apply {
            every { activity } returns mockk {
                every { isInPictureInPictureMode } returns false
                every { isEnablePip } returns false
            }
        }
        initPlayer()
        assertFalse(testObject.onBackPressed())
    }

    @Test
    fun `onBackPressed returns false when player is casting`() {
        configInfo.apply {
            every { activity } returns mockk {
                every { isInPictureInPictureMode } returns false
                every { isEnablePip } returns true
            }
        }
        every {
            postTvPlayerImpl.isCasting()
        } returns true
        initPlayer()
        assertFalse(testObject.onBackPressed())
    }

    @Test
    fun `createTimerTask creates timer`() {
        testObject = spyk(testObject)
        every { testObject.createTimer(any(), any()) } just Runs
        val video =
            TestUtils.createDefaultVideo(bestStream = mockBestStream, isLive = true)
        every { builder.build() } returns video
        every {
            utils.createTrackingHelper(
                expectedManifestUrl,
                testObject,
                configInfo,
                mContext,
                videoFrameLayout,
                testObject
            )
        } returns trackingHelper
        every {
            utils.createPostTvPlayerImpl(
                configInfo,
                testObject,
                trackingHelper
            )
        } returns postTvPlayerImpl
        every { enableServerSideAds(any(), any()) } returns false
        every { postTvPlayerImpl.getVideo() } returns video
        every { postTvPlayerImpl.getId() } returns "id"
        testObject.setErrorListener(errorListener)
        testObject.initMediaPlayer(configInfo)
        testObject.initMedia(videoStream)
        testObject.initEvents(eventTracker)
        clearAllMocks(answers = false)

        testObject.timerWork()

        verifySequence {
            testObject.timerWork()
            videoAdData.trackingUrl
            videoAdData.trackingUrl
            AdUtils.getAvails(expectedTrackingUrl)
            testObject.currentTimelinePosition
            trackingHelper.addEvents(availList, 0)
            testObject.createTimer(expectedPollingDelay, expectedPollingDelay)
        }
    }

    @Test
    fun `createTimer cancels existing timer if not null`() {
        val video =
            TestUtils.createDefaultVideo(bestStream = mockBestStream)
        every { builder.build() } returns video
        every { postTvPlayerImpl.getVideo() } returns video
        every { postTvPlayerImpl.getId() } returns "id"
        testObject.setErrorListener(errorListener)
        testObject.initMediaPlayer(configInfo)
        testObject.initMedia(videoStream)
        testObject.initEvents(eventTracker)
        clearAllMocks(answers = false)
        testObject = spyk(testObject)
        every { testObject.createTimerTask() } returns mockk()

        testObject.timerWork()
        testObject.timerWork()

        verify(exactly = 1) {
            mTimer.cancel()
        }
    }

    @Test
    fun `initMedia(streams) throws exception if configInfo is null`() {
        assertThrows(ArcXPException::class.java) {
            testObject.initMedia(emptyList())
        }
    }

    @Test
    fun `initMedia(streams) mIsLive false`() {
        val video1 =
            TestUtils.createDefaultVideo(id = "id1", bestStream = mockBestStream)
        val video2 =
            TestUtils.createDefaultVideo(id = "id2", bestStream = mockBestStream)
        every { builder.build() } returns video1
        every { builder2.build() } returns video2
        every { configInfo.adConfig } returns mockAdConfig
        testObject.initMediaPlayer(configInfo)
        testObject.initMedia(listOf(videoStream, videoStream2))

        assertEquals("id1", video1.id)
        assertEquals("id2", video2.id)
        assertTrue(video1.autoStartPlay)
        assertTrue(video2.autoStartPlay)
        val actual = slot<MutableList<ArcVideo>>()
        verify {
            enableServerSideAds(videoStream, mockBestStream)
            enableServerSideAds(videoStream2, mockBestStream)
            utils.createTrackingHelper(
                "id1",
                testObject,
                configInfo,
                mContext,
                videoFrameLayout,
                testObject
            )
            postTvPlayerImpl.playVideos(capture(actual))
        }
        val expected = listOf(video1, video2)
        assertEquals(expected, actual.captured)
    }

    @Test
    fun `initMedia(streams, adurls) mIsLive false`() {
        val video1 =
            TestUtils.createDefaultVideo(id = "id1", bestStream = mockBestStream)
        val video2 =
            TestUtils.createDefaultVideo(id = "id2", bestStream = mockBestStream)
        every { builder.build() } returns video1
        every { builder2.build() } returns video2
        every { configInfo.adConfig } returns mockAdConfig
        testObject.initMediaPlayer(configInfo)
        testObject.initMedia(listOf(videoStream, videoStream2), listOf("ad1", "ad2"))

        assertEquals("id1", video1.id)
        assertEquals("id2", video2.id)
        assertTrue(video1.autoStartPlay)
        assertTrue(video2.autoStartPlay)
        val actual = slot<MutableList<ArcVideo>>()
        verify {
            videoStream.adTagUrl = "ad1"
            videoStream2.adTagUrl = "ad2"
            enableServerSideAds(videoStream, mockBestStream)
            enableServerSideAds(videoStream2, mockBestStream)
            utils.createTrackingHelper(
                "id1",
                testObject,
                configInfo,
                mContext,
                videoFrameLayout,
                testObject
            )
            postTvPlayerImpl.playVideos(capture(actual))
        }
        val expected = listOf(video1, video2)
        assertEquals(expected, actual.captured)
    }

    @Test
    fun `initMedia(streams) mIsLive true`() {
        val video1 =
            TestUtils.createDefaultVideo(id = "id1", isLive = true, bestStream = mockBestStream)
        val video2 =
            TestUtils.createDefaultVideo(id = "id2", isLive = true, bestStream = mockBestStream)
        every { builder.build() } returns video1
        every { builder2.build() } returns video2
        every { configInfo.adConfig } returns mockAdConfig
        testObject.initMediaPlayer(configInfo)
        testObject.initMedia(listOf(videoStream, videoStream2))
        testObject.initMedia(listOf(videoStream, videoStream2))

        assertEquals(expectedManifestUrl, video1.id)
        assertEquals(expectedManifestUrl, video2.id)
        assertTrue(video1.autoStartPlay)
        assertTrue(video2.autoStartPlay)
        val actual = mutableListOf<MutableList<ArcVideo>>()
        verify {
            enableServerSideAds(videoStream, mockBestStream)
            enableServerSideAds(videoStream2, mockBestStream)
            utils.createTrackingHelper(
                expectedManifestUrl,
                testObject,
                configInfo,
                mContext,
                videoFrameLayout,
                testObject
            )
            postTvPlayerImpl.playVideos(capture(actual))
        }
        val expected = listOf(video1, video2)
        actual.forEach { assertEquals(expected, it) }
    }

    @Test
    fun `initMedia(streams, adurls) mIsLive true`() {
        val video1 =
            TestUtils.createDefaultVideo(id = "id1", isLive = true, bestStream = mockBestStream)
        val video2 =
            TestUtils.createDefaultVideo(id = "id2", isLive = true, bestStream = mockBestStream)
        every { builder.build() } returns video1
        every { builder2.build() } returns video2
        every { configInfo.adConfig } returns mockAdConfig
        testObject.initMediaPlayer(configInfo)
//        testObject.initMedia(listOf(videoStream, videoStream2))
//        testObject.initMedia(listOf(videoStream, videoStream2))

        testObject.initMedia(listOf(videoStream, videoStream2), listOf("ad1", "ad2"))
        testObject.initMedia(listOf(videoStream, videoStream2), listOf("ad1", "ad2"))

        assertEquals(expectedManifestUrl, video1.id)
        assertEquals(expectedManifestUrl, video2.id)
        assertTrue(video1.autoStartPlay)
        assertTrue(video2.autoStartPlay)
        val actual = mutableListOf<MutableList<ArcVideo>>()
        verify {
            videoStream.adTagUrl = "ad1"
            videoStream2.adTagUrl = "ad2"
            enableServerSideAds(videoStream, mockBestStream)
            enableServerSideAds(videoStream2, mockBestStream)
            utils.createTrackingHelper(
                expectedManifestUrl,
                testObject,
                configInfo,
                mContext,
                videoFrameLayout,
                testObject
            )
            postTvPlayerImpl.playVideos(capture(actual))
        }
        val expected = listOf(video1, video2)
        actual.forEach { assertEquals(expected, it) }
    }

    @Test
    fun `initMedia(videoStream) given player not null, is pip calls release on player`() {
        val video1 =
            TestUtils.createDefaultVideo(id = "id1", isLive = true, bestStream = mockBestStream)
        val video2 =
            TestUtils.createDefaultVideo(id = "id2", isLive = true, bestStream = mockBestStream)
        every { builder.build() } returns video1
        every { builder2.build() } returns video2
        every { configInfo.adConfig } returns mockAdConfig
        testObject.initMediaPlayer(configInfo)
        testObject.initMedia(listOf(videoStream, videoStream2))
        testObject.setIsInPIP(true)

        testObject.initMedia(listOf(videoStream, videoStream2))


        verify {
            postTvPlayerImpl.release()
        }
    }

    @Test
    fun `initMedia(videoStreams, adurls) given player not null, is pip calls release`() {
        val video1 =
            TestUtils.createDefaultVideo(id = "id1", isLive = true, bestStream = mockBestStream)
        val video2 =
            TestUtils.createDefaultVideo(id = "id2", isLive = true, bestStream = mockBestStream)
        every { builder.build() } returns video1
        every { builder2.build() } returns video2
        every { configInfo.adConfig } returns mockAdConfig
        testObject.initMediaPlayer(configInfo)
        testObject.initMedia(listOf(videoStream, videoStream2), listOf("ad1", "ad2"))
        testObject.setIsInPIP(true)
        testObject.initMedia(listOf(videoStream, videoStream2), listOf("ad1", "ad2"))


        verify {
            postTvPlayerImpl.release()
        }
    }

    @Test
    fun `initMedia(videoStreams, adurls) throws ArcXPException if config is null`() {
        assertThrows(ArcXPException::class.java) {
            testObject.initMedia(listOf(videoStream, videoStream2), listOf("ad1", "ad2"))
        }
    }

    @Test
    fun `initMedia(videoStreams, adurls) throws ArcXPException if too few ad urls for video streams`() {
        assertThrows(ArcXPException::class.java) {
            initPlayer()
            testObject.initMedia(listOf(videoStream, videoStream2), listOf("ad1"))
        }
    }

    @Test
    fun `startPIP sets pip and runs minimize if enabled and suppported with Activity`() {
        val expectedHeight = 200
        val expectedWidth = 100
        val expectedUnRoundedHeight = 999.7834798f
        val expectedUnRoundedWidth = 499.789f
        val view1 = mockk<View>(relaxed = true)
        val view2 = mockk<View>(relaxed = true)
        val display = mockk<Display>(relaxed = true)
        val mockDisplayMetrics = mockk<DisplayMetrics>()
        val mockActionBar = mockk<ActionBar>(relaxed = true)
        val mockActivity = mockk<Activity>(relaxed = true) {
            every { actionBar } returns mockActionBar
            every { windowManager } returns mockk {
                every { defaultDisplay } returns display
            }
//            every { enterPictureInPictureMode()}
        }
        val mockConfiguration = Configuration()
        mockConfiguration.screenWidthDp = expectedWidth
        mockConfiguration.screenHeightDp = expectedHeight
        mockResources.apply {
            every { configuration } returns mockConfiguration
            every { displayMetrics } returns mockDisplayMetrics
        }
        configInfo.apply {
            every { viewsToHide } returns listOf(view1, view2)
            every { isEnablePip } returns true
            every { activity } returns mockActivity
        }
        mockkStatic(TypedValue::class)
        every {
            TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                expectedHeight.toFloat(), mockDisplayMetrics
            )
        } returns expectedUnRoundedHeight
        every {
            TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                expectedWidth.toFloat(), mockDisplayMetrics
            )
        } returns expectedUnRoundedWidth





        initPlayer()
        testObject.startPIP(mockk())

        verify {
            view1.visibility = View.GONE
            view2.visibility = View.GONE
            mockActionBar.hide()
            display.getSize(any())
            anyConstructed<PictureInPictureParams.Builder>().setAspectRatio(any())
            pictureInPictureParamsBuilder.build()
            pictureInPictureParamsBuilder.build()
            mockActivity.enterPictureInPictureMode(pictureInPictureParams)
        }
        assertTrue(testObject.isInPIP)
    }

    @Test
    fun `startPip with null video frame returns from minimize`() {
        configInfo.apply {
            every { isEnablePip } returns true
            every { videoFrame } returns null
        }

        initPlayer()
        testObject.startPIP(mockk())
        assertTrue(testObject.isInPIP)
        verify(exactly = 0) {
            configInfo.viewsToHide
        }
    }

    @Test
    fun `stopPip stops pip if enabled`() {
        val view1 = mockk<View>(relaxed = true)
        val view2 = mockk<View>(relaxed = true)
        val mockActionBar = mockk<ActionBar>(relaxed = true)
        val mockActivity = mockk<Activity>(relaxed = true) {
            every { actionBar } returns mockActionBar
        }
        configInfo.apply {
            every { isEnablePip } returns true
            every { viewsToHide } returns listOf(view1, view2)
            every { activity } returns mockActivity
        }


        initPlayer()
        testObject.stopPIP()
        verify {
            postTvPlayerImpl.onPipExit()
            view1.visibility = View.VISIBLE
            view2.visibility = View.VISIBLE
            mockActionBar.show()
        }
        assertFalse(testObject.isInPIP)
    }

    @Test
    fun `displayVideo switches views`() {
        val mockParent = mockk<ViewGroup>(relaxed = true)
        val mockVideoFrame = mockk<ArcVideoFrame>(relaxed = true)
        every { configInfo.videoFrame } returns mockVideoFrame
        every { mMessageOverlay.parent } returns mockParent
        every { videoFrameLayout.parent } returns mockParent
        initPlayer()
        testObject.displayVideo()
        verifySequence {
            videoFrameLayout.parent
            mockParent.removeView(mMessageOverlay)
            videoFrameLayout.parent
            videoFrameLayout.parent
            mockParent.removeView(videoFrameLayout)
            videoFrameLayout.tag = null
            mockVideoFrame.addView(videoFrameLayout)
        }


        assertFalse(testObject.isStickyPlayer)
    }

    @Test
    fun `removePlayerFrame when is stickyplayer`() {
        initPlayer()
        testObject.setIsStickyPlayer(true)
        testObject.removePlayerFrame()

        verifySequence {
            videoFrameLayout.setOnClickListener(null)
            postTvPlayerImpl.onStickyPlayerStateChanged(false)
        }
        assertFalse(testObject.isStickyPlayer)
    }

    @Test
    fun `onScrolled when not youtube player`() {
        initPlayer()
        testObject.onScrolled(mockk())

        assertTrue(testObject.isStickyPlayer)
    }

    @Test
    fun `initMedia(stream) does not call getVideoManifest twice when client and server ads enabled`() {
        val video =
            TestUtils.createDefaultVideo(id = "url", isLive = true, bestStream = mockBestStream)
        val errorListener = mockk<ArcVideoSDKErrorListener>(relaxed = true)
        mockkObject(AdUtils)
        every {
            getVideoManifest(
                videoStream,
                mockBestStream,
                configInfo
            )
        } returns videoAdData


        every { builder.build() } returns video
        every {
            utils.createTrackingHelper(
                "url",
                testObject,
                configInfo,
                mContext,
                videoFrameLayout,
                testObject
            )
        } returns trackingHelper
        testObject.initMediaPlayer(configInfo)
        testObject.setErrorListener(errorListener)
        clearAllMocks(answers = false)
        testObject.initMedia(videoStream)


        verify(exactly = 1) {
            getVideoManifest(any(), any(), any())
        }
    }

    @Test
    fun `isMinimalNow returns true when PostTvPlayer is not null and minimal controls are active`() {
        initPlayer()
        every { postTvPlayerImpl.isMinimalControlsNow() } returns true
        assertTrue(testObject.isMinimalNow)
    }

    @Test
    fun `isMinimalNow returns false when PostTvPlayer is not null and minimal controls are not active`() {
        initPlayer()
        every { postTvPlayerImpl.isMinimalControlsNow() } returns false
        assertFalse(testObject.isMinimalNow)
    }

    @Test
    fun `isMinimalNow returns false when PostTvPlayer is null`() {
        assertFalse(testObject.isMinimalNow)
    }
}


