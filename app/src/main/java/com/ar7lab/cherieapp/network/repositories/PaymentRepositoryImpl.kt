package com.ar7lab.cherieapp.network.repositories

import com.ar7lab.cherieapp.network.request.*
import com.ar7lab.cherieapp.network.response.AddCardResponse
import com.ar7lab.cherieapp.network.response.BankPaymentResponse
import com.ar7lab.cherieapp.network.response.CreatePaymentResponse
import com.ar7lab.cherieapp.network.service.PaymentService
import com.ar7lab.cherieapp.utils.enccriptData
import com.google.gson.Gson

class PaymentRepositoryImpl(private val service: PaymentService) : PaymentRepository {

    private val gson = Gson()

    override suspend fun addCard(
        cardNumber: String,
        cardExpMonth: String,
        cardExpYear: String,
        cardCVC: String
    ) = service.addCardRequest(
        AddCardRequestEncryption(
            enccriptData(
                gson.toJson(
                    AddCardRequest(
                        cardNumber,
                        cardExpMonth,
                        cardExpYear,
                        cardCVC
                    )
                )
            )!!
        )
    )

    override suspend fun addBank(
        idOfUser: String,
        country: String,
        bankName: String,
        accountName: String,
        accountNumber: String,
        defaultAccount: Boolean
    ) = service.addBankRequest(
        AddBankRequest(idOfUser, country, bankName, accountName, accountNumber, defaultAccount)
    )

    override suspend fun bankDeposit(
        bankId: String,
        paymentMethodType: String,
        transactionType: Int,
        amount: Float,
        status: String,
        createdBy: String,
        currency: String
    ) = service.bankDepositRequest(
        BankDepositRequest(
            bankId,
            paymentMethodType,
            transactionType,
            amount,
            status,
            createdBy,
            currency
        )
    )

    override suspend fun cardDeposit(
        amount: Int,
        currency: String,
        paymentMethodId: String,
        description: String
    ) = service.bankDepositRequest(
        CardDepositRequest(
            amount,
            currency,
            paymentMethodId,
            description
        )
    )

    override suspend fun getCards() = service.getCardsRequest()

    override suspend fun getBanksDetailList() = service.getBanksDetailList()
   /* override suspend fun createPayment(
        artId: String,
        amount: String,
        currency: String,
        description: String,
        paymentMethodId: String
    )= service.createPaymentRequest(
        artId,
        CreatePaymentRequest(amount,currency,description,paymentMethodId)
    )*/
   override suspend fun paymentThoughCard(
       artId: String,
       amount: String,
       currency: String,
       paymentMethodId: String,
       description: String,
   ): CreatePaymentResponse = service.paymentThoughCard(artId,
       CreatePaymentRequestEncryption(enccriptData(gson.toJson(CardPaymentRequest(artId, amount, currency, paymentMethodId, description)))!!)
   )

    override suspend fun deleteBank(accountNumber: List<String>):
       AddCardResponse = service.deleteBankRequest(DeleteBankRequest(accountNumber))

    override suspend fun deleteCard(
        cardId: String,
    ): AddCardResponse = service.deleteCardRequest(DeleteCardRequestEncryption(enccriptData(gson.toJson(DeleteCardRequest(cardId)))!!))

    override suspend fun paymentThroughBank(
        artId: String,
        paymentType: String,
        artShares: String,
        price: String,
        salesDateId: String,
        currency: String,
        bankAccountId: String,
    ): BankPaymentResponse  = service.paymentThoughBank(BankPaymentRequest(artId, paymentType, artShares, price, salesDateId, currency, bankAccountId))

    override suspend fun paymentThroughCherieWallet(
        artId: String,
        artShares: String,
        price: String,
        salesDateId: String,
        currency: String,
    ): AddCardResponse = service.paymentThoughCherieWallet(artId, CherieWalletPaymentRequest(artShares, price, salesDateId, currency) )
}