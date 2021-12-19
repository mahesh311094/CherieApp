package com.ar7lab.cherieapp.model

import com.squareup.moshi.Json
import java.io.Serializable

data class TransactionHistory(
    @Json(name = "_id")
    var id: String = "",
    var status: String = "",
    var amount: Double? = 0.0,
    var noOfShares: Double? = 0.0,
    var currency: String = ""
): Serializable