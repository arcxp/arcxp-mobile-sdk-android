package com.arcxp.commerce.viewmodels

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.*

/**
 * Base Class for view related model
 * @suppress
 */
open class BaseAuthViewModel(
    mainDispatcher: CoroutineDispatcher = Dispatchers.Main,
    ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

    private val job = SupervisorJob()
    protected val mUiScope = CoroutineScope(mainDispatcher + job)
    protected val mIoScope = CoroutineScope(ioDispatcher + job)

    override fun onCleared() {
        super.onCleared()
        job.cancel()
    }
}
