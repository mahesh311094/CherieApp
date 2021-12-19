package com.ar7lab.cherieapp.network.repositories

import com.ar7lab.cherieapp.network.request.GetArtCommentsRequest
import com.ar7lab.cherieapp.network.request.GetNotiicationRequest
import com.ar7lab.cherieapp.network.response.NotificationMarkAllAsReadResponse
import com.ar7lab.cherieapp.network.response.NotificationResponse
import com.ar7lab.cherieapp.network.service.NotificationService

class NotificationRepositoryImpl(private val service: NotificationService) : NotificationRepository {

    override suspend fun getNotifications(page: Int, limit: Int): NotificationResponse {
        return service.getNotification(GetNotiicationRequest(page, limit))
    }

    override suspend fun setNotificationMakeAllAsRead() =
        service.setNotificationMarkAllAsRead()

}