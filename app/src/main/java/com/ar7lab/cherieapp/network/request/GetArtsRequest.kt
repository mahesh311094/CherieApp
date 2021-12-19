package com.ar7lab.cherieapp.network.request

class GetArtsRequest(
    var page: Int = 1,
    var limit: Int = 10,
    var filter: Filter? = null,
    var userId: String,
    var search: String = ""
) {
    class Filter(var category: String? = "") {
    }
}