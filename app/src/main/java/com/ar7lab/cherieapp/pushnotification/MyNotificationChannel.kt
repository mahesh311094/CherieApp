package com.ar7lab.cherieapp.pushnotification

abstract class MyNotificationChannel {
    abstract val channelId: String
    abstract val channelName: String
    abstract val channelDescription: String
    abstract val channelGroupKey: String
    abstract val channelSummaryId: Int
}