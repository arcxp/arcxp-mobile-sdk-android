package com.arcxp.commerce.models

import androidx.annotation.Keep

@Keep
data class ArcXPTaskRequest(
    val createdOn: String,
    val createdBy: String,
    val modifiedOn: String,
    val modifiedBy: String,
    val deletedOn: String,
    val id: String,
    val startTime: String,
    val data: String,
    val taskId: String,
    val taskName: String,
    val status: String,
    //TODO: val detail: Detail,
    val reason: String,
    val notes: String
) {
    companion object {
        enum class Status {
            PendingApproval, Ready, InProgress, Completed, Error, Declined
        }
        enum class Reason {
            MISTAKE, CHANGED_MIND, OTHER
        }
    }

}