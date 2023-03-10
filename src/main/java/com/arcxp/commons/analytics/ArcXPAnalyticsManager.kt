package com.arcxp.commons.analytics

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.annotation.VisibleForTesting
import com.arcxp.commons.models.ArcxpAnalytics
import com.arcxp.commons.models.ArcxpEventFields
import com.arcxp.commons.models.EventType
import com.arcxp.commons.models.SdkName
import com.arcxp.commons.retrofit.AnalyticsController
import com.arcxp.commons.util.AnalyticsUtil
import com.arcxp.commons.util.BuildVersionProvider
import com.arcxp.commons.util.ConnectionUtil
import com.arcxp.commons.util.Constants
import com.arcxp.commons.util.DependencyFactory.createIOScope
import com.arcxp.commons.util.MoshiController.fromJsonList
import com.arcxp.commons.util.MoshiController.toJson
import com.arcxp.video.util.TAG
import kotlinx.coroutines.launch
import java.util.*


/**
 * @suppress
 * This class is responsible for analytics reporting
 *
 * @constructor used internally only
 * @param application [Application] Application context
 * @param organization [String] Org to use in non-prod mode
 * @param site [String] currently unused
 * @param environment [String] Env to use in non-prod mode
 * @property buildVersionProvider [BuildVersionProvider] provides build version information (internal)
 *
 */
class ArcXPAnalyticsManager(
    private val application: Application,
    private val organization: String,
    private val site: String,
    private val environment: String,
    private val sdk_name: SdkName,
    private val sdk_version: String,
    private val buildVersionProvider: BuildVersionProvider,
    private val analyticsUtil: AnalyticsUtil
) {
    private val mIOScope = createIOScope()

    private var shared: SharedPreferences =
        application.getSharedPreferences(Constants.ANALYTICS, Context.MODE_PRIVATE)
    private val sharedEdit = shared.edit()
    private var deviceID: String
    private var locale = analyticsUtil.getCurrentLocale()
    private val deviceModel = buildVersionProvider.manufacturer() + buildVersionProvider.model()
    private val debugMode = buildVersionProvider.debug()
    private val platformVersion = buildVersionProvider.sdkInt()
    private var installed: Boolean = false
    private val analyticsService = AnalyticsController.getAnalyticsService(application = application)
    private val packageName = application.applicationContext.packageName

    init {
        val deviceID = shared.getString(Constants.DEVICE_ID, null)
        if (!deviceID.isNullOrEmpty()) {
            this.deviceID = deviceID
        } else {
            this.deviceID = UUID.randomUUID().toString()
            sharedEdit.putString(Constants.DEVICE_ID, this.deviceID).apply()
        }
        installed = shared.getBoolean(sdk_name.value, false)

        if (!installed) {
            log(EventType.INSTALL)
            sharedEdit.putBoolean(sdk_name.value, true).apply()
        } else {
            log(EventType.PING)
        }
    }

    /**
     * method to send analytics event
     *
     * @param event [EventType] event to be sent
     */
    fun log(event: EventType) {
        try {

            //Send ping on install or
            //Check to see if we have sent a ping in the last 24h
            if (event == EventType.INSTALL || checkLastPing()) {

                //create current analytics
                val currentEvent = buildAnalytics(event)

                //create list of analytics including anything that was stored
                //while offline
                val events: List<ArcxpAnalytics> = buildFullAnalytics(currentEvent)

                //clear out any stored analytics
                sharedEdit.remove(Constants.PENDING_ANALYTICS).apply()

                if (ConnectionUtil.isInternetAvailable(application.applicationContext)) {

                    mIOScope.launch {
                        analyticsService.postAnalytics(events).apply {
                            when {
                                isSuccessful -> {
                                    //do nothing
                                }
                                else -> {
                                    //something went wrong, store the analytics
                                    sharedEdit
                                        .putString(
                                            Constants.PENDING_ANALYTICS,
                                            toJson(events).toString()
                                        )
                                        .apply()
                                }
                            }
                        }
                    }
                } else {
                    //offline so store it all back into shared preferences
                    sharedEdit
                        .putString(Constants.PENDING_ANALYTICS, toJson(events).toString())
                        .apply()
                }
            }
        } catch (e: Exception) {
            //something went wrong so punt.
        }
    }

    fun checkLastPing(): Boolean {
        //check to see if we we have already sent out a ping today
        val lastPing = shared.getLong(Constants.LAST_PING_TIME, 0)
        //We have pinged before so if it has happened today then don't
        //send analytics.  If the event is INIT then we won't have a
        //lastPing value
        val currentTime = Calendar.getInstance().timeInMillis
        if (lastPing > 0) {
            if (currentTime - lastPing <= 86400000) {
                //is within 24 hours so exit
                return false
            }
            sharedEdit.putLong(Constants.LAST_PING_TIME, currentTime)
        } else {
            sharedEdit.putLong(Constants.LAST_PING_TIME, currentTime)
        }
        return true
    }

    fun buildAnalytics(event: EventType) =

        //build current analytics
        ArcxpAnalytics(
            event = ArcxpEventFields(
                event = event.value,
                deviceUUID = deviceID,
                sdkName = sdk_name.name,
                sdkVersion = sdk_version,
                org = organization,
                site = site,
                environment = environment,
                locale = locale,
                platform = "android",
                platformVersion = platformVersion.toString(),
                deviceModel = deviceModel,
                connectivityState = analyticsUtil.deviceConnectionState(),
                connectivityType = analyticsUtil.deviceConnectionType(),
                orientation = analyticsUtil.screenOrientation(),
                packageName = packageName
            ),
            time = Calendar.getInstance().time.time,
            source = if (debugMode) "arcxp-mobile-dev" else "arcxp-mobile-prod",
            sourcetype = "arcxp-mobile",
            index = "arcxp-mobile"
        )

    fun buildFullAnalytics(event: ArcxpAnalytics): List<ArcxpAnalytics> {
        //create a list for the analytics
        val analyticsEvents = mutableListOf(event)

        //add the offline stuff if there are any
        try {
            val offlineAnalytics = shared.getString(Constants.PENDING_ANALYTICS, null)
            if (offlineAnalytics != null) {
                val offlineEvents: List<ArcxpAnalytics>? =
                    fromJsonList(offlineAnalytics, ArcxpAnalytics::class.java)
                analyticsEvents.addAll(offlineEvents as List<ArcxpAnalytics>)
            }
        } catch (e: Exception) {
            //Something went wrong.  Nothing we can do so punt.
            Log.e(TAG, e.message ?: "unknown error")
        }

        return analyticsEvents
    }

    @VisibleForTesting
    fun getDeviceId(): String? {
        return deviceID
    }
}
