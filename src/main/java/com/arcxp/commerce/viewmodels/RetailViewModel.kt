package com.arcxp.commerce.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.arcxp.commerce.callbacks.ArcXPRetailListener
import com.arcxp.commerce.models.ArcXPActivePaywallRules
import com.arcxp.commerce.repositories.RetailRepository
import com.arcxp.commons.throwables.ArcXPException
import com.arcxp.commons.util.Failure
import com.arcxp.commons.util.Success
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * RetailViewModel is responsible for managing the retail-related data and operations within the ArcXP Commerce module.
 * It interacts with the RetailRepository to fetch active paywall rules and exposes LiveData objects to observe the results.
 *
 * The ViewModel handles the retrieval of active paywall rules and processes the results and errors, updating the corresponding LiveData objects.
 *
 * Usage:
 * - Instantiate the RetailViewModel with the required RetailRepository instance.
 * - Observe the LiveData objects to get updates on the retail operations.
 * - Call the provided methods to perform retail operations and handle the results through callbacks or LiveData observers.
 *
 * Example:
 *
 * val retailViewModel = RetailViewModel(retailRepository)
 * retailViewModel.getActivePaywallRules(null)
 * retailViewModel.paywallRulesResponseArcxp.observe(this, Observer { paywallRules ->
 *     // Handle the paywall rules data
 * })
 *
 * Note: Ensure that the required RetailRepository instance is properly initialized before creating an instance of RetailViewModel.
 *
 * @property repo The RetailRepository instance used to perform retail operations.
 * @property _paywallRulesResponse LiveData object for observing active paywall rules data.
 * @property errorResponse LiveData object for observing error data.
 */
public class RetailViewModel(
    private val repo: RetailRepository) : BaseAuthViewModel() {

    private val _paywallRulesResponse = MutableLiveData<ArcXPActivePaywallRules>()
    val paywallRulesResponseArcxp: LiveData<ArcXPActivePaywallRules> = _paywallRulesResponse

    private val _errorResponse = MutableLiveData<ArcXPException>()
    val errorResponse: LiveData<ArcXPException> = _errorResponse

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
                            _errorResponse.value = res.failure as ArcXPException /*handleFailure(res.l)*/
                        } else {
                            callback.onGetActivePaywallRulesFailure(res.failure as ArcXPException)
                        }
                    }
                }
            }
        }
    }

}
