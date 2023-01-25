package com.arcxp.content.db

import androidx.room.*
import androidx.sqlite.db.SupportSQLiteQuery

/**
 * @suppress
 */
@Dao
interface ContentSDKDao {

    // raw json of section header list, so we can use same caching pattern
    // this should only insert/query for 1 item (the current section header list)

    @Query("SELECT * FROM sectionHeaderItem where id = 1")
    suspend fun getSectionList(): SectionHeaderItem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNavigation(sectionHeaderItem: SectionHeaderItem)


    // raw json stored by ans id  this should work on all items hopefully
    // and provide json results or model results based on logic in repository layer
    // depending on method called
    @Query("SELECT * FROM jsonItem where id = :id")
    suspend fun getJsonById(id: String): JsonItem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJsonItem(jsonItem: JsonItem)

//    @Delete
//    suspend fun deleteJsonItem(jsonItem: JsonItem)
    @Query("DELETE FROM jsonItem where id = :id")
    suspend fun deleteJsonItemById(id: String)


    // raw collection results stored by collection alias
    @Query("SELECT * FROM collectionItem where contentAlias = :id AND indexValue >= :from AND `indexValue` <= (:size + :from) ORDER BY `indexValue` LIMIT :size")
    suspend fun getCollectionById(id: String, from: Int, size: Int): List<CollectionItem>?// raw collection results stored by collection alias

    @Query("SELECT * FROM collectionItem")
    suspend fun getCollections(): List<CollectionItem?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCollectionItem(collectionItem: CollectionItem)

//    @Delete
//    suspend fun deleteCollectionItem(collectionItem: CollectionItem)
    @Query("DELETE FROM collectionItem where contentAlias = :contentAlias")
    suspend fun deleteCollectionItemByContentAlias(contentAlias: String)


    @Query("DELETE FROM collectionItem where contentAlias = :id AND indexValue = :index")
    suspend fun deleteCollectionItemByIndex(id: String, index: Int)

//    @Query("DELETE FROM collectionItem WHERE id IN (SELECT id FROM collectionItem ORDER BY createdAt ASC LIMIT 1)")
//    suspend fun deleteOldestCollectionItem()
    @Query("DELETE FROM jsonItem WHERE createdAt IN (SELECT createdAt FROM jsonItem ORDER BY createdAt ASC LIMIT 1)")
    suspend fun deleteOldestJsonItem()

    @Query("SELECT COUNT(id) from jsonItem")
    fun countJsonItems(): Int

    @RawQuery
    fun vacuumDb(supportSQLiteQuery: SupportSQLiteQuery): Int

    @RawQuery
    fun walCheckPoint(supportSQLiteQuery: SupportSQLiteQuery): Int
}