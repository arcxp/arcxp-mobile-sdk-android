package com.arcxp.content

import android.app.Application
import androidx.annotation.Keep
import androidx.lifecycle.LiveData
import com.arcxp.commons.analytics.ArcXPAnalyticsManager
import com.arcxp.commons.throwables.ArcXPException
import com.arcxp.commons.throwables.ArcXPSDKErrorType
import com.arcxp.commons.util.Constants.DEFAULT_PAGINATION_SIZE
import com.arcxp.commons.util.Constants.VALID_COLLECTION_SIZE_RANGE
import com.arcxp.commons.util.DependencyFactory.createArcXPException
import com.arcxp.commons.util.DependencyFactory.createIOScope
import com.arcxp.commons.util.DependencyFactory.createLiveData
import com.arcxp.commons.util.Either
import com.arcxp.commons.util.Failure
import com.arcxp.commons.util.Success
import com.arcxp.content.extendedModels.ArcXPContentElement
import com.arcxp.content.extendedModels.ArcXPStory
import com.arcxp.content.models.ArcXPContentCallback
import com.arcxp.content.models.ArcXPSection
import com.arcxp.content.models.EventType
import com.arcxp.content.repositories.ContentRepository
import com.arcxp.content.util.AuthManager
import com.arcxp.sdk.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Keep
/**
 * This class is responsible for dispensing content.
 *
 * Content source may be from api or db depending on repository
 *
 * This class has a restricted constructor,
 * so use the instance provided by [com.arcxp.ArcXPMobileSDK] method [com.arcxp.ArcXPMobileSDK.contentManager]
 * after initializing [com.arcxp.ArcXPMobileSDK].
 *
 * Each request method can either use [ArcXPContentCallback] parameter for result stream
 * or use the return value [LiveData] and subscribe to result stream.
 *
 * @property application Application Context (provided by initialization)
 * @property contentRepository Repository (provided by initialization)
 * @property arcXPAnalyticsManager Analytics Manager (provided by initialization)
 */
class ArcXPContentManager internal constructor(
    private val application: Application,
    private val contentRepository: ContentRepository,
    private val arcXPAnalyticsManager: ArcXPAnalyticsManager,
    private val mIoScope: CoroutineScope = createIOScope(),
    private val contentConfig: ArcXPContentConfig
) {

    init {
        AuthManager.accessToken = application.getString(R.string.bearer_token)
    }

    /**
     * This function requests a collection result by content alias
     *
     * returns result either through callback interface or livedata
     *
     * @param id Content Alias
     * @param listener Callback interface,
     *
     * override [ArcXPContentCallback.onGetCollectionSuccess] for success
     *
     * override [ArcXPContentCallback.onError] for failure
     *
     * or leave null and use livedata result and error livedata
     * @param shouldIgnoreCache if true, we ignore caching for this call only
     * @param from index in which to start (ie for pagination, you may want to start at index for next page)
     * @param size number of entries to request: (valid range [VALID_COLLECTION_SIZE_RANGE], will coerce parameter into this range if it is outside)
     * @return [LiveData] subscribe to this livedata for successful results (or use callback interface)
     *
     * Note: each result is a stream, even the interface methods can possibly have additional results
     */
    fun getCollection(
        id: String,
        listener: ArcXPContentCallback? = null,
        shouldIgnoreCache: Boolean = false,
        from: Int = 0,
        size: Int = DEFAULT_PAGINATION_SIZE
    ): LiveData<Either<ArcXPException, Map<Int, ArcXPContentElement>>> {
        //arcXPAnalyticsManager.sendAnalytics(EventType.COLLECTION)
        val stream =
            createLiveData<Either<ArcXPException, Map<Int, ArcXPContentElement>>>()
        mIoScope.launch {
            stream.postValue(contentRepository.getCollection(
                collectionAlias = id,
                shouldIgnoreCache = shouldIgnoreCache,
                from = from,
                size = size.coerceIn(VALID_COLLECTION_SIZE_RANGE)
            ).apply {
                when (this) {
                    is Success -> listener?.onGetCollectionSuccess(response = success)
                    is Failure -> listener?.onError(error = failure)
                }
            })
        }
        return stream
    }

    /**
     * [getCollectionSuspend] this suspend function requests a collection result by content alias
     * @param id Content Alias
     * @param shouldIgnoreCache if true, we ignore caching for this call only
     * @param from index in which to start (ie for pagination, you may want to start at index for next page)
     * @param size number of entries to request: (valid range [VALID_COLLECTION_SIZE_RANGE], will coerce parameter into this range if it is outside)
     * @return [Either] returns either Success Map<Int, ArcXPContentElement> with collections in their desired order from server or Failure ArcXPException
     */
    suspend fun getCollectionSuspend(
        id: String,
        shouldIgnoreCache: Boolean = false,
        from: Int = 0,
        size: Int = DEFAULT_PAGINATION_SIZE
    ): Either<ArcXPException, Map<Int, ArcXPContentElement>> =
        withContext(mIoScope.coroutineContext) {
            //arcXPAnalyticsManager.sendAnalytics(EventType.COLLECTION)
            contentRepository.getCollection(
                collectionAlias = id,
                shouldIgnoreCache = shouldIgnoreCache,
                from = from,
                size = size.coerceIn(VALID_COLLECTION_SIZE_RANGE)
            )
        }


    /**
     * This function requests a collection result from the user provided mobile video collection content alias
     *
     * returns result either through callback interface or livedata
     *
     * @param listener Callback interface,
     *
     * override [ArcXPContentCallback.onGetCollectionSuccess] for success
     *
     * override [ArcXPContentCallback.onError] for failure
     *
     * or leave null and use livedata result and error livedata
     * @param shouldIgnoreCache if true, we ignore caching for this call only
     * @param from index in which to start (ie for pagination, you may want to start at index for next page)
     * @param size number of entries to request: (valid range [VALID_COLLECTION_SIZE_RANGE], will coerce parameter into this range if it is outside)
     * @return [LiveData] subscribe to this livedata for successful results (or use callback interface)
     *
     * Note: each result is a stream, even the interface methods can possibly have additional results
     */
    fun getVideoCollection(
        listener: ArcXPContentCallback? = null,
        shouldIgnoreCache: Boolean = false,
        from: Int = 0,
        size: Int = DEFAULT_PAGINATION_SIZE
    ) = getCollection(
        id = contentConfig.videoCollectionName,
        listener = listener,
        shouldIgnoreCache = shouldIgnoreCache,
        from = from,
        size = size
    )
    /**
     * This function requests a collection result from the user provided mobile video collection content alias
     *
     * @param shouldIgnoreCache if true, we ignore caching for this call only
     * @param from index in which to start (ie for pagination, you may want to start at index for next page)
     * @param size number of entries to request: (valid range [VALID_COLLECTION_SIZE_RANGE], will coerce parameter into this range if it is outside)
     * @return [Either]<[ArcXPException], [Map]<[Int], [ArcXPContentElement]> indexed map of results from search in order from WebSked
     *
     */
    suspend fun getVideoCollectionSuspend(
        shouldIgnoreCache: Boolean = false,
        from: Int = 0,
        size: Int = DEFAULT_PAGINATION_SIZE
    ): Either<ArcXPException, Map<Int, ArcXPContentElement>> =
        withContext(mIoScope.coroutineContext) {
            contentRepository.getCollection(
                collectionAlias = contentConfig.videoCollectionName,
                shouldIgnoreCache = shouldIgnoreCache,
                from = from,
                size = size.coerceIn(VALID_COLLECTION_SIZE_RANGE)
            )
        }


    /**
     * This function requests a collection result by content alias as a json string
     *
     * returns result either through callback interface or livedata
     *
     * @param id Content Alias
     * @param listener Callback interface,
     *
     * override [ArcXPContentCallback.onGetJsonSuccess] for success
     *
     * override [ArcXPContentCallback.onError] for failure
     *
     * or leave null and use livedata result and error livedata
     * @param from index in which to start (ie for pagination, you may want to start at index for next page)
     * @param size number of entries to request: (valid range [VALID_COLLECTION_SIZE_RANGE], will coerce parameter into this range if it is outside)
     * @return [LiveData] subscribe to this livedata for successful results (or use callback interface)
     *
     * Note: each result is a stream, even the interface methods can possibly have additional results
     */
    fun getCollectionAsJson(
        id: String,
        listener: ArcXPContentCallback? = null,
        from: Int = 0,
        size: Int = DEFAULT_PAGINATION_SIZE
    ): LiveData<Either<ArcXPException, String>> {
        //arcXPAnalyticsManager.sendAnalytics(EventType.COLLECTION)
        val stream =
            createLiveData<Either<ArcXPException, String>>()
        mIoScope.launch {
            stream.postValue(contentRepository.getCollectionAsJson(
                id = id,
                from = from,
                size = size.coerceIn(VALID_COLLECTION_SIZE_RANGE)
            ).apply {
                when (this) {
                    is Success -> listener?.onGetJsonSuccess(response = success)
                    is Failure -> listener?.onError(error = failure)
                }
            })
        }
        return stream
    }

    /**
     * This function requests a search to be performed by search Term (keyword/tag based on resolver setup default is tag(used in example app))
     * note: cache is not used for search, but if you open item it will be cached
     *
     * returns result either through callback interface or livedata
     *
     * @param searchTerms List of strings to search
     * @param listener Callback interface:
     * override [ArcXPContentCallback.onSearchSuccess] for success
     * override [ArcXPContentCallback.onError] for failure
     * or leave [listener] null and use livedata result and error livedata
     * @param from index in which to start (ie for pagination, you may want to start at index for next page)
     * @param size number of entries to request: (valid range [VALID_COLLECTION_SIZE_RANGE])
     * @return [LiveData] subscribe to this livedata for successful results (or use callback interface)
     */
    fun search(
        searchTerms: List<String>,
        listener: ArcXPContentCallback? = null,
        from: Int = 0,
        size: Int = DEFAULT_PAGINATION_SIZE
    ): LiveData<Either<ArcXPException, Map<Int, ArcXPContentElement>>> {
        return search(
            searchTerm = searchTerms.joinToString(separator = ","),
            listener = listener,
            from = from,
            size = size
        )
    }

    /**
     * This function requests a search to be performed by search Term (keyword/tag based on resolver setup default is tag(used in example app))
     * note: cache is not used for search, but if you open item it will be cached
     *
     * returns result either through callback interface or livedata
     *
     * @param searchTerms List of strings to search
     * @param listener Callback interface:
     * override [ArcXPContentCallback.onSearchSuccess] for success
     * override [ArcXPContentCallback.onError] for failure
     * or leave [listener] null and use livedata result and error livedata
     * @param from index in which to start (ie for pagination, you may want to start at index for next page)
     * @param size number of entries to request: (valid range [VALID_COLLECTION_SIZE_RANGE])
     * @return [LiveData] subscribe to this livedata for successful results (or use callback interface)
     */
    fun searchVideos(
        searchTerms: List<String>,
        listener: ArcXPContentCallback? = null,
        from: Int = 0,
        size: Int = DEFAULT_PAGINATION_SIZE
    ): LiveData<Either<ArcXPException, Map<Int, ArcXPContentElement>>> {
        return searchVideos(
            searchTerm = searchTerms.joinToString(separator = ","),
            listener = listener,
            from = from,
            size = size
        )
    }

    /**
     * [searchSuspend] requests a search to be performed by search Term (keyword/tag based on resolver setup default is tag(used in example app))
     * note: cache is not used for search, but if you open item it will be cached
     *
     * @param searchTerms List of strings to search
     * @param from index in which to start (ie for pagination, you may want to start at index for next page)
     * @param size number of entries to request: (valid range [VALID_COLLECTION_SIZE_RANGE])
     * @return [Either] returns either Success Map<Int, ArcXPContentElement> with search results in their server given order or Failure ArcXPException.
     */
    suspend fun searchSuspend(
        searchTerms: List<String>,
        from: Int = 0,
        size: Int = DEFAULT_PAGINATION_SIZE
    ): Either<ArcXPException, Map<Int, ArcXPContentElement>> =
        searchSuspend(
            searchTerm = searchTerms.joinToString(separator = ","),
            from = from,
            size = size
        )

    /**
     * [searchVideosSuspend] requests a search to be performed by search Term (keyword/tag based on resolver setup default is tag(used in example app))
     * note: cache is not used for search, but if you open item it will be cached
     *
     * @param searchTerms List of strings to search
     * @param from index in which to start (ie for pagination, you may want to start at index for next page)
     * @param size number of entries to request: (valid range [VALID_COLLECTION_SIZE_RANGE])
     * @return [Either] returns either Success Map<Int, ArcXPContentElement> with search results in their server given order or Failure ArcXPException.
     */
    suspend fun searchVideosSuspend(
        searchTerms: List<String>,
        from: Int = 0,
        size: Int = DEFAULT_PAGINATION_SIZE
    ): Either<ArcXPException, Map<Int, ArcXPContentElement>> =
        searchVideosSuspend(
            searchTerm = searchTerms.joinToString(separator = ","),
            from = from,
            size = size
        )


    /**
     * This function requests a search to be performed by search Term (keyword/tag based on resolver setup default is tag(used in example app))
     * note: cache is not used for search, but if you open item it will be cached
     *
     * returns result either through callback interface or livedata
     *
     * @param searchTerm string to search (searches TAG by default)
     * @param listener Callback interface,
     * override [ArcXPContentCallback.onSearchSuccess] for success
     * override [ArcXPContentCallback.onError] for failure
     * or leave [listener] null and use livedata result and error livedata
     * @param from index in which to start (ie for pagination, you may want to start at index for next page)
     * @param size number of entries to request: (valid range [VALID_COLLECTION_SIZE_RANGE], will coerce parameter into this range if it is outside)
     * @return [LiveData] subscribe to this livedata for successful results (or use callback interface)
     */
    fun search(
        searchTerm: String,
        listener: ArcXPContentCallback? = null,
        from: Int = 0,
        size: Int = DEFAULT_PAGINATION_SIZE
    ): LiveData<Either<ArcXPException, Map<Int, ArcXPContentElement>>> {
        val stream =
            createLiveData<Either<ArcXPException, Map<Int, ArcXPContentElement>>>()
        mIoScope.launch {
            val regPattern = Regex("[^A-Za-z0-9,\\- ]")
            val searchTermsCheckedChecked = regPattern.replace(searchTerm, "")
            //arcXPAnalyticsManager.sendAnalytics(EventType.SEARCH)
            stream.postValue(
                contentRepository.searchSuspend(
                    searchTerm = searchTermsCheckedChecked,
                    from = from,
                    size = size.coerceIn(VALID_COLLECTION_SIZE_RANGE)
                ).apply {
                    when (this) {
                        is Success -> {
                            listener?.onSearchSuccess(success)
                        }

                        is Failure -> {
                            listener?.onError(failure)
                        }
                    }
                }
            )
        }
        return stream
    }

    /**
     * This function requests a search to be performed by search Term (keyword/tag based on resolver setup default is tag(used in example app))
     * note: cache is not used for search, but if you open item it will be cached
     *
     * returns result either through callback interface or livedata
     *
     * @param searchTerm string to search (searches TAG by default)
     * @param listener Callback interface,
     * override [ArcXPContentCallback.onSearchSuccess] for success
     * override [ArcXPContentCallback.onError] for failure
     * or leave [listener] null and use livedata result and error livedata
     * @param from index in which to start (ie for pagination, you may want to start at index for next page)
     * @param size number of entries to request: (valid range [VALID_COLLECTION_SIZE_RANGE], will coerce parameter into this range if it is outside)
     * @return [LiveData] subscribe to this livedata for successful results (or use callback interface)
     */
    fun searchVideos(
        searchTerm: String,
        listener: ArcXPContentCallback? = null,
        from: Int = 0,
        size: Int = DEFAULT_PAGINATION_SIZE
    ): LiveData<Either<ArcXPException, Map<Int, ArcXPContentElement>>> {
        val stream =
            createLiveData<Either<ArcXPException, Map<Int, ArcXPContentElement>>>()
        mIoScope.launch {
            val regPattern = Regex("[^A-Za-z0-9,\\- ]")
            val searchTermsCheckedChecked = regPattern.replace(searchTerm, "")
            //arcXPAnalyticsManager.sendAnalytics(EventType.SEARCH)
            stream.postValue(
                contentRepository.searchVideosSuspend(
                    searchTerm = searchTermsCheckedChecked,
                    from = from,
                    size = size.coerceIn(VALID_COLLECTION_SIZE_RANGE)
                ).apply {
                    when (this) {
                        is Success -> {
                            listener?.onSearchSuccess(success)
                        }

                        is Failure -> {
                            listener?.onError(failure)
                        }
                    }
                }
            )
        }
        return stream
    }

    /**
     * This function requests a search to be performed by search Term (keyword/tag based on resolver setup default is tag(used in example app))
     * note: cache is not used for search, but if you open item it will be cached
     *
     * returns result either through callback interface or livedata
     *
     * @param keyword string to search (searches TAG by default)
     * @param listener Callback interface,
     * override [ArcXPContentCallback.onSearchSuccess] for success
     * override [ArcXPContentCallback.onError] for failure
     * or leave [listener] null and use livedata result and error livedata
     * @param from index in which to start (ie for pagination, you may want to start at index for next page)
     * @param size number of entries to request: (valid range [VALID_COLLECTION_SIZE_RANGE], will coerce parameter into this range if it is outside)
     * @return [LiveData] subscribe to this livedata for successful results (or use callback interface)
     */
    @Deprecated(
        message = "use search",
        ReplaceWith(expression = "search(searchTerm, listener, from, size)")
    )
    fun searchByKeyword(
        keyword: String,
        listener: ArcXPContentCallback? = null,
        from: Int = 0,
        size: Int = DEFAULT_PAGINATION_SIZE
    ) = search(
        searchTerm = keyword,
        listener = listener,
        from = from,
        size = size
    )

    /**
     * This function requests a search to be performed by search Term (keyword/tag based on resolver setup default is tag(used in example app))
     * note: cache is not used for search, but if you open item it will be cached
     *
     * returns result either through callback interface or livedata
     *
     * @param keywords string to search (searches TAG by default)
     * @param listener Callback interface,
     * override [ArcXPContentCallback.onSearchSuccess] for success
     * override [ArcXPContentCallback.onError] for failure
     * or leave [listener] null and use livedata result and error livedata
     * @param from index in which to start (ie for pagination, you may want to start at index for next page)
     * @param size number of entries to request: (valid range [VALID_COLLECTION_SIZE_RANGE], will coerce parameter into this range if it is outside)
     * @return [LiveData] subscribe to this livedata for successful results (or use callback interface)
     */
    @Deprecated(
        message = "use search",
        ReplaceWith(expression = "search(searchTerms, listener, from, size)")
    )
    fun searchByKeywords(
        keywords: List<String>,
        listener: ArcXPContentCallback? = null,
        from: Int = 0,
        size: Int = DEFAULT_PAGINATION_SIZE
    ) = search(
        searchTerms = keywords,
        listener = listener,
        from = from,
        size = size
    )

    /**
     * [searchSuspend] requests a search to be performed by search Term (keyword/tag based on resolver setup default is tag(used in example app))
     * note: cache is not used for search, but if you open item it will be cached
     *
     * @param searchTerm term to search
     * @param from index in which to start (ie for pagination, you may want to start at index for next page)
     * @param size number of entries to request: (valid range [VALID_COLLECTION_SIZE_RANGE])
     * @return [Either] returns either Success Map<Int, ArcXPContentElement> with search results in their server given order or Failure ArcXPException.
     */
    suspend fun searchSuspend(
        searchTerm: String,
        from: Int = 0,
        size: Int = DEFAULT_PAGINATION_SIZE
    ): Either<ArcXPException, Map<Int, ArcXPContentElement>> {
        return withContext(mIoScope.coroutineContext) {
            val regPattern = Regex("[^A-Za-z0-9,\\- ]")
            val searchTermChecked = regPattern.replace(searchTerm, "")
            //arcXPAnalyticsManager.sendAnalytics(EventType.SEARCH)
            contentRepository.searchSuspend(
                searchTerm = searchTermChecked,
                from = from,
                size = size.coerceIn(VALID_COLLECTION_SIZE_RANGE)
            )
        }
    }


    /**
     * [searchCollectionSuspend] requests a search to be performed by search Term (keyword/tag based on resolver setup default is tag(used in example app))
     * note: cache is not used for search, but if you open item it will be cached
     *
     * @param searchTerm term to search
     * @param from index in which to start (ie for pagination, you may want to start at index for next page)
     * @param size number of entries to request: (valid range [VALID_COLLECTION_SIZE_RANGE])
     * @return [Either] returns either Success Map<Int, ArcXPContentElement> with collections in their desired order from server or Failure ArcXPException
     */
    suspend fun searchCollectionSuspend(
        searchTerm: String,
        from: Int = 0,
        size: Int = DEFAULT_PAGINATION_SIZE
    ): Either<ArcXPException, Map<Int, ArcXPContentElement>> {
        return withContext(mIoScope.coroutineContext) {
            val regPattern = Regex("[^A-Za-z0-9,\\- ]")
            val searchTermChecked = regPattern.replace(searchTerm, "")
            //arcXPAnalyticsManager.sendAnalytics(EventType.SEARCH)
            contentRepository.searchCollectionSuspend(
                searchTerm = searchTermChecked,
                from = from,
                size = size.coerceIn(VALID_COLLECTION_SIZE_RANGE)
            )
        }
    }
    /**
     * [searchCollectionSuspend] requests a search to be performed by search Term (keyword/tag based on resolver setup default is tag(used in example app))
     * note: cache is not used for search, but if you open item it will be cached
     *
     * @param searchTerms list of terms to search
     * @param from index in which to start (ie for pagination, you may want to start at index for next page)
     * @param size number of entries to request: (valid range [VALID_COLLECTION_SIZE_RANGE])
     * @return [Either] returns either Success Map<Int, ArcXPContentElement> with collections in their desired order from server or Failure ArcXPException
     */
    suspend fun searchCollectionSuspend(
        searchTerms: List<String>,
        from: Int = 0,
        size: Int = DEFAULT_PAGINATION_SIZE
    ): Either<ArcXPException, Map<Int, ArcXPContentElement>> =
        searchCollectionSuspend(
            searchTerm = searchTerms.joinToString(separator = ","),
            from = from,
            size = size
        )

    /**
     * [searchVideosSuspend] requests a search to be performed by search Term (keyword/tag based on resolver setup default is tag(used in example app))
     * - searches only video results
     * note: cache is not used for search, but if you open item it will be cached
     *
     * @param searchTerm term to search
     * @param from index in which to start (ie for pagination, you may want to start at index for next page)
     * @param size number of entries to request: (valid range [VALID_COLLECTION_SIZE_RANGE])
     * @return [Either] returns either Success Map<Int, ArcXPContentElement> with search results in their server given order or Failure ArcXPException.
     */
    suspend fun searchVideosSuspend(
        searchTerm: String,
        from: Int = 0,
        size: Int = DEFAULT_PAGINATION_SIZE
    ): Either<ArcXPException, Map<Int, ArcXPContentElement>> {
        return withContext(mIoScope.coroutineContext) {
            val regPattern = Regex("[^A-Za-z0-9,\\- ]")
            val searchTermChecked = regPattern.replace(searchTerm, "")
            //arcXPAnalyticsManager.sendAnalytics(EventType.SEARCH)
            contentRepository.searchVideosSuspend(
                searchTerm = searchTermChecked,
                from = from,
                size = size.coerceIn(VALID_COLLECTION_SIZE_RANGE)
            )
        }
    }

    /**
     * [searchByKeywordSuspend] requests a search to be performed by tag (or however resolver is setup)
     * note: cache is not used for search, but if you open item it will be cached
     *
     * @param keyword term to search
     * @param from index in which to start (ie for pagination, you may want to start at index for next page)
     * @param size number of entries to request: (valid range [VALID_COLLECTION_SIZE_RANGE])
     * @return [Either] returns either Success Map<Int, ArcXPContentElement> with search results in their server given order or Failure ArcXPException.
     */
    @Deprecated(
        message = "use searchSuspend",
        ReplaceWith(expression = "searchSuspend(searchTerm, listener, from, size)")
    )
    suspend fun searchByKeywordSuspend(
        keyword: String,
        from: Int = 0,
        size: Int = DEFAULT_PAGINATION_SIZE
    ) = searchSuspend(searchTerm = keyword, from = from, size = size)


    /**
     * [searchByKeywordsSuspend] requests a search to be performed by keyword
     * note: cache is not used for search, but if you open item it will be cached
     *
     * @param keywords List of Keywords to search
     * @param from index in which to start (ie for pagination, you may want to start at index for next page)
     * @param size number of entries to request: (valid range [VALID_COLLECTION_SIZE_RANGE])
     * @return [Either] returns either Success Map<Int, ArcXPContentElement> with search results in their server given order or Failure ArcXPException.
     */
    @Deprecated(
        message = "use searchSuspend",
        ReplaceWith(expression = "searchSuspend(searchTerms, listener, from, size)")
    )
    suspend fun searchByKeywordsSuspend(
        keywords: List<String>,
        from: Int = 0,
        size: Int = DEFAULT_PAGINATION_SIZE
    ): Either<ArcXPException, Map<Int, ArcXPContentElement>> =
        searchSuspend(
            searchTerm = keywords.joinToString(separator = ","),
            from = from,
            size = size
        )


    /**
     * [getStory] This function requests a story / article result by ANS ID
     *
     * returns result with ans type = "story" either through callback interface or livedata
     *
     * @param id ANS ID
     * @param listener Callback interface,
     * override [ArcXPContentCallback.onGetContentSuccess] for success
     * override [ArcXPContentCallback.onError] for failure
     * or leave null and use livedata result and error livedata
     * @param shouldIgnoreCache if true, we ignore caching for this call only
     * @return [LiveData] subscribe to this livedata for successful results (or use callback interface)
     *
     * Note: each result is a stream, even the interface methods can possibly have additional results
     */
    @Deprecated(
        message = "use getArcXPStory for updated data object",
        ReplaceWith(expression = "getArcXPStory(id, listener, shouldIgnoreCache)")
    )
    fun getStory(
        id: String,
        listener: ArcXPContentCallback? = null,
        shouldIgnoreCache: Boolean = false
    ): LiveData<Either<ArcXPException, ArcXPContentElement>> =
        getContentByType(
            id = id,
            shouldIgnoreCache = shouldIgnoreCache,
            contentType = EventType.STORY,
            listener = listener
        )


    /**
     * [getArcXPStory] This function requests a story / article result by ANS ID
     *
     * returns result with ans type = "story" either through callback interface or livedata
     *
     * @param id ANS ID
     * @param listener Callback interface,
     * override [ArcXPContentCallback.onGetContentSuccess] for success
     * override [ArcXPContentCallback.onError] for failure
     * or leave null and use livedata result and error livedata
     * @param shouldIgnoreCache if true, we ignore caching for this call only
     * @return [LiveData] subscribe to this livedata for successful results (or use callback interface)
     *
     * Note: each result is a stream, even the interface methods can possibly have additional results
     */
    fun getArcXPStory(
        id: String,
        listener: ArcXPContentCallback? = null,
        shouldIgnoreCache: Boolean = false
    ): LiveData<Either<ArcXPException, ArcXPStory>> =
        fetchArcXPStory(
            id = id,
            shouldIgnoreCache = shouldIgnoreCache,
            listener = listener
        )

    /**
     * [getContentSuspend] This suspend function requests an ans result by id
     *
     * @param id ANS ID
     * @param shouldIgnoreCache if true, we ignore caching for this call only
     * @return [Either] returns either Success ArcXPContentElement or Failure ArcXPException
     */
    suspend fun getContentSuspend(
        id: String,
        shouldIgnoreCache: Boolean = false
    ): Either<ArcXPException, ArcXPContentElement> =
        withContext(mIoScope.coroutineContext) {
            contentRepository.getContent(
                uuid = id,
                shouldIgnoreCache = shouldIgnoreCache
            )
        }

    /**
     * [getContentAsJson] This function requests a result by ANS ID as a json string
     *
     * returns result either through callback interface or livedata
     *
     * @param id ANS ID
     * @param listener Callback interface,
     * override [ArcXPContentCallback.onGetJsonSuccess] for success
     * override [ArcXPContentCallback.onError] for failure
     * or leave null and use livedata result and error livedata
     * @return [LiveData] subscribe to this livedata for successful results (or use callback interface)
     *
     * Note: each result is a stream, even the interface methods can possibly have additional results
     */
    fun getContentAsJson(
        id: String,
        listener: ArcXPContentCallback? = null
    ): LiveData<Either<ArcXPException, String>> {
        val stream = createLiveData<Either<ArcXPException, String>>()
        mIoScope.launch {
            stream.postValue(
                contentRepository.getContentAsJson(
                    id = id
                ).apply {
                    when (this) {
                        is Success -> listener?.onGetJsonSuccess(response = success)
                        is Failure -> listener?.onError(error = failure)
                    }
                })
        }
        return stream
    }

    private fun getContentByType(
        id: String,
        shouldIgnoreCache: Boolean,
        contentType: EventType,
        listener: ArcXPContentCallback?
    ): LiveData<Either<ArcXPException, ArcXPContentElement>> {
        //arcXPAnalyticsManager.sendAnalytics(event = contentType)
        val stream =
            createLiveData<Either<ArcXPException, ArcXPContentElement>>()
        mIoScope.launch {
            contentRepository.getContent(
                uuid = id,
                shouldIgnoreCache = shouldIgnoreCache
            ).apply {
                when (this) {
                    is Success -> {
                        if (success.type == contentType.value) {
                            listener?.onGetContentSuccess(response = success)
                            stream.postValue(Success(success = success))
                        } else {
                            val notCorrectTypeError = createArcXPException(
                                type = ArcXPSDKErrorType.SERVER_ERROR,
                                message = "Result did not match the given type: ${contentType.value}"
                            )
                            listener?.onError(error = notCorrectTypeError)
                            stream.postValue(Failure(failure = notCorrectTypeError))
                        }
                    }

                    is Failure -> {
                        listener?.onError(error = failure)
                        stream.postValue(Failure(failure = failure))
                    }
                }
            }
        }
        return stream
    }

    private fun fetchArcXPStory(
        id: String,
        shouldIgnoreCache: Boolean,
        listener: ArcXPContentCallback?
    ): LiveData<Either<ArcXPException, ArcXPStory>> {
        //arcXPAnalyticsManager.sendAnalytics(event = EventType.STORY)
        val stream =
            createLiveData<Either<ArcXPException, ArcXPStory>>()
        mIoScope.launch {
            contentRepository.getStory(
                uuid = id,
                shouldIgnoreCache = shouldIgnoreCache
            ).apply {
                when (this) {
                    is Success -> {
                        if (success.type == EventType.STORY.value) {
                            listener?.onGetStorySuccess(response = success)
                            stream.postValue(Success(success = success))
                        } else {
                            val notCorrectTypeError = createArcXPException(
                                type = ArcXPSDKErrorType.SERVER_ERROR,
                                message = "Result was not a story"
                            )
                            listener?.onError(error = notCorrectTypeError)
                            stream.postValue(Failure(failure = notCorrectTypeError))
                        }
                    }

                    is Failure -> {
                        listener?.onError(error = failure)
                        stream.postValue(Failure(failure = failure))
                    }
                }
            }
        }
        return stream
    }

    /**
     * This function requests a gallery result by ANS ID
     *
     * returns result with ans type = "gallery" either through callback interface or livedata
     *
     * @param id ANS ID
     * @param listener Callback interface,
     * override [ArcXPContentCallback.onGetContentSuccess] for success
     * override [ArcXPContentCallback.onError] for failure
     * or leave [listener] null and use livedata result stream and error livedata stream
     * @param shouldIgnoreCache if true, we ignore caching for this call only
     * @return [LiveData] subscribe to this livedata for successful results (or use callback interface)
     *
     * Note: each result is a stream, even the interface methods can possibly have additional results
     */
    fun getGallery(
        id: String,
        listener: ArcXPContentCallback? = null,
        shouldIgnoreCache: Boolean = false
    ): LiveData<Either<ArcXPException, ArcXPContentElement>> =
        getContentByType(
            id = id,
            shouldIgnoreCache = shouldIgnoreCache,
            contentType = EventType.GALLERY,
            listener = listener
        )


    /**
     * This function requests a video result by ANS ID
     *
     * returns result with ans type = "video" either through callback interface or livedata
     *
     * @param id ANS ID
     * @param listener Callback interface,
     * override [ArcXPContentCallback.onGetContentSuccess] for success
     * override [ArcXPContentCallback.onError] for failure
     * or leave [listener] null and use livedata result stream and error livedata stream
     * @return [LiveData] subscribe to this livedata for successful results (or use callback interface)
     *
     * Note: each result is a stream, even the interface methods can possibly have additional results
     */
    @Deprecated("Use findByUuid() in Video SDK")
    fun getVideo(
        id: String,
        listener: ArcXPContentCallback? = null,
        shouldIgnoreCache: Boolean = false
    ): LiveData<Either<ArcXPException, ArcXPContentElement>> = getContentByType(
        id = id,
        shouldIgnoreCache = shouldIgnoreCache,
        contentType = EventType.VIDEO,
        listener = listener
    )

    /**
     * This function requests list of section headers that are used for navigation
     * It is expected these correlate with existing collections
     *
     * returns result either through callback interface or livedata
     *
     * @param listener Callback interface,
     * override [ArcXPContentCallback.onGetSectionsSuccess] for success
     * override [ArcXPContentCallback.onError] for failure
     * or leave null and use livedata result and error livedata
     * @param shouldIgnoreCache if true, we ignore caching for this call only
     * @return [LiveData] subscribe to this livedata for successful results (or use callback interface)
     *
     * Note: each result is a stream, even the interface methods can possibly have additional results
     */
    fun getSectionList(
        listener: ArcXPContentCallback? = null,
        shouldIgnoreCache: Boolean = false
    ): LiveData<Either<ArcXPException, List<ArcXPSection>>> {
        //arcXPAnalyticsManager.sendAnalytics(EventType.NAVIGATION)
        val stream =
            createLiveData<Either<ArcXPException, List<ArcXPSection>>>()
        mIoScope.launch {
            contentRepository.getSectionList(shouldIgnoreCache = shouldIgnoreCache).apply {
                when (this) {
                    is Success -> {
                        listener?.onGetSectionsSuccess(success)
                        stream.postValue(this)
                    }

                    is Failure -> {
                        listener?.onError(
                            createArcXPException(
                                type = ArcXPSDKErrorType.SERVER_ERROR,
                                message = application.getString(R.string.section_load_failure)
                            )
                        )
                        stream.postValue(
                            Failure(
                                createArcXPException(
                                    ArcXPSDKErrorType.SERVER_ERROR,
                                    application.getString(R.string.section_load_failure)
                                )
                            )
                        )
                    }
                }
            }
        }
        return stream
    }

    /**
     * [getSectionListSuspend] This suspend function requests list of section headers that are used for navigation
     * It is expected these results correlate with existing collections for subsequent requests
     *
     * @param shouldIgnoreCache if true, we ignore caching for this call only
     * @return [Either] returns either Success List<ArcXPSection> or Failure ArcXPException
     */
    suspend fun getSectionListSuspend(shouldIgnoreCache: Boolean = false): Either<ArcXPException, List<ArcXPSection>> =
        withContext(mIoScope.coroutineContext) {
            //arcXPAnalyticsManager.sendAnalytics(EventType.NAVIGATION)
            contentRepository.getSectionList(shouldIgnoreCache = shouldIgnoreCache)
        }

    /**
     * This function requests list of section headers as a json string that are used for navigation
     * It is expected these correlate with existing collections
     *
     * returns result either through callback interface or livedata
     *
     * @param listener Callback interface,
     * override [ArcXPContentCallback.onGetJsonSuccess] for success
     * override [ArcXPContentCallback.onError] for failure
     * or leave null and use livedata result and error livedata
     * @return [LiveData] subscribe to this livedata for successful results (or use callback interface)
     *
     * Note: each result is a stream, even the interface methods can possibly have additional results
     */
    fun getSectionListAsJson(
        listener: ArcXPContentCallback? = null
    ): LiveData<Either<ArcXPException, String>> {
        //arcXPAnalyticsManager.sendAnalytics(EventType.NAVIGATION)
        val stream =
            createLiveData<Either<ArcXPException, String>>()
        mIoScope.launch {
            contentRepository.getSectionListAsJson().apply {
                when (this) {
                    is Success -> {
                        listener?.onGetJsonSuccess(success)
                        stream.postValue(this)
                    }

                    is Failure -> {
                        listener?.onError(
                            createArcXPException(
                                ArcXPSDKErrorType.SERVER_ERROR,
                                application.getString(R.string.section_load_failure)
                            )
                        )
                        stream.postValue(
                            Failure(
                                createArcXPException(
                                    ArcXPSDKErrorType.SERVER_ERROR,
                                    application.getString(R.string.section_load_failure)
                                )
                            )
                        )
                    }
                }
            }
        }
        return stream
    }

    /**
     * [getContentAsJsonSuspend] - request content element as JSON by ANS id
     * Note this should be a troubleshooting function, does not use cache
     * @param id searches for this ANS id (first through db if enabled, then api if not or stale)
     */
    suspend fun getContentAsJsonSuspend(
        id: String
    ): Either<ArcXPException, String> =
        withContext(mIoScope.coroutineContext) {
            contentRepository.getContentAsJson(id = id)
        }

    /**
     * [getCollectionAsJsonSuspend] - request collection by content alias
     * Note this should be a troubleshooting function, does not use cache
     * @param id searches for this id
     * @param from starting index to return results, ie 0 for page 1, 20(size) for page 2
     * @param size number of results to return
     * @return [Either] Json string or [ArcXPException]
     */
    suspend fun getCollectionAsJsonSuspend(
        id: String,
        from: Int = 0,
        size: Int = DEFAULT_PAGINATION_SIZE
    ): Either<ArcXPException, String> =
        withContext(mIoScope.coroutineContext) {
            //arcXPAnalyticsManager.sendAnalytics(EventType.COLLECTION)
            contentRepository.getCollectionAsJson(id = id, from = from, size = size)
        }

    /**
     * [getSectionListAsJsonSuspend] - request section lists / navigation
     * Note this should be a troubleshooting function, does not use cache
     */
    suspend fun getSectionListAsJsonSuspend(): Either<ArcXPException, String> =
        withContext(mIoScope.coroutineContext) {
            //arcXPAnalyticsManager.sendAnalytics(event = EventType.NAVIGATION)
            contentRepository.getSectionListAsJson()
        }

}