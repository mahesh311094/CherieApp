package com.ar7lab.cherieapp.network.request

import com.ar7lab.cherieapp.enums.AccountTypeEnum
import com.ar7lab.cherieapp.enums.SocialMediaTypeEnum

class GoogleLoginRequest(
    var socialMediaId: String,
    var email: String,
    var firstName: String,
    var deviceToken: String,
    var socialMediaType: SocialMediaTypeEnum = SocialMediaTypeEnum.GOOGLE,
    var accountType:AccountTypeEnum=AccountTypeEnum.PERSONAL
)