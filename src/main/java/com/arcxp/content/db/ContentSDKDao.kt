package com.arcxp.content.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RawQuery
import androidx.sqlite.db.SupportSQLiteQuery
import java.util.Date

/**
 * ContentSDKDao is an interface that defines the data access operations for the ArcXP Commerce module.
 * It provides methods to interact with the database, including querying, inserting, and deleting content-related data.
 *
 * The interface defines the following operations:
 * - Retrieve section lists and JSON items by ID
 * - Insert section lists and JSON items
 * - Delete JSON items and collections by ID or alias
 * - Retrieve collections of content items with indexed JSON entries
 * - Retrieve the expiration date of a collection
 * - Count the total number of items in the database
 * - Perform database maintenance operations like vacuuming and checkpointing
 *
 * Usage:
 * - Implement this interface to define the database operations for content management.
 *
 * Example:
 *
 * val sectionList = contentSDKDao.getSectionList("siteHierarchy")
 * contentSDKDao.insertJsonItem(jsonItem)
 * contentSDKDao.deleteJsonItemById("uuid")
 *
 * Note: Ensure that the database and its entities are properly configured before using ContentSDKDao.
 *
 * @method getSectionList Retrieve the section list for a given site hierarchy.
 * @method insertSectionList Insert a section list into the database.
 * @method getJsonById Retrieve a JSON item by its UUID.
 * @method insertJsonItem Insert a JSON item into the database.
 * @method deleteJsonItemById Delete a JSON item by its UUID.
 * @method getCollectionIndexedJson Retrieve a collection of content items with indexed JSON entries.
 * @method getCollectionExpiration Retrieve the expiration date of a collection.
 * @method getCollections Retrieve all collections.
 * @method insertCollectionItem Insert a collection item into the database.
 * @method deleteCollection Delete a collection by its alias.
 * @method deleteJsonItem Delete a JSON item by its UUID.
 * @method deleteJsonTable Delete all JSON items.
 * @method deleteCollectionTable Delete all collection items.
 * @method deleteSectionHeaderTable Delete all section header items.
 * @method deleteOldestJsonItem Delete the oldest JSON item.
 * @method deleteOldestCollectionItem Delete the oldest collection item.
 * @method countItems Count the total number of items in the database.
 * @method countJsonItems Count the number of JSON items in the database.
 * @method countCollectionItems Count the number of collection items in the database.
 * @method vacuumDb Perform a VACUUM operation on the database.
 * @method walCheckPoint Perform a WAL checkpoint operation on the database.
 */
@Dao
interface ContentSDKDao {

    // raw json of section header list, so we can use same caching pattern
    // this should only insert/query for 1 item (the current section header list)

    @Query("SELECT * FROM sectionHeaderItem where siteHierarchy = :siteHierarchy")
    suspend fun getSectionList(siteHierarchy: String): SectionHeaderItem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSectionList(sectionHeaderItem: SectionHeaderItem)

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
        where collectionItem.collectionAlias = :collectionAlias 
        AND indexValue >= :from 
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
        WHERE collectionItem.collectionAlias = :collectionAlias
        """
    )
    suspend fun getCollectionExpiration(collectionAlias: String): Date?


    @Query("SELECT * FROM collectionItem")
    suspend fun getCollections(): List<CollectionItem?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCollectionItem(collectionItem: CollectionItem)

    @Query("DELETE FROM collectionItem where collectionAlias = :collectionAlias")
    suspend fun deleteCollection(collectionAlias: String)

    @Query("DELETE FROM jsonitem where uuid = :uuid")
    suspend fun deleteJsonItem(uuid: String)

    @Query("DELETE FROM jsonItem")
    suspend fun deleteJsonTable()

    @Query("DELETE FROM collectionitem")
    suspend fun deleteCollectionTable()

    @Query("DELETE FROM sectionHeaderItem")
    suspend fun deleteSectionHeaderTable()

    @Query("DELETE FROM jsonItem WHERE createdAt IN (SELECT createdAt FROM jsonItem ORDER BY createdAt ASC LIMIT 1)")
    suspend fun deleteOldestJsonItem()

    @Query("DELETE FROM collectionitem WHERE createdAt IN (SELECT createdAt FROM collectionitem ORDER BY createdAt ASC LIMIT 1)")
    suspend fun deleteOldestCollectionItem()

    @Query(
        """
        SELECT
            (SELECT COUNT(*) FROM jsonItem) +
            (SELECT COUNT(*) FROM sectionheaderitem) +
            (SELECT COUNT(*) FROM collectionItem) AS totalItemCount;
        """
    )
    fun countItems(): Int

    @Query("SELECT COUNT(uuid) from jsonItem")
    fun countJsonItems(): Int

    @Query("SELECT COUNT(uuid) from collectionItem")
    fun countCollectionItems(): Int

    @RawQuery
    suspend fun vacuumDb(supportSQLiteQuery: SupportSQLiteQuery): Int

    @RawQuery
    fun walCheckPoint(supportSQLiteQuery: SupportSQLiteQuery): Int

    data class IndexedJsonItem(val indexValue: Int, val jsonResponse: String)
}