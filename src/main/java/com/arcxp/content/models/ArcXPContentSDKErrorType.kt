package com.arcxp.content.models

import androidx.annotation.Keep

@Keep
/**
 * error classifications
 * currently also used for exception typing
 */
enum class ArcXPContentSDKErrorType {
    INIT_ERROR,
    CONFIG_ERROR,
    SERVER_ERROR,
    INVALID_SESSION,
    RECAPTCHA_ERROR,
    LOGIN_ERROR,
    ONE_TIME_ACCESS_LINK_ERROR,
    REGISTRATION_ERROR,
    APPLE_CONFIG_ERROR,
    APPLE_LOGIN_ERROR,
    APPLE_LOGIN_CANCEL,
    FACEBOOK_LOGIN_ERROR,
    FACEBOOK_LOGIN_CANCEL,
    GOOGLE_LOGIN_ERROR,
    GOOGLE_LOGIN_ERROR_NO_ACCOUNT,
    GOOGLE_LOGIN_CANCEL,
    SEARCH_ERROR
}