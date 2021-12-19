package com.ar7lab.cherieapp.network.response

import com.ar7lab.cherieapp.model.Art

class GetArtsResponse : BaseResponse<GetArtsResponse.Data>() {
    class Data {
        var arts: List<Art> = emptyList()
    }
}

