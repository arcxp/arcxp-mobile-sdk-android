package com.arcxp.commerce.models.applesignin

import android.net.Uri
import android.os.Parcel
import android.os.Parcelable
import androidx.fragment.app.FragmentManager
import com.arcxp.commerce.models.applesignin.view.SignInWebViewDialogFragment
import java.util.*

/**
 * @suppress
 */
class SignInWithAppleService(
    private val fragmentManager: FragmentManager,
    private val fragmentTag: String,
    private val configuration: SignInWithAppleConfiguration,
    private val callback: (SignInWithAppleResult) -> Unit
) {

    constructor(
        fragmentManager: FragmentManager,
        fragmentTag: String,
        configuration: SignInWithAppleConfiguration
    ) : this(fragmentManager, fragmentTag, configuration, toFunction())

    init {
        val fragmentIfShown =
            fragmentManager.findFragmentByTag(fragmentTag) as? SignInWebViewDialogFragment
        fragmentIfShown?.configure(callback)
    }

    internal data class AuthenticationAttempt(
        val authenticationUri: String,
        val redirectUri: String,
        val authTokenUrl: String,
        val state: String
    ) : Parcelable {
        constructor(parcel: Parcel) : this(
            parcel.readString() ?: "invalid",
            parcel.readString() ?: "invalid",
            parcel.readString()?: "invalid",
            parcel.readString() ?: "invalid"
        )

        override fun writeToParcel(parcel: Parcel, flags: Int) { }

        override fun describeContents(): Int {
            return 0
        }

        companion object CREATOR : Parcelable.Creator<AuthenticationAttempt> {

            override fun createFromParcel(parcel: Parcel) =  AuthenticationAttempt(parcel)

            override fun newArray(size: Int): Array<AuthenticationAttempt?> = arrayOfNulls(size)

            /*
            The authentication page URI we're creating is based off the URI constructed by Apple's JavaScript SDK,
            which is why certain fields (like the version, v) are included in the URI construction.

            We have to build this URI ourselves because Apple's behavior in JavaScript is to POST the response,
            while we need a GET so we can retrieve the authentication code and verify the state
            merely by intercepting the URL.

            See the Sign In With Apple Javascript SDK for comparison:
            https://developer.apple.com/documentation/signinwithapplejs/configuring_your_webpage_for_sign_in_with_apple
            */
//            fun create(
//                configuration: SignInWithAppleConfiguration,
//                state: String = UUID.randomUUID().toString()
//            ): AuthenticationAttempt {
//                val authenticationUri = Uri
//                    .parse("https://appleid.apple.com/auth/authorize")
//                    .buildUpon().apply {
//                        appendQueryParameter("response_type", "code")
//                        appendQueryParameter("v", "1.1.6")
//                        appendQueryParameter("client_id", configuration.clientId)
//                        appendQueryParameter("redirect_uri", configuration.redirectUri)
//                        appendQueryParameter("scope", configuration.scope)
//                        appendQueryParameter("state", state)
//                        appendQueryParameter("response_mode", "form_post")
//                    }
//                    .build()
//                    .toString()
//
//                return AuthenticationAttempt(authenticationUri, configuration.redirectUri, configuration.authTokenUrl, state)
//            }

            fun createWithArcAuth(configuration: SignInWithAppleConfiguration): AuthenticationAttempt {
                val state = Uri
                    .parse(configuration.arcAuthUrl)
                    .getQueryParameter("state")
                val authenticationUri = Uri
                    .parse(configuration.arcAuthUrl)
                    .buildUpon().apply {
                        appendQueryParameter("redirect_uri", configuration.redirectUri)
                    }
                    .build()
                    .toString()


                return AuthenticationAttempt(
                    authenticationUri,
                    configuration.redirectUri,
                    configuration.authTokenUrl,
                    state ?: UUID.randomUUID().toString()
                )

            }
        }
    }

    fun show() {
        val fragment = SignInWebViewDialogFragment.newInstance(
            AuthenticationAttempt.createWithArcAuth(
                configuration
            )
        )
        fragment.configure(callback)
        fragment.show(fragmentManager, fragmentTag)
    }
}
