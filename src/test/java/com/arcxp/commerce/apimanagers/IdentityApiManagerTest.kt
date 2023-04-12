package com.arcxp.commerce.apimanagers

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import com.arcxp.commerce.ArcXPCommerceConfig
import com.arcxp.commerce.callbacks.ArcXPIdentityListener
import com.arcxp.commerce.extendedModels.ArcXPProfileManage
import com.arcxp.commerce.models.*
import com.arcxp.commerce.util.AuthManager
import com.arcxp.commerce.viewmodels.IdentityViewModel
import com.arcxp.commons.throwables.ArcXPException
import com.arcxp.commons.util.DependencyFactory
import io.mockk.*
import io.mockk.impl.annotations.RelaxedMockK
import junit.framework.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class IdentityApiManagerTest {

    @get:Rule
    var instantExecuteRule = InstantTaskExecutorRule()

    @RelaxedMockK
    private lateinit var listener: ArcXPIdentityListener

    @RelaxedMockK
    private lateinit var authManager: AuthManager

    @RelaxedMockK
    private lateinit var viewModel: IdentityViewModel

    @RelaxedMockK
    private lateinit var fragment: Fragment

    @io.mockk.impl.annotations.MockK
    private lateinit var lifecycleOwner: LifecycleOwner


    private val userName = "user"
    private val pw = "pw"
    private val firstName = "1st"
    private val lastName = "last"
    private val email = "email"
    private val refreshToken = "token"
    private val nonce = "123"
    private val recaptchaToken = "abc"
    private val grantType = "gt"
    private val token = "token"
    private val grantTypeEnum = ArcXPAuthRequest.Companion.GrantType.REFRESH_TOKEN

    private lateinit var testObject: IdentityApiManager

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        mockkObject(DependencyFactory)
        every { DependencyFactory.createIdentityViewModel(authManager = authManager) } returns viewModel
        every { DependencyFactory.createIdentityRepository() } returns mockk()
        testObject = IdentityApiManager(authManager, viewModel)
        every { viewModel.nonce } returns nonce
        every { viewModel.recaptchaToken } returns recaptchaToken
        every { fragment.viewLifecycleOwner } returns lifecycleOwner
    }

    @Test
    fun `verify changePassword is called 1 time with successful response - changePassword`() {
        val response = mockk<ArcXPIdentity>()
        val captureCallback = slot<ArcXPIdentityListener>()

        testObject.changePassword("b", "a", listener)

        verify(exactly = 1) {
            viewModel.changeUserPassword("a", "b", capture(captureCallback))
        }
        captureCallback.captured.onPasswordChangeSuccess(response)
        verify {
            listener.onPasswordChangeSuccess(response)
        }
    }

    @Test
    fun `verify changePassword is called 1 time with failed response - changePassword`() {
        val response = mockk<ArcXPException>()
        val captureCallback = slot<ArcXPIdentityListener>()

        testObject.changePassword("b", "a", listener)

        verify(exactly = 1) {
            viewModel.changeUserPassword("a", "b", capture(captureCallback))
        }
        captureCallback.captured.onPasswordChangeError(response)
        verify {
            listener.onPasswordChangeError(response)
        }
    }

    @Test
    fun `verify obtainNonceByEmailAddress is called 1 time with successful response - obtainNonceByEmailAddress`() {
        val response = mockk<ArcXPRequestPasswordReset>()
        val captureCallback = slot<ArcXPIdentityListener>()

        testObject.obtainNonceByEmailAddress("a", listener)

        verify(exactly = 1) {
            viewModel.obtainNonceByEmailAddress("a", capture(captureCallback))
        }
        captureCallback.captured.onPasswordResetNonceSuccess(response)
        verify {
            listener.onPasswordResetNonceSuccess(response)
        }
    }

    @Test
    fun `verify obtainNonceByEmailAddress is called 1 time with failed response - obtainNonceByEmailAddress`() {
        val response = mockk<ArcXPException>()
        val captureCallback = slot<ArcXPIdentityListener>()

        testObject.obtainNonceByEmailAddress("a", listener)

        verify(exactly = 1) {
            viewModel.obtainNonceByEmailAddress("a", capture(captureCallback))
        }
        captureCallback.captured.onPasswordResetNonceFailure(response)
        verify {
            listener.onPasswordResetNonceFailure(response)
        }
    }

    @Test
    fun `verify resetPasswordByNonce is called 1 time with successful response - resetPasswordByNonce`() {
        val response = mockk<ArcXPIdentity>()
        val captureCallback = slot<ArcXPIdentityListener>()

        testObject.resetPasswordByNonce("a", "b", listener)

        verify(exactly = 1) {
            viewModel.resetPasswordByNonce("a", "b", capture(captureCallback))
        }
        captureCallback.captured.onPasswordResetSuccess(response)
        verify {
            listener.onPasswordResetSuccess(response)
        }
    }

    @Test
    fun `verify resetPasswordByNonce is called 1 time with failed response - resetPasswordByNonce`() {
        val response = mockk<ArcXPException>()
        val captureCallback = slot<ArcXPIdentityListener>()

        testObject.resetPasswordByNonce("a", "b", listener)

        verify(exactly = 1) {
            viewModel.resetPasswordByNonce("a", "b", capture(captureCallback))
        }
        captureCallback.captured.onPasswordResetError(response)
        verify {
            listener.onPasswordResetError(response)
        }
    }

    @Test
    fun `verify makeLoginCall is called 1 time with successful response - login`() {
        val response = mockk<ArcXPAuth>()
        val captureCallback = slot<ArcXPIdentityListener>()

        testObject.login("a", "b", listener)

        verify(exactly = 1) {
            viewModel.makeLoginCall("a", "b", any(), capture(captureCallback))
        }
        captureCallback.captured.onLoginSuccess(response)
        verify {
            listener.onLoginSuccess(response)
        }
    }

    @Test
    fun `verify makeLoginCall is called 1 time with failed response - login`() {
        val response = mockk<ArcXPException>()
        val captureCallback = slot<ArcXPIdentityListener>()

        testObject.login("a", "b", listener)

        verify(exactly = 1) {
            viewModel.makeLoginCall("a", "b", any(), capture(captureCallback))
        }
        captureCallback.captured.onLoginError(response)
        verify {
            listener.onLoginError(response)
        }
    }

    @Test
    fun `verify setRecaptchaToken is sent to viewModel - setRecaptchaToken`() {
        testObject.setRecaptchaToken("123abc")
    }

    @Test
    fun `verify getCallBackScheme does nothing when containing frag`() {
        testObject.login("a", "b", listener)

        verify(exactly = 1) {
            viewModel.makeLoginCall("a", "b", any(), any())
        }
    }

    @Test
    fun `verify thirdPartyLogin is called once in viewmodel - thirdPartyLogin`() {
        testObject.thirdPartyLogin(
            token = token,
            type = grantTypeEnum,
            arcIdentityListener = listener
        )

        verify(exactly = 1) {
            viewModel.thirdPartyLoginCall(
                accessToken = token,
                grantType = grantTypeEnum,
                callback = listener
            )
        }
    }

    @Test
    fun `verify thirdPartyLoginCall is called 1 time with successful response - thirdPartyLogin`() {
        val response = mockk<ArcXPAuth>()
        val captureCallback = slot<ArcXPIdentityListener>()

        testObject.thirdPartyLogin("", mockk(), listener)

        verify(exactly = 1) {
            viewModel.thirdPartyLoginCall("", any(), capture(captureCallback))
        }
        captureCallback.captured.onLoginSuccess(response)
        verify {
            listener.onLoginSuccess(response)
        }
    }

    @Test
    fun `verify thirdPartyLoginCall is called 1 time with failed response - thirdPartyLogin`() {
        val response = mockk<ArcXPException>()
        val captureCallback = slot<ArcXPIdentityListener>()

        testObject.thirdPartyLogin("", mockk(), listener)

        verify(exactly = 1) {
            viewModel.thirdPartyLoginCall("", any(), capture(captureCallback))
        }
        captureCallback.captured.onLoginError(response)
        verify {
            listener.onLoginError(response)
        }
    }

    @Test
    fun `verify verifyEmailCall is called 1 time with successful response - sendVerificationEmail`() {
        val response = mockk<ArcXPEmailVerification>()
        val captureCallback = slot<ArcXPIdentityListener>()

        testObject.sendVerificationEmail("", listener)

        verify(exactly = 1) {
            viewModel.verifyEmailCall("", capture(captureCallback))
        }
        captureCallback.captured.onEmailVerificationSentSuccess(response)
        verify {
            listener.onEmailVerificationSentSuccess(response)
        }
    }

    @Test
    fun `verify verifyEmailCall is called 1 time with failed response - sendVerificationEmail`() {
        val response = mockk<ArcXPException>()
        val captureCallback = slot<ArcXPIdentityListener>()

        testObject.sendVerificationEmail("", listener)

        verify(exactly = 1) {
            viewModel.verifyEmailCall("", capture(captureCallback))
        }
        captureCallback.captured.onEmailVerificationSentError(response)
        verify {
            listener.onEmailVerificationSentError(response)
        }
    }


    @Test
    fun `verify verifyEmail is called once in viewmodel with successful response - verifyEmail`() {
        val response = mockk<ArcXPEmailVerification>()
        val captureCallback = slot<ArcXPIdentityListener>()

        testObject.verifyEmail("", listener)

        verify(exactly = 1) {
            viewModel.verifyEmail("", capture(captureCallback))
        }
        captureCallback.captured.onEmailVerifiedSuccess(response)
        verify {
            listener.onEmailVerifiedSuccess(response)
        }
    }

    @Test
    fun `verify verifyEmail is called once in viewmodel with failed response - verifyEmail`() {
        val error = mockk<ArcXPException>()
        val captureCallback = slot<ArcXPIdentityListener>()

        testObject.verifyEmail("", listener)

        verify(exactly = 1) {
            viewModel.verifyEmail("", capture(captureCallback))
        }
        captureCallback.captured.onEmailVerifiedError(error)
        verify {
            listener.onEmailVerifiedError(error)
        }
    }

    @Test
    fun `verify getNonce returns nonce successful response - getNonce`() {
        val expected = nonce
        val actual = testObject.getNonce()
        assertEquals("Nonce is returned", expected, actual)
    }

    @Test
    fun `verify getMagicLink is called once in viewmodel with successful response - getMagicLink`() {
        val response = mockk<ArcXPOneTimeAccessLink>()
        val captureCallback = slot<ArcXPIdentityListener>()

        testObject.getMagicLink(email = email, listener = listener)

        verify(exactly = 1) {
            viewModel.getMagicLink(
                email = email,
                recaptchaToken = recaptchaToken,
                callback = capture(captureCallback)
            )
        }
        captureCallback.captured.onOneTimeAccessLinkSuccess(response)
        verify {
            listener.onOneTimeAccessLinkSuccess(response)
        }
    }

    @Test
    fun `verify getMagicLink is called once in viewmodel with failed response - getMagicLink`() {
        val response = mockk<ArcXPException>()
        val captureCallback = slot<ArcXPIdentityListener>()

        testObject.getMagicLink(email = email, listener = listener)

        verify(exactly = 1) {
            viewModel.getMagicLink(
                email = email,
                recaptchaToken = recaptchaToken,
                callback = capture(captureCallback)
            )
        }
        captureCallback.captured.onOneTimeAccessLinkError(response)
        verify {
            listener.onOneTimeAccessLinkError(response)
        }
    }

    @Test
    fun `verify loginMagicLink is called once in viewmodel with successful response - loginMagicLink`() {
        val response = mockk<ArcXPOneTimeAccessLinkAuth>()
        val captureCallback = slot<ArcXPIdentityListener>()

        testObject.loginMagicLink(nonce = nonce, listener = listener)

        verify(exactly = 1) {
            viewModel.loginMagicLink(nonce = nonce, callback = capture(captureCallback))
        }
        captureCallback.captured.onOneTimeAccessLinkLoginSuccess(response)
        verify {
            listener.onOneTimeAccessLinkLoginSuccess(response)
        }
    }


    @Test
    fun `verify loginMagicLink is called once in viewmodel with failed response - loginMagicLink`() {
        val response = mockk<ArcXPException>()
        val captureCallback = slot<ArcXPIdentityListener>()

        testObject.loginMagicLink(nonce = nonce, listener = listener)

        verify(exactly = 1) {
            viewModel.loginMagicLink(nonce = nonce, callback = capture(captureCallback))
        }
        captureCallback.captured.onOneTimeAccessLinkError(response)
        verify {
            listener.onOneTimeAccessLinkError(response)
        }
    }

    @Test
    fun `verify patchProfile is called once in viewmodel with successful response - updateProfile`() {
        val response = mockk<ArcXPProfileManage>()
        val captureCallback = slot<ArcXPIdentityListener>()
        val update = mockk<ArcXPProfilePatchRequest>()

        testObject.updateProfile(update = update, listener = listener)

        verify(exactly = 1) {
            viewModel.patchProfile(
                profilePatchRequest = update,
                callback = capture(captureCallback)
            )
        }
        captureCallback.captured.onProfileUpdateSuccess(response)
        verify {
            listener.onProfileUpdateSuccess(response)
        }
    }

    @Test
    fun `verify patchProfile is called once in viewmodel with failed response - updateProfile`() {
        val response = mockk<ArcXPException>()
        val captureCallback = slot<ArcXPIdentityListener>()
        val update = mockk<ArcXPProfilePatchRequest>()

        testObject.updateProfile(update = update, listener = listener)

        verify(exactly = 1) {
            viewModel.patchProfile(profilePatchRequest = update, capture(captureCallback))
        }
        captureCallback.captured.onProfileError(response)
        verify {
            listener.onProfileError(response)
        }
    }

    @Test
    fun `verify getProfile is called once in viewmodel with successful response - getProfile`() {
        val response = mockk<ArcXPProfileManage>()
        val captureCallback = slot<ArcXPIdentityListener>()

        testObject.getProfile(listener)

        verify(exactly = 1) {
            viewModel.getProfile(capture(captureCallback))
        }
        captureCallback.captured.onFetchProfileSuccess(response)
        verify {
            listener.onFetchProfileSuccess(response)
        }
    }

    @Test
    fun `verify getProfile is called once in viewmodel with failed response - getProfile`() {
        val response = mockk<ArcXPException>()
        val captureCallback = slot<ArcXPIdentityListener>()

        testObject.getProfile(listener)

        verify(exactly = 1) {
            viewModel.getProfile(capture(captureCallback))
        }
        captureCallback.captured.onProfileError(response)
        verify {
            listener.onProfileError(response)
        }
    }

    @Test
    fun `registerUser - verify makeRegistrationCall is called once in viewmodel with successful response`() {
        val response = mockk<ArcXPUser>()
        val captureCallback = slot<ArcXPIdentityListener>()

        testObject.registerUser(
            username = userName,
            password = pw,
            email = email,
            firstname = firstName,
            lastname = lastName,
            listener = listener
        )

        verify(exactly = 1) {
            viewModel.makeRegistrationCall(
                userName = userName,
                password = pw,
                email = email,
                firstName = firstName,
                lastName = lastName,
                callback = capture(captureCallback)
            )
        }
        captureCallback.captured.onRegistrationSuccess(response)
        verify {
            listener.onRegistrationSuccess(response)
        }
    }

    @Test
    fun `registerUser - verify makeRegistrationCall is called once in viewmodel with successful response (default first,last name)`() {
        val response = mockk<ArcXPUser>()
        val captureCallback = slot<ArcXPIdentityListener>()

        testObject.registerUser(
            username = userName,
            password = pw,
            email = email,
            listener = listener
        )

        verify(exactly = 1) {
            viewModel.makeRegistrationCall(
                userName = userName,
                password = pw,
                email = email,
                firstName = null,
                lastName = null,
                callback = capture(captureCallback)
            )
        }
        captureCallback.captured.onRegistrationSuccess(response)
        verify {
            listener.onRegistrationSuccess(response)
        }
    }

    @Test
    fun `registerUser - verify makeRegistrationCall is called once in viewmodel with failed response`() {
        val response = mockk<ArcXPException>()
        val captureCallback = slot<ArcXPIdentityListener>()

        testObject.registerUser(
            username = userName,
            password = pw,
            email = email,
            firstname = firstName,
            lastname = lastName,
            listener = listener
        )

        verify(exactly = 1) {
            viewModel.makeRegistrationCall(
                userName = userName,
                password = pw,
                email = email,
                firstName = firstName,
                lastName = lastName,
                callback = capture(captureCallback)
            )
        }
        captureCallback.captured.onRegistrationError(response)
        verify {
            listener.onRegistrationError(response)
        }
    }

    @Test
    fun `verify logout is called once in viewmodel with successful response - logout`() {
        val captureCallback = slot<ArcXPIdentityListener>()

        testObject.logout(listener = listener)

        verify(exactly = 1) {
            viewModel.logout(callback = capture(lst = captureCallback))
        }
        captureCallback.captured.onLogoutSuccess()
        verify {
            listener.onLogoutSuccess()
        }
    }

    @Test
    fun `verify logout is called once in viewmodel with failed response - logout`() {
        val response = mockk<ArcXPException>()
        val captureCallback = slot<ArcXPIdentityListener>()

        testObject.logout(listener)

        verify(exactly = 1) {
            viewModel.logout(capture(captureCallback))
        }
        captureCallback.captured.onLogoutError(response)
        verify {
            listener.onLogoutError(response)
        }
    }

    @Test
    fun `verify deleteUser is called once in viewmodel with successful response - deleteUser`() {
        val captureCallback = slot<ArcXPIdentityListener>()

        testObject.deleteUser(listener)

        verify(exactly = 1) {
            viewModel.deleteUser(capture(captureCallback))
        }
        captureCallback.captured.onDeleteUserSuccess()
        verify {
            listener.onDeleteUserSuccess()
        }
    }

    @Test
    fun `verify deleteUser is called once in viewmodel with failed response - deleteUser`() {
        val response = mockk<ArcXPException>()
        val captureCallback = slot<ArcXPIdentityListener>()

        testObject.deleteUser(listener)

        verify(exactly = 1) {
            viewModel.deleteUser(capture(captureCallback))
        }
        captureCallback.captured.onDeleteUserError(response)
        verify {
            listener.onDeleteUserError(response)
        }
    }

    @Test
    fun `verify approveDeletion is called once in viewmodel with successful response - approveDeletion`() {
        val response = mockk<ArcXPDeleteUser>()
        val captureCallback = slot<ArcXPIdentityListener>()

        testObject.approveDeletion("", listener)

        verify(exactly = 1) {
            viewModel.approveDeletion("", capture(captureCallback))
        }
        captureCallback.captured.onApproveDeletionSuccess(response)
        verify {
            listener.onApproveDeletionSuccess(response)
        }
    }

    @Test
    fun `verify approveDeletion is called once in viewmodel with failed response - approveDeletion`() {
        val response = mockk<ArcXPException>()
        val captureCallback = slot<ArcXPIdentityListener>()

        testObject.approveDeletion(nonce = nonce, listener = listener)
        verify(exactly = 1) {
            viewModel.approveDeletion(nonce = nonce, listener = capture(captureCallback))
        }
        captureCallback.captured.onApproveDeletionError(response)
        verify {
            listener.onApproveDeletionError(response)
        }
    }

    @Test
    fun `validateJwt token - verify validateJwt is called once in viewmodel`() {
        testObject.validateJwt(token = refreshToken, arcIdentityListener = listener)

        verify(exactly = 1) {
            viewModel.validateJwt(token = refreshToken, callback = listener)
        }
    }

    @Test
    fun `validateJwt token - verify validateJwt is called once in viewmodel non null fragment`() {


        testObject.validateJwt(token = refreshToken, arcIdentityListener = listener)
        verify(exactly = 1) {
            viewModel.validateJwt(token = refreshToken, callback = listener)
        }
    }

    @Test
    fun `validateJwt listener(no token) success - verify validateJwt is called once in viewmodel`() {
        val captureCallback = slot<ArcXPIdentityListener>()
        clearAllMocks(answers = false)

        testObject.validateJwt(listener)

        verifySequence {
            viewModel.validateJwt(capture(captureCallback))
        }
        clearAllMocks(answers = false)
        captureCallback.captured.onValidateSessionSuccess()
        verifySequence {
            listener.onValidateSessionSuccess()
            listener.onIsLoggedIn(result = true)
        }
    }

    @Test
    fun `validateJwt listener(no token) failure - verify listener success`() {
        every { authManager.refreshToken } returns refreshToken
        val captureCallback = slot<ArcXPIdentityListener>()
        val refreshCallback = slot<ArcXPIdentityListener>()
        clearAllMocks(answers = false)

        testObject.validateJwt(listener)

        verifySequence {
            viewModel.validateJwt(callback = capture(captureCallback))
        }
        clearAllMocks(answers = false)
        captureCallback.captured.onValidateSessionError(error = mockk())
        verifySequence {
            viewModel.refreshToken(
                token = refreshToken, grantType =
                ArcXPAuthRequest.Companion.GrantType.REFRESH_TOKEN.value, capture(refreshCallback)
            )
        }
        clearAllMocks(answers = false)
        refreshCallback.captured.onRefreshSessionSuccess(mockk())
        verifySequence {
            listener.onValidateSessionSuccess()
            listener.onIsLoggedIn(result = true)
        }
    }

    @Test
    fun `validateJwt listener(no token) failure - verify listener failure`() {
        every { authManager.refreshToken } returns refreshToken
        val captureCallback = slot<ArcXPIdentityListener>()
        val refreshCallback = slot<ArcXPIdentityListener>()
        clearAllMocks(answers = false)

        testObject.validateJwt(listener)

        verifySequence {
            viewModel.validateJwt(callback = capture(captureCallback))
        }
        clearAllMocks(answers = false)
        captureCallback.captured.onValidateSessionError(error = mockk())
        verifySequence {
            viewModel.refreshToken(
                token = refreshToken,
                grantType =
                ArcXPAuthRequest.Companion.GrantType.REFRESH_TOKEN.value,
                callback = capture(refreshCallback)
            )
        }
        clearAllMocks(answers = false)
        refreshCallback.captured.onRefreshSessionFailure(error = mockk())
        verifySequence {
            listener.onIsLoggedIn(result = false)
        }
    }

    @Test
    fun `verify refreshToken is called once in viewmodel with successful response - refreshToken`() {
        val response = mockk<ArcXPAuth>()
        val captureCallback = slot<ArcXPIdentityListener>()

        testObject.refreshToken(
            token = refreshToken,
            grantType = grantType,
            arcIdentityListener = listener
        )
        verify(exactly = 1) {
            viewModel.refreshToken(
                token = refreshToken,
                grantType = grantType,
                capture(captureCallback)
            )
        }
        captureCallback.captured.onRefreshSessionSuccess(response)
        verify {
            listener.onRefreshSessionSuccess(response)
        }
    }

    @Test
    fun `verify rememberUser is sent to viewModel - rememberUser`() {
        testObject.rememberUser(remember = true)

        verify(exactly = 1) {
            viewModel.rememberUser(remember = true)
        }
    }

    @Test
    fun `verify getTenetConfig is called once in viewmodel with successful response - loadConfig`() {
        val response = mockk<ArcXPConfig>()
        val captureCallback = slot<ArcXPIdentityListener>()

        testObject.loadConfig(listener)

        verify(exactly = 1) {
            viewModel.getTenetConfig(capture(captureCallback))
        }
        captureCallback.captured.onLoadConfigSuccess(response)
        verify {
            listener.onLoadConfigSuccess(response)
        }
    }

    @Test
    fun `verify getTenetConfig is called once in viewmodel with failed response - loadConfig`() {
        val response = mockk<ArcXPException>()
        val captureCallback = slot<ArcXPIdentityListener>()

        testObject.loadConfig(listener)

        verify(exactly = 1) {
            viewModel.getTenetConfig(capture(captureCallback))
        }
        captureCallback.captured.onLoadConfigFailure(response)
        verify {
            listener.onLoadConfigFailure(response)
        }
    }

    @Test
    fun `verify removeIdentity is called once in viewmodel with successful response - removeIdentity`() {
        val response = mockk<ArcXPUpdateUserStatus>()
        val captureCallback = slot<ArcXPIdentityListener>()

        testObject.removeIdentity(grantType = grantType, listener = listener)

        verify(exactly = 1) {
            viewModel.removeIdentity(grantType = grantType, callback = capture(captureCallback))
        }
        captureCallback.captured.onRemoveIdentitySuccess(response)
        verify {
            listener.onRemoveIdentitySuccess(response)
        }
    }

    @Test
    fun `verify removeIdentity is called once in viewmodel with failed response - removeIdentity`() {
        val response = mockk<ArcXPException>()
        val captureCallback = slot<ArcXPIdentityListener>()

        testObject.removeIdentity(grantType = grantType, listener = listener)

        verify(exactly = 1) {
            viewModel.removeIdentity(grantType, capture(captureCallback))
        }
        captureCallback.captured.onRemoveIdentityFailure(response)
        verify {
            listener.onRemoveIdentityFailure(response)
        }
    }

    @Test
    fun `checkRecaptcha calls vm`() {
        val config = mockk<ArcXPCommerceConfig>()
        val context = mockk<Application>()
        val key = "key"
        every { config.recaptchaSiteKey } returns key
        every { config.context } returns context

        clearAllMocks(answers = false)

        testObject.checkRecaptcha(config = config, arcIdentityListener = listener)

        verifySequence {
            viewModel.checkRecaptcha(context = context, siteKey = key, callback = listener)
        }
    }
}