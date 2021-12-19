package com.ar7lab.cherieapp.network.service

import com.ar7lab.cherieapp.network.request.FollowArtistRequest
import com.ar7lab.cherieapp.network.request.GetArtistsRequest
import com.ar7lab.cherieapp.network.response.FollowArtistResponse
import com.ar7lab.cherieapp.network.response.GetArtistDetailsResponse
import com.ar7lab.cherieapp.network.response.GetArtistsResponse
import retrofit2.http.*

interface ArtistService {

    @POST("artist/search/getArtists")
    suspend fun getArtists(@Body getArtistsRequest: GetArtistsRequest): GetArtistsResponse

    @POST("artist/follower/{artistId}")
    suspend fun followArtist(
        @Path("artistId") artistId: String,
        @Body followArtistRequest: FollowArtistRequest
    ): FollowArtistResponse

    @DELETE("artist/follower/{artistId}")
    suspend fun unFollowArtist(
        @Path("artistId") artistId: String,

    ): FollowArtistResponse

    @GET("artist/detail/{id}")
    suspend fun getArtistDetails(@Path("id") artistId: String): GetArtistDetailsResponse

    @POST("artist/like/{artistId}")
    suspend fun likeArtist(@Path("artistId") artId: String): Any

    @DELETE("artist/like/{artistId}")
    suspend fun unLikeArtist(@Path("artistId") artId: String): Any
}