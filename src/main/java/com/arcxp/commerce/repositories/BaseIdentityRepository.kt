package com.arcxp.commerce.repositories

import com.arcxp.commerce.retrofit.IdentityService
import com.arcxp.commerce.retrofit.RetrofitController

/**
 * Base Class for remote repository
 * @suppress
 */
open class BaseIdentityRepository(private val identityService: IdentityService = RetrofitController.getIdentityService()) {

    protected fun getIdentityService() = identityService
}
