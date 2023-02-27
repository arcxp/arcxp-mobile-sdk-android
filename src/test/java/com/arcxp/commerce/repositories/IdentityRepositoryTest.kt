package com.arcxp.commerce.repositories

import com.arcxp.commons.throwables.ArcXPSDKErrorType
import com.arcxp.commerce.extendedModels.ArcXPProfileManage
import com.arcxp.commerce.models.*
import com.arcxp.commerce.retrofit.IdentityService
import com.arcxp.commerce.retrofit.RetrofitController
import com.arcxp.commons.testutils.TestUtils.getJson
import com.arcxp.commerce.util.AuthManager
import com.arcxp.commons.throwables.ArcXPException
import com.arcxp.commons.util.Failure
import com.arcxp.commons.util.Success
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.After
import org.junit.Before
import org.junit.Test
import retrofit2.Response
import kotlin.test.assertEquals
import kotlin.test.assertNull


@OptIn(ExperimentalCoroutinesApi::class)
class IdentityRepositoryTest {

    @MockK
    lateinit var identityResponse: ArcXPIdentity

    @MockK
    lateinit var identityList: List<ArcXPIdentity>

    @MockK
    lateinit var profileManageResponse: ArcXPProfileManage

    @MockK
    lateinit var profileResponse: ArcXPProfile

    @MockK
    lateinit var profileRequest: ArcXPProfileRequest

    @MockK
    lateinit var patchRequest: ArcXPProfilePatchRequest

    @MockK
    lateinit var arcXPAuthRequest: ArcXPAuthRequest

    @MockK
    lateinit var arcXPOneTimeAccessLinkRequest: ArcXPOneTimeAccessLinkRequest

    @MockK
    lateinit var resetPasswordNonceRequest: ArcXPResetPasswordNonceRequest
    @MockK
    lateinit var verifyEmailRequest: ArcXPVerifyEmailRequest

    @MockK
    lateinit var resetPasswordRequest: ArcXPResetPasswordRequestRequest

    @MockK
    lateinit var authResponse: ArcXPAuth

    @MockK
    lateinit var configResponse: ArcXPConfig

    @MockK
    lateinit var updateUserStatusResponse: ArcXPUpdateUserStatus

    @MockK
    lateinit var identityService: IdentityService

    @MockK
    lateinit var identityServiceApple: IdentityService

    @MockK
    lateinit var exception: Exception

    private lateinit var testObject: IdentityRepository

    private val nonce = "nonce"
    private val message = "message"
    private val token = "token"
    private val grantType = "gt"

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        mockkObject(RetrofitController)
        every { RetrofitController.getIdentityService() } returns identityService
        every { RetrofitController.getIdentityServiceForApple() } returns identityServiceApple
        testObject = IdentityRepository()
        mockkObject(AuthManager)
        every { AuthManager.getInstance() } returns mockk {
            every { accessToken = any() } just Runs
        }
        every { exception.message } returns message
    }

    @After
    fun tearDown() {
        unmockkObject(AuthManager)
        unmockkObject(RetrofitController)
    }

    @Test
    fun `make call to login user successful response - login`() = runTest {
        val expected = ArcXPAuth("123", "abc", "", "", "", "")
        val result = Response.success(expected)
        coEvery {
            identityService.login(eq(ArcXPAuthRequest("d", "code")))
        } returns result

        val actual = testObject.login(ArcXPAuthRequest("d", "code"))
        assertEquals(expected, (actual as Success).success)
    }

    @Test
    fun `make call to login user failed response - login`() = runTest {
        val result =
            Response.error<ArcXPAuth>(401, getJson("identity_error_response.json").toResponseBody())
        coEvery {
            identityService.login(any())
        } returns result

        val actual = testObject.login(mockk())
        assertEquals(
            ArcXPSDKErrorType.SERVER_ERROR,
            (((actual as Failure).failure) as ArcXPException).type
        )
        assertEquals("Authentication failed", (actual.failure as ArcXPException).message)
        assertEquals("300041", (actual.failure as ArcXPException).code)
    }

    @Test
    fun `make call to login user exception response - login`() = runTest {
        val expected = ArcXPException(
            message = message,
            type = ArcXPSDKErrorType.SERVER_ERROR,
            value = exception
        )
        coEvery {
            identityService.login(authRequest = arcXPAuthRequest)
        } throws exception

        val result = testObject.login(authRequest = arcXPAuthRequest)
        val actual = ((result as Failure).failure) as ArcXPException

        assertEquals(expected, actual)
    }

    @Test
    fun `changePassword - make call to change user password successful response`() = runTest {
        val expected = identityResponse
        val result = Response.success(expected)
        coEvery {
            identityService.changePassword(eq(ArcXPPasswordResetRequest("a", "b")))
        } returns result

        val actual = testObject.changePassword(ArcXPPasswordResetRequest("a", "b"))
        assertEquals(expected, (actual as Success).success)
    }

    @Test
    fun `changePassword - make call to change user password failed response`() = runTest {
        val result = Response.error<ArcXPIdentity>(
            401,
            getJson("identity_error_response.json").toResponseBody()
        )
        coEvery {
            identityService.changePassword(ArcXPPasswordResetRequest("a", "b"))
        } returns result

        val actual = testObject.changePassword(ArcXPPasswordResetRequest("a", "b"))
        assertEquals(
            ArcXPSDKErrorType.SERVER_ERROR,
            (((actual as Failure).failure) as ArcXPException).type
        )
        assertEquals("Authentication failed", (actual.failure as ArcXPException).message)
        assertEquals("300041", (actual.failure as ArcXPException).code)
    }

    @Test
    fun `changePassword - make call to change user password exception response`() = runTest {
        val expected = ArcXPException(
            message = message,
            type = ArcXPSDKErrorType.SERVER_ERROR,
            value = exception
        )
        coEvery {
            identityService.changePassword(any())
        } throws exception

        val result = testObject.changePassword(mockk())
        val actual = ((result as Failure).failure) as ArcXPException

        assertEquals(expected, actual)
    }

    @Test
    fun `resetPassword - make call to reset user password successful response`() = runTest {
        val expected = ArcXPRequestPasswordReset(true)
        val result = Response.success(expected)
        coEvery {
            identityService.resetPassword(eq(ArcXPResetPasswordRequestRequest("d")))
        } returns result

        val actual = testObject.resetPassword(ArcXPResetPasswordRequestRequest("d"))
        assertEquals(expected, (actual as Success).success)
    }

    @Test
    fun `resetPassword - make call to reset user password failed response`() = runTest {
        val result = Response.error<ArcXPRequestPasswordReset>(
            401,
            getJson("identity_error_response.json").toResponseBody()
        )
        coEvery {
            identityService.resetPassword(ArcXPResetPasswordRequestRequest("d"))
        } returns result

        val actual = testObject.resetPassword(ArcXPResetPasswordRequestRequest("d"))
        assertEquals(
            ArcXPSDKErrorType.SERVER_ERROR,
            (((actual as Failure).failure) as ArcXPException).type
        )
        assertEquals("Authentication failed", (actual.failure as ArcXPException).message)
        assertEquals("300041", (actual.failure as ArcXPException).code)
    }

    @Test
    fun `resetPassword - make call to reset user password throws exception`() = runTest {
        val expected = ArcXPException(
            message = message,
            type = ArcXPSDKErrorType.SERVER_ERROR,
            value = exception
        )
        coEvery {
            identityService.resetPassword(passwordResetRequest = resetPasswordRequest)
        } throws exception

        val result = testObject.resetPassword(resetPasswordRequest = resetPasswordRequest)
        val actual = ((result as Failure).failure) as ArcXPException

        assertEquals(expected, actual)
    }

    @Test
    fun `resetPassword - make call to reset user password with nonce successful response`() =
        runTest {
            val expected = identityResponse
            val result = Response.success(expected)
            coEvery {
                identityService.resetPassword("a", ArcXPResetPasswordNonceRequest("abc"))
            } returns result

            val actual = testObject.resetPassword("a", ArcXPResetPasswordNonceRequest("abc"))
            assertEquals(expected, (actual as Success).success)
        }

    @Test
    fun `resetPassword - make call to reset user password with nonce failed response`() = runTest {
        val result = Response.error<ArcXPIdentity>(
            401,
            getJson("identity_error_response.json").toResponseBody()
        )
        coEvery {
            identityService.resetPassword("a", ArcXPResetPasswordNonceRequest("b"))
        } returns result

        val actual = testObject.resetPassword("a", ArcXPResetPasswordNonceRequest("b"))
        assertEquals(
            ArcXPSDKErrorType.SERVER_ERROR,
            (((actual as Failure).failure) as ArcXPException).type
        )
        assertEquals("Authentication failed", (actual.failure as ArcXPException).message)
        assertEquals("300041", (actual.failure as ArcXPException).code)
    }

    @Test
    fun `resetPassword - make call to reset user password with nonce throws exception`() = runTest {
        val expected = ArcXPException(
            message = message,
            type = ArcXPSDKErrorType.SERVER_ERROR,
            value = exception
        )
        coEvery {
            identityService.resetPassword(
                nonce = nonce,
                resetPasswordNonceRequest = resetPasswordNonceRequest
            )
        } throws exception

        val result = testObject.resetPassword(
            nonce = nonce,
            resetPasswordNonceRequest = resetPasswordNonceRequest
        )
        val actual = ((result as Failure).failure) as ArcXPException

        assertEquals(expected, actual)
    }

    @Test
    fun `getMagicLink - make call to get one time access link successful response`() = runTest {
        val expected = ArcXPOneTimeAccessLink(true)
        val result = Response.success(expected)
        coEvery {
            identityService.getMagicLink(ArcXPOneTimeAccessLinkRequest("a"))
        } returns result

        val actual = testObject.getMagicLink(ArcXPOneTimeAccessLinkRequest("a"))
        assertEquals(expected, (actual as Success).success)
    }

    @Test
    fun `getMagicLink - make call to get one time access link failed response`() = runTest {
        val result = Response.error<ArcXPOneTimeAccessLink>(
            401,
            getJson("identity_error_response.json").toResponseBody()
        )
        coEvery {
            identityService.getMagicLink(ArcXPOneTimeAccessLinkRequest("a"))
        } returns result

        val actual = testObject.getMagicLink(ArcXPOneTimeAccessLinkRequest("a"))
        assertEquals(
            ArcXPSDKErrorType.SERVER_ERROR,
            (((actual as Failure).failure) as ArcXPException).type
        )
        assertEquals("Authentication failed", (actual.failure as ArcXPException).message)
        assertEquals("300041", (actual.failure as ArcXPException).code)
    }

    @Test
    fun `getMagicLink - make call to get one time access link failed throws exception`() = runTest {
        val expected = ArcXPException(
            message = message,
            type = ArcXPSDKErrorType.SERVER_ERROR,
            value = exception
        )
        coEvery {
            identityService.getMagicLink(oneTimeAccessLinkRequest = arcXPOneTimeAccessLinkRequest)
        } throws exception

        val result = testObject.getMagicLink(request = arcXPOneTimeAccessLinkRequest)
        val actual = ((result as Failure).failure) as ArcXPException

        assertEquals(expected, actual)
    }

    @Test
    fun `loginMagicLink - make call to signin with one time access link successful response`() =
        runTest {
            val expected = ArcXPOneTimeAccessLinkAuth("", "")
            val result = Response.success(expected)
            coEvery {
                identityService.loginMagicLink("a")
            } returns result

            val actual = testObject.loginMagicLink("a")
            assertEquals(expected, (actual as Success).success)
        }

    @Test
    fun `loginMagicLink - make call to signin with one time access link failed response`() =
        runTest {
            val result = Response.error<ArcXPOneTimeAccessLinkAuth>(
                401,
                getJson("identity_error_response.json").toResponseBody()
            )
            coEvery {
                identityService.loginMagicLink("a")
            } returns result

            val actual = testObject.loginMagicLink("a")
            assertEquals(
                ArcXPSDKErrorType.SERVER_ERROR,
                (((actual as Failure).failure) as ArcXPException).type
            )
            assertEquals("Authentication failed", (actual.failure as ArcXPException).message)
            assertEquals("300041", (actual.failure as ArcXPException).code)
        }

    @Test
    fun `loginMagicLink - make call to signin with one time access link throws exception`() =
        runTest {
            val expected = ArcXPException(
                message = message,
                type = ArcXPSDKErrorType.SERVER_ERROR,
                value = exception
            )
            coEvery {
                identityService.loginMagicLink(nonce = nonce)
            } throws exception

            val result = testObject.loginMagicLink(nonce = nonce)
            val actual = ((result as Failure).failure) as ArcXPException

            assertEquals(expected, actual)
        }

    @Test
    fun `getProfile - make call to get profile successful response`() = runTest {
        val expected = ArcXPProfileManage("", "", "", uuid = "", identities = identityList)
        val result = Response.success(expected)
        coEvery {
            identityService.getProfile()
        } returns result

        val actual = testObject.getProfile()
        assertEquals(expected, (actual as Success).success)
    }

    @Test
    fun `getProfile - make call to get profile failed response`() = runTest {
        val result = Response.error<ArcXPProfileManage>(
            401,
            getJson("identity_error_response.json").toResponseBody()
        )
        coEvery {
            identityService.getProfile()
        } returns result

        val actual = testObject.getProfile()
        assertEquals(
            ArcXPSDKErrorType.SERVER_ERROR,
            (((actual as Failure).failure) as ArcXPException).type
        )
        assertEquals("Authentication failed", (actual.failure as ArcXPException).message)
        assertEquals("300041", (actual.failure as ArcXPException).code)
    }

    @Test
    fun `getProfile - make call to get profile throws exception`() = runTest {
        val expected = ArcXPException(
            message = message,
            type = ArcXPSDKErrorType.SERVER_ERROR,
            value = exception
        )
        coEvery {
            identityService.getProfile()
        } throws exception

        val result = testObject.getProfile()
        val actual = ((result as Failure).failure) as ArcXPException

        assertEquals(expected, actual)
    }

    @Test
    fun `patchProfile - make call to get update user profile successful response`() = runTest {
        val expected = profileManageResponse
        val result = Response.success(expected)
        coEvery {
            identityService.patchProfile(ArcXPProfilePatchRequest("John"))
        } returns result

        val actual = testObject.patchProfile(ArcXPProfilePatchRequest("John"))
        assertEquals(expected, (actual as Success).success)
    }

    @Test
    fun `patchProfile - make call to get update user profile failed response`() = runTest {
        val result = Response.error<ArcXPProfileManage>(
            401,
            getJson("identity_error_response.json").toResponseBody()
        )
        coEvery {
            identityService.patchProfile(ArcXPProfilePatchRequest("a"))
        } returns result

        val actual = testObject.patchProfile(ArcXPProfilePatchRequest("a"))
        assertEquals(
            ArcXPSDKErrorType.SERVER_ERROR,
            (((actual as Failure).failure) as ArcXPException).type
        )
        assertEquals("Authentication failed", (actual.failure as ArcXPException).message)
        assertEquals("300041", (actual.failure as ArcXPException).code)
    }

    @Test
    fun `patchProfile - make call to get update user profile throws exception`() = runTest {
        val expected = ArcXPException(
            message = message,
            type = ArcXPSDKErrorType.SERVER_ERROR,
            value = exception
        )
        coEvery {
            identityService.patchProfile(profilePatchRequest = patchRequest)
        } throws exception

        val result = testObject.patchProfile(profileRequest = patchRequest)
        val actual = ((result as Failure).failure) as ArcXPException

        assertEquals(expected, actual)
    }

    @Test
    fun `signUp - make call to register new user successful response`() = runTest {
        val expected = ArcXPUser("", true, identityList, profileResponse)
        val result = Response.success(expected)
        coEvery {
            identityService.signUp(
                ArcXPSignUpRequest(
                    ArcXPIdentityRequest("", "", null),
                    profileRequest
                )
            )
        } returns result

        val actual = testObject.signUp(
            ArcXPSignUpRequest(
                ArcXPIdentityRequest("", "", null),
                profileRequest
            )
        )
        assertEquals(expected, (actual as Success).success)
    }

    @Test
    fun `signUp - make call to register new user failed response`() = runTest {
        val result =
            Response.error<ArcXPUser>(401, getJson("identity_error_response.json").toResponseBody())
        coEvery {
            identityService.signUp(
                ArcXPSignUpRequest(
                    ArcXPIdentityRequest("", "", null),
                    profileRequest
                )
            )
        } returns result

        val actual = testObject.signUp(
            ArcXPSignUpRequest(
                ArcXPIdentityRequest("", "", null),
                profileRequest
            )
        )

        assertEquals(
            ArcXPSDKErrorType.SERVER_ERROR,
            (((actual as Failure).failure) as ArcXPException).type
        )
        assertEquals("Authentication failed", (actual.failure as ArcXPException).message)
        assertEquals("300041", (actual.failure as ArcXPException).code)
    }

    @Test
    fun `signUp - make call to register new user throws exception`() = runTest {
        val expected = ArcXPException(
            message = message,
            type = ArcXPSDKErrorType.SERVER_ERROR,
            value = exception
        )
        val req = mockk<ArcXPSignUpRequest>()
        coEvery {
            identityService.signUp(signUpRequest = req)
        } throws exception

        val result = testObject.signUp(signUpRequest = req)
        val actual = ((result as Failure).failure) as ArcXPException

        assertEquals(expected, actual)
    }

    @Test
    fun `verifyEmail - make call to verify email address successful response`() = runTest {
        val expected = ArcXPEmailVerification(true)
        val result = Response.success(expected)
        coEvery {
            identityService.verifyEmail(ArcXPVerifyEmailRequest("a"))
        } returns result

        val actual = testObject.verifyEmail(ArcXPVerifyEmailRequest("a"))
        assertEquals(expected, (actual as Success).success)
    }

    @Test
    fun `verifyEmail - make call to verify email address failed response`() = runTest {
        val result = Response.error<ArcXPEmailVerification>(
            401,
            getJson("identity_error_response.json").toResponseBody()
        )
        coEvery {
            identityService.verifyEmail(ArcXPVerifyEmailRequest("a"))
        } returns result

        val actual = testObject.verifyEmail(ArcXPVerifyEmailRequest("a"))
        assertEquals(
            ArcXPSDKErrorType.SERVER_ERROR,
            (((actual as Failure).failure) as ArcXPException).type
        )
        assertEquals("Authentication failed", (actual.failure as ArcXPException).message)
        assertEquals("300041", (actual.failure as ArcXPException).code)
    }

    @Test
    fun `verifyEmail - make call to verify email address throws exception`() = runTest {
        val expected = ArcXPException(
            message = message,
            type = ArcXPSDKErrorType.SERVER_ERROR,
            value = exception
        )
        coEvery {
            identityService.verifyEmail(verifyEmailRequest = verifyEmailRequest)
        } throws exception

        val result = testObject.verifyEmail(verifyEmailRequest = verifyEmailRequest)
        val actual = ((result as Failure).failure) as ArcXPException

        assertEquals(expected, actual)
    }

    @Test
    fun `verifyEmailNonce - make call to verify email from nonce successful response`() = runTest {
        val expected = ArcXPEmailVerification(true)
        val result = Response.success(expected)
        coEvery {
            identityService.verifyEmail("a")
        } returns result

        val actual = testObject.verifyEmailNonce("a")
        assertEquals(expected, (actual as Success).success)
    }

    @Test
    fun `verifyEmailNonce - make call to verify email from nonce failed response`() = runTest {
        val result = Response.error<ArcXPEmailVerification>(
            401,
            getJson("identity_error_response.json").toResponseBody()
        )
        coEvery {
            identityService.verifyEmail("a")
        } returns result

        val actual = testObject.verifyEmailNonce("a")
        assertEquals(
            ArcXPSDKErrorType.SERVER_ERROR,
            (((actual as Failure).failure) as ArcXPException).type
        )
        assertEquals("Authentication failed", (actual.failure as ArcXPException).message)
        assertEquals("300041", (actual.failure as ArcXPException).code)
    }

    @Test
    fun `verifyEmailNonce - make call to verify email from nonce throws exception`() = runTest {
        val expected = ArcXPException(
            message = message,
            type = ArcXPSDKErrorType.SERVER_ERROR,
            value = exception
        )
        coEvery {
            identityService.verifyEmail(nonce = nonce)
        } throws exception

        val result = testObject.verifyEmailNonce(nonce = nonce)
        val actual = ((result as Failure).failure) as ArcXPException

        assertEquals(expected, actual)
    }

    @Test
    fun `logout make call to log user out successful response`() = runTest {
        val expected = Unit as? Void
        val result = Response.success(expected)
        coEvery {
            identityService.logout()
        } returns result

        val actual = testObject.logout()
        assertEquals(expected, (actual as Success).success)
    }

    @Test
    fun `logout make call to log user out failed response`() = runTest {
        val result =
            Response.error<Void>(401, getJson("identity_error_response.json").toResponseBody())
        coEvery {
            identityService.logout()
        } returns result

        val actual = testObject.logout()
        assertEquals(
            ArcXPSDKErrorType.SERVER_ERROR,
            (((actual as Failure).failure) as ArcXPException).type
        )
        assertEquals("Authentication failed", (actual.failure as ArcXPException).message)
        assertEquals("300041", (actual.failure as ArcXPException).code)
    }

    @Test
    fun `logout make call to log user out throws exception`() = runTest {
        val expected = ArcXPException(
            message = message,
            type = ArcXPSDKErrorType.SERVER_ERROR,
            value = exception
        )
        coEvery {
            identityService.logout()
        } throws exception

        val result = testObject.logout()
        val actual = ((result as Failure).failure) as ArcXPException

        assertEquals(expected, actual)
    }

    @Test
    fun `make call to verify cached access token is valid successful response - validateJwt`() =
        runTest {
            val expected = authResponse
            val result = Response.success(expected)
            coEvery {
                identityService.recapToken(ArcXPAuthRequest())
            } returns result

            val actual = testObject.validateJwt()
            assertEquals(expected, (actual as Success).success)
        }

    @Test
    fun `make call to verify cached access token is valid failed response - validateJwt`() =
        runTest {
            val result =
                Response.error<ArcXPAuth>(
                    401,
                    getJson("identity_error_response.json").toResponseBody()
                )
            coEvery {
                identityService.recapToken(ArcXPAuthRequest())
            } returns result

            val actual = testObject.validateJwt()
            assertEquals(
                ArcXPSDKErrorType.INVALID_SESSION,
                (((actual as Failure).failure) as ArcXPException).type
            )
            assertEquals("Authentication failed", (actual.failure as ArcXPException).message)
            assertEquals("300041", (actual.failure as ArcXPException).code)
        }

    @Test
    fun `make call to verify cached access token is valid throws exception - validateJwt`() =
        runTest {
            val expected = ArcXPException(
                message = message,
                type = ArcXPSDKErrorType.SERVER_ERROR,
                value = exception
            )
            coEvery {
                identityService.recapToken(any())
            } throws exception

            val result = testObject.validateJwt()
            val actual = ((result as Failure).failure) as ArcXPException
            assertEquals(expected, actual)
        }

    @Test
    fun `make call to validate access token successful response - validateJwt`() = runTest {
        val expected = authResponse
        val result = Response.success(expected)
        coEvery {
            identityService.recapToken(ArcXPAuthRequest(token = ""))
        } returns result

        val actual = testObject.validateJwt("")
        assertEquals(expected, (actual as Success).success)
    }

    @Test
    fun `make call to validate access token failed response - validateJwt`() = runTest {
        val result =
            Response.error<ArcXPAuth>(401, getJson("identity_error_response.json").toResponseBody())
        coEvery {
            identityService.recapToken(ArcXPAuthRequest(token = ""))
        } returns result

        val actual = testObject.validateJwt("")
        assertEquals(
            ArcXPSDKErrorType.INVALID_SESSION,
            (((actual as Failure).failure) as ArcXPException).type
        )
        assertEquals("Authentication failed", (actual.failure as ArcXPException).message)
        assertEquals("300041", (actual.failure as ArcXPException).code)
    }

    @Test
    fun `make call to validate access token throws exception - validateJwt`() = runTest {
        val expected = ArcXPException(
            message = message,
            type = ArcXPSDKErrorType.SERVER_ERROR,
            value = exception
        )
        val request = ArcXPAuthRequest(token = token)
        coEvery {
            identityService.recapToken(authRequest = request)
        } throws exception

        val result = testObject.validateJwt(token = token)
        val actual = ((result as Failure).failure) as ArcXPException

        assertEquals(expected, actual)
    }

    @Test
    fun `refreshToken make call to extend user session successful response`() = runTest {
        val expected = authResponse
        val result = Response.success(expected)
        coEvery {
            identityService.recapToken(ArcXPAuthRequest(token = "", grantType = "refresh-token"))
        } returns result

        val actual = testObject.refreshToken(token = "", grantType = "refresh-token")
        assertEquals(expected, (actual as Success).success)
    }

    @Test
    fun `refreshToken make call to extend user session failed response`() = runTest {
        val result =
            Response.error<ArcXPAuth>(401, getJson("identity_error_response.json").toResponseBody())
        coEvery {
            identityService.recapToken(ArcXPAuthRequest(token = "", grantType = "refresh-token"))
        } returns result

        val actual = testObject.refreshToken(token = "", grantType = "refresh-token")
        assertEquals(
            ArcXPSDKErrorType.SERVER_ERROR,
            (((actual as Failure).failure) as ArcXPException).type
        )
        assertEquals("Authentication failed", (actual.failure as ArcXPException).message)
        assertEquals("300041", (actual.failure as ArcXPException).code)
    }

    @Test
    fun `refreshToken make call to extend user session throws exception`() = runTest {
        val expected = ArcXPException(
            message = message,
            type = ArcXPSDKErrorType.SERVER_ERROR,
            value = exception
        )
        val request = ArcXPAuthRequest(token = token, grantType = grantType)
        coEvery {
            identityService.recapToken(authRequest = request)
        } throws exception

        val result = testObject.refreshToken(token = token, grantType = grantType)
        val actual = ((result as Failure).failure) as ArcXPException

        assertEquals(expected, actual)
    }

    @Test
    fun `deleteUser make call to request deletion of user successful response`() = runTest {
        val expected = ArcXPAnonymizeUser(true)
        val result = Response.success(expected)
        coEvery {
            identityService.deleteUser()
        } returns result

        val actual = testObject.deleteUser()
        assertEquals(expected, (actual as Success).success)
    }

    @Test
    fun `deleteUser make call to request deletion of user failed response`() = runTest {
        val result = Response.error<ArcXPAnonymizeUser>(
            401,
            getJson("identity_error_response.json").toResponseBody()
        )
        coEvery {
            identityService.deleteUser()
        } returns result

        val actual = testObject.deleteUser()
        assertEquals(
            ArcXPSDKErrorType.SERVER_ERROR,
            (((actual as Failure).failure) as ArcXPException).type
        )
        assertEquals("Authentication failed", (actual.failure as ArcXPException).message)
        assertEquals("300041", (actual.failure as ArcXPException).code)
    }

    @Test
    fun `deleteUser make call to request deletion of user throws exception`() = runTest {
        val expected = ArcXPException(
            message = message,
            type = ArcXPSDKErrorType.SERVER_ERROR,
            value = exception
        )
        coEvery {
            identityService.deleteUser()
        } throws exception

        val result = testObject.deleteUser()
        val actual = ((result as Failure).failure) as ArcXPException

        assertEquals(expected, actual)
    }

    @Test
    fun `make call to get apple auth url successful response - appleAuthUrl`() = runTest {
        val expected = mockk<ResponseBody>()
        val result = Response.success(expected)
        coEvery {
            identityService.appleAuthUrl()
        } returns result

        val actual = testObject.appleAuthUrl()

        assertEquals(expected, (actual as Success).success)
    }

    @Test
    fun `make call to get apple auth url failed response - appleAuthUrl`() = runTest {
        val result = Response.error<ResponseBody>(
            401,
            getJson("identity_error_response.json").toResponseBody()
        )
        coEvery {
            identityService.appleAuthUrl()
        } returns result

        val actual = testObject.appleAuthUrl()
        assertEquals(
            ArcXPSDKErrorType.SERVER_ERROR,
            (((actual as Failure).failure) as ArcXPException).type
        )
        assertEquals("Authentication failed", (actual.failure as ArcXPException).message)
        assertEquals("300041", (actual.failure as ArcXPException).code)
    }


    @Test
    fun `appleAuthUrl - make call to get apple auth url throws exception`() = runTest {
        val expectedMessage = "myMessage"
        val exception = Exception(expectedMessage)

        coEvery { identityService.appleAuthUrl() } throws exception

        val result = testObject.appleAuthUrl()
        assertEquals(exception, (result as Failure).failure)

        unmockkObject(RetrofitController)
    }

    @Test
    fun `appleAuthUrlUpdatedURL - make call to get apple auth url successful response`() = runTest {
        val expected = mockk<ResponseBody>()
        val result = Response.success(expected)
        coEvery {
            identityServiceApple.appleAuthUrl()
        } returns result

        val actual = testObject.appleAuthUrlUpdatedURL()
        assertEquals(expected, (actual as Success).success)
    }

    @Test
    fun `appleAuthUrlUpdatedURL make call to get apple auth url failed response`() = runTest {
        val result = Response.error<ResponseBody>(
            401,
            getJson("identity_error_response.json").toResponseBody()
        )
        coEvery {
            identityServiceApple.appleAuthUrl()
        } returns result

        val actual = testObject.appleAuthUrlUpdatedURL()
        assertEquals(
            ArcXPSDKErrorType.SERVER_ERROR,
            (((actual as Failure).failure) as ArcXPException).type
        )
        assertEquals("Authentication failed", (actual.failure as ArcXPException).message)
        assertEquals("300041", (actual.failure as ArcXPException).code)
    }


    @Test
    fun `appleAuthUrlUpdatedURL - make call to get apple auth url throws exception`() = runTest {
        val expectedMessage = "myMessage"
        val exception = Exception(expectedMessage)

        coEvery { identityServiceApple.appleAuthUrl() } throws exception

        val result = testObject.appleAuthUrlUpdatedURL()
        assertEquals(exception, (result as Failure).failure)

        unmockkObject(RetrofitController)
    }

    @Test
    fun `getConfig make call to get config settings successful response`() = runTest {
        val expected = configResponse
        val result = Response.success(expected)
        coEvery {
            identityService.config()
        } returns result

        val actual = testObject.getConfig()
        assertEquals(expected, (actual as Success).success)
    }

    @Test
    fun `getConfig make call to get config settings failed response`() = runTest {
        val result = Response.error<ArcXPConfig>(
            401,
            getJson("identity_error_response.json").toResponseBody()
        )
        coEvery {
            identityService.config()
        } returns result

        val actual = testObject.getConfig()
        assertEquals(
            ArcXPSDKErrorType.CONFIG_ERROR,
            (((actual as Failure).failure) as ArcXPException).type
        )
        assertEquals("Authentication failed", (actual.failure as ArcXPException).message)
        assertEquals("300041", (actual.failure as ArcXPException).code)
    }

    @Test
    fun `getConfig make call to get config settings exception returns failed response`() = runTest {

        val exception = Exception("myMessage")
        coEvery {
            identityService.config()
        } throws exception

        val actual = testObject.getConfig()

        assertEquals(exception, (actual as Failure).failure)

    }

    @Test
    fun `make call to remove identity successful response - removeIdentity`() = runTest {
        val expected = updateUserStatusResponse
        val result = Response.success(expected)
        coEvery {
            identityService.deleteIdentities("")
        } returns result

        val actual = testObject.removeIdentity("")
        assertEquals(expected, (actual as Success).success)
    }

    @Test
    fun `make call to remove identity failed response - removeIdentity`() = runTest {
        val result = Response.error<ArcXPUpdateUserStatus>(
            401,
            getJson("identity_error_response.json").toResponseBody()
        )
        coEvery {
            identityService.deleteIdentities("")
        } returns result

        val actual = testObject.removeIdentity("")
        assertEquals(
            ArcXPSDKErrorType.SERVER_ERROR,
            (((actual as Failure).failure) as ArcXPException).type
        )
        assertEquals("Authentication failed", (actual.failure as ArcXPException).message)
        assertEquals("300041", (actual.failure as ArcXPException).code)
    }

    @Test
    fun `remove identity - throws exception`() = runTest {
        val expectedMessage = "myMessage"
        val exception = Exception(expectedMessage)
        coEvery { identityService.deleteIdentities(grantType = "") } throws exception

        val result = testObject.removeIdentity(grantType = "")

        assertEquals(
            ArcXPSDKErrorType.SERVER_ERROR,
            ((result as Failure).failure as ArcXPException).type
        )
        assertEquals(
            exception,
            (result.failure as ArcXPException).value
        )
    }

    @Test
    fun `approveDeletion - successful response`() = runTest {
        val expected = mockk<ArcXPDeleteUser>()
        val result = Response.success(expected)
        coEvery {
            identityService.approveDeletion(nonce = nonce, body = any())
        } returns result

        val actual = testObject.approveDeletion(nonce = nonce)
        assertEquals(expected, (actual as Success).success)
    }

    @Test
    fun `approveDeletion failed response`() = runTest {
        val result = Response.error<ArcXPDeleteUser>(
            401,
            getJson("identity_error_response.json").toResponseBody()
        )
        coEvery {
            identityService.approveDeletion(nonce = nonce, body = any())
        } returns result
        val actual = testObject.approveDeletion(nonce = nonce)
        assertEquals(
            ArcXPSDKErrorType.SERVER_ERROR,
            (((actual as Failure).failure) as ArcXPException).type
        )
        assertEquals("Authentication failed", (actual.failure as ArcXPException).message)
        assertEquals("300041", (actual.failure as ArcXPException).code)
    }

    @Test
    fun `approveDeletion - throws exception`() = runTest {
        val expectedMessage = "myMessage"
        val exception = Exception(expectedMessage)

        coEvery { identityService.approveDeletion(nonce = nonce, body = any()) } throws exception

        val actual = testObject.approveDeletion(nonce = nonce)
        assertEquals(
            ArcXPSDKErrorType.SERVER_ERROR,
            ((actual as Failure).failure as ArcXPException).type
        )
        assertEquals(
            exception,
            (actual.failure as ArcXPException).value
        )
    }

    @Test
    fun `appleLogin - successful response`() = runTest {
        val input = mockk<ArcXPAuthRequest>()
        val expected = mockk<ArcXPAuth>()
        val result = Response.success(expected)
        coEvery {
            identityServiceApple.login(authRequest = input)
        } returns result

        val actual = testObject.appleLogin(authRequest = input)
        assertEquals(expected, (actual as Success).success)
    }

    @Test
    fun `appleLogin failed response`() = runTest {
        val input = mockk<ArcXPAuthRequest>()
        val result = Response.error<ArcXPAuth>(
            401,
            getJson("identity_error_response.json").toResponseBody()
        )
        coEvery {
            identityServiceApple.login(authRequest = input)
        } returns result
        val actual = testObject.appleLogin(authRequest = input)
        assertEquals(
            ArcXPSDKErrorType.SERVER_ERROR,
            ((actual as Failure).failure as ArcXPException).type
        )
        assertEquals("Authentication failed", (actual.failure as ArcXPException).message)
        assertEquals("300041", (actual.failure as ArcXPException).code)
    }

    @Test
    fun `appleLogin - throws exception`() = runTest {
        val input = mockk<ArcXPAuthRequest>()
        val expectedMessage = "myMessage"
        val exception = Exception(expectedMessage)

        coEvery { identityServiceApple.login(authRequest = input) } throws exception

        val actual = testObject.appleLogin(authRequest = input)
        assertEquals(
            ArcXPSDKErrorType.SERVER_ERROR,
            ((actual as Failure).failure as ArcXPException).type
        )
        assertEquals(
            exception,
            (actual.failure as ArcXPException).value
        )
    }
}