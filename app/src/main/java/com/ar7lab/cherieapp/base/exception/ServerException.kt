package com.ar7lab.cherieapp.base.exception

import com.squareup.moshi.JsonClass

/**
 * Server Exception for handling the json error response in header
 */
@JsonClass(generateAdapter = true)
class ServerException(var status: String, var message: String) {
    var data: Data? = null

    @JsonClass(generateAdapter = true)
    class Data {
        var errors: List<Error> = emptyList()
        var sessionExpired:Boolean=false
    }

    @JsonClass(generateAdapter = true)
    class Error {
        var msg: String = ""
    }
}