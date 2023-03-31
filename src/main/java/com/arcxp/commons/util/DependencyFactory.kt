package com.arcxp.commons.util

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.room.Room
import androidx.sqlite.db.SimpleSQLiteQuery
import com.arcxp.commons.throwables.ArcXPException
import com.arcxp.commons.throwables.ArcXPSDKErrorType
import com.arcxp.commerce.ArcXPCommerceConfig
import com.arcxp.commerce.ArcXPCommerceManager
import com.arcxp.commerce.apimanagers.IdentityApiManager
import com.arcxp.commerce.apimanagers.RetailApiManager
import com.arcxp.commerce.apimanagers.SalesApiManager
import com.arcxp.commerce.paywall.PaywallManager
import com.arcxp.commerce.repositories.IdentityRepository
import com.arcxp.commerce.repositories.RetailRepository
import com.arcxp.commerce.repositories.SalesRepository
import com.arcxp.commerce.util.AuthManager
import com.arcxp.commerce.viewmodels.IdentityViewModel
import com.arcxp.commerce.viewmodels.RetailViewModel
import com.arcxp.commerce.viewmodels.SalesViewModel
import com.arcxp.commons.analytics.ArcXPAnalyticsManager
import com.arcxp.commons.models.SdkName
import com.arcxp.commons.throwables.ArcXPError
import com.arcxp.content.ArcXPContentManager
import com.arcxp.content.apimanagers.ContentApiManager
import com.arcxp.content.db.CacheManager
import com.arcxp.content.db.Database
import com.arcxp.content.repositories.ContentRepository
import com.arcxp.content.retrofit.RetrofitController
import com.arcxp.video.ArcMediaClient
import com.arcxp.video.api.VideoApiManager
import com.facebook.CallbackManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

object DependencyFactory {

    //commons
    //v1 resizer
    fun createArcXPResizer(application: Application, baseUrl: String) = ArcXPResizer(
        application = application,
        baseUrl = baseUrl
    )

    fun createArcXPAnalyticsManager(
        application: Application,
        organization: String,
        site: String,
        environment: String,
        sdk_name: SdkName,
        sdk_version: String
    ) = ArcXPAnalyticsManager(
        application = application,
        organization = organization,
        site = site,
        environment = environment,
        sdk_name = sdk_name,
        sdk_version = sdk_version,
        buildVersionProvider = createBuildVersionProvider(),
        analyticsUtil = AnalyticsUtil(application)
    )

    fun createArcXPLogger(
        application: Application,
        organization: String,
        environment: String,
        site: String
    ) = ArcXPLogger(application, organization, environment, site)

    fun createIOScope() = CoroutineScope(Dispatchers.IO + SupervisorJob())
    fun ioDispatcher() = Dispatchers.IO
    fun createBuildVersionProvider() = BuildVersionProviderImpl()


    //commerce
    fun createArcXPCommerceManager(
        application: Application,
        config: ArcXPCommerceConfig,
        clientCachedData: Map<String, String>
    ) = ArcXPCommerceManager.initialize(
        context = application,
        config = config,
        clientCachedData = clientCachedData
    )

    fun createRetailViewModel() = RetailViewModel(createRetailRepository())
    fun createSalesViewModel() = SalesViewModel(createSalesRepository())
    fun createIdentityViewModel(authManager: AuthManager) =
        IdentityViewModel(authManager, createIdentityRepository())

    fun createCallBackManager() = CallbackManager.Factory.create()
    fun createIdentityRepository() = IdentityRepository()
    fun createSalesRepository() = SalesRepository()
    fun createRetailRepository() = RetailRepository()
    fun createIdentityApiManager(authManager: AuthManager) = IdentityApiManager(authManager)
    fun createSalesApiManager() = SalesApiManager()
    fun createRetailApiManager() = RetailApiManager()
    fun createPaywallManager(application: Application, retailApiManager: RetailApiManager, salesApiManager: SalesApiManager) = PaywallManager(
        context = application,
        retailApiManager = retailApiManager,
        salesApiManager = salesApiManager
    )

    //video
    fun createMediaClient(orgName: String, env: String) = ArcMediaClient.createClient(
        orgName = orgName,
        serverEnvironment = env
    )

    fun createVideoApiManager(baseUrl: String) = VideoApiManager(baseUrl = baseUrl)
    fun createVideoApiManager(orgName: String, environmentName: String) =
        VideoApiManager(orgName = orgName, environmentName = environmentName)


    //content
    // this creates arcxp content manager, repository and database
    fun createArcXPContentManager(application: Application) = ArcXPContentManager(
        application = application,
        contentRepository = createContentRepository(application = application)
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

    fun createContentApiManager() = ContentApiManager()
    fun createContentService() = RetrofitController.getContentService()
    fun createNavigationService() = RetrofitController.navigationService()
    fun <T> createLiveData() = MutableLiveData<T>()
    fun vacuumQuery() = SimpleSQLiteQuery("VACUUM")
    fun checkPointQuery() = SimpleSQLiteQuery("pragma wal_checkpoint(full)")

    //errors / exceptions
    fun createArcXPException(
        type: ArcXPSDKErrorType = ArcXPSDKErrorType.INIT_ERROR,
        message: String?,
        value: Any? = null
    ) = ArcXPException(
        type = type,
        message = message ?: "",
        value = value
    )

    fun createArcXPError(
        type: ArcXPSDKErrorType = ArcXPSDKErrorType.INIT_ERROR,
        message: String?,
        value: Any? = null
    ) = ArcXPError(
        type = type,
        message = message ?: "",
        value = value
    )

}