package com.ar7lab.cherieapp.network.response

open class BaseResponse<T> {
    var status: String = ""
    var message: String = ""
    var data: T? = null
}