package com.arcxp.video.players

import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.graphics.drawable.Drawable
import android.net.Uri
import android.util.Log
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
import androidx.media3.common.C
import androidx.media3.common.Format
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaItem.SubtitleConfiguration
import androidx.media3.exoplayer.source.MergingMediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.exoplayer.source.SingleSampleMediaSource
import androidx.media3.common.TrackGroup
import androidx.media3.exoplayer.source.TrackGroupArray
import androidx.media3.exoplayer.dash.DashMediaSource
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.exoplayer.smoothstreaming.SsMediaSource
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.exoplayer.trackselection.MappingTrackSelector
import androidx.media3.datasource.DataSource
import androidx.media3.common.util.Util
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
import io.mockk.verifySequence
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

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

    @RelaxedMockK
    private lateinit var mTrackSelector: DefaultTrackSelector

    private val expectedTitle = "title"

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
    }

    @Test
    fun `showCaptionsSelectionDialog renderer index changes `() {
        val expectedIndex = 2
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
        every {
            ContextCompat.getDrawable(
                activity,
                R.drawable.CcOffDrawableButton
            )
        } returns drawable
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
    }

    @Test
    fun `showCaptionsSelectionDialog isVideoCaptionsEnabled throws exception, returns false `() {
        val expectedIndex = 2
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
        every {
            ContextCompat.getDrawable(
                activity,
                R.drawable.CcOffDrawableButton
            )
        } returns drawable
        every { mTrackSelector.currentMappedTrackInfo } throws Exception()

        onDismissListener.captured.onDismiss(mockk())
        //setVideoCaptionsEnabled
        verifySequence {
            PrefManager.saveBoolean(activity, PrefManager.IS_CAPTIONS_ENABLED, false)
            ccButton.setImageDrawable(drawable)
        }
    }

    @Test
    fun `showCaptionsSelectionDialog getTextRendererIndex throws exception, returns -1 `() {
        val expectedIndex = 2
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
            every { getRendererType(expectedIndex) } throws Exception()
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

    }

    @Test
    fun `showCaptionsSelectionDialog captions are disabled`() {
        val expectedIndex = 2
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

    @Test
    fun `initVideoCaptions throws exception and is handled`() {
        val message = "error message 101"
        val exception = Exception(message)
        mockkStatic(PrefManager::class)
        every { PrefManager.getBoolean(any(), any(), any()) } throws exception

        testObject.initVideoCaptions()

        verifySequence {
            PrefManager.getBoolean(activity, PrefManager.IS_CAPTIONS_ENABLED, false)
            mListener.onError(ArcVideoSDKErrorType.EXOPLAYER_ERROR, message, exception)
        }

    }

    @Test
    fun `initVideoCaptions mTrackSelector is null`() {
        mockkStatic(PrefManager::class)
        every { PrefManager.getBoolean(any(), any(), any()) } returns true
        every { playerState.mTrackSelector } returns null

        testObject.initVideoCaptions()

        verifySequence {
            PrefManager.getBoolean(activity, PrefManager.IS_CAPTIONS_ENABLED, false)
            playerState.mTrackSelector
        }

    }

    @Test
    fun `initVideoCaptions mapped track info is null`() {
        mockkStatic(PrefManager::class)
        every { PrefManager.getBoolean(any(), any(), any()) } returns true
        every { playerState.mTrackSelector } returns mTrackSelector

        every { mTrackSelector.currentMappedTrackInfo } returns null


        testObject.initVideoCaptions()

        verifySequence {
            PrefManager.getBoolean(activity, PrefManager.IS_CAPTIONS_ENABLED, false)
            playerState.mTrackSelector
            playerState.mTrackSelector
            mTrackSelector.currentMappedTrackInfo
        }
    }

    @Test
    fun `initVideoCaptions has no text renderer`() {
        mockkStatic(PrefManager::class)
        every { PrefManager.getBoolean(any(), any(), any()) } returns true
        every { playerState.mTrackSelector } returns mTrackSelector

        every { mTrackSelector.currentMappedTrackInfo } returns mockk {
            every { rendererCount } returns 2
            every { getRendererType(0) } returns C.TRACK_TYPE_AUDIO
            every { getRendererType(1) } returns C.TRACK_TYPE_IMAGE
        }


        testObject.initVideoCaptions()

        verifySequence {
            PrefManager.getBoolean(activity, PrefManager.IS_CAPTIONS_ENABLED, false)
            playerState.mTrackSelector
            playerState.mTrackSelector
            mTrackSelector.currentMappedTrackInfo
        }
        verify(exactly = 0) {
            mTrackSelector.buildUponParameters()
        }
    }

    @Test
    fun `initVideoCaptions when captions disabled isShowClosedCaptionTrackSelection true`() {
        val expectedIndex = 2
        val parametersBuilder = mockk<DefaultTrackSelector.Parameters.Builder>(relaxed = true)
        every { mTrackSelector.buildUponParameters() } returns parametersBuilder
        every { mConfig.isShowClosedCaptionTrackSelection } returns true
        mockkStatic(PrefManager::class)
        every { PrefManager.getBoolean(any(), any(), any()) } returns false
        every { playerState.mTrackSelector } returns mTrackSelector

        every { mTrackSelector.currentMappedTrackInfo } returns mockk {
            every { rendererCount } returns 3
            every { getRendererType(0) } returns C.TRACK_TYPE_AUDIO
            every { getRendererType(1) } returns C.TRACK_TYPE_IMAGE
            every { getRendererType(expectedIndex) } returns C.TRACK_TYPE_TEXT
        }


        testObject.initVideoCaptions()

        verifySequence {
            mConfig.activity
            PrefManager.getBoolean(activity, PrefManager.IS_CAPTIONS_ENABLED, false)
            playerState.mTrackSelector
            playerState.mTrackSelector
            mTrackSelector.currentMappedTrackInfo
            playerState.mTrackSelector
            mTrackSelector.buildUponParameters()
            parametersBuilder.clearSelectionOverrides(expectedIndex)
            parametersBuilder.setRendererDisabled(expectedIndex, true)
            playerState.mTrackSelector
            mTrackSelector.setParameters(parametersBuilder)
            mConfig.isShowClosedCaptionTrackSelection
        }
    }

    @Test
    fun `initVideoCaptions when captions disabled isShowClosedCaptionTrackSelection false but ccButton is null`() {
        val expectedIndex = 2
        val parametersBuilder = mockk<DefaultTrackSelector.Parameters.Builder>(relaxed = true)
        every { mTrackSelector.buildUponParameters() } returns parametersBuilder
        every { mConfig.isShowClosedCaptionTrackSelection } returns false
        mockkStatic(PrefManager::class)
        every { PrefManager.getBoolean(any(), any(), any()) } returns false
        every { playerState.mTrackSelector } returns mTrackSelector
        every { playerState.ccButton } returns null

        every { mTrackSelector.currentMappedTrackInfo } returns mockk {
            every { rendererCount } returns 3
            every { getRendererType(0) } returns C.TRACK_TYPE_AUDIO
            every { getRendererType(1) } returns C.TRACK_TYPE_IMAGE
            every { getRendererType(expectedIndex) } returns C.TRACK_TYPE_TEXT
        }


        testObject.initVideoCaptions()

        verifySequence {
            mConfig.activity
            PrefManager.getBoolean(activity, PrefManager.IS_CAPTIONS_ENABLED, false)
            playerState.mTrackSelector
            playerState.mTrackSelector
            mTrackSelector.currentMappedTrackInfo
            playerState.mTrackSelector
            mTrackSelector.buildUponParameters()
            parametersBuilder.clearSelectionOverrides(expectedIndex)
            parametersBuilder.setRendererDisabled(expectedIndex, true)
            playerState.mTrackSelector
            mTrackSelector.setParameters(parametersBuilder)
            mConfig.isShowClosedCaptionTrackSelection
            playerState.ccButton
        }
    }

    @Test
    fun `initVideoCaptions when captions disabled isShowClosedCaptionTrackSelection false`() {
        val expectedIndex = 2
        val parametersBuilder = mockk<DefaultTrackSelector.Parameters.Builder>(relaxed = true)
        every { mTrackSelector.buildUponParameters() } returns parametersBuilder
        every { mConfig.isShowClosedCaptionTrackSelection } returns false
        mockkStatic(PrefManager::class)
        every { PrefManager.getBoolean(any(), any(), any()) } returns false
        every { playerState.mTrackSelector } returns mTrackSelector
        every { playerState.ccButton } returns ccButton
        val drawable: Drawable = mockk()
        mockkStatic(ContextCompat::class)
        every {
            ContextCompat.getDrawable(
                activity,
                R.drawable.CcOffDrawableButton
            )
        } returns drawable

        every { mTrackSelector.currentMappedTrackInfo } returns mockk {
            every { rendererCount } returns 3
            every { getRendererType(0) } returns C.TRACK_TYPE_AUDIO
            every { getRendererType(1) } returns C.TRACK_TYPE_IMAGE
            every { getRendererType(expectedIndex) } returns C.TRACK_TYPE_TEXT
        }


        testObject.initVideoCaptions()

        verifySequence {
            mConfig.activity
            PrefManager.getBoolean(activity, PrefManager.IS_CAPTIONS_ENABLED, false)
            playerState.mTrackSelector
            playerState.mTrackSelector
            mTrackSelector.currentMappedTrackInfo
            playerState.mTrackSelector
            mTrackSelector.buildUponParameters()
            parametersBuilder.clearSelectionOverrides(expectedIndex)
            parametersBuilder.setRendererDisabled(expectedIndex, true)
            playerState.mTrackSelector
            mTrackSelector.setParameters(parametersBuilder)
            mConfig.isShowClosedCaptionTrackSelection
            playerState.ccButton
            mConfig.activity
            ccButton.setImageDrawable(drawable)
        }
    }

    @Test
    fun `initVideoCaptions when captions enabled isShowClosedCaptionTrackSelection false`() {
        val expectedIndex = 2
        val parametersBuilder = mockk<DefaultTrackSelector.Parameters.Builder>(relaxed = true)
        val override = mockk<DefaultTrackSelector.SelectionOverride>()
        val trackGroups = TrackGroupArray(mockk())

        every { mTrackSelector.currentMappedTrackInfo } returns mockk {
            every { rendererCount } returns 3
            every { getRendererType(0) } returns C.TRACK_TYPE_AUDIO
            every { getRendererType(1) } returns C.TRACK_TYPE_IMAGE
            every { getRendererType(expectedIndex) } returns C.TRACK_TYPE_TEXT
            every { getTrackGroups(expectedIndex) } returns trackGroups
        }
        every { utils.createSelectionOverride(0, 0) } returns override


        every { mTrackSelector.buildUponParameters() } returns parametersBuilder
        every { mConfig.isShowClosedCaptionTrackSelection } returns false
        mockkStatic(PrefManager::class)
        every { PrefManager.getBoolean(any(), any(), any()) } returns true
        every { playerState.mTrackSelector } returns mTrackSelector
        every { playerState.ccButton } returns ccButton
        val drawable: Drawable = mockk()
        mockkStatic(ContextCompat::class)
        every {
            ContextCompat.getDrawable(
                activity,
                R.drawable.CcDrawableButton
            )
        } returns drawable

        testObject.initVideoCaptions()

        verifySequence {
            mConfig.activity
            PrefManager.getBoolean(activity, PrefManager.IS_CAPTIONS_ENABLED, false)
            playerState.mTrackSelector
            playerState.mTrackSelector
            mTrackSelector.currentMappedTrackInfo
            playerState.mTrackSelector
            mTrackSelector.buildUponParameters()
            parametersBuilder.setSelectionOverride(expectedIndex, trackGroups, override)
            parametersBuilder.setRendererDisabled(expectedIndex, false)
            playerState.mTrackSelector
            mTrackSelector.setParameters(parametersBuilder)
            mConfig.isShowClosedCaptionTrackSelection
            playerState.ccButton
            mConfig.activity
            ccButton.setImageDrawable(drawable)
        }
    }

    @Test
    fun `initVideoCaptions when captions enabled isShowClosedCaptionTrackSelection false but ccButton is null`() {
        val expectedIndex = 2
        val parametersBuilder = mockk<DefaultTrackSelector.Parameters.Builder>(relaxed = true)
        val override = mockk<DefaultTrackSelector.SelectionOverride>()
        val trackGroups = TrackGroupArray(mockk())

        every { mTrackSelector.currentMappedTrackInfo } returns mockk {
            every { rendererCount } returns 3
            every { getRendererType(0) } returns C.TRACK_TYPE_AUDIO
            every { getRendererType(1) } returns C.TRACK_TYPE_IMAGE
            every { getRendererType(expectedIndex) } returns C.TRACK_TYPE_TEXT
            every { getTrackGroups(expectedIndex) } returns trackGroups
        }
        every { utils.createSelectionOverride(0, 0) } returns override


        every { mTrackSelector.buildUponParameters() } returns parametersBuilder
        every { mConfig.isShowClosedCaptionTrackSelection } returns false
        mockkStatic(PrefManager::class)
        every { PrefManager.getBoolean(any(), any(), any()) } returns true
        every { playerState.mTrackSelector } returns mTrackSelector
        every { playerState.ccButton } returns null
        val drawable: Drawable = mockk()
        mockkStatic(ContextCompat::class)
        every {
            ContextCompat.getDrawable(
                activity,
                R.drawable.CcDrawableButton
            )
        } returns drawable

        testObject.initVideoCaptions()

        verifySequence {
            mConfig.activity
            PrefManager.getBoolean(activity, PrefManager.IS_CAPTIONS_ENABLED, false)
            playerState.mTrackSelector
            playerState.mTrackSelector
            mTrackSelector.currentMappedTrackInfo
            playerState.mTrackSelector
            mTrackSelector.buildUponParameters()
            parametersBuilder.setSelectionOverride(expectedIndex, trackGroups, override)
            parametersBuilder.setRendererDisabled(expectedIndex, false)
            playerState.mTrackSelector
            mTrackSelector.setParameters(parametersBuilder)
            mConfig.isShowClosedCaptionTrackSelection
            playerState.ccButton
        }
    }

    @Test
    fun `initVideoCaptions when captions enabled isShowClosedCaptionTrackSelection true`() {
        val expectedIndex = 2
        val parametersBuilder = mockk<DefaultTrackSelector.Parameters.Builder>(relaxed = true)
        val override = mockk<DefaultTrackSelector.SelectionOverride>()
        val trackGroups = TrackGroupArray(mockk())
        every { mTrackSelector.buildUponParameters() } returns parametersBuilder
        every { mConfig.isShowClosedCaptionTrackSelection } returns true
        mockkStatic(PrefManager::class)
        every { PrefManager.getBoolean(any(), any(), any()) } returns true
        every { playerState.mTrackSelector } returns mTrackSelector

        every { mTrackSelector.currentMappedTrackInfo } returns mockk {
            every { rendererCount } returns 3
            every { getRendererType(0) } returns C.TRACK_TYPE_AUDIO
            every { getRendererType(1) } returns C.TRACK_TYPE_IMAGE
            every { getRendererType(expectedIndex) } returns C.TRACK_TYPE_TEXT
            every { getTrackGroups(expectedIndex) } returns trackGroups
        }
        every { utils.createSelectionOverride(0, 0) } returns override

        testObject.initVideoCaptions()

        verifySequence {
            mConfig.activity
            PrefManager.getBoolean(activity, PrefManager.IS_CAPTIONS_ENABLED, false)
            playerState.mTrackSelector
            playerState.mTrackSelector
            mTrackSelector.currentMappedTrackInfo
            playerState.mTrackSelector
            mTrackSelector.buildUponParameters()
            parametersBuilder.setSelectionOverride(expectedIndex, trackGroups, override)
            parametersBuilder.setRendererDisabled(expectedIndex, false)
            playerState.mTrackSelector
            mTrackSelector.setParameters(parametersBuilder)
            mConfig.isShowClosedCaptionTrackSelection
        }
    }

    @Test
    fun `toggleClosedCaption with null track selector`() {
        every { playerState.mTrackSelector } returns null
        testObject.toggleClosedCaption()
        verifySequence {
            playerState.mTrackSelector
        }
    }

    @Test
    fun `toggleClosedCaption with null mapped tracks`() {
        every { playerState.mTrackSelector } returns mTrackSelector

        every { mTrackSelector.currentMappedTrackInfo } returns null
        testObject.toggleClosedCaption()
        verifySequence {
            playerState.mTrackSelector
            playerState.mTrackSelector
            mTrackSelector.currentMappedTrackInfo
        }
    }

    @Test
    fun `toggleClosedCaption with no text renderer`() {
        every { playerState.mTrackSelector } returns mTrackSelector

        every { mTrackSelector.currentMappedTrackInfo } returns mockk {
            every { rendererCount } returns 2
            every { getRendererType(0) } returns C.TRACK_TYPE_AUDIO
            every { getRendererType(1) } returns C.TRACK_TYPE_IMAGE
        }
        testObject.toggleClosedCaption()
        verifySequence {
            playerState.mTrackSelector
            playerState.mTrackSelector
            mTrackSelector.currentMappedTrackInfo
        }
    }

    @Test
    fun `toggleClosedCaption show is false`() {
        val expectedIndex = 2
        val parameters = mockk<DefaultTrackSelector.Parameters>()
        val parametersBuilder = mockk<DefaultTrackSelector.Parameters.Builder>(relaxed = true)
        every { mTrackSelector.buildUponParameters() } returns parametersBuilder
        every { playerState.mTrackSelector } returns mTrackSelector
        every { playerState.ccButton } returns null
        every { mConfig.isLoggingEnabled } returns true
        every { mTrackSelector.currentMappedTrackInfo } returns mockk {
            every { rendererCount } returns 3
            every { getRendererType(0) } returns C.TRACK_TYPE_AUDIO
            every { getRendererType(1) } returns C.TRACK_TYPE_IMAGE
            every { getRendererType(expectedIndex) } returns C.TRACK_TYPE_TEXT
        }
        every { mTrackSelector.parameters } returns parameters
        every { parameters.getRendererDisabled(expectedIndex) } returns false
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        mockkStatic(PrefManager::class)
        every { PrefManager.saveBoolean(any(), any(), any()) } returns true

        testObject.toggleClosedCaption()

        verifySequence {
            playerState.mTrackSelector
            playerState.mTrackSelector
            mTrackSelector.currentMappedTrackInfo

            playerState.mTrackSelector
            mTrackSelector.parameters
            parameters.getRendererDisabled(expectedIndex)
            playerState.mTrackSelector
            mTrackSelector.buildUponParameters()
            mConfig.isLoggingEnabled
            Log.d("ArcVideoSDK", "Toggling CC off")
            parametersBuilder.clearSelectionOverrides(expectedIndex)
            parametersBuilder.setRendererDisabled(expectedIndex, true)
            playerState.mTrackSelector
            mTrackSelector.setParameters(parametersBuilder)
            mConfig.activity
            PrefManager.saveBoolean(
                activity,
                PrefManager.IS_CAPTIONS_ENABLED,
                false
            )
            playerState.ccButton
        }
    }

    @Test
    fun `toggleClosedCaption show is false no logging`() {
        val expectedIndex = 2
        val parameters = mockk<DefaultTrackSelector.Parameters>()
        val parametersBuilder = mockk<DefaultTrackSelector.Parameters.Builder>(relaxed = true)
        every { mTrackSelector.buildUponParameters() } returns parametersBuilder
        every { playerState.mTrackSelector } returns mTrackSelector
        every { playerState.ccButton } returns null
        every { mConfig.isLoggingEnabled } returns false
        every { mTrackSelector.currentMappedTrackInfo } returns mockk {
            every { rendererCount } returns 3
            every { getRendererType(0) } returns C.TRACK_TYPE_AUDIO
            every { getRendererType(1) } returns C.TRACK_TYPE_IMAGE
            every { getRendererType(expectedIndex) } returns C.TRACK_TYPE_TEXT
        }
        every { mTrackSelector.parameters } returns parameters
        every { parameters.getRendererDisabled(expectedIndex) } returns false
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        mockkStatic(PrefManager::class)
        every { PrefManager.saveBoolean(any(), any(), any()) } returns true

        testObject.toggleClosedCaption()

        verify(exactly = 0) {
            Log.d(any(), any())
        }
    }

    @Test
    fun `toggleClosedCaption show is true`() {
        val expectedIndex = 2
        val parameters = mockk<DefaultTrackSelector.Parameters>()
        val parametersBuilder = mockk<DefaultTrackSelector.Parameters.Builder>(relaxed = true)
        val override = mockk<DefaultTrackSelector.SelectionOverride>()
        val trackGroups = TrackGroupArray(mockk())
        every { mTrackSelector.buildUponParameters() } returns parametersBuilder
        every { playerState.mTrackSelector } returns mTrackSelector
        every { playerState.ccButton } returns null
        every { mConfig.isLoggingEnabled } returns true
        every { mTrackSelector.currentMappedTrackInfo } returns mockk {
            every { rendererCount } returns 3
            every { getRendererType(0) } returns C.TRACK_TYPE_AUDIO
            every { getRendererType(1) } returns C.TRACK_TYPE_IMAGE
            every { getRendererType(expectedIndex) } returns C.TRACK_TYPE_TEXT
            every { getTrackGroups(expectedIndex) } returns trackGroups
        }
        every { mTrackSelector.parameters } returns parameters
        every { parameters.getRendererDisabled(expectedIndex) } returns true
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        mockkStatic(PrefManager::class)
        every { PrefManager.saveBoolean(any(), any(), any()) } returns true
        every { utils.createSelectionOverride(0, 0) } returns override
        testObject.toggleClosedCaption()

        verifySequence {
            playerState.mTrackSelector
            playerState.mTrackSelector
            mTrackSelector.currentMappedTrackInfo

            playerState.mTrackSelector
            mTrackSelector.parameters
            parameters.getRendererDisabled(expectedIndex)
            playerState.mTrackSelector
            mTrackSelector.buildUponParameters()
            mConfig.isLoggingEnabled
            Log.d("ArcVideoSDK", "Toggling CC on")
            parametersBuilder.setSelectionOverride(expectedIndex, trackGroups, override)
            parametersBuilder.setRendererDisabled(expectedIndex, false)
            playerState.mTrackSelector
            mTrackSelector.setParameters(parametersBuilder)
            mConfig.activity
            PrefManager.saveBoolean(
                activity,
                PrefManager.IS_CAPTIONS_ENABLED,
                true
            )
            playerState.ccButton
        }
    }

    @Test
    fun `toggleClosedCaption show is true no logging`() {
        val expectedIndex = 2
        val parameters = mockk<DefaultTrackSelector.Parameters>()
        val parametersBuilder = mockk<DefaultTrackSelector.Parameters.Builder>(relaxed = true)
        val override = mockk<DefaultTrackSelector.SelectionOverride>()
        val trackGroups = TrackGroupArray(mockk())
        every { mTrackSelector.buildUponParameters() } returns parametersBuilder
        every { playerState.mTrackSelector } returns mTrackSelector
        every { playerState.ccButton } returns null
        every { mConfig.isLoggingEnabled } returns false
        every { mTrackSelector.currentMappedTrackInfo } returns mockk {
            every { rendererCount } returns 3
            every { getRendererType(0) } returns C.TRACK_TYPE_AUDIO
            every { getRendererType(1) } returns C.TRACK_TYPE_IMAGE
            every { getRendererType(expectedIndex) } returns C.TRACK_TYPE_TEXT
            every { getTrackGroups(expectedIndex) } returns trackGroups
        }
        every { mTrackSelector.parameters } returns parameters
        every { parameters.getRendererDisabled(expectedIndex) } returns true
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        mockkStatic(PrefManager::class)
        every { PrefManager.saveBoolean(any(), any(), any()) } returns true
        every { utils.createSelectionOverride(0, 0) } returns override
        testObject.toggleClosedCaption()

        verify(exactly = 0) {
            Log.d(any(), any())
        }
    }

    @Test
    fun `isClosedCaptionAvailable throws exception returns false`() {
        every { playerState.mTrackSelector } throws Exception()
        assertFalse(testObject.isClosedCaptionAvailable())
    }

    @Test
    fun `isClosedCaptionAvailable track selector null returns false`() {
        every { playerState.mTrackSelector } returns null
        assertFalse(testObject.isClosedCaptionAvailable())
    }

    @Test
    fun `isClosedCaptionAvailable mappedTrackInfo null returns false`() {
        every { playerState.mTrackSelector } returns mockk {
            every { currentMappedTrackInfo } returns null
        }
        assertFalse(testObject.isClosedCaptionAvailable())
    }

    @Test
    fun `isClosedCaptionAvailable returns true if has tracks`() {
        val expectedIndex = 2
        val format: Format = mockk()
        val trackGroup = TrackGroup(format)

        val trackGroups = TrackGroupArray(trackGroup)
        every { playerState.mTrackSelector } returns mTrackSelector
        every { playerState.defaultTrackFilter } returns mockk {
            every { filter(format, trackGroups) } returns true
        }


        val mappedTrackInfo = mockk<MappingTrackSelector.MappedTrackInfo> {
            every { rendererCount } returns 3
            every { getRendererType(0) } returns C.TRACK_TYPE_AUDIO
            every { getRendererType(1) } returns C.TRACK_TYPE_IMAGE
            every { getRendererType(expectedIndex) } returns C.TRACK_TYPE_TEXT
            every { getTrackGroups(expectedIndex) } returns trackGroups
        }
        every { mTrackSelector.currentMappedTrackInfo } returns mappedTrackInfo
        assertTrue(testObject.isClosedCaptionAvailable())
    }

    @Test
    fun `isClosedCaptionAvailable returns false if has no tracks`() {
        val expectedIndex = 2
        val format: Format = mockk()
        val trackGroup = TrackGroup(format)

        val trackGroups = TrackGroupArray(trackGroup)
        every { playerState.mTrackSelector } returns mTrackSelector
        every { playerState.defaultTrackFilter } returns mockk {
            every { filter(format, trackGroups) } returns false
        }

        val mappedTrackInfo = mockk<MappingTrackSelector.MappedTrackInfo> {
            every { rendererCount } returns 3
            every { getRendererType(0) } returns C.TRACK_TYPE_AUDIO
            every { getRendererType(1) } returns C.TRACK_TYPE_IMAGE
            every { getRendererType(expectedIndex) } returns C.TRACK_TYPE_TEXT
            every { getTrackGroups(expectedIndex) } returns trackGroups
        }
        every { mTrackSelector.currentMappedTrackInfo } returns mappedTrackInfo
        assertFalse(testObject.isClosedCaptionAvailable())
    }

    @Test
    fun `isClosedCaptionAvailable if hasAvailableSubtitlesTracks throws exception returns false logging enabled`() {
        val exception = Exception("serious error111")
        val expectedIndex = 2
        val format: Format = mockk()
        val trackGroup = TrackGroup(format)

        val trackGroups = TrackGroupArray(trackGroup)
        every { mConfig.isLoggingEnabled } returns true
        every { playerState.mTrackSelector } returns mTrackSelector
        every { playerState.defaultTrackFilter } returns mockk {
            every { filter(format, trackGroups) } throws exception
        }
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0


        val mappedTrackInfo = mockk<MappingTrackSelector.MappedTrackInfo> {
            every { rendererCount } returns 3
            every { getRendererType(0) } returns C.TRACK_TYPE_AUDIO
            every { getRendererType(1) } returns C.TRACK_TYPE_IMAGE
            every { getRendererType(expectedIndex) } returns C.TRACK_TYPE_TEXT
            every { getTrackGroups(expectedIndex) } returns trackGroups
        }
        every { mTrackSelector.currentMappedTrackInfo } returns mappedTrackInfo
        assertFalse(testObject.isClosedCaptionAvailable())
        verify(exactly = 1) {
            Log.d("ArcVideoSDK", "Exception thrown detecting CC tracks. serious error111")
        }
    }

    @Test
    fun `isClosedCaptionAvailable if hasAvailableSubtitlesTracks throws exception returns false logging disabled`() {
        val exception = Exception("serious error111")
        val expectedIndex = 2
        val format: Format = mockk()
        val trackGroup = TrackGroup(format)

        val trackGroups = TrackGroupArray(trackGroup)
        every { mConfig.isLoggingEnabled } returns false
        every { playerState.mTrackSelector } returns mTrackSelector
        every { playerState.defaultTrackFilter } returns mockk {
            every { filter(format, trackGroups) } throws exception
        }
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0


        val mappedTrackInfo = mockk<MappingTrackSelector.MappedTrackInfo> {
            every { rendererCount } returns 3
            every { getRendererType(0) } returns C.TRACK_TYPE_AUDIO
            every { getRendererType(1) } returns C.TRACK_TYPE_IMAGE
            every { getRendererType(expectedIndex) } returns C.TRACK_TYPE_TEXT
            every { getTrackGroups(expectedIndex) } returns trackGroups
        }
        every { mTrackSelector.currentMappedTrackInfo } returns mappedTrackInfo
        assertFalse(testObject.isClosedCaptionAvailable())
        verify(exactly = 0) {
            Log.d(any(), any())
        }
    }

    @Test
    fun `enableClosedCaption false when available calls toggleClosed Caption`() {
        val expectedIndex = 2
        val format: Format = mockk()
        val trackGroup = TrackGroup(format)
        val trackGroups = TrackGroupArray(trackGroup)
        every { playerState.mTrackSelector } returns mTrackSelector
        every { playerState.defaultTrackFilter } returns mockk {
            every { filter(format, trackGroups) } returns true
        }
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0


        val mappedTrackInfo = mockk<MappingTrackSelector.MappedTrackInfo> {
            every { rendererCount } returns 3
            every { getRendererType(0) } returns C.TRACK_TYPE_AUDIO
            every { getRendererType(1) } returns C.TRACK_TYPE_IMAGE
            every { getRendererType(expectedIndex) } returns C.TRACK_TYPE_TEXT
            every { getTrackGroups(expectedIndex) } returns trackGroups
        }
        every { mTrackSelector.currentMappedTrackInfo } returns mappedTrackInfo
        every { mConfig.isLoggingEnabled } returns true

        assertFalse(testObject.enableClosedCaption(false))

        verifySequence {
            Log.d("ArcVideoSDK", "Call to toggle CC off")
        }
    }

    @Test
    fun `enableClosedCaption true when available calls toggleClosed Caption`() {
        val expectedIndex = 2
        val format: Format = mockk()
        val trackGroup = TrackGroup(format)
        val trackGroups = TrackGroupArray(trackGroup)
        val parametersBuilder = mockk<DefaultTrackSelector.Parameters.Builder>(relaxed = true)
        val override = mockk<DefaultTrackSelector.SelectionOverride>()
        every { playerState.mTrackSelector } returns mTrackSelector
        every { playerState.ccButton } returns null
        every { playerState.defaultTrackFilter } returns mockk {
            every { filter(format, trackGroups) } returns true
        }
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        mockkStatic(PrefManager::class)
        every { PrefManager.saveBoolean(any(), any(), any()) } returns true
        every { mTrackSelector.buildUponParameters() } returns parametersBuilder

        val mappedTrackInfo = mockk<MappingTrackSelector.MappedTrackInfo> {
            every { rendererCount } returns 3
            every { getRendererType(0) } returns C.TRACK_TYPE_AUDIO
            every { getRendererType(1) } returns C.TRACK_TYPE_IMAGE
            every { getRendererType(expectedIndex) } returns C.TRACK_TYPE_TEXT
            every { getTrackGroups(expectedIndex) } returns trackGroups
        }
        every { mTrackSelector.currentMappedTrackInfo } returns mappedTrackInfo
        every { mConfig.isLoggingEnabled } returns true
        every { utils.createSelectionOverride(0, 0) } returns override      


        assertTrue(testObject.enableClosedCaption(true))

        verifySequence {
            playerState.mTrackSelector
            playerState.mTrackSelector
            mTrackSelector.currentMappedTrackInfo
            playerState.defaultTrackFilter
            Log.d("ArcVideoSDK", "Call to toggle CC on")
            playerState.mTrackSelector
            mTrackSelector.currentMappedTrackInfo
            playerState.mTrackSelector
            mTrackSelector.buildUponParameters()
            parametersBuilder.setSelectionOverride(
                expectedIndex,
                trackGroups,
                override
            )
            parametersBuilder.setRendererDisabled(expectedIndex, false)
            playerState.mTrackSelector
            mTrackSelector.setParameters(parametersBuilder)
            PrefManager.saveBoolean(
                activity,
                PrefManager.IS_CAPTIONS_ENABLED,
                true
            )
            playerState.ccButton
        }
    }

    @Test
    fun `toggleClosedCaption when no logging`() {
        val expectedIndex = 2
        val format: Format = mockk()
        val trackGroup = TrackGroup(format)
        val trackGroups = TrackGroupArray(trackGroup)
        every { playerState.mTrackSelector } returns mTrackSelector
        every { playerState.defaultTrackFilter } returns mockk {
            every { filter(format, trackGroups) } returns true
        }
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0


        val mappedTrackInfo = mockk<MappingTrackSelector.MappedTrackInfo> {
            every { rendererCount } returns 3
            every { getRendererType(0) } returns C.TRACK_TYPE_AUDIO
            every { getRendererType(1) } returns C.TRACK_TYPE_IMAGE
            every { getRendererType(expectedIndex) } returns C.TRACK_TYPE_TEXT
            every { getTrackGroups(expectedIndex) } returns trackGroups
        }
        every { mTrackSelector.currentMappedTrackInfo } returns mappedTrackInfo
        every { mConfig.isLoggingEnabled } returns false

        assertFalse(testObject.enableClosedCaption(false))

        verify(exactly = 0) {
            Log.d(any(), any())
        }
    }

    @Test
    fun `toggleClosedCaption when null track selector`() {

        every { playerState.mTrackSelector } returns null
        mockkStatic(Log::class)
        every { mConfig.isLoggingEnabled } returns false

        assertFalse(testObject.enableClosedCaption(false))
    }
}