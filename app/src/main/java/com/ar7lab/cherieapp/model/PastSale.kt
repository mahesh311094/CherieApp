package com.ar7lab.cherieapp.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.io.Serializable

@JsonClass(generateAdapter = true)
data class PastSale (
    @Json(name = "_id")
    var id: String = "",
    var salesStartDate: String = "",
    var salesEndDate: String = "",
    var salesQuantity: Float = 0f,
    var soldShares: Int = 0,
    var soldPieces: Int = 0
) : Serializable