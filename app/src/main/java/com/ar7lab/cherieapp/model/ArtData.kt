package com.ar7lab.cherieapp.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.io.Serializable

@JsonClass(generateAdapter = true)
data class ArtData (
    @Json(name = "artId")
    var artId: String?,
    var artName: String?,
    var artDescription: String?,
    var fileUrls: List<FileUrls?>?,
    var artistName: String?,
    var artistBirthDate: String?,
    var thumbImageUrl: String?,
    var yearOfArtRelease: Int?
):Serializable