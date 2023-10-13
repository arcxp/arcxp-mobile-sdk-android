package com.arcxp.commerce.retrofit

import androidx.annotation.Keep
import com.arcxp.commerce.models.*
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

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