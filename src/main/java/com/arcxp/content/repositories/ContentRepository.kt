package com.arcxp.content.repositories

import com.arcxp.ArcXPMobileSDK.contentConfig
import com.arcxp.commons.throwables.ArcXPException
import com.arcxp.commons.throwables.ArcXPSDKErrorType
import com.arcxp.commons.util.Constants.DEFAULT_PAGINATION_SIZE
import com.arcxp.commons.util.DependencyFactory.createArcXPException
import com.arcxp.commons.util.DependencyFactory.createContentApiManager
import com.arcxp.commons.util.DependencyFactory.createIOScope
import com.arcxp.commons.util.Either
import com.arcxp.commons.util.Failure
import com.arcxp.commons.util.MoshiController.fromJson
import com.arcxp.commons.util.Success
import com.arcxp.commons.util.Utils
import com.arcxp.commons.util.Utils.parseJsonArray
import com.arcxp.content.apimanagers.ContentApiManager
import com.arcxp.content.db.*
import com.arcxp.content.extendedModels.ArcXPContentElement
import com.arcxp.content.extendedModels.ArcXPStory
import com.arcxp.content.models.*
import com.arcxp.content.util.*
import kotlinx.coroutines.*
import java.util.*

/**
 * @suppress
 *
 * This is our repository layer abstraction, clients to this class(ArcxpContentManager) can request data and we return via db or api call where appropriate (through callbacks only currently)
 * so this should be considered Single Source of Truth (SSOT) for our data from backend
 */
class ContentRepository(
    private val contentApiManager: ContentApiManager = createContentApiManager(),
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
        shouldIgnoreCache: Boolean,
        from: Int,
        size: Int
    ): Either<ArcXPException, Map<Int, ArcXPContentElement>> {
        return if (shouldIgnoreCache) {
            doCollectionApiCall(
                id = collectionAlias,
                shouldIgnoreCache = true,
                from = from,
                size = size
            )
        } else {

            val cacheContentElementMap =
                cacheManager.getCollection(collectionAlias = collectionAlias, from = from, size = size)

            return if (shouldMakeApiCall(cacheManager.getCollectionExpiration(collectionAlias))) {
                val apiResult = doCollectionApiCall(
                    id = collectionAlias,
                    shouldIgnoreCache = false,
                    from = from,
                    size = size
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
     * Note this should be a troubleshooting function, does not use cache
     * @param id searches for this id
     * @param from starting index to return results, ie 0 for page 1, 20(size) for page 2
     * @param size number of results to return
     * @return [Either] Json string or [ArcXPException]
     */
    suspend fun getCollectionAsJson(
        id: String,
        from: Int,
        size: Int
    ): Either<ArcXPException, String> =
        when (val response = contentApiManager.getCollection(
            collectionAlias = id,
            from = from,
            size = size
        )) {
            is Success -> Success(success = response.success.first)
            is Failure -> Failure(failure = response.failure)
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
        shouldIgnoreCache: Boolean
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
                    jsonDbItem != null ->
                        Success(
                            fromJson(
                                jsonDbItem.jsonResponse,
                                ArcXPContentElement::class.java
                            )!!
                        )

                    else -> apiResult
                }
            } else {
                Success(
                    fromJson(
                        jsonDbItem!!.jsonResponse,
                        ArcXPContentElement::class.java
                    )!!
                )
            }
        }
    }

    /**
     * [getStory] - request article/story by ANS id
     * @param shouldIgnoreCache if enabled, skips db operation
     * @param uuid searches for this ANS id (first through db if enabled, then api if not or stale)
     */
    suspend fun getStory(
        uuid: String,
        shouldIgnoreCache: Boolean
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
                    jsonDbItem != null ->
                        Success(
                            fromJson(
                                jsonDbItem.jsonResponse,
                                ArcXPStory::class.java
                            )!!
                        )

                    else -> apiResult
                }
            } else {
                val story = fromJson(
                    jsonDbItem!!.jsonResponse,
                    ArcXPStory::class.java
                )!!
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
     * @param id searches for this ANS id (first through db if enabled, then api if not or stale)
     */
    suspend fun getContentAsJson(
        id: String
    ): Either<ArcXPException, String> =
        when (val response = doContentJsonApiCall(id = id)) {
            is Success -> Success(success = response.success)
            is Failure -> Failure(failure = response.failure)
        }

    /**
     * [getSectionList] - request section lists / navigation
     * @param shouldIgnoreCache if enabled, skips db operation
     */
    suspend fun getSectionList(shouldIgnoreCache: Boolean): Either<ArcXPException, List<ArcXPSection>> {
        return if (shouldIgnoreCache) {
            doSectionListApiCall(shouldIgnoreCache = true)
        } else {
            val navigationEntry = cacheManager.getSectionList()
            if (shouldMakeApiCall(baseItem = navigationEntry)) {
                val apiResult = doSectionListApiCall(shouldIgnoreCache = false)
                when {
                    apiResult is Success -> apiResult
                    navigationEntry != null -> Success(
                        fromJson(
                            navigationEntry.sectionHeaderResponse,
                            Array<ArcXPSection>::class.java
                        )!!.toList()
                    )

                    else -> apiResult
                }
            } else {
                Success(
                    fromJson(
                        navigationEntry!!.sectionHeaderResponse,
                        Array<ArcXPSection>::class.java
                    )!!.toList()
                )
            }
        }
    }

    /**
     * [getSectionListAsJson] - request section lists / navigation
     * Note this should be a troubleshooting function, does not use cache
     */
    suspend fun getSectionListAsJson(): Either<ArcXPException, String> =
        when (val response = contentApiManager.getSectionList()) {
            is Success -> Success(success = response.success.first)
            is Failure -> Failure(failure = response.failure)
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
        size: Int
    ): Either<ArcXPException, Map<Int, ArcXPContentElement>> {
        val preLoading = contentConfig().preLoading
        return when (val response = contentApiManager.getCollection(
            collectionAlias = id,
            from = from,
            size = size,
            full = preLoading
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
                                    contentAlias = id,
                                    uuid = mapOfItems[key]!!._id,
                                    index = key,
                                    json = value,
                                    expiresAt = expiresAt
                                )
                            }
                        }
                        Success(success = mapOfItems)
                    } else {
                        Failure(
                            failure = createArcXPException(
                                type = ArcXPSDKErrorType.SERVER_ERROR,
                                message = "Get Collection result was Empty"
                            )
                        )
                    }

                } catch (e: Exception) {
                    Failure(
                        failure = createArcXPException(
                            type = ArcXPSDKErrorType.SERVER_ERROR,
                            message = "Get Collection Deserialization Error"
                        )
                    )
                }
            }

            is Failure -> {
                Failure(failure = response.failure)
            }
        }
    }

    private fun insertCollectionItem(
        contentAlias: String,
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
                    contentAlias = contentAlias,
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
                    Failure(
                        createArcXPException(
                            type = ArcXPSDKErrorType.SERVER_ERROR,
                            message = "Get Content Deserialization Error"
                        )
                    )
                }
            }

            is Failure -> {
                Failure(failure = response.failure)
            }
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
                    Failure(
                        createArcXPException(
                            type = ArcXPSDKErrorType.SERVER_ERROR,
                            message = "Get Story Deserialization Error"
                        )
                    )
                }
            }

            is Failure -> {
                Failure(failure = response.failure)
            }
        }

    private suspend fun doContentJsonApiCall(id: String): Either<ArcXPException, String> =
        when (val response = contentApiManager.getContent(id = id)) {
            is Success -> Success(success = response.success.first)
            is Failure -> Failure(failure = response.failure)
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
                    Failure(
                        createArcXPException(
                            type = ArcXPSDKErrorType.SERVER_ERROR,
                            message = "Navigation Deserialization Error"
                        )
                    )
                }
            }

            is Failure -> {
                Failure(
                    createArcXPException(
                        type = ArcXPSDKErrorType.SERVER_ERROR,
                        message = "Failed to load navigation"
                    )
                )
            }
        }

    // if (item is non null and is not stale) item is still good, so we don't make api call else we do
    private fun shouldMakeApiCall(baseItem: BaseItem?) =
        baseItem?.let { shouldMakeApiCall(it.expiresAt) } ?: true

    private fun shouldMakeApiCall(date: Date?) = date?.let { Utils.currentTime() > it } ?: true
}