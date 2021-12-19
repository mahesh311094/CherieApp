package com.ar7lab.cherieapp.pushnotification

class UserUpdateNotificationChannel : MyNotificationChannel() {
    override val channelId: String
        get() = "channel_user_update"
    override val channelName: String
        get() = "User update"
    override val channelDescription: String
        get() = "User update notifications"
    override val channelGroupKey: String
        get() = "GROUP_USER"
    override val channelSummaryId: Int
        get() = 111
}