package com.ar7lab.cherieapp.network.request

class GetArtsByArtistIdNftRequest(
    var page: Int = 1,
    var limit: Int = 10,
    var artistId: String,
)
