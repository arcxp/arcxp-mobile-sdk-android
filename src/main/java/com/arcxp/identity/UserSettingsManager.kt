package com.arcxp.identity

import android.util.Log
import com.arcxp.commerce.apimanagers.IdentityApiManager
import com.arcxp.commerce.callbacks.ArcXPIdentityListener
import com.arcxp.commerce.extendedModels.ArcXPProfileManage
import com.arcxp.commerce.models.ArcXPAttribute
import com.arcxp.commerce.models.ArcXPAttributeRequest
import com.arcxp.commerce.models.ArcXPProfilePatchRequest
import com.arcxp.commerce.models.TopicSubscription
import com.arcxp.commons.throwables.ArcXPException
import com.arcxp.commons.util.MoshiController.fromJsonList
import com.arcxp.commons.util.MoshiController.toJson

class UserSettingsManager(val identityApiManager: IdentityApiManager) {

    /* attributes as they arrive from server in profile, caching here to easily access custom attributes */
    private var currentAttributes = mutableListOf<ArcXPAttribute>()

    /* list of currently subscribed topics */
    private var currentSubscribedTopics = mutableListOf<TopicSubscription>()

    /* list of favorite uuids: articles */
    private var currentFavoriteArticles = mutableListOf<String>()

    /* list of favorite uuids: videos */
    private var currentFavoriteVideos = mutableListOf<String>()

    companion object {
        const val PUSH_NOTIFICATIONS_TOPICS_KEY = "topic list"
        const val FAVORITE_ARTICLES_KEY = "favorite article uuids"
        const val FAVORITE_VIDEOS_KEY = "favorite video uuids"
    }

    fun setCurrentAttributes(attributes: List<ArcXPAttribute>) {//TODO make private, call from within sdk upon profile success
        //this function should run from sdk when we get profile request, so should set/replace our local values

        //reset local attributes
        currentAttributes = attributes.toMutableList()
        currentSubscribedTopics.clear()

        //if the given list contains topics, store in currentSubscribedTopics for easy access
        currentAttributes.find { it.name == PUSH_NOTIFICATIONS_TOPICS_KEY }?.let {
            val list = fromJsonList(it.value, TopicSubscription::class.java)
            list?.let {
                currentSubscribedTopics = list.toMutableList()
            }
        }

    }

    fun clearAttributes() { //don't think you can clear this explicitly but you can replace values like so (must be >= 1 char)
        setAttributesOnBackEnd(
            attributes = listOf(
                ArcXPAttributeRequest(
                    name = PUSH_NOTIFICATIONS_TOPICS_KEY,
                    value = " ",
                    type = "String"
                ),
                ArcXPAttributeRequest(name = FAVORITE_ARTICLES_KEY, value = " ", type = "String"),
                ArcXPAttributeRequest(name = FAVORITE_VIDEOS_KEY, value = " ", type = "String")
            ),
        )
    }

    fun getAttribute(key: String) = currentAttributes.find { it.name == key }
    fun setAttribute(key: String, value: String, type: String) {
        //set locally
        //check both locals to update if it exists
        val existingAttribute = currentAttributes.find { it.name == key }
        if (existingAttribute != null) {
            existingAttribute.value = value
        } else {
            currentAttributes.add(ArcXPAttribute(name = key, value = value, type = type))
        }


        //then set on back end
//        setCurrentAttributes(attributes = currentAttributes)

        //need list of arcxp profile request from attributes
        val list = mutableListOf<ArcXPAttributeRequest>()
        currentAttributes.forEach {
            list.add(ArcXPAttributeRequest(name = it.name, value = it.value, type = it.type))
        }
        setAttributesOnBackEnd(attributes = list)
    }

    fun getPushNotificationTopics() = currentSubscribedTopics.toList()

    fun setSubscribedPushNotificationTopics(
        newTopics: List<TopicSubscription>,
        arcXPIdentityListener: ArcXPIdentityListener? = null
    ) {
        //sets topics
        // (replacing any old topics on backend and locally without removing any other attributes)
        currentSubscribedTopics = newTopics.toMutableList()
        updateBackendWithCurrentAttributes()
//        val oldTopics = currentAttributes.find { it.name == PUSH_NOTIFICATIONS_TOPICS_KEY }
//        oldTopics?.let { currentAttributes.remove(oldTopics) }
//        val newList = mutableListOf<ArcXPAttributeRequest>()
//        currentAttributes.forEach {
//            newList.add(ArcXPAttributeRequest(name = it.name, value = it.value, type = it.type))
//        }
//        newList.add(
//            ArcXPAttributeRequest(
//                name = PUSH_NOTIFICATIONS_TOPICS_KEY,
//                value = toJson(newTopics)!!,
//                type = "String"
//            )
//        )
//        setAttributesOnBackEnd(attributes = newList, arcXPIdentityListener = arcXPIdentityListener)
    }

    private fun updateBackendWithCurrentAttributes() {
        val newList = mutableListOf<ArcXPAttributeRequest>()
        currentAttributes.forEach {
            if (it.name != PUSH_NOTIFICATIONS_TOPICS_KEY) {
                newList.add(ArcXPAttributeRequest(name = it.name, value = it.value, type = it.type))
            }
        }
        val oldTopics = currentAttributes.find { it.name == PUSH_NOTIFICATIONS_TOPICS_KEY }
        oldTopics?.let { currentAttributes.remove(it) }
        val json = toJson(currentSubscribedTopics)!!
        newList.add(
            ArcXPAttributeRequest(
                name = PUSH_NOTIFICATIONS_TOPICS_KEY,
                value = json,
                type = "String"
            )
        )
        currentAttributes.add(
            ArcXPAttribute(
                name = PUSH_NOTIFICATIONS_TOPICS_KEY,
                value = json,
                type = "String"
            )
        )
        setAttributesOnBackEnd(attributes = newList)
    }

    private fun setAttributesOnBackEnd(
        attributes: List<ArcXPAttributeRequest>,
        arcXPIdentityListener: ArcXPIdentityListener? = null
    ) {
        identityApiManager.updateProfile(
            ArcXPProfilePatchRequest(attributes = attributes), object : ArcXPIdentityListener() {
                override fun onProfileUpdateSuccess(profileManageResponse: ArcXPProfileManage) {
                    arcXPIdentityListener?.onProfileUpdateSuccess(profileManageResponse = profileManageResponse)
                    Log.d("vm", "on profile update success")//TODO should we update
                }

                override fun onProfileError(error: ArcXPException) {
                    arcXPIdentityListener?.onProfileError(error = error)
                    Log.d("vm", "on profile update failure")
                    Log.d("vm", error.message.orEmpty())
                }
            })
    }

    fun addTopic(topicSubscription: TopicSubscription) {
        val nameList = currentSubscribedTopics.map { it.name }
        val index = nameList.indexOf(topicSubscription.name)
        if (index >= 0) {//currently in list, update at index
            currentSubscribedTopics[index] = topicSubscription
        } else {//not currently in list, add
            currentSubscribedTopics.add(topicSubscription)
        }
        updateBackendWithCurrentAttributes()
    }

    fun removeTopic(topicSubscription: TopicSubscription) {
        val index = currentSubscribedTopics.map { it.name }.indexOf(topicSubscription.name)
        if (index >= 0) {//currently in list, update at index
            currentSubscribedTopics.removeAt(index)
        } else {//not currently in list
        }
        updateBackendWithCurrentAttributes()
    }


    fun getFavoriteArticles() = currentFavoriteArticles.toList()

    fun setFavoriteArticles(newUuids: List<String>) {
        currentFavoriteArticles = newUuids.toMutableList()
        //update backend
    }

    fun getFavoriteVideos() = currentFavoriteVideos.toList()

    fun setFavoriteVideos(newUuids: List<String>) {
        currentFavoriteVideos = newUuids.toMutableList()
        //update backend
    }

    fun addFavoriteVideo(newUuid: String) {
        currentFavoriteVideos.add(newUuid)
        //update backend
    }

    fun removeFavoriteVideo(newUuid: String) {
        currentFavoriteVideos.remove(newUuid)
        //update backend
    }

    fun addFavoriteArticle(newUuid: String) {
        currentFavoriteArticles.add(newUuid)
        //update backend
    }

    fun removeFavoriteArticle(newUuid: String) {
        currentFavoriteArticles.remove(newUuid)
        //update backend
    }
}