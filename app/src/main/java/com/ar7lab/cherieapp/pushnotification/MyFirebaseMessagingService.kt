package com.ar7lab.cherieapp.pushnotification

import android.annotation.SuppressLint
import android.app.Notification
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.ar7lab.cherieapp.R
import com.ar7lab.cherieapp.di.SharePreferencesManager
import com.ar7lab.cherieapp.enums.NotificationTypeEnum
import com.ar7lab.cherieapp.ui.splash.SplashActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject
import kotlin.random.Random

@AndroidEntryPoint
class MyFirebaseMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var sharePreferencesManager: SharePreferencesManager

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Timber.d("Device token: $token")
        sharePreferencesManager.deviceToken = token
    }

    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    // [START receive_message]
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // [START_EXCLUDE]
        // There are two types of messages data messages and notification messages. Data messages are handled
        // here in onMessageReceived whether the app is in the foreground or background. Data messages are the type
        // traditionally used with GCM. Notification messages are only received here in onMessageReceived when the app
        // is in the foreground. When the app is in the background an automatically generated notification is displayed.
        // When the user taps on the notification they are returned to the app. Messages containing both notification
        // and data payloads are treated as notification messages. The Firebase console always sends notification
        // messages. For more see: https://firebase.google.com/docs/cloud-messaging/concept-options
        // [END_EXCLUDE]

        // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Timber.d("From: ${remoteMessage.from}")

        // Check if message contains a data payload.
        // as of now, the BE does not send any data payload, ignore this
        if (remoteMessage.data.isNotEmpty()) {
            Timber.d("Message data payload: ${remoteMessage.data}")

            val title = remoteMessage.data["title"] ?: ""
            val body = remoteMessage.data["body"] ?: ""
            val type = remoteMessage.data["type"] ?: NotificationTypeEnum.CREATE_ACCOUNT.value
            sendNotification(title, body, type)
        }

        // Check if message contains a notification payload.
        remoteMessage.notification?.let {
            Timber.d("Message Notification: ${it.title} ${it.body}")
            val title = it.title ?: ""
            val body = it.body ?: ""
            sendNotification(
                title, body, NotificationTypeEnum.CREATE_ACCOUNT.value
            )
        }

        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
    }
    // [END receive_message]


    /**
     * Create and show a simple notification containing the received FCM message.
     *
     * @param messageBody FCM message body received.
     */
    private fun sendNotification(messageTitle: String, messageBody: String, type: String) {
        var notificationChannelId: String
        var notifyIntent: Intent
        var summaryText: String
        var groupKey: String
        var summaryId: Int
        var notificationId: Int

        notificationId = Random.nextInt()
        val defaultChannel = DefaultNotificationChannel()
        summaryText = defaultChannel.channelName
        groupKey = defaultChannel.channelGroupKey
        summaryId = defaultChannel.channelSummaryId
        notificationChannelId =
            createNotificationChannel(this, defaultChannel)

        notifyIntent = Intent(this, SplashActivity::class.java)
        notifyIntent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP

        showNotification(
            notificationId,
            notificationChannelId,
            messageBody,
            messageTitle,
            summaryText,
            notifyIntent,
            groupKey,
            summaryId
        )

        /*when (type) {
            NotificationTypeEnum.CREATE_ACCOUNT.value -> {

            }
            NotificationTypeEnum.CREATE_PROFILE.value -> {

            }
            NotificationTypeEnum.UPDATE_PROFILE.value -> {

            }
            NotificationTypeEnum.FOLLOW.value -> {

            }
            NotificationTypeEnum.ADD_ART.value -> {

            }
            NotificationTypeEnum.LIKE.value -> {

            }
            NotificationTypeEnum.COMMENT.value -> {

            }
        }*/


    }

    @SuppressLint("InlinedApi")
    private fun showNotification(
        notificationId: Int,
        notificationChannelId: String, body: String?, title: String?,
        summaryText: String, notifyIntent: Intent, groupKey: String, summaryId: Int
    ) {
        val bigTextStyle = NotificationCompat.BigTextStyle()
            .bigText(body)
            .setBigContentTitle(title)
            .setSummaryText(summaryText)

        val notifyPendingIntent = PendingIntent.getActivity(
            this,
            0,
            notifyIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notificationCompatBuilder = NotificationCompat.Builder(
            applicationContext, notificationChannelId
        )

        val notification = notificationCompatBuilder
            .setStyle(bigTextStyle)
            .setContentTitle(title)
            .setContentText(body)
            .setSmallIcon(R.drawable.ic_stat_ic_notification)
            //            .setLargeIcon(
            //                BitmapFactory.decodeResource(
            //                    resources,
            //                    R.drawable.ic_notification
            //                )
            //            )
            .setContentIntent(notifyPendingIntent)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setColor(ContextCompat.getColor(applicationContext, R.color.dark_blue))

            //             .setGroupSummary(true)
            .setGroup(groupKey)
            .setCategory(Notification.CATEGORY_MESSAGE)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .setAutoCancel(true)
            .build()

        val summaryNotification = NotificationCompat.Builder(this, notificationChannelId)
            .setContentTitle(title)
            .setContentText(body)
            .setSmallIcon(R.drawable.ic_stat_ic_notification)
            .setGroup(groupKey)
            .setGroupSummary(true)
            .setAutoCancel(true)
            .build()


        NotificationManagerCompat.from(this).apply {
            notify(notificationId, notification)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                notify(summaryId, summaryNotification)
            }
        }
    }
}