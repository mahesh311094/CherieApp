package com.ar7lab.cherieapp.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.io.Serializable

@JsonClass(generateAdapter = true)
data class BannersItem(
    @Json(name = "_id")
    var id: String? = "",
    var createdAt: String? = "",
    var description: String? = "",
    var fileUrl: String? = ""
) : Serializable
