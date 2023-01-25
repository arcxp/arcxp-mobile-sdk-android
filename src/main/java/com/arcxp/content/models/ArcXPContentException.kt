package com.arcxp.content.models


/**
 * Fatal Exceptions
 *
 */
class ArcXPContentException : RuntimeException {
    /**
     * Returns the type of the error
     * @return [ArcXPContentSDKErrorType]
     */
    var type: ArcXPContentSDKErrorType? = null
        private set

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
    constructor(type: ArcXPContentSDKErrorType, message: String, value: Any?) : super(message) {
        this.type = type
        this.value = value
    }

    /**
     * Constructor
     * @param type [ArcXPContentSDKErrorType]
     * @param message Description of the Exception
     */
    constructor(type: ArcXPContentSDKErrorType, message: String) : super(message) {
        this.type = type
    }

    /**
     * Constructor
     * @param message Description of the Exception
     */
    constructor(message: String) : super(message) {}
}