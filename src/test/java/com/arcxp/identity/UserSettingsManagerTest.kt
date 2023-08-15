package com.arcxp.identity

import android.util.Log
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.arcxp.commerce.apimanagers.IdentityApiManager
import com.arcxp.commerce.callbacks.ArcXPIdentityListener
import com.arcxp.commerce.extendedModels.ArcXPProfileManage
import com.arcxp.commerce.models.ArcXPAttribute
import com.arcxp.commerce.models.ArcXPAttributeRequest
import com.arcxp.commerce.models.ArcXPProfilePatchRequest
import com.arcxp.commerce.models.TopicSubscription
import com.arcxp.commons.throwables.ArcXPException
import com.arcxp.commons.util.MoshiController.fromJsonList
import com.arcxp.commons.util.MoshiController.moshi
import com.arcxp.commons.util.MoshiController.toJson
import com.arcxp.identity.UserSettingsManager.Companion.FAVORITE_ARTICLES_KEY
import com.arcxp.identity.UserSettingsManager.Companion.FAVORITE_VIDEOS_KEY
import com.arcxp.identity.UserSettingsManager.Companion.PUSH_NOTIFICATIONS_TOPICS_KEY
import com.arcxp.video.util.TAG
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkAll
import io.mockk.verify
import io.mockk.verifySequence
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class UserSettingsManagerTest {

    @get:Rule
    var instantExecuteRule = InstantTaskExecutorRule()

    @RelaxedMockK
    private lateinit var identityApiManager: IdentityApiManager

    @RelaxedMockK
    private lateinit var arcXPIdentityListener: ArcXPIdentityListener

    @MockK
    private lateinit var error: ArcXPException


    private val expectedNameKey = "expected"
    private val expectedType = "String"
    private val expectedValue = "value"
    private val expectedDisplayName = "display name"

    private lateinit var testObject: UserSettingsManager

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        testObject = UserSettingsManager(identityApiManager)
    }

    @Test
    fun `setCurrentAttributes sets a unique key and it is returned via getAttribute`() {
        val expected =
            ArcXPAttribute(name = expectedNameKey, value = expectedValue, type = expectedType)
        val expectedList = listOf(expected)

        testObject.setCurrentAttributes(attributes = expectedList)

        assertEquals(expected, testObject.getAttribute(expectedNameKey))
    }

    @Test
    fun `setCurrentAttributes with topics sets value locally and returns value in live data`() {
        val expectedSub1 =
            TopicSubscription(name = "topic1", displayName = "display1", subscribed = true)
        val expectedSub2 =
            TopicSubscription(name = "topic2", displayName = "display2", subscribed = false)
        val expectedSub3 =
            TopicSubscription(name = "topic3", displayName = "display3", subscribed = true)
        val expectedList = listOf(expectedSub1, expectedSub2, expectedSub3)

        val expectedJson = toJson(expectedList)!!
        val attribute = ArcXPAttribute(
            name = PUSH_NOTIFICATIONS_TOPICS_KEY,
            value = expectedJson,
            type = expectedType
        )

        testObject.setCurrentAttributes(attributes = listOf(attribute))

        assertEquals(expectedList, testObject.getPushNotificationTopics())
        assertEquals(expectedList, testObject.currentSubscribedTopicsLiveData.value)
    }

    @Test
    fun `setCurrentAttributes with topics moshi throws exception and clears data`() {
        val expectedSub1 =
            TopicSubscription(name = "topic1", displayName = "display1", subscribed = true)
        val expectedSub2 =
            TopicSubscription(name = "topic2", displayName = "display2", subscribed = false)
        val expectedSub3 =
            TopicSubscription(name = "topic3", displayName = "display3", subscribed = true)
        val expectedList = listOf(expectedSub1, expectedSub2, expectedSub3)

        val expectedJson = toJson(expectedList)!!
        val attribute = ArcXPAttribute(
            name = PUSH_NOTIFICATIONS_TOPICS_KEY,
            value = expectedJson,
            type = expectedType
        )
        mockkObject(moshi)
        mockkStatic(Log::class)
        every { Log.e(TAG, "Deserialization Error reading topic subscription attribute") } returns 0
        val exception = mockk<Exception>(relaxed = true)
        every { fromJsonList(any(), TopicSubscription::class.java) } throws exception


        testObject.setCurrentAttributes(attributes = listOf(attribute))
        verifySequence {
            exception.printStackTrace()
            Log.e(
                "UserSettingsManager",
                "Deserialization Error reading topic subscription attribute"
            )
        }

        assertTrue(testObject.getPushNotificationTopics().isEmpty())
        assertTrue(testObject.currentSubscribedTopicsLiveData.value!!.isEmpty())
        unmockkAll()
    }

    @Test
    fun `setCurrentAttributes with videos sets value locally and returns value in live data`() {
        val expectedList = listOf("uuid1", "uuid2", "uuid3")

        val expectedJson = toJson(expectedList)!!
        val attribute =
            ArcXPAttribute(name = FAVORITE_VIDEOS_KEY, value = expectedJson, type = expectedType)

        testObject.setCurrentAttributes(attributes = listOf(attribute))

        assertEquals(expectedList, testObject.getFavoriteVideos())
        assertEquals(expectedList, testObject.currentFavoriteVideosLiveData.value)
    }

    @Test
    fun `setCurrentAttributes with videos moshi throws exception and clears data`() {
        val expectedList = listOf("uuid1", "uuid2", "uuid3")

        val expectedJson = toJson(expectedList)!!
        val attribute = ArcXPAttribute(
            name = FAVORITE_VIDEOS_KEY,
            value = expectedJson,
            type = expectedType
        )
        mockkObject(moshi)
        mockkStatic(Log::class)
        every { Log.e(TAG, "Deserialization Error reading video favorites attribute") } returns 0
        val exception = mockk<Exception>(relaxed = true)
        every { fromJsonList(any(), String::class.java) } throws exception


        testObject.setCurrentAttributes(attributes = listOf(attribute))
        verifySequence {
            exception.printStackTrace()
            Log.e("UserSettingsManager", "Deserialization Error reading video favorites attribute")
        }

        assertTrue(testObject.getFavoriteVideos().isEmpty())
        assertTrue(testObject.currentFavoriteVideosLiveData.value!!.isEmpty())
        unmockkAll()
    }

    @Test
    fun `setCurrentAttributes with articles sets value locally and returns value in live data`() {
        val expectedList = listOf("uuid1", "uuid2", "uuid3")

        val expectedJson = toJson(expectedList)!!
        val attribute =
            ArcXPAttribute(name = FAVORITE_ARTICLES_KEY, value = expectedJson, type = expectedType)

        testObject.setCurrentAttributes(attributes = listOf(attribute))

        assertEquals(expectedList, testObject.getFavoriteArticles())
        assertEquals(expectedList, testObject.currentFavoriteArticlesLiveData.value)
    }

    @Test
    fun `setCurrentAttributes with articles moshi throws exception and clears data`() {
        val expectedList = listOf("uuid1", "uuid2", "uuid3")

        val expectedJson = toJson(expectedList)!!
        val attribute = ArcXPAttribute(
            name = FAVORITE_ARTICLES_KEY,
            value = expectedJson,
            type = expectedType
        )
        mockkObject(moshi)
        mockkStatic(Log::class)
        every { Log.e(TAG, "Deserialization Error reading article favorites attribute") } returns 0
        val exception = mockk<Exception>(relaxed = true)
        every { fromJsonList(any(), String::class.java) } throws exception

        testObject.setCurrentAttributes(attributes = listOf(attribute))

        verifySequence {
            exception.printStackTrace()
            Log.e(
                "UserSettingsManager",
                "Deserialization Error reading article favorites attribute"
            )
        }
        assertTrue(testObject.getFavoriteVideos().isEmpty())
        assertTrue(testObject.currentFavoriteVideosLiveData.value!!.isEmpty())
        unmockkAll()
    }

    @Test
    fun `clearAttributes sets expected values no listener`() {
        val initialList = listOf("uuid1", "uuid2", "uuid3")

        val expectedJson = toJson(initialList)!!
        val attribute =
            ArcXPAttribute(name = FAVORITE_ARTICLES_KEY, value = expectedJson, type = expectedType)

        val expectedClearedAttributes = listOf(
            ArcXPAttributeRequest(
                name = PUSH_NOTIFICATIONS_TOPICS_KEY,
                value = " ",
                type = "String"
            ),
            ArcXPAttributeRequest(name = FAVORITE_ARTICLES_KEY, value = " ", type = "String"),
            ArcXPAttributeRequest(name = FAVORITE_VIDEOS_KEY, value = " ", type = "String")
        )
        val expectedAttributes = listOf(
            ArcXPAttribute(
                name = PUSH_NOTIFICATIONS_TOPICS_KEY,
                value = " ",
                type = "String"
            ),
            ArcXPAttribute(name = FAVORITE_ARTICLES_KEY, value = " ", type = "String"),
            ArcXPAttribute(name = FAVORITE_VIDEOS_KEY, value = " ", type = "String")
        )

        testObject.setCurrentAttributes(attributes = listOf(attribute))
        val slot = slot<ArcXPIdentityListener>()


        testObject.clearAttributes()
        verify {
            identityApiManager.updateProfile(
                ArcXPProfilePatchRequest(attributes = expectedClearedAttributes),
                capture(slot)
            )
        }

        val response = mockk<ArcXPProfileManage> {
            every { attributes } returns expectedAttributes
        }
        slot.captured.onProfileUpdateSuccess(profileManageResponse = response)

        assertEquals(emptyList(), testObject.getFavoriteArticles())
        assertEquals(emptyList(), testObject.currentFavoriteArticlesLiveData.value)
        assertEquals(emptyList(), testObject.getFavoriteVideos())
        assertEquals(emptyList(), testObject.currentFavoriteVideosLiveData.value)
        assertEquals(emptyList(), testObject.getPushNotificationTopics())
        assertEquals(emptyList(), testObject.currentSubscribedTopicsLiveData.value)
        assertEquals(" ", testObject.getAttribute(FAVORITE_ARTICLES_KEY)!!.value)
        assertEquals(" ", testObject.getAttribute(FAVORITE_VIDEOS_KEY)!!.value)
        assertEquals(" ", testObject.getAttribute(PUSH_NOTIFICATIONS_TOPICS_KEY)!!.value)
    }

    @Test
    fun `clearAttributes sets expected values with listener`() {
        val initialList = listOf("uuid1", "uuid2", "uuid3")

        val expectedJson = toJson(initialList)!!
        val attribute =
            ArcXPAttribute(name = FAVORITE_ARTICLES_KEY, value = expectedJson, type = expectedType)

        val expectedClearedAttributes = listOf(
            ArcXPAttributeRequest(
                name = PUSH_NOTIFICATIONS_TOPICS_KEY,
                value = " ",
                type = "String"
            ),
            ArcXPAttributeRequest(name = FAVORITE_ARTICLES_KEY, value = " ", type = "String"),
            ArcXPAttributeRequest(name = FAVORITE_VIDEOS_KEY, value = " ", type = "String")
        )
        val expectedAttributes = listOf(
            ArcXPAttribute(
                name = PUSH_NOTIFICATIONS_TOPICS_KEY,
                value = " ",
                type = "String"
            ),
            ArcXPAttribute(name = FAVORITE_ARTICLES_KEY, value = " ", type = "String"),
            ArcXPAttribute(name = FAVORITE_VIDEOS_KEY, value = " ", type = "String")
        )

        testObject.setCurrentAttributes(attributes = listOf(attribute))
        val slot = slot<ArcXPIdentityListener>()


        testObject.clearAttributes(arcXPIdentityListener = arcXPIdentityListener)
        verify {
            identityApiManager.updateProfile(
                ArcXPProfilePatchRequest(attributes = expectedClearedAttributes),
                capture(slot)
            )
        }

        val response = mockk<ArcXPProfileManage> {
            every { attributes } returns expectedAttributes
        }
        slot.captured.onProfileUpdateSuccess(profileManageResponse = response)
        verify { arcXPIdentityListener.onProfileUpdateSuccess(profileManageResponse = response) }
        slot.captured.onProfileError(error = error)
        verify { arcXPIdentityListener.onProfileError(error = error) }


        assertEquals(emptyList(), testObject.getFavoriteArticles())
        assertEquals(emptyList(), testObject.currentFavoriteArticlesLiveData.value)
        assertEquals(emptyList(), testObject.getFavoriteVideos())
        assertEquals(emptyList(), testObject.currentFavoriteVideosLiveData.value)
        assertEquals(emptyList(), testObject.getPushNotificationTopics())
        assertEquals(emptyList(), testObject.currentSubscribedTopicsLiveData.value)
        assertEquals(" ", testObject.getAttribute(FAVORITE_ARTICLES_KEY)!!.value)
        assertEquals(" ", testObject.getAttribute(FAVORITE_VIDEOS_KEY)!!.value)
        assertEquals(" ", testObject.getAttribute(PUSH_NOTIFICATIONS_TOPICS_KEY)!!.value)
    }

    @Test
    fun `setAttribute with no listener sets attribute on back end and locally`() {
        val expectedAttribute =
            ArcXPAttribute(name = expectedNameKey, value = expectedValue, type = expectedType)
        val expectedRequest =
            ArcXPAttributeRequest(name = expectedNameKey, value = expectedValue, type = expectedType)
        testObject.setAttribute(key = expectedNameKey, value = expectedValue, type = expectedType)

        val slot = slot<ArcXPIdentityListener>()
        verify {
            identityApiManager.updateProfile(
                ArcXPProfilePatchRequest(attributes = listOf(expectedRequest)),
                capture(slot)
            )
        }
        val response = mockk<ArcXPProfileManage> {
            every { attributes } returns listOf(expectedAttribute)
        }
        slot.captured.onProfileUpdateSuccess(profileManageResponse = response)
        assertEquals(emptyList(), testObject.getFavoriteArticles())
        assertEquals(emptyList(), testObject.currentFavoriteArticlesLiveData.value)
        assertEquals(emptyList(), testObject.getFavoriteVideos())
        assertEquals(emptyList(), testObject.currentFavoriteVideosLiveData.value)
        assertEquals(emptyList(), testObject.getPushNotificationTopics())
        assertEquals(emptyList(), testObject.currentSubscribedTopicsLiveData.value)
        assertEquals(expectedAttribute, testObject.getAttribute(expectedNameKey)!!)
    }

    @Test
    fun `setAttribute with existing attribute, no listener sets attribute on back end and locally`() {
        val expectedAttribute =
            ArcXPAttribute(name = expectedNameKey, value = expectedValue, type = expectedType)
        val expectedRequest =
            ArcXPAttributeRequest(name = expectedNameKey, value = expectedValue, type = expectedType)

        val expectedFinalAttribute =
            ArcXPAttribute(name = expectedNameKey, value = "newValue", type = expectedType)
        testObject.setAttribute(key = expectedNameKey, value = expectedValue, type = expectedType)

        val slot = slot<ArcXPIdentityListener>()
        verify {
            identityApiManager.updateProfile(
                ArcXPProfilePatchRequest(attributes = listOf(expectedRequest)),
                capture(slot)
            )
        }
        val response = mockk<ArcXPProfileManage> {
            every { attributes } returns listOf(expectedAttribute)
        }
        slot.captured.onProfileUpdateSuccess(profileManageResponse = response)
        assertEquals(emptyList(), testObject.getFavoriteArticles())
        assertEquals(emptyList(), testObject.currentFavoriteArticlesLiveData.value)
        assertEquals(emptyList(), testObject.getFavoriteVideos())
        assertEquals(emptyList(), testObject.currentFavoriteVideosLiveData.value)
        assertEquals(emptyList(), testObject.getPushNotificationTopics())
        assertEquals(emptyList(), testObject.currentSubscribedTopicsLiveData.value)
        assertEquals(expectedAttribute, testObject.getAttribute(expectedNameKey)!!)

        testObject.setAttribute(key = expectedNameKey, value = "newValue", type = expectedType)

        assertEquals(emptyList(), testObject.getFavoriteArticles())
        assertEquals(emptyList(), testObject.currentFavoriteArticlesLiveData.value)
        assertEquals(emptyList(), testObject.getFavoriteVideos())
        assertEquals(emptyList(), testObject.currentFavoriteVideosLiveData.value)
        assertEquals(emptyList(), testObject.getPushNotificationTopics())
        assertEquals(emptyList(), testObject.currentSubscribedTopicsLiveData.value)
        assertEquals(expectedFinalAttribute, testObject.getAttribute(expectedNameKey)!!)
    }

    @Test
    fun `setAttribute with listener sets attribute on back end and locally`() {
        val expectedAttribute =
            ArcXPAttribute(name = expectedNameKey, value = expectedValue, type = expectedType)
        val expectedRequest =
            ArcXPAttributeRequest(name = expectedNameKey, value = expectedValue, type = expectedType)
        testObject.setAttribute(key = expectedNameKey, value = expectedValue, type = expectedType, arcXPIdentityListener = arcXPIdentityListener)

        val slot = slot<ArcXPIdentityListener>()
        verify {
            identityApiManager.updateProfile(
                ArcXPProfilePatchRequest(attributes = listOf(expectedRequest)),
                capture(slot)
            )
        }
        val response = mockk<ArcXPProfileManage> {
            every { attributes } returns listOf(expectedAttribute)
        }
        slot.captured.onProfileUpdateSuccess(profileManageResponse = response)
        verify { arcXPIdentityListener.onProfileUpdateSuccess(profileManageResponse = response) }
        slot.captured.onProfileError(error = error)
        verify { arcXPIdentityListener.onProfileError(error = error) }
        assertEquals(emptyList(), testObject.getFavoriteArticles())
        assertEquals(emptyList(), testObject.currentFavoriteArticlesLiveData.value)
        assertEquals(emptyList(), testObject.getFavoriteVideos())
        assertEquals(emptyList(), testObject.currentFavoriteVideosLiveData.value)
        assertEquals(emptyList(), testObject.getPushNotificationTopics())
        assertEquals(emptyList(), testObject.currentSubscribedTopicsLiveData.value)
        assertEquals(expectedAttribute, testObject.getAttribute(expectedNameKey)!!)
    }

    @Test
    fun `setSubscribedPushNotificationTopics no listener sets topics locally and updates single attribute on backend`(){
        val customAttribute =
            ArcXPAttribute(name = expectedNameKey, value = expectedValue, type = expectedType)
        val customAttributeRequest =
            ArcXPAttributeRequest(name = expectedNameKey, value = expectedValue, type = expectedType)
        val initialAttributeList = listOf(customAttribute)
        testObject.setCurrentAttributes(attributes = initialAttributeList)
        assertEquals(customAttribute, testObject.getAttribute(expectedNameKey))

        val expectedSub1 =
            TopicSubscription(name = "topic1", displayName = "display1", subscribed = true)
        val expectedSub2 =
            TopicSubscription(name = "topic2", displayName = "display2", subscribed = false)
        val expectedSub3 =
            TopicSubscription(name = "topic3", displayName = "display3", subscribed = true)
        val expectedList = listOf(expectedSub1, expectedSub2, expectedSub3)

        val expectedJson = toJson(expectedList)!!
        val topicAttributeRequest = ArcXPAttributeRequest(
            name = PUSH_NOTIFICATIONS_TOPICS_KEY,
            value = expectedJson,
            type = expectedType
        )
        val videosAttributeRequest = ArcXPAttributeRequest(
            name = FAVORITE_VIDEOS_KEY,
            value = "[]",
            type = expectedType
        )
        val articlesAttributeRequest = ArcXPAttributeRequest(
            name = FAVORITE_ARTICLES_KEY,
            value = "[]",
            type = expectedType
        )
        val topicAttribute = ArcXPAttribute(
            name = PUSH_NOTIFICATIONS_TOPICS_KEY,
            value = expectedJson,
            type = expectedType
        )
        val updatedAttributeRequestList = listOf(customAttributeRequest, topicAttributeRequest, articlesAttributeRequest,videosAttributeRequest, )
        val updatedAttributeList = listOf(customAttribute, topicAttribute)

        testObject.setSubscribedPushNotificationTopics(newTopics = expectedList)
        assertEquals(expectedList, testObject.getPushNotificationTopics())


        val slot = slot<ArcXPIdentityListener>()


        verify {
            identityApiManager.updateProfile(
                ArcXPProfilePatchRequest(attributes = updatedAttributeRequestList),
                capture(slot)
            )
        }
//
        val response = mockk<ArcXPProfileManage> {
            every { attributes } returns updatedAttributeList
        }
        slot.captured.onProfileUpdateSuccess(profileManageResponse = response)

        //existing keys unchanged:
        assertEquals(customAttribute, testObject.getAttribute(expectedNameKey))
        assertEquals(expectedList, testObject.getPushNotificationTopics())
        assertEquals(expectedList, testObject.currentSubscribedTopicsLiveData.value)
        assertTrue(testObject.getFavoriteVideos().isEmpty())
        assertTrue(testObject.currentFavoriteVideosLiveData.value!!.isEmpty())
        assertTrue(testObject.getFavoriteArticles().isEmpty())
        assertTrue(testObject.currentFavoriteArticlesLiveData.value!!.isEmpty())


    }
    @Test
    fun `setSubscribedPushNotificationTopics with listener sets topics locally and on backend`(){
        val customAttribute =
            ArcXPAttribute(name = expectedNameKey, value = expectedValue, type = expectedType)
        val customAttributeRequest =
            ArcXPAttributeRequest(name = expectedNameKey, value = expectedValue, type = expectedType)
        val initialAttributeList = listOf(customAttribute)
        testObject.setCurrentAttributes(attributes = initialAttributeList)
        assertEquals(customAttribute, testObject.getAttribute(expectedNameKey))

        val expectedSub1 =
            TopicSubscription(name = "topic1", displayName = "display1", subscribed = true)
        val expectedSub2 =
            TopicSubscription(name = "topic2", displayName = "display2", subscribed = false)
        val expectedSub3 =
            TopicSubscription(name = "topic3", displayName = "display3", subscribed = true)
        val expectedList = listOf(expectedSub1, expectedSub2, expectedSub3)

        val expectedJson = toJson(expectedList)!!
        val topicAttributeRequest = ArcXPAttributeRequest(
            name = PUSH_NOTIFICATIONS_TOPICS_KEY,
            value = expectedJson,
            type = expectedType
        )
        val videosAttributeRequest = ArcXPAttributeRequest(
            name = FAVORITE_VIDEOS_KEY,
            value = "[]",
            type = expectedType
        )
        val articlesAttributeRequest = ArcXPAttributeRequest(
            name = FAVORITE_ARTICLES_KEY,
            value = "[]",
            type = expectedType
        )
        val topicAttribute = ArcXPAttribute(
            name = PUSH_NOTIFICATIONS_TOPICS_KEY,
            value = expectedJson,
            type = expectedType
        )
        val updatedAttributeRequestList = listOf(customAttributeRequest, topicAttributeRequest, articlesAttributeRequest,videosAttributeRequest, )
        val updatedAttributeList = listOf(customAttribute, topicAttribute)

        testObject.setSubscribedPushNotificationTopics(newTopics = expectedList, arcXPIdentityListener = arcXPIdentityListener)
        assertEquals(expectedList, testObject.getPushNotificationTopics())


        val slot = slot<ArcXPIdentityListener>()


        verify {
            identityApiManager.updateProfile(
                ArcXPProfilePatchRequest(attributes = updatedAttributeRequestList),
                capture(slot)
            )
        }
        val response = mockk<ArcXPProfileManage> {
            every { attributes } returns updatedAttributeList
        }
        slot.captured.onProfileUpdateSuccess(profileManageResponse = response)
        verify { arcXPIdentityListener.onProfileUpdateSuccess(profileManageResponse = response) }
        slot.captured.onProfileError(error = error)
        verify { arcXPIdentityListener.onProfileError(error = error) }
        //existing keys unchanged:
        assertEquals(customAttribute, testObject.getAttribute(expectedNameKey))
        assertEquals(expectedList, testObject.getPushNotificationTopics())
        assertEquals(expectedList, testObject.currentSubscribedTopicsLiveData.value)
        assertTrue(testObject.getFavoriteVideos().isEmpty())
        assertTrue(testObject.currentFavoriteVideosLiveData.value!!.isEmpty())
        assertTrue(testObject.getFavoriteArticles().isEmpty())
        assertTrue(testObject.currentFavoriteArticlesLiveData.value!!.isEmpty())


    }

    @Test
    fun `addTopic while in current list no listener`() {
        val expectedSub1 =
            TopicSubscription(name = "topic1", displayName = "display1", subscribed = true)
        val expectedSub2 =
            TopicSubscription(name = "topic2", displayName = "display2", subscribed = false)
        val expectedSub3 =
            TopicSubscription(name = "topic3", displayName = "display3", subscribed = true)
        val expectedList = listOf(expectedSub1, expectedSub2, expectedSub3)

        val expectedJson = toJson(expectedList)!!
        val topicAttributeRequest = ArcXPAttributeRequest(
            name = PUSH_NOTIFICATIONS_TOPICS_KEY,
            value = expectedJson,
            type = expectedType
        )
        val topicAttribute = ArcXPAttribute(
            name = PUSH_NOTIFICATIONS_TOPICS_KEY,
            value = expectedJson,
            type = expectedType
        )
        val videosAttributeRequest = ArcXPAttributeRequest(
            name = FAVORITE_VIDEOS_KEY,
            value = "[]",
            type = expectedType
        )
        val articlesAttributeRequest = ArcXPAttributeRequest(
            name = FAVORITE_ARTICLES_KEY,
            value = "[]",
            type = expectedType
        )
        val updatedAttributeRequestList = listOf(topicAttributeRequest, articlesAttributeRequest, videosAttributeRequest)
        val updatedAttributeList = listOf(topicAttribute)

        testObject.setSubscribedPushNotificationTopics(newTopics = expectedList)

        val slot = slot<ArcXPIdentityListener>()

        verify {
            identityApiManager.updateProfile(
                ArcXPProfilePatchRequest(attributes = updatedAttributeRequestList),
                capture(slot)
            )
        }
        val response = mockk<ArcXPProfileManage> {
            every { attributes } returns updatedAttributeList
        }
        slot.captured.onProfileUpdateSuccess(profileManageResponse = response)





        assertEquals(expectedList, testObject.getPushNotificationTopics())

        val newRepeatedTopic =
            TopicSubscription(name = "topic3", displayName = "display3 updated", subscribed = false)


        testObject.addTopic(topicSubscription = newRepeatedTopic)

        val expectedList2 = listOf(expectedSub1, expectedSub2, newRepeatedTopic)

        val expectedJson2 = toJson(expectedList2)!!
        val topicAttributeRequest2 = ArcXPAttributeRequest(
            name = PUSH_NOTIFICATIONS_TOPICS_KEY,
            value = expectedJson2,
            type = expectedType
        )
        val topicAttribute2 = ArcXPAttribute(
            name = PUSH_NOTIFICATIONS_TOPICS_KEY,
            value = expectedJson2,
            type = expectedType
        )
        val updatedAttributeList2 = listOf(topicAttribute2)
        val updatedAttributeRequestList2 = listOf(topicAttributeRequest2, articlesAttributeRequest, videosAttributeRequest)
        val slot2 = slot<ArcXPIdentityListener>()
        verify {
            identityApiManager.updateProfile(
                ArcXPProfilePatchRequest(attributes = updatedAttributeRequestList2),
                capture(slot2)
            )
        }
        val response2 = mockk<ArcXPProfileManage> {
            every { attributes } returns updatedAttributeList2
        }
        slot2.captured.onProfileUpdateSuccess(profileManageResponse = response2)



        assertTrue(testObject.currentSubscribedTopicsLiveData.value!!.contains(newRepeatedTopic))
        assertTrue(testObject.getPushNotificationTopics().contains(newRepeatedTopic))
        assertTrue(testObject.getPushNotificationTopics().size == 3)//updated and did not add
    }
@Test
    fun `addTopic while in current list with listener`() {
        val expectedSub1 =
            TopicSubscription(name = "topic1", displayName = "display1", subscribed = true)
        val expectedSub2 =
            TopicSubscription(name = "topic2", displayName = "display2", subscribed = false)
        val expectedSub3 =
            TopicSubscription(name = "topic3", displayName = "display3", subscribed = true)
        val expectedList = listOf(expectedSub1, expectedSub2, expectedSub3)

        val expectedJson = toJson(expectedList)!!
        val topicAttributeRequest = ArcXPAttributeRequest(
            name = PUSH_NOTIFICATIONS_TOPICS_KEY,
            value = expectedJson,
            type = expectedType
        )
        val topicAttribute = ArcXPAttribute(
            name = PUSH_NOTIFICATIONS_TOPICS_KEY,
            value = expectedJson,
            type = expectedType
        )
        val videosAttributeRequest = ArcXPAttributeRequest(
            name = FAVORITE_VIDEOS_KEY,
            value = "[]",
            type = expectedType
        )
        val articlesAttributeRequest = ArcXPAttributeRequest(
            name = FAVORITE_ARTICLES_KEY,
            value = "[]",
            type = expectedType
        )
        val updatedAttributeRequestList = listOf(topicAttributeRequest, articlesAttributeRequest, videosAttributeRequest)
        val updatedAttributeList = listOf(topicAttribute)

        testObject.setSubscribedPushNotificationTopics(newTopics = expectedList)

        val slot = slot<ArcXPIdentityListener>()

        verify {
            identityApiManager.updateProfile(
                ArcXPProfilePatchRequest(attributes = updatedAttributeRequestList),
                capture(slot)
            )
        }
        val response = mockk<ArcXPProfileManage> {
            every { attributes } returns updatedAttributeList
        }
        slot.captured.onProfileUpdateSuccess(profileManageResponse = response)





        assertEquals(expectedList, testObject.getPushNotificationTopics())

        val newRepeatedTopic =
            TopicSubscription(name = "topic3", displayName = "display3 updated", subscribed = false)


        testObject.addTopic(topicSubscription = newRepeatedTopic, arcXPIdentityListener = arcXPIdentityListener)

        val expectedList2 = listOf(expectedSub1, expectedSub2, newRepeatedTopic)

        val expectedJson2 = toJson(expectedList2)!!
        val topicAttributeRequest2 = ArcXPAttributeRequest(
            name = PUSH_NOTIFICATIONS_TOPICS_KEY,
            value = expectedJson2,
            type = expectedType
        )
        val topicAttribute2 = ArcXPAttribute(
            name = PUSH_NOTIFICATIONS_TOPICS_KEY,
            value = expectedJson2,
            type = expectedType
        )
        val updatedAttributeList2 = listOf(topicAttribute2)
        val updatedAttributeRequestList2 = listOf(topicAttributeRequest2, articlesAttributeRequest, videosAttributeRequest)
        val slot2 = slot<ArcXPIdentityListener>()
        verify {
            identityApiManager.updateProfile(
                ArcXPProfilePatchRequest(attributes = updatedAttributeRequestList2),
                capture(slot2)
            )
        }
        val response2 = mockk<ArcXPProfileManage> {
            every { attributes } returns updatedAttributeList2
        }
        slot2.captured.onProfileUpdateSuccess(profileManageResponse = response2)
        verify { arcXPIdentityListener.onProfileUpdateSuccess(profileManageResponse = response2) }
        slot2.captured.onProfileError(error = error)
        verify { arcXPIdentityListener.onProfileError(error = error) }



        assertTrue(testObject.currentSubscribedTopicsLiveData.value!!.contains(newRepeatedTopic))
        assertTrue(testObject.getPushNotificationTopics().contains(newRepeatedTopic))
        assertTrue(testObject.getPushNotificationTopics().size == 3)//updated and did not add
    }

    @Test
    fun `addTopic while not in current list`() {
        val expectedSub1 =
            TopicSubscription(name = "topic1", displayName = "display1", subscribed = true)
        val expectedSub2 =
            TopicSubscription(name = "topic2", displayName = "display2", subscribed = false)
        val expectedSub3 =
            TopicSubscription(name = "topic3", displayName = "display3", subscribed = true)
        val expectedList = listOf(expectedSub1, expectedSub2, expectedSub3)

        val expectedJson = toJson(expectedList)!!
        val topicAttributeRequest = ArcXPAttributeRequest(
            name = PUSH_NOTIFICATIONS_TOPICS_KEY,
            value = expectedJson,
            type = expectedType
        )
        val topicAttribute = ArcXPAttribute(
            name = PUSH_NOTIFICATIONS_TOPICS_KEY,
            value = expectedJson,
            type = expectedType
        )
        val videosAttributeRequest = ArcXPAttributeRequest(
            name = FAVORITE_VIDEOS_KEY,
            value = "[]",
            type = expectedType
        )
        val articlesAttributeRequest = ArcXPAttributeRequest(
            name = FAVORITE_ARTICLES_KEY,
            value = "[]",
            type = expectedType
        )
        val updatedAttributeRequestList = listOf(topicAttributeRequest, articlesAttributeRequest, videosAttributeRequest)
        val updatedAttributeList = listOf(topicAttribute)

        testObject.setSubscribedPushNotificationTopics(newTopics = expectedList)

        val slot = slot<ArcXPIdentityListener>()

        verify {
            identityApiManager.updateProfile(
                ArcXPProfilePatchRequest(attributes = updatedAttributeRequestList),
                capture(slot)
            )
        }
        val response = mockk<ArcXPProfileManage> {
            every { attributes } returns updatedAttributeList
        }
        slot.captured.onProfileUpdateSuccess(profileManageResponse = response)





        assertEquals(expectedList, testObject.getPushNotificationTopics())

        val newRepeatedTopic =
            TopicSubscription(name = "topic4", displayName = "display4", subscribed = false)


        testObject.addTopic(topicSubscription = newRepeatedTopic)

        val expectedList2 = listOf(expectedSub1, expectedSub2, expectedSub3, newRepeatedTopic)

        val expectedJson2 = toJson(expectedList2)!!
        val topicAttributeRequest2 = ArcXPAttributeRequest(
            name = PUSH_NOTIFICATIONS_TOPICS_KEY,
            value = expectedJson2,
            type = expectedType
        )
        val topicAttribute2 = ArcXPAttribute(
            name = PUSH_NOTIFICATIONS_TOPICS_KEY,
            value = expectedJson2,
            type = expectedType
        )
        val updatedAttributeList2 = listOf(topicAttribute2)
        val updatedAttributeRequestList2 = listOf(topicAttributeRequest2, articlesAttributeRequest, videosAttributeRequest)
        val slot2 = slot<ArcXPIdentityListener>()
        verify {
            identityApiManager.updateProfile(
                ArcXPProfilePatchRequest(attributes = updatedAttributeRequestList2),
                capture(slot2)
            )
        }
        val response2 = mockk<ArcXPProfileManage> {
            every { attributes } returns updatedAttributeList2
        }
        slot2.captured.onProfileUpdateSuccess(profileManageResponse = response2)



        assertTrue(testObject.currentSubscribedTopicsLiveData.value!!.contains(newRepeatedTopic))
        assertTrue(testObject.getPushNotificationTopics().contains(newRepeatedTopic))
        assertTrue(testObject.getPushNotificationTopics().size == 4)//updated and did not add
    }
}