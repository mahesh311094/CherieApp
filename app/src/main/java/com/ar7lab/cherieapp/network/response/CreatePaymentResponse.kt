package com.ar7lab.cherieapp.network.response

import com.ar7lab.cherieapp.model.PaymentTransactionDetails

class CreatePaymentResponse(
    var status: String,
    var message: String,
    var data: PaymentSuccessData
)

class PaymentSuccessData(
    var paymentTransectionDetail: PaymentTransactionDetails
)

