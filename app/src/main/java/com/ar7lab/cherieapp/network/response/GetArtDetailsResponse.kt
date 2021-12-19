package com.ar7lab.cherieapp.network.response

import com.ar7lab.cherieapp.model.Art

class GetArtDetailsResponse : BaseResponse<GetArtDetailsResponse.Data>() {
    class Data(
        var art: Art
    )
}