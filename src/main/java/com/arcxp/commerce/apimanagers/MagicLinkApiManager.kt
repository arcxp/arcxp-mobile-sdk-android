package com.arcxp.commerce.apimanagers

import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.lifecycle.OnLifecycleEvent
import com.arcxp.commerce.models.ArcXPOneTimeAccessLinkAuth
import com.arcxp.commerce.models.ArcXPOneTimeAccessLink
import com.arcxp.commerce.util.ArcXPError
import com.arcxp.commerce.viewmodels.MagicLinkViewModel

/**
 * @suppress
 */
class MagicLinkApiManager<T : Fragment>(
    private val fragment: T,
    private val magicLinkUiInteraction: MagicLinkUiInteraction,
    private val viewModel: MagicLinkViewModel
): BaseApiManager<T>(fragment) {


    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    private fun onCreate() {
        viewModel.oneTimeAccessLinkResponse.observe(fragment.viewLifecycleOwner, Observer {
            magicLinkUiInteraction.onSuccessMagicLink(it)
        })

        viewModel.oneTimeAccessLinkAuthResponse.observe(fragment.viewLifecycleOwner, Observer {
            magicLinkUiInteraction.onSuccessMagicLinkLogin(it)
        })

        viewModel.errorResponse.observe(fragment.viewLifecycleOwner, Observer {
            magicLinkUiInteraction.onError(it)
        })
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    private fun onDestroy() {

    }

    fun getRecaptchaToken() = viewModel.recaptchaToken
    fun setRecaptchaToken(recaptchaToken: String) {
        viewModel.recaptchaToken = recaptchaToken
    }

    fun getNonce(): String? {
        return viewModel.nonce
    }

    fun getMagicLink(
        email: String,
        recaptchaToken: String?
    ) {
        viewModel.getMagicLink(email, recaptchaToken)
    }

    fun loginMagicLink(nonce: String) {
        viewModel.loginMagicLink(nonce)
    }

}
 /**
  * @suppress
  */
interface MagicLinkUiInteraction {
    fun onSuccessMagicLink(response: ArcXPOneTimeAccessLink)
    fun onSuccessMagicLinkLogin(response: ArcXPOneTimeAccessLinkAuth)
    fun onError(error: ArcXPError)
}