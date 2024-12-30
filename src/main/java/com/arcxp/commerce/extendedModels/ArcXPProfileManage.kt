package com.arcxp.commerce.extendedModels

import androidx.annotation.Keep
import com.arcxp.commerce.models.*

/**
 * ArcXPProfileManage is a data class representing a user's profile within the ArcXP Commerce module.
 * It includes various attributes related to the user's personal information, contact details, addresses, and identities.
 *
 * The class defines the following properties:
 * - createdOn: The date when the profile was created.
 * - modifiedOn: The date when the profile was last modified.
 * - deletedOn: The date when the profile was deleted.
 * - firstName: The user's first name.
 * - lastName: The user's last name.
 * - secondLastName: The user's second last name.
 * - displayName: The user's display name.
 * - gender: The user's gender.
 * - email: The user's email address.
 * - unverifiedEmail: The user's unverified email address.
 * - picture: The URL to the user's profile picture.
 * - birthYear: The user's birth year.
 * - birthMonth: The user's birth month.
 * - birthDay: The user's birth day.
 * - emailVerified: A flag indicating whether the user's email is verified.
 * - contacts: A list of the user's contact details.
 * - addresses: A list of the user's addresses.
 * - attributes: A list of additional attributes related to the user.
 * - identities: A list of the user's identities.
 * - legacyId: The user's legacy ID.
 * - status: The user's status.
 * - deletionRule: The rule for deleting the user's profile.
 * - uuid: The unique identifier for the user's profile.
 *
 * The class also provides utility methods to format the user's birthdate, contact details, and address.
 *
 * Usage:
 * - Create an instance of ArcXPProfileManage and use the provided properties and methods to manage and display user profile information.
 *
 * Example:
 *
 * val profile = ArcXPProfileManage(
 *     createdOn = "2023-01-01",
 *     modifiedOn = "2023-01-02",
 *     deletedOn = "2023-01-03",
 *     firstName = "John",
 *     lastName = "Doe",
 *     uuid = "123e4567-e89b-12d3-a456-426614174000",
 *     identities = listOf()
 * )
 * val birthdate = profile.birthdate()
 * val contact = profile.contact()
 * val address = profile.address()
 *
 * Note: Ensure that the required properties are properly initialized before using ArcXPProfileManage.
 *
 * @method birthdate Format the user's birthdate.
 * @method contact Format the user's contact details.
 * @method address Format the user's address.
 */
@Keep
data class ArcXPProfileManage(
    val createdOn: String,
    val modifiedOn: String,
    val deletedOn: String,
    val firstName: String? = null,
    val lastName: String? = null,
    val secondLastName: String? = null,
    val displayName: String? = null,
    val gender: String? = null,
    val email: String? = null,
    val unverifiedEmail: String? = null,
    val picture: String? = null,
    val birthYear: String? = null,
    val birthMonth: String? = null,
    val birthDay: String? = null,
    val emailVerified: Boolean? = null,
    val contacts: List<ArcXPContact>? = null,
    val addresses: List<ArcXPAddress>? = null,
    val attributes: List<ArcXPAttribute>? = null,
    val identities: List<ArcXPIdentity>,
    val legacyId: String? = null,
    val status: String? = null,
    val deletionRule: Int? = null,
    val uuid: String
) {

    fun birthdate(): String? {
        if (birthMonth == null) {
            return null
        }
        return "$birthMonth/$birthDay/$birthYear"
    }

    fun contact(): String? {
        if (contacts == null) {
            return null
        }

        val number = contacts[0].phone
        val type = contacts[0].type

        var stringContact = when (type) {
            LocationType.HOME.name -> "🏠\n"
            LocationType.WORK.name -> "🏢\n"
            LocationType.OTHER.name -> "OTHER\n"
            LocationType.PRIMARY.name -> "PRIMARY\n"
            else -> ""
        }
        stringContact += when (number.length) {

            7 -> "${number.substring(0, 3)}-${number.substring(3)}"
            10 -> "(${number.substring(0, 3)})${number.substring(3, 6)}-${number.substring(6)}"
            11 -> "+${number[0]}(${number.substring(1, 4)})${
                number.substring(
                    4,
                    7
                )
            }-${number.substring(7)}"
            12 -> "+${number.substring(0, 2)}(${number.substring(2, 5)})${
                number.substring(
                    5,
                    8
                )
            }-${number.substring(8)}"
            else -> number
        }
        return stringContact
    }

    fun address(): String? {

        if (addresses == null) {
            return null
        }

        var stringAddress = ""
        val line1 = addresses.get(0).line1
        val line2 = addresses.get(0).line2
        val locality = addresses.get(0).locality
        val region = addresses.get(0).region
        val postal = addresses.get(0).postal
        val country = addresses.get(0).country
        val type = addresses.get(0).type

        var address = mapOf(
            "line1" to line1,
            "line2" to line2,
            "locality" to locality,
            "region" to region,
            "postal" to postal,
            "country" to country
        )

        when (type) {
            LocationType.HOME.name -> stringAddress = "🏠\n"
            LocationType.WORK.name -> stringAddress = "🏢\n"
            LocationType.OTHER.name -> stringAddress = "OTHER\n"
            LocationType.PRIMARY.name -> stringAddress = "PRIMARY\n"
            else -> ""
        }

        for ((k, v) in address) {
            if (v != null && k != "locality" && k != "region" && k != "postal") {
                if (k == "country") {
                    stringAddress += "\n${v.toUpperCase()}"
                } else {
                    stringAddress += "${v.capitalize()}\n"
                }
            } else if (v != null) {
                stringAddress += "${v.capitalize()} "
            }
        }
        return stringAddress
    }
}