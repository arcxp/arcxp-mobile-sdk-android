package com.arcxp.video.players

import android.app.Activity
import android.util.Log
import android.view.View
import com.arcxp.sdk.R
import com.arcxp.video.ArcXPVideoConfig
import com.arcxp.video.cast.ArcCastManager
import com.arcxp.video.listeners.VideoListener
import com.arcxp.video.model.ArcVideoSDKErrorType
import com.arcxp.video.model.TrackingType
import com.arcxp.video.model.TrackingTypeData
import com.arcxp.video.util.TrackingHelper
import com.arcxp.video.util.Utils
import com.google.ads.interactivemedia.v3.api.AdEvent
import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.ui.StyledPlayerView
import com.google.android.exoplayer2.upstream.FileDataSource
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.verify
import io.mockk.verifySequence
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertTrue

internal class PlayerListenerTest {

    @MockK
    private lateinit var trackingHelper: TrackingHelper
    @RelaxedMockK
    private lateinit var playerState: PlayerState
    @RelaxedMockK
    private lateinit var playerStateHelper: PlayerStateHelper
    @RelaxedMockK
    private lateinit var mListener: VideoListener
    @MockK
    private lateinit var captionsManager: CaptionsManager
    @RelaxedMockK
    private lateinit var mConfig: ArcXPVideoConfig
    @MockK
    private lateinit var arcCastManager: ArcCastManager
    @MockK
    private lateinit var utils: Utils
    @MockK
    private lateinit var adEventListener: AdEvent.AdEventListener
    @MockK
    private lateinit var videoPlayer: ArcVideoPlayer
    @RelaxedMockK
    private lateinit var mPlayerView: StyledPlayerView
    @RelaxedMockK
    private lateinit var mPlayer: ExoPlayer
    @RelaxedMockK
    private lateinit var mockActivity: Activity

    @RelaxedMockK
    lateinit var mockFullscreenOverlays: HashMap<String, View>
    @RelaxedMockK
    lateinit var mockView1: View

    @RelaxedMockK
    lateinit var mockView2: View

    @RelaxedMockK
    lateinit var mockView3: View

    private val sourceError = "An error occurred during playback."
    private val unknownError = "An unknown error occurred while trying to play the video."
    private lateinit var testObject: PlayerListener
    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        every { playerState.mFullscreenOverlays } returns mockFullscreenOverlays
        every { playerState.mLocalPlayerView } returns mPlayerView
        every { playerState.mLocalPlayer } returns mPlayer
        every { mConfig.activity} returns mockActivity
        every { mockActivity.getString(R.string.source_error)} returns sourceError
        every { mockActivity.getString(R.string.unknown_error) } returns unknownError
        every { mockFullscreenOverlays.values } returns mutableListOf(
            mockView1,
            mockView2,
            mockView3
        )

        testObject = PlayerListener(trackingHelper, playerState, playerStateHelper, mListener, captionsManager, mConfig, arcCastManager, utils, adEventListener, videoPlayer)
    }

    @After
    fun tearDown() {
    }




    @Test
    fun `onPlayerError isBehindLiveWindow plays Video and triggers event and removes overlay views`() {
        val exception = mockk<RuntimeException>()
        val exoPlaybackException = ExoPlaybackException.createForUnexpected(
            exception,
            PlaybackException.ERROR_CODE_BEHIND_LIVE_WINDOW
        )

        val trackingTypeData = slot<TrackingTypeData>()

        testObject.onPlayerError(exoPlaybackException)

        verifySequence {
            mPlayerView.removeView(mockView1)
            mPlayerView.removeView(mockView2)
            mPlayerView.removeView(mockView3)
            mPlayer.seekToDefaultPosition()
            mPlayer.prepare()
            playerStateHelper.onVideoEvent(TrackingType.BEHIND_LIVE_WINDOW_ADJUSTMENT, capture(trackingTypeData))
        }
        assertTrue(trackingTypeData.captured is TrackingTypeData.TrackingErrorTypeData)
    }

    @Test
    fun `onPlayerError exception is other than ERROR_CODE_IO_NETWORK_CONNECTION_FAILED Exception calls error handler`() {
        val exception = mockk<RuntimeException>()
        val exoPlaybackException = ExoPlaybackException.createForUnexpected(
            exception,
            PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED
        )

        testObject.onPlayerError(exoPlaybackException)

        verifySequence {
            mListener.onError(
                ArcVideoSDKErrorType.SOURCE_ERROR,
                sourceError,
                exception
            )
        }
    }

    @Test
    fun `onPlayerError exception is ERROR_CODE_IO_NETWORK_CONNECTION_FAILED and File Data Source Exception logs error`() {
        val sourceException = mockk<FileDataSource.FileDataSourceException>()
        val exoPlaybackException = ExoPlaybackException.createForSource(
            sourceException,
            PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED
        )

        testObject.onPlayerError(exoPlaybackException)

        verifySequence {
            mListener.onError(ArcVideoSDKErrorType.SOURCE_ERROR, sourceError, sourceException)
            mListener.logError("Exoplayer Source Error: No url passed from backend. Caused by:\n$sourceException")
        }
    }

    @Test
    fun `onPlayerError exception is logged`() {
        mockkStatic(Log::class)
        val sourceException = mockk<FileDataSource.FileDataSourceException>()
        val exoPlaybackException = ExoPlaybackException.createForSource(
            sourceException,
            PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED
        )
        every { mConfig.isLoggingEnabled } returns true

        testObject.onPlayerError(exoPlaybackException)

        verify(exactly = 1) {
            Log.e("PlayerListener", "ExoPlayer Error", exoPlaybackException)
        }
    }

    @Test
    fun `onPlayerError exception is unknown and calls handler`() {
        val sourceException = mockk<FileDataSource.FileDataSourceException>()
        val exoPlaybackException = ExoPlaybackException.createForUnexpected(RuntimeException())
        every { sourceException.cause } returns null

        testObject.onPlayerError(exoPlaybackException)

        verifySequence {
            mListener.onError(
                ArcVideoSDKErrorType.EXOPLAYER_ERROR,
                unknownError,
                exoPlaybackException
            )
        }
    }
}