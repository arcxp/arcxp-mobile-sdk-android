package com.arcxp.commerce.repositories

import com.arcxp.commerce.base.BaseUnitTest
import com.arcxp.commerce.di.configureTestAppComponent
import com.arcxp.commerce.ArcXPCommerceSDKErrorType
import com.arcxp.commerce.models.ArcXPAuthRequest
import com.arcxp.commerce.models.ArcXPAuth
import com.arcxp.commerce.repositories.LoginRepository
import com.arcxp.commerce.retrofit.IdentityService
import com.arcxp.commerce.util.*
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.koin.core.context.startKoin
import org.koin.java.KoinJavaComponent.inject
import java.net.HttpURLConnection

class LoginRepositoryTest : BaseUnitTest() {

    private lateinit var mRepo: LoginRepository

    private val mService: IdentityService by inject(IdentityService::class.java)

    @Before
    fun start() {
        super.setUp()
        startKoin { modules(configureTestAppComponent(getMockWebServerUrl())) }
        mRepo = LoginRepository(mService)
    }

    @Test
    fun `test login request get success response`() = runBlocking {
        mockNetworkResponseWithFileContent(
            "login_success_response.json",
            HttpURLConnection.HTTP_OK
        )

        val response: Either<Any?, ArcXPAuth?> = mRepo.login(ArcXPAuthRequest())
        assertNotNull("Response is null!", response)
        val successResponse = response as Success<ArcXPAuth?>
        assertNotNull("Response is not success!", response)
        val successRes = successResponse.r
        assertEquals(successRes?.uuid, "bc999b7e-5353-48c9-b485-32a5dae5899b")
    }

    @Test
    fun `test login request get error response`() = runBlocking {
        mockNetworkResponseWithFileContent(
            "login_arc_error_response.json",
            HttpURLConnection.HTTP_UNAUTHORIZED
        )
        val response: Either<Any?, ArcXPAuth?> = mRepo.login(ArcXPAuthRequest())
        assertNotNull("Response is null!", response)

        val actualResponse = (response as Failure<Any?>).l as ArcXPError

        assertEquals("300040", actualResponse.code)
        assertEquals(ArcXPCommerceSDKErrorType.SERVER_ERROR, actualResponse.type)
        assertEquals("Authentication failed", actualResponse.message)
    }
}
