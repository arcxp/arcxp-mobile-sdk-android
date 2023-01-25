package com.arcxp.content.testUtils

import com.arcxp.content.sdk.extendedModels.ArcXPContentElement
import com.arcxp.content.sdk.extendedModels.ArcXPStory
import com.arcxp.content.sdk.models.*
import com.squareup.moshi.Json
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import java.util.*

interface DispatcherProvider {

    fun main(): CoroutineDispatcher = Dispatchers.Main
    fun default(): CoroutineDispatcher = Dispatchers.Default
    fun io(): CoroutineDispatcher = Dispatchers.IO
    fun unconfined(): CoroutineDispatcher = Dispatchers.Unconfined

}

class DefaultDispatcherProvider : DispatcherProvider

//creates a content element for testing, can add elements to constructor with defaults
fun createContentElement(id: String, type: String = "type") = ArcXPContentElement(
    _id = id,
    additional_properties = null,
    created_date = null,
    display_date = null,
    first_publish_date = null,
    last_updated_date = null,
    owner = null,
    publish_date = null,
    publishing = null,
    revision = null,
    type = type,
    version = null,
    website = null,
    address = null,
    content_elements = null,
    caption = null,
    credits = null,
    geo = null,
    height = null,
    width = null,
    licensable = null,
    newKeywords = null,
    referent_properties = null,
    selectedGalleries = null,
    subtitle = null,
    taxonomy = null,
    url = null,
    copyright = null,
    description = null,
    headlines = null,
    language = null,
    location = null,
    promoItem = null,
    video_type = null,
    canonical_url = null,
    subtype = null,
    content = null,
    embed_html = null,
    subheadlines = null,
    streams = null,
    duration = null
)

fun createStoryElement(
    _id: String?,
    type: String,
    version: String? = null,
    alignment: String? = null,

    // dates
    created_date: Date? = null,
    last_updated_date: Date? = null,
    publish_date: Date? = null,
    first_publish_date: Date? = null,
    display_date: Date? = null,

    // location / language
    geo: Geo? = null,
    language: String? = null,
    location: String? = null,
    address: Address? = null,

    content_elements: List<StoryElement>? = null,

    related_content: Map<String, *>? = null,
    publishing: ArcXPStory.Publishing? = null,
    revision: Revision? = null,
    website: String? = null,
    websites: Map<String, SiteInfo>? = null,
    website_url: String? = null,
    short_url: String? = null,
    channels: List<String>? = null,
    owner: Owner? = null,
    credits: Credits? = null,
    vanity_credits: Credits? = null,
    editor_note: String? = null,
    taxonomy: Taxonomy? = null,
    copyright: String? = null,
    description: Description? = null,
    headlines: Headline? = null,
    subheadlines: Subheadlines? = null,
    label: Label? = null,
    @Json(name = "promo_items") promoItem: PromoItem? = null,
    content: String? = null,
    canonical_url: String? = null,
    canonical_website: String? = null,
    source: Source? = null,
    subtype: String? = null,
    planning: Planning? = null,
    pitches: Pitches? = null,
    syndication: Syndication? = null,
    distributor: Distributor? = null,
    tracking: Any? = null,
    comments: Comments? = null,
    slug: String? = null,
    content_restrictions: ContentRestrictions? = null,
    content_aliases: List<String>? = null,
    corrections: List<Correction>? = null,
    rendering_guides: List<RenderingGuide>? = null,
    status: String? = null,
    workFlow: WorkFlow? = null,
    additional_properties: Map<String, *>? = null,
    duration: Long? = 0L
) = ArcXPStory(_id, type, version, alignment, created_date, last_updated_date, publish_date, first_publish_date, display_date, geo, language, location, address, content_elements, related_content, publishing, revision, website, websites, website_url, short_url, channels, owner, credits, vanity_credits, editor_note, taxonomy, copyright, description, headlines, subheadlines, label, promoItem, content, canonical_url, canonical_website, source, subtype, planning, pitches, syndication, distributor, tracking, comments, slug, content_restrictions, content_aliases, corrections, rendering_guides, status, workFlow, additional_properties, duration)
