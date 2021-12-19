package com.ar7lab.cherieapp.network.service

import com.ar7lab.cherieapp.network.request.GetNewsRequest
import com.ar7lab.cherieapp.network.response.GetNewsBookmarkedResponse
import com.ar7lab.cherieapp.network.response.GetNewsDetailsResponse
import com.ar7lab.cherieapp.network.response.GetNewsResponse
import com.ar7lab.cherieapp.network.response.NewsCategoryResponse
import retrofit2.http.*

interface NewsService {
    @POST("news/list")
    suspend fun getNews(@Body getNewsRequest: GetNewsRequest): GetNewsResponse

    @GET("news/category/list")
    suspend fun getNewsCategory(): NewsCategoryResponse

    @POST("/news/{id}/bookmark")
    suspend fun addBookmark(@Path("id") newsId: String): Any

    @DELETE("/news/{id}/bookmark")
    suspend fun removeBookmark(@Path("id") newsId: String): Any

    @POST("news/bookmark/list")
    suspend fun getNewsBookmarked(@Body getNewsRequest: GetNewsRequest): GetNewsBookmarkedResponse

    @GET("news/{id}")
    suspend fun getNewsDetails(@Path("id") id: String): GetNewsDetailsResponse
}