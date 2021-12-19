package com.ar7lab.cherieapp.network.request

class NotificationSettingsUpdateRequest(
    var settings: Settings? = null
) {
    class Settings(
        var notification: Notification? = null
    )
    class Notification(
        var isNotice: Boolean,
        var isNews: Boolean,
        var isWork: Boolean,
        var isLikeFollowComment: Boolean,
        var isAssetChange: Boolean
    )
}