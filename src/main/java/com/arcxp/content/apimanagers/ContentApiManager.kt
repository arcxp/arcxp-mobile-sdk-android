package com.arcxp.content.apimanagers

import com.arcxp.ArcXPMobileSDK.application
import com.arcxp.ArcXPMobileSDK.contentConfig
import com.arcxp.commons.throwables.ArcXPException
import com.arcxp.commons.throwables.ArcXPSDKErrorType
import com.arcxp.commons.util.Constants
import com.arcxp.commons.util.Constants.expires
import com.arcxp.commons.util.DependencyFactory
import com.arcxp.commons.util.DependencyFactory.createArcXPException
import com.arcxp.commons.util.Either
import com.arcxp.commons.util.Failure
import com.arcxp.commons.util.Success
import com.arcxp.commons.util.Utils.determineExpiresAt
import com.arcxp.content.extendedModels.ArcXPContentElement
import com.arcxp.content.retrofit.ContentService
import com.arcxp.content.retrofit.NavigationService
import com.arcxp.sdk.R
import java.util.Date

/**
 * @suppress
 */
class ContentApiManager(
    private val contentService: ContentService = DependencyFactory.createContentService(),
    private val navigationService: NavigationService = DependencyFactory.createNavigationService()
) {
    //this function returns a pair of json response, expires date
    //or an error from response
    suspend fun getCollection(
        collectionAlias: String,
        from: Int,
        size: Int,
        full: Boolean = false
    ): Either<ArcXPException, Pair<String, Date>> =
        try {
            val response = if (full) {
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
                else -> {
                    Failure(
                        createArcXPException(
                            type = ArcXPSDKErrorType.SERVER_ERROR,
                            message = application().getString(R.string.get_collection_failure_message, response.errorBody())
                        )
                    )
                }
            }
        } catch (e: Exception) {
            Failure(
                createArcXPException(
                    type = ArcXPSDKErrorType.SERVER_ERROR,
                    message = application().getString(R.string.get_collection_failure_message, e.message)
                )
            )
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
                else -> {
                    Failure(
                        createArcXPException(
                            type = ArcXPSDKErrorType.SEARCH_ERROR,
                            message = "Search Call Failure: ${response.errorBody()}"
                        )
                    )
                }
            }
        } catch (e: Exception) {
            Failure(
                createArcXPException(
                    type = ArcXPSDKErrorType.SEARCH_ERROR,
                    message = "Search Call Error: $searchTerm"
                )
            )
        }

    suspend fun searchCollection(
        searchTerm: String,
        from: Int = 0,
        size: Int = Constants.DEFAULT_PAGINATION_SIZE
    ): Either<ArcXPException, Map<Int, ArcXPContentElement>> =
        try {
            val response =
                contentService.searchCollection(
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
                else -> {
                    Failure(
                        createArcXPException(
                            type = ArcXPSDKErrorType.SEARCH_ERROR,
                            message = "Search Collection Call Failure: ${response.errorBody()}"
                        )
                    )
                }
            }
        } catch (e: Exception) {
            Failure(
                createArcXPException(
                    type = ArcXPSDKErrorType.SEARCH_ERROR,
                    message = "Search Collection Call Error: $searchTerm"
                )
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
                else -> {
                    Failure(
                        createArcXPException(
                            type = ArcXPSDKErrorType.SEARCH_ERROR,
                            message = "Search Call Failure: ${response.errorBody()}"
                        )
                    )
                }
            }
        } catch (e: Exception) {
            Failure(
                createArcXPException(
                    type = ArcXPSDKErrorType.SEARCH_ERROR,
                    message = "Search Call Error: $searchTerm"
                )
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
                else -> {
                    Failure(
                        createArcXPException(
                            type = ArcXPSDKErrorType.SERVER_ERROR,
                            message = "Error: ${response.errorBody()}"
                        )
                    )
                }
            }
        } catch (e: Exception) {
            Failure(
                createArcXPException(
                    type = ArcXPSDKErrorType.SERVER_ERROR,
                    message = "Get Content Call Error for ANS id:$id"
                )
            )
        }


    suspend fun getSectionList(): Either<ArcXPException, Pair<String, Date>> =
        try {
            val response =
                navigationService.getSectionList(endpoint = contentConfig().navigationEndpoint)

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
                else -> {
                    Failure(
                        createArcXPException(
                            type = ArcXPSDKErrorType.SERVER_ERROR,
                            message = "Unable to get navigation"
                        )
                    )
                }
            }
        } catch (e: Exception) {
            Failure(
                createArcXPException(
                    type = ArcXPSDKErrorType.SERVER_ERROR,
                    message = "Unable to get navigation"
                )
            )
        }
}

