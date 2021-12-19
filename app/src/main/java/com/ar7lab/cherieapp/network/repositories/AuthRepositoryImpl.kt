package com.ar7lab.cherieapp.network.repositories

import com.ar7lab.cherieapp.enums.AccountTypeEnum
import com.ar7lab.cherieapp.network.request.*
import com.ar7lab.cherieapp.network.response.*
import com.ar7lab.cherieapp.network.service.AuthService
import com.ar7lab.cherieapp.utils.prepareFilePart
import com.ar7lab.cherieapp.utils.prepareStringMultiPart
import okhttp3.MultipartBody

class AuthRepositoryImpl(private val service: AuthService) : AuthRepository {
    override suspend fun login(email: String, password: String, deviceToken: String) =
        service.login(LoginRequest(email, password, deviceToken))

    override suspend fun resetEmailPassword(email: String, newPassword: String) =
        service.resetEmailPassword((ResetPasswordByEmailRequest(email,newPassword)))

    override suspend fun resetMobilePassword(mobileNumber: String, newPassword: String) =
        service.resetMobilePassword((ResetPasswordByMobileRequest(mobileNumber,newPassword)))

    override suspend fun loginWithNumber(contactNo: String, password: String, deviceToken: String) =
        service.loginWithNumber(LoginMobileRequest(contactNo, password, deviceToken))

    override suspend fun loginWithGoogle(
        googleId: String, googleEmail: String, firstName: String, deviceToken: String
    ) =
        service.loginWithGoogle(GoogleLoginRequest(googleId, googleEmail, firstName, deviceToken))

    override suspend fun loginWithKakao(
        kakaoUserId: String,
        kakaoEmail: String,
        firstName: String,
        deviceToken: String
    ) = service.loginWithKakao(KakaoLoginRequest(kakaoUserId, kakaoEmail, firstName, deviceToken))

    override suspend fun loginWithLine(
        lineUserId: String,
        lineEmail: String,
        firstName: String,
        deviceToken: String
    ) = service.loginWithLine(LineLoginRequest(lineUserId, lineEmail, firstName, deviceToken))

    override suspend fun loginWithFacebook(
        facebookId: String, facebookEmail: String, deviceToken: String
    ) = service.loginWithFacebook(FacebookLoginRequest(facebookId, facebookEmail, deviceToken))

    override suspend fun personalSignup(
        userName: String,
        email: String,
        mobileNumber: String,
        password: String,
        accountType: String,
        firstName: String,
        lastName: String,
        country: String
    ) = service.personalSignup(
        PersonalSignupRequest(
            email,
            password,
            accountType,
            userName,
            mobileNumber,
            firstName,
            lastName,
            country
        )
    )

    override suspend fun companySignup(
        email: String,
        mobileNumber: String,
        password: String,
        accountType: String,
        companyName: String,
        country: String,
        firstName: String, lastName: String,
        userName: String
    ) = service.companySignup(
        CompanySignupRequest(
            email,
            password,
            accountType,
            companyName,
            country,
            mobileNumber,
            firstName,
            lastName,
            userName
        )
    )

    override suspend fun forgotPassword(email: String) =
        service.forgotPassword(ForgotPasswordRequest(email))

    override suspend fun getUserProfileDetails() = service.getUserProfileDetails()
    override suspend fun editPersonalUserProfile(
        userName: String,
        accountType: String,
        firstName: String,
        lastName: String,
        country: String,
        contactNo: String
    ) = service.editPersonalUserProfile(
        EditPersonalUserProfileRequest(
            userName, accountType, firstName, lastName, country, contactNo
        )
    )

    override suspend fun editCompanyUserProfile(
        userName: String,
        accountType: String,
        firstName: String,
        lastName: String,
        companyName: String,
        country: String,
        contactNo: String
    ) = service.editCompanyUserProfile(
        EditCompanyUserProfileRequest(
            userName, accountType, firstName, lastName, companyName, country, contactNo
        )
    )

    override suspend fun changePassword(
        oldPassword: String,
        newPassword: String
    ): ChangePasswordResponse {
        return service.changePassword(ChangePasswordRequest(oldPassword, newPassword))
    }

    override suspend fun editOverviewUserProfile(overview: String) =
        service.editOverviewUserProfile(
            EditOverviewUserProfileRequest(
                overview
            )
        )

    override suspend fun updateUser(
        firstName: String,
        lastName: String,
        contactNo: String,
        accountType: AccountTypeEnum,
        companyName: String,
        country: String,
        profileImage: String,
        coverImage: String,
    ): LoginResponse = service.updateUser(
        UpdateUserDetailRequest(
            firstName,
            lastName,
            contactNo,
            accountType,
            companyName,
            country,
            profileImage,
            coverImage
        )
    )

    override suspend fun updateProfilePicture(filePath: String): UpdateProfilePictureResponse {
        val parts = ArrayList<MultipartBody.Part>()
        parts.add(prepareFilePart("profileImage", filePath))
        return service.updatePicture(parts)
    }

    override suspend fun updateCoverPicture(filePath: String): UpdateProfilePictureResponse {
        val parts = ArrayList<MultipartBody.Part>()
        parts.add(prepareFilePart("coverImage", filePath))
        return service.updatePicture(parts)
    }


    override suspend fun resendVerification(email: String) =
        service.resendVerification(VerificationRequest(email))

    override suspend fun notificationSettingsUpdate(
        isNotice: Boolean,
        isNews: Boolean,
        isWork: Boolean,
        isLikeFollowComment: Boolean,
        isAssetChange: Boolean
    ) = service.notificationSettingsUpdate(
        NotificationSettingsUpdateRequest(
            NotificationSettingsUpdateRequest.Settings(
                NotificationSettingsUpdateRequest.Notification(
                    isNotice, isNews, isWork, isLikeFollowComment, isAssetChange
                )
            )
        )
    )

    override suspend fun newSignUp(email: String) = service.newSignUp(SignupRequest(email))

    override suspend fun newSignUpVerifyEmailCode(
        email: String,
        password: String,
        signUpType: String,
        verificationCode: Int
    ): SignupResponse = service.newSignUpVerifyEmailCode(
        VerifyEmailRequest(email, password, signUpType, verificationCode)
    )

    override suspend fun newSignUpVerifyMobile(
        contactNo: String,
        password: String,
        signUpType: String
    ): SignupResponse = service.newSignUpVerifyMobile(
        VerifyMobileRequest(contactNo, password, signUpType)
    )

    override suspend fun personalKYC(
        country: String,
        name: String,
        birthday: String,
        address: String,
        postalCode: String,
        city: String
    ): PersonalKYCResponse = service.personalKYC(
        PersonalKYCRequest(country, name, birthday, address, postalCode, city)
    )

    override suspend fun verifyForgetOTP(email: String, code: String) = service.verifyForgetOTP(email, code)

    override suspend fun identityUpload(filePath: String,filePath2:String,type:String): PersonalKYCResponse {
        val parts = ArrayList<MultipartBody.Part>()
        parts.add(prepareFilePart("fileA", filePath))
        parts.add(prepareFilePart("fileB", filePath2))
        return service.identityUpload(parts,prepareStringMultiPart(type))
    }

    override suspend fun faceUpload(filePath: String): PersonalKYCResponse {
        val parts = ArrayList<MultipartBody.Part>()
        parts.add(prepareFilePart("file", filePath))
        return service.faceUpload(parts)
    }

    override suspend fun getKYCDetail(): GetKYCResponse {
        return service.getKYCDetail()
    }
}