package com.arcxp.commerce.util

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.annotation.VisibleForTesting
import androidx.media3.common.util.UnstableApi
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
 * Class manage authentication sessions and api environment
 * @suppress
 */
@UnstableApi
class AuthManager(
    private val context: Application,
    private val clientCachedData: Map<String, String> = mutableMapOf(),
    private val config: ArcXPCommerceConfig
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
        if (this.config.autoCache) {
            sharedPreferences = EncryptedSharedPreferences.create(
                context,
                SUBSCRIPTION_PREFERENCE,
                createMasterKey(context = context),
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        }


        if (config.baseUrl.isNullOrBlank()) {
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
                config.baseUrl
            )
            identityBaseUrlApple = context.getString(R.string.old_base_url)
            salesBaseUrl = context.getString(
                R.string.sales_base_url_2,
                config.baseSalesUrl
            )
            retailBaseUrl = context.getString(
                R.string.retail_base_url_2,
                config.baseRetailUrl
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
        if (this.config.autoCache) {
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
        if (this.config.autoCache) {
            sharedPreferences.edit().putString(USER_UUID, response.uuid).apply()
            sharedPreferences.edit().putString(CACHED_ACCESS_TOKEN, response.accessToken).apply()
            sharedPreferences.edit().putString(CACHED_REFRESH_TOKEN, response.refreshToken).apply()
        }
    }

    fun cacheSession(response: ArcXPOneTimeAccessLinkAuth) {
        uuid = response.uuid
        accessToken = response.accessToken
        if (this.config.autoCache) {
            sharedPreferences.edit().putString(USER_UUID, response.uuid).apply()
            sharedPreferences.edit().putString(CACHED_ACCESS_TOKEN, response.accessToken).apply()
        }
    }

    fun deleteSession() {
        uuid = null
        accessToken = null
        refreshToken = null
        if (this.config.autoCache) {
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
