package com.arcxp.content.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters


/**
 * @suppress
 */
@Database(
    entities = [CollectionItem::class, SectionHeaderItem::class, JsonItem::class],
    version = 3
)
@TypeConverters(DateConverter::class)
abstract class Database : RoomDatabase() {
    abstract fun sdkDao(): ContentSDKDao
}
