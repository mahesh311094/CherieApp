package com.ar7lab.cherieapp.network.request

class AddBankRequest(
    var idOfUser: String,
    var country: String,
    var bankName: String,
    var accountName: String,
    var accountNumber: String,
    var defaultAccount: Boolean
)

