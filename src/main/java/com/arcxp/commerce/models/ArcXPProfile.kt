package com.arcxp.commerce.models

import androidx.annotation.Keep

@Keep
data class ArcXPProfile(
    val createdOn: String,
    val createdBy: String,
    val modifiedOn: String,
    val modifiedBy: String,
    val deletedOn: String,
    val firstName: String,
    val lastName: String,
    val secondLastName: String,
    val displayName: String,
    val gender: String,
    val email: String,
    val unverifiedEmail: String,
    val picture: String,
    val birthYear: String,
    val birthMonth: String,
    val birthDay: String,
    val legacyId: String,
    val contacts: List<ArcXPContact>,
    val addresses: List<ArcXPAddress>,
    val attributes: List<ArcXPAttribute>,
    val identities: List<ArcXPIdentity>,
    val deletionRules: Int,
    val profileNotificationEventResponse: ArcXPProfileNotificationEvent
) {
}