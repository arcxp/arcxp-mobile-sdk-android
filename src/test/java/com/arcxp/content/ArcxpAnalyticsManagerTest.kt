package com.arcxp.content

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import com.arcxp.content.sdk.models.ArcxpAnalytics
import com.arcxp.content.sdk.models.EventType
import com.arcxp.content.sdk.util.AuthManager
import com.arcxp.content.sdk.util.BuildVersionProvider
import com.arcxp.content.sdk.util.Constants
import com.arcxp.content.sdk.util.MoshiController.toJson
import io.mockk.*
import io.mockk.impl.annotations.RelaxedMockK
import org.junit.Assert.assertEquals

import org.junit.Before
import org.junit.Test
import java.util.*

class ArcxpAnalyticsManagerTest {

    @RelaxedMockK
    lateinit var application: Application

    @RelaxedMockK
    lateinit var shared: SharedPreferences

    @RelaxedMockK
    lateinit var sharedEditor: SharedPreferences.Editor

    @RelaxedMockK
    lateinit var buildVersionProvider: BuildVersionProvider

    private lateinit var testObject: ArcXPAnalyticsManager2

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxUnitFun = true)
        every { application.getString(R.string.bearer_token) } returns "bearer token"
        mockkObject(AuthManager)
        every { buildVersionProvider.model() } returns "model"
        every { application.getSharedPreferences("analytics", Context.MODE_PRIVATE) } returns shared
        every { shared.edit() } returns sharedEditor

        testObject = ArcXPAnalyticsManager2(
            application,
            "arctesting1",
            "config",
            "sandbox",
            buildVersionProvider = buildVersionProvider
        )
    }


    @Test
    fun `Id is null and sharedPref does not contain deviceID shared Pref`() {

        mockkStatic(UUID::class)

        every { shared.contains("deviceID") } returns false
        every { shared.getString("deviceID", null) } returns null
        every { UUID.randomUUID().toString() } returns "123-456-789"

        testObject.sendAnalytics(EventType.COLLECTION)

        verify(exactly = 1) {

            sharedEditor.putString(Constants.DEVICE_ID, "123-456-789")
        }
    }

    @Test
    fun `Id is null and sharedPref contains deviceID`() {

        val currentMDate = Calendar.getInstance()
        val pendingAnalytics = mutableSetOf<String>()

        currentMDate.set(2022, 1, 1, 0, 0, 0)
        pendingAnalytics.add(
            toJson(
                ArcxpAnalytics(
                    EventType.COLLECTION,
                    "123",
                    "1",
                    currentMDate.time,
                    "tenantID"
                )
            )!!
        )

        every { shared.getStringSet("pendingAnalytics", null)?.isNotEmpty() } returns true
        every { shared.getStringSet("pendingAnalytics", null) } returns pendingAnalytics
        every { shared.contains("deviceID") } returns true
        every { shared.getString("deviceID", null) } returns "123"

        testObject.sendAnalytics(EventType.COLLECTION)

        assertEquals("123", shared.getString("deviceID", null))
        //TODO fix tests here, this is an invalid test
        //assertion on mock.. doesn't test anything
    }

    @Test
    fun `Makes a successful call and removes any pending analytics`() {


        testObject.makesSuccessfulCall()

        verify(exactly = 1) {
            sharedEditor.remove("pendingAnalytics")
        }

    }

    @Test
    fun `sendAnalytics - when ID is not null`() {


        every { shared.contains(Constants.DEVICE_ID) } returns true
        every { shared.getString(Constants.DEVICE_ID, null) } returns "123"

        testObject.sendAnalytics(EventType.COLLECTION)

        testObject.sendAnalytics(EventType.COLLECTION)

        verify(exactly = 1) {
            shared.getString(Constants.DEVICE_ID, null)
        }

    }

    @Test
    fun `failedCall - When sharedPref doesn't contain any pending analytics`() {

        every { shared.getStringSet("pendingAnalytics", null) } returns null

        testObject.sendAnalytics(EventType.COLLECTION)
        testObject.failedCall()

        assertEquals("test", null, shared.getStringSet(Constants.PENDING_ANALYTICS, null))
        //TODO fix tests here, another invalid test
        //assertion on mock.. doesn't test anything
    }

    @Test
    fun `setup - When deviceID is null and not in shared pref`() {

        mockkStatic(UUID::class)
        every { shared.getStringSet("pendingAnalytics", null) } returns null
        every { shared.getString("deviceID", null) } returns null
        every { UUID.randomUUID().toString() } returns "123-456-789"

        testObject.sendAnalytics(EventType.COLLECTION)

        verify(exactly = 1) {
            sharedEditor.putString(Constants.DEVICE_ID, "123-456-789")
        }

    }

}