package com.ar7lab.cherieapp.network.response

import com.ar7lab.cherieapp.model.Artist

class GetArtistDetailsResponse : BaseResponse<GetArtistDetailsResponse.Data>() {
    class Data (
        var artist: Artist
    )
}

