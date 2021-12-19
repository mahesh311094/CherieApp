package com.ar7lab.cherieapp.network.request

import com.ar7lab.cherieapp.enums.AccountTypeEnum

class UpdateUserProfileRequest(
    var firstName: String,
    var lastName: String,
    var country: String,
)