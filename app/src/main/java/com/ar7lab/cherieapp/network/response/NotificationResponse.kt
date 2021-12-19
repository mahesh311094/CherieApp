package com.ar7lab.cherieapp.network.response

import com.ar7lab.cherieapp.model.NewsCategory
import com.ar7lab.cherieapp.model.Notifications

class NotificationResponse(): BaseResponse<NotificationResponse.Data>() {

    class Data(
        var notificationDetailObj: List<Notifications> = emptyList()
    )

}
