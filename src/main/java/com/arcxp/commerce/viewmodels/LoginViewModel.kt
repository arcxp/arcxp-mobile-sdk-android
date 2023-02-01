package com.arcxp.commerce.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.arcxp.commerce.models.ArcXPAuthRequest
import com.arcxp.commerce.models.ArcXPAuth
import com.arcxp.commerce.models.ArcXPEmailVerification
import com.arcxp.commerce.models.ArcXPVerifyEmailRequest
import com.arcxp.commerce.repositories.LoginRepository
import com.arcxp.commerce.util.*
import kotlinx.coroutines.*

/**
 * View model for login screen
 *
 * @param repo Login repository [LoginRepository]
 * @suppress
 */
public class LoginViewModel(
    private val repo: LoginRepository,
    mainDispatcher: CoroutineDispatcher = Dispatchers.Main,
    ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : BaseAuthViewModel(mainDispatcher, ioDispatcher) {

    private val _authResponse = MutableLiveData<ArcXPAuth>()
    val authResponse: LiveData<ArcXPAuth> = _authResponse

    private val _verificationResponse = MutableLiveData<ArcXPEmailVerification>()
    val verificationResponse: LiveData<ArcXPEmailVerification> = _verificationResponse

    private val _appleAuthUrl = MutableLiveData<String>()
    val appleAuthUrl: LiveData<String> = _appleAuthUrl

    private val _errorResponse = MutableLiveData<ArcXPError>()
    val errorResponse: LiveData<ArcXPError> = _errorResponse

    var shouldRememberUser = true
    var recaptchaToken: String? = null

    /**
     * Function to make login call
     *
     * @param userName user name for login
     * @param password password for login
     */
    fun makeLoginCall(userName: String, password: String, recaptchaToken: String? = null) {
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
                    is Success -> cacheSession(res.r)
                    is Failure -> _errorResponse.value = res.l as ArcXPError
                }
            }
        }
    }

    /**
     * Function to make third party login call
     *
     * @param accessToken accessToken from third party
     * @param grantType grandType of third party authentication, options:
     * [ArcXPAuthRequest.Companion.GrantType.FACEBOOK],
     * [ArcXPAuthRequest.Companion.GrantType.GOOGLE],
     * [ArcXPAuthRequest.Companion.GrantType.APPLE]
     */
    fun thirdPartyLoginCall(accessToken: String, grantType: ArcXPAuthRequest.Companion.GrantType) {
        mIoScope.launch {
            val res = repo.login(
                ArcXPAuthRequest(
                    userName = "",
                    credentials = accessToken,
                    grantType = grantType.value
                )
            )

            withContext(mUiScope.coroutineContext) {
                when (res) {
                    is Success -> cacheSession(res.r)
                    is Failure -> _errorResponse.value = res.l as ArcXPError
                }
            }
        }
    }

    /**
     * Function to verify email
     *
     * @param email registered email
     */
    fun verifyEmailCall(email: String) {
        mIoScope.launch {
            val res = repo.verifyEmail(
                ArcXPVerifyEmailRequest(email)
            )

            withContext(mUiScope.coroutineContext) {
                when (res) {
                    is Success -> _verificationResponse.value = res.r!!
                    is Failure -> _errorResponse.value = res.l as ArcXPError
                }
            }
        }
    }

    /**
     * Function to obtain apple authentication url
     *
     */
    fun appleAuthUrl() {
        mIoScope.launch {
            val res = repo.appleAuthUrl()

            withContext(mUiScope.coroutineContext) {
                when (res) {
                    is Success -> _appleAuthUrl.value = res.r?.string()?.replace("\"", "")
                    is Failure -> _errorResponse.value = res.l as ArcXPError
                }
            }
        }
    }

    /**
     * Function to cache the current session
     *
     * @param response user auth info needed to be cache
     */
    private fun cacheSession(response: ArcXPAuth?) {
        _authResponse.value = response!!
        response?.let {
            AuthManager.getInstance().cacheSession(it)
        }
        AuthManager.getInstance().setShouldRememberUser(shouldRememberUser)
    }

}
