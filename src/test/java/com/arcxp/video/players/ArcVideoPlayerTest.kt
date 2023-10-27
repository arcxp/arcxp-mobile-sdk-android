package com.arcxp.video.players

import android.app.Activity
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import com.arcxp.commons.testutils.TestUtils.createDefaultVideo
import com.arcxp.sdk.R
import com.arcxp.video.ArcXPVideoConfig
import com.arcxp.video.cast.ArcCastManager
import com.arcxp.video.listeners.ArcKeyListener
import com.arcxp.video.listeners.VideoListener
import com.arcxp.video.model.ArcVideo
import com.arcxp.video.model.ArcVideoSDKErrorType
import com.arcxp.video.util.TrackingHelper
import com.arcxp.video.util.Utils
import com.google.ads.interactivemedia.v3.api.AdEvent
import com.google.ads.interactivemedia.v3.api.AdsLoader
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.ext.cast.CastPlayer
import com.google.android.exoplayer2.ext.ima.ImaAdsLoader
import com.google.android.exoplayer2.ui.PlayerControlView
import com.google.android.exoplayer2.ui.StyledPlayerView
import com.google.android.gms.cast.framework.CastContext
import io.mockk.EqMatcher
import io.mockk.MockKAnnotations
import io.mockk.called
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.verify
import io.mockk.verifyOrder
import io.mockk.verifySequence
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertFalse

internal class ArcVideoPlayerTest {


    @RelaxedMockK
    private lateinit var playerState: PlayerState

    @RelaxedMockK
    private lateinit var mCastControlView: PlayerControlView

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
    private lateinit var trackingHelper: TrackingHelper

    @RelaxedMockK
    private lateinit var playerListener: PlayerListener
    @RelaxedMockK
    private lateinit var adEventListener: AdEvent.AdEventListener

    @MockK
    private lateinit var captionsManager: CaptionsManager

    @RelaxedMockK
    private lateinit var mPlayer: ExoPlayer

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


    private lateinit var testObject: ArcVideoPlayer

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        every { playerState.mLocalPlayer } returns mPlayer
        every { playerState.mLocalPlayerView } returns mPlayerView
        every { playerState.mCastControlView } returns mCastControlView
        every { mConfig.activity } returns mockActivity
        every { arcCastManager.getCastContext() } returns mCastContext
//        every { mCastControlView.findViewById<ImageButton>(any())} returns mockk(relaxed = true)
//        every { mCastControlView.findViewById<ImageView>(any())} returns mockk(relaxed = true)


        every { mCastControlView.findViewById<ImageButton>(R.id.exo_fullscreen) } returns mockk(
            relaxed = true
        )
        every { mCastControlView.findViewById<ImageButton>(R.id.exo_pip) } returns mockk(relaxed = true)
        every { mCastControlView.findViewById<ImageButton>(R.id.exo_share) } returns mockk(relaxed = true)
        every { mCastControlView.findViewById<ImageButton>(R.id.exo_volume) } returns mockk(relaxed = true)
        every { mCastControlView.findViewById<ImageButton>(R.id.exo_cc) } returns mockk(relaxed = true)
        every { mCastControlView.findViewById<ImageView>(R.id.exo_artwork) } returns mockk(relaxed = true)

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
            mPlayer.playWhenReady = true
            mPlayerView.hideController()
        }
        verify { mListener wasNot called }
    }

    @Test
    fun `pausePlay given false calls mPlayer, mPlayerView, trackingHelper if they are not null`() {
        testObject.pausePlay(false)

        verifySequence {
            mPlayer.playWhenReady = false
            mPlayerView.hideController()
        }
        verify { mListener wasNot called }
    }

    @Test
    fun `pausePlay throws exception, is handled by listener`() {
        val error = " pause Play error"
        val exception = Exception(error)
        every { mPlayer.playWhenReady = true } throws exception

        testObject.pausePlay(true)

        verifySequence {
            mPlayer.playWhenReady = true
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
        every { mPlayer.playWhenReady = true } throws exception

        testObject.start()

        verifyOrder {
            mPlayer.playWhenReady = true
            mListener.onError(ArcVideoSDKErrorType.EXOPLAYER_ERROR, error, exception)
        }
    }

    @Test
    fun `start calls mPlayer setPlayWhenReady with true`() {
        testObject.start()

        verifySequence { mPlayer.playWhenReady = true }

        verify { mListener wasNot called }
    }

    @Test
    fun `pause calls mPlayer and trackingHelper if they are not null`() {
        testObject.pause()

        verifySequence {
            mPlayer.playWhenReady = false
        }

        verify { mListener wasNot called }
    }


    @Test
    fun `pause throws exception, is handled by listener`() {
        val error = " pause error"
        val exception = Exception(error)
        every { mPlayer.playWhenReady = false } throws exception

        testObject.pause()

        verifySequence {
            mPlayer.playWhenReady = false
            mListener.onError(ArcVideoSDKErrorType.EXOPLAYER_ERROR, error, exception)
        }
        verify { trackingHelper wasNot called }
    }

    @Test
    fun `setPlayerKeyListener sets listeners with no exceptions`() {
        testObject.setPlayerKeyListener(mockk())

        verifySequence {
            mPlayerView.setOnKeyListener(any())
            mCastControlView.setOnKeyListener(any())
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
            mCastControlView.setOnKeyListener(any())
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
        verify { mCastControlView.setOnKeyListener(capture(mCastControlViewListener)) }
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
        every { playerState.mAdsLoader} returns mAdsLoader
        every { playerState.mVideoId} returns arcVideo.uuid
        testObject.playVideo(arcVideo)

        verify(exactly = 1) {
            Log.e("ArcVideoSDK", "Error preparing ad for video uuid", exception)
        }
    }
}