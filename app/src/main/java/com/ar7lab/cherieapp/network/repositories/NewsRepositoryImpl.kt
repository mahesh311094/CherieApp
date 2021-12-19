package com.ar7lab.cherieapp.network.repositories

import com.ar7lab.cherieapp.network.request.GetNewsRequest
import com.ar7lab.cherieapp.network.response.GetNewsBookmarkedResponse
import com.ar7lab.cherieapp.network.response.GetNewsDetailsResponse
import com.ar7lab.cherieapp.network.response.NewsCategoryResponse
import com.ar7lab.cherieapp.network.service.NewsService

class NewsRepositoryImpl(private val service: NewsService) : NewsRepository {
    override suspend fun getNews(page: Int, limit: Int, filter: String?) =
        service.getNews(GetNewsRequest(page, limit, GetNewsRequest.Filter(filter)))

    override suspend fun getNewsCategory(): NewsCategoryResponse = service.getNewsCategory()
    override suspend fun addBookmark(newsId: String): Any = service.addBookmark(newsId)
    override suspend fun removeBookmark(newsId: String): Any = service.removeBookmark(newsId)
    override suspend fun getNewsBookmarked(page: Int, limit: Int): GetNewsBookmarkedResponse =
        service.getNewsBookmarked(
            GetNewsRequest(page, limit)
        )

    override suspend fun getNewsDetails(id: String): GetNewsDetailsResponse =
        service.getNewsDetails(id)
}