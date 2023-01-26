package com.arcxp.content.apimanagers

import com.arcxp.content.ArcXPContentSDK
import com.arcxp.content.extendedModels.ArcXPContentElement
import com.arcxp.content.models.ArcXPContentError
import com.arcxp.content.models.ArcXPContentSDKErrorType
import com.arcxp.content.retrofit.ContentService
import com.arcxp.content.retrofit.NavigationService
import com.arcxp.content.util.*
import java.util.*

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
        id: String,
        from: Int,
        size: Int,
        full: Boolean = false
    ): Either<ArcXPContentError, Pair<String, Date>> =
        try {
            val response = if (full) {
                contentService.getCollectionFull(id = id, from = from, size = size)
            } else {
                contentService.getCollection(id = id, from = from, size = size)
            }
            when {
                response.isSuccessful -> {
                    val json = response.body()!!.string()
                    val expiresAt =
                        determineExpiresAt(expiresAt = response.headers().get("expires")!!)
                    Success(Pair(json, expiresAt))
                }
                else -> {
                    Failure(
                        ArcXPContentError(
                            type = ArcXPContentSDKErrorType.SERVER_ERROR,
                            message = "Get Collection: ${response.errorBody()}"
                        )
                    )
                }
            }
        } catch (e: Exception) {
            Failure(
                ArcXPContentError(
                    type = ArcXPContentSDKErrorType.SERVER_ERROR,
                    message = "Get Collection: ${e.message}"
                )
            )
        }

    suspend fun search(
        searchTerm: String,
        from: Int = 0,
        size: Int = Constants.DEFAULT_PAGINATION_SIZE
    ): Either<ArcXPContentError, Map<Int, ArcXPContentElement>> =
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
                        ArcXPContentError(
                            type = ArcXPContentSDKErrorType.SEARCH_ERROR,
                            message = "Search Call Failure: ${response.errorBody()}"
                        )
                    )
                }
            }
        } catch (e: Exception) {
            Failure(
                ArcXPContentError(
                    type = ArcXPContentSDKErrorType.SEARCH_ERROR,
                    message = "Search Call Error: $searchTerm"
                )
            )
        }

    suspend fun searchVideos(
        searchTerm: String,
        from: Int = 0,
        size: Int = Constants.DEFAULT_PAGINATION_SIZE
    ): Either<ArcXPContentError, Map<Int, ArcXPContentElement>> =
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
                        ArcXPContentError(
                            type = ArcXPContentSDKErrorType.SEARCH_ERROR,
                            message = "Search Call Failure: ${response.errorBody()}"
                        )
                    )
                }
            }
        } catch (e: Exception) {
            Failure(
                ArcXPContentError(
                    type = ArcXPContentSDKErrorType.SEARCH_ERROR,
                    message = "Search Call Error: $searchTerm"
                )
            )
        }


    //this function returns a pair of json response, expires date
    //or an error from response
    suspend fun getContent(id: String): Either<ArcXPContentError, Pair<String, Date>> =
        try {
            val response = contentService.getContent(id = id)
            when {
                response.isSuccessful -> {
                    Success(
                        Pair(
                            response.body()!!.string(),
                            determineExpiresAt(response.headers().get("expires")!!)
                        )
                    )
                }
                else -> {
                    Failure(
                        ArcXPContentError(
                            type = ArcXPContentSDKErrorType.SERVER_ERROR,
                            message = "Error: ${response.errorBody()}"
                        )
                    )
                }
            }
        } catch (e: Exception) {
            Failure(
                ArcXPContentError(
                    type = ArcXPContentSDKErrorType.SERVER_ERROR,
                    message = "Get Content Call Error for ANS id:$id"
                )
            )
        }


    suspend fun getSectionList(): Either<ArcXPContentError, Pair<String, Date>> =
        try {
            val response =
                navigationService.getSectionList(endpoint = ArcXPContentSDK.arcxpContentConfig().navigationEndpoint)

            when {
                response.isSuccessful -> {
                    Success(
                        Pair(
                            response.body()!!.string(), determineExpiresAt(
                                expiresAt = response.headers().get("expires")!!
                            )
                        )
                    )
                }
                else -> {
                    Failure(
                        ArcXPContentError(
                            type = ArcXPContentSDKErrorType.SERVER_ERROR,
                            message = "Unable to get navigation"
                        )
                    )
                }
            }
        } catch (e: Exception) {
            Failure(
                ArcXPContentError(
                    type = ArcXPContentSDKErrorType.SERVER_ERROR,
                    message = "Unable to get navigation"
                )
            )
        }
}

