package com.ar7lab.cherieapp.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.io.Serializable

@JsonClass(generateAdapter = true)
data class Artist(
    @Json(name = "_id")
    var id: String = "",
    var profilePicture: String? = "",
    var name: String = "",
    var overview: String = "",
    var followed: Boolean = false,
    var role: String = "",
    var birthDate: String = "",
    var deathDate: String = "",
    var likeCount: Int = 0,
    var followersCount: Int = 0,
    var liked: Boolean = false,
) : Serializable