package com.ar7lab.cherieapp.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.io.Serializable

@JsonClass(generateAdapter = true)
data class Art(
    @Json(name = "_id")
    var id: String = "",
    var isNFT: Boolean = false,
    var fileUrl: String = "",
    var name: String = "",
    var price: Double = 0.0,
    var description: String = "",
    var artistId: String = "",
    var artist: Artist = Artist(),
    var investmentInfo: InvestmentInfo = InvestmentInfo(),
    var likeCount: Int = 0,
    var commentCount: Int = 0,
    var currency: String = "",
    var material: String = "",
    var yearOfArtRelease: String = "",
    var size: Size = Size(),
    var liked: Boolean = false,
    var source: String = "",
    var salesInfo: SalesInfo = SalesInfo(),
    var category: String = "",
    var statisticalInfo: StatisticalInfo = StatisticalInfo(),
    var status: String = "",
    var shareSalesRate: Double? = 0.0,
    var fileUrls: List<FileUrls> = listOf(),
    var titleHolders: Int? = 0,
    var soldShares: Int? = 0,
    var availableShares: Int? = 0,
    var totalPieces: Int? = 0,
    var piecePrice: Double? = 0.0,
    var thumbImageUrl: String? = "",
    var notified: Boolean = false,
    var signature: String? = "",
    var certificateIssuedBy: String? = "",
    var condition: String? = "",
    var observations: String? = "",
    var medium: String? = "",
    var title: String? = "",
    var typeOfArtwork: String? = "",
    var startFrom: Float? = 0f
) : Serializable {
    class Size : Serializable {
        var width: Double = 0.0
        var hight: Double = 0.0
    }
}