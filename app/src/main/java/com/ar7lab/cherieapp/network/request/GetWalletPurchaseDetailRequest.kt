package com.ar7lab.cherieapp.network.request

class GetWalletPurchaseDetailRequest(
    var page: Int = 1,
    var limit: Int = 10,
    var filter: Filter? = Filter(),
    var sort: Sort?=Sort()
) {
    class Filter() {
    }
    class Sort() {
    }
}
