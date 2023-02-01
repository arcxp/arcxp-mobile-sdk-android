package com.arcxp.commerce.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.arcxp.commerce.models.ArcXPRequestPasswordReset
import com.arcxp.commerce.models.ArcXPResetPasswordRequestRequest
import com.arcxp.commerce.repositories.ForgotPasswordRepository
import com.arcxp.commerce.util.Failure
import com.arcxp.commerce.util.Success
import com.arcxp.commerce.util.handleFailure
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * View model for forgot password screen
 *
 * @param repo Forgot password repository [ForgotPasswordRepository]
 * @suppress
 */
class ForgotPasswordViewModel(
    private val repo: ForgotPasswordRepository,
    mainDispatcher: CoroutineDispatcher = Dispatchers.Main,
    ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : BaseAuthViewModel(mainDispatcher, ioDispatcher) {

    private val _requestPasswordResetResponse = MutableLiveData<ArcXPRequestPasswordReset>()
    val requestPasswordResetResponse: LiveData<ArcXPRequestPasswordReset> =
        _requestPasswordResetResponse

    private val _errorResponse = MutableLiveData<String>()
    val errorResponse: LiveData<String> = _errorResponse

    /**
     * Function to reset password by the given username
     *
     * @param userName user name for password reset
     */
    fun resetPasswordByUserName(userName: String) {
        mIoScope.launch {
            val res = repo.resetPassword(
                ArcXPResetPasswordRequestRequest(userName)
            )

            withContext(mUiScope.coroutineContext) {
                when (res) {
                    is Success -> _requestPasswordResetResponse.value = res.r!!
                    is Failure -> _errorResponse.value = handleFailure(res.l)
                }
            }
        }
    }
}
