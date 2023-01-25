package com.arcxp.commerce.util

import com.arcxp.commerce.ArcXPCommerceSDKErrorType
import org.json.JSONObject
import retrofit2.Response

/**
 * @suppress
 */

class ArcXPError : Throwable {

    /**
     * Returns the type of the error
     * @return [ArcXPCommerceSDKErrorType]
     */
    var type: ArcXPCommerceSDKErrorType? = null
        private set

    var code: String? = null

    override var message: String? = null

    /**
     * Returns the attached value object
     * @return An object
     */
    var value: Any? = null
        private set

    /**
     * Constructor
     * @param type [ArcXPCommerceSDKErrorType]
     * @param message Description of the error
     * @param value An object appropriate for the exception
     * @suppress
     */
    constructor(type: ArcXPCommerceSDKErrorType, message: String, value: Any?) {
        this.type = type
        this.value = value
        if (this.value is Response<*>) {
            val jsonObj = JSONObject((value as Response<*>).errorBody()!!.charStream().readText())
            this.code = jsonObj.getString("code")
            this.message = jsonObj.getString("message")
        }
    }

    /**
     * Constructor
     * @param type [ArcXPCommerceSDKErrorType]
     * @param message Description of the error
     * @suppress
     */
    constructor(type: ArcXPCommerceSDKErrorType, message: String) {
        this.type = type
        this.message = message
    }

    /**
     * Constructor
     * @param message Description of the Error
     * @suppress
     */
    constructor(message: String) {
        this.message = message
    }
}