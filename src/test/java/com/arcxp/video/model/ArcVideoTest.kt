package com.arcxp.video.model

import com.arc.arcvideo.ArcMediaPlayerConfig
import com.google.android.exoplayer2.C
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.mockk
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class ArcVideoTest {

    private lateinit var testObject: ArcVideo.Builder

    @RelaxedMockK private lateinit var stream: ArcVideoStream
    @RelaxedMockK private lateinit var config: ArcMediaPlayerConfig
    @RelaxedMockK private lateinit var adConfig: AdConfig
    @RelaxedMockK private lateinit var expectedStream: Stream

    private val expectedBitRate = 3289764
    private val expectedStreamType = ArcMediaPlayerConfig.PreferredStreamType.MP4
    private val url = "\\url\\"
    private val expectedUrl = "url"
    private val expectedUuid = "uuid"
    private val expectedCCStartMode = ArcMediaPlayerConfig.CCStartMode.ON


    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxUnitFun = true)
        every { config.maxBitRate } returns expectedBitRate
        every { config.getPreferredStreamType() } returns expectedStreamType

        every { stream.findBestStream(expectedStreamType, expectedBitRate) } returns expectedStream
        every { stream.uuid } returns expectedUuid
        every { expectedStream.url } returns url
        testObject = ArcVideo.Builder()
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `setVideoStream sets shouldPlayAds true given adTagUrl is null, adConfig is null, isEnableAds true`() {
        every { stream.adTagUrl } returns null
        every { config.adConfig } returns null
        every { config.isEnableAds } returns true
        testObject.setVideoStream(stream, config)
        val actual = testObject.build()

        assertTrue(actual.shouldPlayAds)
    }

    @Test
    fun `setVideoStream sets shouldPlayAds false given adTagUrl is null, adConfig is null, isEnableAds false`() {
        every { stream.adTagUrl } returns null
        every { config.adConfig } returns null
        every { config.isEnableAds } returns false

        testObject.setVideoStream(stream, config)
        val actual = testObject.build()

        assertFalse(actual.shouldPlayAds)
    }


    @Test
    fun `setVideoStream sets shouldPlayAds true given adTagUrl is null, adConfig is non null, isAdEnabled true`() {
        every { stream.adTagUrl } returns null
        every { config.adConfig } returns adConfig
        every { adConfig.isAdEnabled } returns true

        testObject.setVideoStream(stream, config)
        val actual = testObject.build()

        assertTrue(actual.shouldPlayAds)
    }

    @Test
    fun `setVideoStream sets shouldPlayAds false given adTagUrl is null, adConfig is non null, isAdEnabled false`() {
        every { stream.adTagUrl } returns null
        every { config.adConfig } returns adConfig
        every { adConfig.isAdEnabled } returns false

        testObject.setVideoStream(stream, config)
        val actual = testObject.build()

        assertFalse(actual.shouldPlayAds)
    }

    @Test
    fun `setVideoStream sets shouldPlayAds true given adTagUrl is non null`() {
        every { stream.adTagUrl } returns "ad tag url"
        testObject.setVideoStream(stream, config)
        val actual = testObject.build()

        assertTrue(actual.shouldPlayAds)
    }

    @Test
    fun `setVideoStream sets adTagUrl to configAdConfigUrl given getAdTagUrl is null, adConfig is null`() {
        every { stream.adTagUrl } returns null
        every { config.adConfig } returns null
        every { config.adConfigUrl } returns expectedUrl

        testObject.setVideoStream(stream, config)
        val actual = testObject.build()

        assertEquals(expectedUrl, actual.adTagUrl)
    }

    @Test
    fun `setVideoStream sets adTagUrl to AdConfigAdConfigUrl given getAdTagUrl is null, adConfig is non null`() {
        every { stream.adTagUrl } returns null
        every { config.adConfig } returns adConfig
        every { adConfig.getAdConfigUrl() } returns expectedUrl

        testObject.setVideoStream(stream, config)
        val actual = testObject.build()

        assertEquals(expectedUrl, actual.adTagUrl)
    }

    @Test
    fun `setVideoStream sets adTagUrl to adTagUrl given getAdTagUrl is non null`() {
        every { stream.adTagUrl } returns expectedUrl

        testObject.setVideoStream(stream, config)
        val actual = testObject.build()

        assertEquals(expectedUrl, actual.adTagUrl)
    }


    @Test
    fun `setVideoStream sets isYouTube true given video type equal to youtube`() {
        every { stream.videoType } returns "youtube"

        testObject.setVideoStream(stream, config)
        val actual = testObject.build()

        assertTrue(actual.isYouTube)
    }

    @Test
    fun `setVideoStream sets isYouTube false given video type unequal to youtube`() {
        every { stream.videoType } returns "!youtube"

        testObject.setVideoStream(stream, config)
        val actual = testObject.build()

        assertFalse(actual.isYouTube)
    }

    @Test
    fun `setVideoStream sets isLive true given video type equal to live`() {
        every { stream.videoType } returns "live"

        testObject.setVideoStream(stream, config)
        val actual = testObject.build()

        assertTrue(actual.isLive)
    }

    @Test
    fun `setVideoStream sets isLive false given video type unequal to live`() {
        every { stream.videoType } returns "!live"

        testObject.setVideoStream(stream, config)
        val actual = testObject.build()

        assertFalse(actual.isLive)
    }

    @Test
    fun `setVideoStream sets startMuted true given config is startedMuted`() {
        every { config.isStartMuted } returns true

        testObject.setVideoStream(stream, config)
        val actual = testObject.build()

        assertTrue(actual.startMuted)
    }

    @Test
    fun `setVideoStream sets startMuted false given config is not startedMuted`() {
        every { config.isStartMuted } returns false

        testObject.setVideoStream(stream, config)
        val actual = testObject.build()

        assertFalse(actual.startMuted)
    }

    @Test
    fun `setVideoStream sets focusSkipButton true given config is focusSkipButton`() {
        every { config.isFocusSkipButton } returns true

        testObject.setVideoStream(stream, config)
        val actual = testObject.build()

        assertTrue(actual.focusSkipButton)
    }

    @Test
    fun `setVideoStream sets focusSkipButton false given config is not focusSkipButton`() {
        every { config.isFocusSkipButton } returns false

        testObject.setVideoStream(stream, config)
        val actual = testObject.build()

        assertFalse(actual.focusSkipButton)
    }

    @Test
    fun `setVideoStreamVirtual loads the necessary values`() {
        config.apply {
            every { isStartMuted } returns true
            every { isFocusSkipButton } returns true
            every { ccStartMode } returns expectedCCStartMode
        }
        testObject.setVideoStreamVirtual("url", config)
        val actual = testObject.build()

        assertEquals("url", actual.id)
        assertTrue(actual.isLive)
        assertEquals(0, actual.startPos)
        assertTrue(actual.startMuted)
        assertTrue(actual.focusSkipButton)
        assertEquals(expectedCCStartMode, actual.ccStartMode)
    }

    @Test
    fun `setVideoStream sets bestStream, id, uuid, startPos, ccStartMode`() {
        every {config.ccStartMode} returns expectedCCStartMode
        testObject.setVideoStream(stream, config)
        val actual = testObject.build()

        assertEquals(expectedStream, actual.bestStream)
        assertEquals(expectedUrl, actual.id)
        assertEquals(expectedUuid, actual.uuid)
        assertEquals(0, actual.startPos)
        assertEquals(expectedCCStartMode, actual.ccStartMode)
    }


    @Test
    fun `setVideoStream sets subtitleUrl given it finds a properly formatted WEB_VTT URL `(){
        every { stream.subtitles } returns mockk {
            every { urls } returns listOf(mockk {
                every { format } returns "WEB_VTT"
                every { url } returns expectedUrl
            })
        }

        testObject.setVideoStream(stream, config)
        val actual = testObject.build()

        assertEquals(expectedUrl, actual.subtitleUrl)
    }

    @Test
    fun `setVideoStream does not ses subtitleUrl given it finds no properly formatted WEB_VTT URL `(){
        every { stream.subtitles } returns mockk {
            every { urls } returns listOf(mockk {
                every { format } returns "!WEB_VTT"
                every { url } returns expectedUrl
            })
        }

        testObject.setVideoStream(stream, config)
        val actual = testObject.build()

        assertNull(actual.subtitleUrl)
    }

    @Test
    fun `ArcVideo constructor sets duration when less than 1`(){
        val actual = testObject.build()
        val expectedDuration = C.TIME_UNSET / ArcVideo.US_PER_MS

        assertEquals(expectedDuration, actual.duration)
    }

    @Test
    fun `ArcVideo constructor uses given duration when 1 or greater`(){
        val expectedDuration = 1L
        val actual = testObject.setDuration(expectedDuration).build()

        assertEquals(expectedDuration, actual.duration)
    }

    @Test
    fun `ArcVideo builder builds correctly`(){
        val expected = ArcVideo("id", expectedUuid, 123L, true, true, 1234L, "shareUrl", "headline", "pageName",
                "videoName", "videoSection", "videoSource", "videoCategory", "contentId", "fallbackUrl",
                "adTagUrl", true, "subtitleUrl", "source", null, true, true,
                true, expectedCCStartMode)
        val actual = testObject.setId("id").setUrl("id").setUuid(expectedUuid).setStartPos(123L).setIsYouTube(true).setIsLive(true).setDuration(1234L).setShareUrl("shareUrl").setHeadline("headline").setPageName("pageName")
                .setVideoName("videoName").setVideoSection("videoSection").setVideoSource("videoSource").setVideoCategory("videoCategory").setContentId("contentId").setFallbackUrl("fallbackUrl")
                .setAdTagUrl("adTagUrl").setShouldPlayAds(true).setSubtitleUrl("subtitleUrl").setSource("source").setAutoStartPlay(true).setStartMuted(true)
                .setFocusSkipButton(true).setCcStartMode(expectedCCStartMode).build()

        assertEquals(expected, actual)
    }
}