package com.arcxp.commerce.viewmodels

import androidx.lifecycle.ViewModel
import com.arcxp.commons.util.DependencyFactory.ioDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/**
 * Base Class for view related model
 */
open class BaseAuthViewModel(
    mainDispatcher: CoroutineDispatcher = Dispatchers.Main,
    ioDispatcher: CoroutineDispatcher = ioDispatcher()
) : ViewModel() {

    private val job = SupervisorJob()
    protected val mUiScope = CoroutineScope(mainDispatcher + job)
    protected val mIoScope = CoroutineScope(ioDispatcher + job)

    override fun onCleared() {
        super.onCleared()
        job.cancel()
    }
}
