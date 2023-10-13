package com.arcxp.video.players

import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.TextView
import com.arcxp.sdk.R
import com.arcxp.video.listeners.VideoListener
import com.arcxp.video.model.ArcVideo
import com.arcxp.video.util.TrackingHelper
import com.arcxp.video.util.Utils
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.ui.StyledPlayerView
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.mockk
import org.junit.After
import org.junit.Before
import org.junit.Test

internal class PlayerStateHelperTest {

    @RelaxedMockK
    private lateinit var playerState: PlayerState

    @MockK
    private lateinit var trackingHelper: TrackingHelper

    @MockK
    private lateinit var utils: Utils

    @RelaxedMockK
    private lateinit var mListener: VideoListener

    @MockK
    private lateinit var captionsManager: CaptionsManager

    @MockK
    private lateinit var playerListener: PlayerListener

    @RelaxedMockK
    private lateinit var exoPlayer: ExoPlayer

    @RelaxedMockK
    private lateinit var playerView: StyledPlayerView

    @MockK
    private lateinit var titleTextView: TextView

    @MockK
    private lateinit var ccButton: ImageButton

    private lateinit var testObject: PlayerStateHelper

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        testObject =
            PlayerStateHelper(playerState, trackingHelper, utils, mListener, captionsManager)
        testObject.playerListener = playerListener
        playerView.apply {
            every { findViewById<TextView>(R.id.styled_controller_title_tv) } returns titleTextView
            every { findViewById<ImageButton>(R.id.exo_cc) } returns ccButton
        }
    }

    @After
    fun tearDown() {
    }

    @Test
    fun `initLocalPlayer not fullscreen, mVideo is not null, start muted, disableControls fully false, has ccButton`() {

        every { utils.createExoPlayer() } returns exoPlayer
        val exoVolume = 0.83f
        every { exoPlayer.volume } returns exoVolume
        every { utils.createPlayerView() } returns playerView
        val layoutParams = mockk<FrameLayout.LayoutParams>()
        every { utils.createMatchParentLayoutParams() } returns layoutParams
        val expectedResizeMode = 2343
        val expectedAutoShowControls = true
        every { playerState.config.videoResizeMode.mode() } returns expectedResizeMode
        every { playerState.config.isAutoShowControls } returns expectedAutoShowControls
        val mVideo = mockk<ArcVideo>()
        every { playerState.mVideo } returns mVideo


        every { mVideo.startMuted } returns true

//
//
//
//        testObject.initLocalPlayer()
//
//        verify {
//            utils.createExoPlayer()
//            playerState.mLocalPlayer = exoPlayer
//            playerState.mLocalPlayer!!.addListener(playerListener)
//            utils.createPlayerView()
//            playerState.mLocalPlayerView = playerView
//            utils.createMatchParentLayoutParams()
//            playerView.layoutParams = layoutParams
//            playerState.config.videoResizeMode.mode()
//            playerView.resizeMode = expectedResizeMode
//            playerView.id = R.id.wapo_player_view
//            playerView.player = exoPlayer
//            playerState.config.isAutoShowControls
//            playerView.controllerAutoShow = expectedAutoShowControls
//            playerView.findViewById<TextView>(R.id.styled_controller_title_tv)
//            playerState.title = titleTextView
//            exoPlayer.volume
//            playerState.mCurrentVolume = exoVolume
//            exoPlayer.volume = 0f
//
//        }
    }
}