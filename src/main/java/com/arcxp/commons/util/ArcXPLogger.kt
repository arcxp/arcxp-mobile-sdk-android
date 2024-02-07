package com.arcxp.commons.util

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import androidx.annotation.Keep
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.arcxp.commons.util.DependencyFactory.createBuildVersionProvider
import com.arcxp.sdk.R
import java.text.SimpleDateFormat
import java.util.*

/**
* @suppress
 **/
class ArcXPLogger(
    application: Application,
    private val organization: String,
    private val environment: String,
    private val site: String,
    private val buildVersionProvider: BuildVersionProvider = createBuildVersionProvider()
) {

    @Keep
    enum class LOG_LEVEL {
        DEBUG, INFO, ERROR
    }

    private val _logEntry = MutableLiveData<ArcXPLogEntry>()
    val logEntry: LiveData<ArcXPLogEntry>
        get() = _logEntry

    private var dateFormat: SimpleDateFormat

    private var locale: Locale
    private var sdkVersion: String
    private var osInfo: String
    private var deviceInfo: String
    private var connectivityManager: ConnectivityManager

    private var breadcrumbs: Deque<String> = LinkedList()

    init {
        sdkVersion = getSDKVersion(application.applicationContext)
        locale = getLocale(application)
        osInfo = getOsInfo()
        deviceInfo = getDeviceInfo()
        connectivityManager =
            application.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        dateFormat = SimpleDateFormat("MM/dd/yyyy h:mm a z", Locale.US)
        dateFormat.timeZone = TimeZone.getTimeZone("UTC")
    }

    fun error(message: String, data: Any? = null, exception: Throwable? = null) {
        val log = ArcXPLogEntry(
            LOG_LEVEL.ERROR,
            organization,
            environment,
            site = site,
            getTimestamp(),
            sdkVersion,
            deviceInfo,
            osInfo,
            locale,
            isConnected(),
            message,
            exception,
            data,
            breadcrumbs
        )
        _logEntry.postValue(log)

    }

    fun e(message: String, data: Any? = null, exception: Throwable? = null) {
        this.error(message, data, exception)
    }

    fun debug(message: String, data: Any? = null) {
        val log = ArcXPLogEntry(
            LOG_LEVEL.DEBUG,
            organization,
            environment,
            site = site,
            getTimestamp(),
            sdkVersion,
            deviceInfo,
            osInfo,
            locale,
            isConnected(),
            message,
            null,
            data,
            breadcrumbs
        )
        _logEntry.postValue(log)
    }

    fun d(message: String, data: Any? = null) {
        debug(message, data)
    }

    fun info(message: String, data: Any? = null) {
        val log = ArcXPLogEntry(
            LOG_LEVEL.INFO,
            organization,
            environment,
            site = site,
            getTimestamp(),
            sdkVersion,
            deviceInfo,
            osInfo,
            locale,
            isConnected(),
            message,
            null,
            data,
            breadcrumbs
        )
        _logEntry.postValue(log)
    }

    fun i(message: String, data: Any? = null) {
        info(message, data)
    }

    fun internal(
        level: LOG_LEVEL,
        message: String,
        data: Any? = null,
        breadcrumb: Any?,
        exception: Throwable? = null
    ) {
        val log = ArcXPLogEntry(
            level = level,
            organization = organization,
            environment = environment,
            site = site,
            timestamp = getTimestamp(),
            sdkVersion = sdkVersion,
            deviceInfo = deviceInfo,
            osInfo = osInfo,
            locale = locale,
            connected = isConnected(),
            message = message,
            exception = exception,
            data = data,
            breadcrumb = breadcrumb
        )
        //do not broadcast this log message.
        logInternal(log)

    }

    private fun logInternal(log: ArcXPLogEntry) {
        //send to internal logging mechanism (splunk, etc).
    }

    fun getDeviceInfo(): String {
        val manufacturer = buildVersionProvider.manufacturer()
        val model = buildVersionProvider.model()
        return "$manufacturer $model"
    }

    private fun isConnected(): Boolean {
        return connectivityManager.isDefaultNetworkActive ?: true
    }

    private fun getTimestamp(): String {
        return dateFormat.format(Date())
    }

    fun getOsInfo(): String {
        return "Android ${buildVersionProvider.sdkInt()}"
    }

    fun getSDKVersion(context: Context): String {
        return context.getString(R.string.sdk_version)
    }

    @SuppressLint("NewApi")
    fun getLocale(application: Application): Locale {
        return if (buildVersionProvider.sdkInt() <= 23) {
            application.resources.configuration.locale
        } else {
            application.resources.configuration.locales.get(0)
        }
    }

    public fun addBreadcrumb(s: String) {
        if (breadcrumbs.size == 5) {
            breadcrumbs.removeLast()
        }
        breadcrumbs.addFirst(s)
    }

    public fun clearBreadcrumbs() {
        breadcrumbs.clear()
    }

    fun getBreadcrumbs(): Deque<String> {
        return breadcrumbs
    }

    fun subscribe(owner: LifecycleOwner, observer: Observer<ArcXPLogEntry>) {
        _logEntry.observe(owner, observer)
    }
}

/**
 * @suppress
 **/
data class ArcXPLogEntry(
    val level: ArcXPLogger.LOG_LEVEL,
    val organization: String,
    val environment: String,
    val site: String,
    val timestamp: String,
    val sdkVersion: String,
    val deviceInfo: String,
    val osInfo: String,
    val locale: Locale,
    val connected: Boolean,
    val message: String,
    val exception: Throwable?,
    val data: Any?,
    val breadcrumb: Any?
)