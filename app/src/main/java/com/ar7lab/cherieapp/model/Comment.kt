package com.ar7lab.cherieapp.model

import com.squareup.moshi.JsonClass
import java.io.Serializable

@JsonClass(generateAdapter = true)
data class Comment(
    var content: String = "Dust Monkey",
    var ts: String = "1h ago",
    var user: List<User> = listOf(),
    var userId: String = ""
) : Serializable

@JsonClass(generateAdapter = true)
data class CommentDetail(
    var comments: Comment = Comment()
) : Serializable