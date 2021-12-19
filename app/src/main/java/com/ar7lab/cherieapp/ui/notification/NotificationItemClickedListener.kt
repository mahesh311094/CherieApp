package com.ar7lab.cherieapp.ui.notification

import com.ar7lab.cherieapp.model.Artist
import com.ar7lab.cherieapp.model.Notifications

/*
* Listener for Follow Artist
* */
interface NotificationItemClickedListener {
    //Click event for follow artist click
    fun onNotificationItemClicked(notification: Notifications)
}