package com.arcxp.commerce.util

/**
 * @suppress
 */
object Constants {

    const val ZERO = 0
    const val ONE = 1
    const val TWO = 2
    const val THREE = 3
    const val FOUR = 4

    const val TIMEOUT = 60L

    const val SUBSCRIPTION_PREFERENCE = "SUBSCRIPTION_PREFERENCE"
    const val ENVIRONMENT_PREFERENCES = "ENVIRONMENT_PREFS"
    const val PAYWALL_PREFERENCES = "PAYWALL_PREFS"
    const val PAYWALL_PREFS_RULES_DATA = "PAYWALL_PREFS_RULE_DATA"
    const val PAYWALL_RULES = "PAYWALL_RULES"
    const val ENTITLEMENTS = "ENTITLEMENTS"

    const val CUR_ENVIRONMENT = "CUR_ENVIRONMENT"
    const val ENV_SAND_BOX = "ENV_SAND_BOX"
    const val ENV_STAGING = "ENV_STAGING"

    const val USER_UUID = "USER_UUID"
    const val CACHED_ACCESS_TOKEN = "CACHED_ACCESS_TOKEN"
    const val CACHED_REFRESH_TOKEN = "CACHED_REFRESH_TOKEN"

    const val USER_CONFIG = "USER_CONFIG"

    const val BEARER = "Bearer "

    const val DEV = "dev"
    const val QA = "qa"

    enum class EnvironmentType {
        ENV_SAND_BOX,
        ENV_STAGING
    }

    const val REGION = "REGION"

    const val DEFAULT_REGION = "us-east-1"

    const val REMEMBER_USER = "REMEMBER_USER"

    const val SERVER_ENVIRONMENT = "staging"

    const val SERVER_ORG = "arctesting1"

    const val SERVER_SITE = "config"

}
