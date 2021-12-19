package com.ar7lab.cherieapp.network.repositories

import com.ar7lab.cherieapp.enums.AccountTypeEnum
import com.ar7lab.cherieapp.network.response.*

interface AuthRepository {

    suspend fun login(email: String, password: String, deviceToken: String): LoginResponse

    suspend fun resetEmailPassword(email: String, newPassword: String): ResetPasswordResponse

    suspend fun resetMobilePassword(mobileNumber: String, newPassword: String): ResetPasswordResponse

    suspend fun loginWithNumber(contactNo: String, password: String, deviceToken: String): LoginResponse

    suspend fun loginWithGoogle(
        googleId: String,
        googleEmail: String,
        firstName: String,
        deviceToken: String
    ): LoginResponse

    suspend fun loginWithFacebook(
        facebookId: String,
        facebookEmail: String,
        deviceToken: String
    ): LoginResponse

    suspend fun loginWithKakao(
        kakaoUserId: String,
        kakaoEmail: String,
        firstName: String,
        deviceToken: String
    ): LoginResponse

    suspend fun loginWithLine(
        kakaoUserId: String,
        kakaoEmail: String,
        firstName: String,
        deviceToken: String
    ): LoginResponse


    suspend fun personalSignup(
        userName: String,
        email: String,
        mobileNumber: String,
        password: String,
        accountType: String,
        firstName: String,
        lastName: String,
        country: String
    ): PersonalSignupResponse

    suspend fun companySignup(
        email: String,
        mobileNumber: String,
        password: String,
        accountType: String,
        companyName: String,
        country: String,
        firstName: String, lastName: String, userName: String
    ): CompanySignupResponse

    suspend fun forgotPassword(email: String): ForgotPasswordResponse

    suspend fun getUserProfileDetails(): GetUserProfileResponse

    suspend fun updateUser(
        firstName: String,
        lastName: String,
        contactNo: String,
        accountType: AccountTypeEnum,
        companyName: String,
        country: String,
        profileImage: String,
        coverImage: String,
    ): LoginResponse

    suspend fun editPersonalUserProfile(
        userName: String,
        accountType: String,
        firstName: String,
        lastName: String,
        country: String,
        contactNo: String
    ): EditUserProfileResponse

    suspend fun editCompanyUserProfile(
        userName: String,
        accountType: String,
        firstName: String,
        lastName: String,
        companyName: String,
        country: String,
        contactNo: String
    ): EditUserProfileResponse

    suspend fun changePassword(
        oldPassword: String,
        newPassword: String
    ): ChangePasswordResponse

    suspend fun editOverviewUserProfile(
        overview: String
    ): EditUserProfileResponse

    suspend fun updateProfilePicture(filePath: String): UpdateProfilePictureResponse

    suspend fun updateCoverPicture(filePath: String): UpdateProfilePictureResponse

    suspend fun resendVerification(email: String): AddCardResponse

    suspend fun notificationSettingsUpdate(
        isNotice: Boolean, isNews: Boolean, isWork: Boolean,
        isLikeFollowComment: Boolean, isAssetChange: Boolean
    ): NotificationSettingsUpdateResponse

    suspend fun newSignUp(email: String): PersonalSignupResponse

    suspend fun newSignUpVerifyEmailCode(
        email: String,
        password: String,
        signUpType: String,
        verificationCode: Int
    ): SignupResponse

    suspend fun newSignUpVerifyMobile(
        contactNo: String,
        password: String,
        signUpType: String
    ): SignupResponse

    suspend fun personalKYC(
        country: String,
        name: String,
        birthday: String,
        address: String,
        postalCode: String,
        city: String
    ): PersonalKYCResponse

    suspend fun verifyForgetOTP(email: String, code: String): CommonResponse

    suspend fun identityUpload(filePath: String,filePath2:String,type:String): PersonalKYCResponse

    suspend fun faceUpload(filePath: String): PersonalKYCResponse

    suspend fun getKYCDetail(): GetKYCResponse
}