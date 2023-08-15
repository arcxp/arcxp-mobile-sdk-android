package com.arcxp.commerce

import android.app.Application
import android.content.Intent
import android.util.Log
import androidx.activity.result.IntentSenderRequest
import androidx.appcompat.app.AppCompatActivity
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import com.arcxp.commerce.apimanagers.IdentityApiManager
import com.arcxp.commerce.apimanagers.RetailApiManager
import com.arcxp.commerce.apimanagers.SalesApiManager
import com.arcxp.commerce.callbacks.ArcXPIdentityListener
import com.arcxp.commerce.callbacks.ArcXPRetailListener
import com.arcxp.commerce.callbacks.ArcXPSalesListener
import com.arcxp.commerce.extendedModels.ArcXPProfileManage
import com.arcxp.commerce.models.ArcXPAddressRequest
import com.arcxp.commerce.models.ArcXPAttributeRequest
import com.arcxp.commerce.models.ArcXPAuth
import com.arcxp.commerce.models.ArcXPAuthRequest
import com.arcxp.commerce.models.ArcXPConfig
import com.arcxp.commerce.models.ArcXPContactRequest
import com.arcxp.commerce.models.ArcXPEntitlements
import com.arcxp.commerce.models.ArcXPIdentity
import com.arcxp.commerce.models.ArcXPOneTimeAccessLink
import com.arcxp.commerce.models.ArcXPProfilePatchRequest
import com.arcxp.commerce.models.ArcXPUpdateProfileRequest
import com.arcxp.commerce.models.ArcXPUser
import com.arcxp.commerce.paywall.PaywallManager
import com.arcxp.commerce.util.AuthManager
import com.arcxp.commons.throwables.ArcXPException
import com.arcxp.commons.throwables.ArcXPSDKErrorType
import com.arcxp.commons.util.Constants.SDK_TAG
import com.arcxp.commons.util.DependencyFactory
import com.arcxp.commons.util.Either
import com.arcxp.commons.util.Failure
import com.arcxp.commons.util.Success
import com.arcxp.sdk.R
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.facebook.login.widget.LoginButton
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.BeginSignInResult
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import io.mockk.MockKAnnotations
import io.mockk.called
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.runs
import io.mockk.slot
import io.mockk.spyk
import io.mockk.unmockkStatic
import io.mockk.verify
import io.mockk.verifySequence
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.Calendar
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ArcXPCommerceManagerTest {

    @get:Rule
    var instantExecuteRule = InstantTaskExecutorRule()

    @RelaxedMockK
    private lateinit var application: Application

    @RelaxedMockK
    private lateinit var activity: AppCompatActivity

    @RelaxedMockK
    private lateinit var config: ArcXPCommerceConfig

    @MockK
    private lateinit var clientCachedData: Map<String, String>

    @RelaxedMockK
    private lateinit var callbackManager: CallbackManager

    @RelaxedMockK
    private lateinit var identityApiManager: IdentityApiManager

    @RelaxedMockK
    private lateinit var salesApiManager: SalesApiManager

    @RelaxedMockK
    private lateinit var retailApiManager: RetailApiManager

    @RelaxedMockK
    private lateinit var paywallManager: PaywallManager

    @RelaxedMockK
    private lateinit var authManager: AuthManager

    @MockK
    private lateinit var authResponse: ArcXPAuth

    @RelaxedMockK
    private lateinit var error: ArcXPException

    @RelaxedMockK
    private lateinit var finalError: ArcXPException

    @RelaxedMockK
    private lateinit var loginLiveData: MutableLiveData<Either<ArcXPException, ArcXPAuth>>

    @RelaxedMockK
    private lateinit var listener: ArcXPIdentityListener

    @RelaxedMockK
    private lateinit var salesListener: ArcXPSalesListener

    @RelaxedMockK
    private lateinit var retailListener: ArcXPRetailListener

    @RelaxedMockK
    private lateinit var pageViewListener: ArcXPPageviewListener

    @RelaxedMockK
    private lateinit var googleSignInClient: GoogleSignInClient

    @RelaxedMockK
    private lateinit var oneTapClient: SignInClient

    @RelaxedMockK
    private lateinit var googleSignInTask: Task<BeginSignInResult>

    @RelaxedMockK
    private lateinit var beginSignInRequestBuilder: BeginSignInRequest.Builder

    @RelaxedMockK
    private lateinit var beginSignInRequest: BeginSignInRequest

    @RelaxedMockK
    private lateinit var beginSignInRequestPasswordRequestBuilder: BeginSignInRequest.PasswordRequestOptions.Builder

    @RelaxedMockK
    private lateinit var beginSignInRequestPasswordRequest: BeginSignInRequest.PasswordRequestOptions

    @MockK
    private lateinit var beginSignInRequestGoogleIDTokenBuilder: BeginSignInRequest.GoogleIdTokenRequestOptions.Builder

    @RelaxedMockK
    private lateinit var beginSignInRequestGoogleIDTokenRequest: BeginSignInRequest.GoogleIdTokenRequestOptions

    @RelaxedMockK
    private lateinit var loginWithGoogleResultsReceiver: LoginWithGoogleResultsReceiver

    @RelaxedMockK
    private lateinit var loginWithGoogleOneTapResultsReceiver: LoginWithGoogleOneTapResultsReceiver

    @RelaxedMockK
    private lateinit var signInIntent: Intent

    @RelaxedMockK
    private lateinit var entitlements: ArcXPEntitlements

//    @RelaxedMockK
//    private lateinit var intentSenderRequest: IntentSenderRequest

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
    private val email = "email"
    private val userName = "userName"
    private val firstName = "first"
    private val lastName = "last"
    private val password = "pw"
    private val token = "token"
    private val nonce = "nonce"
    private val googleKey = "gkey"
    private val loginTag = "LoginWithGoogle"
    private val expectedErrorMessage = "expected"
    private val grantType = "grantType"
    private val id = "id"
    private val pid = "pid"
    private val refreshToken = "refreshToken"
    private val pageId = "pageId"
    private val contentType = "contentType"
    private val contentSection = "contentSection"
    private val deviceClass = "deviceClass"
    private val addressLine1 = "line1"
    private val addressLine2 = "line2"
    private val addressLocality = "locality"
    private val addressRegion = "region"
    private val addressPostal = "Postal"
    private val addressCountry = "addressCountry"
    private val addressType = "addressType"
    private val orderNumber = "order#"
    private val mid = "mid"
    private val phone = "phone"
    private val browserInfo = "browserInfo"
    private val userCancelledLoginError = "User cancelled login"
    private val requestCode = 1234
    private val resultCode = 5678

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
        every { authManager.refreshToken } returns refreshToken
        every { AuthManager.getInstance() } returns authManager
        mockkObject(DependencyFactory)
        every { DependencyFactory.createCallBackManager() } returns callbackManager
        every { DependencyFactory.createIdentityApiManager(authManager = authManager) } returns identityApiManager
        every { DependencyFactory.createSalesApiManager() } returns salesApiManager
        every { DependencyFactory.createRetailApiManager() } returns retailApiManager
        every { DependencyFactory.createLiveData<Either<ArcXPException, ArcXPAuth>>() } returns loginLiveData
        every { DependencyFactory.createGoogleSignInClient(application = application) } returns googleSignInClient
        every {
            DependencyFactory.createPaywallManager(
                application = application,
                retailApiManager = retailApiManager,
                salesApiManager = salesApiManager
            )
        } returns paywallManager
        every {
            DependencyFactory.createLoginWithGoogleResultsReceiver(
                signInIntent = signInIntent,
                manager = any(),
                listener = any()
            )
        } returns loginWithGoogleResultsReceiver
        every {
            DependencyFactory.createLoginWithGoogleOneTapResultsReceiver(
                signInIntent = any(),
                manager = any(),
                listener = any()
            )
        } returns loginWithGoogleOneTapResultsReceiver
        mockkStatic(Identity::class)
        every { Identity.getSignInClient(application) } returns oneTapClient
        mockkStatic(BeginSignInRequest::class)
        every { BeginSignInRequest.builder() } returns beginSignInRequestBuilder
        beginSignInRequestBuilder.apply {
            every { setPasswordRequestOptions(beginSignInRequestPasswordRequest) } returns this
            every { setGoogleIdTokenRequestOptions(beginSignInRequestGoogleIDTokenRequest) } returns this
            every { setAutoSelectEnabled(false) } returns this
            every { build() } returns beginSignInRequest
        }
        mockkStatic(BeginSignInRequest.PasswordRequestOptions::class)
        every { BeginSignInRequest.PasswordRequestOptions.builder() } returns beginSignInRequestPasswordRequestBuilder
        beginSignInRequestPasswordRequestBuilder.apply {
            every { setSupported(true) } returns this
            every { build() } returns beginSignInRequestPasswordRequest
        }
        mockkStatic(BeginSignInRequest.GoogleIdTokenRequestOptions::class)
        every { BeginSignInRequest.GoogleIdTokenRequestOptions.builder() } returns beginSignInRequestGoogleIDTokenBuilder
        beginSignInRequestGoogleIDTokenBuilder.apply {
            every { setSupported(true) } returns this
            every { setServerClientId(googleKey) } returns this
            every { setFilterByAuthorizedAccounts(true) } returns this
            every { build() } returns beginSignInRequestGoogleIDTokenRequest
        }
        every { googleSignInClient.signInIntent } returns signInIntent
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
        every { config.googleOneTapEnabled } returns true
        every { activity.getString(R.string.google_key) } returns googleKey
        every { application.getString(R.string.google_key) } returns googleKey
        every { application.getString(R.string.google_login_fragment_tag) } returns loginTag
        every { application.getString(R.string.user_cancelled_login_error) } returns userCancelledLoginError
        every { oneTapClient.beginSignIn(beginSignInRequest) } returns googleSignInTask
        googleSignInTask.apply {
            every { addOnSuccessListener(activity, any()) } returns this
            every { addOnFailureListener(activity, any()) } returns this
        }

    }

    @After
    fun tearDown() {
        clearAllMocks()
        testObject.reset()
    }

    @Test
    fun `create without using local config calls identity api manager to load config with listeners`() {
        every { config.useLocalConfig } returns false
        initializeTestObject()

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
        every { application.getString(R.string.local_tenet_loaded) } returns expectedLog
        mockkStatic(Log::class)

        initializeTestObject()
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

    @Test
    fun `login with recaptcha non null sitekey`() {
        every { config.recaptchaForSignin } returns true
        initializeTestObject()
        val resultListener = slot<ArcXPIdentityListener>()
        testObject = spyk(testObject)
        testObject.login(email = email, password = password, listener = listener)

        verify(exactly = 1) {
            testObject.runRecaptcha(listener = capture(resultListener))
        }
        val commerceConfig = testObject.commerceConfig
        verify(exactly = 1) {
            identityApiManager.checkRecaptcha(
                config = commerceConfig,
                arcIdentityListener = resultListener.captured
            )
        }
        //recaptcha success

        clearAllMocks(answers = false)
        resultListener.captured.onRecaptchaSuccess(token = token)
        val recaptchaListener = slot<ArcXPIdentityListener>()


        verifySequence {
            identityApiManager.setRecaptchaToken(token)
            identityApiManager.login(
                username = email,
                password = password,
                listener = capture(recaptchaListener)
            )
        }
        clearAllMocks(answers = false)
        recaptchaListener.captured.onLoginSuccess(response = authResponse)
        verifySequence {
            listener.onLoginSuccess(response = authResponse)
            loginLiveData.postValue(Success(authResponse))
        }

        clearAllMocks(answers = false)
        recaptchaListener.captured.onLoginError(error = error)
        verifySequence {
            listener.onLoginError(error = error)
            loginLiveData.postValue(Failure(error))
        }

        //recaptcha failure
        val expectedError = mockk<ArcXPException>()
        val errorMsg = "error"
        every { application.getString(R.string.recaptcha_error_login) } returns errorMsg
        every {
            DependencyFactory.createArcXPException(
                type = ArcXPSDKErrorType.LOGIN_ERROR,
                message = errorMsg,
                value = error
            )
        } returns expectedError
        resultListener.captured.onRecaptchaFailure(error = error)
        verify(exactly = 1) {
            listener.onLoginError(error = expectedError)
        }

        resultListener.captured.onRecaptchaCancel()

    }

    @Test
    fun `login with recaptcha null sitekey`() {
        every { config.recaptchaSiteKey } returns null
        every { config.recaptchaForSignin } returns true
        val expectedError = "err"
        every { application.getString(R.string.recaptchaSiteKey_error) } returns expectedError
        initializeTestObject()
        testObject = spyk(testObject)
        testObject.login(email = email, password = password, listener = listener)


        verify(exactly = 1) {
            testObject.runRecaptcha(listener = any())
        }
        //note: can't spy on the created listener here so can't verify the method was called.. but the method runs this though:
        verify {
            DependencyFactory.createArcXPException(
                ArcXPSDKErrorType.RECAPTCHA_ERROR,
                expectedError,
                null
            )
        }

    }

    @Test
    fun `login with recaptcha blank sitekey`() {
        every { config.recaptchaSiteKey } returns " "
        every { config.recaptchaForSignin } returns true
        val expectedError = "err"
        every { application.getString(R.string.recaptchaSiteKey_error) } returns expectedError
        initializeTestObject()
        testObject = spyk(testObject)
        testObject.login(email = email, password = password, listener = listener)


        verify(exactly = 1) {
            testObject.runRecaptcha(listener = any())
        }
        //note: can't spy on the created listener here so can't verify the method was called.. but the method runs this though:
        verify {
            DependencyFactory.createArcXPException(
                ArcXPSDKErrorType.RECAPTCHA_ERROR,
                expectedError,
                null
            )
        }

    }


    @Test
    fun `login with recaptcha no listener`() {
        every { config.recaptchaForSignin } returns true
        initializeTestObject()

        testObject.login(email = email, password = password)

    }

    @Test
    fun `login without recaptcha`() {
        every { config.recaptchaForSignin } returns false
        initializeTestObject()
        val slot = slot<ArcXPIdentityListener>()

        testObject.login(email = email, password = password, listener = listener)
        verify(exactly = 1) {
            identityApiManager.login(
                username = email,
                password = password,
                listener = capture(slot)
            )
        }

        //success
        slot.captured.onLoginSuccess(response = authResponse)
        verify(exactly = 1) {
            listener.onLoginSuccess(response = authResponse)
            loginLiveData.postValue(Success(authResponse))
        }
        //failure
        slot.captured.onLoginError(error = error)
        verify(exactly = 1) {
            loginLiveData.postValue(Failure(error))
        }

    }

    @Test
    fun `login without recaptcha no listener`() {
        every { config.recaptchaForSignin } returns false
        initializeTestObject()
        val slot = slot<ArcXPIdentityListener>()

        testObject.login(email = email, password = password)
        verify(exactly = 1) {
            identityApiManager.login(
                username = email,
                password = password,
                listener = capture(slot)
            )
        }

        //success
        slot.captured.onLoginSuccess(response = authResponse)
        verify(exactly = 1) {
            loginLiveData.postValue(Success(authResponse))
        }
        //failure
        slot.captured.onLoginError(error = error)
        verify(exactly = 1) {
            loginLiveData.postValue(Failure(error))
        }

    }

    @Test
    fun `changePassword calls api manager`() {
        initializeTestObject()
        val oldPassword = "old"

        testObject.changePassword(
            newPassword = password,
            oldPassword = oldPassword,
            listener = listener
        )

        verify(exactly = 1) {
            identityApiManager.changePassword(
                newPassword = password,
                oldPassword = oldPassword,
                listener = listener
            )
        }
    }

    @Test
    fun `updatePassword calls api manager and posts to livedata`() {
        initializeTestObject()
        val oldPassword = "old"
        val slot = slot<ArcXPIdentityListener>()

        val liveData = testObject.updatePassword(
            newPassword = password,
            oldPassword = oldPassword,
            listener = listener
        )

        verify(exactly = 1) {
            identityApiManager.changePassword(
                newPassword = password,
                oldPassword = oldPassword,
                listener = capture(slot)
            )
        }

        //success
        val success = mockk<ArcXPIdentity>()
        slot.captured.onPasswordChangeSuccess(it = success)

        verify(exactly = 1) {
            listener.onPasswordChangeSuccess(it = success)
        }
        assertEquals(Success(success), liveData.value)


        //failure
        val exception = mockk<ArcXPException>()
        slot.captured.onPasswordChangeError(error = exception)
        verify(exactly = 1) {
            listener.onPasswordChangeError(error = exception)
        }
        assertEquals(Failure(exception), liveData.value)

    }

    @Test
    fun `updatePassword calls api manager and posts to livedata null listener`() {
        initializeTestObject()
        val oldPassword = "old"
        val slot = slot<ArcXPIdentityListener>()

        val liveData = testObject.updatePassword(
            newPassword = password,
            oldPassword = oldPassword,
            listener = null
        )

        verify(exactly = 1) {
            identityApiManager.changePassword(
                newPassword = password,
                oldPassword = oldPassword,
                listener = capture(slot)
            )
        }

        //success
        val success = mockk<ArcXPIdentity>()
        slot.captured.onPasswordChangeSuccess(it = success)
        assertEquals(Success(success), liveData.value)


        //failure
        val exception = mockk<ArcXPException>()
        slot.captured.onPasswordChangeError(error = exception)
        assertEquals(Failure(exception), liveData.value)

    }

    @Test
    fun `obtainNonceByEmailAddress calls api manager`() {
        initializeTestObject()

        testObject.obtainNonceByEmailAddress(email = email, listener = listener)

        verify(exactly = 1) {
            identityApiManager.obtainNonceByEmailAddress(email = email, listener = listener)
        }
    }

    @Test
    fun `requestResetPassword calls api manager`() {
        initializeTestObject()

        testObject.requestResetPassword(username = userName, listener = listener)

        verify(exactly = 1) {
            identityApiManager.obtainNonceByEmailAddress(email = userName, listener = listener)
        }
    }

    @Test
    fun `resetPasswordByNonce calls api manager`() {
        initializeTestObject()

        testObject.resetPasswordByNonce(nonce = nonce, newPassword = password, listener = listener)

        verify(exactly = 1) {
            identityApiManager.resetPasswordByNonce(
                nonce = nonce,
                newPassword = password,
                listener = listener
            )
        }
    }

    @Test
    fun `resetPassword calls api manager`() {
        initializeTestObject()

        testObject.resetPassword(nonce = nonce, newPassword = password, listener = listener)

        verify(exactly = 1) {
            identityApiManager.resetPasswordByNonce(
                nonce = nonce,
                newPassword = password,
                listener = listener
            )
        }
    }

    @Test
    fun `requestOneTimeAccessLink when config allows, recaptchaSiteKey is populated`() {
        initializeTestObject()
        testObject = spyk(testObject)
        val slot = slot<ArcXPIdentityListener>()

        testObject.requestOneTimeAccessLink(email = email, listener = listener)
        verify(exactly = 1) {
            testObject.runRecaptcha(listener = capture(slot))
        }
        verify(exactly = 1) {
            identityApiManager.checkRecaptcha(config = config, arcIdentityListener = slot.captured)
        }

        //success
        val response = mockk<ArcXPOneTimeAccessLink>()
        slot.captured.onRecaptchaSuccess(token = token)
        val successListener = slot<ArcXPIdentityListener>()
        verify(exactly = 1) {
            identityApiManager.setRecaptchaToken(recaptchaToken = token)
            identityApiManager.getMagicLink(email = email, listener = capture(successListener))
        }
        successListener.captured.onOneTimeAccessLinkSuccess(response = response)
        verify(exactly = 1) {
            listener.onOneTimeAccessLinkSuccess(response = response)
        }

        //failure
        val expectedException = mockk<ArcXPException>()
        val errorText = "expected"
        every { application.getString(R.string.recaptchaMagicLink_error) } returns errorText
        every {
            DependencyFactory.createArcXPException(
                type = ArcXPSDKErrorType.ONE_TIME_ACCESS_LINK_ERROR,
                message = errorText,
                value = error
            )
        } returns expectedException
        slot.captured.onRecaptchaFailure(error = error)
        verify(exactly = 1) {
            listener.onOneTimeAccessLinkError(error = expectedException)
        }

        slot.captured.onRecaptchaCancel()
    }

    @Test
    fun `requestOneTimeAccessLink when config allows, recaptchaSiteKey is null`() {
        every { config.recaptchaSiteKey } returns null
        val expectedErrorMessage = "expected"
        val magicLinkErrorMessage = "expected magic link"
        val expectedRecaptchaException = mockk<ArcXPException>()
        every { application.getString(R.string.recaptchaSiteKey_error) } returns expectedErrorMessage
        every { application.getString(R.string.recaptchaMagicLink_error) } returns magicLinkErrorMessage
        initializeTestObject()
        testObject = spyk(testObject)
        every {
            DependencyFactory.createArcXPException(
                type = ArcXPSDKErrorType.RECAPTCHA_ERROR,
                message = expectedErrorMessage,
                value = null
            )
        } returns expectedRecaptchaException
        val expectedException = mockk<ArcXPException>()
        every {
            DependencyFactory.createArcXPException(
                type = ArcXPSDKErrorType.ONE_TIME_ACCESS_LINK_ERROR,
                message = magicLinkErrorMessage,
                value = expectedRecaptchaException
            )
        } returns expectedException


        testObject.requestOneTimeAccessLink(email = email, listener = listener)

        verify(exactly = 1) {
            listener.onOneTimeAccessLinkError(error = expectedException)
        }
    }

    @Test
    fun `requestOneTimeAccessLink when config disallows`() {
        every { config.recaptchaForOneTimeAccess } returns false
        initializeTestObject()

        testObject.requestOneTimeAccessLink(email = email, listener = listener)

        val slot = slot<ArcXPIdentityListener>()
        verify(exactly = 1) {
            identityApiManager.getMagicLink(email = email, listener = capture(slot))
        }
        //success
        val response = mockk<ArcXPOneTimeAccessLink>()
        slot.captured.onOneTimeAccessLinkSuccess(response = response)
        verify(exactly = 1) {
            listener.onOneTimeAccessLinkSuccess(response = response)
        }
        //failure
        slot.captured.onOneTimeAccessLinkError(error = error)
        verify(exactly = 1) {
            listener.onOneTimeAccessLinkError(error = error)
        }
    }

    @Test
    fun `loginOneTimeAccessLink calls api manager`() {
        initializeTestObject()

        testObject.loginOneTimeAccessLink(nonce = nonce, listener = listener)

        verify(exactly = 1) {
            identityApiManager.loginMagicLink(nonce = nonce, listener = listener)
        }

    }

    @Test
    fun `redeemOneTimeAccessLink calls api manager`() {
        initializeTestObject()

        testObject.redeemOneTimeAccessLink(nonce = nonce, listener = listener)

        verify(exactly = 1) {
            identityApiManager.loginMagicLink(nonce = nonce, listener = listener)
        }

    }

    @Test
    fun `updateProfile calls api manager`() {
        val attributes = listOf(mockk<ArcXPAttributeRequest>())
        val contacts = listOf(mockk<ArcXPContactRequest>())
        val addresses = listOf(mockk<ArcXPAddressRequest>())
        val input = ArcXPUpdateProfileRequest(
            firstName = "firstName",
            lastName = "lastName",
            secondLastName = "secondLastName",
            displayName = "displayName",
            gender = "gender",
            email = "email",
            picture = "picture",
            birthYear = "birthYear",
            birthMonth = "birthMonth",
            birthDay = "birthDay",
            legacyId = "legacyId",
            contacts = contacts,
            addresses = addresses,
            attributes = attributes
        )
        val expected = ArcXPProfilePatchRequest(
            firstName = "firstName",
            lastName = "lastName",
            secondLastName = "secondLastName",
            displayName = "displayName",
            gender = "gender",
            email = "email",
            picture = "picture",
            birthYear = "birthYear",
            birthMonth = "birthMonth",
            birthDay = "birthDay",
            legacyId = "legacyId",
            contacts = contacts,
            addresses = addresses,
            attributes = attributes
        )
        initializeTestObject()

        testObject.updateProfile(update = input, listener = listener)

        verify(exactly = 1) {
            identityApiManager.updateProfile(update = expected, listener = listener)
        }

    }

    @Test
    fun `fetchProfile calls api manager`() {
        initializeTestObject()

        testObject.fetchProfile(listener = listener)

        verify(exactly = 1) {
            identityApiManager.getProfile(listener = listener)
        }
    }

    @Test
    fun `getUserProfile calls api manager and returns live data event no listener`() {
        initializeTestObject()
        val slot = slot<ArcXPIdentityListener>()

        val result = testObject.getUserProfile(listener = listener)

        verify(exactly = 1) {
            identityApiManager.getProfile(listener = capture(slot))
        }

        //success
        val response = mockk<ArcXPProfileManage>(relaxed = true)
        slot.captured.onFetchProfileSuccess(profileResponse = response)
        verify(exactly = 1) {
            listener.onFetchProfileSuccess(profileResponse = response)
        }
        assertEquals(Success(response), result.value)

        //failure
        slot.captured.onProfileError(error = error)
        verify(exactly = 1) {
            listener.onProfileError(error = error)
        }
        assertEquals(Failure(error), result.value)
    }

    @Test
    fun `registerUser calls signup with params`() {
        initializeTestObject()
        testObject = spyk(testObject)
        testObject.registerUser(
            username = userName,
            password = password,
            email = email,
            firstname = firstName,
            lastname = lastName,
            listener = listener
        )

        verify(exactly = 1) {
            testObject.signUp(
                username = userName,
                password = password,
                email = email,
                firstname = firstName,
                lastname = lastName,
                listener = listener
            )
        }
    }

    @Test
    fun `signUp when recaptcha for signup, non null site key`() {
        val errorText = "expected"
        every { application.getString(R.string.recaptchaMagicLink_error) } returns errorText
        initializeTestObject()
        testObject = spyk(testObject)
        val result = testObject.signUp(
            username = userName,
            password = password,
            email = email,
            firstname = firstName,
            lastname = lastName,
            listener = listener
        )

        val slot = slot<ArcXPIdentityListener>()
        verify(exactly = 1) {
            testObject.runRecaptcha(listener = capture(slot))

        }
        verify(exactly = 1) {
            identityApiManager.checkRecaptcha(config = config, arcIdentityListener = slot.captured)
        }


        //success recaptcha
        val registrationListener = slot<ArcXPIdentityListener>()
        clearAllMocks(answers = false)
        slot.captured.onRecaptchaSuccess(token = token)
        verifySequence {
            identityApiManager.setRecaptchaToken(recaptchaToken = token)
            identityApiManager.registerUser(
                username = userName,
                password = password,
                email = email,
                firstname = firstName,
                lastname = lastName,
                listener = capture(registrationListener)
            )
        }

        //success registration
        val response = mockk<ArcXPUser>()
        registrationListener.captured.onRegistrationSuccess(response = response)
        verify(exactly = 1) {
            listener.onRegistrationSuccess(response = response)
        }
        assertEquals(Success(response), result.value)


        //failure registration


        registrationListener.captured.onRegistrationError(error = error)
        verify(exactly = 1) {
            listener.onRegistrationError(error = error)
        }
        assertEquals(Failure(error), result.value)
        assertEquals(error, testObject.errors.value)


        //failure recaptcha
        val expectedException = mockk<ArcXPException>()
        every {
            DependencyFactory.createArcXPException(
                type = ArcXPSDKErrorType.REGISTRATION_ERROR,
                message = errorText,
                value = error
            )
        } returns expectedException
        slot.captured.onRecaptchaFailure(error = error)
        verify(exactly = 1) { listener.onRegistrationError(error = expectedException) }
        assertEquals(Failure(expectedException), result.value)
        assertEquals(expectedException, testObject.errors.value)

        slot.captured.onRecaptchaCancel()
    }

    @Test
    fun `signUp when recaptcha for signup, no listener, firstname, lastname, non null site key`() {
        val errorText = "expected"
        every { application.getString(R.string.recaptchaMagicLink_error) } returns errorText
        initializeTestObject()
        testObject = spyk(testObject)
        val result = testObject.signUp(username = userName, password = password, email = email)

        val slot = slot<ArcXPIdentityListener>()
        verify(exactly = 1) {
            testObject.runRecaptcha(listener = capture(slot))

        }
        verify(exactly = 1) {
            identityApiManager.checkRecaptcha(config = config, arcIdentityListener = slot.captured)
        }


        //success recaptcha
        val registrationListener = slot<ArcXPIdentityListener>()
        clearAllMocks(answers = false)
        slot.captured.onRecaptchaSuccess(token = token)
        verifySequence {
            identityApiManager.setRecaptchaToken(recaptchaToken = token)
            identityApiManager.registerUser(
                username = userName,
                password = password,
                email = email,
                firstname = null,
                lastname = null,
                listener = capture(registrationListener)
            )
        }

        //success registration
        val response = mockk<ArcXPUser>()
        registrationListener.captured.onRegistrationSuccess(response = response)
//        verify(exactly = 1) {
//            listener.onRegistrationSuccess(response = response)
//        }
        assertEquals(Success(response), result.value)


        //failure registration


        registrationListener.captured.onRegistrationError(error = error)
//        verify(exactly = 1) {
//            listener.onRegistrationError(error = error)
//        }
        assertEquals(Failure(error), result.value)
        assertEquals(error, testObject.errors.value)


        //failure recaptcha
        val expectedException = mockk<ArcXPException>()
        every {
            DependencyFactory.createArcXPException(
                type = ArcXPSDKErrorType.REGISTRATION_ERROR,
                message = errorText,
                value = error
            )
        } returns expectedException
        slot.captured.onRecaptchaFailure(error = error)
//        verify(exactly = 1) { listener.onRegistrationError(error = expectedException) }
        assertEquals(Failure(expectedException), result.value)
        assertEquals(expectedException, testObject.errors.value)

        slot.captured.onRecaptchaCancel()
    }

    @Test
    fun `signUp when recaptcha for signup, null site key`() {
        every { config.recaptchaSiteKey } returns null
        val errorText = "expected"
        every { application.getString(R.string.recaptchaSiteKey_error) } returns errorText
        val errorText2 = "expected2"
        every { application.getString(R.string.recaptchaMagicLink_error) } returns errorText2
        initializeTestObject()
//        testObject = spyk(testObject)

        every {
            DependencyFactory.createArcXPException(
                type = ArcXPSDKErrorType.RECAPTCHA_ERROR,
                message = errorText,
                value = null
            )
        } returns error
        val expectedException = mockk<ArcXPException>()
        every {
            DependencyFactory.createArcXPException(
                type = ArcXPSDKErrorType.REGISTRATION_ERROR,
                message = errorText2,
                value = error
            )
        } returns expectedException
        val result = testObject.signUp(
            username = userName,
            password = password,
            email = email,
            firstname = firstName,
            lastname = lastName,
            listener = listener
        )

//        val slot = slot<ArcXPIdentityListener>()
//        verify(exactly = 1) {
//            testObject.runRecaptcha(listener = capture(slot))
//
//        }
//        verify(exactly = 1) {
//            identityApiManager.checkRecaptcha(config = config, arcIdentityListener = slot.captured)
//        }


//        //success recaptcha
//        val registrationListener = slot<ArcXPIdentityListener>()
//        clearAllMocks(answers = false)
//        slot.captured.onRecaptchaSuccess(token = token)
//        verifySequence {
//            identityApiManager.setRecaptchaToken(recaptchaToken = token)
//            identityApiManager.registerUser(username = userName, password = password, email = email, firstname = firstName, lastname = lastName, listener = capture(registrationListener))
//        }
//
//        //success registration
//        val response = mockk<ArcXPUser>()
//        registrationListener.captured.onRegistrationSuccess(response = response)
//        verify(exactly = 1) {
//            listener.onRegistrationSuccess(response = response)
//        }
//        assertEquals(Success(response), result.value)
//
//
//        //failure registration
//
//
//        registrationListener.captured.onRegistrationError(error = error)
//        verify(exactly = 1) {
//            listener.onRegistrationError(error = error)
//        }
//        assertEquals(Failure(error), result.value)
//        assertEquals(error, testObject.errors.value)
//
//
//
//
        //failure recaptcha
//        val expectedException = mockk<ArcXPException>()
//        every {
//            DependencyFactory.createArcXPException(type = ArcXPSDKErrorType.REGISTRATION_ERROR, message = errorText, value = error)
//        } returns expectedException
//        slot.captured.onRecaptchaFailure(error = error)
        verify(exactly = 1) { listener.onRegistrationError(error = expectedException) }
        assertEquals(Failure(expectedException), result.value)
        assertEquals(expectedException, testObject.errors.value)
//
//        slot.captured.onRecaptchaCancel()
    }

    @Test
    fun `signUp when not recaptcha for signup`() {
        every {
            config.recaptchaForSignup
        } returns false
        val errorText = "expected"
        every { application.getString(R.string.recaptchaMagicLink_error) } returns errorText
        initializeTestObject()
        val result = testObject.signUp(
            username = userName,
            password = password,
            email = email,
            firstname = firstName,
            lastname = lastName,
            listener = listener
        )

        val slot = slot<ArcXPIdentityListener>()
        verify(exactly = 1) {
            identityApiManager.registerUser(
                username = userName,
                password = password,
                email = email,
                firstname = firstName,
                lastname = lastName,
                listener = capture(slot)
            )
        }

        //success
        val response = mockk<ArcXPUser>()
        slot.captured.onRegistrationSuccess(response = response)
        verify(exactly = 1) {
            listener.onRegistrationSuccess(response = response)
        }
        assertEquals(Success(response), result.value)

        //failure
        slot.captured.onRegistrationError(error = error)
        verify(exactly = 1) {
            listener.onRegistrationError(error = error)
        }
        assertEquals(Failure(error), result.value)
        assertEquals(error, testObject.errors.value)
    }

    @Test
    fun `logOut with blank google key`() {
        every { application.getString(R.string.google_key) } returns " "
        initializeTestObject()
        val result = testObject.logout(listener = listener)
        val slot = slot<ArcXPIdentityListener>()
        verify(exactly = 1) {
            identityApiManager.logout(listener = capture(slot))
        }

        //success
        slot.captured.onLogoutSuccess()
        verify(exactly = 1) {
            identityApiManager.rememberUser(remember = false)
            listener.onLogoutSuccess()
        }
        assertEquals(Success(true), result.value)

        //failure
        slot.captured.onLogoutError(error = error)
        verify(exactly = 1) {
            listener.onLogoutError(error = error)
        }
        assertEquals(Failure(error), result.value)
    }

    @Test
    fun `logOut with empty google key, no listener`() {
        every { application.getString(R.string.google_key) } returns ""
        initializeTestObject()
        val result = testObject.logout()
        val slot = slot<ArcXPIdentityListener>()
        verify(exactly = 1) {
            identityApiManager.logout(listener = capture(slot))
        }

        //success
        slot.captured.onLogoutSuccess()
        verify(exactly = 1) {
            identityApiManager.rememberUser(remember = false)
        }
        assertEquals(Success(true), result.value)

        //failure
        slot.captured.onLogoutError(error = error)
        assertEquals(Failure(error), result.value)
    }

    @Test
    fun `logOut with google key`() {
        val signInResult = mockk<BeginSignInResult>(relaxed = true)
        val request = mockk<IntentSenderRequest>()
        every { DependencyFactory.buildIntentSenderRequest(intentSender = signInResult.pendingIntent.intentSender) } returns request
        initializeTestObject()
        mockkObject(AccessToken)
        every { AccessToken.getCurrentAccessToken() } returns mockk()
        mockkStatic(LoginManager::class)
        val loginManager = mockk<LoginManager>()
        every { LoginManager.getInstance() } returns loginManager
        every { loginManager.logOut() } just runs
        testObject.loginWithGoogle(activity = activity, listener = listener)
        testObject.loginWithGoogleOneTap(activity = activity, listener = listener)
        assertNotNull(testObject.getLoginWithGoogleResultsReceiver())


        val signInSuccessListener = slot<OnSuccessListener<BeginSignInResult>>()
        verify {
            googleSignInTask.addOnSuccessListener(activity, capture(signInSuccessListener))
        }
        signInSuccessListener.captured.onSuccess(signInResult)

        assertNotNull(testObject.getLoginWithGoogleOneTapResultsReceiver())

        assertNotNull(testObject.oneTapClient)
        val task = mockk<Task<Void>>(relaxed = true)
        every { oneTapClient.signOut() } returns task
        every { task.addOnSuccessListener(any()) } returns task
        val result = testObject.logout(listener = listener)
        val successListener = slot<OnSuccessListener<Void>>()
        val failureListener = slot<OnFailureListener>()
        val apiLogoutListener = slot<ArcXPIdentityListener>()

        verify(exactly = 1) {
            loginManager.logOut()
            googleSignInClient.signOut()
            oneTapClient.signOut()
            task.addOnSuccessListener(capture(successListener))
            task.addOnFailureListener(capture(failureListener))
            identityApiManager.logout(capture(apiLogoutListener))
            googleSignInTask.addOnSuccessListener(activity, capture(signInSuccessListener))

        }
        assertNull(testObject.getLoginWithGoogleResultsReceiver())
        assertNull(testObject.getLoginWithGoogleOneTapResultsReceiver())
        clearAllMocks(answers = false)

        //api logout success
        apiLogoutListener.captured.onLogoutSuccess()
        verifySequence {
            identityApiManager.rememberUser(remember = false)
            listener.onLogoutSuccess()
        }
        assertEquals(Success(true), result.value)
        clearAllMocks(answers = false)

        //api logout failure
        apiLogoutListener.captured.onLogoutError(error = error)
        verify(exactly = 1) {
            listener.onLogoutError(error = error)
        }
        assertEquals(Failure(error), result.value)
        clearAllMocks(answers = false)

        //sign out success
        successListener.captured.onSuccess(mockk())
        verifySequence {
            listener.onLogoutSuccess()
        }
        clearAllMocks(answers = false)

        //sign out failure
        val msg = "msg"
        every { error.message } returns msg
        every {
            DependencyFactory.createArcXPException(
                type = ArcXPSDKErrorType.GOOGLE_LOGIN_ERROR,
                message = msg,
                value = error
            )
        } returns finalError
        failureListener.captured.onFailure(error)
        verifySequence {
            listener.onLogoutError(error = finalError)
        }
    }

    @Test
    fun `loginWithGoogle sets receivers`() {
        initializeTestObject()
        val slot = slot<ArcXPIdentityListener>()

        val result = testObject.loginWithGoogle(activity = activity, listener = listener)

        verify(exactly = 1) {
            DependencyFactory.createLoginWithGoogleResultsReceiver(
                signInIntent = signInIntent,
                manager = testObject,
                listener = capture(slot)
            )
            activity.supportFragmentManager.beginTransaction()
                .add(loginWithGoogleResultsReceiver, loginTag).commit()
        }
        assertNull(result.value)

        //success
        clearAllMocks(answers = false)
        slot.captured.onLoginSuccess(response = authResponse)
        verifySequence {
            activity.supportFragmentManager.beginTransaction()
                .remove(loginWithGoogleResultsReceiver as Fragment).commit()
            listener.onLoginSuccess(response = authResponse)
        }
        assertEquals(authResponse, result.value)
        assertNotNull(testObject.getLoginWithGoogleResultsReceiver())

        //failure
        clearAllMocks(answers = false)
        slot.captured.onLoginError(error = error)
        verifySequence {
            activity.supportFragmentManager.beginTransaction()
                .remove(loginWithGoogleResultsReceiver as Fragment).commit()
            listener.onLoginError(error = error)
        }
        assertEquals(error, testObject.errors.value)
        assertNull(testObject.getLoginWithGoogleResultsReceiver())

        //call function again so receiver is non null
        clearAllMocks(answers = false)
        testObject.loginWithGoogle(activity = activity, listener = listener)

        verify(exactly = 1) {
            DependencyFactory.createLoginWithGoogleResultsReceiver(
                signInIntent = signInIntent,
                manager = testObject,
                listener = capture(slot)
            )
            activity.supportFragmentManager.beginTransaction()
                .add(loginWithGoogleResultsReceiver, loginTag).commit()
        }
        testObject.loginWithGoogle(activity = activity, listener = listener)
    }

    @Test
    fun `loginWithGoogle sets receivers no listener`() {
        initializeTestObject()
        val slot = slot<ArcXPIdentityListener>()

        val result = testObject.loginWithGoogle(activity = activity)

        verify(exactly = 1) {
            DependencyFactory.createLoginWithGoogleResultsReceiver(
                signInIntent = signInIntent,
                manager = testObject,
                listener = capture(slot)
            )
            activity.supportFragmentManager.beginTransaction()
                .add(loginWithGoogleResultsReceiver, loginTag).commit()
        }
        assertNull(result.value)

        //success
        clearAllMocks(answers = false)
        slot.captured.onLoginSuccess(response = authResponse)
        verifySequence {
            activity.supportFragmentManager.beginTransaction()
                .remove(loginWithGoogleResultsReceiver as Fragment).commit()
        }
        assertEquals(authResponse, result.value)
        assertNotNull(testObject.getLoginWithGoogleResultsReceiver())

        //failure
        clearAllMocks(answers = false)
        slot.captured.onLoginError(error = error)
        verifySequence {
            activity.supportFragmentManager.beginTransaction()
                .remove(loginWithGoogleResultsReceiver as Fragment).commit()
        }
        assertEquals(error, testObject.errors.value)
        assertNull(testObject.getLoginWithGoogleResultsReceiver())
    }

    @Test
    fun `loginWithGoogleOneTap with one tap disabled`() {
        every { config.googleOneTapEnabled } returns false
        initializeTestObject()
        clearAllMocks(answers = false)
        every {
            application.getString(R.string.one_tap_disabled_error_message)
        } returns expectedErrorMessage
        every {
            DependencyFactory.createArcXPException(
                type = ArcXPSDKErrorType.GOOGLE_LOGIN_ERROR,
                message = expectedErrorMessage
            )
        } returns error

        testObject.loginWithGoogleOneTap(activity = activity, listener = listener)

        verifySequence {
            config.googleOneTapEnabled
            listener.onLoginError(error = error)
        }
        assertNull(testObject.oneTapClient)
    }

    @Test
    fun `loginWithGoogleOneTap sets client and receiver`() {
        initializeTestObject()
        clearAllMocks(answers = false)
        val signInSuccessListener = slot<OnSuccessListener<BeginSignInResult>>()
        val signInFailureListener = slot<OnFailureListener>()

        testObject.loginWithGoogleOneTap(activity = activity, listener = listener)

        verifySequence {
            config.googleOneTapEnabled
            googleSignInTask.addOnSuccessListener(activity, capture(signInSuccessListener))
            googleSignInTask.addOnFailureListener(activity, capture(signInFailureListener))
        }
        assertEquals(oneTapClient, testObject.oneTapClient)
        assertEquals(beginSignInRequest, testObject.getSignInRequest())

        //beginSignIn success
        val result = mockk<BeginSignInResult>(relaxed = true)
        val request = mockk<IntentSenderRequest>()
        every { DependencyFactory.buildIntentSenderRequest(intentSender = result.pendingIntent.intentSender) } returns request
        assertNull(testObject.getLoginWithGoogleOneTapResultsReceiver())
        val loginListener = slot<ArcXPIdentityListener>()
        signInSuccessListener.captured.onSuccess(result)

        verify(exactly = 1) {
            DependencyFactory.createLoginWithGoogleOneTapResultsReceiver(
                signInIntent = request,
                manager = testObject,
                listener = capture(loginListener)
            )
            activity.supportFragmentManager.beginTransaction()
                .add(loginWithGoogleOneTapResultsReceiver, loginTag)
        }
        assertEquals(
            loginWithGoogleOneTapResultsReceiver,
            testObject.getLoginWithGoogleOneTapResultsReceiver()
        )
        //onGoogleOneTapLoginSuccess
        clearAllMocks(answers = false)
        loginListener.captured.onGoogleOneTapLoginSuccess(
            username = userName,
            password = password,
            token = token
        )
        verifySequence {
            activity.supportFragmentManager.beginTransaction()
                .remove(loginWithGoogleOneTapResultsReceiver as Fragment).commit()
            listener.onGoogleOneTapLoginSuccess(
                username = userName,
                password = password,
                token = token
            )
        }

        //onLoginError
        clearAllMocks(answers = false)
        loginListener.captured.onLoginError(error = error)
        verifySequence {
            activity.supportFragmentManager.beginTransaction()
                .remove(loginWithGoogleOneTapResultsReceiver as Fragment).commit()
            listener.onLoginError(error = error)
        }

        //beginSignIn failure
        clearAllMocks(answers = false)
        val msg = "msg"
        every { error.localizedMessage } returns msg
        every {
            DependencyFactory.createArcXPException(
                type = ArcXPSDKErrorType.GOOGLE_LOGIN_ERROR_NO_ACCOUNT,
                message = msg,
                value = error
            )
        } returns finalError
        signInFailureListener.captured.onFailure(error)
        verifySequence {
            listener.onLoginError(error = finalError)
        }
    }

    @Test
    fun `removeIdentity calls through to api manager`() {
        initializeTestObject()
        clearAllMocks(answers = false)

        testObject.removeIdentity(grantType = grantType, listener = listener)
        verifySequence {
            identityApiManager.removeIdentity(grantType = grantType, listener = listener)
        }
    }

    @Test
    fun `deleteUser calls through to api manager`() {
        initializeTestObject()
        clearAllMocks(answers = false)

        testObject.deleteUser(listener = listener)
        verifySequence {
            identityApiManager.deleteUser(listener = listener)
        }
    }

    @Test
    fun `requestDeleteAccount calls through to api manager`() {
        initializeTestObject()
        clearAllMocks(answers = false)

        testObject.requestDeleteAccount(listener = listener)
        verifySequence {
            identityApiManager.deleteUser(listener = listener)
        }
    }

    @Test
    fun `approveDeletion calls through to api manager`() {
        initializeTestObject()
        clearAllMocks(answers = false)

        testObject.approveDeletion(nonce = nonce, listener = listener)
        verifySequence {
            identityApiManager.approveDeletion(nonce = nonce, listener = listener)
        }
    }

    @Test
    fun `approveDeleteAccount calls through to api manager`() {
        initializeTestObject()
        clearAllMocks(answers = false)

        testObject.approveDeleteAccount(nonce = nonce, listener = listener)
        verifySequence {
            identityApiManager.approveDeletion(nonce = nonce, listener = listener)
        }
    }

    @Test
    fun `validateSession calls through to api manager`() {
        initializeTestObject()
        clearAllMocks(answers = false)

        testObject.validateSession(token = token, listener = listener)
        verifySequence {
            identityApiManager.validateJwt(token = token, arcIdentityListener = listener)
        }
    }

    @Test
    fun `validateSession no token calls through to api manager`() {
        initializeTestObject()
        clearAllMocks(answers = false)

        testObject.validateSession(listener = listener)
        verifySequence {
            identityApiManager.validateJwt(listenerArc = listener)
        }
    }

    @Test
    fun `refreshSession calls through to api manager`() {
        initializeTestObject()
        clearAllMocks(answers = false)

        testObject.refreshSession(token = token, listener = listener)
        verifySequence {
            identityApiManager.refreshToken(
                token = token,
                grantType = ArcXPAuthRequest.Companion.GrantType.REFRESH_TOKEN.value,
                arcIdentityListener = listener
            )
        }
    }

    @Test
    fun `refreshSession no token calls through to api manager`() {
        initializeTestObject()
        clearAllMocks(answers = false)

        testObject.refreshSession(listener = listener)
        verifySequence {
            identityApiManager.refreshToken(
                token = refreshToken,
                grantType = ArcXPAuthRequest.Companion.GrantType.REFRESH_TOKEN.value,
                arcIdentityListener = listener
            )
        }
    }

    @Test
    fun `getAllSubscriptions calls through to api manager`() {
        initializeTestObject()
        clearAllMocks(answers = false)

        testObject.getAllSubscriptions(listener = salesListener)
        verifySequence {
            salesApiManager.getAllSubscriptions(callback = salesListener)
        }
    }

    @Test
    fun `getAllActiveSubscriptions calls through to api manager`() {
        initializeTestObject()
        clearAllMocks(answers = false)

        testObject.getAllActiveSubscriptions(listener = salesListener)
        verifySequence {
            salesApiManager.getAllActiveSubscriptions(callback = salesListener)
        }
    }

    @Test
    fun `getEntitlements calls through to api manager`() {
        initializeTestObject()
        clearAllMocks(answers = false)

        testObject.getEntitlements(listener = salesListener)
        verifySequence {
            salesApiManager.getEntitlements(callback = salesListener)
        }
    }

    @Test
    fun `getActivePaywallRules calls through to api manager`() {
        initializeTestObject()
        clearAllMocks(answers = false)

        testObject.getActivePaywallRules(listener = retailListener)
        verifySequence {
            retailApiManager.getActivePaywallRules(listener = retailListener)
        }
    }

    @Test
    fun `evaluatePage with listener`() {
        initializeTestObject()
        val otherConditions = HashMap<String, String>()
        otherConditions["a"] = "a"
        otherConditions["b"] = "b"
        otherConditions["c"] = "c"
        val expectedConditions = HashMap<String, String>()
        expectedConditions["contentType"] = contentType
        expectedConditions["contentSection"] = contentSection
        expectedConditions["deviceClass"] = deviceClass
        expectedConditions["contentType"] = contentType
        expectedConditions["a"] = "a"
        expectedConditions["b"] = "b"
        expectedConditions["c"] = "c"
        val expectedPageViewData =
            ArcXPPageviewData(pageId = pageId, conditions = expectedConditions)
        testObject = spyk(testObject)

        testObject.evaluatePage(
            pageId = pageId,
            contentType = contentType,
            contentSection = contentSection,
            deviceClass = deviceClass,
            otherConditions = otherConditions,
            entitlements = entitlements,
            listener = pageViewListener
        )

        verify(exactly = 1) {
            testObject.evaluatePage(
                pageviewData = expectedPageViewData,
                entitlements = entitlements,
                currentTime = null,
                listener = pageViewListener
            )
        }
    }

    @Test
    fun `evaluatePage with listener with null inputs`() {
        initializeTestObject()
        val expectedConditions = HashMap<String, String>()
        val expectedPageViewData =
            ArcXPPageviewData(pageId = pageId, conditions = expectedConditions)
        testObject = spyk(testObject)

        testObject.evaluatePage(
            pageId = pageId,
            contentType = null,
            contentSection = null,
            deviceClass = null,
            otherConditions = null,
            listener = pageViewListener
        )

        verify(exactly = 1) {
            testObject.evaluatePage(
                pageviewData = expectedPageViewData,
                entitlements = null,
                currentTime = null,
                listener = pageViewListener
            )
        }
    }

    @Test
    fun `evaluatePage with liveData`() {
        initializeTestObject()
        val otherConditions = HashMap<String, String>()
        otherConditions["a"] = "a"
        otherConditions["b"] = "b"
        otherConditions["c"] = "c"
        val expectedConditions = HashMap<String, String>()
        expectedConditions["contentType"] = contentType
        expectedConditions["contentSection"] = contentSection
        expectedConditions["deviceClass"] = deviceClass
        expectedConditions["contentType"] = contentType
        expectedConditions["a"] = "a"
        expectedConditions["b"] = "b"
        expectedConditions["c"] = "c"
        val expectedPageViewData =
            ArcXPPageviewData(pageId = pageId, conditions = expectedConditions)
        testObject = spyk(testObject)
        val slot = slot<ArcXPPageviewListener>()

        val result = testObject.evaluatePage(
            pageId = pageId,
            contentType = contentType,
            contentSection = contentSection,
            deviceClass = deviceClass,
            otherConditions = otherConditions,
            entitlements = entitlements
        )

        verify(exactly = 1) {
            testObject.evaluatePage(
                pageviewData = expectedPageViewData,
                entitlements = entitlements,
                currentTime = null,
                listener = capture(slot)
            )
        }
        val expected = mockk<ArcXPPageviewEvaluationResult>()
        slot.captured.onEvaluationResult(response = expected)
        assertEquals(expected, result.value)
    }

    @Test
    fun `evaluatePage with liveData with null inputs`() {
        initializeTestObject()
        val expectedConditions = HashMap<String, String>()
        val expectedPageViewData =
            ArcXPPageviewData(pageId = pageId, conditions = expectedConditions)
        testObject = spyk(testObject)
        val slot = slot<ArcXPPageviewListener>()

        val result = testObject.evaluatePage(
            pageId = pageId,
            contentType = null,
            contentSection = null,
            deviceClass = null,
            otherConditions = null,
        )

        verify(exactly = 1) {
            testObject.evaluatePage(
                pageviewData = expectedPageViewData,
                entitlements = null,
                currentTime = null,
                listener = capture(slot)
            )
        }
        val expected = mockk<ArcXPPageviewEvaluationResult>()
        slot.captured.onEvaluationResult(response = expected)
        assertEquals(expected, result.value)
    }

    @Test
    fun `evaluatePageTime with time param`() {
//        mockkStatic(Calendar::class)
        val expectedTime = 1000L
//        every { Calendar.getInstance().timeInMillis } returns expectedTime
        initializeTestObject()
        val otherConditions = HashMap<String, String>()
        otherConditions["a"] = "a"
        otherConditions["b"] = "b"
        otherConditions["c"] = "c"
        val expectedConditions = HashMap<String, String>()
        expectedConditions["contentType"] = contentType
        expectedConditions["contentSection"] = contentSection
        expectedConditions["deviceClass"] = deviceClass
        expectedConditions["contentType"] = contentType
        expectedConditions["a"] = "a"
        expectedConditions["b"] = "b"
        expectedConditions["c"] = "c"
        val expectedPageViewData =
            ArcXPPageviewData(pageId = pageId, conditions = expectedConditions)
        testObject = spyk(testObject)

        testObject.evaluatePageTime(
            pageId = pageId,
            contentType = contentType,
            contentSection = contentSection,
            deviceClass = deviceClass,
            otherConditions = otherConditions,
            entitlements = entitlements,
            listener = pageViewListener,
            currentTime = expectedTime
        )

        verify(exactly = 1) {
            testObject.evaluatePage(
                pageviewData = expectedPageViewData,
                entitlements = entitlements,
                currentTime = expectedTime,
                listener = pageViewListener
            )
        }
    }

    @Test
    fun `evaluatePageTime with null data`() {
        mockkStatic(Calendar::class)
        val expectedTime = 1000L
        every { Calendar.getInstance().timeInMillis } returns expectedTime
        initializeTestObject()
        val expectedConditions = HashMap<String, String>()
        val expectedPageViewData =
            ArcXPPageviewData(pageId = pageId, conditions = expectedConditions)
        testObject = spyk(testObject)

        testObject.evaluatePageTime(
            pageId = pageId,
            contentType = null,
            contentSection = null,
            deviceClass = null,
            otherConditions = null,
            listener = pageViewListener
        )

        verify(exactly = 1) {
            testObject.evaluatePage(
                pageviewData = expectedPageViewData,
                entitlements = null,
                currentTime = expectedTime,
                listener = pageViewListener
            )
        }
        unmockkStatic(Calendar::class)
    }

    @Test
    fun `evaluatePage main method calls paywall manager, active session`() {
        every { authManager.uuid } returns "something"
        val pageviewData = mockk<ArcXPPageviewData>()
        val timeInput = 1001L
        initializeTestObject()
        clearAllMocks(answers = false)
        val slot = slot<ArcXPPageviewListener>()

        testObject.evaluatePage(
            pageviewData = pageviewData,
            entitlements = entitlements,
            currentTime = timeInput,
            listener = pageViewListener
        )
        verifySequence {
            paywallManager.initialize(
                entitlementsResponse = entitlements,
                passedInTime = timeInput,
                loggedInState = true,
                listener = capture(slot)
            )
        }
        //onInitializationResult true
        val result = mockk<ArcXPPageviewEvaluationResult>()
        every { result.show } returns true
        every { result.campaign } returns "campaign"
        every { pageviewData.pageId } returns "pageID"
        val expectedFinalResult = ArcXPPageviewEvaluationResult(
            pageId = "pageID",
            show = true,
            campaign = "campaign"
        )
        every { paywallManager.evaluate(pageviewData = pageviewData) } returns result
        clearAllMocks(answers = false)
        slot.captured.onInitializationResult(success = true)
        verifySequence {
            paywallManager.evaluate(pageviewData = pageviewData)
            pageViewListener.onEvaluationResult(response = expectedFinalResult)
        }

        //onInitializationResult false
        clearAllMocks(answers = false)
        every { pageviewData.pageId } returns "pageID"
        val expectedFinalResult2 = ArcXPPageviewEvaluationResult(
            pageId = "pageID",
            show = false,
        )
        slot.captured.onInitializationResult(success = false)
        verifySequence {
            pageViewListener.onEvaluationResult(response = expectedFinalResult2)
        }

    }

    @Test
    fun `evaluatePage main method calls paywall manager, active session, null - default inputs`() {
        mockkStatic(Calendar::class)
        val expectedTime = 1000L
        every { Calendar.getInstance().timeInMillis } returns expectedTime
        every { authManager.uuid } returns "something"
        val pageviewData = mockk<ArcXPPageviewData>()
        initializeTestObject()
        clearAllMocks(answers = false)
        val slot = slot<ArcXPPageviewListener>()

        testObject.evaluatePage(
            pageviewData = pageviewData,
            listener = pageViewListener
        )
        verifySequence {
            paywallManager.initialize(
                entitlementsResponse = null,
                passedInTime = expectedTime,
                loggedInState = true,
                listener = capture(slot)
            )
        }
        //onInitializationResult true
        val result = mockk<ArcXPPageviewEvaluationResult>()
        every { result.show } returns true
        every { result.campaign } returns "campaign"
        every { pageviewData.pageId } returns "pageID"
        val expectedFinalResult = ArcXPPageviewEvaluationResult(
            pageId = "pageID",
            show = true,
            campaign = "campaign"
        )
        every { paywallManager.evaluate(pageviewData = pageviewData) } returns result
        clearAllMocks(answers = false)
        slot.captured.onInitializationResult(success = true)
        verifySequence {
            paywallManager.evaluate(pageviewData = pageviewData)
            pageViewListener.onEvaluationResult(response = expectedFinalResult)
        }

        //onInitializationResult false
        clearAllMocks(answers = false)
        every { pageviewData.pageId } returns "pageID"
        val expectedFinalResult2 = ArcXPPageviewEvaluationResult(
            pageId = "pageID",
            show = false,
        )
        slot.captured.onInitializationResult(success = false)
        verifySequence {
            pageViewListener.onEvaluationResult(response = expectedFinalResult2)
        }
        unmockkStatic(Calendar::class)
    }

    @Test
    fun `evaluatePage main method calls paywall manager, inactive session`() {
        every { authManager.uuid } returns null
        val pageviewData = mockk<ArcXPPageviewData>()
        val timeInput = 1001L
        initializeTestObject()
        clearAllMocks(answers = false)
        val slot = slot<ArcXPPageviewListener>()

        testObject.evaluatePage(
            pageviewData = pageviewData,
            entitlements = entitlements,
            currentTime = timeInput,
            listener = pageViewListener
        )
        verifySequence {
            paywallManager.initialize(
                entitlementsResponse = entitlements,
                passedInTime = timeInput,
                loggedInState = false,
                listener = capture(slot)
            )
        }
        //onInitializationResult true
        val result = mockk<ArcXPPageviewEvaluationResult>()
        every { result.show } returns true
        every { result.campaign } returns "campaign"
        every { pageviewData.pageId } returns "pageID"
        val expectedFinalResult = ArcXPPageviewEvaluationResult(
            pageId = "pageID",
            show = true,
            campaign = "campaign"
        )
        every { paywallManager.evaluate(pageviewData = pageviewData) } returns result
        clearAllMocks(answers = false)
        slot.captured.onInitializationResult(success = true)
        verifySequence {
            paywallManager.evaluate(pageviewData = pageviewData)
            pageViewListener.onEvaluationResult(response = expectedFinalResult)
        }

        //onInitializationResult false
        clearAllMocks(answers = false)
        every { pageviewData.pageId } returns "pageID"
        val expectedFinalResult2 = ArcXPPageviewEvaluationResult(
            pageId = "pageID",
            show = false,
        )
        slot.captured.onInitializationResult(success = false)
        verifySequence {
            pageViewListener.onEvaluationResult(response = expectedFinalResult2)
        }

    }

    @Test
    fun `evaluatePageNoTime calls paywall mgr`() {
        every { authManager.uuid } returns "something"
        val pageviewData = mockk<ArcXPPageviewData>()
        initializeTestObject()
        clearAllMocks(answers = false)
        val slot = slot<ArcXPPageviewListener>()

        testObject.evaluatePageNoTime(pageviewData = pageviewData, listener = pageViewListener)


        verifySequence {
            paywallManager.initialize(
                entitlementsResponse = null,
                passedInTime = null,
                loggedInState = true,
                listener = capture(slot)
            )
        }

        //onInitializationResult true
        val result = mockk<ArcXPPageviewEvaluationResult>()
        every { result.show } returns true
        every { result.campaign } returns "campaign"
        every { pageviewData.pageId } returns "pageID"
        val expectedFinalResult = ArcXPPageviewEvaluationResult(
            pageId = "pageID",
            show = true,
            campaign = "campaign"
        )
        every { paywallManager.evaluate(pageviewData = pageviewData) } returns result
        clearAllMocks(answers = false)
        slot.captured.onInitializationResult(success = true)
        verifySequence {
            paywallManager.evaluate(pageviewData = pageviewData)
            pageViewListener.onEvaluationResult(response = expectedFinalResult)
        }

        //onInitializationResult false
        clearAllMocks(answers = false)
        every { pageviewData.pageId } returns "pageID"
        val expectedFinalResult2 = ArcXPPageviewEvaluationResult(
            pageId = "pageID",
            show = false,
        )
        slot.captured.onInitializationResult(success = false)
        verifySequence {
            pageViewListener.onEvaluationResult(response = expectedFinalResult2)
        }

    }

    @Test
    fun `getPaywallCache gets from paywall mgr`() {
        val expected = "paywallCache"
        every { paywallManager.getPaywallCache() } returns expected
        initializeTestObject()

        val actual = testObject.getPaywallCache()

        assertEquals(expected, actual)
    }

    @Test
    fun `clearPaywallCache clears from paywall mgr`() {
        initializeTestObject()
        clearAllMocks(answers = false)

        testObject.clearPaywallCache()

        verifySequence {
            paywallManager.clearPaywallCache()
        }
    }

    @Test
    fun `getConfig gets from auth manager`() {
        val expected = mockk<ArcXPConfig>()
        every { authManager.getConfig() } returns expected
        initializeTestObject()

        val actual = testObject.getConfig()

        assertEquals(expected, actual)
    }

    @Test
    fun `getRefreshToken gets from auth manager`() {
        val expected = "refresh token"
        every { authManager.refreshToken } returns expected
        initializeTestObject()

        val actual = testObject.getRefreshToken()

        assertEquals(expected, actual)
    }

    @Test
    fun `getAccessToken gets from auth manager`() {
        val expected = "access token"
        every { authManager.accessToken } returns expected
        initializeTestObject()

        val actual = testObject.getAccessToken()

        assertEquals(expected, actual)
    }

    @Test
    fun `isLoggedIn validates with api manager`() {
        initializeTestObject()
        val slot = slot<ArcXPIdentityListener>()

        testObject.isLoggedIn(listener = listener)
        verify(exactly = 1) {
            identityApiManager.validateJwt(listenerArc = capture(slot))
        }

        //validate success
        slot.captured.onValidateSessionSuccess()
        verify(exactly = 1) {
            listener.onValidateSessionSuccess()
        }

        //validate failure
        slot.captured.onValidateSessionError(error = error)
        verify(exactly = 1) {
            listener.onValidateSessionError(error = error)
        }
    }

    @Test
    fun `isLoggedIn validates with api manager no listener`() {
        initializeTestObject()
        val slot = slot<ArcXPIdentityListener>()

        val result = testObject.isLoggedIn()
        verify(exactly = 1) {
            identityApiManager.validateJwt(listenerArc = capture(slot))
        }

        //validate success
        slot.captured.onValidateSessionSuccess()
        assertTrue(result.value!!)

        //validate failure
        slot.captured.onValidateSessionError(error = error)
        assertFalse(result.value!!)
    }

    @Test
    fun `setRecaptchaToken sets in api mgr`() {
        initializeTestObject()

        testObject.setRecaptchaToken(token = token)

        verify(exactly = 1) {
            identityApiManager.setRecaptchaToken(recaptchaToken = token)
        }
    }

    @Test
    fun `getRecaptchaToken returns from api manager`() {
        every { identityApiManager.getRecaptchaToken() } returns token
        initializeTestObject()

        assertEquals(token, testObject.getRecaptchaToken())
    }

    @Test
    fun `thirdPartyLogin calls api mgr`() {
        initializeTestObject()
        val type = ArcXPAuthRequest.Companion.GrantType.REFRESH_TOKEN
        clearAllMocks(answers = false)

        testObject.thirdPartyLogin(token = token, type = type, listener = listener)

        verifySequence {
            identityApiManager.thirdPartyLogin(
                token = token,
                type = type,
                arcIdentityListener = listener
            )
        }
    }

    @Test
    fun `sendVerificationEmail calls api manager`() {
        initializeTestObject()
        clearAllMocks(answers = false)

        testObject.sendVerificationEmail(email = email, listener = listener)

        verifySequence {
            identityApiManager.sendVerificationEmail(email = email, listener = listener)
        }
    }

    @Test
    fun `setAccessToken sets in auth mgr`() {
        initializeTestObject()
        clearAllMocks(answers = false)

        testObject.setAccessToken(token = token)

        verifySequence {
            authManager.accessToken = token
        }
    }

    @Test
    fun `verifyEmail calls api mgr`() {
        initializeTestObject()
        clearAllMocks(answers = false)

        testObject.verifyEmail(nonce = nonce, listener = listener)

        verifySequence {
            identityApiManager.verifyEmail(nonce = nonce, listener = listener)
        }
    }

    @Test
    fun loginWithFacebook() {
        initializeTestObject()
        clearAllMocks(answers = false)
        testObject = spyk(testObject)
        val fbLoginButton = mockk<LoginButton>(relaxed = true)
        val slot = slot<FacebookCallback<LoginResult?>>()

        val liveData =
            testObject.loginWithFacebook(fbLoginButton = fbLoginButton, listener = listener)
        verifySequence {
            fbLoginButton.registerCallback(callbackManager, capture(slot))
        }
        val callback = slot.captured

        //onSuccess
        mockkObject(AccessToken)
        val currentAccessToken = mockk<AccessToken>()
        every { currentAccessToken.token } returns token
        every { AccessToken.getCurrentAccessToken() } returns currentAccessToken
        val result = mockk<LoginResult>()
        val loginListener = slot<ArcXPIdentityListener>()
        clearAllMocks(answers = false)
        callback.onSuccess(result = result)
        verifySequence {
            testObject.thirdPartyLogin(
                token = token,
                type = ArcXPAuthRequest.Companion.GrantType.FACEBOOK,
                listener = capture(loginListener)
            )
        }
        //onLoginSuccess
        val response = mockk<ArcXPAuth>()
        loginListener.captured.onLoginSuccess(response = response)
        verify(exactly = 1) {
            listener.onLoginSuccess(response = response)
        }
        assertEquals(response, liveData.value)
        //onLoginFailure
        loginListener.captured.onLoginError(error = error)
        verify(exactly = 1) {
            listener.onLoginError(error = error)
        }
        assertEquals(error, testObject.errors.value)
        //onCancel
        every {
            DependencyFactory.createArcXPException(
                type = ArcXPSDKErrorType.FACEBOOK_LOGIN_CANCEL,
                message = userCancelledLoginError
            )
        } returns finalError
        callback.onCancel()
        verify(exactly = 1) {
            listener.onLoginError(error = finalError)
        }
        assertEquals(finalError, testObject.errors.value)

        //onError
        clearAllMocks(answers = false)
        val fbError = mockk<FacebookException>()
        val fbErrorFinal = mockk<ArcXPException>()
        every { fbError.message } returns expectedErrorMessage
        every {
            DependencyFactory.createArcXPException(
                type = ArcXPSDKErrorType.FACEBOOK_LOGIN_ERROR,
                message = expectedErrorMessage
            )
        } returns fbErrorFinal
        callback.onError(error = fbError)
        verify(exactly = 1) {
            listener.onLoginError(error = fbErrorFinal)
        }
        assertEquals(fbErrorFinal, testObject.errors.value)
    }

//    @Test
//    fun `loginWithFacebook no listener`() {
//        initializeTestObject()
//        clearAllMocks(answers = false)
//        testObject = spyk(testObject)
//        val fbLoginButton = mockk<LoginButton>(relaxed = true)
//        val slot = slot<FacebookCallback<LoginResult?>>()
//
//        val liveData =
//            testObject.loginWithFacebook(fbLoginButton = fbLoginButton)
//        verifySequence {
//            fbLoginButton.registerCallback(callbackManager, capture(slot))
//        }
//        val callback = slot.captured
//
//        //onSuccess
//        mockkObject(AccessToken)
//        val currentAccessToken = mockk<AccessToken>()
//        every { currentAccessToken.token } returns token
//        every { AccessToken.getCurrentAccessToken() } returns currentAccessToken
//        val result = mockk<LoginResult>()
//        val loginListener = slot<ArcXPIdentityListener>()
//        clearAllMocks(answers = false)
//        callback.onSuccess(result = result)
//        verifySequence {
//            testObject.thirdPartyLogin(
//                token = token,
//                type = ArcXPAuthRequest.Companion.GrantType.FACEBOOK,
//                listener = capture(loginListener)
//            )
//        }
//        //onLoginSuccess
//        val response = mockk<ArcXPAuth>()
//        loginListener.captured.onLoginSuccess(response = response)
//        assertEquals(response, liveData.value)
//        //onLoginFailure
//        loginListener.captured.onLoginError(error = error)
//        assertEquals(error, testObject.errors.value)
//        //onCancel
//        every {
//            DependencyFactory.createArcXPException(
//                type = ArcXPSDKErrorType.FACEBOOK_LOGIN_CANCEL,
//                message = userCancelledLoginError
//            )
//        } returns finalError
//        callback.onCancel()
//        assertEquals(finalError, testObject.errors.value)
//
//        //onError
//        clearAllMocks(answers = false)
//        val fbError = mockk<FacebookException>()
//        val fbErrorFinal = mockk<ArcXPException>()
//        every { fbError.message } returns expectedErrorMessage
//        every {
//            DependencyFactory.createArcXPException(
//                type = ArcXPSDKErrorType.FACEBOOK_LOGIN_ERROR,
//                message = expectedErrorMessage
//            )
//        } returns fbErrorFinal
//        callback.onError(error = fbError)
//        assertEquals(fbErrorFinal, testObject.errors.value)
//    }  //TODO callback manager is lazily instantiated, but is a singleton so can't reset with more glue (resettable lazy implementation..)

    @Test
    fun `logoutOfGoogle key is set`() {
        val signInResult = mockk<BeginSignInResult>(relaxed = true)
        val request = mockk<IntentSenderRequest>()
        every { DependencyFactory.buildIntentSenderRequest(intentSender = signInResult.pendingIntent.intentSender) } returns request
        initializeTestObject()
        mockkObject(AccessToken)
        every { AccessToken.getCurrentAccessToken() } returns mockk()
        mockkStatic(LoginManager::class)
        val loginManager = mockk<LoginManager>()
        every { LoginManager.getInstance() } returns loginManager
        every { loginManager.logOut() } just runs
        testObject.loginWithGoogle(activity = activity, listener = listener)
        testObject.loginWithGoogleOneTap(activity = activity, listener = listener)
        assertNotNull(testObject.getLoginWithGoogleResultsReceiver())


        val signInSuccessListener = slot<OnSuccessListener<BeginSignInResult>>()
        verify {
            googleSignInTask.addOnSuccessListener(activity, capture(signInSuccessListener))
        }
        signInSuccessListener.captured.onSuccess(signInResult)

        assertNotNull(testObject.getLoginWithGoogleOneTapResultsReceiver())

        assertNotNull(testObject.oneTapClient)
        val task = mockk<Task<Void>>(relaxed = true)
        every { oneTapClient.signOut() } returns task
        every { task.addOnSuccessListener(any()) } returns task
        val successListener = slot<OnSuccessListener<Void>>()
        val failureListener = slot<OnFailureListener>()

        clearAllMocks(answers = false)

        testObject.logoutOfGoogle(listener = listener)

        verifySequence {
            googleSignInClient.signOut()
            oneTapClient.signOut()
            task.addOnSuccessListener(capture(successListener))
            task.addOnFailureListener(capture(failureListener))
        }

        //success
        clearAllMocks(answers = false)
        successListener.captured.onSuccess(mockk())
        verifySequence {
            listener.onLogoutSuccess()
        }
        //failure
        clearAllMocks(answers = false)
        every { error.message } returns expectedErrorMessage
        every {
            DependencyFactory.createArcXPException(
                type = ArcXPSDKErrorType.GOOGLE_LOGIN_ERROR,
                message = expectedErrorMessage,
                value = error
            )
        } returns finalError
        failureListener.captured.onFailure(error)
        verifySequence {
            listener.onLogoutError(error = finalError)
        }
    }

    @Test
    fun `logoutOfGoogle key is blank`() {
        every { application.getString(R.string.google_key) } returns "  "
        initializeTestObject()

        testObject.logoutOfGoogle(listener = listener)

        verify { googleSignInClient wasNot called }
        verify { oneTapClient wasNot called }
    }

//    @Test
//    fun `onActivityResults calls back to mgr`() {
//        every { application.getString(R.string.google_key) } returns "  "
//        initializeTestObject()
//        clearAllMocks(answers = false)
//
//        testObject.onActivityResults(
//            requestCode = requestCode,
//            resultCode = resultCode,
//            data = signInIntent,
//            listener = listener
//        )
//
//        verifySequence {
//            callbackManager.onActivityResult(
//                requestCode = requestCode,
//                resultCode = resultCode,
//                data = signInIntent
//            )
//        }
//    }

//    @Test
//    fun `initializePaymentMethod calls api mgr`() {
//        initializeTestObject()
//        clearAllMocks(answers = false)
//
//        testObject.initializePaymentMethod(
//            id = id,
//            pid = pid,
//            listener = salesListener
//        )
//        verifySequence {
//            salesApiManager.initializePaymentMethod(
//                id = id,
//                pid = pid,
//                callback = salesListener
//            )
//        }
//    }

//    @Test
//    fun `finalizePaymentMethod3ds with address calls api mgr`() {
//        val aRequest = mockk<ArcXPAddressRequest>()
//        val request = ArcXPFinalizePaymentRequest(
//            token = token,
//            email = email,
//            address = aRequest,
//            phone = phone,
//            browserInfo = browserInfo,
//            firstName = firstName,
//            lastName = lastName
//        )
//        initializeTestObject()
//        clearAllMocks(answers = false)
//
//        testObject.finalizePaymentMethod3ds(
//            id = id,
//            pid = pid,
//            token = token,
//            email = email,
//            address = aRequest,
//            phone = phone,
//            browserInfo = browserInfo,
//            firstName = firstName,
//            lastName = lastName,
//            listener = salesListener
//        )
//        verifySequence {
//            salesApiManager.finalizePaymentMethod3ds(
//                id = id,
//                pid = pid,
//                request = request,
//                callback = salesListener
//            )
//        }
//    }

//    @Test
//    fun `finalizePaymentMethod3ds with address default values calls api mgr`() {
//        val request = ArcXPFinalizePaymentRequest(
//            token = null,
//            email = null,
//            address = null,
//            phone = null,
//            browserInfo = null,
//            firstName = null,
//            lastName = null
//        )
//        initializeTestObject()
//        clearAllMocks(answers = false)
//
//        testObject.finalizePaymentMethod3ds(
//            id = id,
//            pid = pid,
//            listener = salesListener
//        )
//        verifySequence {
//            salesApiManager.finalizePaymentMethod3ds(
//                id = id,
//                pid = pid,
//                request = request,
//                callback = salesListener
//            )
//        }
//    }

//    @Test
//    fun `finalizePaymentMethod with address calls api mgr`() {
//        val aRequest = mockk<ArcXPAddressRequest>()
//        val request = ArcXPFinalizePaymentRequest(
//            token = token,
//            email = email,
//            address = aRequest,
//            phone = phone,
//            browserInfo = browserInfo,
//            firstName = firstName,
//            lastName = lastName
//        )
//        initializeTestObject()
//        clearAllMocks(answers = false)
//
//        testObject.finalizePaymentMethod(
//            id = id,
//            pid = pid,
//            token = token,
//            email = email,
//            address = aRequest,
//            phone = phone,
//            browserInfo = browserInfo,
//            firstName = firstName,
//            lastName = lastName,
//            listener = salesListener
//        )
//        verifySequence {
//            salesApiManager.finalizePaymentMethod(
//                id = id,
//                pid = pid,
//                request = request,
//                callback = salesListener
//            )
//        }
//    }

//    @Test
//    fun `finalizePaymentMethod with address default values calls api mgr`() {
//        val request = ArcXPFinalizePaymentRequest(
//            token = null,
//            email = null,
//            address = null,
//            phone = null,
//            browserInfo = null,
//            firstName = null,
//            lastName = null
//        )
//        initializeTestObject()
//        clearAllMocks(answers = false)
//
//        testObject.finalizePaymentMethod(
//            id = id,
//            pid = pid,
//            listener = salesListener
//        )
//        verifySequence {
//            salesApiManager.finalizePaymentMethod(
//                id = id,
//                pid = pid,
//                request = request,
//                callback = salesListener
//            )
//        }
//    }

//    @Test
//    fun `finalizePaymentMethod3ds with request calls api manager`() {
//        val request = mockk<ArcXPFinalizePaymentRequest>()
//        initializeTestObject()
//        clearAllMocks(answers = false)
//
//        testObject.finalizePaymentMethod3ds(
//            id = id,
//            pid = pid,
//            request = request,
//            listener = salesListener
//        )
//        verifySequence {
//            salesApiManager.finalizePaymentMethod3ds(
//                id = id,
//                pid = pid,
//                request = request,
//                callback = salesListener
//            )
//        }
//    }

//    @Test
//    fun `finalizePaymentMethod with request calls api manager`() {
//        val request = mockk<ArcXPFinalizePaymentRequest>()
//        initializeTestObject()
//        clearAllMocks(answers = false)
//
//        testObject.finalizePaymentMethod(
//            id = id,
//            pid = pid,
//            request = request,
//            listener = salesListener
//        )
//        verifySequence {
//            salesApiManager.finalizePaymentMethod(
//                id = id,
//                pid = pid,
//                request = request,
//                callback = salesListener
//            )
//        }
//    }

//    @Test
//    fun `finalizePaymentMethod3ds with extended address values calls api manager`() {
//        val aRequest = ArcXPAddressRequest(
//            line1 = addressLine1,
//            line2 = addressLine2,
//            locality = addressLocality,
//            region = addressRegion,
//            postal = addressPostal,
//            country = addressCountry,
//            type = addressType
//        )
//        val request = ArcXPFinalizePaymentRequest(
//            token = token,
//            email = email,
//            address = aRequest,
//            phone = phone,
//            browserInfo = browserInfo,
//            firstName = firstName,
//            lastName = lastName
//        )
//        initializeTestObject()
//        clearAllMocks(answers = false)
//
//        testObject.finalizePaymentMethod3ds(
//            id = id,
//            pid = pid,
//            token = token,
//            email = email,
//            addressLine1 = addressLine1,
//            addressLine2 = addressLine2,
//            addressLocality = addressLocality,
//            addressRegion = addressRegion,
//            addressPostal = addressPostal,
//            addressCountry = addressCountry,
//            addressType = addressType,
//            phone = phone,
//            browserInfo = browserInfo,
//            firstName = firstName,
//            lastName = lastName,
//            listener = salesListener
//        )
//        verifySequence {
//            salesApiManager.finalizePaymentMethod3ds(
//                id = id,
//                pid = pid,
//                request = request,
//                callback = salesListener
//            )
//        }
//    }

//    @Test
//    fun `finalizePaymentMethod3ds with extended address default values calls api manager`() {
//        val aRequest = ArcXPAddressRequest(
//            line1 = addressLine1,
//            line2 = null,
//            locality = addressLocality,
//            region = null,
//            postal = null,
//            country = addressCountry,
//            type = addressType
//        )
//        val request = ArcXPFinalizePaymentRequest(
//            token = null,
//            email = null,
//            address = aRequest,
//            phone = null,
//            browserInfo = null,
//            firstName = null,
//            lastName = null
//        )
//        initializeTestObject()
//        clearAllMocks(answers = false)
//
//        testObject.finalizePaymentMethod3ds(
//            id = id,
//            pid = pid,
//            addressLine1 = addressLine1,
//            addressLocality = addressLocality,
//            addressCountry = addressCountry,
//            addressType = addressType,
//            listener = salesListener
//        )
//        verifySequence {
//            salesApiManager.finalizePaymentMethod3ds(
//                id = id,
//                pid = pid,
//                request = request,
//                callback = salesListener
//            )
//        }
//    }

//    @Test
//    fun `finalizePaymentMethod with extended address values calls api manager`() {
//        val aRequest = ArcXPAddressRequest(
//            line1 = addressLine1,
//            line2 = addressLine2,
//            locality = addressLocality,
//            region = addressRegion,
//            postal = addressPostal,
//            country = addressCountry,
//            type = addressType
//        )
//        val request = ArcXPFinalizePaymentRequest(
//            token = token,
//            email = email,
//            address = aRequest,
//            phone = phone,
//            browserInfo = browserInfo,
//            firstName = firstName,
//            lastName = lastName
//        )
//        initializeTestObject()
//        clearAllMocks(answers = false)
//
//        testObject.finalizePaymentMethod(
//            id = id,
//            pid = pid,
//            token = token,
//            email = email,
//            addressLine1 = addressLine1,
//            addressLine2 = addressLine2,
//            addressLocality = addressLocality,
//            addressRegion = addressRegion,
//            addressPostal = addressPostal,
//            addressCountry = addressCountry,
//            addressType = addressType,
//            phone = phone,
//            browserInfo = browserInfo,
//            firstName = firstName,
//            lastName = lastName,
//            listener = salesListener
//        )
//        verifySequence {
//            salesApiManager.finalizePaymentMethod(
//                id = id,
//                pid = pid,
//                request = request,
//                callback = salesListener
//            )
//        }
//    }

//    @Test
//    fun `finalizePaymentMethod with extended address default values calls api manager`() {
//        val aRequest = ArcXPAddressRequest(
//            line1 = addressLine1,
//            line2 = null,
//            locality = addressLocality,
//            region = null,
//            postal = null,
//            country = addressCountry,
//            type = addressType
//        )
//        val request = ArcXPFinalizePaymentRequest(
//            token = null,
//            email = null,
//            address = aRequest,
//            phone = null,
//            browserInfo = null,
//            firstName = null,
//            lastName = null
//        )
//        initializeTestObject()
//        clearAllMocks(answers = false)
//
//        testObject.finalizePaymentMethod(
//            id = id,
//            pid = pid,
//            addressLine1 = addressLine1,
//            addressLocality = addressLocality,
//            addressCountry = addressCountry,
//            addressType = addressType,
//            listener = salesListener
//        )
//        verifySequence {
//            salesApiManager.finalizePaymentMethod(
//                id = id,
//                pid = pid,
//                request = request,
//                callback = salesListener
//            )
//        }
//    }

//    @Test
//    fun `cancelSubscription request calls api manager`() {
//        val request = mockk<ArcXPCancelSubscriptionRequest>()
//        initializeTestObject()
//        clearAllMocks(answers = false)
//
//        testObject.cancelSubscription(id = id, request = request, listener = salesListener)
//        verifySequence {
//            salesApiManager.cancelSubscription(id = id, request = request, callback = salesListener)
//        }
//    }

//    @Test
//    fun `cancelSubscription reason calls api manager`() {
//        val request = ArcXPCancelSubscriptionRequest(reason = "reason")
//        initializeTestObject()
//        clearAllMocks(answers = false)
//
//        testObject.cancelSubscription(id = id, reason = "reason", listener = salesListener)
//        verifySequence {
//            salesApiManager.cancelSubscription(id = id, request = request, callback = salesListener)
//        }
//    }

//    @Test
//    fun `updateAddress with address calls api manager`() {
//        val request = mockk<ArcXPUpdateAddressRequest>()
//        initializeTestObject()
//        clearAllMocks(answers = false)
//
//        testObject.updateAddress(request = request, listener = salesListener)
//        verifySequence {
//            salesApiManager.updateAddress(request = request, callback = salesListener)
//        }
//    }

//    @Test
//    fun `updateAddress with subscription id calls api manager`() {
//        initializeTestObject()
//        val billingAddress = mockk<ArcXPAddressRequest>()
//        val subscriptionID = 234
//        val expectedRequest = ArcXPUpdateAddressRequest(
//            subscriptionID = subscriptionID,
//            billingAddress = billingAddress
//        )
//        clearAllMocks(answers = false)
//
//        testObject.updateAddress(
//            subscriptionID = subscriptionID,
//            billingAddress = billingAddress,
//            listener = salesListener
//        )
//
//        verifySequence {
//            salesApiManager.updateAddress(
//                request = expectedRequest,
//                callback = salesListener
//            )
//        }
//    }

//    @Test
//    fun `updateAddress with extended address calls api manager`() {
//        initializeTestObject()
//        val subscriptionID = 234
//        val aRequest = ArcXPAddressRequest(
//            line1 = addressLine1,
//            line2 = addressLine2,
//            locality = addressLocality,
//            region = addressRegion,
//            postal = addressPostal,
//            country = addressCountry,
//            type = addressType
//        )
//        val expectedRequest = ArcXPUpdateAddressRequest(
//            subscriptionID = subscriptionID,
//            billingAddress = aRequest
//        )
//        clearAllMocks(answers = false)
//
//        testObject.updateAddress(
//            subscriptionID = subscriptionID,
//            addressLine1 = addressLine1,
//            addressLine2 = addressLine2,
//            addressLocality = addressLocality,
//            addressRegion = addressRegion,
//            addressPostal = addressPostal,
//            addressCountry = addressCountry,
//            addressType = addressType,
//            listener = salesListener
//        )
//
//        verifySequence {
//            salesApiManager.updateAddress(request = expectedRequest, callback = salesListener)
//        }
//    }

//    @Test
//    fun `updateAddress with extended address with defaults calls api manager`() {
//        initializeTestObject()
//        val subscriptionID = 234
//        val aRequest = ArcXPAddressRequest(
//            line1 = addressLine1,
//            line2 = null,
//            locality = addressLocality,
//            region = null,
//            postal = null,
//            country = addressCountry,
//            type = addressType
//        )
//        val expectedRequest = ArcXPUpdateAddressRequest(
//            subscriptionID = subscriptionID,
//            billingAddress = aRequest
//        )
//        clearAllMocks(answers = false)
//
//        testObject.updateAddress(
//            subscriptionID = subscriptionID,
//            addressLine1 = addressLine1,
//            addressLocality = addressLocality,
//            addressCountry = addressCountry,
//            addressType = addressType,
//            listener = salesListener
//        )
//
//        verifySequence {
//            salesApiManager.updateAddress(request = expectedRequest, callback = salesListener)
//        }
//    }

//    @Test
//    fun `getSubscriptionDetails calls api manager`() {
//        initializeTestObject()
//        clearAllMocks(answers = false)
//
//        testObject.getSubscriptionDetails(id = id, listener = salesListener)
//        verifySequence {
//            salesApiManager.getSubscriptionDetails(id = id, callback = salesListener)
//        }
//    }

//    @Test
//    fun `createCustomerOrder calls api manager`() {
//        val shippingAddress = mockk<ArcXPAddressRequest>()
//        val billingAddress = mockk<ArcXPAddressRequest>()
//        val secondLastName = "last name b"
//        val request = ArcXPCustomerOrderRequest(
//            email = email,
//            phone = phone,
//            shippingAddress = shippingAddress,
//            billingAddress = billingAddress,
//            firstName = firstName,
//            lastName = lastName,
//            secondLastName = secondLastName
//        )
//        initializeTestObject()
//        clearAllMocks(answers = false)
//
//        testObject.createCustomerOrder(
//            email = email,
//            phone = phone,
//            shippingAddress = shippingAddress,
//            billingAddress = billingAddress,
//            firstName = firstName,
//            lastName = lastName,
//            secondLastName = secondLastName,
//            listener = salesListener
//        )
//
//        verifySequence {
//            salesApiManager.createCustomerOrder(
//                request = request,
//                callback = salesListener
//            )
//        }
//
//    }

//    @Test
//    fun `createCustomerOrder with extended address calls api manager`() {
//        initializeTestObject()
//        clearAllMocks(answers = false)
//        val shippingAddressLine1 = "ship line 1"
//        val shippingAddressLine2 = "ship line 2"
//        val shippingAddressLocality = "ship locality"
//        val shippingAddressRegion = "ship region"
//        val shippingAddressPostal = "ship postal"
//        val shippingAddressCountry = "ship country"
//        val shippingAddressType = "ship type"
//        val billingAddressLine1 = "bill address 1"
//        val billingAddressLine2 = "bill address 2"
//        val billingAddressLocality = "bill locality"
//        val billingAddressRegion = "bill region"
//        val billingAddressPostal = "bill postal"
//        val billingAddressCountry = "billing country"
//        val billingAddressType = "b type"
//        val secondLastName = "2nd last"
//        val aRequest = ArcXPAddressRequest(
//            line1 = shippingAddressLine1,
//            line2 = shippingAddressLine2,
//            locality = shippingAddressLocality,
//            region = shippingAddressRegion,
//            postal = shippingAddressPostal,
//            country = shippingAddressCountry,
//            type = shippingAddressType
//        )
//        val bRequest = ArcXPAddressRequest(
//            line1 = billingAddressLine1,
//            line2 = billingAddressLine2,
//            locality = billingAddressLocality,
//            region = billingAddressRegion,
//            postal = billingAddressPostal,
//            country = billingAddressCountry,
//            type = billingAddressType
//        )
//        val request = ArcXPCustomerOrderRequest(
//            email = email,
//            phone = phone,
//            shippingAddress = aRequest,
//            billingAddress = bRequest,
//            firstName = firstName,
//            lastName = lastName,
//            secondLastName = secondLastName
//        )
//
//        testObject.createCustomerOrder(
//            email = email,
//            phone = phone,
//            shippingAddressLine1 = shippingAddressLine1,
//            shippingAddressLine2 = shippingAddressLine2,
//            shippingAddressLocality = shippingAddressLocality,
//            shippingAddressRegion = shippingAddressRegion,
//            shippingAddressPostal = shippingAddressPostal,
//            shippingAddressCountry = shippingAddressCountry,
//            shippingAddressType = shippingAddressType,
//            billingAddressLine1 = billingAddressLine1,
//            billingAddressLine2 = billingAddressLine2,
//            billingAddressLocality = billingAddressLocality,
//            billingAddressRegion = billingAddressRegion,
//            billingAddressPostal = billingAddressPostal,
//            billingAddressCountry = billingAddressCountry,
//            billingAddressType = billingAddressType,
//            firstName = firstName,
//            lastName = lastName,
//            secondLastName = secondLastName,
//            listener = salesListener
//        )
//
//        verifySequence {
//            salesApiManager.createCustomerOrder(request = request, callback = salesListener)
//        }
//    }

//    @Test
//    fun `createCustomerOrder with extended address defaults calls api manager`() {
//        initializeTestObject()
//        clearAllMocks(answers = false)
//        val shippingAddressLine1 = "ship line 1"
//        val shippingAddressLocality = "ship locality"
//        val shippingAddressCountry = "ship country"
//        val shippingAddressType = "ship type"
//        val billingAddressLine1 = "bill address 1"
//        val billingAddressLocality = "bill locality"
//        val billingAddressCountry = "billing country"
//        val billingAddressType = "b type"
//        val secondLastName = "2nd last"
//        val aRequest = ArcXPAddressRequest(
//            line1 = shippingAddressLine1,
//            line2 = null,
//            locality = shippingAddressLocality,
//            region = null,
//            postal = null,
//            country = shippingAddressCountry,
//            type = shippingAddressType
//        )
//        val bRequest = ArcXPAddressRequest(
//            line1 = billingAddressLine1,
//            line2 = null,
//            locality = billingAddressLocality,
//            region = null,
//            postal = null,
//            country = billingAddressCountry,
//            type = billingAddressType
//        )
//        val request = ArcXPCustomerOrderRequest(
//            email = email,
//            phone = phone,
//            shippingAddress = aRequest,
//            billingAddress = bRequest,
//            firstName = firstName,
//            lastName = lastName,
//            secondLastName = secondLastName
//        )
//
//        testObject.createCustomerOrder(
//            email = email,
//            phone = phone,
//            shippingAddressLine1 = shippingAddressLine1,
//            shippingAddressLocality = shippingAddressLocality,
//            shippingAddressCountry = shippingAddressCountry,
//            shippingAddressType = shippingAddressType,
//            billingAddressLine1 = billingAddressLine1,
//            billingAddressLocality = billingAddressLocality,
//            billingAddressCountry = billingAddressCountry,
//            billingAddressType = billingAddressType,
//            firstName = firstName,
//            lastName = lastName,
//            secondLastName = secondLastName,
//            listener = salesListener
//        )
//
//        verifySequence {
//            salesApiManager.createCustomerOrder(request = request, callback = salesListener)
//        }
//    }

//    @Test
//    fun `getPaymentOptions calls api manager`() {
//        initializeTestObject()
//        clearAllMocks(answers = false)
//
//        testObject.getPaymentOptions(listener = salesListener)
//        verifySequence {
//            salesApiManager.getPaymentOptions(callback = salesListener)
//        }
//    }

//    @Test
//    fun `getPaymentAddresses calls api manager`() {
//        initializeTestObject()
//        clearAllMocks(answers = false)
//
//        testObject.getPaymentAddresses(listener = salesListener)
//        verifySequence {
//            salesApiManager.getPaymentAddresses(callback = salesListener)
//        }
//    }

//    @Test
//    fun `initializePayment calls api manager`() {
//        initializeTestObject()
//        clearAllMocks(answers = false)
//
//        testObject.initializePayment(orderNumber = orderNumber, mid = mid, listener = salesListener)
//        verifySequence {
//            salesApiManager.initializePayment(
//                orderNumber = orderNumber,
//                mid = mid,
//                callback = salesListener
//            )
//        }
//    }

//    @Test
//    fun `finalizePayment with request calls api manager`() {
//        initializeTestObject()
//        val request = mockk<ArcXPFinalizePaymentRequest>()
//        clearAllMocks(answers = false)
//
//        testObject.finalizePayment(
//            orderNumber = orderNumber,
//            mid = mid,
//            request = request,
//            listener = salesListener
//        )
//        verifySequence {
//            salesApiManager.finalizePayment(
//                orderNumber = orderNumber,
//                mid = mid,
//                request = request,
//                callback = salesListener
//            )
//        }
//    }

//    @Test
//    fun `finalizePayment with address calls api manager`() {
//        initializeTestObject()
//        val billingAddress = mockk<ArcXPAddressRequest>()
//        val expectedPaymentRequest = ArcXPFinalizePaymentRequest(
//            token = token,
//            email = email,
//            address = billingAddress,
//            phone,
//            browserInfo,
//            firstName,
//            lastName
//        )
////        val expectedRequest = ArcXPCartItemsRequest(items = items, billingAddress = billingAddress)
//        clearAllMocks(answers = false)
//
//        testObject.finalizePayment(
//            orderNumber = orderNumber,
//            mid = mid,
//            token = token,
//            email = email,
//            address = billingAddress,
//            phone = phone,
//            browserInfo = browserInfo,
//            firstName = firstName,
//            lastName = lastName,
//            listener = salesListener
//        )
//
//        verifySequence {
//            salesApiManager.finalizePayment(
//                orderNumber,
//                mid,
//                expectedPaymentRequest,
//                salesListener
//            )
//        }
//    }

//    @Test
//    fun `finalizePayment with extended user info calls api manager`() {
//        initializeTestObject()
//        val billingAddress = ArcXPAddressRequest(
//            addressLine1, addressLine2, addressLocality, addressRegion,
//            addressPostal, addressCountry, addressType
//        )
//        val expectedPaymentRequest = ArcXPFinalizePaymentRequest(
//            token = token,
//            email = email,
//            address = billingAddress,
//            phone,
//            browserInfo,
//            firstName,
//            lastName
//        )
//        clearAllMocks(answers = false)
//
//        testObject.finalizePayment(
//            orderNumber = orderNumber,
//            mid = mid,
//            token = token,
//            email = email,
//            addressLine1 = addressLine1,
//            addressLine2 = addressLine2,
//            addressLocality = addressLocality,
//            addressRegion = addressRegion,
//            addressPostal = addressPostal,
//            addressCountry = addressCountry,
//            addressType = addressType,
//            phone = phone,
//            browserInfo = browserInfo,
//            firstName = firstName,
//            lastName = lastName,
//            listener = salesListener
//        )
//        verifySequence {
//            salesApiManager.finalizePayment(
//                orderNumber = orderNumber,
//                mid = mid,
//                request = expectedPaymentRequest,
//                callback = salesListener
//            )
//        }
//
//    }

//    @Test
//    fun `finalizePayment3ds with payment request calls api manager`() {
//        initializeTestObject()
//        val request = mockk<ArcXPFinalizePaymentRequest>()
//        clearAllMocks(answers = false)
//
//        testObject.finalizePayment3ds(
//            orderNumber = orderNumber,
//            mid = mid,
//            request = request,
//            listener = salesListener
//        )
//        verifySequence {
//            salesApiManager.finalizePayment3ds(
//                orderNumber = orderNumber,
//                mid = mid,
//                request = request,
//                callback = salesListener
//            )
//        }
//    }

//    @Test
//    fun `finalizePayment3ds with address request calls api manager`() {
//        initializeTestObject()
//        val billingAddress = mockk<ArcXPAddressRequest>()
//        val expectedPaymentRequest = ArcXPFinalizePaymentRequest(
//            token = token,
//            email = email,
//            address = billingAddress,
//            phone,
//            browserInfo,
//            firstName,
//            lastName
//        )
////        val expectedRequest = ArcXPCartItemsRequest(items = items, billingAddress = billingAddress)
//        clearAllMocks(answers = false)
//
//        testObject.finalizePayment3ds(
//            orderNumber = orderNumber,
//            mid = mid,
//            token = token,
//            email = email,
//            address = billingAddress,
//            phone = phone,
//            browserInfo = browserInfo,
//            firstName = firstName,
//            lastName = lastName,
//            listener = salesListener
//        )
//
//        verifySequence {
//            salesApiManager.finalizePayment3ds(
//                orderNumber,
//                mid,
//                expectedPaymentRequest,
//                salesListener
//            )
//        }
//    }

//    @Test
//    fun `finalizePayment3ds with extended user info calls api manager`() {
//
//        initializeTestObject()
//        val billingAddress = ArcXPAddressRequest(
//            addressLine1, addressLine2, addressLocality, addressRegion,
//            addressPostal, addressCountry, addressType
//        )
//        val expectedPaymentRequest = ArcXPFinalizePaymentRequest(
//            token = token,
//            email = email,
//            address = billingAddress,
//            phone,
//            browserInfo,
//            firstName,
//            lastName
//        )
////        val expectedRequest = ArcXPCartItemsRequest(items = items, billingAddress = billingAddress)
//        clearAllMocks(answers = false)
//
//        testObject.finalizePayment3ds(
//            orderNumber = orderNumber,
//            mid = mid,
//            token = token,
//            email = email,
//            addressLine1 = addressLine1,
//            addressLine2 = addressLine2,
//            addressLocality = addressLocality,
//            addressRegion = addressRegion,
//            addressPostal = addressPostal,
//            addressCountry = addressCountry,
//            addressType = addressType,
//            phone = phone,
//            browserInfo = browserInfo,
//            firstName = firstName,
//            lastName = lastName,
//            listener = salesListener
//        )
//
//        verifySequence {
//            salesApiManager.finalizePayment3ds(
//                orderNumber,
//                mid,
//                expectedPaymentRequest,
//                salesListener
//            )
//        }
//    }

//    @Test
//    fun `getOrderHistory calls api manager`() {
//        initializeTestObject()
//        clearAllMocks(answers = false)
//
//        testObject.getOrderHistory(listener = salesListener)
//        verifySequence {
//            salesApiManager.getOrderHistory(callback = salesListener)
//        }
//    }

//    @Test
//    fun `getOrderDetails calls api manager`() {
//        initializeTestObject()
//        clearAllMocks(answers = false)
//
//        testObject.getOrderDetails(orderNumber = "order#", listener = salesListener)
//        verifySequence {
//            salesApiManager.getOrderDetails(callback = salesListener, orderNumber = "order#")
//        }
//    }

//    @Test
//    fun `clearCart calls api manager`() {
//        initializeTestObject()
//        clearAllMocks(answers = false)
//
//        testObject.clearCart(listener = salesListener)
//        verifySequence {
//            salesApiManager.clearCart(callback = salesListener)
//        }
//    }

//    @Test
//    fun `getCurrentCart calls api manager`() {
//        initializeTestObject()
//        clearAllMocks(answers = false)
//
//        testObject.getCurrentCart(listener = salesListener)
//        verifySequence {
//            salesApiManager.getCurrentCart(callback = salesListener)
//        }
//
//    }

//    @Test
//    fun `addItemToCart calls api manager`() {
//        initializeTestObject()
//        clearAllMocks(answers = false)
//        val request = mockk<ArcXPCartItemsRequest>()
//
//        testObject.addItemToCart(request = request, listener = salesListener)
//
//        verifySequence {
//            salesApiManager.addItemToCart(request = request, callback = salesListener)
//        }
//    }

//    @Test
//    fun `addItemToCart extended address calls api manager`() {
//        val cartItem1 = mockk<CartItem>()
//        val cartItem2 = mockk<CartItem>()
//        val cartItem3 = mockk<CartItem>()
//
////        listener: ArcXPSalesListener?
//        val items = listOf(cartItem1, cartItem2, cartItem3)
//        initializeTestObject()
//        val billingAddress = ArcXPAddressRequest(
//            addressLine1, addressLine2, addressLocality, addressRegion,
//            addressPostal, addressCountry, addressType
//        )
//        val expectedRequest = ArcXPCartItemsRequest(items = items, billingAddress = billingAddress)
//        clearAllMocks(answers = false)
//
//        testObject.addItemToCart(
//            items = items,
//            addressLine1 = addressLine1,
//            addressLine2 = addressLine2,
//            addressLocality = addressLocality,
//            addressRegion = addressRegion,
//            addressPostal = addressPostal,
//            addressCountry = addressCountry,
//            addressType = addressType,
//            listener = salesListener
//        )
//
//        verifySequence {
//            salesApiManager.addItemToCart(request = expectedRequest, callback = salesListener)
//        }
//    }

//    @Test
//    fun `addItemToCart with items calls api manager`() {
//        val cartItem1 = mockk<CartItem>()
//        val cartItem2 = mockk<CartItem>()
//        val cartItem3 = mockk<CartItem>()
//        val items = listOf(cartItem1, cartItem2, cartItem3)
//        initializeTestObject()
//        val billingAddress = mockk<ArcXPAddressRequest>()
//        val expectedRequest = ArcXPCartItemsRequest(items = items, billingAddress = billingAddress)
//        clearAllMocks(answers = false)
//
//        testObject.addItemToCart(
//            items = items,
//            billingAddress = billingAddress,
//            listener = salesListener
//        )
//
//        verifySequence {
//            salesApiManager.addItemToCart(request = expectedRequest, callback = salesListener)
//        }
//
//    }

//    @Test
//    fun `removeItemFromCart calls api manager`() {
//        initializeTestObject()
//        clearAllMocks(answers = false)
//
//        testObject.removeItemFromCart(sku = "sku", listener = salesListener)
//        verifySequence { salesApiManager.removeItemFromCart(sku = "sku", callback = salesListener) }
//    }

    private fun initializeTestObject() {
        testObject = ArcXPCommerceManager.initialize(
            context = application,
            clientCachedData = clientCachedData,
            config = config
        )
    }
}