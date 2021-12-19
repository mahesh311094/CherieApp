package com.ar7lab.cherieapp.network.repositories

import com.ar7lab.cherieapp.network.request.FollowArtistRequest
import com.ar7lab.cherieapp.network.request.GetArtistsRequest
import com.ar7lab.cherieapp.network.service.ArtistService

class ArtistRepositoryImpl(private val service: ArtistService) : ArtistRepository {
    override suspend fun getArtists(page: Int, limit: Int) =
        service.getArtists(GetArtistsRequest(page, limit))

    override suspend fun getStoreTopTraditionalArtists() =
        service.getArtists(GetArtistsRequest(1, 3))

    override suspend fun getStoreTopNFTArtists() =
        service.getArtists(GetArtistsRequest(1, 3))

    override suspend fun followArtist(artistId: String, userId: String) =
        service.followArtist(artistId, FollowArtistRequest(userId))

    override suspend fun unFollowArtist(artistId: String, userId: String) =
        service.unFollowArtist(artistId)

    override suspend fun getArtistDetails(artistID: String) =
        service.getArtistDetails(artistID)

    override suspend fun likeArtist(artId: String) = service.likeArtist(artId)

    override suspend fun unLikeArtist(artId: String) = service.unLikeArtist(artId)

}