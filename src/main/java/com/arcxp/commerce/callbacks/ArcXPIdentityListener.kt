package com.arcxp.commerce.callbacks

import com.arcxp.commerce.extendedModels.ArcXPProfileManage
import com.arcxp.commerce.models.*
import com.arcxp.commerce.models.applesignin.SignInWithAppleResult
import com.arcxp.commons.throwables.ArcXPException

/**
 * Interface used to update fragment UI
 */
abstract class ArcXPIdentityListener {
    open fun onLoginSuccess(response: ArcXPAuth) {}
    open fun onLoginError(error: ArcXPException) {}
    open fun onEmailVerificationSentSuccess(it: ArcXPEmailVerification) {}
    open fun onEmailVerificationSentError(error: ArcXPException) {}
    open fun onPasswordChangeSuccess(it: ArcXPIdentity) {}
    open fun onPasswordChangeError(error: ArcXPException) {}
    open fun onPasswordResetSuccess(response: ArcXPIdentity) {}
    open fun onPasswordResetError(error: ArcXPException) {}
    open fun onIdentitySuccess(response: ArcXPIdentity) {}
    open fun onError(error: ArcXPException) {}
    open fun onOneTimeAccessLinkSuccess(response: ArcXPOneTimeAccessLink) {}
    open fun onOneTimeAccessLinkLoginSuccess(response: ArcXPOneTimeAccessLinkAuth) {}
    open fun onOneTimeAccessLinkError(error: ArcXPException) {}
    open fun onProfileUpdateSuccess(profileManageResponse: ArcXPProfileManage) {}
    open fun onFetchProfileSuccess(profileResponse: ArcXPProfileManage) {}
    open fun onProfileError(error: ArcXPException) {}
    open fun onRegistrationSuccess(response: ArcXPUser) {}
    open fun onRegistrationError(error: ArcXPException) {}
    open fun onLogoutSuccess() {}
    open fun onLogoutError(error: ArcXPException) {}
    open fun onApproveDeletionSuccess(response: ArcXPDeleteUser){}
    open fun onApproveDeletionError(error: ArcXPException){}
    open fun onDeleteUserSuccess() {}
    open fun onDeleteUserError(error: ArcXPException) {}
    open fun onValidateSessionSuccess() {}
    open fun onValidateSessionError(error: ArcXPException) {}
    open fun onRefreshSessionSuccess(response: ArcXPAuth) {}
    open fun onRefreshSessionFailure(error: ArcXPException) {}
    open fun onIsLoggedIn(result: Boolean) {}
    open fun onRecaptchaSuccess(token: String) {}
    open fun onRecaptchaCancel() {}
    open fun onRecaptchaFailure(error: ArcXPException) {}
    open fun onAppleAuthUrlObtained(url: String){}
    open fun onAppleLoginSuccess(result: SignInWithAppleResult){}
    open fun onAppleLoginFailure(error: ArcXPException){}
    open fun onLoadConfigSuccess(result: ArcXPConfig) {}
    open fun onLoadConfigFailure(error: ArcXPException) {}
    open fun onEmailVerifiedSuccess(response: ArcXPEmailVerification){}
    open fun onEmailVerifiedError(error: ArcXPException){}
    open fun onPasswordResetNonceSuccess(response : ArcXPRequestPasswordReset?){}
    open fun onPasswordResetNonceFailure(error: ArcXPException){}
    open fun onRemoveIdentitySuccess(response: ArcXPUpdateUserStatus){}
    open fun onRemoveIdentityFailure(error: ArcXPException){}
    open fun onGoogleOneTapLoginSuccess(username: String?, password: String?, token: String?) {}
}