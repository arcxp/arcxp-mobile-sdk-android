package com.arcxp.commons.util

import com.arcxp.ArcXPMobileSDK
import com.arcxp.ArcXPMobileSDK.application
import com.arcxp.commons.throwables.ArcXPSDKErrorType
import com.arcxp.content.models.Image
import com.arcxp.sdk.R
import com.google.gson.JsonParser
import java.io.DataOutputStream
import java.io.OutputStream
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object Utils {
    internal fun determineExpiresAt(expiresAt: String): Date {
        //if this value is null, we will default to the "expires" header value
        val timeUntilUpdateMinutes = ArcXPMobileSDK.contentConfig().cacheTimeUntilUpdateMinutes
        return if (timeUntilUpdateMinutes != null) {
            val calendar = currentCalendar()
            calendar.add(Calendar.MINUTE, timeUntilUpdateMinutes)
            calendar.time
        } else {
            val sdf = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US)
            sdf.parse(expiresAt)!!
        }
    }

    fun parseJsonArray(jsonArrayString: String): List<String> {
        val jsonArray = JsonParser.parseString(jsonArrayString).asJsonArray
        return jsonArray.map { it.toString() }
    }

    private const val thumbnailResizeUrlKey = "thumbnailResizeUrl"
    private const val resizeUrlKey = "resizeUrl"

    val formatter = SimpleDateFormat("MMM dd, yyyy", Locale.US)

    fun Image.fallback() =
        createFullImageUrl((this.additional_properties?.get(thumbnailResizeUrlKey) as String))

    internal fun createFullImageUrl(url: String): String {
        return "${ArcXPMobileSDK.baseUrl}$url"
    }

    internal fun currentTime(): Date = Calendar.getInstance().time

    internal fun currentCalendar() = Calendar.getInstance()

    internal fun currentTimeInMillis() = Calendar.getInstance().timeInMillis

    internal fun createOutputStream(outputStream: OutputStream) = DataOutputStream(outputStream)

    internal fun createURL(spec: String) = URL(spec)

    internal fun createURLandReadText(spec: String) = URL(spec).readText()

    internal fun createDate(date: Long? = null) = if (date != null) Date(date) else Date()
    internal fun createTimeStamp(date: Long? = null) = if (date != null) Date(date).time.toString() else Date().time.toString()

    enum class AnsTypes(val type: String) {
        VIDEO("video"),
        GALLERY("gallery"),
        STORY("story"),
        LINK("interstitial_link"),
        IMAGE("image"),
        TEXT("text");
    }

    internal fun createFailure(
        error: String? = null,
        type: ArcXPSDKErrorType = ArcXPSDKErrorType.SERVER_ERROR,
        value: Any? = null
    ) = Failure(DependencyFactory.createArcXPException(message = error, type = type, value = value))

    internal fun createNavFailure(message: String?, value: Any? = null) = createFailure(
        error = application().getString(
            R.string.failed_to_load_navigation,
            message ?: ""
        ), value = value ?: message
    )

    internal fun createSearchFailure(message: String?, searchTerm: String, value: Any? = null) =
        createFailure(
            error = application().getString(
                R.string.search_failure_message,
                searchTerm,
                message ?: ""
            ), value = value ?: message
        )
}