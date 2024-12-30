package com.arcxp.commerce.retrofit

import androidx.annotation.Keep
import com.arcxp.commerce.models.*
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

/**
 * IdentityServiceNoAuth is a Retrofit service interface for handling identity-related API calls that do not require authentication.
 * It provides methods for user sign-up, login, email verification, token refresh, and magic link operations.
 *
 * The interface defines the following operations:
 * - User sign-up
 * - User login
 * - Token refresh
 * - Magic link generation and login
 * - Email verification
 * - Apple authentication URL retrieval
 *
 * Usage:
 * - Implement this interface using Retrofit to create an instance of the service.
 * - Call the provided methods to perform identity operations and handle the results through Retrofit's response handling.
 *
 * Example:
 *
 * val identityService = retrofit.create(IdentityServiceNoAuth::class.java)
 * val response = identityService.signUp(signUpRequest)
 *
 * Note: Ensure that the Retrofit instance is properly configured before creating an instance of IdentityServiceNoAuth.
 *
 * @method signUp User sign-up with the provided sign-up request data.
 * @method login User login with the provided authentication request data.
 * @method refreshToken Refresh the authentication token with the provided request data.
 * @method getMagicLink Generate a magic link with the provided one-time access link request data.
 * @method loginMagicLink Login using the provided magic link nonce.
 * @method appleAuthUrl Retrieve the Apple authentication URL.
 * @method verifyEmail Verify the user's email with the provided verification request data.
 */
interface IdentityServiceNoAuth {

    /**
     * User sign up identity
     * @param signUpRequest User data needed for sign up
     * @return Sign up Response
     */
    @Keep
    @POST("signup")
    suspend fun signUp(@Body signUpRequest: ArcXPSignUpRequest): Response<ArcXPUser>

    /**
     * Verify email before user could login
     * @param authRequest login request
     * @return User login response
     */
    @Keep
    @POST("auth/login")
    suspend fun login(@Body authRequest: ArcXPAuthRequest): Response<ArcXPAuth>

    @Keep
    @POST("auth/token")
    suspend fun refreshToken(@Body authRequest: ArcXPAuthRequest): Response<ArcXPAuth>

    /**
     * Gets a one time access nonce
     * @param oneTimeAccessLinkRequest Request containing the email and recaptcha token
     * @return Success or failure
     */
    @Keep
    @POST("auth/magiclink")
    suspend fun getMagicLink(@Body oneTimeAccessLinkRequest: ArcXPOneTimeAccessLinkRequest): Response<ArcXPOneTimeAccessLink>

    /**
     * Use the one time access nonce to login
     * @param nonce String value
     * @return Response containing a uuid and access token
     */
    @Keep
    @GET("auth/magiclink/{nonce}")
    suspend fun loginMagicLink(@Path("nonce") nonce: String): Response<ArcXPOneTimeAccessLinkAuth>
    @Keep
    @GET("apple/authurl")
    suspend fun appleAuthUrl(): Response<ResponseBody>

    /**
     * Verify email before user could login. Once the response is success,
     * it allows 24 hours for email verify nonce to be triggered.
     * @param verifyEmailRequest verify email request
     * @return Email verification response
     */
    @Keep
    @POST("email/verify")
    suspend fun verifyEmail(@Body verifyEmailRequest: ArcXPVerifyEmailRequest): Response<ArcXPEmailVerification>


}