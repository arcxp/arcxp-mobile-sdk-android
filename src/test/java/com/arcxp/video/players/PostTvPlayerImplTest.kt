package com.arcxp.video.players

import android.view.KeyEvent
import com.arcxp.commons.testutils.TestUtils.createDefaultVideo
import com.arcxp.video.listeners.ArcKeyListener
import com.arcxp.video.model.ArcVideo
import com.google.ads.interactivemedia.v3.api.AdEvent
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.mockk
import io.mockk.verifySequence
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue


class PostTvPlayerImplTest {

    @RelaxedMockK
    private lateinit var playerStateHelper: PlayerStateHelper
    @RelaxedMockK
    private lateinit var videoPlayer: ArcVideoPlayer
    @RelaxedMockK
    private lateinit var adEventListener: AdEvent.AdEventListener
    @RelaxedMockK
    private lateinit var playerListener: PlayerListener
    @RelaxedMockK
    private lateinit var arcKeyListener: ArcKeyListener
    @RelaxedMockK
    private lateinit var arcVideo: ArcVideo


    private lateinit var testObject: PostTvPlayerImpl

    private val expectedString = "expected"
    private val expectedLong = 2134L
    private val expectedInt = 123
    private val expectedFloat = 0.123f

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        testObject = PostTvPlayerImpl(playerStateHelper, videoPlayer, adEventListener, playerListener)
        //init
        verifySequence {
            playerStateHelper.playVideoAtIndex = playerListener::playVideoAtIndex
            videoPlayer.playerListener = playerListener
            videoPlayer.adEventListener = adEventListener
            playerStateHelper.playerListener = playerListener
        }
        clearAllMocks(answers = false)

    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `getId returns Id`() {
        every { videoPlayer.id} returns expectedString
        assertEquals(expectedString, testObject.getId())
    }
    @Test
    fun `getAdType returns adType`() {
        val expected = 1324L
        every { videoPlayer.adType} returns expected
        assertEquals(expected, testObject.getAdType())
    }

    @Test
    fun `show Controls calls player`() {
        testObject.showControls(show = true)
        verifySequence {
            videoPlayer.showControls(show = true)
        }
    }

    @Test
    fun `toggleAutoShow calls player`() {
        testObject.toggleAutoShow(show = true)
        verifySequence {
            videoPlayer.toggleAutoShow(show = true)
        }
    }

    @Test
    fun `isControlsVisible returns value from player controls`() {
        every { videoPlayer.playControls} returns mockk {
            every { isControllerFullyVisible } returns true
        }
        assertTrue(testObject.isControlsVisible())
    }

    @Test
    fun `isControlsVisible returns false when play controls null`() {
        every { videoPlayer.playControls} returns null
        assertFalse(testObject.isControlsVisible())
    }

    @Test
    fun `isControlsVisible returns false value from player controls`() {
        every { videoPlayer.playControls} returns mockk {
            every { isControllerFullyVisible } returns false
        }
        assertFalse(testObject.isControlsVisible())
    }

    @Test
    fun `isControllerFullyVisible returns value from player controls`() {
        every { videoPlayer.playControls} returns mockk {
            every { isControllerFullyVisible } returns true
        }
        assertTrue(testObject.isControllerFullyVisible())
    }
    @Test
    fun `isControllerFullyVisible returns false value from player controls`() {
        every { videoPlayer.playControls} returns mockk {
            every { isControllerFullyVisible } returns false
        }
        assertFalse(testObject.isControllerFullyVisible())
    }
    @Test
    fun `isControllerFullyVisible returns false when play controls null`() {
        every { videoPlayer.playControls} returns null
        assertFalse(testObject.isControllerFullyVisible())
    }
    @Test
    fun `isControlsVisible returns false if player controls null`() {
        every { videoPlayer.playControls} returns null
        assertFalse(testObject.isControlsVisible())
    }

    @Test
    fun `getCurrentVideoDuration calls player`() {
        every { videoPlayer.currentVideoDuration} returns expectedLong
        assertEquals(expectedLong, testObject.getCurrentVideoDuration())
    }

    @Test
    fun `isClosedCaptionAvailable calls player`() {
        every { videoPlayer.isClosedCaptionAvailable} returns true
        assertTrue (testObject.isClosedCaptionAvailable())
    }
    @Test
    fun `isClosedCaptionVisibleAndOn calls player`() {
        every { videoPlayer.isVideoCaptionEnabled} returns true
        assertTrue (testObject.isClosedCaptionVisibleAndOn())
    }

    @Test
    fun `enableClosedCaption calls player`() {
        testObject.enableClosedCaption(enable = true)
        verifySequence {
            videoPlayer.enableClosedCaption(enable = true)
        }
    }

    @Test
    fun `setCcButtonDrawable calls player`() {
        testObject.setCcButtonDrawable(expectedInt)
        verifySequence {
            videoPlayer.setCcButtonDrawable(expectedInt)
        }
    }

    @Test
    fun `isFullScreen calls player`() {
        every { videoPlayer.isFullScreen} returns true
        assertTrue (testObject.isFullScreen())
    }


    @Test
    fun `setFullScreen calls player`() {
        testObject.setFullscreen(full = true)
        verifySequence {
            videoPlayer.setFullscreen(full = true)
        }
    }

    @Test
    fun `setFullScreenUi calls player`() {
        testObject.setFullscreenUi(full = true)
        verifySequence {
            videoPlayer.setFullscreenUi(full = true)
        }
    }
    @Test
    fun `setFullscreenListener calls player`() {

        testObject.setFullscreenListener(listener = arcKeyListener)
        verifySequence {
            videoPlayer.setFullscreenListener(listener = arcKeyListener)
        }
    }
    @Test
    fun `setPlayerKeyListener calls player`() {
        testObject.setPlayerKeyListener(listener = arcKeyListener)
        verifySequence {
            videoPlayer.setPlayerKeyListener(listener = arcKeyListener)
        }
    }

    @Test
    fun `onPipExit calls player helper`() {
        testObject.onPipExit()
        verifySequence {
            playerStateHelper.onPipExit()
        }
    }
    @Test
    fun `onPipEnter calls player helper`() {
        testObject.onPipEnter()
        verifySequence {
            playerStateHelper.onPipEnter()
        }
    }
    @Test
    fun `release calls player`() {
        testObject.release()
        verifySequence {
            videoPlayer.release()
        }
    }


    @Test
    fun `playVideo calls player`() {
        testObject.playVideo(arcVideo)
        verifySequence { videoPlayer.playVideo(video = arcVideo) }
    }
    @Test
    fun `playVideos calls player`() {
        val list = mutableListOf(createDefaultVideo(id = "1"), createDefaultVideo(id = "2"))
        testObject.playVideos(list)
        verifySequence { videoPlayer.playVideos(videos = list) }
    }

    @Test
    fun `addVideo calls player`() {
        testObject.addVideo(arcVideo)
        verifySequence { videoPlayer.addVideo(video = arcVideo) }
    }

    @Test
    fun `isPlaying calls player`() {
        every { videoPlayer.isPlaying} returns true
        assertTrue(testObject.isPlaying())
    }

    @Test
    fun `isCasting calls player`() {
        every { videoPlayer.isCasting()} returns true
        assertTrue(testObject.isCasting())
    }

    @Test
    fun `getPlayWhenReadyState calls player`() {
        every { videoPlayer.playWhenReadyState } returns true
        assertTrue(testObject.getPlayWhenReadyState())
    }

    @Test
    fun `onStickyPlayerStateChanged calls player`() {
        testObject.onStickyPlayerStateChanged(isSticky = true)
        verifySequence {
            videoPlayer.onStickyPlayerStateChanged(isSticky = true)
        }
    }

    @Test
    fun `pausePlay calls player`() {
        testObject.pausePlay(shouldPlay = true)
        verifySequence {
            videoPlayer.pausePlay(shouldPlay = true)
        }
    }

    @Test
    fun `toggleCaptions calls player`() {
        testObject.toggleCaptions()
        verifySequence {
            videoPlayer.toggleCaptions()
        }
    }

    @Test
    fun `getVideo calls player`() {
        every { videoPlayer.video } returns arcVideo
        assertEquals(arcVideo, testObject.getVideo())
    }

    @Test
    fun `onActivityResume calls player`() {
        testObject.onActivityResume()
        verifySequence {
            videoPlayer.onActivityResume()
        }
    }

    @Test
    fun `start calls player`() {
        testObject.start()
        verifySequence {
            videoPlayer.start()
        }
    }

    @Test
    fun `stop calls player`() {
        testObject.stop()
        verifySequence {
            videoPlayer.stop()
        }
    }

    @Test
    fun `pause calls player`() {
        testObject.pause()
        verifySequence {
            videoPlayer.pause()
        }
    }

    @Test
    fun `resume calls player`() {
        testObject.resume()
        verifySequence {
            videoPlayer.resume()
        }
    }

    @Test
    fun `seekTo calls player`() {
        testObject.seekTo(ms = expectedInt)
        verifySequence {
            videoPlayer.seekTo(ms = expectedInt)
        }
    }
    @Test
    fun `setVolume calls player`() {
        testObject.setVolume(volume = expectedFloat)
        verifySequence {
            videoPlayer.setVolume(volume = expectedFloat)
        }
    }

    @Test
    fun `getPlaybackState returns from player`() {
        every {videoPlayer.playbackState} returns expectedInt
        assertEquals(expectedInt, testObject.getPlaybackState())
    }
    @Test
    fun `getCurrentPosition returns from player`() {
        every {videoPlayer.currentPosition} returns expectedLong
        assertEquals(expectedLong, testObject.getCurrentPosition())
    }
    @Test
    fun `getCurrentTimelinePosition returns from player`() {
        every {videoPlayer.currentTimelinePosition} returns expectedLong
        assertEquals(expectedLong, testObject.getCurrentTimelinePosition())
    }

    @Test
    fun `onKeyEvent calls player`() {
        val event = mockk<KeyEvent>()
        testObject.onKeyEvent(event = event)
        verifySequence {
            videoPlayer.onKeyEvent(event = event)
        }
    }
    @Test
    fun `getOverlay calls player`() {
        testObject.getOverlay(tag = expectedString)
        verifySequence {
            videoPlayer.getOverlay(tag = expectedString)
        }
    }

    @Test
    fun `getVideoPlayer returns player`() {
        assertEquals(videoPlayer, testObject.getVideoPlayer())
    }

    @Test
    fun `isMinimalControlsNow returns player value`() {
        every { videoPlayer.isMinimalControlsNow()} returns true
        assertTrue(testObject.isMinimalControlsNow())
    }
}