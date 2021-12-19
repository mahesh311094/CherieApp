package com.ar7lab.cherieapp.utils

import android.content.Context
import android.net.Uri
import android.os.Environment
import android.os.SystemClock
import android.provider.MediaStore
import android.text.TextUtils
import java.io.*

/**
 *
 */
class FileUtil {

    companion object {
        const val PROCESSING_DIR_NAME = "processing"
        private const val DEFAULT_IMAGE_FILE_NAME = "captured_file.jpg"

        fun getDiskCacheDir(context: Context): File {
            return getDiskCacheDir(context, PROCESSING_DIR_NAME)
        }

        fun getDiskCacheDir(context: Context, uniqueName: String): File {
            val cachePath = if (Environment.MEDIA_MOUNTED == Environment.getExternalStorageState())
                getExternalCacheDir(context)!!.path
            else
                context.cacheDir.path

            return File(cachePath + File.separator + uniqueName)
        }

        private fun getExternalCacheDir(context: Context): File? {
            return context.externalCacheDir
        }

        fun getOutputMediaFile(
            context: Context,
            fileName: String?,
            isVideo: Boolean = false
        ): File? {
            val mediaStorageDir = FileUtil.getDiskCacheDir(context)
            if (!mediaStorageDir.exists()) {
                if (!mediaStorageDir.mkdirs()) {
                    return null
                }
            }

            var trueFileName = ""
            trueFileName = if (TextUtils.isEmpty(fileName)) DEFAULT_IMAGE_FILE_NAME else "$fileName.jpg"

            val file = File(mediaStorageDir.path + File.separator + trueFileName)
            if (file.exists())
                file.delete()
            return file
        }

        fun getRealPathFromURI(context: Context, contentUri: Uri): String? {
            var result: String? = null
            try {
                val proj = arrayOf(MediaStore.Images.Media.DATA)
                val cr = context.contentResolver
                val cursor = cr.query(contentUri, proj, null, null, null)
                if (cursor != null && !cursor.isClosed) {
                    val columnIndex = cursor
                            .getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                    cursor.moveToFirst()
                    result = cursor.getString(columnIndex)
                    cursor.close()
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
            }

            return result
        }

        fun getImagePathFromInputStreamUri(
            context: Context,
            uri: Uri,
        ): String? {
            var inputStream: InputStream? = null
            var filePath: String? = null
            if (uri.authority != null) {
                try {
                    inputStream = context.contentResolver.openInputStream(uri) // context needed
                    val photoFile = createTemporalFileFrom(context, inputStream)
                    filePath = photoFile!!.path
                } catch (e: FileNotFoundException) {
                    e.printStackTrace()
                } catch (e: IOException) {
                    e.printStackTrace()
                } finally {
                    try {
                        inputStream?.close()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }
            return filePath
        }

        @Throws(IOException::class)
        private fun createTemporalFileFrom(
            context: Context,
            inputStream: InputStream?,
        ): File? {
            var targetFile: File? = null
            if (inputStream != null) {
                var read: Int
                val buffer = ByteArray(8 * 1024)
                targetFile = createTemporalFile(context)
                val outputStream: OutputStream = FileOutputStream(targetFile)
                while (inputStream.read(buffer).also { read = it } != -1) {
                    outputStream.write(buffer, 0, read)
                }
                outputStream.flush()
                try {
                    outputStream.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
            return targetFile
        }

        private fun createTemporalFile(context: Context): File {
            val suffixName =  DEFAULT_IMAGE_FILE_NAME
            return File(
                getExternalCacheDir(context),
                "${SystemClock.uptimeMillis()}_$suffixName"
            )
        }
    }

}