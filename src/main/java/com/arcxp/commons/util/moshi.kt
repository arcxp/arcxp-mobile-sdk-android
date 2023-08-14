package com.arcxp.commons.util

import com.arcxp.content.models.Code
import com.arcxp.content.models.Correction
import com.arcxp.content.models.CustomEmbed
import com.arcxp.content.models.Divider
import com.arcxp.content.models.ElementGroup
import com.arcxp.content.models.Endorsement
import com.arcxp.content.models.Gallery
import com.arcxp.content.models.Header
import com.arcxp.content.models.Image
import com.arcxp.content.models.InterstitialLink
import com.arcxp.content.models.LinkList
import com.arcxp.content.models.Links
import com.arcxp.content.models.NumericRating
import com.arcxp.content.models.OembedResponse
import com.arcxp.content.models.Quote
import com.arcxp.content.models.RawHTML
import com.arcxp.content.models.StoryElement
import com.arcxp.content.models.StoryList
import com.arcxp.content.models.StoryListElement
import com.arcxp.content.models.Table
import com.arcxp.content.models.Text
import com.arcxp.content.models.UnknownStoryListElement
import com.arcxp.content.models.UnknownWebsiteSection
import com.arcxp.content.models.Video
import com.arcxp.content.models.WebsiteSection
import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.Moshi
import com.squareup.moshi.ToJson
import com.squareup.moshi.Types
import com.squareup.moshi.adapters.PolymorphicJsonAdapterFactory
import com.squareup.moshi.adapters.Rfc3339DateJsonAdapter
import okio.Buffer
import org.json.JSONException
import org.json.JSONObject
import java.util.Date

object MoshiController {

    val moshi: Moshi = Moshi.Builder()
        .add(Date::class.java, Rfc3339DateJsonAdapter().nullSafe())
        .add(
            PolymorphicJsonAdapterFactory.of(WebsiteSection::class.java, "type")
                .withSubtype(WebsiteSection.Section::class.java, "section")
                .withSubtype(WebsiteSection.Reference::class.java, "reference")
                .withDefaultValue(UnknownWebsiteSection())
        )
        .add(
            PolymorphicJsonAdapterFactory.of(StoryListElement::class.java, "type")
                .withSubtype(StoryListElement.Text::class.java, "text")
                .withSubtype(StoryListElement.StoryListItem::class.java, "list")
                .withDefaultValue(UnknownStoryListElement())
        )
        .add(
            PolymorphicJsonAdapterFactory.of(Links::class.java, "type")
                .withSubtype(Links.Text::class.java, "text")
                .withSubtype(Links.InterstitialLink::class.java, "interstitial_link")
                .withDefaultValue(Links.UnknownLinks())
        )
        .add(
            PolymorphicJsonAdapterFactory.of(StoryElement::class.java, "type")
                .withSubtype(Gallery::class.java, "gallery")
                .withSubtype(Video::class.java, "video")
                .withSubtype(Image::class.java, "image")
                .withSubtype(Code::class.java, "code")
                .withSubtype(Correction::class.java, "correction")
                .withSubtype(CustomEmbed::class.java, "custom_embed")
                .withSubtype(Divider::class.java, "divider")
                .withSubtype(ElementGroup::class.java, "element_group")
                .withSubtype(Endorsement::class.java, "endorsement")
                .withSubtype(Header::class.java, "header")
                .withSubtype(InterstitialLink::class.java, "interstitial_link")
                .withSubtype(LinkList::class.java, "link_list")
                .withSubtype(StoryList::class.java, "list")
                .withSubtype(NumericRating::class.java, "numeric_rating")
                .withSubtype(Quote::class.java, "quote")
                .withSubtype(RawHTML::class.java, "raw_html")
                .withSubtype(Table::class.java, "table")
                .withSubtype(Text::class.java, "text")
                .withSubtype(OembedResponse::class.java, "oembed_response")
                .withDefaultValue(StoryElement.UnknownStoryElement())
        )
        .add(JSONObjectAdapter())
        .add(Date::class.java, Rfc3339DateJsonAdapter().nullSafe())
        .build()

    @FromJson
    fun <T> fromJson(obj: String, classT: Class<T>): T? {
        val jsonAdapter: JsonAdapter<T> = moshi.adapter(classT)
        return jsonAdapter.fromJson(obj)
    }

    fun <T> fromJsonList(obj: String, classT: Class<T>): List<T>? {
        return if (obj.isNotBlank()) {
            val type = Types.newParameterizedType(List::class.java, classT)
            val jsonAdapter: JsonAdapter<List<T>> = moshi.adapter(type)
            jsonAdapter.fromJson(obj)
        } else emptyList()
    }

    @ToJson
    inline fun <reified T> toJson(obj: T): String? {
        val jsonAdapter: JsonAdapter<T> = moshi.adapter(T::class.java).serializeNulls()
        return jsonAdapter.toJson(obj)
    }

    class JSONObjectAdapter {

        @FromJson
        fun fromJson(reader: JsonReader): JSONObject? {
            // Here we're expecting the JSON object, it is processed as Map<String, Any> by Moshi
            return (reader.readJsonValue() as? Map<String, Any>)?.let { data ->
                try {
                    JSONObject(data)
                } catch (e: JSONException) {
                    // Handle exception
                    return null
                }
            }
        }

        @ToJson
        fun toJson(writer: JsonWriter, value: JSONObject?) {
            value?.let { writer.value(Buffer().writeUtf8(value.toString())) }
        }
    }
}