package com.arcxp.commerce.repositories

import com.arcxp.commons.throwables.ArcXPSDKErrorType
import com.arcxp.commerce.models.*
import com.arcxp.commerce.retrofit.RetrofitController
import com.arcxp.commerce.retrofit.SalesService
import com.arcxp.commons.throwables.ArcXPException
import com.arcxp.commons.util.DependencyFactory.createArcXPException
import com.arcxp.commons.util.Either
import com.arcxp.commons.util.Failure
import com.arcxp.commons.util.Success

/**
 * @suppress
 */
class SalesRepository(private val salesService: SalesService = RetrofitController.getSalesService()) {

    suspend fun getAllActiveSubscriptions(): Either<ArcXPException, ArcXPSubscriptions> =
        try {
            val response = salesService.getAllActiveSubscriptions()
            with(response) {
                when {
                    isSuccessful -> Success(ArcXPSubscriptions(body()!!))
                    else -> Failure(createArcXPException(
                        type = ArcXPSDKErrorType.SERVER_ERROR,
                        message = response.message(),
                        value = response
                    ))
                }
            }
        } catch (e: Exception) {
            Failure(createArcXPException(ArcXPSDKErrorType.SERVER_ERROR, e.message!!, e))
        }

    suspend fun getAllSubscriptions(): Either<Any?, ArcXPSubscriptions?> =
        try {
            val response = salesService.getAllSubscriptions()
            with(response) {
                when {
                    isSuccessful -> Success(ArcXPSubscriptions(body()!!))
                    else -> Failure(createArcXPException(ArcXPSDKErrorType.SERVER_ERROR, response.message(), response))
                }
            }
        } catch (e: Exception) {
            Failure(createArcXPException(ArcXPSDKErrorType.SERVER_ERROR, e.message!!, e))
        }

    suspend fun getEntitlements(): Either<Any?, ArcXPEntitlements?> =
        try {
            val response = salesService.getEntitlements()
            with(response) {
                when {
                    isSuccessful -> Success(body()!!)
                    else -> Failure(createArcXPException(ArcXPSDKErrorType.SERVER_ERROR, response.message(), response))
                }
            }
        } catch (e: Exception) {
            Failure(createArcXPException(ArcXPSDKErrorType.SERVER_ERROR, e.message!!, e))
        }

    suspend fun initializePaymentMethod(id: String, pid: String): Either<Any?, Map<String,String>?> =
        try {
            val response = salesService.initializePaymentMethod(id, pid)
            with(response) {
                when {
                    isSuccessful -> Success(body()!!)
                    else -> Failure(createArcXPException(ArcXPSDKErrorType.SERVER_ERROR, response.message(), response))
                }
            }
        } catch (e: Exception) {
            Failure(createArcXPException(ArcXPSDKErrorType.SERVER_ERROR, e.message!!, e))
        }

    suspend fun finalizePaymentMethod(id: String, pid: String, request: ArcXPFinalizePaymentRequest): Either<Any?, ArcXPFinalizePayment> =
        try {
            val response = salesService.finalizePaymentMethod(id, pid, request)
            with(response) {
                when {
                    isSuccessful -> Success(body()!!)
                    else -> Failure(createArcXPException(ArcXPSDKErrorType.SERVER_ERROR, response.message(), response))
                }
            }
        } catch (e: Exception) {
            Failure(createArcXPException(ArcXPSDKErrorType.SERVER_ERROR, e.message!!, e))
        }

    suspend fun finalizePaymentMethod3ds(id: String, pid: String, request: ArcXPFinalizePaymentRequest): Either<Any?, ArcXPFinalizePayment> =
        try {
            val response = salesService.finalizePaymentMethod3ds(id, pid, request)
            with(response) {
                when {
                    isSuccessful -> Success(body()!!)
                    else -> Failure(createArcXPException(ArcXPSDKErrorType.SERVER_ERROR, response.message(), response))
                }
            }
        } catch (e: Exception) {
            Failure(createArcXPException(ArcXPSDKErrorType.SERVER_ERROR, e.message!!, e))
        }

    suspend fun cancelSubscription(id: String, request: ArcXPCancelSubscriptionRequest): Either<Any?, ArcXPCancelSubscription> =
        try {
            val response = salesService.cancelSubscription(id, request)
            with(response) {
                when {
                    isSuccessful -> Success(body()!!)
                    else -> Failure(createArcXPException(ArcXPSDKErrorType.SERVER_ERROR, response.message(), response))
                }
            }
        } catch (e: Exception) {
            Failure(createArcXPException(ArcXPSDKErrorType.SERVER_ERROR, e.message!!, e))
        }

    suspend fun updateAddress(request: ArcXPUpdateAddressRequest): Either<Any?, ArcXPAddress> =
        try {
            val response = salesService.updateAddress(request)
            with(response) {
                when {
                    isSuccessful -> Success(body()!!)
                    else -> Failure(createArcXPException(ArcXPSDKErrorType.SERVER_ERROR, response.message(), response))
                }
            }
        } catch (e: Exception) {
            Failure(createArcXPException(ArcXPSDKErrorType.SERVER_ERROR, e.message!!, e))
        }

    suspend fun getSubscriptionsDetails(id: String): Either<Any?, ArcXPSubscriptionDetails> =
        try {
            val response = salesService.getSubscriptionsDetails(id)
            with(response) {
                when {
                    isSuccessful -> Success(body()!!)
                    else -> Failure(createArcXPException(ArcXPSDKErrorType.SERVER_ERROR, response.message(), response))
                }
            }
        } catch (e: Exception) {
            Failure(createArcXPException(ArcXPSDKErrorType.SERVER_ERROR, e.message!!, e))
        }

    suspend fun createCustomerOrder(request: ArcXPCustomerOrderRequest): Either<Any?, ArcXPCustomerOrder> =
        try {
            val response = salesService.createCustomerOrder(request)
            with(response) {
                when {
                    isSuccessful -> Success(body()!!)
                    else -> Failure(createArcXPException(ArcXPSDKErrorType.SERVER_ERROR, response.message(), response))
                }
            }
        } catch (e: Exception) {
            Failure(createArcXPException(ArcXPSDKErrorType.SERVER_ERROR, e.message!!, e))
        }

    suspend fun getPaymentOptions(): Either<Any?, List<String?>> =
        try {
            val response = salesService.getPaymentOptions()
            with(response) {
                when {
                    isSuccessful -> Success(body()!!)
                    else -> Failure(createArcXPException(ArcXPSDKErrorType.SERVER_ERROR, response.message(), response))
                }
            }
        } catch (e: Exception) {
            Failure(createArcXPException(ArcXPSDKErrorType.SERVER_ERROR, e.message!!, e))
        }

    suspend fun getAddresses(): Either<Any?, List<ArcXPAddress?>> =
        try {
            val response = salesService.getAddresses()
            with(response) {
                when {
                    isSuccessful -> Success(body()!!)
                    else -> Failure(createArcXPException(ArcXPSDKErrorType.SERVER_ERROR, response.message(), response))
                }
            }
        } catch (e: Exception) {
            Failure(createArcXPException(ArcXPSDKErrorType.SERVER_ERROR, e.message!!, e))
        }

    suspend fun initializePayment(orderNumber: String, mid: String): Either<Any?, Void> =
        try {
            val response = salesService.initializePayment(orderNumber, mid)
            with(response) {
                when {
                    isSuccessful -> Success(body()!!)//TODO check this lint, this should fail
                    else -> Failure(createArcXPException(ArcXPSDKErrorType.SERVER_ERROR, response.message(), response))
                }
            }
        } catch (e: Exception) {
            Failure(createArcXPException(ArcXPSDKErrorType.SERVER_ERROR, e.message!!, e))
        }

    suspend fun finalizePayment(orderNumber: String, mid: String, request: ArcXPFinalizePaymentRequest): Either<Any?, ArcXPFinalizePayment> =
        try {
            val response = salesService.finalizePayment(orderNumber, mid, request)
            with(response) {
                when {
                    isSuccessful -> Success(body()!!)
                    else -> Failure(createArcXPException(ArcXPSDKErrorType.SERVER_ERROR, response.message(), response))
                }
            }
        } catch (e: Exception) {
            Failure(createArcXPException(ArcXPSDKErrorType.SERVER_ERROR, e.message!!, e))
        }

    suspend fun finalizePayment3ds(orderNumber: String, mid: String, request: ArcXPFinalizePaymentRequest): Either<Any?, ArcXPFinalizePayment> =
        try {
            val response = salesService.finalizePayment3ds(orderNumber, mid, request)
            with(response) {
                when {
                    isSuccessful -> Success(body()!!)
                    else -> Failure(createArcXPException(ArcXPSDKErrorType.SERVER_ERROR, response.message(), response))
                }
            }
        } catch (e: Exception) {
            Failure(createArcXPException(ArcXPSDKErrorType.SERVER_ERROR, e.message!!, e))
        }

    suspend fun getOrderHistory(): Either<Any?, ArcXPOrderHistory> =
        try {
            val response = salesService.getOrderHistory()
            with(response) {
                when {
                    isSuccessful -> Success(body()!!)
                    else -> Failure(createArcXPException(ArcXPSDKErrorType.SERVER_ERROR, response.message(), response))
                }
            }
        } catch (e: Exception) {
            Failure(createArcXPException(ArcXPSDKErrorType.SERVER_ERROR, e.message!!, e))
        }

    suspend fun getOrderDetails(orderNumber: String): Either<Any?, ArcXPCustomerOrder> =
        try {
            val response = salesService.getOrderDetails(orderNumber)
            with(response) {
                when {
                    isSuccessful -> Success(body()!!)
                    else -> Failure(createArcXPException(ArcXPSDKErrorType.SERVER_ERROR, response.message(), response))
                }
            }
        } catch (e: Exception) {
            Failure(createArcXPException(ArcXPSDKErrorType.SERVER_ERROR, e.message!!, e))
        }

    suspend fun clearCart(): Either<Any?, ArcXPCustomerOrder> =
        try {
            val response = salesService.clearCart()
            with(response) {
                when {
                    isSuccessful -> Success(body()!!)
                    else -> Failure(createArcXPException(ArcXPSDKErrorType.SERVER_ERROR, response.message(), response))
                }
            }
        } catch (e: Exception) {
            Failure(createArcXPException(ArcXPSDKErrorType.SERVER_ERROR, e.message!!, e))
        }

    suspend fun getCurrentCart(): Either<Any?, ArcXPCustomerOrder> =
        try {
            val response = salesService.getCurrentCart()
            with(response) {
                when {
                    isSuccessful -> Success(body()!!)
                    else -> Failure(createArcXPException(ArcXPSDKErrorType.SERVER_ERROR, response.message(), response))
                }
            }
        } catch (e: Exception) {
            Failure(createArcXPException(ArcXPSDKErrorType.SERVER_ERROR, e.message!!, e))
        }

    suspend fun addItemToCart(request: ArcXPCartItemsRequest): Either<Any?, ArcXPCustomerOrder> =
        try {
            val response = salesService.addItemToCart(request)
            with(response) {
                when {
                    isSuccessful -> Success(body()!!)
                    else -> Failure(createArcXPException(ArcXPSDKErrorType.SERVER_ERROR, response.message(), response))
                }
            }
        } catch (e: Exception) {
            Failure(createArcXPException(ArcXPSDKErrorType.SERVER_ERROR, e.message!!, e))
        }

    suspend fun removeItemFromCart(sku: String): Either<Any?, ArcXPCustomerOrder> =
        try {
            val response = salesService.removeItemFromCart(sku)
            with(response) {
                when {
                    isSuccessful -> Success(body()!!)
                    else -> Failure(createArcXPException(ArcXPSDKErrorType.SERVER_ERROR, response.message(), response))
                }
            }
        } catch (e: Exception) {
            Failure(createArcXPException(ArcXPSDKErrorType.SERVER_ERROR, e.message!!, e))
        }
}
