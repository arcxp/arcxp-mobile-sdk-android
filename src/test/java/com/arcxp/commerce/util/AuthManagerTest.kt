package com.arcxp.commerce.util

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.arcxp.ArcXPMobileSDK
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
import com.arcxp.commons.util.Constants.USER_CONFIG
import com.arcxp.commons.util.Constants.USER_UUID
import com.arcxp.commons.util.DependencyFactory
import com.arcxp.sdk.R
import com.google.gson.Gson
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.called
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.spyk
import io.mockk.verify
import io.mockk.verifySequence
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class AuthManagerTest {

    @RelaxedMockK
    lateinit var context: Application

    @MockK
    lateinit var envSharedPrefs: SharedPreferences

    @MockK
    lateinit var configSharedPreferences: SharedPreferences

    @MockK
    lateinit var sharedPreferencesEditor: SharedPreferences.Editor

    @MockK
    lateinit var encryptedSharedPreferences: SharedPreferences

    @RelaxedMockK
    lateinit var masterKeyAlias: MasterKey

    private lateinit var testObject: AuthManager

    private val identityBaseUrl1 = "identityBaseUrl1"
    private val identityBaseUrl2 = "identityBaseUrl2"
    private val identityOldBaseUrl = "old"
    private val salesBaseUrl1 = "sales1"
    private val salesBaseUrl2 = "sales2"
    private val retailBaseUrl1 = "retail1"
    private val retailBaseUrl2 = "retail2"

    private val org = "org"
    private val site = "site"
    private val env = "env"
    private val facebookAppId = "fb"
    private val googleClientId = "gooogle"
    private val signupRecaptcha = true
    private val signinRecaptcha = true
    private val recaptchaSiteKey = "key"
    private val magicLinkRecapatcha = true
    private val configBaseUrl = "configBaseUrl"
    private val baseRetailUrl = "baseRetailUrl"
    private val baseSalesUrl = "baseSalesUrl"
    private val uuid = "123"
    private val accessToken = "923487JKL"
    private val refreshToken = "932847KJHJHG"
    private val clientCachedData =
        mapOf("uuid" to uuid, "accessToken" to accessToken, "refreshToken" to refreshToken)

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        mockkObject(DependencyFactory)
        every { DependencyFactory.createMasterKey(context = context) } returns masterKeyAlias
        context.apply {
            every {
                getSharedPreferences(
                    Constants.ENVIRONMENT_PREFERENCES,
                    Context.MODE_PRIVATE
                )
            } returns envSharedPrefs
            every {
                getSharedPreferences(
                    USER_CONFIG,
                    Context.MODE_PRIVATE
                )
            } returns configSharedPreferences
            every {
                getString(
                    R.string.identity_base_url_1,
                    org,
                    site,
                    env
                )
            } returns identityBaseUrl1
            every {
                getString(
                    R.string.identity_base_url_2,
                    configBaseUrl
                )
            } returns identityBaseUrl2
            every { getString(R.string.old_base_url) } returns identityOldBaseUrl
            every { getString(R.string.sales_base_url_1, org) } returns salesBaseUrl1
            every { getString(R.string.sales_base_url_2, baseSalesUrl) } returns salesBaseUrl2
            every { getString(R.string.retail_base_url_1, org) } returns retailBaseUrl1
            every { getString(R.string.retail_base_url_2, baseRetailUrl) } returns retailBaseUrl2
        }
        mockkStatic(
            EncryptedSharedPreferences::
            class
        )
        every {
            EncryptedSharedPreferences.create(
                context,
                Constants.SUBSCRIPTION_PREFERENCE,
                masterKeyAlias,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } returns encryptedSharedPreferences
        mockkObject(ArcXPMobileSDK)
        every { ArcXPMobileSDK.organization } returns org
        every { ArcXPMobileSDK.site } returns site
        every { ArcXPMobileSDK.environment } returns env
        every { ArcXPMobileSDK.application() } returns context

    }

    @Test
    fun `initEnvironments sets baseUrls when config baseUrl is null`() {
        val config =
            ArcXPCommerceConfig.Builder()
                .setContext(context = context)
                .build()
        every { envSharedPrefs.getBoolean(REMEMBER_USER, false) } returns false

        testObject = AuthManager(context = context, config = config)

        assertEquals(identityBaseUrl1, testObject.identityBaseUrl)
        assertEquals(identityOldBaseUrl, testObject.identityBaseUrlApple)
        assertEquals(retailBaseUrl1, testObject.retailBaseUrl)
        assertEquals(salesBaseUrl1, testObject.salesBaseUrl)

    }

    @Test
    fun `initEnvironments sets baseUrls when config baseUrl is blank`() {
        val config =
            ArcXPCommerceConfig.Builder()
                .setBaseUrl("")
                .setContext(context = context)
                .build()
        every { envSharedPrefs.getBoolean(REMEMBER_USER, false) } returns false

        testObject = AuthManager(context = context, config = config)

        assertEquals(identityBaseUrl1, testObject.identityBaseUrl)
        assertEquals(identityOldBaseUrl, testObject.identityBaseUrlApple)
        assertEquals(retailBaseUrl1, testObject.retailBaseUrl)
        assertEquals(salesBaseUrl1, testObject.salesBaseUrl)

    }

    @Test
    fun `initEnvironments sets baseUrls when baseUrls are populated`() {
        val config =
            ArcXPCommerceConfig.Builder()
                .setBaseUrl(configBaseUrl)
                .setBaseRetailUrl(baseRetailUrl)
                .setBaseSalesUrl(baseSalesUrl)
                .setContext(context = context)
                .build()
        every { envSharedPrefs.getBoolean(REMEMBER_USER, false) } returns false

        testObject = AuthManager(context = context, config = config)

        assertEquals(identityBaseUrl2, testObject.identityBaseUrl)
        assertEquals(identityOldBaseUrl, testObject.identityBaseUrlApple)
        assertEquals(retailBaseUrl2, testObject.retailBaseUrl)
        assertEquals(salesBaseUrl2, testObject.salesBaseUrl)
    }

    @Test
    fun `recapSession when not auto cache`() {
        val config =
            ArcXPCommerceConfig.Builder()
                .setBaseUrl(configBaseUrl)
                .setContext(context = context)
                .build()
        every { envSharedPrefs.getBoolean(REMEMBER_USER, false) } returns true

        testObject =
            AuthManager(context = context, clientCachedData = clientCachedData, config = config)

        assertEquals(uuid, testObject.uuid)
        assertEquals(accessToken, testObject.accessToken)
        assertEquals(refreshToken, testObject.refreshToken)
    }

    @Test
    fun `recapSession when auto cache`() {
        val config =
            ArcXPCommerceConfig.Builder()
                .setBaseUrl(configBaseUrl)
                .enableAutoCache(enable = true)
                .setContext(context = context)
                .build()
        every { envSharedPrefs.getBoolean(REMEMBER_USER, false) } returns true
        every { encryptedSharedPreferences.getString(USER_UUID, null) } returns uuid
        every {
            encryptedSharedPreferences.getString(
                CACHED_ACCESS_TOKEN,
                null
            )
        } returns accessToken
        every {
            encryptedSharedPreferences.getString(
                CACHED_REFRESH_TOKEN,
                null
            )
        } returns refreshToken

        testObject = AuthManager(context = context, config = config)

        assertEquals(uuid, testObject.uuid)
        assertEquals(accessToken, testObject.accessToken)
        assertEquals(refreshToken, testObject.refreshToken)
    }

    @Test
    fun `init deleteSession with autoCache`() {
        val config =
            ArcXPCommerceConfig.Builder()
                .setBaseUrl(configBaseUrl)
                .enableAutoCache(enable = true)
                .setContext(context = context)
                .build()
        every { envSharedPrefs.getBoolean(REMEMBER_USER, false) } returns false
        every { encryptedSharedPreferences.getString(USER_UUID, null) } returns uuid
        every {
            encryptedSharedPreferences.getString(
                CACHED_ACCESS_TOKEN,
                null
            )
        } returns accessToken
        every {
            encryptedSharedPreferences.getString(
                CACHED_REFRESH_TOKEN,
                null
            )
        } returns refreshToken
        every { encryptedSharedPreferences.edit() } returns sharedPreferencesEditor
        every { sharedPreferencesEditor.putString(any(), any()) } returns sharedPreferencesEditor
        every { sharedPreferencesEditor.apply() } just Runs

        testObject = AuthManager(context = context, config = config)

        verifySequence {
            encryptedSharedPreferences.edit()
            sharedPreferencesEditor.putString(USER_UUID, null)
            sharedPreferencesEditor.apply()
            encryptedSharedPreferences.edit()
            sharedPreferencesEditor.putString(CACHED_ACCESS_TOKEN, null)
            sharedPreferencesEditor.apply()
            encryptedSharedPreferences.edit()
            sharedPreferencesEditor.putString(CACHED_REFRESH_TOKEN, null)
            sharedPreferencesEditor.apply()
        }
    }


    @Test
    fun `deleteSession clears items`() {
        val config =
            ArcXPCommerceConfig.Builder()
                .setBaseUrl(configBaseUrl)
                .enableAutoCache(enable = true)
                .setContext(context = context)
                .build()
        every { envSharedPrefs.getBoolean(REMEMBER_USER, false) } returns true
        every { encryptedSharedPreferences.getString(USER_UUID, null) } returns uuid
        every {
            encryptedSharedPreferences.getString(
                CACHED_ACCESS_TOKEN,
                null
            )
        } returns accessToken
        every {
            encryptedSharedPreferences.getString(
                CACHED_REFRESH_TOKEN,
                null
            )
        } returns refreshToken
        every { encryptedSharedPreferences.edit() } returns sharedPreferencesEditor
        every { sharedPreferencesEditor.putString(any(), any()) } returns sharedPreferencesEditor
        every { sharedPreferencesEditor.apply() } just Runs
        testObject = AuthManager(context = context, config = config)
        assertEquals(uuid, testObject.uuid)
        assertEquals(accessToken, testObject.accessToken)
        assertEquals(refreshToken, testObject.refreshToken)

        testObject.deleteSession()

        assertNull(testObject.uuid)
        assertNull(testObject.accessToken)
        assertNull(testObject.refreshToken)
    }

    @Test
    fun `redefineEnvironment with non null region`() {
        val region = "region"
        val environment = Constants.EnvironmentType.ENV_SAND_BOX
        val config =
            ArcXPCommerceConfig.Builder()
                .setBaseUrl(configBaseUrl)
                .setBaseRetailUrl(baseRetailUrl)
                .setBaseSalesUrl(baseSalesUrl)
                .setContext(context = context)
                .build()
        every { envSharedPrefs.getBoolean(REMEMBER_USER, false) } returns false
        testObject = AuthManager(context = context, config = config)
        every { configSharedPreferences.edit() } returns sharedPreferencesEditor
        every { sharedPreferencesEditor.putString(any(), any()) } returns sharedPreferencesEditor
        every { sharedPreferencesEditor.apply() } just Runs

        testObject.redefineEnvironment(env = environment, reg = region)

        verifySequence {
            configSharedPreferences.edit()
            sharedPreferencesEditor.putString(CUR_ENVIRONMENT, environment.name)
            sharedPreferencesEditor.apply()
            configSharedPreferences.edit()
            sharedPreferencesEditor.putString(REGION, region)
            sharedPreferencesEditor.apply()
        }
        assertEquals(environment.name, testObject.getCurrentEnvironment())
        assertEquals(region, testObject.getRegion())
    }

    @Test
    fun `redefineEnvironment with null region`() {
        val environment = Constants.EnvironmentType.ENV_STAGING
        val config =
            ArcXPCommerceConfig.Builder()
                .setBaseUrl(configBaseUrl)
                .setBaseRetailUrl(baseRetailUrl)
                .setBaseSalesUrl(baseSalesUrl)
                .setContext(context = context)
                .build()
        every { envSharedPrefs.getBoolean(REMEMBER_USER, false) } returns false
        every { configSharedPreferences.edit() } returns sharedPreferencesEditor
        every { sharedPreferencesEditor.putString(any(), any()) } returns sharedPreferencesEditor
        every { sharedPreferencesEditor.apply() } just Runs
        testObject = AuthManager(context = context, config = config)

        testObject.redefineEnvironment(env = environment)

        verifySequence {
            configSharedPreferences.edit()
            sharedPreferencesEditor.putString(CUR_ENVIRONMENT, environment.name)
            sharedPreferencesEditor.apply()
        }
    }

    @Test
    fun `cacheSession when auto cache`() {
        val response = mockk<ArcXPAuth>()
        every { response.uuid } returns uuid
        every { response.accessToken } returns accessToken
        every { response.refreshToken } returns refreshToken
        val config =
            ArcXPCommerceConfig.Builder()
                .setBaseUrl(configBaseUrl)
                .setBaseRetailUrl(baseRetailUrl)
                .setBaseSalesUrl(baseSalesUrl)
                .enableAutoCache(enable = true)
                .setContext(context = context)
                .build()
        every { envSharedPrefs.getBoolean(REMEMBER_USER, false) } returns false
        every { encryptedSharedPreferences.getString(USER_UUID, null) } returns uuid
        every {
            encryptedSharedPreferences.getString(
                CACHED_ACCESS_TOKEN,
                null
            )
        } returns accessToken
        every {
            encryptedSharedPreferences.getString(
                CACHED_REFRESH_TOKEN,
                null
            )
        } returns refreshToken
        every { encryptedSharedPreferences.edit() } returns sharedPreferencesEditor
        every { sharedPreferencesEditor.putString(any(), any()) } returns sharedPreferencesEditor
        every { sharedPreferencesEditor.apply() } just Runs

        testObject = AuthManager(context = context, config = config)

        assertNull(testObject.uuid)
        assertNull(testObject.accessToken)
        assertNull(testObject.refreshToken)

        every { encryptedSharedPreferences.edit() } returns sharedPreferencesEditor
        every { sharedPreferencesEditor.putString(any(), any()) } returns sharedPreferencesEditor
        every { sharedPreferencesEditor.apply() } just Runs

        clearAllMocks(answers = false)
        testObject.cacheSession(response = response)

        assertEquals(uuid, testObject.uuid)
        assertEquals(accessToken, testObject.accessToken)
        assertEquals(refreshToken, testObject.refreshToken)


        verifySequence {
            encryptedSharedPreferences.edit()
            sharedPreferencesEditor.putString(USER_UUID, uuid)
            sharedPreferencesEditor.apply()
            encryptedSharedPreferences.edit()
            sharedPreferencesEditor.putString(CACHED_ACCESS_TOKEN, accessToken)
            sharedPreferencesEditor.apply()
            encryptedSharedPreferences.edit()
            sharedPreferencesEditor.putString(CACHED_REFRESH_TOKEN, refreshToken)
            sharedPreferencesEditor.apply()
        }


    }

    @Test
    fun `cacheSession when not auto cache`() {
        val response = mockk<ArcXPAuth>()
        every { response.uuid } returns uuid
        every { response.accessToken } returns accessToken
        every { response.refreshToken } returns refreshToken
        val config =
            ArcXPCommerceConfig.Builder()
                .setBaseUrl(configBaseUrl)
                .setBaseRetailUrl(baseRetailUrl)
                .setBaseSalesUrl(baseSalesUrl)
                .enableAutoCache(enable = false)
                .setContext(context = context)
                .build()
        every { envSharedPrefs.getBoolean(REMEMBER_USER, false) } returns false
        every { encryptedSharedPreferences.getString(USER_UUID, null) } returns uuid
        every {
            encryptedSharedPreferences.getString(
                CACHED_ACCESS_TOKEN,
                null
            )
        } returns accessToken
        every {
            encryptedSharedPreferences.getString(
                CACHED_REFRESH_TOKEN,
                null
            )
        } returns refreshToken
        every { encryptedSharedPreferences.edit() } returns sharedPreferencesEditor
        every { sharedPreferencesEditor.putString(any(), any()) } returns sharedPreferencesEditor
        every { sharedPreferencesEditor.apply() } just Runs

        testObject = AuthManager(context = context, config = config)

        assertNull(testObject.uuid)
        assertNull(testObject.accessToken)
        assertNull(testObject.refreshToken)

        every { encryptedSharedPreferences.edit() } returns sharedPreferencesEditor
        every { sharedPreferencesEditor.putString(any(), any()) } returns sharedPreferencesEditor
        every { sharedPreferencesEditor.apply() } just Runs

        clearAllMocks(answers = false)
        testObject.cacheSession(response = response)

        assertEquals(uuid, testObject.uuid)
        assertEquals(accessToken, testObject.accessToken)
        assertEquals(refreshToken, testObject.refreshToken)


        verify {
            encryptedSharedPreferences wasNot called
        }
    }

    @Test
    fun `cacheSession one time access when auto cache`() {
        val response = mockk<ArcXPOneTimeAccessLinkAuth>()
        every { response.uuid } returns uuid
        every { response.accessToken } returns accessToken
        val config =
            ArcXPCommerceConfig.Builder()
                .setBaseUrl(configBaseUrl)
                .setBaseRetailUrl(baseRetailUrl)
                .setBaseSalesUrl(baseSalesUrl)
                .enableAutoCache(enable = true)
                .setContext(context = context)
                .build()
        every { envSharedPrefs.getBoolean(REMEMBER_USER, false) } returns false
        every { encryptedSharedPreferences.getString(USER_UUID, null) } returns uuid
        every {
            encryptedSharedPreferences.getString(
                CACHED_ACCESS_TOKEN,
                null
            )
        } returns accessToken
        every { encryptedSharedPreferences.edit() } returns sharedPreferencesEditor
        every { sharedPreferencesEditor.putString(any(), any()) } returns sharedPreferencesEditor
        every { sharedPreferencesEditor.apply() } just Runs

        testObject = AuthManager(context = context, config = config)

        assertNull(testObject.uuid)
        assertNull(testObject.accessToken)


        every { encryptedSharedPreferences.edit() } returns sharedPreferencesEditor
        every { sharedPreferencesEditor.putString(any(), any()) } returns sharedPreferencesEditor
        every { sharedPreferencesEditor.apply() } just Runs

        clearAllMocks(answers = false)
        testObject.cacheSession(response = response)

        assertEquals(uuid, testObject.uuid)
        assertEquals(accessToken, testObject.accessToken)


        verifySequence {
            encryptedSharedPreferences.edit()
            sharedPreferencesEditor.putString(USER_UUID, uuid)
            sharedPreferencesEditor.apply()
            encryptedSharedPreferences.edit()
            sharedPreferencesEditor.putString(CACHED_ACCESS_TOKEN, accessToken)
            sharedPreferencesEditor.apply()
        }
    }

    @Test
    fun `cacheSession one time access when not auto cache`() {
        val response = mockk<ArcXPOneTimeAccessLinkAuth>()
        every { response.uuid } returns uuid
        every { response.accessToken } returns accessToken
        val config =
            ArcXPCommerceConfig.Builder()
                .setBaseUrl(configBaseUrl)
                .setBaseRetailUrl(baseRetailUrl)
                .setBaseSalesUrl(baseSalesUrl)
                .enableAutoCache(enable = false)
                .setContext(context = context)
                .build()
        every { envSharedPrefs.getBoolean(REMEMBER_USER, false) } returns false
        every { encryptedSharedPreferences.getString(USER_UUID, null) } returns uuid
        every {
            encryptedSharedPreferences.getString(
                CACHED_ACCESS_TOKEN,
                null
            )
        } returns accessToken
        every { encryptedSharedPreferences.edit() } returns sharedPreferencesEditor
        every { sharedPreferencesEditor.putString(any(), any()) } returns sharedPreferencesEditor
        every { sharedPreferencesEditor.apply() } just Runs

        testObject = AuthManager(context = context, config = config)

        assertNull(testObject.uuid)
        assertNull(testObject.accessToken)

        every { encryptedSharedPreferences.edit() } returns sharedPreferencesEditor
        every { sharedPreferencesEditor.putString(any(), any()) } returns sharedPreferencesEditor
        every { sharedPreferencesEditor.apply() } just Runs

        clearAllMocks(answers = false)
        testObject.cacheSession(response = response)

        assertEquals(uuid, testObject.uuid)
        assertEquals(accessToken, testObject.accessToken)

        verify {
            encryptedSharedPreferences wasNot called
        }
    }

    @Test
    fun `setShouldRememberUser works as expected`() {
        val config =
            ArcXPCommerceConfig.Builder()
                .setContext(context = context)
                .build()
        every { envSharedPrefs.getBoolean(REMEMBER_USER, false) } returns false
        testObject = AuthManager(context = context, config = config)
        every { envSharedPrefs.edit() } returns sharedPreferencesEditor
        every { sharedPreferencesEditor.putBoolean(any(), any()) } returns sharedPreferencesEditor
        every { sharedPreferencesEditor.apply() } just Runs
        clearAllMocks(answers = false)

        testObject.setShouldRememberUser(true)

        verifySequence {
            envSharedPrefs.edit()
            sharedPreferencesEditor.putBoolean(REMEMBER_USER, true)
            sharedPreferencesEditor.apply()
        }
    }

    @Test
    fun `setConfig works as expected when arcConfig is populated`() {
        val config =
            ArcXPCommerceConfig.Builder()
                .setContext(context = context)
                .build()
        every { envSharedPrefs.getBoolean(REMEMBER_USER, false) } returns false
        testObject = AuthManager(context = context, config = config)
        every { configSharedPreferences.edit() } returns sharedPreferencesEditor
        every { sharedPreferencesEditor.putString(any(), any()) } returns sharedPreferencesEditor
        every { sharedPreferencesEditor.apply() } just Runs
        clearAllMocks(answers = false)
        val expected = ArcXPConfig(
            facebookAppId = "id",
            googleClientId = null,
            orgTenants = null,
            signupRecaptcha = null,
            signinRecaptcha = null,
            magicLinkRecapatcha = null,
            recaptchaSiteKey = null,
            pwMinLength = null,
            pwSpecialCharacters = null,
            pwUppercase = null,
            pwLowercase = null,
            disqus = null,
            teamId = null,
            keyId = null,
            urlToReceiveAuthToken = null,
            pwPwNumbers = 123
        )
        val json = Gson().toJson(expected)
        testObject.setConfig(response = expected)

        verifySequence {
            configSharedPreferences.edit()
            sharedPreferencesEditor.putString(USER_CONFIG, json)
            sharedPreferencesEditor.apply()
        }
        every { configSharedPreferences.getString(USER_CONFIG, null) } returns json


        assertNull(testObject.getConfig())

    }

    @Test
    fun `setConfig works as expected when arcConfig null and shared prefs returns value`() {
        val config =
            ArcXPCommerceConfig.Builder()
                .setContext(context = context)
                .build()
        every { envSharedPrefs.getBoolean(REMEMBER_USER, false) } returns false
        testObject = AuthManager(context = context, config = config)
        every { configSharedPreferences.edit() } returns sharedPreferencesEditor
        every { sharedPreferencesEditor.putString(any(), any()) } returns sharedPreferencesEditor
        every { sharedPreferencesEditor.apply() } just Runs
        clearAllMocks(answers = false)
        val expected = ArcXPConfig(
            facebookAppId = "id",
            googleClientId = null,
            orgTenants = null,
            signupRecaptcha = null,
            signinRecaptcha = null,
            magicLinkRecapatcha = null,
            recaptchaSiteKey = null,
            pwMinLength = null,
            pwSpecialCharacters = null,
            pwUppercase = null,
            pwLowercase = null,
            disqus = null,
            teamId = null,
            keyId = null,
            urlToReceiveAuthToken = null,
            pwPwNumbers = 123
        )
        val json = Gson().toJson(expected)
        every { configSharedPreferences.getString(USER_CONFIG, null) } returns json
        val actual = testObject.getConfig()

        assertEquals(expected, actual)

    }

    @Test
    fun `getConfig works as expected when arcConfig is null from prefs`() {
        val config =
            ArcXPCommerceConfig.Builder()
                .setContext(context = context)
                .build()
        every { envSharedPrefs.getBoolean(REMEMBER_USER, false) } returns false
        testObject = AuthManager(context = context, config = config)
        every { configSharedPreferences.edit() } returns sharedPreferencesEditor
        every { sharedPreferencesEditor.putString(any(), any()) } returns sharedPreferencesEditor
        every { sharedPreferencesEditor.apply() } just Runs
        clearAllMocks(answers = false)

        every { configSharedPreferences.getString(USER_CONFIG, null) } returns null


        assertNull(testObject.getConfig())

    }

    @Test
    fun `loadLocalConfig returns value from shared pref`() {
        //TODO hmm this disregards param if in shared prefs?
        val config =
            ArcXPCommerceConfig.Builder()
                .setContext(context = context)
                .build()
        every { envSharedPrefs.getBoolean(REMEMBER_USER, false) } returns false
        testObject = AuthManager(context = context, config = config)
        every { configSharedPreferences.edit() } returns sharedPreferencesEditor
        every { sharedPreferencesEditor.putString(any(), any()) } returns sharedPreferencesEditor
        every { sharedPreferencesEditor.apply() } just Runs
        clearAllMocks(answers = false)
        val expected = ArcXPConfig(
            facebookAppId = facebookAppId,
            googleClientId = googleClientId,
            signupRecaptcha = signupRecaptcha,
            signinRecaptcha = signinRecaptcha,
            recaptchaSiteKey = recaptchaSiteKey,
            magicLinkRecapatcha = magicLinkRecapatcha,
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
        val input = ArcXPCommerceConfig.Builder()
            .setContext(context = context)
            .setFacebookAppId(id = facebookAppId)
            .setGoogleClientId(id = googleClientId)
            .enableRecaptchaForSignup(enable = signupRecaptcha)
            .enableRecaptchaForSignin(enable = signinRecaptcha)
            .setRecaptchaSiteKey(key = recaptchaSiteKey)
            .enableRecaptchaForOneTimeAccess(enable = magicLinkRecapatcha)
            .build()
        val json = Gson().toJson(expected)
        every { configSharedPreferences.getString(USER_CONFIG, null) } returns json
        testObject = spyk(testObject)
        testObject.loadLocalConfig(config = input)



        verifySequence {
            configSharedPreferences.getString(USER_CONFIG, null)
            configSharedPreferences.edit()
            sharedPreferencesEditor.putString(USER_CONFIG, json)
            sharedPreferencesEditor.apply()

        }

        assertEquals(expected, testObject.getConfigTest())

    }

    @Test
    fun `loadLocalConfig returns value from input`() {
        val config =
            ArcXPCommerceConfig.Builder()
                .setContext(context = context)
                .build()
        every { envSharedPrefs.getBoolean(REMEMBER_USER, false) } returns false
        AuthManager.getInstance(
            context = context,
            clientCachedData = clientCachedData,
            config = config
        )
        every { configSharedPreferences.edit() } returns sharedPreferencesEditor
        every { sharedPreferencesEditor.putString(any(), any()) } returns sharedPreferencesEditor
        every { sharedPreferencesEditor.apply() } just Runs
        clearAllMocks(answers = false)
        val expected = ArcXPConfig(
            facebookAppId = facebookAppId,
            googleClientId = googleClientId,
            signupRecaptcha = signupRecaptcha,
            signinRecaptcha = signinRecaptcha,
            recaptchaSiteKey = recaptchaSiteKey,
            magicLinkRecapatcha = magicLinkRecapatcha,
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
        val input = ArcXPCommerceConfig.Builder()
            .setContext(context = context)
            .setFacebookAppId(id = facebookAppId)
            .setGoogleClientId(id = googleClientId)
            .enableRecaptchaForSignup(enable = signupRecaptcha)
            .enableRecaptchaForSignin(enable = signinRecaptcha)
            .setRecaptchaSiteKey(key = recaptchaSiteKey)
            .enableRecaptchaForOneTimeAccess(enable = magicLinkRecapatcha)
            .build()
        val json = Gson().toJson(expected)
        every { configSharedPreferences.getString(USER_CONFIG, null) } returns null
        AuthManager.getInstance().loadLocalConfig(config = input)



        verifySequence {
            configSharedPreferences.getString(USER_CONFIG, null)
            configSharedPreferences.edit()
            sharedPreferencesEditor.putString(USER_CONFIG, json)
            sharedPreferencesEditor.apply()

        }

        assertEquals(expected, AuthManager.getInstance().getConfigTest())

    }
}
