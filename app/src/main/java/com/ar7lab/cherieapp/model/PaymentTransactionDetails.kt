package com.ar7lab.cherieapp.model

import java.io.Serializable

/**
 * Payment transaction details serializable class
 * @property _id = payment id
 * @property transactionId = payment transaction id
 * @property status = payment status
 * @property amount = total paid amount
 * @property currency = amount currency Ex. like " $ "
 * @property transactionStatus = payment transaction status
 * @property description = payment description
 * @property paymentMethodType = payment method type
 * @property transactionReceiptUrl = transaction receipt url
 * @property artId = artid for paid amount
 * @property userId = user id for user
* */
data class PaymentTransactionDetails(
    var _id: String? = "",
    var transactionId: String? = "",
    var status: String? = "",
    var amount: Int? = 0,
    val createdAt: String? = "",
    var currency: String? = "",
    var transactionStatus: String? = "",
    var description: String? = "",
    var paymentMethodType: String? = "",
    var transactionReceiptUrl: String? = "",
    var artId: String? = "",
    var userId: String? = "",
) : Serializable