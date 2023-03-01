package com.arcxp.commons.models

import androidx.annotation.Keep
import com.arcxp.commons.util.MoshiController.toJson
import com.google.gson.annotations.SerializedName
import com.squareup.moshi.Json
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
data class ArcxpAnalytics(
    @Json(name = "event") val event: ArcxpEventFields,
    @Json(name = "time") val time: Long,
    @Json(name = "source") val source: String,
    @Json(name = "sourcetype") val sourcetype: String,
    @Json(name = "index") val index: String
)

@Keep
data class ArcxpEventFields(
    @Json(name = "event") val event: String,
    @Json(name = "uuid") val deviceUUID: String,
    @Json(name = "sdk_name") val sdkName: String,
    @Json(name = "organization") val org: String,
    @Json(name = "site") val site: String,
    @Json(name = "environment") val environment: String,
    @Json(name = "region") val locale: String,
    @Json(name = "platform") val platform: String,
    @Json(name = "os_version") val platformVersion: String,
    @Json(name = "device_model")val deviceModel: String,
    @Json(name = "connectivity") val connectivityState: String,
    @Json(name = "connectivity_type") val connectivityType: String,
    @Json(name = "device_orientation") val orientation: String
)
@Keep
/**
 * analytics event types
 */
enum class EventType(val value: String) {
    INSTALL("install"), PING("daily_ping")
}

@Keep
enum class DeviceOrientation(val value: Int, val text: String) {
    PORTRAIT(1, "portrait"), LANDSCAPE(2, "landscape");

    companion object {
        fun from(findValue: Int): DeviceOrientation = DeviceOrientation.values().first { it.value == findValue }
    }
}

@Keep
enum class ConnectivityState(val value:String) {
    OFFLINE("Offline"), ONLINE("Online")
}

@Keep
enum class ConnectivityType(val value:String) {
    WIFI("Wifi"), CELL("Cellular")
}

@Keep
enum class SdkName(val value: String) {
    COMMERCE("commerce"), VIDEO("video"), CONTENT("content"), SINGLE("single")
}
