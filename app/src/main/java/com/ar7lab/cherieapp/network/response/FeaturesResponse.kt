package com.ar7lab.cherieapp.network.response

import com.ar7lab.cherieapp.model.Features


class FeaturesResponse : BaseResponse<FeaturesResponse.Data>() {
    class Data(
        var featureScreens: List<Features>
    )
}