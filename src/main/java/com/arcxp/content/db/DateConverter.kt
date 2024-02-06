package com.arcxp.content.db

import androidx.media3.common.util.UnstableApi
import androidx.room.TypeConverter
import com.arcxp.commons.util.Utils.createDate
import java.util.*

/**
 * @suppress
 */
@UnstableApi
class DateConverter {

    @TypeConverter
    fun toDate(timestamp: Long?): Date? {
        return timestamp?.let { createDate(it) }
    }

    @TypeConverter
    fun toTimestamp(date: Date?): Long? {
        return date?.time
    }
}