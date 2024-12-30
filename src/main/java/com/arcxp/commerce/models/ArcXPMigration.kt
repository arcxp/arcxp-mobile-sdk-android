package com.arcxp.commerce.models

import androidx.annotation.Keep

@Keep
data class ArcXPMigration(
    val records: ArcXPMigrationResponseRecord,
    val time: Int,
    val errorCount: Int,
    val successCount: Int
) {
}