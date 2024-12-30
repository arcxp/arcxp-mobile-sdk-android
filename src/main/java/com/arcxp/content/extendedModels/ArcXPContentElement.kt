package com.arcxp.content.extendedModels

import androidx.annotation.Keep
import com.arcxp.ArcXPMobileSDK.imageUtils
import com.arcxp.commons.util.Utils
import com.arcxp.commons.util.Utils.formatter
import com.arcxp.content.models.AdditionalProperties
import com.arcxp.content.models.Address
import com.arcxp.content.models.Credits
import com.arcxp.content.models.Description
import com.arcxp.content.models.Geo
import com.arcxp.content.models.Headline
import com.arcxp.content.models.Owner
import com.arcxp.content.models.PromoItem
import com.arcxp.content.models.Publishing
import com.arcxp.content.models.ReferentProperties
import com.arcxp.content.models.Revision
import com.arcxp.content.models.Streams
import com.arcxp.content.models.Subheadlines
import com.arcxp.content.models.Taxonomy
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.util.Date

/**
 * ArcXPContentElement represents a content element within the ArcXP Commerce module.
 * It encapsulates various properties related to the content, such as metadata, publishing information, and media details.
 *
 * The class defines the following properties:
 * - Metadata: additional_properties, created_date, display_date, first_publish_date, last_updated_date, owner, publish_date, revision, type, version, _id, website
 * - Location/Language: address, geo, language, location
 * - Content: content_elements, caption, credits, newKeywords, referent_properties, selectedGalleries, subtitle, taxonomy, url, description, headlines, subheadlines, content, embed_html
 * - Media: promoItem, video_type, canonical_url, subtype, streams, duration, auth
 *
 * Usage:
 * - Create an instance of ArcXPContentElement with the necessary properties.
 * - Use the provided methods to interact with the content element.
 *
 * Example:
 *
 * val contentElement = ArcXPContentElement(
 *     type = "article",
 *     _id = "12345",
 *     publish_date = Date(),
 *     headlines = Headline(basic = "Sample Headline")
 * )
 * val authorName = contentElement.author()
 * val title = contentElement.title()
 * val description = contentElement.description()
 *
 * Note: Ensure that the properties are properly configured before using ArcXPContentElement.
 *
 * @property additional_properties Additional properties related to the content element.
 * @property created_date The date when the content element was created.
 * @property display_date The date when the content element is displayed.
 * @property first_publish_date The date when the content element was first published.
 * @property last_updated_date The date when the content element was last updated.
 * @property owner The owner of the content element.
 * @property publish_date The date when the content element was published.
 * @property publishing Publishing information related to the content element.
 * @property revision Revision information related to the content element.
 * @property type The type of the content element.
 * @property version The version of the content element.
 * @property _id The unique identifier of the content element.
 * @property website The website associated with the content element.
 * @property address The address associated with the content element.
 * @property content_elements A list of nested content elements.
 * @property caption The caption of the content element.
 * @property credits The credits associated with the content element.
 * @property geo The geographical information related to the content element.
 * @property height The height of the content element.
 * @property width The width of the content element.
 * @property licensable Indicates if the content element is licensable.
 * @property newKeywords New keywords associated with the content element.
 * @property referent_properties Referent properties related to the content element.
 * @property selectedGalleries A list of selected galleries associated with the content element.
 * @property subtitle The subtitle of the content element.
 * @property taxonomy The taxonomy information related to the content element.
 * @property url The URL of the content element.
 * @property copyright The copyright information related to the content element.
 * @property description The description of the content element.
 * @property headlines The headlines of the content element.
 * @property language The language of the content element.
 * @property location The location associated with the content element.
 * @property promoItem The promotional item associated with the content element.
 * @property video_type The type of video associated with the content element.
 * @property canonical_url The canonical URL of the content element.
 * @property subtype The subtype of the content element.
 * @property content The content of the content element.
 * @property embed_html The embedded HTML content of the content element.
 * @property subheadlines The subheadlines of the content element.
 * @property streams A list of streams associated with the content element.
 * @property duration The duration of the content element.
 * @property auth Authentication information related to the content element.
 */
@Keep
@JsonClass(generateAdapter = true)
data class ArcXPContentElement(
    val additional_properties: AdditionalProperties? = null,
    val created_date: String? = null,
    val display_date: String? = null,
    val first_publish_date: String? = null,
    val last_updated_date: String? = null,
    val owner: Owner? = null,
    val publish_date: Date? = null,
    val publishing: Publishing? = null,
    val revision: Revision? = null,
    val type: String,
    val version: String? = null,
    val _id: String,
    val website: String? = null,
    val address: Address? = null,
    val content_elements: List<ArcXPContentElement>? = null,
    val caption: String? = null,
    val credits: Credits? = null,
    val geo: Geo? = null,
    val height: Int? = null,
    val width: Int? = null,
    val licensable: Boolean? = null,
    val newKeywords: String? = null,
    val referent_properties: ReferentProperties? = null,
    val selectedGalleries: List<String>? = null,
    val subtitle: String? = null,
    val taxonomy: Taxonomy? = null,
    val url: String? = null,
    val copyright: String? = null,
    val description: Description? = null,
    val headlines: Headline? = null,
    val language: String? = null,
    val location: String? = null,
    @Json(name = "promo_items") val promoItem: PromoItem? = null,
    val video_type: String? = null,
    val canonical_url: String? = null,
    val subtype: String? = null,
    val content: String? = null,
    val embed_html: String? = null,
    val subheadlines: Subheadlines? = null,
    val streams: List<Streams>? = null,
    val duration: Long? = null,
    val auth: Map<String, String>? = null
)

fun ArcXPContentElement.author(): String {
    if (this.credits?.by?.isNotEmpty() == true) {
        return this.credits.by.get(0).name ?: ""
    }
    return ""
}

fun ArcXPContentElement.title() = this.headlines?.basic ?: ""

fun ArcXPContentElement.description() = this.description?.basic ?: ""

fun ArcXPContentElement.date() = this.publish_date?.let { formatter.format(it) } ?: ""

fun ArcXPContentElement.thumbnail() = this.promoItem?.let { promoItem ->
        imageUtils().thumbnail(promoItem)
    } ?: ""

fun ArcXPContentElement.fallback() = this.promoItem?.let { promoItem ->
        imageUtils().fallback(promoItem)
    } ?: ""

fun ArcXPContentElement.imageUrl(): String = this.promoItem?.basic?.let { promoItem ->
        imageUtils().imageUrl(promoItem)
    } ?: ""

fun ArcXPContentElement.isVideo() = type == Utils.AnsTypes.VIDEO.type