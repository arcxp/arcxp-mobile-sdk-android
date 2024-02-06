package com.arcxp.commerce.repositories

import androidx.media3.common.util.UnstableApi
import com.arcxp.commerce.extendedModels.ArcXPProfileManage
import com.arcxp.commerce.models.ArcXPAnonymizeUser
import com.arcxp.commerce.models.ArcXPAuth
import com.arcxp.commerce.models.ArcXPAuthRequest
import com.arcxp.commerce.models.ArcXPConfig
import com.arcxp.commerce.models.ArcXPDeleteUser
import com.arcxp.commerce.models.ArcXPEmailVerification
import com.arcxp.commerce.models.ArcXPIdentity
import com.arcxp.commerce.models.ArcXPOneTimeAccessLink
import com.arcxp.commerce.models.ArcXPOneTimeAccessLinkAuth
import com.arcxp.commerce.models.ArcXPOneTimeAccessLinkRequest
import com.arcxp.commerce.models.ArcXPPasswordResetRequest
import com.arcxp.commerce.models.ArcXPProfilePatchRequest
import com.arcxp.commerce.models.ArcXPRequestPasswordReset
import com.arcxp.commerce.models.ArcXPResetPasswordNonceRequest
import com.arcxp.commerce.models.ArcXPResetPasswordRequestRequest
import com.arcxp.commerce.models.ArcXPSignUpRequest
import com.arcxp.commerce.models.ArcXPUpdateUserStatus
import com.arcxp.commerce.models.ArcXPUser
import com.arcxp.commerce.models.ArcXPVerifyEmailRequest
import com.arcxp.commerce.retrofit.IdentityService
import com.arcxp.commerce.retrofit.IdentityServiceNoAuth
import com.arcxp.commerce.retrofit.RetrofitController
import com.arcxp.commerce.util.AuthManager
import com.arcxp.commons.throwables.ArcXPSDKErrorType
import com.arcxp.commons.util.DependencyFactory.createArcXPException
import com.arcxp.commons.util.Either
import com.arcxp.commons.util.Failure
import com.arcxp.commons.util.Success
import okhttp3.ResponseBody
import retrofit2.Response

/**
 * @suppress
 */
@UnstableApi
class IdentityRepository(
    private val identityService: IdentityService = RetrofitController.getIdentityService(),
    private val identityServiceNoAuth: IdentityServiceNoAuth = RetrofitController.getIdentityServiceNoAuth(),
    private val identityServiceApple: IdentityServiceNoAuth = RetrofitController.getIdentityServiceForApple()
) {

    /**
     * function to make login request
     *
     * @param authRequest request for authentication
     * @return Either success response or failure
     */
    suspend fun login(authRequest: ArcXPAuthRequest): Either<Any?, ArcXPAuth?> =
        try {
            val response = identityServiceNoAuth.login(
                authRequest
            )
            with(response) {
                when {
                    isSuccessful -> Success(body())
                    else -> Failure(
                        createArcXPException(
                            ArcXPSDKErrorType.AUTHENTICATION_ERROR,
                            response.message(),
                            response
                        )
                    )
                }
            }

        } catch (e: Exception) {
            Failure(createArcXPException(ArcXPSDKErrorType.EXCEPTION, e.message, e))
        }

    /*
    For internal use only. Not meant for public
     */
    suspend fun appleLogin(authRequest: ArcXPAuthRequest): Either<Any?, ArcXPAuth?> =
        try {
            val response = identityServiceApple.login(
                authRequest
            )
            with(response) {
                when {
                    isSuccessful -> Success(body())
                    else -> Failure(
                        createArcXPException(
                            ArcXPSDKErrorType.APPLE_LOGIN_ERROR,
                            response.message(),
                            response
                        )
                    )
                }
            }

        } catch (e: Exception) {
            Failure(createArcXPException(ArcXPSDKErrorType.EXCEPTION, e.message, e))
        }

    /**
     * function to make reset password request
     *
     * @param passwordChangeRequest request for reset password
     * @return Either success response or failure
     */
    suspend fun changePassword(passwordChangeRequest: ArcXPPasswordResetRequest): Either<Any?, ArcXPIdentity?> =
        try {
            val response = identityService.changePassword(
                passwordChangeRequest
            )
            with(response) {
                when {
                    isSuccessful -> Success(body())
                    else -> Failure(
                        createArcXPException(
                            ArcXPSDKErrorType.SERVER_ERROR,
                            response.message(),
                            response
                        )
                    )
                }
            }

        } catch (e: Exception) {
            Failure(createArcXPException(ArcXPSDKErrorType.EXCEPTION, e.message, e))
        }

    /**
     * function to make reset password request
     *
     * @param resetPasswordRequest request for reset password
     * @return Either success response or failure
     */
    suspend fun resetPassword(resetPasswordRequest: ArcXPResetPasswordRequestRequest): Either<Any?, ArcXPRequestPasswordReset?> =
        try {
            val response = identityService.resetPassword(
                resetPasswordRequest
            )
            with(response) {
                when {
                    isSuccessful -> Success(body())
                    else -> Failure(
                        createArcXPException(
                            ArcXPSDKErrorType.SERVER_ERROR,
                            response.message(),
                            response
                        )
                    )
                }
            }

        } catch (e: Exception) {
            Failure(createArcXPException(ArcXPSDKErrorType.EXCEPTION, e.message, e))
        }

    /**
     * function to make approve password reset
     *
     * @param resetPasswordNonceRequest request for reset password
     * @return Either success response or failure
     */
    suspend fun resetPassword(
        nonce: String,
        resetPasswordNonceRequest: ArcXPResetPasswordNonceRequest
    ): Either<Any?, ArcXPIdentity?> =
        try {
            val response = identityService.resetPassword(nonce, resetPasswordNonceRequest)
            with(response) {
                when {
                    isSuccessful -> Success(body())
                    else -> Failure(
                        createArcXPException(
                            ArcXPSDKErrorType.SERVER_ERROR,
                            response.message(),
                            response
                        )
                    )
                }
            }

        } catch (e: Exception) {
            Failure(createArcXPException(ArcXPSDKErrorType.EXCEPTION, e.message, e))
        }

    suspend fun getMagicLink(request: ArcXPOneTimeAccessLinkRequest): Either<Any?, ArcXPOneTimeAccessLink?> =
        try {
            val response = identityServiceNoAuth.getMagicLink(
                request
            )
            with(response) {
                when {
                    isSuccessful -> Success(body())
                    else -> Failure(
                        createArcXPException(
                            ArcXPSDKErrorType.SERVER_ERROR,
                            response.message(),
                            response
                        )
                    )
                }
            }
        } catch (e: Exception) {
            Failure(createArcXPException(ArcXPSDKErrorType.EXCEPTION, e.message, e))
        }

    suspend fun loginMagicLink(nonce: String): Either<Any?, ArcXPOneTimeAccessLinkAuth?> =
        try {
            val response = identityServiceNoAuth.loginMagicLink(
                nonce
            )
            with(response) {
                when {
                    isSuccessful -> Success(body())
                    else -> Failure(
                        createArcXPException(
                            ArcXPSDKErrorType.SERVER_ERROR,
                            response.message(),
                            response
                        )
                    )
                }
            }
        } catch (e: Exception) {
            Failure(createArcXPException(ArcXPSDKErrorType.EXCEPTION, e.message, e))
        }

    /**
     * function to make get profile request
     *
     * @return Either success response or failure
     */
    suspend fun getProfile(): Either<Any?, ArcXPProfileManage?> =
        try {
            val response = identityService.getProfile()
            with(response) {
                when {
                    isSuccessful -> Success(body())
                    else -> Failure(
                        createArcXPException(
                            ArcXPSDKErrorType.SERVER_ERROR,
                            response.message(),
                            response
                        )
                    )
                }
            }
        } catch (e: Exception) {
            Failure(createArcXPException(ArcXPSDKErrorType.EXCEPTION, e.message, e))
        }

    /**
     * function to make patch profile request
     *
     * @param profileRequest request for patch profile
     * @return Either success response or failure
     */
    suspend fun patchProfile(profileRequest: ArcXPProfilePatchRequest): Either<Any?, ArcXPProfileManage?> =
        try {
            val response = identityService.patchProfile(
                profileRequest
            )
            with(response) {
                when {
                    isSuccessful -> Success(body())
                    else -> Failure(
                        createArcXPException(
                            ArcXPSDKErrorType.SERVER_ERROR,
                            response.message(),
                            response
                        )
                    )
                }
            }
        } catch (e: Exception) {
            Failure(createArcXPException(ArcXPSDKErrorType.EXCEPTION, e.message, e))
        }

    /**
     * function to make registration request
     *
     * @param signUpRequest request for registration
     * @return Either success response or failure
     */
    suspend fun signUp(signUpRequest: ArcXPSignUpRequest): Either<Any?, ArcXPUser?> =
        try {
            val response = identityServiceNoAuth.signUp(
                signUpRequest
            )
            with(response) {
                when {
                    isSuccessful -> Success(body())
                    else -> Failure(
                        createArcXPException(
                            ArcXPSDKErrorType.SERVER_ERROR,
                            response.message(),
                            response
                        )
                    )
                }
            }

        } catch (e: Exception) {
            Failure(createArcXPException(ArcXPSDKErrorType.EXCEPTION, e.message, e))
        }

    /**
     * function to make verification email request
     *
     * @param verifyEmailRequest request for verify email
     * @return Either success response or failure
     */
    suspend fun verifyEmail(verifyEmailRequest: ArcXPVerifyEmailRequest): Either<Any?, ArcXPEmailVerification?> =
        try {
            val response = identityServiceNoAuth.verifyEmail(
                verifyEmailRequest
            )
            with(response) {
                when {
                    isSuccessful -> Success(body())
                    else -> Failure(
                        createArcXPException(
                            ArcXPSDKErrorType.SERVER_ERROR,
                            response.message(),
                            response
                        )
                    )
                }
            }
        } catch (e: Exception) {
            Failure(createArcXPException(ArcXPSDKErrorType.EXCEPTION, e.message, e))
        }

    suspend fun verifyEmailNonce(nonce: String): Either<Any?, ArcXPEmailVerification?> =
        try {
            val response = identityService.verifyEmail(nonce)
            with(response) {
                when {
                    isSuccessful -> Success(body())
                    else -> Failure(
                        createArcXPException(
                            ArcXPSDKErrorType.SERVER_ERROR,
                            response.message(),
                            response
                        )
                    )
                }
            }
        } catch (e: Exception) {
            Failure(createArcXPException(ArcXPSDKErrorType.EXCEPTION, e.message, e))
        }


    suspend fun logout(): Either<Any?, Void?> =
        try {
            val response = identityService.logout()
            with(response) {
                when {
                    isSuccessful -> Success(body())
                    else -> Failure(
                        createArcXPException(
                            ArcXPSDKErrorType.SERVER_ERROR,
                            response.message(),
                            response
                        )
                    )
                }
            }
        } catch (e: Exception) {
            Failure(createArcXPException(ArcXPSDKErrorType.EXCEPTION, e.message, e))
        }

    /**
     * function to validate if the cached access token is still validated
     *
     * @return Either success response or failure
     */
    suspend fun validateJwt(): Either<Any?, ArcXPAuth?> =
        try {
            val response = identityService.validateJwt(ArcXPAuthRequest())
            with(response) {
                when {
                    isSuccessful -> Success(body())
                    else -> Failure(
                        createArcXPException(
                            ArcXPSDKErrorType.INVALID_SESSION,
                            "Invalid Session",
                            response
                        )
                    )
                }
            }
        } catch (e: Exception) {
            Failure(createArcXPException(ArcXPSDKErrorType.EXCEPTION, e.message, e))
        }

    /**
     * function to extend the session using refresh token
     *
     * @return Either success response or failure
     */
    suspend fun refreshToken(token: String?, grantType: String): Either<Any?, ArcXPAuth?> =
        try {
            AuthManager.getInstance().accessToken = null
            val response = identityServiceNoAuth.refreshToken(
                ArcXPAuthRequest(token = token, grantType = grantType)
            )
            with(response) {
                when {
                    isSuccessful -> Success(body())
                    else -> Failure(
                        createArcXPException(
                            ArcXPSDKErrorType.INVALID_SESSION,
                            "Refresh Token expired",
                            response
                        )
                    )
                }
            }
        } catch (e: Exception) {
            Failure(createArcXPException(ArcXPSDKErrorType.EXCEPTION, e.message, e))
        }

    suspend fun removeIdentity(grantType: String): Either<Any?, ArcXPUpdateUserStatus?> =
        try {
            val response = identityService.deleteIdentities(grantType)
            with(response) {
                when {
                    isSuccessful -> Success(body())
                    else -> Failure(
                        createArcXPException(
                            ArcXPSDKErrorType.SERVER_ERROR,
                            response.message(),
                            response
                        )
                    )
                }
            }
        } catch (e: Exception) {
            Failure(createArcXPException(ArcXPSDKErrorType.EXCEPTION, e.message, e))
        }


    /**
     * function to request a current user deletion
     *
     * @return Either success response or failure
     */
    suspend fun deleteUser(): Either<Any?, ArcXPAnonymizeUser?> =
        try {
            val response = identityService.deleteUser()
            with(response) {
                when {
                    isSuccessful -> Success(body())
                    else -> Failure(createArcXPException(ArcXPSDKErrorType.SERVER_ERROR, response.message(), response))
                }
            }
        } catch (e: Exception) {
            Failure(createArcXPException(ArcXPSDKErrorType.EXCEPTION, e.message, e))
        }

    suspend fun approveDeletion(nonce: String): Either<Any?, ArcXPDeleteUser?> =
        try {
            val response = identityService.approveDeletion(nonce)
            with(response) {
                when {
                    isSuccessful -> Success(body())
                    else -> Failure(
                        createArcXPException(
                            ArcXPSDKErrorType.SERVER_ERROR,
                            response.message(),
                            response
                        )
                    )
                }
            }
        } catch (e: Exception) {
            Failure(createArcXPException(ArcXPSDKErrorType.EXCEPTION, e.message, e))
        }

    /**
     * function to get apple login auth url
     *
     * @return Either success response or failure
     */
    suspend fun appleAuthUrl(): Either<Any?, ResponseBody?> =
        try {
            val response = identityServiceNoAuth.appleAuthUrl()
            with(response) {
                when {
                    isSuccessful -> Success(body())
                    else -> Failure(
                        createArcXPException(
                            ArcXPSDKErrorType.APPLE_LOGIN_ERROR,
                            response.message(),
                            response
                        )
                    )
                }
            }
        } catch (e: Exception) {
            Failure(createArcXPException(ArcXPSDKErrorType.EXCEPTION, e.message, e))
        }

    /*
    For internal use only. Not meant for public
     */
    internal suspend fun appleAuthUrlUpdatedURL(): Either<Any?, ResponseBody?> =
        try {
            val response = identityServiceApple.appleAuthUrl()
            with(response) {
                when {
                    isSuccessful -> Success(body())
                    else -> Failure(
                        createArcXPException(
                            ArcXPSDKErrorType.APPLE_LOGIN_ERROR,
                            response.message(),
                            response
                        )
                    )
                }
            }
        } catch (e: Exception) {
            Failure(createArcXPException(ArcXPSDKErrorType.EXCEPTION, e.message, e))
        }

    suspend fun getConfig(): Either<Any?, ArcXPConfig?> =
        try {
            val response = identityService.config()
            with(response) {
                when {
                    isSuccessful -> Success(body())
                    else -> Failure(
                        createArcXPException(
                            ArcXPSDKErrorType.CONFIG_ERROR,
                            response.message(),
                            response
                        )
                    )
                }
            }
        } catch (e: Exception) {
            Failure(createArcXPException(ArcXPSDKErrorType.EXCEPTION, e.message, e))
        }
}