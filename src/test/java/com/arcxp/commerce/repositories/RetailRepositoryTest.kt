package com.arcxp.commerce.repositories

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.arcxp.commerce.ArcXPCommerceSDKErrorType
import com.arcxp.commerce.models.ActivePaywallRule
import com.arcxp.commerce.models.ArcXPActivePaywallRules
import com.arcxp.commerce.retrofit.RetailService
import com.arcxp.commerce.retrofit.RetrofitController
import com.arcxp.commons.testutils.TestUtils
import com.arcxp.commerce.util.*
import com.arcxp.commons.util.Failure
import com.arcxp.commons.util.Success
import io.mockk.*
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import retrofit2.Response
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class RetailRepositoryTest {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainDispatcherRule = TestUtils.MainDispatcherRule()

    @MockK
    lateinit var retailService: RetailService

    @RelaxedMockK
    lateinit var response: Response<List<ActivePaywallRule>>

    @MockK
    lateinit var responseBody: List<ActivePaywallRule>

    lateinit var testObject: RetailRepository

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `getActivePaywallRules on Success returns expected`() = runTest {
        testObject = RetailRepository(retailService = retailService)
        coEvery { retailService.getActivePaywallRules() } returns response
        coEvery { response.isSuccessful } returns true
        coEvery { response.body() } returns responseBody

        val result = testObject.getActivePaywallRules()

        assertEquals(ArcXPActivePaywallRules(responseBody), (result as Success).success)
    }

    @Test
    fun `getActivePaywallRules when unsuccessful returns failure`() = runTest {
        val errorBody = TestUtils.getJson(filename = "identity_error_response.json")
        val mockWebServer = MockWebServer()
        val mockResponse = MockResponse()
            .setBody(errorBody)
            .setResponseCode(404)
        mockWebServer.enqueue(mockResponse)
        mockWebServer.start()
        val baseUrl = mockWebServer.url("\\").toString()
        mockkObject(AuthManager)
        every { AuthManager.getInstance() } returns mockk {
            every { accessToken } returns ""
            every { retailBaseUrl } returns baseUrl
        }

        testObject = RetailRepository()

        val result = testObject.getActivePaywallRules()

        val error = ((result as Failure).failure as ArcXPError)
        assertEquals(ArcXPCommerceSDKErrorType.SERVER_ERROR, error.type)
        assertEquals("Authentication failed", error.message)
        assertEquals("300041", error.code)
        mockWebServer.shutdown()
    }

    @Test
    fun `getActivePaywallRules when exception returns failure`() = runTest {
        val expectedMessage = "myMessage"
        val exception = Exception(expectedMessage)
        mockkObject(RetrofitController)
        every { RetrofitController.getRetailService() } returns retailService
        coEvery { retailService.getActivePaywallRules() } throws exception
        testObject = RetailRepository()

        val result = testObject.getActivePaywallRules()

        val error = ((result as Failure).failure as ArcXPError)
        assertEquals(ArcXPCommerceSDKErrorType.SERVER_ERROR, error.type)
        assertEquals(exception, error.value)
        unmockkObject(RetrofitController)
    }
}