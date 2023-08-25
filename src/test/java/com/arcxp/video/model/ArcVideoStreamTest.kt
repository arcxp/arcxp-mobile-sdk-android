package com.arcxp.video.model

import com.arcxp.ArcXPMobileSDK
import com.arcxp.ArcXPMobileSDK.baseUrl
import com.arcxp.video.ArcMediaPlayerConfig
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import org.junit.Assert.assertEquals
import org.junit.Test

class ArcVideoStreamTest {

    @Test
    fun `findBestStream returns only stream if streams contains one stream`() {
        val stream = mockk<Stream>()

        val testObject =
            ArcVideoStream(
                "type",
                "id",
                "uuid",
                "version",
                "canonicalUrl",
                "shortUrl",
                "createdDate",
                "lastUpdatedDate",
                "publishedDate",
                "firstPublishDate",
                "DisplayDate",
                headlines = mockk(),
                subheadlines = mockk(),
                description = mockk(),
                credits = mockk(),
                taxonomy = mockk(),
                additionalProperties = mockk(),
                100L,
                "videoType",
                listOf(stream),
                null,
                PromoItemBasic("", "", Credits(null), "", 0, 0),
                null
            )

        assertEquals(stream, testObject.findBestStream(mockk(), 0))
    }

    @Test
    fun `findBestStream when no match among preferred, and next is HLS chooses first entry regardless of bitrate`() {
        val preferredStreamType = ArcMediaPlayerConfig.PreferredStreamType.GIFMP4
        val expectedStream = Stream(
            1,
            2,
            12345L,
            ArcMediaPlayerConfig.PreferredStreamType.GIF.getPreferredStreamType(),
            "url",
            400,
            "provider"
        )
        val stream2 = Stream(
            1,
            2,
            12345L,
            ArcMediaPlayerConfig.PreferredStreamType.GIF.getPreferredStreamType(),
            "url",
            405,
            "provider"
        )
        val stream3 = Stream(
            1,
            2,
            12345L,
            ArcMediaPlayerConfig.PreferredStreamType.GIF.getPreferredStreamType(),
            "url",
            410,
            "provider"
        )
        val stream4 = Stream(
            1,
            2,
            12345L,
            ArcMediaPlayerConfig.PreferredStreamType.GIF.getPreferredStreamType(),
            "url",
            800,
            "provider"
        )
        val stream5 = Stream(
            1,
            2,
            12345L,
            ArcMediaPlayerConfig.PreferredStreamType.GIF.getPreferredStreamType(),
            "url",
            1200,
            "provider"
        )
        val streams = listOf(expectedStream, stream2, stream3, stream4, stream5)

        val testObject = ArcVideoStream(
            "type",
            "id",
            "uuid",
            "version",
            "canonicalUrl",
            "shortUrl",
            "createdDate",
            "lastUpdatedDate",
            "publishedDate",
            "firstPublishDate",
            "DisplayDate",
            headlines = mockk(),
            subheadlines = mockk(),
            description = mockk(),
            credits = mockk(),
            taxonomy = mockk(),
            additionalProperties = mockk(),
            100L,
            "videoType",
            streams = streams,
            null,
            PromoItemBasic("", "", Credits(null), "", 0, 0),
            null
        )
        val actualStream = testObject.findBestStream(preferredStreamType, 800)

        assertEquals(expectedStream, actualStream)
    }

    @Test
    fun `findBestStream when match among preferred disregards closer bit rates in non preferred types`() {
        val preferredStreamType = ArcMediaPlayerConfig.PreferredStreamType.GIFMP4
        val expectedStream = Stream(
            1,
            2,
            12345L,
            ArcMediaPlayerConfig.PreferredStreamType.GIFMP4.getPreferredStreamType(),
            "url",
            400,
            "provider"
        )
        val stream2 = Stream(
            1,
            2,
            12345L,
            ArcMediaPlayerConfig.PreferredStreamType.GIF.getPreferredStreamType(),
            "url",
            405,
            "provider"
        )
        val stream3 = Stream(
            1,
            2,
            12345L,
            ArcMediaPlayerConfig.PreferredStreamType.GIF.getPreferredStreamType(),
            "url",
            410,
            "provider"
        )
        val stream4 = Stream(
            1,
            2,
            12345L,
            ArcMediaPlayerConfig.PreferredStreamType.GIF.getPreferredStreamType(),
            "url",
            800,
            "provider"
        )
        val stream5 = Stream(
            1,
            2,
            12345L,
            ArcMediaPlayerConfig.PreferredStreamType.GIF.getPreferredStreamType(),
            "url",
            1200,
            "provider"
        )
        val streams = listOf(expectedStream, stream2, stream3, stream4, stream5)

        val testObject = ArcVideoStream(
            "type",
            "id",
            "uuid",
            "version",
            "canonicalUrl",
            "shortUrl",
            "createdDate",
            "lastUpdatedDate",
            "publishedDate",
            "firstPublishDate",
            "DisplayDate",
            headlines = mockk(),
            subheadlines = mockk(),
            description = mockk(),
            credits = mockk(),
            taxonomy = mockk(),
            additionalProperties = mockk(),
            100L,
            "videoType",
            streams = streams,
            null,
            PromoItemBasic("", "", Credits(null), "", 0, 0),
            null
        )
        val actualStream = testObject.findBestStream(preferredStreamType, 1200)

        assertEquals(expectedStream, actualStream)
    }

    @Test
    fun `findBestStream when match among preferred chooses closest bitrate to preferred`() {
        val preferredStreamType = ArcMediaPlayerConfig.PreferredStreamType.GIFMP4
        val stream1 = Stream(
            1,
            2,
            12345L,
            ArcMediaPlayerConfig.PreferredStreamType.GIFMP4.getPreferredStreamType(),
            "url",
            400,
            "provider"
        )
        val stream2 = Stream(
            1,
            2,
            12345L,
            ArcMediaPlayerConfig.PreferredStreamType.GIFMP4.getPreferredStreamType(),
            "url",
            405,
            "provider"
        )
        val stream3 = Stream(
            1,
            2,
            12345L,
            ArcMediaPlayerConfig.PreferredStreamType.GIFMP4.getPreferredStreamType(),
            "url",
            410,
            "provider"
        )
        val stream4 = Stream(
            1,
            2,
            12345L,
            ArcMediaPlayerConfig.PreferredStreamType.GIFMP4.getPreferredStreamType(),
            "url",
            800,
            "provider"
        )
        val expectedStream = Stream(
            1,
            2,
            12345L,
            ArcMediaPlayerConfig.PreferredStreamType.GIFMP4.getPreferredStreamType(),
            "url",
            1200,
            "provider"
        )
        val streams = listOf(stream1, stream2, stream3, stream4, expectedStream)

        val testObject = ArcVideoStream(
            "type",
            "id",
            "uuid",
            "version",
            "canonicalUrl",
            "shortUrl",
            "createdDate",
            "lastUpdatedDate",
            "publishedDate",
            "firstPublishDate",
            "DisplayDate",
            headlines = mockk(),
            subheadlines = mockk(),
            description = mockk(),
            credits = mockk(),
            taxonomy = mockk(),
            additionalProperties = mockk(),
            100L,
            "videoType",
            streams = streams,
            null,
            PromoItemBasic("", "", Credits(null), "", 0, 0),
            null
        )
        val actualStream = testObject.findBestStream(preferredStreamType, 1200)

        assertEquals(expectedStream, actualStream)
    }

    @Test
    fun `findBestStream when checking next type chooses closest bitrate to preferred`() {
        val preferredStreamType = ArcMediaPlayerConfig.PreferredStreamType.GIF
        val stream1 = Stream(
            1,
            2,
            12345L,
            ArcMediaPlayerConfig.PreferredStreamType.GIFMP4.getPreferredStreamType(),
            "url",
            400,
            "provider"
        )
        val stream2 = Stream(
            1,
            2,
            12345L,
            ArcMediaPlayerConfig.PreferredStreamType.GIFMP4.getPreferredStreamType(),
            "url",
            405,
            "provider"
        )
        val stream3 = Stream(
            1,
            2,
            12345L,
            ArcMediaPlayerConfig.PreferredStreamType.GIFMP4.getPreferredStreamType(),
            "url",
            410,
            "provider"
        )
        val stream4 = Stream(
            1,
            2,
            12345L,
            ArcMediaPlayerConfig.PreferredStreamType.GIFMP4.getPreferredStreamType(),
            "url",
            800,
            "provider"
        )
        val expectedStream = Stream(
            1,
            2,
            12345L,
            ArcMediaPlayerConfig.PreferredStreamType.GIFMP4.getPreferredStreamType(),
            "url",
            1200,
            "provider"
        )
        val streams = listOf(stream1, stream2, stream3, stream4, expectedStream)

        val testObject = ArcVideoStream(
            "type",
            "id",
            "uuid",
            "version",
            "canonicalUrl",
            "shortUrl",
            "createdDate",
            "lastUpdatedDate",
            "publishedDate",
            "firstPublishDate",
            "DisplayDate",
            headlines = mockk(),
            subheadlines = mockk(),
            description = mockk(),
            credits = mockk(),
            taxonomy = mockk(),
            additionalProperties = mockk(),
            100L,
            "videoType",
            streams = streams,
            null,
            PromoItemBasic("", "", Credits(null), "", 0, 0),
            null
        )
        val actualStream = testObject.findBestStream(preferredStreamType, 1200)

        assertEquals(expectedStream, actualStream)
    }

    @Test
    fun `url returns full video url`() {
        val url = "videoUrl"
        val expected = "baseUrl/$url"
        mockkObject(ArcXPMobileSDK)
        every { baseUrl } returns "baseUrl/"
        val testObject = ArcVideoStream(
            type = "type",
            id = "id",
            uuid = "uuid",
            version = "version",
            canonicalUrl = url,
            shortUrl = "shortUrl",
            createdDate = "createdDate",
            lastUpdatedDate = "lastUpdatedDate",
            publishedDate = "publishedDate",
            firstPublishDate = "firstPublishDate",
            displayDate = "DisplayDate",
            headlines = mockk(),
            subheadlines = mockk(),
            description = mockk(),
            credits = mockk(),
            taxonomy = mockk(),
            additionalProperties = mockk(),
            duration = 100L,
            videoType = "videoType",
            streams = emptyList(),
            subtitles = null,
            promoImage = PromoItemBasic(
                type = "",
                version = "",
                credits = Credits(null),
                url = "",
                width = 0,
                height = 0
            ),
            adTagUrl = null
        )

        val actual = testObject.url()

        assertEquals(expected, actual)
        unmockkAll()
    }
}