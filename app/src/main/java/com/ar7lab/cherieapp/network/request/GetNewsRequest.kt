package com.ar7lab.cherieapp.network.request

import com.squareup.moshi.Json

class GetNewsRequest(
    var page: Int = 1,
    var limit: Int = 10,
    var filter: Filter? = null,
    var sort: Sort = Sort()
) {
    class Filter(var tag: String? = "")
    class Sort(
        @Json(name = "createdAt")
        var sortTs: Int = -1
    )
}