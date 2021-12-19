package com.ar7lab.cherieapp.network.repositories

import com.ar7lab.cherieapp.network.response.*

interface PaymentRepository {
    suspend fun addCard(
        cardNumber: String,
        cardExpMonth: String,
        cardExpYear: String,
        cardCVC: String
    ): AddCardResponse

    suspend fun addBank(
        idOfUser: String,
        country: String,
        bankName: String,
        accountName: String,
        accountNumber: String,
        defaultAccount: Boolean
    ): AddCardResponse

    suspend fun bankDeposit(
        bankId: String,
        paymentMethodType: String,
        transactionType: Int,
        amount: Float,
        status: String,
        createdBy: String,
        currency: String
    ): AddCardResponse

    suspend fun cardDeposit(
        amount: Int,
        currency: String,
        paymentMethodId: String,
        description: String
    ): AddCardResponse

    suspend fun getCards(): GetCardsResponse
    suspend fun getBanksDetailList(): GetBanksResponse
    suspend fun paymentThoughCard(artId: String, amount: String, currency: String, paymentMethodId: String, description: String,) : CreatePaymentResponse
    suspend fun deleteBank(accountNumber: List<String>): AddCardResponse
    suspend fun deleteCard(cardId: String): AddCardResponse
    suspend fun paymentThroughBank( artId: String, paymentType: String, artShares: String, price: String, salesDateId: String, currency: String, bankAccountId: String): BankPaymentResponse
    suspend fun paymentThroughCherieWallet( artId: String, artShares: String, price: String, salesDateId: String, currency: String): AddCardResponse
}