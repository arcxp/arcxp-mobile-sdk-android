package com.arcxp.commerce.apimanagers

import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.lifecycle.OnLifecycleEvent
import com.arcxp.commerce.models.ArcXPEmailVerification
import com.arcxp.commerce.models.ArcXPUser
import com.arcxp.commerce.ui.*
import com.arcxp.commerce.util.ArcXPError
import com.arcxp.commerce.viewmodels.RegistrationViewModel

/**
 * Manager class for handling user registration. Used by client developer.
 *
 * @param fragment The Fragment hold registration screen
 * @param registrationUIInteraction the ui interaction [RegistrationUIInteraction] implemented by the fragment
 * @param viewModel view model [RegistrationViewModel] bind to the fragment
 *
 * eg: class RegistrationFragment: Fragment, RegistrationUIInteraction {
 *        ...
 *        val apiManager = createApiManager { RegistrationApiManager(it, it, RegistrationViewModel()) }
 *        ...
 *     }
 */
/**
 * @suppress
 */
class RegistrationApiManager<T : Fragment>(
    private val fragment: T,
    private val registrationUIInteraction: RegistrationUIInteraction,
    private val viewModel: RegistrationViewModel
) : BaseApiManager<T>(fragment) {

    /**
     * Add observer for data change when view is created
     */
    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    private fun onCreate() {
        viewModel.userResponse.observe(fragment.viewLifecycleOwner, Observer {
            registrationUIInteraction.onSuccessRegistration(it)
        })

        viewModel.verificationResponse.observe(fragment.viewLifecycleOwner, Observer {
            registrationUIInteraction.onEmailVerified(it)
        })

        viewModel.errorResponse.observe(fragment.viewLifecycleOwner, Observer {
            registrationUIInteraction.onError(it)
        })
    }

    /**
     * Set up logic for click button to send out profile patch request with the updated profile
     *
     * @param edt edit text for property input
     * @param regBtn Button to trigger registration request
     * @param progress optional data loading indicator while making api call
     *
     * eg: apiManager.setupRegistration(et_user_name, et_confirm_password, et_email, et_ et_password, loginBtn = btn_login) {
     *        activity?.showProgress(true)
     *    }
     */
    fun setupRegistration(
        vararg edt: ArcXPAuthEditText,
        regBtn: Button,
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
        regBtn.setOnClickListener {
            progress?.invoke()
            var userName = ""
            var password = ""
            var email = ""
            var firstName: String? = null
            var lastName: String? = null
            var allValidate = true
            edt.forEach { et ->
                et.validate()?.let { errorIndex ->
                    et.error = et.errorText(errorIndex)
                    allValidate = false
                }
                when (et) {
                    is UserNameEditText -> userName = et.text.toString()
                    is PasswordEditText -> password = et.text.toString()
                    is EmailEditText -> email = et.text.toString()
                    is FirstNameEditText -> firstName = et.text.toString()
                    is LastNameEditText -> lastName = et.text.toString()
                    else -> return@forEach
                }
            }
            if (!allValidate) {
                registrationUIInteraction.onError(ArcXPError("Please check all inputs"))
                return@setOnClickListener
            }
            viewModel.makeRegistrationCall(userName, password, email, firstName, lastName)
        }
    }

    /**
     * Function to send verification email
     */
    fun sendVerificationEmail(email: String) {
        viewModel.verifyEmailCall(email)
    }
}

/**
 * Interface used to update fragment UI
 * @suppress
 */
interface RegistrationUIInteraction {
    fun onSuccessRegistration(response: ArcXPUser)
    fun onEmailVerified(response: ArcXPEmailVerification)
    fun onError(error: ArcXPError)
}
