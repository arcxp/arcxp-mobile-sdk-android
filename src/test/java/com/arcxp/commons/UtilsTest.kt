//package com.arcxp.commons
//
//import com.arcxp.ArcXPMobileSDK
//import com.arcxp.ArcXPMobileSDK.contentConfig
//import com.arcxp.commons.util.Utils.determineExpiresAt
//import io.mockk.clearAllMocks
//import io.mockk.every
//import io.mockk.mockkObject
//import io.mockk.mockkStatic
//import org.junit.After
//import org.junit.Assert.assertEquals
//import org.junit.Before
//import org.junit.Test
//import java.util.*
//
//class UtilsTest {
//
//    @Before
//    fun setUp() {
//        mockkObject(ArcXPMobileSDK)
//    }
//
//    @After
//    fun tearDown() {
//        clearAllMocks()
//    }
//
//    @Test
//    fun `determineExpiresAt from Header value`() {
//        every { contentConfig().cacheTimeUntilUpdateMinutes } returns null
//        val expected = Calendar.getInstance()
//        expected.set(2022, Calendar.MARCH, 1, 22, 5, 54)
//        expected.timeZone = TimeZone.getTimeZone("GMT")
//        expected.set(Calendar.MILLISECOND, 0)
//
//        val result = determineExpiresAt("Tue, 01 Mar 2022 22:05:54 GMT")
//
//        assertEquals(expected.time, result)
//    }
//
//    @Test
//    fun `determineExpiresAt from User value`() {
//        val initialDate = Calendar.getInstance()
//        initialDate.set(2022, Calendar.FEBRUARY, 8, 11, 0, 0)
//        val expected = Calendar.getInstance()
//        expected.set(2022, Calendar.FEBRUARY, 8, 11, 1, 0)
//        mockkStatic(Calendar::class)
//        every { Calendar.getInstance(any<TimeZone>()) } returns initialDate
//        every { contentConfig().cacheTimeUntilUpdateMinutes } returns 1
//
//        val actual = determineExpiresAt(expiresAt = "ignored")
//
//        val actualCalendar = Calendar.getInstance()
//        actualCalendar.time = actual
//        val actualYear = actualCalendar.get(Calendar.YEAR)
//        val actualMonth = actualCalendar.get(Calendar.MONTH)
//        val actualDay = actualCalendar.get(Calendar.DAY_OF_MONTH)
//        val actualHour = actualCalendar.get(Calendar.HOUR_OF_DAY)
//        val actualMinute = actualCalendar.get(Calendar.MINUTE)
//        val actualSecond = actualCalendar.get(Calendar.SECOND)
//        val expectedYear = expected.get(Calendar.YEAR)
//        val expectedMonth = expected.get(Calendar.MONTH)
//        val expectedDay = expected.get(Calendar.DAY_OF_MONTH)
//        val expectedHour = expected.get(Calendar.HOUR_OF_DAY)
//        val expectedMinute = expected.get(Calendar.MINUTE)
//        val expectedSecond = expected.get(Calendar.SECOND)
//        assertEquals(expectedYear, actualYear)
//        assertEquals(expectedMonth, actualMonth)
//        assertEquals(expectedDay, actualDay)
//        assertEquals(expectedHour, actualHour)
//        assertEquals(expectedMinute, actualMinute)
//        assertEquals(expectedSecond, actualSecond)
//
//    }
//}