package com.arcxp.content.db

import android.app.Application
import android.util.Log
import com.arcxp.ArcXPMobileSDK.contentConfig
import com.arcxp.commons.util.DependencyFactory
import com.arcxp.commons.util.DependencyFactory.createIOScope
import com.arcxp.commons.util.MoshiController.fromJson
import com.arcxp.commons.util.Utils.constructJsonArray
import com.arcxp.content.extendedModels.ArcXPContentElement
import com.arcxp.sdk.R
import com.arcxp.video.util.TAG
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.Date

/**
 * This class is responsible for all database access
 *
 * @property application Application context
 * @property database Our database instance
 * @property mIoScope Scope for db operations
 */
class CacheManager(
    private val application: Application,
    private val database: Database,
    private val mIoScope: CoroutineScope = createIOScope()
) {
    private val maxSizeBytes =
        contentConfig().cacheSizeMB/*mb*/ * 1024 /*kb*/ * 1024 /*bytes*/
    private val dao = database.sdkDao()

    init {
        mIoScope.launch {
            vac()//rebuilds the database file, repacking it into a minimal amount of disk space
        }
    }

    private fun getDBSize() = (application.getDatabasePath("database")
        .length() // Add the shared memory (WAL index) file size
            + application.getDatabasePath("database-shm").length() // Add the WAL file size
            + application.getDatabasePath("database-wal").length() // Add the journal file size
            + application.getDatabasePath("database-journal").length())

    suspend fun getCollections() = dao.getCollections()
    suspend fun getSectionList(siteHierarchy: String) =
        dao.getSectionList(siteHierarchy = siteHierarchy)

    suspend fun insertNavigation(sectionHeaderItem: SectionHeaderItem) =
        dao.insertSectionList(sectionHeaderItem)

    suspend fun getJsonById(uuid: String) = dao.getJsonById(uuid = uuid)

    suspend fun insert(collectionItem: CollectionItem? = null, jsonItem: JsonItem) {
        collectionItem?.let { dao.insertCollectionItem(collectionItem = it) }
        dao.insertJsonItem(jsonItem = jsonItem)
        checkPoint()
        while ((getDBSize() > maxSizeBytes) and (dao.countItems() > 0)) {
            dao.deleteOldestCollectionItem()
            dao.deleteOldestJsonItem()
            checkPoint()
        }
    }

    private suspend fun vac() = dao.vacuumDb(supportSQLiteQuery = DependencyFactory.vacuumQuery())

    private fun checkPoint() =
        dao.walCheckPoint(supportSQLiteQuery = DependencyFactory.checkPointQuery())

    /**
     * [getCollection] returns a collection map<index, ArcXPContentElement> entry
     */
    suspend fun getCollection(
        collectionAlias: String,
        from: Int,
        size: Int
    ) = dao.getCollectionIndexedJson(collectionAlias, from, size).mapNotNull {
        try {
            it.indexValue to
                    fromJson(
                        it.jsonResponse,
                        ArcXPContentElement::class.java
                    )!!
        } catch (e: Exception) {
            Log.e(
                TAG,
                application.getString(
                    R.string.get_collection_deserialization_failure_message,
                    e.message
                ),
                e
            )
            return@mapNotNull null
        }
    }.toMap()

    /**
     * [getCollectionAsJson] returns a collection String entry, or empty if nothing in db
     */
    suspend fun getCollectionAsJson(
        collectionAlias: String,
        from: Int,
        size: Int
    ): String {
        var jsonArray = ""
        val strings = dao.getCollectionIndexedJson(collectionAlias, from, size)
        if (strings.isNotEmpty()) {
            jsonArray =
                constructJsonArray(jsonStrings = strings.map { indexedJsonItem -> indexedJsonItem.jsonResponse }
                    .toList())
        }
        return jsonArray

    }


    suspend fun getCollectionExpiration(collectionAlias: String): Date? =
        dao.getCollectionExpiration(collectionAlias)

    fun deleteCollection(collectionAlias: String) =
        mIoScope.launch { dao.deleteCollection(collectionAlias = "/$collectionAlias") }

    fun deleteAll() = mIoScope.launch {
        dao.deleteJsonTable()
        dao.deleteCollectionTable()
        dao.deleteSectionHeaderTable()
    }

    fun deleteItem(uuid: String) = mIoScope.launch { dao.deleteJsonItem(uuid = uuid) }
}