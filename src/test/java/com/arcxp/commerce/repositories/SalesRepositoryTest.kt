package com.arcxp.commerce.repositories

import com.arcxp.commerce.base.BaseUnitTest
import com.arcxp.commerce.di.configureTestAppComponent
import com.arcxp.commerce.models.ArcXPSubscriptions
import com.arcxp.commerce.repositories.SalesRepository
import com.arcxp.commerce.retrofit.SalesService
import com.arcxp.commerce.util.Either
import com.arcxp.commerce.util.Success
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.koin.core.context.startKoin
import org.koin.java.KoinJavaComponent
import java.net.HttpURLConnection

class SalesRepositoryTest: BaseUnitTest() {
    private lateinit var salesRepository: SalesRepository

    private val salesService: SalesService by KoinJavaComponent.inject(SalesService::class.java)

    @Before
    fun start() {
        super.setUp()
        startKoin { modules(configureTestAppComponent(getMockWebServerUrl())) }
        salesRepository = SalesRepository(salesService)
    }

    @Test
    fun `test get all active subscriptions`() = runBlocking {
        mockNetworkResponseWithFileContent(
            "sales_all_active_subscriptions.json",
            HttpURLConnection.HTTP_OK
        )

        val response: Either<Any?, ArcXPSubscriptions?> = salesRepository.getAllActiveSubscriptions()
        assertNotNull("Response is null!", response)
        val successResponse = response as Success<ArcXPSubscriptions>
        assertNotNull("Response is not success!", response)
        val successRes = successResponse.r
        assertEquals(successRes.response.get(0).productName, "Yearly")
        assertEquals(successRes.response.get(1).productName, "Monthly")
        assertEquals(successRes.response.get(0).attributes.get(0).get("key1"), "value1")
        assertEquals(successRes.response.get(0).paymentMethod?.cardHolderName, "John Doe")
    }

    @Test
    fun `test get all subscriptions`() = runBlocking {
        mockNetworkResponseWithFileContent(
            "sales_all_subscriptions.json",
            HttpURLConnection.HTTP_OK
        )

        val response: Either<Any?, ArcXPSubscriptions?> = salesRepository.getAllSubscriptions()
        assertNotNull("Response is null!", response)
        val successResponse = response as Success<ArcXPSubscriptions>
        assertNotNull("Response is not success!", response)
        val successRes = successResponse.r
        assertEquals(successRes.response.get(0).productName, "Yearly")
        assertEquals(successRes.response.get(1).productName, "Monthly")
        assertEquals(successRes.response.get(0).paymentMethod?.cardHolderName, "John Doe")
        assertEquals(successRes.response.get(0).attributes.get(0).get("key1"), "value1")
        assertEquals(successRes.response.get(0).paymentMethod?.cardHolderName, "John Doe")
    }
}