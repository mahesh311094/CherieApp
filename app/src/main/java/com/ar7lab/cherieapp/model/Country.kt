package com.ar7lab.cherieapp.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Country(
    var name: String,
    var code2: String,
    var code3: String,
    var phone: String
)