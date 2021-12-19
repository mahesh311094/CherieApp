package com.ar7lab.cherieapp.ui.payment.selectpaymentmethod

import com.ar7lab.cherieapp.model.CardDetails

interface CreatePaymentListener {
    fun onClick(art: CardDetails)
}