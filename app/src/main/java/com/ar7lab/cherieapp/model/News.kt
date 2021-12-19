package com.ar7lab.cherieapp.model


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.io.Serializable

@JsonClass(generateAdapter = true)
data class News(
    @Json(name = "createdAt")
    var createdAt: String = "",
    @Json(name = "description")
    var description: String = "",
    @Json(name = "_id")
    var id: String = "",
    @Json(name = "fileUrl")
    var fileUrl: String = "",
    @Json(name = "isDeleted")
    var isDeleted: Boolean = false,
    @Json(name = "overview")
    var overview: String = "",
    @Json(name = "tag")
    var tag: String = "",
    @Json(name = "title")
    var title: String = "",
    @Json(name = "updatedAt")
    var updatedAt: String = "",
    var isBookmarked: Boolean = false
) : Serializable