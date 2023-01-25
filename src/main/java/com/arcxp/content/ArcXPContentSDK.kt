package com.arcxp.content

import android.app.Application
import android.content.Context
import androidx.annotation.Keep
import androidx.annotation.VisibleForTesting
import com.arcxp.commons.analytics.ArcXPAnalyticsManager
import com.arcxp.commons.models.SdkName
import com.arcxp.commons.util.ArcXPAnalytics
import com.arcxp.content.models.ArcXPContentException
import com.arcxp.content.models.ArcXPContentSDKErrorType
import com.arcxp.content.util.DependencyFactory
import com.arcxp.sdk.R

@Keep
/**
 * This singleton is a single access point for initialization and to
 * access all Content SDK functionality.
 *
 * Need to run [initialize] method, then you can access it anywhere
 * Access ArcXP Content via [contentManager] instance created during initialization
 */
object ArcXPContentSDK {

    private var arcXPContentConfig: ArcXPContentConfig? = null // cannot reset/redefine
    private var logger: ArcXPLogger? = null
    private var arcXPContentManager: ArcXPContentManager? = null
    private var resizer: ArcXPResizer? = null
    private var analytics: ArcXPAnalyticsManager? = null
    private var initialized = false

    /**
     * If initialization has occurred,
     * this function returns the [ArcXPContentConfig] passed in during [initialize] method
     * @return [arcXPContentConfig]
     */
    fun arcxpContentConfig() =
        if (arcXPContentConfig != null) {
            arcXPContentConfig!!
        } else {
            throw ArcXPContentException(
                type = ArcXPContentSDKErrorType.INIT_ERROR,
                message = "Failed Retrieving Config: SDK uninitialized, please run initialize method first"
            )
        }

    /**
     * If initialization has occurred,
     * this function returns the [ArcXPContentManager] created during initialization
     * @return [arcXPContentManager]
     */
    fun contentManager() =
        if (initialized) {
            arcXPContentManager!!
        } else {
            throw ArcXPContentException(
                type = ArcXPContentSDKErrorType.INIT_ERROR,
                message = "Failed Initialization: SDK uninitialized, please run initialize method first"
            )
        }
    /**
     * If initialization has occurred,
     * this function returns the [ArcXPAnalyticsManager2] created during initialization
     * @return [arcXPAnalyticsManager]
     */
    fun analytics(): ArcXPAnalyticsManager {
        if (!initialized) {
            throw ArcXPContentException(
                type = ArcXPContentSDKErrorType.INIT_ERROR,
                message = "Failed Initialization: SDK uninitialized, please run initialize method first"
            )
        }
        return analytics!!
    }




    /**
     * @suppress
     * If initialization has occurred,
     * this function returns the [ArcXPLogger] created during initialization
     * @return [logger]
     */
    fun logger() =
        if (initialized) {
            logger!!
        } else {
            throw ArcXPContentException(
                type = ArcXPContentSDKErrorType.INIT_ERROR,
                message = "Failed Initialization: SDK uninitialized, please run initialize method first"
            )
        }

    /**
     * @suppress
     * If initialization has occurred,
     * this function returns the [ArcXPResizer] created during initialization
     * @return [resizer]
     */
    fun resizer() =
        if (initialized) {
            resizer!!
        } else {
            throw ArcXPContentException(
                type = ArcXPContentSDKErrorType.INIT_ERROR,
                message = "Failed Initialization: SDK uninitialized, please run initialize method first"
            )
        }

    /**
     * Call this method to initialize Content SDK
     *
     * @param application Application Context
     * @param config [ArcXPContentConfig] configuration containing baseUrl and other initialization values
     */
    fun initialize(application: Application, config: ArcXPContentConfig) {
        if (!initialized) {
            initialized = true
            this.arcXPContentConfig = config
            this.logger = DependencyFactory.createArcXPLogger(
                application = application,
                organization = config.orgName,
                environment = config.environment,
                site = config.siteName
            )
            this.analytics = ArcXPAnalytics.createArcXPAnalyticsManager(
                application = application,
                organization = config.orgName,
                site = config.siteName,
                environment = config.environment,
                SdkName.CONTENT
            )
            this.arcXPContentManager =
                DependencyFactory.createArcXPContentManager(application = application)
            this.resizer = DependencyFactory.createArcXPResizer(application = application, baseUrl = config.baseUrl)

        } else {
            throw ArcXPContentException(
                type = ArcXPContentSDKErrorType.INIT_ERROR,
                message = "Failed Initialization: SDK can only be initialized once"
            )
        }
    }

    fun getVersion(context: Context): String {
        return context.getString(R.string.sdk_version)
    }

    public fun isInitialized() : Boolean {
        return initialized;
    }


    //this resets our singleton state for testing purposes only
    @VisibleForTesting
    internal fun reset() {
        initialized = false
        arcXPContentConfig = null
        arcXPContentManager = null
        analytics = null
        logger = null
        resizer = null
    }

}