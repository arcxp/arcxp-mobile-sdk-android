package com.arcxp.commerce.callbacks

import com.arcxp.commerce.models.*
import com.arcxp.commons.throwables.ArcXPException

abstract class ArcXPSalesListener {
    open fun onGetAllSubscriptionsSuccess(response: ArcXPSubscriptions) {}
    open fun onGetSubscriptionsFailure(error: ArcXPException) {}
    open fun onGetAllActiveSubscriptionsSuccess(response: ArcXPSubscriptions) {}
    open fun onGetEntitlementsSuccess(response: ArcXPEntitlements) {}
    open fun onGetEntitlementsFailure(error: ArcXPException) {}
    open fun onInitializePaymentMethodSuccess(response: Map<String, String>) {}
    open fun onInitializePaymentMethodFailure(error: ArcXPException) {}
    open fun onFinalizePaymentMethodSuccess(response: ArcXPFinalizePayment) {}
    open fun onFinalizePaymentMethodFailure(error: ArcXPException) {}
    open fun onFinalizePaymentMethod3dsSuccess(response: ArcXPFinalizePayment) {}
    open fun onFinalizePaymentMethod3dsFailure(error: ArcXPException) {}
    open fun onCancelSubscriptionSuccess(response: ArcXPCancelSubscription) {}
    open fun onCancelSubscriptionFailure(error: ArcXPException) {}
    open fun onUpdateAddressSuccess(response: ArcXPAddress) {}
    open fun onUpdateAddressFailure(error: ArcXPException) {}
    open fun onGetSubscriptionDetailsSuccess(response: ArcXPSubscriptionDetails) {}
    open fun onGetSubscriptionDetailsFailure(error: ArcXPException) {}
    open fun onCreateCustomerOrderSuccess(response: ArcXPCustomerOrder) {}
    open fun onCreateCustomerOrderFailure(error: ArcXPException) {}
    open fun onGetPaymentOptionsSuccess(response: List<String?>) {}
    open fun onGetPaymentOptionsFailure(error: ArcXPException) {}
    open fun onGetAddressesSuccess(response: List<ArcXPAddress?>) {}
    open fun onGetAddressesFailure(error: ArcXPException) {}
    open fun onInitializePaymentSuccess() {}
    open fun onInitializePaymentFailure(error: ArcXPException) {}
    open fun onFinalizePaymentSuccess(response: ArcXPFinalizePayment) {}
    open fun onFinalizePaymentFailure(error: ArcXPException) {}
    open fun onFinalizePayment3dsSuccess(response: ArcXPFinalizePayment) {}
    open fun onFinalizePayment3dsFailure(error: ArcXPException) {}
    open fun onOrderHistorySuccess(response: ArcXPOrderHistory) {}
    open fun onOrderHistoryFailure(error: ArcXPException) {}
    open fun onOrderDetailsSuccess(response: ArcXPCustomerOrder) {}
    open fun onOrderDetailsFailure(error: ArcXPException) {}
    open fun onClearCartSuccess(response: ArcXPCustomerOrder) {}
    open fun onClearCartFailure(error: ArcXPException) {}
    open fun onGetCurrentCartSuccess(response: ArcXPCustomerOrder) {}
    open fun onGetCurrentCartFailure(error: ArcXPException) {}
    open fun onAddItemToCartSuccess(response: ArcXPCustomerOrder) {}
    open fun onAddItemToCartFailure(error: ArcXPException) {}
    open fun onRemoveItemFromCartSuccess(response: ArcXPCustomerOrder) {}
    open fun onRemoveItemFromCartFailure(error: ArcXPException) {}
}