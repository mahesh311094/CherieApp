package com.ar7lab.cherieapp.network.repositories

import com.ar7lab.cherieapp.network.response.NotificationMarkAllAsReadResponse
import com.ar7lab.cherieapp.network.response.NotificationResponse

interface NotificationRepository {
    suspend fun getNotifications(page: Int, limit: Int): NotificationResponse
    suspend fun setNotificationMakeAllAsRead(): NotificationMarkAllAsReadResponse
}