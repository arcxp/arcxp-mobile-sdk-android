package com.arcxp.commerce.retrofit

import androidx.annotation.Keep
import com.arcxp.commerce.extendedModels.ArcXPProfileManage
import com.arcxp.commerce.models.*
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

/**
 * All service call for communication with Arc identity API
 * @suppress
 */
interface IdentityService {

    /**
     * User sign up identity
     * @param signUpRequest User data needed for sign up
     * @return Sign up Response
     */
    @Keep
    @POST("signup")
    suspend fun signUp(@Body signUpRequest: ArcXPSignUpRequest): Response<ArcXPUser>

    /**
     * Verify email before user could login. Once the response is success,
     * it allows 24 hours for email verify nonce to be triggered.
     * @param verifyEmailRequest verify email request
     * @return Email verification response
     */
    @Keep
    @POST("email/verify")
    suspend fun verifyEmail(@Body verifyEmailRequest: ArcXPVerifyEmailRequest): Response<ArcXPEmailVerification>

    //    This is used for demo only. Verification should go through email
    @Keep
    @GET("email/verify/{nonce}")
    suspend fun verifyEmail(@Path("nonce") nonce: String): Response<ArcXPEmailVerification>

    /**
     * Verify email before user could login
     * @param authRequest login request
     * @return User login response
     */
    @Keep
    @POST("auth/login")
    suspend fun login(@Body authRequest: ArcXPAuthRequest): Response<ArcXPAuth>

    /**
     * Log out user
     */
    @Keep
    @DELETE("auth/token")
    suspend fun logout(): Response<Void>

    /**
     * Recap the user session if expired
     * @param authRequest recap request
     * @return User login response
     */
    @Keep
    @POST("auth/token")
    suspend fun recapToken(@Body authRequest: ArcXPAuthRequest): Response<ArcXPAuth>

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

    /**
     * Request to reset password with user email
     * @param passwordResetRequest password reset request
     * @return Password reset response
     */
    @Keep
    @POST("password/reset")
    suspend fun resetPassword(@Body passwordResetRequest: ArcXPResetPasswordRequestRequest): Response<ArcXPRequestPasswordReset>

    /**
     * Request to reset password with logged in user
     * @param passwordChangeRequest password reset request
     * @return Identity response
     */
    @Keep
    @PUT("password")
    suspend fun changePassword(@Body passwordChangeRequest: ArcXPPasswordResetRequest): Response<ArcXPIdentity>

    //    This is used for demo only. Reset should go through email
    @Keep
    @PUT("password/reset/{nonce}")
    suspend fun resetPassword(
        @Path("nonce") nonce: String,
        @Body resetPasswordNonceRequest: ArcXPResetPasswordNonceRequest
    ): Response<ArcXPIdentity>

    /**
     * Request to obtain user profile
     * @return User profile
     */
    @Keep
    @GET("profile")
    suspend fun getProfile(): Response<ArcXPProfileManage>

    @Keep
    @PATCH("profile")
    suspend fun setProfileAttribute(@Body arcXPProfileAttributePatchRequest: ArcXPProfileAttributePatchRequest): Response<String>

    /**
     * Request to update user profile, all previous user data will be replaced
     * @param profileRequest User profile will be updated
     * @return Updated user profile
     */
    @Keep
    @PUT("profile")
    suspend fun updateProfile(@Body profileRequest: ArcXPProfileRequest): Response<ArcXPProfileManage>

    /**
     * Request to patch user profile, only revised data will be replaced
     * @param profilePatchRequest User profile will be patched
     * @return Updated user profile
     */
    @Keep
    @PATCH("profile")
    suspend fun patchProfile(@Body profilePatchRequest: ArcXPProfilePatchRequest): Response<ArcXPProfileManage>

    /**
     * Request to delete logged in user, the request would reject
     * if user has active subscriptions, once the request approve,
     * it allows 24 hours for deletion nonce to be triggered.
     *
     * @return Response of user deletion approval
     */
    @Keep
    @DELETE("user/anonymize")
    suspend fun deleteUser(): Response<ArcXPAnonymizeUser>

    //    This is used for demo only. Deletion confirm should go through other platform
    @Keep
    @PUT("user/anonymize/approve/{nonce}")
    suspend fun approveDeletion(@Path("nonce") nonce: String, @Body body: Any = Object()): Response<ArcXPDeleteUser>

    @Keep
    @GET("apple/authurl")
    suspend fun appleAuthUrl(): Response<ResponseBody>

    @Keep
    @GET("config")
    suspend fun config(): Response<ArcXPConfig>

    @Keep
    @DELETE("identity/{grantType}")
    suspend fun deleteIdentities(@Path("grantType") grantType: String): Response<ArcXPUpdateUserStatus>
}
