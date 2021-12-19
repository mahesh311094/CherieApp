package com.ar7lab.cherieapp.network.response

import com.ar7lab.cherieapp.model.CardDetails

class GetWalletBalanceResponse : BaseResponse<GetWalletBalanceResponse.Data>() {
    class Data {
        var assets: Assets= Assets()
    }
    class Assets{
        var _id:String=""
        var NFT:String=""
        var TA:String=""
        var CASH:String=""
        var CRYPTOCURRENCY:String=""
        var walletBalance=WalletBalance()
    }
    class WalletBalance{
        var fiat:String=""
        var crypto:String=""
    }
}

