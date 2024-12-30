package com.arcxp.commerce.models

import androidx.annotation.Keep

@Keep
data class ArcXPConfig(
    val facebookAppId: String?,
    val googleClientId: String?,
    val orgTenants: List<OrgTenants>?,
    val signupRecaptcha: Boolean?,
    val signinRecaptcha: Boolean?,
    val magicLinkRecapatcha: Boolean?,
    val recaptchaSiteKey: String?,
    val pwMinLength: Int?,
    val pwSpecialCharacters: Int?,
    val pwUppercase: Int?,
    val pwLowercase: Int?,
    val disqus: ArcXPDisqus?,
    val teamId: String?,
    val keyId: String?,
    val urlToReceiveAuthToken: String?,
    val pwPwNumbers: Int?
)

@Keep
data class OrgTenants(
    val name: String,
    val orgHeader: String,
    val siteHeader: String,
    val domain: String
)