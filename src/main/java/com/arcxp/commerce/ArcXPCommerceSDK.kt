package com.arcxp.commerce

import android.app.Application
import android.content.Context
import androidx.annotation.Keep
import com.arcxp.commons.analytics.ArcXPAnalyticsManager
import com.arcxp.commons.models.SdkName
import com.arcxp.commons.util.ArcXPAnalytics
import com.arcxp.sdk.R

@Keep
object ArcXPCommerceSDK {

    private var arcxpCommerceConfig: ArcXPCommerceConfig? = null
    private var arcxpCommerceManager: ArcXPCommerceManager? = null
    private var analytics: ArcXPAnalyticsManager? = null

    private var initialized = false

    fun arcxpCommerceConfig() =
        if (arcxpCommerceConfig != null) {
            arcxpCommerceConfig!!
        } else {
            throw ArcXPCommerceException(
                type = ArcXPCommerceSDKErrorType.INIT_ERROR,
                message = "Failed Retrieving Config: SDK uninitialized, please run initialize method first"
            )
        }

    fun commerceManager() =
        if (initialized) {
            arcxpCommerceManager!!
        } else {
            throw ArcXPCommerceException(
                type = ArcXPCommerceSDKErrorType.INIT_ERROR,
                message = "Failed Initialization: SDK uninitialized, please run initialize method first"
            )
        }

    /**
     * If initialization has occurred,
     * this function returns the [ArcXPAnalyticsManager] created during initialization
     * @return [arcXPAnalyticsManager]
     */
    fun analytics(): ArcXPAnalyticsManager {
        if (!initialized) {
            throw ArcXPCommerceException(
                type = ArcXPCommerceSDKErrorType.INIT_ERROR,
                message = "Failed Initialization: SDK uninitialized, please run initialize method first"
            )
        }
        return analytics!!
    }

    fun initialize(application: Application, clientCachedData: Map<String, String>, config: ArcXPCommerceConfig) {
        if (!initialized) {
            initialized = true
            this.arcxpCommerceConfig = config
            this.analytics = ArcXPAnalytics.createArcXPAnalyticsManager(application, config.orgName!!, config.siteName!!, config.environment!!, SdkName.COMMERCE)
            this.arcxpCommerceManager =
                ArcXPCommerceManager.initialize(context = application, config = config, clientCachedData = clientCachedData)
        } else {
            throw ArcXPCommerceException(
                type = ArcXPCommerceSDKErrorType.INIT_ERROR,
                message = "Failed Initialization: SDK can only be initialized once"
            )
        }
    }

    public fun isInitialized(): Boolean {
        return initialized
    }

    fun getVersion(context: Context): String {
        return context.getString(
            R.string.commerce_sdk_version)
    }

}