package com.arcxp.commerce.repositories

import com.arcxp.commerce.base.BaseUnitTest
import com.arcxp.commerce.di.configureTestAppComponent
import com.arcxp.commerce.models.ArcXPActivePaywallRules
import com.arcxp.commerce.repositories.RetailRepository
import com.arcxp.commerce.retrofit.RetailService
import com.arcxp.commerce.util.Either
import com.arcxp.commerce.util.Success
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.koin.core.context.startKoin
import org.koin.java.KoinJavaComponent
import java.net.HttpURLConnection

class RetailRepositoryTest: BaseUnitTest() {

    private lateinit var retailRepository: RetailRepository

    private val retailService: RetailService by KoinJavaComponent.inject(RetailService::class.java)

    @Before
    fun start() {
        super.setUp()
        startKoin { modules(configureTestAppComponent(getMockWebServerUrl())) }
        retailRepository = RetailRepository(retailService)
    }

    @Test
    fun `test get active paywall rules`() = runBlocking {
        mockNetworkResponseWithFileContent(
            "paywall_active_rules.json",
            HttpURLConnection.HTTP_OK
        )

        val response: Either<Any?, ArcXPActivePaywallRules?> = retailRepository.getActivePaywallRules()
        assertNotNull("Response is null!", response)
        val successResponse = response as Success<ArcXPActivePaywallRules>
        assertNotNull("Response is not success!", response)
        val successRes = successResponse.r
        assertEquals(successRes.response.get(0).id, 888)
        assertEquals(successRes.response.get(1).id, 930)
    }
}