package com.ar7lab.cherieapp.network.request

class SearchArtsRequest(
    var page: Int = 1,
    var limit: Int = 10,
    var filter: String = "",
    var userId: String,
)