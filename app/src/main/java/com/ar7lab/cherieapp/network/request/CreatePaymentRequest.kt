package com.ar7lab.cherieapp.network.request

class CreatePaymentRequest(
    var cardNumber: String,
    var cardExpMonth: String,
    var cardExpYear: String,
    var cardCVC: String,
    var amount: String,
    var currency: String,
    var description: String,
    var paymentMethodId: String
)
