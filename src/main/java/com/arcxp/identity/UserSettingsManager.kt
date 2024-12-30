package com.arcxp.identity

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.arcxp.commerce.apimanagers.IdentityApiManager
import com.arcxp.commerce.callbacks.ArcXPIdentityListener
import com.arcxp.commerce.extendedModels.ArcXPProfileManage
import com.arcxp.commerce.models.ArcXPAttribute
import com.arcxp.commerce.models.ArcXPAttributeRequest
import com.arcxp.commerce.models.ArcXPProfilePatchRequest
import com.arcxp.commerce.models.TopicSubscription
import com.arcxp.commons.throwables.ArcXPException
import com.arcxp.commons.throwables.ArcXPSDKErrorType
import com.arcxp.commons.util.DependencyFactory.createArcXPException
import com.arcxp.commons.util.MoshiController.fromJsonList
import com.arcxp.commons.util.MoshiController.toJson
import com.arcxp.video.util.TAG

/**
 * UserSettingsManager is a class that manages user settings and preferences within the ArcXP Commerce module.
 * It handles operations related to user attributes, favorite articles, favorite videos, and push notification topics.
 * The class interacts with the backend through the IdentityApiManager to update and retrieve user settings.
 *
 * The class defines the following properties:
 * - currentAttributes: A list of current user attributes.
 * - currentSubscribedTopics: A list of currently subscribed push notification topics.
 * - currentFavoriteArticles: A list of favorite article UUIDs.
 * - currentFavoriteVideos: A list of favorite video UUIDs.
 * - currentSubscribedTopicsLiveData: LiveData for observing changes in subscribed topics.
 * - currentFavoriteArticlesLiveData: LiveData for observing changes in favorite articles.
 * - currentFavoriteVideosLiveData: LiveData for observing changes in favorite videos.
 *
 * Usage:
 * - Create an instance of UserSettingsManager with the necessary IdentityApiManager.
 * - Use the provided methods to manage user settings and preferences.
 *
 * Example:
 *
 * val userSettingsManager = UserSettingsManager(identityApiManager)
 * userSettingsManager.setAttribute("key", "value", "String")
 * val favoriteArticles = userSettingsManager.getFavoriteArticles()
 * userSettingsManager.addFavoriteArticle("articleUuid")
 *
 * Note: Ensure that the IdentityApiManager is properly configured before using UserSettingsManager.
 *
 * @property currentAttributes A list of current user attributes.
 * @property currentSubscribedTopics A list of currently subscribed push notification topics.
 * @property currentFavoriteArticles A list of favorite article UUIDs.
 * @property currentFavoriteVideos A list of favorite video UUIDs.
 * @property currentSubscribedTopicsLiveData LiveData for observing changes in subscribed topics.
 * @property currentFavoriteArticlesLiveData LiveData for observing changes in favorite articles.
 * @property currentFavoriteVideosLiveData LiveData for observing changes in favorite videos.
 * @method setCurrentAttributes Sets the current user attributes.
 * @method clearAttributes Clears the user attributes.
 * @method getAttribute Retrieves a user attribute by key.
 * @method setAttribute Sets a user attribute.
 * @method getPushNotificationTopics Retrieves the list of subscribed push notification topics.
 * @method setSubscribedPushNotificationTopics Sets the list of subscribed push notification topics.
 * @method addTopic Adds a topic to the list of subscribed push notification topics.
 * @method removeTopic Removes a topic from the list of subscribed push notification topics.
 * @method unsubscribeFromTopic Unsubscribes from a push notification topic.
 * @method subscribeToTopic Subscribes to a push notification topic.
 * @method getFavoriteArticles Retrieves the list of favorite articles.
 * @method setFavoriteArticles Sets the list of favorite articles.
 * @method getFavoriteVideos Retrieves the list of favorite videos.
 * @method setFavoriteVideos Sets the list of favorite videos.
 * @method addFavoriteVideo Adds a video to the list of favorite videos.
 * @method removeFavoriteVideo Removes a video from the list of favorite videos.
 * @method addFavoriteArticle Adds an article to the list of favorite articles.
 * @method removeFavoriteArticle Removes an article from the list of favorite articles.
 * @method updateBackendWithCurrentAttributes Updates the backend with the current user attributes.
 * @method setAttributesOnBackEnd Sets the user attributes on the backend.
 */
class UserSettingsManager(val identityApiManager: IdentityApiManager) {

    /* attributes as they arrive from server in profile, caching here to easily access custom attributes */
    private var currentAttributes = mutableListOf<ArcXPAttribute>()

    /* list of currently subscribed topics */
    private var currentSubscribedTopics = mutableListOf<TopicSubscription>()

    //this will contain currentSubscribedTopics latest value when returning from backend successfully
    private val _currentSubscribedTopicsLiveData =
        MutableLiveData<List<TopicSubscription>>(emptyList())

    //clients can subscribe to updates here
    val currentSubscribedTopicsLiveData: LiveData<List<TopicSubscription>> =
        _currentSubscribedTopicsLiveData

    /* list of favorite uuids: articles */
    private var currentFavoriteArticles = mutableListOf<String>()


    //this will contain currentFavoriteArticles latest value when returning from backend successfully
    private val _currentFavoriteArticlesLiveData =
        MutableLiveData<List<String>>(emptyList())

    //clients can subscribe to updates here
    val currentFavoriteArticlesLiveData: LiveData<List<String>> =
        _currentFavoriteArticlesLiveData

    /* list of favorite uuids: videos */
    private var currentFavoriteVideos = mutableListOf<String>()

    //this will contain currentFavoriteArticles latest value when returning from backend successfully
    private val _currentFavoriteVideosLiveData =
        MutableLiveData<List<String>>(emptyList())

    //clients can subscribe to updates here
    val currentFavoriteVideosLiveData: LiveData<List<String>> =
        _currentFavoriteVideosLiveData

    companion object {
        const val PUSH_NOTIFICATIONS_TOPICS_KEY = "topic list"
        const val FAVORITE_ARTICLES_KEY = "favorite article uuids"
        const val FAVORITE_VIDEOS_KEY = "favorite video uuids"
    }

    internal fun setCurrentAttributes(attributes: List<ArcXPAttribute>) {
        //this function should run from sdk when we get profile request, so should set/replace our local values

        //reset local attributes
        currentAttributes = attributes.toMutableList()
        currentSubscribedTopics.clear()


        //if the given list contains topics, store for easy access
        try {
            currentAttributes.find { it.name == PUSH_NOTIFICATIONS_TOPICS_KEY }?.let {
                val list = fromJsonList(it.value, TopicSubscription::class.java)!!
                currentSubscribedTopics = list.toMutableList()
                _currentSubscribedTopicsLiveData.postValue(list)
            }
        } catch (e: Exception) {
            //should only get here if someone has manually incorrectly entered incorrect json and stored with this key
            e.printStackTrace()
            Log.e(TAG, "Deserialization Error reading topic subscription attribute")
            //handling internally by clearing list right now
            currentSubscribedTopics = mutableListOf()
            _currentSubscribedTopicsLiveData.postValue(emptyList())
        }
        //if the given list contains video uuid favorites, store for easy access
        try {
            currentAttributes.find { it.name == FAVORITE_VIDEOS_KEY }?.let {
                val list = fromJsonList(it.value, String::class.java)!!
                currentFavoriteVideos = list.toMutableList()
                _currentFavoriteVideosLiveData.postValue(list)
            }
        } catch (e: Exception) {
            //should only get here if someone has manually incorrectly entered incorrect json and stored with this key
            e.printStackTrace()
            Log.e(TAG, "Deserialization Error reading video favorites attribute")
            //handling internally by clearing list right now
            currentFavoriteVideos = mutableListOf()
            _currentFavoriteVideosLiveData.postValue(emptyList())
        }
        //if the given list contains article uuid favorites, store for easy access
        try {
            currentAttributes.find { it.name == FAVORITE_ARTICLES_KEY }?.let {
                val list = fromJsonList(it.value, String::class.java)!!
                currentFavoriteArticles = list.toMutableList()
                _currentFavoriteArticlesLiveData.postValue(list)
            }
        } catch (e: Exception) {
            //should only get here if someone has manually incorrectly entered incorrect json and stored with this key
            e.printStackTrace()
            Log.e(TAG, "Deserialization Error reading article favorites attribute")
            //handling internally by clearing list right now
            currentSubscribedTopics = mutableListOf()
            _currentSubscribedTopicsLiveData.postValue(emptyList())
        }
    }

    fun clearAttributes(arcXPIdentityListener: ArcXPIdentityListener? = null) { //don't think you can clear this explicitly but you can replace values like so (must be >= 1 char)
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
            arcXPIdentityListener = arcXPIdentityListener
        )
    }

    fun getAttribute(key: String) = currentAttributes.find { it.name == key }
    fun setAttribute(
        key: String,
        value: String,
        type: String,
        arcXPIdentityListener: ArcXPIdentityListener? = null
    ) {
        //set locally
        //check both locals to update if it exists
        val existingAttribute = currentAttributes.find { it.name == key }
        if (existingAttribute != null) {
            existingAttribute.value = value
        } else {
            currentAttributes.add(ArcXPAttribute(name = key, value = value, type = type))
        }

        //need list of arcxp profile request from attributes
        val list = mutableListOf<ArcXPAttributeRequest>()
        currentAttributes.forEach {
            list.add(ArcXPAttributeRequest(name = it.name, value = it.value, type = it.type))
        }
        setAttributesOnBackEnd(attributes = list, arcXPIdentityListener = arcXPIdentityListener)
    }

    fun getPushNotificationTopics() = currentSubscribedTopics.toList()

    fun setSubscribedPushNotificationTopics(
        newTopics: List<TopicSubscription>,
        arcXPIdentityListener: ArcXPIdentityListener? = null
    ) {
        //sets topics
        // (replacing any old topics on backend and locally without removing any other attributes)
        currentSubscribedTopics = newTopics.toMutableList()
        updateBackendWithCurrentAttributes(arcXPIdentityListener = arcXPIdentityListener)
    }

    fun addTopic(
        topicSubscription: TopicSubscription,
        arcXPIdentityListener: ArcXPIdentityListener? = null
    ) {
        val nameList = currentSubscribedTopics.map { it.name }
        val index = nameList.indexOf(topicSubscription.name)
        if (index >= 0) {//currently in list, update at index
            currentSubscribedTopics[index] = topicSubscription
        } else {//not currently in list, add
            currentSubscribedTopics.add(topicSubscription)
        }
        updateBackendWithCurrentAttributes(arcXPIdentityListener = arcXPIdentityListener)
    }

    fun removeTopic(name: String, arcXPIdentityListener: ArcXPIdentityListener? = null) {
        val index = currentSubscribedTopics.map { it.name }.indexOf(name)
        if (index >= 0) {//currently in list, update at index
            currentSubscribedTopics.removeAt(index)
            updateBackendWithCurrentAttributes(arcXPIdentityListener = arcXPIdentityListener)
        } else {//not currently in list
            arcXPIdentityListener?.onProfileError(error = createArcXPException(type = ArcXPSDKErrorType.CONFIG_ERROR, message = "removeTopic: cannot find given topic"))
        }
    }

    fun unsubscribeFromTopic(name: String, arcXPIdentityListener: ArcXPIdentityListener? = null) {
        val index = currentSubscribedTopics.map { it.name }.indexOf(name)
        if (index >= 0) {//currently in list, update at index
            currentSubscribedTopics[index].subscribed = false
            updateBackendWithCurrentAttributes(arcXPIdentityListener = arcXPIdentityListener)
        } else {//not currently in list
            arcXPIdentityListener?.onProfileError(error = createArcXPException(type = ArcXPSDKErrorType.CONFIG_ERROR, message = "unsubscribeFromTopic: cannot find given topic"))
        }

    }

    fun subscribeToTopic(name: String, arcXPIdentityListener: ArcXPIdentityListener? = null) {
        val index = currentSubscribedTopics.map { it.name }.indexOf(name)
        if (index >= 0) {//currently in list, update at index
            currentSubscribedTopics[index].subscribed = true
        } else {//not currently in list
            arcXPIdentityListener?.onProfileError(error = createArcXPException(type = ArcXPSDKErrorType.CONFIG_ERROR, message = "subscribeFromTopic: cannot find given topic"))
        }
        updateBackendWithCurrentAttributes(arcXPIdentityListener = arcXPIdentityListener)
    }


    fun getFavoriteArticles() = currentFavoriteArticles.toList()

    fun setFavoriteArticles(
        newUuids: List<String>,
        arcXPIdentityListener: ArcXPIdentityListener? = null
    ) {
        currentFavoriteArticles = newUuids.toMutableList()
        updateBackendWithCurrentAttributes(arcXPIdentityListener = arcXPIdentityListener)
    }

    fun getFavoriteVideos() = currentFavoriteVideos.toList()

    fun setFavoriteVideos(
        newUuids: List<String>,
        arcXPIdentityListener: ArcXPIdentityListener? = null
    ) {
        currentFavoriteVideos = newUuids.toMutableList()
        updateBackendWithCurrentAttributes(arcXPIdentityListener = arcXPIdentityListener)
    }

    fun addFavoriteVideo(newUuid: String, arcXPIdentityListener: ArcXPIdentityListener? = null) {
        currentFavoriteVideos.add(newUuid)
        updateBackendWithCurrentAttributes(arcXPIdentityListener = arcXPIdentityListener)
    }

    fun removeFavoriteVideo(uuid: String, arcXPIdentityListener: ArcXPIdentityListener? = null) {
        currentFavoriteVideos.remove(uuid)
        updateBackendWithCurrentAttributes(arcXPIdentityListener = arcXPIdentityListener)
    }

    fun addFavoriteArticle(newUuid: String, arcXPIdentityListener: ArcXPIdentityListener? = null) {
        currentFavoriteArticles.add(newUuid)
        updateBackendWithCurrentAttributes(arcXPIdentityListener = arcXPIdentityListener)
    }

    fun removeFavoriteArticle(uuid: String, arcXPIdentityListener: ArcXPIdentityListener? = null) {
        currentFavoriteArticles.remove(uuid)
        updateBackendWithCurrentAttributes(arcXPIdentityListener = arcXPIdentityListener)
    }

    private fun updateBackendWithCurrentAttributes(arcXPIdentityListener: ArcXPIdentityListener?) {
        try {
            val newList = mutableListOf<ArcXPAttributeRequest>()
            val oldList = mutableListOf<ArcXPAttribute>()
            currentAttributes.forEach {
                if (it.name !in listOf(
                        PUSH_NOTIFICATIONS_TOPICS_KEY,
                        FAVORITE_ARTICLES_KEY,
                        FAVORITE_VIDEOS_KEY
                    )
                ) {
                    newList.add(
                        ArcXPAttributeRequest(
                            name = it.name,
                            value = it.value,
                            type = it.type
                        )
                    )
                } else {
                    oldList.add(it)
                }
            }
            oldList.forEach { currentAttributes.remove(it) }
            val topicJson = toJson(currentSubscribedTopics)!!
            val favoriteArticlesJson = toJson(currentFavoriteArticles)!!
            val favoriteVideosJson = toJson(currentFavoriteVideos)!!
            newList.addAll(
                listOf(
                    ArcXPAttributeRequest(
                        name = PUSH_NOTIFICATIONS_TOPICS_KEY,
                        value = topicJson,
                        type = "String"
                    ),
                    ArcXPAttributeRequest(
                        name = FAVORITE_ARTICLES_KEY,
                        value = favoriteArticlesJson,
                        type = "String"
                    ),
                    ArcXPAttributeRequest(
                        name = FAVORITE_VIDEOS_KEY,
                        value = favoriteVideosJson,
                        type = "String"
                    )
                )
            )
            currentAttributes.addAll(
                listOf(
                    ArcXPAttribute(
                        name = PUSH_NOTIFICATIONS_TOPICS_KEY,
                        value = topicJson,
                        type = "String"
                    ),
                    ArcXPAttribute(
                        name = FAVORITE_ARTICLES_KEY,
                        value = favoriteArticlesJson,
                        type = "String"
                    ),
                    ArcXPAttribute(
                        name = FAVORITE_VIDEOS_KEY,
                        value = favoriteVideosJson,
                        type = "String"
                    ),
                )
            )
            setAttributesOnBackEnd(
                attributes = newList,
                arcXPIdentityListener = arcXPIdentityListener
            )
        } catch (e: Exception) {
            e.printStackTrace()
            arcXPIdentityListener?.onProfileError(
                createArcXPException(
                    type = ArcXPSDKErrorType.DESERIALIZATION_ERROR,
                    message = "Current Attribute backend update failure",
                    value = e
                )
            )
        }

    }

    private fun setAttributesOnBackEnd(
        attributes: List<ArcXPAttributeRequest>,
        arcXPIdentityListener: ArcXPIdentityListener?
    ) {
        identityApiManager.updateProfile(
            ArcXPProfilePatchRequest(attributes = attributes), object : ArcXPIdentityListener() {
                override fun onProfileUpdateSuccess(profileManageResponse: ArcXPProfileManage) {
                    arcXPIdentityListener?.onProfileUpdateSuccess(profileManageResponse = profileManageResponse)
                    profileManageResponse.attributes?.let { setCurrentAttributes(attributes = it) }
                }

                override fun onProfileError(error: ArcXPException) {
                    arcXPIdentityListener?.onProfileError(error = error)
                }
            })
    }
}