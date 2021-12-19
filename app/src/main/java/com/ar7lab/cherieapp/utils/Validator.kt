package com.ar7lab.cherieapp.utils

import android.util.Base64
import java.lang.Exception
import java.security.KeyFactory
import java.security.PublicKey
import java.security.spec.X509EncodedKeySpec
import java.util.regex.Pattern
import javax.crypto.Cipher

private const val PASSWORD_PATTERN =
    "^(?=.*?[a-z])(?=.*?[A-Z])(?=.*?[0-9])(?=.*?[\$&+,:;=?@#|'<>.^*()%!-]).{6,12}\$"
private const val USERNAME_PATTERN =
    "^[a-z0-9_.]{1,11}\$"
private const val FIRSTNAME_PATTERN =
    "^[a-zA-Z]{1,11}\$"
private const val COMPANY_NAME_PATTERN =
    "^[a-zA-Z0-9 ]{1,20}\$"
private const val BANK_NAME_PATTERN =
    "^[a-zA-Z0-9 ]{1,30}\$"
private const val ACCOUNT_NUMBER_PATTERN =
    "^[0-9]{5,30}\$"
private const val CARD_PATTERN =
    "^(?:4[0-9]{12}(?:[0-9]{3})?|[25][1-7][0-9]{14}|6(?:011|5[0-9][0-9])[0-9]{12}|3[47][0-9]{13}|3(?:0[0-5]|[68][0-9])[0-9]{11}|(?:2131|1800|35\\d{3})\\d{11})\$"
private const val CARD_EXPIRY_PATTERN =
    "^(0[1-9]|1[0-2])\\/?([0-9]{4})\$"
private const val CVV_PATTERN =
    "^[0-9]{3,4}\$"
private const val CARDHOLDER_PATTERN =
    "^[a-zA-Z ]{1,30}\$"
// password lengthvalidation
fun passwordLengthValidation(password:String)= password.length >= 6


fun isValidEmail(email: String?) =
    !email.isNullOrBlank() && android.util.Patterns.EMAIL_ADDRESS.matcher(email)
        .matches()

fun isValidPassword(password: String?) =
    !password.isNullOrBlank()
            && Pattern.compile(PASSWORD_PATTERN)
        .matcher(password).matches()

fun isValidUserName(userName: String?) =
    !userName.isNullOrBlank()
            && Pattern.compile(USERNAME_PATTERN)
        .matcher(userName).matches()

fun isValidCountryName(country: String?) =
    !country.isNullOrBlank()
            && Pattern.compile(BANK_NAME_PATTERN)
        .matcher(country).matches()

fun isAccountNumberValid(number:String?) =
    !number.isNullOrBlank()
            && Pattern.compile(ACCOUNT_NUMBER_PATTERN)
        .matcher(number).matches()

fun isValidFirstName(firstName: String?) =
    !firstName.isNullOrBlank()
            && Pattern.compile(FIRSTNAME_PATTERN)
        .matcher(firstName).matches()

fun isValidLastName(lastName: String?) =
    !lastName.isNullOrBlank()
            && Pattern.compile(FIRSTNAME_PATTERN)
        .matcher(lastName).matches()


fun isValidCompanyName(companyName: String?) =
    !companyName.isNullOrBlank()
            && Pattern.compile(COMPANY_NAME_PATTERN)
        .matcher(companyName).matches()

fun isValidMobileNumber(mobileNumber: String?) =
    !mobileNumber.isNullOrBlank() && android.util.Patterns.PHONE.matcher(mobileNumber)
        .matches()

fun isValidCard(card: String?) =
    !card.isNullOrBlank()
            && Pattern.compile(CARD_PATTERN)
        .matcher(card).matches()

fun isValidCardExpiry(cardExpiry: String?) =
    !cardExpiry.isNullOrBlank()
            && Pattern.compile(CARD_EXPIRY_PATTERN)
        .matcher(cardExpiry).matches()

fun isValidCVC(cvc: String?) =
    !cvc.isNullOrBlank()
            && Pattern.compile(CVV_PATTERN)
        .matcher(cvc).matches()

fun isCardHolderNameValid(cardHolder: String?) =
    !cardHolder.isNullOrBlank()
            && Pattern.compile(CARDHOLDER_PATTERN)
        .matcher(cardHolder).matches()


/*
var PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAuSmP/rJPnOaIzfwnodCd/pc9xBdH5ZRFYD4TpedDMGez8B1lAfuYyj2fl5f7VD08+89TSeJ/+q3YuUEW8Z4k2X5FC+Q6E2nOWGe4A3Osh6XAQNPINasKsfpTG9p0eoYuLLzlSMKpvat4LViXwsb5r6hKPRTrbytBWIUQrcoqarJ0VcJBZGInFSJnMq73zse1EFTGedf5ZFOiSbN2ueL90HyyhtI0dA47z0mimV6epFlZUcTwtaB85WIXBXiVeSYHsfW1WLCNohiS7VISZ6HPB+LHzsgFM5H8an+3t/2sFcp+b3Vo2iCZkCEISpGUfxEVmg7rGvPuuhX1IkW+TbAByQIDAQAB"


fun enccriptData(txt: String): String? {
    var encoded = ""
    var encrypted: ByteArray? = null
    try {
        val publicBytes: ByteArray = Base64.decode(PUBLIC_KEY, Base64.DEFAULT)
        val keySpec = X509EncodedKeySpec(publicBytes)
        val keyFactory: KeyFactory = KeyFactory.getInstance("RSA")
        val pubKey: PublicKey = keyFactory.generatePublic(keySpec)
        val cipher: Cipher = Cipher.getInstance("RSA/ECB/PKCS1PADDING") //or try with "RSA"
        cipher.init(Cipher.ENCRYPT_MODE, pubKey)
        encrypted = cipher.doFinal(txt.toByteArray())
        encoded = Base64.encodeToString(encrypted, Base64.DEFAULT)
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return encoded.replace("\n","")
}*/
