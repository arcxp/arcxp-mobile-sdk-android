package com.arcxp.content.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase


/**
 * @suppress
 */
@Database(
    entities = [CollectionItem::class, SectionHeaderItem::class, JsonItem::class],
    version = 2
)
@TypeConverters(DateConverter::class)
abstract class Database : RoomDatabase() {
    abstract fun sdkDao(): ContentSDKDao


}

val MIGRATION_1_2: Migration = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Since we didn't alter the table, there's nothing else to do here.

        //for table collection:
//        @ColumnInfo val indexValue: Int, //given current collection ordering, this is item at index, 0 is top etc
//        @ColumnInfo val contentAlias: String,
//        @ColumnInfo val uuid: String,
//        @ColumnInfo override val createdAt: Date = Utils.createDate(),
//        @ColumnInfo override val expiresAt: Date
//        ): BaseItem(createdAt, expiresAt) {
//            @PrimaryKey var internalId: String = "$contentAlias-$indexValue"
        // Create new table with the desired schema
        database.execSQL("CREATE TABLE new_CollectionItems (uuid TEXT, indexValue INTEGER, contentAlias TEXT, createdAt DATE, expiresAt DATE, internalId TEXT, PRIMARY KEY(internalId)")
        database.execSQL("INSERT INTO new_CollectionItems (uuid, username, createdAt, expiresAt, internalId) SELECT id, username, createdAt, expiresAt, internalId FROM collectionItems")
        database.execSQL("DROP TABLE collectionItems")
        database.execSQL("ALTER TABLE new_CollectionItems RENAME TO collectionItems")
//        @Entity(indices = [Index(value = ["uuid"], unique = true)])
//        @TypeConverters(DateConverter::class)
//        data class JsonItem(
//            @PrimaryKey val uuid: String, //ans uuid
//            @ColumnInfo val jsonResponse: String, // raw json
//            @ColumnInfo override val createdAt: Date = Utils.createDate(),
//            @ColumnInfo override val expiresAt: Date
//        ): BaseItem(createdAt, expiresAt)
        //for table json
        database.execSQL("CREATE TABLE new_JsonItems (uuid TEXT, jsonResponse TEXT, createdAt DATE, expiresAt DATE,  PRIMARY KEY(uuid)")
        database.execSQL("INSERT INTO new_JsonItems (uuid, jsonResponse, createdAt, expiresAt) SELECT id, jsonResponse, createdAt, expiresAt FROM jsonItems")
        database.execSQL("DROP TABLE jsonItems")
        database.execSQL("ALTER TABLE new_JsonItems RENAME TO jsonItems")

    }
}
