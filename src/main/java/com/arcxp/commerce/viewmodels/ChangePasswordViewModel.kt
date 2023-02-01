package com.arcxp.commerce.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.arcxp.commerce.models.ArcXPIdentity
import com.arcxp.commerce.models.ArcXPPasswordResetRequest
import com.arcxp.commerce.repositories.ChangePasswordRepository
import com.arcxp.commerce.util.*
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * View model for forgot password screen
 *
 * @param repo Forgot password repository [ChangePasswordRepository]
 * @suppress
 */
class ChangePasswordViewModel(
    private val repo: ChangePasswordRepository,
    mainDispatcher: CoroutineDispatcher = Dispatchers.Main,
    ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : BaseAuthViewModel(mainDispatcher, ioDispatcher) {

    private val _identityResponse = MutableLiveData<ArcXPIdentity>()
    val identityResponse: LiveData<ArcXPIdentity> = _identityResponse

    private val _errorResponse = MutableLiveData<ArcXPError>()
    val errorResponse: LiveData<ArcXPError> = _errorResponse

    /**
     * Function to reset password by the given username
     *
     * @param oldPassword old password
     * @param newPassword new password
     */
    fun changeUserPassword(oldPassword: String, newPassword: String) {
        mIoScope.launch {
            val res = repo.changePassword(
                ArcXPPasswordResetRequest(
                    oldPassword, newPassword
                )
            )
            withContext(mUiScope.coroutineContext) {
                when (res) {
                    is Success -> _identityResponse.value = res.r!!
                    is Failure -> _errorResponse.value = res.l as ArcXPError
                }
            }
        }
    }

}
