package com.ar7lab.cherieapp.network.repositories

import com.ar7lab.cherieapp.network.request.GetAssestDataRequest
import com.ar7lab.cherieapp.network.request.MyCollectionListRequest
import com.ar7lab.cherieapp.network.service.UserService

class UserRepositoryImpl(private val service: UserService) : UserRepository {

    override suspend fun getMyCollection(page: Int, limit: Int) = service.getMyCollection(
        MyCollectionListRequest(page, limit)
    )

    override suspend fun getMyAssestDataById(artId :String ,page: Int, limit: Int) =
        service.getMyAssestDataById(artId, GetAssestDataRequest(page,limit))
}