package com.ar7lab.cherieapp.network.request

class VerifyEmailRequest(
    var email: String,
    var password: String,
    var signUpType: String,
    var verificationCode: Int
)

