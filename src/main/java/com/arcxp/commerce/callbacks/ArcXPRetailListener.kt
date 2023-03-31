package com.arcxp.commerce.callbacks

import com.arcxp.commerce.models.ArcXPActivePaywallRules
import com.arcxp.commons.throwables.ArcXPException

/**
 * Interface used to update fragment UI
 * @suppress
 */
abstract class ArcXPRetailListener  {
    open fun onGetActivePaywallRulesSuccess(responseArcxp: ArcXPActivePaywallRules) {}
    open fun onGetActivePaywallRulesFailure(error: ArcXPException) {}
}