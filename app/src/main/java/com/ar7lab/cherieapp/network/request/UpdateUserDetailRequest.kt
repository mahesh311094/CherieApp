package com.ar7lab.cherieapp.network.request

import com.ar7lab.cherieapp.enums.AccountTypeEnum

class UpdateUserDetailRequest(
    var firstName: String,
    var lastName: String,
    var contactNo: String,
    var accountType: AccountTypeEnum,
    var companyName: String,
    var country: String,
    var profileImage: String = "",
    var coverImage: String = "",
)