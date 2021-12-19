package com.ar7lab.cherieapp.pushnotification

class DefaultNotificationChannel : MyNotificationChannel() {
    override val channelId: String
        get() = "channel_default"
    override val channelName: String
        get() = "Default"
    override val channelDescription: String
        get() = "Default notifications"
    override val channelGroupKey: String
        get() = "group_default"
    override val channelSummaryId: Int
        get() = 110
}