package com.ar7lab.cherieapp.network.response

import com.ar7lab.cherieapp.model.FAQ

class GetFAQsResponse : BaseResponse<GetFAQsResponse.Data>() {
    class Data(
        var faqs: List<FAQ>
    )
}