package com.arcxp.commerce.viewmodels

import androidx.lifecycle.ViewModel
import androidx.media3.common.util.UnstableApi
import com.arcxp.commons.util.DependencyFactory.ioDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/**
 * Base Class for view related model
 * @suppress
 */
@UnstableApi
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
