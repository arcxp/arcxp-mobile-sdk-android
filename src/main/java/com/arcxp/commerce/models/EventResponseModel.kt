package com.arcxp.commerce.models

data class EventResponseModel(
    val index: String,
    val type: String,
    val message: EventMessage,
    val eventTime: Long
)

data class EventMessage(
    val status: String,
    val identifier: String,
    val uuid: String,
    val subscriptionID: Long,
    val email: String,
    val nonce: String
)