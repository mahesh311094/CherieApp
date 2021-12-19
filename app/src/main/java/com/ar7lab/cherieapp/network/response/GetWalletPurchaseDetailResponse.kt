package com.ar7lab.cherieapp.network.response

import com.ar7lab.cherieapp.model.FAQ
import com.ar7lab.cherieapp.model.WalletPurchaseDetail

class GetWalletPurchaseDetailResponse : BaseResponse<GetWalletPurchaseDetailResponse.Data>(){

    class Data(
        var transactionHistory: List<WalletPurchaseDetail>,
        var totalCount:Int?
    )

}

