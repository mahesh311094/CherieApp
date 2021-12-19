package com.ar7lab.cherieapp.model

import androidx.databinding.BaseObservable
import androidx.databinding.Bindable

class AddCard : BaseObservable() {
    @Bindable
    var cardNumber: String = ""
    var cardExpMonth: String = ""
    var cardExpYear: String = ""
    var cardCVC: String = ""
    var cardName: String = ""
    var cardExpMonthYear: String = ""
}

