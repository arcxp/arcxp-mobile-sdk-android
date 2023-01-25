package com.arcxp.content.util

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.room.Room
import androidx.sqlite.db.SimpleSQLiteQuery
import com.arcxp.content.ArcXPContentManager
import com.arcxp.content.ArcXPLogger
import com.arcxp.content.ArcXPResizer
import com.arcxp.content.apimanagers.ContentApiManager
import com.arcxp.content.db.CacheManager
import com.arcxp.content.db.Database
import com.arcxp.content.models.ArcXPContentError
import com.arcxp.content.models.ArcXPContentSDKErrorType
import com.arcxp.content.repositories.ContentRepository
import com.arcxp.content.retrofit.RetrofitController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/**
 * @suppress
 */
object DependencyFactory {


    // this creates arcxp content manager, repository and database
    fun createArcXPContentManager(application: Application) = ArcXPContentManager(
        application = application,
        contentRepository = createContentRepository(application = application)
    )

    fun createArcXPResizer(application: Application, baseUrl: String) = ArcXPResizer(application, baseUrl)

    fun createArcXPLogger(application: Application, organization: String, environment: String, site: String) = ArcXPLogger(application, organization, environment, site)

    private fun createDb(application: Application) = Room.databaseBuilder(
        application,
        Database::class.java, "database"
    ).build()

    fun createContentRepository(application: Application): ContentRepository {
        val cacheManager = CacheManager(application = application, database = createDb(application = application))
        cacheManager.vac() //rebuilds the database file, repacking it into a minimal amount of disk space
        return ContentRepository(cacheManager = cacheManager)
    }

    fun createContentApiManager() = ContentApiManager()

    fun createError(
        message: String,
        type: ArcXPContentSDKErrorType = ArcXPContentSDKErrorType.SERVER_ERROR
    ) = ArcXPContentError(
        type = type,
        message = message
    )

    fun buildVersionUtil(): BuildVersionProvider = BuildVersionProviderImpl()

    fun createIOScope() = CoroutineScope(context = Dispatchers.IO + SupervisorJob())

    fun createContentService() = RetrofitController.getContentService()
    fun createNavigationService() = RetrofitController.navigationService()

    fun <T>createLiveData() = MutableLiveData<T>()

    fun vacuumQuery() = SimpleSQLiteQuery("VACUUM")
    fun checkPointQuery() = SimpleSQLiteQuery("pragma wal_checkpoint(full)")
}