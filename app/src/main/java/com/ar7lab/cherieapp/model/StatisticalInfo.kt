package com.ar7lab.cherieapp.model


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.io.Serializable

@JsonClass(generateAdapter = true)
data class StatisticalInfo(
    @Json(name = "avgAnnualAuctionTransactionInLast3Years")
    var avgAnnualAuctionTransactionInLast3Years: Int = 0,
    @Json(name = "avgAnnualValueGrowthRateOfSimilarArtwork")
    var avgAnnualValueGrowthRateOfSimilarArtwork: Int = 0,
    @Json(name = "estimatedValue")
    var estimatedValue: Int = 0,
    @Json(name = "highTradingVolumeAndTradingPrice")
    var highTradingVolumeAndTradingPrice: Int = 0,
    @Json(name = "_id")
    var id: String = "",
    @Json(name = "similarWorkAvgAnnualGrowth")
    var similarWorkAvgAnnualGrowth: Int = 0,
    @Json(name = "statisticsSimilarArtwork")
    var statisticsSimilarArtwork: Int = 0,
    @Json(name = "uploadGraph")
    var uploadGraph: Int = 0
) : Serializable