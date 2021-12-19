package com.ar7lab.cherieapp.model


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.io.Serializable

@JsonClass(generateAdapter = true)
data class FileUrls(
    @Json(name = "_id")
    var id: String = "",
    @Json(name = "url")
    var url: String = "",
    @Json(name = "index")
    var index: String = ""
):Serializable