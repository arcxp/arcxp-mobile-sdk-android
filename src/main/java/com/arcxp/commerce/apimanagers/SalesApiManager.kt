package com.arcxp.commerce.apimanagers

import com.arcxp.commerce.callbacks.ArcXPSalesListener
import com.arcxp.commerce.models.*
import com.arcxp.commons.util.DependencyFactory

/**
 * SalesApiManager is responsible for managing sales-related API operations within the ArcXP Commerce module.
 * It acts as a bridge between the UI layer and the SalesViewModel, facilitating various sales operations such as managing subscriptions,
 * entitlements, payment methods, orders, and cart operations.
 *
 * The class defines the following operations:
 * - Retrieve active and all subscriptions
 * - Retrieve entitlements (v1 and v2)
 * - Initialize and finalize payment methods (including 3DS)
 * - Cancel subscriptions
 * - Update address
 * - Retrieve subscription details
 * - Create customer orders
 * - Retrieve payment options
 * - Retrieve addresses
 * - Initialize and finalize payments (including 3DS)
 * - Retrieve order history and details
 * - Clear and manage cart items
 *
 * Usage:
 * - Create an instance of SalesApiManager and call the provided methods to perform sales operations.
 * - Handle the results through the ArcXPSalesListener, which provides callback methods for success and failure cases.
 *
 * Example:
 *
 * val salesApiManager = SalesApiManager()
 * salesApiManager.getAllActiveSubscriptions(object : ArcXPSalesListener() {
 *     override fun onGetAllActiveSubscriptionsSuccess(response: ArcXPSubscriptions) {
 *         // Handle success
 *     }
 *     override fun onGetSubscriptionsFailure(error: ArcXPException) {
 *         // Handle failure
 *     }
 * })
 *
 * Note: Ensure that the DependencyFactory and SalesViewModel are properly configured before using SalesApiManager.
 *
 * @method getAllActiveSubscriptions Retrieve all active subscriptions.
 * @method getAllSubscriptions Retrieve all subscriptions.
 * @method getEntitlements Retrieve entitlements.
 * @method getEntitlementsV2 Retrieve entitlements (v2).
 * @method initializePaymentMethod Initialize a payment method.
 * @method finalizePaymentMethod Finalize a payment method.
 * @method finalizePaymentMethod3ds Finalize a payment method with 3DS.
 * @method cancelSubscription Cancel a subscription.
 * @method updateAddress Update the address.
 * @method getSubscriptionDetails Retrieve subscription details.
 * @method createCustomerOrder Create a customer order.
 * @method getPaymentOptions Retrieve payment options.
 * @method getPaymentAddresses Retrieve addresses.
 * @method initializePayment Initialize a payment.
 * @method finalizePayment Finalize a payment.
 * @method finalizePayment3ds Finalize a payment with 3DS.
 * @method getOrderHistory Retrieve order history.
 * @method getOrderDetails Retrieve order details.
 * @method clearCart Clear the cart.
 * @method getCurrentCart Retrieve the current cart.
 * @method addItemToCart Add an item to the cart.
 * @method removeItemFromCart Remove an item from the cart.
 */
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

    fun getEntitlementsV2(callback: ArcXPSalesListener) {
        viewModel.getEntitlementsV2(callback)
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


