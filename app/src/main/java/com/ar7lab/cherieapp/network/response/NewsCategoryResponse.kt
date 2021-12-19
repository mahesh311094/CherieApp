package com.ar7lab.cherieapp.network.response

import com.ar7lab.cherieapp.model.NewsCategory

class NewsCategoryResponse : BaseResponse<NewsCategoryResponse.Data>() {
    class Data(
        var categoryList: List<NewsCategory> = emptyList()
    )
}