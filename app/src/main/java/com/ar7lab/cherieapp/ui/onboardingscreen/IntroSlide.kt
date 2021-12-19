package com.ar7lab.cherieapp.ui.onboardingscreen

/**
 * Payment transaction details serializable class
 * @property title = slider title
 * @property description = slider description
 * @property subDescription = slider sub description
 * @property icon = slider icon from drawable folder
 * */
data class IntroSlide (
    val title: String,
    val description: String,
    val subDescription: String,
    val icon: Int
)
