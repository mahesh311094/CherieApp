package com.ar7lab.cherieapp.network.service

import com.ar7lab.cherieapp.network.request.*
import com.ar7lab.cherieapp.network.response.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.*

interface AuthService {

    @POST("user/login")
    suspend fun login(@Body loginRequest: LoginRequest): LoginResponse

    @POST("user/login")
    suspend fun loginWithNumber(@Body loginMobileRequest: LoginMobileRequest): LoginResponse

    @POST("user/login")
    suspend fun loginWithGoogle(@Body googleLoginRequest: GoogleLoginRequest): LoginResponse

    @POST("user/login")
    suspend fun loginWithFacebook(@Body facebookLoginRequest: FacebookLoginRequest): LoginResponse

    @POST("user/login")
    suspend fun loginWithKakao(@Body kakaoLoginRequest: KakaoLoginRequest): LoginResponse

    @POST("user/login")
    suspend fun loginWithLine(@Body lineLoginRequest: LineLoginRequest): LoginResponse

    @POST("user/personalsignup")
    suspend fun personalSignup(@Body personalSignupRequest: PersonalSignupRequest): PersonalSignupResponse

    @POST("user/companysignup")
    suspend fun companySignup(@Body companySignupRequest: CompanySignupRequest): CompanySignupResponse

    @POST("user/forgetPassword")
    suspend fun forgotPassword(@Body forgotPasswordRequest: ForgotPasswordRequest): ForgotPasswordResponse

    @GET("user/detail")
    suspend fun getUserProfileDetails(): GetUserProfileResponse

    @PUT("user/detail")
    suspend fun updateUser(@Body updateUserDetailRequest: UpdateUserDetailRequest): LoginResponse

    @PUT("user/detail")
    suspend fun editPersonalUserProfile(@Body editPersonalUserProfileRequest: EditPersonalUserProfileRequest): EditUserProfileResponse

    @PUT("user/password")
    suspend fun changePassword(@Body changePasswordRequest: ChangePasswordRequest): ChangePasswordResponse

    @PUT("user/detail")
    suspend fun editCompanyUserProfile(@Body editCompanyUserProfileRequest: EditCompanyUserProfileRequest): EditUserProfileResponse

    @PUT("user/detail")
    suspend fun editOverviewUserProfile(@Body editOverviewUserProfileRequest: EditOverviewUserProfileRequest): EditUserProfileResponse

    @POST("/user/uploadUserImages")
    @Multipart
    suspend fun updatePicture(@Part parts: ArrayList<MultipartBody.Part>): UpdateProfilePictureResponse

    @POST("user/resendVerificationLink")
    suspend fun resendVerification(@Body verificationRequest: VerificationRequest): AddCardResponse

    @PUT("user/detail")
    suspend fun notificationSettingsUpdate(@Body notificationSettingsUpdateRequest: NotificationSettingsUpdateRequest): NotificationSettingsUpdateResponse

    @POST("user/sendVerificationCode")
    suspend fun newSignUp(@Body signupRequest: SignupRequest): PersonalSignupResponse

    @POST("user/signup")
    suspend fun newSignUpVerifyEmailCode(@Body verifyEmailRequest: VerifyEmailRequest): SignupResponse

    @POST("user/signup")
    suspend fun newSignUpVerifyMobile(@Body verifyMobileRequest: VerifyMobileRequest): SignupResponse

    @POST("user/kyc/personal")
    suspend fun personalKYC(@Body personalKYCRequest: PersonalKYCRequest): PersonalKYCResponse

    @PATCH("user/kyc/document")
    @Multipart
    suspend fun identityUpload(@Part parts: ArrayList<MultipartBody.Part>?,@Part("type") type: RequestBody?): PersonalKYCResponse


    @PATCH("user/kyc/face")
    @Multipart
    suspend fun faceUpload(@Part parts: ArrayList<MultipartBody.Part>?): PersonalKYCResponse

    @GET("user/kyc")
    suspend fun getKYCDetail(): GetKYCResponse

    @POST("user/resetPassword")
    suspend fun resetEmailPassword(@Body resetPasswordRequest: ResetPasswordByEmailRequest): ResetPasswordResponse

    @POST("user/resetPassword")
    suspend fun resetMobilePassword(@Body resetPasswordRequest: ResetPasswordByMobileRequest): ResetPasswordResponse

    @GET("user/{email}/verifyEmail/{code}")
    suspend fun verifyForgetOTP(@Path("email") email: String,@Path("code") code: String): CommonResponse
}