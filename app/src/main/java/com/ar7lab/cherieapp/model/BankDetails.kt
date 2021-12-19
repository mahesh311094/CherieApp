package com.ar7lab.cherieapp.model

import java.io.Serializable

/**
 * bankdetails details serializable class
 * @property defaultAccount = is user default account
 * @property _id = inserted detail id
 * @property country = Inserted Contry Name
 * @property bankName = Bank Name
 * @property accountName =Account Holder Name
 * @property accountNumber = Account Number
 * @property createdAt = inserted record date
 * @property updatedAt = upadted record date
 * */
data class BankDetails(
    var defaultAccount: Boolean = false,
    var _id: String = "",
    var country: String = "",
    var bankName: String = "",
    var accountName: String = "",
    var accountNumber: String = "",
    var createdAt: String = "",
    var updatedAt: String = ""
) : Serializable