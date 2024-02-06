package com.arcxp.content.db

import androidx.media3.common.util.UnstableApi
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters


/**
 * @suppress
 */
@UnstableApi
@Database(
    entities = [CollectionItem::class, SectionHeaderItem::class, JsonItem::class],
    version = 2
)
@TypeConverters(DateConverter::class)
abstract class Database : RoomDatabase() {
    abstract fun sdkDao(): ContentSDKDao
}
