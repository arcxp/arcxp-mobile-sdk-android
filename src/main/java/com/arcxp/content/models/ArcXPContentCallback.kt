package com.arcxp.content.models

import com.arcxp.content.sdk.extendedModels.ArcXPCollection
import com.arcxp.content.sdk.extendedModels.ArcXPContentElement
import com.arcxp.content.sdk.extendedModels.ArcXPStory

/**
 * callback interface
 *
 * override related methods to receive response or errors
 *
 */
interface ArcXPContentCallback {
    fun onGetCollectionSuccess(response: Map<Int, ArcXPCollection>) {}
    fun onGetContentSuccess(response: ArcXPContentElement) {}
    fun onGetStorySuccess(response: ArcXPStory) {}
    fun onSearchSuccess(response: Map<Int, ArcXPContentElement>) {}
    fun onGetSectionsSuccess(response: List<ArcXPSection>) {}
    fun onGetJsonSuccess(response: String) {}
    fun onError(error: ArcXPContentError) {}
}