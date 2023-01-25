package com.arcxp.content.models

import org.json.JSONObject
import retrofit2.Response


/**
 * Recoverable Errors
 */
class ArcXPContentError : Throwable {

    /**
     * Returns the type of the error
     * @return [ArcXPContentSDKErrorType]
     */
    val type: ArcXPContentSDKErrorType

    /**
     * Returns code String from error body if it exists in value constructor parameter
     */
    var code: String? = null
        private set

    /**
     * Description of the error
     */
    override val message: String

    /**
     * Returns the attached value object
     * @return An object
     */
    var value: Any? = null
        private set

    /**
     * Constructor
     * @param type [ArcXPContentSDKErrorType]
     * @param message Description of the error
     * @param value An object appropriate for the exception
     */
    constructor(type: ArcXPContentSDKErrorType, message: String, value: Any?) {
        this.type = type
        this.value = value
        this.message = message
        if (value is Response<*>) {
            this.code = value.errorBody()?.let {
                JSONObject(it.charStream().readText()).getString("code")
            }
        }
    }

    /**
     * Constructor
     * @param type [ArcXPContentSDKErrorType]
     * @param message Description of the error
     */
    constructor(type: ArcXPContentSDKErrorType, message: String) {
        this.type = type
        this.message = message
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ArcXPContentError

        if (type != other.type) return false
        if (code != other.code) return false
        if (message != other.message) return false
        if (value != other.value) return false

        return true
    }

    override fun hashCode(): Int {
        var result = type.hashCode()
        result = 31 * result + (code?.hashCode() ?: 0)
        result = 31 * result + message.hashCode()
        result = 31 * result + (value?.hashCode() ?: 0)
        return result
    }
}