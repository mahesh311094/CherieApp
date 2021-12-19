package com.ar7lab.cherieapp.network.service

import com.ar7lab.cherieapp.network.request.*
import com.ar7lab.cherieapp.network.response.*
import retrofit2.http.*

interface ArtService {
//    @POST("art/getArts")
//    suspend fun getArts(@Body getArtsRequest: GetArtsRequest): GetArtsResponse

    @POST("art/search/getArts")
    suspend fun searchArt(@Body getArtsRequest: GetArtsRequest): GetArtsResponse

    @POST("art/search/getArtsForStore")
    suspend fun searchArtForStore(@Body getArtsForStoreRequest: GetArtsForStoreRequest): GetArtsForStoreResponse

    @POST("art/like/{artId}")
    suspend fun likeArt(@Path("artId") artId: String): Any

    @DELETE("art/like/{artId}")
    suspend fun unLikeArt(@Path("artId") artId: String): Any

    @POST("/art/comment/{artId}/detail")
    suspend fun getArtComments(
        @Path("artId") artId: String,
        @Body getArtCommentsRequest: GetArtCommentsRequest
    ): GetArtCommentsResponse

    @POST("art/comment/{artId}")
    suspend fun sendArtComment(
        @Path("artId") artId: String,
        @Body sendArtCommentRequest: SendArtCommentRequest
    ): SendArtCommentResponse

    @GET("art/detail/{id}")
    suspend fun getArtDetails(@Path("id") artId: String): GetArtDetailsResponse

    @POST("art/getArtsByArtistId")
    suspend fun getArtsByArtistId(@Body getArtsByArtistIdRequest: GetArtsByArtistIdRequest): GetArtsByArtistIdResponse

    @POST("art/getArtsByArtistId")
    suspend fun getArtsByArtistIdTraditional(@Body getArtsByArtistIdRequest: GetArtsByArtistIdTraditionalRequest): GetArtsByArtistIdResponse

    @POST("art/getArtsByArtistId")
    suspend fun getArtsByArtistIdNft(@Body getArtsByArtistIdRequest: GetArtsByArtistIdNftRequest): GetArtsByArtistIdResponse

    @POST("art/like/{artId}/detail")
    suspend fun getArtLikes(
        @Path("artId") artId: String,
        @Body getArtLikesRequest: GetArtLikesRequest
    ): GetArtLikesResponse

    @POST("art/transaction-history/{artId}")
    suspend fun getArtTransactionDetails(@Path("artId") artId: String,@Body getArtTransactionDetailsRequest: GetArtTransactionDetailsRequest): GetArtTransactionDetailsResponse

    @POST("art/notify/{artId}")
    suspend fun addNotify(@Path("artId") artId: String): Any

    @DELETE("art/notify/{artId}")
    suspend fun deleteNotify(@Path("artId") artId: String): Any

    @POST("user/art/like/details")
    suspend fun storeArtLikeDetails(@Body getStoreLikeDetailsRequest: GetStoreLikeDetailsRequest): GetStoreLikeDetailsResponse
}