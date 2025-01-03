package com.arcxp.commerce

import android.app.Application
import androidx.annotation.Keep
import com.arcxp.ArcXPMobileSDK.application
import com.arcxp.commons.throwables.ArcXPSDKErrorType
import com.arcxp.commons.util.DependencyFactory.createArcXPException
import com.arcxp.sdk.R

/**
 * ArcXPCommerceConfig is a configuration class for the ArcXP Commerce module.
 * It encapsulates various settings required for initializing and managing the commerce services.
 *
 * The class includes parameters for reCAPTCHA, social login integrations, URL configurations,
 * password policies, and caching options. It provides a Builder class to facilitate
 * the construction of ArcXPCommerceConfig instances with a fluent API.
 *
 * Usage:
 * - Use the Builder class to set the desired configurations.
 * - Call the build method to create an instance of ArcXPCommerceConfig.
 *
 * Example:
 *
 * val commerceConfig = ArcXPCommerceConfig.Builder()
 *      .setContext(application)
 *      .setRecaptchaSiteKey("your-recaptcha-site-key")
 *      .setFacebookAppId("your-facebook-app-id")
 *      .setGoogleClientId("your-google-client-id")
 *      .enableRecaptchaForSignup(true)
 *      .setBaseUrl("https://api.example.com")
 *      .build()
 *
 *
 * Note: Ensure that the context is set before calling the `build` method, as it is required
 * for creating the configuration instance.
 *
 * */
@Keep
class ArcXPCommerceConfig(

    val recaptchaSiteKey: String?,

    val facebookAppId: String?,

    val googleClientId: String?,

    val recaptchaForSignup: Boolean,

    val recaptchaForSignin: Boolean,

    val recaptchaForOneTimeAccess: Boolean,

    val baseUrl: String?,

    val baseSalesUrl: String?,

    val baseRetailUrl: String?,

    val useLocalConfig: Boolean,

    val pwMinLength: Int?,

    val pwSpecialCharacters: Int?,

    val pwUppercase: Int?,

    val pwLowercase: Int?,

    val userNameIsEmail: Boolean,

    val pwPwNumbers: Int?,

    val googleOneTapEnabled: Boolean,

    val googleOneTapAutoLoginEnabled: Boolean,

    val autoCache: Boolean,

    val useCachedPaywall: Boolean
) {
    class Builder {
        private var context: Application? = null
        private var recaptchaSiteKey: String? = null
        private var facebookAppId: String? = null
        private var googleClientId: String? = null
        private var recaptchaForSignup = false
        private var recaptchaForSignin = false
        private var recaptchaForOneTimeAccess = false
        private var baseUrl: String? = null
        private var baseSalesUrl: String? = null
        private var baseRetailUrl: String? = null
        private var useLocalConfig: Boolean = false
        private var pwMinLength: Int = 0
        private var pwSpecialCharacters: Int = 0
        private var pwUppercase: Int = 0
        private var pwLowercase: Int = 0
        private var userNameIsEmail: Boolean = false
        private var pwPwNumbers: Int = 0
        private var googleOneTapEnabled = true
        private var googleOneTapAutoLoginEnabled = true
        private var autoCache = false
        private var useCachedPaywall: Boolean = true

        fun setContext(context: Application): Builder {
            this.context = context
            return this
        }

        fun setRecaptchaSiteKey(key: String): Builder {
            this.recaptchaSiteKey = key
            return this
        }

        fun setFacebookAppId(id: String): Builder {
            this.facebookAppId = id
            return this
        }

        fun setGoogleClientId(id: String): Builder {
            this.googleClientId = id
            return this
        }

        fun enableRecaptchaForSignup(enable: Boolean): Builder {
            this.recaptchaForSignup = enable
            return this
        }

        fun enableRecaptchaForSignin(enable: Boolean): Builder {
            this.recaptchaForSignin = enable
            return this
        }

        fun enableRecaptchaForOneTimeAccess(enable: Boolean): Builder {
            this.recaptchaForOneTimeAccess = enable
            return this
        }

        fun setBaseUrl(url: String): Builder {
            this.baseUrl = url
            return this
        }

        fun setBaseSalesUrl(url: String): Builder {
            this.baseSalesUrl = url
            return this
        }

        fun setBaseRetailUrl(url: String): Builder {
            this.baseRetailUrl = url
            return this
        }

        fun useLocalConfig(use: Boolean): Builder {
            this.useLocalConfig = use
            return this
        }

        fun setPwMinLength(length: Int): Builder {
            pwMinLength = length
            return this
        }

        fun setPwSpecialCharacters(number: Int): Builder {
            pwSpecialCharacters = number
            return this
        }

        fun setPwUppercase(number: Int): Builder {
            pwUppercase = number
            return this
        }

        fun setPwLowercase(number: Int): Builder {
            pwLowercase = number
            return this
        }

        fun setUserNameIsEmail(isUsernameEmail: Boolean): Builder {
            userNameIsEmail = isUsernameEmail
            return this
        }

        fun setPwPwNumbers(number: Int): Builder {
            pwPwNumbers = number
            return this
        }

        fun enableGoogleOneTap(enable: Boolean): Builder {
            googleOneTapEnabled = enable
            return this
        }

        fun enableGoogleOneTapAutoLogin(enable: Boolean): Builder {
            googleOneTapAutoLoginEnabled = enable
            return this
        }

        fun enableAutoCache(enable: Boolean): Builder {
            autoCache = enable
            return this
        }

        fun usePaywallCache(enable: Boolean): Builder {
            useCachedPaywall = enable
            return this
        }

        fun build(): ArcXPCommerceConfig {
            if (context == null) {
                throw createArcXPException(
                    type = ArcXPSDKErrorType.CONFIG_ERROR,
                    message = application().getString(R.string.commerce_builder_missing_context)
                )
            }
            return ArcXPCommerceConfig(
                recaptchaSiteKey = recaptchaSiteKey,
                facebookAppId = facebookAppId,
                googleClientId = googleClientId,
                recaptchaForSignup = recaptchaForSignup,
                recaptchaForSignin = recaptchaForSignin,
                recaptchaForOneTimeAccess = recaptchaForOneTimeAccess,
                baseUrl = baseUrl,
                baseSalesUrl = baseSalesUrl,
                baseRetailUrl = baseRetailUrl,
                useLocalConfig = useLocalConfig,
                pwMinLength = pwMinLength,
                pwSpecialCharacters = pwSpecialCharacters,
                pwUppercase = pwUppercase,
                pwLowercase = pwLowercase,
                userNameIsEmail = userNameIsEmail,
                pwPwNumbers = pwPwNumbers,
                googleOneTapEnabled = googleOneTapEnabled,
                googleOneTapAutoLoginEnabled = googleOneTapAutoLoginEnabled,
                autoCache = autoCache,
                useCachedPaywall = useCachedPaywall
            )
        }
    }

}