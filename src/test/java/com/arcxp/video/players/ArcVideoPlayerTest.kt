package com.arcxp.video.players

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.drawable.Drawable
import android.net.Uri
import android.util.Log
import android.util.Pair
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams
import android.view.accessibility.CaptioningManager
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.core.content.ContextCompat
import com.arcxp.commons.testutils.TestUtils.createDefaultVideo
import com.arcxp.sdk.R
import com.arcxp.video.ArcXPVideoConfig
import com.arcxp.video.VideoTracker
import com.arcxp.video.cast.ArcCastManager
import com.arcxp.video.listeners.AdsLoadedListener
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
import androidx.media3.common.Player
import androidx.media3.cast.CastPlayer
import androidx.media3.common.MediaItem
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DataSpec
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.ima.ImaAdsLoader
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.ads.AdsMediaSource
import androidx.media3.ui.PlayerControlView
import androidx.media3.ui.PlayerView
import androidx.media3.exoplayer.source.ads.AdsLoader
import com.google.android.gms.cast.framework.CastContext
import io.mockk.Called
import io.mockk.EqMatcher
import io.mockk.MockKAnnotations
import io.mockk.called
import io.mockk.clearAllMocks
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.runs
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
    private var mLocalPlayer: ExoPlayer? = null

    @RelaxedMockK
    private lateinit var mockView1: View

    @RelaxedMockK
    private lateinit var mockView2: View

    @RelaxedMockK
    private lateinit var mockView3: View

    @RelaxedMockK
    private lateinit var mockView1Parent: ViewGroup

    @RelaxedMockK
    private lateinit var mockView2Parent: ViewGroup

    @RelaxedMockK
    private lateinit var mockView3Parent: ViewGroup

    @RelaxedMockK
    private lateinit var mPlayerView: PlayerView

    @RelaxedMockK
    private lateinit var mockActivity: Activity

    @RelaxedMockK
    private lateinit var mAdsLoader: ImaAdsLoader

    @RelaxedMockK
    private lateinit var mCastPlayer: CastPlayer

    @RelaxedMockK
    private lateinit var mCastContext: CastContext

    @RelaxedMockK
    private lateinit var fullScreenButton: ImageButton

    @RelaxedMockK
    private lateinit var ccButton: ImageButton

    @RelaxedMockK
    private lateinit var volumeButton: ImageButton

    @RelaxedMockK
    private lateinit var shareButton: ImageButton

    @RelaxedMockK
    private lateinit var pipButton: ImageButton

    @RelaxedMockK
    private lateinit var artwork: ImageView

    @RelaxedMockK
    private lateinit var mMediaDataSourceFactory: DataSource.Factory

    @RelaxedMockK
    lateinit var adsMediaSource: AdsMediaSource

    @RelaxedMockK
    private lateinit var mediaSourceFactory: DefaultMediaSourceFactory

    private lateinit var testObject: ArcVideoPlayer

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        every { playerState.mLocalPlayer } returns mLocalPlayer
        every { playerState.mLocalPlayerView } returns mPlayerView
        every { playerState.mCastControlView } returns mCastControlView
        every { playerState.currentPlayer } returns mLocalPlayer
        every { playerState.mCastPlayer } returns mCastPlayer
        every { playerState.mMediaDataSourceFactory } returns mMediaDataSourceFactory
        every { mConfig.activity } returns mockActivity
        every { arcCastManager.getCastContext() } returns mCastContext
        every { mCastControlView!!.findViewById<ImageButton>(R.id.exo_fullscreen) } returns fullScreenButton
        every { mPlayerView.findViewById<ImageButton>(R.id.exo_fullscreen) } returns fullScreenButton

        every { mCastControlView!!.findViewById<ImageButton>(R.id.exo_pip) } returns pipButton
        every { mCastControlView!!.findViewById<ImageButton>(R.id.exo_share) } returns shareButton
        every { mCastControlView!!.findViewById<ImageButton>(R.id.exo_volume) } returns volumeButton
        every { mPlayerView.findViewById<ImageButton>(R.id.exo_volume) } returns volumeButton
        every { mCastControlView!!.findViewById<ImageButton>(R.id.exo_cc) } returns ccButton
        every { mCastControlView!!.findViewById<ImageView>(R.id.exo_artwork) } returns artwork
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
        mockkConstructor(DefaultMediaSourceFactory::class)

        testObject = ArcVideoPlayer(
            playerState = playerState,
            playerStateHelper = playerStateHelper,
            mListener = mListener,
            mConfig = mConfig,
            arcCastManager = arcCastManager,
            utils = utils,
            trackingHelper = trackingHelper,
            captionsManager = captionsManager
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
            mLocalPlayer!!.playWhenReady = true
            mPlayerView.hideController()
        }
        verify { mListener wasNot called }
    }

    @Test
    fun `pausePlay given false calls mPlayer, mPlayerView, trackingHelper if they are not null`() {
        testObject.pausePlay(false)

        verifySequence {
            mLocalPlayer!!.playWhenReady = false
            mPlayerView.hideController()
        }
        verify { mListener wasNot called }
    }

    @Test
    fun `pausePlay when player and view are null`() {
        every { playerState.mLocalPlayer } returns null
        every { playerState.mLocalPlayerView } returns null

        testObject.pausePlay(false)

        verifySequence {
            playerState.mLocalPlayer
        }
        verify { mListener wasNot called }
    }

    @Test
    fun `pausePlay when playerView is null`() {
        every { playerState.mLocalPlayerView } returns null

        testObject.pausePlay(false)

        verifySequence {
            playerState.mLocalPlayer
            playerState.mLocalPlayerView
        }
        verify { mListener wasNot called }
    }

    @Test
    fun `pausePlay when player is null`() {
        every { playerState.mLocalPlayer } returns null

        testObject.pausePlay(false)

        verifySequence {
            playerState.mLocalPlayer
        }
        verify { mListener wasNot called }
    }

    @Test
    fun `pausePlay throws exception, is handled by listener`() {
        val error = " pause Play error"
        val exception = Exception(error)
        every { mLocalPlayer!!.playWhenReady = true } throws exception

        testObject.pausePlay(true)

        verifySequence {
            mLocalPlayer!!.playWhenReady = true
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
        every { mLocalPlayer!!.playWhenReady = true } throws exception

        testObject.start()

        verifyOrder {
            mLocalPlayer!!.playWhenReady = true
            mListener.onError(ArcVideoSDKErrorType.EXOPLAYER_ERROR, error, exception)
        }
    }

    @Test
    fun `start calls mPlayer setPlayWhenReady with true`() {
        testObject.start()

        verifySequence { mLocalPlayer!!.playWhenReady = true }

        verify { mListener wasNot called }
    }

    @Test
    fun `start when mPlayer is null`() {
        every { playerState.mLocalPlayer } returns null

        testObject.start()

        verify { mListener wasNot called }
    }

    @Test
    fun `pause calls mPlayer and trackingHelper if they are not null`() {
        testObject.pause()

        verifySequence {
            mLocalPlayer!!.playWhenReady = false
        }

        verify { mListener wasNot called }
    }

    @Test
    fun `pause when mPlayer is null`() {
        every { playerState.mLocalPlayer } returns null

        testObject.pause()

        verify { mListener wasNot called }
    }

    @Test
    fun `pause throws exception, is handled by listener`() {
        val error = " pause error"
        val exception = Exception(error)
        every { mLocalPlayer!!.playWhenReady = false } throws exception

        testObject.pause()

        verifySequence {
            mLocalPlayer!!.playWhenReady = false
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
    fun `setPlayerKeyListener mPlayerView onKeyListener when key code is KEYCODE_BACK`() {
        val mPlayerViewListener = slot<View.OnKeyListener>()
        val keyEvent = mockk<KeyEvent> {
            every { action } returns KeyEvent.ACTION_UP
        }
        val arcKeyListener = mockk<ArcKeyListener>(relaxed = true)
        every { keyEvent.keyCode } returns KeyEvent.KEYCODE_BACK
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
    fun `setPlayerKeyListener mPlayerView onKeyListener when key code is not KEYCODE_BACK`() {
        val mPlayerViewListener = slot<View.OnKeyListener>()
        val keyEvent = mockk<KeyEvent> {
            every { action } returns KeyEvent.ACTION_UP
        }
        val arcKeyListener = mockk<ArcKeyListener>(relaxed = true)
        every { keyEvent.keyCode } returns KeyEvent.KEYCODE_11
        testObject.setPlayerKeyListener(arcKeyListener)
        verify { mPlayerView.setOnKeyListener(capture(mPlayerViewListener)) }
        clearAllMocks(answers = false)

        assertFalse(mPlayerViewListener.captured.onKey(mockk(), 2321, keyEvent))

        verifySequence {
            keyEvent.action
            keyEvent.keyCode
            arcKeyListener.onKey(keyCode = 2321, keyEvent = keyEvent)
        }
    }

    @Test
    fun `setPlayerKeyListener mPlayerView onKeyListener when key is not back`() {
        val mPlayerViewListener = slot<View.OnKeyListener>()
        val keyEvent = mockk<KeyEvent> {
            every { action } returns KeyEvent.KEYCODE_0
        }
        testObject.setPlayerKeyListener(mockk())
        verify { mPlayerView.setOnKeyListener(capture(mPlayerViewListener)) }
        clearAllMocks(answers = false)

        assertFalse(mPlayerViewListener.captured.onKey(mockk(), 1, keyEvent))

        verifySequence {
            keyEvent.action
        }
    }

    @Test
    fun `setPlayerKeyListener mPlayerView onKeyListener when listener is null`() {
        val mPlayerViewListener = slot<View.OnKeyListener>()
        testObject.setPlayerKeyListener(null)
        verify { mPlayerView.setOnKeyListener(capture(mPlayerViewListener)) }
        clearAllMocks(answers = false)

        assertFalse(mPlayerViewListener.captured.onKey(mockk(), 1, mockk()))
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
        testObject.setPlayerKeyListener(arcKeyListener)
        verify { mCastControlView!!.setOnKeyListener(capture(mCastControlViewListener)) }
        clearAllMocks(answers = false)

        assertFalse(mCastControlViewListener.captured.onKey(mockk(), keyCode, keyEvent))

        verifySequence { arcKeyListener.onKey(keyCode, keyEvent) }
    }

    @Test
    fun `setPlayerKeyListener when player views are null`() {


        every { playerState.mLocalPlayerView } returns null
        every { playerState.mCastControlView } returns null

        testObject.setPlayerKeyListener(listener = null)


    }

    @Test
    fun `setPlayerKeyListener cast key listener when listener is null`() {
        val mCastControlViewListener = slot<View.OnKeyListener>()
        every { playerState.mLocalPlayerView } returns null
        every { mCastControlView!!.setOnKeyListener(capture(mCastControlViewListener)) } just runs
        testObject.setPlayerKeyListener(listener = null)

        assertFalse(mCastControlViewListener.captured.onKey(mockk(), 1, mockk()))
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
    fun `init Cast Player given null views`() {
        val arcVideo = createDefaultVideo()
        every { playerState.mVideo } returns arcVideo
        every { mCastControlView!!.findViewById<ImageButton>(R.id.exo_fullscreen) } returns null
        every { mCastControlView!!.findViewById<ImageButton>(R.id.exo_pip) } returns null
        every { mCastControlView!!.findViewById<ImageButton>(R.id.exo_share) } returns null
        every { mCastControlView!!.findViewById<ImageButton>(R.id.exo_volume) } returns null
        every { mCastControlView!!.findViewById<ImageButton>(R.id.exo_cc) } returns null
        every { mCastControlView!!.findViewById<ImageButton>(R.id.exo_artwork) } returns null

        testObject.playVideo(arcVideo)

        verifyOrder {
            playerState.mCastPlayer = mCastPlayer
            mCastPlayer.addListener(playerListener)
            mCastPlayer.setSessionAvailabilityListener(testObject)
            playerState.mCastControlView = mCastControlView
            mCastControlView!!.id = R.id.wapo_cast_control_view
            mCastControlView!!.player = mCastPlayer
            mCastControlView!!.showTimeoutMs = -1
            mCastControlView!!.findViewById<ImageButton>(R.id.exo_fullscreen)
            mCastControlView!!.findViewById<ImageButton>(R.id.exo_pip)
            mCastControlView!!.findViewById<ImageButton>(R.id.exo_share)
            mCastControlView!!.findViewById<ImageButton>(R.id.exo_volume)
            mCastControlView!!.findViewById<ImageButton>(R.id.exo_cc)
            mCastControlView!!.findViewById<ImageView>(R.id.exo_artwork)
            mListener.addVideoView(mCastControlView)
        }
    }

    @Test
    fun `initCastPlayer artwork with non null artwork url`() {
        val artworkUrl = "art"
        val arcVideo = createDefaultVideo()
        every { playerState.mVideo } returns arcVideo
        every { mConfig.artworkUrl } returns artworkUrl

        testObject.playVideo(arcVideo)

        verifyOrder {
            artwork.visibility = VISIBLE
            utils.loadImageIntoView(artworkUrl, artwork)
        }
    }

    @Test
    fun `initCastPlayer artwork with null artwork url`() {
        val arcVideo = createDefaultVideo()
        every { playerState.mVideo } returns arcVideo
        every { mConfig.artworkUrl } returns null

        testObject.playVideo(arcVideo)

        verifyOrder {
            artwork.visibility = VISIBLE
            mConfig.artworkUrl
        }
    }

    @Test
    fun `initCastPlayer sets pip button visibility to gone`() {
        val arcVideo = createDefaultVideo()
        every { playerState.mVideo } returns arcVideo

        testObject.playVideo(arcVideo)

        verifyOrder {
            pipButton.visibility = GONE
        }
    }

    @Test
    fun `initCastPlayer sets volume visibility and listener`() {
        val arcVideo = createDefaultVideo()
        val onClickListener = slot<View.OnClickListener>()
        every { playerState.mVideo } returns arcVideo
        every { playerState.castMuteOn } returns true
        every { volumeButton.setOnClickListener(capture(onClickListener)) } just runs
        val drawableOn = mockk<Drawable>()
        val drawableOff = mockk<Drawable>()
        mockkStatic(ContextCompat::class)
        every {
            ContextCompat.getDrawable(
                mockActivity.applicationContext,
                R.drawable.MuteDrawableButton
            )
        } returns drawableOn
        every {
            ContextCompat.getDrawable(
                mockActivity.applicationContext,
                R.drawable.MuteOffDrawableButton
            )
        } returns drawableOff
        testObject.playVideo(arcVideo)



        verifyOrder {
            volumeButton.visibility = VISIBLE
            volumeButton.setImageDrawable(drawableOff)


        }
        clearAllMocks(answers = false)
        onClickListener.captured.onClick(mockk())

        verify {
            playerState.castMuteOn = false
            arcCastManager.setMute(false)
            volumeButton.setImageDrawable(drawableOn)
        }

        clearAllMocks(answers = false)
        every { playerState.castMuteOn } returns false
        onClickListener.captured.onClick(mockk())

        verifySequence {
            playerState.castMuteOn
            playerState.castMuteOn = true
            arcCastManager.setMute(true)
            playerState.castMuteOn
            volumeButton.setImageDrawable(drawableOff)
        }
    }

    @Test
    fun `initCastPlayer sets ccButton visibility and listener with subtitle url`() {
        val arcVideo = createDefaultVideo()
        val onClickListener = slot<View.OnClickListener>()
        every { playerState.mVideo } returns arcVideo
        every { playerState.castMuteOn } returns true
        every { playerState.castSubtitlesOn } returns true
        every { ccButton.setOnClickListener(capture(onClickListener)) } just runs
        val ccOn = mockk<Drawable>()
        val ccOff = mockk<Drawable>()
        mockkStatic(ContextCompat::class)
        every {
            ContextCompat.getDrawable(
                mockActivity.applicationContext,
                R.drawable.CcDrawableButton
            )
        } returns ccOn
        every {
            ContextCompat.getDrawable(
                mockActivity.applicationContext,
                R.drawable.CcOffDrawableButton
            )
        } returns ccOff
        testObject.playVideo(arcVideo)




        onClickListener.captured.onClick(mockk())
        verify(exactly = 1) {
            ccButton.visibility = VISIBLE
            ccButton.setImageDrawable(ccOn)
        }
        clearAllMocks(answers = false)
        every { playerState.castSubtitlesOn } returns false
        onClickListener.captured.onClick(mockk())

        verify(exactly = 1) {
            ccButton.setImageDrawable(ccOff)
        }
    }

    @Test
    fun `play video when cast player is null`() {
        every { playerState.mCastPlayer } returns null
        testObject.playVideo(createDefaultVideo())
    }

    @Test
    fun `play Video player on touch listener`() {
        val onTouchListener = slot<View.OnTouchListener>()
        every { mPlayerView.setOnTouchListener(capture(onTouchListener)) } just runs
        every { mConfig.isDisableControlsWithTouch } returns false
        testObject.playVideo(createDefaultVideo())
        val view: View = mockk(relaxed = true)
        val event: MotionEvent = mockk()
        every { event.action } returns MotionEvent.ACTION_UP

        clearAllMocks(answers = false)
        val expectedTimeLinePosition = 123L
        every { playerStateHelper.getCurrentTimelinePosition() } returns expectedTimeLinePosition

        assertFalse(onTouchListener.captured.onTouch(view, event))

        verifySequence {
            trackingHelper.onTouch(event, expectedTimeLinePosition)
        }
    }

    @Test
    fun `initCastPlayer sets ccButton visibility and listener when isLive`() {
        val arcVideo = createDefaultVideo(subtitleUrl = null, isLive = true)
        val onClickListener = slot<View.OnClickListener>()
        every { playerState.mVideo } returns arcVideo
        every { playerState.castMuteOn } returns true
        every { playerState.castSubtitlesOn } returns true
        every { ccButton.setOnClickListener(capture(onClickListener)) } just runs
        val ccOn = mockk<Drawable>()
        val ccOff = mockk<Drawable>()
        mockkStatic(ContextCompat::class)
        every {
            ContextCompat.getDrawable(
                mockActivity.applicationContext,
                R.drawable.CcDrawableButton
            )
        } returns ccOn
        every {
            ContextCompat.getDrawable(
                mockActivity.applicationContext,
                R.drawable.CcOffDrawableButton
            )
        } returns ccOff
        testObject.playVideo(arcVideo)




        onClickListener.captured.onClick(mockk())
        verify(exactly = 1) {
            ccButton.visibility = VISIBLE
            ccButton.setImageDrawable(ccOn)
        }
        clearAllMocks(answers = false)
        every { playerState.castSubtitlesOn } returns false
        onClickListener.captured.onClick(mockk())

        verify(exactly = 1) {
            ccButton.setImageDrawable(ccOff)
        }
    }

    @Test
    fun `initCastPlayer sets ccButton visibility and listener when no subtitle and not live`() {
        val arcVideo = createDefaultVideo(subtitleUrl = null, isLive = false)
        every { playerState.mVideo } returns arcVideo

        testObject.playVideo(arcVideo)
        verify(exactly = 1) {
            ccButton.visibility = GONE
        }
    }

    @Test
    fun `initCastPlayer sets share Button visibility and listener`() {
        val arcVideo = createDefaultVideo(shareUrl = "")
        every { playerState.mShareUrl } returns ""
        every { playerState.mHeadline } returns "headline"
        every { playerState.mVideo } returns arcVideo
        val videoData = mockk<TrackingTypeData.TrackingVideoTypeData>(relaxed = true)
        every { utils.createTrackingVideoTypeData() } returns videoData
        val onClickListener = slot<View.OnClickListener>()
        every { shareButton.setOnClickListener(capture(onClickListener)) } just runs
        val expectedPosition = 3245L
        every { mCastPlayer.currentPosition } returns expectedPosition

        testObject.playVideo(arcVideo)
        verify(exactly = 1) {
            shareButton.visibility = GONE
        }

        onClickListener.captured.onClick(mockk())
        verify {
            videoData.arcVideo = arcVideo
            videoData.position = expectedPosition
            playerStateHelper.onVideoEvent(TrackingType.ON_SHARE, videoData)
            mListener.onShareVideo("headline", "")
        }
        every { playerState.mShareUrl } returns "not empty"
        testObject.playVideo(arcVideo)
        verify(exactly = 1) {
            shareButton.visibility = VISIBLE
        }
    }


    @Test
    fun `initCastPlayer fullScreenListener calls toggleFullScreenCast when cast full screen on`() {
        val arcVideo = createDefaultVideo()
        val onClickListener = slot<View.OnClickListener>()
        val mFullScreenDialog: Dialog = mockk(relaxed = true)
        every { playerState.mVideo } returns arcVideo
        every { playerState.castFullScreenOn } returns true
        every { playerState.mFullScreenDialog } returns mFullScreenDialog
        every { fullScreenButton.setOnClickListener(capture(onClickListener)) } just runs
        testObject.playVideo(arcVideo)
        clearAllMocks(answers = false)
        val drawable = mockk<Drawable>()
        mockkStatic(ContextCompat::class)
        every {
            ContextCompat.getDrawable(
                mockActivity.applicationContext,
                R.drawable.FullScreenDrawableButton
            )
        } returns drawable
        every { playerState.mFullscreenOverlays } returns mockk {
            every { values } returns mutableListOf(
                mockView1,
                mockView2,
                mockView3
            )
        }
        every { mockView1.parent } returns mockView1Parent
        every { mockView2.parent } returns mockView2Parent
        every { mockView3.parent } returns mockView3Parent
        val expectedPosition = 435L
        every { mCastPlayer.currentPosition } returns expectedPosition
        val videoData = mockk<TrackingTypeData.TrackingVideoTypeData>(relaxed = true)
        every { utils.createTrackingVideoTypeData() } returns videoData
        val playerFrame: RelativeLayout = mockk(relaxed = true)
        every { mListener.playerFrame } returns playerFrame
        //toggleFullScreenCast
        onClickListener.captured.onClick(mockk())

        verify(exactly = 1) {
            playerState.castFullScreenOn = false
            mCastControlViewParent.removeView(mCastControlView)
            playerFrame.addView(mCastControlView)
            mFullScreenDialog.setOnDismissListener(null)
            mFullScreenDialog.dismiss()
            videoData.arcVideo = arcVideo
            videoData.position = expectedPosition
            playerStateHelper.onVideoEvent(TrackingType.ON_CLOSE_FULL_SCREEN, videoData)
            trackingHelper.normalScreen()
            mockView1Parent.removeView(mockView1)
            playerFrame.addView(mockView1)
            mockView2Parent.removeView(mockView2)
            playerFrame.addView(mockView2)
            mockView3Parent.removeView(mockView3)
            playerFrame.addView(mockView3)
            fullScreenButton.setImageDrawable(drawable)
        }
    }


    @Test
    fun `initCastPlayer fullScreenListener calls toggleFullScreenCast when cast full screen off`() {
        val arcVideo = createDefaultVideo()
        val onClickListener = slot<View.OnClickListener>()
        val onDismissListener = slot<DialogInterface.OnDismissListener>()
        val mFullScreenDialog: Dialog = mockk(relaxed = true)
        every { mFullScreenDialog.setOnDismissListener(capture(onDismissListener)) } just runs
        val layoutParams: LayoutParams = mockk()
        every { utils.createLayoutParams() } returns layoutParams
        every { playerState.mVideo } returns arcVideo
        every { playerState.castFullScreenOn } returns false
        every { playerState.mFullScreenDialog } returns mFullScreenDialog
        every { fullScreenButton.setOnClickListener(capture(onClickListener)) } just runs
        testObject.playVideo(arcVideo)
        clearAllMocks(answers = false)
        val drawable = mockk<Drawable>()
        mockkStatic(ContextCompat::class)
        every {
            ContextCompat.getDrawable(
                mockActivity.applicationContext,
                R.drawable.FullScreenDrawableButtonCollapse
            )
        } returns drawable
        every { playerState.mFullscreenOverlays } returns mockk {
            every { values } returns mutableListOf(
                mockView1,
                mockView2,
                mockView3
            )
        }
        every {
            ContextCompat.getDrawable(
                mockActivity.applicationContext,
                R.drawable.FullScreenDrawableButton
            )
        } returns drawable
        every { playerState.mFullscreenOverlays } returns mockk {
            every { values } returns mutableListOf(
                mockView1,
                mockView2,
                mockView3
            )
        }

        val videoData = mockk<TrackingTypeData.TrackingVideoTypeData>(relaxed = true)
        every { utils.createTrackingVideoTypeData() } returns videoData
        val playerFrame: RelativeLayout = mockk(relaxed = true)
        every { mListener.playerFrame } returns playerFrame
        every { mockView1.parent } returns mockView1Parent
        every { mockView2.parent } returns mockView2Parent
        every { mockView3.parent } returns mockView3Parent
        val expectedPosition = 435L
        every { mCastPlayer.currentPosition } returns expectedPosition
        //toggleFullScreenCast
        onClickListener.captured.onClick(mockk())

        verify(exactly = 1) {
            playerState.castFullScreenOn = true
            mCastControlViewParent.removeView(mCastControlView)
            mFullScreenDialog.addContentView(mCastControlView!!, layoutParams)
            fullScreenButton.setImageDrawable(drawable)
            playerStateHelper.addOverlayToFullScreen()
            mFullScreenDialog.show()
            mFullScreenDialog.setOnDismissListener(any())
            playerState.mIsFullScreen = true
            playerStateHelper.createTrackingEvent(TrackingType.ON_OPEN_FULL_SCREEN)
            trackingHelper.fullscreen()

        }
        clearAllMocks(answers = false)
        every { playerState.castFullScreenOn } returns true
        onDismissListener.captured.onDismiss(mockk())
        //just runs toggle again
        verify(exactly = 1) {
            playerState.castFullScreenOn = false
            mCastControlViewParent.removeView(mCastControlView)
            playerFrame.addView(mCastControlView)
            mFullScreenDialog.setOnDismissListener(null)
            mFullScreenDialog.dismiss()
            videoData.arcVideo = arcVideo
            videoData.position = expectedPosition
            playerStateHelper.onVideoEvent(TrackingType.ON_CLOSE_FULL_SCREEN, videoData)
            trackingHelper.normalScreen()
            mockView1Parent.removeView(mockView1)
            playerFrame.addView(mockView1)
            mockView2Parent.removeView(mockView2)
            playerFrame.addView(mockView2)
            mockView3Parent.removeView(mockView3)
            playerFrame.addView(mockView3)
            fullScreenButton.setImageDrawable(drawable)
        }

    }

    @Test
    fun `initCastPlayer fullScreenListener calls toggleFullScreenCast when cast full screen off null cast parent and button`() {
        val arcVideo = createDefaultVideo()
        val onClickListener = slot<View.OnClickListener>()
        val onDismissListener = slot<DialogInterface.OnDismissListener>()
        val mFullScreenDialog: Dialog = mockk(relaxed = true)
        every { mFullScreenDialog.setOnDismissListener(capture(onDismissListener)) } just runs
        val layoutParams: LayoutParams = mockk()
        every { utils.createLayoutParams() } returns layoutParams
        every { playerState.mVideo } returns arcVideo
        every { playerState.castFullScreenOn } returns false
        every { playerState.mFullScreenDialog } returns mFullScreenDialog
        every { fullScreenButton.setOnClickListener(capture(onClickListener)) } just runs
        every { mCastControlView!!.parent } returns null
        testObject.playVideo(arcVideo)
        clearAllMocks(answers = false)
        every { playerState.mFullscreenOverlays } returns mockk {
            every { values } returns mutableListOf(
                mockView1,
                mockView2,
                mockView3
            )
        }
        every { mCastControlView!!.findViewById<ImageButton>(R.id.exo_fullscreen) } returns null

        val videoData = mockk<TrackingTypeData.TrackingVideoTypeData>(relaxed = true)
        every { utils.createTrackingVideoTypeData() } returns videoData
        val playerFrame: RelativeLayout = mockk(relaxed = true)
        every { mListener.playerFrame } returns playerFrame
        every { mockView1.parent } returns mockView1Parent
        every { mockView2.parent } returns mockView2Parent
        every { mockView3.parent } returns mockView3Parent
        val expectedPosition = 435L
        every { mCastPlayer.currentPosition } returns expectedPosition
        //toggleFullScreenCast
        onClickListener.captured.onClick(mockk())

        verify(exactly = 1) {
            playerState.castFullScreenOn = true
            mFullScreenDialog.addContentView(mCastControlView!!, layoutParams)
            playerStateHelper.addOverlayToFullScreen()
            mFullScreenDialog.show()
            mFullScreenDialog.setOnDismissListener(any())
            playerState.mIsFullScreen = true
            playerStateHelper.createTrackingEvent(TrackingType.ON_OPEN_FULL_SCREEN)
            trackingHelper.fullscreen()

        }
    }

    @Test
    fun `initCastPlayer fullScreenListener calls toggleFullScreenCast when cast full screen on, null dialog and fullscreen button`() {
        val arcVideo = createDefaultVideo()
        val onClickListener = slot<View.OnClickListener>()
        every { playerState.mVideo } returns arcVideo
        every { playerState.castFullScreenOn } returns true
        every { playerState.mFullScreenDialog } returns null
        every { fullScreenButton.setOnClickListener(capture(onClickListener)) } just runs
        testObject.playVideo(arcVideo)
        clearAllMocks(answers = false)
        every { mCastControlView!!.findViewById<ImageButton>(R.id.exo_fullscreen) } returns null
        every { playerState.mFullscreenOverlays } returns mockk {
            every { values } returns mutableListOf(
                mockView1,
                mockView2,
                mockView3
            )
        }
        every { mockView1.parent } returns mockView1Parent
        every { mockView2.parent } returns mockView2Parent
        every { mockView3.parent } returns mockView3Parent
        val expectedPosition = 435L
        every { mCastPlayer.currentPosition } returns expectedPosition
        val videoData = mockk<TrackingTypeData.TrackingVideoTypeData>(relaxed = true)
        every { utils.createTrackingVideoTypeData() } returns videoData
        val playerFrame: RelativeLayout = mockk(relaxed = true)
        every { mListener.playerFrame } returns playerFrame
        //toggleFullScreenCast
        onClickListener.captured.onClick(mockk())

        verify(exactly = 1) {
            playerState.castFullScreenOn = false
            mCastControlViewParent.removeView(mCastControlView)
            playerFrame.addView(mCastControlView)
            videoData.arcVideo = arcVideo
            videoData.position = expectedPosition
            playerStateHelper.onVideoEvent(TrackingType.ON_CLOSE_FULL_SCREEN, videoData)
            trackingHelper.normalScreen()
            mockView1Parent.removeView(mockView1)
            playerFrame.addView(mockView1)
            mockView2Parent.removeView(mockView2)
            playerFrame.addView(mockView2)
            mockView3Parent.removeView(mockView3)
            playerFrame.addView(mockView3)
        }
    }

    @Test
    fun `playVideo when video id, fallback url, mVideos, cast Manager is null`() {
        val expectedIsLive = true
        val arcVideo = createDefaultVideo(isLive = expectedIsLive, id = null, fallbackUrl = null)
        every { playerState.mVideo } returns arcVideo
        every { playerState.mVideos } returns null
        every { playerState.mLocalPlayer } returns mLocalPlayer
        every { playerState.currentPlayer } returns mLocalPlayer
        testObject = ArcVideoPlayer(
            playerState = playerState,
            playerStateHelper = playerStateHelper,
            mListener = mListener,
            mConfig = mConfig,
            arcCastManager = null,
            utils = utils,
            trackingHelper = trackingHelper,
            captionsManager = captionsManager
        )

        testObject.playVideo(arcVideo)

        verify {
            playerState.mVideoId = ""
            playerState.mVideos
            playerState.mVideo = arcVideo
            playerState.mIsLive = expectedIsLive
            playerState.mHeadline = "headline"
            playerState.mShareUrl = "mShareUrl"
            playerState.mVideoId = ""
            trackingHelper.initVideo("")
            playerStateHelper.initLocalPlayer()
            arcCastManager
        }
    }

    @Test
    fun `playVideo when video id null and has fallback url`() {
        val arcVideo = createDefaultVideo(id = null)
        every { playerState.mVideo } returns arcVideo
        every { playerState.mVideos } returns null
        every { playerState.mLocalPlayer } returns mLocalPlayer
        every { playerState.currentPlayer } returns mLocalPlayer


        testObject.playVideo(arcVideo)

        verify(exactly = 1) {
            playerState.mVideoId = "fallbackUrl"
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
    fun `setCurrentPlayer throws exception preparing ad, logging is disabled`() {
        mockkStatic(Log::class)

        val errorMessage = "i am error"
        val exception = Exception(errorMessage)
        val arcVideo = createDefaultVideo()
        every { playerState.mVideo } returns arcVideo
        every { mAdsLoader.adsLoader } throws exception
        every { mConfig.isLoggingEnabled } returns false
        every { mCastPlayer.isCastSessionAvailable } returns true
        every { playerState.mAdsLoader } returns mAdsLoader
        every { playerState.mVideoId } returns arcVideo.uuid
        testObject.playVideo(arcVideo)

        verify(exactly = 0) {
            Log.e(any(), any())
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
    fun playOnLocal() {
        every { playerState.mVideo } returns createDefaultVideo()
        testObject.playOnLocal()

        verifyOrder {
            captionsManager.createMediaSourceWithCaptions()
            playerState.mVideo

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
        every { mLocalPlayer!!.currentPosition } returns expectedPosition

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
        every { mLocalPlayer!!.currentPosition } returns expectedPosition
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
            mLocalPlayer!!.currentPosition
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
    fun `onActivityResume when player view non null`() {
        val arcVideo = createDefaultVideo()
        every { playerState.mIsFullScreen } returns true
//        every { playerState.mLocalPlayerView } returns null
        every { playerState.mVideo } returns arcVideo
        testObject = spyk(testObject)

        testObject.onActivityResume()

        verify(exactly = 0) {
            testObject.playVideo(arcVideo)
        }
    }

    @Test
    fun `onActivityResume when mVideo is null`() {
        val arcVideo = createDefaultVideo()
        every { playerState.mIsFullScreen } returns true
        every { playerState.mLocalPlayerView } returns null
        every { playerState.mVideo } returns null
        testObject = spyk(testObject)

        testObject.onActivityResume()

        verify(exactly = 0) {
            testObject.playVideo(arcVideo)
        }
    }

    @Test
    fun `onActivityResume when not in fullscreen`() {
        val arcVideo = createDefaultVideo()
        every { playerState.mIsFullScreen } returns false
        every { playerState.mLocalPlayerView } returns null
        every { playerState.mVideo } returns null
        testObject = spyk(testObject)

        testObject.onActivityResume()

        verify(exactly = 0) {
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
            mLocalPlayer!!.volume = expectedVolume
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
            mLocalPlayer!!.volume = expectedVolume
            mPlayerView.findViewById<ImageButton>(R.id.exo_volume)
            ContextCompat.getDrawable(mockActivity, R.drawable.MuteDrawableButton)
            volumeButton.setImageDrawable(drawable)
        }

        verify { mListener wasNot called }
    }

    @Test
    fun `setVolume when mPlayer is null`() {
        every { playerState.mLocalPlayer } returns null

        testObject.setVolume(999f)

        verify { mListener wasNot called }
    }

    @Test
    fun `set Volume throws exception and is handled`() {
        val expectedMessage = "error text"
        val expectedVolume = 0.5f
        val exception = Exception(expectedMessage)
        every { mLocalPlayer!!.volume = expectedVolume } throws exception

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
        every { mLocalPlayer!!.playWhenReady } returns true
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
        every { mLocalPlayer!!.playbackState } returns Player.STATE_READY
        every { mLocalPlayer!!.playWhenReady } returns true
        assertTrue(testObject.isPlaying)
    }

    @Test
    fun `isPlaying returns false when currentPlayer is not null state ready and getPlayWhenReady false`() {
        every { mLocalPlayer!!.playbackState } returns Player.STATE_READY
        every { mLocalPlayer!!.playWhenReady } returns false
        assertFalse(testObject.isPlaying)
    }

    @Test
    fun `isPlaying returns false when currentPlayer is not null state other than ready`() {
        every { mLocalPlayer!!.playbackState } returns Player.STATE_ENDED
        every { mLocalPlayer!!.playWhenReady } returns false
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
    fun `show controls when player view null`() {
        every { playerState.mLocalPlayerView } returns null
        testObject.showControls(false)

        verifySequence { playerState.mLocalPlayerView }
    }

    @Test
    fun `getAdType mPlayer not null and is playing ad gets adGroupTime from mPlayer`() {
        every { mLocalPlayer!!.isPlayingAd } returns true

        assertEquals(0, testObject.adType)
    }

    @Test
    fun `getAdType mPlayer null `() {
        every { playerState.mLocalPlayer } returns null
        every { mLocalPlayer!!.isPlayingAd } returns true

        assertEquals(0, testObject.adType)
    }

    @Test
    fun `getAdType mPlayer not null and is not playing ad returns zero`() {
        every { mLocalPlayer!!.isPlayingAd } returns false


        assertEquals(0, testObject.adType)
    }
    @Test
    fun `getAdType mPlayer not null and is not playing ad returns expected`() {
        val expected = 987234L
        every { mLocalPlayer!!.isPlayingAd } returns true
        every { mLocalPlayer!!.currentPeriodIndex } returns 234
        every { mLocalPlayer!!.currentAdGroupIndex } returns 23434
        every { mLocalPlayer!!.currentTimeline} returns mockk {
            every { getPeriod(234, any())} returns mockk {
                every { getAdGroupTimeUs(23434)} returns expected
            }
        }

        assertEquals(expected, testObject.adType)
    }

    @Test
    fun `getAdType mPlayer null returns zero`() {
        assertEquals(0, testObject.adType)
    }


    @Test
    fun `getPlaybackState returns currentPlayer playbackState when it is not null`() {
        val expectedPlayBackState = 3
        every { mLocalPlayer!!.playbackState } returns expectedPlayBackState



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
    fun `isVideoCaptionEnabled with  startMode off`() {
        every { playerState.mVideo } returns createDefaultVideo(ccStartMode = ArcXPVideoConfig.CCStartMode.OFF)

        assertFalse(testObject.isVideoCaptionEnabled)
    }

    @Test
    fun `isVideoCaptionEnabled throws exception returns false`() {
        every { playerState.mVideo } returns createDefaultVideo()
        mockkStatic(PrefManager::class)
        every {
            PrefManager.getBoolean(
                mockActivity,
                PrefManager.IS_CAPTIONS_ENABLED,
                any()
            )
        } throws Exception()



        assertFalse(testObject.isVideoCaptionEnabled)
    }

    @Test
    fun `isVideoCaptionEnabled with null video returns false`() {
        every { playerState.mVideo } returns null

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

        clearAllMocks(answers = false)

        testObject.setCcButtonDrawable(expectedDrawableIntValue)

        verifySequence { ccButton.setImageDrawable(expectedDrawable) }
    }

    @Test
    fun `setCcButtonDrawable when ccButton is null returns false`() {
        every { playerState.ccButton } returns null
        assertFalse(testObject.setCcButtonDrawable(34597))
    }

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
    fun `seekTo seeks to time`() {
        val seekToMs = 287364

        testObject.seekTo(seekToMs)

        verify(exactly = 1) { mLocalPlayer!!.seekTo(seekToMs.toLong()) }
    }

    @Test
    fun `seekTo when mPlayer is null`() {
        every { playerState.mLocalPlayer } returns null

        testObject.seekTo(101)

        verify { mListener wasNot called }
    }

    @Test
    fun `seekTo throws exception and is handled`() {
        val errorMessage = "error in seek to"
        val exception = Exception(errorMessage)
        every { mLocalPlayer!!.seekTo(any()) } throws exception
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
        every { mLocalPlayer!!.playWhenReady = false } throws exception

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
            mLocalPlayer!!.playWhenReady = false
            mLocalPlayer!!.stop()
            mLocalPlayer!!.seekTo(0)
        }
    }

    @Test
    fun `stop when mPlayer is null`() {
        every { playerState.mLocalPlayer } returns null

        testObject.stop()

        verify { mListener wasNot called }
    }

    @Test
    fun `resume if mPlayer is not null, restarts player and sends tracking event to listener`() {
        testObject.resume()

        verifySequence {
            mLocalPlayer!!.playWhenReady = true
            playerStateHelper.createTrackingEvent(TrackingType.ON_PLAY_RESUMED)
        }
    }

    @Test
    fun `resume when mPlayer is null`() {
        every { playerState.mLocalPlayer } returns null

        testObject.resume()

        verify { mListener wasNot called }
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
        every { playerState.mVideo } returns arcVideo1

        testObject.playVideos(videoList)

        verify {
            playerState.mVideos = videoList
            playerState.mVideo = arcVideo1
            playerState.mHeadline = "headline"
            playerState.mShareUrl = "mShareUrl"
            playerState.mVideoId = "1"
        } //calls play video
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
            mLocalPlayer!!.stop()
            playerState.mLocalPlayer
            mLocalPlayer!!.release()
            playerState.mLocalPlayer = null
            playerState.mTrackSelector
            playerState.mTrackSelector = null
            playerState.mAdsLoader
            playerState.mAdsLoader!!.setPlayer(null)
            playerState.mAdsLoader!!.release()
            playerState.mAdsLoader = null
            playerState.mIsFullScreen
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
            mLocalPlayer!!.stop()
            playerState.mLocalPlayer
            mLocalPlayer!!.release()
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
    fun `release when local player view parents are null`() {


        every { mPlayerView.parent } returns null
        every { mCastControlView!!.parent } returns null
        testObject.release()
    }

    @Test
    fun `release when player state values are null`() {
        every { playerState.mLocalPlayerView } returns null
        every { playerState.videoTrackingSub } returns null
        every { playerState.mLocalPlayer } returns null
        every { playerState.mTrackSelector } returns null
        every { playerState.mAdsLoader } returns null
        every { playerState.mCastPlayer } returns null
        every { playerState.mCastControlView } returns null

        testObject.release()
    }

    @Test
    fun `release when exceptions are thrown ignores them`() {
        every { playerState.mIsFullScreen } returns true
        every { playerState.mAdsLoader } returns mAdsLoader
        every { playerStateHelper.toggleFullScreenDialog(true) } throws Exception()
        every { mPlayerView.parent } throws Exception()
        every { playerState.videoTrackingSub!!.unsubscribe() } throws Exception()
        every { mLocalPlayer!!.release() } throws Exception()
        every { mAdsLoader.release() } throws Exception()
        every { mListener.removePlayerFrame() } throws Exception()
        every { mCastPlayer.release() } throws Exception()
        every { mCastControlView!!.parent } throws Exception()
        testObject.release()
        every { playerState.mIsFullScreen } returns false
        testObject.release()
    }

    @Test
    fun `get id  fetches from player state`() {
        every { playerState.mVideoId } returns "expected"
        assertEquals("expected", testObject.id)
    }

    @Test
    fun `onStickyPlayerStateChanged isSticky not fullscreen`() {
        testObject.onStickyPlayerStateChanged(true)

        verifySequence {
            mPlayerView.apply {
                hideController()
                requestLayout()
                setControllerVisibilityListener(any<PlayerView.ControllerVisibilityListener>())
            }
        }
    }

    @Test
    fun `onStickyPlayerStateChanged isSticky not fullscreen test listener hides controller and requests layout`() {
        val listener = slot<PlayerView.ControllerVisibilityListener>()

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
        val listener = slot<PlayerView.ControllerVisibilityListener>()
        testObject.onStickyPlayerStateChanged(true)

        verify { mPlayerView.setControllerVisibilityListener(capture(listener)) }


        clearMocks(mPlayerView)
        listener.captured.onVisibilityChanged(PlayerControlView.INVISIBLE)

        verify { mPlayerView wasNot Called }
    }

    @Test
    fun `onStickyPlayerStateChanged test listener does nothing if local player view null`() {
        val listener = slot<PlayerView.ControllerVisibilityListener>()
        testObject.onStickyPlayerStateChanged(true)

        verify { mPlayerView.setControllerVisibilityListener(capture(listener)) }


        clearAllMocks(answers = false)
        every { playerState.mLocalPlayerView } returns null
        listener.captured.onVisibilityChanged(PlayerControlView.INVISIBLE)

        verifySequence { playerState.mLocalPlayerView }
    }

    @Test
    fun `onStickyPlayerStateChanged when isSticky true and is fullscreen sets listener to null`() {
        val drawable = mockk<Drawable>()
        val videoData = mockk<TrackingTypeData.TrackingVideoTypeData>(relaxed = true)
        val expectedPosition = 324343L
        every { playerState.mIsFullScreen } returns true
        every {
            ContextCompat.getDrawable(
                mockActivity,
                R.drawable.FullScreenDrawableButtonCollapse
            )
        } returns drawable
        every { utils.createTrackingVideoTypeData() } returns videoData
        every { mLocalPlayer!!.currentPosition } returns expectedPosition

        testObject.onStickyPlayerStateChanged(true)

        verify { mPlayerView.setControllerVisibilityListener(null as PlayerView.ControllerVisibilityListener?) }
    }

    @Test
    fun `onStickyPlayerStateChanged not isSticky`() {
        testObject.onStickyPlayerStateChanged(false)

        verifySequence { mPlayerView.setControllerVisibilityListener(null as PlayerView.ControllerVisibilityListener?) }
    }

    @Test
    fun `onStickyPlayerStateChanged with null player view`() {
        every { playerState.mLocalPlayerView } returns null
        testObject.onStickyPlayerStateChanged(false)

        verifySequence { playerState.mLocalPlayerView }
    }

    @Test
    fun `is Closed Caption Turned On`() {
        mockkStatic(PrefManager::class)
        every {
            PrefManager.getBoolean(
                mockActivity,
                PrefManager.IS_CAPTIONS_ENABLED,
                false
            )
        } returns true

        testObject.isClosedCaptionTurnedOn
        verifySequence {
            PrefManager.getBoolean(
                mockActivity,
                PrefManager.IS_CAPTIONS_ENABLED,
                false
            )
        }
    }

    @Test
    fun `getters for coverage`() {
        testObject.adEventListener
        testObject.playerListener
        testObject.playerState
    }

    @Test
    fun `addVideo adds to mVideos`() {
        val mVideos = mutableListOf<ArcVideo>()
        every { playerState.mVideos } returns mVideos
        val newVideo = createDefaultVideo()

        testObject.addVideo(newVideo)

        assert(mVideos.contains(newVideo))
    }

    @Test
    fun `addVideo does not add when mVideos null`() {
        every { playerState.mVideos } returns null

        testObject.addVideo(createDefaultVideo())

        verifySequence {
            playerState.mVideos
        }
    }

    @Test
    fun `setFullscreenListener sets in player state`() {
        val arcKeyListener: ArcKeyListener = mockk()

        testObject.setFullscreenListener(listener = arcKeyListener)

        verifySequence {
            playerState.mArcKeyListener = arcKeyListener
        }
    }

    @Test
    fun `setFullscreenUi when null fullscreen button`() {
        every { mPlayerView.findViewById<ImageButton>(R.id.exo_fullscreen) } returns null

        testObject.setFullscreenUi(full = true)

        verifySequence {
            trackingHelper.fullscreen()
            playerState.mLocalPlayerView
            mPlayerView.findViewById<ImageButton>(R.id.exo_fullscreen)
            playerState.mIsFullScreen = true
            playerStateHelper.createTrackingEvent(TrackingType.ON_OPEN_FULL_SCREEN)
        }

    }

    @Test
    fun `setFullscreenUi when null playerView and player, not sticky`() {
        every { playerState.mLocalPlayer } returns null
        every { playerState.mLocalPlayerView } returns null
        every { mCastControlView!!.findViewById<ImageButton>(R.id.exo_fullscreen) } returns null
        every { mListener.isStickyPlayer } returns false
        val videoData = mockk<TrackingTypeData.TrackingVideoTypeData>(relaxed = true)
        every { utils.createTrackingVideoTypeData() } returns videoData

        testObject.setFullscreenUi(full = false)

        verifySequence {
            trackingHelper.normalScreen()
            playerState.mLocalPlayerView
            playerState.mIsFullScreen = false
            utils.createTrackingVideoTypeData()
            videoData.arcVideo = playerState.mVideo
            playerState.mLocalPlayer
            playerStateHelper.onVideoEvent(TrackingType.ON_CLOSE_FULL_SCREEN, videoData)
        }
    }

    @Test
    fun `setFullscreenUi when not sticky`() {
        every { playerState.mLocalPlayer } returns null
        every { mListener.isStickyPlayer } returns false
        val videoData = mockk<TrackingTypeData.TrackingVideoTypeData>(relaxed = true)
        every { utils.createTrackingVideoTypeData() } returns videoData

        testObject.setFullscreenUi(full = false)

        verifySequence {
            trackingHelper.normalScreen()
            playerState.mLocalPlayerView
            playerState.mLocalPlayerView
            mPlayerView.findViewById<ImageButton>(R.id.exo_fullscreen)
            playerState.mIsFullScreen = false
            utils.createTrackingVideoTypeData()
            videoData.arcVideo = playerState.mVideo
            playerState.mLocalPlayer
            playerStateHelper.onVideoEvent(TrackingType.ON_CLOSE_FULL_SCREEN, videoData)
        }
    }

    @Test
    fun `onKeyEvent calls method on player view true`() {
        val keyEvent: KeyEvent = mockk()
        every { mPlayerView.dispatchKeyEvent(keyEvent) } returns true

        assertTrue(testObject.onKeyEvent(event = keyEvent))

        verifySequence {
            playerState.mLocalPlayerView
            mPlayerView.dispatchKeyEvent(keyEvent)
        }
    }

    @Test
    fun `onKeyEvent calls method on player view false`() {
        val keyEvent: KeyEvent = mockk()
        every { mPlayerView.dispatchKeyEvent(keyEvent) } returns false

        assertFalse(testObject.onKeyEvent(event = keyEvent))

        verifySequence {
            playerState.mLocalPlayerView
            mPlayerView.dispatchKeyEvent(keyEvent)
        }
    }

    @Test
    fun `onKeyEvent when player view is null`() {
        val keyEvent: KeyEvent = mockk()
        every { playerState.mLocalPlayerView } returns null

        assertFalse(testObject.onKeyEvent(event = keyEvent))

        verifySequence {
            playerState.mLocalPlayerView
        }
    }

    @Test
    fun `getCurrentPosition returns value from state`() {
        val expected = 123L
        every { mLocalPlayer!!.currentPosition } returns expected

        val actual = testObject.currentPosition

        assertEquals(expected, actual)
    }

    @Test
    fun `getCurrentPosition returns 0 when currentPlayer is null`() {
        val expected = 0L
        every { playerState.currentPlayer } returns null

        val actual = testObject.currentPosition

        assertEquals(expected, actual)
    }

    @Test
    fun `getCurrentVideoDuration returns value from state`() {
        val expected = 123L
        every { mLocalPlayer!!.duration } returns expected

        val actual = testObject.currentVideoDuration

        assertEquals(expected, actual)
    }

    @Test
    fun `getCurrentVideoDuration returns 0 when currentPlayer is null`() {
        val expected = 0L
        every { playerState.currentPlayer } returns null

        val actual = testObject.currentVideoDuration

        assertEquals(expected, actual)
    }

    @Test
    fun `getCurrentTimelinePosition returns expected`() {
        val expected = 123L
        every { playerStateHelper.getCurrentTimelinePosition() } returns expected

        val actual = testObject.currentTimelinePosition

        assertEquals(expected, actual)
    }

    @Test
    fun `toggleCaptions calls captions manager`() {
        testObject.toggleCaptions()

        verifySequence {
            captionsManager.showCaptionsSelectionDialog()
        }
    }

    @Test
    fun `isFullScreen returns expected`() {
        val expected = true
        every { playerState.mIsFullScreen } returns expected

        val actual = testObject.isFullScreen

        assertEquals(expected, actual)
    }

    @Test
    fun `getPlayControls returns expected`() {
        assertEquals(mPlayerView, testObject.playControls)
    }

    @Test
    fun `isClosedCaptionAvailable returns expected`() {
        val expected = true
        every { captionsManager.isClosedCaptionAvailable() } returns expected

        val actual = testObject.isClosedCaptionAvailable

        assertEquals(expected, actual)
    }

    @Test
    fun `enableClosedCaption returns expected`() {
        val expected = true
        every { captionsManager.enableClosedCaption(expected) } returns expected

        val actual = testObject.enableClosedCaption(expected)

        assertEquals(expected, actual)
    }

    @Test
    fun `isCasting when true`() {
        every { playerState.currentPlayer } returns mCastPlayer

        assertTrue { testObject.isCasting() }
    }

    @Test
    fun `isCasting when false`() {
        every { playerState.currentPlayer } returns mLocalPlayer

        assertFalse(testObject.isCasting())
    }

    @Test
    fun `disableControls disables controller when not disabled globally`() {
        every { mConfig.isDisableControlsFully } returns false

        testObject.disableControls()

        verifySequence {
            playerState.disabledControlsForAd = true
            playerState.adPlaying = true
            playerState.mLocalPlayerView
            mPlayerView.useController = false
        }
    }

    @Test
    fun `disableControls when local player view null`() {
        every { mConfig.isDisableControlsFully } returns false
        every { playerState.mLocalPlayerView } returns null
        testObject.disableControls()

        verifySequence {
            playerState.disabledControlsForAd = true
            playerState.adPlaying = true
            playerState.mLocalPlayerView
        }
    }

    @Test
    fun `disableControls when controller disabled globally`() {
        every { mConfig.isDisableControlsFully } returns true

        testObject.disableControls()

        verifySequence {
            playerState.disabledControlsForAd = true
            playerState.adPlaying = true
        }
    }

    @Test
    fun `addToCast adds media item`() {
        val arcVideo = createDefaultVideo()
        val mediaItem: MediaItem = mockk()
        every { playerState.mVideo } returns arcVideo
        mockkObject(ArcCastManager.Companion)

        every { ArcCastManager.createMediaItem(arcVideo = arcVideo) } returns mediaItem

        testObject.addToCast()

        verifySequence {
            mCastPlayer.addMediaItem(mediaItem)
        }
    }

    @Test
    fun `addToCast throws exception and sends error through listener`() {
        val arcVideo = createDefaultVideo()
        val id = "id"
        every { playerState.mVideo } returns arcVideo
        every { playerState.mCastPlayer } throws Exception()
        every { mListener.sessionId } returns id


        testObject.addToCast()

        verifySequence {
            mListener.sessionId
            mListener.onTrackingEvent(
                TrackingType.ON_ERROR_OCCURRED,
                TrackingTypeData.TrackingErrorTypeData(
                    arcVideo,
                    id,
                    null
                )
            )
        }
    }

    @Test
    fun `onCastSessionUnavailable calls setCurrentPlayer then startVideoOnCurrentPlayer then playOnLocal when going to local from cast`() {
        val expectedPosition = 38743L
        val savedPosition = 38374L
        val expectedId = "234"
        val arcVideo =
            createDefaultVideo(id = expectedId, shouldPlayAds = true, autoStartPlay = true)
        val adsLoadedListener: AdsLoadedListener = mockk()
        val videoTracker: VideoTracker = mockk()
        val artworkUrl = "art"
        every { mConfig.artworkUrl } returns artworkUrl
        every { mListener.getSavedPosition(expectedId) } returns savedPosition
        every { playerState.mAdsLoader } returns mAdsLoader
        every { playerState.mVideoId } returns expectedId
        every { playerState.mVideo } returns arcVideo
        every { playerState.currentPlayer } returnsMany listOf(mCastPlayer, mCastPlayer, mLocalPlayer)
        every { mCastPlayer.playbackState } returns Player.STATE_READY
        every { mCastPlayer.currentPosition } returns expectedPosition
        every {
            utils.createAdsLoadedListener(
                mListener,
                arcVideo,
                testObject
            )
        } returns adsLoadedListener
        mockkObject(VideoTracker.Companion)
        every {
            VideoTracker.getInstance(
                mListener, mLocalPlayer as ExoPlayer, trackingHelper,
                playerState.mIsLive, mockActivity
            )
        } returns videoTracker
        mockkObject(com.arcxp.commons.util.Utils)
        every { com.arcxp.commons.util.Utils.createTimeStamp() } returns "12345"
        val lambdaSlot = slot<AdsLoader.Provider>()
        every {
            constructedWith<DefaultMediaSourceFactory>(EqMatcher(mMediaDataSourceFactory)).setLocalAdInsertionComponents(
                capture(lambdaSlot),
                mPlayerView
            )
        } returns mediaSourceFactory
        mockkStatic(Uri::class)
        val dataSpec: DataSpec = mockk()
        val adUri: Uri = mockk()
        every { utils.createDataSpec(adUri) } returns dataSpec
        every { Uri.parse("addTagUrl12345") } returns adUri
        every { adUri.toString() } returns "adUri"
        val pairSlot = slot<Pair<String, String>>()
        every {
            utils.createAdsMediaSource(
                contentMediaSource,
                dataSpec,
                capture(pairSlot),
                mediaSourceFactory,
                mAdsLoader,
                mPlayerView
            )
        } returns adsMediaSource
        testObject.onCastSessionUnavailable()

        verify(atLeast = 1) {
            //setCurrentPlayer
            mPlayerView.visibility = VISIBLE
            playerState.currentPlayView = mPlayerView
            mCastControlView!!.hide()
            mCastControlView!!.keepScreenOn = false
            mListener.setSavedPosition(expectedId, expectedPosition)
            mAdsLoader.adsLoader!!.addAdsLoadedListener(adsLoadedListener)
            mAdsLoader.setPlayer(mLocalPlayer)
            mCastPlayer.stop()
            mCastPlayer.clearMediaItems()
            playerState.currentPlayer = mLocalPlayer
            playerState.mVideoTracker = videoTracker
            mLocalPlayer!!.playWhenReady = true
            //playOnLocal
            mLocalPlayer!!.setMediaSource(adsMediaSource)
            mLocalPlayer!!.prepare()
            //back
            mLocalPlayer!!.seekTo(savedPosition)
            trackingHelper.onPlaybackStart()
        }
        clearAllMocks(answers = false)
        assertEquals(mAdsLoader, lambdaSlot.captured.getAdsLoader(mockk()))
    }


    @Test
    fun `onCastSessionUnavailable calls setCurrentPlayer then startVideoOnCurrentPlayer then playOnLocal when going to local from cast, should not play ads`() {
        val expectedPosition = 38743L
        val savedPosition = 38374L
        val expectedId = "234"
        val arcVideo =
            createDefaultVideo(id = expectedId, shouldPlayAds = false, autoStartPlay = true)
        val adsLoadedListener: AdsLoadedListener = mockk()
        val videoTracker: VideoTracker = mockk()
        val artworkUrl = "art"
        every { mConfig.artworkUrl } returns artworkUrl
        every { mListener.getSavedPosition(expectedId) } returns savedPosition
         every { playerState.mVideoId } returns expectedId
        every { playerState.mVideo } returns arcVideo
        every { playerState.currentPlayer } returnsMany listOf(mCastPlayer, mCastPlayer, mLocalPlayer)
        every { mCastPlayer.playbackState } returns Player.STATE_READY
        every { mCastPlayer.currentPosition } returns expectedPosition
        every {
            utils.createAdsLoadedListener(
                mListener,
                arcVideo,
                testObject
            )
        } returns adsLoadedListener
        mockkObject(VideoTracker.Companion)
        every {
            VideoTracker.getInstance(
                mListener, mLocalPlayer as ExoPlayer, trackingHelper,
                playerState.mIsLive, mockActivity
            )
        } returns videoTracker
        mockkObject(com.arcxp.commons.util.Utils)
        every { com.arcxp.commons.util.Utils.createTimeStamp() } returns "12345"
        val lambdaSlot = slot<AdsLoader.Provider>()
        every {
            constructedWith<DefaultMediaSourceFactory>(EqMatcher(mMediaDataSourceFactory)).setLocalAdInsertionComponents(
                capture(lambdaSlot),
                mPlayerView
            )
        } returns mediaSourceFactory
        mockkStatic(Uri::class)
        val dataSpec: DataSpec = mockk()
        val adUri: Uri = mockk()
        every { utils.createDataSpec(adUri) } returns dataSpec
        every { Uri.parse("addTagUrl12345") } returns adUri
        every { adUri.toString() } returns "adUri"
        val pairSlot = slot<Pair<String, String>>()
        every {
            utils.createAdsMediaSource(
                contentMediaSource,
                dataSpec,
                capture(pairSlot),
                mediaSourceFactory,
                mAdsLoader,
                mPlayerView
            )
        } returns adsMediaSource
        testObject.onCastSessionUnavailable()

        verify(atLeast = 1) {
            //setCurrentPlayer
            mPlayerView.visibility = VISIBLE
            playerState.currentPlayView = mPlayerView
            mCastControlView!!.hide()
            mCastControlView!!.keepScreenOn = false
            mListener.setSavedPosition(expectedId, expectedPosition)
            mCastPlayer.stop()
            mCastPlayer.clearMediaItems()
            playerState.currentPlayer = mLocalPlayer
            playerState.mVideoTracker = videoTracker
            mLocalPlayer!!.playWhenReady = true
            //playOnLocal
            mLocalPlayer!!.setMediaSource(contentMediaSource)
            mLocalPlayer!!.prepare()
            //back
            mLocalPlayer!!.seekTo(savedPosition)
            trackingHelper.onPlaybackStart()
        }
    }
    @Test
    fun `onCastSessionUnavailable calls setCurrentPlayer then startVideoOnCurrentPlayer then playOnLocal when going to local from cast, ad tag url is empty`() {
        val expectedPosition = 38743L
        val savedPosition = 38374L
        val expectedId = "234"
        val arcVideo =
            createDefaultVideo(id = expectedId, shouldPlayAds = true, autoStartPlay = true, adTagUrl = "")
        val adsLoadedListener: AdsLoadedListener = mockk()
        val videoTracker: VideoTracker = mockk()
        val artworkUrl = "art"
        every { mConfig.artworkUrl } returns artworkUrl
        every { mListener.getSavedPosition(expectedId) } returns savedPosition
        every { playerState.mAdsLoader } returns mAdsLoader
        every { playerState.mVideoId } returns expectedId
        every { playerState.mVideo } returns arcVideo
        every { playerState.currentPlayer } returnsMany listOf(mCastPlayer, mCastPlayer, mLocalPlayer)
        every { mCastPlayer.playbackState } returns Player.STATE_READY
        every { mCastPlayer.currentPosition } returns expectedPosition
        every {
            utils.createAdsLoadedListener(
                mListener,
                arcVideo,
                testObject
            )
        } returns adsLoadedListener
        mockkObject(VideoTracker.Companion)
        every {
            VideoTracker.getInstance(
                mListener, mLocalPlayer as ExoPlayer, trackingHelper,
                playerState.mIsLive, mockActivity
            )
        } returns videoTracker
        mockkObject(com.arcxp.commons.util.Utils)
        every { com.arcxp.commons.util.Utils.createTimeStamp() } returns "12345"
        val lambdaSlot = slot<AdsLoader.Provider>()
        every {
            constructedWith<DefaultMediaSourceFactory>(EqMatcher(mMediaDataSourceFactory)).setLocalAdInsertionComponents(
                capture(lambdaSlot),
                mPlayerView
            )
        } returns mediaSourceFactory
        mockkStatic(Uri::class)
        val dataSpec: DataSpec = mockk()
        val adUri: Uri = mockk()
        every { utils.createDataSpec(adUri) } returns dataSpec
        every { Uri.parse("addTagUrl12345") } returns adUri
        every { adUri.toString() } returns "adUri"
        val pairSlot = slot<Pair<String, String>>()
        every {
            utils.createAdsMediaSource(
                contentMediaSource,
                dataSpec,
                capture(pairSlot),
                mediaSourceFactory,
                mAdsLoader,
                mPlayerView
            )
        } returns adsMediaSource
        testObject.onCastSessionUnavailable()

        verify(atLeast = 1) {
            //setCurrentPlayer
            mPlayerView.visibility = VISIBLE
            playerState.currentPlayView = mPlayerView
            mCastControlView!!.hide()
            mCastControlView!!.keepScreenOn = false
            mListener.setSavedPosition(expectedId, expectedPosition)
            mCastPlayer.stop()
            mCastPlayer.clearMediaItems()
            playerState.currentPlayer = mLocalPlayer
            playerState.mVideoTracker = videoTracker
            mLocalPlayer!!.playWhenReady = true
            //playOnLocal
            mLocalPlayer!!.setMediaSource(contentMediaSource)
            mLocalPlayer!!.prepare()
            //back
            mLocalPlayer!!.seekTo(savedPosition)
            trackingHelper.onPlaybackStart()
        }
    }
    @Test
    fun `onCastSessionUnavailable null cast view for coverage`() {
        val expectedPosition = 38743L
        val savedPosition = 38374L
        val expectedId = "234"
        val arcVideo =
            createDefaultVideo(id = expectedId, shouldPlayAds = true, autoStartPlay = true, adTagUrl = "")
        val adsLoadedListener: AdsLoadedListener = mockk()
        val videoTracker: VideoTracker = mockk()
        val artworkUrl = "art"
        every { mConfig.artworkUrl } returns artworkUrl
        every { mListener.getSavedPosition(expectedId) } returns savedPosition
        every { playerState.mAdsLoader } returns mAdsLoader
        every { playerState.mVideoId } returns expectedId
        every { playerState.mVideo } returns arcVideo
        every { playerState.currentPlayer } returnsMany listOf(mCastPlayer, mCastPlayer, mLocalPlayer)
        every { playerState.mCastControlView } returns null
        every { mCastPlayer.playbackState } returns Player.STATE_READY
        every { mCastPlayer.currentPosition } returns expectedPosition
        every {
            utils.createAdsLoadedListener(
                mListener,
                arcVideo,
                testObject
            )
        } returns adsLoadedListener
        mockkObject(VideoTracker.Companion)
        every {
            VideoTracker.getInstance(
                mListener, mLocalPlayer as ExoPlayer, trackingHelper,
                playerState.mIsLive, mockActivity
            )
        } returns videoTracker
        mockkObject(com.arcxp.commons.util.Utils)
        every { com.arcxp.commons.util.Utils.createTimeStamp() } returns "12345"
        val lambdaSlot = slot<AdsLoader.Provider>()
        every {
            constructedWith<DefaultMediaSourceFactory>(EqMatcher(mMediaDataSourceFactory)).setLocalAdInsertionComponents(
                capture(lambdaSlot),
                mPlayerView
            )
        } returns mediaSourceFactory
        mockkStatic(Uri::class)
        val dataSpec: DataSpec = mockk()
        val adUri: Uri = mockk()
        every { utils.createDataSpec(adUri) } returns dataSpec
        every { Uri.parse("addTagUrl12345") } returns adUri
        every { adUri.toString() } returns "adUri"
        val pairSlot = slot<Pair<String, String>>()
        every {
            utils.createAdsMediaSource(
                contentMediaSource,
                dataSpec,
                capture(pairSlot),
                mediaSourceFactory,
                mAdsLoader,
                mPlayerView
            )
        } returns adsMediaSource
        testObject.onCastSessionUnavailable()
    }

    @Test
    fun `onCastSessionAvailable calls setCurrentPlayer then startVideoOnCurrentPlayer then playOnCast when going to cast from local`() {
        val savedPosition = 38374L
        val expectedId = "234"
        val arcVideo =
            createDefaultVideo(id = expectedId, shouldPlayAds = true, autoStartPlay = true)
        val adsLoadedListener: AdsLoadedListener = mockk()
        val videoTracker: VideoTracker = mockk()
        val artworkUrl = "art"
        every { mConfig.artworkUrl } returns artworkUrl
        every { mListener.getSavedPosition(expectedId) } returns savedPosition
        every { playerState.mAdsLoader } returns mAdsLoader
        every { playerState.mVideoId } returns expectedId
        every { playerState.mVideo } returns arcVideo
        every { playerState.currentPlayer } returnsMany listOf(mLocalPlayer, mLocalPlayer, mCastPlayer)
        every { mLocalPlayer!!.playbackState } returns Player.STATE_ENDED
        every {
            utils.createAdsLoadedListener(
                mListener,
                arcVideo,
                testObject
            )
        } returns adsLoadedListener
        mockkObject(VideoTracker.Companion)
        every {
            VideoTracker.getInstance(
                mListener, mCastPlayer, trackingHelper,
                playerState.mIsLive, mockActivity
            )
        } returns videoTracker
        mockkObject(com.arcxp.commons.util.Utils)
        every { com.arcxp.commons.util.Utils.createTimeStamp() } returns "12345"
        val lambdaSlot = slot<AdsLoader.Provider>()
        every {
            constructedWith<DefaultMediaSourceFactory>(EqMatcher(mMediaDataSourceFactory)).setLocalAdInsertionComponents(
                capture(lambdaSlot),
                mPlayerView
            )
        } returns mediaSourceFactory
        mockkStatic(Uri::class)
        val dataSpec: DataSpec = mockk()
        val adUri: Uri = mockk()
        every { utils.createDataSpec(adUri) } returns dataSpec
        every { Uri.parse("addTagUrl12345") } returns adUri
        every { adUri.toString() } returns "adUri"
        val pairSlot = slot<Pair<String, String>>()
        every {
            utils.createAdsMediaSource(
                contentMediaSource,
                dataSpec,
                capture(pairSlot),
                mediaSourceFactory,
                mAdsLoader,
                mPlayerView
            )
        } returns adsMediaSource
        testObject.onCastSessionAvailable()

        verify(atLeast = 1) {
            //setCurrentPlayer
            mPlayerView.visibility = GONE
            mCastControlView!!.show()
            mCastControlView!!.keepScreenOn = true
            playerState.currentPlayView = mCastControlView
            mAdsLoader.adsLoader!!.addAdsLoadedListener(adsLoadedListener)
            mAdsLoader.setPlayer(mLocalPlayer)
            mLocalPlayer!!.stop()
            mLocalPlayer!!.clearMediaItems()
            playerState.currentPlayer = mCastPlayer
            playerState.mVideoTracker = videoTracker

            mCastPlayer.playWhenReady = true

            //playOnCast
            arcCastManager.doCastSession(
                video = arcVideo,
                position = savedPosition,
                artWorkUrl = artworkUrl
            )
            //back
            mCastPlayer.seekTo(savedPosition)
            trackingHelper.onPlaybackStart()
        }
    }
    @Test
    fun `onCastSessionAvailable calls setCurrentPlayer then startVideoOnCurrentPlayer then playOnCast but arc cast manager is null`() {
        testObject = ArcVideoPlayer(
            playerState = playerState,
            playerStateHelper = playerStateHelper,
            mListener = mListener,
            mConfig = mConfig,
            arcCastManager = null,
            utils = utils,
            trackingHelper = trackingHelper,
            captionsManager = captionsManager
        )
        testObject.playerListener = playerListener
        testObject.adEventListener = adEventListener
        val savedPosition = 38374L
        val expectedId = "234"
        val arcVideo =
            createDefaultVideo(id = expectedId, shouldPlayAds = true, autoStartPlay = true)
        val adsLoadedListener: AdsLoadedListener = mockk()
        val videoTracker: VideoTracker = mockk()
        val artworkUrl = "art"
        every { mConfig.artworkUrl } returns artworkUrl
        every { mListener.getSavedPosition(expectedId) } returns savedPosition
        every { playerState.mAdsLoader } returns mAdsLoader
        every { playerState.mVideoId } returns expectedId
        every { playerState.mVideo } returns arcVideo
        every { playerState.currentPlayer } returnsMany listOf(mLocalPlayer, mLocalPlayer, mCastPlayer)
        every { mLocalPlayer!!.playbackState } returns Player.STATE_ENDED
        every {
            utils.createAdsLoadedListener(
                mListener,
                arcVideo,
                testObject
            )
        } returns adsLoadedListener
        mockkObject(VideoTracker.Companion)
        every {
            VideoTracker.getInstance(
                mListener, mCastPlayer, trackingHelper,
                playerState.mIsLive, mockActivity
            )
        } returns videoTracker
        mockkObject(com.arcxp.commons.util.Utils)
        every { com.arcxp.commons.util.Utils.createTimeStamp() } returns "12345"
        val lambdaSlot = slot<AdsLoader.Provider>()
        every {
            constructedWith<DefaultMediaSourceFactory>(EqMatcher(mMediaDataSourceFactory)).setLocalAdInsertionComponents(
                capture(lambdaSlot),
                mPlayerView
            )
        } returns mediaSourceFactory
        mockkStatic(Uri::class)
        val dataSpec: DataSpec = mockk()
        val adUri: Uri = mockk()
        every { utils.createDataSpec(adUri) } returns dataSpec
        every { Uri.parse("addTagUrl12345") } returns adUri
        every { adUri.toString() } returns "adUri"
        val pairSlot = slot<Pair<String, String>>()
        every {
            utils.createAdsMediaSource(
                contentMediaSource,
                dataSpec,
                capture(pairSlot),
                mediaSourceFactory,
                mAdsLoader,
                mPlayerView
            )
        } returns adsMediaSource
        testObject.onCastSessionAvailable()

        verify(atLeast = 1) {
            //setCurrentPlayer
            mPlayerView.visibility = GONE
            mCastControlView!!.show()
            mCastControlView!!.keepScreenOn = true
            playerState.currentPlayView = mCastControlView
            mAdsLoader.adsLoader!!.addAdsLoadedListener(adsLoadedListener)
            mAdsLoader.setPlayer(mLocalPlayer)
            mLocalPlayer!!.stop()
            mLocalPlayer!!.clearMediaItems()
            playerState.currentPlayer = mCastPlayer
            playerState.mVideoTracker = videoTracker

            mCastPlayer.playWhenReady = true

            //back
            mCastPlayer.seekTo(savedPosition)
            trackingHelper.onPlaybackStart()
        }
    }
    @Test
    fun `onCastSessionAvailable calls setCurrentPlayer then startVideoOnCurrentPlayer then playOnCast throws exception and calls listener`() {
        val sessionId = "session ID"
        val savedPosition = 38374L
        val expectedId = "234"
        val arcVideo =
            createDefaultVideo(id = expectedId, shouldPlayAds = true, autoStartPlay = true)
        val adsLoadedListener: AdsLoadedListener = mockk()
        val videoTracker: VideoTracker = mockk()
        val artworkUrl = "art"
        every { arcCastManager.doCastSession(any(), any(), any()) } throws Exception()
        every { mListener.sessionId } returns sessionId
        every { mConfig.artworkUrl } returns artworkUrl
        every { mListener.getSavedPosition(expectedId) } returns savedPosition
        every { playerState.mAdsLoader } returns mAdsLoader
        every { playerState.mVideoId } returns expectedId
        every { playerState.mVideo } returns arcVideo
        every { playerState.currentPlayer } returnsMany listOf(mLocalPlayer, mLocalPlayer, mCastPlayer)
        every { mLocalPlayer!!.playbackState } returns Player.STATE_ENDED
        every {
            utils.createAdsLoadedListener(
                mListener,
                arcVideo,
                testObject
            )
        } returns adsLoadedListener
        mockkObject(VideoTracker.Companion)
        every {
            VideoTracker.getInstance(
                mListener, mCastPlayer, trackingHelper,
                playerState.mIsLive, mockActivity
            )
        } returns videoTracker
        mockkObject(com.arcxp.commons.util.Utils)
        every { com.arcxp.commons.util.Utils.createTimeStamp() } returns "12345"
        val lambdaSlot = slot<AdsLoader.Provider>()
        every {
            constructedWith<DefaultMediaSourceFactory>(EqMatcher(mMediaDataSourceFactory)).setLocalAdInsertionComponents(
                capture(lambdaSlot),
                mPlayerView
            )
        } returns mediaSourceFactory
        mockkStatic(Uri::class)
        val dataSpec: DataSpec = mockk()
        val adUri: Uri = mockk()
        every { utils.createDataSpec(adUri) } returns dataSpec
        every { Uri.parse("addTagUrl12345") } returns adUri
        every { adUri.toString() } returns "adUri"
        val pairSlot = slot<Pair<String, String>>()
        every {
            utils.createAdsMediaSource(
                contentMediaSource,
                dataSpec,
                capture(pairSlot),
                mediaSourceFactory,
                mAdsLoader,
                mPlayerView
            )
        } returns adsMediaSource
        testObject.onCastSessionAvailable()

        verify(atLeast = 1) {
            //setCurrentPlayer
            mPlayerView.visibility = GONE
            mCastControlView!!.show()
            mCastControlView!!.keepScreenOn = true
            playerState.currentPlayView = mCastControlView
            mAdsLoader.adsLoader!!.addAdsLoadedListener(adsLoadedListener)
            mAdsLoader.setPlayer(mLocalPlayer)
            mLocalPlayer!!.stop()
            mLocalPlayer!!.clearMediaItems()
            playerState.currentPlayer = mCastPlayer
            playerState.mVideoTracker = videoTracker

            mCastPlayer.playWhenReady = true

            //playOnCast
            arcCastManager.doCastSession(
                video = arcVideo,
                position = savedPosition,
                artWorkUrl = artworkUrl
            )
            mListener.onTrackingEvent(
                TrackingType.ON_ERROR_OCCURRED,
                TrackingTypeData.TrackingErrorTypeData(
                    arcVideo = arcVideo,
                    sessionId = sessionId,
                    adData = null
                )
            )
            //back
            mCastPlayer.seekTo(savedPosition)
            trackingHelper.onPlaybackStart()
        }
    }

    @Test
    fun `onCastSessionAvailable when previous player is null calls setCurrentPlayer then startVideoOnCurrentPlayer then playOnCast when going to cast from local`() {
        val savedPosition = 38374L
        val expectedId = "234"
        val arcVideo =
            createDefaultVideo(id = expectedId, shouldPlayAds = true, autoStartPlay = true)
        val adsLoadedListener: AdsLoadedListener = mockk()
        val videoTracker: VideoTracker = mockk()
        val artworkUrl = "art"
        every { mConfig.artworkUrl } returns artworkUrl
        every { mListener.getSavedPosition(expectedId) } returns savedPosition
        every { playerState.mAdsLoader } returns mAdsLoader
        every { playerState.mVideoId } returns expectedId
        every { playerState.mVideo } returns arcVideo
        every { playerState.currentPlayer } returnsMany listOf(null, null, mCastPlayer)
        every { mLocalPlayer!!.playbackState } returns Player.STATE_ENDED
        every {
            utils.createAdsLoadedListener(
                mListener,
                arcVideo,
                testObject
            )
        } returns adsLoadedListener
        mockkObject(VideoTracker.Companion)
        every {
            VideoTracker.getInstance(
                mListener, mCastPlayer, trackingHelper,
                playerState.mIsLive, mockActivity
            )
        } returns videoTracker
        mockkObject(com.arcxp.commons.util.Utils)
        every { com.arcxp.commons.util.Utils.createTimeStamp() } returns "12345"
        val lambdaSlot = slot<AdsLoader.Provider>()
        every {
            constructedWith<DefaultMediaSourceFactory>(EqMatcher(mMediaDataSourceFactory)).setLocalAdInsertionComponents(
                capture(lambdaSlot),
                mPlayerView
            )
        } returns mediaSourceFactory
        mockkStatic(Uri::class)
        val dataSpec: DataSpec = mockk()
        val adUri: Uri = mockk()
        every { utils.createDataSpec(adUri) } returns dataSpec
        every { Uri.parse("addTagUrl12345") } returns adUri
        every { adUri.toString() } returns "adUri"
        val pairSlot = slot<Pair<String, String>>()
        every {
            utils.createAdsMediaSource(
                contentMediaSource,
                dataSpec,
                capture(pairSlot),
                mediaSourceFactory,
                mAdsLoader,
                mPlayerView
            )
        } returns adsMediaSource
        testObject.onCastSessionAvailable()

        verify(atLeast = 1) {
            //setCurrentPlayer
            mPlayerView.visibility = GONE
            mCastControlView!!.show()
            mCastControlView!!.keepScreenOn = true
            playerState.currentPlayView = mCastControlView
            playerState.currentPlayer = mCastPlayer
            playerState.mVideoTracker = videoTracker
            mCastPlayer.playWhenReady = true

            //playOnCast
            arcCastManager.doCastSession(
                video = arcVideo,
                position = savedPosition,
                artWorkUrl = artworkUrl
            )
            //back
            mCastPlayer.seekTo(savedPosition)
            trackingHelper.onPlaybackStart()
        }
    }
}