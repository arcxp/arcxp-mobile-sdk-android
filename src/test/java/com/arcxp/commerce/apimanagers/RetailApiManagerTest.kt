package com.arcxp.commerce.apimanagers

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.arcxp.commerce.callbacks.ArcXPRetailListener
import com.arcxp.commerce.models.ArcXPActivePaywallRules
import com.arcxp.commerce.viewmodels.RetailViewModel
import com.arcxp.commons.throwables.ArcXPException
import com.arcxp.commons.util.DependencyFactory
import io.mockk.*
import io.mockk.impl.annotations.RelaxedMockK
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class RetailApiManagerTest {

    @get:Rule
    var instantExecuteRule = InstantTaskExecutorRule()

    @RelaxedMockK
    private lateinit var viewModel: RetailViewModel

    @RelaxedMockK
    private lateinit var listener: ArcXPRetailListener

    private lateinit var testObject: RetailApiManager

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        mockkObject(DependencyFactory)
        every { DependencyFactory.createRetailViewModel() } returns viewModel
        every { DependencyFactory.createRetailRepository() } returns mockk()
        testObject = RetailApiManager()
    }

    @Test
    fun `getActivePaywallRules() calls listeners on success`() {
        testObject.getActivePaywallRules(listener = listener)
        val slot = slot<ArcXPRetailListener>()

        verifySequence { viewModel.getActivePaywallRules(callback = capture(slot))}

        val responseArcxp = mockk<ArcXPActivePaywallRules>()

        slot.captured.onGetActivePaywallRulesSuccess(responseArcxp = responseArcxp)
        
        verifySequence { 
            listener.onGetActivePaywallRulesSuccess(responseArcxp = responseArcxp)
        }
    }

    @Test
    fun `getActivePaywallRules() calls listeners on failure`() {
        testObject.getActivePaywallRules(listener = listener)
        val slot = slot<ArcXPRetailListener>()

        verifySequence { viewModel.getActivePaywallRules(callback = capture(slot))}

        val error = mockk<ArcXPException>()

        slot.captured.onGetActivePaywallRulesFailure(error = error)
        
        verifySequence { 
            listener.onGetActivePaywallRulesFailure(error = error)
        }
    }
}