package com.ar7lab.cherieapp.network.request

import com.squareup.moshi.Json

class GetArtCommentsRequest(
    var page: Int = 1,
    var limit: Int = 10,
    var sort: Sort = Sort()
) {
    class Sort(
        @Json(name = "comments.ts")
        var sortTs: Int = -1
    )
}