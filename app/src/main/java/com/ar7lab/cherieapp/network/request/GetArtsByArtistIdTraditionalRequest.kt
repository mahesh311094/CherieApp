package com.ar7lab.cherieapp.network.request

class GetArtsByArtistIdTraditionalRequest(
    var page: Int = 1,
    var limit: Int = 10,
    var artistId: String,
)
