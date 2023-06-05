package com.arcxp.commons.util

import com.arcxp.ArcXPMobileSDK
import com.arcxp.content.models.Image
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

    private const val thumbnailResizeUrlKey = "thumbnailResizeUrl"
    private const val resizeUrlKey = "resizeUrl"

    val formatter = SimpleDateFormat("MMM dd, yyyy", Locale.US)

    internal fun Image.fallback() =
        createFullImageUrl((this.additional_properties?.get(thumbnailResizeUrlKey) as String))

    internal fun createFullImageUrl(url: String): String {
        return "${ArcXPMobileSDK.baseUrl}$url"
    }

    internal fun currentTime(): Date = Calendar.getInstance().time

    internal fun currentCalendar() = Calendar.getInstance()

    internal fun currentTimeInMillis() = Calendar.getInstance().timeInMillis

    internal fun createOutputStream(outputStream: OutputStream) = DataOutputStream(outputStream)

    internal fun createURL(spec: String) = URL(spec)

    internal fun createDate(date: Long? = null) = if (date != null) Date(date) else Date()
}