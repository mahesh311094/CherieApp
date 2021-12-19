package com.ar7lab.cherieapp.model


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.io.Serializable

@JsonClass(generateAdapter = true)
data class CurrentSale(
    @Json(name = "_id")
    var id: String = "",
    @Json(name = "salesStartDate")
    var salesStartDate: String = "",
    @Json(name = "salesEndDate")
    var salesEndDate: String = "",
    @Json(name = "salesQuantity")
    var salesQuantity: Int = 0,
    @Json(name = "soldShares")
    var soldShares: String = "",
    @Json(name = "soldPieces")
    var soldPieces: Int = 0,
) : Serializable