package com.ar7lab.cherieapp.network.request

import com.google.gson.annotations.SerializedName
import com.squareup.moshi.Json

class GetNotiicationRequest(
    var page: Int = 1,
    var limit: Int = 10,
    var sort: Sort = Sort()
) {
    class Sort(
        @Json(name = "createdAt")
        var sortTs: Int = -1
    )
}