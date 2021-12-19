package com.ar7lab.cherieapp.model

import com.squareup.moshi.JsonClass
import java.io.Serializable


@JsonClass(generateAdapter = true)
data class Like(
    var userName: String?,
    var profileImage: String?,
) : Serializable

@JsonClass(generateAdapter = true)
data class LikeDetail(
    var users: Like
) : Serializable