package com.ar7lab.cherieapp.network.service

import com.ar7lab.cherieapp.network.request.GetBannerRequest
import com.ar7lab.cherieapp.network.request.GetFAQsRequest
import com.ar7lab.cherieapp.network.request.RequestCallbackRequest
import com.ar7lab.cherieapp.network.response.FeaturesResponse
import com.ar7lab.cherieapp.network.response.BannerResponse
import com.ar7lab.cherieapp.network.response.GetFAQsResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface CommonService {

    @POST("FAQ/list")
    suspend fun getFAQs(@Body getFAQsRequest: GetFAQsRequest): GetFAQsResponse

    @POST("query/detail")
    suspend fun submitRequestCallback(@Body requestCallbackRequest: RequestCallbackRequest): Any

    @POST("banner/list")
    suspend fun getBanners(@Body getBannerRequest: GetBannerRequest): BannerResponse

    @POST("feature-screen/list")
    suspend fun getFeaturesList(): FeaturesResponse

}