package com.arcxp.commerce.util

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.annotation.VisibleForTesting
import androidx.security.crypto.EncryptedSharedPreferences
import com.arcxp.ArcXPMobileSDK.environment
import com.arcxp.ArcXPMobileSDK.organization
import com.arcxp.ArcXPMobileSDK.site
import com.arcxp.commerce.ArcXPCommerceConfig
import com.arcxp.commerce.models.ArcXPAuth
import com.arcxp.commerce.models.ArcXPConfig
import com.arcxp.commerce.models.ArcXPOneTimeAccessLinkAuth
import com.arcxp.commons.util.Constants
import com.arcxp.commons.util.Constants.CACHED_ACCESS_TOKEN
import com.arcxp.commons.util.Constants.CACHED_REFRESH_TOKEN
import com.arcxp.commons.util.Constants.CUR_ENVIRONMENT
import com.arcxp.commons.util.Constants.REGION
import com.arcxp.commons.util.Constants.REMEMBER_USER
import com.arcxp.commons.util.Constants.SUBSCRIPTION_PREFERENCE
import com.arcxp.commons.util.Constants.USER_CONFIG
import com.arcxp.commons.util.Constants.USER_UUID
import com.arcxp.commons.util.DependencyFactory.createMasterKey
import com.arcxp.sdk.R
import com.google.gson.Gson

/**
 * AuthManager is responsible for managing authentication sessions and API environment configurations within the ArcXP Commerce module.
 * It handles user session management, including caching and deleting session data, and provides methods to redefine the environment and set configuration settings.
 *
 * The class supports encrypted shared preferences for secure storage of sensitive data such as access tokens and user UUIDs.
 * It also provides methods to initialize environments, handle user sessions, and manage configuration settings.
 *
 * Usage:
 * - Use `AuthManager.getInstance` to obtain a singleton instance of AuthManager.
 * - Call the provided methods to manage user sessions and environment configurations.
 *
 * Example:
 *
 * val authManager = AuthManager.getInstance(application, clientCachedData, config)
 * authManager.cacheSession(response)
 * authManager.deleteSession()
 *
 * Note: Ensure that the required parameters such as application context, client cached data, and initial configuration are properly initialized before creating an instance of AuthManager.
 *
 * @property currentEnvironment The current environment name.
 * @property sharedPreferences The shared preferences instance for storing session data.
 * @property region The current region.
 * @property identityBaseUrl The base URL for identity-related API calls.
 * @property identityBaseUrlApple The base URL for Apple identity-related API calls.
 * @property salesBaseUrl The base URL for sales-related API calls.
 * @property retailBaseUrl The base URL for retail-related API calls.
 * @property arcConfig The current ArcXP configuration.
 * @property uuid The user UUID.
 * @property accessToken The access token for the current session.
 * @property refreshToken The refresh token for the current session.
 * @property envSharedPrefs The shared preferences instance for environment settings.
 * @property configSharedPreferences The shared preferences instance for configuration settings.
 */
class AuthManager(
    context: Application,
    clientCachedData: Map<String, String> = mutableMapOf(),
    private val initialConfig: ArcXPCommerceConfig
) {

    private lateinit var currentEnvironment: String
    lateinit var sharedPreferences: SharedPreferences
    private lateinit var region: String
    lateinit var identityBaseUrl: String
    lateinit var identityBaseUrlApple: String
    lateinit var salesBaseUrl: String
    lateinit var retailBaseUrl: String
    private var arcConfig: ArcXPConfig? = null

    var uuid: String? = null
    var accessToken: String? = null
    var refreshToken: String? = null

    private val envSharedPrefs: SharedPreferences =
        context.getSharedPreferences(Constants.ENVIRONMENT_PREFERENCES, Context.MODE_PRIVATE)
    private val configSharedPreferences: SharedPreferences =
        context.getSharedPreferences(USER_CONFIG, Context.MODE_PRIVATE)

    init {
        initEnvironments(context)
        handleUserSession(clientCachedData)
    }

    private fun initEnvironments(context: Context) {
        if (initialConfig.autoCache) {
            sharedPreferences = EncryptedSharedPreferences.create(
                context,
                SUBSCRIPTION_PREFERENCE,
                createMasterKey(context = context),
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        }


        if (initialConfig.baseUrl.isNullOrBlank()) {
            identityBaseUrl = context.getString(
                R.string.identity_base_url_1,
                organization, site, environment
            )
            identityBaseUrlApple = context.getString(R.string.old_base_url)
            salesBaseUrl = context.getString(
                R.string.sales_base_url_1,
                organization
            )
            retailBaseUrl = context.getString(
                R.string.retail_base_url_1,
                organization
            )
        } else {
            identityBaseUrl = context.getString(
                R.string.identity_base_url_2,
                initialConfig.baseUrl
            )
            identityBaseUrlApple = context.getString(R.string.old_base_url)
            salesBaseUrl = context.getString(
                R.string.sales_base_url_2,
                initialConfig.baseSalesUrl
            )
            retailBaseUrl = context.getString(
                R.string.retail_base_url_2,
                initialConfig.baseRetailUrl
            )
        }
    }

    private fun handleUserSession(clientCachedData: Map<String, String>) {
        if (envSharedPrefs.getBoolean(REMEMBER_USER, false)) {
            recapSession(clientCachedData)
        } else {
            deleteSession()
        }
    }

    private fun recapSession(clientCachedData: Map<String, String>) {
        if (initialConfig.autoCache) {
            uuid = sharedPreferences.getString(USER_UUID, null)
            accessToken = sharedPreferences.getString(CACHED_ACCESS_TOKEN, null)
            refreshToken = sharedPreferences.getString(CACHED_REFRESH_TOKEN, null)
        } else {
            uuid = clientCachedData["uuid"]
            accessToken = clientCachedData["accessToken"]
            refreshToken = clientCachedData["refreshToken"]
        }
    }

    fun redefineEnvironment(env: Constants.EnvironmentType, reg: String? = null) {
        currentEnvironment = env.name
        configSharedPreferences.edit().putString(CUR_ENVIRONMENT, env.name).apply()
        reg?.let {
            region = reg
            configSharedPreferences.edit().putString(REGION, it).apply()
        }
    }

    fun cacheSession(response: ArcXPAuth) {
        uuid = response.uuid
        accessToken = response.accessToken
        refreshToken = response.refreshToken
        if (initialConfig.autoCache) {
            sharedPreferences.edit().putString(USER_UUID, response.uuid).apply()
            sharedPreferences.edit().putString(CACHED_ACCESS_TOKEN, response.accessToken).apply()
            sharedPreferences.edit().putString(CACHED_REFRESH_TOKEN, response.refreshToken).apply()
        }
    }

    fun cacheSession(response: ArcXPOneTimeAccessLinkAuth) {
        uuid = response.uuid
        accessToken = response.accessToken
        if (initialConfig.autoCache) {
            sharedPreferences.edit().putString(USER_UUID, response.uuid).apply()
            sharedPreferences.edit().putString(CACHED_ACCESS_TOKEN, response.accessToken).apply()
        }
    }

    fun deleteSession() {
        uuid = null
        accessToken = null
        refreshToken = null
        if (initialConfig.autoCache) {
            sharedPreferences.edit().putString(USER_UUID, null).apply()
            sharedPreferences.edit().putString(CACHED_ACCESS_TOKEN, null).apply()
            sharedPreferences.edit().putString(CACHED_REFRESH_TOKEN, null).apply()
        }
    }

    fun setShouldRememberUser(remember: Boolean) {
        envSharedPrefs.edit().putBoolean(REMEMBER_USER, remember).apply()
    }

    fun setConfig(response: ArcXPConfig) {
        val jsonString = Gson().toJson(response)
        configSharedPreferences.edit().putString(USER_CONFIG, jsonString).apply()
        arcConfig = response
    }

    fun getConfig(): ArcXPConfig? {
        return if (arcConfig == null) {
            val jsonString = configSharedPreferences.getString(USER_CONFIG, null)
            if (jsonString != null)
                Gson().fromJson(jsonString, ArcXPConfig::class.java)
            else
                null
        } else {
            null
        }
    }

    fun loadLocalConfig(config: ArcXPCommerceConfig) {
        val jsonString = configSharedPreferences.getString(USER_CONFIG, null)
        arcConfig = if (jsonString != null) {
            Gson().fromJson(jsonString, ArcXPConfig::class.java)
        } else {
            ArcXPConfig(
                facebookAppId = config.facebookAppId,
                googleClientId = config.googleClientId,
                signupRecaptcha = config.recaptchaForSignup,
                signinRecaptcha = config.recaptchaForSignin,
                recaptchaSiteKey = config.recaptchaSiteKey,
                magicLinkRecapatcha = config.recaptchaForOneTimeAccess,
                disqus = null,
                keyId = null,
                orgTenants = null,
                pwLowercase = null,
                pwMinLength = null,
                pwPwNumbers = null,
                pwSpecialCharacters = null,
                pwUppercase = null,
                teamId = null,
                urlToReceiveAuthToken = null
            )
        }
        setConfig(arcConfig!!)
    }

    @VisibleForTesting
    internal fun getCurrentEnvironment() = currentEnvironment

    @VisibleForTesting
    internal fun getRegion() = region

    @VisibleForTesting
    internal fun getConfigTest() = arcConfig


    companion object {

        @Volatile
        private var INSTANCE: AuthManager? = null

        @JvmStatic
        fun getInstance(
            context: Application,
            clientCachedData: Map<String, String>,
            config: ArcXPCommerceConfig
        ): AuthManager {
            INSTANCE ?: synchronized(this)
            {
                INSTANCE
                    ?: AuthManager(context, clientCachedData, config).also {
                        INSTANCE = it
                    }
            }

            return INSTANCE!!
        }

        fun getInstance(): AuthManager {
            return INSTANCE!!
        }
    }
}
