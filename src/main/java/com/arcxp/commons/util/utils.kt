package com.arcxp.commons.util

import com.arcxp.ArcXPMobileSDK
import com.arcxp.content.models.Image
import java.text.SimpleDateFormat
import java.util.*

internal fun determineExpiresAt(expiresAt: String): Date {
    //if this value is null, we will default to the "expires" header value
    val timeUntilUpdateMinutes = ArcXPMobileSDK.contentConfig().cacheTimeUntilUpdateMinutes
    return if (timeUntilUpdateMinutes != null) {
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        calendar.add(Calendar.MINUTE, timeUntilUpdateMinutes)
        calendar.time
    } else {
        val sdf = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US)
        sdf.parse(expiresAt)!!
    }
}

private const val thumbnailResizeUrlKey = "thumbnailResizeUrl"
private const val resizeUrlKey = "resizeUrl"

val formatter = SimpleDateFormat("MMM dd, yyyy", Locale.US)

fun Image.fallback() = createFullImageUrl((this.additional_properties?.get(thumbnailResizeUrlKey)as String))

fun createFullImageUrl(url: String): String {
    return "${ArcXPMobileSDK.baseUrl}$url"
}