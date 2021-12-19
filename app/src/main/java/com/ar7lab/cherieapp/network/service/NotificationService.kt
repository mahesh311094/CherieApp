package com.ar7lab.cherieapp.network.service

import com.ar7lab.cherieapp.network.request.GetNotiicationRequest
import com.ar7lab.cherieapp.network.response.GetArtDetailsResponse
import com.ar7lab.cherieapp.network.response.NotificationMarkAllAsReadResponse
import com.ar7lab.cherieapp.network.response.NotificationResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface NotificationService {

    @POST("notification/list")
    suspend fun getNotification(@Body getNewsRequest: GetNotiicationRequest): NotificationResponse

    @POST("notification/read/all")
    suspend fun setNotificationMarkAllAsRead(): NotificationMarkAllAsReadResponse

}