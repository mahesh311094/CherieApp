package com.ar7lab.cherieapp.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.io.Serializable

@JsonClass(generateAdapter = true)
data class Notifications(
    @Json(name = "_id")
    var id: String = "",
    @Json(name = "readAt")
    var readAt: String? = "",
    @Json(name = "title")
    var title: String = "",
    @Json(name = "context")
    var context: String = "",
    @Json(name = "createdAt")
    var createdAt: String = "",
    @Json(name = "image")
    var image: String? ="",
    @Json(name = "type")
    var type: String? =""
): Serializable
