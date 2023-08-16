package com.arcxp.commerce.models

import androidx.annotation.Keep


/**
 * @suppress
 */
@Keep
data class ArcXPAttribute(
    val name: String,
    var value: String,
    val type: String
) {

    companion object {
        enum class Type {
            String, Number, Boolean, Date
        }
    }
}