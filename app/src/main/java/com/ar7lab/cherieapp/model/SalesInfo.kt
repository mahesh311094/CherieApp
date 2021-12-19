package com.ar7lab.cherieapp.model


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.io.Serializable

@JsonClass(generateAdapter = true)
data class SalesInfo(
    @Json(name = "_id")
    var id: String = "",
    @Json(name = "salesStartDate")
    var salesStartDate: String = "",
    @Json(name = "salesEndDate")
    var salesEndDate: String = "",
    @Json(name = "currentSale")
    var currentSale: CurrentSale = CurrentSale(),
    @Json(name = "pastSales")
    var pastSales: List<PastSale> = listOf(),
    @Json(name = "futureSales")
    var futureSales: List<FutureSale> = listOf(),
    @Json(name = "firstSalesQty")
    var firstSalesQty: Int = 0,
    @Json(name = "firstSalesStartDate")
    var firstSalesStartDate: String = "",
    @Json(name = "qtyOfOurHoldings")
    var qtyOfOurHoldings: Int = 0,
    @Json(name = "secondSalesQty")
    var secondSalesQty: Int = 0,
    @Json(name = "secondSalesStartDate")
    var secondSalesStartDate: String = "",
    @Json(name = "thirdSalesQty")
    var thirdSalesQty: Int = 0,
    @Json(name = "thirdSalesStartDate")
    var thirdSalesStartDate: String = "",
) : Serializable