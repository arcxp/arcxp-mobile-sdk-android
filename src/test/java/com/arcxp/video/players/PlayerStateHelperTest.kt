package com.arcxp.video.players

import android.app.Activity
import android.app.Dialog
import android.graphics.drawable.Drawable
import android.view.MotionEvent
import android.view.View
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.core.content.ContextCompat
import com.arcxp.sdk.R
import com.arcxp.video.ArcXPVideoConfig
import com.arcxp.video.listeners.VideoListener
import com.arcxp.video.model.ArcVideo
import com.arcxp.video.model.TrackingType
import com.arcxp.video.model.TrackingTypeData
import com.arcxp.video.util.TrackingHelper
import com.arcxp.video.util.Utils
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.ui.StyledPlayerView
import io.mockk.MockKAnnotations
import io.mockk.called
import io.mockk.clearAllMocks
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
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertFalse

internal class PlayerStateHelperTest {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()


    @RelaxedMockK
    private lateinit var playerState: PlayerState

    @RelaxedMockK
    private lateinit var trackingHelper: TrackingHelper

    @RelaxedMockK
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

    @RelaxedMockK
    private lateinit var audioAttributesBuilder: AudioAttributes.Builder

    @MockK
    private lateinit var audioAttributes: AudioAttributes

    @MockK
    private lateinit var titleTextView: TextView

    @MockK
    private lateinit var ccButton: ImageButton

    @RelaxedMockK
    private lateinit var fullScreenButton: ImageButton

    @RelaxedMockK
    private lateinit var mFullScreenDialog: Dialog

    @RelaxedMockK
    lateinit var mockView1: View

    @RelaxedMockK
    lateinit var mockView2: View

    @RelaxedMockK
    lateinit var mockView3: View

    @RelaxedMockK
    lateinit var mockView1Parent: ViewGroup

    @RelaxedMockK
    lateinit var mockView2Parent: ViewGroup

    @RelaxedMockK
    lateinit var mockView3Parent: ViewGroup

    @RelaxedMockK
    lateinit var arcXPVideoConfig: ArcXPVideoConfig

    @MockK
    lateinit var mockFullscreenOverlays: HashMap<String, View>

    @RelaxedMockK
    private lateinit var playerViewParent: ViewGroup

    @RelaxedMockK
    private lateinit var layoutParams: ViewGroup.LayoutParams

    @RelaxedMockK
    private lateinit var mockActivity: Activity

    @RelaxedMockK
    private lateinit var videoData: TrackingTypeData.TrackingVideoTypeData


    @MockK
    private lateinit var mockFullScreenCollapseDrawable: Drawable

    @MockK
    private lateinit var mockFullScreenDrawable: Drawable

    private val expectedCurrentPosition = 12345L;

    private lateinit var testObject: PlayerStateHelper


    @Before
    fun setUp() {
        MockKAnnotations.init(this)


        every { playerView.findViewById<TextView>(R.id.styled_controller_title_tv) } returns titleTextView
        every { playerView.findViewById<ImageButton>(R.id.exo_cc) } returns ccButton

        every { utils.createAudioAttributeBuilder() } returns audioAttributesBuilder
        every { audioAttributesBuilder.setUsage(any()) } returns audioAttributesBuilder
        every { audioAttributesBuilder.setContentType(any()) } returns audioAttributesBuilder
        every { audioAttributesBuilder.build() } returns audioAttributes
        every { playerState.config } returns arcXPVideoConfig
        every { playerState.mLocalPlayer } returns exoPlayer
        every { mockView1.parent } returns mockView1Parent
        every { mockView2.parent } returns mockView2Parent
        every { mockView3.parent } returns mockView3Parent
        every { playerView.parent } returns playerViewParent
        every { exoPlayer.currentPosition } returns expectedCurrentPosition

        every { utils.createLayoutParams() } returns layoutParams
        every { utils.createTrackingVideoTypeData() } returns videoData
        every { arcXPVideoConfig.activity } returns mockActivity

        mockkStatic(ContextCompat::class)
        every {
            ContextCompat.getDrawable(
                mockActivity,
                R.drawable.FullScreenDrawableButtonCollapse
            )
        } returns mockFullScreenCollapseDrawable
        every {
            ContextCompat.getDrawable(
                mockActivity,
                R.drawable.FullScreenDrawableButton
            )
        } returns mockFullScreenDrawable

        testObject =
            PlayerStateHelper(playerState, trackingHelper, utils, mListener, captionsManager)
        testObject.playerListener = playerListener
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `initLocalPlayer not fullscreen, mVideo is not null, start muted, disableControls fully false, has ccButton, disable controls fully, not full screen`() {
        val exoVolume = 0.83f
        val expectedResizeMode = 2343
        val expectedAutoShowControls = true
        val mockVideo = mockk<ArcVideo>()
        every { utils.createExoPlayer() } returns exoPlayer
        every { utils.createPlayerView() } returns playerView

        every { exoPlayer.volume } returns exoVolume


        every { playerState.mVideo } returns mockVideo

        every { mockVideo.startMuted } returns true
        val expectedResize = mockk<ArcXPVideoConfig.VideoResizeMode>()
        every { arcXPVideoConfig.videoResizeMode } returns expectedResize
        every { expectedResize.mode() } returns expectedResizeMode

        every { arcXPVideoConfig.isAutoShowControls } returns expectedAutoShowControls
        every { arcXPVideoConfig.isDisableControlsFully } returns true

        every { playerState.mFullscreenOverlays } returns mockFullscreenOverlays
        every { mockFullscreenOverlays.values } returns mutableListOf(
            mockView1,
            mockView2,
            mockView3
        )

        every { playerState.mIsFullScreen } returns false

        testObject.initLocalPlayer()

        val slot = slot<View.OnTouchListener>()

        verifySequence {
            utils.createExoPlayer()
            playerState.mLocalPlayer = exoPlayer
            playerState.mLocalPlayer
            exoPlayer.addListener(playerListener)
            utils.createPlayerView()
            playerState.mLocalPlayerView = playerView
            utils.createLayoutParams()
            playerView.layoutParams = layoutParams
            playerState.config
            arcXPVideoConfig.videoResizeMode
            expectedResize.mode()
            playerView.resizeMode = expectedResizeMode
            playerView.id = R.id.wapo_player_view
            playerView.player = exoPlayer
            playerState.config
            arcXPVideoConfig.isAutoShowControls
            playerView.controllerAutoShow = expectedAutoShowControls
            playerView.findViewById<TextView>(R.id.styled_controller_title_tv)
            playerState.title = titleTextView
            playerState.mVideo
            mockVideo.startMuted
            exoPlayer.volume
            playerState.mCurrentVolume = exoVolume
            exoPlayer.volume = 0f
            playerState.config
            arcXPVideoConfig.isDisableControlsFully // setUpPlayerControlListeners
            utils.createAudioAttributeBuilder() // setAudioAttributes
            audioAttributesBuilder.setUsage(C.USAGE_MEDIA)
            audioAttributesBuilder.setContentType(C.AUDIO_CONTENT_TYPE_MOVIE)
            audioAttributesBuilder.build()
            exoPlayer.setAudioAttributes(audioAttributes, true) //setAudioAttributes
            playerView.setOnTouchListener(capture(slot))
            playerState.mIsFullScreen
            mListener.addVideoView(playerView)
            playerState.mFullscreenOverlays
            mockFullscreenOverlays.values
            playerView.addView(mockView1)
            playerView.addView(mockView2)
            playerView.addView(mockView3)
            playerState.config
            arcXPVideoConfig.isDisableControlsFully
            playerView.useController = false
        }

    }

    @Test
    fun `initLocalPlayer sets playerView on touch listener`() {
        val exoVolume = 0.83f
        val expectedResizeMode = 2343
        val expectedAutoShowControls = true
        val mockVideo = mockk<ArcVideo>()
        every { utils.createExoPlayer() } returns exoPlayer
        every { utils.createPlayerView() } returns playerView

        every { exoPlayer.volume } returns exoVolume


        every { playerState.mVideo } returns mockVideo

        every { mockVideo.startMuted } returns true
        val expectedResize = mockk<ArcXPVideoConfig.VideoResizeMode>()
        every { arcXPVideoConfig.videoResizeMode } returns expectedResize
        every { expectedResize.mode() } returns expectedResizeMode

        every { arcXPVideoConfig.isAutoShowControls } returns expectedAutoShowControls
        every { arcXPVideoConfig.isDisableControlsFully } returns true

        every { playerState.mFullscreenOverlays } returns mockFullscreenOverlays
        every { mockFullscreenOverlays.values } returns mutableListOf(
            mockView1,
            mockView2,
            mockView3
        )

        every { playerState.mIsFullScreen } returns false

        testObject.initLocalPlayer()

        val slot = slot<View.OnTouchListener>()

        verify(exactly = 1) {

            playerView.setOnTouchListener(capture(slot))
        }

        //on touch up event
        val expectedTimelinePosition = 0L
        every { testObject.getCurrentTimelinePosition() } returns expectedTimelinePosition
        val view = mockk<View>(relaxed = true)
        val motionEvent = mockk<MotionEvent>()
        every { motionEvent.action } returns MotionEvent.ACTION_UP
        assertFalse(slot.captured.onTouch(view, motionEvent))
        verifySequence {
            view.performClick()
            trackingHelper.onTouch(event = motionEvent, position = expectedTimelinePosition)
        }

        //on touch other event
        clearAllMocks(answers = false)
        every { motionEvent.action } returns MotionEvent.ACTION_DOWN
        assertFalse(slot.captured.onTouch(view, motionEvent))
        verifySequence {
            view.performClick()
        }
        verify { trackingHelper wasNot called }
    }

    @Test
    fun `initLocalPlayer not fullscreen, mVideo is not null, start muted, disableControls fully true, has ccButton, disable controls fully, not full screen`() {
        val exoVolume = 0.83f
        val expectedResizeMode = 2343
        val expectedAutoShowControls = true
        val mockVideo = mockk<ArcVideo>()
        every { utils.createExoPlayer() } returns exoPlayer
        every { utils.createPlayerView() } returns playerView

        every { exoPlayer.volume } returns exoVolume


        every { playerState.mVideo } returns mockVideo

        every { mockVideo.startMuted } returns true
        val expectedResize = mockk<ArcXPVideoConfig.VideoResizeMode>()
        every { arcXPVideoConfig.videoResizeMode } returns expectedResize
        every { expectedResize.mode() } returns expectedResizeMode

        every { arcXPVideoConfig.isAutoShowControls } returns expectedAutoShowControls
        every { arcXPVideoConfig.isDisableControlsFully } returns true

        every { playerState.mFullscreenOverlays } returns mockFullscreenOverlays
        every { mockFullscreenOverlays.values } returns mutableListOf(
            mockView1,
            mockView2,
            mockView3
        )

        every { playerState.mIsFullScreen } returns false

        testObject.initLocalPlayer()

        val slot = slot<View.OnTouchListener>()

        verifySequence {
            utils.createExoPlayer()
            playerState.mLocalPlayer = exoPlayer
            playerState.mLocalPlayer
            exoPlayer.addListener(playerListener)
            utils.createPlayerView()
            playerState.mLocalPlayerView = playerView
            utils.createLayoutParams()
            playerView.layoutParams = layoutParams
            playerState.config
            arcXPVideoConfig.videoResizeMode
            expectedResize.mode()
            playerView.resizeMode = expectedResizeMode
            playerView.id = R.id.wapo_player_view
            playerView.player = exoPlayer
            playerState.config
            arcXPVideoConfig.isAutoShowControls
            playerView.controllerAutoShow = expectedAutoShowControls
            playerView.findViewById<TextView>(R.id.styled_controller_title_tv)
            playerState.title = titleTextView
            playerState.mVideo
            mockVideo.startMuted
            exoPlayer.volume
            playerState.mCurrentVolume = exoVolume
            exoPlayer.volume = 0f
            playerState.config
            arcXPVideoConfig.isDisableControlsFully // setUpPlayerControlListeners
            utils.createAudioAttributeBuilder() // setAudioAttributes
            audioAttributesBuilder.setUsage(C.USAGE_MEDIA)
            audioAttributesBuilder.setContentType(C.AUDIO_CONTENT_TYPE_MOVIE)
            audioAttributesBuilder.build()
            exoPlayer.setAudioAttributes(audioAttributes, true) //setAudioAttributes
            playerView.setOnTouchListener(capture(slot))
            playerState.mIsFullScreen
            mListener.addVideoView(playerView)
            playerState.mFullscreenOverlays
            mockFullscreenOverlays.values
            playerView.addView(mockView1)
            playerView.addView(mockView2)
            playerView.addView(mockView3)
            playerState.config
            arcXPVideoConfig.isDisableControlsFully
            playerView.useController = false
        }
    }

    @Test
    fun `setUpPlayerControlListeners disableControls fully false, null local player`() {
        val exoVolume = 0.83f
        val expectedResizeMode = 2343
        val expectedAutoShowControls = true
        val mockVideo = mockk<ArcVideo>()
        every { utils.createExoPlayer() } returns exoPlayer
        every { utils.createPlayerView() } returns playerView

        every { exoPlayer.volume } returns exoVolume


        every { playerState.mVideo } returns mockVideo

        every { mockVideo.startMuted } returns false
        val expectedResize = mockk<ArcXPVideoConfig.VideoResizeMode>()
        every { arcXPVideoConfig.videoResizeMode } returns expectedResize
        every { expectedResize.mode() } returns expectedResizeMode

        every { arcXPVideoConfig.isAutoShowControls } returns expectedAutoShowControls
        every { arcXPVideoConfig.isDisableControlsFully } returns false

        every { playerState.mFullscreenOverlays } returns mockFullscreenOverlays
        every { playerState.mLocalPlayer } returns null
        every { mockFullscreenOverlays.values } returns mutableListOf(
            mockView1,
            mockView2,
            mockView3
        )

        every { playerState.mIsFullScreen } returns false

        testObject.setUpPlayerControlListeners()

        verifySequence {
            playerState.config
            arcXPVideoConfig.isDisableControlsFully
            playerState.mLocalPlayer
        }


    }

    @Test
    fun `setUpPlayerControlListeners disableControls fully false, null local player view`() {
        val exoVolume = 0.83f
        val expectedResizeMode = 2343
        val expectedAutoShowControls = true
        val mockVideo = mockk<ArcVideo>()
        every { utils.createExoPlayer() } returns exoPlayer
        every { utils.createPlayerView() } returns playerView


        every { exoPlayer.volume } returns exoVolume


        every { playerState.mVideo } returns mockVideo

        every { mockVideo.startMuted } returns false
        val expectedResize = mockk<ArcXPVideoConfig.VideoResizeMode>()
        every { arcXPVideoConfig.videoResizeMode } returns expectedResize
        every { expectedResize.mode() } returns expectedResizeMode

        every { arcXPVideoConfig.isAutoShowControls } returns expectedAutoShowControls
        every { arcXPVideoConfig.isDisableControlsFully } returns false

        every { playerState.mFullscreenOverlays } returns mockFullscreenOverlays
        every { playerState.mLocalPlayerView } returns null
        every { mockFullscreenOverlays.values } returns mutableListOf(
            mockView1,
            mockView2,
            mockView3
        )

        every { playerState.mIsFullScreen } returns false

        testObject.setUpPlayerControlListeners()

        verifySequence {
            playerState.config
            arcXPVideoConfig.isDisableControlsFully
            playerState.mLocalPlayer
            playerState.mLocalPlayerView
        }


    }

    @Test
    fun `setUpPlayerControlListeners disableControls fully false, find view by id on buttons return null`() {

        val exoVolume = 0.83f
        val expectedResizeMode = 2343
        val expectedAutoShowControls = true
        val mockVideo = mockk<ArcVideo>()
        every { utils.createExoPlayer() } returns exoPlayer
        every { utils.createPlayerView() } returns playerView


        every { exoPlayer.volume } returns exoVolume


        every { playerState.mVideo } returns mockVideo


        every { mockVideo.startMuted } returns false
        val expectedResize = mockk<ArcXPVideoConfig.VideoResizeMode>()
        every { arcXPVideoConfig.videoResizeMode } returns expectedResize
        every { expectedResize.mode() } returns expectedResizeMode

        every { arcXPVideoConfig.isAutoShowControls } returns expectedAutoShowControls
        every { arcXPVideoConfig.isDisableControlsFully } returns false

        every { playerState.mFullscreenOverlays } returns mockFullscreenOverlays
        every { playerState.mLocalPlayerView } returns playerView
        every { playerState.ccButton } returns null
        every { playerView.findViewById<ImageButton>(any()) } returns null
        every { mockFullscreenOverlays.values } returns mutableListOf(
            mockView1,
            mockView2,
            mockView3
        )

        every { playerState.mIsFullScreen } returns false

        testObject.setUpPlayerControlListeners()


        val slot = mutableListOf<Int>()
        verify {
            playerState.config
            arcXPVideoConfig.isDisableControlsFully
            playerState.mLocalPlayer
            playerState.mLocalPlayerView
            playerState.mLocalPlayerView
            playerView.findViewById<ImageButton>(R.id.exo_fullscreen)
            playerView.findViewById<ImageButton>(R.id.exo_share)
            playerView.findViewById<ImageButton>(R.id.exo_back)
            playerView.findViewById<ImageButton>(R.id.exo_pip)
            playerView.findViewById<ImageButton>(R.id.exo_volume)
            playerView.findViewById<ImageButton>(R.id.exo_cc)
            playerView.findViewById<ImageButton>(R.id.exo_next_button)
            playerView.findViewById<ImageButton>(R.id.exo_prev_button)
            //TODO test rest here or elsewhere
        }


    }

    @Test
    fun `toggleFullScreenDialog via setUpPlayerControlListeners fullScreen Button when is showFullScreen, fullscreen Button listener`() {
        val exoVolume = 0.83f
        val expectedResizeMode = 2343
        val expectedAutoShowControls = true
        val mockVideo = mockk<ArcVideo>()
        val expectedResize = mockk<ArcXPVideoConfig.VideoResizeMode>()
        every { utils.createExoPlayer() } returns exoPlayer
        every { utils.createPlayerView() } returns playerView
        every { exoPlayer.volume } returns exoVolume
        every { playerState.mVideo } returns mockVideo
        every { mockVideo.startMuted } returns false
        every { arcXPVideoConfig.videoResizeMode } returns expectedResize
        every { expectedResize.mode() } returns expectedResizeMode
        every { arcXPVideoConfig.isAutoShowControls } returns expectedAutoShowControls
        every { arcXPVideoConfig.isDisableControlsFully } returns false
        every { arcXPVideoConfig.showFullScreenButton } returns true
        every { playerState.mFullscreenOverlays } returns mockFullscreenOverlays
        every { playerState.mLocalPlayerView } returns playerView
        every { playerState.ccButton } returns null
        every { playerView.findViewById<ImageButton>(any()) } returns mockk(relaxed = true)
        every { playerView.findViewById<ImageButton>(R.id.exo_fullscreen) } returns fullScreenButton
        every { mockFullscreenOverlays.values } returns mutableListOf(
            mockView1,
            mockView2,
            mockView3
        )
        every { utils.createFullScreenDialog(mockActivity) } returns mFullScreenDialog
        every { playerState.mFullScreenDialog } returns mFullScreenDialog
        every { playerState.mIsFullScreen } returns false

        testObject.setUpPlayerControlListeners()

        val slot = slot<View.OnClickListener>()
        verify(exactly = 1) {
            fullScreenButton.setOnClickListener(capture(slot))
            fullScreenButton.visibility = VISIBLE
        }
        clearAllMocks(answers = false)
        slot.captured.onClick(mockk())

        verifySequence {
//            toggleFullScreenDialog()
            playerState.mIsFullScreen
            playerState.config
            arcXPVideoConfig.activity
            utils.createFullScreenDialog(mockActivity)
            playerState.mFullScreenDialog = mFullScreenDialog
            playerState.mFullScreenDialog
            mFullScreenDialog.setOnKeyListener(any())//TODO test this listener
            playerState.mFullScreenDialog
            playerState.mLocalPlayerView
            playerState.mLocalPlayerView

            playerView.parent
            playerState.mLocalPlayerView
            playerView.parent
            playerState.mLocalPlayerView
            playerViewParent.removeView(playerView)
            //addPlayerToFullScreen()
            playerState.mFullScreenDialog
            playerState.mLocalPlayerView

            utils.createLayoutParams()
            mFullScreenDialog.addContentView(playerView, layoutParams)
            // end addPlayerToFullScreen()
            //addOverlayToFullScreen()
            playerState.mFullScreenDialog
            utils.createLayoutParams()
            playerState.mFullscreenOverlays
            mockFullscreenOverlays.values
            mockView1.parent
            mockView1Parent.removeView(mockView1)
            playerState.mFullScreenDialog
            mFullScreenDialog.addContentView(mockView1, layoutParams)
            mockView1.bringToFront()
            mockView2.parent
            mockView2Parent.removeView(mockView2)
            playerState.mFullScreenDialog
            mFullScreenDialog.addContentView(mockView2, layoutParams)
            mockView2.bringToFront()
            mockView3.parent
            mockView3Parent.removeView(mockView3)
            playerState.mFullScreenDialog
            mFullScreenDialog.addContentView(mockView3, layoutParams)
            mockView3.bringToFront()

            //end addOverlayToFullScreen()
            playerState.mLocalPlayerView
            playerView.findViewById<ImageButton>(R.id.exo_fullscreen)
            playerState.config
            arcXPVideoConfig.activity
            fullScreenButton.setImageDrawable(mockFullScreenCollapseDrawable)
            playerState.mFullScreenDialog
            mFullScreenDialog.show()
            playerState.mIsFullScreen = true
            //createTrackingEvent()
            utils.createTrackingVideoTypeData()
            playerState.mVideo
            videoData.arcVideo = mockVideo
            playerState.mLocalPlayer
            playerState.mLocalPlayer
            exoPlayer.currentPosition
            videoData.position = expectedCurrentPosition
            mListener.onTrackingEvent(TrackingType.ON_OPEN_FULL_SCREEN, videoData)
            //end createTrackingEvent()

            trackingHelper.fullscreen()
            //end toggleFullScreenDialog()
        }
    }
}