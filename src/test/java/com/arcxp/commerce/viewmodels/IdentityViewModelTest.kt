package com.arcxp.commerce.viewmodels

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.arcxp.commerce.ArcXPCommerceSDKErrorType
import com.arcxp.commerce.apimanagers.ArcXPIdentityListener
import com.arcxp.commerce.extendedModels.ArcXPProfileManage
import com.arcxp.commerce.models.*
import com.arcxp.commerce.repositories.IdentityRepository
import com.arcxp.commons.testutils.TestUtils
import com.arcxp.commerce.util.*
import com.arcxp.commerce.util.ArcXPError
import com.arcxp.commons.util.DependencyFactory
import com.arcxp.commons.util.DependencyFactory.createError
import com.arcxp.commons.util.DependencyFactory.ioDispatcher
import com.arcxp.commons.util.Failure
import com.arcxp.commons.util.Success
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
        firstName = "",
        lastName = "",
        secondLastName = null,
        displayName = null,
        gender = null,
        email = "",
        picture = null,
        birthYear = null,
        birthMonth = null,
        birthDay = null,
        legacyId = null,
        deletionRule = null,
        contacts = null,
        addresses = null,
        attributes = null
    )


    private var emptyProfileRequest = ArcXPProfileRequest(email = "")

    private var identityRequest = ArcXPIdentityRequest(
        userName = "",
        credentials = "",
        grantType = "password"
    )

    private val authResponse = ArcXPAuth(
        uuid = "1234",
        accessToken = "asdf123",
        refreshToken = "321fdsa",
        dn = "",
        un = "",
        jti = ""
    )

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
        mockkObject(DependencyFactory)
        every { ioDispatcher() } returns Dispatchers.Unconfined
        testObject = IdentityViewModel(authManager, identityRepository)
    }

    @Test
    fun `change user password with successful response - changeUserPassword`() = runTest {
        val response = Success(
            identityResponse
        )
        coEvery {
            identityRepository.changePassword(eq(ArcXPPasswordResetRequest("a", "b")))
        } returns response

        testObject.changeUserPassword("a", "b", listener)

        coVerify {
            identityRepository.changePassword(eq(ArcXPPasswordResetRequest("a", "b")))
            listener.onPasswordChangeSuccess(response.success)
        }
    }

    @Test
    fun `change user password with failed response with callback - changeUserPassword `() =
        runTest {
            val response = Failure(ArcXPError("error"))
            coEvery {
                identityRepository.changePassword(eq(ArcXPPasswordResetRequest("a", "b")))
            } returns response

            testObject.changeUserPassword("a", "b", listener)

            coVerify {
                identityRepository.changePassword(eq(ArcXPPasswordResetRequest("a", "b")))
                listener.onPasswordChangeError(response.failure)
            }
        }


    @Test
    fun `change user password with failed response no callback - changeUserPassword `() = runTest {
        val response = ArcXPError("error")
        coEvery {
            identityRepository.changePassword(eq(ArcXPPasswordResetRequest("a", "b")))
        } returns Failure(response)

        testObject.changeUserPassword("a", "b", null)

        coVerify {
            identityRepository.changePassword(eq(ArcXPPasswordResetRequest("a", "b")))
        }

        assertEquals(response, testObject.changePasswordError.value)
    }

    @Test
    fun `change user password with successful response to observer - changeUserPassword`() =
        runTest {
            val response = identityResponse
            coEvery {
                identityRepository.changePassword(eq(ArcXPPasswordResetRequest("a", "b")))
            } returns Success(response)

            testObject.changeUserPassword("a", "b", null)

            coVerify {
                identityRepository.changePassword(eq(ArcXPPasswordResetRequest("a", "b")))
            }

            assertEquals(response, testObject.changePasswordResponse.value)

        }

    @Test
    fun `reset user password by email with successful response - obtainNonceByEmailAddress`() =
        runTest {
            val response = Success(ArcXPRequestPasswordReset(true))
            coEvery {
                identityRepository.resetPassword(eq(ArcXPResetPasswordRequestRequest("tester@arctest.com")))
            } returns response

            testObject.obtainNonceByEmailAddress("tester@arctest.com", listener)

            coVerify {
                identityRepository.resetPassword(eq(ArcXPResetPasswordRequestRequest("tester@arctest.com")))
                listener.onPasswordResetNonceSuccess(response.success)
            }
        }

    @Test
    fun `reset user password by email with failed response - obtainNonceByEmailAddress`() =
        runTest {
            val response = Failure(ArcXPError("Failed"))


            coEvery {
                identityRepository.resetPassword(eq(ArcXPResetPasswordRequestRequest("tester@arctest.com")))
            } returns response

            testObject.obtainNonceByEmailAddress("tester@arctest.com", listener)

            coVerify {
                identityRepository.resetPassword(eq(ArcXPResetPasswordRequestRequest("tester@arctest.com")))
                listener.onPasswordResetNonceFailure(response.failure)
            }
        }

    @Test
    fun `reset user password by email with successful response without callback - obtainNonceByEmailAddress`() =
        runTest {
            val response = ArcXPRequestPasswordReset(true)
            coEvery {
                identityRepository.resetPassword(eq(ArcXPResetPasswordRequestRequest("tester@arctest.com")))
            } returns Success(response)

            testObject.obtainNonceByEmailAddress("tester@arctest.com", null)

            coVerify {
                identityRepository.resetPassword(eq(ArcXPResetPasswordRequestRequest("tester@arctest.com")))
            }
            assertEquals(response, testObject.requestPasswordResetResponse.value)
        }

    @Test
    fun `reset user password by email with failed response without callback - obtainNonceByEmailAddress`() =
        runTest {
            val response = ArcXPError("Failed")
            coEvery {
                identityRepository.resetPassword(eq(ArcXPResetPasswordRequestRequest("tester@arctest.com")))
            } returns Failure(response)

            testObject.obtainNonceByEmailAddress("tester@arctest.com", null)

            coVerify {
                identityRepository.resetPassword(eq(ArcXPResetPasswordRequestRequest("tester@arctest.com")))
            }
            assertEquals(response, testObject.passwordResetErrorResponse.value)
        }

    @Test
    fun `reset user password by nonce with success response - resetPasswordByNonce`() = runTest {
        val response = Success(identityResponse)
        coEvery {
            identityRepository.resetPassword("asdf", ArcXPResetPasswordNonceRequest("asdf"))
        } returns response

        testObject.resetPasswordByNonce("asdf", "asdf", listener)

        coVerify {
            identityRepository.resetPassword("asdf", ArcXPResetPasswordNonceRequest("asdf"))
            listener.onPasswordResetSuccess(response.success)
        }
    }

    @Test
    fun `reset user password by nonce with failed response - resetPasswordByNonce`() = runTest {
        val response = Failure(ArcXPError("Error"))
        coEvery {
            identityRepository.resetPassword("asdf", ArcXPResetPasswordNonceRequest("asdf"))
        } returns response

        testObject.resetPasswordByNonce("asdf", "asdf", listener)

        coVerify {
            identityRepository.resetPassword("asdf", ArcXPResetPasswordNonceRequest("asdf"))
            listener.onPasswordResetError(response.failure)
        }
    }

    @Test
    fun `reset user password by nonce with success response without callback - resetPasswordByNonce`() =
        runTest {
            val response = Success(identityResponse)
            coEvery {
                identityRepository.resetPassword("asdf", ArcXPResetPasswordNonceRequest("asdf"))
            } returns response

            testObject.resetPasswordByNonce("asdf", "asdf", null)

            coVerify {
                identityRepository.resetPassword("asdf", ArcXPResetPasswordNonceRequest("asdf"))
            }
        }

    @Test
    fun `reset user password by nonce with failed response without callback - resetPasswordByNonce`() =
        runTest {
            val response = Failure(ArcXPError("Error"))
            coEvery {
                identityRepository.resetPassword("asdf", ArcXPResetPasswordNonceRequest("asdf"))
            } returns response

            testObject.resetPasswordByNonce("asdf", "asdf", null)

            coVerify {
                identityRepository.resetPassword("asdf", ArcXPResetPasswordNonceRequest("asdf"))
            }
        }

    @Test
    fun `login using username & password successful response - makeLoginCall`() = runTest {
        val response = Success(authResponse)
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
            listener.onLoginSuccess(response.success)
        }
    }

    @Test
    fun `makeLoginCall- login using username & password successful response without callback`() =
        runTest {
            val response = authResponse

            coEvery {
                identityRepository.login(ArcXPAuthRequest("tester", "asdf", null, "password"))
            } returns Success(response)

            testObject.makeLoginCall(userName = "tester", password = "asdf", callback = null)

            coVerify {
                identityRepository.login(ArcXPAuthRequest("tester", "asdf", null, "password"))
            }
            assertEquals(response, testObject.authResponse.value)
        }

    @Test
    fun `login using username & password failed response- makeLoginCall`() = runTest {
        val response = Failure(ArcXPError("Error"))
        coEvery {
            identityRepository.login(ArcXPAuthRequest("tester", "asdf", null, "password", null))
        } returns response

        testObject.makeLoginCall("tester", "asdf", null, listener)

        coVerify {
            identityRepository.login(ArcXPAuthRequest("tester", "asdf", null, "password", null))
            listener.onLoginError(response.failure)
        }
    }

    @Test
    fun `makeLoginCall - login using username & password failed response without callback`() =
        runTest {
            val response = ArcXPError("Error")
            coEvery {
                identityRepository.login(
                    ArcXPAuthRequest(
                        "tester",
                        "asdf",
                        null,
                        "password",
                        null
                    )
                )
            } returns Failure(response)

            testObject.makeLoginCall("tester", "asdf", null, null)

            coVerify {
                identityRepository.login(
                    ArcXPAuthRequest(
                        "tester",
                        "asdf",
                        null,
                        "password",
                        null
                    )
                )
            }
            assertEquals(response, testObject.loginErrorResponse.value)
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
            listener.onLoginSuccess(response.success)
        }
    }

    @Test
    fun `login using third party login failure - thirdPartyLoginCall`() = runTest {
        val response = Failure(ArcXPError("Error"))
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
            listener.onLoginError(response.failure)
        }
    }

    @Test
    fun `login using third party login successfully without callback (apple) - thirdPartyLoginCall`() =
        runTest {
            val response = Success(authResponse)
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

    @Test
    fun `login using third party login failure without callback - thirdPartyLoginCall`() = runTest {
        val response = ArcXPError("Error")
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

    @Test
    fun `login using third party login (apple) failure with callback - thirdPartyLoginCall`() =
        runTest {
            val response = Failure(ArcXPError("Error"))
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
                listener.onLoginError(response.failure)
            }
        }

    @Test
    fun `login using third party login (apple) failure without callback - thirdPartyLoginCall`() =
        runTest {
            val response = Failure(ArcXPError("Error"))
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

    @Test
    fun `login using third party login successfully with callback (attach accounts)- thirdPartyLoginCall`() =
        runTest {
            val response = Success(authResponse)

            mockkObject(AuthManager)
            every { AuthManager.getInstance().uuid } returns "1234"
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
                listener.onLoginSuccess(response.success)
            }
        }

    @Test
    fun `login using third party login successfully with callback (unable to attach accounts)- thirdPartyLoginCall`() =
        runTest {
            val response = authResponse

            mockkObject(AuthManager)
            every { AuthManager.getInstance().uuid } returns "12"

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

    @Test
    fun `login using third party login successfully without callback (unable to attach accounts)- thirdPartyLoginCall`() =
        runTest {
            val response = authResponse

            mockkObject(AuthManager)
            every { AuthManager.getInstance().uuid } returns "12"
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

    @Test
    fun `login using third party login successfully without callback - thirdPartyLoginCall`() =
        runTest {
            val response = Success(authResponse)

            mockkObject(AuthManager)
            every { AuthManager.getInstance().uuid } returns null

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

    @Test
    fun `verify registered email successful response - verifyEmailCall`() = runTest {
        val response = Success(ArcXPEmailVerification(true))

        coEvery {
            identityRepository.verifyEmail(ArcXPVerifyEmailRequest("test@arctest.com"))
        } returns response

        testObject.verifyEmailCall("test@arctest.com", listener)

        coVerify {
            identityRepository.verifyEmail(ArcXPVerifyEmailRequest("test@arctest.com"))
            listener.onEmailVerificationSentSuccess(response.success)
        }
    }

    @Test
    fun `verify registered email failure response - verifyEmailCall`() = runTest {
        val response = Failure(ArcXPError("Error"))

        coEvery {
            identityRepository.verifyEmail(ArcXPVerifyEmailRequest("test@arctest.com"))
        } returns response

        testObject.verifyEmailCall("test@arctest.com", listener)

        coVerify {
            identityRepository.verifyEmail(ArcXPVerifyEmailRequest("test@arctest.com"))
            listener.onEmailVerificationSentError(response.failure)
        }
    }

    @Test
    fun `verify registered email successful response without callback - verifyEmailCall`() =
        runTest {
            val response = ArcXPEmailVerification(true)

            coEvery {
                identityRepository.verifyEmail(ArcXPVerifyEmailRequest("test@arctest.com"))
            } returns Success(response)

            testObject.verifyEmailCall("test@arctest.com", null)

            coVerify {
                identityRepository.verifyEmail(ArcXPVerifyEmailRequest("test@arctest.com"))
            }
            assertEquals(response, testObject.emailVerificationResponse.value)
        }

    @Test
    fun `verify registered email failure response without callback - verifyEmailCall`() = runTest {
        val response = ArcXPError("Error")

        coEvery {
            identityRepository.verifyEmail(ArcXPVerifyEmailRequest("test@arctest.com"))
        } returns Failure(response)

        testObject.verifyEmailCall("test@arctest.com", null)

        coVerify {
            identityRepository.verifyEmail(ArcXPVerifyEmailRequest("test@arctest.com"))
        }
        assertEquals(response, testObject.emailVerificationErrorResponse.value)
    }

    @Test
    fun `verify email with nonce successful response - verifyEmail`() = runTest {
        val response = Success(ArcXPEmailVerification(true))

        coEvery {
            identityRepository.verifyEmailNonce("asdf")
        } returns response

        testObject.verifyEmail("asdf", listener)

        coVerify {
            identityRepository.verifyEmailNonce("asdf")
            listener.onEmailVerifiedSuccess(response.success)
        }
    }

    @Test
    fun `verify email with nonce failure response - verifyEmail`() = runTest {
        val response = Failure(ArcXPError("Error"))

        coEvery {
            identityRepository.verifyEmailNonce("asdf")
        } returns response

        testObject.verifyEmail("asdf", listener)

        coVerify {
            identityRepository.verifyEmailNonce("asdf")
            listener.onEmailVerifiedError(response.failure)
        }
    }

    @Test
    fun `verify email with nonce successful response without callback - verifyEmail`() = runTest {
        val response = Success(ArcXPEmailVerification(true))

        coEvery {
            identityRepository.verifyEmailNonce("asdf")
        } returns response

        testObject.verifyEmail("asdf", null)

        coVerify {
            identityRepository.verifyEmailNonce("asdf")
        }
    }

    @Test
    fun `verify email with nonce failure response without callback - verifyEmail`() = runTest {
        val response = Failure(ArcXPError("Error"))

        coEvery {
            identityRepository.verifyEmailNonce("asdf")
        } returns response

        testObject.verifyEmail("asdf", null)

        coVerify {
            identityRepository.verifyEmailNonce("asdf")
        }
    }

    @Test
    fun `make call to get magicLink successful response - getMagicLink`() = runTest {
        val response = Success(ArcXPOneTimeAccessLink(true))

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
            listener.onOneTimeAccessLinkSuccess(response.success)
        }
    }

    @Test
    fun `make call to get magicLink failure response - getMagicLink`() = runTest {
        val response = Failure(ArcXPError("Error"))

        coEvery {
            identityRepository.getMagicLink(ArcXPOneTimeAccessLinkRequest("test@arctest.com"))
        } returns response

        testObject.getMagicLink("test@arctest.com", null, listener)

        coVerify {
            identityRepository.getMagicLink(ArcXPOneTimeAccessLinkRequest("test@arctest.com"))
            listener.onOneTimeAccessLinkError(response.failure)
        }
    }

    @Test
    fun `make call to get magicLink successful response without callback - getMagicLink`() =
        runTest {
            val response = Success(ArcXPOneTimeAccessLink(true))

            coEvery {
                identityRepository.getMagicLink(ArcXPOneTimeAccessLinkRequest("test@arctest.com"))
            } returns response

            testObject.getMagicLink("test@arctest.com", null)

            coVerify {
                identityRepository.getMagicLink(ArcXPOneTimeAccessLinkRequest("test@arctest.com"))
            }
        }

    @Test
    fun `make call to get magicLink failure response without callback - getMagicLink`() = runTest {
        val response = Failure(ArcXPError("Error"))

        coEvery {
            identityRepository.getMagicLink(ArcXPOneTimeAccessLinkRequest("test@arctest.com"))
        } returns response

        testObject.getMagicLink("test@arctest.com", null, null)

        coVerify {
            identityRepository.getMagicLink(ArcXPOneTimeAccessLinkRequest("test@arctest.com"))
        }
    }

    @Test
    fun `login with magic link successful response - loginMagicLink`() = runTest {
        val response = Success(ArcXPOneTimeAccessLinkAuth("asdf", "1234"))

        coEvery {
            identityRepository.loginMagicLink("asdf")
        } returns response

        testObject.loginMagicLink("asdf", listener)

        coVerify {
            identityRepository.loginMagicLink("asdf")
            listener.onOneTimeAccessLinkLoginSuccess(response.success)
        }
    }

    @Test
    fun `login with magic link failure response - loginMagicLink`() = runTest {
        val response = Failure(ArcXPError("Error"))

        coEvery {
            identityRepository.loginMagicLink("asdf")
        } returns response

        testObject.loginMagicLink("asdf", listener)

        coVerify {
            identityRepository.loginMagicLink("asdf")
            listener.onOneTimeAccessLinkError(response.failure)
        }
    }

    @Test
    fun `login with magic link successful response without callback - loginMagicLink`() = runTest {
        val response = ArcXPOneTimeAccessLinkAuth("asdf", "1234")

        coEvery {
            identityRepository.loginMagicLink("asdf")
        } returns Success(response)

        testObject.loginMagicLink("asdf", null)

        coVerify {
            identityRepository.loginMagicLink("asdf")
        }
        assertEquals(response, testObject.oneTimeAccessLinkAuthResponse.value)
    }

    @Test
    fun `login with magic link failure response without callback - loginMagicLink`() = runTest {
        val response = ArcXPError("Error")

        coEvery {
            identityRepository.loginMagicLink("asdf")
        } returns Failure(response)

        testObject.loginMagicLink("asdf", null)

        coVerify {
            identityRepository.loginMagicLink("asdf")
        }
        assertEquals(response, testObject.magicLinkErrorResponse.value)
    }

    @Test
    fun `logout - Successful response with callback`() = runTest {
        val response = mockk<Void>()

        coEvery {
            identityRepository.logout()
        } returns Success(response)

        testObject.logout(listener)

        coVerify {
            identityRepository.logout()
            listener.onLogoutSuccess()
        }
    }

    @Test
    fun `logout - Successful response without callback`() = runTest {
        val response = mockk<Void>()
        coEvery {
            identityRepository.logout()
        } returns Success(response)

        testObject.logout(null)

        coVerify {
            identityRepository.logout()
        }

        assertEquals(true, testObject.logoutResponse.value)
    }

    @Test
    fun `logout - Failed response with callback`() = runTest {
        val response = Failure(ArcXPError("Failed"))

        coEvery {
            identityRepository.logout()
        } returns response

        testObject.logout(listener)

        coVerify {
            identityRepository.logout()
            listener.onLogoutError(response.failure)
        }
    }


    @Test
    fun `logout - Failed response without callback`() = runTest {
        val response = ArcXPError("Failed")

        coEvery {
            identityRepository.logout()
        } returns Failure(response)

        testObject.logout(null)

        coVerify {
            identityRepository.logout()
        }

        assertEquals(response, testObject.logoutErrorResponse.value)
    }

    @Test
    fun `make a call to patch profile successful response - patchProfile`() = runTest {

        val response = Success(mockk<ArcXPProfileManage>())
        coEvery {
            identityRepository.patchProfile(patchRequest)
        } returns response

        testObject.patchProfile(patchRequest, listener)

        coVerify {
            identityRepository.patchProfile(patchRequest)
            listener.onProfileUpdateSuccess(response.success)
        }
    }

    @Test
    fun `make a call to patch profile failure response - patchProfile`() = runTest {
        val response = Failure(ArcXPError("Error"))

        coEvery {
            identityRepository.patchProfile(patchRequest)
        } returns response

        testObject.patchProfile(patchRequest, listener)

        coVerify {
            identityRepository.patchProfile(patchRequest)
            listener.onProfileError(response.failure)
        }
    }

    @Test
    fun `make a call to patch profile successful response without callback - patchProfile`() =
        runTest {
            val response = mockk<ArcXPProfileManage>()

            coEvery {
                identityRepository.patchProfile(patchRequest)
            } returns Success(response)

            testObject.patchProfile(patchRequest, null)

            coVerify {
                identityRepository.patchProfile(patchRequest)
            }
            assertEquals(response, testObject.profileResponse.value)
        }

    @Test
    fun `make a call to patch profile failure response without callback - patchProfile`() =
        runTest {
            val response = ArcXPError("Error")

            coEvery {
                identityRepository.patchProfile(patchRequest)
            } returns Failure(response)

            testObject.patchProfile(patchRequest, null)

            coVerify {
                identityRepository.patchProfile(patchRequest)
            }
            assertEquals(response, testObject.profileErrorResponse.value)
        }

    @Test
    fun `make call to fetch profile with successful response - getProfile`() = runTest {
        val response = Success(mockk<ArcXPProfileManage>())

        coEvery {
            identityRepository.getProfile()
        } returns response

        testObject.getProfile(listener)

        coVerify {
            identityRepository.getProfile()
            listener.onFetchProfileSuccess(response.success)
        }
    }

    @Test
    fun `make call to fetch profile with failure response - getProfile`() = runTest {
        val response = Failure(ArcXPError("Error"))

        coEvery {
            identityRepository.getProfile()
        } returns response

        testObject.getProfile(listener)

        coVerify {
            identityRepository.getProfile()
            listener.onProfileError(response.failure)
        }
    }

    @Test
    fun `make call to fetch profile with successful response without callback - getProfile`() =
        runTest {
            val response = Success(mockk<ArcXPProfileManage>())

            coEvery {
                identityRepository.getProfile()
            } returns response

            testObject.getProfile(null)

            coVerify {
                identityRepository.getProfile()
            }
        }

    @Test
    fun `make call to fetch profile with failure response without callback - getProfile`() =
        runTest {
            val response = Failure(ArcXPError("Error"))

            coEvery {
                identityRepository.getProfile()
            } returns response

            testObject.getProfile(null)

            coVerify {
                identityRepository.getProfile()
            }
        }


    @Test
    fun `register a new user successfully - makeRegistrationCall`() = runTest {
        val response = Success(mockk<ArcXPUser>())

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
            listener.onRegistrationSuccess(response.success)
        }
    }

    @Test
    fun `register a new user failure response - makeRegistrationCall`() = runTest {
        val response = Failure(ArcXPError("Error"))

        coEvery {
            identityRepository.signUp(ArcXPSignUpRequest(identityRequest, profileRequest))
        } returns response

        testObject.makeRegistrationCall("", "", "", "", "", listener)

        coVerify {
            identityRepository.signUp(ArcXPSignUpRequest(identityRequest, profileRequest))
            listener.onRegistrationError(response.failure)
        }
    }

    @Test
    fun `register a new user successfully without callback - makeRegistrationCall`() = runTest {
        val response = mockk<ArcXPUser>()

        coEvery {
            identityRepository.signUp(ArcXPSignUpRequest(identityRequest, profileRequest))
        } returns Success(response)

        testObject.makeRegistrationCall("", "", "", "", "", null)

        coVerify {
            identityRepository.signUp(ArcXPSignUpRequest(identityRequest, profileRequest))
        }

        assertEquals(response, testObject.registrationResponse.value)
    }

    @Test
    fun `register a new user failure response without callback - makeRegistrationCall`() = runTest {
        val response = ArcXPError("Error")

        coEvery {
            identityRepository.signUp(ArcXPSignUpRequest(identityRequest, profileRequest))
        } returns Failure(response)

        testObject.makeRegistrationCall("", "", "", "", "", null)

        coVerify {
            identityRepository.signUp(ArcXPSignUpRequest(identityRequest, profileRequest))
        }
        assertEquals(response, testObject.registrationError.value)
    }

    @Test
    fun `make a call to delete user successful response - deleteUser`() = runTest {
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
    fun `make a call to delete user failure response - deleteUser`() = runTest {
        val response = Failure(ArcXPError("Error"))

        coEvery {
            identityRepository.deleteUser()
        } returns response

        testObject.deleteUser(listener)

        coVerify {
            identityRepository.deleteUser()
            listener.onDeleteUserError(response.failure)
        }
    }

    @Test
    fun `make a call to delete user successful response without callback - deleteUser`() = runTest {
        val response = ArcXPAnonymizeUser(true)

        coEvery {
            identityRepository.deleteUser()
        } returns Success(response)

        testObject.deleteUser(null)

        coVerify {
            identityRepository.deleteUser()
        }
        assertEquals(true, testObject.deletionResponse.value)
    }

    @Test
    fun `make a call to delete user failure response without callback - deleteUser`() = runTest {
        val response = ArcXPError("Error")

        coEvery {
            identityRepository.deleteUser()
        } returns Failure(response)

        testObject.deleteUser(null)

        coVerify {
            identityRepository.deleteUser()
        }
        assertEquals(response, testObject.deletionErrorResponse.value)
    }

    @Test
    fun `make a call to delete user successful response valid is false - deleteUser`() = runTest {
        val response = Success(ArcXPAnonymizeUser(false))
        val failedResponse = Failure(ArcXPError("Your account deletion request is declined."))

        coEvery {
            identityRepository.deleteUser()
        } returns response

        testObject.deleteUser(listener)

        coVerify {
            identityRepository.deleteUser()
        }
    }

    @Test
    fun `make call to approve deletion request successful response - approveDeletion`() = runTest {
        val response = Success(ArcXPDeleteUser(true))

        coEvery {
            identityRepository.approveDeletion("asdf")
        } returns response

        testObject.approveDeletion("asdf", listener)

        coVerify {
            identityRepository.approveDeletion("asdf")
            listener.onApproveDeletionSuccess(response.success)
        }
    }

    @Test
    fun `make call to approve deletion request successful response without ArcXPDeletUser  - approveDeletion`() =
        runTest {
            val response = Success(ArcXPDeleteUser(false))

            coEvery {
                identityRepository.approveDeletion("asdf")
            } returns response

            testObject.approveDeletion("asdf", listener)

            coVerify {
                identityRepository.approveDeletion("asdf")
            }
        }

    @Test
    fun `make call to approve deletion request failure response - approveDeletion`() = runTest {
        val response = Failure(ArcXPError("Error"))

        coEvery {
            identityRepository.approveDeletion("asdf")
        } returns response

        testObject.approveDeletion("asdf", listener)

        coVerify {
            identityRepository.approveDeletion("asdf")
            listener.onApproveDeletionError(response.failure)
        }
    }

    @Test
    fun `make call to approve deletion request failure response without callback - approveDeletion`() =
        runTest {
            val response = Failure(ArcXPError("Error"))

            coEvery {
                identityRepository.approveDeletion("asdf")
            } returns response

            testObject.approveDeletion("asdf", listener)

            coVerify {
                identityRepository.approveDeletion("asdf")
                listener.onApproveDeletionError(response.failure)
            }
        }

    @Test
    fun `make call to validate jwt with token successful response - validateJwt`() = runTest {
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
    fun `make call to validate jwt with token failure response - validateJwt`() = runTest {
        val response = Failure(ArcXPError("Error"))

        coEvery {
            identityRepository.validateJwt("asdf")
        } returns response

        testObject.validateJwt("asdf", listener)

        coVerify {
            identityRepository.validateJwt("asdf")
            listener.onValidateSessionError(response.failure)
        }
    }

    @Test
    fun `make call to validate jwt with token successful response without callback - validateJwt`() =
        runTest {
            val response = Success(authResponse)

            coEvery {
                identityRepository.validateJwt("asdf")
            } returns response

            testObject.validateJwt("asdf", null)

            coVerify {
                identityRepository.validateJwt("asdf")
                Success(authResponse)
            }
        }

    @Test
    fun `make call to validate jwt with token failure response without callback - validateJwt`() =
        runTest {
            val response = Failure(ArcXPError("Error"))

            coEvery {
                identityRepository.validateJwt("asdf")
            } returns response

            testObject.validateJwt("asdf", null)

            coVerify {
                identityRepository.validateJwt("asdf")
                Failure(ArcXPError("Error"))
            }
        }

    @Test
    fun `make call to validate jwt successful response - validateJwt`() = runTest {
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
    fun `make call to validate jwt failure response - validateJwt`() = runTest {
        val response = Failure(ArcXPError("Error"))

        coEvery {
            identityRepository.validateJwt()
        } returns response

        testObject.validateJwt(listener)

        coVerify {
            identityRepository.validateJwt()
            listener.onValidateSessionError(response.failure)
        }
    }

    @Test
    fun `make call to validate jwt successful response without callback - validateJwt`() = runTest {
        val response = Success(authResponse)

        coEvery {
            identityRepository.validateJwt()
        } returns response

        testObject.validateJwt(null)

        coVerify {
            identityRepository.validateJwt()
            Success(authResponse)
        }
    }

    @Test
    fun `make call to validate jwt failure response without callback - validateJwt`() = runTest {
        val response = Failure(ArcXPError("Error"))

        coEvery {
            identityRepository.validateJwt()
        } returns response

        testObject.validateJwt(null)

        coVerify {
            identityRepository.validateJwt()
            Failure(ArcXPError("Error"))
        }
    }

    @Test
    fun `make call to refresh token successful response`() = runTest {
        val response = Success(authResponse)

        coEvery {
            identityRepository.refreshToken("asdf", "refresh-token")
        } returns response

        testObject.refreshToken("asdf", "refresh-token", listener)

        coVerify {
            identityRepository.refreshToken("asdf", "refresh-token")
            listener.onRefreshSessionSuccess(response.success)
        }
    }

    @Test
    fun `make call to refresh token successful response without callback`() = runTest {
        val response = Success(authResponse)

        coEvery {
            identityRepository.refreshToken("asdf", "refresh-token")
        } returns response

        testObject.refreshToken("asdf", "refresh-token", null)

        coVerify {
            identityRepository.refreshToken("asdf", "refresh-token")
            Success(authResponse)
        }
    }

    @Test
    fun `make call to refresh token failure response`() = runTest {
        val response = Failure(ArcXPError("Error"))

        coEvery {
            identityRepository.refreshToken("asdf", "refresh-token")
        } returns response

        testObject.refreshToken("asdf", "refresh-token", listener)

        coVerify {
            identityRepository.refreshToken("asdf", "refresh-token")
            listener.onRefreshSessionFailure(response.failure)
        }
    }


    @Test
    fun `make call to refresh token failure response without callback`() = runTest {
        val response = Failure(ArcXPError("Error"))

        coEvery {
            identityRepository.refreshToken("asdf", "refresh-token")
        } returns response

        testObject.refreshToken("asdf", "refresh-token", null)

        coVerify {
            identityRepository.refreshToken("asdf", "refresh-token")
            Failure(ArcXPError("Error"))
        }
    }

    @Test
    fun `make call to get tenet config successful response - getTenetConfig`() = runTest {
        val response = Success(configResponse)

        coEvery {
            identityRepository.getConfig()
        } returns response

        testObject.getTenetConfig(listener)

        coVerify {
            identityRepository.getConfig()
            listener.onLoadConfigSuccess(response.success)
        }
    }

    @Test
    fun `make call to get tenet config failure response - getTenetConfig`() = runTest {
        val response = Failure(ArcXPError("Error"))

        coEvery {
            identityRepository.getConfig()
        } returns response

        testObject.getTenetConfig(listener)

        coVerify {
            identityRepository.getConfig()
            listener.onLoadConfigFailure(response.failure)
        }
    }

    @Test
    fun `getTenetConfig failure response with exception`() = runTest {
        val error = mockk<ArcXPError>()
        val exception = mockk<Exception>()
        val message = "message"
        coEvery { exception.message } returns message
        mockkObject(DependencyFactory)
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

        coEvery {
            identityRepository.removeIdentity("")
        } returns response

        testObject.removeIdentity("", listener)

        coVerify {
            identityRepository.removeIdentity("")
            listener.onRemoveIdentitySuccess(response.success)
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
        mockkObject(DependencyFactory)
        coEvery {
            createError(
                ArcXPCommerceSDKErrorType.RECAPTCHA_ERROR,
                localizedMsg,
                exception
            )
        } returns error

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
        mockkObject(DependencyFactory)
        coEvery {
            createError(
                ArcXPCommerceSDKErrorType.RECAPTCHA_ERROR,
                localizedMsg,
                exception
            )
        } returns error

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

        coEvery {
            identityRepository.removeIdentity("")
        } returns response

        testObject.removeIdentity("", listener)

        coVerify {
            identityRepository.removeIdentity("")
            listener.onRemoveIdentityFailure(response.failure)
        }
    }

    @Test
    fun `verify removal of identity successful response without callback - removeIdentity`() =
        runTest {
            val response = updateUserStatusResponse

            coEvery {
                identityRepository.removeIdentity("")
            } returns Success(response)

            testObject.removeIdentity("", null)

            coVerify {
                identityRepository.removeIdentity("")
            }
            assertEquals(response, testObject.updateUserStatusResponse.value)
        }

    @Test
    fun `verify removal of identity failure response without callback - removeIdentity`() =
        runTest {
            val response = ArcXPError("Error")

            coEvery {
                identityRepository.removeIdentity("")
            } returns Failure(response)

            testObject.removeIdentity("", null)

            coVerify {
                identityRepository.removeIdentity("")
            }
            assertEquals(response, testObject.updateUserStatusFailureResponse.value)
        }

    @Test
    fun `rememerUser - Succesful request`() {
        testObject.rememberUser(true)

        verify {
            authManager.setShouldRememberUser(true)
        }
    }

    @Test
    fun `nonce - nonce is null`() {
        testObject.nonce
    }

    @Test
    fun `nonce - nonce is set`() {
        testObject.nonce = "nonce"
        testObject.nonce
    }

    @Test
    fun `recaptchaToken - recaptchaToken is null`() {
        testObject.recaptchaToken
    }

    @Test
    fun `recaptchaToken - recaptchaToken is set`() {
        testObject.recaptchaToken = "recaptcha"
        testObject.recaptchaToken
    }
}