package com.ar7lab.cherieapp.network.response

import com.ar7lab.cherieapp.model.User

class GetUserProfileResponse : BaseResponse<GetUserProfileResponse.Data>() {
    class Data(
        var user: User
    )
}