package com.arcxp.content.apimanagers

import android.app.Application
import com.arcxp.commons.throwables.ArcXPException
import com.arcxp.commons.util.Constants
import com.arcxp.commons.util.Constants.expires
import com.arcxp.commons.util.Either
import com.arcxp.commons.util.Success
import com.arcxp.commons.util.Utils.createFailure
import com.arcxp.commons.util.Utils.createNavFailure
import com.arcxp.commons.util.Utils.createSearchFailure
import com.arcxp.commons.util.Utils.determineExpiresAt
import com.arcxp.content.ArcXPContentConfig
import com.arcxp.content.extendedModels.ArcXPContentElement
import com.arcxp.content.retrofit.ContentService
import com.arcxp.content.retrofit.NavigationService
import com.arcxp.sdk.R
import java.util.Date

/**
 * ContentApiManager is responsible for managing content-related API calls within the ArcXP Commerce module.
 * It provides methods to fetch collections, search content, retrieve specific content, and get section lists.
 *
 * The class defines the following operations:
 * - Fetch a collection of content items
 * - Search for content items based on a search term
 * - Retrieve specific content by its ID
 * - Get a list of sections for a given site hierarchy
 *
 * Usage:
 * - Create an instance of ContentApiManager with the necessary configuration, application context, and service instances.
 * - Use the provided methods to interact with the content API.
 *
 * Example:
 *
 * val contentApiManager = ContentApiManager(contentConfig, application, contentService, navigationService)
 * val collectionResult = contentApiManager.getCollection("collectionAlias", 0, 10, true)
 * val searchResult = contentApiManager.search("searchTerm")
 * val contentResult = contentApiManager.getContent("contentId")
 * val sectionListResult = contentApiManager.getSectionList("siteHierarchy")
 *
 * Note: Ensure that the configuration, application context, and service instances are properly configured before using ContentApiManager.
 *
 * @method getCollection Fetch a collection of content items.
 * @method search Search for content items based on a search term.
 * @method searchAsJson Search for content items and return the result as a JSON string.
 * @method searchVideos Search for video content items based on a search term.
 * @method getContent Retrieve specific content by its ID.
 * @method getSectionList Get a list of sections for a given site hierarchy.
 */
class ContentApiManager(
    private val contentConfig: ArcXPContentConfig,
    private val application: Application,
    private val contentService: ContentService,
    private val navigationService: NavigationService,
) {
    //this function returns a pair of json response, expires date
    //or an error from response
    suspend fun getCollection(
        collectionAlias: String,
        from: Int,
        size: Int,
        full: Boolean?
    ): Either<ArcXPException, Pair<String, Date>> {
        //if unspecified(null) here from outer call, use preloading value here from initialization
        val finalFullChoice = full ?: contentConfig.preLoading
        return try {
            val response = if (finalFullChoice) {
                contentService.getCollectionFull(id = collectionAlias, from = from, size = size)
            } else {
                contentService.getCollection(id = collectionAlias, from = from, size = size)
            }
            when {
                response.isSuccessful -> {
                    val json = response.body()!!.string()
                    val expiresAt =
                        determineExpiresAt(expiresAt = response.headers()[expires]!!)
                    Success(Pair(json, expiresAt))
                }

                else -> createFailure(
                    message = application.getString(
                        R.string.get_collection_failure_message,
                        response.errorBody()!!.string()
                    )
                )
            }
        } catch (e: Exception) {
            createFailure(
                message = application.getString(
                    R.string.get_collection_failure_message,
                    e.message
                ),
                value = e
            )
        }
    }

    suspend fun search(
        searchTerm: String,
        from: Int = 0,
        size: Int = Constants.DEFAULT_PAGINATION_SIZE
    ): Either<ArcXPException, Map<Int, ArcXPContentElement>> =
        try {
            val response =
                contentService.search(
                    searchTerms = searchTerm,
                    from = from,
                    size = size
                )
            when {
                response.isSuccessful -> {
                    val list = response.body()!!
                    val map = HashMap<Int, ArcXPContentElement>()
                    list.forEachIndexed { index, arcXPSearchResponse ->
                        map[index + from] = arcXPSearchResponse
                    }
                    Success(map)
                }

                else -> createSearchFailure(
                    searchTerm = searchTerm,
                    message = response.errorBody()!!.string()
                )
            }
        } catch (e: Exception) {
            createSearchFailure(
                searchTerm = searchTerm,
                message = e.message, value = e
            )
        }

    suspend fun searchAsJson(
        searchTerm: String,
        from: Int = 0,
        size: Int = Constants.DEFAULT_PAGINATION_SIZE
    ): Either<ArcXPException, String> =
        try {
            val response =
                contentService.searchAsJson(
                    searchTerms = searchTerm,
                    from = from,
                    size = size
                )
            when {
                response.isSuccessful -> Success(response.body()!!.string())
                else -> {
                    val errorText = response.errorBody()!!.string()
                    createSearchFailure(
                        message = errorText,
                        searchTerm = searchTerm
                    )
                }
            }
        } catch (e: Exception) {
            createSearchFailure(
                message = e.message,
                searchTerm = searchTerm,
                value = e
            )
        }

    suspend fun searchVideos(
        searchTerm: String,
        from: Int = 0,
        size: Int = Constants.DEFAULT_PAGINATION_SIZE
    ): Either<ArcXPException, Map<Int, ArcXPContentElement>> =
        try {
            val response =
                contentService.searchVideos(
                    searchTerms = searchTerm,
                    from = from,
                    size = size
                )
            when {
                response.isSuccessful -> {
                    val list = response.body()!!
                    val map = HashMap<Int, ArcXPContentElement>()
                    list.forEachIndexed { index, arcXPSearchResponse ->
                        map[index + from] = arcXPSearchResponse
                    }
                    Success(map)
                }

                else -> createSearchFailure(
                    message = response.errorBody()!!.string(),
                    searchTerm = searchTerm
                )
            }
        } catch (e: Exception) {
            createSearchFailure(
                message = e.message,
                searchTerm = searchTerm,
                value = e
            )
        }


    //this function returns a pair of json response, expires date
    //or an error from response
    suspend fun getContent(id: String): Either<ArcXPException, Pair<String, Date>> =
        try {
            val response = contentService.getContent(id = id)
            when {
                response.isSuccessful -> {
                    Success(
                        Pair(
                            response.body()!!.string(),
                            determineExpiresAt(response.headers()["expires"]!!)
                        )
                    )
                }

                else -> createFailure(
                    message = application.getString(
                        R.string.content_failure_message,
                        id,
                        response.errorBody()!!.string()
                    )
                )
            }
        } catch (e: Exception) {
            createFailure(
                message = application.getString(
                    R.string.content_failure_message,
                    id,
                    e.message
                ), value = e
            )
        }


    suspend fun getSectionList(siteHierarchy: String): Either<ArcXPException, Pair<String, Date>> =
        try {
            val response =
                navigationService.getSectionList(siteHierarchy = siteHierarchy)
            when {
                response.isSuccessful -> {
                    Success(
                        Pair(
                            response.body()!!.string(), determineExpiresAt(
                                expiresAt = response.headers()["expires"]!!
                            )
                        )
                    )
                }

                else -> createNavFailure(message = response.errorBody()!!.string())
            }
        } catch (e: Exception) {
            createNavFailure(message = e.message, value = e)
        }
}
