package com.arcxp.commerce.retrofit

import androidx.annotation.Keep
import com.arcxp.commerce.models.*
import retrofit2.Response
import retrofit2.http.*

/**
 * @suppress
 */
interface SalesService {

    @Keep
    @GET("subscription/allactive")
    suspend fun getAllActiveSubscriptions(): Response<List<SubscriptionSummary>>

    @Keep
    @GET("subscription/all")
    suspend fun getAllSubscriptions(): Response<List<SubscriptionSummary>>

    @Keep
    @GET("entitlements")
    suspend fun getEntitlements(): Response<ArcXPEntitlements>

    @Keep
    @PUT("paymentmethod/{id}/provider/{pid}")
    suspend fun initializePaymentMethod(@Path("id")id: String, @Path("pid")pid: String) : Response<Map<String,String>>

    @Keep
    @PUT("paymentmethod/{id}/provider/{pid}/finalize")
    suspend fun finalizePaymentMethod(@Path("id")id: String,
                                      @Path("pid")pid: String,
                                      @Body request: ArcXPFinalizePaymentRequest
    ): Response<ArcXPFinalizePayment>

    @Keep
    @PUT("paymentmethod/{id}/provider/{pid}/finalize3ds")
    suspend fun finalizePaymentMethod3ds(@Path("id")id: String,
                                   @Path("pid")pid: String,
                                   @Body request: ArcXPFinalizePaymentRequest
    ): Response<ArcXPFinalizePayment>

    @Keep
    @PUT("subscription/{id}/cancel")
    suspend fun cancelSubscription(@Path("id")id: String,
                                   @Body request: ArcXPCancelSubscriptionRequest
    ) : Response<ArcXPCancelSubscription>


    @Keep
    @PUT("subscriptions/address")
    suspend fun updateAddress(@Body request: ArcXPUpdateAddressRequest): Response<ArcXPAddress>

    @Keep
    @GET("subscription/{id}/details")
    suspend fun getSubscriptionsDetails(@Path("id")id: String) : Response<ArcXPSubscriptionDetails>

    @Keep
    @POST("checkout/order")
    suspend fun createCustomerOrder(@Body request: ArcXPCustomerOrderRequest): Response<ArcXPCustomerOrder>

    @Keep
    @GET("payment/options")
    suspend fun getPaymentOptions() : Response<List<String?>>

    @Keep
    @GET("checkout/addresses")
    suspend fun getAddresses() : Response<List<ArcXPAddress>>

    @Keep
    @GET("checkout/order/{orderNumber}/payment/{mid}")
    suspend fun initializePayment(@Path("orderNumber")orderNumber: String, @Path("mid")mid: String) : Response<Void>

    @Keep
    @PUT("checkout/order/{orderNumber}/payment/{mid}")
    suspend fun finalizePayment(@Path("orderNumber")orderNumber: String,
                                @Path("mid")mid: String,
                                @Body request: ArcXPFinalizePaymentRequest) : Response<ArcXPFinalizePayment>

    @Keep
    @PUT("checkout/3ds/order/{orderNumber}/payment/{mid}")
    suspend fun finalizePayment3ds(@Path("orderNumber")orderNumber: String,
                                   @Path("mid")mid: String,
                                   @Body request: ArcXPFinalizePaymentRequest) : Response<ArcXPFinalizePayment>

    @Keep
    @GET("order/history")
    suspend fun getOrderHistory() : Response<ArcXPOrderHistory>

    @Keep
    @GET("order/detail/{orderNumber}")
    suspend fun getOrderDetails(@Path("orderNumber")orderNumber: String) : Response<ArcXPCustomerOrder>

    @Keep
    @DELETE("cart/clear")
    suspend fun clearCart() : Response<ArcXPCustomerOrder>

    @Keep
    @GET("cart")
    suspend fun getCurrentCart() : Response<ArcXPCustomerOrder>

    @Keep
    @POST("cart/item")
    suspend fun addItemToCart(@Body request: ArcXPCartItemsRequest) : Response<ArcXPCustomerOrder>

    @Keep
    @DELETE("cart/item/{sku}")
    suspend fun removeItemFromCart(@Path("sku")sku: String) : Response<ArcXPCustomerOrder>
}