package com.ar7lab.cherieapp.network.request

import com.ar7lab.cherieapp.enums.AccountTypeEnum
import com.ar7lab.cherieapp.enums.SocialMediaTypeEnum

class FacebookLoginRequest(
    var socialMediaId: String,
    var email: String,
    var deviceToken: String,
    var socialMediaType: SocialMediaTypeEnum = SocialMediaTypeEnum.FACEBOOK,
    var accountType:AccountTypeEnum=AccountTypeEnum.PERSONAL
)