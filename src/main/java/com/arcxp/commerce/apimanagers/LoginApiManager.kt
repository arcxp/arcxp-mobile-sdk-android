package com.arcxp.commerce.apimanagers

import android.widget.Button
import android.widget.CheckBox
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.lifecycle.OnLifecycleEvent
import com.arcxp.commerce.models.ArcXPAuthRequest
import com.arcxp.commerce.models.ArcXPAuth
import com.arcxp.commerce.models.ArcXPEmailVerification
import com.arcxp.commerce.ui.*
import com.arcxp.commerce.util.ArcXPError
import com.arcxp.commerce.viewmodels.LoginViewModel

/**
 * Manager class for handling login related logic, used by client developer.
 * Recommended way to create instance by using [createApiManger]
 *
 * @param fragment The Fragment hold login screen
 * @param loginUiInteraction the ui interaction [LoginUiInteraction] implemented by the fragment
 * @param viewModel view model [LoginViewModel] bind to the fragment
 *
 * eg: class LoginFragment: Fragment, LoginUiInteraction {
 *        ...
 *        val apiManager = createApiManager { LoginApiManager(it, it, LoginViewModel()) }
 *        ...
 *     }
 */

/**
 * @suppress
 */
class LoginApiManager<T : Fragment>(
    private val fragment: T,
    private val loginUiInteraction: LoginUiInteraction,
    private val viewModel: LoginViewModel
) : BaseApiManager<T>(fragment) {

    /**
     * Add observer for data change when view is created
     */
    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    private fun onCreate() {
        viewModel.authResponse.observe(fragment.viewLifecycleOwner, Observer {
            loginUiInteraction.onSuccessLogin(it)
        })

        viewModel.verificationResponse.observe(fragment.viewLifecycleOwner, Observer {
            loginUiInteraction.onEmailVerified(it)
        })

        viewModel.errorResponse.observe(fragment.viewLifecycleOwner, Observer {
            loginUiInteraction.onError(it)
        })

        viewModel.appleAuthUrl.observe(fragment.viewLifecycleOwner, Observer {
            loginUiInteraction.onAppleAuthUrlObtained(it)
        })
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    private fun onDestroy() {
        
    }

    /**
     * Set up logic for click button to send out password reset request with the given user name
     *
     * @param edt edit text for username and password
     * @param cbRemember Checkbox for remember me option
     * @param loginBtn Button to trigger password reset request
     * @param progress optional data loading indicator while making api call
     *
     * eg: apiManager.setupLogin(et_user_name, et_password, loginBtn = btn_login) {
     *        activity?.showProgress(true)
     *    }
     */
    fun setupLogin(
        vararg edt: ArcXPAuthEditText,
        cbRemember: CheckBox? = null,
        loginBtn: Button,
        progress: (() -> Unit)? = null
    ) {
        edt.forEach {
            it.addAfterTextListener()
        }
        loginBtn.setOnClickListener {
            progress?.invoke()
            cbRemember?.let { viewModel.shouldRememberUser = it.isChecked }
            var userName = ""
            var password = ""
            var allValidate = true
            edt.forEach { et ->
                et.validate()?.let { errorIndex ->
                    et.error = et.errorText(errorIndex)
                    allValidate = false
                }
                when (et) {
                    is UserNameEditText -> userName = et.text.toString()
                    is PasswordEditText -> password = et.text.toString()
                    else -> return@forEach
                }
            }
            if (!allValidate) {
                loginUiInteraction.onError(ArcXPError("Please check all inputs"))
                return@setOnClickListener
            }
            viewModel.makeLoginCall(userName, password, getRecaptchaToken())
        }
    }

    fun getRecaptchaToken() = viewModel.recaptchaToken
    fun setRecaptchaToken(recaptchaToken: String) {
        viewModel.recaptchaToken = recaptchaToken
    }

    /**
     * Set up logic for click button to send out password reset request with the given user name
     *
     * @param token token sent by third party
     * @param type grant type of third party login
     */
    fun thirdPartyLogin(token: String, type: ArcXPAuthRequest.Companion.GrantType) {
        viewModel.thirdPartyLoginCall(token, type)
    }

    fun sendVerificationEmail(email: String) {
        viewModel.verifyEmailCall(email)
    }

    fun appleAuthUrl(progress: (() -> Unit)? = null) {
        progress?.invoke()
        viewModel.appleAuthUrl()
    }

}

/**
 * Interface used to update fragment UI
 * @suppress
 */
interface LoginUiInteraction {
    fun onSuccessLogin(response: ArcXPAuth)
    fun onError(error: ArcXPError)
    fun onEmailVerified(it: ArcXPEmailVerification?)
    fun onAppleAuthUrlObtained(url: String)
}
