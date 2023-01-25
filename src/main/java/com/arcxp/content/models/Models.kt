package com.arcxp.content.models

import androidx.annotation.Keep
import com.arcxp.content.ArcXPContentSDK
import com.arcxp.content.extendedModels.ArcXPStory
import com.arcxp.content.util.Constants.RESIZE_URL_KEY
import com.squareup.moshi.Json
import java.util.*
import kotlin.math.max

/**
 * A stream of video. Configuration for a piece of video content, over a stream.
 *
 * @property url Where to get the stream from
 * @property width The width of the video.
 * @property height The height of the video.
 * @property filesize The size of the video, in bytes.
 * @property streamType The type of video (e.g. mp4).
 * @property bitrate The bitrate of the video.
 * @property video_codec The video codec.
 * @property audio_codec The audio codec.
 * @property provider The provider of the video.
 */
@Keep
data class Streams(
    val url: String?,
    val width: Int?,
    val height: Int?,
    val filesize: Long?,
    @Json(name = "stream_type") val streamType: String?,
    val bitrate: Int?,
    val video_codec: Int?,
    val audio_codec: Int?,
    val provider: String?
)

@Keep
data class AdditionalProperties(
    val has_published_copy: Boolean?,
    val date_created: String?,
    val first_published: String?,
    val last_published: String?,
    val publication_end: String?,
    val publication_start: String?,
    val published: Boolean?,
    val roles: List<String>?,
    val template: String?,
    val version: Int?,
    val countryId: Int?,
    val galleries: List<Gallery>?,
    val keywords: List<String>?,
    val mime_type: String?,
    val originalName: String?,
    val originalUrl: String?,
    val fullSizeResizeUrl: String?,
    val ingestionMethod: String?,
    val owner: String?,
    val proxyUrl: String?,
    val resizeUrl: String?,
    val takenOn: String?,
    val subsection: String?,
    val videoId: String?,
    val comments: List<Any>?,
    val videoCategory: String?,
    val gifAsThumbnail: Boolean?,
    val permalinkUrl: String?,
    val platform: String?,
    val playlist: String?,
    val forceClosedCaptionsOn: Boolean?,
    val thumbnailResizeUrl: String?
)

/**
 * Various unrelated fields that should be preserved for backwards-compatibility reasons. See also trait_source.
 *
 * @property id The machine-readable unique identifier of the organization whose database this content is stored in. In Arc, this is equivalent to ARC_ORG_NAME.
 * @property name Deprecated in 0.10.7. See `distributor.name`. (Formerly: The human-readable name of original producer of content. Distinguishes between Wires, Staff and other sources.)
 * @property sponsored True if this content is advertorial or native advertising.
 */
@Keep
data class Owner(
    val id: String?,
    val name: String?,
    val sponsored: Boolean?
)

/**
 * Revision data class
 *
 * @property branch The name of the branch this revision was created on.
 * @property editions A list of identifiers of editions that point to this revision.
 * @property published Whether or not this revision's parent story is published, in any form or place
 * @property revision_id The unique id of this revision.
 * @property parent_id The unique id of the revision that this revisions was branched from, or preceded it on the current branch.
 * @property user_id The unique user id of the person who created this revision.
 * @property additional_properties A grab-bag object for non-validatable data.
 */
@Keep
data class Revision(
    val branch: String?,
    val editions: List<String>?,
    val published: Boolean?,
    val revision_id: String?,
    val parent_id: String?,
    val user_id: String?,
    val additional_properties: Map<String, *>?
)


/**
 * Address data class following the convention of http://microformats.org/wiki/hcard
 *
 * @constructor populates immutable fields
 * @property country_name Country Name
 * @property locality City/Town
 * @property postal_code Zip/Postal Code
 * @property region Region/State
 * @property street_address Street Address
 * @property post_office_box PO Box
 * @property extended_address Extended Address
 * @property additional_properties A grab-bag object for non-validatable data.
 */
@Keep
data class Address(
    val country_name: String?,
    val locality: String?,
    val postal_code: String?,
    val region: String?,
    val street_address: String?,
    val post_office_box: String?,
    val extended_address: String?,
    val additional_properties: Map<String, *>?
)

@Keep
data class Headline(
    val basic: String?,
    val print: String?,
    val social: String?,
    val native: String?,
    val tablet: String?,
    val web: String?
)

/**
 * Credits of article
 *
 * @property by The primary author(s) of this document. For a story, is is the writer or reporter. For an image, it is the photographer.
 * @property photos_by The photographer(s) of supplementary images included in this document, if it is a story. Note that if this document is an image, the photographer(s) should appear in the 'by' slot.
 */
@Keep
data class Credits(
    val by: List<CreditsBy>?,
    val photos_by: List<CreditsBy>?,
) {
    /**
     * Information about article credit from author or photographer
     *
     * @property _id Globally Unique ID trait: A globally unique identifier of the content in the ANS repository.
     * @property image An image: Holds attributes of an ANS image component. In the Arc ecosystem, these are stored in Anglerfish.
     * @property name Name: The full human name of contributor. See also byline, first_name, last_name, middle_name, suffix.
     * @property org Organization:
     * @property slug A short reference name for internal editorial use
     * @property social_links Social Links: Links to various social media
     * @property type could be 'author' or 'reference'
     * @property url A link to an author's landing page on the website, or a personal website.
     * @property version Describes the ANS version of this object: The version of ANS that this object was serialized as, in major.minor.patch format.  For top-level content objects, this is a required trait.
     * @property first_name The real first name of a human author.
     * @property middle_name The real middle name of a human author.
     * @property last_name The real last name of a human author.
     * @property suffix The real suffix of a human author.
     * @property byline The public-facing name, or nom-de-plume, name of the author.
     * @property location The city or locality that the author resides in or is primarily associated with.
     * @property division The desk or group that this author normally reports to. E.g., 'Politics' or 'Sports.'
     * @property email The professional email address of this author.
     * @property role The organizational role or title of this author.
     * @property expertise A comma-delimited list of subjects the author in which the author has expertise.
     * @property affiliation The name of an organization the author is affiliated with. E.g., The Washington Post, or George Mason University.
     * @property languages A description of list of languages that the author is somewhat fluent in, excluding the native language of the parent publication, and identified in the language of the parent publication. E.g., Russian, Japanese, Greek.
     * @property bio A one or two sentence description of the author.
     * @property long_bio The full biography of the author.
     * @property books A list of books written by the author.
     * @property education A list of schools that this author has graduated from.
     * @property awards A list of awards the author has received.
     * @property contributor If true, this author is an external contributor to the publication.
     * @property subtype A user-defined categorization method to supplement type. In Arc, this field is reserved for organization-defined purposes, such as selecting the PageBuilder template that should be used to render a document.
     * @property channels Channel trait: An optional list of output types for which this element should be visible
     * @property alignment Alignment: A property used to determine horizontal positioning of a content element relative to the elements that immediately follow it in the element sequence. could be 'left', 'right', or 'center'
     * @property additional_properties A grab-bag object for non-validatable data.
     */
    @Keep
    data class CreditsBy(
        val _id: String?,
        val image: Image?,
        val name: String?,
        val org: String?,
        val slug: String?,
        val social_links: List<Social>?,
        val type: String?,
        val url: String?,
        val version: String?,
        val first_name: String?,
        val middle_name: String?,
        val last_name: String?,
        val suffix: String?,
        val byline: String?,
        val location: String?,
        val division: String?,
        val email: String?,
        val role: String?,
        val expertise: String?,
        val affiliation: String?,
        val languages: String?,
        val bio: String?,
        val long_bio: String?,
        val books: List<Book>?,
        val education: List<School>?,
        val awards: List<Award>?,
        val contributor: Boolean?,
        val subtype: String?,
        val channels: List<String>?,
        val alignment: String?,
        val additional_properties: Map<String, *>?
    ) {
        /**
         * book written by the author.
         *
         * @property book_title The book title.
         * @property book_url A link to a page to purchase or learn more about the book.
         */
        @Keep
        data class Book(
            val book_title: String?,
            val book_url: String?,
        )

        /**
         * school that this author has graduated from.
         *
         * @property school_name The name of the school.
         */
        @Keep
        data class School(
            val school_name: String?
        )

        /**
         * award the author has received
         *
         * @property award_name The name of the award.
         */
        @Keep
        data class Award(
            val award_name: String?
        )
    }
}

/**
 * Social Links: Links to various social media
 *
 * @property site
 * @property url
 */
@Keep
data class Social(
    val site: String?,
    val url: String?
)

/**
 * Latitidue and Longitude of the content
 *
 * @property latitude
 * @property longitude
 */
@Keep
data class Geo(
    val latitude: Double?,
    val longitude: Double?
)

/**
 * Holds the collection of tags, categories, keywords, etc that describe content.
 *
 * @property keywords A list of keywords. In the Arc ecosystem, this list is populated by Clavis.
 * @property named_entities A list of named entities. In the Arc ecosystem, this list is populated by Clavis.
 * @property topics A list of topics. In the Arc ecosystem, this list is populated by Clavis.
 * @property auxiliaries A list of auxiliaries. In the Arc ecosystem, this list is populated by Clavis.
 * @property tags Models a keyword used in describing a piece of content.
 * @property primary_section A hierarchical section in a taxonomy. In the Arc ecosystem, these are stored in the arc-site-service.
 * @property sections A list of site objects or references to them. In the Arc ecosystem, references in this list are denormalized into sites from the arc-site-service.  In a multi-site context, sites will be denormalized against an organization's default website only.
 * @property seo_keywords A list of user-editable manually entered keywords for search purposes. In the Arc ecosystem, these can be generated and saved in source CMS systems, editors, etc.
 * @property stock_symbols A list of stock symbols of companies related to this content. In the Arc ecosystem, these can be generated and saved in source CMS systems, editors, etc.
 * @property associated_tasks A list of WebSked task IDs that this content was created or curated to satisfy.
 * @property additional_properties A grab-bag object for non-validatable data.
 */
@Keep
data class Taxonomy(
    val keywords: List<Keyword>?,
    val named_entities: List<NamedEntity>?,
    val topics: List<Topic>?,
    val auxiliaries: List<Auxiliary>?,
    val tags: List<Tag>?,
    val primary_section: Section?,
    val sections: List<Section>?,
    val seo_keywords: List<String>?,
    val stock_symbols: List<String>?,
    val associated_tasks: List<String>?,
    val additional_properties: Map<String, *>?
) {
    /**
     * Models a keyword used in describing a piece of content.
     *
     * @property keyword The keyword used to describe a piece of content
     * @property score An arbitrary weighting to give the keyword, this is a javascript number so could be float or int
     * @property tag The Part of Speech tag for this keyword.
     * @property frequency An optional count of the frequency of the keyword as it appears in the content it describes
     */
    @Keep
    data class Keyword(
        val keyword: String?,
        val score: Any?,
        val tag: String?,
        val frequency: Int?
    )

    /**
     * Models a named entity (i.e. name of a person, place, or organization) used in a piece of content.
     *
     * @property _id A unique identifier for the concept of the named entity.
     * @property name The actual string of text that was identified as a named entity.
     * @property type A description of what the named entity is. E.g. 'organization', 'person', or 'location'.
     * @property number An optional relevancy for this named entity. (could be int or double)
     */
    @Keep
    data class NamedEntity(
        val _id: String?,
        val name: String?,
        val type: String?,
        val number: Any?
    )

    /**
     * Models a topic used in describing a piece of content.
     *
     * @property _id The unique identifier for this topic.
     * @property name The general name for this topic.
     * @property score An arbitrary weighting to give the topic (could be int or double)
     * @property uid A short identifier for this topic. Usually used in cases where a long form id cannot work.
     */
    @Keep
    data class Topic(
        val _id: String?,
        val name: String?,
        val score: Any?,
        val uid: String?
    )

    /**
     * Models a auxiliary used in targeting a piece of content.
     *
     * @property _id The unique identifier for this auxiliary.
     * @property name The general name for this auxiliary.
     * @property uid A short identifier for this auxiliary. Usually used in cases where a long form id cannot work.
     */
    @Keep
    data class Auxiliary(
        val _id: String?,
        val name: String?,
        val uid: String?
    )

    /**
     * Models a keyword used in describing a piece of content.
     *
     * @property _id Globally Unique ID trait
     * @property type will be type 'tag'
     * @property subtype A user-defined categorization method to supplement type. In Arc, this field is reserved for organization-defined purposes, such as selecting the PageBuilder template that should be used to render a document.
     * @property text The text of the tag as displayed to users.
     * @property slug A short reference name for internal editorial use
     * @property additional_properties A grab-bag object for non-validatable data.
     */
    @Keep
    data class Tag(
        val _id: String?,
        val type: String?,
        val subtype: String?,
        val text: String?,
        val slug: String?,
        val additional_properties: Map<String, *>?
    )

    /**
     * A primary section object or reference to one. In the Arc ecosystem, a reference here is denormalized into a site from the arc-site-service.
     *
     * @property type will be type 'section'
     * @property _id Globally Unique ID trait: A globally unique identifier of the content in the ANS repository.
     * @property _website The _id of the website on which this document exists. This field is only available in Content API. If different from canonical_website, then this document was originally sourced from the canonical_website. Generated at fetch time by Content API.
     * @property version Describes the ANS version of this object: The version of ANS that this object was serialized as, in major.minor.patch format.  For top-level content objects, this is a required trait.
     * @property name The name of this site
     * @property description A short description or tagline about this site
     * @property path The url path to this site
     * @property parent_id The id of this section's parent site, if any
     * @property primary Is this the primary site?
     */
    @Keep
    data class Section(

        val type: String?,
        val _id: String?,
        val _website: String?,
        val version: String?,
        val name: String?,
        val description: String?,
        val path: String?,
        val parent_id: String?,
        val primary: Boolean?,
    )
}

/**
 * The descriptions, or blurbs, for the content.
 *
 * @property basic
 */
@Keep
data class Description(
    val basic: String?
)

/**
 * Lists of promotional content to use when highlighting the story. In the Arc ecosystem, references in these lists will be denormalized.
 *
 * @property basic
 */
@Keep
data class PromoItem(
    val basic: PromoItemBasic?,
    val lead_art: PromoItemBasic?
) {
    /**
     *
     * @property _id Globally Unique ID trait: A globally unique identifier of the content in the ANS repository.
     * @property address Address: An Address following the convention of http://microformats.org/wiki/hcard
     * @property alignment Alignment: A property used to determine horizontal positioning of a content element relative to the elements that immediately follow it in the element sequence. could be 'left', 'right', or 'center'
     * @property canonical_url Canonical URL: The relative URL to this document on the website specified by the `canonical_website` field. In the Arc ecosystem, this will be populated by the content api from the arc-canonical-url service if present based on the canonical_website. In conjunction with canonical_website, it can be used to determine the SEO canonical url or open graph url. In a multi-site context, this field may be different from the website_url field.
     * @property caption Caption for an image.
     * @property channels Channel trait: An optional list of output types for which this element should be visible
     * @property content Any arbitrary chunk of  HTML.
     * @property copyright Copyright information: A copyright notice for the legal owner of this content. E.g., '© 1996-2018 The Washington Post.' Format may vary between organizations.
     * @property created_date Created Date: When the content was originally created (RFC3339-formatted). In the Arc ecosystem, this will be automatically generated for stories in the Story API.
     * @property credits Credits: A list of people and groups attributed to this content, keyed by type of contribution. In the Arc ecosystem, references in this list will be denormalized into author objects from the arc-author-service.
     * @property description Description: The descriptions, or blurbs, for the content.
     * @property display_date Display_Date: The RFC3339-formatted dated time of the most recent date the story was (re)displayed on a public site.
     * @property editor_note Editor_Note: Additional information to be displayed near the content from the editor.
     * @property embed A custom embed element. Can be used to reference content from external providers about which little is known.
     * @property first_publish_date First Publish Date: When the story was first published.
     * @property geo Geo: Latitude and Longitude of the content
     * @property headlines Headlines: The headline(s) or title for this content.
     * @property height Height for an image.
     * @property width Width for an image.
     * @property last_updated_date Last Updated Date: When the content was last updated (RFC3339-formatted).
     * @property language Locale: The primary language of the content. The value should follow IETF BCP47. (e.g. 'en', 'es-419', etc.)
     * @property licensable True if the image can legally be licensed to others.
     * @property location Location related trait: A description of the location, useful if a full address or lat/long specification is overkill.
     * @property owner Various unrelated fields that should be preserved for backwards-compatibility reasons. See also trait_source.
     * @property publish_date Publish_Date: When the story was published.
     * @property short_url Short_Url: A url-shortened version of the canonical url.
     * @property status Status: Optional field to story story workflow related status (e.g. published/embargoed/etc)
     * @property subheadlines Sub-Headlines: The sub-headline(s) for the content.
     * @property subtitle Subtitle for an image.
     * @property subtype Subtype or Template: A user-defined categorization method to supplement type. In Arc, this field is reserved for organization-defined purposes, such as selecting the PageBuilder template that should be used to render a document.
     * @property taxonomy Holds the collection of tags, categories, keywords, etc that describe content.
     * @property type could be "string", "raw_html", or "custom_embed"
     * @property url URL for an image.
     * @property version The version of ANS that this object was serialized as, in major.minor.patch format.  For top-level content objects, this is a required trait.
     * @property additional_properties A grab-bag object for non-validatable data.
     */
    @Keep
    data class PromoItemBasic(
        val _id: String?,
        val address: Address?,
        val alignment: String?,
        val canonical_url: String?,
        val caption: String?,
        val channels: List<String>?,
        val content: String?,
        val copyright: String?,
        val created_date: Date?,
        val credits: Credits?,
        val description: Description?,
        val display_date: Date?,
        val editor_note: String?,
        val embed: Embed?,
        val first_publish_date: Date?,
        val geo: Geo?,
        val headlines: Headline?,
        val height: Int?,
        val width: Int?,
        val last_updated_date: Date?,
        val language: String?,
        val licensable: Boolean?,
        val location: String?,
        val owner: Owner?,
        val publish_date: Date?,
        val short_url: String?,
        val status: String?,
        val subheadlines: Headline?,
        val subtitle: String?,
        val subtype: String?,
        val taxonomy: Taxonomy?,
        val type: String?,
        val url: String?,
        val version: String?,
        val promo_items: PromoItem?,
        val additional_properties: Map<String, *>?
    )
}

/**
 * The embed data.
 *
 * @property id Embed ID
 * @property url Embed Provider URL
 * @property config Embed Configuration: Arbitrary configuration data generated by a plugin. Users are responsible for maintaining schema.
 */
@Keep
data class Embed(
    val id: String?,
    val url: String?,
    val config: Map<String, *>?
)

@Keep
data class Parent(
    val clone: String?,
    val matTest: String?,
    val ellipsis: String?,
    val test: String?,
    val goldfish: String?,
    val default: String?,
    val cloneSomesite: String?,
    val nativeAppBar: String?,
    val cloneLaur: String?,
    val cloneTest: String?,
    val nativeApp: String?
)

@Keep
data class SiteInfo(
    val website_section: WebsiteSection?,
    val website_url: String?
)

/**
 * sealed class: could be either Section or Reference based on type
 *
 * @property type "section" or "reference"
 */
@Keep
sealed class WebsiteSection(val type: String) {
    /**
     * Section data class
     *
     * @property _id Globally Unique ID trait: A globally unique identifier of the content in the ANS repository.
     * @property _website The _id of the website on which this document exists. This field is only available in Content API. If different from canonical_website, then this document was originally sourced from the canonical_website. Generated at fetch time by Content API.
     * @property version Describes the ANS version of this object: The version of ANS that this object was serialized as, in major.minor.patch format.  For top-level content objects, this is a required trait.
     * @property name The name of this site
     * @property description A short description or tagline about this site
     * @property path The url path to this site
     * @property parent_id The id of this section's parent section in the default hierarchy, if any.
     * @property parent The id of this section's parent section in various commonly-used hierarchies, where available
     * @property primary Is this the primary site?
     * @property additional_properties A grab-bag object for non-validatable data.
     */
    @Keep
    data class Section(
        val _id: String?,
        val _website: String?,
        val version: String?,
        val name: String?,
        val description: String?,
        val path: String?,
        val parent_id: String?,
        val parent: Parent?,
        val primary: Boolean?,
        val additional_properties: Map<String, Any?>?
    ) : WebsiteSection(type = "section")

    /**
     * Reference data class
     *
     * @property _id Globally Unique ID trait: A globally unique identifier of the content in the ANS repository.
     * @property subtype A user-defined categorization method to supplement type. In Arc, this field is reserved for organization-defined purposes, such as selecting the PageBuilder template that should be used to render a document.
     * @property channels Channel trait: An optional list of output types for which this element should be visible
     * @property referent
     * @property alignment Alignment: A property used to determine horizontal positioning of a content element relative to the elements that immediately follow it in the element sequence. could be 'left', 'right', or 'center'
     * @property additional_properties A grab-bag object for non-validatable data.
     */
    @Keep
    data class Reference(
        val _id: String?,
        val subtype: String?,
        val channels: List<String>?,
        val referent: Referent?,
        val alignment: String?,
        val additional_properties: Map<*, *>?
    ) : WebsiteSection(type = "reference")
}

/**
 * Trait that applies contains the content restrictions of an ANS object.
 *
 * @property content_code The content restriction code/level/flag associated with the ANS object
 * @property embargo Embargo configuration to enforce publishing restrictions. Embargoed content must not go live.
 * @property geo Geo-Restriction configuration that contains the restriction ids that this content should be associated with.
 */
@Keep
data class ContentRestrictions(
    val content_code: String?,
    val embargo: Embargo?,
    val geo: Geo?
) {
    /**
     * Embargo configuration to enforce publishing restrictions. Embargoed content must not go live.
     *
     * @property active The boolean flag to indicate if the embargo is active or not. If this field is false, ignore the embargo.
     * @property end_time An optional end time for the embargo to indicate when it ends. When it's not defined, it means the embargo keeps applying. The end time should be ignored if active flag is false.
     * @property description An optional description for the embargo.
     */
    @Keep
    data class Embargo(
        val active: Boolean?,
        val end_time: Date?,
        val description: String?,
    )
}

/**
 * Trait that applies workflow information to a document or resource. In the Arc ecosystem, this data is generated by WebSked.
 *
 * @property status_code Code indicating the story's current workflow status. This number should match the values configured in WebSked.
 * @property note This note will be used for any task automatically generated via WebSked task triggers.
 * @property additional_properties A grab-bag object for non-validatable data.
 */
@Keep
data class WorkFlow(
    val status_code: Int?,
    val note: String?,
    val additional_properties: Map<String, *>?
)

/**
 * The default label object for this piece of content.
 *
 * @property text The text of this label.
 * @property url An optional destination url of this label.
 * @property display If false, this label should be hidden.
 * @property additional_properties A grab-bag object for non-validatable data.
 */
@Keep
data class Label(
    val text: String?,
    val url: String?,
    val display: Boolean?,
    val additional_properties: Map<String, *>?
)

/**
 * Information about the original source and/or owner of this content
 *
 * @property source_id The id of this content in a foreign CMS.
 * @property source_type Deprecated in 0.10.7. See `distributor.category` and `distributor.subcategory`. (Formerly: The method used to enter this content. E.g. 'staff', 'wires'.)
 * @property name Deprecated in  0.10.7. See `distributor.name`. (Formerly: The human-readable name of the organization who first produced this content. E.g., 'Reuters'.)
 * @property system The software (CMS or editor) that was used to enter this content. E.g., 'wordpress', 'ellipsis'.
 * @property edit_url A link to edit this content in its source CMS.
 * @property additional_properties A grab-bag object for non-validatable data.
 */
@Keep
data class Source(
    val source_id: String?,
    val source_type: String?,
    val name: String?,
    val system: String?,
    val edit_url: String?,
    val additional_properties: Map<String, *>?
)

@Keep
data class Headlines(
    val basic: String?
)

@Keep
sealed class StoryElement(val type: String) {
    class UnknownStoryElement : StoryElement(type = "unknown")
}

/**
 * Sealed class for links, could be interstitial_link or text based on type property
 *
 * @property type could be "interstitial_link" or "text"
 */
@Keep
sealed class Links(val type: String) {
    /**
     * Interstitial Link
     *
     * @property _id Globally Unique ID trait: A globally unique identifier of the content in the ANS repository.
     * @property subtype A user-defined categorization method to supplement type. In Arc, this field is reserved for organization-defined purposes, such as selecting the PageBuilder template that should be used to render a document.
     * @property channels Channel trait: An optional list of output types for which this element should be visible
     * @property alignment Alignment: A property used to determine horizontal positioning of a content element relative to the elements that immediately follow it in the element sequence. could be 'left', 'right', or 'center'
     * @property additional_properties A grab-bag object for non-validatable data.
     * @property url The interstitial link url. This is where the user should be taken if they follow this link.
     * @property content Link Title: The interstitial link title text. This text should be considered part of the link.
     * @property description The interstitial link url. This is where the user should be taken if they follow this link.
     * @property image An optional image. This should be considered part of the link.
     */
    @Keep
    data class InterstitialLink(
        val _id: String?,
        val subtype: String?,
        val channels: List<String>?,
        val alignment: String?,
        val additional_properties: Map<String, *>?,
        val url: String?,
        val content: String?,
        val description: Text?,
        val image: Image?,
    ) : Links(type = "interstitial_link")

    /**
     * A textual content element
     *
     * @property _id Globally Unique ID trait: A globally unique identifier of the content in the ANS repository.
     * @property subtype A user-defined categorization method to supplement type. In Arc, this field is reserved for organization-defined purposes, such as selecting the PageBuilder template that should be used to render a document.
     * @property channels Channel trait: An optional list of output types for which this element should be visible
     * @property alignment Alignment: A property used to determine horizontal positioning of a content element relative to the elements that immediately follow it in the element sequence. could be 'left', 'right', or 'center'
     * @property block_properties Block properties for style formatting content elements
     * @property additional_properties A grab-bag object for non-validatable data.
     * @property content The text of the paragraph.
     */
    @Keep
    data class Text(
        val _id: String?,
        val subtype: String?,
        val channels: List<String>?,
        val alignment: String?,
        val block_properties: BlockProperties?,
        val additional_properties: Map<String, *>?,
        val content: String?
    ) : Links(type = "text") {
        /**
         * Block properties for style formatting content elements
         *
         * @property dropcap Style the first letter of the first word with a dropcap
         */
        @Keep
        data class BlockProperties(
            val dropcap: String?
        )
    }

    /**
     * this object gets returned given an unknown link
     */
    class UnknownLinks : Links(type = "unknown")
}

/**
 * this object gets returned given an unknown WebsiteSection
 */
class UnknownWebsiteSection : WebsiteSection(type = "unknown")

/**
 * this object gets returned given an unknown StoryListElement
 */
class UnknownStoryListElement : StoryListElement(type = "unknown")

/**
 * RawHTML story element data class
 *
 * @property _id Globally Unique ID trait: A globally unique identifier of the content in the ANS repository.
 * @property subtype A user-defined categorization method to supplement type. In Arc, this field is reserved for organization-defined purposes, such as selecting the PageBuilder template that should be used to render a document.
 * @property channels Channel trait: An optional list of output types for which this element should be visible
 * @property alignment Alignment: A property used to determine horizontal positioning of a content element relative to the elements that immediately follow it in the element sequence. could be 'left', 'right', or 'center'
 * @property content Any arbitrary chunk of  HTML.
 */
@Keep
data class RawHTML(
    val _id: String?,
    val subtype: String?,
    val channels: List<String>?,
    val alignment: String?,
    val content: String?,
) : StoryElement(type = "raw_html")

/**
 * Table story element data class
 *
 * @property _id Globally Unique ID trait: A globally unique identifier of the content in the ANS repository.
 * @property subtype A user-defined categorization method to supplement type. In Arc, this field is reserved for organization-defined purposes, such as selecting the PageBuilder template that should be used to render a document.
 * @property channels Channel trait: An optional list of output types for which this element should be visible
 * @property additional_properties A grab-bag object for non-validatable data.
 * @property header The header row of the table
 * @property rows  The data rows of the table
 */
@Keep
data class Table(
    val _id: String?,
    val subtype: String?,
    val channels: List<String>?,
    val additional_properties: Map<String, *>?,
    val header: List<Text>?,
    val rows: List<Text>?,
) : StoryElement(type = "table")

/**
 * Holds attributes of an ANS video component. In the Arc ecosystem, these are stored in Goldfish.
 *
 * @property _id Globally Unique ID trait: A globally unique identifier of the content in the ANS repository.
 * @property version Describes the ANS version of this object: The version of ANS that this object was serialized as, in major.minor.patch format.  For top-level content objects, this is a required trait.
 * @property subtype A user-defined categorization method to supplement type. In Arc, this field is reserved for organization-defined purposes, such as selecting the PageBuilder template that should be used to render a document.
 * @property channels Channel trait: An optional list of output types for which this element should be visible
 * @property alignment Alignment: A property used to determine horizontal positioning of a content element relative to the elements that immediately follow it in the element sequence. could be 'left', 'right', or 'center'
 * @property copyright Copyright information: A copyright notice for the legal owner of this content. E.g., '© 1996-2018 The Washington Post.' Format may vary between organizations.
 * @property canonical_url Canonical URL: The relative URL to this document on the website specified by the `canonical_website` field. In the Arc ecosystem, this will be populated by the content api from the arc-canonical-url service if present based on the canonical_website. In conjunction with canonical_website, it can be used to determine the SEO canonical url or open graph url. In a multi-site context, this field may be different from the website_url field.
 * @property canonical_website The _id of the website from which this document was originally authored. In conjunction with canonical_url, it can be used to determine the SEO canonical url or open graph url. In a multi-site context, this field may be different from the website field.
 * @property short_url Short_Url: A url-shortened version of the canonical url.
 * @property created_date Created Date: When the content was originally created (RFC3339-formatted). In the Arc ecosystem, this will be automatically generated for stories in the Story API.
 * @property last_updated_date Last Updated Date: When the content was last updated (RFC3339-formatted).
 * @property publish_date Publish_Date: When the story was published.
 * @property first_publish_date First Publish Date: When the story was first published.
 * @property display_date Display_Date: The RFC3339-formatted dated time of the most recent date the story was (re)displayed on a public site.
 * @property geo Geo: Latitude and Longitude of the content
 * @property language The primary language of the content. The value should follow IETF BCP47. (e.g. 'en', 'es-419', etc.)
 * @property location Location related trait: A description of the location, useful if a full address or lat/long specification is overkill.
 * @property address An Address following the convention of http://microformats.org/wiki/hcard
 * @property editor_note Editor_Note: Additional information to be displayed near the content from the editor.
 * @property status Status: Optional field to story story workflow related status (e.g. published/embargoed/etc)
 * @property headlines Headlines: The headline(s) or title for this content.
 * @property subheadlines SubHeadlines: The subheadline(s) or title for this content.
 * @property description The interstitial link url. This is where the user should be taken if they follow this link.
 * @property credits Credits: A list of people and groups attributed to this content, keyed by type of contribution. In the Arc ecosystem, references in this list will be denormalized into author objects from the arc-author-service.
 * @property vanity_credits Similar to the credits trait, but to be used only when ANS is being directly rendered to readers natively. For legal and technical reasons, the `credits` trait is preferred when converting ANS into feeds or other distribution formats. However, when present, `vanity_credits` allows more sophisticated credits presentation to override the default without losing that original data.
 * @property taxonomy Holds the collection of tags, categories, keywords, etc that describe content.
 * @property promo_items Lists of promotional content to use when highlighting the story. In the Arc ecosystem, references in these lists will be denormalized.
 * @property related_content Lists of content items or references this story is related to, arbitrarily keyed. In the Arc ecosystem, references in this object will be denormalized into the fully-inflated content objects they represent.
 * @property owner Various unrelated fields that should be preserved for backwards-compatibility reasons. See also trait_source.
 * @property planning Trait that applies planning information to a document or resource. In the Arc ecosystem, this data is generated by WebSked. Newsroom use only. All these fields should be available and editable in WebSked.
 * @property pitches Trait that represents a story's pitches. In the Arc ecosystem, this data is generated by WebSked.
 * @property source Information about the original source and/or owner of this content
 * @property distributor Information about a third party that provided this content from outside this document's hosted organization.
 * @property tracking Tracking information, probably implementation-dependent
 * @property comments Comment configuration data
 * @property label What the Washington Post calls a Kicker
 * @property slug A short reference name for internal editorial use
 * @property content_restrictions Trait that applies contains the content restrictions of an ANS object.
 * @property content_aliases An list of alternate names that this content can be fetched by instead of id.
 * @property duration Runtime of the video in milliseconds.
 * @property transcript A transcript of the contents of the video.
 * @property rating A rating of the video, to be used for appropriate age/content warnings.
 * @property video_type The type of video (e.g. clip, livestream, etc)
 * @property youtube_content_id The YouTube ID of the content, if (re)hosted on youtube.com
 * @property streams The different streams this video can play in.
 * @property subtitles Data about different subtitle encodings and confidences of auto-transcribed content.
 * @property additional_properties A grab-bag object for non-validatable data.
 * @property image_type A more specific category for an image. "photograph", "graphic", "illustration", or "thumbnail"
 * @property alt_text The direct ANS equivalent of the HTML 'alt' attribute. A description of the contents of an image for improved accessibility.
 * @property focal_point coordinates representing the 'visual center' of an image. The X axis is horizontal line and the Y axis the vertical line, with 0,0 being the LEFT, TOP of the image.
 * @property subtitle Subtitle for the image.
 * @property caption Caption for the image.
 * @property url URL for the image.
 * @property width Width for the image.
 * @property height Height for the image.
 * @property licensable True if the image can legally be licensed to others.
 * @property contributors Trait that holds information on who created and contributed to a given document in Arc.
 * @property promo_image A promo/leader image to the video.
 * @property embed_html An HTML snippet used to embed this video in another document. Used for oembed responses.
 * @property corrections Trait that applies a list of corrections to a document.
 * @property websites Website-specific  metadata for url generation for multi-site copies. These fields are not indexed in Content API.
 */
@Keep
data class Video(
    val _id: String?,
    val version: String?,
    val subtype: String?,
    val channels: List<String>?,
    val alignment: String?,

    val copyright: String?,
    val canonical_url: String?,
    val canonical_website: String?,
    val short_url: String?,

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

    val editor_note: String?,
    val status: String?,
    val headlines: Headline?,
    val subheadlines: Headline?,
    val description: Description?,
    val credits: Credits?,
    val vanity_credits: Credits?,
    val taxonomy: Taxonomy?,
    val promo_items: PromoItem?,
    val related_content: Map<String, *>?,
    val owner: Owner?,
    val planning: Planning?,
    val pitches: Pitches?,
    val source: Source?,
    val distributor: Distributor?,
    val tracking: Any?,
    val comments: Comments?,
    val label: Label?,
    val slug: String?,
    val content_restrictions: ContentRestrictions?,
    val content_aliases: List<String>?,
    val duration: Int?,
    val transcript: String?,
    val rating: String?,
    val video_type: String?,
    val youtube_content_id: String?,
    val streams: List<Streams>?,
    val subtitles: Subtitle?,
    val additional_properties: Map<String, *>?,
    val image_type: String?,
    val alt_text: String?,
    val focal_point: Image.FocalPoint?,
    val subtitle: String?,
    val caption: String?,
    val url: String?,
    val width: Int?,
    val height: Int?,
    val licensable: Boolean?,
    val contributors: Image.CreatedBy?,
    val promo_image: Image?,
    val embed_html: String?,
    val corrections: List<Correction>?,
    val websites: Map<String, SiteInfo>?,

    ) : StoryElement(type = "video") {
    /**
     * Subtitle for video
     *
     * @property confidence How confident the transcriber (human or automated) is of the accuracy of the subtitles. (int/double)
     * @property urls The locations of any subtitle transcriptions of the video.
     */
    @Keep
    data class Subtitle(
        val confidence: Any?,
        val urls: List<String>?
    )
}

/**
 * Quote story element data class
 *
 * @property _id Globally Unique ID trait: A globally unique identifier of the content in the ANS repository.
 * @property subtype A user-defined categorization method to supplement type. In Arc, this field is reserved for organization-defined purposes, such as selecting the PageBuilder template that should be used to render a document.
 * @property channels Channel trait: An optional list of output types for which this element should be visible
 * @property alignment Alignment: A property used to determine horizontal positioning of a content element relative to the elements that immediately follow it in the element sequence. could be 'left', 'right', or 'center'
 * @property content_elements A collection of content. Holds attributes of an ANS collection - a common parent to story and gallery objects.
 * @property additional_properties A grab-bag object for non-validatable data.
 * @property citation A textual content element
 */
@Keep
data class Quote(
    val _id: String?,
    val subtype: String?,
    val channels: List<String>?,
    val alignment: String?,
    val content_elements: List<StoryElement>?,
    val additional_properties: Map<String, *>?,
    val citation: Text?
) : StoryElement(type = "quote")

/**
 * Numeric rating story element data class: Indicates a numeric rating value
 *
 * @property _id Globally Unique ID trait: A globally unique identifier of the content in the ANS repository.
 * @property subtype A user-defined categorization method to supplement type. In Arc, this field is reserved for organization-defined purposes, such as selecting the PageBuilder template that should be used to render a document.
 * @property channels Channel trait: An optional list of output types for which this element should be visible
 * @property alignment Alignment: A property used to determine horizontal positioning of a content element relative to the elements that immediately follow it in the element sequence. could be 'left', 'right', or 'center'
 * @property additional_properties A grab-bag object for non-validatable data.
 * @property numeric_rating A number indicating the item's value. (int or double)
 * @property min The minimum possible value of rating. (int or double)
 * @property max The maximum possible value of rating. (int or double)
 * @property units A string describing the rating units. (int or double)
 */
@Keep
data class NumericRating(
    val _id: String?,
    val subtype: String?,
    val channels: List<String>?,
    val alignment: String?,
    val additional_properties: Map<String, *>?,
    val numeric_rating: Any?,
    val min: Any?,
    val max: Any?,
    val units: String?,
) : StoryElement(type = "numeric_rating")

/**
 * Text story element data class: A textual content element
 *
 * @property _id Globally Unique ID trait: A globally unique identifier of the content in the ANS repository.
 * @property subtype A user-defined categorization method to supplement type. In Arc, this field is reserved for organization-defined purposes, such as selecting the PageBuilder template that should be used to render a document.
 * @property channels Channel trait: An optional list of output types for which this element should be visible
 * @property alignment Alignment: A property used to determine horizontal positioning of a content element relative to the elements that immediately follow it in the element sequence. could be 'left', 'right', or 'center'
 * @property block_properties Block properties for style formatting content elements
 * @property additional_properties A grab-bag object for non-validatable data.
 * @property content The text of the paragraph.
 */
@Keep
data class Text(
    val _id: String?,
    val subtype: String?,
    val channels: List<String>?,
    val alignment: String?,
    val block_properties: BlockProperties?,
    val additional_properties: Map<String, *>?,
    val content: String?
) : StoryElement(type = "text") {
    /**
     * Block properties for style formatting content elements
     *
     * @property dropcap Style the first letter of the first word with a dropcap
     */
    @Keep
    data class BlockProperties(
        val dropcap: String?
    )
}

/**
 * Code sample story element data class
 *
 * @property _id Globally Unique ID trait: A globally unique identifier of the content in the ANS repository.
 * @property subtype A user-defined categorization method to supplement type. In Arc, this field is reserved for organization-defined purposes, such as selecting the PageBuilder template that should be used to render a document.
 * @property channels Channel trait: An optional list of output types for which this element should be visible
 * @property alignment Alignment: A property used to determine horizontal positioning of a content element relative to the elements that immediately follow it in the element sequence. could be 'left', 'right', or 'center'
 * @property additional_properties A grab-bag object for non-validatable data.
 * @property content The code sample.
 * @property language The programming or markup language of the code sample
 */
@Keep
data class Code(
    val _id: String?,
    val subtype: String?,
    val channels: List<String>?,
    val alignment: String?,
    val additional_properties: Map<String, *>?,
    val content: String?,
    val language: String?,
) : StoryElement(type = "code")

/**
 * Describes a change that has been made to the document for transparency, or describes inaccuracies or falsehoods that remain in the document.
 *
 * @property _id Globally Unique ID trait: A globally unique identifier of the content in the ANS repository.
 * @property subtype A user-defined categorization method to supplement type. In Arc, this field is reserved for organization-defined purposes, such as selecting the PageBuilder template that should be used to render a document.
 * @property channels Channel trait: An optional list of output types for which this element should be visible
 * @property alignment Alignment: A property used to determine horizontal positioning of a content element relative to the elements that immediately follow it in the element sequence. could be 'left', 'right', or 'center'
 * @property additional_properties A grab-bag object for non-validatable data.
 * @property correction_type Type of correction. E.g., clarification, retraction.
 * @property text The text of the correction.
 */
@Keep
data class Correction(
    val _id: String?,
    val subtype: String?,
    val channels: List<String>?,
    val alignment: String?,
    val additional_properties: Map<String, *>?,
    val correction_type: String?,
    val text: String?,
) : StoryElement(type = "correction")

/**
 * A custom embed element. Can be used to reference content from external providers about which little is known.
 *
 * @property _id Globally Unique ID trait: A globally unique identifier of the content in the ANS repository.
 * @property subtype A user-defined categorization method to supplement type. In Arc, this field is reserved for organization-defined purposes, such as selecting the PageBuilder template that should be used to render a document.
 * @property channels Channel trait: An optional list of output types for which this element should be visible
 * @property alignment Alignment: A property used to determine horizontal positioning of a content element relative to the elements that immediately follow it in the element sequence. could be 'left', 'right', or 'center'
 * @property additional_properties A grab-bag object for non-validatable data.
 * @property embed A custom embed element. Can be used to reference content from external providers about which little is known.
 */
@Keep
data class CustomEmbed(
    val _id: String?,
    val subtype: String?,
    val channels: List<String>?,
    val alignment: String?,
    val additional_properties: Map<String, *>?,
    val embed: Embed?
) : StoryElement(type = "custom_embed")

/**
 * A divider between segments of an article.
 *
 * @property _id Globally Unique ID trait: A globally unique identifier of the content in the ANS repository.
 * @property subtype A user-defined categorization method to supplement type. In Arc, this field is reserved for organization-defined purposes, such as selecting the PageBuilder template that should be used to render a document.
 * @property channels Channel trait: An optional list of output types for which this element should be visible
 * @property alignment Alignment: A property used to determine horizontal positioning of a content element relative to the elements that immediately follow it in the element sequence. could be 'left', 'right', or 'center'
 * @property additional_properties A grab-bag object for non-validatable data.
 */
@Keep
data class Divider(
    val _id: String?,
    val subtype: String?,
    val channels: List<String>?,
    val alignment: String?,
    val additional_properties: Map<String, *>?
) : StoryElement(type = "divider")

/**
 * A sub-sequence of related content elements
 *
 * @property _id Globally Unique ID trait: A globally unique identifier of the content in the ANS repository.
 * @property subtype A user-defined categorization method to supplement type. In Arc, this field is reserved for organization-defined purposes, such as selecting the PageBuilder template that should be used to render a document.
 * @property channels Channel trait: An optional list of output types for which this element should be visible
 * @property alignment Alignment: A property used to determine horizontal positioning of a content element relative to the elements that immediately follow it in the element sequence. could be 'left', 'right', or 'center'
 * @property additional_properties A grab-bag object for non-validatable data.
 * @property content_elements A collection of content. Holds attributes of an ANS collection - a common parent to story and gallery objects.
 */
@Keep
data class ElementGroup(
    val _id: String?,
    val subtype: String?,
    val channels: List<String>?,
    val alignment: String?,
    val additional_properties: Map<String, *>?,
    val content_elements: List<StoryElement>?,
) : StoryElement(type = "element_group")

/**
 * A string indicating the item's value.
 *
 * @property _id Globally Unique ID trait: A globally unique identifier of the content in the ANS repository.
 * @property subtype A user-defined categorization method to supplement type. In Arc, this field is reserved for organization-defined purposes, such as selecting the PageBuilder template that should be used to render a document.
 * @property channels Channel trait: An optional list of output types for which this element should be visible
 * @property alignment Alignment: A property used to determine horizontal positioning of a content element relative to the elements that immediately follow it in the element sequence. could be 'left', 'right', or 'center'
 * @property additional_properties A grab-bag object for non-validatable data.
 * @property endorsement A string indicating the value.
 */
@Keep
data class Endorsement(
    val _id: String?,
    val subtype: String?,
    val channels: List<String>?,
    val alignment: String?,
    val additional_properties: Map<String, *>?,
    val endorsement: String?
) : StoryElement(type = "endorsement")

/**
 * A Header story element data class
 *
 * @property _id Globally Unique ID trait: A globally unique identifier of the content in the ANS repository.
 * @property subtype A user-defined categorization method to supplement type. In Arc, this field is reserved for organization-defined purposes, such as selecting the PageBuilder template that should be used to render a document.
 * @property channels Channel trait: An optional list of output types for which this element should be visible
 * @property alignment Alignment: A property used to determine horizontal positioning of a content element relative to the elements that immediately follow it in the element sequence. could be 'left', 'right', or 'center'
 * @property additional_properties A grab-bag object for non-validatable data.
 * @property content The text of the heading.
 * @property level Header level
 */
@Keep
data class Header(
    val _id: String?,
    val subtype: String?,
    val channels: List<String>?,
    val alignment: String?,
    val additional_properties: Map<String, *>?,
    val content: String?,
    val level: Int?
) : StoryElement(type = "header")

/**
 * Interstitial Link story element data class
 *
 * @property _id Globally Unique ID trait: A globally unique identifier of the content in the ANS repository.
 * @property subtype A user-defined categorization method to supplement type. In Arc, this field is reserved for organization-defined purposes, such as selecting the PageBuilder template that should be used to render a document.
 * @property channels Channel trait: An optional list of output types for which this element should be visible
 * @property alignment Alignment: A property used to determine horizontal positioning of a content element relative to the elements that immediately follow it in the element sequence. could be 'left', 'right', or 'center'
 * @property additional_properties A grab-bag object for non-validatable data.
 * @property url The interstitial link url. This is where the user should be taken if they follow this link.
 * @property content Link Title: The interstitial link title text. This text should be considered part of the link.
 * @property description Additional text about the link. May or may not be considered part of the link, depending on implementation.
 * @property image An optional image. This should be considered part of the link.
 */
@Keep
data class InterstitialLink(
    val _id: String?,
    val subtype: String?,
    val channels: List<String>?,
    val alignment: String?,
    val additional_properties: Map<String, *>?,
    val url: String?,
    val content: String?,
    val description: Text?,
    val image: Image?,
) : StoryElement(type = "interstitial_link")

/**
 * A list of links to related or external content that may be embedded in a story as a unit. Can be used for curated re-circulation, or simple 'More Information' boxes. Each item in the list must be an interstitial link.
 *
 * @property _id Globally Unique ID trait: A globally unique identifier of the content in the ANS repository.
 * @property subtype A user-defined categorization method to supplement type. In Arc, this field is reserved for organization-defined purposes, such as selecting the PageBuilder template that should be used to render a document.
 * @property channels Channel trait: An optional list of output types for which this element should be visible
 * @property alignment Alignment: A property used to determine horizontal positioning of a content element relative to the elements that immediately follow it in the element sequence. could be 'left', 'right', or 'center'
 * @property additional_properties A grab-bag object for non-validatable data.
 * @property title The title of the list. Plain-text with no formatting.
 * @property items The links in this list.
 */
@Keep
data class LinkList(
    val _id: String?,
    val subtype: String?,
    val channels: List<String>?,
    val alignment: String?,
    val additional_properties: Map<String, *>?,
    val title: String?,
    val items: Links?,
) : StoryElement(type = "link_list")

/**
 * list of text items or other lists
 *
 * @property _id Globally Unique ID trait: A globally unique identifier of the content in the ANS repository.
 * @property subtype A user-defined categorization method to supplement type. In Arc, this field is reserved for organization-defined purposes, such as selecting the PageBuilder template that should be used to render a document.
 * @property channels Channel trait: An optional list of output types for which this element should be visible
 * @property alignment Alignment: A property used to determine horizontal positioning of a content element relative to the elements that immediately follow it in the element sequence. could be 'left', 'right', or 'center'
 * @property additional_properties A grab-bag object for non-validatable data.
 * @property list_type The type of list to render as (ordered, unordered, etc)
 * @property items The items in this list.
 */
@Keep
data class StoryList(
    val _id: String?,
    val subtype: String?,
    val channels: List<String>?,
    val alignment: String?,
    val additional_properties: Map<String, *>?,
    val list_type: String?,
    val items: List<StoryListElement>?,
) : StoryElement(type = "list")

/**
 * An oembed object
 *
 * @property _id Globally Unique ID trait: A globally unique identifier of the content in the ANS repository.
 * @property subtype A user-defined categorization method to supplement type. In Arc, this field is reserved for organization-defined purposes, such as selecting the PageBuilder template that should be used to render a document.
 * @property channels Channel trait: An optional list of output types for which this element should be visible
 * @property additional_properties A grab-bag object for non-validatable data.
 * @property raw_oembed
 * @property referent
 */
@Keep
data class OembedResponse(
    val _id: String?,
    val subtype: String?,
    val channels: List<String>?,
    val additional_properties: Map<String, *>?,
    val raw_oembed: Any?,
    val referent: Referent?
) : StoryElement(type = "oembed_response")

/**
 * sealed class for story list element, could be text or list sub types depending on type field
 *
 * @property type "text" or "list"
 */
@Keep
sealed class StoryListElement(val type: String) {
    /**
     * A textual content element
     *
     * @property _id Globally Unique ID trait: A globally unique identifier of the content in the ANS repository.
     * @property subtype A user-defined categorization method to supplement type. In Arc, this field is reserved for organization-defined purposes, such as selecting the PageBuilder template that should be used to render a document.
     * @property channels Channel trait: An optional list of output types for which this element should be visible
     * @property alignment Alignment: A property used to determine horizontal positioning of a content element relative to the elements that immediately follow it in the element sequence. could be 'left', 'right', or 'center'
     * @property block_properties Block properties for style formatting content elements
     * @property additional_properties A grab-bag object for non-validatable data.
     * @property content The text of the paragraph.
     */
    @Keep
    data class Text(
        val _id: String?,
        val subtype: String?,
        val channels: List<String>?,
        val alignment: String?,
        val block_properties: BlockProperties?,
        val additional_properties: Map<String, *>?,
        val content: String?
    ) : StoryListElement(type = "text") {
        /**
         * Block properties for style formatting content elements
         *
         * @property dropcap Style the first letter of the first word with a dropcap
         */
        @Keep
        data class BlockProperties(
            val dropcap: String?
        )
    }

    /**
     * Story List item data class
     *
     * @property _id Globally Unique ID trait: A globally unique identifier of the content in the ANS repository.
     * @property subtype A user-defined categorization method to supplement type. In Arc, this field is reserved for organization-defined purposes, such as selecting the PageBuilder template that should be used to render a document.
     * @property channels Channel trait: An optional list of output types for which this element should be visible
     * @property alignment Alignment: A property used to determine horizontal positioning of a content element relative to the elements that immediately follow it in the element sequence. could be 'left', 'right', or 'center'
     * @property additional_properties A grab-bag object for non-validatable data.
     * @property list_type The type of list to render as (ordered, unordered, etc)
     * @property items nested story list elements
     */
    @Keep
    data class StoryListItem(
        val _id: String?,
        val subtype: String?,
        val channels: List<String>?,
        val alignment: String?,
        val additional_properties: Map<String, *>?,
        val list_type: String?,
        val items: List<StoryListElement>?,
    ) : StoryListElement(type = "list")
}

/**
 * Image ANS type
 *
 * @property _id Globally Unique ID trait: A globally unique identifier of the content in the ANS repository.
 * @property version Describes the ANS version of this object: The version of ANS that this object was serialized as, in major.minor.patch format.  For top-level content objects, this is a required trait.
 * @property subtype A user-defined categorization method to supplement type. In Arc, this field is reserved for organization-defined purposes, such as selecting the PageBuilder template that should be used to render a document.
 * @property channels Channel trait: An optional list of output types for which this element should be visible
 * @property alignment Alignment: A property used to determine horizontal positioning of a content element relative to the elements that immediately follow it in the element sequence. could be 'left', 'right', or 'center'
 * @property copyright Copyright information: A copyright notice for the legal owner of this content. E.g., '© 1996-2018 The Washington Post.' Format may vary between organizations.
 * @property canonical_url Canonical URL: The relative URL to this document on the website specified by the `canonical_website` field. In the Arc ecosystem, this will be populated by the content api from the arc-canonical-url service if present based on the canonical_website. In conjunction with canonical_website, it can be used to determine the SEO canonical url or open graph url. In a multi-site context, this field may be different from the website_url field.
 * @property short_url Short_Url: A url-shortened version of the canonical url.
 * @property created_date Created Date: When the content was originally created (RFC3339-formatted). In the Arc ecosystem, this will be automatically generated for stories in the Story API.
 * @property last_updated_date Last Updated Date: When the content was last updated (RFC3339-formatted).
 * @property publish_date Publish_Date: When the story was published.
 * @property first_publish_date First Publish Date: When the story was first published.
 * @property display_date Display_Date: The RFC3339-formatted dated time of the most recent date the story was (re)displayed on a public site.
 * @property geo Geo: Latitude and Longitude of the content
 * @property language The primary language of the content. The value should follow IETF BCP47. (e.g. 'en', 'es-419', etc.)
 * @property location Location related trait: A description of the location, useful if a full address or lat/long specification is overkill.
 * @property address An Address following the convention of http://microformats.org/wiki/hcard
 * @property editor_note Editor_Note: Additional information to be displayed near the content from the editor.
 * @property status Status: Optional field to story story workflow related status (e.g. published/embargoed/etc)
 * @property headlines Headlines: The headline(s) or title for this content.
 * @property subheadlines SubHeadlines: The subheadline(s) or title for this content.
 * @property description The descriptions, or blurbs, for the content
 * @property credits Credits: A list of people and groups attributed to this content, keyed by type of contribution. In the Arc ecosystem, references in this list will be denormalized into author objects from the arc-author-service.
 * @property vanity_credits Similar to the credits trait, but to be used only when ANS is being directly rendered to readers natively. For legal and technical reasons, the `credits` trait is preferred when converting ANS into feeds or other distribution formats. However, when present, `vanity_credits` allows more sophisticated credits presentation to override the default without losing that original data.
 * @property promo_items Lists of promotional content to use when highlighting the story. In the Arc ecosystem, references in these lists will be denormalized.
 * @property related_content Lists of content items or references this story is related to, arbitrarily keyed. In the Arc ecosystem, references in this object will be denormalized into the fully-inflated content objects they represent.
 * @property owner Various unrelated fields that should be preserved for backwards-compatibility reasons. See also trait_source.
 * @property planning Trait that applies planning information to a document or resource. In the Arc ecosystem, this data is generated by WebSked. Newsroom use only. All these fields should be available and editable in WebSked.
 * @property pitches Trait that represents a story's pitches. In the Arc ecosystem, this data is generated by WebSked.
 * @property source Information about the original source and/or owner of this content
 * @property distributor Information about a third party that provided this content from outside this document's hosted organization.
 * @property tracking Tracking information, probably implementation-dependent
 * @property comments Comment configuration data
 * @property label What the Washington Post calls a Kicker
 * @property slug A short reference name for internal editorial use
 * @property content_restrictions Trait that applies contains the content restrictions of an ANS object.
 * @property image_type A more specific category for an image. "photograph", "graphic", "illustration", or "thumbnail"
 * @property alt_text The direct ANS equivalent of the HTML 'alt' attribute. A description of the contents of an image for improved accessibility.
 * @property focal_point coordinates representing the 'visual center' of an image. The X axis is horizontal line and the Y axis the vertical line, with 0,0 being the LEFT, TOP of the image.
 * @property additional_properties A grab-bag object for non-validatable data.
 * @property subtitle Subtitle for the image.
 * @property caption Caption for the image.
 * @property url URL for the image.
 * @property width Width for the image.
 * @property height Height for the image.
 * @property licensable True if the image can legally be licensed to others.
 * @property contributors Trait that holds information on who created and contributed to a given document in Arc.
 */
@Keep
data class Image(
    val _id: String?,
    val version: String?,
    val subtype: String?,
    val channels: List<String>?,
    val alignment: String?,
    val copyright: String?,
    val canonical_url: String?,
    val short_url: String?,

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

    val editor_note: String?,
    val status: String?,
    val headlines: Headline?,
    val subheadlines: Headline?,
    val description: Description?,
    val credits: Credits?,
    val vanity_credits: Credits?,
    val promo_items: PromoItem?,
    val related_content: Map<String, *>?,
    val owner: Owner?,
    val planning: Planning?,
    val pitches: Pitches?,
    val source: Source?,
    val distributor: Distributor?,
    val tracking: Any?,
    val comments: Comments?,
    val label: Label?,
    val slug: String?,
    val content_restrictions: ContentRestrictions?,
    val image_type: String?,
    val alt_text: String?,
    val focal_point: FocalPoint?,
    val additional_properties: Map<String, *>?,
    val subtitle: String?,
    val caption: String?,
    val url: String?,
    val width: Int?,
    val height: Int?,
    val licensable: Boolean?,
    val contributors: CreatedBy?,

    ) : StoryElement(type = "image") {
    /**
     * coordinates representing the 'visual center' of an image. The X axis is horizontal line and the Y axis the vertical line, with 0,0 being the LEFT, TOP of the image.
     *
     * @property x The coordinate point on the horizontal axis (int or double)
     * @property y The coordinate point on the vertical axis (int or double)
     */
    @Keep
    data class FocalPoint(
        val x: Any?,
        val y: Any?
    )

    /**
     * Trait that holds information on who created and contributed to a given document in Arc.
     *
     * @property user_id The unique ID of the Arc user who created the Document
     * @property display_name The display name of the Arc user who created the Document
     */
    @Keep
    data class CreatedBy(
        val user_id: String?,
        val display_name: String?,
    )
}

fun Image.imageUrl(): String {
    //whether in portrait or landscape, we don't want an image with resolution larger than the max screen dimension
    val maxScreenSize = ArcXPContentSDK.resizer().getScreenSize()
    val imageHeight = this.height
    val imageWidth = this.width

    //if undesired (param = false) or we don't have height/width we do not want to resize
    val weShouldResize =
        (imageHeight != null) and
                (imageWidth != null)
    if (weShouldResize) {
        //choose whether the maximum dimension is width or height
        val maxImageSize = max(imageHeight!!, imageWidth!!)

        //we want to scale preserving aspect ratio on this dimension
        val maxIsHeight = maxImageSize == imageHeight

        ///if image is smaller than device we do not want to resize
        if (maxImageSize >= maxScreenSize) {
            val finalUrl = if (this.type == "video") {
                this.url?.substringAfter("=/")
            } else {
                (this.additional_properties?.get(RESIZE_URL_KEY) as? String)?.substringAfter(
                    "=/"
                )
            }
            if (finalUrl?.isNotEmpty() == true) {
                return if (maxIsHeight) {
                    ArcXPContentSDK.resizer().resizeHeight(url = finalUrl, height = maxScreenSize)
                } else {
                    ArcXPContentSDK.resizer().resizeWidth(url = finalUrl, width = maxScreenSize)
                }
            }

        } else return this.url ?: ""

    }
    return ""
}

/**
 * Gallery ANS Type
 *
 * @property _id Globally Unique ID trait: A globally unique identifier of the content in the ANS repository.
 * @property version Describes the ANS version of this object: The version of ANS that this object was serialized as, in major.minor.patch format.  For top-level content objects, this is a required trait.
 * @property subtype A user-defined categorization method to supplement type. In Arc, this field is reserved for organization-defined purposes, such as selecting the PageBuilder template that should be used to render a document.
 * @property channels Channel trait: An optional list of output types for which this element should be visible
 * @property alignment Alignment: A property used to determine horizontal positioning of a content element relative to the elements that immediately follow it in the element sequence. could be 'left', 'right', or 'center'
 * @property copyright Copyright information: A copyright notice for the legal owner of this content. E.g., '© 1996-2018 The Washington Post.' Format may vary between organizations.
 * @property canonical_url Canonical URL: The relative URL to this document on the website specified by the `canonical_website` field. In the Arc ecosystem, this will be populated by the content api from the arc-canonical-url service if present based on the canonical_website. In conjunction with canonical_website, it can be used to determine the SEO canonical url or open graph url. In a multi-site context, this field may be different from the website_url field.
 * @property short_url Short_Url: A url-shortened version of the canonical url.
 * @property created_date Created Date: When the content was originally created (RFC3339-formatted). In the Arc ecosystem, this will be automatically generated for stories in the Story API.
 * @property last_updated_date Last Updated Date: When the content was last updated (RFC3339-formatted).
 * @property publish_date Publish_Date: When the story was published.
 * @property first_publish_date First Publish Date: When the story was first published.
 * @property display_date Display_Date: The RFC3339-formatted dated time of the most recent date the story was (re)displayed on a public site.
 * @property geo Geo: Latitude and Longitude of the content
 * @property language The primary language of the content. The value should follow IETF BCP47. (e.g. 'en', 'es-419', etc.)
 * @property location Location related trait: A description of the location, useful if a full address or lat/long specification is overkill.
 * @property address An Address following the convention of http://microformats.org/wiki/hcard
 * @property editor_note Editor_Note: Additional information to be displayed near the content from the editor.
 * @property status Status: Optional field to story story workflow related status (e.g. published/embargoed/etc)
 * @property headlines Headlines: The headline(s) or title for this content.
 * @property subheadlines Subheadlines: The subheadline(s) or title for this content.
 * @property description Description: The descriptions, or blurbs, for the content.
 * @property credits Credits: A list of people and groups attributed to this content, keyed by type of contribution. In the Arc ecosystem, references in this list will be denormalized into author objects from the arc-author-service.
 * @property vanity_credits Similar to the credits trait, but to be used only when ANS is being directly rendered to readers natively. For legal and technical reasons, the `credits` trait is preferred when converting ANS into feeds or other distribution formats. However, when present, `vanity_credits` allows more sophisticated credits presentation to override the default without losing that original data.
 * @property promo_items Lists of promotional content to use when highlighting the story. In the Arc ecosystem, references in these lists will be denormalized.
 * @property related_content Lists of content items or references this story is related to, arbitrarily keyed. In the Arc ecosystem, references in this object will be denormalized into the fully-inflated content objects they represent.
 * @property owner Various unrelated fields that should be preserved for backwards-compatibility reasons. See also trait_source.
 * @property planning Trait that applies planning information to a document or resource. In the Arc ecosystem, this data is generated by WebSked. Newsroom use only. All these fields should be available and editable in WebSked.
 * @property pitches Trait that represents a story's pitches. In the Arc ecosystem, this data is generated by WebSked.
 * @property source Information about the original source and/or owner of this content
 * @property distributor Information about a third party that provided this content from outside this document's hosted organization.
 * @property tracking Tracking information, probably implementation-dependent
 * @property comments Comment configuration data
 * @property label What the Washington Post calls a Kicker
 * @property slug A short reference name for internal editorial use
 * @property content_restrictions Trait that applies contains the content restrictions of an ANS object.
 * @property image_type A more specific category for an image. "photograph", "graphic", "illustration", or "thumbnail"
 * @property alt_text The direct ANS equivalent of the HTML 'alt' attribute. A description of the contents of an image for improved accessibility.
 * @property additional_properties A grab-bag object for non-validatable data.
 * @property subtitle Subtitle for the image.
 * @property caption Caption for the image.
 * @property url URL for the image.
 * @property width Width for the image.
 * @property height Height for the image.
 * @property licensable True if the image can legally be licensed to others.
 * @property contributors Trait that holds information on who created and contributed to a given document in Arc.
 * @property content_elements A collection of content. Holds attributes of an ANS collection - a common parent to story and gallery objects.
 * @property content_aliases An list of alternate names that this content can be fetched by instead of id.
 */
@Keep
data class Gallery(
    val _id: String?,
    val version: String?,
    val subtype: String?,
    val channels: List<String>?,
    val alignment: String?,
    val copyright: String?,
    val canonical_url: String?,
    val short_url: String?,

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

    val editor_note: String?,
    val status: String?,
    val headlines: Headline?,
    val subheadlines: Headline?,
    val description: Description?,
    val credits: Credits?,
    val vanity_credits: Credits?,
    val promo_items: PromoItem?,
    val related_content: Map<String, *>?,
    val owner: Owner?,
    val planning: Planning?,
    val pitches: Pitches?,
    val source: Source?,
    val distributor: Distributor?,
    val tracking: Any?,
    val comments: Comments?,
    val label: Label?,
    val slug: String?,
    val content_restrictions: ContentRestrictions?,
    val image_type: String?,
    val alt_text: String?,
    val additional_properties: Map<String, *>?,
    val subtitle: String?,
    val caption: String?,
    val url: String?,
    val width: Int?,
    val height: Int?,
    val licensable: Boolean?,
    val contributors: Image.CreatedBy?,
    val content_elements: List<StoryElement>?,
    val content_aliases: List<String>?,
) : StoryElement(type = "gallery")

/**
 * Trait that applies planning information to a document or resource. In the Arc ecosystem, this data is generated by WebSked. Newsroom use only. All these fields should be available and editable in WebSked.
 *
 * @property additional_properties A grab-bag object for non-validatable data.
 * @property websked_sort_date Date to be used for chronological sorting in WebSked.
 * @property deadline_miss The delta between the story's planned publish date and actual first publish time, in minutes.
 * @property internal_note This note is used for shared communication inside the newsroom.
 * @property budget_line Used for the newsroom to identify what the story is about, especially if a user is unfamiliar with the slug of a story and the headline or they do not yet exist. Newsroom use only.
 * @property scheduling Scheduling information.
 * @property story_length Story length information.
 * @property workFlow Trait that applies workflow information to a document or resource. In the Arc ecosystem, this data is generated by WebSked.
 * @property revision Trait that applies revision information to a document. In the Arc ecosystem, many of these fields are populated in stories by the Story API.
 * @property syndication Key-boolean pair of syndication services where this article may go
 */
@Keep
data class Planning(
    val additional_properties: Map<String, *>?,
    val websked_sort_date: Date?,
    val deadline_miss: Int?,
    val internal_note: String?,
    val budget_line: String?,
    val scheduling: Scheduling?,
    val story_length: StoryLength?,
    val workFlow: WorkFlow?,
    val revision: Revision?,
    val syndication: Syndication?,
)

/**
 * Scheduling information.
 *
 * @property planned_publish_date When the content is planned to be published.
 * @property scheduled_publish_date When the content was first published.
 * @property will_have_gallery Will this content have galleries?
 * @property will_have_graphic Will this content have graphics?
 * @property will_have_image Will this content have images?
 * @property will_have_video Will this content have videos?
 */
@Keep
data class Scheduling(
    val planned_publish_date: Date?,
    val scheduled_publish_date: Date?,
    val will_have_gallery: Boolean?,
    val will_have_graphic: Boolean?,
    val will_have_image: Boolean?,
    val will_have_video: Boolean?
)

/**
 * Story length information.
 *
 * @property word_count_planned The anticipated number of words in the story.
 * @property word_count_actual Current number of words.
 * @property inch_count_planned The anticipated length of the story in inches.
 * @property inch_count_actual The current length of the story in inches.
 * @property line_count_planned The anticipated length of the story in lines.
 * @property line_count_actual The current length of the story in lines.
 * @property character_count_planned The anticipated number of characters in the story.
 * @property character_count_actual The current number of characters in the story.
 * @property character_encoding The encoding used for counting characters in the story.
 */
@Keep
data class StoryLength(
    val word_count_planned: Int?,
    val word_count_actual: Int?,
    val inch_count_planned: Int?,
    val inch_count_actual: Int?,
    val line_count_planned: Int?,
    val line_count_actual: Int?,
    val character_count_planned: Int?,
    val character_count_actual: Int?,
    val character_encoding: String?
)

/**
 * Trait that represents a story's pitches. In the Arc ecosystem, this data is generated by WebSked.
 *
 * @property additional_properties A grab-bag object for non-validatable data.
 * @property platform A list of the story's pitches to a platform.
 * @property publication A list of the story's pitches to a publication.
 */
@Keep
data class Pitches(
    val additional_properties: Map<String, *>?,
    val platform: List<PlatformPitch>?,
    val publication: List<PublicationPitch>?
)

/**
 * Platform pitch: Trait that represents a pitch to a platform. In the Arc ecosystem, this data is generated by WebSked.
 *
 * @property additional_properties A grab-bag object for non-validatable data.
 * @property platform_path The path of the platform that this pitch targets.
 * @property creation_event Trait that represents initial event for a pitch to a platform. In the Arc ecosystem, this data is generated by WebSked.
 * @property latest_event Trait that represents latest event for a pitch to a platform. In the Arc ecosystem, this data is generated by WebSked.
 */
@Keep
data class PlatformPitch(
    val additional_properties: Map<String, *>?,
    val platform_path: String?,
    val creation_event: PlatformPitchEvent,
    val latest_event: PlatformPitchEvent
) {
    /**
     * Trait that represents an update event for a pitch to a platform. In the Arc ecosystem, this data is generated by WebSked.
     *
     * @property additional_properties A grab-bag object for non-validatable data.
     * @property status The current status of the pitch.
     * @property time The time of this update.
     * @property user_id The ID of the user who made this update
     * @property note Optional note associated with this update.
     */
    @Keep
    data class PlatformPitchEvent(
        val additional_properties: Map<String, *>?,
        val status: String?,
        val time: Date?,
        val user_id: String?,
        val note: String?
    )
}

/**
 * Trait that represents a pitch to a publication. In the Arc ecosystem, this data is generated by WebSked.
 *
 * @property additional_properties A grab-bag object for non-validatable data.
 * @property publication_id The ID of the publication that this pitch targets.
 * @property creation_event Trait that represents initial event for a pitch to a publication. In the Arc ecosystem, this data is generated by WebSked.
 * @property latest_event Trait that represents latest event for a pitch to a publication. In the Arc ecosystem, this data is generated by WebSked.
 */
@Keep
data class PublicationPitch(
    val additional_properties: Map<String, *>?,
    val publication_id: String?,
    val creation_event: PublicationPitchEvent,
    val latest_event: PublicationPitchEvent
) {
    /**
     * Trait that represents an update event for a pitch to a publication. In the Arc ecosystem, this data is generated by WebSked.
     *
     * @property additional_properties A grab-bag object for non-validatable data.
     * @property status The current status of the pitch.
     * @property time The time of this update.
     * @property user_id The ID of the user who made this update
     * @property note Optional note associated with this update.
     * @property edition_id The ID of the publication edition that this pitch targets.
     * @property edition_time The time of the publication edition that this pitch targets.
     */
    @Keep
    data class PublicationPitchEvent(
        val additional_properties: Map<String, *>?,
        val status: String?,
        val time: Date?,
        val user_id: String?,
        val note: String?,
        val edition_id: String?,
        val edition_time: Date?
    )
}

/**
 * Key-boolean pair of syndication services where this article may go
 *
 * @property external_distribution Necessary for fulfilling contractual agreements with third party clients
 * @property search Necessary so that we can filter out all articles that editorial has deemed should not be discoverable via search
 */
@Keep
data class Syndication(
    val external_distribution: Boolean?,
    val search: Boolean?
)

/**
 * Information about a third party that provided this content from outside this document's hosted organization.
 *
 * @property name The human-readable name of the distributor of this content. E.g., Reuters.
 * @property reference_id The ARC UUID of the distributor of this content. E.g., ABCDEFGHIJKLMNOPQRSTUVWXYZ.
 * @property category could be any of : "staff", "wires", "freelance", "stock", "handout", "other" : The machine-readable category of how this content was produced. Use 'staff' if this content was produced by an employee of the organization who owns this document repository. (Multisite note: content produced within a single *organization*, but shared across multiple *websites* should still be considered 'staff.') Use ‘wires’ if this content was produced for another organization and shared with the one who owns this document repository. Use 'freelance' if this content was produced by an individual on behalf of the organization who owns this document repository. Use 'stock' if this content is stock media distributed directly from a stock media provider. Use 'handout' if this is content provided from a source for an article (usually promotional media distributed directly from a company). Use 'other' for all other cases.
 * @property subcategory The machine-readable subcategory of how this content was produced. E.g., 'freelance - signed'. May vary between organizations.
 * @property additional_properties A grab-bag object for non-validatable data.
 * @property mode could be "custom" or "reference"
 */
@Keep
data class Distributor(
    val name: String?,
    val reference_id: String?,
    val category: String?,
    val subcategory: String?,
    val additional_properties: Map<String, *>?,
    val mode: String?
)

/**
 * Comment configuration data
 *
 * @property comments_period How long (in days) after publish date until comments are closed.
 * @property allow_comments If false, commenting is disabled on this content.
 * @property display_comments If false, do not render comments on this content.
 * @property moderation_required If true, comments must be moderator-approved before being displayed.
 * @property additional_properties A grab-bag object for non-validatable data.
 */
@Keep
data class Comments(
    val comments_period: Int?,
    val allow_comments: Boolean?,
    val display_comments: Boolean?,
    val moderation_required: Boolean?,
    val additional_properties: Map<String, *>?
)

/**
 * Trait that provides suggestions for the rendering system.
 *
 * @property enum could be "website" or "native" if populated
 * @property type Other than the well-known values are allowed, and can be ignored if not recognized
 */
@Keep
data class RenderingGuide(
    val enum: String?,
    val type: String?
)

/**
 * Referent data class
 *
 * @property type The ANS type that the provider should return.
 * @property service The type of interaction the provider expects. E.g., 'oembed'
 * @property id The id passed to the provider to retrieve an ANS document
 * @property provider A URL that can resolve the id into an ANS element
 * @property website The website which the referenced id belongs to. Currently supported only for sections.
 * @property referent_properties An object for key-value pairs that should override the values of keys with the same name in the denormalized object
 */
@Keep
data class Referent(
    val type: String?,
    val service: String?,
    val id: String?,
    val provider: String?,
    val website: String?,
    val referent_properties: Map<String, *>?
)

@Keep
data class ReferentProperties(
    val additional_properties: AdditionalProperties?
)

@Keep
data class Subheadlines(
    val basic: String?
)

@Keep
data class Publishing(
    val scheduled_operations: ArcXPStory.Publishing.ScheduledOperations?
)