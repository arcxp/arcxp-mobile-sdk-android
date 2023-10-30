package com.arcxp.video.players

import android.app.Activity
import android.content.Context
import android.graphics.drawable.Drawable
import android.net.Uri
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.view.accessibility.CaptioningManager
import android.widget.ImageButton
import android.widget.ImageView
import androidx.core.content.ContextCompat
import com.arcxp.commons.testutils.TestUtils.createDefaultVideo
import com.arcxp.sdk.R
import com.arcxp.video.ArcXPVideoConfig
import com.arcxp.video.cast.ArcCastManager
import com.arcxp.video.listeners.ArcKeyListener
import com.arcxp.video.listeners.VideoListener
import com.arcxp.video.model.ArcVideo
import com.arcxp.video.model.ArcVideoSDKErrorType
import com.arcxp.video.model.PlayerState
import com.arcxp.video.model.TrackingType
import com.arcxp.video.model.TrackingTypeData
import com.arcxp.video.util.PrefManager
import com.arcxp.video.util.TrackingHelper
import com.arcxp.video.util.Utils
import com.google.ads.interactivemedia.v3.api.AdEvent
import com.google.ads.interactivemedia.v3.api.AdsLoader
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ext.cast.CastPlayer
import com.google.android.exoplayer2.ext.ima.ImaAdsLoader
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ads.AdsMediaSource
import com.google.android.exoplayer2.ui.PlayerControlView
import com.google.android.exoplayer2.ui.StyledPlayerView
import com.google.android.gms.cast.framework.CastContext
import io.mockk.Called
import io.mockk.EqMatcher
import io.mockk.MockKAnnotations
import io.mockk.called
import io.mockk.clearAllMocks
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.spyk
import io.mockk.verify
import io.mockk.verifyOrder
import io.mockk.verifySequence
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class ArcVideoPlayerTest {


    @RelaxedMockK
    private lateinit var playerState: PlayerState

    @RelaxedMockK
    private var mCastControlView: PlayerControlView? = null

    @RelaxedMockK
    private lateinit var mCastControlViewParent: ViewGroup

    @RelaxedMockK
    private lateinit var playerStateHelper: PlayerStateHelper

    @RelaxedMockK
    private lateinit var mListener: VideoListener

    @RelaxedMockK
    private lateinit var mConfig: ArcXPVideoConfig

    @RelaxedMockK
    private lateinit var arcCastManager: ArcCastManager

    @RelaxedMockK
    private lateinit var utils: Utils

    @RelaxedMockK
    private lateinit var contentMediaSource: MediaSource

    @RelaxedMockK
    private lateinit var trackingHelper: TrackingHelper

    @RelaxedMockK
    private lateinit var playerListener: PlayerListener

    @RelaxedMockK
    private lateinit var adEventListener: AdEvent.AdEventListener

    @RelaxedMockK
    private lateinit var captionsManager: CaptionsManager

    @RelaxedMockK
    private var mPlayer: ExoPlayer? = null

    @RelaxedMockK
    private lateinit var mPlayerView: StyledPlayerView

    @RelaxedMockK
    private lateinit var mockActivity: Activity

    @RelaxedMockK
    private lateinit var mAdsLoader: ImaAdsLoader

    @RelaxedMockK
    private lateinit var mAdsLoaderAdsLoader: AdsLoader

    @RelaxedMockK
    private lateinit var mCastPlayer: CastPlayer

    @RelaxedMockK
    private lateinit var mCastContext: CastContext

    @RelaxedMockK
    lateinit var expectedAdUri: Uri

    @RelaxedMockK
    lateinit var adsMediaSource: AdsMediaSource

    @RelaxedMockK
    private lateinit var fullScreenButton: ImageButton

    @RelaxedMockK
    private lateinit var ccButton: ImageButton

    @RelaxedMockK
    private lateinit var volumeButton: ImageButton

    private lateinit var testObject: ArcVideoPlayer

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        every { playerState.mLocalPlayer } returns mPlayer
        every { playerState.mLocalPlayerView } returns mPlayerView
        every { playerState.mCastControlView } returns mCastControlView
        every { playerState.currentPlayer } returns mPlayer
        every { playerState.mCastPlayer } returns mCastPlayer
        every { mConfig.activity } returns mockActivity
        every { arcCastManager.getCastContext() } returns mCastContext
//        every { mCastControlView!!.findViewById<ImageButton>(any())} returns mockk(relaxed = true)
//        every { mCastControlView!!.findViewById<ImageView>(any())} returns mockk(relaxed = true)
        every { mCastControlView!!.findViewById<ImageButton>(R.id.exo_fullscreen) } returns fullScreenButton
        every { mPlayerView.findViewById<ImageButton>(R.id.exo_fullscreen) } returns fullScreenButton

        every { mCastControlView!!.findViewById<ImageButton>(R.id.exo_pip) } returns mockk(relaxed = true)
        every { mCastControlView!!.findViewById<ImageButton>(R.id.exo_share) } returns mockk(relaxed = true)
        every { mCastControlView!!.findViewById<ImageButton>(R.id.exo_volume) } returns volumeButton
        every { mPlayerView.findViewById<ImageButton>(R.id.exo_volume) } returns volumeButton
        every { mCastControlView!!.findViewById<ImageButton>(R.id.exo_cc) } returns mockk(relaxed = true)
        every { mCastControlView!!.findViewById<ImageView>(R.id.exo_artwork) } returns mockk(relaxed = true)
        every { mCastControlView!!.parent } returns mCastControlViewParent

        every { captionsManager.createMediaSourceWithCaptions() } returns contentMediaSource

        every { utils.createCastPlayer(mCastContext) } returns mCastPlayer
        every { utils.createPlayerControlView() } returns mCastControlView

        mockkConstructor(ImaAdsLoader.Builder::class)
        val mockImaAdsLoaderBuilder = mockk<ImaAdsLoader.Builder>()
        every {
            constructedWith<ImaAdsLoader.Builder>(EqMatcher(mockActivity)).setAdEventListener(
                any()
            )
        } returns mockImaAdsLoaderBuilder
        every { mockImaAdsLoaderBuilder.build() } returns mAdsLoader

        testObject = ArcVideoPlayer(
            playerState,
            playerStateHelper,
            mListener,
            mConfig,
            arcCastManager,
            utils,
            trackingHelper,
            captionsManager
        )
        testObject.playerListener = playerListener
        testObject.adEventListener = adEventListener
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }


    @Test
    fun `pausePlay given true calls mPlayer, mPlayerView, trackingHelper if they are not null`() {
        testObject.pausePlay(true)

        verifySequence {
            mPlayer!!.playWhenReady = true
            mPlayerView.hideController()
        }
        verify { mListener wasNot called }
    }

    @Test
    fun `pausePlay given false calls mPlayer, mPlayerView, trackingHelper if they are not null`() {
        testObject.pausePlay(false)

        verifySequence {
            mPlayer!!.playWhenReady = false
            mPlayerView.hideController()
        }
        verify { mListener wasNot called }
    }

    @Test
    fun `pausePlay throws exception, is handled by listener`() {
        val error = " pause Play error"
        val exception = Exception(error)
        every { mPlayer!!.playWhenReady = true } throws exception

        testObject.pausePlay(true)

        verifySequence {
            mPlayer!!.playWhenReady = true
            mListener.onError(ArcVideoSDKErrorType.EXOPLAYER_ERROR, error, exception)
        }
        verify {
            mPlayerView wasNot called
            trackingHelper wasNot called
        }
    }

    @Test
    fun `start throws exception, is handled by listener`() {
        val error = "start error"
        val exception = Exception(error)
        every { mPlayer!!.playWhenReady = true } throws exception

        testObject.start()

        verifyOrder {
            mPlayer!!.playWhenReady = true
            mListener.onError(ArcVideoSDKErrorType.EXOPLAYER_ERROR, error, exception)
        }
    }

    @Test
    fun `start calls mPlayer setPlayWhenReady with true`() {
        testObject.start()

        verifySequence { mPlayer!!.playWhenReady = true }

        verify { mListener wasNot called }
    }

    @Test
    fun `pause calls mPlayer and trackingHelper if they are not null`() {
        testObject.pause()

        verifySequence {
            mPlayer!!.playWhenReady = false
        }

        verify { mListener wasNot called }
    }


    @Test
    fun `pause throws exception, is handled by listener`() {
        val error = " pause error"
        val exception = Exception(error)
        every { mPlayer!!.playWhenReady = false } throws exception

        testObject.pause()

        verifySequence {
            mPlayer!!.playWhenReady = false
            mListener.onError(ArcVideoSDKErrorType.EXOPLAYER_ERROR, error, exception)
        }
        verify { trackingHelper wasNot called }
    }

    @Test
    fun `setPlayerKeyListener sets listeners with no exceptions`() {
        testObject.setPlayerKeyListener(mockk())

        verifySequence {
            mPlayerView.setOnKeyListener(any())
            mCastControlView!!.setOnKeyListener(any())
        }
        verify { trackingHelper wasNot called }
    }

    @Test
    fun `setPlayerKeyListener throws exception, is handled, and then sets mCastControlView listener`() {
        val error = "setPlayerKeyListener error"
        val exception = Exception(error)
        every { mPlayerView.setOnKeyListener(any()) } throws exception

        testObject.setPlayerKeyListener(mockk())

        verifySequence {
            mPlayerView.setOnKeyListener(any())
            mListener.onError(ArcVideoSDKErrorType.EXOPLAYER_ERROR, error, exception)
            mCastControlView!!.setOnKeyListener(any())
        }
    }

    @Test
    fun `setPlayerKeyListener mPlayerView onKeyListener when key is KEYCODE_BACK`() {
        val mPlayerViewListener = slot<View.OnKeyListener>()
        val keyEvent = mockk<KeyEvent> {
            every { action } returns KeyEvent.ACTION_UP
        }
        val arcKeyListener = mockk<ArcKeyListener>(relaxed = true)
        every { keyEvent.keyCode } returns KeyEvent.KEYCODE_BACK
        testObject.playVideo(createDefaultVideo())
        testObject.setPlayerKeyListener(arcKeyListener)
        verify { mPlayerView.setOnKeyListener(capture(mPlayerViewListener)) }
        clearAllMocks(answers = false)

        assertFalse(mPlayerViewListener.captured.onKey(mockk(), 2321, keyEvent))

        verifySequence {
            keyEvent.action
            keyEvent.keyCode
            arcKeyListener.onBackPressed()
        }
    }

    @Test
    fun `setPlayerKeyListener mPlayerView onKeyListener when key is not back`() {
        val mPlayerViewListener = slot<View.OnKeyListener>()
        val keyEvent = mockk<KeyEvent> {
            every { action } returns KeyEvent.ACTION_UP
        }
        val keyCode = 2334
        val arcKeyListener = mockk<ArcKeyListener>(relaxed = true)
        every { keyEvent.keyCode } returns KeyEvent.KEYCODE_0
        testObject.playVideo(createDefaultVideo())
        testObject.setPlayerKeyListener(arcKeyListener)
        verify { mPlayerView.setOnKeyListener(capture(mPlayerViewListener)) }
        clearAllMocks(answers = false)

        assertFalse(mPlayerViewListener.captured.onKey(mockk(), keyCode, keyEvent))

        verifySequence {
            keyEvent.action
            keyEvent.keyCode
            arcKeyListener.onKey(keyCode, keyEvent)
        }
    }

    @Test
    fun `setPlayerKeyListener mCastControlView onKeyListener`() {
        val mCastControlViewListener = slot<View.OnKeyListener>()
        val keyEvent = mockk<KeyEvent> {
            every { action } returns KeyEvent.ACTION_UP
        }
        val keyCode = 2334
        val arcKeyListener = mockk<ArcKeyListener>(relaxed = true)
        every { keyEvent.keyCode } returns KeyEvent.KEYCODE_0
        testObject.playVideo(createDefaultVideo())
        testObject.setPlayerKeyListener(arcKeyListener)
        verify { mCastControlView!!.setOnKeyListener(capture(mCastControlViewListener)) }
        clearAllMocks(answers = false)

        assertFalse(mCastControlViewListener.captured.onKey(mockk(), keyCode, keyEvent))

        verifySequence { arcKeyListener.onKey(keyCode, keyEvent) }
    }

    @Test
    fun `when playVideo throws Exception, then Listener handles error`() {
        val mockVideo = mockk<ArcVideo>(relaxed = true)
        val exceptionMessage = "our exception message"
        val exception = Exception(exceptionMessage)
        every { trackingHelper.initVideo(any()) } throws exception
        every {
            mListener.onError(
                ArcVideoSDKErrorType.EXOPLAYER_ERROR,
                exceptionMessage,
                mockVideo
            )
        } returns Unit

        testObject.playVideo(mockVideo)

        verify {
            mListener.onError(
                ArcVideoSDKErrorType.EXOPLAYER_ERROR,
                exceptionMessage,
                exception
            )
        }
    }


    @Test
    fun `playVideo sets expected values from video`() {
        val expectedIsLive = true
        val arcVideo = createDefaultVideo(isLive = expectedIsLive)

        every { playerState.mVideo } returns arcVideo
        every { trackingHelper.initVideo(any()) } throws Exception()
        testObject.playVideo(arcVideo)

        verify {
            playerState.mVideo = arcVideo
            playerState.mIsLive = expectedIsLive
            playerState.mHeadline = "headline"
            playerState.mShareUrl = "mShareUrl"
            playerState.mVideoId = "id"
        }

    }

    @Test
    fun `setCurrentPlayer throws exception preparing ad, is output to Logger`() {
        mockkStatic(Log::class)

        val errorMessage = "i am error"
        val exception = Exception(errorMessage)
        val arcVideo = createDefaultVideo()
        every { playerState.mVideo } returns arcVideo
        every { mAdsLoader.adsLoader } throws exception
        every { mConfig.isLoggingEnabled } returns true
        every { mCastPlayer.isCastSessionAvailable } returns true
        every { playerState.mAdsLoader } returns mAdsLoader
        every { playerState.mVideoId } returns arcVideo.uuid
        testObject.playVideo(arcVideo)

        verify(exactly = 1) {
            Log.e("ArcVideoSDK", "Error preparing ad for video uuid", exception)
        }
    }


    @Test
    fun `playOnLocal throws exception, and is handled`() {

        val message = "play on local exception"
        val exception = Exception(message)
        every { playerState.mVideo } returns createDefaultVideo(shouldPlayAds = true)
        every {
            mConfig.activity
        } throws exception
        testObject.playOnLocal()

        verify(exactly = 1) {
            mListener.onError(ArcVideoSDKErrorType.INIT_ERROR, message, exception)
        }
    }

    @Test
    fun `setFullscreenUi changes to fullscreen given true`() {
        val drawable = mockk<Drawable>()
        val videoData = mockk<TrackingTypeData.TrackingVideoTypeData>(relaxed = true)
        val expectedPosition = 324343L
        mockkStatic(ContextCompat::class)
        every {
            ContextCompat.getDrawable(
                mockActivity,
                R.drawable.FullScreenDrawableButtonCollapse
            )
        } returns drawable
        every { utils.createTrackingVideoTypeData() } returns videoData
        every { mPlayer!!.currentPosition } returns expectedPosition
        testObject.playVideo(createDefaultVideo())
        clearAllMocks(answers = false)

        testObject.setFullscreenUi(true)

        verifySequence {
            trackingHelper.fullscreen()
            playerState.mLocalPlayerView
            mPlayerView.findViewById<ImageButton>(R.id.exo_fullscreen)
            ContextCompat.getDrawable(mockActivity, R.drawable.FullScreenDrawableButtonCollapse)
            fullScreenButton.setImageDrawable(drawable)
            playerState.mIsFullScreen = true
            playerStateHelper.createTrackingEvent(TrackingType.ON_OPEN_FULL_SCREEN)
        }
    }

    @Test
    fun `setFullscreenUi changes to normal screen given false`() {
        val drawable = mockk<Drawable>()
        val videoData = mockk<TrackingTypeData.TrackingVideoTypeData>(relaxed = true)
        val expectedPosition = 324343L
        every {
            ContextCompat.getDrawable(
                mockActivity,
                R.drawable.FullScreenDrawableButton
            )
        } returns drawable
        every { utils.createTrackingVideoTypeData() } returns videoData
        every { mPlayer!!.currentPosition } returns expectedPosition
        every { mListener.isStickyPlayer } returns true

        testObject.setFullscreenUi(false)

        verifyOrder {
            trackingHelper.normalScreen()
            mPlayerView.findViewById<ImageButton>(R.id.exo_fullscreen)
            ContextCompat.getDrawable(mockActivity, R.drawable.FullScreenDrawableButton)
            fullScreenButton.setImageDrawable(drawable)
            mListener.isStickyPlayer
            mPlayerView.hideController()
            mPlayerView.requestLayout()
            playerState.mIsFullScreen = false
            utils.createTrackingVideoTypeData()
            videoData.arcVideo = testObject.video
            mPlayer!!.currentPosition
            videoData.position = expectedPosition
            playerStateHelper.onVideoEvent(TrackingType.ON_CLOSE_FULL_SCREEN, videoData)
        }
    }

    @Test
    fun `onActivityResume plays video`() {
        val arcVideo = createDefaultVideo()
        every { playerState.mIsFullScreen } returns true
        every { playerState.mLocalPlayerView } returns null
        every { playerState.mVideo } returns arcVideo
        testObject = spyk(testObject)
        testObject.onActivityResume()
        verify(exactly = 1) {
            testObject.playVideo(arcVideo)
        }
    }


    @Test
    fun `setFullscreen true and isUseFullScreenDialog`() {
        every { mConfig.isUseFullScreenDialog } returns true
        testObject.setFullscreen(full = true)
        verifySequence {
            playerStateHelper.toggleFullScreenDialog(false)
            mConfig.isUseFullScreenDialog
        }
    }

    @Test
    fun `setFullscreen false not isUseFullScreenDialog`() {
        every { mConfig.isUseFullScreenDialog } returns false
        testObject.setFullscreen(full = false)
        verifySequence {
            playerStateHelper.toggleFullScreenDialog(true)
            mConfig.isUseFullScreenDialog
            mListener.setFullscreen(false)
        }
    }


    @Test
    fun `set Volume sets mPlayer volume when mPlayer is not null`() {
        val expectedVolume = .78f
        val drawable = mockk<Drawable>()

        every {
            ContextCompat.getDrawable(
                mockActivity,
                R.drawable.MuteOffDrawableButton
            )
        } returns drawable

        testObject.setVolume(expectedVolume)

        verifySequence {
            mPlayer!!.volume = expectedVolume
            mPlayerView.findViewById<ImageButton>(R.id.exo_volume)
            ContextCompat.getDrawable(mockActivity, R.drawable.MuteOffDrawableButton)
            volumeButton.setImageDrawable(drawable)
        }

        verify { mListener wasNot called }
    }

    @Test
    fun `set Volume to 0 sets mPlayer volume when mPlayer is not null`() {
        val expectedVolume = 0.0f
        val drawable = mockk<Drawable>()

        every {
            ContextCompat.getDrawable(
                mockActivity,
                R.drawable.MuteDrawableButton
            )
        } returns drawable

        testObject.setVolume(expectedVolume)

        verifySequence {
            mPlayer!!.volume = expectedVolume
            mPlayerView.findViewById<ImageButton>(R.id.exo_volume)
            ContextCompat.getDrawable(mockActivity, R.drawable.MuteDrawableButton)
            volumeButton.setImageDrawable(drawable)
        }

        verify { mListener wasNot called }
    }

    @Test
    fun `set Volume throws exception and is handled`() {
        val expectedMessage = "error text"
        val expectedVolume = 0.5f
        val exception = Exception(expectedMessage)
        every { mPlayer!!.volume = expectedVolume } throws exception

        testObject.setVolume(expectedVolume)

        verifySequence {
            mListener.onError(
                ArcVideoSDKErrorType.EXOPLAYER_ERROR,
                expectedMessage,
                exception
            )
        }
    }


    @Test
    fun `getPlayWhenReadyState returns currentPlayer getPlayWhenReady value when currentPlayer is not null`() {
        every { mPlayer!!.playWhenReady } returns true
        assertTrue(testObject.playWhenReadyState)
    }

    @Test
    fun `getPlayWhenReadyState returns false if currentPlayer is null`() {
        every { playerState.currentPlayer } returns null
        assertFalse(testObject.playWhenReadyState)
    }

    @Test
    fun `isPlaying returns false when currentPlayer is null`() {
        every { playerState.currentPlayer } returns null
        assertFalse(testObject.isPlaying)
    }

    @Test
    fun `isPlaying returns true when currentPlayer is not null state ready and getPlayWhenReady true`() {
        every { mPlayer!!.playbackState } returns Player.STATE_READY
        every { mPlayer!!.playWhenReady } returns true
        assertTrue(testObject.isPlaying)
    }

    @Test
    fun `isPlaying returns false when currentPlayer is not null state ready and getPlayWhenReady false`() {
        every { mPlayer!!.playbackState } returns Player.STATE_READY
        every { mPlayer!!.playWhenReady } returns false
        assertFalse(testObject.isPlaying)
    }

    @Test
    fun `isPlaying returns false when currentPlayer is not null state other than ready`() {
        every { mPlayer!!.playbackState } returns Player.STATE_ENDED
        every { mPlayer!!.playWhenReady } returns false
        assertFalse(testObject.isPlaying)
    }


    @Test
    fun `show controls given true shows controller`() {
        testObject.showControls(true)
        verify(exactly = 1) { mPlayerView.showController() }
    }

    @Test
    fun `show controls given true but disableControls true does not show or hide controller`() {
        every { playerState.disabledControlsForAd } returns true

        testObject.showControls(true)

        verify { mPlayerView wasNot called }
    }

    @Test
    fun `show controls given false hides controller`() {
        testObject.showControls(false)

        verify(exactly = 1) { mPlayerView.hideController() }
    }

    @Test
    fun `getAdType mPlayer not null and is playing ad gets adGroupTime from mPlayer`() {
        every { mPlayer!!.isPlayingAd } returns true



        assertEquals(0, testObject.adType)
    }

    @Test
    fun `getAdType mPlayer not null and is not playing ad returns zero`() {
        every { mPlayer!!.isPlayingAd } returns false


        assertEquals(0, testObject.adType)
    }//TODO test for non zero return

    @Test
    fun `getAdType mPlayer null returns zero`() {
        assertEquals(0, testObject.adType)
    }


    @Test
    fun `getPlaybackState returns currentPlayer playbackState when it is not null`() {
        val expectedPlayBackState = 3
        every { mPlayer!!.playbackState } returns expectedPlayBackState

        testObject.playVideo(createDefaultVideo())

        assertEquals(expectedPlayBackState, testObject.playbackState)
    }

    @Test
    fun `getPlaybackState returns zero if currentPlayer is null`() {
        every { playerState.currentPlayer } returns null
        assertEquals(0, testObject.playbackState)
    }


    @Test
    fun `isVideoCaptionEnabled with default CC startMode non captioning manager service`() {
        val service = mockk<Any>()
        mockkStatic(PrefManager::class)
        every {
            PrefManager.getBoolean(
                mockActivity,
                PrefManager.IS_CAPTIONS_ENABLED,
                false
            )
        } returns true
        every { mockActivity.getSystemService(Context.CAPTIONING_SERVICE) } returns service
        every { playerState.mVideo } returns createDefaultVideo()

        assertTrue(testObject.isVideoCaptionEnabled)
    }

    @Test
    fun `isVideoCaptionEnabled with default CC startMode Captioning manager service`() {
        val service = mockk<CaptioningManager>()
        mockkStatic(PrefManager::class)
        every {
            PrefManager.getBoolean(
                mockActivity,
                PrefManager.IS_CAPTIONS_ENABLED,
                true
            )
        } returns true
        every { service.isEnabled } returns true
        every { mockActivity.getSystemService(Context.CAPTIONING_SERVICE) } returns service

        every { playerState.mVideo } returns createDefaultVideo()

        assertTrue(testObject.isVideoCaptionEnabled)
    }

    @Test
    fun `isVideoCaptionEnabled with ON CC startMode`() {
        every { playerState.mVideo } returns createDefaultVideo(ccStartMode = ArcXPVideoConfig.CCStartMode.ON)

        assertTrue(testObject.isVideoCaptionEnabled)
    }

    @Test
    fun `isVideoCaptionEnabled throws exception returns false`() {
        mockkStatic(PrefManager::class)
        every {
            PrefManager.getBoolean(
                mockActivity,
                PrefManager.IS_CAPTIONS_ENABLED,
                any()
            )
        } throws Exception()

        testObject.playVideo(createDefaultVideo())

        assertFalse(testObject.isVideoCaptionEnabled)
    }


    @Test
    fun `setCcButtonDrawable when ccButton is not null, sets drawable then returns true`() {
        every { playerState.ccButton } returns ccButton
        val expectedDrawable = mockk<Drawable>()
        val expectedDrawableIntValue = 234534
        every {
            ContextCompat.getDrawable(
                mockActivity,
                expectedDrawableIntValue
            )
        } returns expectedDrawable
        testObject.playVideo(createDefaultVideo())
        clearAllMocks(answers = false)

        testObject.setCcButtonDrawable(expectedDrawableIntValue)

        verifySequence { ccButton.setImageDrawable(expectedDrawable) }
    }

    @Test
    fun `setCcButtonDrawable when ccButton is null returns false`() {
        every { playerState.ccButton } returns null
        assertFalse(testObject.setCcButtonDrawable(34597))
    }

    //
    @Test
    fun `getOverlay returns item from mFullscreenOverlays`() {
        val mFullscreenOverlays = mockk<HashMap<String, View>>(relaxed = true)
        every { playerState.mFullscreenOverlays } returns mFullscreenOverlays
        val tag = "tag"
        val view = mockk<View>()
        every { mFullscreenOverlays[tag] } returns view

        assertEquals(view, testObject.getOverlay(tag))
    }

    @Test
    fun `removeOverlay removes from map and View group`() {
        val mFullscreenOverlays = mockk<HashMap<String, View>>(relaxed = true)
        every { playerState.mFullscreenOverlays } returns mFullscreenOverlays
        val tag = "TAG"
        val viewGroup = mockk<ViewGroup>()
        val view = mockk<View>()
        every { mFullscreenOverlays.put(tag, view) } returns view
        every { mFullscreenOverlays[tag] } returns view
        every { mFullscreenOverlays.remove(tag) } returns view
        every { view.parent } returns viewGroup
        every { viewGroup.removeView(view) } returns Unit

        testObject.removeOverlay(tag)


        verifySequence {
            mFullscreenOverlays[tag]
            mFullscreenOverlays.remove(tag)
            view.parent
            viewGroup.removeView(view)
        }
    } //TODO ask about null safety here (removeOverlay) will view always have a parent? if not, NPE possible

    @Test
    fun `seekTo seeks to time if mPlayer is not null`() {
        val seekToMs = 287364

        testObject.seekTo(seekToMs)

        verify(exactly = 1) { mPlayer!!.seekTo(seekToMs.toLong()) }
    }

    @Test
    fun `seekTo throws exception and is handled`() {
        val errorMessage = "error in seek to"
        val exception = Exception(errorMessage)
        every { mPlayer!!.seekTo(any()) } throws exception
        testObject.seekTo(234)

        verify(exactly = 1) {
            mListener.onError(
                ArcVideoSDKErrorType.EXOPLAYER_ERROR,
                errorMessage,
                exception
            )
        }
    }

    @Test
    fun `stop throws exception and is handled`() {
        val errorMessage = "error in stop"
        val exception = Exception(errorMessage)
        every { mPlayer!!.playWhenReady = false } throws exception

        testObject.stop()

        verifySequence {
            mListener.onError(
                ArcVideoSDKErrorType.EXOPLAYER_ERROR,
                errorMessage,
                exception
            )
        }
    }

    @Test
    fun `stop stops player, setPlayWhenReady to false and seek to zero`() {

        testObject.stop()

        verifySequence {
            mPlayer!!.playWhenReady = false
            mPlayer!!.stop()
            mPlayer!!.seekTo(0)
        }
    }


    @Test
    fun `resume if mPlayer is not null, restarts player and sends tracking event to listener`() {
//        val arcVideo = createDefaultVideo()
//        val expectedCurrentPosition = 876435L
//        val videoData = mockk<TrackingTypeData.TrackingVideoTypeData>(relaxed = true)
//        every { utils.createTrackingVideoTypeData() } returns videoData
//        every { mPlayer!!.currentPosition } returns expectedCurrentPosition

        testObject.resume()

        verifySequence {
            mPlayer!!.playWhenReady = true
            playerStateHelper.createTrackingEvent(TrackingType.ON_PLAY_RESUMED)
        }
    }

    @Test
    fun `resume throws exception, and is handled`() {
        val errorMessage = "error in resume"
        val exception = Exception(errorMessage)
        every { playerState.mLocalPlayer } throws exception


        testObject.resume()

        verifySequence {
            mListener.onError(
                ArcVideoSDKErrorType.EXOPLAYER_ERROR,
                errorMessage,
                exception
            )
        }
    }


    @Test
    fun `playVideos, when given a list with videos, sets mVideo and MVideos, then plays video `() {
        val arcVideo1 = createDefaultVideo(id = "1")
        val arcVideo2 = createDefaultVideo(id = "2")
        val arcVideo3 = createDefaultVideo(id = "3")
        val videoList = mutableListOf(arcVideo1, arcVideo2, arcVideo3)

        testObject.playVideos(videoList)

        verify {
            playerState.mVideos = videoList
            playerState.mVideo = arcVideo1

        }//todo ensure playvideo is called
    }

    @Test
    fun `playVideos, when given empty list, fires error event`() {
        testObject.playVideos(mutableListOf())
        verifySequence { playerStateHelper.onVideoEvent(TrackingType.ERROR_PLAYLIST_EMPTY, null) }
        verify { playerState wasNot called }
    }

    @Test
    fun `release frees appropriate resources given populated and is fullscreen`() {

        every { playerState.mIsFullScreen } returns true
        val playerViewParent = mockk<ViewGroup>(relaxed = true)
        every { mPlayerView.parent } returns playerViewParent

        testObject.release()

        verifyOrder {
            playerState.mIsFullScreen
            playerStateHelper.toggleFullScreenDialog(true)
            playerState.mLocalPlayerView
            playerState.mLocalPlayerView
            mPlayerView.parent
            playerState.mLocalPlayerView
            mPlayerView.parent
            playerState.mLocalPlayerView
            playerViewParent.removeView(mPlayerView)
            playerState.mLocalPlayerView = null
            playerState.videoTrackingSub
            playerState.videoTrackingSub!!.unsubscribe()
            playerState.videoTrackingSub = null
            playerState.mLocalPlayer
            playerState.mLocalPlayer
            mPlayer!!.stop()
            playerState.mLocalPlayer
            mPlayer!!.release()
            playerState.mLocalPlayer = null
            playerState.mTrackSelector
            playerState.mTrackSelector = null
            playerState.mAdsLoader
            playerState.mAdsLoader!!.setPlayer(null)
            playerState.mAdsLoader!!.release()
            playerState.mAdsLoader = null
            playerState.mIsFullScreen
//            mListener.removePlayerFrame()
            playerState.mCastPlayer
            playerState.mCastPlayer
            mCastPlayer.setSessionAvailabilityListener(null)
            playerState.mCastPlayer
            mCastPlayer.release()
            playerState.mCastControlView
            playerState.mCastControlView
            mCastControlView!!.player = null
            playerState.mCastControlView
            mCastControlView!!.parent
            playerState.mCastControlView
            mCastControlView!!.parent
            mCastControlViewParent.removeView(mCastControlView)
            playerState.mCastControlView = null
        }
    }

    @Test
    fun `release frees appropriate resources given populated and is not fullscreen`() {

        every { playerState.mIsFullScreen } returns false
        val playerViewParent = mockk<ViewGroup>(relaxed = true)
        every { mPlayerView.parent } returns playerViewParent

        testObject.release()

        verifyOrder {
            playerState.mIsFullScreen
//            playerStateHelper.toggleFullScreenDialog(true)
            playerState.mLocalPlayerView
            playerState.mLocalPlayerView
            mPlayerView.parent
            playerState.mLocalPlayerView
            mPlayerView.parent
            playerState.mLocalPlayerView
            playerViewParent.removeView(mPlayerView)
            playerState.mLocalPlayerView = null
            playerState.videoTrackingSub
            playerState.videoTrackingSub!!.unsubscribe()
            playerState.videoTrackingSub = null
            playerState.mLocalPlayer
            playerState.mLocalPlayer
            mPlayer!!.stop()
            playerState.mLocalPlayer
            mPlayer!!.release()
            playerState.mLocalPlayer = null
            playerState.mTrackSelector
            playerState.mTrackSelector = null
            playerState.mAdsLoader
            playerState.mAdsLoader!!.setPlayer(null)
            playerState.mAdsLoader!!.release()
            playerState.mAdsLoader = null
            playerState.mIsFullScreen
            mListener.removePlayerFrame()
            playerState.mCastPlayer
            playerState.mCastPlayer
            mCastPlayer.setSessionAvailabilityListener(null)
            playerState.mCastPlayer
            mCastPlayer.release()
            playerState.mCastControlView
            playerState.mCastControlView
            mCastControlView!!.player = null
            playerState.mCastControlView
            mCastControlView!!.parent
            playerState.mCastControlView
            mCastControlView!!.parent
            mCastControlViewParent.removeView(mCastControlView)
            playerState.mCastControlView = null
        }
    }

    @Test
    fun `onStickyPlayerStateChanged isSticky not fullscreen`() {
        testObject.playVideo(createDefaultVideo())
        clearMocks(mPlayerView)
        testObject.onStickyPlayerStateChanged(true)

        verifySequence {
            mPlayerView.apply {
                hideController()
                requestLayout()
                setControllerVisibilityListener(any<StyledPlayerView.ControllerVisibilityListener>())
            }
        }
    }

    @Test
    fun `onStickyPlayerStateChanged isSticky not fullscreen test listener hides controller and requests layout`() {
        testObject.playVideo(createDefaultVideo())

        val listener = slot<StyledPlayerView.ControllerVisibilityListener>()

        testObject.onStickyPlayerStateChanged(true)

        verify { mPlayerView.setControllerVisibilityListener(capture(listener)) }

        clearMocks(mPlayerView)
        listener.captured.onVisibilityChanged(PlayerControlView.VISIBLE)

        verifySequence {
            mPlayerView.hideController()
            mPlayerView.requestLayout()
        }
    }

    @Test
    fun `onStickyPlayerStateChanged when isSticky true but not fullscreen, test listener does nothing if not visible`() {
        val listener = slot<StyledPlayerView.ControllerVisibilityListener>()
        testObject.playVideo(createDefaultVideo())
        testObject.onStickyPlayerStateChanged(true)

        verify { mPlayerView.setControllerVisibilityListener(capture(listener)) }


        clearMocks(mPlayerView)
        listener.captured.onVisibilityChanged(PlayerControlView.INVISIBLE)

        verify { mPlayerView wasNot Called }
    }

    @Test
    fun `onStickyPlayerStateChanged when isSticky true and is fullscreen sets listener to null`() {
        val drawable = mockk<Drawable>()
        val videoData = mockk<TrackingTypeData.TrackingVideoTypeData>(relaxed = true)
        val expectedPosition = 324343L
        every { playerState.mIsFullScreen} returns true
        every {
            ContextCompat.getDrawable(
                mockActivity,
                R.drawable.FullScreenDrawableButtonCollapse
            )
        } returns drawable
        every { utils.createTrackingVideoTypeData() } returns videoData
        every { mPlayer!!.currentPosition } returns expectedPosition

        testObject.onStickyPlayerStateChanged(true)

        verify { mPlayerView.setControllerVisibilityListener(null as StyledPlayerView.ControllerVisibilityListener?) }
    }

    @Test
    fun `onStickyPlayerStateChanged not isSticky`() {
        testObject.onStickyPlayerStateChanged(false)

        verifySequence { mPlayerView.setControllerVisibilityListener(null as StyledPlayerView.ControllerVisibilityListener?) }
    }


}