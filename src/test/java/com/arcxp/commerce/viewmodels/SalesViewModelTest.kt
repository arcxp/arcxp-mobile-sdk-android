package com.arcxp.commerce.viewmodels

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.arcxp.commerce.apimanagers.ArcXPSalesListener
import com.arcxp.commerce.models.*
import com.arcxp.commerce.repositories.SalesRepository
import com.arcxp.commons.testutils.TestUtils
import com.arcxp.commons.throwables.ArcXPException
import com.arcxp.commons.util.Failure
import com.arcxp.commons.util.Success
import io.mockk.*
import io.mockk.impl.annotations.RelaxedMockK
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi

import kotlinx.coroutines.test.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SalesViewModelTest {

    private lateinit var testObject: SalesViewModel

    @RelaxedMockK
    private lateinit var arcXPCustomerOrderRequest: ArcXPCustomerOrderRequest

    @RelaxedMockK
    private lateinit var arcXPAddressRequest: ArcXPAddressRequest

    @RelaxedMockK
    private lateinit var arcXPAddress: ArcXPAddress

    @RelaxedMockK
    private lateinit var arcXPSubscriptions: ArcXPSubscriptions

    @RelaxedMockK
    private lateinit var arcXPEntitlements: ArcXPEntitlements

    @RelaxedMockK
    private lateinit var arcXPFinalizePayment : ArcXPFinalizePayment

    @RelaxedMockK
    private lateinit var arcXPFinalizePaymentRequest: ArcXPFinalizePaymentRequest

    @RelaxedMockK
    private lateinit var arcXPSubscriptionDetails: ArcXPSubscriptionDetails

    @RelaxedMockK
    private lateinit var arcXPCustomerOrder: ArcXPCustomerOrder

    @RelaxedMockK
    private lateinit var salesRepository: SalesRepository

    @RelaxedMockK
    private lateinit var listener: ArcXPSalesListener

    @RelaxedMockK
    private lateinit var void: Void

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainDispatcherRule = TestUtils.MainDispatcherRule()

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        testObject = SalesViewModel(
            salesRepository
        )
    }

    @Test
    fun `getAllSubscriptions - successful response with callback`() = runTest {
        val response = arcXPSubscriptions


        coEvery {
            salesRepository.getAllSubscriptions()
        } returns Success(response)

        testObject.getAllSubscriptions(listener)

        coVerify {
            salesRepository.getAllSubscriptions()
            listener.onGetAllSubscriptionsSuccess(arcXPSubscriptions)

        }
    }

    @Test
    fun `getAllSubscriptions - successful response without callback`() = runTest {
        val response = arcXPSubscriptions


        coEvery {
            salesRepository.getAllSubscriptions()
        } returns Success(response)

        testObject.getAllSubscriptions(null)

        coVerify {
            salesRepository.getAllSubscriptions()
        }
        assertEquals(response, testObject.allSubscriptionsResponse.value)
    }


    @Test
    fun `getAllSubscriptions - failed response with callback`() = runTest {
        val response = Failure(ArcXPException("Failed"))


        coEvery {
            salesRepository.getAllSubscriptions()
        } returns response

        testObject.getAllSubscriptions(listener)

        coVerify {
            salesRepository.getAllSubscriptions()
            listener.onGetSubscriptionsFailure(response.failure)
        }

    }

    @Test
    fun `getAllSubscriptions - failed response without callback`() = runTest {
        val response = ArcXPException("Failed")


        coEvery {
            salesRepository.getAllSubscriptions()
        } returns Failure(response)

        testObject.getAllSubscriptions(null)

        coVerify {
            salesRepository.getAllSubscriptions()
        }

        assertEquals(response, testObject.errorResponse.value)
    }

    @Test
    fun `getAllActiveSubscriptions - successful response with callback`() = runTest {
        val response = arcXPSubscriptions


        coEvery {
            salesRepository.getAllActiveSubscriptions()
        } returns Success(response)

        testObject.getAllActiveSubscriptions(listener)

        coVerify {
            salesRepository.getAllActiveSubscriptions()
            listener.onGetAllActiveSubscriptionsSuccess(arcXPSubscriptions)

        }
    }

    @Test
    fun `getAllActiveSubscriptions - successful response without callback`() = runTest {
        val response = arcXPSubscriptions


        coEvery {
            salesRepository.getAllActiveSubscriptions()
        } returns Success(response)

        testObject.getAllActiveSubscriptions(null)

        coVerify {
            salesRepository.getAllActiveSubscriptions()
        }
        assertEquals(response, testObject.subscriptionsResponse.value)

    }

    @Test
    fun `getAllActiveSubscriptions - failed response with callback`() = runTest {
        val response = Failure(ArcXPException("Failed"))


        coEvery {
            salesRepository.getAllActiveSubscriptions()
        } returns response

        testObject.getAllActiveSubscriptions(listener)

        coVerify {
            salesRepository.getAllActiveSubscriptions()
            listener.onGetSubscriptionsFailure(response.failure)

        }
    }

    @Test
    fun `getAllActiveSubscriptions - failed response without callback`() = runTest {
        val response = Failure(ArcXPException("Failed"))


        coEvery {
            salesRepository.getAllActiveSubscriptions()
        } returns response

        testObject.getAllActiveSubscriptions(null)

        coVerify {
            salesRepository.getAllActiveSubscriptions()

        }
    }

    @Test
    fun `getEntitlements - successful response with callback`() = runTest {
        val response = arcXPEntitlements


        coEvery {
            salesRepository.getEntitlements()
        } returns Success(response)

        testObject.getEntitlements(listener)

        coVerify {
            salesRepository.getEntitlements()
            listener.onGetEntitlementsSuccess(arcXPEntitlements)
        }

    }

    @Test
    fun `getEntitlements - successful response without callback`() = runTest {
        val response = arcXPEntitlements


        coEvery {
            salesRepository.getEntitlements()
        } returns Success(response)

        testObject.getEntitlements(null)

        coVerify {
            salesRepository.getEntitlements()
        }
        assertEquals(response, testObject.entitlementsResponse.value)

    }

    @Test
    fun `getEntitlements - failed response with callback`() = runTest {
        val response = Failure(ArcXPException("Failed"))


        coEvery {
            salesRepository.getEntitlements()
        } returns response

        testObject.getEntitlements(listener)

        coVerify {
            salesRepository.getEntitlements()
            listener.onGetEntitlementsFailure(response.failure)
        }

    }

    @Test
    fun `getEntitlements - failed response without callback`() = runTest {
        val response = ArcXPException("Failed")


        coEvery {
            salesRepository.getEntitlements()
        } returns Failure(response)

        testObject.getEntitlements(null)

        coVerify {
            salesRepository.getEntitlements()
        }

        assertEquals(response, testObject.entitlementsErrorResponse.value)
    }

    @Test
    fun `initializePaymentMethod - successful response with callback`() = runTest {
        val response = mapOf(Pair("1", "A"))


        coEvery {
            salesRepository.initializePaymentMethod("1", "A")
        } returns Success(response)

        testObject.initializePaymentMethod("1", "A", listener)

        coVerify {
            salesRepository.initializePaymentMethod("1", "A")
            listener.onInitializePaymentMethodSuccess(response)
        }

    }

    @Test
    fun `initializePaymentMethod - successful response without callback`() = runTest {
        val response = mapOf(Pair("1", "A"))


        coEvery {
            salesRepository.initializePaymentMethod("1", "A")
        } returns Success(response)

        testObject.initializePaymentMethod("1", "A", null)

        coVerify {
            salesRepository.initializePaymentMethod("1", "A")
        }
        assertEquals(response, testObject.initializePaymentMethodResponse.value)

    }

    @Test
    fun `initializePaymentMethod - failed response with callback`() = runTest {
        val response = Failure(ArcXPException("Failed"))


        coEvery {
            salesRepository.initializePaymentMethod("1", "A")
        } returns response

        testObject.initializePaymentMethod("1", "A", listener)

        coVerify {
            salesRepository.initializePaymentMethod("1", "A")
            listener.onInitializePaymentMethodFailure(response.failure)

        }
    }

    @Test
    fun `initializePaymentMethod - failed response without callback`() = runTest {
        val response = ArcXPException("Failed")


        coEvery {
            salesRepository.initializePaymentMethod("1", "A")
        } returns Failure(response)

        testObject.initializePaymentMethod("1", "A", null)

        coVerify {
            salesRepository.initializePaymentMethod("1", "A")
        }

        assertEquals(response, testObject.initializePaymentMethodError.value)
    }

    @Test
    fun `finalizePaymentMethod - successful response with callback`() = runTest {
        val response = arcXPFinalizePayment


        coEvery {
            salesRepository.finalizePaymentMethod("1", "A", arcXPFinalizePaymentRequest)
        } returns Success(response)

        testObject.finalizePaymentMethod("1", "A", arcXPFinalizePaymentRequest, listener)

        coVerify {
            salesRepository.finalizePaymentMethod("1", "A", arcXPFinalizePaymentRequest)
            listener.onFinalizePaymentMethodSuccess(response)
        }

    }

    @Test
    fun `finalizePaymentMethod - successful response without callback`() = runTest {
        val response = arcXPFinalizePayment


        coEvery {
            salesRepository.finalizePaymentMethod("1", "A", arcXPFinalizePaymentRequest)
        } returns Success(response)

        testObject.finalizePaymentMethod("1", "A", arcXPFinalizePaymentRequest, null)

        coVerify {
            salesRepository.finalizePaymentMethod("1", "A", arcXPFinalizePaymentRequest)
        }

        assertEquals(response, testObject.finalizePaymentMethodResponse.value)
    }

    @Test
    fun `finalizePaymentMethod - failed response with callback`() = runTest {
        val response = Failure(ArcXPException("Failed"))


        coEvery {
            salesRepository.finalizePaymentMethod("1", "A", arcXPFinalizePaymentRequest)
        } returns response

        testObject.finalizePaymentMethod("1", "A", arcXPFinalizePaymentRequest, listener)

        coVerify {
            salesRepository.finalizePaymentMethod("1", "A", arcXPFinalizePaymentRequest)
            listener.onFinalizePaymentMethodFailure(response.failure)
        }

    }

    @Test
    fun `finalizePaymentMethod - failed response without callback`() = runTest {
        val response = ArcXPException("Failed")


        coEvery {
            salesRepository.finalizePaymentMethod("1", "A", arcXPFinalizePaymentRequest)
        } returns Failure(response)

        testObject.finalizePaymentMethod("1", "A", arcXPFinalizePaymentRequest, null)

        coVerify {
            salesRepository.finalizePaymentMethod("1", "A", arcXPFinalizePaymentRequest)
        }

        assertEquals(response, testObject.finalizePaymentMethodError.value)
    }

    @Test
    fun `finalizePaymentMethod3ds - successful response with callback`() = runTest {
        val response = arcXPFinalizePayment


        coEvery {
            salesRepository.finalizePaymentMethod3ds("1", "A", arcXPFinalizePaymentRequest)
        } returns Success(response)

        testObject.finalizePaymentMethod3ds("1", "A", arcXPFinalizePaymentRequest, listener)

        coVerify {
            salesRepository.finalizePaymentMethod3ds("1", "A", arcXPFinalizePaymentRequest)
            listener.onFinalizePaymentMethod3dsSuccess(response)
        }

    }

    @Test
    fun `finalizePaymentMethod3ds - successful response without callback`() = runTest {
        val response = arcXPFinalizePayment


        coEvery {
            salesRepository.finalizePaymentMethod3ds("1", "A", arcXPFinalizePaymentRequest)
        } returns Success(response)

        testObject.finalizePaymentMethod3ds("1", "A", arcXPFinalizePaymentRequest, null)

        coVerify {
            salesRepository.finalizePaymentMethod3ds("1", "A", arcXPFinalizePaymentRequest)
        }

        assertEquals(response, testObject.finalizePaymentMethod3dsResponse.value)
    }

    @Test
    fun `finalizePaymentMethod3ds - failed response with callback`() = runTest {
        val response = Failure(ArcXPException("Failed"))


        coEvery {
            salesRepository.finalizePaymentMethod3ds("1", "A", arcXPFinalizePaymentRequest)
        } returns response

        testObject.finalizePaymentMethod3ds("1", "A", arcXPFinalizePaymentRequest, listener)

        coVerify {
            salesRepository.finalizePaymentMethod3ds("1", "A", arcXPFinalizePaymentRequest)
            listener.onFinalizePaymentMethod3dsFailure(response.failure)
        }

    }

    @Test
    fun `finalizePaymentMethod3ds - failed response without callback`() = runTest {
        val response = ArcXPException("Failed")


        coEvery {
            salesRepository.finalizePaymentMethod3ds("1", "A", arcXPFinalizePaymentRequest)
        } returns Failure(response)

        testObject.finalizePaymentMethod3ds("1", "A", arcXPFinalizePaymentRequest, null)

        coVerify {
            salesRepository.finalizePaymentMethod3ds("1", "A", arcXPFinalizePaymentRequest)
        }

        assertEquals(response, testObject.finalizePaymentMethod3dsError.value)
    }

    @Test
    fun `cancelSubscription - successful response with callback`() = runTest {
        val response = ArcXPCancelSubscription(null, null)


        coEvery {
            salesRepository.cancelSubscription(
                "1",
                ArcXPCancelSubscriptionRequest("User Requested")
            )
        } returns Success(response)

        testObject.cancelSubscription(
            "1",
            ArcXPCancelSubscriptionRequest("User Requested"),
            listener
        )

        coVerify {
            salesRepository.cancelSubscription(
                "1",
                ArcXPCancelSubscriptionRequest("User Requested")
            )
            listener.onCancelSubscriptionSuccess(response)
        }

    }

    @Test
    fun `cancelSubscription - successful response without callback`() = runTest {
        val response = ArcXPCancelSubscription(null, null)


        coEvery {
            salesRepository.cancelSubscription(
                "1",
                ArcXPCancelSubscriptionRequest("User Requested")
            )
        } returns Success(response)

        testObject.cancelSubscription(
            "1",
            ArcXPCancelSubscriptionRequest("User Requested"),
            null
        )

        coVerify {
            salesRepository.cancelSubscription(
                "1",
                ArcXPCancelSubscriptionRequest("User Requested")
            )
        }

        assertEquals(response, testObject.cancelSubscriptionResponse.value)
    }

    @Test
    fun `cancelSubscription - failed response with callback`() = runTest {
        val response = Failure(ArcXPException("Failed"))


        coEvery {
            salesRepository.cancelSubscription(
                "1",
                ArcXPCancelSubscriptionRequest("User Requested")
            )
        } returns response

        testObject.cancelSubscription(
            "1",
            ArcXPCancelSubscriptionRequest("User Requested"),
            listener
        )

        coVerify {
            salesRepository.cancelSubscription(
                "1",
                ArcXPCancelSubscriptionRequest("User Requested")
            )
            listener.onCancelSubscriptionFailure(response.failure)
        }

    }

    @Test
    fun `cancelSubscription - failed response without callback`() = runTest {
        val response = ArcXPException("Failed")


        coEvery {
            salesRepository.cancelSubscription(
                "1",
                ArcXPCancelSubscriptionRequest("User Requested")
            )
        } returns Failure(response)

        testObject.cancelSubscription(
            "1",
            ArcXPCancelSubscriptionRequest("User Requested"),
            null
        )

        coVerify {
            salesRepository.cancelSubscription(
                "1",
                ArcXPCancelSubscriptionRequest("User Requested")
            )
        }

        assertEquals(response, testObject.cancelSubscriptionError.value)
    }

    @Test
    fun `updateAddress - successful response with callback`() = runTest {
        val response = ArcXPAddress("123", null, "city", null, null, "USA", "HOME")


        coEvery {
            salesRepository.updateAddress(ArcXPUpdateAddressRequest(1, arcXPAddressRequest))
        } returns Success(response)

        testObject.updateAddress(ArcXPUpdateAddressRequest(1, arcXPAddressRequest), listener)

        coVerify {
            salesRepository.updateAddress(ArcXPUpdateAddressRequest(1, arcXPAddressRequest))
            listener.onUpdateAddressSuccess(response)
        }

    }

    @Test
    fun `updateAddress - successful response without callback`() = runTest {
        val response = ArcXPAddress("123", null, "city", null, null, "USA", "HOME")


        coEvery {
            salesRepository.updateAddress(ArcXPUpdateAddressRequest(1, arcXPAddressRequest))
        } returns Success(response)

        testObject.updateAddress(ArcXPUpdateAddressRequest(1, arcXPAddressRequest), null)

        coVerify {
            salesRepository.updateAddress(ArcXPUpdateAddressRequest(1, arcXPAddressRequest))
        }

        assertEquals(response, testObject.updateAddressResponse.value)
    }

    @Test
    fun `updateAddress - failed response with callback`() = runTest {
        val response = Failure(ArcXPException("Failed"))


        coEvery {
            salesRepository.updateAddress(ArcXPUpdateAddressRequest(1, arcXPAddressRequest))
        } returns response

        testObject.updateAddress(ArcXPUpdateAddressRequest(1, arcXPAddressRequest), listener)

        coVerify {
            salesRepository.updateAddress(ArcXPUpdateAddressRequest(1, arcXPAddressRequest))
            listener.onUpdateAddressFailure(response.failure)
        }

    }

    @Test
    fun `updateAddress - failed response without callback`() = runTest {
        val response = ArcXPException("Failed")


        coEvery {
            salesRepository.updateAddress(ArcXPUpdateAddressRequest(1, arcXPAddressRequest))
        } returns Failure(response)

        testObject.updateAddress(ArcXPUpdateAddressRequest(1, arcXPAddressRequest), null)

        coVerify {
            salesRepository.updateAddress(ArcXPUpdateAddressRequest(1, arcXPAddressRequest))
        }

        assertEquals(response, testObject.updateAddressError.value)
    }

    @Test
    fun `getSubscriptionDetails - successful response with callback`() = runTest {
        val response = arcXPSubscriptionDetails


        coEvery {
            salesRepository.getSubscriptionsDetails("123")
        } returns Success(response)

        testObject.getSubscriptionDetails("123", listener)

        coVerify {
            salesRepository.getSubscriptionsDetails("123")
            listener.onGetSubscriptionDetailsSuccess(response)
        }

    }

    @Test
    fun `getSubscriptionDetails - successful response without callback`() = runTest {
        val response = arcXPSubscriptionDetails


        coEvery {
            salesRepository.getSubscriptionsDetails("123")
        } returns Success(response)

        testObject.getSubscriptionDetails("123", null)

        coVerify {
            salesRepository.getSubscriptionsDetails("123")
        }

        assertEquals(response, testObject.subscriptionDetailsResponse.value)
    }

    @Test
    fun `getSubscriptionDetails - failed response with callback`() = runTest {
        val response = Failure(ArcXPException("Failed"))


        coEvery {
            salesRepository.getSubscriptionsDetails("123")
        } returns response

        testObject.getSubscriptionDetails("123", listener)

        coVerify {
            salesRepository.getSubscriptionsDetails("123")
            listener.onGetSubscriptionDetailsFailure(response.failure)
        }

    }

    @Test
    fun `getSubscriptionDetails - failed response without callback`() = runTest {
        val response = ArcXPException("Failed")


        coEvery {
            salesRepository.getSubscriptionsDetails("123")
        } returns Failure(response)

        testObject.getSubscriptionDetails("123", null)

        coVerify {
            salesRepository.getSubscriptionsDetails("123")
        }

        assertEquals(response, testObject.subscriptionDetailsError.value)
    }

    @Test
    fun `createCustomerOrder - successful response with callback`() = runTest {
        val response = arcXPCustomerOrder


        coEvery {
            salesRepository.createCustomerOrder(
                arcXPCustomerOrderRequest
            )
        } returns Success(response)

        testObject.createCustomerOrder(
            arcXPCustomerOrderRequest
            , listener
        )

        coVerify {
            salesRepository.createCustomerOrder(
                arcXPCustomerOrderRequest

            )
            listener.onCreateCustomerOrderSuccess(response)
        }

    }

    @Test
    fun `createCustomerOrder - successful response without callback`() = runTest {
        val response = arcXPCustomerOrder


        coEvery {
            salesRepository.createCustomerOrder(
                arcXPCustomerOrderRequest
            )
        } returns Success(response)

        testObject.createCustomerOrder(
            arcXPCustomerOrderRequest
            , null
        )

        coVerify {
            salesRepository.createCustomerOrder(
                arcXPCustomerOrderRequest
            )
        }

        assertEquals(response, testObject.createCustomerOrderResponse.value)
    }

    @Test
    fun `createCustomerOrder - failed response with callback`() = runTest {
        val response = Failure(ArcXPException("Failed"))


        coEvery {
            salesRepository.createCustomerOrder(
                arcXPCustomerOrderRequest
            )
        } returns response

        testObject.createCustomerOrder(
            arcXPCustomerOrderRequest
            , listener
        )

        coVerify {
            salesRepository.createCustomerOrder(
                arcXPCustomerOrderRequest
            )
            listener.onCreateCustomerOrderFailure(response.failure)
        }

    }

    @Test
    fun `createCustomerOrder - failed response without callback`() = runTest {
        val response = ArcXPException("Failed")


        coEvery {
            salesRepository.createCustomerOrder(
                arcXPCustomerOrderRequest
            )
        } returns Failure(response)

        testObject.createCustomerOrder(
            arcXPCustomerOrderRequest
            , null
        )

        coVerify {
            salesRepository.createCustomerOrder(
                arcXPCustomerOrderRequest
            )
        }

        assertEquals(response, testObject.createCustomerOrderError.value)
    }

    @Test
    fun `getPaymentOptions - successful response with callback`() = runTest {
        val response = listOf("123")


        coEvery {
            salesRepository.getPaymentOptions()
        } returns Success(response)

        testObject.getPaymentOptions(listener)

        coVerify {
            salesRepository.getPaymentOptions()
            listener.onGetPaymentOptionsSuccess(response)
        }

    }

    @Test
    fun `getPaymentOptions - successful response without callback`() = runTest {
        val response = listOf("123")


        coEvery {
            salesRepository.getPaymentOptions()
        } returns Success(response)

        testObject.getPaymentOptions(null)

        coVerify {
            salesRepository.getPaymentOptions()
        }

        assertEquals(response, testObject.getPaymentOptionsResponse.value)
    }

    @Test
    fun `getPaymentOptions - failed response with callback`() = runTest {
        val response = Failure(ArcXPException("Failed"))


        coEvery {
            salesRepository.getPaymentOptions()
        } returns response

        testObject.getPaymentOptions(listener)

        coVerify {
            salesRepository.getPaymentOptions()
            listener.onGetPaymentOptionsFailure(response.failure)
        }

    }

    @Test
    fun `getPaymentOptions - failed response without callback`() = runTest {
        val response = ArcXPException("Failed")


        coEvery {
            salesRepository.getPaymentOptions()
        } returns Failure(response)

        testObject.getPaymentOptions(null)

        coVerify {
            salesRepository.getPaymentOptions()
        }

        assertEquals(response, testObject.getPaymentOptionsError.value)
    }

    @Test
    fun `getPaymentAddresses - successful response with callback`() = runTest {
        val response = arcXPAddress


        coEvery {
            salesRepository.getAddresses()
        } returns Success(listOf(response))

        testObject.getPaymentAddresses(listener)

        coVerify {
            salesRepository.getAddresses()
            listener.onGetAddressesSuccess(listOf(response))
        }

    }

    @Test
    fun `getPaymentAddresses - successful response without callback`() = runTest {
        val response = listOf(arcXPAddress)


        coEvery {
            salesRepository.getAddresses()
        } returns Success(response)

        testObject.getPaymentAddresses(null)

        coVerify {
            salesRepository.getAddresses()
        }

        assertEquals(response, testObject.getAddressesResponse.value)
    }

    @Test
    fun `getPaymentAddresses - failed response with callback`() = runTest {
        val response = Failure(ArcXPException("Failed"))


        coEvery {
            salesRepository.getAddresses()
        } returns response

        testObject.getPaymentAddresses(listener)

        coVerify {
            salesRepository.getAddresses()
            listener.onGetAddressesFailure(response.failure)
        }

    }

    @Test
    fun `getPaymentAddresses - failed response without callback`() = runTest {
        val response = ArcXPException("Failed")


        coEvery {
            salesRepository.getAddresses()
        } returns Failure(response)

        testObject.getPaymentAddresses(null)

        coVerify {
            salesRepository.getAddresses()
        }

        assertEquals(response, testObject.getAddressesError.value)
    }

    @Test
    fun `initializePayment - successful response with callback`() = runTest {


        coEvery {
            salesRepository.initializePayment("1", "2")
        } returns Success(mockk())

        testObject.initializePayment("1", "2", listener)

        coVerify {
            salesRepository.initializePayment("1", "2")
            listener.onInitializePaymentSuccess()
        }

    }

    @Test
    fun `initializePayment - successful response without callback`() = runTest {


        coEvery {
            salesRepository.initializePayment("1", "2")
        } returns Success(void)

        testObject.initializePayment("1", "2", null)

        coVerify {
            salesRepository.initializePayment("1", "2")
        }

        assertEquals(void, testObject.initializePaymentResponse.value)
    }

    @Test
    fun `initializePayment - failed response with callback`() = runTest {
        val response = Failure(ArcXPException("Failed"))


        coEvery {
            salesRepository.initializePayment("1", "2")
        } returns response

        testObject.initializePayment("1", "2", listener)

        coVerify {
            salesRepository.initializePayment("1", "2")
            listener.onInitializePaymentFailure(response.failure)
        }

    }

    @Test
    fun `initializePayment - failed response without callback`() = runTest {
        val response = ArcXPException("Failed")


        coEvery {
            salesRepository.initializePayment("1", "2")
        } returns Failure(response)

        testObject.initializePayment("1", "2", null)

        coVerify {
            salesRepository.initializePayment("1", "2")
        }

        assertEquals(response, testObject.initializePaymentError.value)
    }

    @Test
    fun `finalizePayment - successful response with callback`() = runTest {
        val response = arcXPFinalizePayment


        coEvery {
            salesRepository.finalizePayment("1", "2", arcXPFinalizePaymentRequest)
        } returns Success(response)

        testObject.finalizePayment("1", "2", arcXPFinalizePaymentRequest, listener)

        coVerify {
            salesRepository.finalizePayment("1", "2", arcXPFinalizePaymentRequest)
            listener.onFinalizePaymentSuccess(response)
        }

    }

    @Test
    fun `finalizePayment - successful response without callback`() = runTest {
        val response = arcXPFinalizePayment


        coEvery {
            salesRepository.finalizePayment("1", "2", arcXPFinalizePaymentRequest)
        } returns Success(response)

        testObject.finalizePayment("1", "2", arcXPFinalizePaymentRequest, null)

        coVerify {
            salesRepository.finalizePayment("1", "2", arcXPFinalizePaymentRequest)
        }

        assertEquals(response, testObject.finalizePaymentResponse.value)
    }

    @Test
    fun `finalizePayment - failed response with callback`() = runTest {
        val response = Failure(ArcXPException("Failed"))


        coEvery {
            salesRepository.finalizePayment("1", "2", arcXPFinalizePaymentRequest)
        } returns response

        testObject.finalizePayment("1", "2", arcXPFinalizePaymentRequest, listener)

        coVerify {
            salesRepository.finalizePayment("1", "2", arcXPFinalizePaymentRequest)
            listener.onFinalizePaymentFailure(response.failure)
        }

    }

    @Test
    fun `finalizePayment - failed response without callback`() = runTest {
        val response = ArcXPException("Failed")


        coEvery {
            salesRepository.finalizePayment("1", "2", arcXPFinalizePaymentRequest)
        } returns Failure(response)

        testObject.finalizePayment("1", "2", arcXPFinalizePaymentRequest, null)

        coVerify {
            salesRepository.finalizePayment("1", "2", arcXPFinalizePaymentRequest)
        }

        assertEquals(response, testObject.finalizePaymentError.value)
    }

    @Test
    fun `finalizePayment3ds - successful response with callback`() = runTest {
        val response = arcXPFinalizePayment


        coEvery {
            salesRepository.finalizePayment3ds("1", "2", arcXPFinalizePaymentRequest)
        } returns Success(response)

        testObject.finalizePayment3ds("1", "2", arcXPFinalizePaymentRequest, listener)

        coVerify {
            salesRepository.finalizePayment3ds("1", "2", arcXPFinalizePaymentRequest)
            listener.onFinalizePayment3dsSuccess(response)
        }

    }

    @Test
    fun `finalizePayment3ds - successful response without callback`() = runTest {
        val response = arcXPFinalizePayment


        coEvery {
            salesRepository.finalizePayment3ds("1", "2", arcXPFinalizePaymentRequest)
        } returns Success(response)

        testObject.finalizePayment3ds("1", "2", arcXPFinalizePaymentRequest, null)

        coVerify {
            salesRepository.finalizePayment3ds("1", "2", arcXPFinalizePaymentRequest)
        }

        assertEquals(response, testObject.finalizePayment3dsResponse.value)
    }

    @Test
    fun `finalizePayment3ds - failed response with callback`() = runTest {
        val response = Failure(ArcXPException("Failed"))


        coEvery {
            salesRepository.finalizePayment3ds("1", "2", arcXPFinalizePaymentRequest)
        } returns response

        testObject.finalizePayment3ds("1", "2", arcXPFinalizePaymentRequest, listener)

        coVerify {
            salesRepository.finalizePayment3ds("1", "2", arcXPFinalizePaymentRequest)
            listener.onFinalizePayment3dsFailure(response.failure)
        }

    }

    @Test
    fun `finalizePayment3ds - failed response without callback`() = runTest {
        val response = ArcXPException("Failed")


        coEvery {
            salesRepository.finalizePayment3ds("1", "2", arcXPFinalizePaymentRequest)
        } returns Failure(response)

        testObject.finalizePayment3ds("1", "2", arcXPFinalizePaymentRequest, null)

        coVerify {
            salesRepository.finalizePayment3ds("1", "2", arcXPFinalizePaymentRequest)
        }

        assertEquals(response, testObject.finalizePayment3dsError.value)
    }

    @Test
    fun `getOrderHistory - successful response with callback`() = runTest {
        val response = ArcXPOrderHistory(null, null, null, null)


        coEvery {
            salesRepository.getOrderHistory()
        } returns Success(response)

        testObject.getOrderHistory(listener)

        coVerify {
            salesRepository.getOrderHistory()
            listener.onOrderHistorySuccess(response)
        }

    }

    @Test
    fun `getOrderHistory - successful response without callback`() = runTest {
        val response = ArcXPOrderHistory(null, null, null, null)


        coEvery {
            salesRepository.getOrderHistory()
        } returns Success(response)

        testObject.getOrderHistory(null)

        coVerify {
            salesRepository.getOrderHistory()
        }

        assertEquals(response, testObject.orderHistoryResponse.value)
    }

    @Test
    fun `getOrderHistory - failed response with callback`() = runTest {
        val response = Failure(ArcXPException("Failed"))


        coEvery {
            salesRepository.getOrderHistory()
        } returns response

        testObject.getOrderHistory(listener)

        coVerify {
            salesRepository.getOrderHistory()
            listener.onOrderHistoryFailure(response.failure)
        }

    }

    @Test
    fun `getOrderHistory - failed response without callback`() = runTest {
        val response = ArcXPException("Failed")


        coEvery {
            salesRepository.getOrderHistory()
        } returns Failure(response)

        testObject.getOrderHistory(null)

        coVerify {
            salesRepository.getOrderHistory()
        }

        assertEquals(response, testObject.orderHistoryError.value)
    }

    @Test
    fun `getOrderDetails - successful response with callback`() = runTest {
        val response = arcXPCustomerOrder


        coEvery {
            salesRepository.getOrderDetails("1")
        } returns Success(response)

        testObject.getOrderDetails("1", listener)

        coVerify {
            salesRepository.getOrderDetails("1")
            listener.onOrderDetailsSuccess(response)
        }

    }

    @Test
    fun `getOrderDetails - successful response without callback`() = runTest {
        val response = arcXPCustomerOrder


        coEvery {
            salesRepository.getOrderDetails("1")
        } returns Success(response)

        testObject.getOrderDetails("1", null)

        coVerify {
            salesRepository.getOrderDetails("1")
        }

        assertEquals(response, testObject.orderDetailsResponse.value)
    }

    @Test
    fun `getOrderDetails - failed response with callback`() = runTest {
        val response = Failure(ArcXPException("Failed"))


        coEvery {
            salesRepository.getOrderDetails("1")
        } returns response

        testObject.getOrderDetails("1", listener)

        coVerify {
            salesRepository.getOrderDetails("1")
            listener.onOrderDetailsFailure(response.failure)
        }

    }

    @Test
    fun `getOrderDetails - failed response without callback`() = runTest {
        val response = ArcXPException("Failed")

        coEvery {
            salesRepository.getOrderDetails("1")
        } returns Failure(response)

        testObject.getOrderDetails("1", null)

        coVerify {
            salesRepository.getOrderDetails("1")
        }
        assertEquals(response, testObject.orderDetailsError.value)
    }

    @Test
    fun `clearCart - successful response with callback`() = runTest {
        val response = arcXPCustomerOrder


        coEvery {
            salesRepository.clearCart()
        } returns Success(response)

        testObject.clearCart(listener)

        coVerify {
            salesRepository.clearCart()
            listener.onClearCartSuccess(response)
        }

    }

    @Test
    fun `clearCart - successful response without callback`() = runTest {
        val response = arcXPCustomerOrder


        coEvery {
            salesRepository.clearCart()
        } returns Success(response)

        testObject.clearCart(null)

        coVerify {
            salesRepository.clearCart()
        }

        assertEquals(response, testObject.clearCartResponse.value)
    }

    @Test
    fun `clearCart - failed response with callback`() = runTest {
        val response = Failure(ArcXPException("Failed"))


        coEvery {
            salesRepository.clearCart()
        } returns response


        testObject.clearCart(listener)

        coVerify {
            salesRepository.clearCart()
            listener.onClearCartFailure(response.failure)
        }

    }

    @Test
    fun `clearCart - failed response without callback`() = runTest {
        val response = ArcXPException("Failed")


        coEvery {
            salesRepository.clearCart()
        } returns Failure(response)

        testObject.clearCart(null)

        coVerify {
            salesRepository.clearCart()
        }

        assertEquals(response, testObject.clearCartError.value)
    }

    @Test
    fun `getCurrentCart - successful response with callback`() = runTest {
        val response = arcXPCustomerOrder


        coEvery {
            salesRepository.getCurrentCart()
        } returns Success(response)

        testObject.getCurrentCart(listener)

        coVerify {
            salesRepository.getCurrentCart()
            listener.onGetCurrentCartSuccess(response)
        }

    }

    @Test
    fun `getCurrentCart - successful response without callback`() = runTest {
        val response = arcXPCustomerOrder


        coEvery {
            salesRepository.getCurrentCart()
        } returns Success(response)

        testObject.getCurrentCart(null)

        coVerify {
            salesRepository.getCurrentCart()
        }

        assertEquals(response, testObject.getCurrentCartResponse.value)
    }

    @Test
    fun `getCurrentCart - failed response with callback`() = runTest {
        val response = Failure(ArcXPException("Failed"))


        coEvery {
            salesRepository.getCurrentCart()
        } returns response


        testObject.getCurrentCart(listener)

        coVerify {
            salesRepository.getCurrentCart()
            listener.onGetCurrentCartFailure(response.failure)
        }

    }

    @Test
    fun `getCurrentCart - failed response without callback`() = runTest {
        val response = ArcXPException("Failed")


        coEvery {
            salesRepository.getCurrentCart()
        } returns Failure(response)


        testObject.getCurrentCart(null)

        coVerify {
            salesRepository.getCurrentCart()
        }

        assertEquals(response, testObject.getCurrentCartError.value)
    }

    @Test
    fun `addItemToCart - successful response with callback`() = runTest {
        val response = arcXPCustomerOrder


        coEvery {
            salesRepository.addItemToCart(ArcXPCartItemsRequest(null, null))
        } returns Success(response)

        testObject.addItemToCart(ArcXPCartItemsRequest(null, null), listener)

        coVerify {
            salesRepository.addItemToCart(ArcXPCartItemsRequest(null, null))
            listener.onAddItemToCartSuccess(response)
        }

    }

    @Test
    fun `addItemToCart - successful response without callback`() = runTest {
        val response = arcXPCustomerOrder


        coEvery {
            salesRepository.addItemToCart(ArcXPCartItemsRequest(null, null))
        } returns Success(response)

        testObject.addItemToCart(ArcXPCartItemsRequest(null, null), null)

        coVerify {
            salesRepository.addItemToCart(ArcXPCartItemsRequest(null, null))
        }

        assertEquals(response, testObject.addItemToCartResponse.value)
    }

    @Test
    fun `addItemToCart - failed response with callback`() = runTest {
        val response = Failure(ArcXPException("Failed"))


        coEvery {
            salesRepository.addItemToCart(ArcXPCartItemsRequest(null, null))
        } returns response


        testObject.addItemToCart(ArcXPCartItemsRequest(null, null), listener)

        coVerify {
            salesRepository.addItemToCart(ArcXPCartItemsRequest(null, null))
            listener.onAddItemToCartFailure(response.failure)
        }

    }

    @Test
    fun `addItemToCart - failed response without callback`() = runTest {
        val response = ArcXPException("Failed")


        coEvery {
            salesRepository.addItemToCart(ArcXPCartItemsRequest(null, null))
        } returns Failure(response)


        testObject.addItemToCart(ArcXPCartItemsRequest(null, null), null)

        coVerify {
            salesRepository.addItemToCart(ArcXPCartItemsRequest(null, null))
        }

        assertEquals(response, testObject.addItemToCartError.value)
    }

    @Test
    fun `removeItemFromCart - successful response with callback`() = runTest {
        val response = arcXPCustomerOrder


        coEvery {
            salesRepository.removeItemFromCart("123")
        } returns Success(response)

        testObject.removeItemFromCart("123", listener)

        coVerify {
            salesRepository.removeItemFromCart("123")
            listener.onRemoveItemFromCartSuccess(response)
        }

    }

    @Test
    fun `removeItemFromCart - successful response without callback`() = runTest {
        val response = arcXPCustomerOrder


        coEvery {
            salesRepository.removeItemFromCart("123")
        } returns Success(response)

        testObject.removeItemFromCart("123", null)

        coVerify {
            salesRepository.removeItemFromCart("123")
        }

        assertEquals(response, testObject.removeItemFromCartResponse.value)
    }

    @Test
    fun `removeItemFromCart - failed response with callback`() = runTest {
        val response = Failure(ArcXPException("Failed"))


        coEvery {
            salesRepository.removeItemFromCart("123")
        } returns response


        testObject.removeItemFromCart("123", listener)

        coVerify {
            salesRepository.removeItemFromCart("123")
            listener.onRemoveItemFromCartFailure(response.failure)
        }

    }

    @Test
    fun `removeItemFromCart - failed response without callback`() = runTest {
        val response = ArcXPException("Failed")


        coEvery {
            salesRepository.removeItemFromCart("123")
        } returns Failure(response)


        testObject.removeItemFromCart("123", null)

        coVerify {
            salesRepository.removeItemFromCart("123")
        }

        assertEquals(response, testObject.removeItemFromCartError.value)
    }
}