package com.arcxp.video

import android.app.Application
import android.content.Context
import androidx.annotation.VisibleForTesting
import com.arcxp.video.model.ArcVideoSDKErrorType
import com.arcxp.video.util.DependencyProvider
import com.arcxp.commons.analytics.ArcXPAnalyticsManager
import com.arcxp.commons.models.SdkName
import com.arcxp.commons.util.ArcXPAnalytics
import com.arcxp.sdk.R

object ArcXPVideoSDK {

    private var mediaClient: ArcMediaClient? = null
    private var initialized = false
    private var analytics: ArcXPAnalyticsManager? = null
    private var resizer: ArcXPResizer? = null

    fun mediaClient() =
        if (initialized) {
            mediaClient!!
        } else {
            throw ArcException(
                type = ArcVideoSDKErrorType.INIT_ERROR,
                message = "Failed Initialization: SDK uninitialized, please run initialize method first"
            )
        }

    fun initialize(application: Application, baseUrl: String,  org: String, site: String, env: String) {
        if (!initialized) {
            initialized = true
            this.mediaClient = DependencyProvider.createMediaClient(org, env)
            this.resizer = DependencyProvider.createArcXPResizer(application = application, baseUrl = baseUrl)
            this.analytics = ArcXPAnalytics.createArcXPAnalyticsManager(
                application = application,
                organization = org,
                site = site,
                environment = env,
                SdkName.CONTENT
            )
        } else {
            throw ArcException(
                type = ArcVideoSDKErrorType.INIT_ERROR,
                message = "Failed Initialization: SDK can only be initialized once"
            )
        }
    }

    /**
     * If initialization has occurred,
     * this function returns the [ArcXPAnalyticsManager] created during initialization
     * @return [arcXPAnalyticsManager]
     */
    fun analytics(): ArcXPAnalyticsManager {
        if (!initialized) {
            throw ArcException(
                type = ArcVideoSDKErrorType.INIT_ERROR,
                message = "Failed Initialization: SDK uninitialized, please run initialize method first"
            )
        }
        return analytics!!
    }

    fun resizer() =
        if (initialized) {
            resizer!!
        } else {
            throw ArcException(
                type = ArcVideoSDKErrorType.INIT_ERROR,
                message = "Failed Initialization: SDK uninitialized, please run initialize method first"
            )
        }

    fun getVersion(context: Context): String {
        return context.getString(R.string.sdk_version)
    }

    @VisibleForTesting
    internal fun reset() {
        initialized = false
        mediaClient = null
        analytics = null
        resizer = null
    }
}