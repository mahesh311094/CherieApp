package com.ar7lab.cherieapp.model

import java.io.Serializable

/**
 * WalletPurchaseDetail serializable class
 * @property type = transation type
 * @property name = Artist Name
 * @property description = description
 * @property image = Photo Url
 * @property method = transation method
 * @property price = Price
 * @property purchasedAt = Purchase Date
 * @property artId = Art Id
 * */
data class WalletPurchaseDetail(
    var _id: String = "",
    var status: String = "",
    var amount: String = "",
    var currency: String = "",
    var createdAt: String = "",
): Serializable {

}