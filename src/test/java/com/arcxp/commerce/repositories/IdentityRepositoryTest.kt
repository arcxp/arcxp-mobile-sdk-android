package com.arcxp.commerce.repositories

import com.arcxp.commerce.ArcXPCommerceSDKErrorType
import com.arcxp.commerce.extendedModels.ArcXPProfileManage
import com.arcxp.commerce.models.*
import com.arcxp.commerce.repositories.IdentityRepository
import com.arcxp.commerce.retrofit.IdentityService
import com.arcxp.commerce.util.*
import com.arcxp.commerce.util.ArcXPError
import io.mockk.*
import io.mockk.impl.annotations.RelaxedMockK
import junit.framework.Assert.assertEquals
import kotlinx.coroutines.runBlocking
import okhttp3.ResponseBody
import org.junit.Before
import org.junit.Test
import retrofit2.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import java.io.File


class IdentityRepositoryTest {

    private lateinit var testObject: IdentityRepository

    private val identityResponse = ArcXPIdentity("", "", "", "", "", 1, "", true, "", "", true)

    private val identityList = listOf(identityResponse)

    private var profileManageResponse = ArcXPProfileManage("01/01/2020", "01/01/2020", "", "John", "Doe", "", "John Doe", "male", "jdoe@arctesting.com",
        "", "p", "1900", "01", "01", false, identities = identityList, uuid = "123123")

    private val profileDeletion = ArcXPProfileDeletionRule("", "", "", "", "", 1, "", 1, 1, 1, 1, "")

    private val profile = ArcXPProfileNotificationEvent("", "", "", "", "", 1, profileDeletion, "", "", "", "", 1)

    private val profileResponse = ArcXPProfile("", "", "", "", "", "", "", "", "",
        "", "", "", "", "", "", "", "", listOf(), listOf(), listOf(), listOf(), 0, profile)

    private val profileRequest = ArcXPProfileRequest("", "", null, null, null, "", null, null,
        null, null, null, null, null, null, null)

    private val authResponse = ArcXPAuth("", "", "","", "", "")

    private val configResponse = ArcXPConfig("", "", null, false, false,false,"",1,1,1,1,null, "", "", "", 1)

    private val updateUserStatusResponse = ArcXPUpdateUserStatus("", "", "", "", "", "", "")

    @RelaxedMockK
    lateinit var identityService: IdentityService

    @RelaxedMockK
    lateinit var identityServiceApple: IdentityService

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        testObject = IdentityRepository(identityService, identityServiceApple )
        mockkObject(AuthManager)
        every {AuthManager.getInstance() } returns mockk {
            every { accessToken = any() } just Runs
        }
    }

    @Test
    fun `make call to login user successful response - login`(){
        val expected = ArcXPAuth("123", "abc", "", "", "", "")
        val result = Response.success(expected)
        coEvery {
            identityService.login(eq(ArcXPAuthRequest("d", "code")))
        }returns result
        runBlocking {
            val actual = testObject.login(ArcXPAuthRequest("d", "code"))
            assertEquals(expected, (actual as Success).r)
        }
    }

    @Test
    fun `make call to login user failed response - login`(){
        val result = Response.error<ArcXPAuth>(401, getJson("identity_error_response.json").toResponseBody())
        coEvery {
            identityService.login(any())
        } returns result
        runBlocking {
            val actual = testObject.login(mockk())
            assertEquals(ArcXPCommerceSDKErrorType.SERVER_ERROR, (((actual as Failure).l) as ArcXPError).type)
            assertEquals("Authentication failed", (actual.l as ArcXPError).message)
            assertEquals("300041", (actual.l as ArcXPError).code)
        }
    }

    @Test
    fun `make call to login user exception response - login`(){
        val result = Response.error<ArcXPAuth>(500, getJson("identity_exception_response.json").toResponseBody())
        coEvery {
            identityService.login(any())
        } returns result
        runBlocking {
            val actual = testObject.login(mockk())
            assertEquals(ArcXPCommerceSDKErrorType.SERVER_ERROR, (((actual as Failure).l) as ArcXPError).type)
            assertEquals(null, (actual.l as ArcXPError).message)
            assertEquals(null, (actual.l as ArcXPError).code)
        }
    }

    @Test
    fun `make call to change user password successful response - changePassword`(){
        val expected = identityResponse
        val result = Response.success(expected)
        coEvery {
            identityService.changePassword(eq(ArcXPPasswordResetRequest("a", "b")))
        }returns result
        runBlocking {
            val actual = testObject.changePassword(ArcXPPasswordResetRequest("a", "b"))
            assertEquals(expected, (actual as Success).r)
        }
    }

    @Test
    fun `make call to change user password failed response - changePassword`(){
        val result = Response.error<ArcXPIdentity>(401, getJson("identity_error_response.json").toResponseBody())
        coEvery {
            identityService.changePassword(ArcXPPasswordResetRequest("a", "b"))
        } returns result
        runBlocking {
            val actual = testObject.changePassword(ArcXPPasswordResetRequest("a", "b"))
            assertEquals(ArcXPCommerceSDKErrorType.SERVER_ERROR, (((actual as Failure).l) as ArcXPError).type)
            assertEquals("Authentication failed", (actual.l as ArcXPError).message)
            assertEquals("300041", (actual.l as ArcXPError).code)
        }
    }

    @Test
    fun `make call to change user password exception response - changePassword`(){
        val result = Response.error<ArcXPIdentity>(500, getJson("identity_exception_response.json").toResponseBody())
        coEvery {
            identityService.changePassword(any())
        } returns result
        runBlocking {
            val actual = testObject.changePassword(mockk())
            assertEquals(ArcXPCommerceSDKErrorType.SERVER_ERROR, (((actual as Failure).l) as ArcXPError).type)
        }
    }
    @Test
    fun `make call to reset user password successful response - resetPassword`(){
        val expected = ArcXPRequestPasswordReset(true)
        val result = Response.success(expected)
        coEvery {
            identityService.resetPassword(eq(ArcXPResetPasswordRequestRequest("d")))
        }returns result
        runBlocking {
            val actual = testObject.resetPassword(ArcXPResetPasswordRequestRequest("d"))
            assertEquals(expected, (actual as Success).r)
        }
    }

    @Test
    fun `make call to reset user password failed response - resetPassword`(){
        val result = Response.error<ArcXPRequestPasswordReset>(401, getJson("identity_error_response.json").toResponseBody())
        coEvery {
            identityService.resetPassword(ArcXPResetPasswordRequestRequest("d"))
        } returns result
        runBlocking {
            val actual = testObject.resetPassword(ArcXPResetPasswordRequestRequest("d"))
            assertEquals(ArcXPCommerceSDKErrorType.SERVER_ERROR, (((actual as Failure).l) as ArcXPError).type)
            assertEquals("Authentication failed", (actual.l as ArcXPError).message)
            assertEquals("300041", (actual.l as ArcXPError).code)
        }
    }

    @Test
    fun `make call to reset user password throws exception - resetPassword`(){
        val result = Response.error<ArcXPRequestPasswordReset>(500, getJson("identity_exception_response.json").toResponseBody())
        coEvery {
            identityService.resetPassword(any())
        } returns result
        runBlocking {
            val actual = testObject.resetPassword(mockk())
            assertEquals(ArcXPCommerceSDKErrorType.SERVER_ERROR, (((actual as Failure).l) as ArcXPError).type)
        }
    }
    @Test
    fun `make call to reset user password with nonce successful response - resetPassword`(){
        val expected = identityResponse
        val result = Response.success(expected)
        coEvery {
            identityService.resetPassword("a", ArcXPResetPasswordNonceRequest("abc"))
        }returns result
        runBlocking {
            val actual = testObject.resetPassword("a", ArcXPResetPasswordNonceRequest("abc"))
            assertEquals(expected, (actual as Success).r)
        }
    }

    @Test
    fun `make call to reset user password with nonce failed response - resetPassword`(){
        val result = Response.error<ArcXPIdentity>(401, getJson("identity_error_response.json").toResponseBody())
        coEvery {
            identityService.resetPassword("a", ArcXPResetPasswordNonceRequest("b"))
        } returns result
        runBlocking {
            val actual = testObject.resetPassword("a", ArcXPResetPasswordNonceRequest("b"))
            assertEquals(ArcXPCommerceSDKErrorType.SERVER_ERROR, (((actual as Failure).l) as ArcXPError).type)
            assertEquals("Authentication failed", (actual.l as ArcXPError).message)
            assertEquals("300041", (actual.l as ArcXPError).code)
        }
    }

    @Test
    fun `make call to reset user password with nonce throws exception - resetPassword`(){
        val result = Response.error<ArcXPIdentity>(500, getJson("identity_exception_response.json").toResponseBody())
        coEvery {
            identityService.resetPassword("a", any())
        } returns result
        runBlocking {
            val actual = testObject.resetPassword("a", mockk())
            assertEquals(ArcXPCommerceSDKErrorType.SERVER_ERROR, (((actual as Failure).l) as ArcXPError).type)
        }
    }

    @Test
    fun `make call to get one time access link successful response - getMagicLink`(){
        val expected = ArcXPOneTimeAccessLink(true)
        val result = Response.success(expected)
        coEvery {
            identityService.getMagicLink(ArcXPOneTimeAccessLinkRequest("a"))
        }returns result
        runBlocking {
            val actual = testObject.getMagicLink(ArcXPOneTimeAccessLinkRequest("a"))
            assertEquals(expected, (actual as Success).r)
        }
    }

    @Test
    fun `make call to get one time access link failed response - getMagicLink`(){
        val result = Response.error<ArcXPOneTimeAccessLink>(401, getJson("identity_error_response.json").toResponseBody())
        coEvery {
            identityService.getMagicLink(ArcXPOneTimeAccessLinkRequest("a"))
        } returns result
        runBlocking {
            val actual = testObject.getMagicLink(ArcXPOneTimeAccessLinkRequest("a"))
            assertEquals(ArcXPCommerceSDKErrorType.SERVER_ERROR, (((actual as Failure).l) as ArcXPError).type)
            assertEquals("Authentication failed", (actual.l as ArcXPError).message)
            assertEquals("300041", (actual.l as ArcXPError).code)
        }
    }

    @Test
    fun `make call to get one time access link failed throw exception - getMagicLink`(){
        val result = Response.error<ArcXPOneTimeAccessLink>(500, getJson("identity_exception_response.json").toResponseBody())
        coEvery {
            identityService.getMagicLink(any())
        } returns result
        runBlocking {
            val actual = testObject.getMagicLink(mockk())
            assertEquals(ArcXPCommerceSDKErrorType.SERVER_ERROR, (((actual as Failure).l) as ArcXPError).type)
        }
    }

    @Test
    fun `make call to signin with one time access link successful response - loginMagicLink`(){
        val expected = ArcXPOneTimeAccessLinkAuth("", "")
        val result = Response.success(expected)
        coEvery {
            identityService.loginMagicLink("a")
        }returns result
        runBlocking {
            val actual = testObject.loginMagicLink("a")
            assertEquals(expected, (actual as Success).r)
        }
    }

    @Test
    fun `make call to signin with one time access link failed response - loginMagicLink`(){
        val result = Response.error<ArcXPOneTimeAccessLinkAuth>(401, getJson("identity_error_response.json").toResponseBody())
        coEvery {
            identityService.loginMagicLink("a")
        } returns result
        runBlocking {
            val actual = testObject.loginMagicLink("a")
            assertEquals(ArcXPCommerceSDKErrorType.SERVER_ERROR, (((actual as Failure).l) as ArcXPError).type)
            assertEquals("Authentication failed", (actual.l as ArcXPError).message)
            assertEquals("300041", (actual.l as ArcXPError).code)
        }
    }

    @Test
    fun `make call to signin with one time access link throw exception - loginMagicLink`(){
        val result = Response.error<ArcXPOneTimeAccessLinkAuth>(500, getJson("identity_exception_response.json").toResponseBody())
        coEvery {
            identityService.loginMagicLink(any())
        } returns result
        runBlocking {
            val actual = testObject.loginMagicLink("a")
            assertEquals(ArcXPCommerceSDKErrorType.SERVER_ERROR, (((actual as Failure).l) as ArcXPError).type)
        }
    }

    @Test
    fun `make call to get profile successful response - getProfile`(){
        val expected = ArcXPProfileManage("", "", "", uuid = "", identities = identityList)
        val result = Response.success(expected)
        coEvery {
            identityService.getProfile()
        }returns result
        runBlocking {
            val actual = testObject.getProfile()
            assertEquals(expected, (actual as Success).r)
        }
    }

    @Test
    fun `make call to get profile failed response - getProfile`(){
        val result = Response.error<ArcXPProfileManage>(401, getJson("identity_error_response.json").toResponseBody())
        coEvery {
            identityService.getProfile()
        } returns result
        runBlocking {
            val actual = testObject.getProfile()
            assertEquals(ArcXPCommerceSDKErrorType.SERVER_ERROR, (((actual as Failure).l) as ArcXPError).type)
            assertEquals("Authentication failed", (actual.l as ArcXPError).message)
            assertEquals("300041", (actual.l as ArcXPError).code)
        }
    }

    @Test
    fun `make call to get profile throw exception - getProfile`(){
        val result = Response.error<ArcXPProfileManage>(500, getJson("identity_exception_response.json").toResponseBody())
        coEvery {
            identityService.getProfile()
        } returns result
        runBlocking {
            val actual = testObject.getProfile()
            assertEquals(ArcXPCommerceSDKErrorType.SERVER_ERROR, (((actual as Failure).l) as ArcXPError).type)
        }
    }

    @Test
    fun `make call to get update user profile successful response - patchProfile`(){
        val expected = profileManageResponse
        val result = Response.success(expected)
        coEvery {
            identityService.patchProfile(ArcXPProfilePatchRequest("John"))
        }returns result
        runBlocking {
            val actual = testObject.patchProfile(ArcXPProfilePatchRequest("John"))
            assertEquals(expected, (actual as Success).r)
        }
    }

    @Test
    fun `make call to get update user profile failed response - patchProfile`(){
        val result = Response.error<ArcXPProfileManage>(401, getJson("identity_error_response.json").toResponseBody())
        coEvery {
            identityService.patchProfile(ArcXPProfilePatchRequest("a"))
        } returns result
        runBlocking {
            val actual = testObject.patchProfile(ArcXPProfilePatchRequest("a"))
            assertEquals(ArcXPCommerceSDKErrorType.SERVER_ERROR, (((actual as Failure).l) as ArcXPError).type)
            assertEquals("Authentication failed", (actual.l as ArcXPError).message)
            assertEquals("300041", (actual.l as ArcXPError).code)
        }
    }

    @Test
    fun `make call to get update user profile throw exception - patchProfile`(){
        val result = Response.error<ArcXPProfileManage>(500, getJson("identity_exception_response.json").toResponseBody())
        coEvery {
            identityService.patchProfile(any())
        } returns result
        runBlocking {
            val actual = testObject.patchProfile(mockk())
            assertEquals(ArcXPCommerceSDKErrorType.SERVER_ERROR, (((actual as Failure).l) as ArcXPError).type)
        }
    }
    @Test
    fun `make call to register new user successful response - signUp`(){
        val expected = ArcXPUser("", true, identityList, profileResponse )
        val result = Response.success(expected)
        coEvery {
            identityService.signUp(ArcXPSignUpRequest(ArcXPIdentityRequest("", "", null), profileRequest))
        }returns result
        runBlocking {
            val actual = testObject.signUp(ArcXPSignUpRequest(ArcXPIdentityRequest("", "", null), profileRequest))
            assertEquals(expected, (actual as Success).r)
        }
    }

    @Test
    fun `make call to register new user failed response - signUp`(){
        val result = Response.error<ArcXPUser>(401, getJson("identity_error_response.json").toResponseBody())
        coEvery {
            identityService.signUp(ArcXPSignUpRequest(ArcXPIdentityRequest("", "", null), profileRequest))
        } returns result
        runBlocking {
            val actual = testObject.signUp(ArcXPSignUpRequest(ArcXPIdentityRequest("", "", null), profileRequest))
            assertEquals(ArcXPCommerceSDKErrorType.SERVER_ERROR, (((actual as Failure).l) as ArcXPError).type)
            assertEquals("Authentication failed", (actual.l as ArcXPError).message)
            assertEquals("300041", (actual.l as ArcXPError).code)
        }
    }

    @Test
    fun `make call to register new user throw exception - signUp`(){
        val result = Response.error<ArcXPUser>(500, getJson("identity_exception_response.json").toResponseBody())
        coEvery {
            identityService.signUp(any())
        } returns result
        runBlocking {
            val actual = testObject.signUp(mockk())
            assertEquals(ArcXPCommerceSDKErrorType.SERVER_ERROR, (((actual as Failure).l) as ArcXPError).type)
        }
    }

    @Test
    fun `make call to verify email address successful response - verifyEmail`(){
        val expected = ArcXPEmailVerification(true)
        val result = Response.success(expected)
        coEvery {
            identityService.verifyEmail(ArcXPVerifyEmailRequest("a"))
        }returns result
        runBlocking {
            val actual = testObject.verifyEmail(ArcXPVerifyEmailRequest("a"))
            assertEquals(expected, (actual as Success).r)
        }
    }

    @Test
    fun `make call to verify email address failed response - verifyEmail`(){
        val result = Response.error<ArcXPEmailVerification>(401, getJson("identity_error_response.json").toResponseBody())
        coEvery {
            identityService.verifyEmail(ArcXPVerifyEmailRequest("a"))
        } returns result
        runBlocking {
            val actual = testObject.verifyEmail(ArcXPVerifyEmailRequest("a"))
            assertEquals(ArcXPCommerceSDKErrorType.SERVER_ERROR, (((actual as Failure).l) as ArcXPError).type)
            assertEquals("Authentication failed", (actual.l as ArcXPError).message)
            assertEquals("300041", (actual.l as ArcXPError).code)
        }
    }

    @Test
    fun `make call to verify email address throw exception - verifyEmail`(){
        val result = Response.error<ArcXPEmailVerification>(500, getJson("identity_exception_response.json").toResponseBody())
        coEvery {
            identityService.verifyEmail(ArcXPVerifyEmailRequest("a"))
        } returns result
        runBlocking {
            val actual = testObject.verifyEmail(ArcXPVerifyEmailRequest("a"))
            assertEquals(ArcXPCommerceSDKErrorType.SERVER_ERROR, (((actual as Failure).l) as ArcXPError).type)
        }
    }

    @Test
    fun `make call to verify email from nonce successful response - verifyEmailNonce`(){
        val expected = ArcXPEmailVerification(true)
        val result = Response.success(expected)
        coEvery {
            identityService.verifyEmail("a")
        }returns result
        runBlocking {
            val actual = testObject.verifyEmailNonce("a")
            assertEquals(expected, (actual as Success).r)
        }
    }

    @Test
    fun `make call to verify email from nonce failed response - verifyEmailNonce`(){
        val result = Response.error<ArcXPEmailVerification>(401, getJson("identity_error_response.json").toResponseBody())
        coEvery {
            identityService.verifyEmail("a")
        } returns result
        runBlocking {
            val actual = testObject.verifyEmailNonce("a")
            assertEquals(ArcXPCommerceSDKErrorType.SERVER_ERROR, (((actual as Failure).l) as ArcXPError).type)
            assertEquals("Authentication failed", (actual.l as ArcXPError).message)
            assertEquals("300041", (actual.l as ArcXPError).code)
        }
    }

    @Test
    fun `make call to verify email from nonce throw exception - verifyEmailNonce`(){
        val result = Response.error<ArcXPEmailVerification>(500, getJson("identity_exception_response.json").toResponseBody())
        coEvery {
            identityService.verifyEmail("a")
        } returns result
        runBlocking {
            val actual = testObject.verifyEmailNonce("a")
            assertEquals(ArcXPCommerceSDKErrorType.SERVER_ERROR, (((actual as Failure).l) as ArcXPError).type)
        }
    }

    @Test
    fun `make call to log user out successful response - logout`(){
        val expected = Unit as? Void
        val result = Response.success(expected)
        coEvery {
            identityService.logout()
        }returns result
        runBlocking {
            val actual = testObject.logout()
            assertEquals(expected, (actual as Success).r)
        }
    }

    @Test
    fun `make call to log user out failed response - logout`(){
        val result = Response.error<Void>(401, getJson("identity_error_response.json").toResponseBody())
        coEvery {
            identityService.logout()
        } returns result
        runBlocking {
            val actual = testObject.logout()
            assertEquals(ArcXPCommerceSDKErrorType.SERVER_ERROR, (((actual as Failure).l) as ArcXPError).type)
            assertEquals("Authentication failed", (actual.l as ArcXPError).message)
            assertEquals("300041", (actual.l as ArcXPError).code)
        }
    }

    @Test
    fun `make call to log user out throw exception - logout`(){
        val result = Response.error<Void>(500, getJson("identity_exception_response.json").toResponseBody())
        coEvery {
            identityService.logout()
        } returns result
        runBlocking {
            val actual = testObject.logout()
            assertEquals(ArcXPCommerceSDKErrorType.SERVER_ERROR, (((actual as Failure).l) as ArcXPError).type)
        }
    }

    @Test
    fun `make call to verify cached access token is valid successful response - validateJwt`(){
        val expected = authResponse
        val result = Response.success(expected)
        coEvery {
            identityService.recapToken(ArcXPAuthRequest())
        }returns result
        runBlocking {
            val actual = testObject.validateJwt()
            assertEquals(expected, (actual as Success).r)
        }
    }

    @Test
    fun `make call to verify cached access token is valid failed response - validateJwt`(){
        val result = Response.error<ArcXPAuth>(401, getJson("identity_error_response.json").toResponseBody())
        coEvery {
            identityService.recapToken(ArcXPAuthRequest())
        } returns result
        runBlocking {
            val actual = testObject.validateJwt()
            assertEquals(ArcXPCommerceSDKErrorType.INVALID_SESSION, (((actual as Failure).l) as ArcXPError).type)
            assertEquals("Authentication failed", (actual.l as ArcXPError).message)
            assertEquals("300041", (actual.l as ArcXPError).code)
        }
    }

    @Test
    fun `make call to verify cached access token is valid throw exception - validateJwt`(){
        val result = Response.error<ArcXPAuth>(500, getJson("identity_exception_response.json").toResponseBody())
        coEvery {
            identityService.recapToken(any())
        } returns result
        runBlocking {
            val actual = testObject.validateJwt()
            assertEquals(ArcXPCommerceSDKErrorType.SERVER_ERROR, (((actual as Failure).l) as ArcXPError).type)
        }
    }

    @Test
    fun `make call to validate access token successful response - validateJwt`(){
        val expected = authResponse
        val result = Response.success(expected)
        coEvery {
            identityService.recapToken(ArcXPAuthRequest(token = ""))
        }returns result
        runBlocking {
            val actual = testObject.validateJwt("")
            assertEquals(expected, (actual as Success).r)
        }
    }

    @Test
    fun `make call to validate access token failed response - validateJwt`(){
        val result = Response.error<ArcXPAuth>(401, getJson("identity_error_response.json").toResponseBody())
        coEvery {
            identityService.recapToken(ArcXPAuthRequest(token = ""))
        } returns result
        runBlocking {
            val actual = testObject.validateJwt("")
            assertEquals(ArcXPCommerceSDKErrorType.INVALID_SESSION, (((actual as Failure).l) as ArcXPError).type)
            assertEquals("Authentication failed", (actual.l as ArcXPError).message)
            assertEquals("300041", (actual.l as ArcXPError).code)
        }
    }

    @Test
    fun `make call to validate access token throw exception - validateJwt`(){
        val result = Response.error<ArcXPAuth>(500, getJson("identity_exception_response.json").toResponseBody())
        coEvery {
            identityService.recapToken(any())
        } returns result
        runBlocking {
            val actual = testObject.validateJwt("")
            assertEquals(ArcXPCommerceSDKErrorType.SERVER_ERROR, (((actual as Failure).l) as ArcXPError).type)
        }
    }

    @Test
    fun `make call to extend user session successful response - refreshToken`(){
        val expected = authResponse
        val result = Response.success(expected)
        coEvery {
            identityService.recapToken(ArcXPAuthRequest(token = "", grantType = "refresh-token"))
        }returns result
        runBlocking {
            val actual = testObject.refreshToken(token = "", grantType = "refresh-token")
            assertEquals(expected, (actual as Success).r)
        }
    }

    @Test
    fun `make call to extend user session failed response - refreshToken`(){
        val result = Response.error<ArcXPAuth>(401, getJson("identity_error_response.json").toResponseBody())
        coEvery {
            identityService.recapToken(ArcXPAuthRequest(token = "", grantType = "refresh-token"))
        } returns result
        runBlocking {
            val actual = testObject.refreshToken(token = "", grantType = "refresh-token")
            assertEquals(ArcXPCommerceSDKErrorType.SERVER_ERROR, (((actual as Failure).l) as ArcXPError).type)
            assertEquals("Authentication failed", (actual.l as ArcXPError).message)
            assertEquals("300041", (actual.l as ArcXPError).code)
        }
    }

    @Test
    fun `make call to extend user session throw exception - refreshToken`(){
        val result = Response.error<ArcXPAuth>(500, getJson("identity_exception_response.json").toResponseBody())
        coEvery {
            identityService.recapToken(any())
        } returns result
        runBlocking {
            val actual = testObject.refreshToken("", "")
            assertEquals(ArcXPCommerceSDKErrorType.SERVER_ERROR, (((actual as Failure).l) as ArcXPError).type)
        }
    }

    @Test
    fun `make call to request deletion of user successful response - deleteUser`(){
        val expected = ArcXPAnonymizeUser(true)
        val result = Response.success(expected)
        coEvery {
            identityService.deleteUser()
        }returns result
        runBlocking {
            val actual = testObject.deleteUser()
            assertEquals(expected, (actual as Success).r)
        }
    }

    @Test
    fun `make call to request deletion of user failed response - deleteUser`(){
        val result = Response.error<ArcXPAnonymizeUser>(401, getJson("identity_error_response.json").toResponseBody())
        coEvery {
            identityService.deleteUser()
        } returns result
        runBlocking {
            val actual = testObject.deleteUser()
            assertEquals(ArcXPCommerceSDKErrorType.SERVER_ERROR, (((actual as Failure).l) as ArcXPError).type)
            assertEquals("Authentication failed", (actual.l as ArcXPError).message)
            assertEquals("300041", (actual.l as ArcXPError).code)
        }
    }

    @Test
    fun `make call to request deletion of user throw exception - deleteUser`(){
        val result = Response.error<ArcXPAnonymizeUser>(500, getJson("identity_exception_response.json").toResponseBody())
        coEvery {
            identityService.deleteUser()
        } returns result
        runBlocking {
            val actual = testObject.deleteUser()
            assertEquals(ArcXPCommerceSDKErrorType.SERVER_ERROR, (((actual as Failure).l) as ArcXPError).type)
        }
    }

    @Test
    fun `make call to get apple auth url successful response - appleAuthUrl`(){
        val expected = Unit as? ResponseBody
        val result = Response.success(expected)
        coEvery {
            identityService.appleAuthUrl()
        }returns result
        runBlocking {
            val actual = testObject.appleAuthUrl()
            assertEquals(expected, (actual as Success).r)
        }
    }

    @Test
    fun `make call to get apple auth url failed response - appleAuthUrl`(){
        val result = Response.error<ResponseBody>(401, getJson("identity_error_response.json").toResponseBody())
        coEvery {
            identityService.appleAuthUrl()
        } returns result
        runBlocking {
            val actual = testObject.appleAuthUrl()
            assertEquals(ArcXPCommerceSDKErrorType.SERVER_ERROR, (((actual as Failure).l) as ArcXPError).type)
            assertEquals("Authentication failed", (actual.l as ArcXPError).message)
            assertEquals("300041", (actual.l as ArcXPError).code)
        }
    }

//    @Test
//    fun `make call to get apple auth url throw exception - appleAuthUrl`(){
//        val result = Response.error<ResponseBody>(500, getJson("identity_exception_response.json").toResponseBody())
//        coEvery {
//            identityService.appleAuthUrl()
//        } returns result
//        runBlocking {
//            val actual = testObject.appleAuthUrl()
//            assertEquals(ArcxpCommerceSDKErrorType.SERVER_ERROR, (((actual as Failure).l) as ArcxpError).type)
//        }
//    }

    @Test
    fun `make call to get config settings successful response - getConfig`(){
        val expected = configResponse
        val result = Response.success(expected)
        coEvery {
            identityService.config()
        }returns result
        runBlocking {
            val actual = testObject.getConfig()
            assertEquals(expected, (actual as Success).r)
        }
    }

    @Test
    fun `make call to get config settings failed response - getConfig`(){
        val result = Response.error<ArcXPConfig>(401, getJson("identity_error_response.json").toResponseBody())
        coEvery {
            identityService.config()
        } returns result
        runBlocking {
            val actual = testObject.getConfig()
            assertEquals(ArcXPCommerceSDKErrorType.CONFIG_ERROR, (((actual as Failure).l) as ArcXPError).type)
            assertEquals("Authentication failed", (actual.l as ArcXPError).message)
            assertEquals("300041", (actual.l as ArcXPError).code)
        }
    }

    @Test
    fun `make call to remove identity successful response - removeIdentity`(){
        val expected = updateUserStatusResponse
        val result = Response.success(expected)
        coEvery {
            identityService.deleteIdentities("")
        }returns result
        runBlocking {
            val actual = testObject.removeIdentity("")
            assertEquals(expected, (actual as Success).r)
        }
    }

    @Test
    fun `make call to remove identity failed response - removeIdentity`(){
            val result = Response.error<ArcXPUpdateUserStatus>(401, getJson("identity_error_response.json").toResponseBody())
            coEvery {
                identityService.deleteIdentities("")
            } returns result
            runBlocking {
                val actual = testObject.removeIdentity("")
                assertEquals(ArcXPCommerceSDKErrorType.SERVER_ERROR, (((actual as Failure).l) as ArcXPError).type)
                assertEquals("Authentication failed", (actual.l as ArcXPError).message)
                assertEquals("300041", (actual.l as ArcXPError).code)
            }
    }

    protected fun getJson(fileName: String): String {
        val file = File(javaClass.classLoader?.getResource(fileName)?.path ?: throw NullPointerException("No path find!"))
        return String(file.readBytes())
    }
}