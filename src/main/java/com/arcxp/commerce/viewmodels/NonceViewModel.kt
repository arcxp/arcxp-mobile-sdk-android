package com.arcxp.commerce.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.arcxp.commerce.models.ArcXPOneTimeAccessLinkRequest
import com.arcxp.commerce.models.ArcXPOneTimeAccessLink
import com.arcxp.commerce.repositories.MagicLinkRepository
import com.arcxp.commerce.util.*
import kotlinx.coroutines.*

/**
 * @suppress
 */
class NonceViewModel(
        private val repo: MagicLinkRepository,
        mainDispatcher: CoroutineDispatcher = Dispatchers.Main,
        ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : BaseAuthViewModel(mainDispatcher, ioDispatcher) {

    private val _nonceResponse = MutableLiveData<ArcXPOneTimeAccessLink>()
    val nonceResponse: LiveData<ArcXPOneTimeAccessLink> = _nonceResponse

    private val _errorResponse = MutableLiveData<String>()
    val errorResponse: LiveData<String> = _errorResponse

    var nonce: String? = null
    var recaptchaToken: String? = null

    fun getMagicLink(email: String, recaptchaToken: String?) {
        mIoScope.launch {
            val timestamp = System.currentTimeMillis()

            val res = repo.getMagicLink(ArcXPOneTimeAccessLinkRequest(email, recaptchaToken))

            withContext(mUiScope.coroutineContext) {
                when (res) {
                    is Success -> {
                        _nonceResponse.postValue(res.r)
//                        ServerUtil.getWebServiceResponse(email, object: ServerEventListener {
//                            override fun onMessage(response: EventResponseModel) {
//                                nonce = response.message.nonce
//                                res.r?.nonce = response.message.nonce
//                                GlobalScope.launch {
//                                    _nonceResponse.postValue(res.r)
//                                }
//                            }
//                        })
                    }
                    is Failure -> _errorResponse.value = handleFailure(res.l)
                }
            }
        }
    }

}

/**
 * @suppress
 */
interface NonceListener1 {
    fun onNonceEvent(response: EventResponseModel)
}