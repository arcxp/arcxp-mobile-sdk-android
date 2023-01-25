package com.arcxp.commerce.apimanagers

import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.lifecycle.OnLifecycleEvent
import com.arcxp.commerce.models.ArcXPIdentity
import com.arcxp.commerce.ui.*
import com.arcxp.commerce.util.ArcXPError
import com.arcxp.commerce.viewmodels.ChangePasswordViewModel


/**
 * Manager class for handling forgot password related logic, used by client developer.
 * Recommended way to create instance by using [createApiManger]
 *
 * @param fragment The Fragment hold forgot password screen
 * @param changePasswordUIInteraction the ui interaction [ChangePasswordUIInteraction] implemented by the fragment
 * @param viewModel view model [ChangePasswordViewModel] bind to the fragment
 *
 * eg: class ChangePasswordFragment: Fragment, ChangePasswordUIInteraction {
 *        ...
 *        val apiManager = createApiManager { ChangePasswordApiManager(it, it, ChangePasswordViewModel()) }
 *        ...
 * }
 */
/**
 * @suppress
 */
class ChangePasswordApiManager<T : Fragment>(
    private val fragment: T,
    private val forgetPasswordUIInteraction: ChangePasswordUIInteraction,
    private val viewModel: ChangePasswordViewModel
) : BaseApiManager<T>(fragment) {

    /**
     * Add observer for data change when view is created
     */
    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    private fun onCreate() {
        viewModel.identityResponse.observe(fragment.viewLifecycleOwner, Observer {
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
     * @param edt Edit text for inputs
     * @param changeBtn Button to trigger password reset request
     * @param progress optional data loading indicator while making api call
     *
     * eg: setUpResetPasswordByUserName(et_old_password, et_new_password, btn_request) {
     *       ...
     *       activity?.showProgress(true)
     *       ...
     *    }
     */
    fun setUpChangePassword(
        vararg edt: ArcXPAuthEditText,
        changeBtn: Button,
        progress: (() -> Unit)? = null
    ) {
        val confirmPasswordEditText = edt.firstOrNull {
            it is ConfirmPasswordEditText
        }
        confirmPasswordEditText?.let { cpe ->
            (cpe as ConfirmPasswordEditText).addAfterTextListener(edt.first { it is PasswordEditText } as PasswordEditText)
        }
        edt.forEach {
            it.addAfterTextListener()
        }
        changeBtn.setOnClickListener {
            progress?.invoke()
            var oldPassword = ""
            var newPassword = ""
            var allValidate = true
            edt.forEach { et ->
                et.validate()?.let { errorIndex ->
                    et.error = et.errorText(errorIndex)
                    allValidate = false
                }
                when (et) {
                    is OldPasswordEditText -> oldPassword = et.text.toString()
                    is PasswordEditText -> newPassword = et.text.toString()
                    else -> return@forEach
                }
            }
            if (!allValidate) {
                forgetPasswordUIInteraction.onError(ArcXPError("Please check all inputs"))
                return@setOnClickListener
            }
            viewModel.changeUserPassword(oldPassword, newPassword)
        }
    }
}

/**
 * Interface used to update fragment UI
 * @suppress
 */
interface ChangePasswordUIInteraction {
    fun onSuccessRequest(response: ArcXPIdentity)
    fun onError(error: ArcXPError)
}
