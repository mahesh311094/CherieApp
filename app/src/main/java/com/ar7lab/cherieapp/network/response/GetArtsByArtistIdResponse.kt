package com.ar7lab.cherieapp.network.response

import com.ar7lab.cherieapp.model.Art

class GetArtsByArtistIdResponse : BaseResponse<GetArtsByArtistIdResponse.Data>() {
    class Data {
        var arts: List<Art> = emptyList()
        var totalCount: Int = 0
    }
}

