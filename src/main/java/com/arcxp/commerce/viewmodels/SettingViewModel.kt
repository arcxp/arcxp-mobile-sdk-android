package com.arcxp.commerce.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.arcxp.commerce.models.ArcXPAnonymizeUser
import com.arcxp.commerce.repositories.SettingRepository
import com.arcxp.commerce.util.*
import kotlinx.coroutines.*

/**
 * View model for setting screen
 *
 * @param repo setting repository [SettingRepository]
 * @suppress
 */
class SettingViewModel(
    private val repo: SettingRepository,
    mainDispatcher: CoroutineDispatcher = Dispatchers.Main,
    ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : BaseAuthViewModel(mainDispatcher, ioDispatcher) {

    private val _logoutResponse = MutableLiveData<Boolean>()
    val logoutResponse: LiveData<Boolean> = _logoutResponse

    private val _deletionResponse = MutableLiveData<Boolean>()
    val deletionResponse: LiveData<Boolean> = _deletionResponse

    private val _logoutErrorResponse = MutableLiveData<ArcXPError>()
    val logoutErrorResponse: LiveData<ArcXPError> = _logoutErrorResponse

    private val _deletionErrorResponse = MutableLiveData<ArcXPError>()
    val deletionErrorResponse: LiveData<ArcXPError> = _deletionErrorResponse

    /**
     * Function to make log out call, delete cached user session either success or failure
     */
    fun logout() {
        mIoScope.launch {
            val res = repo.logout()

            withContext(mUiScope.coroutineContext) {
                when (res) {
                    is Success -> handleLogoutSuccess()
                    is Failure -> handleLogoutFailure(res.l as ArcXPError)
                }
            }
        }

    }

    /**
     * Function to request current user deletion
     */
    fun deleteUser() {
        mIoScope.launch {
            val res = repo.deleteUser()

            withContext(mUiScope.coroutineContext) {
                when (res) {
                    is Success -> handleDeletionResponse(res.r)
                    is Failure -> handleDeletionFailure(res.l as ArcXPError)
                }
            }
        }
    }

    private fun handleDeletionResponse(response: ArcXPAnonymizeUser?) {
        with(response) {
            if (this?.valid == true) {
                _deletionResponse.value = true
            } else handleDeletionFailure(ArcXPError("Your account deletion request is declined."))
        }
    }

    private fun handleDeletionFailure(error: ArcXPError) {
        _deletionErrorResponse.value = error
    }

    /**
     * Function to handle log out success
     */
    private fun handleLogoutSuccess() {
        _logoutResponse.value = true
        AuthManager.getInstance().deleteSession()
    }

    /**
     * Function to handle log out failure
     */
    private fun handleLogoutFailure(error: ArcXPError) {
        _logoutErrorResponse.value = error
        AuthManager.getInstance().deleteSession()
    }

}
