package com.arcxp.commerce

import android.app.Application
import android.util.Log
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.arcxp.commerce.apimanagers.IdentityApiManager
import com.arcxp.commerce.apimanagers.RetailApiManager
import com.arcxp.commerce.apimanagers.SalesApiManager
import com.arcxp.commerce.callbacks.ArcXPIdentityListener
import com.arcxp.commerce.models.ArcXPConfig
import com.arcxp.commerce.paywall.PaywallManager
import com.arcxp.commerce.util.AuthManager
import com.arcxp.commons.util.Constants.SDK_TAG
import com.arcxp.commons.util.DependencyFactory
import com.arcxp.sdk.R
import com.facebook.CallbackManager
import io.mockk.*
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.robolectric.RuntimeEnvironment.application
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ArcXPCommerceManagerTest {

    @get:Rule
    var instantExecuteRule = InstantTaskExecutorRule()

    @RelaxedMockK
    private lateinit var application: Application

    @RelaxedMockK
    private lateinit var config: ArcXPCommerceConfig

    @MockK
    private lateinit var clientCachedData: Map<String, String>

    @MockK
    private lateinit var callbackManager: CallbackManager

    @RelaxedMockK
    private lateinit var identityApiManager: IdentityApiManager

    @MockK
    private lateinit var salesApiManager: SalesApiManager

    @MockK
    private lateinit var retailApiManager: RetailApiManager

    @MockK
    private lateinit var paywallManager: PaywallManager

    @RelaxedMockK
    private lateinit var authManager: AuthManager

    private val facebookAppId = "a"
    private val googleClientId = "b"
    private val recaptchaForSignup = true
    private val recaptchaForSignin = true
    private val recaptchaSiteKey = "e"
    private val recaptchaForOneTimeAccess = true
    private val pwLowercase = 1
    private val pwMinLength = 2
    private val pwPwNumbers = 3
    private val pwSpecialCharacters = 4
    private val pwUppercase = 8


    private lateinit var testObject: ArcXPCommerceManager

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        mockkObject(AuthManager)
        every {
            AuthManager.getInstance(
                context = any(),
                clientCachedData = any(),
                config = any()
            )
        } returns authManager
        every { AuthManager.getInstance() } returns authManager
        mockkObject(DependencyFactory)
        every { DependencyFactory.createCallBackManager() } returns callbackManager
        every { DependencyFactory.createIdentityApiManager(authManager = authManager) } returns identityApiManager
        every { DependencyFactory.createSalesApiManager() } returns salesApiManager
        every { DependencyFactory.createRetailApiManager() } returns retailApiManager
        every {
            DependencyFactory.createPaywallManager(
                application = application,
                retailApiManager = retailApiManager,
                salesApiManager = salesApiManager
            )
        } returns paywallManager
        every { config.facebookAppId } returns facebookAppId
        every { config.googleClientId } returns googleClientId
        every { config.recaptchaForSignup } returns recaptchaForSignup
        every { config.recaptchaForSignin } returns recaptchaForSignin
        every { config.recaptchaSiteKey } returns recaptchaSiteKey
        every { config.recaptchaForOneTimeAccess } returns recaptchaForOneTimeAccess
        every { config.pwLowercase } returns pwLowercase
        every { config.pwMinLength } returns pwMinLength
        every { config.pwPwNumbers } returns pwPwNumbers
        every { config.pwSpecialCharacters } returns pwSpecialCharacters
        every { config.pwUppercase } returns pwUppercase


    }

    @After
    fun tearDown() {
        unmockkObject(AuthManager)
        unmockkObject(DependencyFactory)
    }

    @Test
    fun `create without using local config calls identity api manager to load config with listeners`() {
        every { config.useLocalConfig } returns false

        testObject = ArcXPCommerceManager.initialize(
            context = application,
            clientCachedData = clientCachedData,
            config = config
        )

        val slot = slot<ArcXPIdentityListener>()
        verify(exactly = 1) {
            identityApiManager.loadConfig(listener = capture(slot))
        }
        val result = mockk<ArcXPConfig>()
        val fbid = "fbid"
        val googid = "googid"
        val expectedSuccessLog = "success"
        val expectedFailLog = "fail"
        every { result.facebookAppId } returns fbid
        every { result.googleClientId } returns googid
        every {
            application.getString(
                R.string.remote_tenet_config_loaded,
                fbid,
                googid
            )
        } returns expectedSuccessLog
        every { application.getString(R.string.tenet_loaded_from_cache) } returns expectedFailLog

        clearAllMocks(answers = false)

        slot.captured.onLoadConfigSuccess(result = result)
        verifySequence {
            AuthManager.getInstance()
            authManager.setConfig(response = result)
            Log.i(SDK_TAG, expectedSuccessLog)
        }
        clearAllMocks(answers = false)

        slot.captured.onLoadConfigFailure(error = mockk())
        verifySequence {
            AuthManager.getInstance()
            authManager.loadLocalConfig(config = config)
            Log.i(SDK_TAG, expectedFailLog)
        }
    }

    @Test
    fun `create with using local config calls auth manager to set ArcXPConfig from param`() {
        every { config.useLocalConfig } returns true
        val expectedLog = "expected"
        every { application.getString(R.string.local_tenet_loaded)} returns expectedLog
        mockkStatic(Log::class)

        testObject = ArcXPCommerceManager.initialize(
            context = application,
            clientCachedData = clientCachedData,
            config = config
        )
        val slot = slot<ArcXPConfig>()

        verify(exactly = 1) {
            authManager.setConfig(response = capture(slot))
            Log.i(SDK_TAG, expectedLog)
        }
        val result = slot.captured

        assertEquals(facebookAppId, result.facebookAppId)
        assertEquals(googleClientId, result.googleClientId)
        assertEquals(recaptchaForSignup, result.signupRecaptcha)
        assertEquals(recaptchaForSignin, result.signinRecaptcha)
        assertEquals(recaptchaSiteKey, result.recaptchaSiteKey)
        assertEquals(recaptchaForOneTimeAccess, result.magicLinkRecapatcha)
        assertNull(result.disqus)
        assertNull(result.keyId)
        assertNull(result.orgTenants)
        assertEquals(pwLowercase, result.pwLowercase)
        assertEquals(pwMinLength, result.pwMinLength)
        assertEquals(pwPwNumbers, result.pwPwNumbers)
        assertEquals(pwSpecialCharacters, result.pwSpecialCharacters)
        assertEquals(pwUppercase, result.pwUppercase)
        assertNull(result.teamId)
        assertNull(result.urlToReceiveAuthToken)
    }
}