package com.arcxp.commerce.models

import com.arcxp.commerce.extendedModels.ArcXPProfileManage
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ArcXPProfileManageTest {


    @Test
    fun `birthday() - returns null when month is null`(){

        val testObject = createArcProfileManager()

         assertNull(testObject.birthdate())
    }

    @Test
    fun `birthday() - returns formatted birthday when month is not null`(){

        val testObject = createArcProfileManager("2020", "01", "01")

        val expected = "01/01/2020"

        val actual  = testObject.birthdate()
        assertEquals(expected, actual)
    }

    @Test
    fun `contact() home - returns formatted home contact when phone number is 10 digits`(){
        val contact = ArcXPContact("1234567890", "HOME")
        val testObject = createArcProfileManager(contacts = listOf(contact))

        val expected = "\uD83C\uDFE0\n(123)456-7890"
        val actual = testObject.contact()

        assertEquals(expected, actual)
    }

    @Test
    fun `contact() work - returns formatted work contact when phone number is 11 digits`(){
        val contact = ArcXPContact("11234567890", "WORK")
        val testObject = createArcProfileManager(contacts = listOf(contact))

        val expected = "\uD83C\uDFE2\n+1(123)456-7890"
        val actual = testObject.contact()

        assertEquals(expected, actual)
    }

    @Test
    fun `contact() other - returns formatted other contact when phone number is 7 digits`(){
        val contact = ArcXPContact("4567890", "OTHER")
        val testObject = createArcProfileManager(contacts = listOf(contact))

        val expected = "OTHER\n456-7890"
        val actual = testObject.contact()

        assertEquals(expected, actual)
    }

    @Test
    fun `contact() primary - returns formatted primary contact when phone number is 12 digits`(){
        val contact = ArcXPContact("011234567890", "PRIMARY")
        val testObject = createArcProfileManager(contacts = listOf(contact))

        val expected = "PRIMARY\n+01(123)456-7890"
        val actual = testObject.contact()

        assertEquals(expected, actual)
    }

    @Test
    fun `contact() - returns formatted primary contact when phone number is less than 7 digits`(){
        val contact = ArcXPContact("123456", "PRIMARY")
        val testObject = createArcProfileManager(contacts = listOf(contact))

        val expected = "PRIMARY\n123456"
        val actual = testObject.contact()

        assertEquals(expected, actual)
    }


    @Test
    fun `contact() no type - returns formatted number without type when type is NA`(){
        val contact = ArcXPContact("123456", "n/a")
        val testObject = createArcProfileManager(contacts = listOf(contact))

        val expected = "123456"
        val actual = testObject.contact()

        assertEquals(expected, actual)
    }

    @Test
    fun `contact() - returns null when contact list is empty`(){
        val testObject = createArcProfileManager()

        assertNull(testObject.contact())
    }

    @Test
    fun `address() - returns null when address list is empty`(){
        val testObject = createArcProfileManager()

        assertNull(testObject.address())
    }

    @Test
    fun `address() home - returns formatted home address when addresses is not null`(){
        val address = ArcXPAddress(
            "line1",
            null,
            "locality",
            null,
            null,
            "country",
            "HOME"
        )
        val testObject = createArcProfileManager(addresses = listOf(address))

        val expected = "\uD83C\uDFE0\nLine1\nLocality \nCOUNTRY"
        val actual = testObject.address()

        assertEquals(expected, actual)
    }

    @Test
    fun `address() work - returns formatted work address when addresses is not null`(){
        val address = ArcXPAddress(
            "line1",
            "line2",
            "locality",
            "region",
            "10036",
            "country",
            "WORK"
        )
        val testObject = createArcProfileManager(addresses = listOf(address))

        val expected = "\uD83C\uDFE2\nLine1\nLine2\nLocality Region 10036 \nCOUNTRY"
        val actual = testObject.address()

        assertEquals(expected, actual)
    }

    @Test
    fun `address() other - returns formatted other address when addresses is not null`(){
        val address = ArcXPAddress(
            "line1",
            "line2",
            "locality",
            "region",
            "10036",
            "country",
            "OTHER"
        )
        val testObject = createArcProfileManager(addresses = listOf(address))

        val expected = "OTHER\nLine1\nLine2\nLocality Region 10036 \nCOUNTRY"
        val actual = testObject.address()

        assertEquals(expected, actual)
    }

    @Test
    fun `address() primary - returns formatted primary address when addresses is not null`(){
        val address = ArcXPAddress(
            "line1",
            "line2",
            "locality",
            "region",
            "10036",
            "country",
            "PRIMARY"
        )
        val testObject = createArcProfileManager(addresses = listOf(address))

        val expected = "PRIMARY\nLine1\nLine2\nLocality Region 10036 \nCOUNTRY"
        val actual = testObject.address()

        assertEquals(expected, actual)
    }

    @Test
    fun `address() no type - returns formatted address without a type when address type is NA`(){
        val address = ArcXPAddress(
            "line1",
            "line2",
            "locality",
            "region",
            "10036",
            "country",
            "n/a"
        )
        val testObject = createArcProfileManager(addresses = listOf(address))

        val expected = "Line1\nLine2\nLocality Region 10036 \nCOUNTRY"
        val actual = testObject.address()

        assertEquals(expected, actual)
    }

    @Test
    fun `calling getters for test coverage purposes`(){
        val testObject = createArcProfileManager()

        testObject.firstName
        testObject.createdOn
        testObject.modifiedOn
        testObject.deletedOn
        testObject.lastName
        testObject.secondLastName
        testObject.displayName
        testObject.gender
        testObject.email
        testObject.unverifiedEmail
        testObject.picture
        testObject.birthYear
        testObject.birthMonth
        testObject.birthDay
        testObject.legacyId
        testObject.contacts
        testObject.addresses
        testObject.attributes
        testObject.identities
        testObject.deletionRule
        testObject.emailVerified
        testObject.status
        testObject.uuid
    }

}

private fun createArcProfileManager(birthYear: String? = null, birthMonth: String? = null, birthDay: String? = null, contacts: List<ArcXPContact>? = null, addresses: List<ArcXPAddress>? = null ): ArcXPProfileManage{
    val identity = ArcXPIdentity("01/01/2020",
        "John Smith",
        "01/01/2022",
        "John Smith",
        "null",
        123,
        "John",
        false,
        "password",
        "01/01/2020",
        false)

    return ArcXPProfileManage("01/01/2020", "01/01/2022", "null", "John", "Smith", null, null, null, null, null, null, birthYear, birthMonth, birthDay, null, contacts, addresses, null, listOf(identity), null, null, null, "uuid" )
}