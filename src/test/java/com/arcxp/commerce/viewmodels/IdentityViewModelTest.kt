package com.arcxp.commerce.viewmodels

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.arcxp.commerce.ArcXPCommerceSDKErrorType
import com.arcxp.commerce.apimanagers.ArcXPIdentityListener
import com.arcxp.commerce.extendedModels.ArcXPProfileManage
import com.arcxp.commerce.models.*
import com.arcxp.commerce.repositories.IdentityRepository
import com.arcxp.commerce.testUtils.TestUtils
import com.arcxp.commerce.util.*
import com.arcxp.commerce.util.ArcXPError
import com.arcxp.commerce.util.DependencyProvider.createError
import com.arcxp.commerce.util.DependencyProvider.ioDispatcher
import com.google.android.gms.safetynet.SafetyNet
import com.google.android.gms.safetynet.SafetyNetApi
import com.google.android.gms.safetynet.SafetyNetClient
import com.google.android.gms.tasks.OnCanceledListener
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import io.mockk.*
import io.mockk.impl.annotations.RelaxedMockK
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class IdentityViewModelTest {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()
    @get:Rule
    val mainDispatcherRule = TestUtils.MainDispatcherRule()

    private lateinit var testObject: IdentityViewModel

    private var profileRequest = ArcXPProfileRequest(
        "", "", null, null, null, "", null, null,
        null, null, null, null, null, null, null
    )


    private var emptyProfileRequest = ArcXPProfileRequest(email = "")

    private var identityRequest = ArcXPIdentityRequest("", "", "password")

    private val authResponse = ArcXPAuth("1234", "asdf123", "321fdsa", "", "", "")

    @RelaxedMockK
    private lateinit var patchRequest: ArcXPProfilePatchRequest

    @RelaxedMockK
    lateinit var context: Context

    @RelaxedMockK
    lateinit var identityResponse: ArcXPIdentity

    @RelaxedMockK
    private lateinit var configResponse: ArcXPConfig

    @RelaxedMockK
    private lateinit var updateUserStatusResponse: ArcXPUpdateUserStatus

    @RelaxedMockK
    private lateinit var authManager: AuthManager

    @RelaxedMockK
    private lateinit var identityRepository: IdentityRepository

    @RelaxedMockK
    private lateinit var listener: ArcXPIdentityListener


    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        mockkObject(DependencyProvider)
        every { ioDispatcher() } returns Dispatchers.Unconfined
        testObject = IdentityViewModel(authManager, identityRepository)
    }

    @Test
    fun `change user password with successful response - changeUserPassword`() = runTest {
        val response = Success(
            identityResponse
        )
        runBlocking {
            coEvery {
                identityRepository.changePassword(eq(ArcXPPasswordResetRequest("a", "b")))
            } returns response

            testObject.changeUserPassword("a", "b", listener)

            coVerify {
                identityRepository.changePassword(eq(ArcXPPasswordResetRequest("a", "b")))
                listener.onPasswordChangeSuccess(response.r)
            }
        }
    }

    @Test
    fun `change user password with failed response with callback - changeUserPassword `() = runTest {
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
    fun `change user password with failed response no callback - changeUserPassword `() = runTest {
        val response = ArcXPError("error")
        runBlocking {
            coEvery {
                identityRepository.changePassword(eq(ArcXPPasswordResetRequest("a", "b")))
            } returns Failure(response)

            testObject.changeUserPassword("a", "b", null)

            coVerify {
                identityRepository.changePassword(eq(ArcXPPasswordResetRequest("a", "b")))
            }
        }
        assertEquals(response, testObject.changePasswordError.value)
    }

    @Test
    fun `change user password with successful response to observer - changeUserPassword`() = runTest {
        val response = identityResponse

        runBlocking {
            coEvery {
                identityRepository.changePassword(eq(ArcXPPasswordResetRequest("a", "b")))
            } returns Success(response)

            testObject.changeUserPassword("a", "b", null)

            coVerify {
                identityRepository.changePassword(eq(ArcXPPasswordResetRequest("a", "b")))
            }

            assertEquals(response, testObject.changePasswordResponse.value)
        }
    }

    @Test
    fun `reset user password by email with successful response - obtainNonceByEmailAddress`() = runTest {
        val response = Success(ArcXPRequestPasswordReset(true))

        runBlocking {
            coEvery {
                identityRepository.resetPassword(eq(ArcXPResetPasswordRequestRequest("tester@arctest.com")))
            } returns response

            testObject.obtainNonceByEmailAddress("tester@arctest.com", listener)

            coVerify {
                identityRepository.resetPassword(eq(ArcXPResetPasswordRequestRequest("tester@arctest.com")))
                listener.onPasswordResetNonceSuccess(response.r)
            }
        }
    }

    @Test
    fun `reset user password by email with failed response - obtainNonceByEmailAddress`() = runTest {
        val response = Failure(ArcXPError("Failed"))

        runBlocking {
            coEvery {
                identityRepository.resetPassword(eq(ArcXPResetPasswordRequestRequest("tester@arctest.com")))
            } returns response

            testObject.obtainNonceByEmailAddress("tester@arctest.com", listener)

            coVerify {
                identityRepository.resetPassword(eq(ArcXPResetPasswordRequestRequest("tester@arctest.com")))
                listener.onPasswordResetNonceFailure(response.l)
            }
        }
    }

    @Test
    fun `reset user password by email with successful response without callback - obtainNonceByEmailAddress`() = runTest {
        val response = ArcXPRequestPasswordReset(true)

        runBlocking {
            coEvery {
                identityRepository.resetPassword(eq(ArcXPResetPasswordRequestRequest("tester@arctest.com")))
            } returns Success(response)

            testObject.obtainNonceByEmailAddress("tester@arctest.com", null)

            coVerify {
                identityRepository.resetPassword(eq(ArcXPResetPasswordRequestRequest("tester@arctest.com")))
            }
            assertEquals(response, testObject.requestPasswordResetResponse.value)
        }
    }

    @Test
    fun `reset user password by email with failed response without callback - obtainNonceByEmailAddress`() = runTest {
        val response = ArcXPError("Failed")

        runBlocking {
            coEvery {
                identityRepository.resetPassword(eq(ArcXPResetPasswordRequestRequest("tester@arctest.com")))
            } returns Failure(response)

            testObject.obtainNonceByEmailAddress("tester@arctest.com", null)

            coVerify {
                identityRepository.resetPassword(eq(ArcXPResetPasswordRequestRequest("tester@arctest.com")))
            }
            assertEquals(response, testObject.passwordResetErrorResponse.value)
        }
    }

    @Test
    fun `reset user password by nonce with success response - resetPasswordByNonce`() = runTest {
        val response = Success(identityResponse)

        runBlocking {
            coEvery {
                identityRepository.resetPassword("asdf", ArcXPResetPasswordNonceRequest("asdf"))
            } returns response

            testObject.resetPasswordByNonce("asdf", "asdf", listener)

            coVerify {
                identityRepository.resetPassword("asdf", ArcXPResetPasswordNonceRequest("asdf"))
                listener.onPasswordResetSuccess(response.r)
            }
        }
    }

    @Test
    fun `reset user password by nonce with failed response - resetPasswordByNonce`() = runTest {
        val response = Failure(ArcXPError("Error"))
        runBlocking {
            coEvery {
                identityRepository.resetPassword("asdf", ArcXPResetPasswordNonceRequest("asdf"))
            } returns response

            testObject.resetPasswordByNonce("asdf", "asdf", listener)

            coVerify {
                identityRepository.resetPassword("asdf", ArcXPResetPasswordNonceRequest("asdf"))
                listener.onPasswordResetError(response.l)
            }
        }
    }

    @Test
    fun `reset user password by nonce with success response without callback - resetPasswordByNonce`() = runTest {
        val response = Success(identityResponse)
        runBlocking {
            coEvery {
                identityRepository.resetPassword("asdf", ArcXPResetPasswordNonceRequest("asdf"))
            } returns response

            testObject.resetPasswordByNonce("asdf", "asdf", null)

            coVerify {
                identityRepository.resetPassword("asdf", ArcXPResetPasswordNonceRequest("asdf"))
            }
        }
    }

    @Test
    fun `reset user password by nonce with failed response without callback - resetPasswordByNonce`() = runTest {
        val response = Failure(ArcXPError("Error"))
        runBlocking {
            coEvery {
                identityRepository.resetPassword("asdf", ArcXPResetPasswordNonceRequest("asdf"))
            } returns response

            testObject.resetPasswordByNonce("asdf", "asdf", null)

            coVerify {
                identityRepository.resetPassword("asdf", ArcXPResetPasswordNonceRequest("asdf"))
            }
        }
    }

    @Test
    fun `login using username & password successful response - makeLoginCall`() = runTest {
        val response = Success(authResponse)
        runBlocking {
            coEvery {
                identityRepository.login(
                    ArcXPAuthRequest(
                        "tester",
                        "asdf",
                        null,
                        "password",
                        "123"
                    )
                )
            } returns response

            testObject.makeLoginCall("tester", "asdf", "123", listener)

            coVerify {
                identityRepository.login(
                    ArcXPAuthRequest(
                        "tester",
                        "asdf",
                        null,
                        "password",
                        "123"
                    )
                )
                listener.onLoginSuccess(response.r)
            }
        }
    }

    @Test
    fun `makeLoginCall- login using username & password successful response without callback`() = runTest {
        val response = authResponse
        runBlocking {
            coEvery {
                identityRepository.login(ArcXPAuthRequest("tester", "asdf", null, "password"))
            } returns Success(response)

            testObject.makeLoginCall(userName = "tester", password = "asdf", callback = null)

            coVerify {
                identityRepository.login(ArcXPAuthRequest("tester", "asdf", null, "password"))
            }
            assertEquals(response, testObject.authResponse.value)
        }
    }

    @Test
    fun `login using username & password failed response- makeLoginCall`() = runTest {
        val response = Failure(ArcXPError("Error"))
        runBlocking {
            coEvery {
                identityRepository.login(ArcXPAuthRequest("tester", "asdf", null, "password", null))
            } returns response

            testObject.makeLoginCall("tester", "asdf", null, listener)

            coVerify {
                identityRepository.login(ArcXPAuthRequest("tester", "asdf", null, "password", null))
                listener.onLoginError(response.l)
            }
        }
    }

    @Test
    fun `makeLoginCall - login using username & password failed response without callback`() = runTest {
        val response = ArcXPError("Error")
        runBlocking {
            coEvery {
                identityRepository.login(ArcXPAuthRequest("tester", "asdf", null, "password", null))
            } returns Failure(response)

            testObject.makeLoginCall("tester", "asdf", null, null)

            coVerify {
                identityRepository.login(ArcXPAuthRequest("tester", "asdf", null, "password", null))
            }
            assertEquals(response, testObject.loginErrorResponse.value)
        }
    }


//    @Test
//    fun `appleAuthUrl - obtain apple auth url without callback`(){
//        val response = Success<ResponseBody?>(responseBody)
//
//        coEvery {
//            identityRepository.appleAuthUrl()
//        } returns response
//
//        testObject.appleAuthUrl(null)
//
//        coVerify {
//            identityRepository.appleAuthUrl()
//        }
//    }
//
//    @Test
//    fun `appleAuthUrlUpdatedURL - obtain apple auth url without callback`(){
//        val response = Success<ResponseBody?>(responseBody)
//
//        coEvery {
//            identityRepository.appleAuthUrlUpdatedURL()
//        } returns response
//
//        testObject.appleAuthUrlUpdatedURL(null)
//
//        coVerify {
//            identityRepository.appleAuthUrlUpdatedURL()
//        }
//    }

    @Test
    fun `login using third party login successfully - thirdPartyLoginCall`() = runTest {
        val response = Success(authResponse)
        runBlocking {
            coEvery {
                identityRepository.appleLogin(
                    ArcXPAuthRequest(
                        "",
                        "asdf123",
                        null,
                        grantType = "apple",
                        null
                    )
                )
            } returns response

            testObject.thirdPartyLoginCall(
                "asdf123",
                ArcXPAuthRequest.Companion.GrantType.APPLE,
                callback = listener
            )

            coVerify {
                identityRepository.appleLogin(
                    ArcXPAuthRequest(
                        "",
                        "asdf123",
                        null,
                        grantType = "apple",
                        null
                    )
                )
                listener.onLoginSuccess(response.r)
            }
        }
    }

    @Test
    fun `login using third party login failure - thirdPartyLoginCall`() = runTest {
        val response = Failure(ArcXPError("Error"))
        runBlocking {
            coEvery {
                identityRepository.login(
                    ArcXPAuthRequest(
                        "",
                        "asdf123",
                        null,
                        grantType = "facebook"
                    )
                )
            } returns response

            testObject.thirdPartyLoginCall(
                "asdf123",
                ArcXPAuthRequest.Companion.GrantType.FACEBOOK,
                callback = listener
            )

            coVerify {
                identityRepository.login(
                    ArcXPAuthRequest(
                        "",
                        "asdf123",
                        null,
                        grantType = "facebook"
                    )
                )
                listener.onLoginError(response.l)
            }
        }
    }

    @Test
    fun `login using third party login successfully without callback (apple) - thirdPartyLoginCall`() = runTest {
        val response = Success(authResponse)
        runBlocking {
            coEvery {
                identityRepository.appleLogin(
                    ArcXPAuthRequest(
                        "",
                        "asdf123",
                        null,
                        grantType = "apple",
                        null
                    )
                )
            } returns response

            testObject.thirdPartyLoginCall(
                "asdf123",
                ArcXPAuthRequest.Companion.GrantType.APPLE,
                null
            )

            coVerify {
                identityRepository.appleLogin(
                    ArcXPAuthRequest(
                        "",
                        "asdf123",
                        null,
                        grantType = "apple",
                        null
                    )
                )
            }
        }
    }

    @Test
    fun `login using third party login failure without callback - thirdPartyLoginCall`() = runTest {
        val response = ArcXPError("Error")
        runBlocking {
            coEvery {
                identityRepository.login(
                    ArcXPAuthRequest(
                        "",
                        "asdf123",
                        null,
                        grantType = "facebook"
                    )
                )
            } returns Failure(response)

            testObject.thirdPartyLoginCall(
                "asdf123",
                ArcXPAuthRequest.Companion.GrantType.FACEBOOK,
                null
            )

            coVerify {
                identityRepository.login(
                    ArcXPAuthRequest(
                        "",
                        "asdf123",
                        null,
                        grantType = "facebook"
                    )
                )
            }
            assertEquals(response, testObject.errorResponse.value)
        }
    }

    @Test
    fun `login using third party login (apple) failure with callback - thirdPartyLoginCall`() = runTest {
        val response = Failure(ArcXPError("Error"))
        runBlocking {
            coEvery {
                identityRepository.appleLogin(
                    ArcXPAuthRequest(
                        "",
                        "asdf123",
                        null,
                        grantType = "apple"
                    )
                )
            } returns response

            testObject.thirdPartyLoginCall(
                "asdf123",
                ArcXPAuthRequest.Companion.GrantType.APPLE,
                listener
            )

            coVerify {
                identityRepository.appleLogin(
                    ArcXPAuthRequest(
                        "",
                        "asdf123",
                        null,
                        grantType = "apple"
                    )
                )
                listener.onLoginError(response.l)
            }
        }
    }

    @Test
    fun `login using third party login (apple) failure without callback - thirdPartyLoginCall`() = runTest {
        val response = Failure(ArcXPError("Error"))
        runBlocking {
            coEvery {
                identityRepository.appleLogin(
                    ArcXPAuthRequest(
                        "",
                        "asdf123",
                        null,
                        grantType = "apple"
                    )
                )
            } returns response

            testObject.thirdPartyLoginCall(
                "asdf123",
                ArcXPAuthRequest.Companion.GrantType.APPLE,
                null
            )

            coVerify {
                identityRepository.appleLogin(
                    ArcXPAuthRequest(
                        "",
                        "asdf123",
                        null,
                        grantType = "apple"
                    )
                )
            }
        }
    }

    @Test
    fun `login using third party login successfully with callback (attach accounts)- thirdPartyLoginCall`() = runTest {
        val response = Success(authResponse)

        mockkObject(AuthManager)
        every { AuthManager.getInstance().uuid } returns "1234"

        runBlocking {

            coEvery {
                identityRepository.login(
                    ArcXPAuthRequest(
                        "",
                        "asdf123",
                        null,
                        grantType = "facebook",
                        null
                    )
                )
            } returns response


            testObject.thirdPartyLoginCall(
                "asdf123",
                ArcXPAuthRequest.Companion.GrantType.FACEBOOK,
                listener
            )

            coVerify {
                identityRepository.login(
                    ArcXPAuthRequest(
                        "",
                        "asdf123",
                        null,
                        grantType = "facebook",
                        null
                    )
                )
                listener.onLoginSuccess(response.r)
            }
        }
    }

    @Test
    fun `login using third party login successfully with callback (unable to attach accounts)- thirdPartyLoginCall`() = runTest {
        val response = authResponse

        mockkObject(AuthManager)
        every { AuthManager.getInstance().uuid } returns "12"

        runBlocking {

            coEvery {
                identityRepository.login(
                    ArcXPAuthRequest(
                        "",
                        "asdf123",
                        null,
                        grantType = "facebook",
                        null
                    )
                )
            } returns Success(response)


            testObject.thirdPartyLoginCall(
                "asdf123",
                ArcXPAuthRequest.Companion.GrantType.FACEBOOK,
                listener
            )

            coVerify {
                identityRepository.login(
                    ArcXPAuthRequest(
                        "",
                        "asdf123",
                        null,
                        grantType = "facebook",
                        null
                    )
                )
            }
            listener.onLoginError(ArcXPError("Account already linked to another account"))
        }
    }

    @Test
    fun `login using third party login successfully without callback (unable to attach accounts)- thirdPartyLoginCall`() = runTest {
        val response = authResponse

        mockkObject(AuthManager)
        every { AuthManager.getInstance().uuid } returns "12"

        runBlocking {

            coEvery {
                identityRepository.login(
                    ArcXPAuthRequest(
                        "",
                        "asdf123",
                        null,
                        grantType = "facebook",
                        null
                    )
                )
            } returns Success(response)


            testObject.thirdPartyLoginCall(
                "asdf123",
                ArcXPAuthRequest.Companion.GrantType.FACEBOOK,
                null
            )

            coVerify {
                identityRepository.login(
                    ArcXPAuthRequest(
                        "",
                        "asdf123",
                        null,
                        grantType = "facebook",
                        null
                    )
                )
            }
        }
    }

    @Test
    fun `login using third party login successfully without callback - thirdPartyLoginCall`() = runTest {
        val response = Success(authResponse)

        mockkObject(AuthManager)
        every { AuthManager.getInstance().uuid } returns null
        runBlocking {
            coEvery {
                identityRepository.login(
                    ArcXPAuthRequest(
                        "",
                        "asdf123",
                        null,
                        grantType = "facebook",
                        null
                    )
                )
            } returns response

            testObject.thirdPartyLoginCall(
                "asdf123",
                ArcXPAuthRequest.Companion.GrantType.FACEBOOK,
                callback = null
            )

            coVerify {
                identityRepository.login(
                    ArcXPAuthRequest(
                        "",
                        "asdf123",
                        null,
                        grantType = "facebook",
                        null
                    )
                )
            }
        }
    }


    @Test
    fun `verify registered email successful response - verifyEmailCall`() = runTest {
        val response = Success(ArcXPEmailVerification(true))
        runBlocking {
            coEvery {
                identityRepository.verifyEmail(ArcXPVerifyEmailRequest("test@arctest.com"))
            } returns response

            testObject.verifyEmailCall("test@arctest.com", listener)

            coVerify {
                identityRepository.verifyEmail(ArcXPVerifyEmailRequest("test@arctest.com"))
                listener.onEmailVerificationSentSuccess(response.r)
            }
        }
    }

    @Test
    fun `verify registered email failure response - verifyEmailCall`() = runTest {
        val response = Failure(ArcXPError("Error"))
        runBlocking {
            coEvery {
                identityRepository.verifyEmail(ArcXPVerifyEmailRequest("test@arctest.com"))
            } returns response

            testObject.verifyEmailCall("test@arctest.com", listener)

            coVerify {
                identityRepository.verifyEmail(ArcXPVerifyEmailRequest("test@arctest.com"))
                listener.onEmailVerificationSentError(response.l)
            }
        }
    }

    @Test
    fun `verify registered email successful response without callback - verifyEmailCall`() = runTest {
        val response = ArcXPEmailVerification(true)
        runBlocking {
            coEvery {
                identityRepository.verifyEmail(ArcXPVerifyEmailRequest("test@arctest.com"))
            } returns Success(response)

            testObject.verifyEmailCall("test@arctest.com", null)

            coVerify {
                identityRepository.verifyEmail(ArcXPVerifyEmailRequest("test@arctest.com"))
            }
            assertEquals(response, testObject.emailVerificationResponse.value)
        }
    }

    @Test
    fun `verify registered email failure response without callback - verifyEmailCall`() = runTest {
        val response = ArcXPError("Error")
        runBlocking {
            coEvery {
                identityRepository.verifyEmail(ArcXPVerifyEmailRequest("test@arctest.com"))
            } returns Failure(response)

            testObject.verifyEmailCall("test@arctest.com", null)

            coVerify {
                identityRepository.verifyEmail(ArcXPVerifyEmailRequest("test@arctest.com"))
            }
            assertEquals(response, testObject.emailVerificationErrorResponse.value)
        }
    }

    @Test
    fun `verify email with nonce successful response - verifyEmail`() = runTest {
        val response = Success(ArcXPEmailVerification(true))
        runBlocking {
            coEvery {
                identityRepository.verifyEmailNonce("asdf")
            } returns response

            testObject.verifyEmail("asdf", listener)

            coVerify {
                identityRepository.verifyEmailNonce("asdf")
                listener.onEmailVerifiedSuccess(response.r)
            }
        }
    }

    @Test
    fun `verify email with nonce failure response - verifyEmail`() = runTest {
        val response = Failure(ArcXPError("Error"))
        runBlocking {
            coEvery {
                identityRepository.verifyEmailNonce("asdf")
            } returns response

            testObject.verifyEmail("asdf", listener)

            coVerify {
                identityRepository.verifyEmailNonce("asdf")
                listener.onEmailVerifiedError(response.l)
            }
        }
    }

    @Test
    fun `verify email with nonce successful response without callback - verifyEmail`() = runTest {
        val response = Success(ArcXPEmailVerification(true))
        runBlocking {
            coEvery {
                identityRepository.verifyEmailNonce("asdf")
            } returns response

            testObject.verifyEmail("asdf", null)

            coVerify {
                identityRepository.verifyEmailNonce("asdf")
            }
        }
    }

    @Test
    fun `verify email with nonce failure response without callback - verifyEmail`() = runTest {
        val response = Failure(ArcXPError("Error"))
        runBlocking {
            coEvery {
                identityRepository.verifyEmailNonce("asdf")
            } returns response

            testObject.verifyEmail("asdf", null)

            coVerify {
                identityRepository.verifyEmailNonce("asdf")
            }
        }
    }


    @Test
    fun `make call to get magicLink successful response - getMagicLink`() = runTest {
        val response = Success(ArcXPOneTimeAccessLink(true))
        runBlocking {
            coEvery {
                identityRepository.getMagicLink(ArcXPOneTimeAccessLinkRequest("test@arctest.com"))
            } returns response

            testObject.getMagicLink(
                email = "test@arctest.com",
                recaptchaToken = null,
                callback = listener
            )

            coVerify {
                identityRepository.getMagicLink(ArcXPOneTimeAccessLinkRequest("test@arctest.com"))
                listener.onOneTimeAccessLinkSuccess(response.r)
            }
        }
    }

    @Test
    fun `make call to get magicLink failure response - getMagicLink`() = runTest {
        val response = Failure(ArcXPError("Error"))
        runBlocking {
            coEvery {
                identityRepository.getMagicLink(ArcXPOneTimeAccessLinkRequest("test@arctest.com"))
            } returns response

            testObject.getMagicLink("test@arctest.com", null, listener)

            coVerify {
                identityRepository.getMagicLink(ArcXPOneTimeAccessLinkRequest("test@arctest.com"))
                listener.onOneTimeAccessLinkError(response.l)
            }
        }
    }

    @Test
    fun `make call to get magicLink successful response without callback - getMagicLink`() = runTest {
        val response = Success(ArcXPOneTimeAccessLink(true))
        runBlocking {
            coEvery {
                identityRepository.getMagicLink(ArcXPOneTimeAccessLinkRequest("test@arctest.com"))
            } returns response

            testObject.getMagicLink("test@arctest.com", null)

            coVerify {
                identityRepository.getMagicLink(ArcXPOneTimeAccessLinkRequest("test@arctest.com"))
            }
        }
    }

    @Test
    fun `make call to get magicLink failure response without callback - getMagicLink`() = runTest {
        val response = Failure(ArcXPError("Error"))
        runBlocking {
            coEvery {
                identityRepository.getMagicLink(ArcXPOneTimeAccessLinkRequest("test@arctest.com"))
            } returns response

            testObject.getMagicLink("test@arctest.com", null, null)

            coVerify {
                identityRepository.getMagicLink(ArcXPOneTimeAccessLinkRequest("test@arctest.com"))
            }
        }
    }

    @Test
    fun `login with magic link successful response - loginMagicLink`() = runTest {
        val response = Success(ArcXPOneTimeAccessLinkAuth("asdf", "1234"))
        runBlocking {
            coEvery {
                identityRepository.loginMagicLink("asdf")
            } returns response

            testObject.loginMagicLink("asdf", listener)

            coVerify {
                identityRepository.loginMagicLink("asdf")
                listener.onOneTimeAccessLinkLoginSuccess(response.r)
            }
        }
    }

    @Test
    fun `login with magic link failure response - loginMagicLink`() = runTest {
        val response = Failure(ArcXPError("Error"))
        runBlocking {
            coEvery {
                identityRepository.loginMagicLink("asdf")
            } returns response

            testObject.loginMagicLink("asdf", listener)

            coVerify {
                identityRepository.loginMagicLink("asdf")
                listener.onOneTimeAccessLinkError(response.l)
            }
        }
    }

    @Test
    fun `login with magic link successful response without callback - loginMagicLink`() = runTest {
        val response = ArcXPOneTimeAccessLinkAuth("asdf", "1234")
        runBlocking {
            coEvery {
                identityRepository.loginMagicLink("asdf")
            } returns Success(response)

            testObject.loginMagicLink("asdf", null)

            coVerify {
                identityRepository.loginMagicLink("asdf")
            }
            assertEquals(response, testObject.oneTimeAccessLinkAuthResponse.value)
        }
    }

    @Test
    fun `login with magic link failure response without callback - loginMagicLink`() = runTest {
        val response = ArcXPError("Error")
        runBlocking {
            coEvery {
                identityRepository.loginMagicLink("asdf")
            } returns Failure(response)

            testObject.loginMagicLink("asdf", null)

            coVerify {
                identityRepository.loginMagicLink("asdf")
            }
            assertEquals(response, testObject.magicLinkErrorResponse.value)
        }
    }

    @Test
    fun `logout - Successful response with callback`() = runTest {
        val response = mockk<Void>()
        runBlocking {
            coEvery {
                identityRepository.logout()
            } returns Success(response)

            testObject.logout(listener)

            coVerify {
                identityRepository.logout()
                listener.onLogoutSuccess()
            }
        }
    }

    @Test
    fun `logout - Successful response without callback`() = runTest {
        val response = mockk<Void>()
        runBlocking {
            coEvery {
                identityRepository.logout()
            } returns Success(response)

            testObject.logout(null)

            coVerify {
                identityRepository.logout()
            }
        }
        assertEquals(true, testObject.logoutResponse.value)
    }

    @Test
    fun `logout - Failed response with callback`() = runTest {
        val response = Failure(ArcXPError("Failed"))
        runBlocking {
            coEvery {
                identityRepository.logout()
            } returns response

            testObject.logout(listener)

            coVerify {
                identityRepository.logout()
                listener.onLogoutError(response.l)
            }
        }
    }

    @Test
    fun `logout - Failed response without callback`() = runTest {
        val response = ArcXPError("Failed")
        runBlocking {
            coEvery {
                identityRepository.logout()
            } returns Failure(response)

            testObject.logout(null)

            coVerify {
                identityRepository.logout()
            }
        }
        assertEquals(response, testObject.logoutErrorResponse.value)
    }

    @Test
    fun `make a call to patch profile successful response - patchProfile`() = runTest {

        val response = Success(mockk<ArcXPProfileManage>())
        runBlocking {
            coEvery {
                identityRepository.patchProfile(patchRequest)
            } returns response

            testObject.patchProfile(patchRequest, listener)

            coVerify {
                identityRepository.patchProfile(patchRequest)
                listener.onProfileUpdateSuccess(response.r)
            }
        }
    }

    @Test
    fun `make a call to patch profile failure response - patchProfile`() = runTest {
        val response = Failure(ArcXPError("Error"))
        runBlocking {
            coEvery {
                identityRepository.patchProfile(patchRequest)
            } returns response

            testObject.patchProfile(patchRequest, listener)

            coVerify {
                identityRepository.patchProfile(patchRequest)
                listener.onProfileError(response.l)
            }
        }
    }

    @Test
    fun `make a call to patch profile successful response without callback - patchProfile`() = runTest {
        val response = mockk<ArcXPProfileManage>()
        runBlocking {
            coEvery {
                identityRepository.patchProfile(patchRequest)
            } returns Success(response)

            testObject.patchProfile(patchRequest, null)

            coVerify {
                identityRepository.patchProfile(patchRequest)
            }
            assertEquals(response, testObject.profileResponse.value)
        }
    }

    @Test
    fun `make a call to patch profile failure response without callback - patchProfile`() = runTest {
        val response = ArcXPError("Error")
        runBlocking {
            coEvery {
                identityRepository.patchProfile(patchRequest)
            } returns Failure(response)

            testObject.patchProfile(patchRequest, null)

            coVerify {
                identityRepository.patchProfile(patchRequest)
            }
            assertEquals(response, testObject.profileErrorResponse.value)
        }
    }

    @Test
    fun `make call to fetch profile with successful response - getProfile`() = runTest {
        val response = Success(mockk<ArcXPProfileManage>())
        runBlocking {
            coEvery {
                identityRepository.getProfile()
            } returns response

            testObject.getProfile(listener)

            coVerify {
                identityRepository.getProfile()
                listener.onFetchProfileSuccess(response.r)
            }
        }
    }

    @Test
    fun `make call to fetch profile with failure response - getProfile`() = runTest {
        val response = Failure(ArcXPError("Error"))
        runBlocking {
            coEvery {
                identityRepository.getProfile()
            } returns response

            testObject.getProfile(listener)

            coVerify {
                identityRepository.getProfile()
                listener.onProfileError(response.l)
            }
        }
    }

    @Test
    fun `make call to fetch profile with successful response without callback - getProfile`() = runTest {
        val response = Success(mockk<ArcXPProfileManage>())
        runBlocking {
            coEvery {
                identityRepository.getProfile()
            } returns response

            testObject.getProfile(null)

            coVerify {
                identityRepository.getProfile()
            }
        }
    }

    @Test
    fun `make call to fetch profile with failure response without callback - getProfile`() = runTest {
        val response = Failure(ArcXPError("Error"))
        runBlocking {
            coEvery {
                identityRepository.getProfile()
            } returns response

            testObject.getProfile(null)

            coVerify {
                identityRepository.getProfile()
            }
        }
    }

    @Test
    fun `register a new user successfully - makeRegistrationCall`() = runTest {
        val response = Success(mockk<ArcXPUser>())
        runBlocking {
            coEvery {
                identityRepository.signUp(ArcXPSignUpRequest(identityRequest, emptyProfileRequest))
            } returns response

            testObject.makeRegistrationCall(
                userName = "",
                password = "",
                email = "",
                callback = listener
            )

            coVerify {
                identityRepository.signUp(ArcXPSignUpRequest(identityRequest, emptyProfileRequest))
                listener.onRegistrationSuccess(response.r)
            }
        }
    }

    @Test
    fun `register a new user failure response - makeRegistrationCall`() = runTest {
        val response = Failure(ArcXPError("Error"))
        runBlocking {
            coEvery {
                identityRepository.signUp(ArcXPSignUpRequest(identityRequest, profileRequest))
            } returns response

            testObject.makeRegistrationCall("", "", "", "", "", listener)

            coVerify {
                identityRepository.signUp(ArcXPSignUpRequest(identityRequest, profileRequest))
                listener.onRegistrationError(response.l)
            }
        }
    }

    @Test
    fun `register a new user successfully without callback - makeRegistrationCall`() = runTest {
        val response = mockk<ArcXPUser>()
        runBlocking {
            coEvery {
                identityRepository.signUp(ArcXPSignUpRequest(identityRequest, profileRequest))
            } returns Success(response)

            testObject.makeRegistrationCall("", "", "", "", "", null)

            coVerify {
                identityRepository.signUp(ArcXPSignUpRequest(identityRequest, profileRequest))
            }
        }
        assertEquals(response, testObject.registrationResponse.value)
    }

    @Test
    fun `register a new user failure response without callback - makeRegistrationCall`() = runTest {
        val response = ArcXPError("Error")
        runBlocking {
            coEvery {
                identityRepository.signUp(ArcXPSignUpRequest(identityRequest, profileRequest))
            } returns Failure(response)

            testObject.makeRegistrationCall("", "", "", "", "", null)

            coVerify {
                identityRepository.signUp(ArcXPSignUpRequest(identityRequest, profileRequest))
            }
            assertEquals(response, testObject.registrationError.value)
        }
    }

    @Test
    fun `make a call to delete user successful response - deleteUser`() = runTest {
        val response = Success(ArcXPAnonymizeUser(true))
        runBlocking {
            coEvery {
                identityRepository.deleteUser()
            } returns response

            testObject.deleteUser(listener)

            coVerify {
                identityRepository.deleteUser()
                listener.onDeleteUserSuccess()
            }
        }
    }

    @Test
    fun `make a call to delete user failure response - deleteUser`() = runTest {
        val response = Failure(ArcXPError("Error"))
        runBlocking {
            coEvery {
                identityRepository.deleteUser()
            } returns response

            testObject.deleteUser(listener)

            coVerify {
                identityRepository.deleteUser()
                listener.onDeleteUserError(response.l)
            }
        }
    }

    @Test
    fun `make a call to delete user successful response without callback - deleteUser`() = runTest {
        val response = ArcXPAnonymizeUser(true)
        runBlocking {
            coEvery {
                identityRepository.deleteUser()
            } returns Success(response)

            testObject.deleteUser(null)

            coVerify {
                identityRepository.deleteUser()
            }
            assertEquals(true, testObject.deletionResponse.value)
        }
    }

    @Test
    fun `make a call to delete user failure response without callback - deleteUser`() = runTest {
        val response = ArcXPError("Error")
        runBlocking {
            coEvery {
                identityRepository.deleteUser()
            } returns Failure(response)

            testObject.deleteUser(null)

            coVerify {
                identityRepository.deleteUser()
            }
            assertEquals(response, testObject.deletionErrorResponse.value)
        }
    }

    @Test
    fun `make a call to delete user successful response valid is false - deleteUser`() = runTest {
        val response = Success(ArcXPAnonymizeUser(false))
        val failedResponse = Failure(ArcXPError("Your account deletion request is declined."))
        runBlocking {
            coEvery {
                identityRepository.deleteUser()
            } returns response

            testObject.deleteUser(listener)

            coVerify {
                identityRepository.deleteUser()
            }
        }
    }

    @Test
    fun `make call to approve deletion request successful response - approveDeletion`() = runTest {
        val response = Success(ArcXPDeleteUser(true))
        runBlocking {
            coEvery {
                identityRepository.approveDeletion("asdf")
            } returns response

            testObject.approveDeletion("asdf", listener)

            coVerify {
                identityRepository.approveDeletion("asdf")
                listener.onApproveDeletionSuccess(response.r)
            }
        }
    }

    @Test
    fun `make call to approve deletion request successful response without ArcXPDeletUser  - approveDeletion`() = runTest {
        val response = Success(ArcXPDeleteUser(false))
        runBlocking {
            coEvery {
                identityRepository.approveDeletion("asdf")
            } returns response

            testObject.approveDeletion("asdf", listener)

            coVerify {
                identityRepository.approveDeletion("asdf")
            }
        }
    }

    @Test
    fun `make call to approve deletion request failure response - approveDeletion`() = runTest {
        val response = Failure(ArcXPError("Error"))
        runBlocking {
            coEvery {
                identityRepository.approveDeletion("asdf")
            } returns response

            testObject.approveDeletion("asdf", listener)

            coVerify {
                identityRepository.approveDeletion("asdf")
                listener.onApproveDeletionError(response.l)
            }
        }
    }

    @Test
    fun `make call to approve deletion request failure response without callback - approveDeletion`() = runTest {
        val response = Failure(ArcXPError("Error"))
        runBlocking {
            coEvery {
                identityRepository.approveDeletion("asdf")
            } returns response

            testObject.approveDeletion("asdf", listener)

            coVerify {
                identityRepository.approveDeletion("asdf")
                listener.onApproveDeletionError(response.l)
            }
        }
    }

    @Test
    fun `make call to validate jwt with token successful response - validateJwt`() = runTest {
        val response = Success(authResponse)
        runBlocking {
            coEvery {
                identityRepository.validateJwt("asdf")
            } returns response

            testObject.validateJwt("asdf", listener)

            coVerify {
                identityRepository.validateJwt("asdf")
                listener.onValidateSessionSuccess()
            }
        }
    }

    @Test
    fun `make call to validate jwt with token failure response - validateJwt`() = runTest {
        val response = Failure(ArcXPError("Error"))
        runBlocking {
            coEvery {
                identityRepository.validateJwt("asdf")
            } returns response

            testObject.validateJwt("asdf", listener)

            coVerify {
                identityRepository.validateJwt("asdf")
                listener.onValidateSessionError(response.l)
            }
        }
    }

    @Test
    fun `make call to validate jwt with token successful response without callback - validateJwt`() = runTest {
        val response = Success(authResponse)
        runBlocking {
            coEvery {
                identityRepository.validateJwt("asdf")
            } returns response

            testObject.validateJwt("asdf", null)

            coVerify {
                identityRepository.validateJwt("asdf")
                Success(authResponse)
            }
        }
    }

    @Test
    fun `make call to validate jwt with token failure response without callback - validateJwt`() = runTest {
        val response = Failure(ArcXPError("Error"))
        runBlocking {
            coEvery {
                identityRepository.validateJwt("asdf")
            } returns response

            testObject.validateJwt("asdf", null)

            coVerify {
                identityRepository.validateJwt("asdf")
                Failure(ArcXPError("Error"))
            }
        }
    }

    @Test
    fun `make call to validate jwt successful response - validateJwt`() = runTest {
        val response = Success(authResponse)
        runBlocking {
            coEvery {
                identityRepository.validateJwt()
            } returns response

            testObject.validateJwt(listener)

            coVerify {
                identityRepository.validateJwt()
                listener.onValidateSessionSuccess()
            }
        }
    }

    @Test
    fun `make call to validate jwt failure response - validateJwt`() = runTest {
        val response = Failure(ArcXPError("Error"))
        runBlocking {
            coEvery {
                identityRepository.validateJwt()
            } returns response

            testObject.validateJwt(listener)

            coVerify {
                identityRepository.validateJwt()
                listener.onValidateSessionError(response.l)
            }
        }
    }

    @Test
    fun `make call to validate jwt successful response without callback - validateJwt`() = runTest {
        val response = Success(authResponse)
        runBlocking {
            coEvery {
                identityRepository.validateJwt()
            } returns response

            testObject.validateJwt(null)

            coVerify {
                identityRepository.validateJwt()
                Success(authResponse)
            }
        }
    }

    @Test
    fun `make call to validate jwt failure response without callback - validateJwt`() = runTest {
        val response = Failure(ArcXPError("Error"))
        runBlocking {
            coEvery {
                identityRepository.validateJwt()
            } returns response

            testObject.validateJwt(null)

            coVerify {
                identityRepository.validateJwt()
                Failure(ArcXPError("Error"))
            }
        }
    }

    @Test
    fun `make call to refresh token successful response`() = runTest {
        val response = Success(authResponse)
        runBlocking {
            coEvery {
                identityRepository.refreshToken("asdf", "refresh-token")
            } returns response

            testObject.refreshToken("asdf", "refresh-token", listener)

            coVerify {
                identityRepository.refreshToken("asdf", "refresh-token")
                listener.onRefreshSessionSuccess(response.r)
            }
        }

    }

    @Test
    fun `make call to refresh token successful response without callback`() = runTest {
        val response = Success(authResponse)
        runBlocking {
            coEvery {
                identityRepository.refreshToken("asdf", "refresh-token")
            } returns response

            testObject.refreshToken("asdf", "refresh-token", null)

            coVerify {
                identityRepository.refreshToken("asdf", "refresh-token")
                Success(authResponse)
            }
        }

    }

    @Test
    fun `make call to refresh token failure response`() = runTest {
        val response = Failure(ArcXPError("Error"))
        runBlocking {
            coEvery {
                identityRepository.refreshToken("asdf", "refresh-token")
            } returns response

            testObject.refreshToken("asdf", "refresh-token", listener)

            coVerify {
                identityRepository.refreshToken("asdf", "refresh-token")
                listener.onRefreshSessionFailure(response.l)
            }
        }
    }

    @Test
    fun `make call to refresh token failure response without callback`() = runTest {
        val response = Failure(ArcXPError("Error"))
        runBlocking {
            coEvery {
                identityRepository.refreshToken("asdf", "refresh-token")
            } returns response

            testObject.refreshToken("asdf", "refresh-token", null)

            coVerify {
                identityRepository.refreshToken("asdf", "refresh-token")
                Failure(ArcXPError("Error"))
            }
        }
    }

    @Test
    fun `make call to get tenet config successful response - getTenetConfig`() = runTest {
        val response = Success(configResponse)
        runBlocking {
            coEvery {
                identityRepository.getConfig()
            } returns response

            testObject.getTenetConfig(listener)

            coVerify {
                identityRepository.getConfig()
                listener.onLoadConfigSuccess(response.r)
            }
        }
    }

    @Test
    fun `make call to get tenet config failure response - getTenetConfig`() = runTest {
        val response = Failure(ArcXPError("Error"))
        runBlocking {
            coEvery {
                identityRepository.getConfig()
            } returns response

            testObject.getTenetConfig(listener)

            coVerify {
                identityRepository.getConfig()
                listener.onLoadConfigFailure(response.l)
            }
        }
    }

    @Test
    fun `getTenetConfig failure response with exception`() = runTest {
        val error = mockk<ArcXPError>()
        val exception = mockk<Exception>()
        val message = "message"
        coEvery { exception.message } returns message
        mockkObject(DependencyProvider)
        coEvery {
            createError(
                type = ArcXPCommerceSDKErrorType.CONFIG_ERROR,
                message = message,
                value = exception
            )
        } returns error
        val response = Failure(exception)

        coEvery {
            identityRepository.getConfig()
        } returns response

        testObject.getTenetConfig(listener)



        coVerify {
            identityRepository.getConfig()
            listener.onLoadConfigFailure(error = error)
        }
    }

    @Test
    fun `verify removal of identity successful response - removeIdentity`() = runTest {
        val response = Success(updateUserStatusResponse)
        runBlocking {
            coEvery {
                identityRepository.removeIdentity("")
            } returns response

            testObject.removeIdentity("", listener)

            coVerify {
                identityRepository.removeIdentity("")
                listener.onRemoveIdentitySuccess(response.r)
            }
        }
    }

    @Test
    fun `checkRecaptcha - with callback`() = runTest {
        val siteKey = "key"
        val client = mockk<SafetyNetClient>()
        val task = mockk<Task<SafetyNetApi.RecaptchaTokenResponse>>()
        val token = mockk<SafetyNetApi.RecaptchaTokenResponse>()
        val tokenResult = "result"
        val localizedMsg = "msg"
        mockkStatic(SafetyNet::class)
        coEvery {
            SafetyNet.getClient(context)
        } returns client

        val successListener = slot<OnSuccessListener<SafetyNetApi.RecaptchaTokenResponse>>()
        val failureListener = slot<OnFailureListener>()
        val canceledListener = slot<OnCanceledListener>()

        coEvery { client.verifyWithRecaptcha(siteKey) } returns task
        coEvery { task.addOnSuccessListener(any()) } returns task
        coEvery { task.addOnFailureListener(any()) } returns task
        coEvery { task.addOnCanceledListener(any()) } returns task

        val exception = mockk<Exception>()
        coEvery { exception.localizedMessage } returns localizedMsg

        val error = mockk<ArcXPError>()
        mockkObject(DependencyProvider)
        coEvery { createError(ArcXPCommerceSDKErrorType.RECAPTCHA_ERROR, localizedMsg, exception)} returns error


        testObject.checkRecaptcha(context = context, siteKey = siteKey, callback = listener)

        coVerify {
            task.addOnSuccessListener(capture(successListener))
            task.addOnFailureListener(capture(failureListener))
            task.addOnCanceledListener(capture(canceledListener))
        }


        coEvery { token.tokenResult } returns tokenResult
        successListener.captured.onSuccess(token)
        coVerify { listener.onRecaptchaSuccess(token = tokenResult) }

        clearAllMocks(answers = false)
        coEvery { token.tokenResult } returns null
        successListener.captured.onSuccess(token)
        coVerify { listener wasNot called }


        failureListener.captured.onFailure(exception)
        coVerify { listener.onRecaptchaFailure(error = error) }

        canceledListener.captured.onCanceled()
        coVerify { listener.onRecaptchaCancel() }


    }

    @Test
    fun `checkRecaptcha - null callback`() = runTest {
        val siteKey = "key"
        val client = mockk<SafetyNetClient>()
        val task = mockk<Task<SafetyNetApi.RecaptchaTokenResponse>>()
        val token = mockk<SafetyNetApi.RecaptchaTokenResponse>()
        val tokenResult = "result"
        val localizedMsg = "msg"
        mockkStatic(SafetyNet::class)
        coEvery {
            SafetyNet.getClient(context)
        } returns client

        val successListener = slot<OnSuccessListener<SafetyNetApi.RecaptchaTokenResponse>>()
        val failureListener = slot<OnFailureListener>()
        val canceledListener = slot<OnCanceledListener>()

        coEvery { client.verifyWithRecaptcha(siteKey) } returns task
        coEvery { task.addOnSuccessListener(any()) } returns task
        coEvery { task.addOnFailureListener(any()) } returns task
        coEvery { task.addOnCanceledListener(any()) } returns task

        val exception = mockk<Exception>()
        coEvery { exception.localizedMessage } returns localizedMsg

        val error = mockk<ArcXPError>()
        mockkObject(DependencyProvider)
        coEvery { createError(ArcXPCommerceSDKErrorType.RECAPTCHA_ERROR, localizedMsg, exception)} returns error


        testObject.checkRecaptcha(context = context, siteKey = siteKey, callback = null)

        coVerify {
            task.addOnSuccessListener(capture(successListener))
            task.addOnFailureListener(capture(failureListener))
            task.addOnCanceledListener(capture(canceledListener))
        }

        coEvery { token.tokenResult } returns tokenResult
        successListener.captured.onSuccess(token)

        clearAllMocks(answers = false)
        coEvery { token.tokenResult } returns null
        successListener.captured.onSuccess(token)


        failureListener.captured.onFailure(exception)

        canceledListener.captured.onCanceled()

    }

    @Test
    fun `verify removal of identity failure response - removeIdentity`() = runTest {
        val response = Failure(ArcXPError("Error"))
        runBlocking {
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

    @Test
    fun `verify removal of identity successful response without callback - removeIdentity`() = runTest {
        val response = updateUserStatusResponse
        runBlocking {
            coEvery {
                identityRepository.removeIdentity("")
            } returns Success(response)

            testObject.removeIdentity("", null)

            coVerify {
                identityRepository.removeIdentity("")
            }
            assertEquals(response, testObject.updateUserStatusResponse.value)
        }
    }

    @Test
    fun `verify removal of identity failure response without callback - removeIdentity`() = runTest {
        val response = ArcXPError("Error")
        runBlocking {
            coEvery {
                identityRepository.removeIdentity("")
            } returns Failure(response)

            testObject.removeIdentity("", null)

            coVerify {
                identityRepository.removeIdentity("")
            }
            assertEquals(response, testObject.updateUserStatusFailureResponse.value)
        }
    }

    @Test
    fun `rememerUser - Succesful request`(){
        testObject.rememberUser(true)

        verify {
            authManager.setShouldRememberUser(true)
        }
    }

    @Test
    fun `nonce - nonce is null`(){
        testObject.nonce
    }

    @Test
    fun `nonce - nonce is set`(){
        testObject.nonce = "nonce"
        testObject.nonce
    }

    @Test
    fun `recaptchaToken - recaptchaToken is null`(){
        testObject.recaptchaToken
    }

    @Test
    fun `recaptchaToken - recaptchaToken is set`(){
        testObject.recaptchaToken = "recaptcha"
        testObject.recaptchaToken
    }

}