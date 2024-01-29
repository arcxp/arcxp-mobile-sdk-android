package com.arcxp.content.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RawQuery
import androidx.sqlite.db.SupportSQLiteQuery
import java.util.Date

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
    @Query("SELECT * FROM jsonItem where uuid = :uuid")
    suspend fun getJsonById(uuid: String): JsonItem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJsonItem(jsonItem: JsonItem)

    @Query("DELETE FROM jsonItem where uuid = :uuid")
    suspend fun deleteJsonItemById(uuid: String)

    /**
     * [getCollectionIndexedJson] returns a collection with a list of data objects
     * containing <index, JsonItem> entries
     */
    @Query(
        """
        SELECT collectionItem.indexValue, jsonItem.jsonResponse FROM collectionItem 
        JOIN jsonItem ON collectionItem.uuid = jsonItem.uuid
        where collectionItem.contentAlias = :collectionAlias 
        AND indexValue >= :from 
       /* AND indexValue <= (:size + :from) Don't think we need this */ 
        ORDER BY indexValue LIMIT :size
    """
    )
    suspend fun getCollectionIndexedJson(
        collectionAlias: String,
        from: Int,
        size: Int
    ): List<IndexedJsonItem>

    @Query(
        """
        SELECT MIN(collectionItem.expiresAt) FROM collectionitem 
        WHERE collectionItem.contentAlias = :collectionAlias
    """
    )
    suspend fun getCollectionExpiration(collectionAlias: String): Date?


    @Query("SELECT * FROM collectionItem")
    suspend fun getCollections(): List<CollectionItem?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCollectionItem(collectionItem: CollectionItem)

    @Query("DELETE FROM collectionItem where contentAlias = :contentAlias")
    suspend fun deleteCollectionItemByContentAlias(contentAlias: String)

    @Query("DELETE FROM collectionItem where contentAlias = :contentAlias AND indexValue = :indexValue")
    suspend fun deleteCollectionItemByIndex(contentAlias: String, indexValue: Int)

    @Query("DELETE FROM jsonItem WHERE createdAt IN (SELECT createdAt FROM jsonItem ORDER BY createdAt ASC LIMIT 1)")
    suspend fun deleteOldestJsonItem()

    @Query("DELETE FROM collectionitem WHERE createdAt IN (SELECT createdAt FROM collectionitem ORDER BY createdAt ASC LIMIT 1)")
    suspend fun deleteOldestCollectionItem()

    @Query(
        """
        SELECT
            (SELECT COUNT(*) FROM jsonItem) +
            (SELECT COUNT(*) FROM collectionItem) AS totalItemCount;
        """
    )
    fun countItems(): Int

    @Query("SELECT COUNT(uuid) from jsonItem")
    fun countJsonItems(): Int

    @Query("SELECT COUNT(uuid) from collectionItem")
    fun countCollectionItems(): Int

    @RawQuery
    fun vacuumDb(supportSQLiteQuery: SupportSQLiteQuery): Int

    @RawQuery
    fun walCheckPoint(supportSQLiteQuery: SupportSQLiteQuery): Int

    data class IndexedJsonItem(val indexValue: Int, val jsonResponse: String)
}