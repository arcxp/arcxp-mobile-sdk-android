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
import com.arcxp.commons.service.AnalyticsService
import com.arcxp.commons.util.*
import rx.android.BuildConfig
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
class ArcXPAnalyticsManager (
    private val application: Application,
    private val organization: String,
    private val site: String,
    private val environment: String,
    private val sdk_name: SdkName,
    private val buildVersionProvider: BuildVersionProvider,
    private val analyticsUtil: AnalyticsUtil
) {
    private val mIOScope = ArcXPAnalytics.createeIOScope()

    private var shared: SharedPreferences = application.getSharedPreferences(Constants.ANALYTICS, Context.MODE_PRIVATE)
    private val sharedEdit = shared.edit()
    private var deviceID: String? = null
    private var locale = analyticsUtil.getCurrentLocale()
    private val deviceModel = buildVersionProvider.manufacturer() + buildVersionProvider.model()
    private val debugMode = BuildConfig.DEBUG
    private val platformVersion = buildVersionProvider.sdkInt()
    private var installed: Boolean = false
    private val analyticsService: AnalyticsService =
        AnalyticsController.getAnalyticsService()

    init {
        val deviceID = shared.getString(Constants.DEVICE_ID, null)
        if (!deviceID.isNullOrEmpty()) {
            this.deviceID = deviceID
        } else {
            sharedEdit.putString(Constants.DEVICE_ID, UUID.randomUUID().toString()).apply()
        }
        Log.e("TAG", "Device ID ${deviceID}")
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
    fun log(event: EventType) =
        try {

            val analyticsEvent = buildAnalytics(event).getJson()

            val eventsJson = createJson(analyticsEvent)

//            if (ConnectionUtil.isInternetAvailable(application.applicationContext)) {
//                mIOScope.launch {
//                    val response1 = analyticsService.postAnalytics(eventsJson)
//                    with(response1) {
//                        when {
//                            isSuccessful -> {
//                                sharedEdit.remove(Constants.PENDING_ANALYTICS).apply()
//                            }
//                            else -> {
//                                sharedEdit.putString(Constants.PENDING_ANALYTICS, eventsJson)
//                                    .apply()
//                            }
//                        }
//                    }
//                }
//            } else {
//                sharedEdit.putString(Constants.PENDING_ANALYTICS, eventsJson).apply()
//            }
        } catch (e: Exception) {
            Log.e("TAG", "Exception: ${e.localizedMessage}")
        }

    fun buildAnalytics(event: EventType) : ArcxpAnalytics =
        ArcxpAnalytics(
            Calendar.getInstance().time.time,
            "arcxp-mobile-dev", //if (buildVersionProvider.debug()) "arcxp-mobile-dev" else "arcxp_mobile-prod",
            "arcxp-mobile",
            "arcxp-mobile",
            ArcxpEventFields(
                event.value,
                getDeviceId()!!,
                sdk_name.name,
                organization,
                site,
                environment,
                locale,
                "android",
                platformVersion,
                deviceModel,
                analyticsUtil.deviceConnection(),
                analyticsUtil.screenOrientation()
            )
        )

    fun createJson(eventJson: String) : String {
        val offlineAnalytics = shared.getString(Constants.PENDING_ANALYTICS, null)
        if (offlineAnalytics != null) {
            if (offlineAnalytics.isNotEmpty()) {
                val analytics = StringBuilder()
                analytics.append(offlineAnalytics)
                analytics.append(eventJson)
                return analytics.toString()
            }
        }
        return eventJson
    }

    @VisibleForTesting
    fun getDeviceId() : String? {
        return deviceID
    }
}
