package com.arcxp.commerce.models.applesignin

data class SignInWithAppleConfiguration(
    val clientId: String,
    val redirectUri: String,
    val scope: String,
    val arcAuthUrl: String,
    val authTokenUrl: String
)
