package com.arcxp.commerce.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.media3.common.util.UnstableApi
import com.arcxp.commerce.callbacks.ArcXPSalesListener
import com.arcxp.commerce.models.*
import com.arcxp.commerce.repositories.SalesRepository
import com.arcxp.commons.throwables.ArcXPException
import com.arcxp.commons.util.Failure
import com.arcxp.commons.util.Success
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * @suppress
 */
@UnstableApi
public class SalesViewModel(
    private val repo: SalesRepository) : BaseAuthViewModel() {

    private val _subscriptionsResponse = MutableLiveData<ArcXPSubscriptions>()
    val subscriptionsResponse: LiveData<ArcXPSubscriptions> = _subscriptionsResponse

    private val _allSubscriptionsResponse = MutableLiveData<ArcXPSubscriptions>()
    val allSubscriptionsResponse: LiveData<ArcXPSubscriptions> = _allSubscriptionsResponse

    private val _entitlementsResponse = MutableLiveData<ArcXPEntitlements>()
    val entitlementsResponse: LiveData<ArcXPEntitlements> = _entitlementsResponse

    private val _entitlementsErrorResponse = MutableLiveData<ArcXPException>()
    val entitlementsErrorResponse: LiveData<ArcXPException> = _entitlementsErrorResponse

    private val _initializePaymentMethodResponse = MutableLiveData<Map<String, String>>()
    val initializePaymentMethodResponse: LiveData<Map<String, String>> = _initializePaymentMethodResponse

    private val _initializePaymentMethodError = MutableLiveData<ArcXPException>()
    val initializePaymentMethodError: LiveData<ArcXPException> = _initializePaymentMethodError

    private val _finalizePaymentMethodResponse = MutableLiveData<ArcXPFinalizePayment>()
    val finalizePaymentMethodResponse: LiveData<ArcXPFinalizePayment> = _finalizePaymentMethodResponse

    private val _finalizePaymentMethodError = MutableLiveData<ArcXPException>()
    val finalizePaymentMethodError: LiveData<ArcXPException> = _finalizePaymentMethodError

    private val _finalizePaymentMethod3dsResponse = MutableLiveData<ArcXPFinalizePayment>()
    val finalizePaymentMethod3dsResponse: LiveData<ArcXPFinalizePayment> = _finalizePaymentMethod3dsResponse

    private val _finalizePaymentMethod3dsError = MutableLiveData<ArcXPException>()
    val finalizePaymentMethod3dsError: LiveData<ArcXPException> = _finalizePaymentMethod3dsError

    private val _cancelSubscriptionResponse = MutableLiveData<ArcXPCancelSubscription>()
    val cancelSubscriptionResponse: LiveData<ArcXPCancelSubscription> = _cancelSubscriptionResponse

    private val _cancelSubscriptionError = MutableLiveData<ArcXPException>()
    val cancelSubscriptionError: LiveData<ArcXPException> = _cancelSubscriptionError

    private val _updateAddressResponse = MutableLiveData<ArcXPAddress>()
    val updateAddressResponse: LiveData<ArcXPAddress> = _updateAddressResponse

    private val _updateAddressError = MutableLiveData<ArcXPException>()
    val updateAddressError: LiveData<ArcXPException> = _updateAddressError

    private val _subscriptionDetailsResponse = MutableLiveData<ArcXPSubscriptionDetails>()
    val subscriptionDetailsResponse: LiveData<ArcXPSubscriptionDetails> = _subscriptionDetailsResponse

    private val _subscriptionDetailsError = MutableLiveData<ArcXPException>()
    val subscriptionDetailsError: LiveData<ArcXPException> = _subscriptionDetailsError

    private val _createCustomerOrderResponse = MutableLiveData<ArcXPCustomerOrder>()
    val createCustomerOrderResponse: LiveData<ArcXPCustomerOrder> = _createCustomerOrderResponse

    private val _createCustomerOrderError = MutableLiveData<ArcXPException>()
    val createCustomerOrderError: LiveData<ArcXPException> = _createCustomerOrderError

    private val _getPaymentOptionsResponse = MutableLiveData<List<String?>>()
    val getPaymentOptionsResponse: LiveData<List<String?>> = _getPaymentOptionsResponse

    private val _getPaymentOptionsError = MutableLiveData<ArcXPException>()
    val getPaymentOptionsError: LiveData<ArcXPException> = _getPaymentOptionsError

    private val _getAddressesResponse = MutableLiveData<List<ArcXPAddress?>>()
    val getAddressesResponse: LiveData<List<ArcXPAddress?>> = _getAddressesResponse

    private val _getAddressesError = MutableLiveData<ArcXPException>()
    val getAddressesError: LiveData<ArcXPException> = _getAddressesError

    private val _initializePaymentResponse = MutableLiveData<Void>()
    val initializePaymentResponse: LiveData<Void> = _initializePaymentResponse

    private val _initializePaymentError = MutableLiveData<ArcXPException>()
    val initializePaymentError: LiveData<ArcXPException> = _initializePaymentError

    private val _finalizePaymentResponse = MutableLiveData<ArcXPFinalizePayment>()
    val finalizePaymentResponse: LiveData<ArcXPFinalizePayment> = _finalizePaymentResponse

    private val _finalizePaymentError = MutableLiveData<ArcXPException>()
    val finalizePaymentError: LiveData<ArcXPException> = _finalizePaymentError

    private val _finalizePayment3dsResponse = MutableLiveData<ArcXPFinalizePayment>()
    val finalizePayment3dsResponse: LiveData<ArcXPFinalizePayment> = _finalizePayment3dsResponse

    private val _finalizePayment3dsError = MutableLiveData<ArcXPException>()
    val finalizePayment3dsError: LiveData<ArcXPException> = _finalizePayment3dsError

    private val _orderHistoryResponse = MutableLiveData<ArcXPOrderHistory>()
    val orderHistoryResponse: LiveData<ArcXPOrderHistory> = _orderHistoryResponse

    private val _orderHistoryError = MutableLiveData<ArcXPException>()
    val orderHistoryError: LiveData<ArcXPException> = _orderHistoryError

    private val _orderDetailsResponse = MutableLiveData<ArcXPCustomerOrder>()
    val orderDetailsResponse: LiveData<ArcXPCustomerOrder> = _orderDetailsResponse

    private val _orderDetailsError = MutableLiveData<ArcXPException>()
    val orderDetailsError: LiveData<ArcXPException> = _orderDetailsError

    private val _clearCartResponse = MutableLiveData<ArcXPCustomerOrder>()
    val clearCartResponse: LiveData<ArcXPCustomerOrder> = _clearCartResponse

    private val _clearCartError = MutableLiveData<ArcXPException>()
    val clearCartError: LiveData<ArcXPException> = _clearCartError

    private val _getCurrentCartResponse = MutableLiveData<ArcXPCustomerOrder>()
    val getCurrentCartResponse: LiveData<ArcXPCustomerOrder> = _getCurrentCartResponse

    private val _getCurrentCartError = MutableLiveData<ArcXPException>()
    val getCurrentCartError: LiveData<ArcXPException> = _getCurrentCartError

    private val _addItemToCartResponse = MutableLiveData<ArcXPCustomerOrder>()
    val addItemToCartResponse: LiveData<ArcXPCustomerOrder> = _addItemToCartResponse

    private val _addItemToCartError = MutableLiveData<ArcXPException>()
    val addItemToCartError: LiveData<ArcXPException> = _addItemToCartError

    private val _removeItemFromCartResponse = MutableLiveData<ArcXPCustomerOrder>()
    val removeItemFromCartResponse: LiveData<ArcXPCustomerOrder> = _removeItemFromCartResponse

    private val _removeItemFromCartError = MutableLiveData<ArcXPException>()
    val removeItemFromCartError: LiveData<ArcXPException> = _removeItemFromCartError

    private val _errorResponse = MutableLiveData<ArcXPException>()
    val errorResponse: LiveData<ArcXPException> = _errorResponse

    fun getAllSubscriptions(callback: ArcXPSalesListener?) {
        mIoScope.launch {
            val res = repo.getAllSubscriptions()
            withContext(mUiScope.coroutineContext) {
                when (res) {
                    is Success -> {
                        if (callback == null) {
                            _allSubscriptionsResponse.postValue(res.success!!)
                        } else {
                            callback.onGetAllSubscriptionsSuccess(res.success!!)
                        }
                    }
                    is Failure -> {
                        if (callback == null) {
                            _errorResponse.value = res.failure as ArcXPException /*handleFailure(res.failure)*/
                        } else {
                            callback.onGetSubscriptionsFailure(res.failure as ArcXPException)
                        }
                    }
                }
            }
        }
    }

    fun getAllActiveSubscriptions(callback: ArcXPSalesListener?) {
        mIoScope.launch {
            val res = repo.getAllActiveSubscriptions()
            withContext(mUiScope.coroutineContext) {
                when (res) {
                    is Success -> {
                        if (callback == null) {
                            _subscriptionsResponse.postValue(res.success!!)
                        } else {
                            callback.onGetAllActiveSubscriptionsSuccess(res.success!!)
                        }
                    }
                    is Failure -> {
                        if (callback == null) {
                            _errorResponse.value = res.failure as ArcXPException /*handleFailure(res.failure)*/
                        } else {
                            callback.onGetSubscriptionsFailure(res.failure as ArcXPException)
                        }
                    }
                }
            }
        }
    }

    fun getEntitlements(callback: ArcXPSalesListener?) {
        mIoScope.launch {
            val res = repo.getEntitlements()
            withContext(mUiScope.coroutineContext) {
                when (res) {
                    is Success -> {
                        if (callback == null) {
                            _entitlementsResponse.postValue(res.success!!)
                        } else {
                            callback.onGetEntitlementsSuccess(res.success!!)
                        }
                    }
                    is Failure -> {
                        if (callback == null) {
                            _entitlementsErrorResponse.value = res.failure as ArcXPException
                        } else {
                            callback.onGetEntitlementsFailure(res.failure as ArcXPException)
                        }
                    }
                }
            }
        }
    }

    fun initializePaymentMethod(id: String, pid: String, callback: ArcXPSalesListener?) {
        mIoScope.launch {
            val res = repo.initializePaymentMethod(id, pid)
            withContext(mUiScope.coroutineContext) {
                when (res) {
                    is Success -> {
                        if (callback == null) {
                            _initializePaymentMethodResponse.postValue(res.success!!)
                        } else {
                            callback.onInitializePaymentMethodSuccess(res.success!!)
                        }
                    }
                    is Failure -> {
                        if (callback == null) {
                            _initializePaymentMethodError.value = res.failure as ArcXPException
                        } else {
                            callback.onInitializePaymentMethodFailure(res.failure as ArcXPException)
                        }
                    }
                }
            }
        }
    }

    fun finalizePaymentMethod(id: String, pid: String, request: ArcXPFinalizePaymentRequest, callback: ArcXPSalesListener?) {
        mIoScope.launch {
            val res = repo.finalizePaymentMethod(id, pid, request)
            withContext(mUiScope.coroutineContext) {
                when (res) {
                    is Success -> {
                        if (callback == null) {
                            _finalizePaymentMethodResponse.postValue(res.success!!)
                        } else {
                            callback.onFinalizePaymentMethodSuccess(res.success)
                        }
                    }
                    is Failure -> {
                        if (callback == null) {
                            _finalizePaymentMethodError.value = res.failure as ArcXPException
                        } else {
                            callback.onFinalizePaymentMethodFailure(res.failure as ArcXPException)
                        }
                    }
                }
            }
        }
    }

    fun finalizePaymentMethod3ds(id: String, pid: String, request: ArcXPFinalizePaymentRequest, callback: ArcXPSalesListener?) {
        mIoScope.launch {
            val res = repo.finalizePaymentMethod3ds(id, pid, request)
            withContext(mUiScope.coroutineContext) {
                when (res) {
                    is Success -> {
                        if (callback == null) {
                            _finalizePaymentMethod3dsResponse.postValue(res.success!!)
                        } else {
                            callback.onFinalizePaymentMethod3dsSuccess(res.success)
                        }
                    }
                    is Failure -> {
                        if (callback == null) {
                            _finalizePaymentMethod3dsError.value = res.failure as ArcXPException
                        } else {
                            callback.onFinalizePaymentMethod3dsFailure(res.failure as ArcXPException)
                        }
                    }
                }
            }
        }
    }

    fun cancelSubscription(id: String, request: ArcXPCancelSubscriptionRequest, callback: ArcXPSalesListener?) {
        mIoScope.launch {
            val res = repo.cancelSubscription(id, request)
            withContext(mUiScope.coroutineContext) {
                when (res) {
                    is Success -> {
                        if (callback == null) {
                            _cancelSubscriptionResponse.postValue(res.success!!)
                        } else {
                            callback.onCancelSubscriptionSuccess(res.success)
                        }
                    }
                    is Failure -> {
                        if (callback == null) {
                            _cancelSubscriptionError.value = res.failure as ArcXPException
                        } else {
                            callback.onCancelSubscriptionFailure(res.failure as ArcXPException)
                        }
                    }
                }
            }
        }
    }

    fun updateAddress(request: ArcXPUpdateAddressRequest, callback: ArcXPSalesListener?) {
        mIoScope.launch {
            val res = repo.updateAddress(request)
            withContext(mUiScope.coroutineContext) {
                when (res) {
                    is Success -> {
                        if (callback == null) {
                            _updateAddressResponse.postValue(res.success!!)
                        } else {
                            callback.onUpdateAddressSuccess(res.success)
                        }
                    }
                    is Failure -> {
                        if (callback == null) {
                            _updateAddressError.value = res.failure as ArcXPException
                        } else {
                            callback.onUpdateAddressFailure(res.failure as ArcXPException)
                        }
                    }
                }
            }
        }
    }

    fun getSubscriptionDetails(id: String, callback: ArcXPSalesListener?) {
        mIoScope.launch {
            val res = repo.getSubscriptionsDetails(id)
            withContext(mUiScope.coroutineContext) {
                when (res) {
                    is Success -> {
                        if (callback == null) {
                            _subscriptionDetailsResponse.postValue(res.success!!)
                        } else {
                            callback.onGetSubscriptionDetailsSuccess(res.success)
                        }
                    }
                    is Failure -> {
                        if (callback == null) {
                            _subscriptionDetailsError.value = res.failure as ArcXPException
                        } else {
                            callback.onGetSubscriptionDetailsFailure(res.failure as ArcXPException)
                        }
                    }
                }
            }
        }
    }

    fun createCustomerOrder(request: ArcXPCustomerOrderRequest, callback: ArcXPSalesListener?) {
        mIoScope.launch {
            val res = repo.createCustomerOrder(request)
            withContext(mUiScope.coroutineContext) {
                when (res) {
                    is Success -> {
                        if (callback == null) {
                            _createCustomerOrderResponse.postValue(res.success!!)
                        } else {
                            callback.onCreateCustomerOrderSuccess(res.success)
                        }
                    }
                    is Failure -> {
                        if (callback == null) {
                            _createCustomerOrderError.value = res.failure as ArcXPException
                        } else {
                            callback.onCreateCustomerOrderFailure(res.failure as ArcXPException)
                        }
                    }
                }
            }
        }
    }

    fun getPaymentOptions(callback: ArcXPSalesListener?) {
        mIoScope.launch {
            val res = repo.getPaymentOptions()
            withContext(mUiScope.coroutineContext) {
                when (res) {
                    is Success -> {
                        if (callback == null) {
                            _getPaymentOptionsResponse.postValue(res.success!!)
                        } else {
                            callback.onGetPaymentOptionsSuccess(res.success)
                        }
                    }
                    is Failure -> {
                        if (callback == null) {
                            _getPaymentOptionsError.value = res.failure as ArcXPException
                        } else {
                            callback.onGetPaymentOptionsFailure(res.failure as ArcXPException)
                        }
                    }
                }
            }
        }
    }

    fun getPaymentAddresses(callback: ArcXPSalesListener?) {
        mIoScope.launch {
            val res = repo.getAddresses()
            withContext(mUiScope.coroutineContext) {
                when (res) {
                    is Success -> {
                        if (callback == null) {
                            _getAddressesResponse.postValue(res.success!!)
                        } else {
                            callback.onGetAddressesSuccess(res.success)
                        }
                    }
                    is Failure -> {
                        if (callback == null) {
                            _getAddressesError.value = res.failure as ArcXPException
                        } else {
                            callback.onGetAddressesFailure(res.failure as ArcXPException)
                        }
                    }
                }
            }
        }
    }

    fun initializePayment(orderNumber: String, mid: String, callback: ArcXPSalesListener?) {
        mIoScope.launch {
            val res = repo.initializePayment(orderNumber, mid)
            withContext(mUiScope.coroutineContext) {
                when (res) {
                    is Success -> {
                        if (callback == null) {
                            _initializePaymentResponse.postValue(res.success!!)
                        } else {
                            callback.onInitializePaymentSuccess()
                        }
                    }
                    is Failure -> {
                        if (callback == null) {
                            _initializePaymentError.value = res.failure as ArcXPException
                        } else {
                            callback.onInitializePaymentFailure(res.failure as ArcXPException)
                        }
                    }
                }
            }
        }
    }

    fun finalizePayment(orderNumber: String, mid: String, request: ArcXPFinalizePaymentRequest, callback: ArcXPSalesListener?) {
        mIoScope.launch {
            val res = repo.finalizePayment(orderNumber, mid, request)
            withContext(mUiScope.coroutineContext) {
                when (res) {
                    is Success -> {
                        if (callback == null) {
                            _finalizePaymentResponse.postValue(res.success!!)
                        } else {
                            callback.onFinalizePaymentSuccess(res.success)
                        }
                    }
                    is Failure -> {
                        if (callback == null) {
                            _finalizePaymentError.value = res.failure as ArcXPException
                        } else {
                            callback.onFinalizePaymentFailure(res.failure as ArcXPException)
                        }
                    }
                }
            }
        }
    }

    fun finalizePayment3ds(orderNumber: String, mid: String, request: ArcXPFinalizePaymentRequest, callback: ArcXPSalesListener?) {
        mIoScope.launch {
            val res = repo.finalizePayment3ds(orderNumber, mid, request)
            withContext(mUiScope.coroutineContext) {
                when (res) {
                    is Success -> {
                        if (callback == null) {
                            _finalizePayment3dsResponse.postValue(res.success!!)
                        } else {
                            callback.onFinalizePayment3dsSuccess(res.success)
                        }
                    }
                    is Failure -> {
                        if (callback == null) {
                            _finalizePayment3dsError.value = res.failure as ArcXPException
                        } else {
                            callback.onFinalizePayment3dsFailure(res.failure as ArcXPException)
                        }
                    }
                }
            }
        }
    }

    fun getOrderHistory(callback: ArcXPSalesListener?) {
        mIoScope.launch {
            val res = repo.getOrderHistory()
            withContext(mUiScope.coroutineContext) {
                when (res) {
                    is Success -> {
                        if (callback == null) {
                            _orderHistoryResponse.postValue(res.success!!)
                        } else {
                            callback.onOrderHistorySuccess(res.success)
                        }
                    }
                    is Failure -> {
                        if (callback == null) {
                            _orderHistoryError.value = res.failure as ArcXPException
                        } else {
                            callback.onOrderHistoryFailure(res.failure as ArcXPException)
                        }
                    }
                }
            }
        }
    }

    fun getOrderDetails(orderNumber: String, callback: ArcXPSalesListener?) {
        mIoScope.launch {
            val res = repo.getOrderDetails(orderNumber)
            withContext(mUiScope.coroutineContext) {
                when (res) {
                    is Success -> {
                        if (callback == null) {
                            _orderDetailsResponse.postValue(res.success!!)
                        } else {
                            callback.onOrderDetailsSuccess(res.success)
                        }
                    }
                    is Failure -> {
                        if (callback == null) {
                            _orderDetailsError.value = res.failure as ArcXPException
                        } else {
                            callback.onOrderDetailsFailure(res.failure as ArcXPException)
                        }
                    }
                }
            }
        }
    }

    fun clearCart(callback: ArcXPSalesListener?) {
        mIoScope.launch {
            val res = repo.clearCart()
            withContext(mUiScope.coroutineContext) {
                when (res) {
                    is Success -> {
                        if (callback == null) {
                            _clearCartResponse.postValue(res.success!!)
                        } else {
                            callback.onClearCartSuccess(res.success)
                        }
                    }
                    is Failure -> {
                        if (callback == null) {
                            _clearCartError.value = res.failure as ArcXPException
                        } else {
                            callback.onClearCartFailure(res.failure as ArcXPException)
                        }
                    }
                }
            }
        }
    }

    fun getCurrentCart(callback: ArcXPSalesListener?) {
        mIoScope.launch {
            val res = repo.getCurrentCart()
            withContext(mUiScope.coroutineContext) {
                when (res) {
                    is Success -> {
                        if (callback == null) {
                            _getCurrentCartResponse.postValue(res.success!!)
                        } else {
                            callback.onGetCurrentCartSuccess(res.success)
                        }
                    }
                    is Failure -> {
                        if (callback == null) {
                            _getCurrentCartError.value = res.failure as ArcXPException
                        } else {
                            callback.onGetCurrentCartFailure(res.failure as ArcXPException)
                        }
                    }
                }
            }
        }
    }

    fun addItemToCart(request: ArcXPCartItemsRequest, callback: ArcXPSalesListener?) {
        mIoScope.launch {
            val res = repo.addItemToCart(request)
            withContext(mUiScope.coroutineContext) {
                when (res) {
                    is Success -> {
                        if (callback == null) {
                            _addItemToCartResponse.postValue(res.success!!)
                        } else {
                            callback.onAddItemToCartSuccess(res.success)
                        }
                    }
                    is Failure -> {
                        if (callback == null) {
                            _addItemToCartError.value = res.failure as ArcXPException
                        } else {
                            callback.onAddItemToCartFailure(res.failure as ArcXPException)
                        }
                    }
                }
            }
        }
    }

    fun removeItemFromCart(sku: String, callback: ArcXPSalesListener?) {
        mIoScope.launch {
            val res = repo.removeItemFromCart(sku)
            withContext(mUiScope.coroutineContext) {
                when (res) {
                    is Success -> {
                        if (callback == null) {
                            _removeItemFromCartResponse.postValue(res.success!!)
                        } else {
                            callback.onRemoveItemFromCartSuccess(res.success)
                        }
                    }
                    is Failure -> {
                        if (callback == null) {
                            _removeItemFromCartError.value = res.failure as ArcXPException
                        } else {
                            callback.onRemoveItemFromCartFailure(res.failure as ArcXPException)
                        }
                    }
                }
            }
        }
    }
}
