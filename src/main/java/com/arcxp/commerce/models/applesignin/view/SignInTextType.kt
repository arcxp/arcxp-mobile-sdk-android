package com.arcxp.commerce.models.applesignin.view

import androidx.annotation.StringRes
import com.arcxp.sdk.R

internal enum class SignInTextType(@StringRes val text: Int) {
    SIGN_IN(R.string.sign_in_with_apple_button_signInWithApple),
    CONTINUE(R.string.sign_in_with_apple_button_continueWithApple)
}