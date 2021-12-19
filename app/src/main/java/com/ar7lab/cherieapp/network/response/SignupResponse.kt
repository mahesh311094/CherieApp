package com.ar7lab.cherieapp.network.response

import com.ar7lab.cherieapp.model.Signup

class SignupResponse : BaseResponse<SignupResponse.Data>() {
    class Data(
        var token: String,
        var user: Signup
    )
}