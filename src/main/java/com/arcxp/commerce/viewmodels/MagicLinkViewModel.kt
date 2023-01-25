package com.arcxp.commerce.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.arcxp.commerce.models.ArcXPOneTimeAccessLinkAuth
import com.arcxp.commerce.models.ArcXPOneTimeAccessLinkRequest
import com.arcxp.commerce.models.ArcXPOneTimeAccessLink
import com.arcxp.commerce.repositories.MagicLinkRepository
import com.arcxp.commerce.util.*
import kotlinx.coroutines.*

/**
 * @suppress
 */
class MagicLinkViewModel(
    private val repo: MagicLinkRepository,
    mainDispatcher: CoroutineDispatcher = Dispatchers.Main,
    ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : BaseAuthViewModel(mainDispatcher, ioDispatcher) {

    private val _magicLinkResponse = MutableLiveData<ArcXPOneTimeAccessLink>()
    val oneTimeAccessLinkResponse: LiveData<ArcXPOneTimeAccessLink> = _magicLinkResponse

    private val _magicLinkAuthResponse = MutableLiveData<ArcXPOneTimeAccessLinkAuth>()
    val oneTimeAccessLinkAuthResponse: LiveData<ArcXPOneTimeAccessLinkAuth> = _magicLinkAuthResponse

    private val _errorResponse = MutableLiveData<ArcXPError>()
    val errorResponse: LiveData<ArcXPError> = _errorResponse

    var nonce: String? = null
    var recaptchaToken: String? = null

    fun getMagicLink(email: String, recaptchaToken: String?) {
        mIoScope.launch {
            val timestamp = System.currentTimeMillis()
            
            val res = repo.getMagicLink(ArcXPOneTimeAccessLinkRequest(email, recaptchaToken))

            withContext(mUiScope.coroutineContext) {
                when (res) {
                    is Success -> {
                        _magicLinkResponse.postValue(res.r)
//                        ServerUtil.getWebServiceResponse(email, object: ServerEventListener {
//                            override fun onMessage(response: EventResponseModel) {
//                                nonce = response.message.nonce
//                                res.r?.nonce = response.message.nonce
//                                GlobalScope.launch {
//                                    _magicLinkResponse.postValue(res.r)
//                                }
//                            }
//                        })
                    }
                    is Failure -> _errorResponse.value = res.l as ArcXPError
                }
            }
        }
    }

    fun loginMagicLink(nonce: String) {
        mIoScope.launch {
            val res = repo.loginMagicLink(nonce)

            withContext(mUiScope.coroutineContext) {
                when (res) {
                    is Success -> {
                        _magicLinkAuthResponse.value = res.r
                        res.r?.let {
                            AuthManager.getInstance().cacheSession(it)
                        }
                    }
                    is Failure -> _errorResponse.value = res.l as ArcXPError
                }
            }
        }
    }

}

/**
 * @suppress
 */
interface NonceListener {
    fun onNonceEvent(response: EventResponseModel)
}