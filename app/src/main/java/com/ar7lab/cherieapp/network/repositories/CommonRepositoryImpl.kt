package com.ar7lab.cherieapp.network.repositories

import com.ar7lab.cherieapp.network.request.GetBannerRequest
import com.ar7lab.cherieapp.network.request.GetFAQsRequest
import com.ar7lab.cherieapp.network.request.RequestCallbackRequest
import com.ar7lab.cherieapp.network.response.FeaturesResponse
import com.ar7lab.cherieapp.network.response.BannerResponse
import com.ar7lab.cherieapp.network.response.GetFAQsResponse
import com.ar7lab.cherieapp.network.service.CommonService

class CommonRepositoryImpl(private val service: CommonService) : CommonRepository {
    override suspend fun getFAQs(search: String): GetFAQsResponse =
        service.getFAQs(GetFAQsRequest(search))

    override suspend fun submitRequestCallback(title: String, description: String): Any =
        service.submitRequestCallback(
            RequestCallbackRequest(title, description)
        )

    override suspend fun getFeaturesList(): FeaturesResponse =
        service.getFeaturesList()

    override suspend fun getBanners(
        page: Int,
        limit: Int,
        filter: String?,
    ): BannerResponse = service.getBanners(GetBannerRequest(page, limit, GetBannerRequest.Filter(filter)))
}