package com.arc.util

import com.arc.arcvideo.ArcMediaPlayerConfig
import com.arc.arcvideo.model.*
import io.mockk.mockk
import java.io.File
import java.lang.reflect.Field
import java.lang.reflect.Modifier

class TestUtils {
    companion object {
        fun getFileAsString(fileName: String): String {
            val file = File(
                this::class.java.classLoader?.getResource(fileName)?.path
                    ?: throw NullPointerException("No path find!")
            )
            return String(file.readBytes())
        }

        fun createDefaultVideo(
            id: String = "id",
            isYouTube: Boolean = false,
            isLive: Boolean = false,
            bestStream: Stream = mockk()
        ): ArcVideo {
            return ArcVideo(
                id,
                "uuid",
                123L,
                isYouTube,
                isLive,
                100,
                "mShareUrl",
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
                "subtitleUrl",
                "source",
                bestStream,
                false,
                false,
                false,
                ArcMediaPlayerConfig.CCStartMode.DEFAULT
            )
        }

        fun createVideoStream(id: String = "id") =
            ArcVideoStream(
                type = "videoType1",
                id = id,
                uuid = "uuid1",
                version = "version",
                canonicalUrl = "canonicalUrl",
                shortUrl = "shortUrl",
                createdDate = "createdDate",
                lastUpdatedDate = "lastUpdatedDate",
                publishedDate = "publishedDate",
                firstPublishDate = "firstPublishDate",
                displayDate = "displayDate",
                headlines = Headlines(basic = "basic", metaTitle = "metaTitle"),
                subheadlines = Subheadlines(basic = "basic"),
                description = Description(basic = "basic"),
                taxonomy = Taxonomy(
                    tags = listOf(Tag("tag1")),
                    sites = emptyList(),
                    seoKeywords = emptyList(),
                    primarySite = Site("", "", "", "", "", true)
                ),
                credits = Credits(by = emptyList()),
                additionalProperties = AdditionalProperties(advertising = Advertising(null, null)),
                duration = 123L,
                videoType = "videoType",
                streams = emptyList(),
                subtitles = null,
                promoImage = PromoItemBasic("", "", Credits(null), "", 0, 0),
                adTagUrl = null
            )
    }
}