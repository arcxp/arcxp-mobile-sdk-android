package com.arcxp.commerce.extendedModels

import androidx.annotation.Keep
import com.arcxp.commerce.models.*

/**
 * @suppress
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