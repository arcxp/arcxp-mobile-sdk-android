package com.arcxp.commons.util

import android.app.Application
import android.os.Build
import com.arcxp.commons.models.ConnectivityState
import com.arcxp.commons.models.ConnectivityType
import com.arcxp.commons.models.DeviceOrientation

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