package com.ar7lab.cherieapp.network.response

import com.ar7lab.cherieapp.model.Artist

class GetArtistsResponse : BaseResponse<GetArtistsResponse.Data>() {
    class Data {
        var artists: List<Artist> = emptyList()
    }
}

