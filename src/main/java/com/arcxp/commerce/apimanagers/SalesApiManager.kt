package com.arcxp.commerce.apimanagers

import androidx.media3.common.util.UnstableApi
import com.arcxp.commerce.callbacks.ArcXPSalesListener
import com.arcxp.commerce.models.*
import com.arcxp.commons.util.DependencyFactory

/**
 * @suppress
 */
@UnstableApi
class SalesApiManager{
    private val viewModel by lazy {
        DependencyFactory.createSalesViewModel()
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
}


