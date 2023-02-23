package com.arcxp.commerce.viewmodels

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.arcxp.commerce.ArcXPCommerceSDKErrorType
import com.arcxp.commerce.apimanagers.ArcXPIdentityListener
import com.arcxp.commerce.extendedModels.ArcXPProfileManage
import com.arcxp.commerce.models.*
import com.arcxp.commerce.repositories.IdentityRepository
import com.arcxp.commerce.util.*
import com.arcxp.commerce.util.ArcXPError
import com.arcxp.commons.util.DependencyFactory.createError
import com.arcxp.commons.util.Failure
import com.arcxp.commons.util.Success
import com.google.android.gms.safetynet.SafetyNet
import kotlinx.coroutines.*

/**
 * @suppress
 */

class IdentityViewModel(
    private val authManager: AuthManager,
    private val repo: IdentityRepository
) : BaseAuthViewModel() {

    private val _errorResponse = MutableLiveData<ArcXPError>()
    val errorResponse: LiveData<ArcXPError> = _errorResponse

    private val _changePasswordResponse = MutableLiveData<ArcXPIdentity>()
    val changePasswordResponse: LiveData<ArcXPIdentity> = _changePasswordResponse

    private val _changePasswordError = MutableLiveData<ArcXPError>()
    val changePasswordError: LiveData<ArcXPError> = _changePasswordError

    private val _loginErrorResponse = MutableLiveData<ArcXPError>()
    val loginErrorResponse: LiveData<ArcXPError> = _loginErrorResponse

    private val _requestPasswordResetResponse = MutableLiveData<ArcXPRequestPasswordReset>()
    val requestPasswordResetResponse: LiveData<ArcXPRequestPasswordReset> =
        _requestPasswordResetResponse

    private val _passwordResetErrorResponse = MutableLiveData<ArcXPError>()
    val passwordResetErrorResponse: LiveData<ArcXPError> = _passwordResetErrorResponse

    private val _authResponse = MutableLiveData<ArcXPAuth>()
    val authResponse: LiveData<ArcXPAuth> = _authResponse

    private val _emailVerificationResponse = MutableLiveData<ArcXPEmailVerification>()
    val emailVerificationResponse: LiveData<ArcXPEmailVerification> = _emailVerificationResponse

    private val _emailVerificationErrorResponse = MutableLiveData<ArcXPError>()
    val emailVerificationErrorResponse: LiveData<ArcXPError> = _emailVerificationErrorResponse

    private val _magicLinkAuthResponse = MutableLiveData<ArcXPOneTimeAccessLinkAuth>()
    val oneTimeAccessLinkAuthResponse: LiveData<ArcXPOneTimeAccessLinkAuth> = _magicLinkAuthResponse

    private val _magicLinkErrorResponse = MutableLiveData<ArcXPError>()
    val magicLinkErrorResponse: LiveData<ArcXPError> = _magicLinkErrorResponse

    private val _profileResponse = MutableLiveData<ArcXPProfileManage>()
    val profileResponse: LiveData<ArcXPProfileManage> = _profileResponse

    private val _updateUserStatusResponse = MutableLiveData<ArcXPUpdateUserStatus>()
    val updateUserStatusResponse: LiveData<ArcXPUpdateUserStatus> = _updateUserStatusResponse

    private val _updateUserStatusFailureResponse = MutableLiveData<ArcXPError>()
    val updateUserStatusFailureResponse: LiveData<ArcXPError> = _updateUserStatusFailureResponse

    private val _profileErrorResponse = MutableLiveData<ArcXPError>()
    val profileErrorResponse: LiveData<ArcXPError> = _profileErrorResponse

    private val _registrationResponse = MutableLiveData<ArcXPUser>()
    val registrationResponse: LiveData<ArcXPUser> = _registrationResponse

    private val _registrationError = MutableLiveData<ArcXPError>()
    val registrationError: LiveData<ArcXPError> = _registrationError

    private val _logoutResponse = MutableLiveData<Boolean>()
    val logoutResponse: LiveData<Boolean> = _logoutResponse

    private val _logoutErrorResponse = MutableLiveData<ArcXPError>()
    val logoutErrorResponse: LiveData<ArcXPError> = _logoutErrorResponse

    private val _deletionResponse = MutableLiveData<Boolean>()
    val deletionResponse: LiveData<Boolean> = _deletionResponse

    private val _deletionErrorResponse = MutableLiveData<ArcXPError>()
    val deletionErrorResponse: LiveData<ArcXPError> = _deletionErrorResponse

//    private val _appleAuthUrl = MutableLiveData<String>()
//    val appleAuthUrl: LiveData<String> = _appleAuthUrl

    var nonce: String? = null
    var recaptchaToken: String? = null

    /**
     * Function to reset password by the given username
     *
     * @param oldPassword old password
     * @param newPassword new password
     */
    fun changeUserPassword(
        oldPassword: String,
        newPassword: String,
        callback: ArcXPIdentityListener?
    ) {
        mIoScope.launch {
            val res = repo.changePassword(
                ArcXPPasswordResetRequest(
                    oldPassword, newPassword
                )
            )

            withContext(mUiScope.coroutineContext) {
                when (res) {
                    is Success -> {
                        if (callback == null) {
                            _changePasswordResponse.value = res.success!!
                        } else {
                            callback.onPasswordChangeSuccess(res.success!!)
                        }
                    }
                    is Failure -> {
                        if (callback == null) {
                            _changePasswordError.value = res.failure as ArcXPError
                        } else {
                            callback.onPasswordChangeError(res.failure as ArcXPError)
                        }
                    }
                }
            }
        }
    }

    /**
     * Function to reset password by the given username
     *
     * @param userName user name for password reset
     */
    fun obtainNonceByEmailAddress(userName: String, callback: ArcXPIdentityListener?) {
        mIoScope.launch {
            val res = repo.resetPassword(
                ArcXPResetPasswordRequestRequest(userName)
            )
            withContext(mUiScope.coroutineContext) {
                when (res) {
                    is Success -> {
                        if (callback == null) {
                            _requestPasswordResetResponse.value = res.success!!
                        } else {
                            callback.onPasswordResetNonceSuccess(res.success!!)
                        }
                    }
                    is Failure -> {
                        if (callback == null) {
                            _passwordResetErrorResponse.value = res.failure as ArcXPError
                        } else {
                            callback.onPasswordResetNonceFailure(res.failure as ArcXPError)
                        }
                    }
                }
            }
        }
    }

    /**
     * Function to reset password by the given username
     *
     * @param userName user name for password reset
     */
    fun resetPasswordByNonce(nonce: String, newPassword: String, callback: ArcXPIdentityListener?) {
        mIoScope.launch {
            val res = repo.resetPassword(
                nonce,
                ArcXPResetPasswordNonceRequest(newPassword)
            )

            withContext(mUiScope.coroutineContext) {
                when (res) {
                    is Success -> {
                        if (callback == null) {
                            _requestPasswordResetResponse.value = ArcXPRequestPasswordReset(true)
                        } else {
                            callback.onPasswordResetSuccess(res.success!!)
                        }
                    }
                    is Failure -> {
                        if (callback == null) {
                            _passwordResetErrorResponse.value = res.failure as ArcXPError
                        } else {
                            callback.onPasswordResetError(res.failure as ArcXPError)
                        }
                    }
                }
            }
        }
    }

    /**
     * Function to make login call
     *
     * @param userName user name for login
     * @param password password for login
     */
    fun makeLoginCall(
        userName: String, password: String,
        recaptchaToken: String? = null,
        callback: ArcXPIdentityListener?
    ) {
        mIoScope.launch {
            val res = repo.login(
                ArcXPAuthRequest(
                    userName = userName,
                    credentials = password,
                    grantType = ArcXPAuthRequest.Companion.GrantType.PASSWORD.value,
                    recaptchaToken = recaptchaToken
                )
            )

            withContext(mUiScope.coroutineContext) {
                when (res) {
                    is Success -> {
                        if (callback == null) {
                            _authResponse.value = res.success!!
                        } else {
                            callback.onLoginSuccess(res.success!!)
                        }
                        cacheSession(res.success!!)
                    }
                    is Failure -> {
                        if (callback == null) {
                            _loginErrorResponse.value = res.failure as ArcXPError
                        } else {
                            callback.onLoginError(res.failure as ArcXPError)
                        }
                    }
                }
            }
        }
    }

    /**
     * Commenting out both Apple functions below due to backend not allowing for multiple credentials.
     * Once the backend has it setup or another solution for allow both iOS and Android platforms to use
     * the same backend, these functions can be reimplemented into the code.
     */

//    /**
//     * Function to obtain apple authentication url
//     *
//     */
//    fun appleAuthUrl(callback: ArcXPIdentityListener?) {
//        mIoScope.launch {
//            val res = repo.appleAuthUrl()
//            withContext(mUiScope.coroutineContext) {
//                when (res) {
//                    is Success -> {
//                        if (callback == null) {
//                            _appleAuthUrl.value = res.success?.string()?.replace("\"", "")
//                        } else {
//                            res.success?.string()?.replace("\"", "")?.let { callback?.onAppleAuthUrlObtained(it) }
//                        }
//                    }
//                    is Failure ->
//                    {
//                        try {
//                            _errorResponse.value = res.failure as ArcXPError
//                        } catch (e: Exception) {
//                            _errorResponse.value = ArcXPError(ArcXPCommerceSDKErrorType.APPLE_CONFIG_ERROR, "Error", res.failure)
//                        }
//                    }
//                }
//            }
//        }
//    }
//    /*
//    For internal use only. Not meant for public
//     */
//
//    fun appleAuthUrlUpdatedURL(callback: ArcXPIdentityListener?) {
//        mIoScope.launch {
//            val res = repo.appleAuthUrlUpdatedURL()
//            withContext(mUiScope.coroutineContext) {
//                when (res) {
//                    is Success -> {
//                        if (callback == null) {
//                            _appleAuthUrl.value = res.success?.string()?.replace("\"", "")
//                        } else {
//                            res.success?.string()?.replace("\"", "")?.let { callback?.onAppleAuthUrlObtained(it) }
//                        }
//                    }
//                    is Failure ->
//                    {
//                        try {
//                            _errorResponse.value = res.failure as ArcXPError
//                        } catch (e: Exception) {
//                            _errorResponse.value = ArcXPError(ArcXPCommerceSDKErrorType.APPLE_CONFIG_ERROR, "Error", res.failure)
//                        }
//                    }
//                }
//            }
//        }
//    }

    /**
     * Function to make third party login call
     *
     * @param accessToken accessToken from third party
     * @param grantType grandType of third party authentication, options:
     * [ArcXPAuthRequest.Companion.GrantType.FACEBOOK],
     * [ArcXPAuthRequest.Companion.GrantType.GOOGLE],
     * [ArcXPAuthRequest.Companion.GrantType.APPLE]
     */
    fun thirdPartyLoginCall(
        accessToken: String,
        grantType: ArcXPAuthRequest.Companion.GrantType,
        callback: ArcXPIdentityListener?
    ) {
        mIoScope.launch {
            //Delete IF statement during production (only for testing), keep only ELSE
            if (grantType.value == "apple") {
                val res = repo.appleLogin(
                    ArcXPAuthRequest(
                        userName = "",
                        credentials = accessToken,
                        grantType = grantType.value
                    )
                )
                withContext(mUiScope.coroutineContext) {
                    when (res) {
                        is Success -> {
                            if (callback == null) {
                                _authResponse.value = res.success!!
                            } else {
                                callback.onLoginSuccess(res.success!!)
                            }
                            cacheSession(res.success!!)
                        }
                        is Failure -> {
                            if (callback == null) {
                                _errorResponse.value = res.failure as ArcXPError
                            } else {
                                callback.onLoginError(res.failure as ArcXPError)
                            }
                        }
                    }
                }
            } else {
                val res = repo.login(
                    ArcXPAuthRequest(
                        userName = "",
                        credentials = accessToken,
                        grantType = grantType.value
                    )
                )
                withContext(mUiScope.coroutineContext) {
                    when (res) {
                        is Success -> {
                            if (res.success!!.uuid == AuthManager.getInstance().uuid || AuthManager.getInstance().uuid == null) {
                                if (callback == null) {
                                    _authResponse.value = res.success!!
                                } else {
                                    callback.onLoginSuccess(res.success!!)
                                }
                                cacheSession(res.success)
                            } else {
                                callback?.onLoginError(ArcXPError("Account already linked to another account"))
                            }
                        }

                        is Failure -> {
                            if (callback == null) {
                                _errorResponse.value = res.failure as ArcXPError
                            } else {
                                callback.onLoginError(res.failure as ArcXPError)
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Function to verify email
     *
     * @param email registered email
     */
    fun verifyEmailCall(email: String, callback: ArcXPIdentityListener?) {
        mIoScope.launch {
            val res = repo.verifyEmail(
                ArcXPVerifyEmailRequest(email)
            )

            withContext(mUiScope.coroutineContext) {
                when (res) {
                    is Success -> {
                        if (callback == null) {
                            _emailVerificationResponse.value = res.success!!
                        } else {
                            callback.onEmailVerificationSentSuccess(res.success!!)
                        }
                    }
                    is Failure -> {
                        if (callback == null) {
                            _emailVerificationErrorResponse.value = res.failure as ArcXPError
                        } else {
                            callback.onEmailVerificationSentError(res.failure as ArcXPError)
                        }
                    }
                }
            }
        }
    }

    /**
     * Function to verify email nonce
     * @param nonce
     */
    fun verifyEmail(nonce: String, callback: ArcXPIdentityListener?) {
        mIoScope.launch {
            val res = repo.verifyEmailNonce(nonce)

            withContext(mUiScope.coroutineContext) {
                when (res) {
                    is Success -> {
                        if (callback == null) {
                            _emailVerificationResponse.value = res.success!!
                        } else {
                            callback.onEmailVerifiedSuccess(res.success!!)
                        }
                    }
                    is Failure -> {
                        if (callback == null) {
                            _emailVerificationErrorResponse.value = res.failure as ArcXPError
                        } else {
                            callback.onEmailVerifiedError(res.failure as ArcXPError)
                        }
                    }
                }
            }
        }
    }

    /**
     * Function to cache the current session
     *
     * @param response user auth info needed to be cache
     */
    private fun cacheSession(response: ArcXPAuth) {
        authManager.cacheSession(response)
    }

    fun getMagicLink(
        email: String,
        recaptchaToken: String?,
        callback: ArcXPIdentityListener? = null
    ) {
        mIoScope.launch {
            val res = repo.getMagicLink(ArcXPOneTimeAccessLinkRequest(email, recaptchaToken))
            withContext(mUiScope.coroutineContext) {
                when (res) {
                    is Success -> {
                        if (callback == null) {
                            Success(res.success)
                        } else {
                            callback.onOneTimeAccessLinkSuccess(res.success!!)
                        }
                    }
                    is Failure -> {
                        if (callback == null) {
                            _magicLinkErrorResponse.value = res.failure as ArcXPError
                        } else {
                            callback.onOneTimeAccessLinkError(res.failure as ArcXPError)
                        }
                    }
                }
            }
        }
    }

    fun loginMagicLink(nonce: String, callback: ArcXPIdentityListener?) {
        mIoScope.launch {
            val res = repo.loginMagicLink(nonce)

            withContext(mUiScope.coroutineContext) {
                when (res) {
                    is Success -> {
                        if (callback == null) {
                            _magicLinkAuthResponse.value = res.success!!
                        } else {
                            callback.onOneTimeAccessLinkLoginSuccess(res.success!!)
                        }
                        authManager.cacheSession(res.success!!)
                    }
                    is Failure -> {
                        if (callback == null) {
                            _magicLinkErrorResponse.value = res.failure as ArcXPError
                        } else {
                            callback.onOneTimeAccessLinkError(res.failure as ArcXPError)
                        }
                    }
                }
            }
        }
    }

    /**
     * Function to patch profile
     *
     * @param profilePatchRequest request to patch profile
     */
    fun patchProfile(
        profilePatchRequest: ArcXPProfilePatchRequest,
        callback: ArcXPIdentityListener?
    ) {
        mIoScope.launch {
            val res = repo.patchProfile(profilePatchRequest)
            withContext(mUiScope.coroutineContext) {
                when (res) {
                    is Success -> {
                        if (callback == null) {
                            _profileResponse.value = res.success!!
                        } else {
                            callback.onProfileUpdateSuccess(res.success!!)
                        }
                    }
                    is Failure -> {
                        if (callback == null) {
                            _profileErrorResponse.value = res.failure as ArcXPError
                        } else {
                            callback.onProfileError(res.failure as ArcXPError)
                        }
                    }
                }
            }
        }
    }

    /**
     * Function to load profile
     */
    fun getProfile(callback: ArcXPIdentityListener?) {
        mIoScope.launch {
            val res = repo.getProfile()
            withContext(mUiScope.coroutineContext) {
                when (res) {
                    is Success -> {
                        if (callback == null) {
                            _profileResponse.value = res.success!!
                        } else {
                            callback.onFetchProfileSuccess(res.success!!)
                        }
                    }
                    is Failure -> {
                        if (callback == null) {
                            _profileErrorResponse.value = res.failure as ArcXPError
                        } else {
                            callback.onProfileError(res.failure as ArcXPError)
                        }
                    }
                }
            }
        }
    }

    /**
     * Function to make registration request
     *
     * @param userName user name
     * @param password user password
     * @param email user email
     * @param firstName user first name
     * @param lastName user last name
     */
    fun makeRegistrationCall(
        userName: String,
        password: String,
        email: String,
        firstName: String? = null,
        lastName: String? = null,
        callback: ArcXPIdentityListener?
    ) {
        mIoScope.launch {
            val res = repo.signUp(
                ArcXPSignUpRequest(
                    ArcXPIdentityRequest(
                        userName = userName,
                        credentials = password,
                        grantType = ArcXPIdentityRequest.Companion.GrantType.password.name
                    ),
                    ArcXPProfileRequest(
                        email = email,
                        firstName = firstName,
                        lastName = lastName
                    )
                )
            )

            withContext(mUiScope.coroutineContext) {
                when (res) {
                    is Success -> {
                        if (callback == null) {
                            _registrationResponse.value = res.success!!
                        } else {
                            callback.onRegistrationSuccess(res.success!!)
                        }
                    }
                    is Failure -> {
                        if (callback == null) {
                            _registrationError.value = res.failure as ArcXPError
                        } else {
                            callback.onRegistrationError(res.failure as ArcXPError)
                        }
                    }
                }
            }
        }
    }

    /**
     * Function to make log out call, delete cached user session either success or failure
     */
    fun logout(callback: ArcXPIdentityListener?) {
        mIoScope.launch {
            val res = repo.logout()

            withContext(mUiScope.coroutineContext) {
                when (res) {
                    is Success -> {
                        authManager.deleteSession()
                        if (callback == null) {
                            _logoutResponse.value = true
                        } else {
                            callback.onLogoutSuccess()
                        }
                    }
                    is Failure -> {
                        authManager.deleteSession()
                        if (callback == null) {
                            _logoutErrorResponse.value = res.failure as ArcXPError
                        } else {
                            callback.onLogoutError(res.failure as ArcXPError)
                        }
                    }
                }
            }
        }

    }

    fun removeIdentity(grantType: String, callback: ArcXPIdentityListener?) {
        mIoScope.launch {
            val res = repo.removeIdentity(
                grantType
            )
            withContext(mUiScope.coroutineContext) {
                when (res) {
                    is Success -> {
                        if (callback == null) {
                            _updateUserStatusResponse.value = res.success!!
                        } else {
                            callback.onRemoveIdentitySuccess(res.success!!)
                        }
                    }
                    is Failure -> {
                        if (callback == null) {
                            _updateUserStatusFailureResponse.value = res.failure as ArcXPError
                        } else {
                            callback.onRemoveIdentityFailure(res.failure as ArcXPError)
                        }
                    }
                }
            }
        }
    }

    /**
     * Function to request current user deletion
     */
    fun deleteUser(callback: ArcXPIdentityListener?) {
        mIoScope.launch {
            val res = repo.deleteUser()

            withContext(mUiScope.coroutineContext) {
                when (res) {
                    is Success -> {
                        handleDeletionResponse(res.success, callback)
                    }
                    is Failure -> {
                        handleDeletionFailure(res.failure, callback)
                    }
                }
            }
        }
    }

    fun approveDeletion(nonce: String, listener: ArcXPIdentityListener) {
        mIoScope.launch {
            val res = repo.approveDeletion(nonce)

            withContext(mUiScope.coroutineContext) {
                when (res) {
                    is Success -> {
                        listener.onApproveDeletionSuccess(res.success!!)
                    }
                    is Failure -> {
                        listener.onApproveDeletionError(res.failure as ArcXPError)
                    }
                }
            }
        }
    }

    fun validateJwt(token: String, callback: ArcXPIdentityListener?) {
        mIoScope.launch {
            val res = repo.validateJwt(token)
            withContext(mUiScope.coroutineContext) {
                when (res) {
                    is Success -> {
                        callback?.onValidateSessionSuccess() ?: Success(res.success)
                    }
                    is Failure -> {
                        callback?.onValidateSessionError(res.failure as ArcXPError) ?: Failure(res.failure)
                    }

                }
            }
        }
    }

    fun validateJwt(callback: ArcXPIdentityListener?) {
        mIoScope.launch {
            val res = repo.validateJwt()
            withContext(mUiScope.coroutineContext) {
                when (res) {
                    is Success -> {
                        callback?.onValidateSessionSuccess() ?: Success(res.success)
                    }
                    is Failure -> {
                        callback?.onValidateSessionError(res.failure as ArcXPError) ?: Failure(res.failure)
                    }

                }
            }
        }
    }

    fun refreshToken(token: String?, grantType: String, callback: ArcXPIdentityListener?) {
        mIoScope.launch {
            val res = repo.refreshToken(token, grantType)
            withContext(mUiScope.coroutineContext) {
                when (res) {
                    is Success -> {
                        cacheSession(res.success!!)
                        callback?.onRefreshSessionSuccess(res.success!!) ?: Success(res.success)
                    }
                    is Failure -> {
                        callback?.onRefreshSessionFailure(res.failure as ArcXPError) ?: Failure(res.failure)
                    }
                }
            }
        }
    }

    fun getTenetConfig(callback: ArcXPIdentityListener) {
        mIoScope.launch {
            val res = repo.getConfig()
            withContext(mUiScope.coroutineContext) {
                when (res) {
                    is Success -> {
                        callback.onLoadConfigSuccess(res.success!!)
                    }
                    is Failure -> {
                        if (res.failure is Exception) {
                            callback.onLoadConfigFailure(
                                createError(
                                    type = ArcXPCommerceSDKErrorType.CONFIG_ERROR,
                                    message = res.failure.message, value = res.failure
                                )
                            )
                        } else {
                            callback.onLoadConfigFailure(res.failure as ArcXPError)
                        }
                    }
                }
            }
        }
    }

    private fun handleDeletionResponse(
        response: ArcXPAnonymizeUser?,
        callback: ArcXPIdentityListener?
    ) {
        with(response) {
            if (this!!.valid) {
                if (callback == null) {
                    _deletionResponse.value = true
                } else {
                    callback.onDeleteUserSuccess()
                }
            } else handleDeletionFailure(ArcXPError("Your account deletion request is declined."), callback)
        }
    }

    private fun handleDeletionFailure(error: Any?, callback: ArcXPIdentityListener?) {
        if (callback == null) {
            _deletionErrorResponse.value = error as ArcXPError
        } else {
            callback.onDeleteUserError(error as ArcXPError)
        }
    }

    fun checkRecaptcha(context: Context, siteKey: String, callback: ArcXPIdentityListener?) {

        SafetyNet.getClient(context)
            .verifyWithRecaptcha(siteKey)
            .addOnSuccessListener {
                it.tokenResult?.let { token -> callback?.onRecaptchaSuccess(token) }
            }.addOnFailureListener {
                callback?.onRecaptchaFailure(
                    createError(
                        ArcXPCommerceSDKErrorType.RECAPTCHA_ERROR,
                        it.localizedMessage,
                        it
                    )
                )
            }.addOnCanceledListener {
                callback?.onRecaptchaCancel()
            }

    }

    fun rememberUser(remember: Boolean) {
        authManager.setShouldRememberUser(remember)
    }
}