package com.ar7lab.cherieapp.network.response

import com.ar7lab.cherieapp.model.News

class GetNewsBookmarkedResponse : BaseResponse<GetNewsBookmarkedResponse.Data>() {
    class Data {
        var bookmarkNewsData: List<News> = emptyList()
    }
}