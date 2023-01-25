package com.arcxp.commerce.apimanagers

import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.OnLifecycleEvent
import com.arcxp.commerce.viewmodels.NonceViewModel
/**
 * @suppress
 */
class NonceApiManager<T : Fragment>(
    private val fragment: T,
    private val nonceUiInteraction: NonceUiInteraction,
    private val viewModel: NonceViewModel
) : BaseApiManager<T>(fragment) {


    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    private fun onDestroy() {

    }

}
/**
 * @suppress
 */
interface NonceUiInteraction {

}