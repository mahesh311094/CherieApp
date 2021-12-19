package com.ar7lab.cherieapp.model

import java.io.Serializable

data class CardPayments(
    val data: Data,
    val message: String,
    val status: String
) : Serializable