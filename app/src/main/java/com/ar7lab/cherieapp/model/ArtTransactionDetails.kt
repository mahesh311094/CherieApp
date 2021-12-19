package com.ar7lab.cherieapp.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.io.Serializable

@JsonClass(generateAdapter = true)
data class ArtTransactionDetails(
    @Json(name = "_id")
    var id: String = "",
    var artId: String = "",
    var transactionHistory: TransactionHistory = TransactionHistory()
) : Serializable