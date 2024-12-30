package com.arcxp.commons.util

import com.arcxp.ArcXPMobileSDK
import com.arcxp.ArcXPMobileSDK.application
import com.arcxp.commons.throwables.ArcXPSDKErrorType
import com.arcxp.content.models.Image
import com.arcxp.sdk.R
import com.google.gson.JsonArray
import com.google.gson.JsonParser
import java.io.DataOutputStream
import java.io.OutputStream
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Utils is a utility object that provides various helper functions and constants used throughout the ArcXP Commerce module.
 * It includes methods for date and time manipulation, JSON parsing and construction, URL creation, and error handling.
 *
 * The object defines the following operations:
 * - Determine expiration dates based on cache configuration
 * - Parse and construct JSON arrays
 * - Create full image URLs
 * - Retrieve the current time and date
 * - Create and manage URLs and output streams
 * - Handle different types of failures and errors
 *
 * Usage:
 * - Use the provided methods to perform common utility operations.
 *
 * Example:
 *
 * val expiresAt = Utils.determineExpiresAt("Wed, 21 Oct 2020 07:28:00 GMT")
 * val jsonArray = Utils.parseJsonArray("[{\"key\":\"value\"}]")
 * val fullImageUrl = Utils.createFullImageUrl("/path/to/image")
 *
 * Note: Ensure that the application context and other parameters are properly configured before using Utils.
 *
 * @method determineExpiresAt Determine the expiration date based on cache configuration.
 * @method parseJsonArray Parse a JSON array string into a list of JSON strings.
 * @method constructJsonArray Construct a JSON array string from a list of JSON strings.
 * @method createFullImageUrl Create a full image URL based on the base URL and the provided path.
 * @method currentTime Retrieve the current time.
 * @method currentCalendar Retrieve the current calendar instance.
 * @method currentTimeInMillis Retrieve the current time in milliseconds.
 * @method createOutputStream Create a DataOutputStream from an OutputStream.
 * @method createURL Create a URL from a string specification.
 * @method createURLandReadText Create a URL from a string specification and read its text content.
 * @method createDate Create a Date instance from a timestamp or the current time.
 * @method createTimeStamp Create a timestamp string from a date or the current time.
 * @method createFailure Create a Failure instance with a specified message, error type, and value.
 * @method createNavFailure Create a navigation failure with a specified message and value.
 * @method createSearchFailure Create a search failure with a specified message, search term, and value.
 */
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

    /** given a json string that is a JSON object with nested children
     * they are all json themselves
     * so this will parse them out as their own string and output the result
     * we use this to cache the json from collection result
     */
    fun parseJsonArray(jsonArrayString: String): List<String> {
        val jsonArray = JsonParser.parseString(jsonArrayString).asJsonArray
        return jsonArray.map { it.toString() }
    }

    /**
     * this is to reconstruct the original collection json, basically undo parseJsonArray
     * we use this to reconstruct a collection result from database json
     */
    fun constructJsonArray(jsonStrings: List<String>): String {
        val jsonArray = JsonArray()
        jsonStrings.forEach {
            // Parse each string as a JSON element and add it to the JSON array
            jsonArray.add(JsonParser.parseString(it))
        }
        return jsonArray.toString()
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
        message: String? = null,
        type: ArcXPSDKErrorType = ArcXPSDKErrorType.SERVER_ERROR,
        value: Any? = null
    ) = Failure(DependencyFactory.createArcXPException(message = message, type = type, value = value))

    internal fun createNavFailure(message: String?, value: Any? = null) = createFailure(
        message = application().getString(
            R.string.failed_to_load_navigation,
            message ?: ""
        ), value = value
    )

    internal fun createSearchFailure(message: String?, searchTerm: String, value: Any? = null) =
        createFailure(
            type = ArcXPSDKErrorType.SEARCH_ERROR,
            message = application().getString(
                R.string.search_failure_message,
                searchTerm,
                message ?: ""
            ), value = value
        )
}