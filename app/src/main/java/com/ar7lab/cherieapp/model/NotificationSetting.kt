package com.ar7lab.cherieapp.model

/**
 * Payment transaction details serializable class
 * @property title = notification setting title
 * @property description = notification setting description
 * @property notification_on_off = check notification on or off
 * */
data class NotificationSetting (
    val title: String,
    val description: String,
    val notification_on_off: Boolean
    )
