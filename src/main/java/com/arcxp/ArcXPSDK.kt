package com.arcxp

import android.app.Application
import android.content.Context
import com.arcxp.commerce.*
import com.arcxp.commons.analytics.ArcXPAnalyticsManager
import com.arcxp.commons.models.SdkName
import com.arcxp.commons.util.ArcXPAnalytics
import com.arcxp.sdk.R
import com.google.errorprone.annotations.Keep

@Keep
object ArcXPSDK {

    private var arcxpConfig: ArcXPConfig? = null
//    private var arcxpCommerceManager: ArcXPCommerceManager? = null
    private var analytics: ArcXPAnalyticsManager? = null

    private var initialized = false

    fun arcxpConfig() =
        if (ArcXPSDK.arcxpConfig != null) {
            ArcXPSDK.arcxpConfig!!
        } else {
            throw ArcXPException(
                type = ArcXPSDKErrorType.INIT_ERROR,
                message = "Failed Retrieving Config: SDK uninitialized, please run initialize method first"
            )
        }

    /**
     * If initialization has occurred,
     * this function returns the [ArcXPAnalyticsManager] created during initialization
     * @return [arcXPAnalyticsManager]
     */
    fun analytics(): ArcXPAnalyticsManager {
        if (!ArcXPSDK.initialized) {
            throw ArcXPException(
                type = ArcXPSDKErrorType.INIT_ERROR,
                message = "Failed Initialization: SDK uninitialized, please run initialize method first"
            )
        }
        return ArcXPSDK.analytics!!
    }

    fun initialize(application: Application, clientCachedData: Map<String, String>, config: ArcXPConfig) {
        if (!ArcXPSDK.initialized) {
            ArcXPSDK.initialized = true
            this.arcxpConfig = config
//            this.analytics = ArcXPAnalytics.createArcXPAnalyticsManager(application, config.orgName!!, config.siteName!!, config.environment!!, SdkName.COMMERCE)
//            this.arcxpManager =
//                ArcXPManager.initialize(context = application, config = config, clientCachedData = clientCachedData)
        } else {
            throw ArcXPException(
                type = ArcXPSDKErrorType.INIT_ERROR,
                message = "Failed Initialization: SDK can only be initialized once"
            )
        }
    }

    public fun isInitialized(): Boolean {
        return ArcXPSDK.initialized
    }

    fun getVersion(context: Context): String {
        return context.getString(
            R.string.sdk_version)
    }
}