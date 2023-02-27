package com.arcxp.commerce.repositories

import com.arcxp.commons.throwables.ArcXPSDKErrorType
import com.arcxp.commerce.models.*
import com.arcxp.commerce.retrofit.RetrofitController
import com.arcxp.commerce.retrofit.SalesService
import com.arcxp.commons.testutils.TestUtils
import com.arcxp.commerce.util.AuthManager
import com.arcxp.commons.throwables.ArcXPException
import com.arcxp.commons.util.Failure
import com.arcxp.commons.util.Success
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.After
import org.junit.Before
import org.junit.Test
import retrofit2.Response
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class SalesRepositoryTest {

    @MockK
    private lateinit var salesService: SalesService

    private lateinit var testObject: SalesRepository

    private val id = "id"
    private val pid = "pid"
    private val orderNumber = "order#"
    private val mid = "mid"
    private val sku = "sku"


    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        mockkObject(RetrofitController)
        every { RetrofitController.getSalesService()} returns salesService
        mockkObject(AuthManager)
        every { AuthManager.getInstance() } returns mockk {
            every { accessToken = any() } just Runs
        }

        testObject = SalesRepository()
    }

    @After
    fun tearDown() {
        unmockkObject(AuthManager)
        unmockkObject(RetrofitController)
    }

    @Test
    fun `getAllActiveSubscriptions - successful response`() = runTest {
        val mockResponse = mockk<List<SubscriptionSummary>>()
        val result = Response.success(mockResponse)
        val expected = ArcXPSubscriptions(mockResponse)
        coEvery {
            salesService.getAllActiveSubscriptions()
        } returns result

        val actual = testObject.getAllActiveSubscriptions()
        assertEquals(expected, (actual as Success).success)
    }

    @Test
    fun `getAllActiveSubscriptions failed response`() = runTest {
        val result = Response.error<List<SubscriptionSummary>>(
            401,
            TestUtils.getJson("identity_error_response.json").toResponseBody()
        )
        coEvery {
            salesService.getAllActiveSubscriptions()
        } returns result
        val actual = testObject.getAllActiveSubscriptions()
        assertEquals(
            ArcXPSDKErrorType.SERVER_ERROR,
            (actual as Failure).failure.type
        )
        assertEquals("Authentication failed", actual.failure.message)
        assertEquals("300041", actual.failure.code)
    }

    @Test
    fun `getAllActiveSubscriptions - throw exception`() = runTest {
        val expectedMessage = "myMessage"
        val exception = Exception(expectedMessage)

        coEvery { salesService.getAllActiveSubscriptions() } throws exception

        val actual = testObject.getAllActiveSubscriptions()
        assertEquals(
            ArcXPSDKErrorType.SERVER_ERROR,
            (actual as Failure).failure.type
        )
        assertEquals(
            exception,
            actual.failure.value
        )
    }

    @Test
    fun `getAllSubscriptions - successful response`() = runTest {
        val mockResponse = mockk<List<SubscriptionSummary>>()
        val result = Response.success(mockResponse)
        val expected = ArcXPSubscriptions(mockResponse)
        coEvery {
            salesService.getAllSubscriptions()
        } returns result

        val actual = testObject.getAllSubscriptions()
        assertEquals(expected, (actual as Success).success)
    }

    @Test
    fun `getAllSubscriptions failed response`() = runTest {
        val result = Response.error<List<SubscriptionSummary>>(
            401,
            TestUtils.getJson("identity_error_response.json").toResponseBody()
        )
        coEvery {
            salesService.getAllSubscriptions()
        } returns result
        val actual = testObject.getAllSubscriptions()
        assertEquals(
            ArcXPSDKErrorType.SERVER_ERROR,
            ((actual as Failure).failure as ArcXPException).type
        )
        assertEquals("Authentication failed", (actual.failure as ArcXPException).message)
        assertEquals("300041", (actual.failure as ArcXPException).code)
    }

    @Test
    fun `getAllSubscriptions - throw exception`() = runTest {
        val expectedMessage = "myMessage"
        val exception = Exception(expectedMessage)

        coEvery { salesService.getAllSubscriptions() } throws exception

        val actual = testObject.getAllSubscriptions()
        assertEquals(
            ArcXPSDKErrorType.SERVER_ERROR,
            ((actual as Failure).failure as ArcXPException).type
        )
        assertEquals(
            exception,
            (actual.failure as ArcXPException).value
        )
    }

    @Test
    fun `getEntitlements - successful response`() = runTest {
        val expected = mockk<ArcXPEntitlements>()
        val mockResponse = Response.success(expected)
        coEvery {
            salesService.getEntitlements()
        } returns mockResponse

        val actual = testObject.getEntitlements()
        assertEquals(expected, (actual as Success).success)
    }

    @Test
    fun `getEntitlements failed response`() = runTest {
        val result = Response.error<ArcXPEntitlements>(
            401,
            TestUtils.getJson("identity_error_response.json").toResponseBody()
        )
        coEvery {
            salesService.getEntitlements()
        } returns result
        val actual = testObject.getEntitlements()
        assertEquals(
            ArcXPSDKErrorType.SERVER_ERROR,
            ((actual as Failure).failure as ArcXPException).type
        )
        assertEquals("Authentication failed", (actual.failure as ArcXPException).message)
        assertEquals("300041", (actual.failure as ArcXPException).code)
    }

    @Test
    fun `getEntitlements - throw exception`() = runTest {
        val expectedMessage = "myMessage"
        val exception = Exception(expectedMessage)

        coEvery { salesService.getEntitlements() } throws exception

        val actual = testObject.getEntitlements()
        assertEquals(
            ArcXPSDKErrorType.SERVER_ERROR,
            ((actual as Failure).failure as ArcXPException).type
        )
        assertEquals(
            exception,
            (actual.failure as ArcXPException).value
        )
    }

    @Test
    fun `initializePaymentMethod - successful response`() = runTest {
        val expected = mockk<Map<String, String>>()
        val mockResponse = Response.success(expected)
        coEvery {
            salesService.initializePaymentMethod(id = id, pid = pid)
        } returns mockResponse

        val actual = testObject.initializePaymentMethod(id = id, pid = pid)
        assertEquals(expected, (actual as Success).success)
    }

    @Test
    fun `initializePaymentMethod failed response`() = runTest {
        val result = Response.error<Map<String, String>>(
            401,
            TestUtils.getJson("identity_error_response.json").toResponseBody()
        )
        coEvery {
            salesService.initializePaymentMethod(id = id, pid = pid)
        } returns result
        val actual = testObject.initializePaymentMethod(id = id, pid = pid)
        assertEquals(
            ArcXPSDKErrorType.SERVER_ERROR,
            ((actual as Failure).failure as ArcXPException).type
        )
        assertEquals("Authentication failed", (actual.failure as ArcXPException).message)
        assertEquals("300041", (actual.failure as ArcXPException).code)
    }

    @Test
    fun `initializePaymentMethod - throw exception`() = runTest {
        val expectedMessage = "myMessage"
        val exception = Exception(expectedMessage)

        coEvery { salesService.initializePaymentMethod(id = id, pid = pid) } throws exception

        val actual = testObject.initializePaymentMethod(id = id, pid = pid)
        assertEquals(
            ArcXPSDKErrorType.SERVER_ERROR,
            ((actual as Failure).failure as ArcXPException).type
        )
        assertEquals(
            exception,
            (actual.failure as ArcXPException).value
        )
    }

    @Test
    fun `finalizePaymentMethod - successful response`() = runTest {
        val request = mockk<ArcXPFinalizePaymentRequest>()
        val expected = mockk<ArcXPFinalizePayment>()
        val mockResponse = Response.success(expected)
        coEvery {
            salesService.finalizePaymentMethod(id = id, pid = pid, request = request)
        } returns mockResponse

        val actual = testObject.finalizePaymentMethod(id = id, pid = pid, request = request)
        assertEquals(expected, (actual as Success).success)
    }

    @Test
    fun `finalizePaymentMethod failed response`() = runTest {
        val request = mockk<ArcXPFinalizePaymentRequest>()
        val result = Response.error<ArcXPFinalizePayment>(
            401,
            TestUtils.getJson("identity_error_response.json").toResponseBody()
        )
        coEvery {
            salesService.finalizePaymentMethod(id = id, pid = pid, request = request)
        } returns result
        val actual = testObject.finalizePaymentMethod(id = id, pid = pid, request = request)
        assertEquals(
            ArcXPSDKErrorType.SERVER_ERROR,
            ((actual as Failure).failure as ArcXPException).type
        )
        assertEquals("Authentication failed", (actual.failure as ArcXPException).message)
        assertEquals("300041", (actual.failure as ArcXPException).code)
    }

    @Test
    fun `finalizePaymentMethod - throw exception`() = runTest {
        val request = mockk<ArcXPFinalizePaymentRequest>()
        val expectedMessage = "myMessage"
        val exception = Exception(expectedMessage)

        coEvery { salesService.finalizePaymentMethod(id = id, pid = pid, request = request) } throws exception

        val actual = testObject.finalizePaymentMethod(id = id, pid = pid, request = request)
        assertEquals(
            ArcXPSDKErrorType.SERVER_ERROR,
            ((actual as Failure).failure as ArcXPException).type
        )
        assertEquals(
            exception,
            (actual.failure as ArcXPException).value
        )
    }

    @Test
    fun `finalizePaymentMethod3ds - successful response`() = runTest {
        val request = mockk<ArcXPFinalizePaymentRequest>()
        val expected = mockk<ArcXPFinalizePayment>()
        val mockResponse = Response.success(expected)
        coEvery {
            salesService.finalizePaymentMethod3ds(id = id, pid = pid, request = request)
        } returns mockResponse

        val actual = testObject.finalizePaymentMethod3ds(id = id, pid = pid, request = request)
        assertEquals(expected, (actual as Success).success)
    }

    @Test
    fun `finalizePaymentMethod3ds failed response`() = runTest {
        val request = mockk<ArcXPFinalizePaymentRequest>()
        val result = Response.error<ArcXPFinalizePayment>(
            401,
            TestUtils.getJson("identity_error_response.json").toResponseBody()
        )
        coEvery {
            salesService.finalizePaymentMethod3ds(id = id, pid = pid, request = request)
        } returns result
        val actual = testObject.finalizePaymentMethod3ds(id = id, pid = pid, request = request)
        assertEquals(
            ArcXPSDKErrorType.SERVER_ERROR,
            ((actual as Failure).failure as ArcXPException).type
        )
        assertEquals("Authentication failed", (actual.failure as ArcXPException).message)
        assertEquals("300041", (actual.failure as ArcXPException).code)
    }

    @Test
    fun `finalizePaymentMethod3ds - throw exception`() = runTest {
        val request = mockk<ArcXPFinalizePaymentRequest>()
        val expectedMessage = "myMessage"
        val exception = Exception(expectedMessage)

        coEvery { salesService.finalizePaymentMethod3ds(id = id, pid = pid, request = request) } throws exception

        val actual = testObject.finalizePaymentMethod3ds(id = id, pid = pid, request = request)
        assertEquals(
            ArcXPSDKErrorType.SERVER_ERROR,
            ((actual as Failure).failure as ArcXPException).type
        )
        assertEquals(
            exception,
            (actual.failure as ArcXPException).value
        )
    }

    @Test
    fun `cancelSubscription - successful response`() = runTest {
        val request = mockk<ArcXPCancelSubscriptionRequest>()
        val expected = mockk<ArcXPCancelSubscription>()
        val mockResponse = Response.success(expected)
        coEvery {
            salesService.cancelSubscription(id = id, request = request)
        } returns mockResponse

        val actual = testObject.cancelSubscription(id = id, request = request)
        assertEquals(expected, (actual as Success).success)
    }

    @Test
    fun `cancelSubscription failed response`() = runTest {
        val request = mockk<ArcXPCancelSubscriptionRequest>()
        val result = Response.error<ArcXPCancelSubscription>(
            401,
            TestUtils.getJson("identity_error_response.json").toResponseBody()
        )
        coEvery {
            salesService.cancelSubscription(id = id, request = request)
        } returns result
        val actual = testObject.cancelSubscription(id = id, request = request)
        assertEquals(
            ArcXPSDKErrorType.SERVER_ERROR,
            ((actual as Failure).failure as ArcXPException).type
        )
        assertEquals("Authentication failed", (actual.failure as ArcXPException).message)
        assertEquals("300041", (actual.failure as ArcXPException).code)
    }

    @Test
    fun `cancelSubscription - throw exception`() = runTest {
        val request = mockk<ArcXPCancelSubscriptionRequest>()
        val expectedMessage = "myMessage"
        val exception = Exception(expectedMessage)

        coEvery { salesService.cancelSubscription(id = id, request = request) } throws exception

        val actual = testObject.cancelSubscription(id = id, request = request)
        assertEquals(
            ArcXPSDKErrorType.SERVER_ERROR,
            ((actual as Failure).failure as ArcXPException).type
        )
        assertEquals(
            exception,
            (actual.failure as ArcXPException).value
        )
    }


    @Test
    fun `updateAddress - successful response`() = runTest {
        val request = mockk<ArcXPUpdateAddressRequest>()
        val expected = mockk<ArcXPAddress>()
        val mockResponse = Response.success(expected)
        coEvery {
            salesService.updateAddress(request = request)
        } returns mockResponse

        val actual = testObject.updateAddress(request = request)
        assertEquals(expected, (actual as Success).success)
    }

    @Test
    fun `updateAddress failed response`() = runTest {
        val request = mockk<ArcXPUpdateAddressRequest>()
        val result = Response.error<ArcXPAddress>(
            401,
            TestUtils.getJson("identity_error_response.json").toResponseBody()
        )
        coEvery {
            salesService.updateAddress(request = request)
        } returns result
        val actual = testObject.updateAddress(request = request)
        assertEquals(
            ArcXPSDKErrorType.SERVER_ERROR,
            ((actual as Failure).failure as ArcXPException).type
        )
        assertEquals("Authentication failed", (actual.failure as ArcXPException).message)
        assertEquals("300041", (actual.failure as ArcXPException).code)
    }

    @Test
    fun `updateAddress - throw exception`() = runTest {
        val request = mockk<ArcXPUpdateAddressRequest>()
        val expectedMessage = "myMessage"
        val exception = Exception(expectedMessage)

        coEvery { salesService.updateAddress(request = request) } throws exception

        val actual = testObject.updateAddress(request = request)
        assertEquals(
            ArcXPSDKErrorType.SERVER_ERROR,
            ((actual as Failure).failure as ArcXPException).type
        )
        assertEquals(
            exception,
            (actual.failure as ArcXPException).value
        )
    }


    @Test
    fun `getSubscriptionsDetails - successful response`() = runTest {
        val expected = mockk<ArcXPSubscriptionDetails>()
        val mockResponse = Response.success(expected)
        coEvery {
            salesService.getSubscriptionsDetails(id = id)
        } returns mockResponse

        val actual = testObject.getSubscriptionsDetails(id = id)
        assertEquals(expected, (actual as Success).success)
    }

    @Test
    fun `getSubscriptionsDetails failed response`() = runTest {
        val result = Response.error<ArcXPSubscriptionDetails>(
            401,
            TestUtils.getJson("identity_error_response.json").toResponseBody()
        )
        coEvery {
            salesService.getSubscriptionsDetails(id = id)
        } returns result
        val actual = testObject.getSubscriptionsDetails(id = id)
        assertEquals(
            ArcXPSDKErrorType.SERVER_ERROR,
            ((actual as Failure).failure as ArcXPException).type
        )
        assertEquals("Authentication failed", (actual.failure as ArcXPException).message)
        assertEquals("300041", (actual.failure as ArcXPException).code)
    }

    @Test
    fun `getSubscriptionsDetails - throw exception`() = runTest {
        val expectedMessage = "myMessage"
        val exception = Exception(expectedMessage)

        coEvery { salesService.getSubscriptionsDetails(id = id) } throws exception

        val actual = testObject.getSubscriptionsDetails(id = id)
        assertEquals(
            ArcXPSDKErrorType.SERVER_ERROR,
            ((actual as Failure).failure as ArcXPException).type
        )
        assertEquals(
            exception,
            (actual.failure as ArcXPException).value
        )
    }

    @Test
    fun `createCustomerOrder - successful response`() = runTest {
        val request = mockk<ArcXPCustomerOrderRequest>()
        val expected = mockk<ArcXPCustomerOrder>()
        val mockResponse = Response.success(expected)
        coEvery {
            salesService.createCustomerOrder(request = request)
        } returns mockResponse

        val actual = testObject.createCustomerOrder(request = request)
        assertEquals(expected, (actual as Success).success)
    }

    @Test
    fun `createCustomerOrder failed response`() = runTest {
        val request = mockk<ArcXPCustomerOrderRequest>()
        val result = Response.error<ArcXPCustomerOrder>(
            401,
            TestUtils.getJson("identity_error_response.json").toResponseBody()
        )
        coEvery {
            salesService.createCustomerOrder(request = request)
        } returns result
        val actual = testObject.createCustomerOrder(request = request)
        assertEquals(
            ArcXPSDKErrorType.SERVER_ERROR,
            ((actual as Failure).failure as ArcXPException).type
        )
        assertEquals("Authentication failed", (actual.failure as ArcXPException).message)
        assertEquals("300041", (actual.failure as ArcXPException).code)
    }

    @Test
    fun `createCustomerOrder - throw exception`() = runTest {
        val request = mockk<ArcXPCustomerOrderRequest>()
        val expectedMessage = "myMessage"
        val exception = Exception(expectedMessage)

        coEvery { salesService.createCustomerOrder(request = request) } throws exception

        val actual = testObject.createCustomerOrder(request = request)
        assertEquals(
            ArcXPSDKErrorType.SERVER_ERROR,
            ((actual as Failure).failure as ArcXPException).type
        )
        assertEquals(
            exception,
            (actual.failure as ArcXPException).value
        )
    }

    @Test
    fun `initializePayment - successful response`() = runTest {
        val expected = mockk<Void>()
        val mockResponse = Response.success(expected)
        coEvery {
            salesService.initializePayment(orderNumber = orderNumber, mid = mid)
        } returns mockResponse

        val actual = testObject.initializePayment(orderNumber = orderNumber, mid = mid)
        assertEquals(expected, (actual as Success).success)
    }

    @Test
    fun `initializePayment failed response`() = runTest {
        val result = Response.error<Void>(
            401,
            TestUtils.getJson("identity_error_response.json").toResponseBody()
        )
        coEvery {
            salesService.initializePayment(orderNumber = orderNumber, mid = mid)
        } returns result
        val actual = testObject.initializePayment(orderNumber = orderNumber, mid = mid)
        assertEquals(
            ArcXPSDKErrorType.SERVER_ERROR,
            ((actual as Failure).failure as ArcXPException).type
        )
        assertEquals("Authentication failed", (actual.failure as ArcXPException).message)
        assertEquals("300041", (actual.failure as ArcXPException).code)
    }

    @Test
    fun `initializePayment - throw exception`() = runTest {
        val expectedMessage = "myMessage"
        val exception = Exception(expectedMessage)

        coEvery { salesService.initializePayment(orderNumber = orderNumber, mid = mid) } throws exception

        val actual = testObject.initializePayment(orderNumber = orderNumber, mid = mid)
        assertEquals(
            ArcXPSDKErrorType.SERVER_ERROR,
            ((actual as Failure).failure as ArcXPException).type
        )
        assertEquals(
            exception,
            (actual.failure as ArcXPException).value
        )
    }

    @Test
    fun `finalizePayment - successful response`() = runTest {
        val request = mockk<ArcXPFinalizePaymentRequest>()
        val expected = mockk<ArcXPFinalizePayment>()
        val mockResponse = Response.success(expected)
        coEvery {
            salesService.finalizePayment(orderNumber = orderNumber, mid = mid, request = request)
        } returns mockResponse

        val actual = testObject.finalizePayment(orderNumber = orderNumber, mid = mid, request = request)
        assertEquals(expected, (actual as Success).success)
    }

    @Test
    fun `finalizePayment failed response`() = runTest {
        val request = mockk<ArcXPFinalizePaymentRequest>()
        val result = Response.error<ArcXPFinalizePayment>(
            401,
            TestUtils.getJson("identity_error_response.json").toResponseBody()
        )
        coEvery {
            salesService.finalizePayment(orderNumber = orderNumber, mid = mid, request = request)
        } returns result
        val actual = testObject.finalizePayment(orderNumber = orderNumber, mid = mid, request = request)
        assertEquals(
            ArcXPSDKErrorType.SERVER_ERROR,
            ((actual as Failure).failure as ArcXPException).type
        )
        assertEquals("Authentication failed", (actual.failure as ArcXPException).message)
        assertEquals("300041", (actual.failure as ArcXPException).code)
    }

    @Test
    fun `finalizePayment - throw exception`() = runTest {
        val request = mockk<ArcXPFinalizePaymentRequest>()
        val expectedMessage = "myMessage"
        val exception = Exception(expectedMessage)

        coEvery { salesService.finalizePayment(orderNumber = orderNumber, mid = mid, request = request) } throws exception

        val actual = testObject.finalizePayment(orderNumber = orderNumber, mid = mid, request = request)
        assertEquals(
            ArcXPSDKErrorType.SERVER_ERROR,
            ((actual as Failure).failure as ArcXPException).type
        )
        assertEquals(
            exception,
            (actual.failure as ArcXPException).value
        )
    }
    @Test
    fun `finalizePayment3ds - successful response`() = runTest {
        val request = mockk<ArcXPFinalizePaymentRequest>()
        val expected = mockk<ArcXPFinalizePayment>()
        val mockResponse = Response.success(expected)
        coEvery {
            salesService.finalizePayment3ds(orderNumber = orderNumber, mid = mid, request = request)
        } returns mockResponse

        val actual = testObject.finalizePayment3ds(orderNumber = orderNumber, mid = mid, request = request)
        assertEquals(expected, (actual as Success).success)
    }

    @Test
    fun `finalizePayment3ds failed response`() = runTest {
        val request = mockk<ArcXPFinalizePaymentRequest>()
        val result = Response.error<ArcXPFinalizePayment>(
            401,
            TestUtils.getJson("identity_error_response.json").toResponseBody()
        )
        coEvery {
            salesService.finalizePayment3ds(orderNumber = orderNumber, mid = mid, request = request)
        } returns result
        val actual = testObject.finalizePayment3ds(orderNumber = orderNumber, mid = mid, request = request)
        assertEquals(
            ArcXPSDKErrorType.SERVER_ERROR,
            ((actual as Failure).failure as ArcXPException).type
        )
        assertEquals("Authentication failed", (actual.failure as ArcXPException).message)
        assertEquals("300041", (actual.failure as ArcXPException).code)
    }

    @Test
    fun `finalizePayment3ds - throw exception`() = runTest {
        val request = mockk<ArcXPFinalizePaymentRequest>()
        val expectedMessage = "myMessage"
        val exception = Exception(expectedMessage)

        coEvery { salesService.finalizePayment3ds(orderNumber = orderNumber, mid = mid, request = request) } throws exception

        val actual = testObject.finalizePayment3ds(orderNumber = orderNumber, mid = mid, request = request)
        assertEquals(
            ArcXPSDKErrorType.SERVER_ERROR,
            ((actual as Failure).failure as ArcXPException).type
        )
        assertEquals(
            exception,
            (actual.failure as ArcXPException).value
        )
    }

    @Test
    fun `getOrderHistory - successful response`() = runTest {
        val expected = mockk<ArcXPOrderHistory>()
        val mockResponse = Response.success(expected)
        coEvery {
            salesService.getOrderHistory()
        } returns mockResponse

        val actual = testObject.getOrderHistory()
        assertEquals(expected, (actual as Success).success)
    }

    @Test
    fun `getOrderHistory failed response`() = runTest {
        val result = Response.error<ArcXPOrderHistory>(
            401,
            TestUtils.getJson("identity_error_response.json").toResponseBody()
        )
        coEvery {
            salesService.getOrderHistory()
        } returns result
        val actual = testObject.getOrderHistory()
        assertEquals(
            ArcXPSDKErrorType.SERVER_ERROR,
            ((actual as Failure).failure as ArcXPException).type
        )
        assertEquals("Authentication failed", (actual.failure as ArcXPException).message)
        assertEquals("300041", (actual.failure as ArcXPException).code)
    }

    @Test
    fun `getOrderHistory - throw exception`() = runTest {
        val expectedMessage = "myMessage"
        val exception = Exception(expectedMessage)

        coEvery { salesService.getOrderHistory() } throws exception

        val actual = testObject.getOrderHistory()
        assertEquals(
            ArcXPSDKErrorType.SERVER_ERROR,
            ((actual as Failure).failure as ArcXPException).type
        )
        assertEquals(
            exception,
            (actual.failure as ArcXPException).value
        )
    }

    @Test
    fun `getOrderDetails - successful response`() = runTest {
        val expected = mockk<ArcXPCustomerOrder>()
        val mockResponse = Response.success(expected)
        coEvery {
            salesService.getOrderDetails(orderNumber = orderNumber)
        } returns mockResponse

        val actual = testObject.getOrderDetails(orderNumber = orderNumber)
        assertEquals(expected, (actual as Success).success)
    }

    @Test
    fun `getOrderDetails failed response`() = runTest {
        val result = Response.error<ArcXPCustomerOrder>(
            401,
            TestUtils.getJson("identity_error_response.json").toResponseBody()
        )
        coEvery {
            salesService.getOrderDetails(orderNumber = orderNumber)
        } returns result
        val actual = testObject.getOrderDetails(orderNumber = orderNumber)
        assertEquals(
            ArcXPSDKErrorType.SERVER_ERROR,
            ((actual as Failure).failure as ArcXPException).type
        )
        assertEquals("Authentication failed", (actual.failure as ArcXPException).message)
        assertEquals("300041", (actual.failure as ArcXPException).code)
    }

    @Test
    fun `getOrderDetails - throw exception`() = runTest {
        val expectedMessage = "myMessage"
        val exception = Exception(expectedMessage)

        coEvery { salesService.getOrderDetails(orderNumber = orderNumber) } throws exception

        val actual = testObject.getOrderDetails(orderNumber = orderNumber)
        assertEquals(
            ArcXPSDKErrorType.SERVER_ERROR,
            ((actual as Failure).failure as ArcXPException).type
        )
        assertEquals(
            exception,
            (actual.failure as ArcXPException).value
        )
    }

    @Test
    fun `getPaymentOptions - successful response`() = runTest {
        val expected = mockk<List<String?>>()
        val mockResponse = Response.success(expected)
        coEvery {
            salesService.getPaymentOptions()
        } returns mockResponse

        val actual = testObject.getPaymentOptions()
        assertEquals(expected, (actual as Success).success)
    }

    @Test
    fun `getPaymentOptions failed response`() = runTest {
        val result = Response.error<List<String?>>(
            401,
            TestUtils.getJson("identity_error_response.json").toResponseBody()
        )
        coEvery {
            salesService.getPaymentOptions()
        } returns result
        val actual = testObject.getPaymentOptions()
        assertEquals(
            ArcXPSDKErrorType.SERVER_ERROR,
            ((actual as Failure).failure as ArcXPException).type
        )
        assertEquals("Authentication failed", (actual.failure as ArcXPException).message)
        assertEquals("300041", (actual.failure as ArcXPException).code)
    }

    @Test
    fun `getPaymentOptions - throw exception`() = runTest {
        val expectedMessage = "myMessage"
        val exception = Exception(expectedMessage)

        coEvery { salesService.getPaymentOptions() } throws exception

        val actual = testObject.getPaymentOptions()
        assertEquals(
            ArcXPSDKErrorType.SERVER_ERROR,
            ((actual as Failure).failure as ArcXPException).type
        )
        assertEquals(
            exception,
            (actual.failure as ArcXPException).value
        )
    }

    @Test
    fun `getAddresses - successful response`() = runTest {
        val expected = mockk<List<ArcXPAddress>>()
        val mockResponse = Response.success(expected)
        coEvery {
            salesService.getAddresses()
        } returns mockResponse

        val actual = testObject.getAddresses()
        assertEquals(expected, (actual as Success).success)
    }

    @Test
    fun `getAddresses failed response`() = runTest {
        val result = Response.error<List<ArcXPAddress>>(
            401,
            TestUtils.getJson("identity_error_response.json").toResponseBody()
        )
        coEvery {
            salesService.getAddresses()
        } returns result
        val actual = testObject.getAddresses()
        assertEquals(
            ArcXPSDKErrorType.SERVER_ERROR,
            ((actual as Failure).failure as ArcXPException).type
        )
        assertEquals("Authentication failed", (actual.failure as ArcXPException).message)
        assertEquals("300041", (actual.failure as ArcXPException).code)
    }

    @Test
    fun `getAddresses - throw exception`() = runTest {
        val expectedMessage = "myMessage"
        val exception = Exception(expectedMessage)

        coEvery { salesService.getAddresses() } throws exception

        val actual = testObject.getAddresses()
        assertEquals(
            ArcXPSDKErrorType.SERVER_ERROR,
            ((actual as Failure).failure as ArcXPException).type
        )
        assertEquals(
            exception,
            (actual.failure as ArcXPException).value
        )
    }

    @Test
    fun `clearCart - successful response`() = runTest {
        val expected = mockk<ArcXPCustomerOrder>()
        val mockResponse = Response.success(expected)
        coEvery {
            salesService.clearCart()
        } returns mockResponse

        val actual = testObject.clearCart()
        assertEquals(expected, (actual as Success).success)
    }

    @Test
    fun `clearCart failed response`() = runTest {
        val result = Response.error<ArcXPCustomerOrder>(
            401,
            TestUtils.getJson("identity_error_response.json").toResponseBody()
        )
        coEvery {
            salesService.clearCart()
        } returns result
        val actual = testObject.clearCart()
        assertEquals(
            ArcXPSDKErrorType.SERVER_ERROR,
            ((actual as Failure).failure as ArcXPException).type
        )
        assertEquals("Authentication failed", (actual.failure as ArcXPException).message)
        assertEquals("300041", (actual.failure as ArcXPException).code)
    }

    @Test
    fun `clearCart - throw exception`() = runTest {
        val expectedMessage = "myMessage"
        val exception = Exception(expectedMessage)

        coEvery { salesService.clearCart() } throws exception

        val actual = testObject.clearCart()
        assertEquals(
            ArcXPSDKErrorType.SERVER_ERROR,
            ((actual as Failure).failure as ArcXPException).type
        )
        assertEquals(
            exception,
            (actual.failure as ArcXPException).value
        )
    }

    @Test
    fun `getCurrentCart - successful response`() = runTest {
        val expected = mockk<ArcXPCustomerOrder>()
        val mockResponse = Response.success(expected)
        coEvery {
            salesService.getCurrentCart()
        } returns mockResponse

        val actual = testObject.getCurrentCart()
        assertEquals(expected, (actual as Success).success)
    }

    @Test
    fun `getCurrentCart failed response`() = runTest {
        val result = Response.error<ArcXPCustomerOrder>(
            401,
            TestUtils.getJson("identity_error_response.json").toResponseBody()
        )
        coEvery {
            salesService.getCurrentCart()
        } returns result
        val actual = testObject.getCurrentCart()
        assertEquals(
            ArcXPSDKErrorType.SERVER_ERROR,
            ((actual as Failure).failure as ArcXPException).type
        )
        assertEquals("Authentication failed", (actual.failure as ArcXPException).message)
        assertEquals("300041", (actual.failure as ArcXPException).code)
    }

    @Test
    fun `getCurrentCart - throw exception`() = runTest {
        val expectedMessage = "myMessage"
        val exception = Exception(expectedMessage)

        coEvery { salesService.getCurrentCart() } throws exception

        val actual = testObject.getCurrentCart()
        assertEquals(
            ArcXPSDKErrorType.SERVER_ERROR,
            ((actual as Failure).failure as ArcXPException).type
        )
        assertEquals(
            exception,
            (actual.failure as ArcXPException).value
        )
    }

    @Test
    fun `addItemToCart - successful response`() = runTest {
        val request = mockk<ArcXPCartItemsRequest>()
        val expected = mockk<ArcXPCustomerOrder>()
        val mockResponse = Response.success(expected)
        coEvery {
            salesService.addItemToCart(request = request)
        } returns mockResponse

        val actual = testObject.addItemToCart(request = request)
        assertEquals(expected, (actual as Success).success)
    }

    @Test
    fun `addItemToCart failed response`() = runTest {
        val request = mockk<ArcXPCartItemsRequest>()
        val result = Response.error<ArcXPCustomerOrder>(
            401,
            TestUtils.getJson("identity_error_response.json").toResponseBody()
        )
        coEvery {
            salesService.addItemToCart(request = request)
        } returns result
        val actual = testObject.addItemToCart(request = request)
        assertEquals(
            ArcXPSDKErrorType.SERVER_ERROR,
            ((actual as Failure).failure as ArcXPException).type
        )
        assertEquals("Authentication failed", (actual.failure as ArcXPException).message)
        assertEquals("300041", (actual.failure as ArcXPException).code)
    }

    @Test
    fun `addItemToCart - throw exception`() = runTest {
        val request = mockk<ArcXPCartItemsRequest>()
        val expectedMessage = "myMessage"
        val exception = Exception(expectedMessage)

        coEvery { salesService.addItemToCart(request = request) } throws exception

        val actual = testObject.addItemToCart(request = request)
        assertEquals(
            ArcXPSDKErrorType.SERVER_ERROR,
            ((actual as Failure).failure as ArcXPException).type
        )
        assertEquals(
            exception,
            (actual.failure as ArcXPException).value
        )
    }


    @Test
    fun `removeItemFromCart - successful response`() = runTest {
        val expected = mockk<ArcXPCustomerOrder>()
        val mockResponse = Response.success(expected)
        coEvery {
            salesService.removeItemFromCart(sku = sku)
        } returns mockResponse

        val actual = testObject.removeItemFromCart(sku = sku)
        assertEquals(expected, (actual as Success).success)
    }

    @Test
    fun `removeItemFromCart failed response`() = runTest {
        val result = Response.error<ArcXPCustomerOrder>(
            401,
            TestUtils.getJson("identity_error_response.json").toResponseBody()
        )
        coEvery {
            salesService.removeItemFromCart(sku = sku)
        } returns result
        val actual = testObject.removeItemFromCart(sku = sku)
        assertEquals(
            ArcXPSDKErrorType.SERVER_ERROR,
            ((actual as Failure).failure as ArcXPException).type
        )
        assertEquals("Authentication failed", (actual.failure as ArcXPException).message)
        assertEquals("300041", (actual.failure as ArcXPException).code)
    }

    @Test
    fun `removeItemFromCart - throw exception`() = runTest {
        val expectedMessage = "myMessage"
        val exception = Exception(expectedMessage)

        coEvery { salesService.removeItemFromCart(sku = sku) } throws exception

        val actual = testObject.removeItemFromCart(sku = sku)
        assertEquals(
            ArcXPSDKErrorType.SERVER_ERROR,
            ((actual as Failure).failure as ArcXPException).type
        )
        assertEquals(
            exception,
            (actual.failure as ArcXPException).value
        )
    }
}