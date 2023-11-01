//package com.arcxp.video.players
//
//import android.app.Activity
//import android.app.AlertDialog
//import android.app.Dialog
//import android.content.Context
//import android.content.DialogInterface
//import android.content.Intent
//import android.content.pm.PackageManager
//import android.content.res.Resources
//import android.graphics.drawable.Drawable
//import android.net.Uri
//import android.os.Looper
//import android.provider.Settings
//import android.util.Log
//import android.view.KeyEvent
//import android.view.MotionEvent
//import android.view.MotionEvent.ACTION_BUTTON_PRESS
//import android.view.MotionEvent.ACTION_UP
//import android.view.View
//import android.view.View.*
//import android.view.ViewGroup
//import android.view.accessibility.CaptioningManager
//import android.widget.*
//import androidx.core.content.ContextCompat
//import com.arcxp.sdk.R
//import com.arcxp.video.ArcXPVideoConfig
//import com.arcxp.video.ArcVideoManager
//import com.arcxp.video.cast.ArcCastManager
//import com.arcxp.video.listeners.AdsLoadedListener
//import com.arcxp.video.listeners.ArcKeyListener
//import com.arcxp.video.listeners.VideoListener
//import com.arcxp.video.model.*
//import com.arcxp.video.model.TrackingType.*
//import com.arcxp.video.util.PrefManager
//import com.arcxp.video.util.TrackingHelper
//import com.arcxp.video.util.Utils
//import com.google.ads.interactivemedia.v3.api.Ad
//import com.google.ads.interactivemedia.v3.api.AdEvent
//import com.google.ads.interactivemedia.v3.api.AdEvent.AdEventType.*
//import com.google.ads.interactivemedia.v3.api.AdsLoader
//import com.google.android.exoplayer2.*
//import com.google.android.exoplayer2.ext.cast.CastPlayer
//import com.google.android.exoplayer2.ext.ima.ImaAdsLoader
//import com.google.android.exoplayer2.source.MergingMediaSource
//import com.google.android.exoplayer2.source.SingleSampleMediaSource
//import com.google.android.exoplayer2.source.TrackGroup
//import com.google.android.exoplayer2.source.ads.AdsMediaSource
//import com.google.android.exoplayer2.source.hls.HlsMediaSource
//import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
//import com.google.android.exoplayer2.trackselection.MappingTrackSelector
//import com.google.android.exoplayer2.ui.DefaultTimeBar
//import com.google.android.exoplayer2.ui.PlayerControlView
//import com.google.android.exoplayer2.ui.StyledPlayerView
//import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
//import com.google.android.exoplayer2.upstream.FileDataSource
//import com.google.android.exoplayer2.util.Util
//import com.google.android.gms.cast.framework.CastContext
//import com.google.common.collect.ImmutableList
//import io.mockk.*
//import io.mockk.impl.annotations.MockK
//import io.mockk.impl.annotations.RelaxedMockK
//import org.junit.After
//import org.junit.Assert.*
//import org.junit.Before
//import org.junit.FixMethodOrder
//import org.junit.Test
//import org.junit.runners.MethodSorters
//import java.util.*
//
//@FixMethodOrder(MethodSorters.NAME_ASCENDING)
//class PostTvPlayerImplTest {
//
//    private lateinit var testObject: PostTvPlayerImpl
//
//    @RelaxedMockK
//    lateinit var mVideoManager: ArcVideoManager
//
//    @RelaxedMockK
//    lateinit var mConfig: ArcXPVideoConfig
//
//    @RelaxedMockK
//    lateinit var trackingHelper: TrackingHelper
//
//    @RelaxedMockK
//    lateinit var mAppContext: Activity
//
//    @RelaxedMockK
//    lateinit var mAdsLoader: ImaAdsLoader
//
//    @RelaxedMockK
//    lateinit var mAdsLoaderAdsLoader: AdsLoader
//
//    @RelaxedMockK
//    lateinit var ccButton: ImageButton
//
//    @RelaxedMockK
//    lateinit var castCcButton: ImageButton
//
//    @RelaxedMockK
//    lateinit var artWork: ImageView
//
//    @RelaxedMockK
//    lateinit var mMediaDataSourceFactory: DefaultDataSourceFactory
//
//    @RelaxedMockK
//    lateinit var mPlayer: ExoPlayer
//
//    @RelaxedMockK
//    lateinit var mPlayerView: StyledPlayerView
//
//    @RelaxedMockK
//    lateinit var mCastControlView: PlayerControlView
//
//    @RelaxedMockK
//    lateinit var mockResources: Resources
//
//    @MockK
//    lateinit var mockPackageManager: PackageManager
//
//    @MockK
//    lateinit var mFullscreenOverlays: HashMap<String, View>
//
//    @MockK
//    lateinit var mockCastManager: ArcCastManager
//
//    @RelaxedMockK
//    lateinit var mListener: VideoListener
//
//    @RelaxedMockK
//    lateinit var mockCastContext: CastContext
//
//    @RelaxedMockK
//    lateinit var mCastPlayer: CastPlayer
//
//    @RelaxedMockK
//    lateinit var mockContentMediaSource: MergingMediaSource
//
//    @MockK
//    lateinit var adsLoadedListener: AdsLoadedListener
//
//    @RelaxedMockK
//    lateinit var mockView1: View
//
//    @RelaxedMockK
//    lateinit var mockView2: View
//
//    @RelaxedMockK
//    lateinit var mockView3: View
//
//    @RelaxedMockK
//    lateinit var fullScreenButton: ImageButton
//
//    @RelaxedMockK
//    lateinit var castFullScreenButton: ImageButton
//
//    @RelaxedMockK
//    lateinit var shareButton: ImageButton
//
//    @RelaxedMockK
//    lateinit var castShareButton: ImageButton
//
//    @RelaxedMockK
//    lateinit var volumeButton: ImageButton
//
//    @RelaxedMockK
//    lateinit var castVolumeButton: ImageButton
//
//    @RelaxedMockK
//    lateinit var playButton: ImageButton
//
//    @RelaxedMockK
//    lateinit var pauseButton: ImageButton
//
//    @RelaxedMockK
//    lateinit var pipButton: ImageButton
//
//    @RelaxedMockK
//    lateinit var castPipButton: ImageButton
//
//    @RelaxedMockK
//    lateinit var nextButton: ImageButton
//
//    @RelaxedMockK
//    lateinit var previousButton: ImageButton
//
//    @RelaxedMockK
//    lateinit var backButton: ImageButton
//
//    @RelaxedMockK
//    lateinit var exoPosition: View
//
//    @RelaxedMockK
//    lateinit var exoDuration: View
//
//    @RelaxedMockK
//    lateinit var exoProgress: DefaultTimeBar
//
//    @RelaxedMockK
//    lateinit var adsMediaSource: AdsMediaSource
//
//    @RelaxedMockK
//    lateinit var expectedAdUri: Uri
//
//    @RelaxedMockK
//    lateinit var expectedSubtitleUri: Uri
//
//    @RelaxedMockK
//    lateinit var expectedIdUri: Uri
//
//    @RelaxedMockK
//    lateinit var exoTimeBarLayout: LinearLayout
//
//    @RelaxedMockK
//    lateinit var mockExoPlayerBuilder: ExoPlayer.Builder
//
//    @RelaxedMockK
//    lateinit var mockLooper: Looper
//
//    @RelaxedMockK
//    lateinit var mockMediaSource: HlsMediaSource
//
//    @RelaxedMockK
//    lateinit var mockFormat: Format
//
//    @RelaxedMockK
//    lateinit var mockSingleSampleMediaSource: SingleSampleMediaSource
//
//    @RelaxedMockK
//    lateinit var mockSingleSampleMediaSourceFactory: SingleSampleMediaSource.Factory
//
//    @RelaxedMockK
//    lateinit var utils: Utils
//
//    @RelaxedMockK
//    lateinit var mTrackSelector: DefaultTrackSelector
//
//    @MockK
//    lateinit var mockDrawable: Drawable
//
//    @RelaxedMockK
//    lateinit var adEvent: AdEvent
//
//    @RelaxedMockK
//    lateinit var subtitleConfiguration: MediaItem.SubtitleConfiguration
//
//    @RelaxedMockK
//    lateinit var titleView: TextView
//
//    @RelaxedMockK
//    lateinit var timeline: Timeline
//
//    @MockK
//    lateinit var currentMappedTrackInfo: MappingTrackSelector.MappedTrackInfo
//
//    @MockK
//    lateinit var period: Timeline.Period
//
//    private val expectedId = "123"
//    private val expectedVolume = 0.67f
//    private val subtitleUrl = "mock subtitle url"
//    private val expectedSavedPosition = 12345L
//    private val expectedPosition = 987453L
//    private val expectedStartPosition = 123L
//    private val renderError = "An error occurred during playback."
//    private val sourceError = "An error occurred during playback."
//    private val unknownError = "An unknown error occurred while trying to play the video."
//    private val mHeadline = "headline"
//    private val mShareUrl = "shareUrl"
//    private val mArtWorkUrl = "artworkUrl"
//    private val mockPackageName = "packageName"
//    private val expectedIncrement = 10000
//    private val expectedSessionId = "sessionId"
//    private val expectedUrl = "expectedUrl"
//    private val mediaItem = MediaItem.fromUri(expectedUrl)
//
//    private val expectedTimeScrubColor = 342
//    private val expectedTimePlayColor = 3421
//    private val expectedTimeUnPlayedColor = 3422
//    private val expectedBufferedColor = 3423
//    private val expectedAdPlayedColor = 342367
//    private val expectedAdMarkerColor = 342378
//    private val expectedCurrentPosition = 83746L
//    private val expectedPeriodPosition = 83744L
//    private val expectedAdjustedPosition = 2L
//    private val expectedPeriodIndex = 7
//
//    @Before
//    fun setup() {
//        MockKAnnotations.init(this, relaxUnitFun = true)
//        every { mTrackSelector.currentMappedTrackInfo } returns currentMappedTrackInfo
//        every {
//            mPlayer.currentTimeline
//        } returns timeline
//        every {
//            timeline.getPeriod(
//                expectedPeriodIndex,
//                any()
//            )
//        } returns period
//        every { period.positionInWindowMs } returns expectedPeriodPosition
//        mConfig.apply {
//            every { controlsShowTimeoutMs } returns null
//            every { isDisableControlsWithTouch } returns false
//            every { overlays } returns mFullscreenOverlays.apply {
//                every { values } returns mutableListOf(mockView1, mockView2, mockView3)
//            }
//            every { isKeepControlsSpaceOnHide } returns true
//            every { activity } returns mAppContext.apply {
//                every { resources } returns mockResources.apply {
//                    every { getInteger(R.integer.ff_inc) } returns expectedIncrement
//                    every { getInteger(R.integer.rew_inc) } returns expectedIncrement
//                    every { getColor(R.color.TimeBarScrubberColor) } returns expectedTimeScrubColor
//                    every { getColor(R.color.TimeBarPlayedColor) } returns expectedTimePlayColor
//                    every { getColor(R.color.TimeBarUnplayedColor) } returns expectedTimeUnPlayedColor
//                    every { getColor(R.color.TimeBarBufferedColor) } returns expectedBufferedColor
//                    every { getColor(R.color.AdMarkerColor) } returns expectedAdMarkerColor
//                    every { getColor(R.color.AdPlayedMarkerColor) } returns expectedAdPlayedColor
//                }
//                every { packageManager } returns mockPackageManager
//                every { packageName } returns mockPackageName
//                every { getString(R.string.render_error) } returns renderError
//                every { getString(R.string.source_error) } returns sourceError
//                every { getString(R.string.unknown_error) } returns unknownError
//            }
//            every { shouldDisableNextButton } returns false
//            every { shouldDisablePreviousButton } returns false
//            every { showNextPreviousButtons } returns false
//            every { userAgent } returns "useragent"
//            every { showFullScreenButton } returns true
//            every { showBackButton } returns true
//            every { showVolumeButton } returns true
//            every { showTitleOnController } returns true
//        }
//        every { mConfig.artworkUrl } returns mArtWorkUrl
//        mVideoManager.apply {
//            every { isShowCountDown } returns true
//            every { isShowProgressBar } returns true
//            every { isShowSeekButton } returns true
//            every { enableClosedCaption() } returns false
//            every { castManager } returns mockCastManager
//            every { currentActivity } returns mAppContext
//            every { isAutoShowControls } returns true
//            every { sessionId } returns expectedSessionId
//        }
//        every { mListener.getSavedPosition(expectedId) } returns expectedSavedPosition
//        utils.apply {
//            every {
//                createAdsMediaSource(
//                    mockContentMediaSource,
//                    any(),
//                    expectedAdUri,
//                    any(),
//                    mAdsLoader,
//                    mPlayerView
//                )
//            } returns adsMediaSource
//            every { createExoPlayerBuilder(mAppContext) } returns mockExoPlayerBuilder.apply {
//                every { setTrackSelector(any()) } returns mockExoPlayerBuilder
//                every { setSeekBackIncrementMs(expectedIncrement.toLong()) } returns mockExoPlayerBuilder
//                every { setSeekForwardIncrementMs(expectedIncrement.toLong()) } returns mockExoPlayerBuilder
//                every { setLooper(any()) } returns mockExoPlayerBuilder
//                every { build() } returns mPlayer.apply {
//                    every { addListener(any()) } returns Unit
//                    every { prepare(any()) } returns Unit
//                    every { volume } returns expectedVolume
//                    every { currentPosition } returns expectedSavedPosition
//                }
//            }
//            every { createPlayerView(mAppContext) } returns mPlayerView.apply {
//                every { findViewById<ImageButton>(R.id.exo_play) } returns playButton
//                every { findViewById<ImageButton>(R.id.exo_pause) } returns pauseButton
//                every { findViewById<ImageButton>(R.id.exo_fullscreen) } returns fullScreenButton
//                every { findViewById<ImageButton>(R.id.exo_share) } returns shareButton
//                every { findViewById<ImageButton>(R.id.exo_pip) } returns pipButton
//                every { findViewById<ImageButton>(R.id.exo_volume) } returns volumeButton
//                every { findViewById<ImageButton>(R.id.exo_cc) } returns ccButton
//                every { findViewById<LinearLayout>(R.id.time_bar_layout) } returns exoTimeBarLayout
//                every { findViewById<View>(R.id.exo_position) } returns exoPosition
//                every { findViewById<View>(R.id.exo_duration) } returns exoDuration
//                every { findViewById<View>(R.id.exo_progress) } returns exoProgress
//                every { findViewById<View>(R.id.exo_next_button) } returns nextButton
//                every { findViewById<View>(R.id.exo_prev_button) } returns previousButton
//                every { findViewById<TextView>(R.id.styled_controller_title_tv) } returns titleView
////                every { findViewById<View>(R.id.exo_ffwd) } returns ffButtton
////                every { findViewById<View>(R.id.exo_rew_with_amount) } returns rwButtton
//                every { findViewById<View>(R.id.exo_back) } returns backButton
//            }
//            every { createPlayerControlView(mAppContext) } returns mCastControlView.apply {
//                every { findViewById<ImageButton>(R.id.exo_fullscreen) } returns castFullScreenButton
//                every { findViewById<ImageButton>(R.id.exo_pip) } returns castPipButton
//                every { findViewById<ImageButton>(R.id.exo_share) } returns castShareButton
//                every { findViewById<ImageButton>(R.id.exo_volume) } returns castVolumeButton
//                every { findViewById<ImageButton>(R.id.exo_cc) } returns castCcButton
//                every { findViewById<ImageView>(R.id.exo_artwork) } returns artWork
//            }
//            every { createCastPlayer(mockCastContext, 10000, 10000) } returns mCastPlayer
//            every { createSingleSampleMediaSourceFactory(mMediaDataSourceFactory) } returns mockSingleSampleMediaSourceFactory.apply {
//                every { setTag(expectedId) } returns this
//                every {
//                    createMediaSource(
//                        subtitleConfiguration,
//                        C.TIME_UNSET
//                    )
//                } returns mockSingleSampleMediaSource
//            }
//            every {
//                createMergingMediaSource(
//                    mockMediaSource,
//                    mockSingleSampleMediaSource
//                )
//            } returns mockContentMediaSource
//            every {
//                createAdsLoadedListener(
//                    mListener,
//                    any(),
//                    any(),
//                    any()
//                )
//            } returns adsLoadedListener
//            every {
//                createDefaultDataSourceFactory(
//                    mAppContext,
//                    "useragent"
//                )
//            } returns mMediaDataSourceFactory
//            every { createDefaultTrackSelector() } returns mTrackSelector
//            every { loadImageIntoView(mAppContext, mArtWorkUrl, artWork) } returns mockk()
//            every {
//                createSubtitleConfiguration(expectedSubtitleUri)
//            } returns subtitleConfiguration
//        }
//        every { mockCastManager.getCastContext() } returns mockCastContext
//        every { mAdsLoader.adsLoader } returns mAdsLoaderAdsLoader
//
//        mockkConstructor(DefaultTrackFilter::class)
//        val expectedTime = 1234567L
//        mockkConstructor(Date::class)
//        every { constructedWith<Date>().time } returns expectedTime
//        mockkConstructor(HlsMediaSource.Factory::class)
//        every {
//            constructedWith<HlsMediaSource.Factory>(EqMatcher(mMediaDataSourceFactory))
//                .createMediaSource(mediaItem)
//        } returns mockMediaSource
//        mockkConstructor(ImaAdsLoader.Builder::class)
//        val mockImaAdsLoaderBuilder = mockk<ImaAdsLoader.Builder>()
//        every { constructedWith<ImaAdsLoader.Builder>(EqMatcher(mAppContext)).setAdEventListener(any()) } returns mockImaAdsLoaderBuilder
//        every { mockImaAdsLoaderBuilder.build() } returns mAdsLoader
//
//        mockkStatic(Uri::class)
//        every { Uri.parse("addTagUrl1234567") } returns expectedAdUri
//        every { Uri.parse(subtitleUrl) } returns expectedSubtitleUri
//        every { Uri.parse(expectedId) } returns expectedIdUri
//
//        mockkStatic(Log::class)
//        every { Log.e(any(), any()) } returns 0
//        every { Log.e(any(), any(), any()) } returns 0
//
//        mockkStatic(Looper::class)
//        every { Looper.getMainLooper() } returns mockLooper
//
//        mockkStatic(Util::class)
//        every { Util.inferContentType(expectedSubtitleUri) } returns C.TYPE_HLS
//        every { Util.inferContentType(expectedIdUri) } returns C.TYPE_HLS
//
//        mockkStatic(ContextCompat::class)
//        every { ContextCompat.getDrawable(mAppContext, any()) } returns mockDrawable
//
////        mockkStatic(MediaItem::class)
////        every { MediaItem.fromUri(expectedIdUri) } returns mediaItem
//
////            every { getProperty("uri") } propertyType Uri::class answers { expectedIdUri }
////        } //expectedIdUri
////        every { mediaItem.localConfiguration  getProperty "uri" } propertyType Uri::class answers { expectedIdUri }
//
//
////        mockkStatic(Format::class)
////        every {
////            Format.createTextSampleFormat(
////                ID_SUBTITLE_URL,
////                MimeTypes.TEXT_VTT,
////                C.SELECTION_FLAG_DEFAULT,
////                "en"
////            )
////        } returns mockFormat
//
//        testObject = PostTvPlayerImpl(
//            mConfig,
//            mVideoManager,
//            mListener,
//            trackingHelper,
//            utils
//        )
//    }
//
//    @After
//    fun tearDown() {
//        clearAllMocks()
//    }


//
//    @Test
//    fun `when mConfig has controls timeout, set in playerView Controller`() {
//        val expectedTimeoutMs = 328746
//        every { mConfig.controlsShowTimeoutMs } returns expectedTimeoutMs
//
//        testObject.playVideo(createDefaultVideo())
//
//        verify(exactly = 1) { mPlayerView.controllerShowTimeoutMs = expectedTimeoutMs }
//    }
//
//    @Test
//    fun `when mConfig isDisableControlsWithTouch, set in playerView Controller`() {
//        every { mConfig.isDisableControlsWithTouch } returns true
//
//        testObject.playVideo(createDefaultVideo())
//
//        verify(exactly = 1) { mPlayerView.controllerHideOnTouch = true }
//    }

//    //    @Test
////    fun `when playVideo videoId is null uses fallback url to set mVideoId`() {
////        val expectedId = "fallback Url"
////        val arcVideo = ArcVideo.Builder().setUrl(null).setFallbackUrl(expectedId).build()
////        val mVideoIdSlot = slot<String>()
////
////        every { mVideoManager.initVideo(any()) } throws Exception()
////
////        testObject.playVideo(arcVideo)
////
////        verify{ mVideoManager.initVideo(capture(mVideoIdSlot))}
////        assertEquals(expectedId, mVideoIdSlot.captured)
////
////
////
////    }
//    //TODO so this section..(line 187) sets mVideoId, then it is overwritten with video.id in playVideo() (even if it is null), probably can scrap code or update to do something and test that
//
//    @Test
//    fun `playVideo adds video to mVideos`() {
//        val arcVideo = createDefaultVideo()
//        every { mVideoManager.initVideo(any()) } throws Exception()
//
//        testObject.playVideo(arcVideo)
//
//        assert(testObject.mVideos!!.contains(arcVideo))
//    }
//
//    @Test
//    fun `playVideo sets mVideo`() {
//        val arcVideo = createDefaultVideo()
//        every { mVideoManager.initVideo(any()) } throws Exception()
//
//        testObject.playVideo(arcVideo)
//
//        assertEquals(testObject.video, arcVideo)
//    }
//
//    @Test
//    fun `playVideo when should play ads false, previous player null, and startMuted false  initializes video player`() {
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
//            "addTagUrl",
//            false,
//            subtitleUrl,
//            "source",
//            mockk(),
//            false,
//            false,
//            false,
//            ArcXPVideoConfig.CCStartMode.DEFAULT
//        )
//
//        testObject.playVideo(arcVideo)
//
//        verify { mAdsLoader wasNot called }
//        verifyNoExceptions()
//    }
//

//
//    @Test
//    fun `playVideo when close caption enabled and available, makes ccButton visible`() {
////        val arcVideo = ArcVideo(expectedId, "uuid", expectedStartPosition, false, false, 100, "shareUrl", "headline", "pageName", "videoName", "videoSection", "videoSource", "videoCategory", "consentId", "fallbackUrl", "addTagUrl", false, subtitleUrl, "source", mockk(), false, false, false, ArcMediaPlayerConfig.CCStartMode.DEFAULT)
////        val currentMappedTrackInfo = mockk<MappingTrackSelector.MappedTrackInfo>()
////        val format = mockk<Format>()
////        val group = TrackGroup(format)
////        val trackGroups = TrackGroupArray(group)
////        every { mVideoManager.enableClosedCaption() } returns true
////        every { mTrackSelector.currentMappedTrackInfo } returns currentMappedTrackInfo
////        every { currentMappedTrackInfo.rendererCount } returns 1
////        every { currentMappedTrackInfo.getRendererType(0) } returns C.TRACK_TYPE_TEXT
////        every { constructedWith<DefaultTrackFilter>().filter(format, trackGroups) } returns true
////        every { currentMappedTrackInfo.getTrackGroups(0) } returns trackGroups
////
////
////        testObject.playVideo(arcVideo)
////
////        verify(exactly = 2) {
////            ccButton.visibility = VISIBLE
////        }
//
//        //TODO test is passing but giving error in initVideoCaptions (which I'm having trouble with)
//        //commented to remove error until fixed
//    }







//
//    @Test
//    fun `createMediaSourceWithCaptions throws exception, and is handled`() {
//        val message = "createMediaSourceWithCaptions exception"
//        val exception = Exception(message)
//        val newId = "382764"
//        val arcVideo = ArcVideo(
//            newId,
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
//        every { Uri.parse(newId) } throws exception
//
//        testObject.playVideo(arcVideo)
//
//        verify(exactly = 1) {
//            mListener.onError(ArcVideoSDKErrorType.EXOPLAYER_ERROR, message, arcVideo)
//        }
//    }


//
//    @Test
//    fun `onVolumeChanged changes volume on trackingHelper`() {
//        val expectedVolume = .345f
//        testObject.onVolumeChanged(expectedVolume)
//        verifySequence { trackingHelper.volumeChange(expectedVolume) }
//    }


//



//
//    @Test
//    fun `playVideo when should play ads false, previous player not null, not ended`() {
//        every { mPlayer.currentPosition } returns 0L
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
//            false,
//            subtitleUrl,
//            "source",
//            mockk(),
//            false,
//            false,
//            false,
//            ArcXPVideoConfig.CCStartMode.DEFAULT
//        )
//        testObject.playVideo(arcVideo)
//        every { mCastPlayer.isCastSessionAvailable } returns true
//        testObject.playVideo(arcVideo)
//
//
//        verify(exactly = 1) {
//            mPlayer.playbackState
//            mPlayer.currentPosition
//            mVideoManager.setSavedPosition(testObject.id, mPlayer.currentPosition)
//            mPlayer.stop()
//        }
//        verify(exactly = 2) { mFullscreenOverlays.values }
//        verify(exactly = 2) {
//            mPlayerView.addView(mockView1)
//            mPlayerView.addView(mockView2)
//            mPlayerView.addView(mockView3)
//        }
//        verifyNoExceptions()
//    }
//
//    @Test
//    fun `playVideo when should play ads false, previous player not null, Player state ended`() {
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
//            false,
//            subtitleUrl,
//            "source",
//            mockk(),
//            false,
//            false,
//            false,
//            ArcXPVideoConfig.CCStartMode.DEFAULT
//        )
//        every { mPlayer.playbackState } returns Player.STATE_ENDED
//        testObject.playVideo(arcVideo)
//        every { mCastPlayer.isCastSessionAvailable } returns true
//        testObject.playVideo(arcVideo)
//
//        verify(exactly = 0) {
//            mPlayer.currentPosition
//            mVideoManager.setSavedPosition(testObject.id, mPlayer.currentPosition)
//        }
//        verify(exactly = 1) {
//            mPlayer.playbackState
//            mPlayer.stop()
//        }
//        verify(exactly = 2) { mFullscreenOverlays.values }
//        verify(exactly = 2) {
//            mPlayerView.addView(mockView1)
//            mPlayerView.addView(mockView2)
//            mPlayerView.addView(mockView3)
//        }
//        verifyNoExceptions()
//    }
//
//    @Test
//    fun `playVideo when should play ads true, previous player not null, not ended`() {
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
//        testObject.playVideo(arcVideo)
//        every { mCastPlayer.isCastSessionAvailable } returns true
//        clearAllMocks(answers = false)
//        testObject.playVideo(arcVideo)
//
//        verify(exactly = 1) {
//            mFullscreenOverlays.values
//            mPlayerView.addView(mockView1)
//            mPlayerView.addView(mockView2)
//            mPlayerView.addView(mockView3)
//            mAdsLoaderAdsLoader.addAdsLoadedListener(adsLoadedListener)
//            mAdsLoader.setPlayer(mPlayer)
//            mPlayer.playbackState
//            mPlayer.currentPosition
//            mVideoManager.setSavedPosition(any(), any())
//            utils.createAdsLoadedListener(mListener, arcVideo, testObject, expectedSessionId)
//            mPlayer.stop()
//        }
//        verifyNoExceptions()
//    }
//
//
//    @Test
//    fun `playVideo uses castPlayer when available`() {
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
//            false,
//            subtitleUrl,
//            "source",
//            mockk(),
//            false,
//            false,
//            false,
//            ArcXPVideoConfig.CCStartMode.DEFAULT
//        )
//        every { mCastPlayer.isCastSessionAvailable } returns true
//        testObject.playVideo(arcVideo)
//        verify(exactly = 1) {
//            mPlayerView.visibility = GONE
//            mCastControlView.show()
//            mCastControlView.keepScreenOn = true
//        }
//    }
//
//    @Test
//    fun `when addVideo called and mVideos is not null, then the video is added to mVideos`() {
//        val newVideo = mockk<ArcVideo>()
//
//        testObject.addVideo(newVideo)
//
//        assertEquals(newVideo, testObject.mVideos!![0])
//    }
//


//
//
//
//    @Test
//    fun `null methods return null`() {
//        assertNull(testObject.setLoadErrorHandlingPolicy(mockk()))
//        assertNull(testObject.setDrmSessionManagerProvider(mockk()))
//        assertNull(testObject.createMediaSource(mockk()))
//    }



//
//    @Test
//    fun `getSupportedTypes returns expected types`() {
//        val expected = intArrayOf(C.TYPE_HLS, C.TYPE_SS, C.TYPE_DASH, C.TYPE_OTHER)
//
//        val actual = testObject.supportedTypes
//
//        assertTrue(expected contentEquals actual)
//    }
//

//
//    @Test
//    fun `isVideoCaptionEnabled mVideo not null CC Start mode Default Start Mode`() {
//        playVideoThenVerify(createDefaultVideo())
//
//        val service = mockk<CaptioningManager>()
//        every { service.isEnabled } returns true
//        mockkStatic(PrefManager::class)
//        every {
//            PrefManager.getBoolean(
//                mAppContext,
//                PrefManager.IS_CAPTIONS_ENABLED,
//                true
//            )
//        } returns true
//        every { mAppContext.getSystemService(Context.CAPTIONING_SERVICE) } returns service
//
//        assertTrue(testObject.isVideoCaptionEnabled)
//        verify(exactly = 1) {
//            mAppContext.getSystemService(Context.CAPTIONING_SERVICE)
//            service.isEnabled
//            PrefManager.getBoolean(mAppContext, PrefManager.IS_CAPTIONS_ENABLED, true)
//        }
//    }
//
//    @Test
//    fun `isVideoCaptionEnabled mVideo not null CC Start mode Default Start throws exception and then returns false`() {
//        playVideoThenVerify(createDefaultVideo())
//        every { mAppContext.getSystemService(Context.CAPTIONING_SERVICE) } throws Exception()
//        clearAllMocks(answers = false)
//
//        assertFalse(testObject.isVideoCaptionEnabled)
//        verifySequence {
//            mAppContext.getSystemService(Context.CAPTIONING_SERVICE)
//        }
//    }
//
//    @Test
//    fun `isVideoCaptionEnabled mVideo not null CC Start mode ON returns true`() {
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
//            ArcXPVideoConfig.CCStartMode.ON
//        )
//
//        playVideoThenVerify(arcVideo)
//
//        assertTrue(testObject.isVideoCaptionEnabled)
//    }
//
//    @Test
//    fun `isVideoCaptionEnabled mVideo not null CC Start mode other returns false`() {
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
//            ArcXPVideoConfig.CCStartMode.OFF
//        )
//
//        playVideoThenVerify(arcVideo)
//
//        assertFalse(testObject.isVideoCaptionEnabled)
//    }
//
//    @Test
//    fun `isVideoCaptionEnabled mVideo null returns false`() {
//        assertFalse(testObject.isVideoCaptionEnabled)
//    }
//


//
//    @Test
//    fun `shareButton cast onClick triggers share video event, and then notifies listener`() {
//        val shareButtonListener = slot<OnClickListener>()
//        val arcVideo = createDefaultVideo()
//        val videoData = mockk<TrackingTypeData.TrackingVideoTypeData>(relaxed = true)
//        every { utils.createTrackingVideoTypeData() } returns videoData
//        testObject.playVideo(arcVideo)
//        verify { castShareButton.setOnClickListener(capture(shareButtonListener)) }
//        clearAllMocks(answers = false)
//
//        shareButtonListener.captured.onClick(mockk())
//
//        verifySequence {
//            utils.createTrackingVideoTypeData()
//            videoData.arcVideo = arcVideo
//            mCastPlayer.currentPosition
//            videoData.position = 0
//            mListener.onTrackingEvent(ON_SHARE, videoData)
//            mListener.onShareVideo(mHeadline, mShareUrl)
//        }
//    }
//    @Test
//    fun `getCurrentVideoDuration returns zero if currentPlayer is null`() {
//        assertEquals(0, testObject.currentVideoDuration)
//    }
//
//    @Test
//    fun `getCurrentVideoDuration returns duration if currentPlayer is not null`() {
//        val expectedDuration = 84765L
//        every { mPlayer.duration } returns expectedDuration
//
//        playVideoThenVerify(createDefaultVideo())
//
//        assertEquals(expectedDuration, testObject.currentVideoDuration)
//    }
//
//
////    @Test
////    fun `mPlayer controller visibility listener playButton is null`() {
////        val listener = slot<StyledPlayerView.ControllerVisibilityListener>()
////        every { mPlayerView.findViewById<ImageButton>(R.id.exo_play) } returns null
////        testObject.playVideo(createDefaultVideo())
////        verify { mPlayerView.setControllerVisibilityListener(capture(listener)) }
////        clearAllMocks(answers = false)
////
////        listener.captured.onVisibilityChanged(PlayerControlView.VISIBLE)
////
////        verifySequence {
////            pauseButton.requestFocus()
////            mPlayerView.requestLayout()
////        }
////    }
//
////    @Test
////    fun `mPlayer controller visibility listener playButton is not visible`() {
////        val listener = slot<StyledPlayerView.ControllerVisibilityListener>()
////        every { playButton.visibility } returns INVISIBLE
////        testObject.playVideo(createDefaultVideo())
////        verify { mPlayerView.setControllerVisibilityListener(capture(listener)) }
////        clearAllMocks(answers = false)
////
////        listener.captured.onVisibilityChanged(PlayerControlView.VISIBLE)
////
////        verifySequence {
////            pauseButton.requestFocus()
////            mPlayerView.requestLayout()
////        }
////    }
//
////    @Test
////    fun `mPlayer controller visibility listener playButton is not null and visible`() {
////        val listener = slot<StyledPlayerView.ControllerVisibilityListener>()
////        testObject.playVideo(createDefaultVideo())
////        verify { mPlayerView.setControllerVisibilityListener(capture(listener)) }
////        clearAllMocks(answers = false)
////
////        listener.captured.onVisibilityChanged(PlayerControlView.VISIBLE)
////
////        verifySequence {
////            playButton.visibility
////            playButton.requestFocus()
////            mPlayerView.requestLayout()
////        }
////    }
//



//
//    @Test
//    fun `fullscreen button listener given false`() {
//        val fullScreenListener = slot<OnClickListener>()
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
//        val viewGroup = mockk<ViewGroup>(relaxed = true)
//        val videoData = mockk<TrackingTypeData.TrackingVideoTypeData>(relaxed = true)
//        val mFullScreenDialog = mockk<Dialog>(relaxed = true)
//        val mockLayoutParams = mockk<ViewGroup.LayoutParams>()
//        val drawable = mockk<Drawable>()
//        every {
//            ContextCompat.getDrawable(
//                mAppContext,
//                R.drawable.FullScreenDrawableButtonCollapse
//            )
//        } returns drawable
//        every { utils.createTrackingVideoTypeData() } returns videoData
//        every { utils.createFullScreenDialog(mAppContext) } returns mFullScreenDialog
//        every { utils.createLayoutParams() } returns mockLayoutParams
//        every { mPlayer.currentPosition } returns expectedPosition
//        every { mPlayerView.parent } returns viewGroup
//        every { mockView1.parent } returns viewGroup
//        every { mockView2.parent } returns viewGroup
//        every { mockView3.parent } returns viewGroup
//        testObject.playVideo(arcVideo)
//        verify { fullScreenButton.setOnClickListener(capture(fullScreenListener)) }
//        clearAllMocks(answers = false)
//
//        fullScreenListener.captured.onClick(fullScreenButton)
//
//        verifySequence {
//            utils.createFullScreenDialog(mAppContext)
//            mFullScreenDialog.setOnKeyListener(any())
//            mPlayerView.parent
//            mPlayerView.parent
//            viewGroup.removeView(mPlayerView)
//            utils.createLayoutParams()
//            mFullScreenDialog.addContentView(mPlayerView, mockLayoutParams)
//            mFullscreenOverlays.values
//            mockView1.parent
//            viewGroup.removeView(mockView1)
//            utils.createLayoutParams()
//            mFullScreenDialog.addContentView(mockView1, mockLayoutParams)
//            mockView1.bringToFront()
//            mockView2.parent
//            viewGroup.removeView(mockView2)
//            utils.createLayoutParams()
//            mFullScreenDialog.addContentView(mockView2, mockLayoutParams)
//            mockView2.bringToFront()
//            mockView3.parent
//            viewGroup.removeView(mockView3)
//            utils.createLayoutParams()
//            mFullScreenDialog.addContentView(mockView3, mockLayoutParams)
//            mockView3.bringToFront()
//            mPlayerView.findViewById<ImageButton>(R.id.exo_fullscreen)
//            ContextCompat.getDrawable(mAppContext, R.drawable.FullScreenDrawableButtonCollapse)
//            fullScreenButton.setImageDrawable(drawable)
//            mFullScreenDialog.show()
//            utils.createTrackingVideoTypeData()
//            videoData.arcVideo = testObject.video
//            videoData.position = expectedPosition
//            mListener.onTrackingEvent(ON_OPEN_FULL_SCREEN, videoData)
//            trackingHelper.fullscreen()
//        }
//    }

//
//    @Test
//    fun `fullScreen dialog listener on something besides back pressed, given firstAdCompleted, controller not visible shows controller`() {
//        testObject.playVideo(createDefaultVideo())
//        val drawable = mockk<Drawable>()
//        val videoData = mockk<TrackingTypeData.TrackingVideoTypeData>(relaxed = true)
//        val viewGroup = mockk<ViewGroup>(relaxed = true)
//        val mFullScreenDialog = mockk<Dialog>(relaxed = true)
//        val mockLayoutParams = mockk<ViewGroup.LayoutParams>()
//        val listener = slot<DialogInterface.OnKeyListener>()
//        val keyEvent = mockk<KeyEvent> {
//            every { action } returns KeyEvent.ACTION_UP
//        }
//        every {
//            ContextCompat.getDrawable(
//                mAppContext,
//                R.drawable.FullScreenDrawableButton
//            )
//        } returns drawable
//        every { utils.createTrackingVideoTypeData() } returns videoData
//        every { utils.createFullScreenDialog(mAppContext) } returns mFullScreenDialog
//        every { utils.createLayoutParams() } returns mockLayoutParams
//        every { mPlayer.currentPosition } returns expectedPosition
//        every { mPlayerView.isControllerFullyVisible } returns false
//        every { mPlayerView.parent } returns viewGroup
//        every { mockView1.parent } returns viewGroup
//        every { mockView2.parent } returns viewGroup
//        every { mockView3.parent } returns viewGroup
//        testObject.setFullscreen(true)
//        verify { mFullScreenDialog.setOnKeyListener(capture(listener)) }
//        every { adEvent.type } returns SKIPPED
//        testObject.onAdEvent(adEvent)
//        clearAllMocks(answers = false)
//
//        assertFalse(listener.captured.onKey(mFullScreenDialog, KeyEvent.KEYCODE_B, keyEvent))
//
//        verifySequence {
//            mPlayerView.isControllerFullyVisible
//            mPlayerView.showController()
//        }
//    }
//
//    @Test
//    fun `fullScreen dialog listener on something besides back pressed, given ads not enabled, controller not visible shows controller`() {
//        testObject.playVideo(createDefaultVideo(shouldPlayAds = false))
//        val drawable = mockk<Drawable>()
//        val videoData = mockk<TrackingTypeData.TrackingVideoTypeData>(relaxed = true)
//        every { utils.createTrackingVideoTypeData() } returns videoData
//        val viewGroup = mockk<ViewGroup>(relaxed = true)
//        val mFullScreenDialog = mockk<Dialog>(relaxed = true)
//        val mockLayoutParams = mockk<ViewGroup.LayoutParams>()
//        val listener = slot<DialogInterface.OnKeyListener>()
//        val keyEvent = mockk<KeyEvent> {
//            every { action } returns KeyEvent.ACTION_UP
//        }
//        every {
//            ContextCompat.getDrawable(
//                mAppContext,
//                R.drawable.FullScreenDrawableButton
//            )
//        } returns drawable
//        every { utils.createFullScreenDialog(mAppContext) } returns mFullScreenDialog
//        every { utils.createLayoutParams() } returns mockLayoutParams
//        every { mPlayer.currentPosition } returns expectedPosition
//        every { mPlayerView.isControllerFullyVisible } returns false
//        every { mPlayerView.parent } returns viewGroup
//        every { mockView1.parent } returns viewGroup
//        every { mockView2.parent } returns viewGroup
//        every { mockView3.parent } returns viewGroup
//        testObject.setFullscreen(true)
//        verify { mFullScreenDialog.setOnKeyListener(capture(listener)) }
//        clearAllMocks(answers = false)
//
//        assertFalse(listener.captured.onKey(mFullScreenDialog, KeyEvent.KEYCODE_B, keyEvent))
//
//        verifySequence {
//            mPlayerView.isControllerFullyVisible
//            mPlayerView.showController()
//        }
//    }
//
//    @Test
//    fun `fullScreen dialog listener on something besides back pressed, controller visible does not show controller`() {
//        testObject.playVideo(createDefaultVideo(shouldPlayAds = false))
//        clearAllMocks(answers = false)
//        val drawable = mockk<Drawable>()
//        val videoData = mockk<TrackingTypeData.TrackingVideoTypeData>(relaxed = true)
//        val viewGroup = mockk<ViewGroup>(relaxed = true)
//        val mFullScreenDialog = mockk<Dialog>(relaxed = true)
//        val mockLayoutParams = mockk<ViewGroup.LayoutParams>()
//        val listener = slot<DialogInterface.OnKeyListener>()
//        val keyEvent = mockk<KeyEvent> {
//            every { action } returns KeyEvent.ACTION_UP
//        }
//        every {
//            ContextCompat.getDrawable(
//                mAppContext,
//                R.drawable.FullScreenDrawableButton
//            )
//        } returns drawable
//        every { utils.createTrackingVideoTypeData() } returns videoData
//        every { utils.createFullScreenDialog(mAppContext) } returns mFullScreenDialog
//        every { utils.createLayoutParams() } returns mockLayoutParams
//        every { mPlayer.currentPosition } returns expectedPosition
//        every { mPlayerView.isControllerFullyVisible } returns true
//        every { mPlayerView.parent } returns viewGroup
//        every { mockView1.parent } returns viewGroup
//        every { mockView2.parent } returns viewGroup
//        every { mockView3.parent } returns viewGroup
//        testObject.setFullscreen(true)
//        verify { mFullScreenDialog.setOnKeyListener(capture(listener)) }
//        clearAllMocks(answers = false)
//
//        assertFalse(listener.captured.onKey(mFullScreenDialog, KeyEvent.KEYCODE_B, keyEvent))
//
//        verifySequence { mPlayerView.isControllerFullyVisible }
//    }
//
//    @Test
//    fun `setFullscreen given false and using fullscreen dialog, does not call mListener`() {
//        val viewGroup = mockk<ViewGroup>(relaxed = true)
//        every { mConfig.isUseFullScreenDialog } returns true
//        every { mPlayerView.parent } returns viewGroup
//        every { mockView1.parent } returns viewGroup
//        every { mockView2.parent } returns viewGroup
//        every { mockView3.parent } returns viewGroup
//        testObject.playVideo(createDefaultVideo())
//
//        testObject.setFullscreen(false)
//
//        verify(exactly = 0) { mListener.setFullscreen(any()) }
//    }
//
//    @Test
//    fun `setFullscreen given true and not using fullscreen dialog, creates dialog, sets on key listener, calls mListener and sets fullscreen`() {
//        val drawable = mockk<Drawable>()
//        val videoData = mockk<TrackingTypeData.TrackingVideoTypeData>(relaxed = true)
//        val viewGroup = mockk<ViewGroup>(relaxed = true)
//        val mFullScreenDialog = mockk<Dialog>(relaxed = true)
//        val mockLayoutParams = mockk<ViewGroup.LayoutParams>()
//        every { mPlayer.currentPosition } returns expectedPosition
//        every {
//            ContextCompat.getDrawable(
//                mAppContext,
//                R.drawable.FullScreenDrawableButtonCollapse
//            )
//        } returns drawable
//        every { utils.createTrackingVideoTypeData() } returns videoData
//        every { utils.createFullScreenDialog(mAppContext) } returns mFullScreenDialog
//        every { utils.createLayoutParams() } returns mockLayoutParams
//        every { mPlayerView.parent } returns viewGroup
//        every { mockView1.parent } returns viewGroup
//        every { mockView2.parent } returns viewGroup
//        every { mockView3.parent } returns viewGroup
//        testObject.playVideo(createDefaultVideo())
//        clearAllMocks(answers = false)
//
//        assertFalse(testObject.isFullScreen)
//        testObject.setFullscreen(true)
//
//        verifySequence {
//            utils.createFullScreenDialog(mAppContext)
//            mFullScreenDialog.setOnKeyListener(any())
//            mPlayerView.parent
//            mPlayerView.parent
//            viewGroup.removeView(mPlayerView)
//            utils.createLayoutParams()
//            mFullScreenDialog.addContentView(mPlayerView, mockLayoutParams)
//            mFullscreenOverlays.values
//            mockView1.parent
//            viewGroup.removeView(mockView1)
//            utils.createLayoutParams()
//            mFullScreenDialog.addContentView(mockView1, mockLayoutParams)
//            mockView1.bringToFront()
//            mockView2.parent
//            viewGroup.removeView(mockView2)
//            utils.createLayoutParams()
//            mFullScreenDialog.addContentView(mockView2, mockLayoutParams)
//            mockView2.bringToFront()
//            mockView3.parent
//            viewGroup.removeView(mockView3)
//            utils.createLayoutParams()
//            mFullScreenDialog.addContentView(mockView3, mockLayoutParams)
//            mockView3.bringToFront()
//            mPlayerView.findViewById<ImageButton>(R.id.exo_fullscreen)
//            ContextCompat.getDrawable(mAppContext, R.drawable.FullScreenDrawableButtonCollapse)
//            fullScreenButton.setImageDrawable(drawable)
//            mFullScreenDialog.show()
//            utils.createTrackingVideoTypeData()
//            videoData.arcVideo = testObject.video
//            mPlayer.currentPosition
//            videoData.position = expectedPosition
//            mListener.onTrackingEvent(ON_OPEN_FULL_SCREEN, videoData)
//            trackingHelper.fullscreen()
//            mListener.setFullscreen(true)
//        }
//        assertTrue(testObject.isFullScreen)
//    }
//
//    @Test
//    fun `setFullscreen given false, but dialog is null, calls mListener`() {
//        testObject.setFullscreen(false)
//
//        verifySequence { mListener.setFullscreen(false) }
//    }
//
//    @Test
//    fun `setFullscreen given false, dialog not null, dismisses dialog and sets to normal screen`() {
//        testObject.playVideo(createDefaultVideo())
//        clearAllMocks(answers = false)
//        val drawable = mockk<Drawable>()
//        val videoData = mockk<TrackingTypeData.TrackingVideoTypeData>(relaxed = true)
//        val viewGroup = mockk<ViewGroup>(relaxed = true)
//        val mFullScreenDialog = mockk<Dialog>(relaxed = true)
//        val mockLayoutParams = mockk<ViewGroup.LayoutParams>()
//        every { mPlayer.currentPosition } returns expectedPosition
//        every {
//            ContextCompat.getDrawable(
//                mAppContext,
//                R.drawable.FullScreenDrawableButton
//            )
//        } returns drawable
//        every { utils.createTrackingVideoTypeData() } returns videoData
//        every { utils.createFullScreenDialog(mAppContext) } returns mFullScreenDialog
//        every { utils.createLayoutParams() } returns mockLayoutParams
//        every { mPlayerView.parent } returns viewGroup
//        every { mockView1.parent } returns viewGroup
//        every { mockView2.parent } returns viewGroup
//        every { mockView3.parent } returns viewGroup
//
//        assertFalse(testObject.isFullScreen)
//        testObject.setFullscreen(true)
//
//        clearAllMocks(answers = false)
//        every { mListener.isStickyPlayer } returns true
//        val playerFrame = mockk<RelativeLayout>(relaxed = true)
//        every { mListener.playerFrame } returns playerFrame
//
//        assertTrue(testObject.isFullScreen)
//        testObject.setFullscreen(false)
//
//        verifySequence {
//            mPlayerView.parent
//            mPlayerView.parent
//            viewGroup.removeView(mPlayerView)
//            mListener.playerFrame
//            playerFrame.addView(mPlayerView)
//            mFullscreenOverlays.values
//            viewGroup.removeView(mockView1)
//            mListener.playerFrame
//            playerFrame.addView(mockView1)
//            viewGroup.removeView(mockView2)
//            mListener.playerFrame
//            playerFrame.addView(mockView2)
//            viewGroup.removeView(mockView3)
//            mListener.playerFrame
//            playerFrame.addView(mockView3)
//            mPlayerView.findViewById<ImageButton>(R.id.exo_fullscreen)
//            ContextCompat.getDrawable(mAppContext, R.drawable.FullScreenDrawableButton)
//            fullScreenButton.setImageDrawable(drawable)
//            mListener.isStickyPlayer
//            mPlayerView.hideController()
//            mPlayerView.requestLayout()
//            mFullScreenDialog.dismiss()
//            utils.createTrackingVideoTypeData()
//            videoData.arcVideo = testObject.video
//            mPlayer.currentPosition
//            videoData.position = expectedPosition
//            mListener.onTrackingEvent(ON_CLOSE_FULL_SCREEN, videoData)
//            trackingHelper.normalScreen()
//            mListener.setFullscreen(false)
//        }
//        assertFalse(testObject.isFullScreen)
//    }
//
//    @Test
//    fun `fullScreen dialog on something besides Back Pressed, if keyListener non-null calls onKey`() {
//        val expectedKeyCode = KeyEvent.KEYCODE_0
//
//        testObject.playVideo(createDefaultVideo())
//        clearAllMocks(answers = false)
//        val drawable = mockk<Drawable>()
//        val videoData = mockk<TrackingTypeData.TrackingVideoTypeData>(relaxed = true)
//        val viewGroup = mockk<ViewGroup>(relaxed = true)
//        val mFullScreenDialog = mockk<Dialog>(relaxed = true)
//        val mockLayoutParams = mockk<ViewGroup.LayoutParams>()
//        val listener = slot<DialogInterface.OnKeyListener>()
//        val keyEvent = mockk<KeyEvent> {
//            every { action } returns KeyEvent.ACTION_UP
//        }
//        val keyListener = mockk<ArcKeyListener>(relaxed = true)
//        every {
//            ContextCompat.getDrawable(
//                mAppContext,
//                R.drawable.FullScreenDrawableButton
//            )
//        } returns drawable
//        every { utils.createTrackingVideoTypeData() } returns videoData
//        every { utils.createFullScreenDialog(mAppContext) } returns mFullScreenDialog
//        every { utils.createLayoutParams() } returns mockLayoutParams
//        every { mPlayer.currentPosition } returns expectedPosition
//        every { mPlayerView.isControllerFullyVisible } returns true
//        every { mPlayerView.parent } returns viewGroup
//        every { mockView1.parent } returns viewGroup
//        every { mockView2.parent } returns viewGroup
//        every { mockView3.parent } returns viewGroup
//        testObject.setFullscreen(true)
//        verify { mFullScreenDialog.setOnKeyListener(capture(listener)) }
//        clearAllMocks(answers = false)
//
//
//        testObject.setFullscreenListener(keyListener)
//
//        assertFalse(listener.captured.onKey(mFullScreenDialog, expectedKeyCode, keyEvent))
//        verifySequence {
//            keyListener.onKey(keyCode = expectedKeyCode, keyEvent = keyEvent)
//        }
//    }
//
//    @Test
//    fun `fullScreen dialog listener on Back Pressed, starts pip if enabled`() {
//        val drawable = mockk<Drawable>()
//        val videoData = mockk<TrackingTypeData.TrackingVideoTypeData>(relaxed = true)
//        val viewGroup = mockk<ViewGroup>(relaxed = true)
//        val mFullScreenDialog = mockk<Dialog>(relaxed = true)
//        val mockLayoutParams = mockk<ViewGroup.LayoutParams>()
//        val listener = slot<DialogInterface.OnKeyListener>()
//        val keyEvent = mockk<KeyEvent> {
//            every { action } returns KeyEvent.ACTION_UP
//        }
//        val playerFrame = mockk<RelativeLayout>(relaxed = true)
//        every {
//            ContextCompat.getDrawable(
//                mAppContext,
//                R.drawable.FullScreenDrawableButton
//            )
//        } returns drawable
//        every { utils.createFullScreenDialog(mAppContext) } returns mFullScreenDialog
//        every { utils.createLayoutParams() } returns mockLayoutParams
//        every { utils.createTrackingVideoTypeData() } returns videoData
//        every { mListener.isStickyPlayer } returns true
//        every { mListener.playerFrame } returns playerFrame
//        every { mPlayer.currentPosition } returns expectedPosition
//        every { mPlayer.playbackState } returns Player.STATE_READY
//        every { mPlayerView.isControllerFullyVisible } returns false
//        every { mPlayerView.parent } returns viewGroup
//        every { mockView1.parent } returns viewGroup
//        every { mockView2.parent } returns viewGroup
//        every { mockView3.parent } returns viewGroup
//        every { mVideoManager.isPipEnabled } returns true
//        val mVideo = createDefaultVideo()
//        testObject.playVideo(mVideo)
//        testObject.setFullscreen(true)
//        verify { mFullScreenDialog.setOnKeyListener(capture(listener)) }
//        clearAllMocks(answers = false)
//        assertTrue(testObject.isFullScreen)
//
//        assertFalse(listener.captured.onKey(mFullScreenDialog, KeyEvent.KEYCODE_BACK, keyEvent))
//
//        verifySequence {
//            mVideoManager.isPipEnabled
//            mVideoManager.isPipEnabled
//            mPlayerView.hideController()
//            mVideoManager.setSavedPosition(expectedId, expectedPosition)
//            mVideoManager.startPIP(mVideo)
//        }
//    }
//
//    @Test
//    fun `fullScreen dialog listener on Back Pressed, alerts key listener when pip disabled or unsupported`() {
//        val drawable = mockk<Drawable>()
//        val videoData = mockk<TrackingTypeData.TrackingVideoTypeData>(relaxed = true)
//        val viewGroup = mockk<ViewGroup>(relaxed = true)
//        val mFullScreenDialog = mockk<Dialog>(relaxed = true)
//        val mockLayoutParams = mockk<ViewGroup.LayoutParams>()
//        val listener = slot<DialogInterface.OnKeyListener>()
//        val keyEvent = mockk<KeyEvent> {
//            every { action } returns KeyEvent.ACTION_UP
//        }
//        val playerFrame = mockk<RelativeLayout>(relaxed = true)
//        every {
//            ContextCompat.getDrawable(
//                mAppContext,
//                R.drawable.FullScreenDrawableButton
//            )
//        } returns drawable
//        every { utils.createFullScreenDialog(mAppContext) } returns mFullScreenDialog
//        every { utils.createLayoutParams() } returns mockLayoutParams
//        every { utils.createTrackingVideoTypeData() } returns videoData
//        every { mListener.isStickyPlayer } returns true
//        every { mListener.playerFrame } returns playerFrame
//        every { mPlayer.currentPosition } returns expectedPosition
//        every { mPlayer.playbackState } returns Player.STATE_READY
//        every { mPlayerView.isControllerFullyVisible } returns false
//        every { mPlayerView.parent } returns viewGroup
//        every { mockView1.parent } returns viewGroup
//        every { mockView2.parent } returns viewGroup
//        every { mockView3.parent } returns viewGroup
//        every { mVideoManager.isPipEnabled } returns false
//        val mVideo = createDefaultVideo()
//        testObject.playVideo(mVideo)
//        testObject.setFullscreen(true)
//        val arcKeyListener = mockk<ArcKeyListener> {
//            every { onBackPressed() } just Runs
//        }
//        verify { mFullScreenDialog.setOnKeyListener(capture(listener)) }
//        clearAllMocks(answers = false)
//        assertTrue(testObject.isFullScreen)
//        testObject.setFullscreenListener(arcKeyListener)
//
//        assertFalse(listener.captured.onKey(mFullScreenDialog, KeyEvent.KEYCODE_BACK, keyEvent))
//
//        verifySequence {
//            mVideoManager.isPipEnabled
//            arcKeyListener.onBackPressed()
//        }
//    }
//
//    @Test
//    fun `fullScreen dialog listener on Back Pressed, returns true if keyAction is not up`() {
//        val drawable = mockk<Drawable>()
//        val videoData = mockk<TrackingTypeData.TrackingVideoTypeData>(relaxed = true)
//        val viewGroup = mockk<ViewGroup>(relaxed = true)
//        val mFullScreenDialog = mockk<Dialog>(relaxed = true)
//        val mockLayoutParams = mockk<ViewGroup.LayoutParams>()
//        val listener = slot<DialogInterface.OnKeyListener>()
//        val keyEvent = mockk<KeyEvent> {
//            every { action } returns KeyEvent.ACTION_DOWN
//        }
//        val playerFrame = mockk<RelativeLayout>(relaxed = true)
//        every {
//            ContextCompat.getDrawable(
//                mAppContext,
//                R.drawable.FullScreenDrawableButton
//            )
//        } returns drawable
//        every { utils.createFullScreenDialog(mAppContext) } returns mFullScreenDialog
//        every { utils.createLayoutParams() } returns mockLayoutParams
//        every { utils.createTrackingVideoTypeData() } returns videoData
//        every { mListener.isStickyPlayer } returns true
//        every { mListener.playerFrame } returns playerFrame
//        every { mPlayer.currentPosition } returns expectedPosition
//        every { mPlayer.playbackState } returns Player.STATE_READY
//        every { mPlayerView.isControllerFullyVisible } returns false
//        every { mPlayerView.parent } returns viewGroup
//        every { mockView1.parent } returns viewGroup
//        every { mockView2.parent } returns viewGroup
//        every { mockView3.parent } returns viewGroup
//        every { mVideoManager.isPipEnabled } returns true
//        val mVideo = createDefaultVideo()
//        testObject.playVideo(mVideo)
//        testObject.setFullscreen(true)
//        verify { mFullScreenDialog.setOnKeyListener(capture(listener)) }
//        clearAllMocks(answers = false)
//        assertTrue(testObject.isFullScreen)
//
//        assertTrue(listener.captured.onKey(mFullScreenDialog, KeyEvent.KEYCODE_BACK, keyEvent))
//    }
//
//    @Test
//    fun `fullScreen dialog listener on something besides back pressed, when arkKeyListener exists calls onKey`() {
//        testObject.playVideo(createDefaultVideo())
//        clearAllMocks(answers = false)
//        val drawable = mockk<Drawable>()
//        val videoData = mockk<TrackingTypeData.TrackingVideoTypeData>(relaxed = true)
//        val viewGroup = mockk<ViewGroup>(relaxed = true)
//        val mFullScreenDialog = mockk<Dialog>(relaxed = true)
//        val mockLayoutParams = mockk<ViewGroup.LayoutParams>()
//        val keyEvent = mockk<KeyEvent> {
//            every { action } returns KeyEvent.ACTION_UP
//        }
//        val keyListener = mockk<ArcKeyListener>(relaxed = true)
//        val listener = slot<DialogInterface.OnKeyListener>()
//        every {
//            ContextCompat.getDrawable(
//                mAppContext,
//                R.drawable.FullScreenDrawableButton
//            )
//        } returns drawable
//        every { utils.createTrackingVideoTypeData() } returns videoData
//        every { utils.createFullScreenDialog(mAppContext) } returns mFullScreenDialog
//        every { utils.createLayoutParams() } returns mockLayoutParams
//        every { mPlayer.currentPosition } returns expectedPosition
//        every { mPlayerView.isControllerFullyVisible } returns true
//        every { mPlayerView.parent } returns viewGroup
//        every { mockView1.parent } returns viewGroup
//        every { mockView2.parent } returns viewGroup
//        every { mockView3.parent } returns viewGroup
//        testObject.setFullscreen(true)
//        verify { mFullScreenDialog.setOnKeyListener(capture(listener)) }
//        testObject.setFullscreenListener(keyListener)
//
//        assertFalse(listener.captured.onKey(mFullScreenDialog, KeyEvent.KEYCODE_B, keyEvent))
//
//        verify(exactly = 1) { keyListener.onKey(KeyEvent.KEYCODE_B, keyEvent) }
//    }

//
//    private fun createAd(): Ad {
//        val inputAd = mockk<Ad>()
//        val expectedAdId = "ad id"
//        val expectedDuration = 1234.0
//        val expectedTitle = "title"
//        val expectedClickThroughUrl = "url"
//        return inputAd.apply {
//            every { adId } returns expectedAdId
//            every { duration } returns expectedDuration
//            every { title } returns expectedTitle
//            every { surveyUrl } returns expectedClickThroughUrl
//        }
//
//    }
//
//    private fun adBreakReadyAdEventToDisableControls() {
//        every { adEvent.type } returns AD_BREAK_READY
//        testObject.onAdEvent(adEvent)
//        assertTrue(testObject.isControlDisabled)
//    }
//
//    private fun testOnAdEventControlsDisabled(type: AdEvent.AdEventType) {
//        adBreakReadyAdEventToDisableControls()
//        every { adEvent.type } returns type
//        testObject.onAdEvent(adEvent)
//        assertFalse(testObject.isControlDisabled)
//    }
//
//// TODO leaving methods using deprecated exoplayer methods to test in https://arcpublishing.atlassian.net/browse/ARCMOBILE-3894
//// createMediaSource, onPlayerStateChanged, onPlayerStateChanged(update release test once we can populate videoTrackingSub), onTimelineChanged, onLoadingChanged
//
//
//
//

//


//




//
//    private fun playVideoThenVerify(arcVideo: ArcVideo) {
//        testObject.playVideo(arcVideo)
//        verifyPlayVideo(arcVideo)
//    }
//
//    private fun verifyPlayVideo(arcVideo: ArcVideo) {
//        verify {
////constructor
//            mConfig.activity
//            utils.createDefaultDataSourceFactory(mAppContext, "useragent")
//            mConfig.overlays
//            mConfig.overlays
//            mVideoManager.castManager
////public playVideo
////private playVideo
//            mVideoManager.initVideo(expectedId)
////initLocalPlayer
//            utils.createExoPlayerBuilder(mAppContext)
//            mockExoPlayerBuilder.apply {
//                setTrackSelector(any())
//                setSeekForwardIncrementMs(expectedIncrement.toLong())
//                setSeekBackIncrementMs(expectedIncrement.toLong())
//                setLooper(mockLooper)
//                build()
//            }
//            mPlayer.addListener(testObject)
//            utils.createPlayerView(mAppContext)
//            mPlayerView.apply {
//                id = R.id.wapo_player_view
//                player = mPlayer
//            }
//            mVideoManager.isAutoShowControls
//            mPlayerView.controllerAutoShow = true
//            mPlayerView.findViewById<TextView>(R.id.styled_controller_title_tv)
//            mPlayerView.findViewById<ImageButton>(R.id.exo_fullscreen)
//            fullScreenButton.setOnClickListener(any())
//            mPlayerView.findViewById<ImageButton>(R.id.exo_play)
//            mPlayerView.findViewById<ImageButton>(R.id.exo_pause)
//            mPlayerView.findViewById<ImageButton>(R.id.exo_share)
//            shareButton.setOnClickListener(any())
//            shareButton.visibility = VISIBLE
//            mPlayerView.findViewById<ImageButton>(R.id.exo_back)
//            mConfig.showBackButton
//            backButton.setOnClickListener(any())
//            mPlayerView.findViewById<ImageButton>(R.id.exo_pip)
//            pipButton.visibility = GONE
//            pipButton.setOnClickListener(any())
//            mPlayerView.findViewById<ImageButton>(R.id.exo_volume)
//            mPlayer.volume
//            volumeButton.setImageDrawable(any())
//            volumeButton.setOnClickListener(any())
//            mPlayerView.findViewById<ImageButton>(R.id.exo_cc)
//            mVideoManager.enableClosedCaption()
//            mConfig.isKeepControlsSpaceOnHide
//            ccButton.setOnClickListener(any())
//            ccButton.visibility = INVISIBLE
//            mPlayerView.findViewById<ImageButton>(R.id.exo_next_button)
//            mPlayerView.findViewById<ImageButton>(R.id.exo_prev_button)
//            mConfig.showNextPreviousButtons
//            mVideoManager.isShowSeekButton
//            mPlayerView.setShowFastForwardButton(true)
//            mVideoManager.isShowSeekButton
//            mPlayerView.setShowRewindButton(true)
//            mPlayerView.findViewById<View>(R.id.exo_position)
//            mPlayerView.findViewById<View>(R.id.exo_duration)
//            mPlayerView.findViewById<View>(R.id.exo_progress)
//            mPlayerView.findViewById<View>(R.id.time_bar_layout)
//            mVideoManager.isShowCountDown
//            exoDuration.visibility = VISIBLE
//            mVideoManager.isShowProgressBar
//            exoProgress.setScrubberColor(expectedTimeScrubColor)
//            exoProgress.setPlayedColor(expectedTimePlayColor)
//            exoProgress.setUnplayedColor(expectedTimeUnPlayedColor)
//            exoProgress.setBufferedColor(expectedBufferedColor)
//            exoProgress.setAdMarkerColor(expectedAdMarkerColor)
//            exoProgress.setPlayedAdMarkerColor(expectedAdPlayedColor)
//            exoTimeBarLayout.visibility = VISIBLE
//            exoPosition.visibility = VISIBLE
//            exoDuration.visibility = VISIBLE
//            exoProgress.visibility = VISIBLE
//            mPlayerView.requestFocus()
//            mConfig.controlsShowTimeoutMs
//            mConfig.isDisableControlsWithTouch
//            //END setUpPlayerControlListeners
//            mConfig.showTitleOnController
//            titleView.text = mHeadline
//            titleView.visibility = VISIBLE
//            mPlayerView.setOnTouchListener(any())
//            mListener.addVideoView(mPlayerView)
//            mFullscreenOverlays.values
//            mPlayerView.apply {
//                addView(mockView1)
//                addView(mockView2)
//                addView(mockView3)
//                findViewById<ImageButton>(R.id.exo_cc)
//            }
//            ContextCompat.getDrawable(mAppContext, R.drawable.CcOffDrawableButton)
//            ccButton.setImageDrawable(mockDrawable)
//
//            //END InitLocalPlayer
//            //initCastPlayer
//            utils.createCastPlayer(
//                mockCastContext,
//                expectedIncrement.toLong(),
//                expectedIncrement.toLong()
//            )
//            mCastPlayer.addListener(any())
//            mCastPlayer.setSessionAvailabilityListener(any())
//            utils.createPlayerControlView(mAppContext)
//            mCastControlView.id = R.id.wapo_cast_control_view
//            mCastControlView.player = mCastPlayer
//            mCastControlView.showTimeoutMs = -1
//            mCastControlView.findViewById<ImageButton>(R.id.exo_fullscreen)
//            mCastControlView.findViewById<ImageButton>(R.id.exo_pip)
//            mCastControlView.findViewById<ImageButton>(R.id.exo_share)
//            mCastControlView.findViewById<ImageButton>(R.id.exo_volume)
//            mCastControlView.findViewById<ImageButton>(R.id.exo_cc)
//            mCastControlView.findViewById<ImageButton>(R.id.exo_artwork)
//            artWork.visibility = VISIBLE
//            mConfig.artworkUrl
//            utils.loadImageIntoView(mAppContext, mArtWorkUrl, artWork)
//            castFullScreenButton.visibility = VISIBLE
//            castFullScreenButton.setOnClickListener(any())//TODO test click listener
//            castPipButton.visibility = GONE
//            castVolumeButton.visibility = VISIBLE
//            castVolumeButton.setOnClickListener(any())//TODO test click listener
//            ContextCompat.getDrawable(mAppContext, R.drawable.MuteOffDrawableButton)
//            castVolumeButton.setImageDrawable(mockDrawable)
//            castCcButton.visibility = VISIBLE
//            castCcButton.setOnClickListener(any())//TODO test click listener
//            ContextCompat.getDrawable(mAppContext, R.drawable.CcOffDrawableButton)
//            castCcButton.setImageDrawable(mockDrawable)
//            castShareButton.setOnClickListener(any())//TODO check if tested
//            castShareButton.visibility = VISIBLE
//            mListener.addVideoView(mCastControlView)
//            //End initCastPlayer
//            //setCurrentPlayer begins (sets current Player, mVideoTracker)
//
//            mCastPlayer.isCastSessionAvailable
//            mPlayerView.visibility = VISIBLE
//            mCastControlView.hide()
//            mCastControlView.keepScreenOn = false
//            //END setCurrentPlayer
//            //begin startVideoOnCurrentPlayer (null checks currentPlayer and mVideo)
//            mPlayer.playWhenReady = false
//
//            Uri.parse(eq("addTagUrl1234567"))
//            //initVideoCaptions Called
//            mPlayer.setMediaSource(any())//TODO
//            mPlayer.prepare()
//            mListener.getSavedPosition(expectedStartPosition.toString())
//            mPlayer.seekTo(expectedSavedPosition)
//            mPlayerView.setOnTouchListener(any())
//        }
//        verifyNoExceptions()
//    }
//
//
//    private fun verifyNoExceptions() =
//        verify(exactly = 0) {
//            Log.d(any(), any())
//            Log.e(any(), any())
//            mListener.onError(any(), any(), any())
//        }
//
//    private fun createDefaultVideo(shouldPlayAds: Boolean = true) = ArcVideo(
//        expectedId,
//        "uuid",
//        expectedStartPosition,
//        false,
//        false,
//        100,
//        mShareUrl,
//        mHeadline,
//        "pageName",
//        "videoName",
//        "videoSection",
//        "videoSource",
//        "videoCategory",
//        "consentId",
//        "fallbackUrl",
//        "addTagUrl[timestamp]",
//        shouldPlayAds,
//        subtitleUrl,
//        "source",
//        mockk(),
//        false,
//        false,
//        false,
//        ArcXPVideoConfig.CCStartMode.DEFAULT
//    )
//}