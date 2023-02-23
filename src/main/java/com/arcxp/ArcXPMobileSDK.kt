package com.arcxp

import android.app.Application
import android.content.Context
import androidx.annotation.VisibleForTesting
import com.arcxp.commerce.ArcXPCommerceConfig
import com.arcxp.commerce.ArcXPCommerceManager
import com.arcxp.commons.analytics.ArcXPAnalyticsManager
import com.arcxp.commons.models.SdkName
import com.arcxp.commons.util.ArcXPLogger
import com.arcxp.commons.util.ArcXPResizer
import com.arcxp.commons.util.DependencyFactory.createArcXPAnalyticsManager
import com.arcxp.commons.util.DependencyFactory.createArcXPCommerceManager
import com.arcxp.commons.util.DependencyFactory.createArcXPContentManager
import com.arcxp.commons.util.DependencyFactory.createArcXPException
import com.arcxp.commons.util.DependencyFactory.createArcXPLogger
import com.arcxp.commons.util.DependencyFactory.createArcXPResizer
import com.arcxp.commons.util.DependencyFactory.createMediaClient
import com.arcxp.content.ArcXPContentConfig
import com.arcxp.content.ArcXPContentManager
import com.arcxp.sdk.R
import com.arcxp.video.ArcMediaClient
import com.google.errorprone.annotations.Keep

@Keep
object ArcXPMobileSDK {

    //commerce
    private var commerceConfig: ArcXPCommerceConfig? = null
    private var commerceManager: ArcXPCommerceManager? = null

    //content
    private var contentConfig: ArcXPContentConfig? = null
    private var contentManager: ArcXPContentManager? = null

    //video
    private var mediaClient: ArcMediaClient? = null

    //shared
    private var analytics: ArcXPAnalyticsManager? = null
    private var resizer: ArcXPResizer? = null
    private var logger: ArcXPLogger? = null
    private var application: Application? = null

    var initialized = false
        private set
    var organization = ""
        private set
    var site = ""
        private set
    var environment = ""
        private set
    var baseUrl = ""
        private set


    const val initError =
        "Failed Initialization: SDK uninitialized, please run initialize method first"
    const val initErrorCommerce = "Failed Initialization: Commerce has not been configured"
    const val initErrorContent = "Failed Initialization: Content has not been configured"
    const val initErrorOrgSite =
        "Failed Initialization: SDK needs values for org and site for analytics/logging"
    const val initErrorBaseUrl =
        "Failed Initialization: SDK needs values for baseUrl for backend communication"

    fun initialize(
        application: Application,
        commerceConfig: ArcXPCommerceConfig? = null,
        clientCachedData: Map<String, String>? = null,
        site: String,
        environment: String,
        baseUrl: String,
        org: String,
        contentConfig: ArcXPContentConfig? = null
    ) {
        when {
            baseUrl.isBlank() -> {
                throw createArcXPException(message = initErrorBaseUrl)
            }
            site.isBlank() || org.isBlank() -> {
                throw createArcXPException(message = initErrorOrgSite)
            }
        }
        this.application = application
        this.organization = org
        this.site = site
        this.environment = environment
        this.baseUrl = baseUrl
        this.mediaClient = createMediaClient(orgName = org, env = environment)
        this.resizer = createArcXPResizer(application = application, baseUrl = baseUrl)
        this.analytics = createArcXPAnalyticsManager(
            application = application,
            organization = org,
            site = site,
            environment = environment,
            sdk_name = SdkName.SINGLE
        )
        this.logger = createArcXPLogger(
            application = application,
            organization = org,
            environment = environment,
            site = site
        )
        contentConfig?.let {
            this.contentConfig = it
            contentManager = createArcXPContentManager(application = application)
        }
        commerceConfig?.let {
            this.commerceConfig = it
            commerceManager = createArcXPCommerceManager(
                application = application,
                config = it,
                clientCachedData = clientCachedData ?: mutableMapOf()
            )
        }
        initialized = true
    }

    fun getVersion(context: Context) = context.getString(R.string.sdk_version)

    internal fun resizer() =
            resizer ?: throw createArcXPException(message = initError)

    fun mediaClient() =
        mediaClient ?: throw createArcXPException(message = initError)

    internal fun logger() =
        logger ?: throw createArcXPException(message = initError)

    internal fun analytics() =
        analytics ?: throw createArcXPException(message = initError)

    fun contentManager() =
        contentManager ?: throw createArcXPException(message = initErrorContent)

    fun contentConfig() =
        contentConfig ?: throw createArcXPException(message = initErrorContent)

    fun commerceManager() =
        commerceManager ?: throw createArcXPException(message = initErrorCommerce)

    fun commerceConfig() =
        commerceConfig ?: throw createArcXPException(message = initErrorCommerce)
    fun application() =
        application ?: throw createArcXPException(message = initError)
    fun commerceInitialized() = commerceManager != null
    fun contentInitialized() = contentManager != null

    @VisibleForTesting
    fun reset() {
        initialized = false
        commerceConfig = null
        commerceManager = null
        contentConfig = null
        contentManager = null
        analytics = null
        logger = null
        resizer = null
        mediaClient = null
        application = null
    }
}
