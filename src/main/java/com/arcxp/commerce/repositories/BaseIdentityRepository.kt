package com.arcxp.commerce.repositories

import androidx.media3.common.util.UnstableApi
import com.arcxp.commerce.retrofit.IdentityService
import com.arcxp.commerce.retrofit.RetrofitController

/**
 * Base Class for remote repository
 * @suppress
 */
@UnstableApi
open class BaseIdentityRepository(private val identityService: IdentityService = RetrofitController.getIdentityService()) {

    protected fun getIdentityService() = identityService
}
