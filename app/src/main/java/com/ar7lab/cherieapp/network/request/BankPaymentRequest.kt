package com.ar7lab.cherieapp.network.request

data class BankPaymentRequest(
    val artId: String,
    val paymentType: String,
    val artShares: String,
    val price: String,
    val salesDateId: String,
    val currency: String,
    val bankId: String
)
