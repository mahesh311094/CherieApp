package com.ar7lab.cherieapp.network.response

import com.ar7lab.cherieapp.model.BankDetails
import com.ar7lab.cherieapp.model.CardDetails

class GetBanksResponse : BaseResponse<GetBanksResponse.Data>() {
    class Data {
        var bankDetails: List<BankDetails> = emptyList()
    }
}

