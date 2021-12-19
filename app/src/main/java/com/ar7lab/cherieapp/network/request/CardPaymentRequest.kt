package com.ar7lab.cherieapp.network.request

data class CardPaymentRequest(
    val artId: String,
    val amount: String,
    val currency: String,
    val paymentMethodId: String,
    val description: String,
)
