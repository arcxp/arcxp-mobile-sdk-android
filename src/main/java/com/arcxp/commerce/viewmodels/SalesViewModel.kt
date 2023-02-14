package com.arcxp.commerce.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.arcxp.commerce.apimanagers.ArcXPSalesListener
import com.arcxp.commerce.models.*
import com.arcxp.commerce.repositories.SalesRepository
import com.arcxp.commerce.util.ArcXPError
import com.arcxp.commerce.util.Failure
import com.arcxp.commerce.util.Success
import kotlinx.coroutines.*

/**
 * @suppress
 */
public class SalesViewModel(
    private val repo: SalesRepository) : BaseAuthViewModel() {

    private val _subscriptionsResponse = MutableLiveData<ArcXPSubscriptions>()
    val subscriptionsResponse: LiveData<ArcXPSubscriptions> = _subscriptionsResponse

    private val _allSubscriptionsResponse = MutableLiveData<ArcXPSubscriptions>()
    val allSubscriptionsResponse: LiveData<ArcXPSubscriptions> = _allSubscriptionsResponse

    private val _entitlementsResponse = MutableLiveData<ArcXPEntitlements>()
    val entitlementsResponse: LiveData<ArcXPEntitlements> = _entitlementsResponse

    private val _entitlementsErrorResponse = MutableLiveData<ArcXPError>()
    val entitlementsErrorResponse: LiveData<ArcXPError> = _entitlementsErrorResponse

    private val _initializePaymentMethodResponse = MutableLiveData<Map<String, String>>()
    val initializePaymentMethodResponse: LiveData<Map<String, String>> = _initializePaymentMethodResponse

    private val _initializePaymentMethodError = MutableLiveData<ArcXPError>()
    val initializePaymentMethodError: LiveData<ArcXPError> = _initializePaymentMethodError

    private val _finalizePaymentMethodResponse = MutableLiveData<ArcXPFinalizePayment>()
    val finalizePaymentMethodResponse: LiveData<ArcXPFinalizePayment> = _finalizePaymentMethodResponse

    private val _finalizePaymentMethodError = MutableLiveData<ArcXPError>()
    val finalizePaymentMethodError: LiveData<ArcXPError> = _finalizePaymentMethodError

    private val _finalizePaymentMethod3dsResponse = MutableLiveData<ArcXPFinalizePayment>()
    val finalizePaymentMethod3dsResponse: LiveData<ArcXPFinalizePayment> = _finalizePaymentMethod3dsResponse

    private val _finalizePaymentMethod3dsError = MutableLiveData<ArcXPError>()
    val finalizePaymentMethod3dsError: LiveData<ArcXPError> = _finalizePaymentMethod3dsError

    private val _cancelSubscriptionResponse = MutableLiveData<ArcXPCancelSubscription>()
    val cancelSubscriptionResponse: LiveData<ArcXPCancelSubscription> = _cancelSubscriptionResponse

    private val _cancelSubscriptionError = MutableLiveData<ArcXPError>()
    val cancelSubscriptionError: LiveData<ArcXPError> = _cancelSubscriptionError

    private val _updateAddressResponse = MutableLiveData<ArcXPAddress>()
    val updateAddressResponse: LiveData<ArcXPAddress> = _updateAddressResponse

    private val _updateAddressError = MutableLiveData<ArcXPError>()
    val updateAddressError: LiveData<ArcXPError> = _updateAddressError

    private val _subscriptionDetailsResponse = MutableLiveData<ArcXPSubscriptionDetails>()
    val subscriptionDetailsResponse: LiveData<ArcXPSubscriptionDetails> = _subscriptionDetailsResponse

    private val _subscriptionDetailsError = MutableLiveData<ArcXPError>()
    val subscriptionDetailsError: LiveData<ArcXPError> = _subscriptionDetailsError

    private val _createCustomerOrderResponse = MutableLiveData<ArcXPCustomerOrder>()
    val createCustomerOrderResponse: LiveData<ArcXPCustomerOrder> = _createCustomerOrderResponse

    private val _createCustomerOrderError = MutableLiveData<ArcXPError>()
    val createCustomerOrderError: LiveData<ArcXPError> = _createCustomerOrderError

    private val _getPaymentOptionsResponse = MutableLiveData<List<String?>>()
    val getPaymentOptionsResponse: LiveData<List<String?>> = _getPaymentOptionsResponse

    private val _getPaymentOptionsError = MutableLiveData<ArcXPError>()
    val getPaymentOptionsError: LiveData<ArcXPError> = _getPaymentOptionsError

    private val _getAddressesResponse = MutableLiveData<List<ArcXPAddress?>>()
    val getAddressesResponse: LiveData<List<ArcXPAddress?>> = _getAddressesResponse

    private val _getAddressesError = MutableLiveData<ArcXPError>()
    val getAddressesError: LiveData<ArcXPError> = _getAddressesError

    private val _initializePaymentResponse = MutableLiveData<Void>()
    val initializePaymentResponse: LiveData<Void> = _initializePaymentResponse

    private val _initializePaymentError = MutableLiveData<ArcXPError>()
    val initializePaymentError: LiveData<ArcXPError> = _initializePaymentError

    private val _finalizePaymentResponse = MutableLiveData<ArcXPFinalizePayment>()
    val finalizePaymentResponse: LiveData<ArcXPFinalizePayment> = _finalizePaymentResponse

    private val _finalizePaymentError = MutableLiveData<ArcXPError>()
    val finalizePaymentError: LiveData<ArcXPError> = _finalizePaymentError

    private val _finalizePayment3dsResponse = MutableLiveData<ArcXPFinalizePayment>()
    val finalizePayment3dsResponse: LiveData<ArcXPFinalizePayment> = _finalizePayment3dsResponse

    private val _finalizePayment3dsError = MutableLiveData<ArcXPError>()
    val finalizePayment3dsError: LiveData<ArcXPError> = _finalizePayment3dsError

    private val _orderHistoryResponse = MutableLiveData<ArcXPOrderHistory>()
    val orderHistoryResponse: LiveData<ArcXPOrderHistory> = _orderHistoryResponse

    private val _orderHistoryError = MutableLiveData<ArcXPError>()
    val orderHistoryError: LiveData<ArcXPError> = _orderHistoryError

    private val _orderDetailsResponse = MutableLiveData<ArcXPCustomerOrder>()
    val orderDetailsResponse: LiveData<ArcXPCustomerOrder> = _orderDetailsResponse

    private val _orderDetailsError = MutableLiveData<ArcXPError>()
    val orderDetailsError: LiveData<ArcXPError> = _orderDetailsError

    private val _clearCartResponse = MutableLiveData<ArcXPCustomerOrder>()
    val clearCartResponse: LiveData<ArcXPCustomerOrder> = _clearCartResponse

    private val _clearCartError = MutableLiveData<ArcXPError>()
    val clearCartError: LiveData<ArcXPError> = _clearCartError

    private val _getCurrentCartResponse = MutableLiveData<ArcXPCustomerOrder>()
    val getCurrentCartResponse: LiveData<ArcXPCustomerOrder> = _getCurrentCartResponse

    private val _getCurrentCartError = MutableLiveData<ArcXPError>()
    val getCurrentCartError: LiveData<ArcXPError> = _getCurrentCartError

    private val _addItemToCartResponse = MutableLiveData<ArcXPCustomerOrder>()
    val addItemToCartResponse: LiveData<ArcXPCustomerOrder> = _addItemToCartResponse

    private val _addItemToCartError = MutableLiveData<ArcXPError>()
    val addItemToCartError: LiveData<ArcXPError> = _addItemToCartError

    private val _removeItemFromCartResponse = MutableLiveData<ArcXPCustomerOrder>()
    val removeItemFromCartResponse: LiveData<ArcXPCustomerOrder> = _removeItemFromCartResponse

    private val _removeItemFromCartError = MutableLiveData<ArcXPError>()
    val removeItemFromCartError: LiveData<ArcXPError> = _removeItemFromCartError

    private val _errorResponse = MutableLiveData<ArcXPError>()
    val errorResponse: LiveData<ArcXPError> = _errorResponse

    fun getAllSubscriptions(callback: ArcXPSalesListener?) {
        mIoScope.launch {
            val res = repo.getAllSubscriptions()
            withContext(mUiScope.coroutineContext) {
                when (res) {
                    is Success -> {
                        if (callback == null) {
                            _allSubscriptionsResponse.postValue(res.r!!)
                        } else {
                            callback.onGetAllSubscriptionsSuccess(res.r!!)
                        }
                    }
                    is Failure -> {
                        if (callback == null) {
                            _errorResponse.value = res.l as ArcXPError /*handleFailure(res.l)*/
                        } else {
                            callback.onGetSubscriptionsFailure(res.l as ArcXPError)
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
                            _subscriptionsResponse.postValue(res.r!!)
                        } else {
                            callback.onGetAllActiveSubscriptionsSuccess(res.r!!)
                        }
                    }
                    is Failure -> {
                        if (callback == null) {
                            _errorResponse.value = res.l as ArcXPError /*handleFailure(res.l)*/
                        } else {
                            callback.onGetSubscriptionsFailure(res.l as ArcXPError)
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
                            _entitlementsResponse.postValue(res.r!!)
                        } else {
                            callback.onGetEntitlementsSuccess(res.r!!)
                        }
                    }
                    is Failure -> {
                        if (callback == null) {
                            _entitlementsErrorResponse.value = res.l as ArcXPError
                        } else {
                            callback.onGetEntitlementsFailure(res.l as ArcXPError)
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
                            _initializePaymentMethodResponse.postValue(res.r!!)
                        } else {
                            callback.onInitializePaymentMethodSuccess(res.r!!)
                        }
                    }
                    is Failure -> {
                        if (callback == null) {
                            _initializePaymentMethodError.value = res.l as ArcXPError
                        } else {
                            callback.onInitializePaymentMethodFailure(res.l as ArcXPError)
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
                            _finalizePaymentMethodResponse.postValue(res.r!!)
                        } else {
                            callback.onFinalizePaymentMethodSuccess(res.r)
                        }
                    }
                    is Failure -> {
                        if (callback == null) {
                            _finalizePaymentMethodError.value = res.l as ArcXPError
                        } else {
                            callback.onFinalizePaymentMethodFailure(res.l as ArcXPError)
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
                            _finalizePaymentMethod3dsResponse.postValue(res.r!!)
                        } else {
                            callback.onFinalizePaymentMethod3dsSuccess(res.r)
                        }
                    }
                    is Failure -> {
                        if (callback == null) {
                            _finalizePaymentMethod3dsError.value = res.l as ArcXPError
                        } else {
                            callback.onFinalizePaymentMethod3dsFailure(res.l as ArcXPError)
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
                            _cancelSubscriptionResponse.postValue(res.r!!)
                        } else {
                            callback.onCancelSubscriptionSuccess(res.r)
                        }
                    }
                    is Failure -> {
                        if (callback == null) {
                            _cancelSubscriptionError.value = res.l as ArcXPError
                        } else {
                            callback.onCancelSubscriptionFailure(res.l as ArcXPError)
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
                            _updateAddressResponse.postValue(res.r!!)
                        } else {
                            callback.onUpdateAddressSuccess(res.r)
                        }
                    }
                    is Failure -> {
                        if (callback == null) {
                            _updateAddressError.value = res.l as ArcXPError
                        } else {
                            callback.onUpdateAddressFailure(res.l as ArcXPError)
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
                            _subscriptionDetailsResponse.postValue(res.r!!)
                        } else {
                            callback.onGetSubscriptionDetailsSuccess(res.r)
                        }
                    }
                    is Failure -> {
                        if (callback == null) {
                            _subscriptionDetailsError.value = res.l as ArcXPError
                        } else {
                            callback.onGetSubscriptionDetailsFailure(res.l as ArcXPError)
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
                            _createCustomerOrderResponse.postValue(res.r!!)
                        } else {
                            callback.onCreateCustomerOrderSuccess(res.r)
                        }
                    }
                    is Failure -> {
                        if (callback == null) {
                            _createCustomerOrderError.value = res.l as ArcXPError
                        } else {
                            callback.onCreateCustomerOrderFailure(res.l as ArcXPError)
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
                            _getPaymentOptionsResponse.postValue(res.r!!)
                        } else {
                            callback.onGetPaymentOptionsSuccess(res.r)
                        }
                    }
                    is Failure -> {
                        if (callback == null) {
                            _getPaymentOptionsError.value = res.l as ArcXPError
                        } else {
                            callback.onGetPaymentOptionsFailure(res.l as ArcXPError)
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
                            _getAddressesResponse.postValue(res.r!!)
                        } else {
                            callback.onGetAddressesSuccess(res.r)
                        }
                    }
                    is Failure -> {
                        if (callback == null) {
                            _getAddressesError.value = res.l as ArcXPError
                        } else {
                            callback.onGetAddressesFailure(res.l as ArcXPError)
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
                            _initializePaymentResponse.postValue(res.r!!)
                        } else {
                            callback.onInitializePaymentSuccess()
                        }
                    }
                    is Failure -> {
                        if (callback == null) {
                            _initializePaymentError.value = res.l as ArcXPError
                        } else {
                            callback.onInitializePaymentFailure(res.l as ArcXPError)
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
                            _finalizePaymentResponse.postValue(res.r!!)
                        } else {
                            callback.onFinalizePaymentSuccess(res.r)
                        }
                    }
                    is Failure -> {
                        if (callback == null) {
                            _finalizePaymentError.value = res.l as ArcXPError
                        } else {
                            callback.onFinalizePaymentFailure(res.l as ArcXPError)
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
                            _finalizePayment3dsResponse.postValue(res.r!!)
                        } else {
                            callback.onFinalizePayment3dsSuccess(res.r)
                        }
                    }
                    is Failure -> {
                        if (callback == null) {
                            _finalizePayment3dsError.value = res.l as ArcXPError
                        } else {
                            callback.onFinalizePayment3dsFailure(res.l as ArcXPError)
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
                            _orderHistoryResponse.postValue(res.r!!)
                        } else {
                            callback.onOrderHistorySuccess(res.r)
                        }
                    }
                    is Failure -> {
                        if (callback == null) {
                            _orderHistoryError.value = res.l as ArcXPError
                        } else {
                            callback.onOrderHistoryFailure(res.l as ArcXPError)
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
                            _orderDetailsResponse.postValue(res.r!!)
                        } else {
                            callback.onOrderDetailsSuccess(res.r)
                        }
                    }
                    is Failure -> {
                        if (callback == null) {
                            _orderDetailsError.value = res.l as ArcXPError
                        } else {
                            callback.onOrderDetailsFailure(res.l as ArcXPError)
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
                            _clearCartResponse.postValue(res.r!!)
                        } else {
                            callback.onClearCartSuccess(res.r)
                        }
                    }
                    is Failure -> {
                        if (callback == null) {
                            _clearCartError.value = res.l as ArcXPError
                        } else {
                            callback.onClearCartFailure(res.l as ArcXPError)
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
                            _getCurrentCartResponse.postValue(res.r!!)
                        } else {
                            callback.onGetCurrentCartSuccess(res.r)
                        }
                    }
                    is Failure -> {
                        if (callback == null) {
                            _getCurrentCartError.value = res.l as ArcXPError
                        } else {
                            callback.onGetCurrentCartFailure(res.l as ArcXPError)
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
                            _addItemToCartResponse.postValue(res.r!!)
                        } else {
                            callback.onAddItemToCartSuccess(res.r)
                        }
                    }
                    is Failure -> {
                        if (callback == null) {
                            _addItemToCartError.value = res.l as ArcXPError
                        } else {
                            callback.onAddItemToCartFailure(res.l as ArcXPError)
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
                            _removeItemFromCartResponse.postValue(res.r!!)
                        } else {
                            callback.onRemoveItemFromCartSuccess(res.r)
                        }
                    }
                    is Failure -> {
                        if (callback == null) {
                            _removeItemFromCartError.value = res.l as ArcXPError
                        } else {
                            callback.onRemoveItemFromCartFailure(res.l as ArcXPError)
                        }
                    }
                }
            }
        }
    }
}
