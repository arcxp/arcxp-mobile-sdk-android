package com.arcxp.video.model

import androidx.annotation.Keep
import com.arcxp.ArcXPMobileSDK.baseUrl
import com.arcxp.video.ArcXPVideoConfig
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * ### What is it?
 * This class holds the data for an individual video object returned from one of the ArcMediaClient API calls.
 *
 * ### How does it relate to other important Video SDK classes
 * The ArcMediaClient API calls will return a list that will contain 0 or more of these objects.  Each one
 * can be sent to an ArcMediaPlayer object and played.
 *
 * ### What are the core components that make it up?
 * This is a data class that has only one method, findBestStream(), which calculates the best stream to use when
 * playing a video based upon passed in parameters.
 *
 * @property type
 * @property id
 * @property uuid
 * @property version
 * @property canonicalUrl
 * @property shortUrl
 * @property createdDate
 * @property lastUpdatedDate
 * @property publishedDate
 * @property firstPublishDate
 * @property displayDate
 * @property headlines
 * @property subheadlines
 * @property description
 * @property credits
 * @property taxonomy
 * @property promoItems
 * @property owner
 * @property planning
 * @property revision
 * @property syndication
 * @property source
 * @property tracking
 * @property additionalProperties
 * @property duration
 * @property videoType
 * @property youtubeContentId
 * @property streams
 * @property subtitles
 * @property promoImage
 * @property embedHtml
 * @property adTagUrl
 * @constructor Create empty Arc video stream
 */
@Keep
@JsonClass(generateAdapter = true)
public data class ArcVideoStream(  //TODO check nullability for items of this class
    val type: String,
    @Json(name = "_id") val id: String,
    val uuid: String?,
    val version: String?,
    @Json(name = "canonical_url") val canonicalUrl: String,
    @Json(name = "short_url") val shortUrl: String?,
    @Json(name = "created_date") val createdDate: String,
    @Json(name = "last_updated_date") val lastUpdatedDate: String,
    @Json(name = "published_date") val publishedDate: String?,
    @Json(name = "first_publish_date") val firstPublishDate: String,
    @Json(name = "display_date") val displayDate: String,
    val headlines: Headlines?,
    val subheadlines: Subheadlines?,
    val description: Description?,
    val credits: Credits?,
    val taxonomy: Taxonomy?,
    //@Json(name ="promo_items") val promoItems: PromoItems,
    //val owner: Owner,
    //val planning: Planning,
    //val revision: Revision,
    //val syndication: Syndication,
    //val source: Source,
    //val tracking: Tracking,
    @Json(name = "additional_properties") val additionalProperties: AdditionalProperties?,
    val duration: Long?,
    @Json(name = "video_type") val videoType: String,
    //@Json(name ="youtube_content_id") val youtubeContentId: String,
    val streams: List<Stream>,
    val subtitles: SubtitleUrls?,
    @Json(name ="promo_image") val promoImage: PromoItemBasic,
    //@Json(name ="embed_html") val embedHtml: String,

    var adTagUrl: String? = null,
    val status: String? = null
) {
    /**
     * This algorithm iterates through the streams looking for one that is of the preferred type and comes closest
     * to matching the max bitrate.  If a match is not found it uses recursion to call the algorithm again with the
     * next preferred stream type (the order is HLS, TS, MP4, GIF, GIF-MP4) until we get a match or we have exhausted our
     * preferred types.
     *
     * @param preferredtype Stream type to try to use
     * @param preferredbitrate Bit rate to try to use
     * @return [ArcVideoStream] object
     */
    fun findBestStream(
        preferredtype: ArcXPVideoConfig.PreferredStreamType,
        preferredbitrate: Int
    ): Stream? {
        if (streams == null) {
            return null
        }
        if (streams.size == 1) {
            return streams[0]
        }
        //This is where we will store the stream we are going to return
        var bestMatchStream: Stream? = null
        //Diff between our max and our current bitrates.  We are looking for a stream that gets closest
        var bitrateDelta: Int = Int.MAX_VALUE
        //Iterate through the streams
        for (stream in streams) {
            //Only look at the streams that are of the preferred type
            if (preferredtype.getPreferredStreamType() == stream.streamType) {
                //Only look at streams below the max bitrate
                if (stream.bitrate != null && stream.bitrate <= preferredbitrate) {
                    //Only look at streams that come closer to our target bit rate
                    if (preferredbitrate - stream.bitrate < bitrateDelta) {
                        //This is a possible winner so save off the info
                        bitrateDelta = preferredbitrate - stream.bitrate
                        bestMatchStream = stream;
                    }
                }
            }
        }
        if (bestMatchStream == null) {
            val nextType = preferredtype.next()
            //make sure we haven't looped all the way back around to the beginning.
            if (nextType != ArcXPVideoConfig.PreferredStreamType.HLS) {
                //If we haven't found a winner yet then use recursion to call the algorithm again
                //but this time use the next preferred type in the list.
                bestMatchStream = findBestStream(nextType, preferredbitrate)
            } else {
                //If we have looped through everything and have not found a winner then I guess
                //we will pick the first stream
                if (bestMatchStream == null && streams != null) {
                    bestMatchStream = streams[0]
                }
            }
        }
        //Return our winner
        return bestMatchStream
    }
}

fun ArcVideoStream.url() = "$baseUrl$canonicalUrl"

@Keep
@JsonClass(generateAdapter = true)
data class  TypeParams(
    val country: String?,
    val zip: String?,
    val dma: String?
)

@Keep
@JsonClass(generateAdapter = true)
data class  ComputedLocation(
    val country: String?,
    val zip: String?,
    val dma: String?
)

@Keep
@JsonClass(generateAdapter = true)
data class  Headlines(
    val basic: String?,
    @Json(name = "meta_title") val metaTitle: String?
)

@Keep
@JsonClass(generateAdapter = true)
data class  Subheadlines(
    val basic: String?
)

@Keep
@JsonClass(generateAdapter = true)
data class  Description(
    val basic: String?
)

@Keep
@JsonClass(generateAdapter = true)
data class  Credits(
    val by: List<CreditsBy>?
)

@Keep
@JsonClass(generateAdapter = true)
data class  CreditsBy(
    val type: String?,
    val name: String?,
    val org: String?
)

@Keep
@JsonClass(generateAdapter = true)
data class  Taxonomy(
    val tags: List<Tag>?,
    @Json(name = "primary_site") val primarySite: Site?,
    val sites: List<Site>?,
    @Json(name = "seo_keywords") val seoKeywords: List<String>?
)

@Keep
@JsonClass(generateAdapter = true)
data class  Tag(
    val text: String
)

@Keep
@JsonClass(generateAdapter = true)
data class  Site(
    val type: String?,
    @Json(name = "_id") val id: String?,
    val version: String?,
    val name: String?,
    val path: String?,
    val primary: Boolean?
)

@Keep
@JsonClass(generateAdapter = true)
data class  PromoItems(
    val basic: PromoItemBasic?
)

@Keep
@JsonClass(generateAdapter = true)
data class  PromoItemBasic(
    val type: String?,
    val version: String?,
    val credits: Credits?,
    val url: String?,
    val width: Int?,
    val height: Int?
)

@Keep
@JsonClass(generateAdapter = true)
data class  Owner(
    val name: String?
)

@Keep
@JsonClass(generateAdapter = true)
data class  Planning(
    val scheduling: Scheduling?
)

@Keep
@JsonClass(generateAdapter = true)
data class  Scheduling(
    val unknown: String = "Fill in details later"
)

@Keep
@JsonClass(generateAdapter = true)
data class  Revision(
    val published: String?
)

@Keep
@JsonClass(generateAdapter = true)
data class  Syndication(
    val search: Boolean?
)

@Keep
@JsonClass(generateAdapter = true)
data class  Source(
    val name: String?,
    val system: String?,
    @Json(name = "edit_url") val editUrl: String?
)

@Keep
@JsonClass(generateAdapter = true)
data class  Tracking(
    @Json(name = "in_url_headline") val inUrlHeadline: String?
)

@Keep
@JsonClass(generateAdapter = true)
data class  AdditionalProperties(
    //val trackAsPool: Boolean,
    //val subsection: String,
    //val videoCategory: String,
    //val gifAsThumbnail: Boolean,
    //val videoId: String,
    //val vertical: Boolean,
    //val embedContinuousPlay: Boolean,
    //val published: Boolean,
    //val imageResizerUrls: List<String>,
    val advertising: Advertising?,
    //val videoAdZone: String,
    //@Json(name ="lastPublishedBy") val lastPublished: PublishedBy,
    //val platform: String,
    //val playVideoAds: Boolean,
    //val playlist: String,
    //val forceClosedCaptionsOn: Boolean,
    //val doNotShowTranscripts: Boolean,
    //val useVariants: Boolean,
    //val playlistTags: List<String>,
    //@Json(name ="firstPublishedBy") val firstPublished: PublishedBy
)

@Keep
@JsonClass(generateAdapter = true)
data class  Advertising(
    val adInsertionUrls: AdInsertionUrls?,
    //val adSetUrls: List<String>,
    //val forceAd: Boolean,
    //val playAds: Boolean,
    //val playVideoAds: Boolean,
    //val enableAutoPreview: Boolean,
    //val commercialAdNode: String,
    //val autoPlayPreroll: Boolean,
    //val allowPrerollOnDomain: Boolean,
    val enableAdInsertion: Boolean?,
    //val videoAdZone: String?
)

@Keep
@JsonClass(generateAdapter = true)
data class  AdInsertionUrls(
    val mt_master: String?,
    val mt_root: String?,
    val mt_session: String?
)

@Keep
@JsonClass(generateAdapter = true)
data class  PublishedBy(
    val email: String?,
    val name: String?,
    val lastname: String?
)

@Keep
@JsonClass(generateAdapter = true)
data class  Stream(
    val height: Int,
    val width: Int,
    val filesize: Long?,
    @Json(name = "stream_type") val streamType: String?,
    val url: String,
    val bitrate: Int?,
    val provider: String?
)

@Keep
@JsonClass(generateAdapter = true)
data class  SubtitleUrls(
    val urls: List<SubtitleUrl>?
)

@Keep
@JsonClass(generateAdapter = true)
data class  SubtitleUrl(
    val format: String?,
    val url: String?
)

