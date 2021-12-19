package com.ar7lab.cherieapp.model


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class NewsCategory(
    @Json(name = "createdAt")
    var createdAt: String = "",
    @Json(name = "_id")
    var id: String = "",
    @Json(name = "name")
    var name: String = ""
)