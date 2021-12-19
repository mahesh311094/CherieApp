package com.ar7lab.cherieapp.network.repositories

import com.ar7lab.cherieapp.network.response.FollowArtistResponse
import com.ar7lab.cherieapp.network.response.GetArtistDetailsResponse
import com.ar7lab.cherieapp.network.response.GetArtistsResponse

interface ArtistRepository {

    suspend fun getArtists(page: Int, limit: Int): GetArtistsResponse
    suspend fun getStoreTopTraditionalArtists(): GetArtistsResponse
    suspend fun getStoreTopNFTArtists(): GetArtistsResponse
    suspend fun followArtist(artistId: String, userId: String): FollowArtistResponse
    suspend fun unFollowArtist(artistId: String, userId: String): FollowArtistResponse
    suspend fun getArtistDetails(artistID: String): GetArtistDetailsResponse
    suspend fun likeArtist(artistId: String): Any
    suspend fun unLikeArtist(artistId: String): Any
}