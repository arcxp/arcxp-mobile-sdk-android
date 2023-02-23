package com.arcxp.commerce.repositories

import com.arcxp.commerce.ArcXPCommerceSDKErrorType
import com.arcxp.commerce.extendedModels.ArcXPProfileManage
import com.arcxp.commerce.models.*
import com.arcxp.commerce.retrofit.IdentityService
import com.arcxp.commerce.retrofit.RetrofitController
import com.arcxp.commons.testutils.TestUtils.getJson
import com.arcxp.commerce.util.ArcXPError
import com.arcxp.commerce.util.AuthManager
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
    lateinit var authResponse: ArcXPAuth

    @MockK
    lateinit var configResponse: ArcXPConfig

    @MockK
    lateinit var updateUserStatusResponse: ArcXPUpdateUserStatus

    @MockK
    lateinit var identityService: IdentityService

    @MockK
    lateinit var identityServiceApple: IdentityService

    private lateinit var testObject: IdentityRepository

    private val nonce = "nonce"

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
            ArcXPCommerceSDKErrorType.SERVER_ERROR,
            (((actual as Failure).failure) as ArcXPError).type
        )
        assertEquals("Authentication failed", (actual.failure as ArcXPError).message)
        assertEquals("300041", (actual.failure as ArcXPError).code)
    }

    @Test
    fun `make call to login user exception response - login`() = runTest {
        val result = Response.error<ArcXPAuth>(
            500,
            getJson("identity_exception_response.json").toResponseBody()
        )
        coEvery {
            identityService.login(any())
        } returns result

        val actual = testObject.login(mockk())
        assertEquals(
            ArcXPCommerceSDKErrorType.SERVER_ERROR,
            (((actual as Failure).failure) as ArcXPError).type
        )
        assertEquals(null, (actual.failure as ArcXPError).message)
        assertEquals(null, (actual.failure as ArcXPError).code)
    }

    @Test
    fun `make call to change user password successful response - changePassword`() = runTest {
        val expected = identityResponse
        val result = Response.success(expected)
        coEvery {
            identityService.changePassword(eq(ArcXPPasswordResetRequest("a", "b")))
        } returns result

        val actual = testObject.changePassword(ArcXPPasswordResetRequest("a", "b"))
        assertEquals(expected, (actual as Success).success)
    }

    @Test
    fun `make call to change user password failed response - changePassword`() = runTest {
        val result = Response.error<ArcXPIdentity>(
            401,
            getJson("identity_error_response.json").toResponseBody()
        )
        coEvery {
            identityService.changePassword(ArcXPPasswordResetRequest("a", "b"))
        } returns result

        val actual = testObject.changePassword(ArcXPPasswordResetRequest("a", "b"))
        assertEquals(
            ArcXPCommerceSDKErrorType.SERVER_ERROR,
            (((actual as Failure).failure) as ArcXPError).type
        )
        assertEquals("Authentication failed", (actual.failure as ArcXPError).message)
        assertEquals("300041", (actual.failure as ArcXPError).code)
    }

    @Test
    fun `make call to change user password exception response - changePassword`() = runTest {
        val result = Response.error<ArcXPIdentity>(
            500,
            getJson("identity_exception_response.json").toResponseBody()
        )
        coEvery {
            identityService.changePassword(any())
        } returns result

        val actual = testObject.changePassword(mockk())
        assertEquals(
            ArcXPCommerceSDKErrorType.SERVER_ERROR,
            (((actual as Failure).failure) as ArcXPError).type
        )
    }

    @Test
    fun `make call to reset user password successful response - resetPassword`() = runTest {
        val expected = ArcXPRequestPasswordReset(true)
        val result = Response.success(expected)
        coEvery {
            identityService.resetPassword(eq(ArcXPResetPasswordRequestRequest("d")))
        } returns result

        val actual = testObject.resetPassword(ArcXPResetPasswordRequestRequest("d"))
        assertEquals(expected, (actual as Success).success)
    }

    @Test
    fun `make call to reset user password failed response - resetPassword`() = runTest {
        val result = Response.error<ArcXPRequestPasswordReset>(
            401,
            getJson("identity_error_response.json").toResponseBody()
        )
        coEvery {
            identityService.resetPassword(ArcXPResetPasswordRequestRequest("d"))
        } returns result

        val actual = testObject.resetPassword(ArcXPResetPasswordRequestRequest("d"))
        assertEquals(
            ArcXPCommerceSDKErrorType.SERVER_ERROR,
            (((actual as Failure).failure) as ArcXPError).type
        )
        assertEquals("Authentication failed", (actual.failure as ArcXPError).message)
        assertEquals("300041", (actual.failure as ArcXPError).code)
    }

    @Test
    fun `make call to reset user password throws exception - resetPassword`() = runTest {
        val result = Response.error<ArcXPRequestPasswordReset>(
            500,
            getJson("identity_exception_response.json").toResponseBody()
        )
        coEvery {
            identityService.resetPassword(any())
        } returns result

        val actual = testObject.resetPassword(mockk())
        assertEquals(
            ArcXPCommerceSDKErrorType.SERVER_ERROR,
            (((actual as Failure).failure) as ArcXPError).type
        )
    }

    @Test
    fun `make call to reset user password with nonce successful response - resetPassword`() =
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
    fun `make call to reset user password with nonce failed response - resetPassword`() = runTest {
        val result = Response.error<ArcXPIdentity>(
            401,
            getJson("identity_error_response.json").toResponseBody()
        )
        coEvery {
            identityService.resetPassword("a", ArcXPResetPasswordNonceRequest("b"))
        } returns result

        val actual = testObject.resetPassword("a", ArcXPResetPasswordNonceRequest("b"))
        assertEquals(
            ArcXPCommerceSDKErrorType.SERVER_ERROR,
            (((actual as Failure).failure) as ArcXPError).type
        )
        assertEquals("Authentication failed", (actual.failure as ArcXPError).message)
        assertEquals("300041", (actual.failure as ArcXPError).code)
    }

    @Test
    fun `make call to reset user password with nonce throws exception - resetPassword`() = runTest {
        val result = Response.error<ArcXPIdentity>(
            500,
            getJson("identity_exception_response.json").toResponseBody()
        )
        coEvery {
            identityService.resetPassword("a", any())
        } returns result

        val actual = testObject.resetPassword("a", mockk())
        assertEquals(
            ArcXPCommerceSDKErrorType.SERVER_ERROR,
            (((actual as Failure).failure) as ArcXPError).type
        )
    }

    @Test
    fun `make call to get one time access link successful response - getMagicLink`() = runTest {
        val expected = ArcXPOneTimeAccessLink(true)
        val result = Response.success(expected)
        coEvery {
            identityService.getMagicLink(ArcXPOneTimeAccessLinkRequest("a"))
        } returns result

        val actual = testObject.getMagicLink(ArcXPOneTimeAccessLinkRequest("a"))
        assertEquals(expected, (actual as Success).success)
    }

    @Test
    fun `make call to get one time access link failed response - getMagicLink`() = runTest {
        val result = Response.error<ArcXPOneTimeAccessLink>(
            401,
            getJson("identity_error_response.json").toResponseBody()
        )
        coEvery {
            identityService.getMagicLink(ArcXPOneTimeAccessLinkRequest("a"))
        } returns result

        val actual = testObject.getMagicLink(ArcXPOneTimeAccessLinkRequest("a"))
        assertEquals(
            ArcXPCommerceSDKErrorType.SERVER_ERROR,
            (((actual as Failure).failure) as ArcXPError).type
        )
        assertEquals("Authentication failed", (actual.failure as ArcXPError).message)
        assertEquals("300041", (actual.failure as ArcXPError).code)
    }

    @Test
    fun `make call to get one time access link failed throw exception - getMagicLink`() = runTest {
        val result = Response.error<ArcXPOneTimeAccessLink>(
            500,
            getJson("identity_exception_response.json").toResponseBody()
        )
        coEvery {
            identityService.getMagicLink(any())
        } returns result

        val actual = testObject.getMagicLink(mockk())
        assertEquals(
            ArcXPCommerceSDKErrorType.SERVER_ERROR,
            (((actual as Failure).failure) as ArcXPError).type
        )
    }

    @Test
    fun `make call to signin with one time access link successful response - loginMagicLink`() =
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
    fun `make call to signin with one time access link failed response - loginMagicLink`() =
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
                ArcXPCommerceSDKErrorType.SERVER_ERROR,
                (((actual as Failure).failure) as ArcXPError).type
            )
            assertEquals("Authentication failed", (actual.failure as ArcXPError).message)
            assertEquals("300041", (actual.failure as ArcXPError).code)
        }

    @Test
    fun `make call to signin with one time access link throw exception - loginMagicLink`() =
        runTest {
            val result = Response.error<ArcXPOneTimeAccessLinkAuth>(
                500,
                getJson("identity_exception_response.json").toResponseBody()
            )
            coEvery {
                identityService.loginMagicLink(any())
            } returns result

            val actual = testObject.loginMagicLink("a")
            assertEquals(
                ArcXPCommerceSDKErrorType.SERVER_ERROR,
                (((actual as Failure).failure) as ArcXPError).type
            )
        }

    @Test
    fun `make call to get profile successful response - getProfile`() = runTest {
        val expected = ArcXPProfileManage("", "", "", uuid = "", identities = identityList)
        val result = Response.success(expected)
        coEvery {
            identityService.getProfile()
        } returns result

        val actual = testObject.getProfile()
        assertEquals(expected, (actual as Success).success)
    }

    @Test
    fun `make call to get profile failed response - getProfile`() = runTest {
        val result = Response.error<ArcXPProfileManage>(
            401,
            getJson("identity_error_response.json").toResponseBody()
        )
        coEvery {
            identityService.getProfile()
        } returns result

        val actual = testObject.getProfile()
        assertEquals(
            ArcXPCommerceSDKErrorType.SERVER_ERROR,
            (((actual as Failure).failure) as ArcXPError).type
        )
        assertEquals("Authentication failed", (actual.failure as ArcXPError).message)
        assertEquals("300041", (actual.failure as ArcXPError).code)
    }

    @Test
    fun `make call to get profile throw exception - getProfile`() = runTest {
        val result = Response.error<ArcXPProfileManage>(
            500,
            getJson("identity_exception_response.json").toResponseBody()
        )
        coEvery {
            identityService.getProfile()
        } returns result

        val actual = testObject.getProfile()
        assertEquals(
            ArcXPCommerceSDKErrorType.SERVER_ERROR,
            (((actual as Failure).failure) as ArcXPError).type
        )
    }

    @Test
    fun `make call to get update user profile successful response - patchProfile`() = runTest {
        val expected = profileManageResponse
        val result = Response.success(expected)
        coEvery {
            identityService.patchProfile(ArcXPProfilePatchRequest("John"))
        } returns result

        val actual = testObject.patchProfile(ArcXPProfilePatchRequest("John"))
        assertEquals(expected, (actual as Success).success)
    }

    @Test
    fun `make call to get update user profile failed response - patchProfile`() = runTest {
        val result = Response.error<ArcXPProfileManage>(
            401,
            getJson("identity_error_response.json").toResponseBody()
        )
        coEvery {
            identityService.patchProfile(ArcXPProfilePatchRequest("a"))
        } returns result

        val actual = testObject.patchProfile(ArcXPProfilePatchRequest("a"))
        assertEquals(
            ArcXPCommerceSDKErrorType.SERVER_ERROR,
            (((actual as Failure).failure) as ArcXPError).type
        )
        assertEquals("Authentication failed", (actual.failure as ArcXPError).message)
        assertEquals("300041", (actual.failure as ArcXPError).code)
    }

    @Test
    fun `make call to get update user profile throw exception - patchProfile`() = runTest {
        val result = Response.error<ArcXPProfileManage>(
            500,
            getJson("identity_exception_response.json").toResponseBody()
        )
        coEvery {
            identityService.patchProfile(any())
        } returns result

        val actual = testObject.patchProfile(mockk())
        assertEquals(
            ArcXPCommerceSDKErrorType.SERVER_ERROR,
            (((actual as Failure).failure) as ArcXPError).type
        )
    }

    @Test
    fun `make call to register new user successful response - signUp`() = runTest {
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
    fun `make call to register new user failed response - signUp`() = runTest {
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
            ArcXPCommerceSDKErrorType.SERVER_ERROR,
            (((actual as Failure).failure) as ArcXPError).type
        )
        assertEquals("Authentication failed", (actual.failure as ArcXPError).message)
        assertEquals("300041", (actual.failure as ArcXPError).code)
    }

    @Test
    fun `make call to register new user throw exception - signUp`() = runTest {
        val result = Response.error<ArcXPUser>(
            500,
            getJson("identity_exception_response.json").toResponseBody()
        )
        coEvery {
            identityService.signUp(any())
        } returns result

        val actual = testObject.signUp(mockk())

        assertEquals(
            ArcXPCommerceSDKErrorType.SERVER_ERROR,
            (((actual as Failure).failure) as ArcXPError).type
        )
    }

    @Test
    fun `make call to verify email address successful response - verifyEmail`() = runTest {
        val expected = ArcXPEmailVerification(true)
        val result = Response.success(expected)
        coEvery {
            identityService.verifyEmail(ArcXPVerifyEmailRequest("a"))
        } returns result

        val actual = testObject.verifyEmail(ArcXPVerifyEmailRequest("a"))
        assertEquals(expected, (actual as Success).success)
    }

    @Test
    fun `make call to verify email address failed response - verifyEmail`() = runTest {
        val result = Response.error<ArcXPEmailVerification>(
            401,
            getJson("identity_error_response.json").toResponseBody()
        )
        coEvery {
            identityService.verifyEmail(ArcXPVerifyEmailRequest("a"))
        } returns result

        val actual = testObject.verifyEmail(ArcXPVerifyEmailRequest("a"))
        assertEquals(
            ArcXPCommerceSDKErrorType.SERVER_ERROR,
            (((actual as Failure).failure) as ArcXPError).type
        )
        assertEquals("Authentication failed", (actual.failure as ArcXPError).message)
        assertEquals("300041", (actual.failure as ArcXPError).code)
    }

    @Test
    fun `make call to verify email address throw exception - verifyEmail`() = runTest {
        val result = Response.error<ArcXPEmailVerification>(
            500,
            getJson("identity_exception_response.json").toResponseBody()
        )
        coEvery {
            identityService.verifyEmail(ArcXPVerifyEmailRequest("a"))
        } returns result

        val actual = testObject.verifyEmail(ArcXPVerifyEmailRequest("a"))
        assertEquals(
            ArcXPCommerceSDKErrorType.SERVER_ERROR,
            (((actual as Failure).failure) as ArcXPError).type
        )
    }

    @Test
    fun `make call to verify email from nonce successful response - verifyEmailNonce`() = runTest {
        val expected = ArcXPEmailVerification(true)
        val result = Response.success(expected)
        coEvery {
            identityService.verifyEmail("a")
        } returns result

        val actual = testObject.verifyEmailNonce("a")
        assertEquals(expected, (actual as Success).success)
    }

    @Test
    fun `make call to verify email from nonce failed response - verifyEmailNonce`() = runTest {
        val result = Response.error<ArcXPEmailVerification>(
            401,
            getJson("identity_error_response.json").toResponseBody()
        )
        coEvery {
            identityService.verifyEmail("a")
        } returns result

        val actual = testObject.verifyEmailNonce("a")
        assertEquals(
            ArcXPCommerceSDKErrorType.SERVER_ERROR,
            (((actual as Failure).failure) as ArcXPError).type
        )
        assertEquals("Authentication failed", (actual.failure as ArcXPError).message)
        assertEquals("300041", (actual.failure as ArcXPError).code)
    }

    @Test
    fun `make call to verify email from nonce throw exception - verifyEmailNonce`() = runTest {
        val result = Response.error<ArcXPEmailVerification>(
            500,
            getJson("identity_exception_response.json").toResponseBody()
        )
        coEvery {
            identityService.verifyEmail("a")
        } returns result

        val actual = testObject.verifyEmailNonce("a")
        assertEquals(
            ArcXPCommerceSDKErrorType.SERVER_ERROR,
            (((actual as Failure).failure) as ArcXPError).type
        )
    }

    @Test
    fun `make call to log user out successful response - logout`() = runTest {
        val expected = Unit as? Void
        val result = Response.success(expected)
        coEvery {
            identityService.logout()
        } returns result

        val actual = testObject.logout()
        assertEquals(expected, (actual as Success).success)
    }

    @Test
    fun `make call to log user out failed response - logout`() = runTest {
        val result =
            Response.error<Void>(401, getJson("identity_error_response.json").toResponseBody())
        coEvery {
            identityService.logout()
        } returns result

        val actual = testObject.logout()
        assertEquals(
            ArcXPCommerceSDKErrorType.SERVER_ERROR,
            (((actual as Failure).failure) as ArcXPError).type
        )
        assertEquals("Authentication failed", (actual.failure as ArcXPError).message)
        assertEquals("300041", (actual.failure as ArcXPError).code)
    }

    @Test
    fun `make call to log user out throw exception - logout`() = runTest {
        val result =
            Response.error<Void>(500, getJson("identity_exception_response.json").toResponseBody())
        coEvery {
            identityService.logout()
        } returns result

        val actual = testObject.logout()
        assertEquals(
            ArcXPCommerceSDKErrorType.SERVER_ERROR,
            (((actual as Failure).failure) as ArcXPError).type
        )
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
                ArcXPCommerceSDKErrorType.INVALID_SESSION,
                (((actual as Failure).failure) as ArcXPError).type
            )
            assertEquals("Authentication failed", (actual.failure as ArcXPError).message)
            assertEquals("300041", (actual.failure as ArcXPError).code)
        }

    @Test
    fun `make call to verify cached access token is valid throw exception - validateJwt`() =
        runTest {
            val result = Response.error<ArcXPAuth>(
                500,
                getJson("identity_exception_response.json").toResponseBody()
            )
            coEvery {
                identityService.recapToken(any())
            } returns result

            val actual = testObject.validateJwt()
            assertEquals(
                ArcXPCommerceSDKErrorType.SERVER_ERROR,
                (((actual as Failure).failure) as ArcXPError).type
            )
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
            ArcXPCommerceSDKErrorType.INVALID_SESSION,
            (((actual as Failure).failure) as ArcXPError).type
        )
        assertEquals("Authentication failed", (actual.failure as ArcXPError).message)
        assertEquals("300041", (actual.failure as ArcXPError).code)
    }

    @Test
    fun `make call to validate access token throw exception - validateJwt`() = runTest {
        val result = Response.error<ArcXPAuth>(
            500,
            getJson("identity_exception_response.json").toResponseBody()
        )
        coEvery {
            identityService.recapToken(any())
        } returns result

        val actual = testObject.validateJwt("")
        assertEquals(
            ArcXPCommerceSDKErrorType.SERVER_ERROR,
            (((actual as Failure).failure) as ArcXPError).type
        )
    }

    @Test
    fun `make call to extend user session successful response - refreshToken`() = runTest {
        val expected = authResponse
        val result = Response.success(expected)
        coEvery {
            identityService.recapToken(ArcXPAuthRequest(token = "", grantType = "refresh-token"))
        } returns result

        val actual = testObject.refreshToken(token = "", grantType = "refresh-token")
        assertEquals(expected, (actual as Success).success)
    }

    @Test
    fun `make call to extend user session failed response - refreshToken`() = runTest {
        val result =
            Response.error<ArcXPAuth>(401, getJson("identity_error_response.json").toResponseBody())
        coEvery {
            identityService.recapToken(ArcXPAuthRequest(token = "", grantType = "refresh-token"))
        } returns result

        val actual = testObject.refreshToken(token = "", grantType = "refresh-token")
        assertEquals(
            ArcXPCommerceSDKErrorType.SERVER_ERROR,
            (((actual as Failure).failure) as ArcXPError).type
        )
        assertEquals("Authentication failed", (actual.failure as ArcXPError).message)
        assertEquals("300041", (actual.failure as ArcXPError).code)
    }

    @Test
    fun `make call to extend user session throw exception - refreshToken`() = runTest {
        val result = Response.error<ArcXPAuth>(
            500,
            getJson("identity_exception_response.json").toResponseBody()
        )
        coEvery {
            identityService.recapToken(any())
        } returns result

        val actual = testObject.refreshToken("", "")
        assertEquals(
            ArcXPCommerceSDKErrorType.SERVER_ERROR,
            (((actual as Failure).failure) as ArcXPError).type
        )
    }

    @Test
    fun `make call to request deletion of user successful response - deleteUser`() = runTest {
        val expected = ArcXPAnonymizeUser(true)
        val result = Response.success(expected)
        coEvery {
            identityService.deleteUser()
        } returns result

        val actual = testObject.deleteUser()
        assertEquals(expected, (actual as Success).success)
    }

    @Test
    fun `make call to request deletion of user failed response - deleteUser`() = runTest {
        val result = Response.error<ArcXPAnonymizeUser>(
            401,
            getJson("identity_error_response.json").toResponseBody()
        )
        coEvery {
            identityService.deleteUser()
        } returns result

        val actual = testObject.deleteUser()
        assertEquals(
            ArcXPCommerceSDKErrorType.SERVER_ERROR,
            (((actual as Failure).failure) as ArcXPError).type
        )
        assertEquals("Authentication failed", (actual.failure as ArcXPError).message)
        assertEquals("300041", (actual.failure as ArcXPError).code)
    }

    @Test
    fun `make call to request deletion of user throw exception - deleteUser`() = runTest {
        val result = Response.error<ArcXPAnonymizeUser>(
            500,
            getJson("identity_exception_response.json").toResponseBody()
        )
        coEvery {
            identityService.deleteUser()
        } returns result

        val actual = testObject.deleteUser()

        assertEquals(
            ArcXPCommerceSDKErrorType.SERVER_ERROR,
            (((actual as Failure).failure) as ArcXPError).type
        )
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
            ArcXPCommerceSDKErrorType.SERVER_ERROR,
            (((actual as Failure).failure) as ArcXPError).type
        )
        assertEquals("Authentication failed", (actual.failure as ArcXPError).message)
        assertEquals("300041", (actual.failure as ArcXPError).code)
    }


    @Test
    fun `appleAuthUrl - make call to get apple auth url throw exception`() = runTest {
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
            ArcXPCommerceSDKErrorType.SERVER_ERROR,
            (((actual as Failure).failure) as ArcXPError).type
        )
        assertEquals("Authentication failed", (actual.failure as ArcXPError).message)
        assertEquals("300041", (actual.failure as ArcXPError).code)
    }


    @Test
    fun `appleAuthUrlUpdatedURL - make call to get apple auth url throw exception`() = runTest {
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
            ArcXPCommerceSDKErrorType.CONFIG_ERROR,
            (((actual as Failure).failure) as ArcXPError).type
        )
        assertEquals("Authentication failed", (actual.failure as ArcXPError).message)
        assertEquals("300041", (actual.failure as ArcXPError).code)
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
            ArcXPCommerceSDKErrorType.SERVER_ERROR,
            (((actual as Failure).failure) as ArcXPError).type
        )
        assertEquals("Authentication failed", (actual.failure as ArcXPError).message)
        assertEquals("300041", (actual.failure as ArcXPError).code)
    }

    @Test
    fun `remove identity - throws exception`() = runTest {
        val expectedMessage = "myMessage"
        val exception = Exception(expectedMessage)
        coEvery { identityService.deleteIdentities(grantType = "") } throws exception

        val result = testObject.removeIdentity(grantType = "")

        assertEquals(
            ArcXPCommerceSDKErrorType.SERVER_ERROR,
            ((result as Failure).failure as ArcXPError).type
        )
        assertEquals(
            exception,
            (result.failure as ArcXPError).value
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
            ArcXPCommerceSDKErrorType.SERVER_ERROR,
            (((actual as Failure).failure) as ArcXPError).type
        )
        assertEquals("Authentication failed", (actual.failure as ArcXPError).message)
        assertEquals("300041", (actual.failure as ArcXPError).code)
    }

    @Test
    fun `approveDeletion - throw exception`() = runTest {
        val expectedMessage = "myMessage"
        val exception = Exception(expectedMessage)

        coEvery { identityService.approveDeletion(nonce = nonce, body = any()) } throws exception

        val actual = testObject.approveDeletion(nonce = nonce)
        assertEquals(
            ArcXPCommerceSDKErrorType.SERVER_ERROR,
            ((actual as Failure).failure as ArcXPError).type
        )
        assertEquals(
            exception,
            (actual.failure as ArcXPError).value
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
            ArcXPCommerceSDKErrorType.SERVER_ERROR,
            ((actual as Failure).failure as ArcXPError).type
        )
        assertEquals("Authentication failed", (actual.failure as ArcXPError).message)
        assertEquals("300041", (actual.failure as ArcXPError).code)
    }

    @Test
    fun `appleLogin - throw exception`() = runTest {
        val input = mockk<ArcXPAuthRequest>()
        val expectedMessage = "myMessage"
        val exception = Exception(expectedMessage)

        coEvery { identityServiceApple.login(authRequest = input) } throws exception

        val actual = testObject.appleLogin(authRequest = input)
        assertEquals(
            ArcXPCommerceSDKErrorType.SERVER_ERROR,
            ((actual as Failure).failure as ArcXPError).type
        )
        assertEquals(
            exception,
            (actual.failure as ArcXPError).value
        )
    }
}