package com.ar7lab.cherieapp.network.request

data class CherieWalletPaymentRequest(
    val artShares: String,
    val price: String,
    val salesDateId: String,
    val currency: String
)
