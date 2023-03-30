package com.arcxp.commerce.apimanagers

import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.lifecycle.OnLifecycleEvent
import com.arcxp.commerce.callbacks.ArcXPRetailListener
import com.arcxp.commerce.models.*
import com.arcxp.commerce.repositories.RetailRepository
import com.arcxp.commerce.util.AuthManager
import com.arcxp.commerce.viewmodels.RetailViewModel
import com.arcxp.commons.throwables.ArcXPException

/**
 * @suppress
 */
class RetailApiManager(
    private val authManager: AuthManager,
    private val fragment: Fragment? = null,
    private val arcxpRetailListener: ArcXPRetailListener
) : BaseApiManager<Fragment>(fragment) {

    private val viewModel by lazy {
        RetailViewModel(RetailRepository())
    }

    /**
     * Add observer for data change when view is created
     */
    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    private fun onCreate() {
        if (fragment != null) {
            viewModel.paywallRulesResponseArcxp.observe(fragment.viewLifecycleOwner, Observer {
                arcxpRetailListener.onGetActivePaywallRulesSuccess(it)
            })

            viewModel.errorResponse.observe(fragment.viewLifecycleOwner, Observer {
                arcxpRetailListener.onGetActivePaywallRulesFailure(it)
            })
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    private fun onDestroy() {
        
    }

    fun getActivePaywallRules(listener: ArcXPRetailListener) {
        viewModel.getActivePaywallRules(object: ArcXPRetailListener(){
            override fun onGetActivePaywallRulesSuccess(responseArcxp: ArcXPActivePaywallRules) {
                listener.onGetActivePaywallRulesSuccess(responseArcxp)
            }

            override fun onGetActivePaywallRulesFailure(error: ArcXPException) {
                listener.onGetActivePaywallRulesFailure(error)
            }
        })
    }

    private fun getCallbackScheme() : ArcXPRetailListener? {
        return if (fragment == null) {
            arcxpRetailListener
        } else {
            null
        }
    }


}
