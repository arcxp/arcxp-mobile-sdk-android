package com.arcxp.commerce.apimanagers

import androidx.fragment.app.Fragment
import com.arcxp.commerce.callbacks.ArcXPSalesListener
import com.arcxp.commerce.models.*
import com.arcxp.commerce.viewmodels.SalesViewModel
import io.mockk.*
import io.mockk.impl.annotations.MockK
import org.junit.Before
import org.junit.Test

class SalesApiManagerTest {

    private lateinit var testObject: SalesApiManager

    @MockK
    private lateinit var listener: ArcXPSalesListener

    @MockK
    private lateinit var viewModel: SalesViewModel


    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        testObject = SalesApiManager(Fragment(), listener, viewModel)
    }

    @Test
    fun `test get all active subscriptions`() {
        testObject.getAllActiveSubscriptions(listener)
        verify(exactly = 1){
            viewModel.getAllActiveSubscriptions(listener)
        }
    }

    @Test
    fun `test get all subscriptions`() {
        testObject.getAllSubscriptions(listener)
        verify(exactly = 1){
            viewModel.getAllSubscriptions(listener)
        }
    }

    @Test
    fun `test get entitlements`() {
        testObject.getEntitlements(listener)
        verify(exactly = 1){
            viewModel.getEntitlements(listener)
        }
    }

    @Test
    fun `test initialize payment method`() {
        testObject.initializePaymentMethod("a", "b", listener)
        verify(exactly = 1){
            viewModel.initializePaymentMethod("a", "b", listener)
        }
    }

    @Test
    fun `test finalize payment method`() {
        val request = mockk<ArcXPFinalizePaymentRequest>()

        testObject.finalizePaymentMethod("id", "pid", request, listener)
        verify(exactly = 1){
            viewModel.finalizePaymentMethod("id", "pid", request, listener)
        }
    }

    @Test
    fun `test finalize payment method 3ds`() {
        val request = mockk<ArcXPFinalizePaymentRequest>()

        testObject.finalizePaymentMethod3ds("id", "pid", request, listener)
        verify(exactly = 1){
            viewModel.finalizePaymentMethod3ds("id", "pid", request, listener)
        }
    }

    @Test
    fun `test cancel subscription`() {
        val request = mockk<ArcXPCancelSubscriptionRequest>()

        testObject.cancelSubscription("id", request, listener)
        verify(exactly = 1){
            viewModel.cancelSubscription("id", request, listener)
        }
    }

    @Test
    fun `test update address`() {
        val request = mockk<ArcXPUpdateAddressRequest>()

        testObject.updateAddress(request, listener)
        verify(exactly = 1){
            viewModel.updateAddress(request, listener)
        }
    }

    @Test
    fun `test get subscription details`() {
        testObject.getSubscriptionDetails("id", listener)
        verify(exactly = 1){
            viewModel.getSubscriptionDetails("id", listener)
        }
    }

    @Test
    fun `test create customer order`() {
        val request = mockk<ArcXPCustomerOrderRequest>()

        testObject.createCustomerOrder(request, listener)
        verify(exactly = 1){
            viewModel.createCustomerOrder(request, listener)
        }
    }

    @Test
    fun `test get payment options`() {
        testObject.getPaymentOptions(listener)
        verify(exactly = 1){
            viewModel.getPaymentOptions(listener)
        }
    }

    @Test
    fun `test get payment addresses`() {
        testObject.getPaymentAddresses(listener)
        verify(exactly = 1){
            viewModel.getPaymentAddresses(listener)
        }
    }

    @Test
    fun `test get initialize payment`() {
        testObject.initializePayment("a", "b", listener)
        verify(exactly = 1){
            viewModel.initializePayment("a", "b", listener)
        }
    }

    @Test
    fun `test finalize payment`() {
        val request = mockk<ArcXPFinalizePaymentRequest>()

        testObject.finalizePayment("a", "b", request, listener)
        verify(exactly = 1){
            viewModel.finalizePayment("a", "b", request, listener)
        }
    }

    @Test
    fun `test finalize payment 3ds`() {
        val request = mockk<ArcXPFinalizePaymentRequest>()

        testObject.finalizePayment3ds("a", "b", request, listener)
        verify(exactly = 1){
            viewModel.finalizePayment3ds("a", "b", request, listener)
        }
    }

    @Test
    fun `test get order history`() {
        val response = mockk<ArcXPOrderHistory>()

        testObject.getOrderHistory(listener)
        verify(exactly = 1){
            viewModel.getOrderHistory(listener)
        }
    }

    @Test
    fun `test get order details`() {
        testObject.getOrderDetails("a", listener)
        verify(exactly = 1){
            viewModel.getOrderDetails("a", listener)
        }
    }

    @Test
    fun `test clear cart`() {
        testObject.clearCart(listener)
        verify(exactly = 1){
            viewModel.clearCart(listener)
        }
    }

    @Test
    fun `test get current cart`() {
        testObject.getCurrentCart(listener)
        verify(exactly = 1){
            viewModel.getCurrentCart(listener)
        }
    }

    @Test
    fun `test add item to cart`() {
        val request = mockk<ArcXPCartItemsRequest>()

        testObject.addItemToCart(request, listener)
        verify(exactly = 1){
            viewModel.addItemToCart(request, listener)
        }
    }

    @Test
    fun `test remove items from cart`() {
        testObject.removeItemFromCart("sku", listener)
        verify(exactly = 1){
            viewModel.removeItemFromCart("sku", listener)
        }
    }

}
