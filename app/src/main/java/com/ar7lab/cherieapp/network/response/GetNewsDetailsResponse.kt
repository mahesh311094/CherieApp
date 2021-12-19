package com.ar7lab.cherieapp.network.response

import com.ar7lab.cherieapp.model.News

class GetNewsDetailsResponse : BaseResponse<GetNewsDetailsResponse.Data>() {
    class Data(
        var news: News
    )
}