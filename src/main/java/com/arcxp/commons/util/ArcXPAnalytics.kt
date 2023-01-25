package com.arcxp.commons.util

import android.app.Application
import com.arcxp.commons.analytics.ArcXPAnalyticsManager
import com.arcxp.commons.models.SdkName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

object ArcXPAnalytics {

    fun createArcXPAnalyticsManager(application: Application,
                                    organization: String,
                                    site: String,
                                    environment: String,
                                    sdk_name: SdkName,) : ArcXPAnalyticsManager {

        return ArcXPAnalyticsManager(
            application,
            organization,
            site,
            environment,
            sdk_name,
            createBuildVersionProvider(),
            createAnalyticsUtil(application))

    }

    fun createBuildVersionProvider() = BuildVersionProviderImpl()

    fun createAnalyticsUtil(application: Application) = AnalyticsUtil(application)

    fun createeIOScope() = CoroutineScope(Dispatchers.IO + SupervisorJob())
}