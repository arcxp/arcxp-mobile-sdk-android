package com.arcxp.video.players

import android.net.Uri
import com.arcxp.commons.testutils.TestUtils.createDefaultVideo
import com.arcxp.video.ArcXPVideoConfig
import com.arcxp.video.listeners.VideoListener
import com.arcxp.video.model.ArcVideoSDKErrorType
import com.arcxp.video.model.PlayerState
import com.arcxp.video.util.Utils
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.MediaItem.SubtitleConfiguration
import com.google.android.exoplayer2.source.MergingMediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.source.SingleSampleMediaSource
import com.google.android.exoplayer2.source.dash.DashMediaSource
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.source.smoothstreaming.SsMediaSource
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.util.Util
import io.mockk.EqMatcher
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.mockkStatic
import io.mockk.verifySequence
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class CaptionsManagerTest {

    @MockK
    private lateinit var playerState: PlayerState

    @MockK
    private lateinit var utils: Utils

    @MockK
    private lateinit var mConfig: ArcXPVideoConfig

    @RelaxedMockK
    private lateinit var mListener: VideoListener


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
}