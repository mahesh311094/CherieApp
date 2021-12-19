package com.ar7lab.cherieapp.network.response
//this class is used to send kyc request response for all post data API
class PersonalKYCResponse : BaseResponse<PersonalKYCResponse.Data>() {
    class Data(
       var kyc:KYC
    )

    class KYC(
        var step: Int?,
        var id: String?,
        var isKYCCompleted: Boolean?,
        var personalVerification: PersonalVerification?,
        var identityVerification:IdentityVerification?,
        var faceRecognition:FaceRecognition?
    )

    class PersonalVerification(
        var _id: String?,
        var country: String?,
        var name: String?,
        var birthday: String?,
        var address: String?,
        var postalCode: String?,
        var city: String?,
        var verifiedAt: String?,
        var isVerified: Boolean?,
        var verifiedBy: String?,
        var createdAt: String?
    )
    class IdentityVerification(
        var verifiedAt:String?,
        var isVerified:Boolean?,
        var verifiedBy:String?,
        var createdAt:String?
    )
    class FaceRecognition(
        var verifiedAt:String?,
        var isVerified:Boolean?,
        var verifiedBy:String?,
        var createdAt:String?

    )
}

