package com.ar7lab.cherieapp.model

import java.io.Serializable

data class SignupSettings(
    var notification : Notification = Notification(),
    var language : String = "",
    var currency : String = ""
) : Serializable