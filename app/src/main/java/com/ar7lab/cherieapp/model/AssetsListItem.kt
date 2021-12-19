package com.ar7lab.cherieapp.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.io.Serializable

@JsonClass(generateAdapter = true)
class AssetsListItem (
    @Json(name = "id")
    val id: String? = null,
    val createdAt: String? = null,
    val amount: Int? = null,
    val nftAddress: String? = null,
    val pieceNo: Int? = null,
    val pieceId: String? = null
): Serializable