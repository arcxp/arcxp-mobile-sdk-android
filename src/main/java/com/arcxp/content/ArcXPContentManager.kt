package com.arcxp.content

import android.app.Application
import androidx.annotation.Keep
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
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
import com.arcxp.commons.util.Utils
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
    private val contentConfig: ArcXPContentConfig,
    private val _contentLiveData: MutableLiveData<Either<ArcXPException, ArcXPContentElement>> = createLiveData(),
    private val _storyLiveData: MutableLiveData<Either<ArcXPException, ArcXPStory>> = createLiveData(),
    private val _collectionLiveData: MutableLiveData<Either<ArcXPException, Map<Int, ArcXPContentElement>>> = createLiveData(),
    private val _sectionListLiveData: MutableLiveData<Either<ArcXPException, List<ArcXPSection>>> = createLiveData(),
    private val _searchLiveData: MutableLiveData<Either<ArcXPException, Map<Int, ArcXPContentElement>>> = createLiveData(),
    private val _jsonLiveData: MutableLiveData<Either<ArcXPException, String>> = createLiveData(),
) {

    /** [contentLiveData] subscribe to this for generic content element results for search / section lists
     * (getVideo, getGallery return this additionally) */
    val contentLiveData: LiveData<Either<ArcXPException, ArcXPContentElement>> = _contentLiveData

    /** [storyLiveData] subscribe to this for story element results (getStory returns this
     * additionally) */
    val storyLiveData: LiveData<Either<ArcXPException, ArcXPStory>> = _storyLiveData

    /** [collectionLiveData]
     * subscribe to this for ordered list of collection with their server index (WebSked order)
     * in list as key (getCollection returns this additionally) */
    val collectionLiveData: LiveData<Either<ArcXPException, Map<Int, ArcXPContentElement>>> =
        _collectionLiveData


    /** [sectionListLiveData]
     * subscribe to this for navigation list from server
     * in list as key (getSectionList returns this additionally) */
    val sectionListLiveData: LiveData<Either<ArcXPException, List<ArcXPSection>>> =
        _sectionListLiveData

    /** [searchLiveData]
     * subscribe to this for ordered list of collection with their server index (search result order)
     * in list as key (search, searchVideo returns this additionally) */
    val searchLiveData: LiveData<Either<ArcXPException, Map<Int, ArcXPContentElement>>> =
        _searchLiveData

    /** [jsonLiveData]
     * subscribe to this for all raw json results as string
     * ( -AsJson methods return this additionally) */
    val jsonLiveData: LiveData<Either<ArcXPException, String>> = _jsonLiveData

    private fun notCorrectTypeError(inputType: String, expectedType: String) = createArcXPException(
        type = ArcXPSDKErrorType.SERVER_ERROR,
        message = application.getString(R.string.incorrect_type, inputType, expectedType)
    )

    init {
        AuthManager.accessToken = application.getString(R.string.bearer_token)
    }

    /**
     * This function requests a collection result by content alias
     *
     * returns result either through callback interface or livedata
     *
     * @param collectionAlias Content Alias
     * @param listener Callback interface for optional callback
     *
     * override [ArcXPContentCallback.onGetCollectionSuccess] for success
     *
     * override [ArcXPContentCallback.onError] for failure
     *
     * or leave null and use livedata result and error livedata
     * @param shouldIgnoreCache if true, we ignore caching for this call only
     * @param from index in which to start (ie for pagination, you may want to start at index for next page)
     * @param size number of entries to request: (valid range [VALID_COLLECTION_SIZE_RANGE], will coerce parameter into this range if it is outside)
     * @return [LiveData] subscribe to this livedata for successful results (or use optional callback interface)
     *
     * Note: additional results will come to same live data returned, listener will have individual results per call
     */
    fun getCollection(
        collectionAlias: String,
        listener: ArcXPContentCallback? = null,
        shouldIgnoreCache: Boolean = false,
        from: Int = 0,
        size: Int = DEFAULT_PAGINATION_SIZE
    ): LiveData<Either<ArcXPException, Map<Int, ArcXPContentElement>>> {
        mIoScope.launch {
            _collectionLiveData.postValue(contentRepository.getCollection(
                collectionAlias = collectionAlias,
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
        return collectionLiveData
    }

    /**
     * [getCollectionSuspend] this suspend function requests a collection result by content alias
     * @param collectionAlias Content Alias
     * @param shouldIgnoreCache if true, we ignore caching for this call only
     * @param from index in which to start (ie for pagination, you may want to start at index for next page)
     * @param size number of entries to request: (valid range [VALID_COLLECTION_SIZE_RANGE], will coerce parameter into this range if it is outside)
     * @return [Either] returns either Success Map<Int, ArcXPContentElement> with collections in their desired order from server or Failure ArcXPException
     */
    suspend fun getCollectionSuspend(
        collectionAlias: String,
        shouldIgnoreCache: Boolean = false,
        from: Int = 0,
        size: Int = DEFAULT_PAGINATION_SIZE
    ): Either<ArcXPException, Map<Int, ArcXPContentElement>> =
        withContext(mIoScope.coroutineContext) {
            contentRepository.getCollection(
                collectionAlias = collectionAlias,
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
     * @param listener Callback interface for optional callback
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
        collectionAlias = contentConfig.videoCollectionName,
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
     * @param listener Callback interface for optional callback
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
        mIoScope.launch {
            _jsonLiveData.postValue(contentRepository.getCollectionAsJson(
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
        return jsonLiveData
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
     * @param listener Callback interface for optional callback
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
        mIoScope.launch {
            val regPattern = Regex("[^A-Za-z0-9,\\- ]")
            val searchTermsChecked = regPattern.replace(searchTerm, "")
            _searchLiveData.postValue(
                contentRepository.searchSuspend(
                    searchTerm = searchTermsChecked,
                    from = from,
                    size = size.coerceIn(VALID_COLLECTION_SIZE_RANGE)
                ).apply {
                    when (this) {
                        is Success -> listener?.onSearchSuccess(success)
                        is Failure -> listener?.onError(failure)
                    }
                }
            )
        }
        return searchLiveData
    }

    /**
     * This function requests a search to be performed by search Term (keyword/tag based on resolver setup default is tag(used in example app))
     * note: cache is not used for search, but if you open item it will be cached
     *
     * returns result either through callback interface or livedata
     *
     * @param searchTerm string to search (searches TAG by default)
     * @param listener Callback interface for optional callback
     * override [ArcXPContentCallback.onSearchSuccess] for success
     * override [ArcXPContentCallback.onError] for failure
     * or leave [listener] null and use livedata result and error livedata
     * @param from index in which to start (ie for pagination, you may want to start at index for next page)
     * @param size number of entries to request: (valid range [VALID_COLLECTION_SIZE_RANGE], will coerce parameter into this range if it is outside)
     * @return [LiveData] subscribe to this livedata for successful results (or use callback interface)
     */
    fun searchAsJson(
        searchTerm: String,
        listener: ArcXPContentCallback? = null,
        from: Int = 0,
        size: Int = DEFAULT_PAGINATION_SIZE
    ): LiveData<Either<ArcXPException, String>> {
        mIoScope.launch {
            val regPattern = Regex("[^A-Za-z0-9,\\- ]")
            val searchTermsChecked = regPattern.replace(searchTerm, "")
            _jsonLiveData.postValue(
                contentRepository.searchAsJsonSuspend(
                    searchTerm = searchTermsChecked,
                    from = from,
                    size = size.coerceIn(VALID_COLLECTION_SIZE_RANGE)
                ).apply {
                    when (this) {
                        is Success -> listener?.onGetJsonSuccess(success)
                        is Failure -> listener?.onError(failure)
                    }
                }
            )
        }
        return jsonLiveData
    }

    /**
     * This function requests a search to be performed by search Term (keyword/tag based on resolver setup default is tag(used in example app))
     * note: cache is not used for search, but if you open item it will be cached
     *
     * returns result either through callback interface or livedata
     *
     * @param searchTerms list of strings to search (searches TAG by default)
     * @param listener Callback interface for optional callback
     * override [ArcXPContentCallback.onSearchSuccess] for success
     * override [ArcXPContentCallback.onError] for failure
     * or leave [listener] null and use livedata result and error livedata
     * @param from index in which to start (ie for pagination, you may want to start at index for next page)
     * @param size number of entries to request: (valid range [VALID_COLLECTION_SIZE_RANGE], will coerce parameter into this range if it is outside)
     * @return [LiveData] subscribe to this livedata for successful results (or use callback interface)
     */
    fun searchAsJson(
        searchTerms: List<String>,
        listener: ArcXPContentCallback? = null,
        from: Int = 0,
        size: Int = DEFAULT_PAGINATION_SIZE
    ): LiveData<Either<ArcXPException, String>> =
        searchAsJson(searchTerms.joinToString(separator = ","), listener, from, size)

    /**
     * [searchAsJsonSuspend]This function requests a search to be performed by search Term (keyword/tag based on resolver setup default is tag(used in example app))
     * note: cache is not used for search, but if you open item it will be cached
     *
     * returns result either
     *
     * @param searchTerm string to search (searches TAG by default)
     * @param from index in which to start (ie for pagination, you may want to start at index for next page)
     * @param size number of entries to request: (valid range [VALID_COLLECTION_SIZE_RANGE], will coerce parameter into this range if it is outside)
     */
    suspend fun searchAsJsonSuspend(
        searchTerm: String,
        from: Int = 0,
        size: Int = DEFAULT_PAGINATION_SIZE
    ): Either<ArcXPException, String> {
        return withContext(mIoScope.coroutineContext) {
            val regPattern = Regex("[^A-Za-z0-9,\\- ]")
            val searchTermsChecked = regPattern.replace(searchTerm, "")
            return@withContext contentRepository.searchAsJsonSuspend(
                searchTerm = searchTermsChecked,
                from = from,
                size = size.coerceIn(VALID_COLLECTION_SIZE_RANGE)
            )
        }
    }

    /**
     * [searchAsJsonSuspend] this function requests a search to be performed by search Term
     * (keyword/tag based on resolver setup default is tag(used in example app))
     * note: cache is not used for search, but if you open item it will be cached
     *
     * returns result either
     *
     * @param searchTerms list of strings to search (searches TAG by default)
     * @param from index in which to start (ie for pagination, you may want to start at index for next page)
     * @param size number of entries to request: (valid range [VALID_COLLECTION_SIZE_RANGE], will coerce parameter into this range if it is outside)
     */
    suspend fun searchAsJsonSuspend(
        searchTerms: List<String>,
        from: Int = 0,
        size: Int = DEFAULT_PAGINATION_SIZE
    ): Either<ArcXPException, String> =
        searchAsJsonSuspend(
            searchTerm = searchTerms.joinToString(separator = ","),
            from = from,
            size = size.coerceIn(VALID_COLLECTION_SIZE_RANGE)
        )

    /**
     * This function requests a search to be performed by search Term (keyword/tag based on resolver setup default is tag(used in example app))
     * note: cache is not used for search, but if you open item it will be cached
     *
     * returns result either through callback interface or livedata
     *
     * @param searchTerm string to search (searches TAG by default)
     * @param listener Callback interface for optional callback
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
        mIoScope.launch {
            val regPattern = Regex("[^A-Za-z0-9,\\- ]")
            val searchTermsChecked = regPattern.replace(searchTerm, "")
            _searchLiveData.postValue(
                contentRepository.searchVideosSuspend(
                    searchTerm = searchTermsChecked,
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
        return searchLiveData
    }

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
            contentRepository.searchSuspend(
                searchTerm = searchTermChecked,
                from = from,
                size = size.coerceIn(VALID_COLLECTION_SIZE_RANGE)
            )
        }
    }

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
            contentRepository.searchVideosSuspend(
                searchTerm = searchTermChecked,
                from = from,
                size = size.coerceIn(VALID_COLLECTION_SIZE_RANGE)
            )
        }
    }

    /**
     * [getArcXPStory] This function requests a story / article result by ANS ID
     *
     * returns result with ans type = "story" either through callback interface or livedata
     *
     * @param id ANS ID
     * @param listener Callback interface for optional callback
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
    ): LiveData<Either<ArcXPException, ArcXPStory>> {
        mIoScope.launch {
            contentRepository.getStory(
                uuid = id,
                shouldIgnoreCache = shouldIgnoreCache
            ).apply {
                when (this) {
                    is Success -> {
                        if (success.type == EventType.STORY.value) {
                            listener?.onGetStorySuccess(response = success)
                            _storyLiveData.postValue(Success(success = success))
                        } else {
                            val error = notCorrectTypeError(
                                inputType = success.type,
                                expectedType = Utils.AnsTypes.STORY.type
                            )
                            listener?.onError(error = error)
                            _storyLiveData.postValue(Failure(failure = error))
                        }
                    }

                    is Failure -> {
                        listener?.onError(error = failure)
                        _storyLiveData.postValue(Failure(failure = failure))
                    }
                }
            }
        }
        return storyLiveData
    }

    /**
     * [getArcXPStorySuspend] This function requests a story / article result by ANS ID
     *
     * returns result with ans type = "story"
     *
     * @param id ANS ID
     * @param shouldIgnoreCache if true, we ignore caching for this call only
     * @return [Either] Success [ArcXPStory] failure [ArcXPException]
     *
     * Note: each result is a stream, even the interface methods can possibly have additional results
     */
    suspend fun getArcXPStorySuspend(
        id: String,
        shouldIgnoreCache: Boolean = false
    ): Either<ArcXPException, ArcXPStory> =
        withContext(mIoScope.coroutineContext) {
            contentRepository.getStory(
                uuid = id,
                shouldIgnoreCache = shouldIgnoreCache
            ).apply {
                return@withContext when (this) {
                    is Success -> {
                        if (success.type == EventType.STORY.value) {
                            Success(success = success)
                        } else {
                            Failure(
                                failure = notCorrectTypeError(
                                    inputType = success.type,
                                    expectedType = Utils.AnsTypes.STORY.type
                                )
                            )
                        }
                    }

                    is Failure -> {
                        Failure(failure = failure)
                    }
                }
            }
        }


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
     * @param listener Callback interface for optional callback
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
        mIoScope.launch {
            _jsonLiveData.postValue(
                contentRepository.getContentAsJson(
                    id = id
                ).apply {
                    when (this) {
                        is Success -> listener?.onGetJsonSuccess(response = success)
                        is Failure -> listener?.onError(error = failure)
                    }
                })
        }
        return jsonLiveData
    }

    private fun getContentByType(
        id: String,
        shouldIgnoreCache: Boolean,
        contentType: Utils.AnsTypes,
        listener: ArcXPContentCallback?
    ): LiveData<Either<ArcXPException, ArcXPContentElement>> {
        mIoScope.launch {
            contentRepository.getContent(
                uuid = id,
                shouldIgnoreCache = shouldIgnoreCache
            ).apply {
                when (this) {
                    is Success -> {
                        if (success.type == contentType.type) {
                            listener?.onGetContentSuccess(response = success)
                            _contentLiveData.postValue(Success(success = success))
                        } else {
                            val error = notCorrectTypeError(
                                inputType = success.type,
                                expectedType = contentType.type
                            )
                            listener?.onError(error = error)
                            _contentLiveData.postValue(Failure(failure = error))
                        }
                    }

                    is Failure -> {
                        listener?.onError(error = failure)
                        _contentLiveData.postValue(Failure(failure = failure))
                    }
                }
            }
        }
        return contentLiveData
    }

    /**
     * This function requests a gallery result by ANS ID
     *
     * returns result with ans type = "gallery" either through callback interface or livedata
     *
     * @param id ANS ID
     * @param listener Callback interface for optional callback
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
            contentType = Utils.AnsTypes.GALLERY,
            listener = listener
        )


    /**
     * This function requests a video result by ANS ID
     *
     * returns result with ans type = "video" either through callback interface or livedata
     *
     * @param id ANS ID
     * @param listener Callback interface for optional callback
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
        contentType = Utils.AnsTypes.VIDEO,
        listener = listener
    )

    /**
     * This function requests list of section headers that are used for navigation
     * It is expected these correlate with existing collections
     *
     * returns result either through callback interface or livedata
     *
     * @param listener Callback interface for optional callback
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
        mIoScope.launch {
            _sectionListLiveData.postValue(
                contentRepository.getSectionList(shouldIgnoreCache = shouldIgnoreCache).apply {
                    when (this) {
                        is Success -> listener?.onGetSectionsSuccess(response = success)
                        is Failure -> listener?.onError(error = failure)
                    }
                })
        }
        return sectionListLiveData
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
            contentRepository.getSectionList(shouldIgnoreCache = shouldIgnoreCache)
        }

    /**
     * This function requests list of section headers as a json string that are used for navigation
     * It is expected these correlate with existing collections
     *
     * returns result either through callback interface or livedata
     *
     * @param listener Callback interface for optional callback
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
        mIoScope.launch {
            _jsonLiveData.postValue(contentRepository.getSectionListAsJson().apply {
                when (this) {
                    is Success -> {
                        listener?.onGetJsonSuccess(success)
                    }

                    is Failure -> {
                        listener?.onError(error = failure)
                    }
                }
            })
        }
        return jsonLiveData
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
            contentRepository.getCollectionAsJson(id = id, from = from, size = size)
        }

    /**
     * [getSectionListAsJsonSuspend] - request section lists / navigation
     * Note this should be a troubleshooting function, does not use cache
     */
    suspend fun getSectionListAsJsonSuspend(): Either<ArcXPException, String> =
        withContext(mIoScope.coroutineContext) {
            contentRepository.getSectionListAsJson()
        }

    /** [deleteCollection]
     * @param collectionAlias String matching collection to delete from cache* remove all entries from database */
    fun deleteCollection(collectionAlias: String) =
        contentRepository.deleteCollection(collectionAlias = collectionAlias)

    /** [deleteItem]
     * @param uuid remove cache entry by uuid */
    fun deleteItem(uuid: String) = contentRepository.deleteItem(uuid = uuid)

    /** [deleteCache]
     * removes all entries from database */
    fun deleteCache() = contentRepository.deleteCache()
}