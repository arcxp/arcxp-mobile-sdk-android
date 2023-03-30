package com.arcxp.commerce.apimanagers

import androidx.fragment.app.Fragment
import com.arcxp.commerce.ArcXPCommerceConfig
import com.arcxp.commerce.callbacks.ArcXPIdentityListener
import com.arcxp.commerce.extendedModels.ArcXPProfileManage
import com.arcxp.commerce.models.*
import com.arcxp.commerce.repositories.IdentityRepository
import com.arcxp.commerce.ui.*
import com.arcxp.commerce.util.AuthManager
import com.arcxp.commerce.viewmodels.IdentityViewModel
import com.arcxp.commons.throwables.ArcXPException

/**
 * @suppress
 */

class IdentityApiManager(
    private val authManager: AuthManager, private val fragment: Fragment? = null,
    private val commerceListenerArc: ArcXPIdentityListener,
    private val viewModel: IdentityViewModel = IdentityViewModel(authManager, IdentityRepository())
) : BaseApiManager<Fragment>(fragment) {

    /**
     * Requests changing the users password
     *
     * @param newPassword New password string
     * @param oldPassword Old password string
     * @param callback Optional callback
     *
     * eg: setUpResetPasswordByUserName(et_old_password, et_new_password, btn_request) {
     *       ...
     *       activity?.showProgress(true)
     *       ...
     *    }
     */
    fun changePassword(
        newPassword: String,
        oldPassword: String,
        listener: ArcXPIdentityListener
    ) {
        viewModel.changeUserPassword(oldPassword, newPassword, object : ArcXPIdentityListener() {
            override fun onPasswordChangeSuccess(it: ArcXPIdentity) {
                listener.onPasswordChangeSuccess(it)
            }

            override fun onPasswordChangeError(error: ArcXPException) {
                listener.onPasswordChangeError(error)
            }
        })
    }

    /**
     * Resets the users password
     *
     * @param username Username of user to reset password
     *
     * eg: setUpResetPasswordByUserName(et_user_name, btn_request) {
     *       ...
     *       activity?.showProgress(true)
     *       ...
     *    }
     */
    fun obtainNonceByEmailAddress(email: String, listener: ArcXPIdentityListener) {
        viewModel.obtainNonceByEmailAddress(email, object : ArcXPIdentityListener() {
            override fun onPasswordResetNonceSuccess(response: ArcXPRequestPasswordReset?) {
                listener.onPasswordResetNonceSuccess(response)
            }

            override fun onPasswordResetNonceFailure(error: ArcXPException) {
                listener.onPasswordResetNonceFailure(error)
            }
        })
    }

    fun resetPasswordByNonce(nonce: String, newPassword: String, listener: ArcXPIdentityListener) {
        viewModel.resetPasswordByNonce(nonce, newPassword, object : ArcXPIdentityListener() {
            override fun onPasswordResetSuccess(response: ArcXPIdentity) {
                listener.onPasswordResetSuccess(response)
            }

            override fun onPasswordResetError(error: ArcXPException) {
                listener.onPasswordResetError(error)
            }
        })
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
    fun login(
        username: String,
        password: String,
        listener: ArcXPIdentityListener
    ) {
        viewModel.makeLoginCall(
            username,
            password,
            getRecaptchaToken(),
            object : ArcXPIdentityListener() {
                override fun onLoginSuccess(response: ArcXPAuth) {
                    listener.onLoginSuccess(response)
                }

                override fun onLoginError(error: ArcXPException) {
                    listener.onLoginError(error)
                }
            })
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
        viewModel.thirdPartyLoginCall(token, type, getCallbackScheme())
    }

    fun thirdPartyLogin(
        token: String,
        type: ArcXPAuthRequest.Companion.GrantType,
        arcIdentityListener: ArcXPIdentityListener
    ) {
        viewModel.thirdPartyLoginCall(token, type, object : ArcXPIdentityListener() {
            override fun onLoginSuccess(response: ArcXPAuth) {
                arcIdentityListener.onLoginSuccess(response)
            }

            override fun onLoginError(error: ArcXPException) {
                arcIdentityListener.onLoginError(error)
            }
        })
    }

    fun sendVerificationEmail(email: String, listener: ArcXPIdentityListener) {
        viewModel.verifyEmailCall(email, object : ArcXPIdentityListener() {
            override fun onEmailVerificationSentSuccess(it: ArcXPEmailVerification) {
                listener.onEmailVerificationSentSuccess(it)
            }

            override fun onEmailVerificationSentError(error: ArcXPException) {
                listener.onEmailVerificationSentError(error)
            }
        })
    }

    fun verifyEmail(nonce: String, listener: ArcXPIdentityListener) {
        viewModel.verifyEmail(nonce, object : ArcXPIdentityListener() {
            override fun onEmailVerifiedSuccess(response: ArcXPEmailVerification) {
                listener.onEmailVerifiedSuccess(response)
            }

            override fun onEmailVerifiedError(error: ArcXPException) {
                listener.onEmailVerifiedError(error)
            }
        })
    }

    fun checkRecaptcha(config: ArcXPCommerceConfig) {
        viewModel.checkRecaptcha(config.context!!, config.recaptchaSiteKey!!, getCallbackScheme())
    }

    fun getNonce(): String? {
        return viewModel.nonce
    }

    fun getMagicLink(
        email: String, listener: ArcXPIdentityListener
    ) {
        viewModel.getMagicLink(email, getRecaptchaToken(), object : ArcXPIdentityListener() {
            override fun onOneTimeAccessLinkSuccess(response: ArcXPOneTimeAccessLink) {
                listener.onOneTimeAccessLinkSuccess(response)
            }

            override fun onOneTimeAccessLinkError(error: ArcXPException) {
                listener.onOneTimeAccessLinkError(error)
            }
        })
    }

    fun loginMagicLink(nonce: String, listener: ArcXPIdentityListener) {
        viewModel.loginMagicLink(nonce, object : ArcXPIdentityListener() {
            override fun onOneTimeAccessLinkLoginSuccess(response: ArcXPOneTimeAccessLinkAuth) {
                listener.onOneTimeAccessLinkLoginSuccess(response)
            }

            override fun onOneTimeAccessLinkError(error: ArcXPException) {
                listener.onOneTimeAccessLinkError(error)
            }
        })
    }

    fun updateProfile(update: ArcXPProfilePatchRequest, listener: ArcXPIdentityListener) {
        viewModel.patchProfile(update, object : ArcXPIdentityListener() {
            override fun onProfileUpdateSuccess(profileManageResponse: ArcXPProfileManage) {
                listener.onProfileUpdateSuccess(profileManageResponse)
            }

            override fun onProfileError(error: ArcXPException) {
                listener.onProfileError(error)
            }
        })
    }

    /**
     * Function to get user profile
     *
     * eg: apiManager.getProfile()
     */
    fun getProfile(listener: ArcXPIdentityListener) {
        viewModel.getProfile(object : ArcXPIdentityListener() {
            override fun onFetchProfileSuccess(profileResponse: ArcXPProfileManage) {
                listener.onFetchProfileSuccess(profileResponse)
            }

            override fun onProfileError(error: ArcXPException) {
                listener.onProfileError(error)
            }
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
    fun registerUser(
        username: String,
        password: String,
        email: String,
        firstname: String? = null,
        lastname: String? = null,
        listener: ArcXPIdentityListener
    ) {
        viewModel.makeRegistrationCall(
            username,
            password,
            email,
            firstname,
            lastname,
            object : ArcXPIdentityListener() {
                override fun onRegistrationSuccess(response: ArcXPUser) {
                    listener.onRegistrationSuccess(response)
                }

                override fun onRegistrationError(error: ArcXPException) {
                    listener.onRegistrationError(error)
                }
            })
    }

    /**
     * log out current user logic
     */
    fun logout(listener: ArcXPIdentityListener) {
        viewModel.logout(object : ArcXPIdentityListener() {
            override fun onLogoutSuccess() {
                listener.onLogoutSuccess()
            }

            override fun onLogoutError(error: ArcXPException) {
                listener.onLogoutError(error)
            }
        })
    }

    fun removeIdentity(grantType: String, listener: ArcXPIdentityListener) {
        viewModel.removeIdentity(grantType, object : ArcXPIdentityListener() {
            override fun onRemoveIdentitySuccess(response: ArcXPUpdateUserStatus) {
                listener.onRemoveIdentitySuccess(response)
            }

            override fun onRemoveIdentityFailure(error: ArcXPException) {
                listener.onRemoveIdentityFailure(error)
            }
        })
    }

    /**
     * Make account deletion request
     */
    fun deleteUser(listener: ArcXPIdentityListener) {
        viewModel.deleteUser(object : ArcXPIdentityListener() {
            override fun onDeleteUserSuccess() {
                listener.onDeleteUserSuccess()
            }

            override fun onDeleteUserError(error: ArcXPException) {
                listener.onDeleteUserError(error)
            }
        })
    }

    /**
     * Approve account deletion
     */
    fun approveDeletion(nonce: String, listener: ArcXPIdentityListener) {
        viewModel.approveDeletion(nonce, object : ArcXPIdentityListener() {
            override fun onApproveDeletionSuccess(respone: ArcXPDeleteUser) {
                listener.onApproveDeletionSuccess(respone)
            }

            override fun onApproveDeletionError(error: ArcXPException) {
                listener.onApproveDeletionError(error)
            }
        })
    }

    private fun getCallbackScheme(): ArcXPIdentityListener? {
        return if (fragment == null) {
            commerceListenerArc
        } else {
            null
        }
    }

    fun validateJwt(token: String) {
        viewModel.validateJwt(token, getCallbackScheme())
    }

    fun validateJwt(listenerArc: ArcXPIdentityListener) {
        viewModel.validateJwt(object : ArcXPIdentityListener() {
            override fun onValidateSessionSuccess() {
                listenerArc.onValidateSessionSuccess()
                listenerArc.onIsLoggedIn(true)
            }

            override fun onValidateSessionError(error: ArcXPException) {
                viewModel.refreshToken(authManager.refreshToken,
                    ArcXPAuthRequest.Companion.GrantType.REFRESH_TOKEN.value,
                    object : ArcXPIdentityListener() {
                        override fun onRefreshSessionSuccess(response: ArcXPAuth) {
                            listenerArc.onValidateSessionSuccess()
                            listenerArc.onIsLoggedIn(true)
                        }

                        override fun onRefreshSessionFailure(error: ArcXPException) {
                            listenerArc.onIsLoggedIn(false)
                        }
                    }
                )
            }
        })
    }

    fun refreshToken(token: String?, grantType: String) {
        viewModel.refreshToken(token, grantType, getCallbackScheme())
    }

    fun rememberUser(remember: Boolean) {
        viewModel.rememberUser(remember)
    }

    fun loadConfig(listener: ArcXPIdentityListener) {
        viewModel.getTenetConfig(listener)
    }

}