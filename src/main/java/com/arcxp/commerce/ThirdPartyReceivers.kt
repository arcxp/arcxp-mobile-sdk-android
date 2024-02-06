package com.arcxp.commerce

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.media3.common.util.UnstableApi
import com.arcxp.commerce.callbacks.ArcXPIdentityListener
import com.arcxp.commerce.models.ArcXPAuth
import com.arcxp.commerce.models.ArcXPAuthRequest
import com.arcxp.commons.throwables.ArcXPException
import com.arcxp.commons.throwables.ArcXPSDKErrorType
import com.arcxp.commons.util.DependencyFactory
import com.arcxp.sdk.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes

@UnstableApi
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

                                        override fun onLoginError(error: ArcXPException) {
                                            listener.onLoginError(error)
                                        }
                                    }
                                )
                            } else {
                                listener.onGoogleOneTapLoginSuccess(
                                    username,
                                    password,
                                    idToken
                                )
                            }
                        }
                        password != null -> {
                            if (manager.commerceConfig.googleOneTapAutoLoginEnabled) {
                                manager.login(username!!,
                                    password,
                                    object : ArcXPIdentityListener() {
                                        override fun onLoginSuccess(response: ArcXPAuth) {
                                            listener.onGoogleOneTapLoginSuccess(
                                                username,
                                                password,
                                                idToken
                                            )
                                        }

                                        override fun onLoginError(error: ArcXPException) {
                                            listener.onLoginError(error)
                                        }
                                    })
                            } else {
                                listener.onGoogleOneTapLoginSuccess(
                                    username,
                                    password,
                                    idToken
                                )
                            }
                        }
                        else -> {
                            listener.onLoginError(
                                DependencyFactory.createArcXPException(
                                    ArcXPSDKErrorType.GOOGLE_LOGIN_ERROR,
                                    "Google One Tap login error - no ID token or password."
                                )
                            )
                            listener.onLoginError(
                                DependencyFactory.createArcXPException(
                                    ArcXPSDKErrorType.GOOGLE_LOGIN_ERROR,
                                    "Google One Tap login error - no ID token or password."
                                )
                            )
                        }
                    }
                } catch (e: ApiException) {
                    when (e.statusCode) {
                        CommonStatusCodes.CANCELED -> {
                            listener.onLoginError(
                                DependencyFactory.createArcXPException(
                                    ArcXPSDKErrorType.GOOGLE_LOGIN_CANCEL,
                                    e.localizedMessage, e
                                )
                            )
                            listener.onLoginError(
                                DependencyFactory.createArcXPException(
                                    ArcXPSDKErrorType.GOOGLE_LOGIN_CANCEL,
                                    e.localizedMessage, e
                                )
                            )
                        }
                        else -> {
                            listener.onLoginError(
                                DependencyFactory.createArcXPException(
                                    ArcXPSDKErrorType.GOOGLE_LOGIN_ERROR,
                                    e.localizedMessage, e
                                )
                            )
                            listener.onLoginError(
                                DependencyFactory.createArcXPException(
                                    ArcXPSDKErrorType.GOOGLE_LOGIN_ERROR,
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

@UnstableApi
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
                        DependencyFactory.createArcXPException(
                            message =
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

                        override fun onLoginError(error: ArcXPException) {
                            listener.onLoginError(error)
                        }
                    })
                }

            } else if (result.resultCode == Activity.RESULT_CANCELED) {
                listener.onLoginError(DependencyFactory.createArcXPException(message = getString(R.string.canceled)))
            }

        })

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        operation.launch(signInIntent)
    }
}