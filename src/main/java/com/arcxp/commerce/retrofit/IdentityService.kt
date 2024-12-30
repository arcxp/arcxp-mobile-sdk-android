package com.arcxp.commerce.retrofit

import androidx.annotation.Keep
import com.arcxp.commerce.extendedModels.ArcXPProfileManage
import com.arcxp.commerce.models.*
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

/**
 * IdentityService is a Retrofit service interface for handling identity-related API calls that require authentication.
 * It provides methods for user profile management, password reset, email verification, user session management, and user deletion.
 *
 * The interface defines the following operations:
 * - Email verification
 * - User logout
 * - Password reset and change
 * - User profile retrieval, update, and patch
 * - User deletion
 * - Configuration retrieval
 * - JWT validation
 *
 * Usage:
 * - Implement this interface using Retrofit to create an instance of the service.
 * - Call the provided methods to perform identity operations and handle the results through Retrofit's response handling.
 *
 * Example:
 *
 * val identityService = retrofit.create(IdentityService::class.java)
 * val response = identityService.getProfile()
 *
 * Note: Ensure that the Retrofit instance is properly configured before creating an instance of IdentityService.
 *
 * @method verifyEmail Verify the user's email with the provided nonce.
 * @method logout Log out the user.
 * @method resetPassword Request to reset the user's password with the provided email.
 * @method changePassword Request to change the user's password with the provided request data.
 * @method validateJwt Validate the JWT with the provided request data.
 * @method getProfile Retrieve the user's profile.
 * @method updateProfile Update the user's profile with the provided request data.
 * @method patchProfile Patch the user's profile with the provided request data.
 * @method deleteUser Request to delete the logged-in user.
 * @method approveDeletion Approve the user's deletion with the provided nonce.
 * @method config Retrieve the configuration settings.
 * @method deleteIdentities Delete identities with the provided grant type.
 */
interface IdentityService {

    //    This is used for demo only. Verification should go through email
    @Keep
    @GET("email/verify/{nonce}")
    suspend fun verifyEmail(@Path("nonce") nonce: String): Response<ArcXPEmailVerification>

    /**
     * Log out user
     */
    @Keep
    @DELETE("auth/token")
    suspend fun logout(): Response<Void>

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

    /**
     * Recap the user session if expired
     * @param authRequest recap request
     * @return User login response
     */
    @Keep
    @POST("auth/token")
    suspend fun validateJwt(@Body authRequest: ArcXPAuthRequest): Response<ArcXPAuth>

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
    @GET("config")
    suspend fun config(): Response<ArcXPConfig>

    @Keep
    @DELETE("identity/{grantType}")
    suspend fun deleteIdentities(@Path("grantType") grantType: String): Response<ArcXPUpdateUserStatus>
}
