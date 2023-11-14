package com.arcxp.video.players

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.drawable.Drawable
import android.net.Uri
import android.widget.ImageButton
import androidx.core.content.ContextCompat
import com.arcxp.commons.testutils.TestUtils.createDefaultVideo
import com.arcxp.sdk.R
import com.arcxp.video.ArcXPVideoConfig
import com.arcxp.video.listeners.VideoListener
import com.arcxp.video.model.ArcVideoSDKErrorType
import com.arcxp.video.model.PlayerState
import com.arcxp.video.util.PrefManager
import com.arcxp.video.util.Utils
import com.arcxp.video.views.ArcTrackSelectionView
import com.google.ads.interactivemedia.v3.internal.mo
import com.google.ads.interactivemedia.v3.internal.va
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.MediaItem.SubtitleConfiguration
import com.google.android.exoplayer2.source.MergingMediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.source.SingleSampleMediaSource
import com.google.android.exoplayer2.source.dash.DashMediaSource
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.source.smoothstreaming.SsMediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.MappingTrackSelector
import com.google.android.exoplayer2.trackselection.TrackSelector
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.util.Util
import io.mockk.EqMatcher
import io.mockk.MockKAnnotations
import io.mockk.called
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.mockkStatic
import io.mockk.runs
import io.mockk.slot
import io.mockk.verify
import io.mockk.verifyOrder
import io.mockk.verifySequence
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.assertEquals
import kotlin.test.assertNull

@RunWith(RobolectricTestRunner::class)
internal class CaptionsManagerTest {

    @MockK
    private lateinit var playerState: PlayerState

    @MockK
    private lateinit var activity: Activity

    @MockK
    private lateinit var utils: Utils

    @MockK
    private lateinit var mConfig: ArcXPVideoConfig

    @RelaxedMockK
    private lateinit var mListener: VideoListener

    @RelaxedMockK
    private lateinit var ccButton: ImageButton


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
    private val expectedTitle = "title"
//    private val mediaItem = MediaItem.fromUri(expectedUrl)

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


    private lateinit var testObject: CaptionsManager

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        every { mConfig.activity } returns activity
        every { activity.getString(R.string.captions_dialog_title) } returns expectedTitle
        testObject = CaptionsManager(playerState, utils, mConfig, mListener)
        mockkStatic(Uri::class)
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }


    @Test
    fun `createMediaSourceWithCaptions throws exception, returns null, and is handled`() {
        val message = "createMediaSourceWithCaptions exception"
        val exception = Exception(message)
        val newId = "382764"
        every { Uri.parse(newId) } throws exception

        every { playerState.mVideo } throws exception


        assertNull(testObject.createMediaSourceWithCaptions())

        verifySequence {
            mListener.onError(ArcVideoSDKErrorType.EXOPLAYER_ERROR, message, exception)
        }
    }

    @Test
    fun `createMediaSourceWithCaptions HLS`() {
        val expected: MergingMediaSource = mockk()
        val newId = "382764"
        val expectedUri: Uri = mockk()
        val mediaItem = MediaItem.Builder().setUri(expectedUri).build()
        val expectedHlsMediaSource = mockk<HlsMediaSource>()
        val factory: DataSource.Factory = mockk()
        val singleSampleSource: SingleSampleMediaSource = mockk()
        val singleSampleSourceFactory: SingleSampleMediaSource.Factory = mockk()
        val config: SubtitleConfiguration = mockk()

        every { utils.createMediaItem(newId) } returns mediaItem
        mockkStatic(Util::class)
        every { Util.inferContentType(expectedUri) } returns C.CONTENT_TYPE_HLS
        mockkConstructor(HlsMediaSource.Factory::class)
        every {
            constructedWith<HlsMediaSource.Factory>(EqMatcher(factory)).createMediaSource(
                mediaItem
            )
        } returns expectedHlsMediaSource
        every { playerState.mMediaDataSourceFactory } returns factory
        every { playerState.mVideo } returns createDefaultVideo(id = newId)
        every { utils.createSubtitleConfig(newId, "subtitleUrl") } returns config
        every { utils.createSingleSampleMediaSourceFactory(factory) } returns singleSampleSourceFactory
        every { singleSampleSourceFactory.setTag(newId) } returns singleSampleSourceFactory
        every {
            singleSampleSourceFactory.createMediaSource(
                config,
                C.TIME_UNSET
            )
        } returns singleSampleSource
        every {
            utils.createMergingMediaSource(
                expectedHlsMediaSource,
                singleSampleSource
            )
        } returns expected

        val actual = testObject.createMediaSourceWithCaptions()

        assertEquals(expected, actual)
    }

    @Test
    fun `createMediaSourceWithCaptions SS`() {
        val expected: MergingMediaSource = mockk()
        val newId = "382764"
        val expectedUri: Uri = mockk()
        val mediaItem = MediaItem.Builder().setUri(expectedUri).build()
        val expectedSsMediaSource = mockk<SsMediaSource>()
        val factory: DataSource.Factory = mockk()
        val singleSampleSource: SingleSampleMediaSource = mockk()
        val singleSampleSourceFactory: SingleSampleMediaSource.Factory = mockk()
        val config: SubtitleConfiguration = mockk()

        every { utils.createMediaItem(newId) } returns mediaItem
        mockkStatic(Util::class)
        every { Util.inferContentType(expectedUri) } returns C.CONTENT_TYPE_SS
        mockkConstructor(SsMediaSource.Factory::class)
        every {
            constructedWith<SsMediaSource.Factory>(EqMatcher(factory)).createMediaSource(
                mediaItem
            )
        } returns expectedSsMediaSource
        every { playerState.mMediaDataSourceFactory } returns factory
        every { playerState.mVideo } returns createDefaultVideo(id = newId)
        every { utils.createSubtitleConfig(newId, "subtitleUrl") } returns config
        every { utils.createSingleSampleMediaSourceFactory(factory) } returns singleSampleSourceFactory
        every { singleSampleSourceFactory.setTag(newId) } returns singleSampleSourceFactory
        every {
            singleSampleSourceFactory.createMediaSource(
                config,
                C.TIME_UNSET
            )
        } returns singleSampleSource
        every {
            utils.createMergingMediaSource(
                expectedSsMediaSource,
                singleSampleSource
            )
        } returns expected

        val actual = testObject.createMediaSourceWithCaptions()

        assertEquals(expected, actual)
    }

    @Test
    fun `createMediaSourceWithCaptions DASH`() {
        val expected: MergingMediaSource = mockk()
        val newId = "382764"
        val expectedUri: Uri = mockk()
        val mediaItem = MediaItem.Builder().setUri(expectedUri).build()
        val expectedDashMediaSource = mockk<DashMediaSource>()
        val factory: DataSource.Factory = mockk()
        val singleSampleSource: SingleSampleMediaSource = mockk()
        val singleSampleSourceFactory: SingleSampleMediaSource.Factory = mockk()
        val config: SubtitleConfiguration = mockk()

        every { utils.createMediaItem(newId) } returns mediaItem
        mockkStatic(Util::class)
        every { Util.inferContentType(expectedUri) } returns C.CONTENT_TYPE_DASH
        mockkConstructor(DashMediaSource.Factory::class)
        every {
            constructedWith<DashMediaSource.Factory>(EqMatcher(factory)).createMediaSource(
                mediaItem
            )
        } returns expectedDashMediaSource
        every { playerState.mMediaDataSourceFactory } returns factory
        every { playerState.mVideo } returns createDefaultVideo(id = newId)
        every { utils.createSubtitleConfig(newId, "subtitleUrl") } returns config
        every { utils.createSingleSampleMediaSourceFactory(factory) } returns singleSampleSourceFactory
        every { singleSampleSourceFactory.setTag(newId) } returns singleSampleSourceFactory
        every {
            singleSampleSourceFactory.createMediaSource(
                config,
                C.TIME_UNSET
            )
        } returns singleSampleSource
        every {
            utils.createMergingMediaSource(
                expectedDashMediaSource,
                singleSampleSource
            )
        } returns expected

        val actual = testObject.createMediaSourceWithCaptions()

        assertEquals(expected, actual)
    }

    @Test
    fun `createMediaSourceWithCaptions OTHER`() {
        val expected: MergingMediaSource = mockk()
        val newId = "382764"
        val expectedUri: Uri = mockk()
        val mediaItem = MediaItem.Builder().setUri(expectedUri).build()
        val expectedProgressiveMediaSource = mockk<ProgressiveMediaSource>()
        val factory: DataSource.Factory = mockk()
        val singleSampleSource: SingleSampleMediaSource = mockk()
        val singleSampleSourceFactory: SingleSampleMediaSource.Factory = mockk()
        val config: SubtitleConfiguration = mockk()

        every { utils.createMediaItem(newId) } returns mediaItem
        mockkStatic(Util::class)
        every { Util.inferContentType(expectedUri) } returns C.CONTENT_TYPE_OTHER
        mockkConstructor(ProgressiveMediaSource.Factory::class)
        every {
            constructedWith<ProgressiveMediaSource.Factory>(EqMatcher(factory)).createMediaSource(
                mediaItem
            )
        } returns expectedProgressiveMediaSource
        every { playerState.mMediaDataSourceFactory } returns factory
        every { playerState.mVideo } returns createDefaultVideo(id = newId)
        every { utils.createSubtitleConfig(newId, "subtitleUrl") } returns config
        every { utils.createSingleSampleMediaSourceFactory(factory) } returns singleSampleSourceFactory
        every { singleSampleSourceFactory.setTag(newId) } returns singleSampleSourceFactory
        every {
            singleSampleSourceFactory.createMediaSource(
                config,
                C.TIME_UNSET
            )
        } returns singleSampleSource
        every {
            utils.createMergingMediaSource(
                expectedProgressiveMediaSource,
                singleSampleSource
            )
        } returns expected

        val actual = testObject.createMediaSourceWithCaptions()

        assertEquals(expected, actual)
    }

    @Test
    fun `createMediaSourceWithCaptions unknown type returns null`() {
        val newId = "382764"
        val expectedUri: Uri = mockk()
        val mediaItem = MediaItem.Builder().setUri(expectedUri).build()

        every { utils.createMediaItem(newId) } returns mediaItem
        mockkStatic(Util::class)
        every { Util.inferContentType(expectedUri) } returns C.CONTENT_TYPE_RTSP
        every { playerState.mVideo } returns createDefaultVideo(id = newId)

        assertNull(testObject.createMediaSourceWithCaptions())
    }

    @Test
    fun `createMediaSourceWithCaptions if localConfig is null returns null`() {
        val newId = "382764"
        val mediaItem = MediaItem.Builder().build()
        every { utils.createMediaItem(newId) } returns mediaItem
        every { playerState.mVideo } returns createDefaultVideo(id = newId)

        assertNull(testObject.createMediaSourceWithCaptions())
    }

    @Test
    fun `createMediaSourceWithCaptions when empty subtitle url`() {
        val expected = mockk<HlsMediaSource>()
        val newId = "382764"
        val expectedUri: Uri = mockk()
        val mediaItem = MediaItem.Builder().setUri(expectedUri).build()
        val factory: DataSource.Factory = mockk()

        every { utils.createMediaItem(newId) } returns mediaItem
        mockkStatic(Util::class)
        every { Util.inferContentType(expectedUri) } returns C.CONTENT_TYPE_HLS
        mockkConstructor(HlsMediaSource.Factory::class)
        every {
            constructedWith<HlsMediaSource.Factory>(EqMatcher(factory)).createMediaSource(
                mediaItem
            )
        } returns expected
        every { playerState.mMediaDataSourceFactory } returns factory
        every { playerState.mVideo } returns createDefaultVideo(id = newId, subtitleUrl = "")

        val actual = testObject.createMediaSourceWithCaptions()

        assertEquals(expected, actual)
    }

    @Test
    fun `createMediaSourceWithCaptions with null video returns null`() {
        every { playerState.mVideo } returns null

        assertNull(testObject.createMediaSourceWithCaptions())
    }

    @Test
    fun `showCaptionsSelectionDialog throws exception, and is handled`() {
        val message = "showCaptionsSelectionDialog exception"
        val exception = Exception(message)

        every { playerState.mTrackSelector } throws exception


        testObject.showCaptionsSelectionDialog()

        verifySequence {
            mListener.onError(ArcVideoSDKErrorType.EXOPLAYER_ERROR, message, exception)
        }
    }

    @Test
    fun `showCaptionsSelectionDialog ccButton is null`() {
        val expectedIndex = 2
        val mTrackSelector = mockk<DefaultTrackSelector>()
        val defaultTrackFilter = mockk<DefaultTrackFilter>()
        val pairFirst = mockk<AlertDialog>(relaxed = true)
        val pairSecond = mockk<ArcTrackSelectionView>(relaxed = true)

        every { playerState.mTrackSelector } returns mTrackSelector
        every { playerState.ccButton } returns null
        every { playerState.defaultTrackFilter } returns defaultTrackFilter
        every { mTrackSelector.currentMappedTrackInfo } returns mockk {
            every { rendererCount } returns 3
            every { getRendererType(0) } returns C.TRACK_TYPE_AUDIO
            every { getRendererType(1) } returns C.TRACK_TYPE_IMAGE
            every { getRendererType(expectedIndex) } returns C.TRACK_TYPE_TEXT
        }
        val javaPair = android.util.Pair.create(pairFirst, pairSecond)

        mockkStatic(ArcTrackSelectionView::class)
        every {
            ArcTrackSelectionView.getDialog(
                activity,
                expectedTitle,
                mTrackSelector,
                expectedIndex,
                defaultTrackFilter
            )
        } returns javaPair

        testObject.showCaptionsSelectionDialog()

        val onDismissListener = slot<DialogInterface.OnDismissListener>()
        verifySequence {
            pairSecond.setShowDisableOption(true)
            pairSecond.setAllowAdaptiveSelections(false)
            pairSecond.setShowDefault(false)
            pairFirst.show()
            pairFirst.setOnDismissListener(capture(onDismissListener))
        }
        verify { mListener wasNot called }
        val parameters = mockk<DefaultTrackSelector.Parameters>()
        every { mTrackSelector.parameters } returns parameters
        every { parameters.getRendererDisabled(expectedIndex) } returns true
        mockkStatic(PrefManager::class)
        every { PrefManager.saveBoolean(any(), any(), any()) } returns true

        onDismissListener.captured.onDismiss(mockk())
        //setVideoCaptionsEnabled
        verifySequence {
            PrefManager.saveBoolean(activity, PrefManager.IS_CAPTIONS_ENABLED, false)
        }


    }

    @Test
    fun `showCaptionsSelectionDialog text renderer index is not found`() {

        val mTrackSelector = mockk<DefaultTrackSelector>()
        val defaultTrackFilter = mockk<DefaultTrackFilter>()

        every { playerState.mTrackSelector } returns mTrackSelector
        every { mTrackSelector.currentMappedTrackInfo } returns mockk {
            every { rendererCount } returns 2
            every { getRendererType(0) } returns C.TRACK_TYPE_AUDIO
            every { getRendererType(1) } returns C.TRACK_TYPE_IMAGE
        }

        testObject.showCaptionsSelectionDialog()

        verifySequence {
            playerState.mTrackSelector
            playerState.mTrackSelector
            mTrackSelector.currentMappedTrackInfo
        }
        verify { mListener wasNot called }
        verify { mConfig wasNot called }


    }
    @Test
    fun `showCaptionsSelectionDialog mappedTrackInfo is null`() {

        val mTrackSelector = mockk<DefaultTrackSelector>()
        val defaultTrackFilter = mockk<DefaultTrackFilter>()

        every { playerState.mTrackSelector } returns mTrackSelector
        every { mTrackSelector.currentMappedTrackInfo } returns null

        testObject.showCaptionsSelectionDialog()

        verifySequence {
            playerState.mTrackSelector
            playerState.mTrackSelector
            mTrackSelector.currentMappedTrackInfo
        }
        verify { mListener wasNot called }
        verify { mConfig wasNot called }


    }

    @Test
    fun `showCaptionsSelectionDialog mTrackSelector is null`() {
        every { playerState.mTrackSelector } returns null
        testObject.showCaptionsSelectionDialog()
        verifySequence {
            playerState.mTrackSelector
        }
    }

    @Test
    fun `showCaptionsSelectionDialog captions are enabled`() {
        val expectedIndex = 2
        val mTrackSelector = mockk<DefaultTrackSelector>()
        val defaultTrackFilter = mockk<DefaultTrackFilter>()
        val pairFirst = mockk<AlertDialog>(relaxed = true)
        val pairSecond = mockk<ArcTrackSelectionView>(relaxed = true)

        every { playerState.mTrackSelector } returns mTrackSelector
        every { playerState.ccButton } returns ccButton
        every { playerState.defaultTrackFilter } returns defaultTrackFilter
        every { mTrackSelector.currentMappedTrackInfo } returns mockk {
            every { rendererCount } returns 3
            every { getRendererType(0) } returns C.TRACK_TYPE_AUDIO
            every { getRendererType(1) } returns C.TRACK_TYPE_IMAGE
            every { getRendererType(expectedIndex) } returns C.TRACK_TYPE_TEXT
        }
        val javaPair = android.util.Pair.create(pairFirst, pairSecond)

        mockkStatic(ArcTrackSelectionView::class)
        every {
            ArcTrackSelectionView.getDialog(
                activity,
                expectedTitle,
                mTrackSelector,
                expectedIndex,
                defaultTrackFilter
            )
        } returns javaPair

        testObject.showCaptionsSelectionDialog()

        val onDismissListener = slot<DialogInterface.OnDismissListener>()
        verifySequence {
            pairSecond.setShowDisableOption(true)
            pairSecond.setAllowAdaptiveSelections(false)
            pairSecond.setShowDefault(false)
            pairFirst.show()
            pairFirst.setOnDismissListener(capture(onDismissListener))
        }
        verify { mListener wasNot called }
        val parameters = mockk<DefaultTrackSelector.Parameters>()
        every { mTrackSelector.parameters } returns parameters
        every { parameters.getRendererDisabled(expectedIndex) } returns false
        mockkStatic(PrefManager::class)
        every { PrefManager.saveBoolean(any(), any(), any()) } returns true
        mockkStatic(ContextCompat::class)
        val drawable: Drawable = mockk()
        every { ContextCompat.getDrawable(activity, R.drawable.CcDrawableButton) } returns drawable


        onDismissListener.captured.onDismiss(mockk())
        //setVideoCaptionsEnabled
        verifySequence {
            PrefManager.saveBoolean(activity, PrefManager.IS_CAPTIONS_ENABLED, true)
            ccButton.setImageDrawable(drawable)
        }
    } @Test
    fun `showCaptionsSelectionDialog renderer index changes `() {
        val expectedIndex = 2
        val mTrackSelector = mockk<DefaultTrackSelector>()
        val defaultTrackFilter = mockk<DefaultTrackFilter>()
        val pairFirst = mockk<AlertDialog>(relaxed = true)
        val pairSecond = mockk<ArcTrackSelectionView>(relaxed = true)

        every { playerState.mTrackSelector } returns mTrackSelector
        every { playerState.ccButton } returns ccButton
        every { playerState.defaultTrackFilter } returns defaultTrackFilter
        every { mTrackSelector.currentMappedTrackInfo } returns mockk {
            every { rendererCount } returns 3
            every { getRendererType(0) } returns C.TRACK_TYPE_AUDIO
            every { getRendererType(1) } returns C.TRACK_TYPE_IMAGE
            every { getRendererType(expectedIndex) } returns C.TRACK_TYPE_TEXT
        }
        val javaPair = android.util.Pair.create(pairFirst, pairSecond)

        mockkStatic(ArcTrackSelectionView::class)
        every {
            ArcTrackSelectionView.getDialog(
                activity,
                expectedTitle,
                mTrackSelector,
                expectedIndex,
                defaultTrackFilter
            )
        } returns javaPair



        val onDismissListener = slot<DialogInterface.OnDismissListener>()
        testObject.showCaptionsSelectionDialog()
        verifySequence {
            pairSecond.setShowDisableOption(true)
            pairSecond.setAllowAdaptiveSelections(false)
            pairSecond.setShowDefault(false)
            pairFirst.show()
            pairFirst.setOnDismissListener(capture(onDismissListener))
        }
        verify { mListener wasNot called }
        val parameters = mockk<DefaultTrackSelector.Parameters>()
        every { mTrackSelector.parameters } returns parameters
        every { parameters.getRendererDisabled(expectedIndex) } returns true
        mockkStatic(PrefManager::class)
        every { PrefManager.saveBoolean(any(), any(), any()) } returns true
        mockkStatic(ContextCompat::class)
        val drawable: Drawable = mockk()
        every { ContextCompat.getDrawable(activity, R.drawable.CcOffDrawableButton) } returns drawable
        every { mTrackSelector.currentMappedTrackInfo } returns mockk {
            every { rendererCount } returns 2
            every { getRendererType(0) } returns C.TRACK_TYPE_AUDIO
            every { getRendererType(1) } returns C.TRACK_TYPE_IMAGE
        }

        onDismissListener.captured.onDismiss(mockk())
        //setVideoCaptionsEnabled
        verifySequence {
            PrefManager.saveBoolean(activity, PrefManager.IS_CAPTIONS_ENABLED, false)
            ccButton.setImageDrawable(drawable)
        }
    }@Test
    fun `showCaptionsSelectionDialog isVideoCaptionsEnabled throws exception, returns false `() {
        val expectedIndex = 2
        val mTrackSelector = mockk<DefaultTrackSelector>()
        val defaultTrackFilter = mockk<DefaultTrackFilter>()
        val pairFirst = mockk<AlertDialog>(relaxed = true)
        val pairSecond = mockk<ArcTrackSelectionView>(relaxed = true)

        every { playerState.mTrackSelector } returns mTrackSelector
        every { playerState.ccButton } returns ccButton
        every { playerState.defaultTrackFilter } returns defaultTrackFilter
        every { mTrackSelector.currentMappedTrackInfo } returns mockk {
            every { rendererCount } returns 3
            every { getRendererType(0) } returns C.TRACK_TYPE_AUDIO
            every { getRendererType(1) } returns C.TRACK_TYPE_IMAGE
            every { getRendererType(expectedIndex) } returns C.TRACK_TYPE_TEXT
        }
        val javaPair = android.util.Pair.create(pairFirst, pairSecond)

        mockkStatic(ArcTrackSelectionView::class)
        every {
            ArcTrackSelectionView.getDialog(
                activity,
                expectedTitle,
                mTrackSelector,
                expectedIndex,
                defaultTrackFilter
            )
        } returns javaPair



        val onDismissListener = slot<DialogInterface.OnDismissListener>()
        testObject.showCaptionsSelectionDialog()
        verifySequence {
            pairSecond.setShowDisableOption(true)
            pairSecond.setAllowAdaptiveSelections(false)
            pairSecond.setShowDefault(false)
            pairFirst.show()
            pairFirst.setOnDismissListener(capture(onDismissListener))
        }
        verify { mListener wasNot called }
        val parameters = mockk<DefaultTrackSelector.Parameters>()
        every { mTrackSelector.parameters } returns parameters
        every { parameters.getRendererDisabled(expectedIndex) } returns true
        mockkStatic(PrefManager::class)
        every { PrefManager.saveBoolean(any(), any(), any()) } returns true
        mockkStatic(ContextCompat::class)
        val drawable: Drawable = mockk()
        every { ContextCompat.getDrawable(activity, R.drawable.CcOffDrawableButton) } returns drawable
        every { mTrackSelector.currentMappedTrackInfo } throws Exception()

        onDismissListener.captured.onDismiss(mockk())
        //setVideoCaptionsEnabled
        verifySequence {
            PrefManager.saveBoolean(activity, PrefManager.IS_CAPTIONS_ENABLED, false)
            ccButton.setImageDrawable(drawable)
        }
    }

    @Test
    fun `showCaptionsSelectionDialog captions are disabled`() {
        val expectedIndex = 2
        val mTrackSelector = mockk<DefaultTrackSelector>()
        val defaultTrackFilter = mockk<DefaultTrackFilter>()
        val pairFirst = mockk<AlertDialog>(relaxed = true)
        val pairSecond = mockk<ArcTrackSelectionView>(relaxed = true)

        every { playerState.mTrackSelector } returns mTrackSelector
        every { playerState.ccButton } returns ccButton
        every { playerState.defaultTrackFilter } returns defaultTrackFilter
        every { mTrackSelector.currentMappedTrackInfo } returns mockk {
            every { rendererCount } returns 3
            every { getRendererType(0) } returns C.TRACK_TYPE_AUDIO
            every { getRendererType(1) } returns C.TRACK_TYPE_IMAGE
            every { getRendererType(expectedIndex) } returns C.TRACK_TYPE_TEXT
        }
        val javaPair = android.util.Pair.create(pairFirst, pairSecond)

        mockkStatic(ArcTrackSelectionView::class)
        every {
            ArcTrackSelectionView.getDialog(
                activity,
                expectedTitle,
                mTrackSelector,
                expectedIndex,
                defaultTrackFilter
            )
        } returns javaPair

        testObject.showCaptionsSelectionDialog()

        val onDismissListener = slot<DialogInterface.OnDismissListener>()
        verifySequence {
            pairSecond.setShowDisableOption(true)
            pairSecond.setAllowAdaptiveSelections(false)
            pairSecond.setShowDefault(false)
            pairFirst.show()
            pairFirst.setOnDismissListener(capture(onDismissListener))
        }
        verify { mListener wasNot called }
        val parameters = mockk<DefaultTrackSelector.Parameters>()
        every { mTrackSelector.parameters } returns parameters
        every { parameters.getRendererDisabled(expectedIndex) } returns true
        mockkStatic(PrefManager::class)
        every { PrefManager.saveBoolean(any(), any(), any()) } returns true
        mockkStatic(ContextCompat::class)
        val drawable: Drawable = mockk()
        every {
            ContextCompat.getDrawable(
                activity,
                R.drawable.CcOffDrawableButton
            )
        } returns drawable


        onDismissListener.captured.onDismiss(mockk())
        //setVideoCaptionsEnabled
        verifySequence {
            PrefManager.saveBoolean(activity, PrefManager.IS_CAPTIONS_ENABLED, false)
            ccButton.setImageDrawable(drawable)
        }
    }
}