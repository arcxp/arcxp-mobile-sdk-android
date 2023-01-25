package com.arcxp.commerce.viewmodels

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.arcxp.commerce.apimanagers.ArcXPIdentityListener
import com.arcxp.commerce.extendedModels.ArcXPProfileManage
import com.arcxp.commerce.models.*
import com.arcxp.commerce.repositories.IdentityRepository
import com.arcxp.commerce.util.*
import com.arcxp.commerce.util.ArcXPError
import com.arcxp.commerce.viewmodels.IdentityViewModel
import io.mockk.*
import io.mockk.impl.annotations.RelaxedMockK
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test


class IdentityViewModelTest {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var testObject: IdentityViewModel

    private val identityResponse = ArcXPIdentity("", "", "", "",
        "", 1, "", false, "", "", false)

    private val authResponse = ArcXPAuth("1234", "asdf123", "321fdsa","", "", "")

    private val patchRequest = ArcXPProfilePatchRequest("a")

    private val identityList = listOf(identityResponse)

    private val configResponse = ArcXPConfig("", "", null, false, false,false,"",1,1,1,1,null, "", "", "", 1)

    private val contact = ArcXPContact("", "")

    private val profileDeletion = ArcXPProfileDeletionRule("", "", "", "", "", 1, "", 1, 1, 1, 1, "")

    private val profile = ArcXPProfileNotificationEvent("", "", "", "", "", 1, profileDeletion, "", "", "", "", 1)

    private val address = ArcXPAddress("", "", "", "", "", "", "")

    private val profileResponse = ArcXPProfile("", "", "", "", "", "", "", "", "",
    "", "", "", "", "", "", "", "", listOf(contact), listOf(address), listOf(), listOf(identityResponse), 0, profile)

    private var profileRequest = ArcXPProfileRequest("", "", null, null, null, "", null, null,
    null, null, null, null, null, null, null)

    private var identityRequest = ArcXPIdentityRequest("", "", "password")

    private val updateUserStatusResponse = ArcXPUpdateUserStatus("", "", "", "", "", "", "")

    @RelaxedMockK
    private lateinit var authManager: AuthManager

    @RelaxedMockK
    private lateinit var identityRepository: IdentityRepository

    @RelaxedMockK
    private lateinit var listener: ArcXPIdentityListener

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        testObject = IdentityViewModel(
            authManager,
            identityRepository,
        Dispatchers.Unconfined,
        Dispatchers.Unconfined
        )
    }

    @Test
    fun `change user password with successful response - changeUserPassword`() {
        val response = Success(
            identityResponse
        )
            coEvery {
                identityRepository.changePassword(eq(ArcXPPasswordResetRequest("a", "b")))
            } returns response

            testObject.changeUserPassword("a", "b", listener)

            coVerify {
                identityRepository.changePassword(eq(ArcXPPasswordResetRequest("a", "b")))
                listener.onPasswordChangeSuccess(response.r)
            }
    }

    @Test
    fun `change user password with failed response - changeUserPassword `() {
        val response = Failure(ArcXPError("error"))
        runBlocking {
            coEvery {
                identityRepository.changePassword(eq(ArcXPPasswordResetRequest("a", "b")))
            } returns response

            testObject.changeUserPassword("a", "b", listener)

            coVerify {
                identityRepository.changePassword(eq(ArcXPPasswordResetRequest("a", "b")))
                listener.onPasswordChangeError(response.l)
            }
        }
    }

    @Test
    fun `change user password with successful response to observer - changeUserPassword`() {
        val response = Success(
            identityResponse
        )

        coEvery {
            identityRepository.changePassword(eq(ArcXPPasswordResetRequest("a", "b")))
        } returns response

        testObject.changeUserPassword("a", "b", null)

        coVerify {
            identityRepository.changePassword(eq(ArcXPPasswordResetRequest("a", "b")))
        }
    }

    @Test
    fun `reset user password by email with successful response - obtainNonceByEmailAddress`(){
        val response = Success(ArcXPRequestPasswordReset(true))

        coEvery {
            identityRepository.resetPassword(eq(ArcXPResetPasswordRequestRequest("tester@arctest.com")))
        } returns response

        testObject.obtainNonceByEmailAddress("tester@arctest.com", listener)

        coVerify {
            identityRepository.resetPassword(eq(ArcXPResetPasswordRequestRequest("tester@arctest.com")))
            listener.onPasswordResetNonceSuccess(response.r)
        }
    }

    @Test
    fun `reset user password by email with failed response - obtainNonceByEmailAddress`(){
        val response = Failure(ArcXPError("Failed"))

        coEvery {
            identityRepository.resetPassword(eq(ArcXPResetPasswordRequestRequest("tester@arctest.com")))
        } returns response

        testObject.obtainNonceByEmailAddress("tester@arctest.com", listener)

        coVerify {
            identityRepository.resetPassword(eq(ArcXPResetPasswordRequestRequest("tester@arctest.com")))
            listener.onPasswordResetNonceFailure(response.l)
        }
    }

    @Test
    fun `reset user password by nonce with success response - resetPasswordByNonce`(){
        val response = Success(identityResponse)

        coEvery {
            identityRepository.resetPassword("asdf", ArcXPResetPasswordNonceRequest("asdf") )
        } returns response

        testObject.resetPasswordByNonce("asdf", "asdf", listener)

        coVerify {
            identityRepository.resetPassword("asdf", ArcXPResetPasswordNonceRequest("asdf"))
            listener.onPasswordResetSuccess(response.r)
        }
    }

    @Test
    fun `reset user password by nonce with failed response - resetPasswordByNonce`(){
        val response = Failure(ArcXPError("Error"))

        coEvery {
            identityRepository.resetPassword("asdf", ArcXPResetPasswordNonceRequest("asdf") )
        } returns response

        testObject.resetPasswordByNonce("asdf", "asdf", listener)

        coVerify {
            identityRepository.resetPassword("asdf", ArcXPResetPasswordNonceRequest("asdf"))
            listener.onPasswordResetError(response.l)
        }
    }

    @Test
    fun `login using username & password successful response - makeLoginCall`(){
        val response = Success(authResponse)

        coEvery {
            identityRepository.login(ArcXPAuthRequest("tester", "asdf", null, "password", null))
        } returns response

        testObject.makeLoginCall("tester", "asdf", null, listener)

        coVerify {
            identityRepository.login(ArcXPAuthRequest("tester", "asdf", null, "password", null))
            listener.onLoginSuccess(response.r)
        }
    }

    @Test
    fun `login using username & password failed response- makeLoginCall`(){
        val response = Failure(ArcXPError("Error"))

        coEvery {
            identityRepository.login(ArcXPAuthRequest("tester", "asdf", null, "password", null))
        } returns response

        testObject.makeLoginCall("tester", "asdf", null, listener)

        coVerify {
            identityRepository.login(ArcXPAuthRequest("tester", "asdf", null, "password", null))
            listener.onLoginError(response.l)
        }
    }

    @Test
    fun `login using third party login successfully - thirdPartyLoginCall`(){
        val response = Success(authResponse)

        coEvery {
            identityRepository.appleLogin(ArcXPAuthRequest("", "asdf123", null, grantType = "apple", null))
        } returns response

        testObject.thirdPartyLoginCall("asdf123", ArcXPAuthRequest.Companion.GrantType.APPLE, callback = listener)

        coVerify {
            identityRepository.appleLogin(ArcXPAuthRequest("", "asdf123", null, grantType = "apple", null))
            listener.onLoginSuccess(response.r)
        }
    }

    @Test
    fun `login using third party login failure - thirdPartyLoginCall`(){
        val response = Failure(ArcXPError("Error"))

        coEvery {
            identityRepository.login(ArcXPAuthRequest("", "asdf123", null, grantType = "facebook"))
        } returns response

        testObject.thirdPartyLoginCall("asdf123", ArcXPAuthRequest.Companion.GrantType.FACEBOOK, callback = listener)

        coVerify {
            identityRepository.login(ArcXPAuthRequest("", "asdf123", null, grantType = "facebook"))
            listener.onLoginError(response.l)
        }
    }

    @Test
    fun `verify registered email successful response - verifyEmailCall`(){
        val response = Success(ArcXPEmailVerification(true))

        coEvery {
            identityRepository.verifyEmail(ArcXPVerifyEmailRequest("test@arctest.com"))
        } returns response

        testObject.verifyEmailCall("test@arctest.com", listener)

        coVerify {
            identityRepository.verifyEmail(ArcXPVerifyEmailRequest("test@arctest.com"))
            listener.onEmailVerificationSentSuccess(response.r)
        }
    }

    @Test
    fun `verify registered email failure response - verifyEmailCall`(){
        val response = Failure(ArcXPError("Error"))

        coEvery {
            identityRepository.verifyEmail(ArcXPVerifyEmailRequest("test@arctest.com"))
        } returns response

        testObject.verifyEmailCall("test@arctest.com", listener)

        coVerify {
            identityRepository.verifyEmail(ArcXPVerifyEmailRequest("test@arctest.com"))
            listener.onEmailVerificationSentError(response.l)
        }
    }

    @Test
    fun `verify email with nonce successful response - verifyEmail`(){
        val response = Success(ArcXPEmailVerification(true))

        coEvery {
            identityRepository.verifyEmailNonce("asdf")
        } returns response

        testObject.verifyEmail("asdf", listener)

        coVerify {
            identityRepository.verifyEmailNonce("asdf")
            listener.onEmailVerifiedSuccess(response.r)
        }
    }

    @Test
    fun `verify email with nonce failure response - verifyEmail`(){
        val response = Failure(ArcXPError("Error"))

        coEvery {
            identityRepository.verifyEmailNonce("asdf")
        } returns response

        testObject.verifyEmail("asdf", listener)

        coVerify {
            identityRepository.verifyEmailNonce("asdf")
            listener.onEmailVerifiedError(response.l)
        }
    }

    @Test
    fun `make call to get magicLink successful response - getMagicLink`(){
        val response = Success(ArcXPOneTimeAccessLink(true))

        coEvery {
            identityRepository.getMagicLink(ArcXPOneTimeAccessLinkRequest("test@arctest.com"))
        } returns response

        testObject.getMagicLink("test@arctest.com", null, listener)

        coVerify {
            identityRepository.getMagicLink(ArcXPOneTimeAccessLinkRequest("test@arctest.com"))
            listener.onOneTimeAccessLinkSuccess(response.r)
        }
    }

    @Test
    fun `make call to get magicLink failure response - getMagicLink`(){
        val response = Failure(ArcXPError("Error"))

        coEvery {
            identityRepository.getMagicLink(ArcXPOneTimeAccessLinkRequest("test@arctest.com"))
        } returns response

        testObject.getMagicLink("test@arctest.com", null, listener)

        coVerify {
            identityRepository.getMagicLink(ArcXPOneTimeAccessLinkRequest("test@arctest.com"))
            listener.onOneTimeAccessLinkError(response.l)
        }
    }

    @Test
    fun `login with magic link successful response - loginMagicLink`(){
        val response = Success(ArcXPOneTimeAccessLinkAuth("asdf", "1234"))

        coEvery {
            identityRepository.loginMagicLink("asdf")
        } returns response

        testObject.loginMagicLink("asdf", listener)

        coVerify {
            identityRepository.loginMagicLink("asdf")
            listener.onOneTimeAccessLinkLoginSuccess(response.r)
        }
    }

    @Test
    fun `login with magic link failure response - loginMagicLink`(){
        val response = Failure(ArcXPError("Error"))

        coEvery {
            identityRepository.loginMagicLink("asdf")
        } returns response

        testObject.loginMagicLink("asdf", listener)

        coVerify {
            identityRepository.loginMagicLink("asdf")
            listener.onOneTimeAccessLinkError(response.l)
        }
    }

    @Test
    fun `make a call to patch profile successful response - patchProfile`(){
        val response = Success(ArcXPProfileManage("asdf", "asdf", "adsf", uuid = "adsf", identities = identityList))

        coEvery {
            identityRepository.patchProfile(patchRequest)
        } returns response

        testObject.patchProfile(patchRequest, listener)

        coVerify {
            identityRepository.patchProfile(patchRequest)
            listener.onProfileUpdateSuccess(response.r)
        }
    }

    @Test
    fun `make a call to patch profile failure response - patchProfile`(){
        val response = Failure(ArcXPError("Error"))

        coEvery {
            identityRepository.patchProfile(patchRequest)
        } returns response

        testObject.patchProfile(patchRequest, listener)

        coVerify {
            identityRepository.patchProfile(patchRequest)
            listener.onProfileError(response.l)
        }
    }

    @Test
    fun `make call to fetch profile with successful response - getProfile`(){
        val response = Success(ArcXPProfileManage("asdf", "asdf", "adsf", uuid = "adsf", identities = identityList ))

        coEvery {
            identityRepository.getProfile()
        } returns response

        testObject.getProfile(listener)

        coVerify {
            identityRepository.getProfile()
            listener.onFetchProfileSuccess(response.r)
        }
    }

    @Test
    fun `make call to fetch profile with failure response - getProfile`(){
        val response = Failure(ArcXPError("Error"))

        coEvery {
            identityRepository.getProfile()
        } returns response

        testObject.getProfile(listener)

        coVerify {
            identityRepository.getProfile()
            listener.onProfileError(response.l)
        }
    }

    @Test
    fun `register a new user successfully - makeRegistrationCall`(){
        val response = Success(ArcXPUser("", true, identityList, profileResponse))

        coEvery {
            identityRepository.signUp(ArcXPSignUpRequest(identityRequest, profileRequest))
        } returns response

        testObject.makeRegistrationCall("", "", "", "", "", listener)

        coVerify {
            identityRepository.signUp(ArcXPSignUpRequest(identityRequest, profileRequest))
            listener.onRegistrationSuccess(response.r)
        }
    }

    @Test
    fun `register a new user failure response - makeRegistrationCall`(){
        val response = Failure(ArcXPError("Error"))

        coEvery {
            identityRepository.signUp(ArcXPSignUpRequest(identityRequest, profileRequest))
        } returns response

        testObject.makeRegistrationCall("", "", "", "", "", listener)

        coVerify {
            identityRepository.signUp(ArcXPSignUpRequest(identityRequest, profileRequest))
            listener.onRegistrationError(response.l)
        }
    }

    @Test
    fun `make a call to delete user successful response - deleteUser`(){
        val response = Success(ArcXPAnonymizeUser(true))

        coEvery {
            identityRepository.deleteUser()
        } returns response

        testObject.deleteUser(listener)

        coVerify {
            identityRepository.deleteUser()
            listener.onDeleteUserSuccess()
        }
    }

    @Test
    fun `make a call to delete user failure response - deleteUser`(){
        val response = Failure(ArcXPError("Error"))

        coEvery {
            identityRepository.deleteUser()
        } returns response

        testObject.deleteUser(listener)

        coVerify {
            identityRepository.deleteUser()
            listener.onDeleteUserError(response.l)
        }
    }

    @Test
    fun `make call to approve deletion request successful response - approveDeletion`(){
        val response = Success(ArcXPDeleteUser(true))

        coEvery {
            identityRepository.approveDeletion("asdf")
        } returns response

        testObject.approveDeletion("asdf", listener)

        coVerify {
            identityRepository.approveDeletion("asdf")
            listener.onApproveDeletionSuccess(response.r)
        }
    }

    @Test
    fun `make call to approve deletion request failure response - approveDeletion`(){
        val response = Failure(ArcXPError("Error"))

        coEvery {
            identityRepository.approveDeletion("asdf")
        } returns response

        testObject.approveDeletion("asdf", listener)

        coVerify {
            identityRepository.approveDeletion("asdf")
            listener.onApproveDeletionError(response.l)
        }
    }

    @Test
    fun `make call to validate jwt with token successful response - validateJwt`(){
        val response = Success(authResponse)

        coEvery {
            identityRepository.validateJwt("asdf")
        } returns response

        testObject.validateJwt("asdf", listener)

        coVerify {
            identityRepository.validateJwt("asdf")
            listener.onValidateSessionSuccess()
        }
    }

    @Test
    fun `make call to validate jwt with token failure response - validateJwt`(){
        val response = Failure(ArcXPError("Error"))

        coEvery {
            identityRepository.validateJwt("asdf")
        } returns response

        testObject.validateJwt("asdf", listener)

        coVerify {
            identityRepository.validateJwt("asdf")
            listener.onValidateSessionError(response.l)
        }
    }

    @Test
    fun `make call to validate jwt successful response - validateJwt`(){
        val response = Success(authResponse)

        coEvery {
            identityRepository.validateJwt()
        } returns response

        testObject.validateJwt(listener)

        coVerify {
            identityRepository.validateJwt()
            listener.onValidateSessionSuccess()
        }
    }

    @Test
    fun `make call to validate jwt failure response - validateJwt`(){
        val response = Failure(ArcXPError("Error"))

        coEvery {
            identityRepository.validateJwt()
        } returns response

        testObject.validateJwt(listener)

        coVerify {
            identityRepository.validateJwt()
            listener.onValidateSessionError(response.l)
        }
    }

    @Test
    fun `make call to refresh token successful response`(){
        val response = Success(authResponse)

        coEvery {
            identityRepository.refreshToken("asdf", "refresh-token")
        } returns response

        testObject.refreshToken("asdf", "refresh-token", listener)

        coVerify {
            identityRepository.refreshToken("asdf", "refresh-token")
            listener.onRefreshSessionSuccess(response.r)
        }

    }

    @Test
    fun `make call to refresh token failure response`(){
        val response = Failure(ArcXPError("Error"))

        coEvery {
            identityRepository.refreshToken("asdf", "refresh-token")
        } returns response

        testObject.refreshToken("asdf", "refresh-token", listener)

        coVerify {
            identityRepository.refreshToken("asdf", "refresh-token")
            listener.onRefreshSessionFailure(response.l)
        }
    }

    @Test
    fun `make call to get tenet config successful response - getTenetConfig`(){
        val response = Success(configResponse)

        coEvery {
            identityRepository.getConfig()
        } returns response

        testObject.getTenetConfig(listener)

        coVerify {
            identityRepository.getConfig()
            listener.onLoadConfigSuccess(response.r)
        }
    }

    @Test
    fun `make call to get tenet config failure response - getTenetConfig`(){
        val response = Failure(ArcXPError("Error"))

        coEvery {
            identityRepository.getConfig()
        } returns response

        testObject.getTenetConfig(listener)

        coVerify {
            identityRepository.getConfig()
            listener.onLoadConfigFailure(response.l)
        }
    }

    @Test
    fun `verify removal of identity successful response - removeIdentity`(){
        val response = Success(updateUserStatusResponse)

        coEvery {
            identityRepository.removeIdentity("")
        } returns response

        testObject.removeIdentity("", listener)

        coVerify {
            identityRepository.removeIdentity("")
            listener.onRemoveIdentitySuccess(response.r)
        }
    }

    @Test
    fun `verify removal of identity failure response - removeIdentity`(){
        val response = Failure(ArcXPError("Error"))

        coEvery {
            identityRepository.removeIdentity("")
        } returns response

        testObject.removeIdentity("", listener)

        coVerify {
            identityRepository.removeIdentity("")
            listener.onRemoveIdentityFailure(response.l)
        }
    }
}