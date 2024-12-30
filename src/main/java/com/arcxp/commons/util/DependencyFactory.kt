package com.arcxp.commons.util

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import androidx.activity.result.IntentSenderRequest
import androidx.lifecycle.MutableLiveData
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
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
import com.arcxp.commons.image.ArcXPResizerV2
import com.arcxp.commons.image.CollectionImageUtil
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

/**
 * DependencyFactory is a utility object responsible for creating and providing various dependencies used within the ArcXP Commerce module.
 * It includes methods to create instances of managers, repositories, view models, and other utility objects required by the application.
 *
 * The object defines the following operations:
 * - Create instances of image utilities, media sessions, and resizers
 * - Create instances of analytics managers and loggers
 * - Create instances of commerce managers, view models, and repositories
 * - Create instances of video clients and managers
 * - Create instances of content managers and repositories
 * - Create instances of error and exception handling classes
 *
 * Usage:
 * - Use the provided methods to obtain instances of required dependencies.
 *
 * Example:
 *
 * val analyticsManager = DependencyFactory.createArcXPAnalyticsManager(application, "organization", "site", "environment", SdkName.COMMERCE, "1.0.0")
 * val logger = DependencyFactory.createArcXPLogger(application, "organization", "environment", "site")
 *
 * Note: Ensure that the application context and other parameters are properly configured before using DependencyFactory.
 *
 * @method createImageUtil Create an instance of CollectionImageUtil.
 * @method createMediaSession Create an instance of MediaSession.
 * @method createArcXPV1Resizer Create an instance of ArcXPResizerV1.
 * @method createArcXPV2Resizer Create an instance of ArcXPResizerV2.
 * @method createArcXPAnalyticsManager Create an instance of ArcXPAnalyticsManager.
 * @method createArcXPLogger Create an instance of ArcXPLogger.
 * @method createIOScope Create a CoroutineScope for IO operations.
 * @method ioDispatcher Provide a Dispatcher for IO operations.
 * @method createBuildVersionProvider Create an instance of BuildVersionProviderImpl.
 * @method createArcXPCommerceManager Create an instance of ArcXPCommerceManager.
 * @method createRetailViewModel Create an instance of RetailViewModel.
 * @method createSalesViewModel Create an instance of SalesViewModel.
 * @method createIdentityViewModel Create an instance of IdentityViewModel.
 * @method createCallBackManager Create an instance of CallbackManager.
 * @method createIdentityRepository Create an instance of IdentityRepository.
 * @method createSalesRepository Create an instance of SalesRepository.
 * @method createRetailRepository Create an instance of RetailRepository.
 * @method createIdentityApiManager Create an instance of IdentityApiManager.
 * @method createSalesApiManager Create an instance of SalesApiManager.
 * @method createUserSettingsManager Create an instance of UserSettingsManager.
 * @method createRetailApiManager Create an instance of RetailApiManager.
 * @method createPaywallManager Create an instance of PaywallManager.
 * @method createGoogleSignInClient Create an instance of GoogleSignInClient.
 * @method createLoginWithGoogleResultsReceiver Create an instance of LoginWithGoogleResultsReceiver.
 * @method createLoginWithGoogleOneTapResultsReceiver Create an instance of LoginWithGoogleOneTapResultsReceiver.
 * @method buildIntentSenderRequest Build an IntentSenderRequest.
 * @method createMasterKey Create an instance of MasterKey.
 * @method createMediaClient Create an instance of ArcMediaClient.
 * @method createCastManager Create an instance of ArcCastManager.
 * @method createVideoApiManager Create an instance of VideoApiManager.
 * @method createArcXPContentManager Create an instance of ArcXPContentManager.
 * @method createLiveData Create an instance of MutableLiveData.
 * @method vacuumQuery Create a SimpleSQLiteQuery for VACUUM.
 * @method checkPointQuery Create a SimpleSQLiteQuery for WAL checkpoint.
 * @method createArcXPException Create an instance of ArcXPException.
 * @method createArcXPError Create an instance of ArcXPError.
 * @method createArcXPRulesData Create an instance of ArcXPRulesData.
 */
internal object DependencyFactory {

    //commons
    fun createImageUtil(baseUrl: String, context: Context) = CollectionImageUtil(baseUrl, context)

    fun createMediaSession(application: Context, exoPlayer: ExoPlayer) = MediaSession.Builder(application, exoPlayer)
        .build()

    //v1 resizer
    fun createArcXPV1Resizer(baseUrl: String, resizerKey: String) = ArcXPResizerV1(
        baseUrl = baseUrl,
        resizerKey = resizerKey
    )

    //v2 resizer
    fun createArcXPV2Resizer(baseUrl: String) = ArcXPResizerV2(
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
    fun createUserSettingsManager(identityApiManager: IdentityApiManager) =
        UserSettingsManager(identityApiManager = identityApiManager)

    fun createRetailApiManager() = RetailApiManager()
    fun createPaywallManager(
        application: Application,
        retailApiManager: RetailApiManager,
        salesApiManager: SalesApiManager
    ) = PaywallManager(
        retailApiManager = retailApiManager,
        salesApiManager = salesApiManager,
        sharedPreferences = application.getSharedPreferences(
            Constants.PAYWALL_PREFERENCES,
            Context.MODE_PRIVATE
        )
    )

    fun createGoogleSignInClient(application: Application) = GoogleSignIn.getClient(
        application, GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestId()
            .requestIdToken(application.getString(R.string.google_key))
            .requestEmail()
            .build()
    )

    fun createLoginWithGoogleResultsReceiver(
        signInIntent: Intent,
        manager: ArcXPCommerceManager,
        listener: ArcXPIdentityListener
    ) = LoginWithGoogleResultsReceiver(
        signInIntent = signInIntent,
        manager = manager,
        listener = listener
    )

    fun createLoginWithGoogleOneTapResultsReceiver(
        signInIntent: IntentSenderRequest,
        manager: ArcXPCommerceManager,
        listener: ArcXPIdentityListener
    ) = LoginWithGoogleOneTapResultsReceiver(
        signInIntent = signInIntent,
        manager = manager,
        listener = listener
    )

    fun buildIntentSenderRequest(intentSender: IntentSender) =
        IntentSenderRequest.Builder(intentSender).build()

    fun createMasterKey(context: Context) =
        MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

    //video
    fun createMediaClient(orgName: String, env: String) = ArcMediaClient(
        orgName = orgName,
        serverEnvironment = env
    )

    internal fun createCastManager(activity: Application) =
        ArcCastManager(mActivityContext = activity)

    fun createVideoApiManager(baseUrl: String) = VideoApiManager(baseUrl = baseUrl)
    fun createVideoApiManager(orgName: String, environmentName: String) =
        VideoApiManager(orgName = orgName, environmentName = environmentName)


    //content
    // this creates arcxp content manager, repository and database
    fun createArcXPContentManager(
        application: Application,
        arcXPAnalyticsManager: ArcXPAnalyticsManager,
        contentConfig: ArcXPContentConfig,
        baseUrl: String,
    ) = ArcXPContentManager(
        application = application,
        contentRepository = ContentRepository(
            application = application,
            cacheManager = CacheManager(
                application = application, database = Room.databaseBuilder(
                    context = application,
                    klass = Database::class.java, name = "database"
                ).fallbackToDestructiveMigration().build()
            ),
            contentApiManager = ContentApiManager(
                contentConfig = contentConfig,
                application = application,
                contentService = RetrofitController.getContentService(baseUrl = baseUrl),
                navigationService = RetrofitController.getNavigationService(baseUrl = baseUrl)
            )
        ),
        arcXPAnalyticsManager = arcXPAnalyticsManager
    )

    fun <T> createLiveData(default: T? = null) = MutableLiveData<T>(default)
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