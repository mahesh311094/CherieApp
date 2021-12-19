package com.ar7lab.cherieapp.model

import java.io.Serializable

data class Notification(
    var isNotice: Boolean = false,
    var isNews: Boolean = false,
    var isWork: Boolean = false,
    var isLikeFollowComment: Boolean = false,
    var isAssetChange: Boolean = false
) : Serializable
