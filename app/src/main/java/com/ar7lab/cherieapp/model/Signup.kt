package com.ar7lab.cherieapp.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.io.Serializable

@JsonClass(generateAdapter = true)
data class Signup(
    @Json(name = "_id")
    var id: String = "",
    var role: String = "",
    var isVerified: Boolean = false,
    var email: String? = "",
    var contactNo: String? = "",
    var createdAt: String = "",
    var settings: SignupSettings = SignupSettings()
)