package com.ar7lab.cherieapp.network.response

import com.ar7lab.cherieapp.model.BannersItem

class BannerResponse : BaseResponse<BannerResponse.BannerData>() {
    class BannerData(
        var banners: List<BannersItem> = emptyList()
    )
}

