package com.arcxp.commons.util

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import androidx.activity.result.IntentSenderRequest
import androidx.lifecycle.MutableLiveData
import androidx.room.Room
import androidx.security.crypto.MasterKey
import androidx.sqlite.db.SimpleSQLiteQuery
import com.arcxp.commerce.ArcXPCommerceConfig
import com.arcxp.commerce.ArcXPCommerceManager
import com.arcxp.commerce.ArcXPRulesData
import com.arcxp.commerce.LoginWithGoogleOneTapResultsReceiver
import com.arcxp.commerce.LoginWithGoogleResultsReceiver
import com.arcxp.commerce.apimanagers.IdentityApiManager
import com.arcxp.commerce.apimanagers.RetailApiManager
import com.arcxp.commerce.apimanagers.SalesApiManager
import com.arcxp.commerce.callbacks.ArcXPIdentityListener
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
import com.arcxp.commons.throwables.ArcXPException
import com.arcxp.commons.throwables.ArcXPSDKErrorType
import com.arcxp.content.ArcXPContentConfig
import com.arcxp.content.ArcXPContentManager
import com.arcxp.content.apimanagers.ContentApiManager
import com.arcxp.content.db.CacheManager
import com.arcxp.content.db.Database
import com.arcxp.content.repositories.ContentRepository
import com.arcxp.content.retrofit.RetrofitController
import com.arcxp.identity.UserSettingsManager
import com.arcxp.sdk.R
import com.arcxp.video.ArcMediaClient
import com.arcxp.video.api.VideoApiManager
import com.arcxp.video.cast.ArcCastManager
import com.facebook.CallbackManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

internal object DependencyFactory {

    //commons
    //v1 resizer
    fun createArcXPResizer(baseUrl: String, resizerKey: String) = ArcXPResizer(
        baseUrl = baseUrl,
        resizerKey = resizerKey
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

    fun createIOScope() = CoroutineScope(ioDispatcher() + SupervisorJob())
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
    fun createUserSettingsManager(identityApiManager: IdentityApiManager) = UserSettingsManager(identityApiManager = identityApiManager)
    fun createRetailApiManager() = RetailApiManager()
    fun createPaywallManager(application: Application, retailApiManager: RetailApiManager, salesApiManager: SalesApiManager) = PaywallManager(
        retailApiManager = retailApiManager,
        salesApiManager = salesApiManager,
        sharedPreferences = application.getSharedPreferences(Constants.PAYWALL_PREFERENCES, Context.MODE_PRIVATE)
    )
    fun createGoogleSignInClient(application: Application) = GoogleSignIn.getClient(application, GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestId()
            .requestIdToken(application.getString(R.string.google_key))
            .requestEmail()
            .build())

    fun createLoginWithGoogleResultsReceiver(signInIntent : Intent, manager: ArcXPCommerceManager, listener: ArcXPIdentityListener) = LoginWithGoogleResultsReceiver(signInIntent = signInIntent, manager = manager, listener = listener)
    fun createLoginWithGoogleOneTapResultsReceiver(signInIntent : IntentSenderRequest, manager: ArcXPCommerceManager, listener: ArcXPIdentityListener) = LoginWithGoogleOneTapResultsReceiver(signInIntent = signInIntent, manager = manager, listener = listener)
    fun buildIntentSenderRequest(intentSender: IntentSender) = IntentSenderRequest.Builder(intentSender).build()

    fun createMasterKey(context: Context) = MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    //video
    fun createMediaClient(orgName: String, env: String) = ArcMediaClient.createClient(
        orgName = orgName,
        serverEnvironment = env
    )
    internal fun createCastManager(activity: Application) = ArcCastManager(mActivityContext = activity)

    fun createVideoApiManager(baseUrl: String) = VideoApiManager(baseUrl = baseUrl)
    fun createVideoApiManager(orgName: String, environmentName: String) =
        VideoApiManager(orgName = orgName, environmentName = environmentName)


    //content
    // this creates arcxp content manager, repository and database
    fun createArcXPContentManager(application: Application, arcXPAnalyticsManager: ArcXPAnalyticsManager, contentConfig: ArcXPContentConfig) = ArcXPContentManager(
        application = application,
        contentRepository = createContentRepository(application = application),
        arcXPAnalyticsManager = arcXPAnalyticsManager,
        contentConfig = contentConfig
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
    
    fun createArcXPRulesData() = ArcXPRulesData(HashMap())

}