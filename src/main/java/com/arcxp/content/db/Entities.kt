package com.arcxp.content.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.arcxp.commons.util.Utils.createDate
import java.util.Date

/**
 * @suppress
 */
@Entity(indices = [Index(value = ["internalId"], unique = true)])
@TypeConverters(DateConverter::class)
data class CollectionItem(
    @ColumnInfo val indexValue: Int, //given current collection ordering, this is item at index, 0 is top etc
    @ColumnInfo val contentAlias: String,
    @ColumnInfo val uuid: String,
    @ColumnInfo override val createdAt: Date = createDate(),
    @ColumnInfo override val expiresAt: Date
): BaseItem(createdAt, expiresAt) {
    @PrimaryKey var internalId: String = "$contentAlias-$indexValue"
}

/**
 * @suppress
 */
@Entity(indices = [Index(value = ["id"], unique = true)])
@TypeConverters(DateConverter::class)
data class SectionHeaderItem(
    @PrimaryKey val id: Int = 1, //so we overwrite each time and thus only cache only one instance of this (do not use this optional parameter with another value)
    @ColumnInfo val sectionHeaderResponse: String, //this should be section header response? json
    @ColumnInfo override val createdAt: Date = createDate(),
    @ColumnInfo override val expiresAt: Date
): BaseItem(createdAt, expiresAt)

/**
 * @suppress
 */
@Entity(indices = [Index(value = ["uuid"], unique = true)])
@TypeConverters(DateConverter::class)
data class JsonItem(
    @PrimaryKey val uuid: String, //ans uuid
    @ColumnInfo val jsonResponse: String, // raw json
    @ColumnInfo override val createdAt: Date = createDate(),
    @ColumnInfo override val expiresAt: Date
): BaseItem(createdAt, expiresAt)

/**
 * @suppress
 */
abstract class BaseItem(
    open val createdAt: Date = createDate(),
    open val expiresAt: Date
)
