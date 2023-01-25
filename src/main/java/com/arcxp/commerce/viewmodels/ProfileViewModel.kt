package com.arcxp.commerce.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.arcxp.commerce.extendedModels.ArcXPProfileManage
import com.arcxp.commerce.repositories.ProfileRepository
import com.arcxp.commerce.util.ArcXPError
import com.arcxp.commerce.util.Failure
import com.arcxp.commerce.util.Success
import kotlinx.coroutines.*

/**
 * View model for view profile screen
 *
 * @param repo profile repository [ProfileRepository]
 * @suppress
 */
class ProfileViewModel(
    private val repo: ProfileRepository,
    mainDispatcher: CoroutineDispatcher = Dispatchers.Main,
    ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : BaseAuthViewModel(mainDispatcher, ioDispatcher) {

    private val _profileResponse = MutableLiveData<ArcXPProfileManage>()
    val profileResponse: LiveData<ArcXPProfileManage> = _profileResponse

    private val _errorResponse = MutableLiveData<ArcXPError>()
    val errorResponse: LiveData<ArcXPError> = _errorResponse

    /**
     * Function to load profile
     */
    private val TAG = "ProfileViewModel"
    fun getProfile() {
        mIoScope.launch {
            val res = repo.getProfile()
            withContext(mUiScope.coroutineContext) {
                when (res) {
                    is Success -> _profileResponse.value = res.r
                    is Failure -> _errorResponse.value = res.l as ArcXPError
                }
            }
        }
    }
}
