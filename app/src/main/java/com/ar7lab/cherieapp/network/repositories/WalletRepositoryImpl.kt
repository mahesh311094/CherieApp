package com.ar7lab.cherieapp.network.repositories

import com.ar7lab.cherieapp.network.request.*
import com.ar7lab.cherieapp.network.response.CreatePaymentResponse
import com.ar7lab.cherieapp.network.service.PaymentService
import com.ar7lab.cherieapp.network.service.WalletService
import com.ar7lab.cherieapp.utils.enccriptData
import com.google.gson.Gson

class WalletRepositoryImpl(private val service: WalletService) : WalletRepository {

    private val gson = Gson()

    override suspend fun getPurchaseDetail(
        page: Int, limit: Int
    )= service.getPurchaseDetail(
        GetWalletPurchaseDetailRequest(page,limit)
    )

    override suspend fun getUserBalance() = service.getUserBalance()
   override suspend fun createPayment(
       artId: String,
       cardNumber: String,
       cardExpMonth: String,
       cardExpYear: String,
       cardCVC: String,
       amount: String,
       currency: String,
       description: String,
       paymentMethodId: String
   ): CreatePaymentResponse = service.createPaymentRequest(
       artId,
       CreatePaymentRequestEncryption(enccriptData(gson.toJson(CreatePaymentRequest(cardNumber,cardExpMonth,cardExpYear,cardCVC,amount,currency,description,paymentMethodId)))!!)
   )
}