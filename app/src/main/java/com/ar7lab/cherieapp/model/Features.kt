package com.ar7lab.cherieapp.model


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.io.Serializable

@JsonClass(generateAdapter = true)
data class Features(
    @Json(name = "_id")
    var id: String = "",
    @Json(name = "fileUrl")
    var fileUrl: String = "",
    @Json(name = "description")
    var description: String = "",
    @Json(name = "title")
    var title: String = "",
    @Json(name = "createdAt")
    var createdAt: String = ""
) : Serializable