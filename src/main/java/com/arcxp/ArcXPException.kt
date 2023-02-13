package com.arcxp

class ArcXPException : RuntimeException {
    /**
     * Returns the type of the error
     * @return [ArcXPCommerceSDKErrorType]
     */
    var type: ArcXPSDKErrorType? = null
        private set

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
     */
    constructor(type: ArcXPSDKErrorType, message: String, value: Any?) : super(message) {
        this.type = type
        this.value = value
    }

    /**
     * Constructor
     * @param type [ArcXPCommerceSDKErrorType]
     * @param message Description of the error
     */
    constructor(type: ArcXPSDKErrorType, message: String) : super(message) {
        this.type = type
    }

    /**
     * Constructor
     * @param message Description of the Error
     */
    constructor(message: String) : super(message) {}
}