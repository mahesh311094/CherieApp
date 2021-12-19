package com.ar7lab.cherieapp.network.response

import com.ar7lab.cherieapp.model.ArtTransactionDetails

class GetArtTransactionDetailsResponse : BaseResponse<GetArtTransactionDetailsResponse.Data>() {
    class Data {
        var artTransactionDetails: List<ArtTransactionDetails> = emptyList()
    }
}

