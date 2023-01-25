package com.arcxp.content.util

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

    const val CONTENT_PREFERENCE = "CONTENT_PREFERENCE"
    const val ENVIRONMENT_PREFERENCES = "ENVIRONMENT_PREFS"
    const val PAYWALL_PREFERENCES = "PAYWALL_PREFS"
    const val PAYWALL_PREFS_RULES = "PAYWALL_PREFS_RULES"

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

    const val THUMBNAIL_RESIZE_URL_KEY = "thumbnailResizeUrl"

    const val RESIZE_URL_KEY = "resizeUrl"

    const val THUMBNAIL_SIZE = 300

    const val REGION = "REGION"

    const val DEFAULT_REGION = "us-east-1"

    const val REMEMBER_USER = "REMEMBER_USER"

    const val SERVER_ENVIRONMENT = "staging"

    const val SERVER_ORG = "arctesting1"

    const val SERVER_SITE = "config"

    const val ORG_NAME = "orgName"

    const val SITE_NAME = "site"

    const val ENVIRONMENT = "environment"

    const val ANALYTICS = "analytics"

    const val PENDING_ANALYTICS = "pendingAnalytics"

    const val DEVICE_ID = "deviceID"

    const val GET_COLLECTION = "getCollection"
    const val GET_STORY = "getStory"
    const val SEARCH_KEYWORDS = "searchByKeywords"
    const val SEARCH_TAGS = "searchByTags"
    const val GET_GALLERY = "getGallery"
    const val GET_SECTION_LIST = "getSectionsList"

    // cache defaults
    const val DEFAULT_CACHE_SIZE_MB = 120
    val VALID_CACHE_SIZE_RANGE_MB = 10..1024
    const val CACHE_TIME_UNTIL_UPDATE_MIN = 1
    const val DEFAULT_PRELOADING = true
    const val DEFAULT_PAGINATION_SIZE = 20
    val VALID_COLLECTION_SIZE_RANGE = 1..20

}
