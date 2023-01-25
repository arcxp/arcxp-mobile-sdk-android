package com.arcxp.commerce.repositories

import com.arcxp.commerce.ArcXPCommerceSDKErrorType
import com.arcxp.commerce.extendedModels.ArcXPProfileManage
import com.arcxp.commerce.models.*
import com.arcxp.commerce.retrofit.IdentityService
import com.arcxp.commerce.retrofit.RetrofitController
import com.arcxp.commerce.util.*
import com.arcxp.commerce.util.ArcXPError
import okhttp3.ResponseBody

/**
 * @suppress
 */
class IdentityRepository(
    private val identityService: IdentityService = RetrofitController.getIdentityService(),
    private val identityServiceApple: IdentityService = RetrofitController.getIdentityServiceForApple()
){


    /**
     * function to make login request
     *
     * @param authRequest request for authentication
     * @return Either success response or failure
     */
    suspend fun login(authRequest: ArcXPAuthRequest): Either<Any?, ArcXPAuth?> =
        try {
            val response = identityService.login(
                authRequest)
            with(response) {
                when {
                    isSuccessful -> Success(body())
                    else -> Failure(ArcXPError(ArcXPCommerceSDKErrorType.SERVER_ERROR, response.message(), response))
                }
            }

        } catch (e: Exception) {
            Failure(ArcXPError(ArcXPCommerceSDKErrorType.SERVER_ERROR, e.message!!, e))
        }

    /*
    For internal use only. Not meant for public
     */
    suspend fun appleLogin(authRequest: ArcXPAuthRequest): Either<Any?, ArcXPAuth?> =
            try {
                val response = identityServiceApple.login(
                        authRequest)
                with(response) {
                    when {
                        isSuccessful -> Success(body())
                        else -> Failure(ArcXPError(ArcXPCommerceSDKErrorType.SERVER_ERROR, response.message(), response))
                    }
                }

            } catch (e: Exception) {
                Failure(ArcXPError(ArcXPCommerceSDKErrorType.SERVER_ERROR, e.message!!, e))
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
                passwordChangeRequest)
            with(response) {
                when {
                    isSuccessful -> Success(body())
                    else -> Failure(ArcXPError(ArcXPCommerceSDKErrorType.SERVER_ERROR, response.message(), response))
                }
            }

        } catch (e: Exception) {
            Failure(ArcXPError(ArcXPCommerceSDKErrorType.SERVER_ERROR, e.message!!, e))
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
                resetPasswordRequest)
            with(response) {
                when {
                    isSuccessful -> Success(body())
                    else -> Failure(ArcXPError(ArcXPCommerceSDKErrorType.SERVER_ERROR, response.message(), response))
                }
            }

        } catch (e: Exception) {
            Failure(ArcXPError(ArcXPCommerceSDKErrorType.SERVER_ERROR, e.message!!, e))
        }

    /**
     * function to make approve password reset
     *
     * @param resetPasswordRequest request for reset password
     * @return Either success response or failure
     */
    suspend fun resetPassword(nonce: String, resetPasswordNonceRequest: ArcXPResetPasswordNonceRequest): Either<Any?, ArcXPIdentity?> =
            try {
                val response = identityService.resetPassword(nonce, resetPasswordNonceRequest)
                with(response) {
                    when {
                        isSuccessful -> Success(body())
                        else -> Failure(ArcXPError(ArcXPCommerceSDKErrorType.SERVER_ERROR, response.message(), response))
                    }
                }

            } catch (e: Exception) {
                Failure(ArcXPError(ArcXPCommerceSDKErrorType.SERVER_ERROR, e.message!!, e))
            }

    suspend fun getMagicLink(request: ArcXPOneTimeAccessLinkRequest) : Either<Any?, ArcXPOneTimeAccessLink?> =
        try {
            val response = identityService.getMagicLink(
                request)
            with(response) {
                when {
                    isSuccessful -> Success(body())
                    else -> Failure(ArcXPError(ArcXPCommerceSDKErrorType.SERVER_ERROR, response.message(), response))
                }
            }
        } catch(e: Exception) {
            Failure(ArcXPError(ArcXPCommerceSDKErrorType.SERVER_ERROR, e.message!!, e))
        }

    suspend fun loginMagicLink(nonce: String): Either<Any?, ArcXPOneTimeAccessLinkAuth?> =
        try {
            val response = identityService.loginMagicLink(
                nonce)
            with(response) {
                when {
                    isSuccessful -> Success(body())
                    else -> Failure(ArcXPError(ArcXPCommerceSDKErrorType.SERVER_ERROR, response.message(), response))
                }
            }
        } catch (e: Exception) {
            Failure(ArcXPError(ArcXPCommerceSDKErrorType.SERVER_ERROR, e.message!!, e))
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
                    else -> Failure(ArcXPError(ArcXPCommerceSDKErrorType.SERVER_ERROR, response.message(), response))
                }
            }
        } catch (e: Exception) {
            Failure(ArcXPError(ArcXPCommerceSDKErrorType.SERVER_ERROR, e.message!!, e))
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
                profileRequest)
            with(response) {
                when {
                    isSuccessful -> Success(body())
                    else -> Failure(ArcXPError(ArcXPCommerceSDKErrorType.SERVER_ERROR, response.message(), response))
                }
            }
        } catch (e: Exception) {
            Failure(ArcXPError(ArcXPCommerceSDKErrorType.SERVER_ERROR, e.message!!, e))
        }

    /**
     * function to make registration request
     *
     * @param signUpRequest request for registration
     * @return Either success response or failure
     */
    suspend fun signUp(signUpRequest: ArcXPSignUpRequest) : Either<Any?, ArcXPUser?> =
        try {
            val response = identityService.signUp(
                signUpRequest)
            with(response) {
                when {
                    isSuccessful -> Success(body())
                    else -> Failure(ArcXPError(ArcXPCommerceSDKErrorType.SERVER_ERROR, response.message(), response))
                }
            }

        } catch (e: Exception) {
            Failure(ArcXPError(ArcXPCommerceSDKErrorType.SERVER_ERROR, e.message!!, e))
        }

    /**
     * function to make verification email request
     *
     * @param verifyEmailRequest request for verify email
     * @return Either success response or failure
     */
    suspend fun verifyEmail(verifyEmailRequest: ArcXPVerifyEmailRequest): Either<Any?, ArcXPEmailVerification?> =
        try {
            val response = identityService.verifyEmail(
                verifyEmailRequest)
            with(response) {
                when {
                    isSuccessful -> Success(body())
                    else -> Failure(ArcXPError(ArcXPCommerceSDKErrorType.SERVER_ERROR, response.message(), response))
                }
            }
        } catch (e: Exception) {
            Failure(ArcXPError(ArcXPCommerceSDKErrorType.SERVER_ERROR, e.message!!, e))
        }

    suspend fun verifyEmailNonce(nonce: String) : Either<Any?, ArcXPEmailVerification?> =
            try {
                val response = identityService.verifyEmail(nonce)
                with(response) {
                    when {
                        isSuccessful -> Success(body())
                        else -> Failure(ArcXPError(ArcXPCommerceSDKErrorType.SERVER_ERROR, response.message(), response))
                    }
                }
            } catch (e: Exception){
                Failure(ArcXPError(ArcXPCommerceSDKErrorType.SERVER_ERROR, e.message!!, e))
            }


    suspend fun logout(): Either<Any?, Void?> =
        try {
            val response = identityService.logout()
            with(response) {
                when {
                    isSuccessful -> Success(body())
                    else -> Failure(ArcXPError(ArcXPCommerceSDKErrorType.SERVER_ERROR, response.message(), response))
                }
            }
        } catch (e: Exception) {
            Failure(ArcXPError(ArcXPCommerceSDKErrorType.SERVER_ERROR, e.message!!, e))
        }

    /**
     * function to validate if the cached access token is still validated
     *
     * @return Either success response or failure
     */
    suspend fun validateJwt(): Either<Any?, ArcXPAuth?> =
        try {
            val response = identityService.recapToken(ArcXPAuthRequest())
            with(response) {
                when {
                    isSuccessful -> Success(body())
                    else -> Failure(ArcXPError(ArcXPCommerceSDKErrorType.INVALID_SESSION, response.message(), response))
                }
            }
        } catch (e: Exception) {
            Failure(ArcXPError(ArcXPCommerceSDKErrorType.SERVER_ERROR, e.message!!, e))
        }

    suspend fun validateJwt(token: String): Either<Any?, ArcXPAuth?> =
        try {
            val response = identityService.recapToken(
                ArcXPAuthRequest(token = token)
            )
            with(response) {
                when {
                    isSuccessful -> Success(body())
                    else -> Failure(ArcXPError(ArcXPCommerceSDKErrorType.INVALID_SESSION, response.message(), response))
                }
            }
        } catch (e: Exception) {
            Failure(ArcXPError(ArcXPCommerceSDKErrorType.SERVER_ERROR, e.message!!, e))
        }

    /**
     * function to extend the session using refresh token
     *
     * @return Either success response or failure
     */
    suspend fun refreshToken(token: String?, grantType: String): Either<Any?, ArcXPAuth?> =
        try {
            AuthManager?.getInstance()?.accessToken = null
            val response = identityService.recapToken(
                ArcXPAuthRequest(token = token, grantType = grantType)
            )
            with(response) {
                when {
                    isSuccessful -> Success(body())
                    else -> {
                        Failure(ArcXPError(ArcXPCommerceSDKErrorType.SERVER_ERROR, response.message(), response))
                    }
                }
            }
        } catch (e: Exception) {
            Failure(ArcXPError(ArcXPCommerceSDKErrorType.SERVER_ERROR, e.message!!, e))
        }

    suspend fun removeIdentity(grantType: String): Either<Any?, ArcXPUpdateUserStatus?> =
        try {
            val response = identityService.deleteIdentities(grantType)
            with(response) {
                when {
                    isSuccessful -> Success(body())
                    else -> Failure(
                        ArcXPError(
                            ArcXPCommerceSDKErrorType.SERVER_ERROR,
                            response.message(),
                            response
                        )
                    )
                }
            }
        } catch (e: Exception){
            Failure(ArcXPError(ArcXPCommerceSDKErrorType.SERVER_ERROR, e.message!!, e))
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
                    else -> Failure(ArcXPError(ArcXPCommerceSDKErrorType.SERVER_ERROR, response.message(), response))
                }
            }
        } catch (e: Exception) {
            Failure(ArcXPError(ArcXPCommerceSDKErrorType.SERVER_ERROR, e.message!!, e))
        }

    suspend fun approveDeletion(nonce: String): Either<Any?, ArcXPDeleteUser?> =
            try {
                val response = identityService.approveDeletion(nonce)
                with(response) {
                    when {
                        isSuccessful -> Success(body())
                        else -> com.arcxp.commerce.util.Failure(com.arcxp.commerce.util.ArcXPError(com.arcxp.commerce.ArcXPCommerceSDKErrorType.SERVER_ERROR, response.message(), response))
                    }
                }
            } catch (e: Exception) {
                Failure(ArcXPError(ArcXPCommerceSDKErrorType.SERVER_ERROR, e.message!!, e))
            }

    /**
     * function to get apple login auth url
     *
     * @return Either success response or failure
     */
    suspend fun appleAuthUrl(): Either<Any?, ResponseBody?> =
            try {
                val response = identityService.appleAuthUrl()
                with(response) {
                    when {
                        isSuccessful -> Success(body())
                        else -> Failure(ArcXPError(ArcXPCommerceSDKErrorType.SERVER_ERROR, response.message(), response))
                    }
                }
            } catch (e: Exception) {
                Failure(e)
            }
    /*
    For internal use only. Not meant for public
     */
    suspend fun appleAuthUrlUpdatedURL(): Either<Any?, ResponseBody?> =
            try {
                val response = identityServiceApple.appleAuthUrl()
                with(response) {
                    when {
                        isSuccessful -> Success(body())
                        else -> Failure(ArcXPError(ArcXPCommerceSDKErrorType.SERVER_ERROR, response.message(), response))
                    }
                }
            } catch (e: Exception) {
                Failure(e)
            }

    suspend fun getConfig(): Either<Any?, ArcXPConfig?> =
        try {
            val response = identityService.config()
            with (response) {
                when {
                    isSuccessful -> Success(body())
                    else -> Failure(ArcXPError(ArcXPCommerceSDKErrorType.CONFIG_ERROR, response.message(), response))
                }
            }
        } catch (e: Exception) {
            Failure(e)
        }
}