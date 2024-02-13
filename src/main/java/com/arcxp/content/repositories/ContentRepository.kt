package com.arcxp.content.repositories

import android.app.Application
import com.arcxp.ArcXPMobileSDK.contentConfig
import com.arcxp.commons.throwables.ArcXPException
import com.arcxp.commons.util.Constants.DEFAULT_PAGINATION_SIZE
import com.arcxp.commons.util.DependencyFactory.createContentApiManager
import com.arcxp.commons.util.DependencyFactory.createIOScope
import com.arcxp.commons.util.Either
import com.arcxp.commons.util.Failure
import com.arcxp.commons.util.MoshiController.fromJson
import com.arcxp.commons.util.Success
import com.arcxp.commons.util.Utils
import com.arcxp.commons.util.Utils.createFailure
import com.arcxp.commons.util.Utils.parseJsonArray
import com.arcxp.content.ArcXPContentConfig
import com.arcxp.content.apimanagers.ContentApiManager
import com.arcxp.content.db.*
import com.arcxp.content.extendedModels.ArcXPContentElement
import com.arcxp.content.extendedModels.ArcXPStory
import com.arcxp.content.models.*
import com.arcxp.content.util.*
import com.arcxp.sdk.R
import kotlinx.coroutines.*
import java.util.*

/**
 * @suppress
 *
 * This is our repository layer abstraction, clients to this class(ArcxpContentManager) can request data and we return via db or api call where appropriate (through callbacks only currently)
 * so this should be considered Single Source of Truth for our data from backend / cache
 * will be in charge of deserializing this data into our data objects to return to calling layer
 */
class ContentRepository(
    private val application: Application,
    private val contentApiManager: ContentApiManager = createContentApiManager(application = application),
    private val mIoScope: CoroutineScope = createIOScope(),
    private val cacheManager: CacheManager
) {


    /**
     * [getCollection] - request collection by content alias
     * @param shouldIgnoreCache if enabled, skips db operation
     * @param collectionAlias searches for this id (first through db if enabled, then api if not or stale)
     * @param from starting index to return results, ie 0 for page 1, 20(size) for page 2
     * @param size number of results to return
     * @return [Either] [ArcXPException] or a map of results ordered by server
     */
    suspend fun getCollection(
        collectionAlias: String,
        shouldIgnoreCache: Boolean = false,
        from: Int,
        size: Int,
        full: Boolean? = null,
    ): Either<ArcXPException, Map<Int, ArcXPContentElement>> {
        return if (shouldIgnoreCache) {
            doCollectionApiCall(
                id = collectionAlias,
                shouldIgnoreCache = true,
                from = from,
                size = size,
                full = full,
            )
        } else {

            val cacheContentElementMap =
                cacheManager.getCollection(
                    collectionAlias = collectionAlias,
                    from = from,
                    size = size
                )

            return if (cacheContentElementMap.isEmpty() || shouldMakeApiCall(
                    cacheManager.getCollectionExpiration(collectionAlias = collectionAlias)
                )
            ) {
                val apiResult = doCollectionApiCall(
                    id = collectionAlias,
                    shouldIgnoreCache = false,
                    from = from,
                    size = size,
                    full = full,
                )
                when {
                    apiResult is Success -> apiResult
                    cacheContentElementMap.isNotEmpty() -> Success(success = cacheContentElementMap)
                    else -> apiResult // returns error since cache is empty
                }
            } else {
                Success(success = cacheContentElementMap)
            }
        }
    }

    /**
     * [getCollectionAsJson] - request collection by content alias
     * @param collectionAlias searches for this id
     * @param from starting index to return results, ie 0 for page 1, 20(size) for page 2
     * @param size number of results to return
     * @param full: [Boolean] should we call collection full? if nothing is entered, will default to [ArcXPContentConfig.preLoading] value
     * @return [Either] Json [String] or [ArcXPException]
     */
    suspend fun getCollectionAsJson(
        collectionAlias: String,
        from: Int,
        size: Int,
        shouldIgnoreCache: Boolean = false,
        full: Boolean? = null
    ): Either<ArcXPException, String> {
        return if (shouldIgnoreCache) {
            when (val response = contentApiManager.getCollection(
                collectionAlias = collectionAlias,
                from = from,
                size = size,
                full = full,
            )) {
                is Success -> Success(success = response.success.first)
                is Failure -> response
            }
        } else {

            val cacheContentJson =
                cacheManager.getCollectionAsJson(
                    collectionAlias = collectionAlias,
                    from = from,
                    size = size
                )

            return if (cacheContentJson.isEmpty() ||
                shouldMakeApiCall(cacheManager.getCollectionExpiration(collectionAlias = collectionAlias))
            ) {
                val apiResult = contentApiManager.getCollection(
                    collectionAlias = collectionAlias,
                    from = from,
                    size = size,
                    full = full
                )
                return when {
                    apiResult is Success -> Success(apiResult.success.first)
                    cacheContentJson.isNotEmpty() -> Success(success = cacheContentJson)
                    else -> Failure((apiResult as Failure).failure) // returns error since cache is empty
                }
            } else {
                Success(success = cacheContentJson)
            }
        }
    }


    /**
     * [searchSuspend] - search does not cache results, so we don't use db, just pass through to api manager
     * @param searchTerm input string to search
     * @param from starting index to return results, ie 0 for page 1, 20(size) for page 2
     * @param size number of results to return
     */
    suspend fun searchSuspend(
        searchTerm: String,
        from: Int = 0,
        size: Int = DEFAULT_PAGINATION_SIZE
    ) = contentApiManager.search(
        searchTerm = searchTerm,
        from = from,
        size = size
    )

    /**
     * [searchAsJsonSuspend] - search does not cache results, so we don't use db, just pass through to api manager
     * @param searchTerm input string to search
     * @param from starting index to return results, ie 0 for page 1, 20(size) for page 2
     * @param size number of results to return
     */
    suspend fun searchAsJsonSuspend(
        searchTerm: String,
        from: Int = 0,
        size: Int = DEFAULT_PAGINATION_SIZE
    ) = contentApiManager.searchAsJson(
        searchTerm = searchTerm,
        from = from,
        size = size
    )

    /**
     * [searchSuspend] - search does not cache results, so we don't use db, just pass through to api manager
     * @param searchTerm input string to search
     * @param from starting index to return results, ie 0 for page 1, 20(size) for page 2
     * @param size number of results to return
     */
    suspend fun searchVideosSuspend(
        searchTerm: String,
        from: Int = 0,
        size: Int = DEFAULT_PAGINATION_SIZE
    ) = contentApiManager.searchVideos(
        searchTerm = searchTerm,
        from = from,
        size = size
    )

    /**
     * [getContent] - request article/story by ANS id
     * @param shouldIgnoreCache if enabled, skips db operation
     * @param uuid searches for this ANS id (first through db if enabled, then api if not or stale)
     */
    suspend fun getContent(
        uuid: String,
        shouldIgnoreCache: Boolean = false
    ): Either<ArcXPException, ArcXPContentElement> {
        return if (shouldIgnoreCache) {
            doContentApiCall(
                id = uuid,
                shouldIgnoreCache = true
            )
        } else {
            val jsonDbItem = cacheManager.getJsonById(uuid = uuid)
            if (shouldMakeApiCall(baseItem = jsonDbItem)) {
                val apiResult = doContentApiCall(
                    id = uuid,
                    shouldIgnoreCache = false
                )
                when {
                    apiResult is Success -> apiResult
                    jsonDbItem != null -> fromJsonCheck(
                        jsonString = jsonDbItem.jsonResponse,
                        classT = ArcXPContentElement::class.java
                    )

                    else -> apiResult
                }
            } else {
                fromJsonCheck(
                    jsonString = jsonDbItem!!.jsonResponse,
                    ArcXPContentElement::class.java
                )
            }
        }
    }

    /**
     * [getStory] - request article/story by ANS id
     * @param shouldIgnoreCache if enabled, skips db operation
     * @param uuid searches for this ANS id (first through db if enabled, then api if not or stale)
     * @return [Either]<[ArcXPException], [ArcXPStory]> will try to deserialize result from json
     */
    suspend fun getStory(
        uuid: String,
        shouldIgnoreCache: Boolean = false
    ): Either<ArcXPException, ArcXPStory> {
        return if (shouldIgnoreCache) {
            doStoryApiCall(
                id = uuid,
                shouldIgnoreCache = true
            )
        } else {
            val jsonDbItem = cacheManager.getJsonById(uuid = uuid)
            if (shouldMakeApiCall(baseItem = jsonDbItem)) {
                val apiResult = doStoryApiCall(
                    id = uuid,
                    shouldIgnoreCache = false
                )
                when {
                    apiResult is Success -> apiResult
                    jsonDbItem != null -> fromJsonCheck(
                        jsonString = jsonDbItem.jsonResponse,
                        classT = ArcXPStory::class.java
                    )

                    else -> apiResult
                }
            } else {
                val story: ArcXPStory?
                try {
                    story = fromJson(
                        jsonDbItem!!.jsonResponse,
                        ArcXPStory::class.java
                    )!!
                } catch (e: Exception) {
                    return createFailure(
                        message = application.getString(
                            R.string.get_story_deserialization_failure_message,
                            e.message
                        ), value = e
                    )
                }
                if (story.content_elements.isNullOrEmpty()) {
                    // story was cached without content elements (no preloading),
                    // so we are returning api call
                    // since cache won't produce a usable story here
                    return doStoryApiCall(
                        id = uuid,
                        shouldIgnoreCache = true
                    )
                } else return Success(story)
            }
        }
    }

    /**
     * [getContentAsJson] - request content element as JSON by ANS id
     * Note this should be a troubleshooting function, does not use cache
     * @param uuid searches for this ANS id (first through db if enabled, then api if not or stale)
     * @param shouldIgnoreCache if enabled, skips db operation
     */
    suspend fun getContentAsJson(
        uuid: String,
        shouldIgnoreCache: Boolean = false
    ): Either<ArcXPException, String> {
        return if (shouldIgnoreCache) {
            doContentJsonApiCall(
                id = uuid,
                shouldIgnoreCache = true
            )
        } else {
            val jsonDbItem = cacheManager.getJsonById(uuid = uuid)
            if (shouldMakeApiCall(baseItem = jsonDbItem)) {
                val apiResult = doContentJsonApiCall(
                    id = uuid,
                    shouldIgnoreCache = false
                )
                when {
                    apiResult is Success -> apiResult
                    jsonDbItem != null -> Success(success = jsonDbItem.jsonResponse)
                    else -> apiResult
                }
            } else {
                Success(success = jsonDbItem!!.jsonResponse)
            }
        }
    }

    /**
     * [getSectionList] - request section lists / navigation
     * @param shouldIgnoreCache if enabled, skips db operation
     */
    suspend fun getSectionList(shouldIgnoreCache: Boolean = false): Either<ArcXPException, List<ArcXPSection>> {
        return if (shouldIgnoreCache) {
            doSectionListApiCall(shouldIgnoreCache = true)
        } else {
            val navigationEntry = cacheManager.getSectionList()
            if (shouldMakeApiCall(baseItem = navigationEntry)) {
                val apiResult = doSectionListApiCall(shouldIgnoreCache = false)
                when {
                    apiResult is Success -> apiResult
                    navigationEntry != null -> navJsonCheck(navJson = navigationEntry.sectionHeaderResponse)
                    else -> apiResult
                }
            } else navJsonCheck(navJson = navigationEntry!!.sectionHeaderResponse)
        }
    }

    /**
     * [getSectionListAsJson] - request section lists / navigation as json string
     * @param shouldIgnoreCache if enabled, skips db operation
     */
    suspend fun getSectionListAsJson(shouldIgnoreCache: Boolean = false): Either<ArcXPException, String> =
        if (shouldIgnoreCache) {
            when (val response = contentApiManager.getSectionList()) {
                is Success -> Success(success = response.success.first)
                is Failure -> response
            }
        } else {
            val databaseSectionList = cacheManager.getSectionList()
            if (shouldMakeApiCall(databaseSectionList)) {
                val response = contentApiManager.getSectionList()
                when {
                    response is Success -> Success(success = response.success.first)
                    databaseSectionList != null -> Success(success = databaseSectionList.sectionHeaderResponse)
                    else -> Failure(failure = (response as Failure).failure)
                }
            } else {
                Success(success = databaseSectionList!!.sectionHeaderResponse)
            }
        }

    private fun insertGeneric(id: String, json: String, expiresAt: Date) {
        mIoScope.launch {
            cacheManager.insert(
                jsonItem = JsonItem(
                    uuid = id,
                    jsonResponse = json,
                    expiresAt = expiresAt
                )
            )
        }
    }

    private fun collectionResponseFromJson(json: String) = fromJson(
        json,
        Array<ArcXPContentElement>::class.java
    )!!.toList()


    private suspend fun doCollectionApiCall(
        id: String,
        shouldIgnoreCache: Boolean,
        from: Int,
        size: Int,
        full: Boolean?
    ): Either<ArcXPException, Map<Int, ArcXPContentElement>> =
        when (val response = contentApiManager.getCollection(
            collectionAlias = id,
            from = from,
            size = size,
            full = full ?: contentConfig().preLoading
        )) {
            is Success -> {
                try {
                    val collectionResultJsonList =
                        response.success.first //full result which is a list
                    val jsonList =
                        parseJsonArray(jsonArrayString = collectionResultJsonList) //list of each result as json
                    val expiresAt = response.success.second
                    val collectionResultList =
                        collectionResponseFromJson(json = collectionResultJsonList) // list of content elements to return
                    if (collectionResultList.isNotEmpty()) {
                        val mapOfItems = HashMap<Int, ArcXPContentElement>()
                        val mapOfJson = HashMap<Int, String>()
                        collectionResultList.indices.forEach { index ->
                            mapOfItems[index + from] = collectionResultList[index]
                            mapOfJson[index + from] = jsonList[index]
                        }
                        if (!shouldIgnoreCache) {
                            //insert collection items into db
                            for ((key, value) in mapOfJson) {
                                insertCollectionItem(
                                    collectionAlias = id,
                                    uuid = mapOfItems[key]!!._id,
                                    index = key,
                                    json = value,
                                    expiresAt = expiresAt
                                )
                            }
                        }
                        Success(success = mapOfItems)
                    } else createFailure(message = application.getString(R.string.get_collection_empty))
                } catch (e: Exception) {
                    createFailure(
                        message = application.getString(
                            R.string.get_collection_deserialization_failure_message,
                            e.message
                        ), value = e
                    )
                }
            }

            is Failure -> response
        }

    private fun insertCollectionItem(
        collectionAlias: String,
        index: Int,
        uuid: String,
        json: String,
        expiresAt: Date
    ) {
        mIoScope.launch {
            // we insert both the json and collection item here into separate tables,
            // this way the data isn't duplicated
            cacheManager.insert(
                collectionItem = CollectionItem(
                    collectionAlias = collectionAlias,
                    indexValue = index,
                    uuid = uuid,
                    expiresAt = expiresAt
                ),
                jsonItem = JsonItem(
                    uuid = uuid,
                    jsonResponse = json,
                    expiresAt = expiresAt
                )
            )
        }
    }

    private suspend fun doContentApiCall(
        id: String,
        shouldIgnoreCache: Boolean
    ): Either<ArcXPException, ArcXPContentElement> =
        when (val response = contentApiManager.getContent(id = id)) {
            is Success -> {
                try {
                    val story = fromJson(response.success.first, ArcXPContentElement::class.java)!!
                    if (!shouldIgnoreCache) {
                        insertGeneric(
                            id = id,
                            json = response.success.first,
                            expiresAt = response.success.second
                        )
                    }
                    Success(success = story)
                } catch (e: Exception) {
                    createFailure(
                        message = application.getString(
                            R.string.get_content_deserialization_failure_message,
                            e.message
                        ), value = e
                    )
                }
            }

            is Failure -> response
        }

    private suspend fun doStoryApiCall(
        id: String,
        shouldIgnoreCache: Boolean
    ): Either<ArcXPException, ArcXPStory> =
        when (val response = contentApiManager.getContent(id = id)) {
            is Success -> {
                try {
                    val story = fromJson(response.success.first, ArcXPStory::class.java)!!
                    if (!shouldIgnoreCache) {
                        insertGeneric(
                            id = id,
                            json = response.success.first,
                            expiresAt = response.success.second
                        )
                    }
                    Success(success = story)
                } catch (e: Exception) {
                    createFailure(
                        message = application.getString(
                            R.string.get_story_deserialization_failure_message,
                            e.message
                        ), value = e
                    )
                }
            }

            is Failure -> response
        }

    private suspend fun doContentJsonApiCall(
        id: String,
        shouldIgnoreCache: Boolean
    ): Either<ArcXPException, String> =
        when (val response = contentApiManager.getContent(id = id)) {
            is Success -> {
                if (!shouldIgnoreCache) {
                    insertGeneric(
                        id = id,
                        json = response.success.first,
                        expiresAt = response.success.second
                    )
                }
                Success(success = response.success.first)
            }

            is Failure -> response
        }

    fun preLoadDb(//TODO we need to expose a public function for this
        id: String,
        listener: ArcXPContentCallback? = null //error result only
    ) {
        mIoScope.launch {
            contentApiManager.getContent(id = id).apply {
                when (this) {
                    is Success -> insertGeneric(
                        id = id,
                        json = success.first,
                        expiresAt = success.second
                    )

                    else -> listener?.onError(error = (this as Failure).failure)
                }
            }
        }
    }

    private suspend fun doSectionListApiCall(shouldIgnoreCache: Boolean): Either<ArcXPException, List<ArcXPSection>> =
        when (val result = contentApiManager.getSectionList()) {
            is Success -> {
                try {
                    val json = result.success.first
                    val expiresAt = result.success.second
                    val sectionList =
                        fromJson(json, Array<ArcXPSection>::class.java)!!.toList()
                    if (!shouldIgnoreCache) {
                        cacheManager.insertNavigation(
                            sectionHeaderItem = SectionHeaderItem(
                                sectionHeaderResponse = json,
                                expiresAt = expiresAt
                            )
                        )
                    }
                    Success(sectionList)
                } catch (e: Exception) {
                    createFailure(
                        message = application.getString(
                            R.string.navigation_deserialization_error,
                            e.message
                        ), value = e
                    )
                }
            }

            is Failure -> result
        }

    // if (item is non null and is not stale) item is still good, so we don't make api call else we do
    private fun shouldMakeApiCall(baseItem: BaseItem?) =
        baseItem?.let { shouldMakeApiCall(it.expiresAt) } ?: true

    private fun shouldMakeApiCall(date: Date?) = date?.let { Utils.currentTime() > it } ?: true

    fun deleteCollection(collectionAlias: String) =
        cacheManager.deleteCollection(collectionAlias = collectionAlias)

    fun deleteItem(uuid: String) = cacheManager.deleteItem(uuid = uuid)
    fun deleteCache() = cacheManager.deleteAll()

    private fun <T> fromJsonCheck(
        jsonString: String,
        classT: Class<T>
    ): Either<ArcXPException, T> =
        try {
            Success(fromJson(jsonString, classT)!!)
        } catch (e: Exception) {
            createFailure(
                message = application.getString(
                    R.string.deserialization_failure_message,
                    classT.simpleName,
                    e.message,
                ), value = e
            )
        }


    private fun navJsonCheck(navJson: String) = try {
        Success(
            fromJson(
                navJson,
                Array<ArcXPSection>::class.java
            )!!.toList()
        )
    } catch (e: Exception) {
        createFailure(
            message = application.getString(
                R.string.navigation_deserialization_error,
                e.message
            ), value = e
        )
    }
}