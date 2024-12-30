package com.arcxp.commerce.models

import androidx.annotation.Keep

@Keep
data class ArcXPAttributeRequest(
    val name: String,
    val value: String,
    val type: String
) {
    companion object {
        enum class Type {
            String, Number, Boolean, Date
        }
    }
}