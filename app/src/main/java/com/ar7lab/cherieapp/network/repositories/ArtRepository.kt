package com.ar7lab.cherieapp.network.repositories

import com.ar7lab.cherieapp.network.response.*

interface ArtRepository {
    suspend fun searchArts(
        page: Int, limit: Int, filter: String?, userId: String, query: String = ""
    ): GetArtsResponse

    suspend fun searchArtsStore(
        page: Int, limit: Int, filter: String?, status: String?, userId: String, query: String = ""
    ): GetArtsForStoreResponse

    suspend fun likeArt(artId: String): Any
    suspend fun unLikeArt(artId: String): Any
    suspend fun getArtComments(artId: String, page: Int, limit: Int): GetArtCommentsResponse
    suspend fun sendArtComment(artId: String, comment: String): SendArtCommentResponse
    suspend fun getArtDetails(artId: String): GetArtDetailsResponse

    suspend fun getArtsByArtistId(
        page: Int, limit: Int, artistId: String, filter: String?
    ): GetArtsByArtistIdResponse

    suspend fun getArtsByArtistIdTraditional(
        page: Int, limit: Int, artistId: String
    ): GetArtsByArtistIdResponse

    suspend fun getArtsByArtistIdNft(
        page: Int, limit: Int, artistId: String
    ): GetArtsByArtistIdResponse

    suspend fun getArtLikes(artId: String, page: Int, limit: Int): GetArtLikesResponse

    suspend fun artTransactionHistory(
        artId: String,page: Int, limit: Int, filter: String?
    ): GetArtTransactionDetailsResponse

    suspend fun addNotify(artId: String): Any
    suspend fun deleteNotify(artId: String): Any

    suspend fun storeLikeDetails(
        page: Int, limit: Int, filter: String?
    ): GetStoreLikeDetailsResponse
}