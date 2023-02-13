package com.arcxp.content.util

import com.arcxp.content.models.*
import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.ToJson
import com.squareup.moshi.adapters.PolymorphicJsonAdapterFactory
import com.squareup.moshi.adapters.Rfc3339DateJsonAdapter
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.util.*

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
        .addLast(KotlinJsonAdapterFactory())
        .build()

    @FromJson
    fun <T> fromJson(obj: String, classT: Class<T>): T? {
        val jsonAdapter: JsonAdapter<T> = moshi.adapter(classT)
        return jsonAdapter.fromJson(obj)
    }

    @ToJson
    inline fun <reified T> toJson(obj: T): String? {
        val jsonAdapter: JsonAdapter<T> = moshi.adapter(T::class.java).serializeNulls()
        return jsonAdapter.toJson(obj)
    }
}