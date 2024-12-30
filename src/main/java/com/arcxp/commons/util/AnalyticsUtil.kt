package com.arcxp.commons.util

import android.app.Application
import android.os.Build
import com.arcxp.commons.models.ConnectivityState
import com.arcxp.commons.models.ConnectivityType
import com.arcxp.commons.models.DeviceOrientation

/**
 * AnalyticsUtil is responsible for providing utility methods related to analytics within the ArcXP Commerce module.
 * It offers methods to retrieve the current locale, determine the device's connection type and state, and get the screen orientation.
 *
 * The class defines the following operations:
 * - Retrieve the current locale of the device
 * - Determine the device's connection type (WiFi or Cellular)
 * - Determine the device's connection state (Online or Offline)
 * - Retrieve the screen orientation of the device
 *
 * Usage:
 * - Create an instance of AnalyticsUtil with the application context.
 * - Use the provided methods to gather analytics-related information.
 *
 * Example:
 *
 * val analyticsUtil = AnalyticsUtil(application)
 * val locale = analyticsUtil.getCurrentLocale()
 * val connectionType = analyticsUtil.deviceConnectionType()
 * val connectionState = analyticsUtil.deviceConnectionState()
 * val orientation = analyticsUtil.screenOrientation()
 *
 * Note: Ensure that the application context is properly configured before using AnalyticsUtil.
 *
 * @method getCurrentLocale Retrieve the current locale of the device.
 * @method deviceConnectionType Determine the device's connection type.
 * @method deviceConnectionState Determine the device's connection state.
 * @method screenOrientation Retrieve the screen orientation of the device.
 */
class AnalyticsUtil(val application: Application) {

    public fun getCurrentLocale(): String {
        val locale =  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            application.resources.configuration.locales[0]
        } else {
            application.resources.configuration.locale
        }
        return "${locale.language}-${locale.country}"
    }

    public fun deviceConnectionType() : String {
        return if (ConnectionUtil.isOnWiFi(application.applicationContext))
            ConnectivityType.WIFI.value else ConnectivityType.CELL.value
    }

    public fun deviceConnectionState() : String {
        return if (ConnectionUtil.isInternetAvailable(application.applicationContext))
            ConnectivityState.ONLINE.value else ConnectivityState.OFFLINE.value
    }

    public fun screenOrientation() : String {
        return DeviceOrientation.from(application.applicationContext.resources.configuration.orientation).text
    }

}