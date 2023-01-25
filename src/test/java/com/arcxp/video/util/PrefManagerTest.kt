package com.arc.arcvideo.util

import android.content.Context
import android.content.SharedPreferences
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.verifySequence
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class PrefManagerTest {

    @MockK private lateinit var context: Context
    @RelaxedMockK private lateinit var sharedPreferences: SharedPreferences
    @RelaxedMockK private lateinit var sharedPreferencesEditor: SharedPreferences.Editor

    private val preference = "mapPreference"

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxUnitFun = true)
        every { sharedPreferences.edit() } returns sharedPreferencesEditor
        every { context.getSharedPreferences(preference, Context.MODE_PRIVATE)} returns sharedPreferences
    }

    @After
    fun tearDown() { clearAllMocks() }

    @Test
    fun `getBoolean returns boolean from shared preferences `(){
        val expectedKey = "key"
        val defaultValue = true
        every { sharedPreferences.getBoolean(expectedKey, defaultValue)} returns true

        assertTrue(PrefManager.getBoolean(context, expectedKey, defaultValue))
    }

    @Test
    fun `getString returns string from shared preferences`(){
        val expectedKey = "key"
        val defaultValue = "default value"
        val sharedPreferenceValue = "not default"
        every { sharedPreferences.getString(expectedKey, defaultValue)} returns sharedPreferenceValue

        assertEquals(sharedPreferenceValue, PrefManager.getString(context, expectedKey, defaultValue))
    }

    @Test
    fun `saveBoolean puts boolean, commits, and returns value`(){
        val expectedKey = "key"
        val expectedValue = true

        assertTrue(PrefManager.saveBoolean(context, expectedKey, expectedValue))

        verifySequence {
            sharedPreferencesEditor.putBoolean(expectedKey, expectedValue)
            sharedPreferencesEditor.commit()
        }
    }

    @Test
    fun `saveString puts string, commits, and returns value`(){
        val expectedKey = "key"
        val expectedValue = "value"

        assertEquals(expectedValue, PrefManager.saveString(context, expectedKey, expectedValue))

        verifySequence {
            sharedPreferencesEditor.putString(expectedKey, expectedValue)
            sharedPreferencesEditor.commit()
        }
    }
}