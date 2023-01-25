package com.arcxp.commerce.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.arcxp.commerce.apimanagers.ArcXPRetailListener
import com.arcxp.commerce.models.*
import com.arcxp.commerce.repositories.RetailRepository
import com.arcxp.commerce.util.ArcXPError
import com.arcxp.commerce.util.Failure
import com.arcxp.commerce.util.Success
import kotlinx.coroutines.*

/**
 * @suppress
 */
public class RetailViewModel(
    private val repo: RetailRepository,
    mainDispatcher: CoroutineDispatcher = Dispatchers.Main,
    ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : BaseAuthViewModel(mainDispatcher, ioDispatcher) {

    private val _paywallRulesResponse = MutableLiveData<ArcXPActivePaywallRules>()
    val paywallRulesResponseArcxp: LiveData<ArcXPActivePaywallRules> = _paywallRulesResponse

    private val _errorResponse = MutableLiveData<ArcXPError>()
    val errorResponse: LiveData<ArcXPError> = _errorResponse

    fun getActivePaywallRules(callback: ArcXPRetailListener?) {
        mIoScope.launch {
            val res = repo.getActivePaywallRules()
            withContext(mUiScope.coroutineContext) {
                when (res) {
                    is Success -> {
                        if (callback == null) {
                            _paywallRulesResponse.postValue(res.r)
                        } else {
                            callback.onGetActivePaywallRulesSuccess(res.r!!)
                        }
                    }
                    is Failure -> {
                        if (callback == null) {
                            _errorResponse.value = res.l as ArcXPError /*handleFailure(res.l)*/
                        } else {
                            callback.onGetActivePaywallRulesFailure(res.l as ArcXPError)
                        }
                    }
                }
            }
        }
    }

}
