package com.arcxp.content.extendedModels

import androidx.annotation.Keep
import com.arcxp.ArcXPMobileSDK.baseUrl
import com.arcxp.ArcXPMobileSDK.imageUtils
import com.arcxp.content.models.*
import com.arcxp.commons.util.Utils.formatter
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.util.*

/**
 * ArcXPStory - article ANS response object
 *
 * @property _id Globally Unique ID trait: A globally unique identifier of the content in the ANS repository.
 * @property type ANS type for object
 * @property version Describes the ANS version of this object: The version of ANS that this object was serialized as, in major.minor.patch format.  For top-level content objects, this is a required trait.
 * @property alignment Alignment: A property used to determine horizontal positioning of a content element relative to the elements that immediately follow it in the element sequence. could be 'left', 'right', or 'center'
 * @property created_date Created Date: When the content was originally created (RFC3339-formatted). In the Arc ecosystem, this will be automatically generated for stories in the Story API.
 * @property last_updated_date Last Updated Date: When the content was last updated (RFC3339-formatted).
 * @property publish_date Publish_Date: When the story was published.
 * @property first_publish_date First Publish Date: When the story was first published.
 * @property display_date Display_Date: The RFC3339-formatted dated time of the most recent date the story was (re)displayed on a public site.
 * @property geo Geo: Latitude and Longitude of the content
 * @property language The primary language of the content. The value should follow IETF BCP47. (e.g. 'en', 'es-419', etc.)
 * @property location Location related trait: A description of the location, useful if a full address or lat/long specification is overkill.
 * @property address An Address following the convention of http://microformats.org/wiki/hcard
 * @property content_elements nested story elements (text, link, gallery, image, video, etc)
 * @property related_content Lists of content items or references this story is related to, arbitrarily keyed. In the Arc ecosystem, references in this object will be denormalized into the fully-inflated content objects they represent.
 * @property publishing Publishing Information: The current published state of all editions of a content item as well as any scheduled publishing information. Machine-generated.
 * @property revision Trait that applies revision information to a document. In the Arc ecosystem, many of these fields are populated in stories by the Story API.
 * @property website The _id of the website on which this document exists. This field is only available in Content API. If different from canonical_website, then this document was originally sourced from the canonical_website. Generated at fetch time by Content API.
 * @property websites Website-specific  metadata for url generation for multi-site copies. These fields are not indexed in Content API.
 * @property website_url The relative URL to this document on the website specified by the `website` field. In a multi-site context, this is the url that is typically queried on when fetching by URL. It may be different than canonical_url. Generated at fetch time by Content API.
 * @property short_url Short_Url: A url-shortened version of the canonical url.
 * @property channels Channel trait: An optional list of output types for which this element should be visible
 * @property owner Various unrelated fields that should be preserved for backwards-compatibility reasons. See also trait_source.
 * @property credits Credits: A list of people and groups attributed to this content, keyed by type of contribution. In the Arc ecosystem, references in this list will be denormalized into author objects from the arc-author-service.
 * @property vanity_credits Similar to the credits trait, but to be used only when ANS is being directly rendered to readers natively. For legal and technical reasons, the `credits` trait is preferred when converting ANS into feeds or other distribution formats. However, when present, `vanity_credits` allows more sophisticated credits presentation to override the default without losing that original data.
 * @property editor_note Editor_Note: Additional information to be displayed near the content from the editor.
 * @property taxonomy Holds the collection of tags, categories, keywords, etc that describe content.
 * @property copyright A copyright notice for the legal owner of this content. E.g., '© 1996-2018 The Washington Post.' Format may vary between organizations.
 * @property description The descriptions, or blurbs, for the content.
 * @property headlines Headlines: The headline(s) or title for this content.
 * @property subheadlines Sub-Headlines: The sub-headline(s) for the content.
 * @property label What the Washington Post calls a Kicker
 * @property promoItem Lists of promotional content to use when highlighting the story. In the Arc ecosystem, references in these lists will be denormalized.
 * @property content //TODO?
 * @property canonical_url Canonical URL: The relative URL to this document on the website specified by the `canonical_website` field. In the Arc ecosystem, this will be populated by the content api from the arc-canonical-url service if present based on the canonical_website. In conjunction with canonical_website, it can be used to determine the SEO canonical url or open graph url. In a multi-site context, this field may be different from the website_url field.
 * @property canonical_website The _id of the website from which this document was originally authored. In conjunction with canonical_url, it can be used to determine the SEO canonical url or open graph url. In a multi-site context, this field may be different from the website field.
 * @property source Information about the original source and/or owner of this content
 * @property subtype A user-defined categorization method to supplement type. In Arc, this field is reserved for organization-defined purposes, such as selecting the PageBuilder template that should be used to render a document.
 * @property planning Trait that applies planning information to a document or resource. In the Arc ecosystem, this data is generated by WebSked. Newsroom use only. All these fields should be available and editable in WebSked.
 * @property pitches Trait that represents a story's pitches. In the Arc ecosystem, this data is generated by WebSked.
 * @property syndication Key-boolean pair of syndication services where this article may go
 * @property distributor Information about a third party that provided this content from outside this document's hosted organization.
 * @property tracking Tracking information, probably implementation-dependent
 * @property comments Comment configuration data
 * @property slug A short reference name for internal editorial use
 * @property content_restrictions Trait that applies contains the content restrictions of an ANS object.
 * @property content_aliases An list of alternate names that this content can be fetched by instead of id.
 * @property corrections Trait that applies a list of corrections to a document.
 * @property rendering_guides Trait that provides suggestions for the rendering system.
 * @property status Optional field to story story workflow related status (e.g. published/embargoed/etc)
 * @property workFlow Status: Optional field to story story workflow related status (e.g. published/embargoed/etc)
 * @property additional_properties A grab-bag object for non-validatable data.
 */
@Keep
@JsonClass(generateAdapter = true)
data class ArcXPStory(
    val _id: String?,
    val type: String,
    val version: String?,
    val alignment: String?,

    // dates
    val created_date: Date?,
    val last_updated_date: Date?,
    val publish_date: Date?,
    val first_publish_date: Date?,
    val display_date: Date?,

    // location / language
    val geo: Geo?,
    val language: String?,
    val location: String?,
    val address: Address?,

    val content_elements: List<StoryElement>?,

    val related_content: Map<String, *>?,
    val publishing: Publishing?,
    val revision: Revision?,
    val website: String?,
    val websites: Map<String, SiteInfo>?,
    val website_url: String?,
    val short_url: String?,
    val channels: List<String>?,
    val owner: Owner?,
    val credits: Credits?,
    val vanity_credits: Credits?,
    val editor_note: String?,
    val taxonomy: Taxonomy?,
    val copyright: String?,
    val description: Description?,
    val headlines: Headline?,
    val subheadlines: Subheadlines?,
    val label: Label?,
    @Json(name = "promo_items") val promoItem: PromoItem?,
    val content: String?,
    val canonical_url: String?,
    val canonical_website: String?,
    val source: Source?,
    val subtype: String?,
    val planning: Planning?,
    val pitches: Pitches?,
    val syndication: Syndication?,
    val distributor: Distributor?,
    val tracking: Any?,
    val comments: Comments?,
    val slug: String?,
    val content_restrictions: ContentRestrictions?,
    val content_aliases: List<String>?,
    val corrections: List<Correction>?,
    val rendering_guides: List<RenderingGuide>?,
    val status: String?,
    val workFlow: WorkFlow?,
    val additional_properties: Map<String, *>?,
    val duration: Long? //TODO(remove this https://arcpublishing.atlassian.net/browse/ARCMOBILE-5145)
) {
    /**
     * Publishing Information: The current published state of all editions of a content item as well as any scheduled publishing information. Machine-generated.
     *
     * @property has_published_edition True if and only if at least one published edition exists for this content item.
     * @property scheduled_operations A map of lists of operations scheduled to be performed on this content item, sorted by operation type.
     */
    @Keep
    @JsonClass(generateAdapter = true)
    data class Publishing(  //
        val has_published_edition: Boolean?, // 
        val scheduled_operations: ScheduledOperations? // 
    ) {
        /**
         * A map of lists of operations scheduled to be performed on this content item, sorted by operation type.
         *
         * @property publish_edition Publish Operations
         * @property unpublish_edition Un-publish operations
         * @property additional_properties A grab-bag object for non-validatable data.
         */
        @Keep
        @JsonClass(generateAdapter = true)
        data class ScheduledOperations(
            val publish_edition: List<Edition>?,
            val unpublish_edition: List<Edition>?,
            val additional_properties: Map<String, *>?
        ) {
            /**
             * Edition data class
             *
             * @property operation will be "publish_edition" or "unpublish_edition"
             * @property operation_revision_id Revision ID (Operation): The revision id to be published.
             * @property operation_edition Edition Name (Operation): The name of the edition this operation will publish to.
             * @property operation_date Operation Date: The date that this operation will be performed.
             * @property additional_properties A grab-bag object for non-validatable data.
             */
            @Keep
            @JsonClass(generateAdapter = true)
            data class Edition(
                val operation: String?,
                val operation_revision_id: String?,
                val operation_edition: String?,
                val operation_date: String?,
                val additional_properties: Map<String, *>?
            )
        }
    }
}

fun ArcXPStory.imageUrl(): String = this.promoItem?.basic?.let { promoItem->
    imageUtils().imageUrl(promoItem)
    } ?: ""

fun ArcXPStory.fallback() = this.promoItem?.let { promoItem ->
        imageUtils().fallback(promoItem)
    } ?: ""


fun ArcXPStory.date() = this.publish_date?.let { formatter.format(it) } ?: ""

fun ArcXPStory.title() = this.headlines?.basic ?: ""

fun ArcXPStory.description() = this.description?.basic ?: ""

fun ArcXPStory.subheadlines() = this.subheadlines?.basic ?: ""

fun ArcXPStory.author(): String {
    if (this.credits?.by?.isNotEmpty() == true) {
        return this.credits.by.get(0).name ?: ""
    }
    return ""
}

fun ArcXPStory.url() = "$baseUrl$canonical_url"