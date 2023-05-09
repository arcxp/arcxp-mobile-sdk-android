package com.arcxp.content.models

import androidx.annotation.Keep
import com.squareup.moshi.JsonClass
import java.util.*

/**
 * This is data class sent per analytics event
 *
 * @property event analytics event type
 * @property deviceModel Device Model (from Build.Model)
 * @property deviceID Device ID (UUID set at install)
 * @property date timestamp
 * @property tenantID org, site
 */
@Keep
@JsonClass(generateAdapter = true)
data class ArcxpAnalytics(
    val event: EventType?,
    val deviceModel: String?,
    val deviceID: String?,
    val date: Date?,
    val tenantID: String?
)
@Keep
/**
 * analytics event types
 */
enum class EventType(val value: String) {
    STORY("story"), COLLECTION("collection"), GALLERY("gallery"), JSON("json"), SEARCH("search"), NAVIGATION("navigation"), VIDEO("video"), INITIALIZE("initialize")
}
