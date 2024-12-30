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
 * SalesRepository is responsible for handling sales-related data operations within the ArcXP Commerce module.
 * It interacts with the SalesService to perform various sales operations such as managing subscriptions, entitlements, payment methods, orders, and cart operations.
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
 * - Create an instance of SalesRepository and call the provided methods to perform sales operations.
 * - Handle the results through the Either type, which encapsulates success and failure cases.
 *
 * Example:
 *
 * val salesRepository = SalesRepository()
 * val result = salesRepository.getAllActiveSubscriptions()
 *
 * Note: Ensure that the RetrofitController and SalesService are properly configured before using SalesRepository.
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
            Failure(createArcXPException(ArcXPSDKErrorType.EXCEPTION, e.message, e))
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
            Failure(createArcXPException(ArcXPSDKErrorType.EXCEPTION, e.message, e))
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
            Failure(createArcXPException(ArcXPSDKErrorType.EXCEPTION, e.message, e))
        }

    suspend fun getEntitlementsV2(): Either<Any?, ArcXPEntitlements?> =
        try {
            val response = salesService.getEntitlementsV2()
            with(response) {
                when {
                    isSuccessful -> Success(body()!!)
                    else -> Failure(createArcXPException(ArcXPSDKErrorType.SERVER_ERROR, response.message(), response))
                }
            }
        } catch (e: Exception) {
            Failure(createArcXPException(ArcXPSDKErrorType.EXCEPTION, e.message, e))
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
            Failure(createArcXPException(ArcXPSDKErrorType.EXCEPTION, e.message, e))
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
            Failure(createArcXPException(ArcXPSDKErrorType.EXCEPTION, e.message, e))
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
            Failure(createArcXPException(ArcXPSDKErrorType.EXCEPTION, e.message, e))
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
            Failure(createArcXPException(ArcXPSDKErrorType.EXCEPTION, e.message, e))
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
            Failure(createArcXPException(ArcXPSDKErrorType.EXCEPTION, e.message, e))
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
            Failure(createArcXPException(ArcXPSDKErrorType.EXCEPTION, e.message, e))
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
            Failure(createArcXPException(ArcXPSDKErrorType.EXCEPTION, e.message, e))
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
            Failure(createArcXPException(ArcXPSDKErrorType.EXCEPTION, e.message, e))
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
            Failure(createArcXPException(ArcXPSDKErrorType.EXCEPTION, e.message, e))
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
            Failure(createArcXPException(ArcXPSDKErrorType.EXCEPTION, e.message, e))
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
            Failure(createArcXPException(ArcXPSDKErrorType.EXCEPTION, e.message, e))
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
            Failure(createArcXPException(ArcXPSDKErrorType.EXCEPTION, e.message, e))
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
            Failure(createArcXPException(ArcXPSDKErrorType.EXCEPTION, e.message, e))
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
            Failure(createArcXPException(ArcXPSDKErrorType.EXCEPTION, e.message, e))
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
            Failure(createArcXPException(ArcXPSDKErrorType.EXCEPTION, e.message, e))
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
            Failure(createArcXPException(ArcXPSDKErrorType.EXCEPTION, e.message, e))
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
            Failure(createArcXPException(ArcXPSDKErrorType.EXCEPTION, e.message, e))
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
            Failure(createArcXPException(ArcXPSDKErrorType.EXCEPTION, e.message, e))
        }
}
