package com.ar7lab.cherieapp.network.service

import com.ar7lab.cherieapp.network.request.*
import com.ar7lab.cherieapp.network.response.*
import retrofit2.http.*

interface PaymentService {
    @POST("payment/createPaymentMethod")
    suspend fun addCardRequest(@Body addCardRequest: AddCardRequestEncryption): AddCardResponse

    //add bank detail
    @POST("user/bankDetail/detail")
    suspend fun addBankRequest(@Body addCardRequest: AddBankRequest): AddCardResponse

    @POST("payment/wallet/deposit/bank")
    suspend fun bankDepositRequest(@Body addCardRequest: BankDepositRequest): AddCardResponse

    @POST("payment/wallet/deposit/card")
    suspend fun bankDepositRequest(@Body cardDepositRequest: CardDepositRequest): AddCardResponse

    //get card list
    @GET("payment/retrivePaymentMethod")
    suspend fun getCardsRequest(): GetCardsResponse

    //get bank list
    @POST("user/bankDetail/list")
    suspend fun getBanksDetailList(): GetBanksResponse



   /* @POST("payment/createPayment/{artId}")
    suspend fun createPaymentRequest(
        @Path ("artId") artId : String,
        @Body createPaymentRequest: CreatePaymentRequest): CreatePaymentResponse*/
   @POST("payment/createPayment/{artId}")
   suspend fun paymentThoughCard(
       @Path ("artId") artId : String,
       @Body createPaymentRequestEncryption: CreatePaymentRequestEncryption): CreatePaymentResponse

    @HTTP(method = "DELETE", path = "user/bankDetail/detail", hasBody = true)
    suspend fun deleteBankRequest(@Body deleteBankRequest: DeleteBankRequest): AddCardResponse

//    @HTTP(method = "DELETE", path = "payment/deletePaymentMethod", hasBody = true)
    @POST("payment/deletePaymentMethod")
    suspend fun deleteCardRequest(@Body deleteCardRequest: DeleteCardRequestEncryption): AddCardResponse

    @POST("/art/order/detail")
    suspend fun paymentThoughBank(@Body bankPaymentRequest: BankPaymentRequest): BankPaymentResponse

    @POST("payment/wallet/{artId}")
    suspend fun paymentThoughCherieWallet(
        @Path ("artId") artId : String,
        @Body cherieWalletPaymentRequest: CherieWalletPaymentRequest): AddCardResponse

}