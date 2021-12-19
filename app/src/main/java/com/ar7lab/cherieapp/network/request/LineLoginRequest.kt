package com.ar7lab.cherieapp.network.request

import com.ar7lab.cherieapp.enums.AccountTypeEnum
import com.ar7lab.cherieapp.enums.SocialMediaTypeEnum

class LineLoginRequest(
    var socialMediaId: String,
    var email: String,
    var firstName: String,
    var deviceToken: String,
    var socialMediaType: SocialMediaTypeEnum = SocialMediaTypeEnum.LINE,
    var accountType:AccountTypeEnum=AccountTypeEnum.PERSONAL
)