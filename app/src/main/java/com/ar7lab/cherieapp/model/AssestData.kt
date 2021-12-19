package com.ar7lab.cherieapp.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.io.Serializable

@JsonClass(generateAdapter = true)
data class AssestData (
    @Json(name = "payload")
var payload: Payload ,
var totalCount: Int?
):Serializable