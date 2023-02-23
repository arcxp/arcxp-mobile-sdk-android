package com.arcxp.commerce.viewmodels

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.arcxp.commerce.apimanagers.ArcXPRetailListener
import com.arcxp.commerce.models.ActivePaywallRule
import com.arcxp.commerce.models.ArcXPActivePaywallRules
import com.arcxp.commerce.models.RuleBudget
import com.arcxp.commerce.repositories.RetailRepository
import com.arcxp.commons.testutils.TestUtils
import com.arcxp.commerce.util.ArcXPError
import com.arcxp.commons.util.Failure
import com.arcxp.commons.util.Success
import io.mockk.*
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class RetailViewModelTest {

    private lateinit var testObject: RetailViewModel

    private val activePaywallRule = ActivePaywallRule(
        id = 1,
        null,
        e = listOf(),
        null,
        null,
        rt = 1,
        budget = RuleBudget("1", "1", "1", "1", 1, 1)
    )

    @MockK
    lateinit var retailRepo: RetailRepository

    @RelaxedMockK
    lateinit var arcXPActivePaywallRules: ArcXPActivePaywallRules


    @RelaxedMockK
    private lateinit var listener: ArcXPRetailListener

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainDispatcherRule = TestUtils.MainDispatcherRule()


    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        testObject = RetailViewModel(
            retailRepo
        )
    }

    @Test
    fun `getActivePaywallRules - successful response with callback`() = runTest {
        val response = ArcXPActivePaywallRules(listOf(activePaywallRule))

        coEvery {
            retailRepo.getActivePaywallRules()
        } returns Success(response)

        testObject.getActivePaywallRules(listener)

        coVerify {
            retailRepo.getActivePaywallRules()
            listener.onGetActivePaywallRulesSuccess(response)
        }
    }

    @Test
    fun `getActivePaywallRules - successful response without callback`() = runTest {
        val response = arcXPActivePaywallRules


        coEvery {
            retailRepo.getActivePaywallRules()
        } returns Success(arcXPActivePaywallRules)

        testObject.getActivePaywallRules(null)

        coVerify {
            retailRepo.getActivePaywallRules()
        }

        assertEquals(response, testObject.paywallRulesResponseArcxp.value)
    }

    @Test
    fun `getActivePaywallRules - failed response with callback`() = runTest {
        val response = ArcXPError("Failed")


        coEvery {
            retailRepo.getActivePaywallRules()
        } returns Failure(response)

        testObject.getActivePaywallRules(listener)

        coVerify {
            retailRepo.getActivePaywallRules()
            listener.onGetActivePaywallRulesFailure(response)

        }
    }

//    @Test
//    fun `getActivePaywallRules - failed response without callback`() = runTest {
//        val response = ArcXPError("Failed")
//        coEvery {
//            retailRepo.getActivePaywallRules()
//        } returns Failure(response)
//
//        testObject.getActivePaywallRules(null)
//
//        coVerify {
//            retailRepo.getActivePaywallRules()
//
//        }
//        assertEquals(response, testObject.errorResponse.value)
//    }


}