package com.arc.arcvideo.util

import android.content.Context
import android.content.res.Resources
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.mockkStatic
import org.json.JSONException
import org.json.JSONObject
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.InputStream


class ConfigManagerTest {

    @RelaxedMockK lateinit var context: Context
    @RelaxedMockK lateinit var resources: Resources
    private val omSdkV1Json = "{\"type\": \"Video\",\"adConfigUrl\": \"\",\"adEnabled\": false }"
    private val inputStream: InputStream = ByteArrayInputStream(omSdkV1Json.toByteArray())


    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxUnitFun = true)
        every { context.resources } returns resources
        every { resources.openRawResource(any()) } returns inputStream
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `updateAndLoadConfig returns null if there is no local res config file`() {
        assertNull(ConfigManager.updateAndLoadConfig(context, -1))
    }

    @Test
    fun `readConfigFromResources throws Runtime Exception if it has a parse error `() {
        every { resources.openRawResource(any()) } throws JSONException("json error")
        assertThrows("Local(raw) config parse error", RuntimeException::class.java) {
            ConfigManager.updateAndLoadConfig(context, 123)
        }
    }

    @Test
    fun `updateAndLoadConfig returns JSONObject from resources `() {
        val expected = JSONObject(omSdkV1Json)
        mockkStatic(Utils::class)
        every { Utils.inputStreamToString(inputStream) } returns omSdkV1Json

        val actual = ConfigManager.updateAndLoadConfig(context, 123)

        assertEquals(expected.toString(), actual.toString())
    }
}