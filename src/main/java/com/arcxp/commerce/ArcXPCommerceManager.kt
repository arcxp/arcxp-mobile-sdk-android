package com.arcxp.commerce

import android.app.Application
import android.content.Intent
import android.util.Log
import androidx.annotation.Keep
import androidx.annotation.VisibleForTesting
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.arcxp.commerce.apimanagers.IdentityApiManager
import com.arcxp.commerce.apimanagers.RetailApiManager
import com.arcxp.commerce.apimanagers.SalesApiManager
import com.arcxp.commerce.callbacks.ArcXPIdentityListener
import com.arcxp.commerce.callbacks.ArcXPRetailListener
import com.arcxp.commerce.callbacks.ArcXPSalesListener
import com.arcxp.commerce.extendedModels.ArcXPProfileManage
import com.arcxp.commerce.models.ArcXPAuth
import com.arcxp.commerce.models.ArcXPAuthRequest
import com.arcxp.commerce.models.ArcXPConfig
import com.arcxp.commerce.models.ArcXPEntitlements
import com.arcxp.commerce.models.ArcXPIdentity
import com.arcxp.commerce.models.ArcXPOneTimeAccessLink
import com.arcxp.commerce.models.ArcXPProfilePatchRequest
import com.arcxp.commerce.models.ArcXPUpdateProfileRequest
import com.arcxp.commerce.models.ArcXPUser
import com.arcxp.commerce.paywall.PaywallManager
import com.arcxp.commerce.util.AuthManager
import com.arcxp.commons.throwables.ArcXPException
import com.arcxp.commons.throwables.ArcXPSDKErrorType
import com.arcxp.commons.util.Constants.SDK_TAG
import com.arcxp.commons.util.DependencyFactory
import com.arcxp.commons.util.DependencyFactory.buildIntentSenderRequest
import com.arcxp.commons.util.DependencyFactory.createArcXPException
import com.arcxp.commons.util.DependencyFactory.createGoogleSignInClient
import com.arcxp.commons.util.DependencyFactory.createIdentityApiManager
import com.arcxp.commons.util.DependencyFactory.createLiveData
import com.arcxp.commons.util.DependencyFactory.createLoginWithGoogleOneTapResultsReceiver
import com.arcxp.commons.util.DependencyFactory.createLoginWithGoogleResultsReceiver
import com.arcxp.commons.util.DependencyFactory.createPaywallManager
import com.arcxp.commons.util.DependencyFactory.createRetailApiManager
import com.arcxp.commons.util.DependencyFactory.createSalesApiManager
import com.arcxp.commons.util.DependencyFactory.createUserSettingsManager
import com.arcxp.commons.util.Either
import com.arcxp.commons.util.Failure
import com.arcxp.commons.util.Success
import com.arcxp.identity.UserSettingsManager
import com.arcxp.sdk.R
import com.facebook.AccessToken
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.facebook.login.widget.LoginButton
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import java.util.Calendar

@Keep
class ArcXPCommerceManager {

    private lateinit var mContext: Application

    private lateinit var authManager: AuthManager

    private lateinit var identityApiManager: IdentityApiManager
    private lateinit var salesApiManager: SalesApiManager
    private lateinit var retailApiManager: RetailApiManager

    internal lateinit var commerceConfig: ArcXPCommerceConfig

    private lateinit var paywallManager: PaywallManager

    internal var oneTapClient: SignInClient? = null
    private lateinit var signInRequest: BeginSignInRequest

    lateinit var userSettingsManager: UserSettingsManager

    private var loginWithGoogleResultsReceiver: LoginWithGoogleResultsReceiver? = null
    private var loginWithGoogleOneTapResultsReceiver: LoginWithGoogleOneTapResultsReceiver? = null

    private val _error = MutableLiveData<ArcXPException>()

    val errors: LiveData<ArcXPException>
        get() = _error

    private val callbackManager by lazy {
        DependencyFactory.createCallBackManager()
    }

    @VisibleForTesting
    internal fun reset() {
        loginWithGoogleResultsReceiver = null
        loginWithGoogleOneTapResultsReceiver = null
        oneTapClient = null
    }

    @VisibleForTesting
    internal fun getLoginWithGoogleResultsReceiver() = loginWithGoogleResultsReceiver

    @VisibleForTesting
    internal fun getLoginWithGoogleOneTapResultsReceiver() = loginWithGoogleOneTapResultsReceiver

    @VisibleForTesting
    internal fun getSignInRequest() = signInRequest

    private fun create(
        context: Application,
        clientCachedData: Map<String, String>,
        config: ArcXPCommerceConfig
    ) {

        this.commerceConfig = config

        mContext = context
        authManager = AuthManager.getInstance(context, clientCachedData, config)



        identityApiManager = createIdentityApiManager(authManager)
        salesApiManager = createSalesApiManager()
        retailApiManager = createRetailApiManager()
        userSettingsManager = createUserSettingsManager(identityApiManager = identityApiManager)

        paywallManager = createPaywallManager(
            application = context,
            retailApiManager = retailApiManager,
            salesApiManager = salesApiManager
        )

        if (!commerceConfig.useLocalConfig) {
            identityApiManager.loadConfig(object : ArcXPIdentityListener() {
                override fun onLoadConfigSuccess(result: ArcXPConfig) {
                    AuthManager.getInstance().setConfig(result)
                    Log.i(
                        SDK_TAG,
                        context.getString(
                            R.string.remote_tenet_config_loaded,
                            result.facebookAppId,
                            result.googleClientId
                        )
                    )
                }

                override fun onLoadConfigFailure(error: ArcXPException) {
                    AuthManager.getInstance().loadLocalConfig(config)
                    Log.i(SDK_TAG, context.getString(R.string.tenet_loaded_from_cache))
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
            Log.i(SDK_TAG, context.getString(R.string.local_tenet_loaded))
        }
    }

    fun login(
        email: String,
        password: String,
        listener: ArcXPIdentityListener? = null
    ): LiveData<Either<ArcXPException, ArcXPAuth>> {
        val stream = createLiveData<Either<ArcXPException, ArcXPAuth>>()
        if (commerceConfig.recaptchaForSignin) {
            runRecaptcha(object : ArcXPIdentityListener() {
                override fun onRecaptchaSuccess(token: String) {
                    setRecaptchaToken(token)
                    identityApiManager.login(email, password, object : ArcXPIdentityListener() {
                        override fun onLoginSuccess(response: ArcXPAuth) {
                            listener?.onLoginSuccess(response)
                            stream.postValue(Success(response))
                        }

                        override fun onLoginError(error: ArcXPException) {
                            listener?.onLoginError(error)
                            stream.postValue(Failure(error))
                        }
                    })
                }

                override fun onRecaptchaFailure(error: ArcXPException) {
                    listener?.onLoginError(
                        createArcXPException(
                            type = ArcXPSDKErrorType.LOGIN_ERROR,
                            message = mContext.getString(R.string.recaptcha_error_login),
                            value = error
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

                override fun onLoginError(error: ArcXPException) {
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
        identityApiManager.changePassword(
            newPassword,
            oldPassword,
            listener
        )
    }

    fun updatePassword(
        newPassword: String,
        oldPassword: String,
        listener: ArcXPIdentityListener?
    ): LiveData<Either<ArcXPException, ArcXPIdentity>> {
        val stream = MutableLiveData<Either<ArcXPException, ArcXPIdentity>>()
        identityApiManager.changePassword(
            newPassword,
            oldPassword,
            object : ArcXPIdentityListener() {
                override fun onPasswordChangeSuccess(it: ArcXPIdentity) {
                    listener?.onPasswordChangeSuccess(it)
                    stream.postValue(Success(it))
                }

                override fun onPasswordChangeError(error: ArcXPException) {
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
    fun obtainNonceByEmailAddress(email: String, listener: ArcXPIdentityListener) =
        identityApiManager.obtainNonceByEmailAddress(email, listener)

    fun requestResetPassword(username: String, listener: ArcXPIdentityListener) =
        identityApiManager.obtainNonceByEmailAddress(username, listener)

    @Deprecated(
        "Use resetPassword",
        ReplaceWith(expression = "resetPassword(nonce, newPassword, listener)")
    )
    fun resetPasswordByNonce(nonce: String, newPassword: String, listener: ArcXPIdentityListener) {
        identityApiManager.resetPasswordByNonce(
            nonce,
            newPassword,
            listener
        )
    }

    fun resetPassword(nonce: String, newPassword: String, listener: ArcXPIdentityListener) =
        identityApiManager.resetPasswordByNonce(
            nonce,
            newPassword,
            listener
        )


    fun requestOneTimeAccessLink(email: String, listener: ArcXPIdentityListener) {
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

                override fun onRecaptchaFailure(error: ArcXPException) {
                    listener.onOneTimeAccessLinkError(
                        createArcXPException(
                            type = ArcXPSDKErrorType.ONE_TIME_ACCESS_LINK_ERROR,
                            message = mContext.getString(R.string.recaptchaMagicLink_error),
                            value = error
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

                override fun onOneTimeAccessLinkError(error: ArcXPException) {
                    listener.onOneTimeAccessLinkError(error)
                }
            })
        }
    }

    @Deprecated(
        "Use redeemOneTimeAccessLink()",
        ReplaceWith(expression = "redeemOneTimeAccessLink(nonce, listener)")
    )
    fun loginOneTimeAccessLink(nonce: String, listener: ArcXPIdentityListener) =
        identityApiManager.loginMagicLink(nonce, listener)


    fun redeemOneTimeAccessLink(nonce: String, listener: ArcXPIdentityListener) {
        identityApiManager.loginMagicLink(nonce, listener)
    }

    fun updateProfile(update: ArcXPUpdateProfileRequest, listener: ArcXPIdentityListener) {
        val request = ArcXPProfilePatchRequest(
            firstName = update.firstName,
            lastName = update.lastName,
            secondLastName = update.secondLastName,
            displayName = update.displayName,
            gender = update.gender,
            email = update.email,
            picture = update.picture,
            birthYear = update.birthYear,
            birthMonth = update.birthMonth,
            birthDay = update.birthDay,
            legacyId = update.legacyId,
            contacts = update.contacts,
            addresses = update.addresses,
            attributes = update.attributes
        )
        identityApiManager.updateProfile(request, listener)
    }

    @Deprecated("Use getUserProfile()", ReplaceWith(expression = "getUserProfile(listener)"))
    fun fetchProfile(listener: ArcXPIdentityListener) = identityApiManager.getProfile(listener)

    fun getUserProfile(listener: ArcXPIdentityListener? = null): LiveData<Either<ArcXPException, ArcXPProfileManage>> {
        val stream = MutableLiveData<Either<ArcXPException, ArcXPProfileManage>>()
        identityApiManager.getProfile(object : ArcXPIdentityListener() {
            override fun onFetchProfileSuccess(profileResponse: ArcXPProfileManage) {

                profileResponse.attributes?.let {
                    userSettingsManager.setCurrentAttributes(it)
                }

                stream.postValue(Success(profileResponse))
                listener?.onFetchProfileSuccess(profileResponse)
            }

            override fun onProfileError(error: ArcXPException) {
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
    ): LiveData<Either<ArcXPException, ArcXPUser>> {
        val stream = MutableLiveData<Either<ArcXPException, ArcXPUser>>()
        if (commerceConfig.recaptchaForSignup) {
            runRecaptcha(object : ArcXPIdentityListener() {
                override fun onRecaptchaSuccess(token: String) {
                    setRecaptchaToken(token)
                    identityApiManager.registerUser(
                        username = username,
                        password = password,
                        email = email,
                        firstname = firstname,
                        lastname = lastname,
                        listener = object : ArcXPIdentityListener() {
                            override fun onRegistrationSuccess(response: ArcXPUser) {
                                listener?.onRegistrationSuccess(response)
                                stream.postValue(Success(response))
                            }

                            override fun onRegistrationError(error: ArcXPException) {
                                listener?.onRegistrationError(error)
                                stream.postValue(Failure(error))
                                _error.postValue(error)
                            }
                        })
                }

                override fun onRecaptchaFailure(error: ArcXPException) {
                    val exception = createArcXPException(
                        ArcXPSDKErrorType.REGISTRATION_ERROR,
                        mContext.getString(R.string.recaptchaMagicLink_error), error
                    )
                    listener?.onRegistrationError(
                        exception
                    )
                    stream.postValue(Failure(exception))
                    _error.postValue(exception)
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
                        stream.postValue(Success(response))
                    }

                    override fun onRegistrationError(error: ArcXPException) {
                        listener?.onRegistrationError(error)
                        stream.postValue(Failure(error))
                        _error.postValue(error)
                    }
                })
        }
        return stream
    }

    fun logout(listener: ArcXPIdentityListener? = null): LiveData<Either<ArcXPException, Boolean>> {
        val stream = MutableLiveData<Either<ArcXPException, Boolean>>()
        if (mContext.getString(R.string.google_key).isNotBlank()) {

            val mGoogleSignInClient = createGoogleSignInClient(application = mContext)

            if (AccessToken.getCurrentAccessToken() != null) {
                LoginManager.getInstance().logOut()
            }

            mGoogleSignInClient.signOut()
            loginWithGoogleOneTapResultsReceiver = null
            loginWithGoogleResultsReceiver = null

            oneTapClient?.signOut()
                ?.addOnSuccessListener {
                    listener?.onLogoutSuccess()
                }
                ?.addOnFailureListener {
                    listener?.onLogoutError(
                        createArcXPException(
                            ArcXPSDKErrorType.GOOGLE_LOGIN_ERROR,
                            it.message!!,
                            it
                        )
                    )
                }
        }
        identityApiManager.logout(object : ArcXPIdentityListener() {
            override fun onLogoutSuccess() {
                rememberUser(false)
                stream.postValue(Success(true))
                listener?.onLogoutSuccess()
            }

            override fun onLogoutError(error: ArcXPException) {
                stream.postValue(Failure(error))
                listener?.onLogoutError(error)
            }
        })
        return stream
    }

    fun removeIdentity(grantType: String, listener: ArcXPIdentityListener) =
        identityApiManager.removeIdentity(grantType = grantType, listener = listener)

    @Deprecated(
        "Use requestDeleteAccount()",
        ReplaceWith(expression = "requestDeleteAccount(listener)")
    )
    fun deleteUser(listener: ArcXPIdentityListener) =
        identityApiManager.deleteUser(listener = listener)

    fun requestDeleteAccount(listener: ArcXPIdentityListener) =
        identityApiManager.deleteUser(listener = listener)

    @Deprecated(
        "Use approveDeleteAccount()",
        ReplaceWith(expression = "approveDeleteAccount(nonce, listener)")
    )
    fun approveDeletion(nonce: String, listener: ArcXPIdentityListener) =
        identityApiManager.approveDeletion(nonce = nonce, listener = listener)

    fun approveDeleteAccount(nonce: String, listener: ArcXPIdentityListener) =
        identityApiManager.approveDeletion(nonce = nonce, listener = listener)

    fun validateSession(token: String, listener: ArcXPIdentityListener) =
        identityApiManager.validateJwt(token = token, arcIdentityListener = listener)

    fun validateSession(listener: ArcXPIdentityListener) =
        identityApiManager.validateJwt(listenerArc = listener)

    fun refreshSession(token: String, listener: ArcXPIdentityListener) =
        identityApiManager.refreshToken(
            token = token,
            grantType = ArcXPAuthRequest.Companion.GrantType.REFRESH_TOKEN.value,
            arcIdentityListener = listener
        )

    fun refreshSession(listener: ArcXPIdentityListener) =
        identityApiManager.refreshToken(
            token = authManager.refreshToken,
            grantType = ArcXPAuthRequest.Companion.GrantType.REFRESH_TOKEN.value,
            arcIdentityListener = listener
        )

    fun getAllSubscriptions(listener: ArcXPSalesListener) =
        salesApiManager.getAllSubscriptions(callback = listener)

    fun getAllActiveSubscriptions(listener: ArcXPSalesListener) =
        salesApiManager.getAllActiveSubscriptions(callback = listener)

    fun getActivePaywallRules(listener: ArcXPRetailListener) =
        retailApiManager.getActivePaywallRules(listener = listener)

    fun getEntitlements(listener: ArcXPSalesListener) =
        salesApiManager.getEntitlements(listener)

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
        evaluatePage(
            pageviewData = pageviewData,
            entitlements = entitlements,
            currentTime = null,
            listener = listener
        )
    }

    fun evaluatePage(
        pageId: String,
        contentType: String?,
        contentSection: String?,
        deviceClass: String?,
        otherConditions: HashMap<String, String>?,
        entitlements: ArcXPEntitlements? = null
    ): LiveData<ArcXPPageviewEvaluationResult> {
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
        evaluatePage(
            pageviewData = pageviewData,
            entitlements = entitlements,
            currentTime = null,
            listener = object : ArcXPPageviewListener() {
                override fun onEvaluationResult(response: ArcXPPageviewEvaluationResult) {
                    stream.postValue(response)
                }
            })
        return stream
    }

    fun evaluatePageTime(
        pageId: String,
        contentType: String?,
        contentSection: String?,
        deviceClass: String?,
        otherConditions: HashMap<String, String>?,
        entitlements: ArcXPEntitlements? = null,
        currentTime: Long? = Calendar.getInstance().timeInMillis,
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
        evaluatePage(
            pageviewData = pageviewData,
            entitlements = entitlements,
            currentTime = currentTime,
            listener = listener
        )
    }

    fun evaluatePage(
        pageviewData: ArcXPPageviewData,
        entitlements: ArcXPEntitlements? = null,
        currentTime: Long? = Calendar.getInstance().timeInMillis,
        listener: ArcXPPageviewListener
    ) {
        paywallManager.initialize(
            entitlementsResponse = entitlements,
            passedInTime = currentTime,
            loggedInState = sessionIsActive(),
            listener = object : ArcXPPageviewListener() {
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

    fun evaluatePageNoTime(
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
        identityApiManager.validateJwt(object : ArcXPIdentityListener() {
            override fun onValidateSessionSuccess() {
                listener?.onValidateSessionSuccess()
                stream.postValue(true)
            }

            override fun onValidateSessionError(error: ArcXPException) {
                listener?.onValidateSessionError(error)
                stream.postValue(false)
            }
        })
        return stream
    }

    fun setRecaptchaToken(token: String) {
        identityApiManager.setRecaptchaToken(recaptchaToken = token)
    }

    fun getRecaptchaToken(): String? {
        return identityApiManager.getRecaptchaToken()
    }

    fun thirdPartyLogin(
        token: String,
        type: ArcXPAuthRequest.Companion.GrantType,
        listener: ArcXPIdentityListener
    ) = identityApiManager.thirdPartyLogin(
        token = token,
        type = type,
        arcIdentityListener = listener
    )

    fun sendVerificationEmail(email: String, listener: ArcXPIdentityListener) =
        identityApiManager.sendVerificationEmail(email = email, listener = listener)


    fun sessionIsActive() = authManager.uuid != null

    fun setAccessToken(token: String) {
        authManager.accessToken = token
    }

    fun verifyEmail(nonce: String, listener: ArcXPIdentityListener) =
        identityApiManager.verifyEmail(nonce, listener)


    fun runRecaptcha(listener: ArcXPIdentityListener) =
        if (commerceConfig.recaptchaSiteKey.isNullOrBlank()) {
            listener.onRecaptchaFailure(
                createArcXPException(
                    ArcXPSDKErrorType.RECAPTCHA_ERROR,
                    mContext.getString(R.string.recaptchaSiteKey_error),
                    null
                )
            )
        } else {
            identityApiManager.checkRecaptcha(commerceConfig, listener)
        }

    fun rememberUser(remember: Boolean) = identityApiManager.rememberUser(remember)

    fun loginWithFacebook(
        fbLoginButton: LoginButton,
        listener: ArcXPIdentityListener? = null
    ): LiveData<ArcXPAuth> {
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

                            override fun onLoginError(error: ArcXPException) {
                                listener?.onLoginError(error)
                                _error.postValue(error)
                            }
                        })
                }
            }

            override fun onCancel() {
                val error = createArcXPException(
                    ArcXPSDKErrorType.FACEBOOK_LOGIN_CANCEL,
                    mContext.getString(R.string.user_cancelled_login_error)
                )
                listener?.onLoginError(error)
                _error.postValue(error)
            }

            override fun onError(error: FacebookException) {
                error.message?.let {
                    createArcXPException(
                        ArcXPSDKErrorType.FACEBOOK_LOGIN_ERROR,
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
        val stream = MutableLiveData<ArcXPAuth>()
        val mGoogleSignInClient = createGoogleSignInClient(application = mContext)

        val signInIntent: Intent = mGoogleSignInClient.signInIntent

        if (loginWithGoogleResultsReceiver == null) {
            loginWithGoogleResultsReceiver =
                createLoginWithGoogleResultsReceiver(signInIntent, this,
                    object : ArcXPIdentityListener() {
                        override fun onLoginSuccess(response: ArcXPAuth) {
                            activity.supportFragmentManager.beginTransaction()
                                .remove(loginWithGoogleResultsReceiver as Fragment).commit()
                            listener?.onLoginSuccess(response)
                            stream.postValue(response)
                        }

                        override fun onLoginError(error: ArcXPException) {
                            activity.supportFragmentManager.beginTransaction()
                                .remove(loginWithGoogleResultsReceiver as Fragment).commit()
                            listener?.onLoginError(error)
                            _error.postValue(error)
                            loginWithGoogleResultsReceiver = null
                        }
                    })
        }

        activity.supportFragmentManager.beginTransaction()
            .add(
                loginWithGoogleResultsReceiver!!,
                mContext.getString(R.string.google_login_fragment_tag)
            ).commit()

        return stream
    }

    fun logoutOfGoogle(listener: ArcXPIdentityListener) {
        if (mContext.getString(R.string.google_key).isNotBlank()) {
            val mGoogleSignInClient = createGoogleSignInClient(application = mContext)

            mGoogleSignInClient.signOut()

            oneTapClient?.signOut()
                ?.addOnSuccessListener {
                    listener.onLogoutSuccess()
                }
                ?.addOnFailureListener {
                    listener.onLogoutError(
                        createArcXPException(
                            type = ArcXPSDKErrorType.GOOGLE_LOGIN_ERROR,
                            message = it.message,
                            value = it
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
            oneTapClient?.let {
                oneTapClient?.beginSignIn(signInRequest)
                    ?.addOnSuccessListener(activity) { result ->
                        val request = buildIntentSenderRequest(result.pendingIntent.intentSender)
                        if (loginWithGoogleOneTapResultsReceiver == null) {
                            loginWithGoogleOneTapResultsReceiver =
                                createLoginWithGoogleOneTapResultsReceiver(
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

                                        override fun onLoginError(error: ArcXPException) {
                                            activity.supportFragmentManager.beginTransaction()
                                                .remove(loginWithGoogleOneTapResultsReceiver as Fragment)
                                                .commit()
                                            listener.onLoginError(error)
                                        }
                                    })
                        }
                        activity.supportFragmentManager.beginTransaction()
                            .add(
                                loginWithGoogleOneTapResultsReceiver!!,
                                mContext.getString(R.string.google_login_fragment_tag)
                            ).commit()
                    }
                    ?.addOnFailureListener(activity) { e ->
                        listener.onLoginError(
                            createArcXPException(
                                ArcXPSDKErrorType.GOOGLE_LOGIN_ERROR_NO_ACCOUNT,
                                e.localizedMessage, e
                            )
                        )
                    }
            }
        } else {
            listener.onLoginError(
                createArcXPException(
                    ArcXPSDKErrorType.GOOGLE_LOGIN_ERROR,
                    mContext.getString(R.string.one_tap_disabled_error_message)
                )
            )
        }
    }


    internal fun handleSignInResult(
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

                        override fun onLoginError(error: ArcXPException) {
                            logoutOfGoogle(listener)
                            listener.onLoginError(error)
                        }
                    })
            }
            // Signed in successfully, show authenticated UI.
        } catch (e: ApiException) {
            listener.onLoginError(
                createArcXPException(
                    ArcXPSDKErrorType.GOOGLE_LOGIN_ERROR,
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
        callbackManager.onActivityResult(
            requestCode = requestCode,
            resultCode = resultCode,
            data = data
        )
    }

    //commenting out sales methods that are not used currently
    //leaving the code in case it is needed later.
//    fun initializePaymentMethod(id: String, pid: String, listener: ArcXPSalesListener?) =
//        salesApiManager.initializePaymentMethod(id, pid, listener)

//    fun finalizePaymentMethod(
//        id: String,
//        pid: String,
//        request: ArcXPFinalizePaymentRequest,
//        listener: ArcXPSalesListener?
//    ) = salesApiManager.finalizePaymentMethod(id, pid, request, listener)

//    fun finalizePaymentMethod(
//        id: String,
//        pid: String,
//        token: String? = null,
//        email: String? = null,
//        address: ArcXPAddressRequest? = null,
//        phone: String? = null,
//        browserInfo: String? = null,
//        firstName: String? = null,
//        lastName: String? = null,
//        listener: ArcXPSalesListener?
//    ) {
//        val request = ArcXPFinalizePaymentRequest(
//            token,
//            email,
//            address,
//            phone,
//            browserInfo,
//            firstName,
//            lastName
//        )
//        salesApiManager.finalizePaymentMethod(id, pid, request, listener)
//    }

//    fun finalizePaymentMethod(
//        id: String,
//        pid: String,
//        token: String? = null,
//        email: String? = null,
//        addressLine1: String,
//        addressLine2: String? = null,
//        addressLocality: String,
//        addressRegion: String? = null,
//        addressPostal: String? = null,
//        addressCountry: String,
//        addressType: String,
//        phone: String? = null,
//        browserInfo: String? = null,
//        firstName: String? = null,
//        lastName: String? = null,
//        listener: ArcXPSalesListener?
//    ) {
//        val aRequest = ArcXPAddressRequest(
//            addressLine1, addressLine2, addressLocality, addressRegion,
//            addressPostal, addressCountry, addressType
//        )
//        val request = ArcXPFinalizePaymentRequest(
//            token,
//            email,
//            aRequest,
//            phone,
//            browserInfo,
//            firstName,
//            lastName
//        )
//        salesApiManager.finalizePaymentMethod(id, pid, request, listener)
//    }

//    fun finalizePaymentMethod3ds(
//        id: String,
//        pid: String,
//        request: ArcXPFinalizePaymentRequest,
//        listener: ArcXPSalesListener?
//    ) {
//        salesApiManager.finalizePaymentMethod3ds(id, pid, request, listener)
//    }

//    fun finalizePaymentMethod3ds(
//        id: String,
//        pid: String,
//        token: String? = null,
//        email: String? = null,
//        address: ArcXPAddressRequest? = null,
//        phone: String? = null,
//        browserInfo: String? = null,
//        firstName: String? = null,
//        lastName: String? = null,
//        listener: ArcXPSalesListener?
//    ) {
//        val request = ArcXPFinalizePaymentRequest(
//            token,
//            email,
//            address,
//            phone,
//            browserInfo,
//            firstName,
//            lastName
//        )
//        salesApiManager.finalizePaymentMethod3ds(id, pid, request, listener)
//    }

//    fun finalizePaymentMethod3ds(
//        id: String,
//        pid: String,
//        token: String? = null,
//        email: String? = null,
//        addressLine1: String,
//        addressLine2: String? = null,
//        addressLocality: String,
//        addressRegion: String? = null,
//        addressPostal: String? = null,
//        addressCountry: String,
//        addressType: String,
//        phone: String? = null,
//        browserInfo: String? = null,
//        firstName: String? = null,
//        lastName: String? = null,
//        listener: ArcXPSalesListener?
//    ) {
//        val aRequest = ArcXPAddressRequest(
//            line1 = addressLine1,
//            line2 = addressLine2,
//            locality = addressLocality,
//            region = addressRegion,
//            postal = addressPostal,
//            country = addressCountry,
//            type = addressType
//        )
//        val request = ArcXPFinalizePaymentRequest(
//            token = token,
//            email = email,
//            address = aRequest,
//            phone = phone,
//            browserInfo = browserInfo,
//            firstName = firstName,
//            lastName = lastName
//        )
//        salesApiManager.finalizePaymentMethod3ds(id, pid, request, listener)
//    }

//    fun cancelSubscription(
//        id: String,
//        request: ArcXPCancelSubscriptionRequest,
//        listener: ArcXPSalesListener?
//    ) = salesApiManager.cancelSubscription(id, request, listener)

//    fun cancelSubscription(id: String, reason: String, listener: ArcXPSalesListener?) {
//        val request = ArcXPCancelSubscriptionRequest(reason)
//        salesApiManager.cancelSubscription(id, request, listener)
//    }

//    fun updateAddress(request: ArcXPUpdateAddressRequest, listener: ArcXPSalesListener?) =
//        salesApiManager.updateAddress(request, listener)

//    fun updateAddress(
//        subscriptionID: Int?,
//        billingAddress: ArcXPAddressRequest?,
//        listener: ArcXPSalesListener?
//    ) {
//        val request = ArcXPUpdateAddressRequest(subscriptionID, billingAddress)
//        salesApiManager.updateAddress(request, listener)
//    }

//    fun updateAddress(
//        subscriptionID: Int?,
//        addressLine1: String,
//        addressLine2: String? = null,
//        addressLocality: String,
//        addressRegion: String? = null,
//        addressPostal: String? = null,
//        addressCountry: String,
//        addressType: String,
//        listener: ArcXPSalesListener?
//    ) {
//        val aRequest = ArcXPAddressRequest(
//            line1 = addressLine1,
//            line2 = addressLine2,
//            locality = addressLocality,
//            region = addressRegion,
//            postal = addressPostal,
//            country = addressCountry,
//            type = addressType
//        )
//        val request = ArcXPUpdateAddressRequest(
//            subscriptionID = subscriptionID,
//            billingAddress = aRequest
//        )
//        salesApiManager.updateAddress(request = request, callback = listener)
//    }

    fun getSubscriptionDetails(id: String, listener: ArcXPSalesListener?) =
        salesApiManager.getSubscriptionDetails(id, listener)

//    fun createCustomerOrder(
//        email: String?,
//        phone: String?,
//        shippingAddress: ArcXPAddressRequest?,
//        billingAddress: ArcXPAddressRequest?,
//        firstName: String?,
//        lastName: String?,
//        secondLastName: String?,
//        listener: ArcXPSalesListener?
//    ) {
//        val request = ArcXPCustomerOrderRequest(
//            email,
//            phone,
//            shippingAddress,
//            billingAddress,
//            firstName,
//            lastName,
//            secondLastName
//        )
//        salesApiManager.createCustomerOrder(request, listener)
//    }

//    fun createCustomerOrder(
//        email: String?,
//        phone: String?,
//        shippingAddressLine1: String,
//        shippingAddressLine2: String? = null,
//        shippingAddressLocality: String,
//        shippingAddressRegion: String? = null,
//        shippingAddressPostal: String? = null,
//        shippingAddressCountry: String,
//        shippingAddressType: String,
//        billingAddressLine1: String,
//        billingAddressLine2: String? = null,
//        billingAddressLocality: String,
//        billingAddressRegion: String? = null,
//        billingAddressPostal: String? = null,
//        billingAddressCountry: String,
//        billingAddressType: String,
//        firstName: String?,
//        lastName: String?,
//        secondLastName: String?,
//        listener: ArcXPSalesListener?
//    ) {
//        val aRequest = ArcXPAddressRequest(
//            line1 = shippingAddressLine1,
//            line2 = shippingAddressLine2,
//            locality = shippingAddressLocality,
//            region = shippingAddressRegion,
//            postal = shippingAddressPostal,
//            country = shippingAddressCountry,
//            type = shippingAddressType
//        )
//        val bRequest = ArcXPAddressRequest(
//            line1 = billingAddressLine1,
//            line2 = billingAddressLine2,
//            locality = billingAddressLocality,
//            region = billingAddressRegion,
//            postal = billingAddressPostal,
//            country = billingAddressCountry,
//            type = billingAddressType
//        )
//        val request = ArcXPCustomerOrderRequest(
//            email = email,
//            phone = phone,
//            shippingAddress = aRequest,
//            billingAddress = bRequest,
//            firstName = firstName,
//            lastName = lastName,
//            secondLastName = secondLastName
//        )
//        salesApiManager.createCustomerOrder(request, listener)
//    }

//    fun getPaymentOptions(listener: ArcXPSalesListener?) =
//        salesApiManager.getPaymentOptions(listener)

//    fun getPaymentAddresses(listener: ArcXPSalesListener?) =
//        salesApiManager.getPaymentAddresses(listener)

//    fun initializePayment(orderNumber: String, mid: String, listener: ArcXPSalesListener?) =
//        salesApiManager.initializePayment(orderNumber, mid, listener)

//    fun finalizePayment(
//        orderNumber: String,
//        mid: String,
//        request: ArcXPFinalizePaymentRequest,
//        listener: ArcXPSalesListener?
//    ) = salesApiManager.finalizePayment(orderNumber, mid, request, listener)

//    fun finalizePayment(
//        orderNumber: String,
//        mid: String,
//        token: String?,
//        email: String?,
//        address: ArcXPAddressRequest?,
//        phone: String?,
//        browserInfo: String?,
//        firstName: String?,
//        lastName: String?,
//        listener: ArcXPSalesListener?
//    ) {
//        val request = ArcXPFinalizePaymentRequest(
//            token,
//            email,
//            address,
//            phone,
//            browserInfo,
//            firstName,
//            lastName
//        )
//        salesApiManager.finalizePayment(orderNumber, mid, request, listener)
//    }

//    fun finalizePayment(
//        orderNumber: String,
//        mid: String,
//        token: String?,
//        email: String?,
//        addressLine1: String,
//        addressLine2: String? = null,
//        addressLocality: String,
//        addressRegion: String? = null,
//        addressPostal: String? = null,
//        addressCountry: String,
//        addressType: String,
//        phone: String?,
//        browserInfo: String?,
//        firstName: String?,
//        lastName: String?,
//        listener: ArcXPSalesListener?
//    ) {
//        val aRequest = ArcXPAddressRequest(
//            addressLine1, addressLine2, addressLocality, addressRegion,
//            addressPostal, addressCountry, addressType
//        )
//        val request = ArcXPFinalizePaymentRequest(
//            token,
//            email,
//            aRequest,
//            phone,
//            browserInfo,
//            firstName,
//            lastName
//        )
//        salesApiManager.finalizePayment(orderNumber, mid, request, listener)
//    }

//    fun finalizePayment3ds(
//        orderNumber: String,
//        mid: String,
//        request: ArcXPFinalizePaymentRequest,
//        listener: ArcXPSalesListener?
//    ) = salesApiManager.finalizePayment3ds(orderNumber, mid, request, listener)

//    fun finalizePayment3ds(
//        orderNumber: String,
//        mid: String,
//        token: String?,
//        email: String?,
//        address: ArcXPAddressRequest?,
//        phone: String?,
//        browserInfo: String?,
//        firstName: String?,
//        lastName: String?,
//        listener: ArcXPSalesListener?
//    ) {
//        val request = ArcXPFinalizePaymentRequest(
//            token,
//            email,
//            address,
//            phone,
//            browserInfo,
//            firstName,
//            lastName
//        )
//        salesApiManager.finalizePayment3ds(orderNumber, mid, request, listener)
//    }

//    fun finalizePayment3ds(
//        orderNumber: String,
//        mid: String,
//        token: String?,
//        email: String?,
//        addressLine1: String,
//        addressLine2: String? = null,
//        addressLocality: String,
//        addressRegion: String? = null,
//        addressPostal: String? = null,
//        addressCountry: String,
//        addressType: String,
//        phone: String?,
//        browserInfo: String?,
//        firstName: String?,
//        lastName: String?,
//        listener: ArcXPSalesListener?
//    ) {
//        val aRequest = ArcXPAddressRequest(
//            addressLine1, addressLine2, addressLocality, addressRegion,
//            addressPostal, addressCountry, addressType
//        )
//        val request = ArcXPFinalizePaymentRequest(
//            token,
//            email,
//            aRequest,
//            phone,
//            browserInfo,
//            firstName,
//            lastName
//        )
//        salesApiManager.finalizePayment3ds(orderNumber, mid, request, listener)
//    }

//    fun getOrderHistory(listener: ArcXPSalesListener?) = salesApiManager.getOrderHistory(listener)

//    fun getOrderDetails(orderNumber: String, listener: ArcXPSalesListener?) =
//        salesApiManager.getOrderDetails(orderNumber, listener)


//    fun clearCart(listener: ArcXPSalesListener?) =
//        salesApiManager.clearCart(listener)

//    fun getCurrentCart(listener: ArcXPSalesListener?) = salesApiManager.getCurrentCart(listener)

//    fun addItemToCart(request: ArcXPCartItemsRequest, listener: ArcXPSalesListener?) =
//        salesApiManager.addItemToCart(request, listener)

//    fun addItemToCart(
//        items: List<CartItem?>?,
//        addressLine1: String,
//        addressLine2: String? = null,
//        addressLocality: String,
//        addressRegion: String? = null,
//        addressPostal: String? = null,
//        addressCountry: String,
//        addressType: String,
//        listener: ArcXPSalesListener?
//    ) {
//        val aRequest = ArcXPAddressRequest(
//            addressLine1, addressLine2, addressLocality, addressRegion,
//            addressPostal, addressCountry, addressType
//        )
//        val request = ArcXPCartItemsRequest(items, aRequest)
//        salesApiManager.addItemToCart(request, listener)
//    }

//    fun addItemToCart(
//        items: List<CartItem?>?,
//        billingAddress: ArcXPAddressRequest?,
//        listener: ArcXPSalesListener?
//    ) {
//        val request = ArcXPCartItemsRequest(items, billingAddress)
//        salesApiManager.addItemToCart(request, listener)
//    }

//    fun removeItemFromCart(sku: String, listener: ArcXPSalesListener?) =
//        salesApiManager.removeItemFromCart(sku, listener)


    suspend fun getAllGDPR() =
        identityApiManager.getAllGDPR()

    suspend fun getUserSettings() =
        identityApiManager.getUserSettings()

    suspend fun setProfileAttribute(key: String, value: String) =
        identityApiManager.setProfileAttribute(key, value)


    companion object {

        @Volatile
        private var INSTANCE: ArcXPCommerceManager? = null

        @JvmStatic
        fun initialize(
            context: Application,
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
