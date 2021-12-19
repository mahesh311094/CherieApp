package com.ar7lab.cherieapp.network.repositories

import com.ar7lab.cherieapp.network.response.MyAssestResponse
import com.ar7lab.cherieapp.network.response.MyCollectionResponse

interface UserRepository {

    suspend fun getMyCollection(page: Int, limit: Int): MyCollectionResponse

    suspend fun getMyAssestDataById(artId :String ,page: Int,limit: Int): MyAssestResponse

}