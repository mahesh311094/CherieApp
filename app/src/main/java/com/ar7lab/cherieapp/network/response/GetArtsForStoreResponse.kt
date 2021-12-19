package com.ar7lab.cherieapp.network.response

import com.ar7lab.cherieapp.model.Art
import com.ar7lab.cherieapp.model.Artist

class GetArtsForStoreResponse : BaseResponse<GetArtsForStoreResponse.Data>() {
    class Data {
        var onSaleArts: List<Art> = emptyList()
        var upcomingArts: List<Art> = emptyList()
        var pastArts: List<Art> = emptyList()
        var artists: List<Artist> = emptyList()
        var onSaleArtsCount: Int? = 0
        var upcomingArtsCount: Int? = 0
        var pastArtsCount: Int? = 0
        var artistsCount: Int? = 0
    }
}

