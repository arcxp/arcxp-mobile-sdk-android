package com.arcxp.commerce.models

import androidx.annotation.Keep

@Keep
data class ArcXPTaskDeclineRequest(
    val reason: String,
    val notes: String
) {
    companion object {
        enum class Reason {
            MISTAKE, CHANGED_MIND, OTHER
        }
    }
}