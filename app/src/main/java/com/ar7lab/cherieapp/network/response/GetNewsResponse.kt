package com.ar7lab.cherieapp.network.response

import com.ar7lab.cherieapp.model.News

class GetNewsResponse : BaseResponse<GetNewsResponse.Data>() {
    class Data {
        var newsList: List<News> = emptyList()
    }
}