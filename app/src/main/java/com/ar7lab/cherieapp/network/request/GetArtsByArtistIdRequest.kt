package com.ar7lab.cherieapp.network.request

class GetArtsByArtistIdRequest(
    var page: Int = 1,
    var limit: Int = 10,
    var artistId: String,
    var filter: Filter? = null
) {
    class Filter(var status: String? = "") {
    }
}