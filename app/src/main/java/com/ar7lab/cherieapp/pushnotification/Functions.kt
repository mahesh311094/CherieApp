package com.ar7lab.cherieapp.pushnotification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat

fun createNotificationChannel(
    context: Context, notificationChannelData: MyNotificationChannel
): String {

    // NotificationChannels are required for Notifications on O (API 26) and above.
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

        // The id of the channel.
        val channelId = notificationChannelData.channelId

        // The user-visible name of the channel.
        val channelName = notificationChannelData.channelName
        // The user-visible description of the channel.
        val channelDescription = notificationChannelData.channelDescription
        val channelImportance = NotificationManager.IMPORTANCE_HIGH
        val channelEnableVibrate = true
        val channelLockScreenVisibility = NotificationCompat.VISIBILITY_PUBLIC

        // Initializes NotificationChannel.
        val notificationChannel = NotificationChannel(channelId, channelName, channelImportance)
        notificationChannel.description = channelDescription
        notificationChannel.enableVibration(channelEnableVibrate)
        notificationChannel.lockscreenVisibility = channelLockScreenVisibility

        // Adds NotificationChannel to system. Attempting to create an existing notification
        // channel with its original values performs no operation, so it's safe to perform the
        // below sequence.
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(notificationChannel)

        return channelId
    } else {
        // Returns null for pre-O (26) devices.
        return ""
    }
}