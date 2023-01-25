package com.arcxp.commerce.models

import androidx.annotation.Keep

/**
 * @suppress
 */
@Keep
data class ArcXPUser(
    val uuid: String,
    val userStates: Boolean,
    val identities: List<ArcXPIdentity>,
    val profile: ArcXPProfile
)