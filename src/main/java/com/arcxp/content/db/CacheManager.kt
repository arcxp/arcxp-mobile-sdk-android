package com.arcxp.content.db

import android.app.Application
import androidx.media3.common.util.UnstableApi
import com.arcxp.ArcXPMobileSDK.contentConfig
import com.arcxp.commons.util.DependencyFactory
import com.arcxp.commons.util.DependencyFactory.createIOScope
import com.arcxp.commons.util.MoshiController.fromJson
import com.arcxp.content.extendedModels.ArcXPContentElement
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.Date

/**
 * @suppress
 * This class is responsible for all database access
 *
 * @property application Application context
 * @property database Our database instance
 * @property mIoScope Scope for db operations
 */
@UnstableApi
class CacheManager(
    private val application: Application,
    private val database: Database,
    private val mIoScope: CoroutineScope = createIOScope()
) {

    private val maxSizeBytes =
        contentConfig().cacheSizeMB/*mb*/ * 1024 /*kb*/ * 1024 /*bytes*/
    private val dao = database.sdkDao()

    private fun getDBSize() = (application.getDatabasePath("database")
        .length() // Add the shared memory (WAL index) file size
            + application.getDatabasePath("database-shm").length() // Add the WAL file size
            + application.getDatabasePath("database-wal").length() // Add the journal file size
            + application.getDatabasePath("database-journal").length())

    suspend fun getCollections() = dao.getCollections()
    suspend fun getSectionList() = dao.getSectionList()
    suspend fun insertNavigation(sectionHeaderItem: SectionHeaderItem) =
        dao.insertNavigation(sectionHeaderItem)

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

    fun vac() =
        mIoScope.launch { dao.vacuumDb(supportSQLiteQuery = DependencyFactory.vacuumQuery()) }

    private fun checkPoint() =
        dao.walCheckPoint(supportSQLiteQuery = DependencyFactory.checkPointQuery())


    /**
     * [getCollectionJson] returns a collection map<index, String> entry as
     */
    suspend fun getCollectionJson(
        collectionAlias: String,
        from: Int,
        size: Int
    ) = dao.getCollectionIndexedJson(collectionAlias, from, size).associate { it.indexValue to it.jsonResponse }

    /**
     * [getCollection] returns a collection map<index, ArcXPContentElement> entry
     */
    suspend fun getCollection(
        collectionAlias: String,
        from: Int,
        size: Int
    ) = dao.getCollectionIndexedJson(collectionAlias, from, size).associate { it.indexValue to fromJson(
        it.jsonResponse,
        ArcXPContentElement::class.java
    )!! }


    suspend fun getCollectionExpiration(collectionAlias: String): Date? = dao.getCollectionExpiration(collectionAlias)
    fun deleteCollection(collectionAlias: String) = mIoScope.launch { dao.deleteCollection(collectionAlias = "/$collectionAlias") }
    fun deleteAll() = mIoScope.launch {
        dao.deleteJsonTable()
        dao.deleteCollectionTable()
        dao.deleteSectionHeaderTable()
    }
    fun deleteItem(uuid:String) = mIoScope.launch { dao.deleteJsonItem(uuid = uuid) }
}