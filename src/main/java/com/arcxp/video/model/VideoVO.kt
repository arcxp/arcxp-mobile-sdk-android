package com.arcxp.video.model

import com.arcxp.ArcXPMobileSDK.imageUtils
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class VideoVO(
    val id: String?,
    val adConfig: AdConfig?,
    val associatedContent: AssociatedContent?,
    val contentConfig: ContentConfig?,
    val customFields: Map<String, *>?,
    val dummy: Boolean?,
    val embedConfig: EmbedConfig?,
    val hideInPlaylist: Boolean?,

    val imageResizerUrls: List<ImageResizer>?,
    val liveEventConfig: LiveEventConfig?,
    val metaConfig: MetaConfig?,
    val platform: String?, //applenews, clean, desktop, facebook, mobile, topbox, youtube
    val playlistName: String?,
    val producerConfig: ProducerConfig?,
    val promoImage: PromoImage?,
    val sponsoredConfig: SponsoredConfig?,
    val subtitlesConfig: SubtitlesConfig?,
    val syncContentEnabled: Boolean?,
    val synchronizedToMethode: Boolean?,
    val truthTellerEnabled: Boolean?,
    val variantExclusions: List<String>?,


    ) {
    @JsonClass(generateAdapter = true)
    data class AssociatedContent(
        val contentId: String?,
        val contentType: String?
    )

    @JsonClass(generateAdapter = true)
    data class ContentConfig(
        val blurb: String?,
        val boldTitle: String?,
        val clarification: String?,
        val collectionPath: String?,
        val commentsConfig: CommentsConfig?,
        val correction: String?,
        val credits: CreditsVO?,
        val dateConfig: DateConfig?,
        val defaultPlaylistLibrary: String?,
        val disableEmbedEndScreen: Boolean?,
        val disableExternalEmbed: Boolean?,
        val enableVideoReactions: Boolean?,
        val gridExperience: Boolean?,
        val hasClarification: Boolean?,
        val hasCorrection: Boolean?,
        val images: String?,
        val includeInSiteSearch: Boolean?,
        val isOoyalaVideo: Boolean?,
        val location: Location?,
        val loid: String?,
        val metadata: Map<String, *>?,
        val methodeFilePath: String?,
        val methodeImageFilePath: String?,
        val path: String?,
        val permaLinkURL: String?,
        val playerBrandingId: String?,
        val playerURL: String?,
        val prerollOnly: Boolean?,
        val promoted: Boolean?,
        val queryTerms: String?,
        val rating: String?,
        val redirect: List<Redirect>?,
        val redirectURL: String?,
        val relatedLinks: List<RelatedLinks>?,
        val shortDescription: String?,
        val shortURL: String?,
        val smsPageUrl: String?,
        val sourceMediaURL: String?,
        val streams: VideoStreamVO?,
        val title: String?,
        val trackAsPool: Boolean?,
        val transcoderVideoId: String?,
        val type: String?, // clip, embed, episode, live
        val urlHash: String?,
        val useVariants: Boolean?,
        val uuid: String?,
        val vendor: String?,
        val vertical: Boolean?,
        val video360: Boolean?,
        val videoContentId: String?,
        val videoDuration: Int?,
        val videoStatus: String?,
        val videoURL: String?,
        val youtubeContentId: String?,

        ) {
        @JsonClass(generateAdapter = true)
        data class CommentsConfig(
            val allowComments: Boolean?,
            val allowPhotos: Boolean?,
            val allowVideos: Boolean?,
            val isDisplayable: Boolean?,
            val isModerated: Boolean?,
            val max: Int?,
            val period: Int?,
            val source: String?
        )

        @JsonClass(generateAdapter = true)
        data class DateConfig(
            val dateCreated: Long?,
            val dateFirstPublished: Long?,
            val dateLastPublished: Long?,
            val datePublished: Long?,
            val dateUpdated: Long?,
            val displayDate: Long?,
            val publicationEndDate: Long?,
            val publicationStartDate: Long?
        )

        @JsonClass(generateAdapter = true)
        data class Location(
            val latitude: Double?,
            val longitude: Double?
        )

        @JsonClass(generateAdapter = true)
        data class Redirect(
            val canonicalUrl: String?,
            val redirectUrl: String?,
            val type: String?,
            val version: String?
        )
    }

    @JsonClass(generateAdapter = true)
    data class CreditsVO(
        val contributors: List<String>?,
        val editor: String?,
        val hostTalent: List<String>?,
        val source: String?
    )

    @JsonClass(generateAdapter = true)
    data class EmbedConfig(
        val embedContentId: String?,
        val embedType: String?,
        val showEmbed: Boolean?
    )

    @JsonClass(generateAdapter = true)
    data class ImageResizer(
        val height: Int?,
        val size: String?,
        val url: String?,
        val width: Int?
    )

    @JsonClass(generateAdapter = true)
    data class LiveEventConfig(
        val closedCaptionsIngestionUrl: String?,
        val displayDate: Long?,
        val draft: Boolean?,
        val redirectLink: RedirectLink?,
        val streamName: String?,
        val streams: List<VideoStreamVO>?,
        val uuid: String?,
        val videoContentId: String?,
        val youtubeEventId: String?,
        val youtubeEventState: String?, //abandoned, complete, completeStarting, created, error, live, liveStarting, ready, reclaimed, revoked, testStarting, testing
        val youtubeStreamId: String?
    ) {
        @JsonClass(generateAdapter = true)
        data class RedirectLink(
            val title: String?,
            val url: String?
        )
    }

    @JsonClass(generateAdapter = true)
    data class MetaConfig(
        val distributor: Distributor?,
        val editor: Editor?,
        val ideology: String?,
        val keywords: List<String>?,
        val order: Int?,
        val playlists: List<String>?,
        val primarySiteNode: String?,
        val secondarySiteNode: String?,
        val section: String?,
        val sectionDisplayName: String?,
        val sectionURL: String?,
        val sentiment: String?,
        val subsection: String?,
        val tags: List<String>?,
        val topic: Topic?,
    ) {
        @JsonClass(generateAdapter = true)
        data class Distributor(
            val additionalProperties: Map<String, *>?,
            val category: String?, //freelance, handout, other, staff, stock, wires
            val name: String?,
            val subcategory: String?,
        )

        @JsonClass(generateAdapter = true)
        data class Editor(
            val email: String?,
            val lastname: String?,
            val name: String?
        )

        @JsonClass(generateAdapter = true)
        data class Topic(
            val apiMethod: String?,
            val apiParams: List<Any>?,
            val collectionPath: String?,
            val id: String?,
            val menuVisible: Boolean?,
            val name: String?,
            val order: Int?,
            val priority: Boolean?,
            val subsection: String?,
            val url: String?
        )
    }

    @JsonClass(generateAdapter = true)
    data class VideoStreamVO(
        val audioCodec: String?,
        val bitrate: Int?,
        val fileSize: Int?,
        val height: Int?,
        val provider: String?, //elastictranscoder, mediaconvert, ooyala
        val type: String?,
        val url: String?,
        val videoCodec: String?,
        val width: Int?

    )

    @JsonClass(generateAdapter = true)
    data class ProducerConfig(
        val createdBy: ProducerInfo?,
        val firstPublishedBy: ProducerInfo?,
    ) {
        @JsonClass(generateAdapter = true)
        data class ProducerInfo(
            val email: String?,
            val lastname: String?,
            val name: String?,
            val samAccountName: String?
        )
    }

    @JsonClass(generateAdapter = true)
    data class PromoImage(
        val image: Image?
    ) {
        @JsonClass(generateAdapter = true)
        data class Image(
            val caption: String?,
            val credits: List<Any>?,
            val height: Int?,
            val photographer: String?,
            val url: String?,
            val width: Int?
        )
    }

    @JsonClass(generateAdapter = true)
    data class RelatedCategory(
        val id: String?,
        val name: String?,
        val uuid: String?,
    )

    @JsonClass(generateAdapter = true)
    data class RelatedLinks(
        val title: String?,
        val url: String?
    )

    @JsonClass(generateAdapter = true)
    data class SeriesConfig(
        val adTagUrlSuffix: String?,
        val blurb: String?,
        val displaySeriesTemplate: Boolean?,
        val episodeDate: Int?,
        val headline: String?,
        val image: String?,
        val menuVisible: Boolean?,
        val menuVisibleDue: Int?,
        val menuVisibleSince: Int?,
        val relatedSeries: RelatedCategory?,
        val requiredField: String?,
        val seriesBlurbHtml: String?,
        val seriesFiltersUrl: String?,
        val seriesGeoDataUrl: String?,
        val seriesHTMLTemplate: String?,
        val seriesIntroVideoUUID: String?,
        val seriesPlaylists: List<String>?,
        val seriesRelatedLinks: RelatedLinks?,
        val seriesShareImage: String?,
        val seriesTitleToEmbed: Boolean?,
        val tagline: String?,
        val url: String?
    )

    @JsonClass(generateAdapter = true)
    data class SponsoredConfig(
        val linkUrl: String?,
        val logoUrl: String?,
        val sponsoredVideo: Boolean?
    )

    @JsonClass(generateAdapter = true)
    data class SubtitlesConfig(
        val linkUrl: String?,
        val logoUrl: String?,
        val sponsoredVideo: Boolean?
    )

    @JsonClass(generateAdapter = true)
    data class YoutubeConfig(
        val youtubePlaylistId: String?,
        val youtubePlaylistItemId: String?,
        val youtubeVideoCategory: Int?,
        val youtubeVideoId: String?,
    )

    @JsonClass(generateAdapter = true)
    data class VideoCategoryConfig(
        val active: Boolean?,
        val collectionPath: String?,
        val commercialAdNode: String?,
        val credits: CreditsVO?,
        val defaultPlaylistLibrary: String?,
        val description: String?,
        val hashtag: String?,
        val id: String?,
        val keywords: List<String>?,
        val methodeImageFilePath: String?,
        val methodeVideoFilePath: String?,
        val name: String?,
        val primarySiteNode: String?,
        val relatedLinks: RelatedLinks?,
        val secondarySiteNode: String?,
        val section: String?,
        val sectionDisplayName: String?,
        val sectionURL: String?,
        val seriesConfig: SeriesConfig?,
        val sponsoredVideo: Boolean?,
        val subsection: String?,
        val tags: List<String>?,
        val types: List<String>?,
        val videoAdZone: String?,
    )
}

fun VideoVO.thumbnail() =
    promoImage?.image?.url?.let { imageUtils().thumbnail(it.substringAfter("https://")) } ?: ""

fun VideoVO.fallback() =
    promoImage?.image?.url ?: ""