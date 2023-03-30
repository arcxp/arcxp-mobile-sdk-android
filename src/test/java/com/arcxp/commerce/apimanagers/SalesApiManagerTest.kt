package com.arcxp.commerce.apimanagers

import androidx.fragment.app.Fragment
import com.arcxp.commerce.callbacks.ArcXPSalesListener
import com.arcxp.commerce.models.*
import com.arcxp.commerce.viewmodels.SalesViewModel
import com.arcxp.commons.util.DependencyFactory
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
        mockkObject(DependencyFactory)
        every { DependencyFactory.createSalesViewModel() } returns viewModel
        testObject = SalesApiManager()
    }

    @Test
    fun `test get all active subscriptions`() {
        testObject.getAllActiveSubscriptions(listener)
        verifySequence {
            viewModel.getAllActiveSubscriptions(listener)
        }
    }

    @Test
    fun `test get all subscriptions`() {
        testObject.getAllSubscriptions(listener)
        verifySequence {
            viewModel.getAllSubscriptions(listener)
        }
    }

    @Test
    fun `test get entitlements`() {
        testObject.getEntitlements(listener)
        verifySequence {
            viewModel.getEntitlements(listener)
        }
    }

    @Test
    fun `test initialize payment method`() {
        testObject.initializePaymentMethod("a", "b", listener)
        verifySequence {
            viewModel.initializePaymentMethod("a", "b", listener)
        }
    }

    @Test
    fun `test finalize payment method`() {
        val request = mockk<ArcXPFinalizePaymentRequest>()

        testObject.finalizePaymentMethod("id", "pid", request, listener)
        verifySequence {
            viewModel.finalizePaymentMethod("id", "pid", request, listener)
        }
    }

    @Test
    fun `test finalize payment method 3ds`() {
        val request = mockk<ArcXPFinalizePaymentRequest>()

        testObject.finalizePaymentMethod3ds("id", "pid", request, listener)
        verifySequence {
            viewModel.finalizePaymentMethod3ds("id", "pid", request, listener)
        }
    }

    @Test
    fun `test cancel subscription`() {
        val request = mockk<ArcXPCancelSubscriptionRequest>()

        testObject.cancelSubscription("id", request, listener)
        verifySequence {
            viewModel.cancelSubscription("id", request, listener)
        }
    }

    @Test
    fun `test update address`() {
        val request = mockk<ArcXPUpdateAddressRequest>()

        testObject.updateAddress(request, listener)
        verifySequence {
            viewModel.updateAddress(request, listener)
        }
    }

    @Test
    fun `test get subscription details`() {
        testObject.getSubscriptionDetails("id", listener)
        verifySequence {
            viewModel.getSubscriptionDetails("id", listener)
        }
    }

    @Test
    fun `test create customer order`() {
        val request = mockk<ArcXPCustomerOrderRequest>()

        testObject.createCustomerOrder(request, listener)
        verifySequence {
            viewModel.createCustomerOrder(request, listener)
        }
    }

    @Test
    fun `test get payment options`() {
        testObject.getPaymentOptions(listener)
        verifySequence {
            viewModel.getPaymentOptions(listener)
        }
    }

    @Test
    fun `test get payment addresses`() {
        testObject.getPaymentAddresses(listener)
        verifySequence {
            viewModel.getPaymentAddresses(listener)
        }
    }

    @Test
    fun `test get initialize payment`() {
        testObject.initializePayment("a", "b", listener)
        verifySequence {
            viewModel.initializePayment("a", "b", listener)
        }
    }

    @Test
    fun `test finalize payment`() {
        val request = mockk<ArcXPFinalizePaymentRequest>()

        testObject.finalizePayment("a", "b", request, listener)
        verifySequence {
            viewModel.finalizePayment("a", "b", request, listener)
        }
    }

    @Test
    fun `test finalize payment 3ds`() {
        val request = mockk<ArcXPFinalizePaymentRequest>()

        testObject.finalizePayment3ds("a", "b", request, listener)
        verifySequence {
            viewModel.finalizePayment3ds("a", "b", request, listener)
        }
    }

    @Test
    fun `test get order history`() {
        val response = mockk<ArcXPOrderHistory>()

        testObject.getOrderHistory(listener)
        verifySequence {
            viewModel.getOrderHistory(listener)
        }
    }

    @Test
    fun `test get order details`() {
        testObject.getOrderDetails("a", listener)
        verifySequence {
            viewModel.getOrderDetails("a", listener)
        }
    }

    @Test
    fun `test clear cart`() {
        testObject.clearCart(listener)
        verifySequence {
            viewModel.clearCart(listener)
        }
    }

    @Test
    fun `test get current cart`() {
        testObject.getCurrentCart(listener)
        verifySequence {
            viewModel.getCurrentCart(listener)
        }
    }

    @Test
    fun `test add item to cart`() {
        val request = mockk<ArcXPCartItemsRequest>()

        testObject.addItemToCart(request, listener)
        verifySequence {
            viewModel.addItemToCart(request, listener)
        }
    }

    @Test
    fun `test remove items from cart`() {
        testObject.removeItemFromCart("sku", listener)
        verifySequence {
            viewModel.removeItemFromCart("sku", listener)
        }
    }

}
