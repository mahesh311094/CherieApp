package com.ar7lab.cherieapp.network.request

class BankDepositRequest(
    var bankId: String,
    var paymentMethodType: String,
    var transactionType: Int=5,
    var amount: Float,
    var status: String="PENDING",
    var createdBy: String,
    var currency:String
)

