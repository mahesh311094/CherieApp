package com.ar7lab.cherieapp.network.repositories

import com.ar7lab.cherieapp.network.response.GetNewsBookmarkedResponse
import com.ar7lab.cherieapp.network.response.GetNewsDetailsResponse
import com.ar7lab.cherieapp.network.response.GetNewsResponse
import com.ar7lab.cherieapp.network.response.NewsCategoryResponse

interface NewsRepository {
    suspend fun getNews(page: Int, limit: Int, filter: String?): GetNewsResponse
    suspend fun getNewsCategory(): NewsCategoryResponse
    suspend fun addBookmark(newsId: String): Any
    suspend fun removeBookmark(newsId: String): Any
    suspend fun getNewsBookmarked(page: Int, limit: Int): GetNewsBookmarkedResponse
    suspend fun getNewsDetails(id: String): GetNewsDetailsResponse
}