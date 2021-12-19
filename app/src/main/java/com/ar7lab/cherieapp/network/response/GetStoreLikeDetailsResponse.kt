package com.ar7lab.cherieapp.network.response

import com.ar7lab.cherieapp.model.Art

class GetStoreLikeDetailsResponse : BaseResponse<GetStoreLikeDetailsResponse.Data>() {
    class Data {
        var onSaleArts: List<Art> = emptyList()
        var upcomingArts: List<Art> = emptyList()
        var pastArts: List<Art> = emptyList()
    }
}

