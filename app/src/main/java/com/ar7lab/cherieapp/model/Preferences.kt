package com.ar7lab.cherieapp.model

/**
 * Payment transaction details serializable class
 * @property title = slider title
 * @property description = slider description
 * @property icon = slider icon from drawable folder
 * */
data class Preferences (
    val title: String,
    val description: String,
    val icon: Int
    )
