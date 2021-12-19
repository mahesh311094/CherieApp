package com.ar7lab.cherieapp.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class Payload (
    @Json(name = "totalPieces")
    var totalPieces: Int,
    var assetsList: List<AssetsListItem>,
    var artData: ArtData
)