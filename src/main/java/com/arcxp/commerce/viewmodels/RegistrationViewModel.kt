package com.arcxp.commerce.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.arcxp.commerce.models.*
import com.arcxp.commerce.repositories.RegistrationRepository
import com.arcxp.commerce.util.ArcXPError
import com.arcxp.commerce.util.Failure
import com.arcxp.commerce.util.Success
import kotlinx.coroutines.*

/**
 * View model for registration screen
 *
 * @param repo registration repository [RegistrationRepository]
 * @suppress
 */
class RegistrationViewModel(
    private val repo: RegistrationRepository,
    mainDispatcher: CoroutineDispatcher = Dispatchers.Main,
    ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : BaseAuthViewModel(mainDispatcher, ioDispatcher) {
    private val _userResponse = MutableLiveData<ArcXPUser>()
    val userResponse: LiveData<ArcXPUser> = _userResponse

    private val _verificationResponse = MutableLiveData<ArcXPEmailVerification>()
    val verificationResponse: LiveData<ArcXPEmailVerification> = _verificationResponse

    private val _errorResponse = MutableLiveData<ArcXPError>()
    val errorResponse: LiveData<ArcXPError> = _errorResponse

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
        lastName: String? = null
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
                    is Success -> _userResponse.value = res.r
                    is Failure -> _errorResponse.value = res.l as ArcXPError
                }
            }
        }
    }

    /**
     * Function to make email verification call
     *
     * @param email user email
     */
    fun verifyEmailCall(email: String) {
        mIoScope.launch {
            val res = repo.verifyEmail(
                ArcXPVerifyEmailRequest(email)
            )

            withContext(mUiScope.coroutineContext) {
                when (res) {
                    is Success -> _verificationResponse.value = res.r
                    is Failure -> _errorResponse.value =  res.l as ArcXPError /*handleFailure(res.l)*/
                }
            }
        }
    }
}
