package com.ar7lab.cherieapp.network.service

import com.ar7lab.cherieapp.network.request.GetAssestDataRequest
import com.ar7lab.cherieapp.network.request.MyCollectionListRequest
import com.ar7lab.cherieapp.network.response.MyAssestResponse
import com.ar7lab.cherieapp.network.response.MyCollectionResponse
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path

interface UserService {

    @POST("user/asset/list")
    suspend fun getMyCollection(@Body getMyCollection: MyCollectionListRequest): MyCollectionResponse

    @POST("user/asset/{artId}/detail")
    suspend fun getMyAssestDataById(
        @Path("artId") artId : String,
        @Body getMyCollection: GetAssestDataRequest): MyAssestResponse

}