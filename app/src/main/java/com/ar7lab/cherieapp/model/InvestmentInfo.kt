package com.ar7lab.cherieapp.model

import com.squareup.moshi.Json
import java.io.Serializable

data class InvestmentInfo(
    @Json(name = "_id")
    var id: String = "",
    var titleHolders: String = "",
    var ownershipSales: Int = 0,
    var remainingPercentageShares: Int = 0,
    var residualOrTotalOwnership: Int = 0,
    var qtyOfOurHoldings: Int = 0,
    var publicOfferingPrice: Double = 0.0,
): Serializable