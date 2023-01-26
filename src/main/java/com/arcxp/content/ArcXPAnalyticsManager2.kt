package com.arcxp.content

import android.app.Application
import android.content.ContentValues
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.arcxp.content.models.ArcxpAnalytics
import com.arcxp.content.models.EventType
import com.arcxp.content.util.BuildVersionProvider
import com.arcxp.content.util.Constants
import com.arcxp.content.util.MoshiController.fromJson
import com.arcxp.content.util.MoshiController.toJson
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
class ArcXPAnalyticsManager2 internal constructor(application: Application, private val organization: String, private val site: String, private val environment: String, private val buildVersionProvider: BuildVersionProvider) {

    private val shared: SharedPreferences = application.getSharedPreferences(Constants.ANALYTICS, Context.MODE_PRIVATE)
    private val sharedEdit = shared.edit()
    private val deviceModel: String = buildVersionProvider.model()
    private var pendingAnalytics = mutableListOf<ArcxpAnalytics>()
    private var deviceID: String? =  null

    /**
     * method to send analytics event
     *
     * @param event [EventType] event to be sent
     */
    fun sendAnalytics(event: EventType) {
        if (deviceID == null) setup()
        val currentTime: Date = Calendar.getInstance().time
        val analyticEvent = ArcxpAnalytics(event, deviceModel, deviceID, currentTime, "${organization}-${site}-${environment}")
        checkOfflineEvents()
        pendingAnalytics.add(analyticEvent)
    }

    private fun checkOfflineEvents() {
        val offlineAnalytics = shared.getStringSet(Constants.PENDING_ANALYTICS, null)
        if (offlineAnalytics?.isNotEmpty() == true) {
            offlineAnalytics.iterator().forEach {
                fromJson(it, ArcxpAnalytics::class.java)?.let { analytics -> pendingAnalytics.add(analytics) }
            }
        }
    }

    /**
     * run upon receiving successful analytics response
     */
    //TODO make private when we hook up to real analytics call
    fun makesSuccessfulCall() {
        Log.d(ContentValues.TAG, pendingAnalytics.toString())
        sharedEdit.remove(Constants.PENDING_ANALYTICS).apply()
        pendingAnalytics.clear()

    }

    /**
     * run upon receiving failed analytics response
     */
    //TODO make private when we hook up to real analytics call
    fun failedCall() {
        val offline = mutableSetOf<String>()
        pendingAnalytics.iterator().forEach {
            toJson(it)?.let { json -> offline.add(json) }
        }
        sharedEdit.putStringSet(Constants.PENDING_ANALYTICS, offline).apply()
    }

    /**
     * upon first time run on device, will set a new universally unique identifier [UUID] that persists
     */
    private fun setup() {
        val deviceID = shared.getString(Constants.DEVICE_ID, null)
        if (!deviceID.isNullOrEmpty()) {
            this.deviceID = deviceID
        } else {
            sharedEdit.putString(Constants.DEVICE_ID, UUID.randomUUID().toString()).apply()
        }
    }
}
