package com.ar7lab.cherieapp.model

import java.io.Serializable

/**
 * Card details serializable class
 * @property _id = payment id
 * @property customer = customer name
 * @property card = card details object
 * @property isSelected = check which card is selected
 * */
data class CardDetails(
    var id: String = "",
    var customer: String = "",
    var card: Card = Card(),
    var isSelected: Boolean = false
) : Serializable {
    /**
     * Card serializable class
     * @property brand = card brand name. Ex. VISA, MAESTRO, RuPay etc.
     * @property country = card usable country
     * @property exp_month = card expiry month
     * @property exp_year = card expiry year
     * @property fingerprint = card finger print
     * @property funding = funding
     * @property last4 = last 4 digit of your card
     * */
    class Card : Serializable {
        var brand: String = ""
        var country: String = ""
        var exp_month: Int = 0
        var exp_year: Int = 0
        var fingerprint: String = ""
        var funding: String = ""
        var last4: String = ""
    }
}