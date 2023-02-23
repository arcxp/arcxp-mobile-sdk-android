package com.arcxp.content.repositories

import com.arcxp.ArcXPMobileSDK.contentConfig
import com.arcxp.commons.util.Constants.DEFAULT_PAGINATION_SIZE
import com.arcxp.commons.util.DependencyFactory.createContentApiManager
import com.arcxp.commons.util.DependencyFactory.createError
import com.arcxp.commons.util.DependencyFactory.createIOScope
import com.arcxp.commons.util.Either
import com.arcxp.commons.util.Failure
import com.arcxp.commons.util.MoshiController.fromJson
import com.arcxp.commons.util.MoshiController.toJson
import com.arcxp.commons.util.Success
import com.arcxp.content.apimanagers.ContentApiManager
import com.arcxp.content.db.*
import com.arcxp.content.extendedModels.ArcXPCollection
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
     * @param id searches for this id (first through db if enabled, then api if not or stale)
     * @param from starting index to return results, ie 0 for page 1, 20(size) for page 2
     * @param size number of results to return
     * @return [Either] [ArcXPContentError] or a map of results ordered by server
     */
    suspend fun getCollection(
        id: String,
        shouldIgnoreCache: Boolean,
        from: Int,
        size: Int
    ): Either<ArcXPContentError, Map<Int, ArcXPCollection>> {
        return if (shouldIgnoreCache) {
            doCollectionApiCall(
                id = id,
                shouldIgnoreCache = true,
                from = from,
                size = size
            )
        } else {

            val collectionItems =
                cacheManager.getCollectionById(id = id, from = from, size = size)
            val map = HashMap<Int, ArcXPCollection>()

            collectionItems?.map { it.indexValue }?.forEachIndexed { index, collectionIndex ->
                map[collectionIndex] =
                    collectionItemFromJson(json = collectionItems[index].collectionResponse)

            }
            if (shouldMakeApiCall(baseItem = if (!collectionItems.isNullOrEmpty()) collectionItems[0] else null)) {
                val apiResult = doCollectionApiCall(
                    id = id,
                    shouldIgnoreCache = false,
                    from = from,
                    size = size
                )
                when {
                    apiResult is Success -> apiResult
                    map.isNotEmpty() -> Success(success = map)
                    else -> apiResult
                }
            } else {
                Success(success = map)
            }
        }
    }

    /**
     * [getCollectionAsJson] - request collection by content alias
     * Note this should be a troubleshooting function, does not use cache
     * @param id searches for this id
     * @param from starting index to return results, ie 0 for page 1, 20(size) for page 2
     * @param size number of results to return
     * @return [Either] Json string or [ArcXPContentError]
     */
    suspend fun getCollectionAsJson(
        id: String,
        from: Int,
        size: Int
    ): Either<ArcXPContentError, String> =
        when (val response = contentApiManager.getCollection(
            id = id,
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
     * @param id searches for this ANS id (first through db if enabled, then api if not or stale)
     */
    suspend fun getContent(
        id: String,
        shouldIgnoreCache: Boolean
    ): Either<ArcXPContentError, ArcXPContentElement> {
        return if (shouldIgnoreCache) {
            doContentApiCall(
                id = id,
                shouldIgnoreCache = true
            )
        } else {
            val jsonDbItem = cacheManager.getJsonById(id = id)
            if (shouldMakeApiCall(baseItem = jsonDbItem)) {
                val apiResult = doContentApiCall(
                    id = id,
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
     * @param id searches for this ANS id (first through db if enabled, then api if not or stale)
     */
    suspend fun getStory(
        id: String,
        shouldIgnoreCache: Boolean
    ): Either<ArcXPContentError, ArcXPStory> {
        return if (shouldIgnoreCache) {
            doStoryApiCall(
                id = id,
                shouldIgnoreCache = true
            )
        } else {
            val jsonDbItem = cacheManager.getJsonById(id = id)
            if (shouldMakeApiCall(baseItem = jsonDbItem)) {
                val apiResult = doStoryApiCall(
                    id = id,
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
                Success(
                    fromJson(
                        jsonDbItem!!.jsonResponse,
                        ArcXPStory::class.java
                    )!!
                )
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
    ): Either<ArcXPContentError, String> =
        when (val response = doContentJsonApiCall(id = id)) {
            is Success -> Success(success = response.success)
            is Failure -> Failure(failure = response.failure)
        }

    /**
     * [getSectionList] - request section lists / navigation
     * @param shouldIgnoreCache if enabled, skips db operation
     */
    suspend fun getSectionList(shouldIgnoreCache: Boolean): Either<ArcXPContentError, List<ArcXPSection>> {
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
    suspend fun getSectionListAsJson(): Either<ArcXPContentError, String> =
        when (val response = contentApiManager.getSectionList()) {
            is Success -> Success(success = response.success.first)
            is Failure -> Failure(failure = response.failure)
        }

    private fun insertGeneric(id: String, json: String, expiresAt: Date) {
        mIoScope.launch {
            cacheManager.insertJsonItem(
                jsonItem = JsonItem(
                    id = id,
                    jsonResponse = json,
                    expiresAt = expiresAt
                )
            )
        }
    }

    private fun collectionResponseFromJson(json: String) = fromJson(
        json,
        Array<ArcXPCollection>::class.java
    )!!.toList()

    private fun collectionResponseFullFromJson(json: String) = fromJson(
        json,
        Array<ArcXPStory>::class.java
    )!!.toList()

    private fun collectionItemFromJson(json: String): ArcXPCollection = fromJson(
        json,
        ArcXPCollection::class.java
    )!!

    private suspend fun doCollectionApiCall(
        id: String,
        shouldIgnoreCache: Boolean,
        from: Int,
        size: Int
    ): Either<ArcXPContentError, Map<Int, ArcXPCollection>> {
        val preLoading = contentConfig().preLoading
        return when (val response = contentApiManager.getCollection(
            id = id,
            from = from,
            size = size,
            full = preLoading
        )) {
            is Success -> {
                try {
                    val collectionResultJson = response.success.first
                    val expiresAt = response.success.second
                    val collectionResultList =
                        collectionResponseFromJson(json = collectionResultJson)
                    if (collectionResultList.isNotEmpty()) {
                        val mapOfItems = HashMap<Int, ArcXPCollection>()
                        val mapOfJson = HashMap<Int, String>()
                        collectionResultList.forEachIndexed { index, arcXPCollection ->
                            mapOfItems[index + from] = arcXPCollection
                            mapOfJson[index + from] = toJson(arcXPCollection)!!
                        }
                        if (!shouldIgnoreCache) {
                            //insert collection items into db
                            for ((key, value) in mapOfJson) {
                                insertCollectionItem(
                                    id = id,
                                    index = key,
                                    json = value,
                                    expiresAt = expiresAt
                                )
                            }
                            if (preLoading) {
                                //insert article items into db
                                val fullResultWithContentElements =
                                    collectionResponseFullFromJson(json = collectionResultJson)
                                val jsonList = fullResultWithContentElements.map { toJson(it)!! }//*****losing items here
                                fullResultWithContentElements.forEachIndexed { index, arcXPStory ->
                                    insertGeneric(
                                        id = arcXPStory._id!!,
                                        json = jsonList[index],
                                        expiresAt = expiresAt
                                    )
                                }
                            }
                        }
                        Success(success = mapOfItems)
                    } else {
                        Failure(failure = createError(message = "Get Collection result was Empty"))
                    }

                } catch (e: Exception) {
                    Failure(failure = createError(message = "Get Collection Deserialization Error"))
                }
            }
            is Failure -> {
                Failure(failure = response.failure)
            }
        }
    }

    private fun insertCollectionItem(id: String, index: Int, json: String, expiresAt: Date) {
        mIoScope.launch {
            cacheManager.insertCollectionItem(
                collectionItem = CollectionItem(
                    contentAlias = id,
                    indexValue = index,
                    collectionResponse = json,
                    expiresAt = expiresAt
                )
            )
        }
    }

    private suspend fun doContentApiCall(
        id: String,
        shouldIgnoreCache: Boolean
    ): Either<ArcXPContentError, ArcXPContentElement> =
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
                    Failure(createError(message = "Get Content Deserialization Error"))
                }
            }
            is Failure -> {
                Failure(failure = response.failure)
            }
        }

    private suspend fun doStoryApiCall(
        id: String,
        shouldIgnoreCache: Boolean
    ): Either<ArcXPContentError, ArcXPStory> =
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
                    Failure(createError(message = "Get Story Deserialization Error"))
                }
            }
            is Failure -> {
                Failure(failure = response.failure)
            }
        }

    private suspend fun doContentJsonApiCall(id: String): Either<ArcXPContentError, String> =
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
                    is Failure -> listener?.onError(error = failure)
                }
            }
        }
    }

    private suspend fun doSectionListApiCall(shouldIgnoreCache: Boolean): Either<ArcXPContentError, List<ArcXPSection>> =
        when (val result = contentApiManager.getSectionList()) {
            is Success -> {
                try {
                    val json = result.success.first
                    val expiresAt = result.success.second
                    val sectionList =
                        fromJson(json, Array<ArcXPSection>::class.java)!!.toList()
                    if (!shouldIgnoreCache) {
                        val done = cacheManager.insertNavigation(
                            sectionHeaderItem = SectionHeaderItem(
                                sectionHeaderResponse = json,
                                expiresAt = expiresAt
                            )
                        )
                        //we provide a set of the current content Aliases from site service to cacheManager
                        //so if the db contains entries outside of these content aliases,
                        //they are immediately purged (navigation was removed)
                        cacheManager.minimizeCollections(newCollectionAliases = sectionList.map {
                            it.id.replace(oldValue = "/", newValue = "")
                        }.toSet())
                    }
                    Success(sectionList)
                } catch (e: Exception) {
                    Failure(createError(message = "Navigation Deserialization Error"))
                }
            }
            is Failure -> {
                Failure(createError(message = "Failed to load navigation"))
            }
        }

    // if (item is non null and is not stale) item is still good, so we don't make api call else we do
    private fun shouldMakeApiCall(baseItem: BaseItem?) =
        baseItem?.let { Calendar.getInstance(TimeZone.getTimeZone("UTC")).time > it.expiresAt }
            ?: true
}