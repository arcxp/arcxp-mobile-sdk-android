package com.arc.arcvideo

import android.app.Activity
import android.content.Context
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.arc.arcvideo.cast.ArcCastManager
import com.arc.arcvideo.listeners.ArcKeyListener
import com.arc.arcvideo.listeners.ArcVideoEventsListener
import com.arc.arcvideo.listeners.ArcVideoSDKErrorListener
import com.arc.arcvideo.listeners.ArcVideoTrackingEvents
import com.arc.arcvideo.model.ArcVideo
import com.arc.arcvideo.model.ArcVideoStream
import com.arc.arcvideo.views.ArcVideoFrame
import io.mockk.clearAllMocks
import io.mockk.every
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import org.powermock.api.mockito.PowerMockito
import org.powermock.core.classloader.annotations.PrepareForTest
import org.powermock.modules.junit4.PowerMockRunner

@RunWith(PowerMockRunner::class)
@PrepareForTest(ArcVideoManager::class, ArcMediaPlayerConfig::class, ArcMediaPlayer::class)
class ArcMediaPlayerTest {

    @Mock
    lateinit var video: ArcVideo
    @Mock
    lateinit var mContext: Context
    @Mock
    lateinit var mConfig: ArcMediaPlayerConfig
    @Mock
    lateinit var arcVideoManager: ArcVideoManager
    @Mock
    lateinit var builder: ArcMediaPlayerConfig.Builder

    private lateinit var testObject: ArcMediaPlayer
    
    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        PowerMockito.whenNew(ArcVideoManager::class.java)
            .withArguments(eq(mContext), any())
            .thenReturn(arcVideoManager)
        `when`(builder.build()).thenReturn(mConfig)
        PowerMockito.whenNew(ArcMediaPlayerConfig.Builder::class.java)
            .withNoArguments()
            .thenReturn(builder)
        testObject = ArcMediaPlayer.instantiate(context = mContext)
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `setActivity sets Activity on mConfigBuilder`() {
        val activity = mock(Activity::class.java)

        testObject
            .setActivity(activity)

        verify(builder).setActivity(activity)

    }

    @Test
    fun `setActivity sets AppCompatActivity on mConfigBuilder`() {
        val activityCompat = mock(AppCompatActivity::class.java)

        testObject
            .setActivity(activityCompat)
    }

    @Test
    fun `setVideoFrame sets frame on builder`() {
        val videoFrame = mock(ArcVideoFrame::class.java)
        testObject
        ArcMediaPlayer.createPlayer(mContext).setVideoFrame(videoFrame)

        verify(builder).setVideoFrame(videoFrame)
    }

    @Test
    fun `initMedia, when mConfig not null, calls release and initializes mediaPlayer and media`() {
        testObject
            .configureMediaPlayer(mConfig)
            .initMedia(video)

        inOrder(arcVideoManager, mConfig).run {
            verify(arcVideoManager).release()
            verify(arcVideoManager).initMediaPlayer(mConfig)
            verify(arcVideoManager).initMedia(video)
        }
    }

    @Test
    fun `initMedia, when mConfig null, calls release and initializes mediaPlayer and media`() {
        testObject
            .initMedia(video)

        inOrder(arcVideoManager, builder, mConfig).run {
            verify(arcVideoManager).release()
            verify(builder).build()
            verify(arcVideoManager).initMediaPlayer(mConfig)
            verify(arcVideoManager).initMedia(video)
        }
    }

    @Test
    fun `initMedia from url, when mConfig not null, calls release and initializes mediaPlayer and media`() {
        val arcVideoStreamVirtualChannel = mock(ArcVideoStream::class.java)
        testObject
            .configureMediaPlayer(mConfig)
            .initMedia(arcVideoStreamVirtualChannel)

        inOrder(arcVideoManager, mConfig).run {
            verify(arcVideoManager).release()
            verify(arcVideoManager).initMediaPlayer(mConfig)
            verify(arcVideoManager).initMedia(arcVideoStreamVirtualChannel)
        }
    }

    @Test
    fun `initMedia from url, when mConfig null, calls release and initializes mediaPlayer and media`() {
        val arcVideoStreamVirtualChannel = mock(ArcVideoStream::class.java)
        testObject
            .initMedia(arcVideoStreamVirtualChannel)

        inOrder(arcVideoManager, builder, mConfig).run {
            verify(arcVideoManager).release()
            verify(builder).build()
            verify(arcVideoManager).initMediaPlayer(mConfig)
            verify(arcVideoManager).initMedia(arcVideoStreamVirtualChannel)
        }
    }

    @Test
    fun `initMedia with stream calls release, config not null, initializes media player and media`() {
        val video = mock(ArcVideoStream::class.java)

        testObject
            .configureMediaPlayer(mConfig)
            .initMedia(video)

        inOrder(arcVideoManager, builder, mConfig).run {
            verify(arcVideoManager).release()
            verify(arcVideoManager).initMediaPlayer(mConfig)
            verify(arcVideoManager).initMedia(video)
        }
    }

    @Test
    fun `initMedia with stream calls release, config null, initializes media player and media`() {
        val video = mock(ArcVideoStream::class.java)

        testObject
            .initMedia(video)

        inOrder(arcVideoManager, builder, mConfig).run {
            verify(arcVideoManager).release()
            verify(builder).build()
            verify(arcVideoManager).initMediaPlayer(mConfig)
            verify(arcVideoManager).initMedia(video)
        }
    }

    @Test
    fun `initMedia with stream, adUrl, and config not null, calls release and initializes media player and media`() {
        val video = mock(ArcVideoStream::class.java)

        testObject
            .configureMediaPlayer(mConfig)
            .initMedia(video, "adUrl")

        inOrder(arcVideoManager, builder, mConfig).run {
            verify(arcVideoManager).release()
            verify(arcVideoManager).initMediaPlayer(mConfig)
            verify(arcVideoManager).initMedia(video, "adUrl")
        }
    }

    @Test
    fun `initMedia with stream, adUrl, and config null, calls release and initializes media player and media`() {
        val video = mock(ArcVideoStream::class.java)
        testObject
            .initMedia(video, "adUrl")

        inOrder(arcVideoManager, builder, mConfig).run {
            verify(arcVideoManager).release()
            verify(builder).build()
            verify(arcVideoManager).initMediaPlayer(mConfig)
            verify(arcVideoManager).initMedia(video, "adUrl")
        }
    }

    @Test
    fun `initMedia with stream list and config not null, calls release and initializes media player and media`() {
        val list = listOf(mock(ArcVideoStream::class.java))

        testObject
            .configureMediaPlayer(mConfig)
            .initMedia(list)

        inOrder(arcVideoManager, builder, mConfig).run {
            verify(arcVideoManager).release()
            verify(arcVideoManager).initMediaPlayer(mConfig)
            verify(arcVideoManager).initMedia(list)
        }
    }

    @Test
    fun `initMedia with stream list and config null, calls release and initializes media player and media`() {
        val list = listOf(mock(ArcVideoStream::class.java))

        testObject
            .initMedia(list)

        inOrder(arcVideoManager, builder, mConfig).run {
            verify(arcVideoManager).release()
            verify(arcVideoManager).initMediaPlayer(mConfig)
            verify(arcVideoManager).initMedia(list)
        }
    }

    @Test
    fun `initMedia with stream list, adUrl, and config not null, calls release and initializes media player and media`() {
        val videos = listOf(mock(ArcVideoStream::class.java))

        val adUrls = listOf("adUrl")
        testObject
            .configureMediaPlayer(mConfig)
            .initMedia(videos, adUrls)

        inOrder(arcVideoManager, builder, mConfig).run {
            verify(arcVideoManager).release()
            verify(arcVideoManager).initMediaPlayer(mConfig)
            verify(arcVideoManager).initMedia(videos, adUrls)
        }
    }

    @Test
    fun `initMedia with stream list, adUrl, and config null, calls release and initializes media player and media`() {
        val videos = listOf(mock(ArcVideoStream::class.java))
        val adUrls = listOf("adUrl")

        testObject
            .initMedia(videos, adUrls)

        inOrder(arcVideoManager, builder, mConfig).run {
            verify(arcVideoManager).release()
            verify(arcVideoManager).initMediaPlayer(mConfig)
            verify(arcVideoManager).initMedia(videos, adUrls)
        }
    }

    @Test
    fun `addVideo adds video to manager`() {
        val video = mock(ArcVideoStream::class.java)
        testObject.addVideo(video)
        verify(arcVideoManager, times(1)).addVideo(video)
    }

    @Test
    fun `addVideo with ad url adds video and ad to manager`() {
        val video = mock(ArcVideoStream::class.java)
        testObject.addVideo(video, "ad")
        verify(arcVideoManager, times(1)).addVideo(video, "ad")
    }

    @Test
    fun `initMediaEvents initializes track events with manager`() {
        val events = mock(ArcVideoEventsListener::class.java)
        testObject.initMediaEvents(events)
        verify(arcVideoManager, times(1)).initEvents(events)
    }

    @Test
    fun `trackMediaEvents initializes track events with manager`() {
        val events = mock(ArcVideoEventsListener::class.java)
        testObject.trackMediaEvents(events)
        verify(arcVideoManager, times(1)).initEvents(events)
    }

    @Test
    fun `setErrorListener sets listener on manager`() {
        val listener = mock(ArcVideoSDKErrorListener::class.java)
        testObject.setErrorListener(listener)
        verify(arcVideoManager, times(1)).setErrorListener(listener)
    }

    @Test
    fun `trackErrors sets listener on manager`() {
        val listener = mock(ArcVideoSDKErrorListener::class.java)
        testObject.trackErrors(listener)
        verify(arcVideoManager, times(1)).setErrorListener(listener)
    }

    @Test
    fun `playVideo plays video `() {
        testObject.playVideo()
        verify(arcVideoManager, times(1)).displayVideo()
    }

    @Test
    fun `displayVideo plays video `() {
        testObject.displayVideo()
        verify(arcVideoManager, times(1)).displayVideo()
    }

    @Test
    fun `finish calls release on manager`() {
        testObject.finish()
        verify(arcVideoManager, times(1)).release()
    }

    @Test
    fun `onBackPressed calls release on manager`() {
        testObject.onBackPressed()
        verify(arcVideoManager, times(1)).onBackPressed()
    }

    @Test
    fun `enablePip calls method on mConfigBuilder`() {
        testObject.enablePip(true)
        verify(builder, times(1)).enablePip(true)
    }

    @Test
    fun `stopPip calls methods on manager and finishes current activity`() {
        val arcVideoActivity = mock(Activity::class.java)
        `when`(arcVideoManager.currentActivity).thenReturn(arcVideoActivity)
        testObject.exitAppFromPip()
        inOrder(arcVideoManager, arcVideoActivity).run {
            verify(arcVideoManager).setmIsInPIP(false)
            verify(arcVideoManager).release()
            verify(arcVideoManager).currentActivity
            verify(arcVideoActivity).finish()
        }
    }

    @Test
    fun `showControls calls through to manager`() {
        testObject.showControls()
        verify(arcVideoManager, times(1)).showControls()
    }

    @Test
    fun `hideControls calls through to manager`() {
        testObject.hideControls()
        verify(arcVideoManager, times(1)).hideControls()
    }

    @Test
    fun `isControlsVisible arcVideoManager boolean is null`() {
        assertFalse(testObject.isControlsVisible)
    }

    @Test
    fun `isControlsVisible arcVideoManager boolean is populated`() {
        `when`(arcVideoManager.isControlsVisible).thenReturn(true)
        assertTrue(testObject.isControlsVisible)
    }

    @Test
    fun `isClosedCaptionAvailable arcVideoManager boolean is null`() {
        assertFalse(testObject.isClosedCaptionAvailable)
    }

    @Test
    fun `isClosedCaptionAvailable arcVideoManager boolean is populated`() {
        `when`(arcVideoManager.isClosedCaptionAvailable).thenReturn(true)
        assertTrue(testObject.isClosedCaptionAvailable)
    }

    @Test
    fun `isFullScreen arcVideoManager boolean is null`() {
        assertFalse(testObject.isFullScreen)
    }

    @Test
    fun `isFullScreen arcVideoManager boolean is populated`() {
        `when`(arcVideoManager.isFullScreen).thenReturn(true)
        assertTrue(testObject.isFullScreen)
    }

    @Test
    fun `setFullScreen calls through to manager with value`() {
        testObject.setFullscreen(true)
        verify(arcVideoManager, times(1)).setFullscreen(true)
    }

    @Test
    fun `setFullscreenKeyListener calls through to manager with value`() {
        val listener = mock(ArcKeyListener::class.java)
        testObject.setFullscreenKeyListener(listener)
        verify(arcVideoManager, times(1)).setFullscreenListener(listener)
    }

    @Test
    fun `setPlayerKeyListener calls through to manager with value`() {
        val listener = mock(ArcKeyListener::class.java)
        testObject.setPlayerKeyListener(listener)
        verify(arcVideoManager, times(1)).setPlayerKeyListener(listener)
    }

    @Test
    fun `stop calls through to manager`() {
        testObject.stop()
        verify(arcVideoManager, times(1)).stopPlay()
    }

    @Test
    fun `start calls through to manager`() {
        testObject.start()
        verify(arcVideoManager, times(1)).startPlay()
    }

    @Test
    fun `pause calls through to manager`() {
        testObject.pause()
        verify(arcVideoManager, times(1)).pausePlay()
    }

    @Test
    fun `resume calls through to manager`() {
        testObject.resume()
        verify(arcVideoManager, times(1)).resumePlay()
    }

    @Test
    fun `seekTo calls through to manager with value`() {
        val seekToTime = 234
        testObject.seekTo(seekToTime)
        verify(arcVideoManager, times(1)).seekTo(seekToTime)
    }

    @Test
    fun `setVolume calls through to manager with value`() {
        val volume = 234.3f
        testObject.setVolume(volume)
        verify(arcVideoManager, times(1)).setVolume(volume)
    }

    @Test
    fun `playbackState returns 0 if value not populated in manager `() {
        assertEquals(0, testObject.playbackState)
    }

    @Test
    fun `playbackState returns value if populated in manager `() {
        val playBackState = 543
        `when`(arcVideoManager.playbackState).thenReturn(playBackState)
        assertEquals(playBackState, testObject.playbackState)
    }

    @Test
    fun `playWhenReadyState returns false if value not populated in manager `() {
        assertFalse(testObject.playWhenReadyState)
    }

    @Test
    fun `playWhenReadyState returns value if populated in manager`() {
        `when`(arcVideoManager.playWhenReadyState).thenReturn(true)
        assertTrue(testObject.playWhenReadyState)
    }

    @Test
    fun `playerPosition returns value populated in manager`() {
        val playHeadPosition = 543324L
        `when`(arcVideoManager.playheadPosition).thenReturn(playHeadPosition)
        assertEquals(playHeadPosition, testObject.playerPosition)
    }

    @Test
    fun `currentTimelinePosition returns value populated in manager`() {
        val currentTimelinePosition = 543324L
        `when`(arcVideoManager.currentTimelinePosition).thenReturn(currentTimelinePosition)
        assertEquals(
            currentTimelinePosition,
            testObject.currentTimelinePosition
        )
    }

    @Test
    fun `currentVideoDuration returns value populated in manager`() {
        val currentVideoDuration = 543324L
        `when`(arcVideoManager.currentVideoDuration).thenReturn(currentVideoDuration)
        assertEquals(
            currentVideoDuration,
            testObject.currentVideoDuration
        )
    }

    @Test
    fun `addOverlay calls builder with values`() {
        val view = mock(View::class.java)
        testObject.addOverlay("tag", view)
        verify(builder, times(1)).addOverlay("tag", view)
    }

    @Test
    fun `getOverlay returns value from manager`() {
        val view = mock(View::class.java)
        `when`(arcVideoManager.getOverlay("tag")).thenReturn(view)
        assertEquals(view, testObject.getOverlay("tag"))
    }

    @Test
    fun `sdkVersion returns value from resources`() {
        assertEquals("1.5.0", testObject.sdkVersion)
    }

    @Test
    fun `setViewsToHide sets views to hide in builder`() {
        val view1 = mock(View::class.java)
        val view2 = mock(View::class.java)
        val view3 = mock(View::class.java)
        testObject.setViewsToHide(view1, view2, view3)
        verify(builder, times(1)).setViewsToHide(view1, view2, view3)
    }

    @Test
    fun `setEnableAds calls builder with value`() {
        testObject.setEnableAds(true)
        verify(builder, times(1)).setEnableAds(true)
    }

    @Test
    fun `setAdConfigUrl calls builder with value`() {
        testObject.setAdConfigUrl("url")
        verify(builder, times(1)).setAdConfigUrl("url")
    }

    @Test
    fun `setPreferredStreamType calls builder with value`() {
        val preferredStreamType = ArcMediaPlayerConfig.PreferredStreamType.HLS
        testObject.setPreferredStreamType(preferredStreamType)
        verify(builder, times(1)).setPreferredStreamType(preferredStreamType)
    }

    @Test
    fun `setMaxBitRate calls builder with value`() {
        val maxBitRate = 123
        testObject.setMaxBitRate(maxBitRate)
        verify(builder, times(1)).setMaxBitRate(maxBitRate)
    }

    @Test
    fun `showClosedCaption calls builder with value`() {
        testObject.showClosedCaption(true)
        verify(builder, times(1)).showClosedCaption(true)
    }

    @Test
    fun `toggleClosedCaption returns value from manager`() {
        `when`(arcVideoManager.enableClosedCaption(true)).thenReturn(true)
        assertTrue(testObject.toggleClosedCaption(true))
    }


    @Test
    fun `setCcButtonDrawable sets value in manager, returns true if successful`() {
        val drawableRes = 3423245
        `when`(arcVideoManager.setCcButtonDrawable(drawableRes)).thenReturn(true)
        assertTrue(testObject.setCcButtonDrawable(drawableRes))
    }

    @Test
    fun `setCcButtonDrawable sets value in manager, returns false if unsuccessful`() {
        val drawableRes = 3423245
        `when`(arcVideoManager.setCcButtonDrawable(drawableRes)).thenReturn(false)
        assertFalse(testObject.setCcButtonDrawable(drawableRes))
    }

    @Test
    fun `showCountdown sets value in builder`() {
        testObject.showCountdown(true)
        verify(builder, times(1)).showCountdown(true)
    }

    @Test
    fun `showProgressBar sets value in builder`() {
        testObject.showProgressBar(true)
        verify(builder, times(1)).showProgressBar(true)
    }

    @Test
    fun `setServerSideAds sets value in builder`() {
        testObject.setServerSideAds(true)
        verify(builder, times(1)).setServerSideAds(true)
    }

    @Test
    fun `setClientSideAds sets value in builder`() {
        testObject.setClientSideAds(true)
        verify(builder, times(1)).setClientSideAds(true)
    }

    @Test
    fun `setAutoStartPlay sets value in builder`() {
        testObject.setAutoStartPlay(true)
        verify(builder, times(1)).setAutoStartPlay(true)
    }

    @Test
    fun `showSeekButton sets value in builder`() {
        testObject.showSeekButton(true)
        verify(builder, times(1)).showSeekButton(true)
    }

    @Test
    fun `setStartMuted sets value in builder`() {
        testObject.setStartMuted(true)
        verify(builder, times(1)).setStartMuted(true)
    }

    @Test
    fun `setFocusSkipButton sets value in builder`() {
        testObject.setFocusSkipButton(true)
        verify(builder, times(1)).setFocusSkipButton(true)
    }

    @Test
    fun `setCcStartMode sets value in builder`() {
        val expectedCcStartMode = ArcMediaPlayerConfig.CCStartMode.ON
        testObject.setCcStartMode(expectedCcStartMode)
        verify(builder, times(1)).setCcStartMode(expectedCcStartMode)
    }

    @Test
    fun `setAutoShowControls sets value in builder`() {
        testObject.setAutoShowControls(true)
        verify(builder, times(1)).setAutoShowControls(true)
    }

    @Test
    fun `setShowClosedCaptionTrackSelection sets value in builder`() {
        testObject.setShowClosedCaptionTrackSelection(true)
        verify(builder, times(1)).setShowClosedCaptionTrackSelection(true)
    }

    @Test
    fun `addAdParam sets values in builder`() {
        testObject.addAdParam("key", "value")
        verify(builder, times(1)).addAdParam("key", "value")
    }

    @Test
    fun `setCastManager sets value in builder`() {
        val arcCastManager = mock(ArcCastManager::class.java)
        testObject.setCastManager(arcCastManager)
        verify(builder, times(1)).setCastManager(arcCastManager)
    }

    @Test
    fun `onPause releases manager and calls manager onPause`() {
        testObject.onPause()
        inOrder(arcVideoManager).run {
            verify(arcVideoManager).release()
            verify(arcVideoManager).onPause()
        }
    }

    @Test
    fun `onStop calls manager onStop`() {
        testObject.onStop()
        verify(arcVideoManager, times(1)).onStop()
    }

    @Test
    fun `onDestroy calls manager onDestroy`() {
        testObject.onDestroy()
        verify(arcVideoManager, times(1)).onDestroy()
    }

    @Test
    fun `onResume calls manager onResume`() {
        testObject.onResume()
        verify(arcVideoManager, times(1)).onResume()
    }

    @Test
    fun `onPictureInPictureModeChanged when arcVideoManger isPipStopRequest stops pip `() {
        val currentActivity = mock(Activity::class.java)
        `when`(arcVideoManager.currentActivity).thenReturn(currentActivity)
        `when`(arcVideoManager.isPipStopRequest).thenReturn(true)

        testObject.onPictureInPictureModeChanged(true, null)

        inOrder(arcVideoManager, currentActivity).run {
            arcVideoManager.setmIsInPIP(false)
            arcVideoManager.release()
            arcVideoManager.currentActivity
            currentActivity.finish()
        }
    }

    @Test
    fun `setControlsShowTimeoutMs sets value in builder`() {
        val ms = 45378
        testObject.setControlsShowTimeoutMs(ms)
        verify(builder, times(1)).setControlsShowTimeoutMs(ms)
    }

    @Test
    fun `enableLogging sets value in builder`() {
        testObject.enableLogging()
        verify(builder, times(1)).enableLogging()
    }

    @Test
    fun `useDialogForFullscreen sets value in builder`() {
        testObject.useDialogForFullscreen(true)
        verify(builder, times(1)).useDialogForFullscreen(true)
    }

    @Test
    fun `keepControlsSpaceOnHide sets value in builder`() {
        testObject.keepControlsSpaceOnHide(true)
        verify(builder, times(1)).setKeepControlsSpaceOnHide(true)
    }

    @Test
    fun `isPipEnabled returns arcVideoManger Value when true`() {
        `when`(arcVideoManager.isPipEnabled).thenReturn(true)
        assertTrue(testObject.isPipEnabled())
    }

    @Test
    fun `isPipEnabled returns arcVideoManger Value when false`() {
        `when`(arcVideoManager.isPipEnabled).thenReturn(false)
        assertFalse(testObject.isPipEnabled())
    }
}