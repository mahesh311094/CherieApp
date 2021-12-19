package com.ar7lab.cherieapp.network.response

import com.ar7lab.cherieapp.model.CardDetails

class GetCardsResponse : BaseResponse<GetCardsResponse.Data>() {
    class Data {
        var cardDetails: List<CardDetails> = emptyList()
    }
}

