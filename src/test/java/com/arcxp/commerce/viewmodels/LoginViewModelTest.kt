package com.arcxp.commerce.viewmodels

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.arcxp.commerce.base.BaseUnitTest
import com.arcxp.commerce.di.configureTestAppComponent
import com.arcxp.commerce.models.ArcXPAuth
import com.arcxp.commerce.models.ArcXPAuthRequest
import com.arcxp.commerce.repositories.LoginRepository
import com.arcxp.commerce.util.ArcXPError
import com.arcxp.commerce.util.AuthManager
import com.arcxp.commerce.util.Failure
import com.arcxp.commerce.util.Success
import com.arcxp.commerce.viewmodels.LoginViewModel
import com.google.gson.Gson
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.Dispatchers
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.koin.core.context.startKoin

class LoginViewModelTest : BaseUnitTest() {

    private lateinit var mViewModel: LoginViewModel
    private val mDispatcher = Dispatchers.Unconfined


    @get:Rule
    var instantExecuteRule = InstantTaskExecutorRule()

    @MockK
    lateinit var mRepo: LoginRepository

    @Before
    fun start() {
        super.setUp()
        MockKAnnotations.init(this)
        startKoin { modules(configureTestAppComponent(getMockWebServerUrl())) }
        mViewModel = LoginViewModel(mRepo, mDispatcher, mDispatcher)
        mockkObject(AuthManager)
        every { AuthManager.getInstance() } returns mockk(relaxed = true, relaxUnitFun = true)

    }

    @Test
    fun `test correct data received and handled for login success response`() {
        val loginSuccessResponse = getJson("login_success_response.json")
        val loginSuccess = Gson().fromJson<ArcXPAuth>(
            loginSuccessResponse,
            ArcXPAuth::class.java
        )

        coEvery {
            mRepo.login(
                ArcXPAuthRequest(
                    "",
                    "",
                    grantType = ArcXPAuthRequest.Companion.GrantType.PASSWORD.value,
                    recaptchaToken = "token"
                )
            )
        } returns Success(loginSuccess)
        mViewModel.authResponse.observeForever { }
        mViewModel.makeLoginCall("", "", "token")

        assertNotNull(mViewModel.authResponse.value)
        assertEquals(mViewModel.authResponse.value?.uuid, "bc999b7e-5353-48c9-b485-32a5dae5899b")
    }

    @Test
    fun `test error data received and handled for login error response`() {
        val loginErrorResponse = getJson("login_arc_error_response.json")
        val loginError = Gson().fromJson<ArcXPError>(
            loginErrorResponse,
            ArcXPError::class.java
        )
        coEvery {
            mRepo.login(
                ArcXPAuthRequest(
                    "userName",
                    "pw",
                    grantType = ArcXPAuthRequest.Companion.GrantType.PASSWORD.value,
                    recaptchaToken = "token"
                )
            )
        } returns Failure(loginError)
        mViewModel.errorResponse.observeForever { }

        mViewModel.makeLoginCall("userName", "pw", "token")

        assertEquals(loginError, mViewModel.errorResponse.value)
    }
}
