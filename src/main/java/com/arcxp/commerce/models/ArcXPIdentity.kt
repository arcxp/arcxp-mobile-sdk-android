package com.arcxp.commerce.models

import androidx.annotation.Keep

/**
 * @suppress
 */
@Keep
data class ArcXPIdentity(
    val createdOn: String,
    val createdBy: String,
    val modifiedOn: String,
    val modifiedBy: String,
    val deletedOn: String,
    val id: Int,
    val userName: String,
    val passwordReset: Boolean,
    val type: String,
    val lastLoginDate: String,
    val locked: Boolean
) {

    companion object {
        enum class Type {
            Password, Twitter, Facebook, Google, DummyLoginSuccess, DummyLoginFailure, DummyLoginSuccess2, Apple
        }
    }
}