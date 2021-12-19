package com.ar7lab.cherieapp.network.repositories

import com.ar7lab.cherieapp.network.request.*
import com.ar7lab.cherieapp.network.response.GetArtLikesResponse
import com.ar7lab.cherieapp.network.response.GetArtTransactionDetailsResponse
import com.ar7lab.cherieapp.network.service.ArtService

class ArtRepositoryImpl(private val service: ArtService) : ArtRepository {

    override suspend fun searchArts(
        page: Int,
        limit: Int,
        filter: String?,
        userId: String,
        query: String
    ) =
        service.searchArt(GetArtsRequest(page, limit, GetArtsRequest.Filter(filter), userId, query))

    override suspend fun searchArtsStore(
        page: Int,
        limit: Int,
        filter: String?,
        status: String?,
        userId: String,
        query: String
    ) =
        service.searchArtForStore(GetArtsForStoreRequest(page, limit, GetArtsForStoreRequest.Filter(filter,status), userId, query))

    override suspend fun likeArt(artId: String) = service.likeArt(artId)

    override suspend fun unLikeArt(artId: String) = service.unLikeArt(artId)

    override suspend fun getArtComments(artId: String, page: Int, limit: Int) =
        service.getArtComments(artId, GetArtCommentsRequest(page, limit))

    override suspend fun sendArtComment(artId: String, comment: String) =
        service.sendArtComment(artId, SendArtCommentRequest(comment))

    override suspend fun getArtDetails(artId: String) = service.getArtDetails(artId)

    override suspend fun getArtsByArtistId(
        page: Int,
        limit: Int,
        artistId: String,
        filter: String?
    ) = service.getArtsByArtistId(GetArtsByArtistIdRequest(page, limit, artistId, GetArtsByArtistIdRequest.Filter(filter)))

    override suspend fun getArtsByArtistIdTraditional(
        page: Int,
        limit: Int,
        artistId: String
    ) = service.getArtsByArtistIdTraditional(
        GetArtsByArtistIdTraditionalRequest(
            page,
            limit,
            artistId
        )
    )

    override suspend fun getArtsByArtistIdNft(
        page: Int,
        limit: Int,
        artistId: String
    ) = service.getArtsByArtistIdNft(GetArtsByArtistIdNftRequest(page, limit, artistId))

    override suspend fun getArtLikes(artId: String, page: Int, limit: Int): GetArtLikesResponse =
        service.getArtLikes(artId, GetArtLikesRequest(page, limit))

    override suspend fun artTransactionHistory(
        artId: String,
        page: Int,
        limit: Int,
        filter: String?
    ) = service.getArtTransactionDetails(artId,GetArtTransactionDetailsRequest(page, limit, GetArtTransactionDetailsRequest.Filter(filter)))

    override suspend fun addNotify(artId: String) = service.addNotify(artId)

    override suspend fun deleteNotify(artId: String) = service.deleteNotify(artId)

    override suspend fun storeLikeDetails(
        page: Int,
        limit: Int,
        filter: String?,
    ) =
        service.storeArtLikeDetails(GetStoreLikeDetailsRequest(page, limit))
//    GetStoreLikeDetailsRequest.Filter(filter)

}