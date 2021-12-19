package com.ar7lab.cherieapp.network.response
//this class getting KYC response getting all detail from server
class GetKYCResponse : BaseResponse<GetKYCResponse.Data>() {
    class Data(
        var step: Int?,
        var _id: String?,
        var userId: String?,
        var isKYCCompleted: Boolean?,
        var personalVerification: PersonalVerification?,
       // var identityVerification:IdentityVerification?,
        var createdAt:String?,
        var updatedAt:String?,
        //var faceRecognition:FaceRecognition?
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
        var createdAt:String?,
        var _id:String?,
        var files:ArrayList<Files>?=null
    )
    class Files(
        var _id:String?,
        var url:String?,
        var blobName:String?
    )

    class FaceRecognition(
        var verifiedAt:String?,
        var isVerified:Boolean?,
        var verifiedBy:String?,
        var createdAt:String?,
        var file:Files?

    )
}

