package com.arcxp.content.db

import android.app.Application
import com.arcxp.content.ArcXPContentSDK
import com.arcxp.content.util.DependencyFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * @suppress
 * This class is responsible for all database access
 *
 * @property application Application context
 * @property database Our database instance
 * @property mIoScope Scope for db operations
 */
class CacheManager(
    private val application: Application,
    private val database: Database,
    private val mIoScope: CoroutineScope = DependencyFactory.createIOScope()
) {

    private val maxSizeBytes =
        ArcXPContentSDK.arcxpContentConfig().cacheSizeMB/*mb*/ * 1024 /*kb*/ * 1024 /*bytes*/
    private val dao = database.sdkDao()

    private fun getDBSize() = (application.getDatabasePath("database")
        .length() // Add the shared memory (WAL index) file size
            + application.getDatabasePath("database-shm").length() // Add the WAL file size
            + application.getDatabasePath("database-wal").length() // Add the journal file size
            + application.getDatabasePath("database-journal").length())

    //crud operations from dao (just pass through to dao)
    suspend fun getCollectionById(id: String, from: Int, size: Int) =
        dao.getCollectionById(id = id, from = from, size = size)

    suspend fun getCollections() = dao.getCollections()
    suspend fun getSectionList() = dao.getSectionList()
    suspend fun insertNavigation(sectionHeaderItem: SectionHeaderItem) =
        dao.insertNavigation(sectionHeaderItem)

    suspend fun getJsonById(id: String) = dao.getJsonById(id = id)
    suspend fun insertJsonItem(jsonItem: JsonItem) {
        dao.insertJsonItem(jsonItem = jsonItem)
        checkPoint()
        while ((getDBSize() > maxSizeBytes) and (dao.countJsonItems() > 0)) {
            deleteOldest()
            checkPoint()
        }
    }

    //    suspend fun deleteJsonItem(jsonItem: JsonItem) = dao.deleteJsonItem(jsonItem = jsonItem)
    suspend fun insertCollectionItem(collectionItem: CollectionItem) =
        dao.insertCollectionItem(collectionItem = collectionItem)

    suspend fun deleteCollectionItemByContentAlias(id: String) =
        dao.deleteCollectionItemByContentAlias(contentAlias = id)

    suspend fun deleteCollectionItemByIndex(id: String, index: Int) =
        dao.deleteCollectionItemByIndex(id = id, index = index)

    private suspend fun deleteOldest() = dao.deleteOldestJsonItem()
    fun jsonCount() = dao.countJsonItems()
    fun vac() =
        mIoScope.launch { dao.vacuumDb(supportSQLiteQuery = DependencyFactory.vacuumQuery()) }

    private fun checkPoint() =
        dao.walCheckPoint(supportSQLiteQuery = DependencyFactory.checkPointQuery())


    //so if a current section header result does not have an id currently cached
    //then we consider that item as old and purge
    //with this, we only consider cache size with the jsonItem table
    //this will serve to auto-minimize the collection responses cached
    //so we only cache responses from the current section headers

    //note we ignore the video collection, since that should not change
    //but if it does, and the collection is not returned from site service
    //they will be considering stale and purged here along with any other old section header

    suspend fun minimizeCollections(newCollectionAliases: Set<String>) {
        // for all collections in db,
        // gather to a set only the content aliases from each,
        // filter set for entries not in provided new list (stale),
        // ignoring video collection
        // for each of these remaining stale contentAliases
        // delete any matching entry in db
        val staleIds =
            getCollections().map { it?.contentAlias }.toSet()
                .filter { !newCollectionAliases.contains(it) }.toMutableSet()
        staleIds.remove(ArcXPContentSDK.arcxpContentConfig().videoCollectionName)
        for (id in staleIds) {
            id?.let {
                deleteCollectionItemByContentAlias(it)
            }
        }
    }
}