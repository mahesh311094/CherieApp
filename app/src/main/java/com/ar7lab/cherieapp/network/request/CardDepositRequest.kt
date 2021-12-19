package com.ar7lab.cherieapp.network.request

class CardDepositRequest(
    var amount: Int,
    var currency:String,
    var paymentMethodId: String,
    var description: String
)

