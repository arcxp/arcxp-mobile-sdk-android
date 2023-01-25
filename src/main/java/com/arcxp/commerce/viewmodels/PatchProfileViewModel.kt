package com.arcxp.commerce.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.arcxp.commerce.extendedModels.ArcXPProfileManage
import com.arcxp.commerce.models.ArcXPProfilePatchRequest
import com.arcxp.commerce.repositories.ProfileRepository
import com.arcxp.commerce.util.*
import kotlinx.coroutines.*

/**
 * View model for patch profile screen
 *
 * @param repo profile repository [ProfileRepository]
 * @suppress
 */
class PatchProfileViewModel(
    private val repo: ProfileRepository,
    mainDispatcher: CoroutineDispatcher = Dispatchers.Main,
    ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : BaseAuthViewModel(mainDispatcher, ioDispatcher) {

    private val _profileResponse = MutableLiveData<ArcXPProfileManage>()
    val profileResponse: LiveData<ArcXPProfileManage> = _profileResponse

    private val _errorResponse = MutableLiveData<ArcXPError>()
    val errorResponse: LiveData<ArcXPError> = _errorResponse

    /**
     * Function to patch profile
     *
     * @param profilePatchRequest request to patch profile
     */
    fun patchProfile(profilePatchRequest: ArcXPProfilePatchRequest) {
        mIoScope.launch {
            val res = repo.patchProfile(profilePatchRequest)
            withContext(mUiScope.coroutineContext) {
                when (res) {
                    is Success -> _profileResponse.value = res.r
                    is Failure -> _errorResponse.value = res.l as ArcXPError
                }
            }
        }
    }
}
