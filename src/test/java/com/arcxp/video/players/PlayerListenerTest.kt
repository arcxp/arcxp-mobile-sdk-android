package com.arcxp.video.players

import android.app.Activity
import android.util.Log
import android.view.View
import android.view.View.GONE
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.widget.ImageButton
import com.arcxp.commons.testutils.TestUtils.createDefaultVideo
import com.arcxp.sdk.R
import com.arcxp.video.ArcXPVideoConfig
import com.arcxp.video.cast.ArcCastManager
import com.arcxp.video.listeners.VideoListener
import com.arcxp.video.model.ArcVideoSDKErrorType
import com.arcxp.video.model.PlayerState
import com.arcxp.video.model.TrackingType
import com.arcxp.video.model.TrackingTypeData
import com.arcxp.video.util.TrackingHelper
import com.arcxp.video.util.Utils
import com.google.ads.interactivemedia.v3.api.AdEvent.AdEventListener
import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Format
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.Tracks
import com.google.android.exoplayer2.ext.cast.CastPlayer
import com.google.android.exoplayer2.source.TrackGroup
import com.google.android.exoplayer2.ui.StyledPlayerView
import com.google.android.exoplayer2.upstream.FileDataSource
import com.google.common.collect.ImmutableList
import io.mockk.MockKAnnotations
import io.mockk.called
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.verify
import io.mockk.verifyOrder
import io.mockk.verifySequence
import org.junit.After
import org.junit.Before
import org.junit.Test
import rx.Subscription
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class PlayerListenerTest {

    @RelaxedMockK
    private lateinit var trackingHelper: TrackingHelper

    @RelaxedMockK
    private lateinit var playerState: PlayerState

    @RelaxedMockK
    private lateinit var playerStateHelper: PlayerStateHelper

    @RelaxedMockK
    private lateinit var mListener: VideoListener

    @RelaxedMockK
    private lateinit var captionsManager: CaptionsManager

    @RelaxedMockK
    private lateinit var mConfig: ArcXPVideoConfig

    @RelaxedMockK
    private lateinit var arcCastManager: ArcCastManager

    @MockK
    private lateinit var utils: Utils

    @MockK
    private lateinit var adEventListener: AdEventListener

    @MockK
    private lateinit var videoPlayer: ArcVideoPlayer

    @RelaxedMockK
    private lateinit var mPlayerView: StyledPlayerView

    @RelaxedMockK
    private lateinit var mPlayer: ExoPlayer

    @RelaxedMockK
    private lateinit var mockActivity: Activity

    @RelaxedMockK
    private lateinit var mockFullscreenOverlays: HashMap<String, View>

    @RelaxedMockK
    private lateinit var mockView1: View

    @RelaxedMockK
    private lateinit var mockView2: View

    @RelaxedMockK
    private lateinit var mockView3: View

    @RelaxedMockK
    private lateinit var ccButton: ImageButton

    @RelaxedMockK
    private lateinit var videoData: TrackingTypeData.TrackingVideoTypeData

    private val sourceError = "An error occurred during playback."
    private val unknownError = "An unknown error occurred while trying to play the video."
    private lateinit var testObject: PlayerListener

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        every { playerState.mFullscreenOverlays } returns mockFullscreenOverlays
        every { playerState.mLocalPlayerView } returns mPlayerView
        every { playerState.mLocalPlayer } returns mPlayer
        every { playerState.currentPlayer } returns mPlayer
        every { playerState.ccButton } returns ccButton
        every { mConfig.activity } returns mockActivity
        every { mockActivity.getString(R.string.source_error) } returns sourceError
        every { mockActivity.getString(R.string.unknown_error) } returns unknownError
        every { mockFullscreenOverlays.values } returns mutableListOf(
            mockView1,
            mockView2,
            mockView3
        )
        every { utils.createTrackingVideoTypeData() } returns videoData

        testObject = PlayerListener(
            trackingHelper = trackingHelper,
            playerState = playerState,
            playerStateHelper = playerStateHelper,
            mListener = mListener,
            captionsManager = captionsManager,
            mConfig = mConfig,
            arcCastManager = arcCastManager,
            utils = utils,
            adEventListener = adEventListener,
            videoPlayer = videoPlayer
        )
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
            playerStateHelper.onVideoEvent(
                TrackingType.BEHIND_LIVE_WINDOW_ADJUSTMENT,
                capture(trackingTypeData)
            )
        }
        assertTrue(trackingTypeData.captured is TrackingTypeData.TrackingErrorTypeData)
    }

    @Test
    fun `onPlayerError isBehindLiveWindow when null player`() {
        val exception = mockk<RuntimeException>()
        val exoPlaybackException = ExoPlaybackException.createForUnexpected(
            exception,
            PlaybackException.ERROR_CODE_BEHIND_LIVE_WINDOW
        )
        val trackingTypeData = slot<TrackingTypeData>()
        every { playerState.mLocalPlayerView } returns null
        every { playerState.mLocalPlayer } returns null

        testObject.onPlayerError(exoPlaybackException)

        verifySequence {
            playerStateHelper.onVideoEvent(
                TrackingType.BEHIND_LIVE_WINDOW_ADJUSTMENT,
                capture(trackingTypeData)
            )
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


    @Test
    fun `onPositionDiscontinuity when reason is period transition`() {
        val arcVideo1 = createDefaultVideo()
        val arcVideo2 = createDefaultVideo()
        val expectedCurrentWindowIndex = 1
        val videoData = mockk<TrackingTypeData.TrackingVideoTypeData>(relaxed = true)
        val positionOld = Player.PositionInfo(null, 0, null, null, 0, 1000, 1000, 0, 0)
        val positionNew = Player.PositionInfo(null, 0, null, null, 0, 1000, 1000, 0, 0)
        every { mPlayer.currentWindowIndex } returns expectedCurrentWindowIndex
        every { utils.createTrackingVideoTypeData() } returns videoData
        every { playerState.mVideos } returns mutableListOf(arcVideo1, arcVideo2)

        testObject.onPositionDiscontinuity(
            positionOld,
            positionNew,
            Player.DISCONTINUITY_REASON_AUTO_TRANSITION
        )

        verifySequence {
            mPlayer.currentWindowIndex
            utils.createTrackingVideoTypeData()
            videoData.percentage = 100
            videoData.arcVideo = arcVideo1
            playerStateHelper.onVideoEvent(TrackingType.ON_PLAY_COMPLETED, videoData)
            utils.createTrackingVideoTypeData()
            videoData.percentage = 0
            videoData.position = 0L
            videoData.arcVideo = arcVideo2
            playerStateHelper.onVideoEvent(TrackingType.ON_PLAY_STARTED, videoData)
        }
    }

    @Test
    fun `onPositionDiscontinuity when reason is period transition mvideos is null`() {
        val expectedCurrentWindowIndex = 1
        val videoData = mockk<TrackingTypeData.TrackingVideoTypeData>(relaxed = true)
        every { mPlayer.currentWindowIndex } returns expectedCurrentWindowIndex
        every { utils.createTrackingVideoTypeData() } returns videoData
        every { playerState.mVideos } returns null
        val positionOld = Player.PositionInfo(null, 0, null, null, 0, 1000, 1000, 0, 0)
        val positionNew = Player.PositionInfo(null, 0, null, null, 0, 1000, 1000, 0, 0)
        testObject.onPositionDiscontinuity(
            positionOld,
            positionNew,
            Player.DISCONTINUITY_REASON_AUTO_TRANSITION
        )

        verifySequence {
            mPlayer.currentWindowIndex
            utils.createTrackingVideoTypeData()
            videoData.percentage = 100
            videoData.arcVideo = null
            playerStateHelper.onVideoEvent(TrackingType.ON_PLAY_COMPLETED, videoData)
            utils.createTrackingVideoTypeData()
            videoData.percentage = 0
            videoData.position = 0L
            videoData.arcVideo = null
            playerStateHelper.onVideoEvent(TrackingType.ON_PLAY_STARTED, videoData)
        }
    }

    @Test
    fun `onPositionDiscontinuity throws an exception`() {
        val expectedCurrentWindowIndex = 1
        every { mPlayer.currentWindowIndex } returns expectedCurrentWindowIndex
        every { utils.createTrackingVideoTypeData() } throws Exception()
        val positionOld = Player.PositionInfo(null, 0, null, null, 0, 1000, 1000, 0, 0)
        val positionNew = Player.PositionInfo(null, 0, null, null, 0, 1000, 1000, 0, 0)
        testObject.onPositionDiscontinuity(
            positionOld,
            positionNew,
            Player.DISCONTINUITY_REASON_AUTO_TRANSITION
        )

        verifySequence {
            mPlayer.currentWindowIndex
            utils.createTrackingVideoTypeData()
        }
    }

    @Test
    fun `onPositionDiscontinuity when reason is period transition but player is null`() {
        every { playerState.currentPlayer } returns null
        testObject.onPositionDiscontinuity(
            mockk(),
            mockk(),
            Player.DISCONTINUITY_REASON_AUTO_TRANSITION
        )

        verifySequence {
            playerState.currentPlayer
        }
    }

    @Test
    fun `onPositionDiscontinuity when reason is not period transition`() {
        testObject.onPositionDiscontinuity(
            mockk(),
            mockk(),
            Player.DISCONTINUITY_REASON_INTERNAL
        )

        verify { playerState wasNot called }
    }


    //TODO ask about the loop only using last found language? is this the expected behavior?
    @Test
    fun `onTracksChanged parses and sends language from id to subtitle event`() {
        val expected = "expected"
        val tracks = mockk<Tracks> {
            every { groups } returns ImmutableList.copyOf(
                listOf(
                    Tracks.Group(
                        TrackGroup(
                            Format.Builder().setId("CC:id blah blah CC:$expected").build()
                        ), false, IntArray(1) { 1 }, BooleanArray(1) { true })
                )
            )
        }
        val sourceCaptureSlot = slot<TrackingTypeData.TrackingSourceTypeData>()

        testObject.onTracksChanged(tracks)

        verifySequence {
            playerStateHelper.onVideoEvent(
                TrackingType.SUBTITLE_SELECTION,
                capture(sourceCaptureSlot)
            )
        }
        assertEquals(expected, sourceCaptureSlot.captured.source)
    }

    @Test
    fun `onTracksChanged with no CC in string sets language to none`() {
        val expected = "none"
        val tracks = mockk<Tracks> {
            every { groups } returns ImmutableList.copyOf(
                listOf(
                    Tracks.Group(
                        TrackGroup(
                            Format.Builder().setId("C:id blah blah CC:$expected").build()
                        ), false, IntArray(1) { 1 }, BooleanArray(1) { true })
                )
            )
        }
        val sourceCaptureSlot = slot<TrackingTypeData.TrackingSourceTypeData>()

        testObject.onTracksChanged(tracks)

        verifySequence {
            playerStateHelper.onVideoEvent(
                TrackingType.SUBTITLE_SELECTION,
                capture(sourceCaptureSlot)
            )
        }
        assertEquals(expected, sourceCaptureSlot.captured.source)
    }

    @Test
    fun `onTracksChanged with null id on group sets language to none`() {
        val expected = "none"
        val tracks = mockk<Tracks> {
            every { groups } returns ImmutableList.copyOf(
                listOf(
                    Tracks.Group(
                        TrackGroup(
                            Format.Builder().setId(null).build()
                        ), false, IntArray(1) { 1 }, BooleanArray(1) { true })
                )
            )
        }
        val sourceCaptureSlot = slot<TrackingTypeData.TrackingSourceTypeData>()

        testObject.onTracksChanged(tracks)

        verifySequence {
            playerStateHelper.onVideoEvent(
                TrackingType.SUBTITLE_SELECTION,
                capture(sourceCaptureSlot)
            )
        }
        assertEquals(expected, sourceCaptureSlot.captured.source)
    }


    @Test
    fun `onTracksChanged with no selection parses and sends language as none to subtitle event`() {
        val expected = "none"
        val tracks = mockk<Tracks> {
            every { groups } returns ImmutableList.copyOf(
                emptyList()
            )
        }
        val sourceCaptureSlot = slot<TrackingTypeData.TrackingSourceTypeData>()

        testObject.onTracksChanged(tracks)

        verifySequence {
            playerStateHelper.onVideoEvent(
                TrackingType.SUBTITLE_SELECTION,
                capture(sourceCaptureSlot)
            )
        }
        assertEquals(expected, sourceCaptureSlot.captured.source)
    }

    @Test
    fun `onVolumeChanged calls tracking helper`() {
        testObject.onVolumeChanged(23f)
        verifySequence {
            trackingHelper.volumeChange(23f)
        }
    }

    @Test
    fun `onIsLoadingChanged calls listener`() {
        testObject.onIsLoadingChanged(true)
        verifySequence {
            mListener.setIsLoading(true)
        }
    }

    @Test
    fun `onTimelineChanged when ccButton is null`() {
        every { playerState.ccButton } returns null
        testObject.onTimelineChanged(mockk(), 1)
    }

    @Test
    fun `onTimelineChanged when showCaptions false not isKeepControlsSpaceOnHide sets ccButton gone`() {
        every { mConfig.isKeepControlsSpaceOnHide } returns false
        every { mConfig.isShowClosedCaptionTrackSelection } returns false
        every { captionsManager.isClosedCaptionAvailable() } returns true
        testObject.onTimelineChanged(mockk(), 1)
        verifySequence {
            ccButton.visibility = GONE
        }
    }

    @Test
    fun `onTimelineChanged when showCaptions false isKeepControlsSpaceOnHide sets ccButton invisible`() {
        every { mConfig.isKeepControlsSpaceOnHide } returns true
        every { mConfig.isShowClosedCaptionTrackSelection } returns true
        every { captionsManager.isClosedCaptionAvailable() } returns false
        testObject.onTimelineChanged(mockk(), 1)
        verifySequence {
            ccButton.visibility = INVISIBLE
        }
    }

    @Test
    fun `onTimelineChanged when showCaptions true sets ccButton visible`() {
        every { mConfig.isKeepControlsSpaceOnHide } returns true
        every { mConfig.isShowClosedCaptionTrackSelection } returns true
        every { captionsManager.isClosedCaptionAvailable() } returns true
        testObject.onTimelineChanged(mockk(), 1)
        verifySequence {
            ccButton.visibility = VISIBLE
        }
    }

    @Test
    fun `isCasting when player is cast player returns true`() {
        every { playerState.currentPlayer } returns mockk<CastPlayer>()
        assertTrue(testObject.isCasting())
    }

    @Test
    fun `isCasting when player is not cast player returns false`() {
        every { playerState.currentPlayer } returns mockk()
        assertFalse(testObject.isCasting())
    }

    @Test
    fun `unused methods for coverage`() {
        testObject.onPlaybackParametersChanged(mockk())
        testObject.onSeekProcessed()
        testObject.onPlaybackSuppressionReasonChanged(1)
        testObject.onIsPlayingChanged(true)
        testObject.onRepeatModeChanged(1)
        testObject.onShuffleModeEnabledChanged(true)
    }

    @Test
    fun `onPlayWhenReadyChanged when casting reloads video`() {
        val vid = createDefaultVideo()
        every { playerState.mVideo } returns vid
        every { playerState.currentPlayer } returns mockk<CastPlayer>()

        testObject.onPlayWhenReadyChanged(true, Player.STATE_IDLE)
        verifySequence {
            arcCastManager.reloadVideo(video = vid)
        }

    }

    @Test
    fun `onPlayWhenReadyChanged when casting but cast manager null`() {
        val vid = createDefaultVideo()
        every { playerState.mVideo } returns vid
        every { playerState.currentPlayer } returns mockk<CastPlayer>()
        testObject = PlayerListener(
            trackingHelper = trackingHelper,
            playerState = playerState,
            playerStateHelper = playerStateHelper,
            mListener = mListener,
            captionsManager = captionsManager,
            mConfig = mConfig,
            arcCastManager = null,
            utils = utils,
            adEventListener = adEventListener,
            videoPlayer = videoPlayer
        )

        testObject.onPlayWhenReadyChanged(true, Player.STATE_IDLE)
    }

    @Test
    fun `onPlayWhenReadyChanged when casting but mVideo null`() {
        every { playerState.mVideo } returns null
        every { playerState.currentPlayer } returns mockk<CastPlayer>()

        testObject.onPlayWhenReadyChanged(true, Player.STATE_IDLE)
    }


    @Test
    fun `onPlayWhenReadyChanged not idling but mLocalPlayer is null`() {
        every { playerState.mLocalPlayer } returns null


        testObject.onPlayWhenReadyChanged(true, Player.STATE_BUFFERING)
    }

    @Test
    fun `onPlayWhenReadyChanged not idling but mVideoTracker is null`() {
        every { playerState.mVideoTracker } returns null


        testObject.onPlayWhenReadyChanged(true, Player.STATE_BUFFERING)
    }

    @Test
    fun `onPlayWhenReadyChanged not idling but mLocalPlayerView is null`() {
        every { playerState.mLocalPlayerView } returns null


        testObject.onPlayWhenReadyChanged(true, Player.STATE_BUFFERING)
    }

    @Test
    fun `onPlayWhenReadyChanged idling but not casting`() {
        testObject.onPlayWhenReadyChanged(true, Player.STATE_IDLE)
    }

    @Test
    fun `onPlayWhenReadyChanged when buffering sets is loading to true`() {
        every { playerState.currentPlayer } returns mockk()//for coverage

        testObject.onPlayWhenReadyChanged(true, Player.STATE_BUFFERING)
        verifySequence {
            mListener.setIsLoading(true)
        }
    }

    @Test
    fun `onPlayWhenReadyChanged when ready, tracking sub is null subscribes to observable`() {
        every { playerState.videoTrackingSub } returns null
//        every { playerState.mVideoId } returns null
        val sub: Subscription = mockk()
        every { playerState.mVideoTracker } returns mockk {
            every { getObs() } returns mockk {
                every { subscribe() } returns sub
            }
        }
        testObject.onPlayWhenReadyChanged(true, Player.STATE_READY)
        verify {
            mPlayerView.keepScreenOn = true
            playerState.videoTrackingSub = sub
            mListener.setIsLoading(false)
        }
    }//happy path wip

    @Test
    fun `onPlayWhenReadyChanged when ready, tracking sub is unsubscribed subscribes to observable`() {
        every { playerState.videoTrackingSub } returns mockk {
            every { isUnsubscribed } returns true
        }
//        every { playerState.mVideoId } returns null
        val sub: Subscription = mockk()
        every { playerState.mVideoTracker } returns mockk {
            every { getObs() } returns mockk {
                every { subscribe() } returns sub
            }
        }
        testObject.onPlayWhenReadyChanged(true, Player.STATE_READY)
        verify(exactly = 1) {
            mPlayerView.keepScreenOn = true
            playerState.videoTrackingSub = sub
            mListener.setIsLoading(false)
        }
    }//happy path wip

    @Test
    fun `onPlayWhenReadyChanged when ready, tracking sub is subscribed does not resubscribe`() {
        val currentPosition = 40L

        every { mPlayer.currentPosition } returns currentPosition
        every { playerState.mIsLive  } returns false
        every { playerState.videoTrackingSub } returns mockk {
            every { isUnsubscribed } returns false
        }
        testObject.onPlayWhenReadyChanged(true, Player.STATE_READY)
        verify(exactly = 0) {

            playerState.videoTrackingSub = any()

        }
    }//happy path wip

    @Test
    fun `onPlayWhenReadyChanged when read when mIsLive calls listener`() {
        val currentPosition = 51L
        every { playerState.videoTrackingSub } returns null
        every { playerState.mIsLive  } returns true
        every { mPlayer.currentPosition } returns currentPosition
        every { mPlayer.isPlayingAd } returns false
        testObject.onPlayWhenReadyChanged(true, Player.STATE_READY)
        verify(exactly = 1) {
            videoData.position = currentPosition
            mListener.onTrackingEvent(TrackingType.ON_PLAY_RESUMED, videoData)
            trackingHelper.resumePlay()
        }

    }//happy path wip
    @Test
    fun `onPlayWhenReadyChanged when ready when position over 50 calls listener`() {
        val currentPosition = 51L
        every { playerState.mIsLive  } returns false
        every { playerState.videoTrackingSub } returns null
        every { mPlayer.currentPosition } returns currentPosition
        every { mPlayer.isPlayingAd } returns true
        testObject.onPlayWhenReadyChanged(true, Player.STATE_READY)
        verify(exactly = 1) {
            videoData.position = currentPosition
            mListener.onTrackingEvent(TrackingType.ON_PLAY_RESUMED, videoData)
            trackingHelper.resumePlay()
        }

    }//happy path wip
    @Test
    fun `onPlayWhenReadyChanged when ready when not playing ad initializes captions`() {
        val currentPosition = 51L
        every { playerState.mIsLive  } returns false
        every { mListener.isInPIP } returns false
        every { playerState.videoTrackingSub } returns null
        every { mPlayer.currentPosition } returns currentPosition
        every { mPlayer.isPlayingAd } returns false
        testObject.onPlayWhenReadyChanged(true, Player.STATE_READY)
        verify(exactly = 1) {
            captionsManager.initVideoCaptions()
        }

    }//happy path wip
    @Test
    fun `onPlayWhenReadyChanged when ready pauses listener if in pip`() {
        every { mListener.isInPIP } returns true
        testObject.onPlayWhenReadyChanged(true, Player.STATE_ENDED)
        verify(exactly = 1) {
            mListener.pausePIP()
        }

    }//happy path wip
    @Test
    fun `onPlayWhenReadyChanged when ready state ended videoTrackingSub null, no more videos`() {
        val expectedVideo = createDefaultVideo()
        every { playerState.videoTrackingSub} returns null
        every { playerStateHelper.haveMoreVideosToPlay()} returns false
        every { playerState.mVideo } returns expectedVideo
        every { mListener.isInPIP } returns true
        testObject.onPlayWhenReadyChanged(true, Player.STATE_ENDED)
        verifyOrder {
            videoData.percentage = 100
            videoData.arcVideo = expectedVideo
            mListener.onTrackingEvent(TrackingType.ON_PLAY_COMPLETED, videoData)
            trackingHelper.onPlaybackEnd()
        }

    }//happy path wip
    @Test
    fun `onPlayWhenReadyChanged when ready state ended videoTrackingSub non null, has more videos`() {
        val expectedVideo = createDefaultVideo()
        every { playerStateHelper.haveMoreVideosToPlay()} returns true
        every { playerState.mVideo } returns expectedVideo
        every { mListener.isInPIP } returns true
        testObject.onPlayWhenReadyChanged(true, Player.STATE_ENDED)
        verifyOrder {
            videoData.percentage = 100
            videoData.arcVideo = expectedVideo
            mListener.onTrackingEvent(TrackingType.ON_PLAY_COMPLETED, videoData)
            playerState.videoTrackingSub!!.unsubscribe()
            playerState.videoTrackingSub = null
            playerState.mVideoTracker!!.reset()
            trackingHelper.onPlaybackEnd()
        }

    }//TODO this calls playVideoAtIndex

    @Test
    fun `onPlayWhenReadyChanged when ready but idle sets is loading to false`() {
        every { playerState.currentPlayer } returns mockk()//for coverage
        every { playerState.mVideoId } returns null

        testObject.onPlayWhenReadyChanged(true, Player.STATE_IDLE)
        verify(exactly = 1) {
            mListener.setIsLoading(false)
        }
    }

    @Test
    fun `onPlayWhenReadyChanged when ready but ended sets is loading to false`() {
        every { playerState.currentPlayer } returns mockk()//for coverage
        every { playerState.mVideoId } returns null

        testObject.onPlayWhenReadyChanged(true, Player.STATE_ENDED)
        verify(exactly = 1) {
            mListener.setIsLoading(false)
        }
    }

    @Test
    fun `onPlayWhenReadyChanged when idling playWhenReady false, mVideo null sets loading to false`() {
        every { playerState.mVideoId } returns null

        testObject.onPlayWhenReadyChanged(false, Player.STATE_IDLE)
        verifySequence {
            mListener.setIsLoading(false)
        }
    }

    //playWhenReady is false.
    //playerStateCode is STATE_IDLE.
    //playerStateCode is not STATE_ENDED.
    @Test
    fun `onPlayWhenReadyChanged when ended playWhenReady false, mVideo null`() {
        every { playerState.mVideoId } returns null

        testObject.onPlayWhenReadyChanged(false, Player.STATE_ENDED)
        verifySequence {
            mListener.setIsLoading(false)
        }
    }

    //playWhenReady is false.
    //playerStateCode is not STATE_IDLE.
    //playerStateCode is STATE_ENDED.
    @Test
    fun `onPlayWhenReadyChanged when not idling or buffering playWhenReady true, mVideo null`() {
        every { playerState.mVideoId } returns null

        testObject.onPlayWhenReadyChanged(false, Player.STATE_READY)
        verifySequence {
            mListener.setIsLoading(false)
        }
        //playWhenReady is false.
        //playerStateCode is not STATE_IDLE.
        //playerStateCode is not STATE_ENDED.
    }

}