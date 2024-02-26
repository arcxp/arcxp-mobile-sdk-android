package com.arcxp.video.players

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.provider.Settings
import android.util.Log
import android.view.KeyEvent
import android.view.KeyEvent.ACTION_UP
import android.view.MotionEvent
import android.view.MotionEvent.ACTION_BUTTON_PRESS
import android.view.View
import android.view.View.GONE
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.core.content.ContextCompat
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.Timeline
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.DefaultTimeBar
import androidx.media3.ui.PlayerView
import com.arcxp.commons.testutils.TestUtils.createDefaultVideo
import com.arcxp.sdk.R
import com.arcxp.video.ArcXPVideoConfig
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
import io.mockk.MockKAnnotations
import io.mockk.called
import io.mockk.clearAllMocks
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.spyk
import io.mockk.verify
import io.mockk.verifyOrder
import io.mockk.verifySequence
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

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
    private lateinit var playerView: PlayerView

    @RelaxedMockK
    private lateinit var audioAttributesBuilder: AudioAttributes.Builder

    @MockK
    private lateinit var audioAttributes: AudioAttributes

    @RelaxedMockK
    private lateinit var titleTextView: TextView

    @RelaxedMockK
    private lateinit var ccButton: ImageButton

    @RelaxedMockK
    private lateinit var backButton: ImageButton

    @RelaxedMockK
    private lateinit var exoProgress: DefaultTimeBar

    @RelaxedMockK
    private lateinit var exoTimeBarLayout: LinearLayout

    @RelaxedMockK
    private lateinit var exoPosition: View

    @RelaxedMockK
    private lateinit var exoDuration: View

    @RelaxedMockK
    private lateinit var separator: TextView

    @RelaxedMockK
    private lateinit var nextButton: ImageButton

    @RelaxedMockK
    private lateinit var previousButton: ImageButton

    @RelaxedMockK
    private lateinit var volumeButton: ImageButton

    @RelaxedMockK
    private lateinit var fullScreenButton: ImageButton

    @RelaxedMockK
    private lateinit var shareButton: ImageButton

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

    @RelaxedMockK
    lateinit var mockFullscreenOverlays: HashMap<String, View>

    @RelaxedMockK
    private lateinit var playerViewParent: ViewGroup

    @RelaxedMockK
    private lateinit var layoutParams: ViewGroup.LayoutParams

    @RelaxedMockK
    private lateinit var mockActivity: Activity

    @MockK
    private lateinit var ccDrawable: Drawable

    @MockK
    private lateinit var ccOffDrawable: Drawable

    @MockK
    private lateinit var mockDialog: AlertDialog

    @RelaxedMockK
    private lateinit var videoData: TrackingTypeData.TrackingVideoTypeData

    @RelaxedMockK
    private lateinit var mockBuilder: AlertDialog.Builder

    @MockK
    private lateinit var period: Timeline.Period

    @RelaxedMockK
    private lateinit var timeline: Timeline

    @MockK
    private lateinit var mockFullScreenCollapseDrawable: Drawable

    @MockK
    private lateinit var mockFullScreenDrawable: Drawable

    private val expectedCurrentPosition = 12345L
    private val expectedStartPosition = 22222L
    private val expectedId = "274893"
    private val subtitleUrl = "subtitle url"

    private lateinit var testObject: PlayerStateHelper


    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        every { playerView.findViewById<View>(any()) } returns mockk<View>(relaxed = true)
        every { playerView.findViewById<ImageButton>(any()) } returns mockk<ImageButton>(relaxed = true)
        every { playerView.findViewById<LinearLayout>(R.id.exo_time) } returns mockk<LinearLayout>(
            relaxed = true
        )

        every { playerView.findViewById<DefaultTimeBar>(R.id.exo_progress) } returns mockk<DefaultTimeBar>(
            relaxed = true
        )
        every { playerView.findViewById<TextView>(R.id.styled_controller_title_tv) } returns titleTextView
        every { playerView.findViewById<ImageButton>(R.id.exo_cc) } returns ccButton
        every { playerView.findViewById<ImageButton>(R.id.exo_fullscreen) } returns fullScreenButton
        every { playerView.findViewById<ImageButton>(R.id.exo_volume) } returns volumeButton
        every { playerView.findViewById<ImageButton>(R.id.exo_share) } returns shareButton
        every { playerView.findViewById<ImageButton>(R.id.exo_next_button) } returns nextButton
        every { playerView.findViewById<ImageButton>(R.id.exo_prev_button) } returns previousButton
        every { playerView.findViewById<ImageButton>(R.id.exo_back) } returns backButton
        every { playerView.findViewById<View>(R.id.exo_position) } returns exoPosition
        every { playerView.findViewById<View>(R.id.exo_duration) } returns exoDuration
        every { playerView.findViewById<DefaultTimeBar>(R.id.exo_progress) } returns exoProgress
        every { playerView.findViewById<TextView>(R.id.separator) } returns separator
        every { playerView.findViewById<LinearLayout>(R.id.exo_time) } returns exoTimeBarLayout

        every { utils.createAudioAttributeBuilder() } returns audioAttributesBuilder
        every { utils.createAlertDialogBuilder(mockActivity) } returns mockBuilder
        every { audioAttributesBuilder.setUsage(any()) } returns audioAttributesBuilder
        every { audioAttributesBuilder.setContentType(any()) } returns audioAttributesBuilder
        every { audioAttributesBuilder.build() } returns audioAttributes
        every { playerState.config } returns arcXPVideoConfig
        every { playerState.mLocalPlayer } returns exoPlayer
        every { playerState.mLocalPlayerView } returns playerView
        every { playerState.mVideoId } returns expectedId
        every { playerState.ccButton } returns ccButton
        every { playerState.mFullscreenOverlays } returns mockFullscreenOverlays
        every { playerState.mFullScreenDialog } returns mFullScreenDialog
        every { mockView1.parent } returns mockView1Parent
        every { mockView2.parent } returns mockView2Parent
        every { mockView3.parent } returns mockView3Parent
        every { playerView.parent } returns playerViewParent
        every { exoPlayer.currentPosition } returns expectedCurrentPosition

        every { utils.createLayoutParams() } returns layoutParams
        every { utils.createTrackingVideoTypeData() } returns videoData
        every { utils.createExoPlayer() } returns exoPlayer
        every { utils.createPlayerView() } returns playerView
        every { utils.createFullScreenDialog(mockActivity) } returns mFullScreenDialog
        every { arcXPVideoConfig.activity } returns mockActivity
        every { mockBuilder.setTitle("Picture-in-Picture functionality is disabled") } returns mockBuilder
        every { mockBuilder.setMessage("Would you like to enable Picture-in-Picture?") } returns mockBuilder
        every { mockBuilder.setPositiveButton(android.R.string.yes, any()) } returns mockBuilder
        every { mockBuilder.setNegativeButton(android.R.string.cancel, null) } returns mockBuilder
        every { mockBuilder.setCancelable(true) } returns mockBuilder
        every { mockBuilder.setIcon(android.R.drawable.ic_dialog_info) } returns mockBuilder
        every { mockBuilder.show() } returns mockDialog

        every { mockFullscreenOverlays.values } returns mutableListOf(
            mockView1,
            mockView2,
            mockView3
        )

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
        every {
            ContextCompat.getDrawable(
                mockActivity,
                R.drawable.CcDrawableButton
            )
        } returns ccDrawable
        every {
            ContextCompat.getDrawable(
                mockActivity,
                R.drawable.CcOffDrawableButton
            )
        } returns ccOffDrawable

        testObject =
            PlayerStateHelper(playerState, trackingHelper, utils, mListener, captionsManager)
        testObject.playerListener = playerListener
        testObject.playVideoAtIndex = mockk(relaxed = true)
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `initLocalPlayer not fullscreen, mVideo is not null, start muted, disableControls fully false, has ccButton, disable controls fully, not full screen, autoshow false, disable controls true`() {
        val exoVolume = 0.83f
        val expectedResizeMode = 2343
        val mockVideo = mockk<ArcVideo>()

        every { exoPlayer.volume } returns exoVolume


        every { playerState.mVideo } returns mockVideo

        every { mockVideo.startMuted } returns true
        val expectedResize = mockk<ArcXPVideoConfig.VideoResizeMode>()
        every { arcXPVideoConfig.videoResizeMode } returns expectedResize
        every { expectedResize.mode() } returns expectedResizeMode

        every { arcXPVideoConfig.isAutoShowControls } returns false
        every { arcXPVideoConfig.isDisableControls } returns true

        every { playerState.mIsFullScreen } returns false

        testObject.initLocalPlayer()

        verifySequence {
            utils.createExoPlayer()
            playerState.mLocalPlayer = exoPlayer
            playerState.mLocalPlayer
            exoPlayer.addListener(playerListener)
            utils.createPlayerView()
            utils.createLayoutParams()
            playerView.layoutParams = layoutParams
            playerState.config
            arcXPVideoConfig.videoResizeMode
            expectedResize.mode()
            playerView.resizeMode = expectedResizeMode
            playerView.id = R.id.wapo_player_view
            playerView.player = exoPlayer
            playerState.mLocalPlayerView = playerView
            playerView.findViewById<TextView>(R.id.styled_controller_title_tv)
            playerState.title = titleTextView
            playerState.mVideo
            mockVideo.startMuted
            exoPlayer.volume
            playerState.mCurrentVolume = exoVolume
            exoPlayer.volume = 0f
            playerState.config
            arcXPVideoConfig.isDisableControls
            utils.createAudioAttributeBuilder() // setAudioAttributes
            audioAttributesBuilder.setUsage(C.USAGE_MEDIA)
            audioAttributesBuilder.setContentType(C.AUDIO_CONTENT_TYPE_MOVIE)
            audioAttributesBuilder.build()
            exoPlayer.setAudioAttributes(audioAttributes, true) //setAudioAttributes
            playerState.mIsFullScreen
            mListener.addVideoView(playerView)
            playerState.mFullscreenOverlays
            mockFullscreenOverlays.values
            playerView.addView(mockView1)
            playerView.addView(mockView2)
            playerView.addView(mockView3)
            playerState.config
            arcXPVideoConfig.isDisableControls
            playerView.useController = false
        }
    }

    @Test
    fun `initLocalPlayer not fullscreen, mVideo is not null, start muted, disableControls fully true, has ccButton, disable controls fully, not full screen, autoshow true, disable controls true`() {
        val exoVolume = 0.83f
        val expectedResizeMode = 2343
        val mockVideo = mockk<ArcVideo>()

        every { exoPlayer.volume } returns exoVolume


        every { playerState.mVideo } returns mockVideo

        every { mockVideo.startMuted } returns true
        val expectedResize = mockk<ArcXPVideoConfig.VideoResizeMode>()
        every { arcXPVideoConfig.videoResizeMode } returns expectedResize
        every { expectedResize.mode() } returns expectedResizeMode

        every { arcXPVideoConfig.isAutoShowControls } returns true
        every { arcXPVideoConfig.isDisableControls } returns true

        every { playerState.mIsFullScreen } returns false

        testObject.initLocalPlayer()

        verifySequence {
            utils.createExoPlayer()
            playerState.mLocalPlayer = exoPlayer
            playerState.mLocalPlayer
            exoPlayer.addListener(playerListener)
            utils.createPlayerView()
            utils.createLayoutParams()
            playerView.layoutParams = layoutParams
            playerState.config
            arcXPVideoConfig.videoResizeMode
            expectedResize.mode()
            playerView.resizeMode = expectedResizeMode
            playerView.id = R.id.wapo_player_view
            playerView.player = exoPlayer
            playerState.mLocalPlayerView = playerView
            playerView.findViewById<TextView>(R.id.styled_controller_title_tv)
            playerState.title = titleTextView
            playerState.mVideo
            mockVideo.startMuted
            exoPlayer.volume
            playerState.mCurrentVolume = exoVolume
            exoPlayer.volume = 0f
            playerState.config
            arcXPVideoConfig.isDisableControls // setUpPlayerControlListeners
            utils.createAudioAttributeBuilder() // setAudioAttributes
            audioAttributesBuilder.setUsage(C.USAGE_MEDIA)
            audioAttributesBuilder.setContentType(C.AUDIO_CONTENT_TYPE_MOVIE)
            audioAttributesBuilder.build()
            exoPlayer.setAudioAttributes(audioAttributes, true) //setAudioAttributes
            playerState.mIsFullScreen
            mListener.addVideoView(playerView)
            playerState.mFullscreenOverlays
            mockFullscreenOverlays.values
            playerView.addView(mockView1)
            playerView.addView(mockView2)
            playerView.addView(mockView3)
            playerState.config
            arcXPVideoConfig.isDisableControls
            playerView.useController = false
        }
    }

    @Test
    fun `setUpPlayerControlListeners disableControls fully false, null local player`() {
        val exoVolume = 0.83f
        val expectedResizeMode = 2343
        val expectedAutoShowControls = true
        val mockVideo = mockk<ArcVideo>()

        every { exoPlayer.volume } returns exoVolume


        every { playerState.mVideo } returns mockVideo

        every { mockVideo.startMuted } returns false
        val expectedResize = mockk<ArcXPVideoConfig.VideoResizeMode>()
        every { arcXPVideoConfig.videoResizeMode } returns expectedResize
        every { expectedResize.mode() } returns expectedResizeMode

        every { arcXPVideoConfig.isAutoShowControls } returns expectedAutoShowControls
        every { arcXPVideoConfig.isDisableControls } returns false
        every { playerState.mLocalPlayer } returns null

        every { playerState.mIsFullScreen } returns false

        testObject.setUpPlayerControlListeners()

        verifySequence {
            playerState.config
            arcXPVideoConfig.isDisableControls
            playerState.mLocalPlayer
        }
    }

//    @Test
//    fun `setUpPlayerControlListener setControllerHideDuringAds true`() {
//        every { arcXPVideoConfig.isHideControlsDuringAds } returns true
//
//        testObject.setUpPlayerControlListeners()
//
//        verify(exactly = 1) {
//            playerView.setControllerHideDuringAds(true)
//        }
//    }
//
//    @Test
//    fun `setUpPlayerControlListener setControllerHideDuringAds false`() {
//        every { arcXPVideoConfig.isHideControlsDuringAds } returns false
//
//        testObject.setUpPlayerControlListeners()
//
//        verify(exactly = 1) {
//            playerView.setControllerHideDuringAds(false)
//        }
//    }

    @Test
    fun `setUpPlayerControlListener controllerHideOnTouch true`() {
        every { arcXPVideoConfig.isHideControlsWithTouch } returns true

        testObject.setUpPlayerControlListeners()

        verify(exactly = 1) {
            playerView.controllerHideOnTouch = true
        }
    }

    @Test
    fun `setUpPlayerControlListener controllerHideOnTouch false`() {
        every { arcXPVideoConfig.isHideControlsWithTouch } returns false

        testObject.setUpPlayerControlListeners()

        verify(exactly = 1) {
            playerView.controllerHideOnTouch = false
        }
    }

    @Test
    fun `setUpPlayerControlListener controllerAutoShow true`() {
        every { arcXPVideoConfig.isAutoShowControls } returns true

        testObject.setUpPlayerControlListeners()

        verify(exactly = 1) {
            playerView.controllerAutoShow = true
        }
    }

    @Test
    fun `setUpPlayerControlListener controllerAutoShow false`() {
        every { arcXPVideoConfig.isAutoShowControls } returns false

        testObject.setUpPlayerControlListeners()

        verify(exactly = 1) {
            playerView.controllerAutoShow = false
        }
    }

    @Test
    fun `setUpPlayerControlListeners disableControls fully false, null local player view`() {
        val exoVolume = 0.83f
        val expectedResizeMode = 2343
        val expectedAutoShowControls = true
        val mockVideo = mockk<ArcVideo>()

        every { exoPlayer.volume } returns exoVolume


        every { playerState.mVideo } returns mockVideo

        every { mockVideo.startMuted } returns false
        val expectedResize = mockk<ArcXPVideoConfig.VideoResizeMode>()
        every { arcXPVideoConfig.videoResizeMode } returns expectedResize
        every { expectedResize.mode() } returns expectedResizeMode

        every { arcXPVideoConfig.isAutoShowControls } returns expectedAutoShowControls
        every { arcXPVideoConfig.isDisableControls } returns false
        every { playerState.mLocalPlayerView } returns null

        every { playerState.mIsFullScreen } returns false

        testObject.setUpPlayerControlListeners()

        verifySequence {
            playerState.config
            arcXPVideoConfig.isDisableControls
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

        every { exoPlayer.volume } returns exoVolume
        every { playerState.mVideo } returns mockVideo

        every { mockVideo.startMuted } returns false
        val expectedResize = mockk<ArcXPVideoConfig.VideoResizeMode>()
        every { arcXPVideoConfig.videoResizeMode } returns expectedResize
        every { expectedResize.mode() } returns expectedResizeMode

        every { arcXPVideoConfig.isAutoShowControls } returns expectedAutoShowControls
        every { arcXPVideoConfig.isDisableControls } returns false
        every { playerState.ccButton } returns null
        every { playerView.findViewById<ImageButton>(any()) } returns null

        every { playerState.mIsFullScreen } returns false

        testObject.setUpPlayerControlListeners()


        val slot = mutableListOf<Int>()
        verify {
            playerState.config
            arcXPVideoConfig.isDisableControls
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
        every { arcXPVideoConfig.showFullScreenButton } returns true
        every { exoPlayer.volume } returns exoVolume
        every { playerState.mVideo } returns mockVideo
        every { mockVideo.startMuted } returns false
        every { arcXPVideoConfig.videoResizeMode } returns expectedResize
        every { expectedResize.mode() } returns expectedResizeMode
        every { arcXPVideoConfig.isAutoShowControls } returns expectedAutoShowControls
        every { arcXPVideoConfig.isDisableControls } returns false
        every { playerState.ccButton } returns null
//        every { playerView.findViewById<ImageButton>(any()) } returns mockk(relaxed = true)
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

    @Test
    fun `setUpPlayerControlListeners fullScreen Button when is not showFullScreen`() {
        every { arcXPVideoConfig.showFullScreenButton } returns false

        testObject.setUpPlayerControlListeners()

        verify(exactly = 1) {
            fullScreenButton.visibility = GONE
        }
    }

//    @Test
//    fun `setUpPlayerControlListeners fullScreen Button when full screen button is null`() {
//        every { playerView.findViewById<ImageButton>(R.id.exo_fullscreen) } returns null
//        every { arcXPVideoConfig.showFullScreenButton } returns true
//
//        testObject.setUpPlayerControlListeners()
//
//        verify { fullScreenButton wasNot called }
//    }//don't think we need this, TODO verify coverage without this


    @Test
    fun `onVideoEvent when live and tracking type is percentage`() {
        mockkStatic(Log::class)
        every { playerState.mIsLive } returns true

        testObject.onVideoEvent(trackingType = TrackingType.VIDEO_PERCENTAGE_WATCHED, value = null)

        verify(exactly = 0) {
            Log.e(any(), any())
            mListener.onTrackingEvent(any(), any())
        }
    }

    @Test
    fun `onVideoEvent when not live and tracking type is percentage`() {
        mockkStatic(Log::class)
        every { playerState.mIsLive } returns false
        val trackingType = TrackingType.VIDEO_PERCENTAGE_WATCHED
        val trackingTypeData = mockk<TrackingTypeData>()

        testObject.onVideoEvent(trackingType = trackingType, value = trackingTypeData)

        verifySequence {
            Log.e("ArcVideoSDK", "onVideoEvent $trackingType")
            mListener.onTrackingEvent(trackingType, trackingTypeData)
        }
    }

    @Test
    fun `onVideoEvent when tracking type is not percentage`() {
        mockkStatic(Log::class)
        val trackingType = TrackingType.AD_BREAK_ENDED
        val trackingTypeData = mockk<TrackingTypeData>()

        testObject.onVideoEvent(trackingType = trackingType, value = trackingTypeData)

        verifySequence {
            Log.e("ArcVideoSDK", "onVideoEvent $trackingType")
            mListener.onTrackingEvent(trackingType, trackingTypeData)
        }

    }

    @Test
    fun `onVideoEvent logs event and calls through to listener`() {

        mockkStatic(Log::class)


    }

    @Test
    fun `getCurrentTimelinePosition returns 0 with exception`() {
        every { playerState.mLocalPlayer!!.currentPosition } throws Exception()
        assertEquals(0, testObject.getCurrentTimelinePosition())
    }

    @Test
    fun `getCurrentTimelinePosition returns 0 if player is null`() {
        every { playerState.mLocalPlayer } returns null
        assertEquals(0, testObject.getCurrentTimelinePosition())
    }

    @Test
    fun `getCurrentTimelinePosition returns position if exoPlayer is not null`() {
        val expectedAdjustedPosition = /*12345 - 744 = */11601L
        val expectedPeriodIndex = 7
        val expectedPeriodPosition = 744L
        every {
            exoPlayer.currentTimeline
        } returns timeline

        every {
            timeline.getPeriod(
                expectedPeriodIndex,
                playerState.period
            )
        } returns period
        every { period.positionInWindowMs } returns expectedPeriodPosition


//        every { exoPlayer.currentPosition } returns expectedCurrentPosition
        every { exoPlayer.currentPeriodIndex } returns expectedPeriodIndex



        assertEquals(expectedAdjustedPosition, testObject.getCurrentTimelinePosition())
    }

//    @Test
//    fun `setUpPlayerControl Listeners share button with empty url keepcontrolsspaceonhide sets invisible`() {
//        val exoVolume = 0.83f
//        val expectedResizeMode = 2343
//        val expectedAutoShowControls = true
//        val mockVideo = mockk<ArcVideo>()
//        val expectedResize = mockk<ArcXPVideoConfig.VideoResizeMode>()
//        every { arcXPVideoConfig.showFullScreenButton } returns true
//        every { arcXPVideoConfig.isKeepControlsSpaceOnHide } returns true
//        every { utils.createExoPlayer() } returns exoPlayer
//        every { utils.createPlayerView() } returns playerView
//        every { exoPlayer.volume } returns exoVolume
//        every { playerState.mVideo } returns mockVideo
//        every { mockVideo.startMuted } returns false
//        every { arcXPVideoConfig.videoResizeMode } returns expectedResize
//        every { expectedResize.mode() } returns expectedResizeMode
//        every { arcXPVideoConfig.isAutoShowControls } returns expectedAutoShowControls
//        every { arcXPVideoConfig.isDisableControls } returns false
//        every { playerState.ccButton } returns null
//        every { playerState.mShareUrl } returns ""
////        every { playerView.findViewById<ImageButton>(any()) } returns mockk(relaxed = true)
//        every { mockFullscreenOverlays.values } returns mutableListOf(
//            mockView1,
//            mockView2,
//            mockView3
//        )
//        every { playerState.mIsFullScreen } returns false
//
//        testObject.setUpPlayerControlListeners()
//
//        val slot = slot<View.OnClickListener>()
//        verify(exactly = 1) {
//            shareButton.setOnClickListener(capture(slot))
//            fullScreenButton.visibility = VISIBLE
//        }
//        clearAllMocks(answers = false)
//        slot.captured.onClick(mockk())
//    }


    @Test
    fun `volumeButton onClick when player volume is non-zero`() {
        val volumeButtonListener = slot<View.OnClickListener>()
        val expectedVolume = 0.6783f
        val drawable = mockk<Drawable>()
        val videoData = mockk<TrackingTypeData.TrackingVideoTypeData>(relaxed = true)
        every { utils.createTrackingVideoTypeData() } returns videoData
        every { exoPlayer.volume } returns expectedVolume
        every {
            ContextCompat.getDrawable(
                mockActivity,
                R.drawable.MuteDrawableButton
            )
        } returns drawable
        every { arcXPVideoConfig.showVolumeButton } returns true
        testObject.setUpPlayerControlListeners()
        verify { volumeButton.setOnClickListener(capture(volumeButtonListener)) }
        clearAllMocks(answers = false)

        volumeButtonListener.captured.onClick(mockk())

        verifyOrder {
            utils.createTrackingVideoTypeData()
            videoData.arcVideo = playerState.mVideo
            exoPlayer.currentPosition
            videoData.position = expectedCurrentPosition
            exoPlayer.volume
            exoPlayer.volume
            exoPlayer.volume = 0f
            ContextCompat.getDrawable(mockActivity, R.drawable.MuteDrawableButton)
            volumeButton.setImageDrawable(drawable)
            mListener.onTrackingEvent(TrackingType.ON_MUTE, videoData)
            trackingHelper.volumeChange(0f)
        }
    }


    @Test
    fun `volumeButton button onClick when player volume is zero`() {
        val volumeButtonListener = slot<View.OnClickListener>()
        val videoData = mockk<TrackingTypeData.TrackingVideoTypeData>(relaxed = true)
        val muteOffDrawableButton = mockk<Drawable>()

        every { arcXPVideoConfig.showVolumeButton } returns true
        testObject.setUpPlayerControlListeners()
        verify { volumeButton.setOnClickListener(capture(volumeButtonListener)) }
        every { utils.createTrackingVideoTypeData() } returns videoData
        every { exoPlayer.volume } returns 0.0f
        every {
            ContextCompat.getDrawable(
                mockActivity,
                R.drawable.MuteOffDrawableButton
            )
        } returns muteOffDrawableButton
        clearAllMocks(answers = false)

        volumeButtonListener.captured.onClick(mockk())

        verifyOrder {
            utils.createTrackingVideoTypeData()
            videoData.arcVideo = playerState.mVideo
            exoPlayer.currentPosition
            videoData.position = expectedCurrentPosition
            exoPlayer.volume
            exoPlayer.volume = 0.0f
            volumeButton.setImageDrawable(muteOffDrawableButton)
            mListener.onTrackingEvent(TrackingType.ON_UNMUTE, videoData)
            trackingHelper.volumeChange(0f)
        }
    }

    @Test
    fun `nextButton On Click Listener when no next video in mVideos calls tracking event`() {
        val arcVideo = createDefaultVideo()
        val videoData = mockk<TrackingTypeData.TrackingVideoTypeData>(relaxed = true)
        val listener = slot<View.OnClickListener>()
        every { arcXPVideoConfig.showNextPreviousButtons } returns true
        every { utils.createTrackingVideoTypeData() } returns videoData
        testObject.setUpPlayerControlListeners()

        verify { nextButton.setOnClickListener(capture(listener)) }
        clearAllMocks(answers = false)
        every { playerState.mVideo } returns arcVideo

        listener.captured.onClick(mockk())

        verifyOrder {
            utils.createTrackingVideoTypeData()
            videoData.arcVideo = arcVideo
            exoPlayer.currentPosition
            videoData.position = expectedCurrentPosition
            mListener.onTrackingEvent(TrackingType.NEXT_BUTTON_PRESSED, videoData)
        }
    }

    @Test
    fun `nextButton On Click Listener when has next video in mVideos play on next and calls tracking event`() {
        val arcVideo1 = createDefaultVideo()
        val arcVideo2 = createDefaultVideo()


        val videoData = mockk<TrackingTypeData.TrackingVideoTypeData>(relaxed = true)
        val listener = mutableListOf<View.OnClickListener>()
        every { arcXPVideoConfig.showNextPreviousButtons } returns true
        every { utils.createTrackingVideoTypeData() } returns videoData
        every { playerState.mVideos } returns mutableListOf(arcVideo1, arcVideo2)


        testObject.setUpPlayerControlListeners()
        verify(exactly = 2) { nextButton.setOnClickListener(capture(listener)) }
        clearAllMocks(answers = false)
        testObject = spyk(testObject)


        listener[1].onClick(mockk())

        verify(exactly = 1) {
            playerState.incrementVideoIndex(true)
            mListener.onTrackingEvent(TrackingType.NEXT_BUTTON_PRESSED, videoData)
        }
    }


    @Test
    fun `prevButton On Click Listener when no prev video in mVideos calls tracking event`() {
        val arcVideo = createDefaultVideo()
        val videoData = mockk<TrackingTypeData.TrackingVideoTypeData>(relaxed = true)
        val listener = slot<View.OnClickListener>()
        every { arcXPVideoConfig.showNextPreviousButtons } returns true
        every { utils.createTrackingVideoTypeData() } returns videoData

        testObject.setUpPlayerControlListeners()

        verify { previousButton.setOnClickListener(capture(listener)) }
        clearAllMocks(answers = false)
        every { playerState.mVideo } returns arcVideo

        listener.captured.onClick(mockk())

        verifyOrder {
            utils.createTrackingVideoTypeData()
            videoData.arcVideo = arcVideo
            exoPlayer.currentPosition
            videoData.position = expectedCurrentPosition
            mListener.onTrackingEvent(TrackingType.PREV_BUTTON_PRESSED, videoData)
        }
    }


    @Test
    fun `when next or previous button null, do not set listeners`() {
        every { playerView.findViewById<View>(R.id.exo_next_button) } returns null
        every { playerView.findViewById<View>(R.id.exo_prev_button) } returns null
        every { arcXPVideoConfig.showNextPreviousButtons } returns true
        val videoData = mockk<TrackingTypeData.TrackingVideoTypeData>(relaxed = true)
        every { utils.createTrackingVideoTypeData() } returns videoData

        testObject.setUpPlayerControlListeners()

        verify { nextButton wasNot called }
        verify { previousButton wasNot called }
    }

    @Test
    fun `volume button is hidden when configured to not show`() {
        arcXPVideoConfig.apply {
            every { showVolumeButton } returns false
        }

        testObject.setUpPlayerControlListeners()

        verify(exactly = 1) {
            volumeButton.visibility = GONE
        }
    }


    @Test
    fun `title view is hidden when configured to not show`() {

        every { arcXPVideoConfig.showTitleOnController } returns false

        every { playerState.title } returns titleTextView

        testObject.setUpPlayerControlListeners()

        verify(exactly = 1) {
            titleTextView.visibility = INVISIBLE
        }
    }

    @Test
    fun `title view is properly set with correct title from mHeadline`() {
        every { arcXPVideoConfig.showTitleOnController } returns true


        every { playerState.title } returns titleTextView
        every { playerState.mVideo } returns createDefaultVideo()

        testObject.setUpPlayerControlListeners()

        verify(exactly = 1) {
            titleTextView.visibility = VISIBLE
            titleTextView.text = "headline"
        }
    }

    @Test
    fun `prevButton On Click Listener when mVideos size greater than 1 calls tracking event, plays prev`() {
        every { arcXPVideoConfig.showNextPreviousButtons } returns true
        val arcVideo1 = createDefaultVideo()
        val arcVideo2 = createDefaultVideo()
        val videoList = mutableListOf(arcVideo1, arcVideo2)
        val videoData = mockk<TrackingTypeData.TrackingVideoTypeData>(relaxed = true)
        val listener = mutableListOf<View.OnClickListener>()
        every { utils.createTrackingVideoTypeData() } returns videoData
        every { playerState.mVideos } returns videoList

        testObject.setUpPlayerControlListeners()

        verify(exactly = 1) {
            previousButton.visibility = VISIBLE
        }
        verify(exactly = 2) {
            previousButton.setOnClickListener(capture(listener))
        }
        clearAllMocks(answers = false)
        every { playerState.currentVideoIndex } returns 1
        listener[1].onClick(mockk())

        verifyOrder {
            playerState.incrementVideoIndex(false)
            mListener.onTrackingEvent(TrackingType.PREV_BUTTON_PRESSED, videoData)
        }
    }


    @Test
    fun `getShowNextPreviousButtons false and next prev buttons are null`() {
        val videoList = mutableListOf(createDefaultVideo(), createDefaultVideo())
        val videoData = mockk<TrackingTypeData.TrackingVideoTypeData>(relaxed = true)

        every { playerView.findViewById<View>(R.id.exo_next_button) } returns null
        every { playerView.findViewById<View>(R.id.exo_prev_button) } returns null
        every { arcXPVideoConfig.showNextPreviousButtons } returns false
        every { utils.createTrackingVideoTypeData() } returns videoData
        every { playerState.mVideos } returns videoList
        testObject.setUpPlayerControlListeners()

        verify { nextButton wasNot called }
        verify { previousButton wasNot called }
    }


    @Test
    fun `initial setup when disabled controls fully does not run logic in set up player control listeners`() {
        every { arcXPVideoConfig.isDisableControls } returns true

        testObject.initLocalPlayer()
        verify(exactly = 1) {
            playerView.useController = false
        }
        verify(exactly = 2) {
            arcXPVideoConfig.isDisableControls
        }
        verify {
            playerView.findViewById<ImageButton>(R.id.exo_fullscreen) wasNot called
        }
    }


    @Test
    fun `set Video Captions Drawable when PrefManager has captions enabled`() {
        mockkStatic(PrefManager::class)
        every {
            PrefManager.getBoolean(
                mockActivity,
                PrefManager.IS_CAPTIONS_ENABLED,
                false
            )
        } returns true

        testObject.initLocalPlayer()


        verify(exactly = 2) {
            playerView.findViewById<ImageButton>(R.id.exo_cc)

        }
        verify(exactly = 1) {
            PrefManager.getBoolean(mockActivity, PrefManager.IS_CAPTIONS_ENABLED, false)
            ccButton.setImageDrawable(ccDrawable)
        }
    }


    @Test
    fun `set Video Captions Drawable when using CCStartMode ON config cc start mode`() {
        every { arcXPVideoConfig.ccStartMode } returns ArcXPVideoConfig.CCStartMode.ON

        testObject.initLocalPlayer()
        verify(exactly = 2) {
            playerView.findViewById<ImageButton>(R.id.exo_cc)
        }
        verify(exactly = 1) {
            PrefManager.getBoolean(mockActivity, PrefManager.IS_CAPTIONS_ENABLED, false)
            arcXPVideoConfig.ccStartMode
            ContextCompat.getDrawable(mockActivity, R.drawable.CcDrawableButton)
            ccButton.setImageDrawable(ccDrawable)
        }
    }

    @Test
    fun `initLocalPlayer mIsFullScreen is false, call addPlayerToFullScreen`() {
        val exoVolume = 0.83f
        val expectedResizeMode = 2343
        val expectedAutoShowControls = true
        val mockVideo = mockk<ArcVideo>()

        every { exoPlayer.volume } returns exoVolume


        every { playerState.mVideo } returns mockVideo

        every { mockVideo.startMuted } returns true
        val expectedResize = mockk<ArcXPVideoConfig.VideoResizeMode>()
        every { arcXPVideoConfig.videoResizeMode } returns expectedResize
        every { expectedResize.mode() } returns expectedResizeMode

        every { arcXPVideoConfig.isAutoShowControls } returns true
        every { arcXPVideoConfig.isDisableControls } returns true
        every { arcXPVideoConfig.isHideControlsWithTouch } returns true

        every { playerState.mIsFullScreen } returns true

        testObject.initLocalPlayer()
        verifySequence {
            utils.createExoPlayer()
            playerState.mLocalPlayer = exoPlayer
            playerState.mLocalPlayer
            exoPlayer.addListener(playerListener)
            utils.createPlayerView()
            utils.createLayoutParams()
            playerView.layoutParams = layoutParams
            playerState.config
            arcXPVideoConfig.videoResizeMode
            expectedResize.mode()
            playerView.resizeMode = expectedResizeMode
            playerView.id = R.id.wapo_player_view
            playerView.player = exoPlayer
            playerState.mLocalPlayerView = playerView
            playerView.findViewById<TextView>(R.id.styled_controller_title_tv)
            playerState.title = titleTextView
            playerState.mVideo
            mockVideo.startMuted
            exoPlayer.volume
            playerState.mCurrentVolume = exoVolume
            exoPlayer.volume = 0f
            playerState.config
            arcXPVideoConfig.isDisableControls // setUpPlayerControlListeners
            utils.createAudioAttributeBuilder() // setAudioAttributes
            audioAttributesBuilder.setUsage(C.USAGE_MEDIA)
            audioAttributesBuilder.setContentType(C.AUDIO_CONTENT_TYPE_MOVIE)
            audioAttributesBuilder.build()
            exoPlayer.setAudioAttributes(audioAttributes, true) //setAudioAttributes
            playerState.mIsFullScreen
            playerState.mFullScreenDialog
            playerState.mLocalPlayerView
            utils.createLayoutParams()
            playerState.mFullscreenOverlays
            mockFullscreenOverlays.values
            playerView.addView(mockView1)
            playerView.addView(mockView2)
            playerView.addView(mockView3)
            playerState.config
            arcXPVideoConfig.isDisableControls
            playerView.useController = false
        }
    }

    @Test
    fun `playVideos when has more videos in mVideos sets nextButton as enabled, onclick listener plays next video (not fullscreen)`() {
        every { arcXPVideoConfig.showNextPreviousButtons } returns true
        val arcVideo1 = createDefaultVideo()
        val arcVideo2 = createDefaultVideo()
        val videoList = mutableListOf(arcVideo1, arcVideo2)
        val expectedCurrentPosition = 876435L
        val videoData = mockk<TrackingTypeData.TrackingVideoTypeData>(relaxed = true)
        val listener = mutableListOf<View.OnClickListener>()
        every { utils.createTrackingVideoTypeData() } returns videoData
        every { playerState.mVideos } returns videoList

        testObject.setUpPlayerControlListeners()

        verify(exactly = 2) {
            nextButton.setOnClickListener(capture(listener))
        }
        clearAllMocks(answers = false)
        listener[1].onClick(mockk())

        verify(exactly = 1) {
            playerState.incrementVideoIndex(true)
            mListener.onTrackingEvent(TrackingType.NEXT_BUTTON_PRESSED, videoData)
        }

    }

    @Test
    fun `playVideos when has no more videos in mVideos sets nextButton as disabled`() {
        every { arcXPVideoConfig.showNextPreviousButtons } returns true
        val arcVideo1 = createDefaultVideo()
        val arcVideo2 = createDefaultVideo()
        val videoList = mutableListOf(arcVideo1, arcVideo2)
        val expectedCurrentPosition = 876435L
        val videoData = mockk<TrackingTypeData.TrackingVideoTypeData>(relaxed = true)
        val listener = slot<View.OnClickListener>()
        every { utils.createTrackingVideoTypeData() } returns videoData
        every { playerState.mVideos } returns videoList
        every { playerState.currentVideoIndex } returns 1

        testObject.setUpPlayerControlListeners()

//
//        verify {
////            nextButton.visibility = VISIBLE
//            nextButton.setOnClickListener(capture(listener))
////            nextButton.alpha = 0.5f
////            nextButton.isEnabled = false
//        }
//        clearAllMocks(answers = false)
//
//
//        listener.captured.onClick(mockk())

        verify {
            nextButton.alpha = .5f
            nextButton.isEnabled = false
        }
    }


    @Test
    fun `playVideo when showNextPrevious enabled shows next and prev Buttons, does not disable them`() {
        arcXPVideoConfig.apply {
            every { showNextPreviousButtons } returns true
            every { shouldDisableNextButton } returns false
            every { shouldDisablePreviousButton } returns false
        }

        testObject.setUpPlayerControlListeners()

        verify(exactly = 1) {
            nextButton.visibility = VISIBLE
            previousButton.visibility = VISIBLE
        }
        verify(exactly = 0) {
            nextButton.isEnabled = false
            previousButton.isEnabled = false
        }
    }

    @Test
    fun `playVideo when showNextPrevious disabled hides next and previous button`() {
        testObject.setUpPlayerControlListeners()

        verify(exactly = 1) {
            nextButton.visibility = GONE
            previousButton.visibility = GONE
        }
    }

    @Test
    fun `playVideo when shouldDisableNextButton disables next button`() {

        every { arcXPVideoConfig.showNextPreviousButtons } returns true
        every { arcXPVideoConfig.shouldDisableNextButton } returns true


        testObject.setUpPlayerControlListeners()

        verify(exactly = 1) {
            nextButton.isEnabled = false
        }
    }

    @Test
    fun `playVideo when shouldDisablePreviousButton disables previous button`() {
        every { arcXPVideoConfig.showNextPreviousButtons } returns true
        every { arcXPVideoConfig.shouldDisablePreviousButton } returns true

        testObject.setUpPlayerControlListeners()

        verify(exactly = 1) {
            previousButton.isEnabled = false
        }
    }


    @Test
    fun `shareButton player onClick triggers share video event, and then notifies listener`() {
        val shareButtonListener = slot<View.OnClickListener>()
        val arcVideo = createDefaultVideo()
        val videoData = mockk<TrackingTypeData.TrackingVideoTypeData>(relaxed = true)
        every { playerState.mVideo } returns arcVideo
        every { playerState.mHeadline } returns "headline"
        every { playerState.mShareUrl } returns "mShareUrl"
        every { utils.createTrackingVideoTypeData() } returns videoData
        testObject.setUpPlayerControlListeners()
        verify { shareButton.setOnClickListener(capture(shareButtonListener)) }
        clearAllMocks(answers = false)


        shareButtonListener.captured.onClick(mockk())

        verifyOrder {
            utils.createTrackingVideoTypeData()
            videoData.arcVideo = arcVideo
            exoPlayer.currentPosition
            videoData.position = expectedCurrentPosition
            mListener.onTrackingEvent(TrackingType.ON_SHARE, videoData)
            mListener.onShareVideo("headline", "mShareUrl")
        }
    }

    @Test
    fun `backButton player onClick triggers back event, and then notifies listener`() {
        every { arcXPVideoConfig.showBackButton } returns true
        val backButtonListener = slot<View.OnClickListener>()
        val arcVideo = createDefaultVideo()
        val videoData = mockk<TrackingTypeData.TrackingVideoTypeData>(relaxed = true)
        every { playerState.mVideo } returns arcVideo
        every { utils.createTrackingVideoTypeData() } returns videoData
        testObject.setUpPlayerControlListeners()
        verify { backButton.setOnClickListener(capture(backButtonListener)) }
        clearAllMocks(answers = false)

        backButtonListener.captured.onClick(mockk())

        verifySequence {
            utils.createTrackingVideoTypeData()
            videoData.arcVideo = arcVideo
            exoPlayer.currentPosition
            videoData.position = expectedCurrentPosition
            mListener.onTrackingEvent(TrackingType.BACK_BUTTON_PRESSED, videoData)
        }
    }

    @Test
    fun `onPipEnter when pipEnabled and not fullscreen, starts pip`() {
        val arcVideo = createDefaultVideo()
        every { mListener.isPipEnabled } returns true
        every { playerState.mIsFullScreen } returns false
        every { playerState.mVideo } returns arcVideo
        every { playerState.mFullscreenOverlays } returns mockFullscreenOverlays
        every { mockFullscreenOverlays.values } returns mutableListOf(
            mockView1,
            mockView2,
            mockView3
        )
        testObject.onPipEnter()

        verify {
            mListener.isPipEnabled
            utils.createFullScreenDialog(mockActivity)
            mFullScreenDialog.setOnKeyListener(any())
            playerView.parent
            playerView.parent
            playerViewParent.removeView(playerView)
            utils.createLayoutParams()
            mFullScreenDialog.addContentView(playerView, any())
            mockFullscreenOverlays.values
            mockView1.parent
            mockView1Parent.removeView(mockView1)
            utils.createLayoutParams()
            mFullScreenDialog.addContentView(mockView1, any())
            mockView1.bringToFront()

            mockView2.parent
            mockView2Parent.removeView(mockView2)
            utils.createLayoutParams()
            mFullScreenDialog.addContentView(mockView2, any())
            mockView2.bringToFront()

            mockView3.parent
            mockView3Parent.removeView(mockView3)
            utils.createLayoutParams()
            mFullScreenDialog.addContentView(mockView3, any())
            mockView3.bringToFront()

            playerView.findViewById<ImageButton>(R.id.exo_fullscreen)
            ContextCompat.getDrawable(mockActivity, R.drawable.FullScreenDrawableButtonCollapse)
            fullScreenButton.setImageDrawable(mockFullScreenCollapseDrawable)
            mFullScreenDialog.show()
            utils.createTrackingVideoTypeData()
            videoData.arcVideo = arcVideo
            exoPlayer.currentPosition
            videoData.position = expectedCurrentPosition
            mListener.onTrackingEvent(TrackingType.ON_OPEN_FULL_SCREEN, videoData)
            trackingHelper.fullscreen()
            playerView.hideController()
            exoPlayer.currentPosition
            mListener.setSavedPosition(expectedId, expectedCurrentPosition)
            mListener.startPIP(arcVideo)

        }
    }

    @Test
    fun `pipButton onClick when in full screen, starts pip mode`() {
        val arcVideo = createDefaultVideo()
        every { mListener.isPipEnabled } returns true
        every { playerState.mIsFullScreen } returns true
        every { playerState.mVideo } returns arcVideo
        every { playerState.mFullscreenOverlays } returns mockFullscreenOverlays
        every { mockFullscreenOverlays.values } returns mutableListOf(
            mockView1,
            mockView2,
            mockView3
        )
        testObject.onPipEnter()

        verify(exactly = 1) {
            playerView.hideController()
            exoPlayer.currentPosition
            mListener.setSavedPosition(expectedId, expectedCurrentPosition)
            mListener.startPIP(arcVideo)
        }
    }
//think this is dupe:
//    @Test
//    fun `pipButton onClick when pipEnabled and is fullscreen, starts pip`() {
//        val arcVideo = ArcVideo(
//            expectedId,
//            "uuid",
//            expectedStartPosition,
//            false,
//            false,
//            100,
//            "shareUrl",
//            "headline",
//            "pageName",
//            "videoName",
//            "videoSection",
//            "videoSource",
//            "videoCategory",
//            "consentId",
//            "fallbackUrl",
//            "addTagUrl[timestamp]",
//            true,
//            subtitleUrl,
//            "source",
//            mockk(),
//            false,
//            false,
//            false,
//            ArcXPVideoConfig.CCStartMode.DEFAULT
//        )
//        val pipButtonListener = slot<View.OnClickListener>()
//        val expectedPosition = 9862345L
//        val mFullScreenDialog = mockk<Dialog>(relaxed = true)
//
//        val viewGroup = mockk<ViewGroup>(relaxed = true)
//        val drawable = mockk<Drawable>()
//        val videoData = mockk<TrackingTypeData.TrackingVideoTypeData>(relaxed = true)
//        every { utils.createTrackingVideoTypeData() } returns videoData
//
//        every {
//            ContextCompat.getDrawable(
//                mockActivity,
//                R.drawable.FullScreenDrawableButtonCollapse
//            )
//        } returns drawable
//        every { playerView.parent } returns viewGroup
//        every { mockView1.parent } returns viewGroup
//        every { mockView2.parent } returns viewGroup
//        every { mockView3.parent } returns viewGroup
//        every { utils.createLayoutParams() } returns mockk()
//
//        every { mListener.isPipEnabled } returns true
//        every { exoPlayer.currentPosition } returns expectedPosition
//        testObject.playVideo(arcVideo)
//        verify { pipButton.setOnClickListener(capture(pipButtonListener)) }
//        clearAllMocks(answers = false)
//
//        pipButtonListener.captured.onClick(mockk())
//
//        verifySequence {
//            mListener.isPipEnabled
//            utils.createFullScreenDialog(mockActivity)
//            mFullScreenDialog.setOnKeyListener(any())
//            playerView.parent
//            playerView.parent
//            viewGroup.removeView(playerView)
//            utils.createLayoutParams()
//            mFullScreenDialog.addContentView(playerView, any())
//            mFullscreenOverlays.values
//            mockView1.parent
//            viewGroup.removeView(mockView1)
//            utils.createLayoutParams()
//            mFullScreenDialog.addContentView(mockView1, any())
//            mockView1.bringToFront()
//
//            mockView2.parent
//            viewGroup.removeView(mockView2)
//            utils.createLayoutParams()
//            mFullScreenDialog.addContentView(mockView2, any())
//            mockView2.bringToFront()
//
//            mockView3.parent
//            viewGroup.removeView(mockView3)
//            utils.createLayoutParams()
//            mFullScreenDialog.addContentView(mockView3, any())
//            mockView3.bringToFront()
//
//            playerView.findViewById<ImageButton>(R.id.exo_fullscreen)
//            ContextCompat.getDrawable(mockActivity, R.drawable.FullScreenDrawableButtonCollapse)
//            fullScreenButton.setImageDrawable(drawable)
//            mFullScreenDialog.show()
//            utils.createTrackingVideoTypeData()
//            videoData.arcVideo = arcVideo
//            exoPlayer.currentPosition
//            videoData.position = expectedPosition
//            mListener.onTrackingEvent(TrackingType.ON_OPEN_FULL_SCREEN, videoData)
//            trackingHelper.fullscreen()
//            playerView.hideController()
//            exoPlayer.currentPosition
//            mListener.setSavedPosition(testObject.mVideoId, expectedPosition)
//            mListener.startPIP(testObject.video)
//
//        }
//    }

    @Test
    fun `pipButton onClick when pip not enabled, opens pip settings dialog with no exceptions`() {
        every { mListener.isPipEnabled } returns false
        testObject.onPipEnter()

        verifySequence {
            utils.createAlertDialogBuilder(mockActivity)
            mockBuilder.apply {
                setTitle("Picture-in-Picture functionality is disabled")
                setMessage("Would you like to enable Picture-in-Picture?")
                setPositiveButton(android.R.string.yes, any())
                setNegativeButton(android.R.string.cancel, null)
                setCancelable(true)
                setIcon(android.R.drawable.ic_dialog_info)
                show()
            }
        }
        verify(exactly = 0) { mListener.onError(any(), any(), any()) }
    }

    @Test
    fun `pipButton onclick when pip disabled opens pip settings dialog, then on positive click starts new activity`() {
        val mockPackageName = "packageName"
        val intent = mockk<Intent>(relaxed = true)
        val uri = mockk<Uri>()
        every { mockActivity.packageName } returns mockPackageName
        every { mListener.isPipEnabled } returns false
        every { utils.createIntent() } returns intent
        mockkStatic(Uri::class)
        every { Uri.fromParts("package", mockPackageName, null) } returns uri
        testObject.onPipEnter()
        val listener = slot<DialogInterface.OnClickListener>()
        verify { mockBuilder.setPositiveButton(android.R.string.yes, capture(listener)) }

        listener.captured.onClick(mockk(), 0)

        verifyOrder {
            utils.createIntent()
            intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            Uri.fromParts("package", mockPackageName, null)
            intent.data = uri
            mockActivity.startActivity(intent)
        }
    }

    @Test
    fun `pipButton onClick when pip disabled, opens pip settings dialog throws exception and is handled`() {
        every { mListener.isPipEnabled } returns false
        val errorMessage = "pip error"
        val exception = mockk<Exception>()
        every { exception.message } returns errorMessage
        every { utils.createAlertDialogBuilder(mockActivity) } throws exception
        testObject.onPipEnter()

        verify(exactly = 1) {
            utils.createAlertDialogBuilder(mockActivity)
            mListener.onError(
                ArcVideoSDKErrorType.EXOPLAYER_ERROR,
                errorMessage,
                playerState.mVideo
            )
        }
    }

    @Test
    fun `pipButton onClick when pip disabled, with no activity logs error`() {
        every { mListener.isPipEnabled } returns false
        every { arcXPVideoConfig.activity } returns null

        val errorMessage = "Activity Not Set"
        testObject.onPipEnter()

        verify(exactly = 1) {
            mListener.onError(ArcVideoSDKErrorType.EXOPLAYER_ERROR, errorMessage, null)
        }
    }

    //    @Test
//    fun `pipButton onClick when pip is enabled, fullscreen, starts pip`() {
//        val pipButtonListener = slot<View.OnClickListener>()
//        val drawable = mockk<Drawable>()
//        val videoData = mockk<TrackingTypeData.TrackingVideoTypeData>(relaxed = true)
//        val expectedPosition = 324343L
//        val viewGroup = mockk<ViewGroup>(relaxed = true)
//        every { mListener.isPipEnabled } returns true
//        every { exoPlayer.currentPosition } returns expectedPosition
//        every { playerView.parent } returns viewGroup
//        every { playerView.findViewById<ImageButton>(R.id.exo_fullscreen) } returns fullScreenButton
//        every {
//            ContextCompat.getDrawable(
//                mockActivity,
//                R.drawable.FullScreenDrawableButton
//            )
//        } returns drawable
//        every { utils.createTrackingVideoTypeData() } returns videoData
//        testObject.setUpPlayerControlListeners()
//        testObject.setFullscreenUi(true)
//        verify { pipButton.setOnClickListener(capture(pipButtonListener)) }
//        clearAllMocks(answers = false)
//
//        pipButtonListener.captured.onClick(mockk())
//
//        verifySequence {
//            mListener.isPipEnabled
//            playerView.hideController()
//            mListener.setSavedPosition(expectedId, expectedPosition)
//            mListener.startPIP(testObject.video)
//        }
//    }
//
    @Test
    fun `onPipExit enables controller and returns to full screen`() {

        every { arcXPVideoConfig.isDisableControls } returns false
        every { playerState.wasInFullScreenBeforePip } returns true

        testObject.onPipExit()

        verifySequence { playerView.useController = true }
        verify { mFullScreenDialog wasNot called }
    }

    @Test
    fun `onPipExit enables controller and returns to normal screen`() {
        val drawable = mockk<Drawable>()
        val videoData = mockk<TrackingTypeData.TrackingVideoTypeData>(relaxed = true)
        val mVideo = createDefaultVideo()
        val expectedPosition = 1234L
        every { utils.createTrackingVideoTypeData() } returns videoData
        every {
            ContextCompat.getDrawable(
                mockActivity,
                R.drawable.FullScreenDrawableButton
            )
        } returns drawable
        every { playerState.mVideo } returns mVideo
        every { exoPlayer.currentPosition } returns expectedPosition
        every { mListener.isPipEnabled } returns true
        every { mListener.isStickyPlayer } returns true


        testObject.onPipExit()

        verify {
            playerView.useController = true
            playerView.parent
            playerView.parent
            playerViewParent.removeView(playerView)
            mListener.playerFrame.addView(playerView)
            mockFullscreenOverlays.values
            mockView1Parent.removeView(mockView1)
            mListener.playerFrame.addView(mockView1)
            mockView2Parent.removeView(mockView2)
            mListener.playerFrame.addView(mockView2)
            mockView3Parent.removeView(mockView3)
            mListener.playerFrame.addView(mockView3)
            playerView.findViewById<ImageButton>(R.id.exo_fullscreen)
            ContextCompat.getDrawable(mockActivity, R.drawable.FullScreenDrawableButton)
            fullScreenButton.setImageDrawable(drawable)
            mListener.isStickyPlayer
            playerView.hideController()
            playerView.requestLayout()
            mFullScreenDialog.dismiss()
            utils.createTrackingVideoTypeData()
            videoData.arcVideo = mVideo
            exoPlayer.currentPosition
            videoData.position = expectedPosition
            mListener.onTrackingEvent(TrackingType.ON_CLOSE_FULL_SCREEN, videoData)
            trackingHelper.normalScreen()
        }
    }

    @Test
    fun `onPipExit when controls fully disabled does not re enable controls`() {
        every { arcXPVideoConfig.isDisableControls } returns true


        testObject.onPipExit()

        verify(exactly = 0) {
            playerView.useController = any()
        }
    }


    @Test
    fun `playVideo with isShowSeekButton false sets show ff and rw buttons false`() {

        every { arcXPVideoConfig.isShowSeekButton } returns false

        testObject.setUpPlayerControlListeners()

        verify(exactly = 1) {
            playerView.setShowFastForwardButton(false)
            playerView.setShowRewindButton(false)
        }
    }

//    @Test
//    fun `playVideo with isShowSeekButton true, but is live sets show ff and rw buttons false`() {
//
//        every { arcXPVideoConfig.isShowSeekButton } returns true
//        every { playerState.mIsLive } returns true
//
//        testObject.setUpPlayerControlListeners()
//
//        verify(exactly = 1) {
//            playerView.setShowFastForwardButton(false)
//            playerView.setShowRewindButton(false)
//        }
//    }

    @Test
    fun `playVideo with isShowSeekButton true, live false sets show ff and rw buttons true`() {

        every { arcXPVideoConfig.isShowSeekButton } returns true
        every { playerState.mIsLive } returns false

        testObject.setUpPlayerControlListeners()

        verify(exactly = 1) {
            playerView.setShowFastForwardButton(true)
            playerView.setShowRewindButton(true)
        }
    }


    @Test
    fun `playVideo with mIsLive true, hides view components`() {
        every { playerState.mIsLive } returns true
        testObject.setUpPlayerControlListeners()

        verify(exactly = 1) {
            exoPosition.visibility = GONE
            exoDuration.visibility = GONE
            exoProgress.visibility = GONE
            playerView.setShowFastForwardButton(false)
            playerView.setShowRewindButton(false)
        }
    }

    @Test
    fun `playVideo with mIsLive false, shows view components`() {
        every { playerState.mIsLive } returns false
        every { arcXPVideoConfig.isShowProgressBar } returns true
        every { arcXPVideoConfig.isShowCountDown } returns true
        testObject.setUpPlayerControlListeners()

        assertNotEquals(exoPosition.visibility, GONE)
        assertNotEquals(exoDuration.visibility, GONE)
        assertNotEquals(exoProgress.visibility, GONE)

        verify(exactly = 1) {
            exoTimeBarLayout.visibility = VISIBLE
            exoProgress.visibility = VISIBLE
            exoDuration.visibility = VISIBLE
        }
    }

    @Test
    fun `playVideo with isShowCountDown false, sets exoDuration to gone`() {

        every { arcXPVideoConfig.isShowCountDown } returns false
        every { arcXPVideoConfig.isShowProgressBar } returns true

        testObject.setUpPlayerControlListeners()

        verify(exactly = 1) {
            exoDuration.visibility = GONE
        }
    }

    @Test
    fun `playVideo with isShowCountDown true, sets exoDuration to visible`() {

        every { arcXPVideoConfig.isShowCountDown } returns true
        every { arcXPVideoConfig.isShowProgressBar } returns true

        testObject.setUpPlayerControlListeners()

        verify(exactly = 1) {
            exoDuration.visibility = VISIBLE
        }
    }

    @Test
    fun `playVideo with isShowProgressBar false sets exoTimeBarLayout to gone`() {
        every { arcXPVideoConfig.isShowProgressBar } returns false

        testObject.setUpPlayerControlListeners()

        verify(exactly = 1) {
            exoTimeBarLayout.visibility = GONE
        }
    }

    @Test
    fun `playVideo if shareUrl is empty and isKeepControlsSpaceOnHide true, sets share button to invisible`() {
        every { arcXPVideoConfig.isKeepControlsSpaceOnHide } returns true

        testObject.setUpPlayerControlListeners()

        verify(exactly = 1) {
            shareButton.visibility = INVISIBLE
        }
    }

    @Test
    fun `playVideo if shareUrl is empty and isKeepControlsSpaceOnHide false, sets share button to gone`() {
        every { arcXPVideoConfig.isKeepControlsSpaceOnHide } returns false

        testObject.setUpPlayerControlListeners()

        verify(exactly = 1) {
            shareButton.visibility = GONE
        }
    }


    @Test
    fun `playVideo if arcXPVideoConfigShowTimeMs is not null, sets value on player view`() {
        val timeout = 38762
        every { arcXPVideoConfig.controlsShowTimeoutMs } returns timeout

        testObject.setUpPlayerControlListeners()

        verify(exactly = 1) {
            playerView.controllerShowTimeoutMs = timeout
        }
    }

    @Test
    fun `playVideo if arcXPVideoConfig isHideControlsWithTouch true, sets value on player view`() {
        every { arcXPVideoConfig.isHideControlsWithTouch } returns true

        testObject.setUpPlayerControlListeners()

        verify(exactly = 1) {
            playerView.controllerHideOnTouch = true
        }
    }

    @Test
    fun `playVideo setUpPlayerControlListeners throws exception, is handled by listener`() {
        val errorMessage = "i am error"
        val exception = Exception(
            errorMessage
        )
        every { playerView.findViewById<ImageButton>(R.id.exo_fullscreen) } throws exception

        testObject.setUpPlayerControlListeners()

        verify(exactly = 1) {
            mListener.onError(ArcVideoSDKErrorType.EXOPLAYER_ERROR, errorMessage, exception)
        }
    }

    @Test
    fun `playVideo when enable close caption but not available, isKeepControlsSpaceOnHide true, makes cc Button invisible`() {
        every { arcXPVideoConfig.enableClosedCaption() } returns true
        every { captionsManager.isClosedCaptionAvailable() } returns false
        every { playerState.config.isKeepControlsSpaceOnHide } returns true

        testObject.setUpPlayerControlListeners()

        verify(exactly = 1) { ccButton.visibility = INVISIBLE }
    }

    @Test
    fun `playVideo when enable close caption but not available, isKeepControlsSpaceOnHide false, makes cc Button gone`() {
        every { arcXPVideoConfig.enableClosedCaption() } returns true
        every { captionsManager.isClosedCaptionAvailable() } returns false
        every { arcXPVideoConfig.isKeepControlsSpaceOnHide } returns false
        val arcVideo = createDefaultVideo(shouldPlayAds = false)
        every { playerState.mVideo } returns arcVideo

        testObject.setUpPlayerControlListeners()

        verify(exactly = 1) {
            ccButton.visibility = GONE
        }
    }

    @Test
    fun `playVideo when back button disabled, changes back button visibility to gone`() {
        every { arcXPVideoConfig.showBackButton } returns false

        testObject.setUpPlayerControlListeners()

        verify(exactly = 1) {
            backButton.visibility = GONE
        }
    }

    @Test
    fun `toggleFullScreen Dialog onKeyListener volume ignores volume and mute`() {
        val onKeyListener = slot<DialogInterface.OnKeyListener>()
        testObject.toggleFullScreenDialog(false)
        verify(exactly = 1) {
            mFullScreenDialog.setOnKeyListener(capture(onKeyListener))
        }
        assertFalse(onKeyListener.captured.onKey(mockk(), KeyEvent.KEYCODE_VOLUME_DOWN, mockk()))
        assertFalse(onKeyListener.captured.onKey(mockk(), KeyEvent.KEYCODE_VOLUME_UP, mockk()))
        assertFalse(onKeyListener.captured.onKey(mockk(), KeyEvent.KEYCODE_VOLUME_MUTE, mockk()))
    }

    @Test
    fun `toggleFullScreen Dialog onKeyListener returns true for anything but key event up`() {
        val onKeyListener = slot<DialogInterface.OnKeyListener>()
        testObject.toggleFullScreenDialog(false)
        verify(exactly = 1) {
            mFullScreenDialog.setOnKeyListener(capture(onKeyListener))
        }
        assertTrue(onKeyListener.captured.onKey(mockk(), KeyEvent.KEYCODE_0, mockk {
            every { action } returns KeyEvent.ACTION_DOWN
        }))
    }

    @Test
    fun `toggleFullScreen Dialog onKeyListener keycode back returns false and calls listener if pip not enabled`() {
        val onKeyListener = slot<DialogInterface.OnKeyListener>()
        val arcKeyListener = mockk<ArcKeyListener>(relaxed = true)
        every { playerState.mArcKeyListener } returns arcKeyListener
        every { mListener.isPipEnabled } returns false
        testObject.toggleFullScreenDialog(false)
        verify(exactly = 1) {
            mFullScreenDialog.setOnKeyListener(capture(onKeyListener))
        }
        assertFalse(onKeyListener.captured.onKey(mockk(), KeyEvent.KEYCODE_BACK, mockk {
            every { action } returns ACTION_UP
        }))
        verify(exactly = 1) {
            arcKeyListener.onBackPressed()
        }
    }

    @Test
    fun `toggleFullScreen Dialog onKeyListener keycode back returns false and calls enters pip when enabled`() {
        val onKeyListener = slot<DialogInterface.OnKeyListener>()
        val arcKeyListener = mockk<ArcKeyListener>(relaxed = true)
        every { playerState.mArcKeyListener } returns arcKeyListener
        every { mListener.isPipEnabled } returns true
        testObject = spyk(testObject)
        testObject.toggleFullScreenDialog(false)
        verify(exactly = 1) {
            mFullScreenDialog.setOnKeyListener(capture(onKeyListener))
        }

        assertFalse(onKeyListener.captured.onKey(mockk(), KeyEvent.KEYCODE_BACK, mockk {
            every { action } returns ACTION_UP
        }))
        verify(exactly = 1) {
            testObject.onPipEnter()
        }

    }
    @Test
    fun `toggleFullScreen Dialog onKeyListener keycode other first ad incomplete, enable ads true returns false and null listener`() {
        val onKeyListener = slot<DialogInterface.OnKeyListener>()
//        val arcKeyListener = mockk<ArcKeyListener>(relaxed = true)
        every { playerState.mArcKeyListener } returns null
        every { playerState.firstAdCompleted } returns false
        every { arcXPVideoConfig.isEnableAds } returns true
        every { mListener.isPipEnabled } returns true
        testObject.toggleFullScreenDialog(false)
        verify(exactly = 1) {
            mFullScreenDialog.setOnKeyListener(capture(onKeyListener))
        }

        val keyCode = KeyEvent.KEYCODE_0
        val keyEvent = mockk<KeyEvent> {
            every { action } returns ACTION_UP
        }
        assertFalse(onKeyListener.captured.onKey(mockk(), keyCode,keyEvent))
        verify(exactly = 0) { playerView.showController()}
    }
    @Test
    fun `toggleFullScreen Dialog onKeyListener keycode other first ad complete, enable ads true returns false and calls listener`() {
        val onKeyListener = slot<DialogInterface.OnKeyListener>()
        val arcKeyListener = mockk<ArcKeyListener>(relaxed = true)
        every { playerState.mArcKeyListener } returns arcKeyListener
        every { playerState.firstAdCompleted } returns false
        every { arcXPVideoConfig.isEnableAds } returns true
        every { mListener.isPipEnabled } returns true
        testObject.toggleFullScreenDialog(false)
        verify(exactly = 1) {
            mFullScreenDialog.setOnKeyListener(capture(onKeyListener))
        }

        val keyCode = KeyEvent.KEYCODE_0
        val keyEvent = mockk<KeyEvent> {
            every { action } returns ACTION_UP
        }
        assertFalse(onKeyListener.captured.onKey(mockk(), keyCode,keyEvent))
        verify(exactly = 1) {
            arcKeyListener.onKey(keyCode, keyEvent)
        }
        verify(exactly = 0) { playerView.showController()}
    }
    @Test
    fun `toggleFullScreen Dialog onKeyListener shows controller when not fully visible`() {
        val onKeyListener = slot<DialogInterface.OnKeyListener>()
        val arcKeyListener = mockk<ArcKeyListener>(relaxed = true)
        every { playerView.isControllerFullyVisible } returns false
        every { playerState.mArcKeyListener } returns arcKeyListener
        every { playerState.firstAdCompleted } returns false
        every { arcXPVideoConfig.isEnableAds } returns false
        every { mListener.isPipEnabled } returns true
        testObject.toggleFullScreenDialog(false)
        verify(exactly = 1) {
            mFullScreenDialog.setOnKeyListener(capture(onKeyListener))
        }

        val keyCode = KeyEvent.KEYCODE_0
        val keyEvent = mockk<KeyEvent> {
            every { action } returns ACTION_UP
        }
        assertFalse(onKeyListener.captured.onKey(mockk(), keyCode,keyEvent))
        verify(exactly = 1) {
            arcKeyListener.onKey(keyCode, keyEvent)
            playerView.showController()
        }
//        verify(exactly = 0) { playerView.showController()}
    }
}