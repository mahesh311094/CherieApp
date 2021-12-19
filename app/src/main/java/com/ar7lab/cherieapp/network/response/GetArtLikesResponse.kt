package com.ar7lab.cherieapp.network.response

import com.ar7lab.cherieapp.model.LikeDetail

class GetArtLikesResponse : BaseResponse<GetArtLikesResponse.Data>() {
    class Data(
        var likeDetail: List<LikeDetail>
    )
}