package com.ar7lab.cherieapp.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.io.Serializable

@JsonClass(generateAdapter = true)
data class MyCollectionDetails(
    @Json(name = "_id")
    var id: String,
    var totalHold: Int,
    var totalPrice: Int,
    var yearOfArtRelease: Int,
    var name: String,
    var description: String,
    var fileUrls: List<FileUrls> = listOf(),
    var thumbImageUrl: String,
    var artistName: String,
    var artistBirthDate: String
) : Serializable