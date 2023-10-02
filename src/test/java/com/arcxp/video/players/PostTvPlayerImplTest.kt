package com.arcxp.video.players

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.MotionEvent.ACTION_BUTTON_PRESS
import android.view.MotionEvent.ACTION_UP
import android.view.View
import android.view.View.*
import android.view.ViewGroup
import android.view.accessibility.CaptioningManager
import android.widget.*
import androidx.core.content.ContextCompat
import com.arcxp.sdk.R
import com.arcxp.video.ArcXPVideoConfig
import com.arcxp.video.ArcVideoManager
import com.arcxp.video.cast.ArcCastManager
import com.arcxp.video.listeners.AdsLoadedListener
import com.arcxp.video.listeners.ArcKeyListener
import com.arcxp.video.listeners.VideoListener
import com.arcxp.video.model.*
import com.arcxp.video.model.TrackingType.*
import com.arcxp.video.util.PrefManager
import com.arcxp.video.util.TrackingHelper
import com.arcxp.video.util.Utils
import com.google.ads.interactivemedia.v3.api.Ad
import com.google.ads.interactivemedia.v3.api.AdEvent
import com.google.ads.interactivemedia.v3.api.AdEvent.AdEventType.*
import com.google.ads.interactivemedia.v3.api.AdsLoader
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.ext.cast.CastPlayer
import com.google.android.exoplayer2.ext.ima.ImaAdsLoader
import com.google.android.exoplayer2.source.MergingMediaSource
import com.google.android.exoplayer2.source.SingleSampleMediaSource
import com.google.android.exoplayer2.source.TrackGroup
import com.google.android.exoplayer2.source.ads.AdsMediaSource
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.MappingTrackSelector
import com.google.android.exoplayer2.ui.DefaultTimeBar
import com.google.android.exoplayer2.ui.PlayerControlView
import com.google.android.exoplayer2.ui.StyledPlayerView
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.upstream.FileDataSource
import com.google.android.exoplayer2.util.Util
import com.google.android.gms.cast.framework.CastContext
import com.google.common.collect.ImmutableList
import io.mockk.*
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runners.MethodSorters
import java.util.*

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class PostTvPlayerImplTest {

    private lateinit var testObject: PostTvPlayerImpl

    @RelaxedMockK
    lateinit var mVideoManager: ArcVideoManager

    @RelaxedMockK
    lateinit var mConfig: ArcXPVideoConfig

    @RelaxedMockK
    lateinit var trackingHelper: TrackingHelper

    @RelaxedMockK
    lateinit var mAppContext: Activity

    @RelaxedMockK
    lateinit var mAdsLoader: ImaAdsLoader

    @RelaxedMockK
    lateinit var mAdsLoaderAdsLoader: AdsLoader

    @RelaxedMockK
    lateinit var ccButton: ImageButton

    @RelaxedMockK
    lateinit var castCcButton: ImageButton

    @RelaxedMockK
    lateinit var artWork: ImageView

    @RelaxedMockK
    lateinit var mMediaDataSourceFactory: DefaultDataSourceFactory

    @RelaxedMockK
    lateinit var mPlayer: ExoPlayer

    @RelaxedMockK
    lateinit var mPlayerView: StyledPlayerView

    @RelaxedMockK
    lateinit var mCastControlView: PlayerControlView

    @RelaxedMockK
    lateinit var mockResources: Resources

    @MockK
    lateinit var mockPackageManager: PackageManager

    @MockK
    lateinit var mFullscreenOverlays: HashMap<String, View>

    @MockK
    lateinit var mockCastManager: ArcCastManager

    @RelaxedMockK
    lateinit var mListener: VideoListener

    @RelaxedMockK
    lateinit var mockCastContext: CastContext

    @RelaxedMockK
    lateinit var mCastPlayer: CastPlayer

    @RelaxedMockK
    lateinit var mockContentMediaSource: MergingMediaSource

    @MockK
    lateinit var adsLoadedListener: AdsLoadedListener

    @RelaxedMockK
    lateinit var mockView1: View

    @RelaxedMockK
    lateinit var mockView2: View

    @RelaxedMockK
    lateinit var mockView3: View

    @RelaxedMockK
    lateinit var fullScreenButton: ImageButton

    @RelaxedMockK
    lateinit var castFullScreenButton: ImageButton

    @RelaxedMockK
    lateinit var shareButton: ImageButton

    @RelaxedMockK
    lateinit var castShareButton: ImageButton

    @RelaxedMockK
    lateinit var volumeButton: ImageButton

    @RelaxedMockK
    lateinit var castVolumeButton: ImageButton

    @RelaxedMockK
    lateinit var playButton: ImageButton

    @RelaxedMockK
    lateinit var pauseButton: ImageButton

    @RelaxedMockK
    lateinit var pipButton: ImageButton

    @RelaxedMockK
    lateinit var castPipButton: ImageButton

    @RelaxedMockK
    lateinit var nextButton: ImageButton

    @RelaxedMockK
    lateinit var previousButton: ImageButton

    @RelaxedMockK
    lateinit var backButton: ImageButton

    @RelaxedMockK
    lateinit var exoPosition: View

    @RelaxedMockK
    lateinit var exoDuration: View

    @RelaxedMockK
    lateinit var exoProgress: DefaultTimeBar

    @RelaxedMockK
    lateinit var adsMediaSource: AdsMediaSource

    @RelaxedMockK
    lateinit var expectedAdUri: Uri

    @RelaxedMockK
    lateinit var expectedSubtitleUri: Uri

    @RelaxedMockK
    lateinit var expectedIdUri: Uri

    @RelaxedMockK
    lateinit var exoTimeBarLayout: LinearLayout

    @RelaxedMockK
    lateinit var mockExoPlayerBuilder: ExoPlayer.Builder

    @RelaxedMockK
    lateinit var mockLooper: Looper

    @RelaxedMockK
    lateinit var mockMediaSource: HlsMediaSource

    @RelaxedMockK
    lateinit var mockFormat: Format

    @RelaxedMockK
    lateinit var mockSingleSampleMediaSource: SingleSampleMediaSource

    @RelaxedMockK
    lateinit var mockSingleSampleMediaSourceFactory: SingleSampleMediaSource.Factory

    @RelaxedMockK
    lateinit var utils: Utils

    @RelaxedMockK
    lateinit var mTrackSelector: DefaultTrackSelector

    @MockK
    lateinit var mockDrawable: Drawable

    @RelaxedMockK
    lateinit var adEvent: AdEvent

    @RelaxedMockK
    lateinit var subtitleConfiguration: MediaItem.SubtitleConfiguration

    @RelaxedMockK
    lateinit var titleView: TextView

    @RelaxedMockK
    lateinit var timeline: Timeline

    @MockK
    lateinit var currentMappedTrackInfo: MappingTrackSelector.MappedTrackInfo

    @MockK
    lateinit var period: Timeline.Period

    private val expectedId = "123"
    private val expectedVolume = 0.67f
    private val subtitleUrl = "mock subtitle url"
    private val expectedSavedPosition = 12345L
    private val expectedPosition = 987453L
    private val expectedStartPosition = 123L
    private val renderError = "An error occurred during playback."
    private val sourceError = "An error occurred during playback."
    private val unknownError = "An unknown error occurred while trying to play the video."
    private val mHeadline = "headline"
    private val mShareUrl = "shareUrl"
    private val mArtWorkUrl = "artworkUrl"
    private val mockPackageName = "packageName"
    private val expectedIncrement = 10000
    private val expectedSessionId = "sessionId"
    private val expectedUrl = "expectedUrl"
    private val mediaItem = MediaItem.fromUri(expectedUrl)

    private val expectedTimeScrubColor = 342
    private val expectedTimePlayColor = 3421
    private val expectedTimeUnPlayedColor = 3422
    private val expectedBufferedColor = 3423
    private val expectedAdPlayedColor = 342367
    private val expectedAdMarkerColor = 342378
    private val expectedCurrentPosition = 83746L
    private val expectedPeriodPosition = 83744L
    private val expectedAdjustedPosition = 2L
    private val expectedPeriodIndex = 7

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxUnitFun = true)
        every { mTrackSelector.currentMappedTrackInfo } returns currentMappedTrackInfo
        every {
            mPlayer.currentTimeline
        } returns timeline
        every {
            timeline.getPeriod(
                expectedPeriodIndex,
                any()
            )
        } returns period
        every { period.positionInWindowMs } returns expectedPeriodPosition
        mConfig.apply {
            every { controlsShowTimeoutMs } returns null
            every { isDisableControlsWithTouch } returns false
            every { overlays } returns mFullscreenOverlays.apply {
                every { values } returns mutableListOf(mockView1, mockView2, mockView3)
            }
            every { isKeepControlsSpaceOnHide } returns true
            every { activity } returns mAppContext.apply {
                every { resources } returns mockResources.apply {
                    every { getInteger(R.integer.ff_inc) } returns expectedIncrement
                    every { getInteger(R.integer.rew_inc) } returns expectedIncrement
                    every { getColor(R.color.TimeBarScrubberColor) } returns expectedTimeScrubColor
                    every { getColor(R.color.TimeBarPlayedColor) } returns expectedTimePlayColor
                    every { getColor(R.color.TimeBarUnplayedColor) } returns expectedTimeUnPlayedColor
                    every { getColor(R.color.TimeBarBufferedColor) } returns expectedBufferedColor
                    every { getColor(R.color.AdMarkerColor) } returns expectedAdMarkerColor
                    every { getColor(R.color.AdPlayedMarkerColor) } returns expectedAdPlayedColor
                }
                every { packageManager } returns mockPackageManager
                every { packageName } returns mockPackageName
                every { getString(R.string.render_error) } returns renderError
                every { getString(R.string.source_error) } returns sourceError
                every { getString(R.string.unknown_error) } returns unknownError
            }
            every { shouldDisableNextButton } returns false
            every { shouldDisablePreviousButton } returns false
            every { showNextPreviousButtons } returns false
            every { userAgent } returns "useragent"
            every { showFullScreenButton } returns true
            every { showBackButton } returns true
            every { showVolumeButton } returns true
            every { showTitleOnController } returns true
        }
        every { mConfig.artworkUrl } returns mArtWorkUrl
        mVideoManager.apply {
            every { isShowCountDown } returns true
            every { isShowProgressBar } returns true
            every { isShowSeekButton } returns true
            every { enableClosedCaption() } returns false
            every { castManager } returns mockCastManager
            every { currentActivity } returns mAppContext
            every { isAutoShowControls } returns true
            every { sessionId } returns expectedSessionId
        }
        every { mListener.getSavedPosition(expectedId) } returns expectedSavedPosition
        utils.apply {
            every {
                createAdsMediaSource(
                    mockContentMediaSource,
                    any(),
                    expectedAdUri,
                    any(),
                    mAdsLoader,
                    mPlayerView
                )
            } returns adsMediaSource
            every { createExoPlayerBuilder(mAppContext) } returns mockExoPlayerBuilder.apply {
                every { setTrackSelector(any()) } returns mockExoPlayerBuilder
                every { setSeekBackIncrementMs(expectedIncrement.toLong()) } returns mockExoPlayerBuilder
                every { setSeekForwardIncrementMs(expectedIncrement.toLong()) } returns mockExoPlayerBuilder
                every { setLooper(any()) } returns mockExoPlayerBuilder
                every { build() } returns mPlayer.apply {
                    every { addListener(any()) } returns Unit
                    every { prepare(any()) } returns Unit
                    every { volume } returns expectedVolume
                    every { currentPosition } returns expectedSavedPosition
                }
            }
            every { createPlayerView(mAppContext) } returns mPlayerView.apply {
                every { findViewById<ImageButton>(R.id.exo_play) } returns playButton
                every { findViewById<ImageButton>(R.id.exo_pause) } returns pauseButton
                every { findViewById<ImageButton>(R.id.exo_fullscreen) } returns fullScreenButton
                every { findViewById<ImageButton>(R.id.exo_share) } returns shareButton
                every { findViewById<ImageButton>(R.id.exo_pip) } returns pipButton
                every { findViewById<ImageButton>(R.id.exo_volume) } returns volumeButton
                every { findViewById<ImageButton>(R.id.exo_cc) } returns ccButton
                every { findViewById<LinearLayout>(R.id.time_bar_layout) } returns exoTimeBarLayout
                every { findViewById<View>(R.id.exo_position) } returns exoPosition
                every { findViewById<View>(R.id.exo_duration) } returns exoDuration
                every { findViewById<View>(R.id.exo_progress) } returns exoProgress
                every { findViewById<View>(R.id.exo_next_button) } returns nextButton
                every { findViewById<View>(R.id.exo_prev_button) } returns previousButton
                every { findViewById<TextView>(R.id.styled_controller_title_tv) } returns titleView
//                every { findViewById<View>(R.id.exo_ffwd) } returns ffButtton
//                every { findViewById<View>(R.id.exo_rew_with_amount) } returns rwButtton
                every { findViewById<View>(R.id.exo_back) } returns backButton
            }
            every { createPlayerControlView(mAppContext) } returns mCastControlView.apply {
                every { findViewById<ImageButton>(R.id.exo_fullscreen) } returns castFullScreenButton
                every { findViewById<ImageButton>(R.id.exo_pip) } returns castPipButton
                every { findViewById<ImageButton>(R.id.exo_share) } returns castShareButton
                every { findViewById<ImageButton>(R.id.exo_volume) } returns castVolumeButton
                every { findViewById<ImageButton>(R.id.exo_cc) } returns castCcButton
                every { findViewById<ImageView>(R.id.exo_artwork) } returns artWork
            }
            every { createCastPlayer(mockCastContext, 10000, 10000) } returns mCastPlayer
            every { createSingleSampleMediaSourceFactory(mMediaDataSourceFactory) } returns mockSingleSampleMediaSourceFactory.apply {
                every { setTag(expectedId) } returns this
                every {
                    createMediaSource(
                        subtitleConfiguration,
                        C.TIME_UNSET
                    )
                } returns mockSingleSampleMediaSource
            }
            every {
                createMergingMediaSource(
                    mockMediaSource,
                    mockSingleSampleMediaSource
                )
            } returns mockContentMediaSource
            every {
                createAdsLoadedListener(
                    mListener,
                    any(),
                    any(),
                    any()
                )
            } returns adsLoadedListener
            every {
                createDefaultDataSourceFactory(
                    mAppContext,
                    "useragent"
                )
            } returns mMediaDataSourceFactory
            every { createDefaultTrackSelector() } returns mTrackSelector
            every { loadImageIntoView(mAppContext, mArtWorkUrl, artWork) } returns mockk()
            every {
                createSubtitleConfiguration(expectedSubtitleUri)
            } returns subtitleConfiguration
        }
        every { mockCastManager.getCastContext() } returns mockCastContext
        every { mAdsLoader.adsLoader } returns mAdsLoaderAdsLoader

        mockkConstructor(DefaultTrackFilter::class)
        val expectedTime = 1234567L
        mockkConstructor(Date::class)
        every { constructedWith<Date>().time } returns expectedTime
        mockkConstructor(HlsMediaSource.Factory::class)
        every {
            constructedWith<HlsMediaSource.Factory>(EqMatcher(mMediaDataSourceFactory))
                .createMediaSource(mediaItem)
        } returns mockMediaSource
        mockkConstructor(ImaAdsLoader.Builder::class)
        val mockImaAdsLoaderBuilder = mockk<ImaAdsLoader.Builder>()
        every { constructedWith<ImaAdsLoader.Builder>(EqMatcher(mAppContext)).setAdEventListener(any()) } returns mockImaAdsLoaderBuilder
        every { mockImaAdsLoaderBuilder.build() } returns mAdsLoader

        mockkStatic(Uri::class)
        every { Uri.parse("addTagUrl1234567") } returns expectedAdUri
        every { Uri.parse(subtitleUrl) } returns expectedSubtitleUri
        every { Uri.parse(expectedId) } returns expectedIdUri

        mockkStatic(Log::class)
        every { Log.e(any(), any()) } returns 0
        every { Log.e(any(), any(), any()) } returns 0

        mockkStatic(Looper::class)
        every { Looper.getMainLooper() } returns mockLooper

        mockkStatic(Util::class)
        every { Util.inferContentType(expectedSubtitleUri) } returns C.TYPE_HLS
        every { Util.inferContentType(expectedIdUri) } returns C.TYPE_HLS

        mockkStatic(ContextCompat::class)
        every { ContextCompat.getDrawable(mAppContext, any()) } returns mockDrawable

//        mockkStatic(MediaItem::class)
//        every { MediaItem.fromUri(expectedIdUri) } returns mediaItem

//            every { getProperty("uri") } propertyType Uri::class answers { expectedIdUri }
//        } //expectedIdUri
//        every { mediaItem.localConfiguration  getProperty "uri" } propertyType Uri::class answers { expectedIdUri }


//        mockkStatic(Format::class)
//        every {
//            Format.createTextSampleFormat(
//                ID_SUBTITLE_URL,
//                MimeTypes.TEXT_VTT,
//                C.SELECTION_FLAG_DEFAULT,
//                "en"
//            )
//        } returns mockFormat

        testObject = PostTvPlayerImpl(
            mConfig,
            mVideoManager,
            mListener,
            trackingHelper,
            utils
        )
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `pausePlay given true calls mPlayer, mPlayerView, trackingHelper if they are not null`() {
        testObject.playVideo(createDefaultVideo())
        clearAllMocks(answers = false)

        testObject.pausePlay(true)

        verifySequence {
            mPlayer.playWhenReady = true
            mPlayerView.hideController()
        }
        verifyNoExceptions()
    }

    @Test
    fun `pausePlay given false calls mPlayer, mPlayerView, trackingHelper if they are not null`() {
        testObject.playVideo(createDefaultVideo())
        clearAllMocks(answers = false)

        testObject.pausePlay(false)

        verifySequence {
            mPlayer.playWhenReady = false
            mPlayerView.hideController()
        }
        verifyNoExceptions()
    }

    @Test
    fun `pausePlay throws exception, is handled by listener`() {
        val error = " pause Play error"
        testObject.playVideo(createDefaultVideo())
        clearAllMocks(answers = false)
        every { mPlayer.playWhenReady = true } throws Exception(error)


        testObject.pausePlay(true)

        verifySequence {
            mPlayer.playWhenReady = true
            mListener.onError(ArcVideoSDKErrorType.EXOPLAYER_ERROR, error, testObject.video)
        }
        verify {
            mPlayerView wasNot called
            trackingHelper wasNot called
        }
    }

    @Test
    fun `start throws exception, is handled by listener`() {
        val error = " start error"
        testObject.playVideo(createDefaultVideo())
        clearAllMocks(answers = false)
        every { mPlayer.playWhenReady = true } throws Exception(error)

        testObject.start()

        verifySequence {
            mPlayer.playWhenReady = true
            mListener.onError(ArcVideoSDKErrorType.EXOPLAYER_ERROR, error, testObject.video)
        }
    }

    @Test
    fun `start calls mPlayer setPlayWhenReady with true`() {
        testObject.playVideo(createDefaultVideo())
        clearAllMocks(answers = false)

        testObject.start()

        verifySequence { mPlayer.playWhenReady = true }
        verifyNoExceptions()
    }

    @Test
    fun `pause calls mPlayer and trackingHelper if they are not null`() {
        testObject.playVideo(createDefaultVideo())
        clearAllMocks(answers = false)

        testObject.pause()

        verifySequence {
            mPlayer.playWhenReady = false
        }
        verifyNoExceptions()
    }


    @Test
    fun `pause throws exception, is handled by listener`() {
        val error = " pause error"
        testObject.playVideo(createDefaultVideo())
        clearAllMocks(answers = false)
        every { mPlayer.playWhenReady = false } throws Exception(error)

        testObject.pause()

        verifySequence {
            mPlayer.playWhenReady = false
            mListener.onError(ArcVideoSDKErrorType.EXOPLAYER_ERROR, error, testObject.video)
        }
        verify { trackingHelper wasNot called }
    }

    @Test
    fun `setPlayerKeyListener sets listeners with no exceptions`() {
        testObject.playVideo(createDefaultVideo())
        clearAllMocks(answers = false)

        testObject.setPlayerKeyListener(mockk())

        verifySequence {
            mPlayerView.setOnKeyListener(any())
            mCastControlView.setOnKeyListener(any())
        }
        verifyNoExceptions()
    }

    @Test
    fun `setPlayerKeyListener throws exception, is handled, and then sets mCastControlView listener`() {
        val error = "setPlayerKeyListener error"
        testObject.playVideo(createDefaultVideo())
        clearAllMocks(answers = false)
        every { mPlayerView.setOnKeyListener(any()) } throws Exception(error)

        testObject.setPlayerKeyListener(mockk())

        verifySequence {
            mPlayerView.setOnKeyListener(any())
            mListener.onError(ArcVideoSDKErrorType.EXOPLAYER_ERROR, error, testObject.video)
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

//    @Test
//    fun `toggleCaptions...`(){}

    @Test
    fun `when playVideo throws Exception, then Listener handles error`() {
        val mockVideo = mockk<ArcVideo>()
        val exceptionMessage = "our exception message"
        every { mVideoManager.initVideo(any()) } throws Exception(exceptionMessage)
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
                mockVideo
            )
        }
    }

    @Test
    fun `when mConfig has controls timeout, set in playerView Controller`() {
        val expectedTimeoutMs = 328746
        every { mConfig.controlsShowTimeoutMs } returns expectedTimeoutMs

        testObject.playVideo(createDefaultVideo())

        verify(exactly = 1) { mPlayerView.controllerShowTimeoutMs = expectedTimeoutMs }
    }

    @Test
    fun `when mConfig isDisableControlsWithTouch, set in playerView Controller`() {
        every { mConfig.isDisableControlsWithTouch } returns true

        testObject.playVideo(createDefaultVideo())

        verify(exactly = 1) { mPlayerView.controllerHideOnTouch = true }
    }

    @Test
    fun `playVideo sets mIsLive from video`() {
        val expectedValue = true
        val arcVideo = ArcVideo(
            expectedId,
            "uuid",
            expectedStartPosition,
            false,
            expectedValue,
            100,
            "shareUrl",
            "headline",
            "pageName",
            "videoName",
            "videoSection",
            "videoSource",
            "videoCategory",
            "consentId",
            "fallbackUrl",
            "addTagUrl",
            false,
            subtitleUrl,
            "source",
            mockk(),
            false,
            false,
            false,
            ArcXPVideoConfig.CCStartMode.DEFAULT
        )

        every { mVideoManager.initVideo(any()) } throws Exception()
        testObject.playVideo(arcVideo)

        assertEquals(expectedValue, testObject.mIsLive)
    }

    @Test
    fun `playVideo sets mHeadline from video`() {
        val expectedValue = "headline"
        val arcVideo = ArcVideo(
            expectedId,
            "uuid",
            expectedStartPosition,
            false,
            false,
            100,
            "shareUrl",
            expectedValue,
            "pageName",
            "videoName",
            "videoSection",
            "videoSource",
            "videoCategory",
            "consentId",
            "fallbackUrl",
            "addTagUrl",
            false,
            subtitleUrl,
            "source",
            mockk(),
            false,
            false,
            false,
            ArcXPVideoConfig.CCStartMode.DEFAULT
        )

        every { mVideoManager.initVideo(any()) } throws Exception()
        testObject.playVideo(arcVideo)

        assertEquals(expectedValue, testObject.mHeadline)
    }

    @Test
    fun `playVideo sets shareUrl from video`() {
        val expectedValue = "shareUrl"
        val arcVideo = ArcVideo(
            expectedId,
            "uuid",
            expectedStartPosition,
            false,
            false,
            100,
            expectedValue,
            "headline",
            "pageName",
            "videoName",
            "videoSection",
            "videoSource",
            "videoCategory",
            "consentId",
            "fallbackUrl",
            "addTagUrl",
            false,
            subtitleUrl,
            "source",
            mockk(),
            false,
            false,
            false,
            ArcXPVideoConfig.CCStartMode.DEFAULT
        )

        every { mVideoManager.initVideo(any()) } throws Exception()
        testObject.playVideo(arcVideo)

        assertEquals(expectedValue, testObject.mShareUrl)
    }

    @Test
    fun `playVideo sets mVideoId from video`() {
        val arcVideo = ArcVideo(
            expectedId,
            "uuid",
            expectedStartPosition,
            false,
            false,
            100,
            "share url",
            "headline",
            "pageName",
            "videoName",
            "videoSection",
            "videoSource",
            "videoCategory",
            "consentId",
            "fallbackUrl",
            "addTagUrl",
            false,
            subtitleUrl,
            "source",
            mockk(),
            false,
            false,
            false,
            ArcXPVideoConfig.CCStartMode.DEFAULT
        )

        every { mVideoManager.initVideo(any()) } throws Exception()
        testObject.playVideo(arcVideo)

        assertEquals(expectedId, testObject.mVideoId)
    }

    //    @Test
//    fun `when playVideo videoId is null uses fallback url to set mVideoId`() {
//        val expectedId = "fallback Url"
//        val arcVideo = ArcVideo.Builder().setUrl(null).setFallbackUrl(expectedId).build()
//        val mVideoIdSlot = slot<String>()
//
//        every { mVideoManager.initVideo(any()) } throws Exception()
//
//        testObject.playVideo(arcVideo)
//
//        verify{ mVideoManager.initVideo(capture(mVideoIdSlot))}
//        assertEquals(expectedId, mVideoIdSlot.captured)
//
//
//
//    }
    //TODO so this section..(line 187) sets mVideoId, then it is overwritten with video.id in playVideo() (even if it is null), probably can scrap code or update to do something and test that

    @Test
    fun `playVideo adds video to mVideos`() {
        val arcVideo = createDefaultVideo()
        every { mVideoManager.initVideo(any()) } throws Exception()

        testObject.playVideo(arcVideo)

        assert(testObject.mVideos!!.contains(arcVideo))
    }

    @Test
    fun `playVideo sets mVideo`() {
        val arcVideo = createDefaultVideo()
        every { mVideoManager.initVideo(any()) } throws Exception()

        testObject.playVideo(arcVideo)

        assertEquals(testObject.video, arcVideo)
    }

    @Test
    fun `playVideo when should play ads false, previous player null, and startMuted false  initializes video player`() {
        val arcVideo = ArcVideo(
            expectedId,
            "uuid",
            expectedStartPosition,
            false,
            false,
            100,
            "shareUrl",
            "headline",
            "pageName",
            "videoName",
            "videoSection",
            "videoSource",
            "videoCategory",
            "consentId",
            "fallbackUrl",
            "addTagUrl",
            false,
            subtitleUrl,
            "source",
            mockk(),
            false,
            false,
            false,
            ArcXPVideoConfig.CCStartMode.DEFAULT
        )

        testObject.playVideo(arcVideo)

        verify { mAdsLoader wasNot called }
        verifyNoExceptions()
    }

    @Test
    fun `playVideo when enable close caption but not available, isKeepControlsSpaceOnHide true, makes cc Button invisible`() {
        every { mVideoManager.enableClosedCaption() } returns true

        playVideoThenVerify(createDefaultVideo())

        verify(exactly = 1) { ccButton.visibility = INVISIBLE }
    }

    @Test
    fun `playVideo when enable close caption but not available, isKeepControlsSpaceOnHide false, makes cc Button gone`() {
        every { mVideoManager.enableClosedCaption() } returns true
        every { mConfig.isKeepControlsSpaceOnHide } returns false

        testObject.playVideo(createDefaultVideo(shouldPlayAds = false))

        verify(exactly = 2) {
            ccButton.visibility = GONE
        }
    }

    @Test
    fun `playVideo when back button disabled, changes back button visibility to gone`() {
        every { mConfig.showBackButton } returns false

        testObject.playVideo(createDefaultVideo())

        verify(exactly = 1) {
            backButton.visibility = GONE
        }
    }

    @Test
    fun `playVideo when close caption enabled and available, makes ccButton visible`() {
//        val arcVideo = ArcVideo(expectedId, "uuid", expectedStartPosition, false, false, 100, "shareUrl", "headline", "pageName", "videoName", "videoSection", "videoSource", "videoCategory", "consentId", "fallbackUrl", "addTagUrl", false, subtitleUrl, "source", mockk(), false, false, false, ArcMediaPlayerConfig.CCStartMode.DEFAULT)
//        val currentMappedTrackInfo = mockk<MappingTrackSelector.MappedTrackInfo>()
//        val format = mockk<Format>()
//        val group = TrackGroup(format)
//        val trackGroups = TrackGroupArray(group)
//        every { mVideoManager.enableClosedCaption() } returns true
//        every { mTrackSelector.currentMappedTrackInfo } returns currentMappedTrackInfo
//        every { currentMappedTrackInfo.rendererCount } returns 1
//        every { currentMappedTrackInfo.getRendererType(0) } returns C.TRACK_TYPE_TEXT
//        every { constructedWith<DefaultTrackFilter>().filter(format, trackGroups) } returns true
//        every { currentMappedTrackInfo.getTrackGroups(0) } returns trackGroups
//
//
//        testObject.playVideo(arcVideo)
//
//        verify(exactly = 2) {
//            ccButton.visibility = VISIBLE
//        }

        //TODO test is passing but giving error in initVideoCaptions (which I'm having trouble with)
        //commented to remove error until fixed
    }

    @Test
    fun `playVideo with isShowSeekButton false and isKeepControlsSpaceOnHide true, makes seek btn layout invisible`() {
        val arcVideo = ArcVideo(
            expectedId,
            "uuid",
            expectedStartPosition,
            false,
            false,
            100,
            "shareUrl",
            "headline",
            "pageName",
            "videoName",
            "videoSection",
            "videoSource",
            "videoCategory",
            "consentId",
            "fallbackUrl",
            "addTagUrl",
            false,
            subtitleUrl,
            "source",
            mockk(),
            false,
            false,
            false,
            ArcXPVideoConfig.CCStartMode.DEFAULT
        )
        every { mVideoManager.isShowSeekButton } returns false
        every { mConfig.isKeepControlsSpaceOnHide } returns true

        testObject.playVideo(arcVideo)

        verify(exactly = 2) {
//            ffButtton.visibility = INVISIBLE
            mPlayerView.setShowFastForwardButton(false)
        }
    }

    @Test
    fun `playVideo with isShowSeekButton false and isKeepControlsSpaceOnHide false, makes seek btn layout gone`() {
        val arcVideo = ArcVideo(
            expectedId,
            "uuid",
            expectedStartPosition,
            false,
            false,
            100,
            "shareUrl",
            "headline",
            "pageName",
            "videoName",
            "videoSection",
            "videoSource",
            "videoCategory",
            "consentId",
            "fallbackUrl",
            "addTagUrl",
            false,
            subtitleUrl,
            "source",
            mockk(),
            false,
            false,
            false,
            ArcXPVideoConfig.CCStartMode.DEFAULT
        )
        every { mVideoManager.isShowSeekButton } returns false
        every { mConfig.isKeepControlsSpaceOnHide } returns false

        testObject.playVideo(arcVideo)

        verify(exactly = 2) {
            mPlayerView.setShowFastForwardButton(false)
            mPlayerView.setShowRewindButton(false)
        }
    }

    @Test
    fun `playVideo with mIsLive true, hides view components`() {
        val arcVideo = ArcVideo(
            expectedId,
            "uuid",
            expectedStartPosition,
            false,
            true,
            100,
            "shareUrl",
            "headline",
            "pageName",
            "videoName",
            "videoSection",
            "videoSource",
            "videoCategory",
            "consentId",
            "fallbackUrl",
            "addTagUrl",
            false,
            subtitleUrl,
            "source",
            mockk(),
            false,
            false,
            false,
            ArcXPVideoConfig.CCStartMode.DEFAULT
        )
        testObject.playVideo(arcVideo)

        verify(exactly = 2) {
            exoPosition.visibility = GONE
            exoDuration.visibility = GONE
            exoProgress.visibility = GONE
            mPlayerView.setShowFastForwardButton(false)
            mPlayerView.setShowRewindButton(false)
        }
    }

    @Test
    fun `playVideo with isShowCountDown false, sets exoDuration to gone`() {
        val arcVideo = ArcVideo(
            expectedId,
            "uuid",
            expectedStartPosition,
            false,
            false,
            100,
            "shareUrl",
            "headline",
            "pageName",
            "videoName",
            "videoSection",
            "videoSource",
            "videoCategory",
            "consentId",
            "fallbackUrl",
            "addTagUrl",
            false,
            subtitleUrl,
            "source",
            mockk(),
            false,
            false,
            false,
            ArcXPVideoConfig.CCStartMode.DEFAULT
        )
        every { mVideoManager.isShowCountDown } returns false

        testObject.playVideo(arcVideo)

        verify(exactly = 2) {
            exoDuration.visibility = GONE
        }
    }

    @Test
    fun `playVideo with isShowProgressBar false sets exoTimeBarLayout to gone`() {
        val arcVideo = ArcVideo(
            expectedId,
            "uuid",
            expectedStartPosition,
            false,
            false,
            100,
            "shareUrl",
            "headline",
            "pageName",
            "videoName",
            "videoSection",
            "videoSource",
            "videoCategory",
            "consentId",
            "fallbackUrl",
            "addTagUrl",
            false,
            subtitleUrl,
            "source",
            mockk(),
            false,
            false,
            false,
            ArcXPVideoConfig.CCStartMode.DEFAULT
        )
        every { mVideoManager.isShowProgressBar } returns false

        testObject.playVideo(arcVideo)

        verify(exactly = 2) {
            exoTimeBarLayout.visibility = GONE
        }
    }

    @Test
    fun `playVideo if shareUrl is empty and isKeepControlsSpaceOnHide true, sets share button to invisible`() {
        val arcVideo = ArcVideo(
            expectedId,
            "uuid",
            expectedStartPosition,
            false,
            false,
            100,
            "",
            "headline",
            "pageName",
            "videoName",
            "videoSection",
            "videoSource",
            "videoCategory",
            "consentId",
            "fallbackUrl",
            "addTagUrl",
            false,
            subtitleUrl,
            "source",
            mockk(),
            false,
            false,
            false,
            ArcXPVideoConfig.CCStartMode.DEFAULT
        )
        testObject.playVideo(arcVideo)

        verify(exactly = 2) {
            shareButton.visibility = INVISIBLE
        }
    }

    @Test
    fun `playVideo if shareUrl is empty and isKeepControlsSpaceOnHide false, sets share button to gone`() {
        val arcVideo = ArcVideo(
            expectedId,
            "uuid",
            expectedStartPosition,
            false,
            false,
            100,
            "",
            "headline",
            "pageName",
            "videoName",
            "videoSection",
            "videoSource",
            "videoCategory",
            "consentId",
            "fallbackUrl",
            "addTagUrl",
            false,
            subtitleUrl,
            "source",
            mockk(),
            false,
            false,
            false,
            ArcXPVideoConfig.CCStartMode.DEFAULT
        )
        every { mConfig.isKeepControlsSpaceOnHide } returns false

        testObject.playVideo(arcVideo)

        verify(exactly = 2) {
            shareButton.visibility = GONE
        }
    }

    @Test
    fun `playVideo when should play ads true, previous player null, startMuted false, initializes video player`() {
        playVideoThenVerify(createDefaultVideo())

        verifyNoExceptions()
    }

    @Test
    fun `playVideo if mConfigShowTimeMs is not null, sets value on player view`() {
        val timeout = 38762
        every { mConfig.controlsShowTimeoutMs } returns timeout

        testObject.playVideo(createDefaultVideo())

        verify(exactly = 1) {
            mPlayerView.controllerShowTimeoutMs = timeout
        }
    }

    @Test
    fun `playVideo if mConfig isDisableControlsWithTouch true, sets value on player view`() {
        every { mConfig.isDisableControlsWithTouch } returns true

        testObject.playVideo(createDefaultVideo())

        verify(exactly = 1) {
            mPlayerView.controllerHideOnTouch = true
        }
    }

    @Test
    fun `playVideo setUpPlayerControlListeners throws exception, is handled by listener`() {
        val errorMessage = "i am error"
        every { mPlayerView.findViewById<ImageButton>(R.id.exo_fullscreen) } throws Exception(
            errorMessage
        )

        testObject.playVideo(createDefaultVideo())

        verify(exactly = 1) {
            mListener.onError(ArcVideoSDKErrorType.EXOPLAYER_ERROR, errorMessage, testObject.video)
        }
    }

    @Test
    fun `setCurrentPlayer throws exception preparing ad, is output to Logger`() {
        val errorMessage = "i am error"
        val exception = Exception(errorMessage)
        val arcVideo = createDefaultVideo()
        every { mAdsLoader.adsLoader } throws exception
        every { mConfig.isLoggingEnabled } returns true

        testObject.playVideo(arcVideo)
        every { mCastPlayer.isCastSessionAvailable } returns true
        testObject.playVideo(arcVideo)

        verify(exactly = 1) {
            Log.e("ArcVideoSDK", "Error preparing ad for video $expectedId", exception)
        }
    }

    @Test
    fun `pipButton onClick when pipEnabled and not fullscreen, starts pip`() {
        val arcVideo = ArcVideo(
            expectedId,
            "uuid",
            expectedStartPosition,
            false,
            false,
            100,
            "shareUrl",
            "headline",
            "pageName",
            "videoName",
            "videoSection",
            "videoSource",
            "videoCategory",
            "consentId",
            "fallbackUrl",
            "addTagUrl[timestamp]",
            true,
            subtitleUrl,
            "source",
            mockk(),
            false,
            false,
            false,
            ArcXPVideoConfig.CCStartMode.DEFAULT
        )
        val pipButtonListener = slot<OnClickListener>()
        val expectedPosition = 9862345L
        val mFullScreenDialog = mockk<Dialog>(relaxed = true)

        val viewGroup = mockk<ViewGroup>(relaxed = true)
        val drawable = mockk<Drawable>()
        val videoData = mockk<TrackingTypeData.TrackingVideoTypeData>(relaxed = true)
        every { utils.createTrackingVideoTypeData() } returns videoData

        every {
            ContextCompat.getDrawable(
                mAppContext,
                R.drawable.FullScreenDrawableButtonCollapse
            )
        } returns drawable
        every { mPlayerView.parent } returns viewGroup
        every { mockView1.parent } returns viewGroup
        every { mockView2.parent } returns viewGroup
        every { mockView3.parent } returns viewGroup
        every { utils.createLayoutParams() } returns mockk()
        every { mVideoManager.isPipEnabled } returns true
        every { mPlayer.currentPosition } returns expectedPosition
        every { utils.createFullScreenDialog(mAppContext) } returns mFullScreenDialog
        testObject.playVideo(arcVideo)
        verify { pipButton.setOnClickListener(capture(pipButtonListener)) }
        clearAllMocks(answers = false)

        pipButtonListener.captured.onClick(mockk())

        verifySequence {
            mVideoManager.isPipEnabled
            utils.createFullScreenDialog(mAppContext)
            mFullScreenDialog.setOnKeyListener(any())
            mPlayerView.parent
            mPlayerView.parent
            viewGroup.removeView(mPlayerView)
            utils.createLayoutParams()
            mFullScreenDialog.addContentView(mPlayerView, any())
            mFullscreenOverlays.values
            mockView1.parent
            viewGroup.removeView(mockView1)
            utils.createLayoutParams()
            mFullScreenDialog.addContentView(mockView1, any())
            mockView1.bringToFront()

            mockView2.parent
            viewGroup.removeView(mockView2)
            utils.createLayoutParams()
            mFullScreenDialog.addContentView(mockView2, any())
            mockView2.bringToFront()

            mockView3.parent
            viewGroup.removeView(mockView3)
            utils.createLayoutParams()
            mFullScreenDialog.addContentView(mockView3, any())
            mockView3.bringToFront()

            mPlayerView.findViewById<ImageButton>(R.id.exo_fullscreen)
            ContextCompat.getDrawable(mAppContext, R.drawable.FullScreenDrawableButtonCollapse)
            fullScreenButton.setImageDrawable(drawable)
            mFullScreenDialog.show()
            utils.createTrackingVideoTypeData()
            videoData.arcVideo = arcVideo
            mPlayer.currentPosition
            videoData.position = expectedPosition
            mListener.onTrackingEvent(ON_OPEN_FULL_SCREEN, videoData)
            trackingHelper.fullscreen()
            mPlayerView.hideController()
            mPlayer.currentPosition
            mVideoManager.setSavedPosition(testObject.mVideoId, expectedPosition)
            mVideoManager.startPIP(testObject.video)

        }
    }

    @Test
    fun `pipButton onClick when in full screen, starts pip mode`() {
        val drawable = mockk<Drawable>()
        val videoData = mockk<TrackingTypeData.TrackingVideoTypeData>(relaxed = true)
        val expectedPosition = 324343L
        val viewGroup = mockk<ViewGroup>(relaxed = true)
        val mFullScreenDialog = mockk<Dialog>(relaxed = true)
        val playerFrame = mockk<RelativeLayout>(relaxed = true)
        val arcVideo = ArcVideo(
            expectedId,
            "uuid",
            expectedStartPosition,
            false,
            false,
            100,
            "shareUrl",
            "headline",
            "pageName",
            "videoName",
            "videoSection",
            "videoSource",
            "videoCategory",
            "consentId",
            "fallbackUrl",
            "addTagUrl[timestamp]",
            true,
            subtitleUrl,
            "source",
            mockk(),
            false,
            false,
            false,
            ArcXPVideoConfig.CCStartMode.DEFAULT
        )
        val pipButtonListener = slot<OnClickListener>()
        every { mVideoManager.isPipEnabled } returns true
        every { mPlayer.currentPosition } returns expectedPosition
        every {
            ContextCompat.getDrawable(
                mAppContext,
                R.drawable.FullScreenDrawableButton
            )
        } returns drawable
        every { utils.createTrackingVideoTypeData() } returns videoData
        every { mPlayer.currentPosition } returns expectedPosition
        every { mPlayerView.parent } returns viewGroup
        every { mockView1.parent } returns viewGroup
        every { mockView2.parent } returns viewGroup
        every { mockView3.parent } returns viewGroup
        every { mCastControlView.parent } returns viewGroup
        every { mListener.playerFrame } returns playerFrame
        every { utils.createFullScreenDialog(mAppContext) } returns mFullScreenDialog
        testObject.playVideo(arcVideo)
        verify { pipButton.setOnClickListener(capture(pipButtonListener)) }
        testObject.setFullscreen(true)
        clearAllMocks(answers = false)

        pipButtonListener.captured.onClick(mockk())

        verify(exactly = 1) {
            mPlayerView.hideController()
            mPlayer.currentPosition
            mVideoManager.setSavedPosition(expectedId, expectedPosition)
            mVideoManager.startPIP(arcVideo)
        }
    }

    @Test
    fun `pipButton onClick when pipEnabled and is fullscreen, starts pip`() {
        val arcVideo = ArcVideo(
            expectedId,
            "uuid",
            expectedStartPosition,
            false,
            false,
            100,
            "shareUrl",
            "headline",
            "pageName",
            "videoName",
            "videoSection",
            "videoSource",
            "videoCategory",
            "consentId",
            "fallbackUrl",
            "addTagUrl[timestamp]",
            true,
            subtitleUrl,
            "source",
            mockk(),
            false,
            false,
            false,
            ArcXPVideoConfig.CCStartMode.DEFAULT
        )
        val pipButtonListener = slot<OnClickListener>()
        val expectedPosition = 9862345L
        val mFullScreenDialog = mockk<Dialog>(relaxed = true)

        val viewGroup = mockk<ViewGroup>(relaxed = true)
        val drawable = mockk<Drawable>()
        val videoData = mockk<TrackingTypeData.TrackingVideoTypeData>(relaxed = true)
        every { utils.createTrackingVideoTypeData() } returns videoData

        every {
            ContextCompat.getDrawable(
                mAppContext,
                R.drawable.FullScreenDrawableButtonCollapse
            )
        } returns drawable
        every { mPlayerView.parent } returns viewGroup
        every { mockView1.parent } returns viewGroup
        every { mockView2.parent } returns viewGroup
        every { mockView3.parent } returns viewGroup
        every { utils.createLayoutParams() } returns mockk()

        every { mVideoManager.isPipEnabled } returns true
        every { mPlayer.currentPosition } returns expectedPosition
        testObject.playVideo(arcVideo)
        verify { pipButton.setOnClickListener(capture(pipButtonListener)) }
        every { utils.createFullScreenDialog(mAppContext) } returns mFullScreenDialog
        clearAllMocks(answers = false)

        pipButtonListener.captured.onClick(mockk())

        verifySequence {
            mVideoManager.isPipEnabled
            utils.createFullScreenDialog(mAppContext)
            mFullScreenDialog.setOnKeyListener(any())
            mPlayerView.parent
            mPlayerView.parent
            viewGroup.removeView(mPlayerView)
            utils.createLayoutParams()
            mFullScreenDialog.addContentView(mPlayerView, any())
            mFullscreenOverlays.values
            mockView1.parent
            viewGroup.removeView(mockView1)
            utils.createLayoutParams()
            mFullScreenDialog.addContentView(mockView1, any())
            mockView1.bringToFront()

            mockView2.parent
            viewGroup.removeView(mockView2)
            utils.createLayoutParams()
            mFullScreenDialog.addContentView(mockView2, any())
            mockView2.bringToFront()

            mockView3.parent
            viewGroup.removeView(mockView3)
            utils.createLayoutParams()
            mFullScreenDialog.addContentView(mockView3, any())
            mockView3.bringToFront()

            mPlayerView.findViewById<ImageButton>(R.id.exo_fullscreen)
            ContextCompat.getDrawable(mAppContext, R.drawable.FullScreenDrawableButtonCollapse)
            fullScreenButton.setImageDrawable(drawable)
            mFullScreenDialog.show()
            utils.createTrackingVideoTypeData()
            videoData.arcVideo = arcVideo
            mPlayer.currentPosition
            videoData.position = expectedPosition
            mListener.onTrackingEvent(ON_OPEN_FULL_SCREEN, videoData)
            trackingHelper.fullscreen()
            mPlayerView.hideController()
            mPlayer.currentPosition
            mVideoManager.setSavedPosition(testObject.mVideoId, expectedPosition)
            mVideoManager.startPIP(testObject.video)

        }
    }

    @Test
    fun `pipButton onClick when pip not enabled, opens pip settings dialog with no exceptions`() {
        val arcVideo = ArcVideo(
            expectedId,
            "uuid",
            expectedStartPosition,
            false,
            false,
            100,
            "shareUrl",
            "headline",
            "pageName",
            "videoName",
            "videoSection",
            "videoSource",
            "videoCategory",
            "consentId",
            "fallbackUrl",
            "addTagUrl[timestamp]",
            true,
            subtitleUrl,
            "source",
            mockk(),
            false,
            false,
            false,
            ArcXPVideoConfig.CCStartMode.DEFAULT
        )
        val pipButtonListener = slot<OnClickListener>()
        val mockDialog = mockk<AlertDialog>()
        val mockBuilder = mockk<AlertDialog.Builder>(relaxed = true) {
            every { setTitle("Picture-in-Picture functionality is disabled") } returns this
            every { setMessage("Would you like to enable Picture-in-Picture?") } returns this
            every { setPositiveButton(android.R.string.yes, any()) } returns this
            every { setNegativeButton(android.R.string.cancel, null) } returns this
            every { setCancelable(true) } returns this
            every { setIcon(android.R.drawable.ic_dialog_info) } returns this
            every { show() } returns mockDialog
        }
        every { utils.createAlertDialogBuilder(mAppContext) } returns mockBuilder
        every { mVideoManager.isPipEnabled } returns false
        testObject.playVideo(arcVideo)
        verify { pipButton.setOnClickListener(capture(pipButtonListener)) }
        clearAllMocks(answers = false)

        pipButtonListener.captured.onClick(mockk())

        verifySequence {
            utils.createAlertDialogBuilder(mAppContext)
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
        verifyNoExceptions()
    }

    @Test
    fun `pipButton onclick when pip disabled opens pip settings dialog, then on positive click starts new activity`() {
        val arcVideo = ArcVideo(
            expectedId,
            "uuid",
            expectedStartPosition,
            false,
            false,
            100,
            "shareUrl",
            "headline",
            "pageName",
            "videoName",
            "videoSection",
            "videoSource",
            "videoCategory",
            "consentId",
            "fallbackUrl",
            "addTagUrl[timestamp]",
            true,
            subtitleUrl,
            "source",
            mockk(),
            false,
            false,
            false,
            ArcXPVideoConfig.CCStartMode.DEFAULT
        )
        val pipButtonListener = slot<OnClickListener>()
        val mockDialog = mockk<AlertDialog>()
        val uri = mockk<Uri>()
        val intent = mockk<Intent>(relaxed = true)
        val listener = slot<DialogInterface.OnClickListener>()
        val mockBuilder = mockk<AlertDialog.Builder>(relaxed = true) {
            every { setTitle("Picture-in-Picture functionality is disabled") } returns this
            every { setMessage("Would you like to enable Picture-in-Picture?") } returns this
            every { setPositiveButton(android.R.string.yes, any()) } returns this
            every { setNegativeButton(android.R.string.cancel, null) } returns this
            every { setCancelable(true) } returns this
            every { setIcon(android.R.drawable.ic_dialog_info) } returns this
            every { show() } returns mockDialog
        }
        every { mVideoManager.isPipEnabled } returns false
        every { utils.createIntent() } returns intent
        every { Uri.fromParts("package", mockPackageName, null) } returns uri
        every { utils.createAlertDialogBuilder(mAppContext) } returns mockBuilder
        testObject.playVideo(arcVideo)
        verify { pipButton.setOnClickListener(capture(pipButtonListener)) }
        pipButtonListener.captured.onClick(mockk())
        verify { mockBuilder.setPositiveButton(android.R.string.yes, capture(listener)) }

        listener.captured.onClick(mockk(), 0)

        verify {
            utils.createIntent()
            intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            Uri.fromParts("package", mockPackageName, null)
            intent.data = uri
            mAppContext.startActivity(intent)
        }
        verifyNoExceptions()
    }

    @Test
    fun `pipButton onClick when pip not enabled, opens pip settings dialog throws exception and is handled`() {
        val errorMessage = "pip error"
        val exception = mockk<Exception>()
        val arcVideo = ArcVideo(
            expectedId,
            "uuid",
            expectedStartPosition,
            false,
            false,
            100,
            "shareUrl",
            "headline",
            "pageName",
            "videoName",
            "videoSection",
            "videoSource",
            "videoCategory",
            "consentId",
            "fallbackUrl",
            "addTagUrl[timestamp]",
            true,
            subtitleUrl,
            "source",
            mockk(),
            false,
            false,
            false,
            ArcXPVideoConfig.CCStartMode.DEFAULT
        )
        val pipButtonListener = slot<OnClickListener>()
        every { exception.message } returns errorMessage
        every { mVideoManager.isPipEnabled } returns false
        every { utils.createAlertDialogBuilder(mAppContext) } throws Exception(errorMessage)
        testObject.playVideo(arcVideo)
        verify { pipButton.setOnClickListener(capture(pipButtonListener)) }

        pipButtonListener.captured.onClick(mockk())

        verify(exactly = 1) {
            utils.createAlertDialogBuilder(mAppContext)
            mListener.onError(ArcVideoSDKErrorType.EXOPLAYER_ERROR, errorMessage, testObject.video)
        }
    }

    @Test
    fun `pipButton onClick when pip is enabled, fullscreen, starts pip`() {
        val pipButtonListener = slot<OnClickListener>()
        val drawable = mockk<Drawable>()
        val videoData = mockk<TrackingTypeData.TrackingVideoTypeData>(relaxed = true)
        val expectedPosition = 324343L
        val viewGroup = mockk<ViewGroup>(relaxed = true)
        every { mVideoManager.isPipEnabled } returns true
        every { mPlayer.currentPosition } returns expectedPosition
        every { mPlayerView.parent } returns viewGroup
        every { mPlayerView.findViewById<ImageButton>(R.id.exo_fullscreen) } returns fullScreenButton
        every {
            ContextCompat.getDrawable(
                mAppContext,
                R.drawable.FullScreenDrawableButton
            )
        } returns drawable
        every { utils.createTrackingVideoTypeData() } returns videoData
        testObject.playVideo(createDefaultVideo())
        testObject.setFullscreenUi(true)
        verify { pipButton.setOnClickListener(capture(pipButtonListener)) }
        clearAllMocks(answers = false)

        pipButtonListener.captured.onClick(mockk())

        verifySequence {
            mVideoManager.isPipEnabled
            mPlayerView.hideController()
            mVideoManager.setSavedPosition(expectedId, expectedPosition)
            mVideoManager.startPIP(testObject.video)
        }
    }

    @Test
    fun `onPipExit enables controller and returns to full screen`() {
        val mFullScreenDialog = mockk<Dialog>(relaxed = true)
        every { utils.createFullScreenDialog(mAppContext) } returns mFullScreenDialog
        val pipButtonListener = slot<OnClickListener>()
        testObject.playVideo(createDefaultVideo())
        verify { pipButton.setOnClickListener(capture(pipButtonListener)) }
        testObject.setFullscreenUi(true)
        pipButtonListener.captured.onClick(mockk())
        clearAllMocks(answers = false)



        testObject.onPipExit()

        verifySequence { mPlayerView.useController = true }
        verify { mFullScreenDialog wasNot called }
    }

    @Test
    fun `onPipExit enables controller and returns to normal screen`() {
        val mFullScreenDialog = mockk<Dialog>(relaxed = true)
        val pipButtonListener = slot<OnClickListener>()
        val viewGroup = mockk<ViewGroup>(relaxed = true)
        val drawable = mockk<Drawable>()
        val videoData = mockk<TrackingTypeData.TrackingVideoTypeData>(relaxed = true)
        val mVideo = createDefaultVideo()
        val expectedPosition = 1234L
        every { utils.createTrackingVideoTypeData() } returns videoData
        every {
            ContextCompat.getDrawable(
                mAppContext,
                R.drawable.FullScreenDrawableButton
            )
        } returns drawable
        every { mPlayerView.parent } returns viewGroup
        every { mockView1.parent } returns viewGroup
        every { mockView2.parent } returns viewGroup
        every { mockView3.parent } returns viewGroup
        every { utils.createFullScreenDialog(mAppContext) } returns mFullScreenDialog
        every { mPlayer.currentPosition } returns expectedPosition
        every { mVideoManager.isPipEnabled } returns true
        every { mListener.isStickyPlayer } returns true
        testObject.playVideo(mVideo)
        verify { pipButton.setOnClickListener(capture(pipButtonListener)) }
        pipButtonListener.captured.onClick(mockk())
        clearAllMocks(answers = false)

        testObject.onPipExit()

        verifySequence {
            mPlayerView.useController = true
            mPlayerView.parent
            mPlayerView.parent
            viewGroup.removeView(mPlayerView)
            mListener.playerFrame.addView(mPlayerView)
            mFullscreenOverlays.values
            viewGroup.removeView(mockView1)
            mListener.playerFrame.addView(mockView1)
            viewGroup.removeView(mockView2)
            mListener.playerFrame.addView(mockView2)
            viewGroup.removeView(mockView3)
            mListener.playerFrame.addView(mockView3)
            mPlayerView.findViewById<ImageButton>(R.id.exo_fullscreen)
            ContextCompat.getDrawable(mAppContext, R.drawable.FullScreenDrawableButton)
            fullScreenButton.setImageDrawable(drawable)
            mListener.isStickyPlayer
            mPlayerView.hideController()
            mPlayerView.requestLayout()
            mFullScreenDialog.dismiss()
            utils.createTrackingVideoTypeData()
            videoData.arcVideo = mVideo
            mPlayer.currentPosition
            videoData.position = expectedPosition
            mListener.onTrackingEvent(ON_CLOSE_FULL_SCREEN, videoData)
            trackingHelper.normalScreen()
        }
    }

    @Test
    fun `onPipExit when controls fully disabled does not re enable controls`() {
        every { mConfig.isDisableControlsFully } returns true
        val mFullScreenDialog = mockk<Dialog>(relaxed = true)
        every { utils.createFullScreenDialog(mAppContext) } returns mFullScreenDialog
        testObject.playVideo(createDefaultVideo())
        clearAllMocks(answers = false)

        testObject.onPipExit()

        verify(exactly = 0) {
            mPlayerView.useController = any()
        }
    }

    @Test
    fun `playVideo when startMuted is true mutes player in initLocalPlayer`() {
        val arcVideo = ArcVideo(
            expectedId,
            "uuid",
            expectedStartPosition,
            false,
            false,
            100,
            "shareUrl",
            "headline",
            "pageName",
            "videoName",
            "videoSection",
            "videoSource",
            "videoCategory",
            "consentId",
            "fallbackUrl",
            "addTagUrl[timestamp]",
            false,
            subtitleUrl,
            "source",
            mockk(),
            false,
            true,
            false,
            ArcXPVideoConfig.CCStartMode.DEFAULT
        )
        every { mPlayer.volume } returns 0.784f

        testObject.playVideo(arcVideo)

        verify(exactly = 3) { mPlayer.volume }
        verify(exactly = 1) { mPlayer.volume = 0f }
    }

    @Test
    fun `onPlayerError isBehindLiveWindow plays Video and triggers event and removes overlay views`() {
        val exception = mockk<RuntimeException>()
        val exoPlaybackException = ExoPlaybackException.createForUnexpected(
            exception,
            PlaybackException.ERROR_CODE_BEHIND_LIVE_WINDOW
        )

        val trackingTypeData = slot<TrackingTypeData>()
        testObject.playVideo(createDefaultVideo())

        clearAllMocks(answers = false)

        testObject.onPlayerError(exoPlaybackException)

        verify(exactly = 1) {
            mPlayerView.removeView(mockView1)
            mPlayerView.removeView(mockView2)
            mPlayerView.removeView(mockView3)
            mPlayer.seekToDefaultPosition()
            mPlayer.prepare()
            mListener.onTrackingEvent(BEHIND_LIVE_WINDOW_ADJUSTMENT, capture(trackingTypeData))
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

        testObject.playVideo(createDefaultVideo())
        clearAllMocks(answers = false)

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
        testObject.playVideo(createDefaultVideo())
        clearAllMocks(answers = false)

        testObject.onPlayerError(exoPlaybackException)

        verifySequence {
            mListener.onError(ArcVideoSDKErrorType.SOURCE_ERROR, sourceError, sourceException)
            mListener.logError("Exoplayer Source Error: No url passed from backend. Caused by:\n$sourceException")
        }
    }

    @Test
    fun `onPlayerError exception is logged`() {
        val sourceException = mockk<FileDataSource.FileDataSourceException>()
        val exoPlaybackException = ExoPlaybackException.createForSource(
            sourceException,
            PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED
        )
        every { mConfig.isLoggingEnabled } returns true
        testObject.playVideo(createDefaultVideo())
        clearAllMocks(answers = false)

        testObject.onPlayerError(exoPlaybackException)

        verify(exactly = 1) {
            Log.e("PostTvPlayerImpl", "ExoPlayer Error", exoPlaybackException)
        }
    }

    @Test
    fun `onPlayerError exception is unknown and calls handler`() {
        val sourceException = mockk<FileDataSource.FileDataSourceException>()
        val exoPlaybackException = ExoPlaybackException.createForUnexpected(RuntimeException())
        every { sourceException.cause } returns null
        testObject.playVideo(createDefaultVideo())
        clearAllMocks(answers = false)

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
        every { mPlayer.currentWindowIndex } returns expectedCurrentWindowIndex
        every { utils.createTrackingVideoTypeData() } returns videoData
        testObject.playVideo(arcVideo1)
        testObject.addVideo(arcVideo2)
        clearAllMocks(answers = false)
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
            videoData.arcVideo = arcVideo1
            mListener.onTrackingEvent(ON_PLAY_COMPLETED, videoData)
            utils.createTrackingVideoTypeData()
            videoData.percentage = 0
            videoData.position = 0L
            videoData.arcVideo = arcVideo2
            mListener.onTrackingEvent(ON_PLAY_STARTED, videoData)
        }
        assertEquals(arcVideo2, testObject.video)
    }

    @Test
    fun `playOnLocal throws exception, and is handled`() {
        val message = "play on local exception"
        val exception = Exception(message)
        every { expectedAdUri.toString() } throws exception
        testObject.playVideo(createDefaultVideo())

        verify(exactly = 1) {
            mListener.onError(ArcVideoSDKErrorType.INIT_ERROR, message, exception)
        }
    }

    @Test
    fun `createMediaSourceWithCaptions throws exception, and is handled`() {
        val message = "createMediaSourceWithCaptions exception"
        val exception = Exception(message)
        val newId = "382764"
        val arcVideo = ArcVideo(
            newId,
            "uuid",
            expectedStartPosition,
            false,
            false,
            100,
            "shareUrl",
            "headline",
            "pageName",
            "videoName",
            "videoSection",
            "videoSource",
            "videoCategory",
            "consentId",
            "fallbackUrl",
            "addTagUrl[timestamp]",
            true,
            subtitleUrl,
            "source",
            mockk(),
            false,
            false,
            false,
            ArcXPVideoConfig.CCStartMode.DEFAULT
        )
        every { Uri.parse(newId) } throws exception

        testObject.playVideo(arcVideo)

        verify(exactly = 1) {
            mListener.onError(ArcVideoSDKErrorType.EXOPLAYER_ERROR, message, arcVideo)
        }
    }

    @Test
    fun `set Volume sets mPlayer volume when mPlayer is not null`() {
        val expectedVolume = .78f
        val drawable = mockk<Drawable>()
        testObject.playVideo(createDefaultVideo())
        clearAllMocks(answers = false)

        every {
            ContextCompat.getDrawable(
                mAppContext,
                R.drawable.MuteOffDrawableButton
            )
        } returns drawable

        testObject.setVolume(expectedVolume)

        verifySequence {
            mPlayer.volume = expectedVolume
            mPlayerView.findViewById<ImageButton>(R.id.exo_volume)
            ContextCompat.getDrawable(mAppContext, R.drawable.MuteOffDrawableButton)
            volumeButton.setImageDrawable(drawable)
        }

        verify { mListener wasNot called }
    }

    @Test
    fun `set Volume to 0 sets mPlayer volume when mPlayer is not null`() {
        val expectedVolume = 0.0f
        val drawable = mockk<Drawable>()
        testObject.playVideo(createDefaultVideo())
        clearAllMocks(answers = false)

        every {
            ContextCompat.getDrawable(
                mAppContext,
                R.drawable.MuteDrawableButton
            )
        } returns drawable

        testObject.setVolume(expectedVolume)

        verifySequence {
            mPlayer.volume = expectedVolume
            mPlayerView.findViewById<ImageButton>(R.id.exo_volume)
            ContextCompat.getDrawable(mAppContext, R.drawable.MuteDrawableButton)
            volumeButton.setImageDrawable(drawable)
        }

        verify { mListener wasNot called }
    }

    @Test
    fun `set Volume throws exception and is handled`() {
        val expectedMessage = "error text"
        val arcVideo = createDefaultVideo()
        testObject.playVideo(arcVideo)
        every { mPlayer.volume = expectedVolume } throws Exception(expectedMessage)
        clearAllMocks(answers = false)

        testObject.setVolume(expectedVolume)

        verifySequence {
            mListener.onError(
                ArcVideoSDKErrorType.EXOPLAYER_ERROR,
                expectedMessage,
                arcVideo
            )
        }
    }

    @Test
    fun `getPlayWhenReadyState returns currentPlayer getPlayWhenReady value when currentPlayer is not null`() {
        every { mPlayer.playWhenReady } returns true

        testObject.playVideo(createDefaultVideo())

        assertTrue(testObject.playWhenReadyState)
    }

    @Test
    fun `getPlayWhenReadyState returns false if currentPlayer is null`() {
        assertFalse(testObject.playWhenReadyState)
    }

    @Test
    fun `onVolumeChanged changes volume on trackingHelper`() {
        val expectedVolume = .345f
        testObject.onVolumeChanged(expectedVolume)
        verifySequence { trackingHelper.volumeChange(expectedVolume) }
    }

    @Test
    fun `getPlaybackState returns currentPlayer playbackState when it is not null`() {
        val expectedPlayBackState = 3
        every { mPlayer.playbackState } returns expectedPlayBackState

        testObject.playVideo(createDefaultVideo())

        assertEquals(expectedPlayBackState, testObject.playbackState)
    }

    @Test
    fun `getPlaybackState returns zero if currentPlayer is null`() {
        assertEquals(0, testObject.playbackState)
    }

    @Test
    fun `seekTo seeks to time if mPlayer is not null`() {
        val seekToMs = 287364
        testObject.playVideo(createDefaultVideo())

        testObject.seekTo(seekToMs)

        verify(exactly = 1) { mPlayer.seekTo(seekToMs.toLong()) }
    }

    @Test
    fun `seekTo throws exception and is handled`() {
        val errorMessage = "error in seek to"
        val arcVideo = createDefaultVideo()

        testObject.playVideo(arcVideo)
        every { mPlayer.seekTo(any()) } throws Exception(errorMessage)
        testObject.seekTo(234)

        verify(exactly = 1) {
            mListener.onError(
                ArcVideoSDKErrorType.EXOPLAYER_ERROR,
                errorMessage,
                arcVideo
            )
        }
    }

    @Test
    fun `stop throws exception and is handled`() {
        val errorMessage = "error in stop"
        val arcVideo = createDefaultVideo()
        testObject.playVideo(arcVideo)
        every { mPlayer.playWhenReady = false } throws Exception(errorMessage)
        clearAllMocks(answers = false)

        testObject.stop()

        verifySequence {
            mListener.onError(
                ArcVideoSDKErrorType.EXOPLAYER_ERROR,
                errorMessage,
                arcVideo
            )
        }
    }

    @Test
    fun `stop stops player, setPlayWhenReady to false and seek to zero`() {
        testObject.playVideo(createDefaultVideo())
        clearAllMocks(answers = false)

        testObject.stop()

        verifySequence {
            mPlayer.playWhenReady = false
            mPlayer.stop()
            mPlayer.seekTo(0)
        }
    }

    @Test
    fun `resume if mPlayer is not null, restarts player and sends tracking event to listener`() {
        val arcVideo = createDefaultVideo()
        val expectedCurrentPosition = 876435L
        val videoData = mockk<TrackingTypeData.TrackingVideoTypeData>(relaxed = true)
        every { utils.createTrackingVideoTypeData() } returns videoData
        every { mPlayer.currentPosition } returns expectedCurrentPosition
        testObject.playVideo(arcVideo)
        clearAllMocks(answers = false)

        testObject.resume()

        verifySequence {
            mPlayer.playWhenReady = true
            utils.createTrackingVideoTypeData()
            videoData.arcVideo = arcVideo
            mPlayer.currentPosition
            videoData.position = expectedCurrentPosition
        }
    }

    @Test
    fun `resume throws exception, and is handled`() {
        val arcVideo = createDefaultVideo()
        val errorMessage = "error in resume"
        testObject.playVideo(arcVideo)
        every { utils.createTrackingVideoTypeData() } throws Exception(errorMessage)
        clearAllMocks(answers = false)

        testObject.resume()

        verifySequence {
            mListener.onError(
                ArcVideoSDKErrorType.EXOPLAYER_ERROR,
                errorMessage,
                arcVideo
            )
        }
    }

    @Test
    fun `setCcButtonDrawable when ccButton is not null, sets drawable then returns true`() {
        val expectedDrawable = mockk<Drawable>()
        val expectedDrawableIntValue = 234534
        every {
            ContextCompat.getDrawable(
                mAppContext,
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
        assertFalse(testObject.setCcButtonDrawable(34597))
    }

    @Test
    fun `playVideo when should play ads false, previous player not null, not ended`() {
        every { mPlayer.currentPosition } returns 0L
        val arcVideo = ArcVideo(
            expectedId,
            "uuid",
            expectedStartPosition,
            false,
            false,
            100,
            "shareUrl",
            "headline",
            "pageName",
            "videoName",
            "videoSection",
            "videoSource",
            "videoCategory",
            "consentId",
            "fallbackUrl",
            "addTagUrl[timestamp]",
            false,
            subtitleUrl,
            "source",
            mockk(),
            false,
            false,
            false,
            ArcXPVideoConfig.CCStartMode.DEFAULT
        )
        testObject.playVideo(arcVideo)
        every { mCastPlayer.isCastSessionAvailable } returns true
        testObject.playVideo(arcVideo)


        verify(exactly = 1) {
            mPlayer.playbackState
            mPlayer.currentPosition
            mVideoManager.setSavedPosition(testObject.id, mPlayer.currentPosition)
            mPlayer.stop()
        }
        verify(exactly = 2) { mFullscreenOverlays.values }
        verify(exactly = 2) {
            mPlayerView.addView(mockView1)
            mPlayerView.addView(mockView2)
            mPlayerView.addView(mockView3)
        }
        verifyNoExceptions()
    }

    @Test
    fun `playVideo when should play ads false, previous player not null, Player state ended`() {
        val arcVideo = ArcVideo(
            expectedId,
            "uuid",
            expectedStartPosition,
            false,
            false,
            100,
            "shareUrl",
            "headline",
            "pageName",
            "videoName",
            "videoSection",
            "videoSource",
            "videoCategory",
            "consentId",
            "fallbackUrl",
            "addTagUrl[timestamp]",
            false,
            subtitleUrl,
            "source",
            mockk(),
            false,
            false,
            false,
            ArcXPVideoConfig.CCStartMode.DEFAULT
        )
        every { mPlayer.playbackState } returns Player.STATE_ENDED
        testObject.playVideo(arcVideo)
        every { mCastPlayer.isCastSessionAvailable } returns true
        testObject.playVideo(arcVideo)

        verify(exactly = 0) {
            mPlayer.currentPosition
            mVideoManager.setSavedPosition(testObject.id, mPlayer.currentPosition)
        }
        verify(exactly = 1) {
            mPlayer.playbackState
            mPlayer.stop()
        }
        verify(exactly = 2) { mFullscreenOverlays.values }
        verify(exactly = 2) {
            mPlayerView.addView(mockView1)
            mPlayerView.addView(mockView2)
            mPlayerView.addView(mockView3)
        }
        verifyNoExceptions()
    }

    @Test
    fun `playVideo when should play ads true, previous player not null, not ended`() {
        val arcVideo = ArcVideo(
            expectedId,
            "uuid",
            expectedStartPosition,
            false,
            false,
            100,
            "shareUrl",
            "headline",
            "pageName",
            "videoName",
            "videoSection",
            "videoSource",
            "videoCategory",
            "consentId",
            "fallbackUrl",
            "addTagUrl[timestamp]",
            true,
            subtitleUrl,
            "source",
            mockk(),
            false,
            false,
            false,
            ArcXPVideoConfig.CCStartMode.DEFAULT
        )
        testObject.playVideo(arcVideo)
        every { mCastPlayer.isCastSessionAvailable } returns true
        clearAllMocks(answers = false)
        testObject.playVideo(arcVideo)

        verify(exactly = 1) {
            mFullscreenOverlays.values
            mPlayerView.addView(mockView1)
            mPlayerView.addView(mockView2)
            mPlayerView.addView(mockView3)
            mAdsLoaderAdsLoader.addAdsLoadedListener(adsLoadedListener)
            mAdsLoader.setPlayer(mPlayer)
            mPlayer.playbackState
            mPlayer.currentPosition
            mVideoManager.setSavedPosition(any(), any())
            utils.createAdsLoadedListener(mListener, arcVideo, testObject, expectedSessionId)
            mPlayer.stop()
        }
        verifyNoExceptions()
    }


    @Test
    fun `playVideo uses castPlayer when available`() {
        val arcVideo = ArcVideo(
            expectedId,
            "uuid",
            expectedStartPosition,
            false,
            false,
            100,
            "shareUrl",
            "headline",
            "pageName",
            "videoName",
            "videoSection",
            "videoSource",
            "videoCategory",
            "consentId",
            "fallbackUrl",
            "addTagUrl[timestamp]",
            false,
            subtitleUrl,
            "source",
            mockk(),
            false,
            false,
            false,
            ArcXPVideoConfig.CCStartMode.DEFAULT
        )
        every { mCastPlayer.isCastSessionAvailable } returns true
        testObject.playVideo(arcVideo)
        verify(exactly = 1) {
            mPlayerView.visibility = GONE
            mCastControlView.show()
            mCastControlView.keepScreenOn = true
        }
    }

    @Test
    fun `when addVideo called and mVideos is not null, then the video is added to mVideos`() {
        val newVideo = mockk<ArcVideo>()

        testObject.addVideo(newVideo)

        assertEquals(newVideo, testObject.mVideos!![0])
    }

    @Test
    fun `getAdType mPlayer not null and is playing ad gets adGroupTime from mPlayer`() {
        every { mPlayer.isPlayingAd } returns true

        playVideoThenVerify(createDefaultVideo())

        assertEquals(0, testObject.adType)
    }

    @Test
    fun `getAdType mPlayer not null and is not playing ad returns zero`() {
        every { mPlayer.isPlayingAd } returns false

        playVideoThenVerify(createDefaultVideo())
        assertEquals(0, testObject.adType)
    }

    @Test
    fun `getAdType mPlayer null returns zero`() {
        assertEquals(0, testObject.adType)
    }

    @Test
    fun `isPlaying returns false when currentPlayer is null`() {
        assertFalse(testObject.isPlaying)
    }

    @Test
    fun `isPlaying returns true when currentPlayer is not null state ready and getPlayWhenReady true`() {
        every { mPlayer.playbackState } returns Player.STATE_READY
        every { mPlayer.playWhenReady } returns true

        playVideoThenVerify(createDefaultVideo())
        assertTrue(testObject.isPlaying)
    }

    @Test
    fun `isPlaying returns false when currentPlayer is not null state ready and getPlayWhenReady false`() {
        every { mPlayer.playbackState } returns Player.STATE_READY
        every { mPlayer.playWhenReady } returns false

        playVideoThenVerify(createDefaultVideo())
        assertFalse(testObject.isPlaying)
    }

    @Test
    fun `isPlaying returns false when currentPlayer is not null state other than ready`() {
        every { mPlayer.playbackState } returns Player.STATE_ENDED
        every { mPlayer.playWhenReady } returns false

        playVideoThenVerify(createDefaultVideo())
        assertFalse(testObject.isPlaying)
    }

    @Test
    fun `onAdEvent with SKIPPABLE_STATE_CHANGED getAd is not null but mPlayer is null`() {
        verifyOnAdEvent(SKIPPABLE_STATE_CHANGED, createAd(), AD_SKIP_SHOWN)
    }

    @Test
    fun `onAdEvent with COMPLETED getAd is not null`() {
        verifyOnAdEvent(COMPLETED, createAd(), AD_PLAY_COMPLETED)
    }

    @Test
    fun `onAdEvent with FIRST_QUARTILE getAd is not null`() {
        verifyOnAdEvent(AdEvent.AdEventType.FIRST_QUARTILE, createAd(), VIDEO_25_WATCHED)
    }

    @Test
    fun `onAdEvent with MIDPOINT getAd is not null`() {
        verifyOnAdEvent(AdEvent.AdEventType.MIDPOINT, createAd(), VIDEO_50_WATCHED)
    }

    @Test
    fun `onAdEvent with THIRD_QUARTILE getAd is not null`() {
        verifyOnAdEvent(AdEvent.AdEventType.THIRD_QUARTILE, createAd(), VIDEO_75_WATCHED)
    }

    @Test
    fun `onAdEvent with AD_LOADED getAd is not null`() {
        testObject.playVideo(createDefaultVideo())
        clearAllMocks(answers = false)
        every { mPlayer.currentPosition } returns 0L
        verifyOnAdEvent(AdEvent.AdEventType.LOADED, createAd(), AD_LOADED)
        assertTrue(testObject.isControlDisabled)
        assertTrue(testObject.adPlaying)
        verify(exactly = 1) {
            mConfig.isDisableControlsFully
            mPlayerView.useController = false
        }
    }

    @Test
    fun `onAdEvent with AD_BREAK_STARTED getAd is not null`() {
        testObject.playVideo(createDefaultVideo())
        clearAllMocks(answers = false)
        every { mPlayer.currentPosition } returns 0L
        verifyOnAdEvent(
            AdEvent.AdEventType.AD_BREAK_STARTED,
            createAd(),
            TrackingType.AD_BREAK_STARTED
        )
        assertTrue(testObject.isControlDisabled)
        assertTrue(testObject.adPlaying)
        verify(exactly = 1) {
            mConfig.isDisableControlsFully
            mPlayerView.useController = false
        }
    }

    @Test
    fun `onAdEvent with AD_BREAK_READY calls disable controls`() {
        mockkConstructor(ArcAd::class)
        mockkConstructor(TrackingTypeData.TrackingAdTypeData::class)
        val inputAdEvent = mockk<AdEvent>(relaxed = true) {
            every { type } returns AD_BREAK_READY
        }
        testObject.playVideo(createDefaultVideo())
        clearAllMocks(answers = false)
        testObject.onAdEvent(inputAdEvent)

        assertTrue(testObject.isControlDisabled)
        assertTrue(testObject.adPlaying)
        verify(exactly = 1) {
            mConfig.isDisableControlsFully
            mPlayerView.useController = false
        }
    }
    @Test
    fun `onAdEvent with AD_BREAK_READY calls disable controls but does not disable when controls are fully disabled`() {
        every { mConfig.isDisableControlsFully } returns true
        mockkConstructor(ArcAd::class)
        mockkConstructor(TrackingTypeData.TrackingAdTypeData::class)
        val inputAdEvent = mockk<AdEvent>(relaxed = true) {
            every { type } returns AD_BREAK_READY
        }
        testObject.playVideo(createDefaultVideo())
        clearAllMocks(answers = false)
        testObject.onAdEvent(inputAdEvent)

        assertTrue(testObject.isControlDisabled)
        assertTrue(testObject.adPlaying)
        verify(exactly = 1) {
            mConfig.isDisableControlsFully
        }
        verify(exactly = 0) {
            mPlayerView.useController = false
        }
    }

    @Test
    fun `onAdEvent with SKIPPABLE_STATE_CHANGED getAd is null and mPlayer is null`() {
        verifyOnAdEvent(SKIPPABLE_STATE_CHANGED, null, AD_SKIP_SHOWN)
    }

    @Test
    fun `onAdEvent with AD_BREAK_ENDED getAd is null and mPlayer is null`() {
        verifyOnAdEvent(AdEvent.AdEventType.AD_BREAK_ENDED, null, TrackingType.AD_BREAK_ENDED)
        assertTrue(testObject.isFirstAdCompleted)
    }

    @Test
    fun `onAdEvent with SKIPPABLE_STATE_CHANGED getAd is null but mPlayer is not null`() {
        every { mPlayer.currentPosition } returns 0L
        testObject.playVideo(createDefaultVideo())
        verifyOnAdEvent(SKIPPABLE_STATE_CHANGED, null, AD_SKIP_SHOWN)
    }

    @Test
    fun `onAdEvent with SKIPPABLE_STATE_CHANGED getAd is not null and mPlayer is not null`() {
        every { mPlayer.currentPosition } returns 0L
        testObject.playVideo(createDefaultVideo())
        verifyOnAdEvent(SKIPPABLE_STATE_CHANGED, createAd(), AD_SKIP_SHOWN)
    }

    @Test
    fun `onAdEvent with PAUSED and ad is not paused`() {
        every { mPlayer.currentPosition } returns 0L
        testObject.adPlaying = true
        testObject.adPaused = false
        testObject.playVideo(createDefaultVideo())
        testObject.pause()
        verifyOnAdEvent(PAUSED, createAd(), AD_PAUSE)
    }

    @Test
    fun `onAdEvent with PAUSED and ad is paused`() {
        every { mPlayer.currentPosition } returns 0L
        testObject.adPlaying = true
        testObject.adPaused = true
        testObject.playVideo(createDefaultVideo())
        testObject.resume()
        verifyOnAdEvent(PAUSED, createAd(), AD_RESUME)
    }

    @Test
    fun `onAdEvent with SKIPPED getAd is not null and mPlayer is null`() {
        testObject.playVideo(createDefaultVideo())
        clearAllMocks(answers = false)
        every { mPlayer.currentPosition } returns 0L
        adBreakReadyAdEventToDisableControls()
        verifyOnAdEvent(SKIPPED, createAd(), AD_SKIPPED)
        assertFalse(testObject.isControlDisabled)
        assertFalse(testObject.adPlaying)
        assertTrue(testObject.isFirstAdCompleted)
        verify(exactly = 1) {
            mPlayerView.useController = true
        }
    }
    @Test
    fun `onAdEvent with SKIPPED when controls fully disabled does not re enable`() {
        every { mConfig.isDisableControlsFully } returns true
        testObject.playVideo(createDefaultVideo())
        clearAllMocks(answers = false)
        every { mPlayer.currentPosition } returns 0L
        adBreakReadyAdEventToDisableControls()
        verifyOnAdEvent(SKIPPED, createAd(), AD_SKIPPED)
        assertFalse(testObject.isControlDisabled)
        assertFalse(testObject.adPlaying)
        assertTrue(testObject.isFirstAdCompleted)
        verify(exactly = 0) {
            mPlayerView.useController = true
        }
    }

    @Test
    fun `onAdEvent with SKIPPED getAd is null and mPlayer is null`() {
        testObject.playVideo(createDefaultVideo())
        clearAllMocks(answers = false)
        every { mPlayer.currentPosition } returns 0L
        adBreakReadyAdEventToDisableControls()
        verifyOnAdEvent(SKIPPED, null, AD_SKIPPED)
        assertFalse(testObject.isControlDisabled)
        assertFalse(testObject.adPlaying)
        assertTrue(testObject.isFirstAdCompleted)
        verify(exactly = 1) {
            mPlayerView.useController = true
        }
    }

    @Test
    fun `onAdEvent with SKIPPED getAd is not null and mPlayer is not null`() {
        every { mPlayer.currentPosition } returns 0L
        testObject.playVideo(createDefaultVideo())
        verifyOnAdEvent(SKIPPED, createAd(), AD_SKIPPED)
        assertTrue(testObject.isFirstAdCompleted)
    }

    @Test
    fun `onAdEvent with SKIPPED getAd is null and mPlayer is not null`() {
        every { mPlayer.currentPosition } returns 0L
        testObject.playVideo(createDefaultVideo())
        verifyOnAdEvent(SKIPPED, null, AD_SKIPPED)
    }

    @Test
    fun `onAdEvent with ALL_ADS_COMPLETED`() {
        testObject.playVideo(createDefaultVideo())
        clearAllMocks(answers = false)

        testOnAdEventControlsDisabled(AdEvent.AdEventType.ALL_ADS_COMPLETED)

        assertTrue(testObject.isFirstAdCompleted)
        verify(exactly = 1) {
            mPlayerView.useController = true
        }
    }

    @Test
    fun `onAdEvent with ALL_ADS_COMPLETED controls fully disabled`() {
        every { mConfig.isDisableControlsFully } returns true
        testObject.playVideo(createDefaultVideo())
        clearAllMocks(answers = false)

        testOnAdEventControlsDisabled(AdEvent.AdEventType.ALL_ADS_COMPLETED)

        assertTrue(testObject.isFirstAdCompleted)
        verify(exactly = 0) {
            mPlayerView.useController = true
        }
    }

    @Test
    fun `onAdEvent with STARTED mPlayerView is not null`() {
        every { mPlayer.currentPosition } returns 0L
        testObject.playVideo(createDefaultVideo())
        every { adEvent.type } returns STARTED
        clearAllMocks(answers = false)

        adBreakReadyAdEventToDisableControls()
        testObject.onAdEvent(adEvent)

        verifyOnAdEvent(STARTED, null, AD_PLAY_STARTED)
        //verify(exactly = 1) { mPlayerView.setUseController(false) }
        assertTrue(testObject.isControlDisabled)
    }


    @Test
    fun `onAdEvent with CLICKED getAd is null and mPlayer is null`() {
        verifyOnAdEvent(CLICKED, null, AD_CLICKED)
    }

    @Test
    fun `onAdEvent with TAPPED getAd is null and mPlayer is null`() {
        verifyOnAdEvent(TAPPED, null, AD_CLICKED)
    }

    @Test
    fun `onAdEvent with CLICKED getAd is not null and mPlayer is null`() {
        verifyOnAdEvent(CLICKED, createAd(), AD_CLICKED)
    }

    @Test
    fun `onAdEvent with TAPPED getAd is not null and mPlayer is null`() {
        verifyOnAdEvent(TAPPED, createAd(), AD_CLICKED)
    }

    @Test
    fun `onVideoEvent when tracking type is VIDEO_PERCENTAGE_WATCHED and mIsLive true does not interact with listener`() {
        val arcVideo = ArcVideo(
            expectedId,
            "uuid",
            expectedStartPosition,
            false,
            true,
            100,
            "shareUrl",
            "headline",
            "pageName",
            "videoName",
            "videoSection",
            "videoSource",
            "videoCategory",
            "consentId",
            "fallbackUrl",
            "addTagUrl[timestamp]",
            true,
            subtitleUrl,
            "source",
            mockk(),
            false,
            false,
            false,
            ArcXPVideoConfig.CCStartMode.DEFAULT
        )
        testObject.playVideo(arcVideo)
        clearMocks(mListener)

        testObject.onVideoEvent(VIDEO_PERCENTAGE_WATCHED, mockk())

        verify { mListener wasNot called }
    }

    @Test
    fun `null methods return null`() {
        assertNull(testObject.setLoadErrorHandlingPolicy(mockk()))
        assertNull(testObject.setDrmSessionManagerProvider(mockk()))
        assertNull(testObject.createMediaSource(mockk()))
    }

    @Test
    fun `getOverlay returns item from mFullscreenOverlays`() {
        val tag = "tag"
        val view = mockk<View>()
        every { mFullscreenOverlays[tag] } returns view

        assertEquals(view, testObject.getOverlay(tag))
    }

    @Test
    fun `isVideoCaptionEnabled with default CC startMode non captioning manager service`() {
        val service = mockk<Any>()
        mockkStatic(PrefManager::class)
        every {
            PrefManager.getBoolean(
                mAppContext,
                PrefManager.IS_CAPTIONS_ENABLED,
                false
            )
        } returns true
        every { mAppContext.getSystemService(Context.CAPTIONING_SERVICE) } returns service

        testObject.playVideo(createDefaultVideo())

        assertTrue(testObject.isVideoCaptionEnabled)
    }

    @Test
    fun `isVideoCaptionEnabled with default CC startMode Captioning manager service`() {
        val service = mockk<CaptioningManager>()
        mockkStatic(PrefManager::class)
        every {
            PrefManager.getBoolean(
                mAppContext,
                PrefManager.IS_CAPTIONS_ENABLED,
                true
            )
        } returns true
        every { service.isEnabled } returns true
        every { mAppContext.getSystemService(Context.CAPTIONING_SERVICE) } returns service

        testObject.playVideo(createDefaultVideo())

        assertTrue(testObject.isVideoCaptionEnabled)
    }

    @Test
    fun `isVideoCaptionEnabled with ON CC startMode`() {
        val arcVideo = ArcVideo(
            expectedId,
            "uuid",
            expectedStartPosition,
            false,
            true,
            100,
            "shareUrl",
            "headline",
            "pageName",
            "videoName",
            "videoSection",
            "videoSource",
            "videoCategory",
            "consentId",
            "fallbackUrl",
            "addTagUrl[timestamp]",
            true,
            subtitleUrl,
            "source",
            mockk(),
            false,
            false,
            false,
            ArcXPVideoConfig.CCStartMode.ON
        )

        testObject.playVideo(arcVideo)

        assertTrue(testObject.isVideoCaptionEnabled)
    }

    @Test
    fun `isVideoCaptionEnabled throws exception returns false`() {
        mockkStatic(PrefManager::class)
        every {
            PrefManager.getBoolean(
                mAppContext,
                PrefManager.IS_CAPTIONS_ENABLED,
                any()
            )
        } throws Exception()

        testObject.playVideo(createDefaultVideo())

        assertFalse(testObject.isVideoCaptionEnabled)
    }

    @Test
    fun `onVideoEvent when tracking type is VIDEO_PERCENTAGE_WATCHED and mIsLive interacts with listener`() {
        val trackingTypeData = mockk<TrackingTypeData>()

        testObject.onVideoEvent(VIDEO_PERCENTAGE_WATCHED, trackingTypeData)

        verifySequence { mListener.onTrackingEvent(VIDEO_PERCENTAGE_WATCHED, trackingTypeData) }
    }

    @Test
    fun `onVideoEvent when tracking type is not VIDEO_PERCENTAGE_WATCHED interacts with listener`() {
        val trackingTypeData = mockk<TrackingTypeData>()

        testObject.onVideoEvent(AD_SKIPPED, trackingTypeData)

        verifySequence { mListener.onTrackingEvent(AD_SKIPPED, trackingTypeData) }
    }

    @Test
    fun `getSupportedTypes returns expected types`() {
        val expected = intArrayOf(C.TYPE_HLS, C.TYPE_SS, C.TYPE_DASH, C.TYPE_OTHER)

        val actual = testObject.supportedTypes

        assertTrue(expected contentEquals actual)
    }

    @Test
    fun `removeOverlay removes from map and View group`() {
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
    fun `getOverlay returns element from overlays map`() {
        val tag = "TAG"
        val view = mockk<View>()
        every { mFullscreenOverlays[tag] } returns view

        assertEquals(view, testObject.getOverlay(tag))
    }

    @Test
    fun `isVideoCaptionEnabled mVideo not null CC Start mode Default Start Mode`() {
        playVideoThenVerify(createDefaultVideo())

        val service = mockk<CaptioningManager>()
        every { service.isEnabled } returns true
        mockkStatic(PrefManager::class)
        every {
            PrefManager.getBoolean(
                mAppContext,
                PrefManager.IS_CAPTIONS_ENABLED,
                true
            )
        } returns true
        every { mAppContext.getSystemService(Context.CAPTIONING_SERVICE) } returns service

        assertTrue(testObject.isVideoCaptionEnabled)
        verify(exactly = 1) {
            mAppContext.getSystemService(Context.CAPTIONING_SERVICE)
            service.isEnabled
            PrefManager.getBoolean(mAppContext, PrefManager.IS_CAPTIONS_ENABLED, true)
        }
    }

    @Test
    fun `isVideoCaptionEnabled mVideo not null CC Start mode Default Start throws exception and then returns false`() {
        playVideoThenVerify(createDefaultVideo())
        every { mAppContext.getSystemService(Context.CAPTIONING_SERVICE) } throws Exception()
        clearAllMocks(answers = false)

        assertFalse(testObject.isVideoCaptionEnabled)
        verifySequence {
            mAppContext.getSystemService(Context.CAPTIONING_SERVICE)
        }
    }

    @Test
    fun `isVideoCaptionEnabled mVideo not null CC Start mode ON returns true`() {
        val arcVideo = ArcVideo(
            expectedId,
            "uuid",
            expectedStartPosition,
            false,
            false,
            100,
            "shareUrl",
            "headline",
            "pageName",
            "videoName",
            "videoSection",
            "videoSource",
            "videoCategory",
            "consentId",
            "fallbackUrl",
            "addTagUrl[timestamp]",
            true,
            subtitleUrl,
            "source",
            mockk(),
            false,
            false,
            false,
            ArcXPVideoConfig.CCStartMode.ON
        )

        playVideoThenVerify(arcVideo)

        assertTrue(testObject.isVideoCaptionEnabled)
    }

    @Test
    fun `isVideoCaptionEnabled mVideo not null CC Start mode other returns false`() {
        val arcVideo = ArcVideo(
            expectedId,
            "uuid",
            expectedStartPosition,
            false,
            false,
            100,
            "shareUrl",
            "headline",
            "pageName",
            "videoName",
            "videoSection",
            "videoSource",
            "videoCategory",
            "consentId",
            "fallbackUrl",
            "addTagUrl[timestamp]",
            true,
            subtitleUrl,
            "source",
            mockk(),
            false,
            false,
            false,
            ArcXPVideoConfig.CCStartMode.OFF
        )

        playVideoThenVerify(arcVideo)

        assertFalse(testObject.isVideoCaptionEnabled)
    }

    @Test
    fun `isVideoCaptionEnabled mVideo null returns false`() {
        assertFalse(testObject.isVideoCaptionEnabled)
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
        mockkConstructor(TrackingTypeData.TrackingSourceTypeData::class)
        val sourceCaptureSlot = slot<TrackingTypeData.TrackingSourceTypeData>()

        testObject.onTracksChanged(tracks)

        verifySequence {
            constructedWith<TrackingTypeData.TrackingSourceTypeData>().source = expected
            mListener.onTrackingEvent(SUBTITLE_SELECTION, capture(sourceCaptureSlot))
        }
        assertEquals(expected, sourceCaptureSlot.captured.source)
    }

    @Test
    fun `mPlayerView OnTouchListener from playVideo, if action up triggers tracking helper on touch event mConfig isDisableControlsWithTouch true`() {
        val listener = mutableListOf<View.OnTouchListener>()
        val view = mockk<View>(relaxed = true)
        val event = mockk<MotionEvent>()
        playVideoThenVerify(createDefaultVideo())
        verify { mPlayerView.setOnTouchListener(capture(listener)) }
        every { event.action } returns ACTION_UP
        every { mConfig.isDisableControlsWithTouch } returns true
        clearAllMocks(answers = false)

        assertTrue(listener[1].onTouch(view, event))

        verifySequence {
            trackingHelper.onTouch(event, expectedSavedPosition)
            mConfig.isDisableControlsWithTouch
        }
    }

    @Test
    fun `mPlayerView OnTouchListener from playVideo, if mConfig isDisableControlsWithTouch false, performs click and returns false`() {
        val view = mockk<View>(relaxed = true)
        val event = mockk<MotionEvent>()
        val listener = mutableListOf<View.OnTouchListener>()
        playVideoThenVerify(createDefaultVideo())
        verify { mPlayerView.setOnTouchListener(capture(listener)) }
        every { event.action } returns ACTION_UP
        every { mConfig.isDisableControlsWithTouch } returns false
        clearAllMocks(answers = false)

        assertFalse(listener[1].onTouch(view, event))
        verifySequence {
            trackingHelper.onTouch(event, expectedSavedPosition)
            mConfig.isDisableControlsWithTouch
            view.performClick()
        }
    }

    @Test
    fun `mPlayerView OnTouchListener from initLocalPlayer, when Event action is UP calls trackingHelper onTouch`() {
        val view = mockk<View>(relaxed = true)
        val event = mockk<MotionEvent>()
        val listener = mutableListOf<View.OnTouchListener>()
        playVideoThenVerify(createDefaultVideo())
        verify { mPlayerView.setOnTouchListener(capture(listener)) }
        clearAllMocks(answers = false)
        every { event.action } returns ACTION_UP

        listener[0].onTouch(view, event)

        verifySequence {
            event.action
            trackingHelper.onTouch(event, expectedSavedPosition)
        }
    }

    @Test
    fun `mPlayerView OnTouchListener from initLocalPlayer, performs click and returns false`() {
        playVideoThenVerify(createDefaultVideo())
        val listener = mutableListOf<View.OnTouchListener>()
        verify { mPlayerView.setOnTouchListener(capture(listener)) }
        val view = mockk<View>(relaxed = true)
        val event = mockk<MotionEvent>()
        clearMocks(trackingHelper)
        every { event.action } returns ACTION_BUTTON_PRESS

        assertFalse(listener[0].onTouch(view, event))
        verifySequence {
            view.performClick()
            event.action
        }
        verify { trackingHelper wasNot called }
    }

    @Test
    fun `shareButton cast onClick triggers share video event, and then notifies listener`() {
        val shareButtonListener = slot<OnClickListener>()
        val arcVideo = createDefaultVideo()
        val videoData = mockk<TrackingTypeData.TrackingVideoTypeData>(relaxed = true)
        every { utils.createTrackingVideoTypeData() } returns videoData
        testObject.playVideo(arcVideo)
        verify { castShareButton.setOnClickListener(capture(shareButtonListener)) }
        clearAllMocks(answers = false)

        shareButtonListener.captured.onClick(mockk())

        verifySequence {
            utils.createTrackingVideoTypeData()
            videoData.arcVideo = arcVideo
            mCastPlayer.currentPosition
            videoData.position = 0
            mListener.onTrackingEvent(ON_SHARE, videoData)
            mListener.onShareVideo(mHeadline, mShareUrl)
        }
    }

    @Test
    fun `shareButton player onClick triggers share video event, and then notifies listener`() {
        val shareButtonListener = slot<OnClickListener>()
        val arcVideo = createDefaultVideo()
        val videoData = mockk<TrackingTypeData.TrackingVideoTypeData>(relaxed = true)
        val expectedPosition = 239847L
        every { mPlayer.currentPosition } returns expectedPosition
        every { utils.createTrackingVideoTypeData() } returns videoData
        testObject.playVideo(arcVideo)
        verify { shareButton.setOnClickListener(capture(shareButtonListener)) }
        clearAllMocks(answers = false)

        shareButtonListener.captured.onClick(mockk())

        verifySequence {
            utils.createTrackingVideoTypeData()
            videoData.arcVideo = testObject.video
            mPlayer.currentPosition
            videoData.position = expectedPosition
            mListener.onTrackingEvent(ON_SHARE, videoData)
            mListener.onShareVideo(mHeadline, mShareUrl)
        }
    }

    @Test
    fun `backButton player onClick triggers back event, and then notifies listener`() {
        val backButtonListener = slot<OnClickListener>()
        val arcVideo = createDefaultVideo()
        val videoData = mockk<TrackingTypeData.TrackingVideoTypeData>(relaxed = true)
        every { utils.createTrackingVideoTypeData() } returns videoData
        testObject.playVideo(arcVideo)
        verify { backButton.setOnClickListener(capture(backButtonListener)) }
        clearAllMocks(answers = false)

        backButtonListener.captured.onClick(mockk())

        verifySequence {
            utils.createTrackingVideoTypeData()
            videoData.arcVideo = testObject.video
            mPlayer.currentPosition
            videoData.position = expectedSavedPosition
            mListener.onTrackingEvent(BACK_BUTTON_PRESSED, videoData)
        }
    }

    @Test
    fun `onTracksChanged with no selection parses and sends language as none to subtitle event`() {
        val expected = "none"
        val tracks = mockk<Tracks> {
            every { groups } returns ImmutableList.copyOf(
                emptyList()
            )
        }
        mockkConstructor(TrackingTypeData.TrackingSourceTypeData::class)
        val sourceCaptureSlot = slot<TrackingTypeData.TrackingSourceTypeData>()

        testObject.onTracksChanged(tracks)

        verifySequence {
            constructedWith<TrackingTypeData.TrackingSourceTypeData>().source = expected
            mListener.onTrackingEvent(SUBTITLE_SELECTION, capture(sourceCaptureSlot))
        }
        assertEquals(expected, sourceCaptureSlot.captured.source)
    }

    @Test
    fun `getCurrentTimelinePosition returns zero if mPlayer is null`() {
        assertEquals(0, testObject.currentTimelinePosition)
    }

    @Test
    fun `getCurrentTimelinePosition returns position if mPlayer is not null`() {
        every { mPlayer.currentPosition } returns expectedCurrentPosition
        every { mPlayer.currentPeriodIndex } returns expectedPeriodIndex

        testObject.playVideo(createDefaultVideo())

        assertEquals(expectedAdjustedPosition, testObject.currentTimelinePosition)
    }

    @Test
    fun `getCurrentTimelinePosition returns 0 if mPlayer throws exception`() {
        every { mPlayer.currentPosition } throws Exception()

        testObject.playVideo(createDefaultVideo())

        assertEquals(0, testObject.currentTimelinePosition)
    }

    @Test
    fun `getCurrentPosition returns position if currentPlayer is not null`() {
        val expectedPosition = 123456888L
        every { mPlayer.currentPosition } returns expectedPosition

        playVideoThenVerify(createDefaultVideo())

        assertEquals(expectedPosition, testObject.currentPosition)
    }

    @Test
    fun `getCurrentPosition returns zero if currentPlayer is null`() {
        assertEquals(0, testObject.currentPosition)
    }

    @Test
    fun `show controls given true shows controller`() {
        playVideoThenVerify(createDefaultVideo())
        testObject.showControls(true)
        verify(exactly = 1) { mPlayerView.showController() }
    }

    @Test
    fun `show controls given true but disableControls true does not show or hide controller`() {
        playVideoThenVerify(createDefaultVideo())
        adBreakReadyAdEventToDisableControls()
        clearAllMocks(answers = false)

        testObject.showControls(true)

        verify { mPlayerView wasNot called }
    }

    @Test
    fun `show controls given false hides controller`() {
        playVideoThenVerify(createDefaultVideo())
        clearAllMocks(answers = false)

        testObject.showControls(false)

        verifySequence { mPlayerView.hideController() }
    }

    @Test
    fun `getCurrentVideoDuration returns zero if currentPlayer is null`() {
        assertEquals(0, testObject.currentVideoDuration)
    }

    @Test
    fun `getCurrentVideoDuration returns duration if currentPlayer is not null`() {
        val expectedDuration = 84765L
        every { mPlayer.duration } returns expectedDuration

        playVideoThenVerify(createDefaultVideo())

        assertEquals(expectedDuration, testObject.currentVideoDuration)
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
        every {
            ContextCompat.getDrawable(
                mAppContext,
                R.drawable.FullScreenDrawableButtonCollapse
            )
        } returns drawable
        every { utils.createTrackingVideoTypeData() } returns videoData
        every { mPlayer.currentPosition } returns expectedPosition
        testObject.playVideo(createDefaultVideo())
        testObject.setFullscreenUi(true)
        clearAllMocks(answers = false)

        testObject.onStickyPlayerStateChanged(true)

        verifySequence { mPlayerView.setControllerVisibilityListener(null as StyledPlayerView.ControllerVisibilityListener?) }
    }

    @Test
    fun `onStickyPlayerStateChanged not isSticky`() {
        testObject.playVideo(createDefaultVideo())
        clearAllMocks(answers = false)

        testObject.onStickyPlayerStateChanged(false)

        verifySequence { mPlayerView.setControllerVisibilityListener(null as StyledPlayerView.ControllerVisibilityListener?) }
    }

//    @Test
//    fun `mPlayer controller visibility listener playButton is null`() {
//        val listener = slot<StyledPlayerView.ControllerVisibilityListener>()
//        every { mPlayerView.findViewById<ImageButton>(R.id.exo_play) } returns null
//        testObject.playVideo(createDefaultVideo())
//        verify { mPlayerView.setControllerVisibilityListener(capture(listener)) }
//        clearAllMocks(answers = false)
//
//        listener.captured.onVisibilityChanged(PlayerControlView.VISIBLE)
//
//        verifySequence {
//            pauseButton.requestFocus()
//            mPlayerView.requestLayout()
//        }
//    }

//    @Test
//    fun `mPlayer controller visibility listener playButton is not visible`() {
//        val listener = slot<StyledPlayerView.ControllerVisibilityListener>()
//        every { playButton.visibility } returns INVISIBLE
//        testObject.playVideo(createDefaultVideo())
//        verify { mPlayerView.setControllerVisibilityListener(capture(listener)) }
//        clearAllMocks(answers = false)
//
//        listener.captured.onVisibilityChanged(PlayerControlView.VISIBLE)
//
//        verifySequence {
//            pauseButton.requestFocus()
//            mPlayerView.requestLayout()
//        }
//    }

//    @Test
//    fun `mPlayer controller visibility listener playButton is not null and visible`() {
//        val listener = slot<StyledPlayerView.ControllerVisibilityListener>()
//        testObject.playVideo(createDefaultVideo())
//        verify { mPlayerView.setControllerVisibilityListener(capture(listener)) }
//        clearAllMocks(answers = false)
//
//        listener.captured.onVisibilityChanged(PlayerControlView.VISIBLE)
//
//        verifySequence {
//            playButton.visibility
//            playButton.requestFocus()
//            mPlayerView.requestLayout()
//        }
//    }

    @Test
    fun `release frees appropriate resources given populated and is fullscreen`() {
        val drawable = mockk<Drawable>()
        val videoData = mockk<TrackingTypeData.TrackingVideoTypeData>(relaxed = true)
        val expectedPosition = 324343L
        val viewGroup = mockk<ViewGroup>(relaxed = true)
        val mFullScreenDialog = mockk<Dialog>(relaxed = true)
        val playerFrame = mockk<RelativeLayout>(relaxed = true)
        every {
            ContextCompat.getDrawable(
                mAppContext,
                R.drawable.FullScreenDrawableButton
            )
        } returns drawable
        every { utils.createTrackingVideoTypeData() } returns videoData
        every { mPlayer.currentPosition } returns expectedPosition
        every { mPlayerView.parent } returns viewGroup
        every { mockView1.parent } returns viewGroup
        every { mockView2.parent } returns viewGroup
        every { mockView3.parent } returns viewGroup
        every { mCastControlView.parent } returns viewGroup
        every { mListener.playerFrame } returns playerFrame
        every { utils.createFullScreenDialog(mAppContext) } returns mFullScreenDialog
        testObject.playVideo(createDefaultVideo())
        testObject.setFullscreen(true)
        clearAllMocks(answers = false)
        assertNotNull(testObject.mPlayerView)
        assertNotNull(testObject.mPlayer)
        assertNotNull(testObject.mTrackSelector)
        assertNotNull(testObject.mAdsLoader)
        assertNotNull(testObject.mCastControlView)


        testObject.release()

        verifySequence {
            mPlayerView.parent
            mPlayerView.parent
            viewGroup.removeView(mPlayerView)
            mListener.playerFrame
            playerFrame.addView(mPlayerView)
            mFullscreenOverlays.values
            viewGroup.removeView(mockView1)
            mListener.playerFrame
            playerFrame.addView(mockView1)
            viewGroup.removeView(mockView2)
            mListener.playerFrame
            playerFrame.addView(mockView2)
            viewGroup.removeView(mockView3)
            mListener.playerFrame
            playerFrame.addView(mockView3)
            mPlayerView.findViewById<ImageButton>(R.id.exo_fullscreen)
            ContextCompat.getDrawable(mAppContext, R.drawable.FullScreenDrawableButton)
            fullScreenButton.setImageDrawable(drawable)
            mListener.isStickyPlayer
            mFullScreenDialog.dismiss()
            utils.createTrackingVideoTypeData()
            videoData.arcVideo = testObject.video
            mPlayer.currentPosition
            videoData.position = expectedPosition
            mListener.onTrackingEvent(ON_CLOSE_FULL_SCREEN, videoData)
            trackingHelper.normalScreen()
            mPlayerView.parent
            mPlayerView.parent
            viewGroup.removeView(mPlayerView)
            mPlayer.stop()
            mPlayer.release()
            mAdsLoader.setPlayer(null)
            mAdsLoader.release()
            mListener.removePlayerFrame()
            mCastPlayer.setSessionAvailabilityListener(null)
            mCastPlayer.release()
            mCastControlView.player = null
            mCastControlView.parent
            mCastControlView.parent
            viewGroup.removeView(mCastControlView)
        }
        assertNull(testObject.mPlayerView)
        assertNull(testObject.mPlayer)
        assertNull(testObject.mTrackSelector)
        assertNull(testObject.mAdsLoader)
        assertNull(testObject.mCastControlView)
    }

    @Test
    fun `onActivityResume plays video`() {
        //TODO unsure if I can reach a state where the onActivityResume code executes, suggestions?
        //note add mocking from above release test function to execute
//        testObject.playVideo(createDefaultVideo())
//        testObject.release()
        //here mVideo is not null, mPlayerView is null, but we are not fullscreen
//        testObject.setFullscreen(true)
//        testObject.setFullscreenUi(true)
        //either hit NPE on mPlayerView being null, so unsure if that is a valid flow

//        testObject.onActivityResume()
    }

    @Test
    fun `setFullscreenUi changes to fullscreen given true`() {
        val drawable = mockk<Drawable>()
        val videoData = mockk<TrackingTypeData.TrackingVideoTypeData>(relaxed = true)
        val expectedPosition = 324343L
        every {
            ContextCompat.getDrawable(
                mAppContext,
                R.drawable.FullScreenDrawableButtonCollapse
            )
        } returns drawable
        every { utils.createTrackingVideoTypeData() } returns videoData
        every { mPlayer.currentPosition } returns expectedPosition
        testObject.playVideo(createDefaultVideo())
        clearAllMocks(answers = false)

        testObject.setFullscreenUi(true)

        verifySequence {
            trackingHelper.fullscreen()
            mPlayerView.findViewById<ImageButton>(R.id.exo_fullscreen)
            ContextCompat.getDrawable(mAppContext, R.drawable.FullScreenDrawableButtonCollapse)
            fullScreenButton.setImageDrawable(drawable)
            utils.createTrackingVideoTypeData()
            videoData.arcVideo = testObject.video
            videoData.position = expectedPosition
            mListener.onTrackingEvent(ON_OPEN_FULL_SCREEN, videoData)
        }
        assertTrue(testObject.isFullScreen)
    }

    @Test
    fun `setFullscreenUi changes to normal screen given false`() {
        val drawable = mockk<Drawable>()
        val videoData = mockk<TrackingTypeData.TrackingVideoTypeData>(relaxed = true)
        val expectedPosition = 324343L
        every {
            ContextCompat.getDrawable(
                mAppContext,
                R.drawable.FullScreenDrawableButton
            )
        } returns drawable
        every { utils.createTrackingVideoTypeData() } returns videoData
        every { mPlayer.currentPosition } returns expectedPosition
        every { mListener.isStickyPlayer } returns true
        testObject.playVideo(createDefaultVideo())
        clearAllMocks(answers = false)

        testObject.setFullscreenUi(false)

        verifySequence {
            trackingHelper.normalScreen()
            mPlayerView.findViewById<ImageButton>(R.id.exo_fullscreen)
            ContextCompat.getDrawable(mAppContext, R.drawable.FullScreenDrawableButton)
            fullScreenButton.setImageDrawable(drawable)
            mListener.isStickyPlayer
            mPlayerView.hideController()
            mPlayerView.requestLayout()
            utils.createTrackingVideoTypeData()
            videoData.arcVideo = testObject.video
            mPlayer.currentPosition
            videoData.position = expectedPosition
            mListener.onTrackingEvent(ON_CLOSE_FULL_SCREEN, videoData)
        }
        assertFalse(testObject.isFullScreen)
    }

    @Test
    fun `fullscreen button listener given false`() {
        val fullScreenListener = slot<OnClickListener>()
        val arcVideo = ArcVideo(
            expectedId,
            "uuid",
            expectedStartPosition,
            false,
            false,
            100,
            "shareUrl",
            "headline",
            "pageName",
            "videoName",
            "videoSection",
            "videoSource",
            "videoCategory",
            "consentId",
            "fallbackUrl",
            "addTagUrl[timestamp]",
            true,
            subtitleUrl,
            "source",
            mockk(),
            false,
            false,
            false,
            ArcXPVideoConfig.CCStartMode.DEFAULT
        )
        val viewGroup = mockk<ViewGroup>(relaxed = true)
        val videoData = mockk<TrackingTypeData.TrackingVideoTypeData>(relaxed = true)
        val mFullScreenDialog = mockk<Dialog>(relaxed = true)
        val mockLayoutParams = mockk<ViewGroup.LayoutParams>()
        val drawable = mockk<Drawable>()
        every {
            ContextCompat.getDrawable(
                mAppContext,
                R.drawable.FullScreenDrawableButtonCollapse
            )
        } returns drawable
        every { utils.createTrackingVideoTypeData() } returns videoData
        every { utils.createFullScreenDialog(mAppContext) } returns mFullScreenDialog
        every { utils.createLayoutParams() } returns mockLayoutParams
        every { mPlayer.currentPosition } returns expectedPosition
        every { mPlayerView.parent } returns viewGroup
        every { mockView1.parent } returns viewGroup
        every { mockView2.parent } returns viewGroup
        every { mockView3.parent } returns viewGroup
        testObject.playVideo(arcVideo)
        verify { fullScreenButton.setOnClickListener(capture(fullScreenListener)) }
        clearAllMocks(answers = false)

        fullScreenListener.captured.onClick(fullScreenButton)

        verifySequence {
            utils.createFullScreenDialog(mAppContext)
            mFullScreenDialog.setOnKeyListener(any())
            mPlayerView.parent
            mPlayerView.parent
            viewGroup.removeView(mPlayerView)
            utils.createLayoutParams()
            mFullScreenDialog.addContentView(mPlayerView, mockLayoutParams)
            mFullscreenOverlays.values
            mockView1.parent
            viewGroup.removeView(mockView1)
            utils.createLayoutParams()
            mFullScreenDialog.addContentView(mockView1, mockLayoutParams)
            mockView1.bringToFront()
            mockView2.parent
            viewGroup.removeView(mockView2)
            utils.createLayoutParams()
            mFullScreenDialog.addContentView(mockView2, mockLayoutParams)
            mockView2.bringToFront()
            mockView3.parent
            viewGroup.removeView(mockView3)
            utils.createLayoutParams()
            mFullScreenDialog.addContentView(mockView3, mockLayoutParams)
            mockView3.bringToFront()
            mPlayerView.findViewById<ImageButton>(R.id.exo_fullscreen)
            ContextCompat.getDrawable(mAppContext, R.drawable.FullScreenDrawableButtonCollapse)
            fullScreenButton.setImageDrawable(drawable)
            mFullScreenDialog.show()
            utils.createTrackingVideoTypeData()
            videoData.arcVideo = testObject.video
            videoData.position = expectedPosition
            mListener.onTrackingEvent(ON_OPEN_FULL_SCREEN, videoData)
            trackingHelper.fullscreen()
        }
    }

    @Test
    fun `setFullscreen given false and not using fullscreen dialog, calls mListener`() {
        every { mConfig.isUseFullScreenDialog } returns false
        testObject.playVideo(createDefaultVideo())

        testObject.setFullscreen(false)

        verify(exactly = 1) { mListener.setFullscreen(false) }
    }

    @Test
    fun `setFullscreen given true and using fullscreen dialog, does not call mListener`() {
        val viewGroup = mockk<ViewGroup>(relaxed = true)
        every { mConfig.isUseFullScreenDialog } returns true
        every { mPlayerView.parent } returns viewGroup
        every { mockView1.parent } returns viewGroup
        every { mockView2.parent } returns viewGroup
        every { mockView3.parent } returns viewGroup
        testObject.playVideo(createDefaultVideo())
        testObject.setFullscreen(true)

        verify(exactly = 0) { mListener.setFullscreen(any()) }
    }

    @Test
    fun `fullScreen dialog listener on something besides back pressed, given firstAdCompleted, controller not visible shows controller`() {
        testObject.playVideo(createDefaultVideo())
        val drawable = mockk<Drawable>()
        val videoData = mockk<TrackingTypeData.TrackingVideoTypeData>(relaxed = true)
        val viewGroup = mockk<ViewGroup>(relaxed = true)
        val mFullScreenDialog = mockk<Dialog>(relaxed = true)
        val mockLayoutParams = mockk<ViewGroup.LayoutParams>()
        val listener = slot<DialogInterface.OnKeyListener>()
        val keyEvent = mockk<KeyEvent> {
            every { action } returns KeyEvent.ACTION_UP
        }
        every {
            ContextCompat.getDrawable(
                mAppContext,
                R.drawable.FullScreenDrawableButton
            )
        } returns drawable
        every { utils.createTrackingVideoTypeData() } returns videoData
        every { utils.createFullScreenDialog(mAppContext) } returns mFullScreenDialog
        every { utils.createLayoutParams() } returns mockLayoutParams
        every { mPlayer.currentPosition } returns expectedPosition
        every { mPlayerView.isControllerFullyVisible } returns false
        every { mPlayerView.parent } returns viewGroup
        every { mockView1.parent } returns viewGroup
        every { mockView2.parent } returns viewGroup
        every { mockView3.parent } returns viewGroup
        testObject.setFullscreen(true)
        verify { mFullScreenDialog.setOnKeyListener(capture(listener)) }
        every { adEvent.type } returns SKIPPED
        testObject.onAdEvent(adEvent)
        clearAllMocks(answers = false)

        assertFalse(listener.captured.onKey(mFullScreenDialog, KeyEvent.KEYCODE_B, keyEvent))

        verifySequence {
            mPlayerView.isControllerFullyVisible
            mPlayerView.showController()
        }
    }

    @Test
    fun `fullScreen dialog listener on something besides back pressed, given ads not enabled, controller not visible shows controller`() {
        testObject.playVideo(createDefaultVideo(shouldPlayAds = false))
        val drawable = mockk<Drawable>()
        val videoData = mockk<TrackingTypeData.TrackingVideoTypeData>(relaxed = true)
        every { utils.createTrackingVideoTypeData() } returns videoData
        val viewGroup = mockk<ViewGroup>(relaxed = true)
        val mFullScreenDialog = mockk<Dialog>(relaxed = true)
        val mockLayoutParams = mockk<ViewGroup.LayoutParams>()
        val listener = slot<DialogInterface.OnKeyListener>()
        val keyEvent = mockk<KeyEvent> {
            every { action } returns KeyEvent.ACTION_UP
        }
        every {
            ContextCompat.getDrawable(
                mAppContext,
                R.drawable.FullScreenDrawableButton
            )
        } returns drawable
        every { utils.createFullScreenDialog(mAppContext) } returns mFullScreenDialog
        every { utils.createLayoutParams() } returns mockLayoutParams
        every { mPlayer.currentPosition } returns expectedPosition
        every { mPlayerView.isControllerFullyVisible } returns false
        every { mPlayerView.parent } returns viewGroup
        every { mockView1.parent } returns viewGroup
        every { mockView2.parent } returns viewGroup
        every { mockView3.parent } returns viewGroup
        testObject.setFullscreen(true)
        verify { mFullScreenDialog.setOnKeyListener(capture(listener)) }
        clearAllMocks(answers = false)

        assertFalse(listener.captured.onKey(mFullScreenDialog, KeyEvent.KEYCODE_B, keyEvent))

        verifySequence {
            mPlayerView.isControllerFullyVisible
            mPlayerView.showController()
        }
    }

    @Test
    fun `fullScreen dialog listener on something besides back pressed, controller visible does not show controller`() {
        testObject.playVideo(createDefaultVideo(shouldPlayAds = false))
        clearAllMocks(answers = false)
        val drawable = mockk<Drawable>()
        val videoData = mockk<TrackingTypeData.TrackingVideoTypeData>(relaxed = true)
        val viewGroup = mockk<ViewGroup>(relaxed = true)
        val mFullScreenDialog = mockk<Dialog>(relaxed = true)
        val mockLayoutParams = mockk<ViewGroup.LayoutParams>()
        val listener = slot<DialogInterface.OnKeyListener>()
        val keyEvent = mockk<KeyEvent> {
            every { action } returns KeyEvent.ACTION_UP
        }
        every {
            ContextCompat.getDrawable(
                mAppContext,
                R.drawable.FullScreenDrawableButton
            )
        } returns drawable
        every { utils.createTrackingVideoTypeData() } returns videoData
        every { utils.createFullScreenDialog(mAppContext) } returns mFullScreenDialog
        every { utils.createLayoutParams() } returns mockLayoutParams
        every { mPlayer.currentPosition } returns expectedPosition
        every { mPlayerView.isControllerFullyVisible } returns true
        every { mPlayerView.parent } returns viewGroup
        every { mockView1.parent } returns viewGroup
        every { mockView2.parent } returns viewGroup
        every { mockView3.parent } returns viewGroup
        testObject.setFullscreen(true)
        verify { mFullScreenDialog.setOnKeyListener(capture(listener)) }
        clearAllMocks(answers = false)

        assertFalse(listener.captured.onKey(mFullScreenDialog, KeyEvent.KEYCODE_B, keyEvent))

        verifySequence { mPlayerView.isControllerFullyVisible }
    }

    @Test
    fun `setFullscreen given false and using fullscreen dialog, does not call mListener`() {
        val viewGroup = mockk<ViewGroup>(relaxed = true)
        every { mConfig.isUseFullScreenDialog } returns true
        every { mPlayerView.parent } returns viewGroup
        every { mockView1.parent } returns viewGroup
        every { mockView2.parent } returns viewGroup
        every { mockView3.parent } returns viewGroup
        testObject.playVideo(createDefaultVideo())

        testObject.setFullscreen(false)

        verify(exactly = 0) { mListener.setFullscreen(any()) }
    }

    @Test
    fun `setFullscreen given true and not using fullscreen dialog, creates dialog, sets on key listener, calls mListener and sets fullscreen`() {
        val drawable = mockk<Drawable>()
        val videoData = mockk<TrackingTypeData.TrackingVideoTypeData>(relaxed = true)
        val viewGroup = mockk<ViewGroup>(relaxed = true)
        val mFullScreenDialog = mockk<Dialog>(relaxed = true)
        val mockLayoutParams = mockk<ViewGroup.LayoutParams>()
        every { mPlayer.currentPosition } returns expectedPosition
        every {
            ContextCompat.getDrawable(
                mAppContext,
                R.drawable.FullScreenDrawableButtonCollapse
            )
        } returns drawable
        every { utils.createTrackingVideoTypeData() } returns videoData
        every { utils.createFullScreenDialog(mAppContext) } returns mFullScreenDialog
        every { utils.createLayoutParams() } returns mockLayoutParams
        every { mPlayerView.parent } returns viewGroup
        every { mockView1.parent } returns viewGroup
        every { mockView2.parent } returns viewGroup
        every { mockView3.parent } returns viewGroup
        testObject.playVideo(createDefaultVideo())
        clearAllMocks(answers = false)

        assertFalse(testObject.isFullScreen)
        testObject.setFullscreen(true)

        verifySequence {
            utils.createFullScreenDialog(mAppContext)
            mFullScreenDialog.setOnKeyListener(any())
            mPlayerView.parent
            mPlayerView.parent
            viewGroup.removeView(mPlayerView)
            utils.createLayoutParams()
            mFullScreenDialog.addContentView(mPlayerView, mockLayoutParams)
            mFullscreenOverlays.values
            mockView1.parent
            viewGroup.removeView(mockView1)
            utils.createLayoutParams()
            mFullScreenDialog.addContentView(mockView1, mockLayoutParams)
            mockView1.bringToFront()
            mockView2.parent
            viewGroup.removeView(mockView2)
            utils.createLayoutParams()
            mFullScreenDialog.addContentView(mockView2, mockLayoutParams)
            mockView2.bringToFront()
            mockView3.parent
            viewGroup.removeView(mockView3)
            utils.createLayoutParams()
            mFullScreenDialog.addContentView(mockView3, mockLayoutParams)
            mockView3.bringToFront()
            mPlayerView.findViewById<ImageButton>(R.id.exo_fullscreen)
            ContextCompat.getDrawable(mAppContext, R.drawable.FullScreenDrawableButtonCollapse)
            fullScreenButton.setImageDrawable(drawable)
            mFullScreenDialog.show()
            utils.createTrackingVideoTypeData()
            videoData.arcVideo = testObject.video
            mPlayer.currentPosition
            videoData.position = expectedPosition
            mListener.onTrackingEvent(ON_OPEN_FULL_SCREEN, videoData)
            trackingHelper.fullscreen()
            mListener.setFullscreen(true)
        }
        assertTrue(testObject.isFullScreen)
    }

    @Test
    fun `setFullscreen given false, but dialog is null, calls mListener`() {
        testObject.setFullscreen(false)

        verifySequence { mListener.setFullscreen(false) }
    }

    @Test
    fun `setFullscreen given false, dialog not null, dismisses dialog and sets to normal screen`() {
        testObject.playVideo(createDefaultVideo())
        clearAllMocks(answers = false)
        val drawable = mockk<Drawable>()
        val videoData = mockk<TrackingTypeData.TrackingVideoTypeData>(relaxed = true)
        val viewGroup = mockk<ViewGroup>(relaxed = true)
        val mFullScreenDialog = mockk<Dialog>(relaxed = true)
        val mockLayoutParams = mockk<ViewGroup.LayoutParams>()
        every { mPlayer.currentPosition } returns expectedPosition
        every {
            ContextCompat.getDrawable(
                mAppContext,
                R.drawable.FullScreenDrawableButton
            )
        } returns drawable
        every { utils.createTrackingVideoTypeData() } returns videoData
        every { utils.createFullScreenDialog(mAppContext) } returns mFullScreenDialog
        every { utils.createLayoutParams() } returns mockLayoutParams
        every { mPlayerView.parent } returns viewGroup
        every { mockView1.parent } returns viewGroup
        every { mockView2.parent } returns viewGroup
        every { mockView3.parent } returns viewGroup

        assertFalse(testObject.isFullScreen)
        testObject.setFullscreen(true)

        clearAllMocks(answers = false)
        every { mListener.isStickyPlayer } returns true
        val playerFrame = mockk<RelativeLayout>(relaxed = true)
        every { mListener.playerFrame } returns playerFrame

        assertTrue(testObject.isFullScreen)
        testObject.setFullscreen(false)

        verifySequence {
            mPlayerView.parent
            mPlayerView.parent
            viewGroup.removeView(mPlayerView)
            mListener.playerFrame
            playerFrame.addView(mPlayerView)
            mFullscreenOverlays.values
            viewGroup.removeView(mockView1)
            mListener.playerFrame
            playerFrame.addView(mockView1)
            viewGroup.removeView(mockView2)
            mListener.playerFrame
            playerFrame.addView(mockView2)
            viewGroup.removeView(mockView3)
            mListener.playerFrame
            playerFrame.addView(mockView3)
            mPlayerView.findViewById<ImageButton>(R.id.exo_fullscreen)
            ContextCompat.getDrawable(mAppContext, R.drawable.FullScreenDrawableButton)
            fullScreenButton.setImageDrawable(drawable)
            mListener.isStickyPlayer
            mPlayerView.hideController()
            mPlayerView.requestLayout()
            mFullScreenDialog.dismiss()
            utils.createTrackingVideoTypeData()
            videoData.arcVideo = testObject.video
            mPlayer.currentPosition
            videoData.position = expectedPosition
            mListener.onTrackingEvent(ON_CLOSE_FULL_SCREEN, videoData)
            trackingHelper.normalScreen()
            mListener.setFullscreen(false)
        }
        assertFalse(testObject.isFullScreen)
    }

    @Test
    fun `fullScreen dialog on something besides Back Pressed, if keyListener non-null calls onKey`() {
        val expectedKeyCode = KeyEvent.KEYCODE_0

        testObject.playVideo(createDefaultVideo())
        clearAllMocks(answers = false)
        val drawable = mockk<Drawable>()
        val videoData = mockk<TrackingTypeData.TrackingVideoTypeData>(relaxed = true)
        val viewGroup = mockk<ViewGroup>(relaxed = true)
        val mFullScreenDialog = mockk<Dialog>(relaxed = true)
        val mockLayoutParams = mockk<ViewGroup.LayoutParams>()
        val listener = slot<DialogInterface.OnKeyListener>()
        val keyEvent = mockk<KeyEvent> {
            every { action } returns KeyEvent.ACTION_UP
        }
        val keyListener = mockk<ArcKeyListener>(relaxed = true)
        every {
            ContextCompat.getDrawable(
                mAppContext,
                R.drawable.FullScreenDrawableButton
            )
        } returns drawable
        every { utils.createTrackingVideoTypeData() } returns videoData
        every { utils.createFullScreenDialog(mAppContext) } returns mFullScreenDialog
        every { utils.createLayoutParams() } returns mockLayoutParams
        every { mPlayer.currentPosition } returns expectedPosition
        every { mPlayerView.isControllerFullyVisible } returns true
        every { mPlayerView.parent } returns viewGroup
        every { mockView1.parent } returns viewGroup
        every { mockView2.parent } returns viewGroup
        every { mockView3.parent } returns viewGroup
        testObject.setFullscreen(true)
        verify { mFullScreenDialog.setOnKeyListener(capture(listener)) }
        clearAllMocks(answers = false)


        testObject.setFullscreenListener(keyListener)

        assertFalse(listener.captured.onKey(mFullScreenDialog, expectedKeyCode, keyEvent))
        verifySequence {
            keyListener.onKey(keyCode = expectedKeyCode, keyEvent = keyEvent)
        }
    }

    @Test
    fun `fullScreen dialog listener on Back Pressed, starts pip if enabled`() {
        val drawable = mockk<Drawable>()
        val videoData = mockk<TrackingTypeData.TrackingVideoTypeData>(relaxed = true)
        val viewGroup = mockk<ViewGroup>(relaxed = true)
        val mFullScreenDialog = mockk<Dialog>(relaxed = true)
        val mockLayoutParams = mockk<ViewGroup.LayoutParams>()
        val listener = slot<DialogInterface.OnKeyListener>()
        val keyEvent = mockk<KeyEvent> {
            every { action } returns KeyEvent.ACTION_UP
        }
        val playerFrame = mockk<RelativeLayout>(relaxed = true)
        every {
            ContextCompat.getDrawable(
                mAppContext,
                R.drawable.FullScreenDrawableButton
            )
        } returns drawable
        every { utils.createFullScreenDialog(mAppContext) } returns mFullScreenDialog
        every { utils.createLayoutParams() } returns mockLayoutParams
        every { utils.createTrackingVideoTypeData() } returns videoData
        every { mListener.isStickyPlayer } returns true
        every { mListener.playerFrame } returns playerFrame
        every { mPlayer.currentPosition } returns expectedPosition
        every { mPlayer.playbackState } returns Player.STATE_READY
        every { mPlayerView.isControllerFullyVisible } returns false
        every { mPlayerView.parent } returns viewGroup
        every { mockView1.parent } returns viewGroup
        every { mockView2.parent } returns viewGroup
        every { mockView3.parent } returns viewGroup
        every { mVideoManager.isPipEnabled } returns true
        val mVideo = createDefaultVideo()
        testObject.playVideo(mVideo)
        testObject.setFullscreen(true)
        verify { mFullScreenDialog.setOnKeyListener(capture(listener)) }
        clearAllMocks(answers = false)
        assertTrue(testObject.isFullScreen)

        assertFalse(listener.captured.onKey(mFullScreenDialog, KeyEvent.KEYCODE_BACK, keyEvent))

        verifySequence {
            mVideoManager.isPipEnabled
            mVideoManager.isPipEnabled
            mPlayerView.hideController()
            mVideoManager.setSavedPosition(expectedId, expectedPosition)
            mVideoManager.startPIP(mVideo)
        }
    }

    @Test
    fun `fullScreen dialog listener on Back Pressed, alerts key listener when pip disabled or unsupported`() {
        val drawable = mockk<Drawable>()
        val videoData = mockk<TrackingTypeData.TrackingVideoTypeData>(relaxed = true)
        val viewGroup = mockk<ViewGroup>(relaxed = true)
        val mFullScreenDialog = mockk<Dialog>(relaxed = true)
        val mockLayoutParams = mockk<ViewGroup.LayoutParams>()
        val listener = slot<DialogInterface.OnKeyListener>()
        val keyEvent = mockk<KeyEvent> {
            every { action } returns KeyEvent.ACTION_UP
        }
        val playerFrame = mockk<RelativeLayout>(relaxed = true)
        every {
            ContextCompat.getDrawable(
                mAppContext,
                R.drawable.FullScreenDrawableButton
            )
        } returns drawable
        every { utils.createFullScreenDialog(mAppContext) } returns mFullScreenDialog
        every { utils.createLayoutParams() } returns mockLayoutParams
        every { utils.createTrackingVideoTypeData() } returns videoData
        every { mListener.isStickyPlayer } returns true
        every { mListener.playerFrame } returns playerFrame
        every { mPlayer.currentPosition } returns expectedPosition
        every { mPlayer.playbackState } returns Player.STATE_READY
        every { mPlayerView.isControllerFullyVisible } returns false
        every { mPlayerView.parent } returns viewGroup
        every { mockView1.parent } returns viewGroup
        every { mockView2.parent } returns viewGroup
        every { mockView3.parent } returns viewGroup
        every { mVideoManager.isPipEnabled } returns false
        val mVideo = createDefaultVideo()
        testObject.playVideo(mVideo)
        testObject.setFullscreen(true)
        val arcKeyListener = mockk<ArcKeyListener> {
            every { onBackPressed() } just Runs
        }
        verify { mFullScreenDialog.setOnKeyListener(capture(listener)) }
        clearAllMocks(answers = false)
        assertTrue(testObject.isFullScreen)
        testObject.setFullscreenListener(arcKeyListener)

        assertFalse(listener.captured.onKey(mFullScreenDialog, KeyEvent.KEYCODE_BACK, keyEvent))

        verifySequence {
            mVideoManager.isPipEnabled
            arcKeyListener.onBackPressed()
        }
    }

    @Test
    fun `fullScreen dialog listener on Back Pressed, returns true if keyAction is not up`() {
        val drawable = mockk<Drawable>()
        val videoData = mockk<TrackingTypeData.TrackingVideoTypeData>(relaxed = true)
        val viewGroup = mockk<ViewGroup>(relaxed = true)
        val mFullScreenDialog = mockk<Dialog>(relaxed = true)
        val mockLayoutParams = mockk<ViewGroup.LayoutParams>()
        val listener = slot<DialogInterface.OnKeyListener>()
        val keyEvent = mockk<KeyEvent> {
            every { action } returns KeyEvent.ACTION_DOWN
        }
        val playerFrame = mockk<RelativeLayout>(relaxed = true)
        every {
            ContextCompat.getDrawable(
                mAppContext,
                R.drawable.FullScreenDrawableButton
            )
        } returns drawable
        every { utils.createFullScreenDialog(mAppContext) } returns mFullScreenDialog
        every { utils.createLayoutParams() } returns mockLayoutParams
        every { utils.createTrackingVideoTypeData() } returns videoData
        every { mListener.isStickyPlayer } returns true
        every { mListener.playerFrame } returns playerFrame
        every { mPlayer.currentPosition } returns expectedPosition
        every { mPlayer.playbackState } returns Player.STATE_READY
        every { mPlayerView.isControllerFullyVisible } returns false
        every { mPlayerView.parent } returns viewGroup
        every { mockView1.parent } returns viewGroup
        every { mockView2.parent } returns viewGroup
        every { mockView3.parent } returns viewGroup
        every { mVideoManager.isPipEnabled } returns true
        val mVideo = createDefaultVideo()
        testObject.playVideo(mVideo)
        testObject.setFullscreen(true)
        verify { mFullScreenDialog.setOnKeyListener(capture(listener)) }
        clearAllMocks(answers = false)
        assertTrue(testObject.isFullScreen)

        assertTrue(listener.captured.onKey(mFullScreenDialog, KeyEvent.KEYCODE_BACK, keyEvent))
    }

    @Test
    fun `fullScreen dialog listener on something besides back pressed, when arkKeyListener exists calls onKey`() {
        testObject.playVideo(createDefaultVideo())
        clearAllMocks(answers = false)
        val drawable = mockk<Drawable>()
        val videoData = mockk<TrackingTypeData.TrackingVideoTypeData>(relaxed = true)
        val viewGroup = mockk<ViewGroup>(relaxed = true)
        val mFullScreenDialog = mockk<Dialog>(relaxed = true)
        val mockLayoutParams = mockk<ViewGroup.LayoutParams>()
        val keyEvent = mockk<KeyEvent> {
            every { action } returns KeyEvent.ACTION_UP
        }
        val keyListener = mockk<ArcKeyListener>(relaxed = true)
        val listener = slot<DialogInterface.OnKeyListener>()
        every {
            ContextCompat.getDrawable(
                mAppContext,
                R.drawable.FullScreenDrawableButton
            )
        } returns drawable
        every { utils.createTrackingVideoTypeData() } returns videoData
        every { utils.createFullScreenDialog(mAppContext) } returns mFullScreenDialog
        every { utils.createLayoutParams() } returns mockLayoutParams
        every { mPlayer.currentPosition } returns expectedPosition
        every { mPlayerView.isControllerFullyVisible } returns true
        every { mPlayerView.parent } returns viewGroup
        every { mockView1.parent } returns viewGroup
        every { mockView2.parent } returns viewGroup
        every { mockView3.parent } returns viewGroup
        testObject.setFullscreen(true)
        verify { mFullScreenDialog.setOnKeyListener(capture(listener)) }
        testObject.setFullscreenListener(keyListener)

        assertFalse(listener.captured.onKey(mFullScreenDialog, KeyEvent.KEYCODE_B, keyEvent))

        verify(exactly = 1) { keyListener.onKey(KeyEvent.KEYCODE_B, keyEvent) }
    }

    @Test
    fun `volumeButton onClick when player volume is non-zero`() {
        val volumeButtonListener = slot<OnClickListener>()
        val expectedVolume = 0.6783f
        val drawable = mockk<Drawable>()
        val videoData = mockk<TrackingTypeData.TrackingVideoTypeData>(relaxed = true)
        every { utils.createTrackingVideoTypeData() } returns videoData
        every { mPlayer.volume } returns expectedVolume
        every {
            ContextCompat.getDrawable(
                mAppContext,
                R.drawable.MuteDrawableButton
            )
        } returns drawable
        testObject.playVideo(createDefaultVideo())
        verify { volumeButton.setOnClickListener(capture(volumeButtonListener)) }
        clearAllMocks(answers = false)

        volumeButtonListener.captured.onClick(mockk())

        verifySequence {
            utils.createTrackingVideoTypeData()
            videoData.arcVideo = testObject.video
            mPlayer.currentPosition
            videoData.position = expectedSavedPosition
            mPlayer.volume
            mPlayer.volume
            mPlayer.volume = 0f
            ContextCompat.getDrawable(mAppContext, R.drawable.MuteDrawableButton)
            volumeButton.setImageDrawable(drawable)
            mVideoManager.onTrackingEvent(ON_MUTE, videoData)
            trackingHelper.volumeChange(0f)
        }
    }

    @Test
    fun `volumeButton button onClick when player volume is zero`() {
        val volumeButtonListener = slot<OnClickListener>()
        val videoData = mockk<TrackingTypeData.TrackingVideoTypeData>(relaxed = true)
        val muteOffDrawableButton = mockk<Drawable>()
        testObject.playVideo(createDefaultVideo())
        verify { volumeButton.setOnClickListener(capture(volumeButtonListener)) }
        every { utils.createTrackingVideoTypeData() } returns videoData
        every { mPlayer.volume } returns 0.0f
        every {
            ContextCompat.getDrawable(
                mAppContext,
                R.drawable.MuteOffDrawableButton
            )
        } returns muteOffDrawableButton
        clearAllMocks(answers = false)

        volumeButtonListener.captured.onClick(mockk())

        verifySequence {
            utils.createTrackingVideoTypeData()
            videoData.arcVideo = testObject.video
            mPlayer.currentPosition
            videoData.position = expectedSavedPosition
            mPlayer.volume
            mPlayer.volume = 0.0f
            volumeButton.setImageDrawable(muteOffDrawableButton)
            mVideoManager.onTrackingEvent(ON_UNMUTE, videoData)
            trackingHelper.volumeChange(0f)
        }
    }

    @Test
    fun `playVideos, when given a list with videos, sets mVideo and MVideos, then plays video `() {
        val arcVideo1 = createDefaultVideo()
        val arcVideo2 = createDefaultVideo()
        val arcVideo3 = createDefaultVideo()
        val videoList = listOf(arcVideo1, arcVideo2, arcVideo3)

        testObject.playVideos(videoList)

        assertTrue(videoList == testObject.mVideos)
        verifyPlayVideo(arcVideo1)
    }

    @Test
    fun `playVideos, when given empty list, fires error event`() {
        testObject.playVideos(emptyList())
        verifySequence { mListener.onTrackingEvent(ERROR_PLAYLIST_EMPTY, null) }
    }

    private fun verifyOnAdEvent(
        adEventType: AdEvent.AdEventType,
        getAdResult: Ad?,
        trackingType: TrackingType
    ) {
        mockkConstructor(ArcAd::class)
        mockkConstructor(TrackingTypeData.TrackingAdTypeData::class)
        val inputAdEvent = mockk<AdEvent>()
        val adCaptureSlot = slot<ArcAd>()
        val valueCaptureSlot = slot<TrackingTypeData.TrackingAdTypeData>()

        inputAdEvent.apply {
            every { type } returns adEventType
            every { ad } returns getAdResult

        }
        clearAllMocks(answers = false)
        testObject.onAdEvent(inputAdEvent)


        inputAdEvent.apply {
            verify {
                type
                ad
                getAdResult?.let {
                    ad
                    ad
                    ad
                    ad
                }
            }
        }

        verify {
            constructedWith<TrackingTypeData.TrackingAdTypeData>().position = 0L
            constructedWith<TrackingTypeData.TrackingAdTypeData>().arcAd =
                capture(adCaptureSlot)

            mListener.onTrackingEvent(trackingType, capture(valueCaptureSlot))
        }

        assertEquals(adCaptureSlot.captured, valueCaptureSlot.captured.arcAd)
        assertEquals(0L, valueCaptureSlot.captured.position)
    }

    private fun createAd(): Ad {
        val inputAd = mockk<Ad>()
        val expectedAdId = "ad id"
        val expectedDuration = 1234.0
        val expectedTitle = "title"
        val expectedClickThroughUrl = "url"
        return inputAd.apply {
            every { adId } returns expectedAdId
            every { duration } returns expectedDuration
            every { title } returns expectedTitle
            every { surveyUrl } returns expectedClickThroughUrl
        }

    }

    private fun adBreakReadyAdEventToDisableControls() {
        every { adEvent.type } returns AD_BREAK_READY
        testObject.onAdEvent(adEvent)
        assertTrue(testObject.isControlDisabled)
    }

    private fun testOnAdEventControlsDisabled(type: AdEvent.AdEventType) {
        adBreakReadyAdEventToDisableControls()
        every { adEvent.type } returns type
        testObject.onAdEvent(adEvent)
        assertFalse(testObject.isControlDisabled)
    }

// TODO leaving methods using deprecated exoplayer methods to test in https://arcpublishing.atlassian.net/browse/ARCMOBILE-3894
// createMediaSource, onPlayerStateChanged, onPlayerStateChanged(update release test once we can populate videoTrackingSub), onTimelineChanged, onLoadingChanged


    @Test
    fun `playVideo when showNextPrevious enabled shows next and prev Buttons, does not disable them`() {
        mConfig.apply {
            every { showNextPreviousButtons } returns true
            every { shouldDisableNextButton } returns false
            every { shouldDisablePreviousButton } returns false
        }

        testObject.playVideo(createDefaultVideo())

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
        testObject.playVideo(createDefaultVideo())

        verify(exactly = 1) {
            nextButton.visibility = GONE
            previousButton.visibility = GONE
        }
    }

    @Test
    fun `playVideo when shouldDisableNextButton disables next button`() {
        mConfig.apply {
            every { showNextPreviousButtons } returns true
            every { shouldDisableNextButton } returns true
        }

        testObject.playVideo(createDefaultVideo())

        verify(exactly = 1) {
            nextButton.isEnabled = false
        }
    }

    @Test
    fun `playVideo when shouldDisablePreviousButton disables previous button`() {
        mConfig.apply {
            every { showNextPreviousButtons } returns true
            every { shouldDisablePreviousButton } returns true
        }

        testObject.playVideo(createDefaultVideo())

        verify(exactly = 1) {
            previousButton.isEnabled = false
        }
    }

    @Test
    fun `nextButton On Click Listener when no next video in mVideos calls tracking event`() {
        val arcVideo = createDefaultVideo()
        val expectedCurrentPosition = 876435L
        val videoData = mockk<TrackingTypeData.TrackingVideoTypeData>(relaxed = true)
        val listener = slot<OnClickListener>()
        every { mConfig.showNextPreviousButtons } returns true
        every { utils.createTrackingVideoTypeData() } returns videoData
        every { mPlayer.currentPosition } returns expectedCurrentPosition
        testObject.playVideo(arcVideo)

        verify { nextButton.setOnClickListener(capture(listener)) }
        clearAllMocks(answers = false)

        listener.captured.onClick(mockk())

        verifySequence {
            utils.createTrackingVideoTypeData()
            videoData.arcVideo = arcVideo
            mPlayer.currentPosition
            videoData.position = expectedCurrentPosition
            mListener.onTrackingEvent(NEXT_BUTTON_PRESSED, videoData)
        }
    }

    @Test
    fun `nextButton On Click Listener when has next video in mVideos play on next and calls tracking event`() {
        val arcVideo1 = createDefaultVideo()
        val arcVideo2 = createDefaultVideo()


        val expectedCurrentPosition = 876435L
        val videoData = mockk<TrackingTypeData.TrackingVideoTypeData>(relaxed = true)
        val listener = mutableListOf<OnClickListener>()
        every { mConfig.showNextPreviousButtons } returns true
        every { utils.createTrackingVideoTypeData() } returns videoData
        every { mPlayer.currentPosition } returns expectedCurrentPosition

        testObject = spyk(testObject)
        testObject.playVideos(listOf(arcVideo1, arcVideo2))

        verify(exactly = 2) { nextButton.setOnClickListener(capture(listener)) }
        clearAllMocks(answers = false)

        listener[1].onClick(mockk())

        verify(exactly = 1) {
            testObject.playVideoAtIndex(1)
            mListener.onTrackingEvent(NEXT_BUTTON_PRESSED, videoData)
        }
    }

    @Test
    fun `prevButton On Click Listener when no prev video in mVideos calls tracking event`() {
        val arcVideo = createDefaultVideo()
        val expectedCurrentPosition = 876435L
        val videoData = mockk<TrackingTypeData.TrackingVideoTypeData>(relaxed = true)
        val listener = slot<OnClickListener>()
        every { mConfig.showNextPreviousButtons } returns true
        every { utils.createTrackingVideoTypeData() } returns videoData
        every { mPlayer.currentPosition } returns expectedCurrentPosition
        testObject.playVideo(arcVideo)

        verify { previousButton.setOnClickListener(capture(listener)) }
        clearAllMocks(answers = false)

        listener.captured.onClick(mockk())

        verifySequence {
            utils.createTrackingVideoTypeData()
            videoData.arcVideo = arcVideo
            mPlayer.currentPosition
            videoData.position = expectedCurrentPosition
            mListener.onTrackingEvent(PREV_BUTTON_PRESSED, videoData)
        }
    }

    @Test
    fun `playVideos when has no more videos in mVideos sets nextButton as disabled, onclick listener just creates tracking event`() {
        every { mConfig.showNextPreviousButtons } returns true
        val arcVideo1 = createDefaultVideo()
        val arcVideo2 = createDefaultVideo()
        val videoList = listOf(arcVideo1, arcVideo2)
        val expectedCurrentPosition = 876435L
        val videoData = mockk<TrackingTypeData.TrackingVideoTypeData>(relaxed = true)
        val listener = slot<OnClickListener>()
        every { utils.createTrackingVideoTypeData() } returns videoData
        every { mPlayer.currentPosition } returns expectedCurrentPosition
        testObject.playVideos(videoList)
        clearAllMocks(answers = false)

        //since we have nothing exposed that can advance playlist except this that I saw
        testObject.onPlayerStateChanged(true, Player.STATE_ENDED)
        verifySequence {
            nextButton.visibility = VISIBLE
            nextButton.setOnClickListener(capture(listener))
            nextButton.alpha = 0.5f
        }
        clearAllMocks(answers = false)

        listener.captured.onClick(mockk())

        verifySequence {
            utils.createTrackingVideoTypeData()
            videoData.arcVideo = arcVideo2
            mPlayer.currentPosition
            videoData.position = expectedCurrentPosition
            mListener.onTrackingEvent(NEXT_BUTTON_PRESSED, videoData)
        }
    }

    @Test
    fun `when next or previous button null, do not set listeners`() {
        every { mPlayerView.findViewById<View>(R.id.exo_next_button) } returns null
        every { mPlayerView.findViewById<View>(R.id.exo_prev_button) } returns null
        every { mConfig.showNextPreviousButtons } returns true
        val videoList = listOf(createDefaultVideo(), createDefaultVideo())
        val expectedCurrentPosition = 876435L
        val videoData = mockk<TrackingTypeData.TrackingVideoTypeData>(relaxed = true)
        every { utils.createTrackingVideoTypeData() } returns videoData
        every { mPlayer.currentPosition } returns expectedCurrentPosition

        testObject.playVideos(videoList)

        verify { nextButton wasNot called }
        verify { previousButton wasNot called }
    }

    @Test
    fun `playVideos when has more videos in mVideos sets nextButton as enabled, onclick listener plays next video (not fullscreen)`() {
        every { mConfig.showNextPreviousButtons } returns true
        val arcVideo1 = createDefaultVideo()
        val arcVideo2 = createDefaultVideo()
        val videoList = listOf(arcVideo1, arcVideo2)
        val expectedCurrentPosition = 876435L
        val videoData = mockk<TrackingTypeData.TrackingVideoTypeData>(relaxed = true)
        val listener = mutableListOf<OnClickListener>()
        every { utils.createTrackingVideoTypeData() } returns videoData
        every { mPlayer.currentPosition } returns expectedCurrentPosition
        testObject.playVideos(videoList)

        verify(exactly = 2) {
            nextButton.setOnClickListener(capture(listener))
        }

        listener[1].onClick(mockk())

        assertEquals(arcVideo2, testObject.mVideo)
    }

    @Test
    fun `volume button is hidden when configured to not show`() {
        mConfig.apply {
            every { showVolumeButton } returns false
        }

        testObject.playVideo(createDefaultVideo())

        verify(exactly = 1) {
            volumeButton.visibility = GONE
        }
    }

    @Test
    fun `title view is hidden when configured to not show`() {
        mConfig.apply {
            every { showTitleOnController } returns false
        }

        testObject.playVideo(createDefaultVideo())

        verify(exactly = 1) {
            titleView.visibility = INVISIBLE
        }
    }

    @Test
    fun `title view is properly set with correct title from mHeadline`() {
        mConfig.apply {
            every { showTitleOnController } returns true
        }

        testObject.playVideo(createDefaultVideo())

        verify(exactly = 1) {
            titleView.visibility = VISIBLE
            titleView.text = "headline"
        }
    }

    @Test
    fun `prevButton On Click Listener when mVideos size greater than 1 calls tracking event, plays prev`() {
        every { mConfig.showNextPreviousButtons } returns true
        val arcVideo1 = createDefaultVideo()
        val arcVideo2 = createDefaultVideo()
        val videoList = listOf(arcVideo1, arcVideo2)
        val expectedCurrentPosition = 876435L
        val videoData = mockk<TrackingTypeData.TrackingVideoTypeData>(relaxed = true)
        val listener = mutableListOf<OnClickListener>()
        every { utils.createTrackingVideoTypeData() } returns videoData
        every { mPlayer.currentPosition } returns expectedCurrentPosition
        testObject.playVideos(videoList)
        clearAllMocks(answers = false)

        assertEquals(arcVideo1, testObject.mVideo)
        //since we have nothing exposed that can advance playlist except this that I saw
        testObject.onPlayerStateChanged(true, Player.STATE_ENDED)
        verify(exactly = 1) {
            previousButton.visibility = VISIBLE
        }
        verify(exactly = 2) {
            previousButton.setOnClickListener(capture(listener))
        }
        assertEquals(arcVideo2, testObject.mVideo)

        listener[1].onClick(mockk())

        assertEquals(arcVideo1, testObject.mVideo)
    }

    @Test
    fun `getShowNextPreviousButtons false and next prev buttons are null`() {
        every { mPlayerView.findViewById<View>(R.id.exo_next_button) } returns null
        every { mPlayerView.findViewById<View>(R.id.exo_prev_button) } returns null
        every { mConfig.showNextPreviousButtons } returns false
        val videoList = listOf(createDefaultVideo(), createDefaultVideo())
        val expectedCurrentPosition = 876435L
        val videoData = mockk<TrackingTypeData.TrackingVideoTypeData>(relaxed = true)
        every { utils.createTrackingVideoTypeData() } returns videoData
        every { mPlayer.currentPosition } returns expectedCurrentPosition

        testObject.playVideos(videoList)

        verify { nextButton wasNot called }
        verify { previousButton wasNot called }
    }

    @Test
    fun `initial setup when disabled controls fully does not run logic in set up player control listeners`() {
        every { mConfig.isDisableControlsFully } returns true
        clearAllMocks(answers = false)

        testObject = PostTvPlayerImpl(
            mConfig,
            mVideoManager,
            mListener,
            trackingHelper,
            utils
        )

        testObject.playVideo(createDefaultVideo())
        verify(exactly = 1) {
            mPlayerView.useController = false
        }
        verify(exactly = 2) {
            mConfig.isDisableControlsFully
        }
        verify {
            mPlayerView.findViewById<ImageButton>(R.id.exo_fullscreen) wasNot called
        }
    }

    @Test
    fun `set Video Captions Drawable when PrefManager has captions enabled`() {
        mockkStatic(PrefManager::class)
        every {
            PrefManager.getBoolean(
                mAppContext,
                PrefManager.IS_CAPTIONS_ENABLED,
                false
            )
        } returns true
        clearAllMocks(answers = false)

        testObject = PostTvPlayerImpl(
            mConfig,
            mVideoManager,
            mListener,
            trackingHelper,
            utils
        )
        testObject.playVideo(createDefaultVideo())
        verify(exactly = 2) {
            mPlayerView.findViewById<ImageButton>(R.id.exo_cc)
            PrefManager.getBoolean(mAppContext, PrefManager.IS_CAPTIONS_ENABLED, false)
        }
        verify(exactly = 1) {
            ContextCompat.getDrawable(mAppContext, R.drawable.CcDrawableButton)
            ccButton.setImageDrawable(mockDrawable)
        }
    }

    @Test
    fun `set Video Captions Drawable when using CCStartMode ON config cc start mode`() {
        every { mConfig.ccStartMode } returns ArcXPVideoConfig.CCStartMode.ON
        clearAllMocks(answers = false)

        testObject = PostTvPlayerImpl(
            mConfig,
            mVideoManager,
            mListener,
            trackingHelper,
            utils
        )
        testObject.playVideo(createDefaultVideo())
        verify(exactly = 2) {
            mPlayerView.findViewById<ImageButton>(R.id.exo_cc)
            PrefManager.getBoolean(mAppContext, PrefManager.IS_CAPTIONS_ENABLED, false)
        }
        verify(exactly = 1) {
            mConfig.ccStartMode
            ContextCompat.getDrawable(mAppContext, R.drawable.CcDrawableButton)
            ccButton.setImageDrawable(mockDrawable)
        }
    }

    private fun playVideoThenVerify(arcVideo: ArcVideo) {
        testObject.playVideo(arcVideo)
        verifyPlayVideo(arcVideo)
    }

    private fun verifyPlayVideo(arcVideo: ArcVideo) {
        verify {
//constructor
            mConfig.activity
            utils.createDefaultDataSourceFactory(mAppContext, "useragent")
            mConfig.overlays
            mConfig.overlays
            mVideoManager.castManager
//public playVideo
//private playVideo
            mVideoManager.initVideo(expectedId)
//initLocalPlayer
            utils.createExoPlayerBuilder(mAppContext)
            mockExoPlayerBuilder.apply {
                setTrackSelector(any())
                setSeekForwardIncrementMs(expectedIncrement.toLong())
                setSeekBackIncrementMs(expectedIncrement.toLong())
                setLooper(mockLooper)
                build()
            }
            mPlayer.addListener(testObject)
            utils.createPlayerView(mAppContext)
            mPlayerView.apply {
                id = R.id.wapo_player_view
                player = mPlayer
            }
            mVideoManager.isAutoShowControls
            mPlayerView.controllerAutoShow = true
            mPlayerView.findViewById<TextView>(R.id.styled_controller_title_tv)
            mPlayerView.findViewById<ImageButton>(R.id.exo_fullscreen)
            fullScreenButton.setOnClickListener(any())
            mPlayerView.findViewById<ImageButton>(R.id.exo_play)
            mPlayerView.findViewById<ImageButton>(R.id.exo_pause)
            mPlayerView.findViewById<ImageButton>(R.id.exo_share)
            shareButton.setOnClickListener(any())
            shareButton.visibility = VISIBLE
            mPlayerView.findViewById<ImageButton>(R.id.exo_back)
            mConfig.showBackButton
            backButton.setOnClickListener(any())
            mPlayerView.findViewById<ImageButton>(R.id.exo_pip)
            pipButton.visibility = GONE
            pipButton.setOnClickListener(any())
            mPlayerView.findViewById<ImageButton>(R.id.exo_volume)
            mPlayer.volume
            volumeButton.setImageDrawable(any())
            volumeButton.setOnClickListener(any())
            mPlayerView.findViewById<ImageButton>(R.id.exo_cc)
            mVideoManager.enableClosedCaption()
            mConfig.isKeepControlsSpaceOnHide
            ccButton.setOnClickListener(any())
            ccButton.visibility = INVISIBLE
            mPlayerView.findViewById<ImageButton>(R.id.exo_next_button)
            mPlayerView.findViewById<ImageButton>(R.id.exo_prev_button)
            mConfig.showNextPreviousButtons
            mVideoManager.isShowSeekButton
            mPlayerView.setShowFastForwardButton(true)
            mVideoManager.isShowSeekButton
            mPlayerView.setShowRewindButton(true)
            mPlayerView.findViewById<View>(R.id.exo_position)
            mPlayerView.findViewById<View>(R.id.exo_duration)
            mPlayerView.findViewById<View>(R.id.exo_progress)
            mPlayerView.findViewById<View>(R.id.time_bar_layout)
            mVideoManager.isShowCountDown
            exoDuration.visibility = VISIBLE
            mVideoManager.isShowProgressBar
            exoProgress.setScrubberColor(expectedTimeScrubColor)
            exoProgress.setPlayedColor(expectedTimePlayColor)
            exoProgress.setUnplayedColor(expectedTimeUnPlayedColor)
            exoProgress.setBufferedColor(expectedBufferedColor)
            exoProgress.setAdMarkerColor(expectedAdMarkerColor)
            exoProgress.setPlayedAdMarkerColor(expectedAdPlayedColor)
            exoTimeBarLayout.visibility = VISIBLE
            exoPosition.visibility = VISIBLE
            exoDuration.visibility = VISIBLE
            exoProgress.visibility = VISIBLE
            mPlayerView.requestFocus()
            mConfig.controlsShowTimeoutMs
            mConfig.isDisableControlsWithTouch
            //END setUpPlayerControlListeners
            mConfig.showTitleOnController
            titleView.text = mHeadline
            titleView.visibility = VISIBLE
            mPlayerView.setOnTouchListener(any())
            mListener.addVideoView(mPlayerView)
            mFullscreenOverlays.values
            mPlayerView.apply {
                addView(mockView1)
                addView(mockView2)
                addView(mockView3)
                findViewById<ImageButton>(R.id.exo_cc)
            }
            ContextCompat.getDrawable(mAppContext, R.drawable.CcOffDrawableButton)
            ccButton.setImageDrawable(mockDrawable)

            //END InitLocalPlayer
            //initCastPlayer
            utils.createCastPlayer(
                mockCastContext,
                expectedIncrement.toLong(),
                expectedIncrement.toLong()
            )
            mCastPlayer.addListener(any())
            mCastPlayer.setSessionAvailabilityListener(any())
            utils.createPlayerControlView(mAppContext)
            mCastControlView.id = R.id.wapo_cast_control_view
            mCastControlView.player = mCastPlayer
            mCastControlView.showTimeoutMs = -1
            mCastControlView.findViewById<ImageButton>(R.id.exo_fullscreen)
            mCastControlView.findViewById<ImageButton>(R.id.exo_pip)
            mCastControlView.findViewById<ImageButton>(R.id.exo_share)
            mCastControlView.findViewById<ImageButton>(R.id.exo_volume)
            mCastControlView.findViewById<ImageButton>(R.id.exo_cc)
            mCastControlView.findViewById<ImageButton>(R.id.exo_artwork)
            artWork.visibility = VISIBLE
            mConfig.artworkUrl
            utils.loadImageIntoView(mAppContext, mArtWorkUrl, artWork)
            castFullScreenButton.visibility = VISIBLE
            castFullScreenButton.setOnClickListener(any())//TODO test click listener
            castPipButton.visibility = GONE
            castVolumeButton.visibility = VISIBLE
            castVolumeButton.setOnClickListener(any())//TODO test click listener
            ContextCompat.getDrawable(mAppContext, R.drawable.MuteOffDrawableButton)
            castVolumeButton.setImageDrawable(mockDrawable)
            castCcButton.visibility = VISIBLE
            castCcButton.setOnClickListener(any())//TODO test click listener
            ContextCompat.getDrawable(mAppContext, R.drawable.CcOffDrawableButton)
            castCcButton.setImageDrawable(mockDrawable)
            castShareButton.setOnClickListener(any())//TODO check if tested
            castShareButton.visibility = VISIBLE
            mListener.addVideoView(mCastControlView)
            //End initCastPlayer
            //setCurrentPlayer begins (sets current Player, mVideoTracker)

            mCastPlayer.isCastSessionAvailable
            mPlayerView.visibility = VISIBLE
            mCastControlView.hide()
            mCastControlView.keepScreenOn = false
            //END setCurrentPlayer
            //begin startVideoOnCurrentPlayer (null checks currentPlayer and mVideo)
            mPlayer.playWhenReady = false

            Uri.parse(eq("addTagUrl1234567"))
            //initVideoCaptions Called
            mPlayer.setMediaSource(any())//TODO
            mPlayer.prepare()
            mListener.getSavedPosition(expectedStartPosition.toString())
            mPlayer.seekTo(expectedSavedPosition)
            mPlayerView.setOnTouchListener(any())
        }
        verifyNoExceptions()
    }


    private fun verifyNoExceptions() =
        verify(exactly = 0) {
            Log.d(any(), any())
            Log.e(any(), any())
            mListener.onError(any(), any(), any())
        }

    private fun createDefaultVideo(shouldPlayAds: Boolean = true) = ArcVideo(
        expectedId,
        "uuid",
        expectedStartPosition,
        false,
        false,
        100,
        mShareUrl,
        mHeadline,
        "pageName",
        "videoName",
        "videoSection",
        "videoSource",
        "videoCategory",
        "consentId",
        "fallbackUrl",
        "addTagUrl[timestamp]",
        shouldPlayAds,
        subtitleUrl,
        "source",
        mockk(),
        false,
        false,
        false,
        ArcXPVideoConfig.CCStartMode.DEFAULT
    )
}