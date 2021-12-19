package com.ar7lab.cherieapp.network.response

import com.ar7lab.cherieapp.model.User

class LoginResponse : BaseResponse<LoginResponse.LoginData>() {
    class LoginData(
        var token: String,
        var user: User
    )
}

