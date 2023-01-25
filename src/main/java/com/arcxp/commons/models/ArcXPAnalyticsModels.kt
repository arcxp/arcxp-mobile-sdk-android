package com.arcxp.commons.models

import androidx.annotation.Keep
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
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
    @SerializedName("time") val timestamp: Long,
    @SerializedName("source") val source: String,
    @SerializedName("sourcetype") val sourcetype: String,
    @SerializedName("index") val index: String,
    @SerializedName("event") val event: ArcxpEventFields
)

{
    fun getJson(): String {
        return Gson().toJson(this)
    }
}

@Keep
data class ArcxpEventFields(
    @SerializedName("type") val event: String,
    @SerializedName("uuid") val deviceUUID: String,
    @SerializedName("sdk_name") val sdkName: String,
    @SerializedName("organization") val org: String,
    @SerializedName("site") val site: String,
    @SerializedName("environment") val environment: String,
    @SerializedName("region") val locale: String,
    @SerializedName("platform") val platform: String,
    @SerializedName("os_version") val platformVersion: String,
    @SerializedName("device_model")val deviceModel: String,
    @SerializedName("connectivity") val connectivityState: String,
    @SerializedName("device_orientation") val orientation: String
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
enum class SdkName(val value: String) {
    COMMERCE("commerce"), VIDEO("video"), CONTENT("content")
}
