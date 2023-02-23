package com.arcxp.commerce.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.arcxp.commerce.apimanagers.ArcXPRetailListener
import com.arcxp.commerce.models.ArcXPActivePaywallRules
import com.arcxp.commerce.repositories.RetailRepository
import com.arcxp.commerce.util.ArcXPError
import com.arcxp.commons.util.Failure
import com.arcxp.commons.util.Success
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * @suppress
 */
public class RetailViewModel(
    private val repo: RetailRepository) : BaseAuthViewModel() {

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
                            _paywallRulesResponse.postValue(res.success!!)
                        } else {
                            callback.onGetActivePaywallRulesSuccess(res.success!!)
                        }
                    }
                    is Failure -> {
                        if (callback == null) {
                            _errorResponse.value = res.failure as ArcXPError /*handleFailure(res.l)*/
                        } else {
                            callback.onGetActivePaywallRulesFailure(res.failure as ArcXPError)
                        }
                    }
                }
            }
        }
    }

}
