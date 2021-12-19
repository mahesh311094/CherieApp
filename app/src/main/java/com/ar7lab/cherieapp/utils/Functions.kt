package com.ar7lab.cherieapp.utils

import android.util.Base64
import android.webkit.MimeTypeMap
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import timber.log.Timber
import java.io.File
import java.lang.Exception
import java.security.KeyFactory
import java.security.PublicKey
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher


fun prepareFilePart(partName: String, filePath: String): MultipartBody.Part {
    val file = File(filePath)
    val requestFile = RequestBody.create(
        getMimeType(file.absolutePath).toMediaTypeOrNull(),
        file
    )
    return MultipartBody.Part.createFormData(partName, file.name, requestFile)
}

fun prepareStringMultiPart(value: String):RequestBody{
    val mediaType = "text/plain"
    val rName = RequestBody.create(mediaType!!.toMediaTypeOrNull(), value)
    return rName
}


fun getMimeType(url: String): String {
    var type = "*/*"
    val extension = MimeTypeMap.getFileExtensionFromUrl(removeFileNameForCheckMimeTypePurpose(url))
    if (extension != null) {
        try {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension) ?: type
        } catch (exception: Throwable) {
            Timber.d("MimeTypeMap.getSingleton…eFromExtension(extension) must not be null")
        }
    }
    return type
}


private fun removeFileNameForCheckMimeTypePurpose(s: String): String {
    if (s.isEmpty() || s.lastIndexOf('.') == -1) {
        return ""
    }
    return s.replaceRange(0, s.lastIndexOf('.'), "filename")
}

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
}