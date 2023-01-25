package com.arcxp.commerce.apimanagers

import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.lifecycle.OnLifecycleEvent
import com.arcxp.commerce.models.*
import com.arcxp.commerce.repositories.SalesRepository
import com.arcxp.commerce.util.ArcXPError
import com.arcxp.commerce.viewmodels.SalesViewModel
/**
 * @suppress
 */
class SalesApiManager(
    private val fragment: Fragment? = null,
    private val arcxpSalesListener: ArcXPSalesListener,
    private val viewModel: SalesViewModel = SalesViewModel(SalesRepository())
) : BaseApiManager<Fragment>(fragment) {

    /**
     * Add observer for data change when view is created
     */
    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    private fun onCreate() {
        if (fragment != null) {
            viewModel.subscriptionsResponse.observe(fragment.viewLifecycleOwner, Observer {
                arcxpSalesListener.onGetAllSubscriptionsSuccess(it)
            })

            viewModel.allSubscriptionsResponse.observe(fragment.viewLifecycleOwner, Observer {
                arcxpSalesListener.onGetAllActiveSubscriptionsSuccess(it)
            })

            viewModel.errorResponse.observe(fragment.viewLifecycleOwner, Observer {
                arcxpSalesListener.onGetSubscriptionsFailure(it)
            })
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    private fun onDestroy() {
        
    }

    fun getAllActiveSubscriptions(callback: ArcXPSalesListener) {
        viewModel.getAllActiveSubscriptions(callback)
    }

    fun getAllSubscriptions(callback: ArcXPSalesListener) {
        viewModel.getAllSubscriptions(callback)
    }

    fun getEntitlements(callback: ArcXPSalesListener) {
        viewModel.getEntitlements(callback)
    }

    fun initializePaymentMethod(id: String, pid: String, callback: ArcXPSalesListener?) {
        viewModel.initializePaymentMethod(id, pid, callback)
    }

    fun finalizePaymentMethod(id: String, pid: String, request: ArcXPFinalizePaymentRequest, callback: ArcXPSalesListener?) {
        viewModel.finalizePaymentMethod(id, pid, request, callback)
    }

    fun finalizePaymentMethod3ds(id: String, pid: String, request: ArcXPFinalizePaymentRequest, callback: ArcXPSalesListener?) {
        viewModel.finalizePaymentMethod3ds(id, pid, request, callback)
    }

    fun cancelSubscription(id: String, request: ArcXPCancelSubscriptionRequest, callback: ArcXPSalesListener?) {
        viewModel.cancelSubscription(id, request, callback)
    }

    fun updateAddress(request: ArcXPUpdateAddressRequest, callback: ArcXPSalesListener?) {
        viewModel.updateAddress(request, callback)
    }

    fun getSubscriptionDetails(id: String, callback: ArcXPSalesListener?) {
        viewModel.getSubscriptionDetails(id, callback)
    }

    fun createCustomerOrder(request: ArcXPCustomerOrderRequest, callback: ArcXPSalesListener?) {
        viewModel.createCustomerOrder(request, callback)
    }

    fun getPaymentOptions(callback: ArcXPSalesListener?) {
        viewModel.getPaymentOptions(callback)
    }

    fun getPaymentAddresses(callback: ArcXPSalesListener?) {
        viewModel.getPaymentAddresses(callback)
    }

    fun initializePayment(orderNumber: String, mid: String, callback: ArcXPSalesListener?) {
        viewModel.initializePayment(orderNumber, mid, callback)
    }

    fun finalizePayment(orderNumber: String, mid: String, request: ArcXPFinalizePaymentRequest, callback: ArcXPSalesListener?) {
        viewModel.finalizePayment(orderNumber, mid, request, callback)
    }

    fun finalizePayment3ds(orderNumber: String, mid: String, request: ArcXPFinalizePaymentRequest, callback: ArcXPSalesListener?) {
        viewModel.finalizePayment3ds(orderNumber, mid, request, callback)
    }

    fun getOrderHistory(callback: ArcXPSalesListener?) {
        viewModel.getOrderHistory(callback)
    }

    fun getOrderDetails(orderNumber: String, callback: ArcXPSalesListener?) {
        viewModel.getOrderDetails(orderNumber, callback)
    }

    fun clearCart(callback: ArcXPSalesListener?) {
        viewModel.clearCart(callback)
    }

    fun getCurrentCart(callback: ArcXPSalesListener?) {
        viewModel.getCurrentCart(callback)
    }

    fun addItemToCart(request: ArcXPCartItemsRequest, callback: ArcXPSalesListener?) {
        viewModel.addItemToCart(request, callback)
    }

    fun removeItemFromCart(sku: String, callback: ArcXPSalesListener?) {
        viewModel.removeItemFromCart(sku, callback)
    }

    private fun getCallbackScheme() : ArcXPSalesListener? {
        return if (fragment == null) {
            arcxpSalesListener
        } else {
            null
        }
    }
}

abstract class ArcXPSalesListener : ArcListener {
    open fun onGetAllSubscriptionsSuccess(response: ArcXPSubscriptions) {}
    open fun onGetSubscriptionsFailure(error: ArcXPError) {}
    open fun onGetAllActiveSubscriptionsSuccess(response: ArcXPSubscriptions) {}
    open fun onGetEntitlementsSuccess(response: ArcXPEntitlements) {}
    open fun onGetEntitlementsFailure(error: ArcXPError) {}
    open fun onInitializePaymentMethodSuccess(response: Map<String, String>) {}
    open fun onInitializePaymentMethodFailure(error: ArcXPError) {}
    open fun onFinalizePaymentMethodSuccess(response: ArcXPFinalizePayment) {}
    open fun onFinalizePaymentMethodFailure(error: ArcXPError) {}
    open fun onFinalizePaymentMethod3dsSuccess(response: ArcXPFinalizePayment) {}
    open fun onFinalizePaymentMethod3dsFailure(error: ArcXPError) {}
    open fun onCancelSubscriptionSuccess(response: ArcXPCancelSubscription) {}
    open fun onCancelSubscriptionFailure(error: ArcXPError) {}
    open fun onUpdateAddressSuccess(response: ArcXPAddress) {}
    open fun onUpdateAddressFailure(error: ArcXPError) {}
    open fun onGetSubscriptionDetailsSuccess(response: ArcXPSubscriptionDetails) {}
    open fun onGetSubscriptionDetailsFailure(error: ArcXPError) {}
    open fun onCreateCustomerOrderSuccess(response: ArcXPCustomerOrder) {}
    open fun onCreateCustomerOrderFailure(error: ArcXPError) {}
    open fun onGetPaymentOptionsSuccess(response: List<String?>) {}
    open fun onGetPaymentOptionsFailure(error: ArcXPError) {}
    open fun onGetAddressesSuccess(response: List<ArcXPAddress?>) {}
    open fun onGetAddressesFailure(error: ArcXPError) {}
    open fun onInitializePaymentSuccess() {}
    open fun onInitializePaymentFailure(error: ArcXPError) {}
    open fun onFinalizePaymentSuccess(response: ArcXPFinalizePayment) {}
    open fun onFinalizePaymentFailure(error: ArcXPError) {}
    open fun onFinalizePayment3dsSuccess(response: ArcXPFinalizePayment) {}
    open fun onFinalizePayment3dsFailure(error: ArcXPError) {}
    open fun onOrderHistorySuccess(response: ArcXPOrderHistory) {}
    open fun onOrderHistoryFailure(error: ArcXPError) {}
    open fun onOrderDetailsSuccess(response: ArcXPCustomerOrder) {}
    open fun onOrderDetailsFailure(error: ArcXPError) {}
    open fun onClearCartSuccess(response: ArcXPCustomerOrder) {}
    open fun onClearCartFailure(error: ArcXPError) {}
    open fun onGetCurrentCartSuccess(response: ArcXPCustomerOrder) {}
    open fun onGetCurrentCartFailure(error: ArcXPError) {}
    open fun onAddItemToCartSuccess(response: ArcXPCustomerOrder) {}
    open fun onAddItemToCartFailure(error: ArcXPError) {}
    open fun onRemoveItemFromCartSuccess(response: ArcXPCustomerOrder) {}
    open fun onRemoveItemFromCartFailure(error: ArcXPError) {}
}
