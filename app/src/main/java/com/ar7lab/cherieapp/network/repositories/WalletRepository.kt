package com.ar7lab.cherieapp.network.repositories

import com.ar7lab.cherieapp.network.response.*

interface WalletRepository {
    suspend fun getPurchaseDetail(page: Int, limit: Int): GetWalletPurchaseDetailResponse
    suspend fun getUserBalance(): GetWalletBalanceResponse
    suspend fun createPayment(artId: String, cardNumber: String, cardExpMonth: String, cardExpYear: String, cardCVC: String, amount: String, currency: String, description: String, paymentMethodId: String) : CreatePaymentResponse
}