package com.ar7lab.cherieapp.network.response

import com.ar7lab.cherieapp.model.CommentDetail

class GetArtCommentsResponse : BaseResponse<GetArtCommentsResponse.Data>() {
    class Data {
        var commentDetail: List<CommentDetail> = emptyList()
    }
}