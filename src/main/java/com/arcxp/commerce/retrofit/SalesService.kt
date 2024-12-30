package com.arcxp.commerce.retrofit

import androidx.annotation.Keep
import com.arcxp.commerce.models.*
import retrofit2.Response
import retrofit2.http.*

/**
 * SalesService is a Retrofit service interface for handling sales-related API calls within the ArcXP Commerce module.
 * It provides methods for managing subscriptions, entitlements, payment methods, orders, and cart operations.
 *
 * The interface defines the following operations:
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
 * - Implement this interface using Retrofit to create an instance of the service.
 * - Call the provided methods to perform sales operations and handle the results through Retrofit's response handling.
 *
 * Example:
 *
 * val salesService = retrofit.create(SalesService::class.java)
 * val response = salesService.getAllActiveSubscriptions()
 *
 * Note: Ensure that the Retrofit instance is properly configured before creating an instance of SalesService.
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
 * @method getSubscriptionsDetails Retrieve subscription details.
 * @method createCustomerOrder Create a customer order.
 * @method getPaymentOptions Retrieve payment options.
 * @method getAddresses Retrieve addresses.
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
interface SalesService {

    @Keep
    @GET("v1/subscription/allactive")
    suspend fun getAllActiveSubscriptions(): Response<List<SubscriptionSummary>>

    @Keep
    @GET("v1/subscription/all")
    suspend fun getAllSubscriptions(): Response<List<SubscriptionSummary>>

    @Keep
    @GET("v1/entitlements")
    suspend fun getEntitlements(): Response<ArcXPEntitlements>

    @Keep
    @GET("v2/subscriptions/entitlements")
    suspend fun getEntitlementsV2(): Response<ArcXPEntitlements>

    @Keep
    @PUT("v1/paymentmethod/{id}/provider/{pid}")
    suspend fun initializePaymentMethod(@Path("id")id: String, @Path("pid")pid: String) : Response<Map<String,String>>

    @Keep
    @PUT("v1/paymentmethod/{id}/provider/{pid}/finalize")
    suspend fun finalizePaymentMethod(@Path("id")id: String,
                                      @Path("pid")pid: String,
                                      @Body request: ArcXPFinalizePaymentRequest
    ): Response<ArcXPFinalizePayment>

    @Keep
    @PUT("v1/paymentmethod/{id}/provider/{pid}/finalize3ds")
    suspend fun finalizePaymentMethod3ds(@Path("id")id: String,
                                   @Path("pid")pid: String,
                                   @Body request: ArcXPFinalizePaymentRequest
    ): Response<ArcXPFinalizePayment>

    @Keep
    @PUT("v1/subscription/{id}/cancel")
    suspend fun cancelSubscription(@Path("id")id: String,
                                   @Body request: ArcXPCancelSubscriptionRequest
    ) : Response<ArcXPCancelSubscription>


    @Keep
    @PUT("v1/subscriptions/address")
    suspend fun updateAddress(@Body request: ArcXPUpdateAddressRequest): Response<ArcXPAddress>

    @Keep
    @GET("v1/subscription/{id}/details")
    suspend fun getSubscriptionsDetails(@Path("id")id: String) : Response<ArcXPSubscriptionDetails>

    @Keep
    @POST("v1/checkout/order")
    suspend fun createCustomerOrder(@Body request: ArcXPCustomerOrderRequest): Response<ArcXPCustomerOrder>

    @Keep
    @GET("v1/payment/options")
    suspend fun getPaymentOptions() : Response<List<String?>>

    @Keep
    @GET("v1/checkout/addresses")
    suspend fun getAddresses() : Response<List<ArcXPAddress>>

    @Keep
    @GET("v1/checkout/order/{orderNumber}/payment/{mid}")
    suspend fun initializePayment(@Path("orderNumber")orderNumber: String, @Path("mid")mid: String) : Response<Void>

    @Keep
    @PUT("v1/checkout/order/{orderNumber}/payment/{mid}")
    suspend fun finalizePayment(@Path("orderNumber")orderNumber: String,
                                @Path("mid")mid: String,
                                @Body request: ArcXPFinalizePaymentRequest) : Response<ArcXPFinalizePayment>

    @Keep
    @PUT("v1/checkout/3ds/order/{orderNumber}/payment/{mid}")
    suspend fun finalizePayment3ds(@Path("orderNumber")orderNumber: String,
                                   @Path("mid")mid: String,
                                   @Body request: ArcXPFinalizePaymentRequest) : Response<ArcXPFinalizePayment>

    @Keep
    @GET("v1/order/history")
    suspend fun getOrderHistory() : Response<ArcXPOrderHistory>

    @Keep
    @GET("v1/order/detail/{orderNumber}")
    suspend fun getOrderDetails(@Path("orderNumber")orderNumber: String) : Response<ArcXPCustomerOrder>

    @Keep
    @DELETE("v1/cart/clear")
    suspend fun clearCart() : Response<ArcXPCustomerOrder>

    @Keep
    @GET("v1/cart")
    suspend fun getCurrentCart() : Response<ArcXPCustomerOrder>

    @Keep
    @POST("v1/cart/item")
    suspend fun addItemToCart(@Body request: ArcXPCartItemsRequest) : Response<ArcXPCustomerOrder>

    @Keep
    @DELETE("v1/cart/item/{sku}")
    suspend fun removeItemFromCart(@Path("sku")sku: String) : Response<ArcXPCustomerOrder>
}