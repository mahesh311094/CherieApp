package com.ar7lab.cherieapp.network.repositories

import com.ar7lab.cherieapp.network.response.FeaturesResponse
import com.ar7lab.cherieapp.network.response.BannerResponse
import com.ar7lab.cherieapp.network.response.GetFAQsResponse

interface CommonRepository {
    suspend fun getFAQs(search: String = ""): GetFAQsResponse
    suspend fun submitRequestCallback(title: String, description: String): Any
    suspend fun getFeaturesList(): FeaturesResponse
    suspend fun getBanners(page: Int, limit: Int, filter: String?): BannerResponse
}