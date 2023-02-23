package com.arcxp.commons.util

import android.app.Application
import androidx.room.Room
import com.arcxp.ArcXPException
import com.arcxp.ArcXPSDKErrorType
import com.arcxp.commerce.ArcXPCommerceConfig
import com.arcxp.commerce.ArcXPCommerceManager
import com.arcxp.commons.analytics.ArcXPAnalyticsManager
import com.arcxp.commons.models.SdkName
import com.arcxp.content.ArcXPContentManager
import com.arcxp.content.db.CacheManager
import com.arcxp.content.db.Database
import com.arcxp.content.repositories.ContentRepository
import com.arcxp.video.ArcMediaClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

object DependencyFactory {

    fun createArcXPResizer(application: Application, baseUrl: String) = ArcXPResizer(
        application = application,
        baseUrl = baseUrl
    )

    fun createMediaClient(orgName: String, env: String) = ArcMediaClient.createClient(
        orgName = orgName,
        serverEnvironment = env
    )

    fun createArcXPAnalyticsManager(
        application: Application,
        organization: String,
        site: String,
        environment: String,
        sdk_name: SdkName
    ) = ArcXPAnalyticsManager(
        application = application,
        organization = organization,
        site = site,
        environment = environment,
        sdk_name = sdk_name,
        buildVersionProvider = createBuildVersionProvider(),
        analyticsUtil = AnalyticsUtil(application)
    )

    fun createIOScope() = CoroutineScope(Dispatchers.IO + SupervisorJob())
    fun createBuildVersionProvider() = BuildVersionProviderImpl()
    fun createArcXPLogger(
        application: Application,
        organization: String,
        environment: String,
        site: String
    ) = ArcXPLogger(application, organization, environment, site)

    // this creates arcxp content manager, repository and database
    fun createArcXPContentManager(application: Application) = ArcXPContentManager(
        application = application,
        contentRepository = createContentRepository(application = application)
    )

    fun createArcXPCommerceManager(
        application: Application,
        config: ArcXPCommerceConfig,
        clientCachedData: Map<String, String>
    ) = ArcXPCommerceManager.initialize(
        context = application,
        config = config,
        clientCachedData = clientCachedData
    )

    private fun createContentRepository(application: Application): ContentRepository {
        val cacheManager =
            CacheManager(application = application, database = createDb(application = application))
        cacheManager.vac() //rebuilds the database file, repacking it into a minimal amount of disk space
        return ContentRepository(cacheManager = cacheManager)
    }

    private fun createDb(application: Application) = Room.databaseBuilder(
        application,
        Database::class.java, "database"
    ).build()

    fun createArcXPException(
        type: ArcXPSDKErrorType = ArcXPSDKErrorType.INIT_ERROR,
        message: String
    ) = ArcXPException(
        type = type,
        message = message
    )

}