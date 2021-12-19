package com.ar7lab.cherieapp.model

import com.ar7lab.cherieapp.enums.AccountTypeEnum
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.io.Serializable

/**
 * User details serializable class
 * @property _id = logged in user id
 * @property userName = user name
 * @property firstName = user first name
 * @property lastName = user last name
 * @property email = user email
 * @property contactNo = user contact number
 * @property accountType = user account type ("Personal"/"Company")
 * @property userId = user id
 * @property isVerified = user is verified or not
 * @property socialMediaType = social media type ("Google"/"Facebook"/"Line"/"Kakao")
 * @property socialMediaId = social media id
 * @property profileImage = user profile image
 * @property coverImage = user profile cover image
 * */
@JsonClass(generateAdapter = true)
data class User(
    @Json(name = "_id")
    var id: String = "",
    var userName: String? = "",
    var firstName: String = "",
    var lastName: String = "",
    var email: String = "",
    var contactNo: String? = "",
    var accountType: AccountTypeEnum = AccountTypeEnum.PERSONAL,
    var userId: String = "",
    var isVerified: Boolean = false,
    var role: String = "",
    var socialMediaType: String? = "",
    var socialMediaId: String = "",
    var profileImage: String? = "",
    var coverImage: String? = "",
    var country: String? = "",
    var overview: String? = "",
    var followingCount: Int? = 0,
    @Json(name = "firstLogin")
    var isFirstTimeLogin: Boolean = true,
    @Json(name = "isKYCCompleted")
    var isKYCCompleted: Boolean = true,
    var companyName: String = "",
    var settings: Settings = Settings()
) : Serializable