package com.arcxp.commerce

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.Keep
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.arcxp.arccommerce.R
import com.arcxp.commerce.apimanagers.*
import com.arcxp.commerce.extendedModels.ArcXPProfileManage
import com.arcxp.commerce.models.*
import com.arcxp.commerce.models.applesignin.SignInWithAppleConfiguration
import com.arcxp.commerce.models.applesignin.SignInWithAppleResult
import com.arcxp.commerce.models.applesignin.view.SignInWithAppleButton
import com.arcxp.commerce.paywall.PaywallManager
import com.arcxp.commerce.util.*
import com.arcxp.commerce.util.ArcXPError
import com.arcxp.commons.analytics.ArcXPAnalyticsManager
import com.facebook.*
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.facebook.login.widget.LoginButton
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.tasks.Task
import kotlin.collections.HashMap

@Keep
class ArcXPCommerceManager {

    private lateinit var mContext: Context

    private lateinit var arcIdentityListener: ArcXPIdentityListener
    private lateinit var arcxpSalesListener: ArcXPSalesListener
    private lateinit var arcxpRetailListener: ArcXPRetailListener

    private var arcIListener: ArcXPIdentityListener? = null
    private var arcxpSListener: ArcXPSalesListener? = null
    private var arcxpRListener: ArcXPRetailListener? = null

    private lateinit var authManager: AuthManager

    private lateinit var identityApiManager: IdentityApiManager
    private lateinit var salesApiManager: SalesApiManager
    private lateinit var retailApiManager: RetailApiManager

    private lateinit var commerceConfig: ArcXPCommerceConfig

    private lateinit var paywallManager: PaywallManager

    private var oneTapClient: SignInClient? = null
    private lateinit var signInRequest: BeginSignInRequest

    private var loginWithGoogleResultsReceiver: LoginWithGoogleResultsReceiver? = null
    private var loginWithGoogleOneTapResultsReceiver: LoginWithGoogleOneTapResultsReceiver? = null

    private val _error = MutableLiveData<ArcXPError>()

    private val arcXPAnalyticsManager: ArcXPAnalyticsManager = ArcXPCommerceSDK.analytics()

    val errors: LiveData<ArcXPError>
        get() = _error

    private val callbackManager by lazy {
        CallbackManager.Factory.create()
    }

    private fun create(
        context: Context,
        clientCachedData: Map<String, String>,
        config: ArcXPCommerceConfig
    ) {

        if (config.orgName.isNullOrBlank() && config.baseUrl.isNullOrBlank()) {
            throw ArcXPError(
                ArcXPCommerceSDKErrorType.INIT_ERROR,
                "orgName or base url cannot be blank"
            )
        }
        this.commerceConfig = config

        mContext = context
        authManager = AuthManager.getInstance(context, clientCachedData, config)

        arcIdentityListener = object : ArcXPIdentityListener() {
            override fun onLoginSuccess(response: ArcXPAuth) {
                arcIListener?.onLoginSuccess(response)
            }

            override fun onLoginError(error: ArcXPError) {
                arcIListener?.onLoginError(error)
            }

            override fun onPasswordChangeSuccess(response: ArcXPIdentity) {
                arcIListener?.onPasswordChangeSuccess(response)
            }

            override fun onPasswordChangeError(error: ArcXPError) {
                arcIListener?.onPasswordChangeError(error)
            }

            override fun onPasswordResetSuccess(response: ArcXPIdentity) {
                arcIListener?.onPasswordResetSuccess(response)
            }

            override fun onPasswordResetError(error: ArcXPError) {
                arcIListener?.onPasswordResetError(error)
            }

            override fun onProfileUpdateSuccess(profileManageResponse: ArcXPProfileManage) {
                arcIListener?.onProfileUpdateSuccess(profileManageResponse)
            }

            override fun onProfileError(error: ArcXPError) {
                arcIListener?.onProfileError(error)
            }

            override fun onEmailVerificationSentSuccess(it: ArcXPEmailVerification) {
                arcIListener?.onEmailVerificationSentSuccess(it)
            }

            override fun onEmailVerificationSentError(error: ArcXPError) {
                arcIListener?.onEmailVerificationSentError(error)
            }

            override fun onFetchProfileSuccess(profileResponse: ArcXPProfileManage) {
                arcIListener?.onFetchProfileSuccess(profileResponse)
            }

            override fun onRegistrationSuccess(response: ArcXPUser) {
                arcIListener?.onRegistrationSuccess(response)
            }

            override fun onRegistrationError(error: ArcXPError) {
                arcIListener?.onRegistrationError(error)
            }

            override fun onLogoutSuccess() {
                arcIListener?.onLogoutSuccess()
            }

            override fun onLogoutError(error: ArcXPError) {
                arcIListener?.onLoginError(error)
            }

            override fun onDeleteUserSuccess() {
                arcIListener?.onDeleteUserSuccess()
            }

            override fun onDeleteUserError(error: ArcXPError) {
                arcIListener?.onDeleteUserError(error)
            }

            override fun onValidateSessionSuccess() {
                arcIListener?.onValidateSessionSuccess()
            }

            override fun onValidateSessionError(error: ArcXPError) {
                arcIListener?.onValidateSessionError(error)
            }

            override fun onRefreshSessionSuccess(response: ArcXPAuth) {
                arcIListener?.onRefreshSessionSuccess(response)
            }

            override fun onRefreshSessionFailure(error: ArcXPError) {
                arcIListener?.onRefreshSessionFailure(error)
            }

            override fun onRecaptchaSuccess(token: String) {
                arcIListener?.onRecaptchaSuccess(token)
            }

            override fun onRecaptchaCancel() {
                arcIListener?.onRecaptchaCancel()
            }

            override fun onRecaptchaFailure(error: ArcXPError) {
                arcIListener?.onRecaptchaFailure(error)
            }

            override fun onAppleAuthUrlObtained(url: String) {
                arcIListener?.onAppleAuthUrlObtained(url)
            }

            override fun onAppleLoginSuccess(result: SignInWithAppleResult) {
                arcIListener?.onAppleLoginSuccess(result)
            }

            override fun onAppleLoginFailure(error: ArcXPError) {
                arcIListener?.onAppleLoginFailure(error)
            }
        }

        arcxpSalesListener = object : ArcXPSalesListener() {
            override fun onGetAllSubscriptionsSuccess(response: ArcXPSubscriptions) {
                arcxpSListener?.onGetAllSubscriptionsSuccess(response)
            }

            override fun onGetSubscriptionsFailure(error: ArcXPError) {
                arcxpSListener?.onGetSubscriptionsFailure(error)
            }

            override fun onGetAllActiveSubscriptionsSuccess(response: ArcXPSubscriptions) {
                arcxpSalesListener.onGetAllActiveSubscriptionsSuccess(response)
            }
        }

        arcxpRetailListener = object : ArcXPRetailListener() {
            override fun onGetActivePaywallRulesSuccess(responseArcxp: ArcXPActivePaywallRules) {
                arcxpRListener?.onGetActivePaywallRulesSuccess(responseArcxp)
            }

            override fun onGetActivePaywallRulesFailure(error: ArcXPError) {
                arcxpRListener?.onGetActivePaywallRulesFailure(error)
            }
        }

        identityApiManager = IdentityApiManager(authManager, null, arcIdentityListener)
        salesApiManager = SalesApiManager(null, arcxpSalesListener)
        retailApiManager = RetailApiManager(authManager, null, arcxpRetailListener)

        paywallManager = PaywallManager(context, retailApiManager, salesApiManager)

        if (!commerceConfig.useLocalConfig) {
            identityApiManager.loadConfig(object : ArcXPIdentityListener() {
                override fun onLoadConfigSuccess(result: ArcXPConfig) {
                    AuthManager.getInstance().setConfig(result)
                    Log.i(
                        "ArcSDK",
                        "Remote Tenet Config loaded: \nFacebook ID: ${result.facebookAppId}\nGoogle ID: ${result.googleClientId}"
                    )
                }

                override fun onLoadConfigFailure(error: ArcXPError) {
                    AuthManager.getInstance().loadLocalConfig(config)
                    Log.i("ArcSDK", "Tenet Config loaded from cache")
                }
            })
        } else {
            AuthManager.getInstance().setConfig(
                ArcXPConfig(
                    facebookAppId = commerceConfig.facebookAppId,
                    googleClientId = commerceConfig.googleClientId,
                    signupRecaptcha = commerceConfig.recaptchaForSignup,
                    signinRecaptcha = commerceConfig.recaptchaForSignin,
                    recaptchaSiteKey = commerceConfig.recaptchaSiteKey,
                    magicLinkRecapatcha = commerceConfig.recaptchaForOneTimeAccess,
                    disqus = null,
                    keyId = null,
                    orgTenants = null,
                    pwLowercase = commerceConfig.pwLowercase,
                    pwMinLength = commerceConfig.pwMinLength,
                    pwPwNumbers = commerceConfig.pwPwNumbers,
                    pwSpecialCharacters = commerceConfig.pwSpecialCharacters,
                    pwUppercase = commerceConfig.pwUppercase,
                    teamId = null,
                    urlToReceiveAuthToken = null
                )
            )
            Log.i("ArcSDK", "Local Tenet Config loaded")
        }
    }

    fun login(
        email: String,
        password: String,
        listener: ArcXPIdentityListener? = null
    ): LiveData<Either<ArcXPError, ArcXPAuth>> {
        arcIListener = listener
        val stream = MutableLiveData<Either<ArcXPError, ArcXPAuth>>()
        if (commerceConfig.recaptchaForSignin) {
            runRecaptcha(object : ArcXPIdentityListener() {
                override fun onRecaptchaSuccess(token: String) {
                    arcIListener = listener
                    setRecaptchaToken(token)
                    identityApiManager.login(email, password, object : ArcXPIdentityListener() {
                        override fun onLoginSuccess(response: ArcXPAuth) {
                            listener?.onLoginSuccess(response)
                            stream.postValue(Success(response))
                        }

                        override fun onLoginError(error: ArcXPError) {
                            listener?.onLoginError(error)
                            stream.postValue(Failure(error))
                        }
                    })
                }

                override fun onRecaptchaFailure(error: ArcXPError) {
                    listener?.onLoginError(
                        ArcXPError(
                            ArcXPCommerceSDKErrorType.LOGIN_ERROR,
                            "Recaptcha error during login", error
                        )
                    )
                }

                override fun onRecaptchaCancel() {

                }
            })
        } else {
            identityApiManager.login(email, password, object : ArcXPIdentityListener() {
                override fun onLoginSuccess(response: ArcXPAuth) {
                    listener?.onLoginSuccess(response)
                    stream.postValue(Success(response))
                }

                override fun onLoginError(error: ArcXPError) {
                    stream.postValue(Failure(error))
                }
            })
        }
        return stream
    }


    @Deprecated(
        "Use updatePassword()",
        ReplaceWith(expression = "updatePassword(newPassword, oldPassword, listener)")
    )
    fun changePassword(newPassword: String, oldPassword: String, listener: ArcXPIdentityListener) {
        arcIListener = listener
        identityApiManager.changePassword(
            newPassword,
            oldPassword,
            object : ArcXPIdentityListener() {
                override fun onPasswordChangeSuccess(it: ArcXPIdentity) {
                    listener.onPasswordChangeSuccess(it)
                }

                override fun onPasswordChangeError(error: ArcXPError) {
                    listener.onPasswordChangeError(error)
                }
            })
    }

    fun updatePassword(
        newPassword: String,
        oldPassword: String,
        listener: ArcXPIdentityListener?
    ): LiveData<Either<ArcXPError, ArcXPIdentity>> {
        arcIListener = listener
        val stream = MutableLiveData<Either<ArcXPError, ArcXPIdentity>>()
        identityApiManager.changePassword(
            newPassword,
            oldPassword,
            object : ArcXPIdentityListener() {
                override fun onPasswordChangeSuccess(it: ArcXPIdentity) {
                    listener?.onPasswordChangeSuccess(it)
                    stream.postValue(Success(it))
                }

                override fun onPasswordChangeError(error: ArcXPError) {
                    listener?.onPasswordChangeError(error)
                    stream.postValue(Failure(error))
                }
            })
        return stream
    }


    @Deprecated(
        "Use requestResetPassword()",
        ReplaceWith(expression = "requestResetPassword(email, listener)")
    )
    fun obtainNonceByEmailAddress(email: String, listener: ArcXPIdentityListener) {
        arcIListener = listener
        identityApiManager.obtainNonceByEmailAddress(email, object : ArcXPIdentityListener() {
            override fun onPasswordResetNonceSuccess(response: ArcXPRequestPasswordReset?) {
                listener.onPasswordResetNonceSuccess(response)
            }

            override fun onPasswordResetNonceFailure(error: ArcXPError) {
                listener.onPasswordResetNonceFailure(error)
            }
        })
    }

    fun requestResetPassword(username: String, listener: ArcXPIdentityListener) {
        arcIListener = listener
        identityApiManager.obtainNonceByEmailAddress(username, object : ArcXPIdentityListener() {
            override fun onPasswordResetNonceSuccess(response: ArcXPRequestPasswordReset?) {
                listener.onPasswordResetNonceSuccess(response)
            }

            override fun onPasswordResetNonceFailure(error: ArcXPError) {
                listener.onPasswordResetNonceFailure(error)
            }
        })
    }


    @Deprecated(
        "Use resetPassword",
        ReplaceWith(expression = "resetPassword(nonce, newPassword, listener)")
    )
    fun resetPasswordByNonce(nonce: String, newPassword: String, listener: ArcXPIdentityListener) {
        arcIListener = listener
        identityApiManager.resetPasswordByNonce(
            nonce,
            newPassword,
            object : ArcXPIdentityListener() {
                override fun onPasswordResetSuccess(response: ArcXPIdentity) {
                    listener.onPasswordResetSuccess(response)
                }

                override fun onPasswordResetError(error: ArcXPError) {
                    listener.onPasswordResetError(error)
                }
            })
    }

    fun resetPassword(nonce: String, newPassword: String, listener: ArcXPIdentityListener) {
        arcIListener = listener
        identityApiManager.resetPasswordByNonce(
            nonce,
            newPassword,
            object : ArcXPIdentityListener() {
                override fun onPasswordResetSuccess(response: ArcXPIdentity) {
                    listener.onPasswordResetSuccess(response)
                }

                override fun onPasswordResetError(error: ArcXPError) {
                    listener.onPasswordResetError(error)
                }
            })
    }

    fun requestOneTimeAccessLink(email: String, listener: ArcXPIdentityListener) {
        arcIListener = listener
        if (commerceConfig.recaptchaForOneTimeAccess) {
            runRecaptcha(object : ArcXPIdentityListener() {
                override fun onRecaptchaSuccess(token: String) {
                    setRecaptchaToken(token)
                    identityApiManager.getMagicLink(email, object : ArcXPIdentityListener() {
                        override fun onOneTimeAccessLinkSuccess(response: ArcXPOneTimeAccessLink) {
                            listener.onOneTimeAccessLinkSuccess(response)
                        }
                    })
                }

                override fun onRecaptchaFailure(error: ArcXPError) {
                    arcIdentityListener.onOneTimeAccessLinkError(
                        ArcXPError(
                            ArcXPCommerceSDKErrorType.ONE_TIME_ACCESS_LINK_ERROR,
                            "Recaptcha error during magic link", error
                        )
                    )
                }

                override fun onRecaptchaCancel() {

                }
            })
        } else {
            identityApiManager.getMagicLink(email, object : ArcXPIdentityListener() {
                override fun onOneTimeAccessLinkSuccess(response: ArcXPOneTimeAccessLink) {
                    listener.onOneTimeAccessLinkSuccess(response)
                }

                override fun onOneTimeAccessLinkError(error: ArcXPError) {
                    listener.onOneTimeAccessLinkError(error)
                }
            })
        }
    }

    @Deprecated(
        "Use redeemOneTimeAccessLink()",
        ReplaceWith(expression = "redeemOneTimeAccessLink(nonce, listener)")
    )
    fun loginOneTimeAccessLink(nonce: String, listener: ArcXPIdentityListener) {
        arcIListener = listener
        identityApiManager.loginMagicLink(nonce, object : ArcXPIdentityListener() {
            override fun onOneTimeAccessLinkLoginSuccess(response: ArcXPOneTimeAccessLinkAuth) {
                listener.onOneTimeAccessLinkLoginSuccess(response)
            }

            override fun onOneTimeAccessLinkError(error: ArcXPError) {
                listener.onOneTimeAccessLinkError(error)
            }
        })
    }

    fun redeemOneTimeAccessLink(nonce: String, listener: ArcXPIdentityListener) {
        arcIListener = listener
        identityApiManager.loginMagicLink(nonce, object : ArcXPIdentityListener() {
            override fun onOneTimeAccessLinkLoginSuccess(response: ArcXPOneTimeAccessLinkAuth) {
                listener.onOneTimeAccessLinkLoginSuccess(response)
            }

            override fun onOneTimeAccessLinkError(error: ArcXPError) {
                listener.onOneTimeAccessLinkError(error)
            }
        })
    }

    fun updateProfile(update: ArcXPUpdateProfileRequest, listener: ArcXPIdentityListener) {
        arcIListener = listener
        val request = ArcXPProfilePatchRequest(
            update.firstName,
            update.lastName,
            update.secondLastName,
            update.displayName,
            update.gender,
            update.email,
            update.picture,
            update.birthYear,
            update.birthMonth,
            update.birthDay,
            update.legacyId,
            update.contacts,
            update.addresses,
            update.attributes
        )
        identityApiManager.updateProfile(request, object : ArcXPIdentityListener() {
            override fun onProfileUpdateSuccess(profileManageResponse: ArcXPProfileManage) {
                listener.onProfileUpdateSuccess(profileManageResponse)
            }

            override fun onProfileError(error: ArcXPError) {
                listener.onProfileError(error)
            }
        })
    }

    @Deprecated("Use getUserProfile()", ReplaceWith(expression = "getUserProfile(listener)"))
    fun fetchProfile(listener: ArcXPIdentityListener) {
        arcIListener = listener
        identityApiManager.getProfile(object : ArcXPIdentityListener() {
            override fun onFetchProfileSuccess(profileResponse: ArcXPProfileManage) {
                listener.onFetchProfileSuccess(profileResponse)
            }

            override fun onProfileError(error: ArcXPError) {
                listener.onProfileError(error)
            }
        })
    }

    fun getUserProfile(listener: ArcXPIdentityListener? = null): LiveData<Either<ArcXPError, ArcXPProfileManage>> {
        arcIListener = listener
        val stream = MutableLiveData<Either<ArcXPError, ArcXPProfileManage>>()
        identityApiManager.getProfile(object : ArcXPIdentityListener() {
            override fun onFetchProfileSuccess(profileResponse: ArcXPProfileManage) {
                stream.postValue(Success(profileResponse))
                listener?.onFetchProfileSuccess(profileResponse)
            }

            override fun onProfileError(error: ArcXPError) {
                stream.postValue((Failure(error)))
                listener?.onProfileError(error)
            }
        })
        return stream
    }

    @Deprecated(
        "Use signUp()",
        ReplaceWith(expression = "signUp(username, password, email, firstname, lastname, listener)")
    )
    fun registerUser(
        username: String,
        password: String,
        email: String,
        firstname: String,
        lastname: String,
        listener: ArcXPIdentityListener
    ) {
        signUp(username, password, email, firstname, lastname, listener)
    }

    fun signUp(
        username: String,
        password: String,
        email: String,
        firstname: String? = null,
        lastname: String? = null,
        listener: ArcXPIdentityListener? = null
    ): LiveData<ArcXPUser> {
        arcIListener = listener
        val stream = MutableLiveData<ArcXPUser>()
        if (commerceConfig.recaptchaForSignup) {
            runRecaptcha(object : ArcXPIdentityListener() {
                override fun onRecaptchaSuccess(token: String) {
                    setRecaptchaToken(token)
                    identityApiManager.registerUser(
                        username,
                        password,
                        email,
                        firstname,
                        lastname,
                        object : ArcXPIdentityListener() {
                            override fun onRegistrationSuccess(response: ArcXPUser) {
                                listener?.onRegistrationSuccess(response)
                                stream.postValue(response)
                            }

                            override fun onRegistrationError(error: ArcXPError) {
                                listener?.onRegistrationError(error)
                                _error.postValue(error)
                            }
                        })
                }

                override fun onRecaptchaFailure(error: ArcXPError) {
                    arcIdentityListener.onRegistrationError(
                        ArcXPError(
                            ArcXPCommerceSDKErrorType.REGISTRATION_ERROR,
                            "Recaptcha error during magic link", error
                        )
                    )
                    _error.postValue(error)
                }

                override fun onRecaptchaCancel() {

                }
            })
        } else {
            identityApiManager.registerUser(
                username,
                password,
                email,
                firstname,
                lastname,
                object : ArcXPIdentityListener() {
                    override fun onRegistrationSuccess(response: ArcXPUser) {
                        listener?.onRegistrationSuccess(response)
                        stream.postValue(response)
                    }

                    override fun onRegistrationError(error: ArcXPError) {
                        listener?.onRegistrationError(error)
                        _error.postValue(error)
                    }
                })
        }
        return stream
    }

    fun logout(listener: ArcXPIdentityListener? = null): LiveData<Either<ArcXPError, Boolean>> {
        val stream = MutableLiveData<Either<ArcXPError, Boolean>>()
        if (mContext.getString(R.string.google_key).isNotEmpty()) {
            val gso =
                GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestId()
                    .requestIdToken(mContext.getString(R.string.google_key))
                    .requestEmail()
                    .build()
            val mGoogleSignInClient = GoogleSignIn.getClient(mContext, gso)

            if (AccessToken.getCurrentAccessToken() != null) {
                LoginManager.getInstance().logOut()
            }

            mGoogleSignInClient?.signOut()
            loginWithGoogleOneTapResultsReceiver = null
            loginWithGoogleResultsReceiver = null

            oneTapClient?.signOut()
                ?.addOnSuccessListener {
                    listener?.onLogoutSuccess()
                }
                ?.addOnFailureListener {
                    listener?.onLogoutError(
                        ArcXPError(
                            ArcXPCommerceSDKErrorType.GOOGLE_LOGIN_ERROR,
                            it.message!!,
                            it
                        )
                    )
                }
        }
        arcIListener = listener
        identityApiManager.logout(object : ArcXPIdentityListener() {
            override fun onLogoutSuccess() {
                rememberUser(false)
                stream.postValue(Success(true))
                listener?.onLogoutSuccess()
            }

            override fun onLogoutError(error: ArcXPError) {
                stream.postValue(Failure(error))
                listener?.onLogoutError(error)
            }
        })
        return stream
    }

    fun removeIdentity(grantType: String, listener: ArcXPIdentityListener) {
        arcIListener = listener
        identityApiManager.removeIdentity(grantType, object : ArcXPIdentityListener() {
            override fun onRemoveIdentitySuccess(response: ArcXPUpdateUserStatus) {
                listener.onRemoveIdentitySuccess(response)
            }

            override fun onRemoveIdentityFailure(error: ArcXPError) {
                listener.onRemoveIdentityFailure(error)
            }
        })
    }

    @Deprecated(
        "Use requestDeleteAccount()",
        ReplaceWith(expression = "requestDeleteAccount(listener)")
    )
    fun deleteUser(listener: ArcXPIdentityListener) {
        arcIListener = listener
        identityApiManager.deleteUser(object : ArcXPIdentityListener() {
            override fun onDeleteUserSuccess() {
                listener.onDeleteUserSuccess()
            }

            override fun onDeleteUserError(error: ArcXPError) {
                listener.onDeleteUserError(error)
            }
        })
    }

    fun requestDeleteAccount(listener: ArcXPIdentityListener) {
        arcIListener = listener
        identityApiManager.deleteUser(object : ArcXPIdentityListener() {
            override fun onDeleteUserSuccess() {
                listener.onDeleteUserSuccess()
            }

            override fun onDeleteUserError(error: ArcXPError) {
                listener.onDeleteUserError(error)
            }
        })
    }

    @Deprecated(
        "Use approveDeleteAccount()",
        ReplaceWith(expression = "approveDeleteAccount(nonce, listener)")
    )
    fun approveDeletion(nonce: String, listener: ArcXPIdentityListener) {
        arcIListener = listener
        identityApiManager.approveDeletion(nonce, object : ArcXPIdentityListener() {
            override fun onApproveDeletionSuccess(respone: ArcXPDeleteUser) {
                listener.onApproveDeletionSuccess(respone)
            }

            override fun onApproveDeletionError(error: ArcXPError) {
                listener.onApproveDeletionError(error)
            }
        })
    }

    fun approveDeleteAccount(nonce: String, listener: ArcXPIdentityListener) {
        arcIListener = listener
        identityApiManager.approveDeletion(nonce, object : ArcXPIdentityListener() {
            override fun onApproveDeletionSuccess(respone: ArcXPDeleteUser) {
                listener.onApproveDeletionSuccess(respone)
            }

            override fun onApproveDeletionError(error: ArcXPError) {
                listener.onApproveDeletionError(error)
            }
        })
    }

    fun validateSession(token: String, listener: ArcXPIdentityListener) {
        arcIListener = listener
        identityApiManager.validateJwt(token)
    }

    fun validateSession(listener: ArcXPIdentityListener) {
        arcIListener = listener
        identityApiManager.validateJwt(object : ArcXPIdentityListener() {
            override fun onValidateSessionSuccess() {
                listener.onValidateSessionSuccess()
            }

            override fun onValidateSessionError(error: ArcXPError) {
                listener.onValidateSessionError(error)
            }
        })
    }

    fun refreshSession(token: String, listener: ArcXPIdentityListener) {
        arcIListener = listener
        identityApiManager.refreshToken(
            token,
            ArcXPAuthRequest.Companion.GrantType.REFRESH_TOKEN.value
        )
    }

    fun refreshSession(listener: ArcXPIdentityListener) {
        arcIListener = listener
        identityApiManager.refreshToken(
            authManager.refreshToken,
            ArcXPAuthRequest.Companion.GrantType.REFRESH_TOKEN.value
        )
    }

    fun getAllSubscriptions(listener: ArcXPSalesListener) {
        arcxpSListener = listener
        salesApiManager.getAllSubscriptions(object : ArcXPSalesListener() {
            override fun onGetAllSubscriptionsSuccess(response: ArcXPSubscriptions) {
                listener.onGetAllSubscriptionsSuccess(response)
            }

            override fun onGetSubscriptionsFailure(error: ArcXPError) {
                listener.onGetSubscriptionsFailure(error)
            }
        })
    }

    fun getAllActiveSubscriptions(listener: ArcXPSalesListener) {
        arcxpSListener = listener
        salesApiManager.getAllActiveSubscriptions(object : ArcXPSalesListener() {
            override fun onGetAllActiveSubscriptionsSuccess(response: ArcXPSubscriptions) {
                listener.onGetAllActiveSubscriptionsSuccess(response)
            }

            override fun onGetSubscriptionsFailure(error: ArcXPError) {
                listener.onGetSubscriptionsFailure(error)
            }
        })
    }

    fun getActivePaywallRules(listener: ArcXPRetailListener) {
        arcxpRListener = listener
        retailApiManager.getActivePaywallRules(object : ArcXPRetailListener() {
            override fun onGetActivePaywallRulesSuccess(responseArcxp: ArcXPActivePaywallRules) {
                listener.onGetActivePaywallRulesSuccess(responseArcxp)
            }

            override fun onGetActivePaywallRulesFailure(error: ArcXPError) {
                listener.onGetActivePaywallRulesFailure(error)
            }
        })
    }

    fun getEntitlements(listener: ArcXPSalesListener) {
        arcxpSListener = listener
        salesApiManager.getEntitlements(object : ArcXPSalesListener() {
            override fun onGetEntitlementsSuccess(response: ArcXPEntitlements) {
                listener.onGetEntitlementsSuccess(response)
            }

            override fun onGetEntitlementsFailure(error: ArcXPError) {
                listener.onGetEntitlementsFailure(error)
            }
        })
    }

    fun evaluatePage(
        pageId: String,
        contentType: String?,
        contentSection: String?,
        deviceClass: String?,
        otherConditions: HashMap<String, String>?,
        entitlements: ArcXPEntitlements? = null,
        listener: ArcXPPageviewListener
    ) {
        val conditions = hashMapOf<String, String>()
        if (contentType != null) {
            conditions["contentType"] = contentType
        }
        if (contentSection != null) {
            conditions["contentSection"] = contentSection
        }
        if (deviceClass != null) {
            conditions["deviceClass"] = deviceClass
        }
        otherConditions?.forEach {
            conditions[it.key] = it.value
        }
        val pageviewData = ArcXPPageviewData(pageId, conditions)
        evaluatePage(pageviewData, entitlements, null, listener)
    }

    fun evaluatePage(
        pageId: String,
        contentType: String?,
        contentSection: String?,
        deviceClass: String?,
        otherConditions: HashMap<String, String>?,
        entitlements: ArcXPEntitlements? = null
    ) : LiveData<ArcXPPageviewEvaluationResult> {
        val stream = MutableLiveData<ArcXPPageviewEvaluationResult>()
        val conditions = hashMapOf<String, String>()
        if (contentType != null) {
            conditions["contentType"] = contentType
        }
        if (contentSection != null) {
            conditions["contentSection"] = contentSection
        }
        if (deviceClass != null) {
            conditions["deviceClass"] = deviceClass
        }
        otherConditions?.forEach {
            conditions[it.key] = it.value
        }
        val pageviewData = ArcXPPageviewData(pageId, conditions)
        evaluatePage(pageviewData, entitlements, null, object: ArcXPPageviewListener() {
            override fun onEvaluationResult(response: ArcXPPageviewEvaluationResult) {
                stream.postValue(response)
            }
        })
        return stream
    }

    fun evaluatePage(
        pageId: String,
        contentType: String?,
        contentSection: String?,
        deviceClass: String?,
        otherConditions: HashMap<String, String>?,
        entitlements: ArcXPEntitlements? = null,
        currentTime: Long? = System.currentTimeMillis(),
        listener: ArcXPPageviewListener
    ) {
        val conditions = hashMapOf<String, String>()
        if (contentType != null) {
            conditions["contentType"] = contentType
        }
        if (contentSection != null) {
            conditions["contentSection"] = contentSection
        }
        if (deviceClass != null) {
            conditions["deviceClass"] = deviceClass
        }
        otherConditions?.forEach {
            conditions[it.key] = it.value
        }
        val pageviewData = ArcXPPageviewData(pageId, conditions)
        evaluatePage(pageviewData, entitlements, currentTime, listener)
    }

    fun evaluatePage(
        pageviewData: ArcXPPageviewData,
        entitlements: ArcXPEntitlements? = null,
        currentTime: Long? = System.currentTimeMillis(),
        listener: ArcXPPageviewListener
    ) {
        paywallManager.initialize(entitlements,
            currentTime,
            sessionIsActive(),
            object : ArcXPPageviewListener() {
                override fun onInitializationResult(success: Boolean) {
                    if (success) {
                        val show = paywallManager.evaluate(pageviewData)
                        listener.onEvaluationResult(
                            ArcXPPageviewEvaluationResult(
                                pageId = pageviewData.pageId,
                                show = show.show,
                                campaign = show.campaign
                            )
                        )
                    } else {
                        listener.onEvaluationResult(
                            ArcXPPageviewEvaluationResult(
                                pageId = pageviewData.pageId,
                                show = false
                            )
                        )
                    }
                }
            }
        )
    }

    fun evaluatePage(
        pageviewData: ArcXPPageviewData,
        listener: ArcXPPageviewListener
    ) {
        paywallManager.initialize(null, null, sessionIsActive(), object : ArcXPPageviewListener() {
            override fun onInitializationResult(success: Boolean) {
                if (success) {
                    val show = paywallManager.evaluate(pageviewData)
                    listener.onEvaluationResult(
                        ArcXPPageviewEvaluationResult(
                            pageId = pageviewData.pageId,
                            show = show.show,
                            campaign = show.campaign
                        )
                    )
                } else {
                    listener.onEvaluationResult(
                        ArcXPPageviewEvaluationResult(
                            pageId = pageviewData.pageId,
                            show = false
                        )
                    )
                }
            }
        }
        )
    }

    fun getPaywallCache(): String? {
        return paywallManager.getPaywallCache()
    }

    fun clearPaywallCache() {
        paywallManager.clearPaywallCache()
    }

    fun getConfig(): ArcXPConfig? {
        return AuthManager.getInstance().getConfig()
    }

    fun getRefreshToken(): String? {
        return authManager.refreshToken
    }

    fun getAccessToken(): String? {
        return authManager.accessToken
    }

    fun isLoggedIn(listener: ArcXPIdentityListener? = null): LiveData<Boolean> {
        val stream = MutableLiveData<Boolean>()
        identityApiManager.validateJwt(object : ArcXPIdentityListener(){
            override fun onValidateSessionSuccess() {
                listener?.onValidateSessionSuccess()
                stream.postValue(true)
            }

            override fun onValidateSessionError(error: ArcXPError) {
                listener?.onValidateSessionError(error)
                stream.postValue(false)
            }
        })
        return stream
    }

    fun setRecaptchaToken(token: String) {
        identityApiManager.setRecaptchaToken(token)
    }

    fun getRecaptchaToken(): String? {
        return identityApiManager.getRecaptchaToken()
    }

    fun thirdPartyLogin(
        token: String,
        type: ArcXPAuthRequest.Companion.GrantType,
        listener: ArcXPIdentityListener
    ) {

        arcIListener = listener
        identityApiManager.thirdPartyLogin(token, type, object : ArcXPIdentityListener() {
            override fun onLoginSuccess(response: ArcXPAuth) {
                listener.onLoginSuccess(response)
            }

            override fun onLoginError(error: ArcXPError) {
                listener.onLoginError(error)
            }
        })
    }

    fun sendVerificationEmail(email: String, listener: ArcXPIdentityListener) {
        arcIdentityListener = listener
        identityApiManager.sendVerificationEmail(email, object : ArcXPIdentityListener() {
            override fun onEmailVerificationSentSuccess(it: ArcXPEmailVerification) {
                listener.onEmailVerificationSentSuccess(it)
            }
        })
    }

    fun sessionIsActive(): Boolean {
        return authManager.uuid != null
    }

    fun setAccessToken(token: String) {
        authManager.accessToken = token
    }

    fun verifyEmail(nonce: String, listener: ArcXPIdentityListener) {
        arcIdentityListener = listener
        identityApiManager.verifyEmail(nonce, object : ArcXPIdentityListener() {
            override fun onEmailVerifiedSuccess(response: ArcXPEmailVerification) {
                listener.onEmailVerifiedSuccess(response)
            }

            override fun onEmailVerifiedError(error: ArcXPError) {
                listener.onEmailVerifiedError(error)
            }
        })
    }

    fun runRecaptcha(listener: ArcXPIdentityListener) {
        arcIListener = listener
        if (commerceConfig.recaptchaSiteKey.isNullOrEmpty()) {
            arcIdentityListener.onRecaptchaFailure(
                ArcXPError(
                    ArcXPCommerceSDKErrorType.RECAPTCHA_ERROR,
                    "ArcCommerceConfig.recaptchaSiteKey is null or blank",
                    null
                )
            )
        } else {
            identityApiManager.checkRecaptcha(commerceConfig)
        }
    }

    fun rememberUser(remember: Boolean) {
        identityApiManager.rememberUser(remember)
    }

    fun loginWithApple(
        button: SignInWithAppleButton,
        fragmentManager: FragmentManager,
        listenerArc: ArcXPIdentityListener
    ) {
        arcIListener = listenerArc
        identityApiManager.appleAuthUrl(object : ArcXPIdentityListener() {
            override fun onAppleAuthUrlObtained(url: String) {
                setUpAppleLogin(url, button, fragmentManager, object : ArcXPIdentityListener() {
                    override fun onAppleLoginSuccess(result: SignInWithAppleResult) {
                        thirdPartyLogin(
                            (result as SignInWithAppleResult.Success).authorizationCode,
                            ArcXPAuthRequest.Companion.GrantType.APPLE,
                            object : ArcXPIdentityListener() {
                                override fun onLoginSuccess(response: ArcXPAuth) {
                                    listenerArc.onLoginSuccess(response)
                                }

                                override fun onLoginError(error: ArcXPError) {
                                    listenerArc.onLoginError(error)
                                }
                            })
                    }

                    override fun onAppleLoginFailure(error: ArcXPError) {
                        listenerArc.onLoginError(error)
                    }
                })
            }

            override fun onAppleLoginFailure(error: ArcXPError) {
                arcIdentityListener.onLoginError(error)
            }
        })
    }

    /**
     * For internal use only. Not meant for public
     * @suppress
     */
    fun loginWithAppleUpdatedURL(
        button: SignInWithAppleButton,
        fragmentManager: FragmentManager,
        listenerArc: ArcXPIdentityListener
    ) {
        arcIListener = listenerArc
        identityApiManager.appleAuthUrlUpdatedURL(object : ArcXPIdentityListener() {
            override fun onAppleAuthUrlObtained(url: String) {
                setUpAppleLogin(url, button, fragmentManager, object : ArcXPIdentityListener() {
                    override fun onAppleLoginSuccess(result: SignInWithAppleResult) {
                        thirdPartyLogin(
                            (result as SignInWithAppleResult.Success).authorizationCode,
                            ArcXPAuthRequest.Companion.GrantType.APPLE,
                            object : ArcXPIdentityListener() {
                                override fun onLoginSuccess(response: ArcXPAuth) {
                                    listenerArc.onLoginSuccess(response)
                                }

                                override fun onLoginError(error: ArcXPError) {
                                    listenerArc.onLoginError(error)
                                }
                            })
                    }

                    override fun onAppleLoginFailure(error: ArcXPError) {
                        listenerArc.onLoginError(error)
                    }
                })
            }

            override fun onAppleLoginFailure(error: ArcXPError) {
                arcIdentityListener.onLoginError(error)
            }
        })
    }

    private fun setUpAppleLogin(
        url: String,
        button: SignInWithAppleButton,
        fragmentManager: FragmentManager,
        listener: ArcXPIdentityListener
    ) {

        val configuration = SignInWithAppleConfiguration(
            arcAuthUrl = url,
            clientId = mContext.getString(R.string.apple_clientID),
            redirectUri = mContext.getString(R.string.apple_redirectUri),
            authTokenUrl = mContext.getString(R.string.apple_auth_token_url),
            scope = mContext.getString(R.string.apple_scope)
        )

        var config = mapOf(
            "arcAuth" to configuration.arcAuthUrl,
            "clientID" to configuration.clientId,
            "redirect" to configuration.redirectUri,
            "authToken" to configuration.authTokenUrl,
            "scope" to configuration.scope
        )

        try {
            validateAppleConfig(config)
        } catch (e: Exception) {
            listener.onAppleLoginFailure(
                ArcXPError(
                    ArcXPCommerceSDKErrorType.APPLE_CONFIG_ERROR,
                    e.localizedMessage
                )
            )
        }

        button.performClick()
        fragmentManager?.let {
            button.setUpSignInWithAppleOnClick(
                it,
                configuration
            ) { result ->
                when (result) {
                    is SignInWithAppleResult.Success -> {
                        // Handle success
                        listener.onAppleLoginSuccess(result)
                    }
                    is SignInWithAppleResult.Failure -> {
                        // Handle failure
                        listener.onAppleLoginFailure(
                            ArcXPError(
                                ArcXPCommerceSDKErrorType.APPLE_LOGIN_ERROR,
                                result.error.toString()
                            )
                        )
                    }
                    is SignInWithAppleResult.Cancel -> {
                        // Handle user cancel
                        listener.onAppleLoginFailure(
                            ArcXPError(
                                ArcXPCommerceSDKErrorType.APPLE_LOGIN_CANCEL,
                                ": User Cancelled"
                            )
                        )
                    }
                }
            }
        }
    }

    private fun validateAppleConfig(config: Map<String, String>) {
        for (data in config) {
            when (data.key) {
                "scope" -> if (data.value == "") throw ArcXPError("Missing scope") else if (!data.value.contains(
                        "email",
                        ignoreCase = true
                    ) &&
                    !data.value.contains("name", ignoreCase = true)
                ) {
                    throw ArcXPError("Scope must contain either email or name")
                }
                "arcAuth" -> if (data.value == "") throw ArcXPError("Missing arcAuthUrl") else if (!data.value.contains(
                        Regex("https")
                    )
                ) {
                    throw ArcXPError("Invalid Auth URL")
                }
                "clientID" -> if (data.value == "") throw ArcXPError("Missing clientId") else if (!data.value.matches(
                        Regex("com(.+)\$")
                    )
                ) {
                    throw ArcXPError("Incorrect format for clientId")
                }
                "redirect" -> if (data.value == "") throw ArcXPError("Missing redirectUri") else if (!data.value.contains(
                        Regex("https")
                    )
                ) {
                    throw ArcXPError("Invalid Redirect URL")
                }
                "authToken" -> if (data.value == "") throw ArcXPError("Missing authTokenUrl") else if (!data.value.contains(
                        Regex("https")
                    )
                ) {
                    throw ArcXPError("Invalid AuthToken URL")
                }
            }
        }
    }

    fun loginWithFacebook(
        fbLoginButton: LoginButton,
        listener: ArcXPIdentityListener? = null
    ): LiveData<ArcXPAuth> {
        arcIListener = listener
        val stream = MutableLiveData<ArcXPAuth>()
        fbLoginButton.registerCallback(callbackManager, object :
            FacebookCallback<LoginResult?> {
            override fun onSuccess(result: LoginResult?) {
                val accessToken = AccessToken.getCurrentAccessToken()
                if (accessToken != null) {
                    thirdPartyLogin(
                        accessToken.token,
                        ArcXPAuthRequest.Companion.GrantType.FACEBOOK,
                        object : ArcXPIdentityListener() {
                            override fun onLoginSuccess(response: ArcXPAuth) {
                                listener?.onLoginSuccess(response)
                                stream.postValue(response)
                            }

                            override fun onLoginError(error: ArcXPError) {
                                listener?.onLoginError(error)
                                _error.postValue(error)
                            }
                        })
                }
            }

            override fun onCancel() {
                val error = ArcXPError(
                    ArcXPCommerceSDKErrorType.FACEBOOK_LOGIN_CANCEL,
                    "User cancelled login"
                )
                listener?.onLoginError(error)
                _error.postValue(error)
            }

            override fun onError(error: FacebookException) {
                error.message?.let {
                    ArcXPError(
                        ArcXPCommerceSDKErrorType.FACEBOOK_LOGIN_ERROR,
                        it
                    )
                }?.let {
                    listener?.onLoginError(it)
                    _error.postValue(it)
                }
            }
        })
        return stream
    }

    fun loginWithGoogle(
        activity: AppCompatActivity,
        listener: ArcXPIdentityListener? = null
    ): LiveData<ArcXPAuth> {
        arcIListener = listener
        val stream = MutableLiveData<ArcXPAuth>()
        val gso =
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestId()
                .requestIdToken(mContext.getString(R.string.google_key))
                .requestEmail()
                .build()
        val mGoogleSignInClient = GoogleSignIn.getClient(mContext, gso)

        val signInIntent: Intent = mGoogleSignInClient.signInIntent

        if (loginWithGoogleResultsReceiver == null) {
            loginWithGoogleResultsReceiver = LoginWithGoogleResultsReceiver(signInIntent, this,
                object : ArcXPIdentityListener() {
                    override fun onLoginSuccess(response: ArcXPAuth) {
                        activity.supportFragmentManager.beginTransaction()
                            .remove(loginWithGoogleResultsReceiver as Fragment).commit()
                        listener?.onLoginSuccess(response)
                        stream.postValue(response)
                    }

                    override fun onLoginError(error: ArcXPError) {
                        activity.supportFragmentManager.beginTransaction()
                            .remove(loginWithGoogleResultsReceiver as Fragment).commit()
                        listener?.onLoginError(error)
                        _error.postValue(error)
                        loginWithGoogleResultsReceiver = null
                    }
                })
        }

        activity.supportFragmentManager.beginTransaction()
            .add(loginWithGoogleResultsReceiver!!, "LoginWithGoogle").commit()

        return stream
    }

    fun logoutOfGoogle(listener: ArcXPIdentityListener) {
        if (mContext.getString(R.string.google_key).isNotEmpty()) {
            val gso =
                GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestId()
                    .requestIdToken(mContext.getString(R.string.google_key))
                    .requestEmail()
                    .build()
            val mGoogleSignInClient = GoogleSignIn.getClient(mContext, gso)

            mGoogleSignInClient?.signOut()

            oneTapClient?.signOut()
                ?.addOnSuccessListener {
                    listener.onLogoutSuccess()
                }
                ?.addOnFailureListener {
                    listener.onLogoutError(
                        ArcXPError(
                            ArcXPCommerceSDKErrorType.GOOGLE_LOGIN_ERROR,
                            it.message!!,
                            it
                        )
                    )
                }
        }
    }

    fun loginWithGoogleOneTap(activity: AppCompatActivity, listener: ArcXPIdentityListener) {

        if (commerceConfig.googleOneTapEnabled) {
            oneTapClient = Identity.getSignInClient(mContext)
            signInRequest = BeginSignInRequest.builder()
                .setPasswordRequestOptions(
                    BeginSignInRequest.PasswordRequestOptions.builder()
                        .setSupported(true)
                        .build()
                )
                .setGoogleIdTokenRequestOptions(
                    BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                        .setSupported(true)
                        .setServerClientId(activity.getString(R.string.google_key))
                        .setFilterByAuthorizedAccounts(true)
                        .build()
                )
                .setAutoSelectEnabled(false)
                .build()

            arcIListener = listener
            oneTapClient?.let {
                oneTapClient?.beginSignIn(signInRequest)
                    ?.addOnSuccessListener(activity) { result ->
                        val request =
                            IntentSenderRequest.Builder(result.pendingIntent.intentSender).build()
                        if (loginWithGoogleOneTapResultsReceiver == null) {
                            loginWithGoogleOneTapResultsReceiver =
                                LoginWithGoogleOneTapResultsReceiver(
                                    request, this,
                                    object : ArcXPIdentityListener() {
                                        override fun onGoogleOneTapLoginSuccess(
                                            username: String?,
                                            password: String?,
                                            token: String?
                                        ) {
                                            activity.supportFragmentManager.beginTransaction()
                                                .remove(loginWithGoogleOneTapResultsReceiver as Fragment)
                                                .commit()
                                            listener.onGoogleOneTapLoginSuccess(
                                                username,
                                                password,
                                                token
                                            )
                                        }

                                        override fun onLoginError(error: ArcXPError) {
                                            activity.supportFragmentManager.beginTransaction()
                                                .remove(loginWithGoogleOneTapResultsReceiver as Fragment)
                                                .commit()
                                            listener.onLoginError(error)
                                        }
                                    })
                        }
                        activity.supportFragmentManager.beginTransaction()
                            .add(loginWithGoogleOneTapResultsReceiver!!, "LoginWithGoogle").commit()
                    }
                    ?.addOnFailureListener(activity) { e ->
                        listener.onLoginError(
                            ArcXPError(
                                ArcXPCommerceSDKErrorType.GOOGLE_LOGIN_ERROR_NO_ACCOUNT,
                                e.localizedMessage, e
                            )
                        )
                    }
            } ?: run {
                listener.onLoginError(
                    ArcXPError(
                        ArcXPCommerceSDKErrorType.GOOGLE_LOGIN_ERROR,
                        "Enable One-Tap Login in ArcxpCommerceConfig before attempting login."
                    )
                )
            }
        }
    }

    private fun handleSignInResult(
        completedTask: Task<GoogleSignInAccount>,
        listener: ArcXPIdentityListener
    ) {
        try {
            val account =
                completedTask.getResult(ApiException::class.java)
            account?.idToken?.let { it ->
                thirdPartyLogin(it,
                    ArcXPAuthRequest.Companion.GrantType.GOOGLE,
                    object : ArcXPIdentityListener() {
                        override fun onLoginSuccess(response: ArcXPAuth) {
                            listener.onLoginSuccess(response)
                        }

                        override fun onLoginError(error: ArcXPError) {
                            logoutOfGoogle(listener)
                            listener.onLoginError(error)
                        }
                    })
            }
            // Signed in successfully, show authenticated UI.
        } catch (e: ApiException) {
            arcIListener?.onLoginError(
                ArcXPError(
                    ArcXPCommerceSDKErrorType.GOOGLE_LOGIN_ERROR,
                    e.message!!, e
                )
            )
        }
    }

    fun onActivityResults(
        requestCode: Int,
        resultCode: Int,
        data: Intent?,
        listener: ArcXPIdentityListener
    ) {
        callbackManager.onActivityResult(requestCode, resultCode, data)
    }

    class LoginWithGoogleOneTapResultsReceiver(
        private val signInIntent: IntentSenderRequest,
        val manager: ArcXPCommerceManager,
        val listener: ArcXPIdentityListener
    ) : Fragment() {
        private val operation = registerForActivityResult(
            ActivityResultContracts.StartIntentSenderForResult(),
            ActivityResultCallback { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    try {
                        val credential =
                            manager.oneTapClient?.getSignInCredentialFromIntent(result.data)
                        val idToken = credential?.googleIdToken
                        val username = credential?.id
                        val password = credential?.password
                        when {
                            idToken != null -> {
                                if (manager.commerceConfig.googleOneTapAutoLoginEnabled) {
                                    manager.thirdPartyLogin(
                                        idToken,
                                        ArcXPAuthRequest.Companion.GrantType.GOOGLE,
                                        object : ArcXPIdentityListener() {
                                            override fun onLoginSuccess(response: ArcXPAuth) {
                                                listener.onGoogleOneTapLoginSuccess(
                                                    username,
                                                    password,
                                                    idToken
                                                )
                                            }

                                            override fun onLoginError(error: ArcXPError) {
                                                listener.onLoginError(error)
                                            }
                                        }
                                    )
                                } else {
                                    manager.arcIListener?.onGoogleOneTapLoginSuccess(
                                        username,
                                        password,
                                        idToken
                                    )
                                }
                            }
                            password != null -> {
                                if (manager.commerceConfig.googleOneTapAutoLoginEnabled) {
                                    manager.login(username!!,
                                        password!!,
                                        object : ArcXPIdentityListener() {
                                            override fun onLoginSuccess(response: ArcXPAuth) {
                                                manager.arcIListener?.onGoogleOneTapLoginSuccess(
                                                    username,
                                                    password,
                                                    idToken
                                                )
                                            }

                                            override fun onLoginError(error: ArcXPError) {
                                                manager.arcIListener?.onLoginError(error)
                                            }
                                        })
                                } else {
                                    manager.arcIListener?.onGoogleOneTapLoginSuccess(
                                        username,
                                        password,
                                        idToken
                                    )
                                }
                            }
                            else -> {
                                manager.arcIListener?.onLoginError(
                                    ArcXPError(
                                        ArcXPCommerceSDKErrorType.GOOGLE_LOGIN_ERROR,
                                        "Google One Tap login error - no ID token or password."
                                    )
                                )
                                listener.onLoginError(
                                    ArcXPError(
                                        ArcXPCommerceSDKErrorType.GOOGLE_LOGIN_ERROR,
                                        "Google One Tap login error - no ID token or password."
                                    )
                                )
                            }
                        }
                    } catch (e: ApiException) {
                        when (e.statusCode) {
                            CommonStatusCodes.CANCELED -> {
                                manager.arcIListener?.onLoginError(
                                    ArcXPError(
                                        ArcXPCommerceSDKErrorType.GOOGLE_LOGIN_CANCEL,
                                        e.localizedMessage, e
                                    )
                                )
                                listener.onLoginError(
                                    ArcXPError(
                                        ArcXPCommerceSDKErrorType.GOOGLE_LOGIN_CANCEL,
                                        e.localizedMessage, e
                                    )
                                )
                            }
                            else -> {
                                manager.arcIListener?.onLoginError(
                                    ArcXPError(
                                        ArcXPCommerceSDKErrorType.GOOGLE_LOGIN_ERROR,
                                        e.localizedMessage, e
                                    )
                                )
                                listener.onLoginError(
                                    ArcXPError(
                                        ArcXPCommerceSDKErrorType.GOOGLE_LOGIN_ERROR,
                                        e.localizedMessage, e
                                    )
                                )
                            }
                        }
                    }

                }

            })

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            operation.launch(signInIntent)
        }
    }

    class LoginWithGoogleResultsReceiver(
        val signInIntent: Intent,
        val manager: ArcXPCommerceManager,
        val listener: ArcXPIdentityListener
    ) : Fragment() {
        private val operation = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult(),
            ActivityResultCallback { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val task =
                        GoogleSignIn.getSignedInAccountFromIntent(result.data)
                    if ((task.exception as? ApiException)?.statusCode == GoogleSignInStatusCodes.SIGN_IN_CANCELLED) {
                        listener.onLoginError(
                            ArcXPError(
                                GoogleSignInStatusCodes.getStatusCodeString(
                                    (task.exception as ApiException).statusCode
                                )
                            )
                        )
                    } else {
                        manager.handleSignInResult(task, object : ArcXPIdentityListener() {
                            override fun onLoginSuccess(response: ArcXPAuth) {
                                listener.onLoginSuccess(response)
                            }

                            override fun onLoginError(error: ArcXPError) {
                                listener.onLoginError(error)
                            }
                        })
                    }

                } else if (result.resultCode == Activity.RESULT_CANCELED){
                    listener.onLoginError(ArcXPError(getString(R.string.canceled)))
                }

            })

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            operation.launch(signInIntent)
        }
    }

    fun initializePaymentMethod(id: String, pid: String, listener: ArcXPSalesListener?) {
        arcxpSListener = listener
        salesApiManager.initializePaymentMethod(id, pid, listener)
    }

    fun finalizePaymentMethod(
        id: String,
        pid: String,
        request: ArcXPFinalizePaymentRequest,
        listener: ArcXPSalesListener?
    ) {
        arcxpSListener = listener
        salesApiManager.finalizePaymentMethod(id, pid, request, listener)
    }

    fun finalizePaymentMethod(
        id: String,
        pid: String,
        token: String? = null,
        email: String? = null,
        address: ArcXPAddressRequest? = null,
        phone: String? = null,
        browserInfo: String? = null,
        firstName: String? = null,
        lastName: String? = null,
        listener: ArcXPSalesListener?
    ) {
        arcxpSListener = listener
        var request = ArcXPFinalizePaymentRequest(
            token,
            email,
            address,
            phone,
            browserInfo,
            firstName,
            lastName
        )
        salesApiManager.finalizePaymentMethod(id, pid, request, listener)
    }

    fun finalizePaymentMethod(
        id: String,
        pid: String,
        token: String? = null,
        email: String? = null,
        addressLine1: String,
        addressLine2: String? = null,
        addressLocality: String,
        addressRegion: String? = null,
        addressPostal: String? = null,
        addressCountry: String,
        addressType: String,
        phone: String? = null,
        browserInfo: String? = null,
        firstName: String? = null,
        lastName: String? = null,
        listener: ArcXPSalesListener?
    ) {
        arcxpSListener = listener
        val aRequest = ArcXPAddressRequest(
            addressLine1, addressLine2, addressLocality, addressRegion,
            addressPostal, addressCountry, addressType
        )
        var request = ArcXPFinalizePaymentRequest(
            token,
            email,
            aRequest,
            phone,
            browserInfo,
            firstName,
            lastName
        )
        salesApiManager.finalizePaymentMethod(id, pid, request, listener)
    }

    fun finalizePaymentMethod3ds(
        id: String,
        pid: String,
        request: ArcXPFinalizePaymentRequest,
        listener: ArcXPSalesListener?
    ) {
        salesApiManager.finalizePaymentMethod3ds(id, pid, request, listener)
    }

    fun finalizePaymentMethod3ds(
        id: String,
        pid: String,
        token: String? = null,
        email: String? = null,
        address: ArcXPAddressRequest? = null,
        phone: String? = null,
        browserInfo: String? = null,
        firstName: String? = null,
        lastName: String? = null,
        listener: ArcXPSalesListener?
    ) {
        arcxpSListener = listener
        var request = ArcXPFinalizePaymentRequest(
            token,
            email,
            address,
            phone,
            browserInfo,
            firstName,
            lastName
        )
        salesApiManager.finalizePaymentMethod3ds(id, pid, request, listener)
    }

    fun finalizePaymentMethod3ds(
        id: String,
        pid: String,
        token: String? = null,
        email: String? = null,
        addressLine1: String,
        addressLine2: String? = null,
        addressLocality: String,
        addressRegion: String? = null,
        addressPostal: String? = null,
        addressCountry: String,
        addressType: String,
        phone: String? = null,
        browserInfo: String? = null,
        firstName: String? = null,
        lastName: String? = null,
        listener: ArcXPSalesListener?
    ) {
        arcxpSListener = listener
        val aRequest = ArcXPAddressRequest(
            addressLine1, addressLine2, addressLocality, addressRegion,
            addressPostal, addressCountry, addressType
        )
        var request = ArcXPFinalizePaymentRequest(
            token,
            email,
            aRequest,
            phone,
            browserInfo,
            firstName,
            lastName
        )
        salesApiManager.finalizePaymentMethod3ds(id, pid, request, listener)
    }

    fun cancelSubscription(
        id: String,
        request: ArcXPCancelSubscriptionRequest,
        listener: ArcXPSalesListener?
    ) {
        arcxpSListener = listener
        salesApiManager.cancelSubscription(id, request, listener)
    }

    fun cancelSubscription(id: String, reason: String, listener: ArcXPSalesListener?) {
        arcxpSListener = listener
        val request = ArcXPCancelSubscriptionRequest(reason)
        salesApiManager.cancelSubscription(id, request, listener)
    }

    fun updateAddress(request: ArcXPUpdateAddressRequest, listener: ArcXPSalesListener?) {
        arcxpSListener = listener
        salesApiManager.updateAddress(request, listener)
    }

    fun updateAddress(
        subscriptionID: Int?,
        billingAddress: ArcXPAddressRequest?,
        listener: ArcXPSalesListener?
    ) {
        arcxpSListener = listener
        val request = ArcXPUpdateAddressRequest(subscriptionID, billingAddress)
        salesApiManager.updateAddress(request, listener)
    }

    fun updateAddress(
        subscriptionID: Int?,
        addressLine1: String,
        addressLine2: String? = null,
        addressLocality: String,
        addressRegion: String? = null,
        addressPostal: String? = null,
        addressCountry: String,
        addressType: String,
        listener: ArcXPSalesListener?
    ) {
        arcxpSListener = listener
        val aRequest = ArcXPAddressRequest(
            addressLine1, addressLine2, addressLocality, addressRegion,
            addressPostal, addressCountry, addressType
        )
        val request = ArcXPUpdateAddressRequest(subscriptionID, aRequest)
        salesApiManager.updateAddress(request, listener)
    }

    fun getSubscriptionDetails(id: String, listener: ArcXPSalesListener?) {
        arcxpSListener = listener
        salesApiManager.getSubscriptionDetails(id, listener)
    }

    fun createCustomerOrder(
        email: String?,
        phone: String?,
        shippingAddress: ArcXPAddressRequest?,
        billingAddress: ArcXPAddressRequest?,
        firstName: String?,
        lastName: String?,
        secondLastName: String?,
        listener: ArcXPSalesListener?
    ) {
        arcxpSListener = listener
        val request = ArcXPCustomerOrderRequest(
            email,
            phone,
            shippingAddress,
            billingAddress,
            firstName,
            lastName,
            secondLastName
        )
        salesApiManager.createCustomerOrder(request, listener)
    }

    fun createCustomerOrder(
        email: String?,
        phone: String?,
        shippingAddressLine1: String,
        shippingAddressLine2: String? = null,
        shippingAddressLocality: String,
        shippingAddressRegion: String? = null,
        shippingAddressPostal: String? = null,
        shippingAddressCountry: String,
        shippingAddressType: String,
        billingAddressLine1: String,
        billingAddressLine2: String? = null,
        billingAddressLocality: String,
        billingAddressRegion: String? = null,
        billingAddressPostal: String? = null,
        billingAddressCountry: String,
        billingAddressType: String,
        firstName: String?,
        lastName: String?,
        secondLastName: String?,
        listener: ArcXPSalesListener?
    ) {
        arcxpSListener = listener
        val aRequest = ArcXPAddressRequest(
            shippingAddressLine1, shippingAddressLine2,
            shippingAddressLocality, shippingAddressRegion,
            shippingAddressPostal, shippingAddressCountry, shippingAddressType
        )
        val bRequest = ArcXPAddressRequest(
            billingAddressLine1, billingAddressLine2,
            billingAddressLocality, billingAddressRegion,
            billingAddressPostal, billingAddressCountry, billingAddressType
        )
        val request = ArcXPCustomerOrderRequest(
            email,
            phone,
            aRequest,
            bRequest,
            firstName,
            lastName,
            secondLastName
        )
        salesApiManager.createCustomerOrder(request, listener)
    }

    fun getPaymentOptions(listener: ArcXPSalesListener?) {
        arcxpSListener = listener
        salesApiManager.getPaymentOptions(listener)
    }

    fun getPaymentAddresses(listener: ArcXPSalesListener?) {
        arcxpSListener = listener
        salesApiManager.getPaymentAddresses(listener)
    }

    fun initializePayment(orderNumber: String, mid: String, listener: ArcXPSalesListener?) {
        arcxpSListener = listener
        salesApiManager.initializePayment(orderNumber, mid, listener)
    }

    fun finalizePayment(
        orderNumber: String,
        mid: String,
        request: ArcXPFinalizePaymentRequest,
        listener: ArcXPSalesListener?
    ) {
        arcxpSListener = listener
        salesApiManager.finalizePayment(orderNumber, mid, request, listener)
    }

    fun finalizePayment(
        orderNumber: String,
        mid: String,
        token: String?,
        email: String?,
        address: ArcXPAddressRequest?,
        phone: String?,
        browserInfo: String?,
        firstName: String?,
        lastName: String?,
        listener: ArcXPSalesListener?
    ) {
        arcxpSListener = listener
        var request = ArcXPFinalizePaymentRequest(
            token,
            email,
            address,
            phone,
            browserInfo,
            firstName,
            lastName
        )
        salesApiManager.finalizePayment(orderNumber, mid, request, listener)
    }

    fun finalizePayment(
        orderNumber: String,
        mid: String,
        token: String?,
        email: String?,
        addressLine1: String,
        addressLine2: String? = null,
        addressLocality: String,
        addressRegion: String? = null,
        addressPostal: String? = null,
        addressCountry: String,
        addressType: String,
        phone: String?,
        browserInfo: String?,
        firstName: String?,
        lastName: String?,
        listener: ArcXPSalesListener?
    ) {
        arcxpSListener = listener
        val aRequest = ArcXPAddressRequest(
            addressLine1, addressLine2, addressLocality, addressRegion,
            addressPostal, addressCountry, addressType
        )
        var request = ArcXPFinalizePaymentRequest(
            token,
            email,
            aRequest,
            phone,
            browserInfo,
            firstName,
            lastName
        )
        salesApiManager.finalizePayment(orderNumber, mid, request, listener)
    }

    fun finalizePayment3ds(
        orderNumber: String,
        mid: String,
        request: ArcXPFinalizePaymentRequest,
        listener: ArcXPSalesListener?
    ) {
        arcxpSListener = listener
        salesApiManager.finalizePayment3ds(orderNumber, mid, request, listener)
    }

    fun finalizePayment3ds(
        orderNumber: String,
        mid: String,
        token: String?,
        email: String?,
        address: ArcXPAddressRequest?,
        phone: String?,
        browserInfo: String?,
        firstName: String?,
        lastName: String?,
        listener: ArcXPSalesListener?
    ) {
        arcxpSListener = listener
        var request = ArcXPFinalizePaymentRequest(
            token,
            email,
            address,
            phone,
            browserInfo,
            firstName,
            lastName
        )
        salesApiManager.finalizePayment3ds(orderNumber, mid, request, listener)
    }

    fun finalizePayment3ds(
        orderNumber: String,
        mid: String,
        token: String?,
        email: String?,
        addressLine1: String,
        addressLine2: String? = null,
        addressLocality: String,
        addressRegion: String? = null,
        addressPostal: String? = null,
        addressCountry: String,
        addressType: String,
        phone: String?,
        browserInfo: String?,
        firstName: String?,
        lastName: String?,
        listener: ArcXPSalesListener?
    ) {
        arcxpSListener = listener
        val aRequest = ArcXPAddressRequest(
            addressLine1, addressLine2, addressLocality, addressRegion,
            addressPostal, addressCountry, addressType
        )
        var request = ArcXPFinalizePaymentRequest(
            token,
            email,
            aRequest,
            phone,
            browserInfo,
            firstName,
            lastName
        )
        salesApiManager.finalizePayment3ds(orderNumber, mid, request, listener)
    }

    fun getOrderHistory(listener: ArcXPSalesListener?) {
        arcxpSListener = listener
        salesApiManager.getOrderHistory(listener)
    }

    fun getOrderDetails(orderNumber: String, listener: ArcXPSalesListener?) {
        arcxpSListener = listener
        salesApiManager.getOrderDetails(orderNumber, listener)
    }

    fun clearCart(listener: ArcXPSalesListener?) {
        arcxpSListener = listener
        salesApiManager.clearCart(listener)
    }

    fun getCurrentCart(listener: ArcXPSalesListener?) {
        arcxpSListener = listener
        salesApiManager.getCurrentCart(listener)
    }

    fun addItemToCart(request: ArcXPCartItemsRequest, listener: ArcXPSalesListener?) {
        arcxpSListener = listener
        salesApiManager.addItemToCart(request, listener)
    }

    fun addItemToCart(
        items: List<CartItem?>?,
        addressLine1: String,
        addressLine2: String? = null,
        addressLocality: String,
        addressRegion: String? = null,
        addressPostal: String? = null,
        addressCountry: String,
        addressType: String,
        listener: ArcXPSalesListener?
    ) {
        arcxpSListener = listener
        val aRequest = ArcXPAddressRequest(
            addressLine1, addressLine2, addressLocality, addressRegion,
            addressPostal, addressCountry, addressType
        )
        val request = ArcXPCartItemsRequest(items, aRequest)
        salesApiManager.addItemToCart(request, listener)
    }

    fun addItemToCart(
        items: List<CartItem?>?,
        billingAddress: ArcXPAddressRequest?,
        listener: ArcXPSalesListener?
    ) {
        arcxpSListener = listener
        val request = ArcXPCartItemsRequest(items, billingAddress)
        salesApiManager.addItemToCart(request, listener)
    }

    fun removeItemFromCart(sku: String, listener: ArcXPSalesListener?) {
        arcxpSListener = listener
        salesApiManager.removeItemFromCart(sku, listener)
    }

    companion object {

        @Volatile
        private var INSTANCE: ArcXPCommerceManager? = null

        @JvmStatic
        fun initialize(
            context: Context,
            clientCachedData: Map<String, String>,
            config: ArcXPCommerceConfig
        ): ArcXPCommerceManager {

            INSTANCE ?: synchronized(this)
            {
                INSTANCE
                    ?: ArcXPCommerceManager().also {
                        INSTANCE = it
                    }
            }
            INSTANCE?.create(context, clientCachedData, config)

            return INSTANCE!!
        }
    }
}
