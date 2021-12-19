package com.ar7lab.cherieapp.network.service

import com.ar7lab.cherieapp.network.request.*
import com.ar7lab.cherieapp.network.response.*
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface WalletService {
    @POST("user/wallet/fiat/transaction/list")
    suspend fun getPurchaseDetail(@Body getWalletPurchaseDetailRequest: GetWalletPurchaseDetailRequest): GetWalletPurchaseDetailResponse

    @GET("user/wallet/fetch/balance")
    suspend fun getUserBalance(): GetWalletBalanceResponse

    @POST("payment/createPayment/{artId}")
    suspend fun createPaymentRequest(
        @Path("artId") artId: String,
        @Body createPaymentRequestEncryption: CreatePaymentRequestEncryption
    ): CreatePaymentResponse
}