package com.ar7lab.cherieapp.model

import java.io.Serializable

data class Settings(
    var notification : Notification = Notification()
) : Serializable