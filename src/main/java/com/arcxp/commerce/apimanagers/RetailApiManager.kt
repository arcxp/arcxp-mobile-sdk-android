package com.arcxp.commerce.apimanagers

import com.arcxp.commerce.callbacks.ArcXPRetailListener
import com.arcxp.commerce.models.ArcXPActivePaywallRules
import com.arcxp.commons.throwables.ArcXPException
import com.arcxp.commons.util.DependencyFactory

/**
 * RetailApiManager is responsible for managing retail-related API operations within the ArcXP Commerce module.
 * It acts as a bridge between the UI layer and the RetailViewModel, facilitating operations such as fetching active paywall rules.
 *
 * The class defines the following operations:
 * - Fetch active paywall rules
 *
 * Usage:
 * - Create an instance of RetailApiManager and call the provided methods to perform retail operations.
 * - Handle the results through the ArcXPRetailListener, which provides callback methods for success and failure cases.
 *
 * Example:
 *
 * val retailApiManager = RetailApiManager()
 * retailApiManager.getActivePaywallRules(object : ArcXPRetailListener() {
 *     override fun onGetActivePaywallRulesSuccess(response: ArcXPActivePaywallRules) {
 *         // Handle success
 *     }
 *     override fun onGetActivePaywallRulesFailure(error: ArcXPException) {
 *         // Handle failure
 *     }
 * })
 *
 * Note: Ensure that the DependencyFactory and RetailViewModel are properly configured before using RetailApiManager.
 *
 * @method getActivePaywallRules Fetch the active paywall rules.
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
