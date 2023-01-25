package com.arcxp.content.db

import androidx.room.TypeConverter
import java.util.*

/**
 * @suppress
 */
class DateConverter {

    @TypeConverter
    fun toDate(timestamp: Long?): Date? {
        return timestamp?.let { Date(it) }
    }

    @TypeConverter
    fun toTimestamp(date: Date?): Long? {
        return date?.time
    }
}