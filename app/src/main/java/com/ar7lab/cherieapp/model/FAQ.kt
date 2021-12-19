package com.ar7lab.cherieapp.model


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.io.Serializable

@JsonClass(generateAdapter = true)
data class FAQ(
    @Json(name = "answer")
    var answer: String = "",
    @Json(name = "createdAt")
    var createdAt: String = "",
    @Json(name = "createdBy")
    var createdBy: String = "",
    @Json(name = "_id")
    var id: String = "",
    @Json(name = "question")
    var question: String = "",
    @Json(name = "updatedAt")
    var updatedAt: String = ""
) : Serializable