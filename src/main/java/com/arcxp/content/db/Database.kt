package com.arcxp.content.db

import androidx.room.Database
import androidx.room.RoomDatabase

/**
 * @suppress
 */
@Database(
    entities = [CollectionItem::class, SectionHeaderItem::class, JsonItem::class],
    version = 1
)
abstract class Database : RoomDatabase() {
    abstract fun sdkDao(): ContentSDKDao
}
