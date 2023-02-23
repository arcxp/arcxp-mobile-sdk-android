package com.arcxp.content.util

import androidx.lifecycle.MutableLiveData
import androidx.sqlite.db.SimpleSQLiteQuery
import com.arcxp.content.apimanagers.ContentApiManager
import com.arcxp.content.models.ArcXPContentError
import com.arcxp.content.models.ArcXPContentSDKErrorType
import com.arcxp.content.retrofit.RetrofitController

/**
 * @suppress
 */
object DependencyFactory {
    fun createContentApiManager() = ContentApiManager()
    fun createError(
        message: String,
        type: ArcXPContentSDKErrorType = ArcXPContentSDKErrorType.SERVER_ERROR
    ) = ArcXPContentError(
        type = type,
        message = message
    )

    fun buildVersionUtil(): BuildVersionProvider = BuildVersionProviderImpl()
    fun createContentService() = RetrofitController.getContentService()
    fun createNavigationService() = RetrofitController.navigationService()
    fun <T> createLiveData() = MutableLiveData<T>()
    fun vacuumQuery() = SimpleSQLiteQuery("VACUUM")
    fun checkPointQuery() = SimpleSQLiteQuery("pragma wal_checkpoint(full)")
}