package com.arcxp.commerce.models

import androidx.annotation.Keep

@Keep
data class ArcXPOrderHistory(val start: Int?,
                             val pageSize: Int?,
                             val maxResults: Int?,
                             val orders: List<Order?>?)

@Keep
data class Order(val orderNumber: String?,
                 val orderDate: String?,
                 val orderType: String?,
                 val orderStatus: String?,
                 val totalAmount: Double?,
                 val tax: Double?,
                 val currency: String?,
                 val products: List<Product?>?,
                 val paymentGateway: String?,
                 val lastFour: String?,
                 val nameOnPayment: String?,
                 val creditCardBrand: String?)

@Keep
data class Product(val sku: String?,
                    val name: String?,
                    val quantity: Int?)