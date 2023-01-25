package com.arcxp.commerce.apimanagers

import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.lifecycle.OnLifecycleEvent
import com.arcxp.commerce.models.ArcXPRequestPasswordReset
import com.arcxp.commerce.ui.UserNameEditText
import com.arcxp.commerce.ui.addAfterTextListener
import com.arcxp.commerce.viewmodels.ForgotPasswordViewModel

/**
 * Manager class for handling forgot password related logic, used by client developer.
 * Recommended way to create instance by using [createApiManger]
 *
 * @param fragment The Fragment hold forgot password screen
 * @param forgetPasswordUIInteraction the ui interaction [ForgotPasswordUIInteraction] implemented by the fragment
 * @param viewModel view model [ForgotPasswordViewModel] bind to the fragment
 *
 * eg: class ForgotPasswordFragment: Fragment, ForgotPasswordUIInteraction {
 *        ...
 *        val apiManager = createApiManager { ForgotPasswordApiManager(it, it, ForgotPasswordViewModel()) }
 *        ...
 * }
 */
/**
 * @suppress
 */
class ForgotPasswordApiManager<T : Fragment>(
    private val fragment: T,
    private val forgetPasswordUIInteraction: ForgotPasswordUIInteraction,
    private val viewModel: ForgotPasswordViewModel
) : BaseApiManager<T>(fragment) {

    /**
     * Add observer for data change when view is created
     */
    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    private fun onCreate() {
        viewModel.requestPasswordResetResponse.observe(fragment.viewLifecycleOwner, Observer {
            forgetPasswordUIInteraction.onSuccessRequest(it)
        })

        viewModel.errorResponse.observe(fragment.viewLifecycleOwner, Observer {
            forgetPasswordUIInteraction.onError(it)
        })
    }

    /**
     * Set up logic for click button to send out password reset request with the given user name
     * Used by the bind fragment,
     *
     * @param edt Edit text for input username
     * @param resetBtn Button to trigger password reset request
     * @param progress optional data loading indicator while making api call
     *
     * eg: setUpResetPasswordByUserName(et_user_name, btn_request) {
     *       ...
     *       activity?.showProgress(true)
     *       ...
     *    }
     */
    fun setUpResetPasswordByUserName(edt: UserNameEditText,
                                     resetBtn: Button,
                                     progress: (() -> Unit)? = null) {
        edt.addAfterTextListener()
        resetBtn.setOnClickListener {
            progress?.invoke()
            edt.validate()?.let { errorIndex ->
                edt.error = edt.errorText(errorIndex)
                return@setOnClickListener
            }
            viewModel.resetPasswordByUserName(edt.text.toString())
        }
    }

}

/**
 * Interface used to update fragment UI
 * @suppress
 */
interface ForgotPasswordUIInteraction {
    fun onSuccessRequest(response: ArcXPRequestPasswordReset)
    fun onError(error: String)
}
