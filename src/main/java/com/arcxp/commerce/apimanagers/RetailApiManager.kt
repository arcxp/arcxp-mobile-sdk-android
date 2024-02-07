package com.arcxp.commerce.apimanagers

import com.arcxp.commerce.callbacks.ArcXPRetailListener
import com.arcxp.commerce.models.ArcXPActivePaywallRules
import com.arcxp.commons.throwables.ArcXPException
import com.arcxp.commons.util.DependencyFactory

/**
 * @suppress
 */
class RetailApiManager {

    private val viewModel by lazy {
        DependencyFactory.createRetailViewModel()
    }

    fun getActivePaywallRules(listener: ArcXPRetailListener) {
        viewModel.getActivePaywallRules(object : ArcXPRetailListener() {
            override fun onGetActivePaywallRulesSuccess(responseArcxp: ArcXPActivePaywallRules) {
                listener.onGetActivePaywallRulesSuccess(responseArcxp)
            }

            override fun onGetActivePaywallRulesFailure(error: ArcXPException) {
                listener.onGetActivePaywallRulesFailure(error)
            }
        })
    }
}
