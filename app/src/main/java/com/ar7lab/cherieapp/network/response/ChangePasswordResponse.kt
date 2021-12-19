package com.ar7lab.cherieapp.network.response

class ChangePasswordResponse (
    var status: String,
    var message: String,
    )
class ChangePassData() {
    var errors: List<ChangePasswordError>? = emptyList()
}

class ChangePasswordError() {
    var value:String?=""
    var msg:String?=""
    var param:String?=""
    var location:String?=""
}