package com.example.aac.util

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.util.*

object FileUtil {
    /**
     * Bitmap을 기기 갤러리(Pictures)에 저장하고 해당 Uri를 반환
     */
    fun saveBitmapToGallery(context: Context, bitmap: Bitmap): Uri? {
        val filename = "AAC_${System.currentTimeMillis()}.jpg"
        var fos: OutputStream? = null
        var imageUri: Uri? = null
        val contentResolver = context.contentResolver

        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.MediaColumns.RELATIVE_PATH, "Pictures/AAC_Words")
                put(MediaStore.MediaColumns.IS_PENDING, 1)
            }
        }

        val collection = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        imageUri = contentResolver.insert(collection, contentValues)

        imageUri?.let { uri ->
            try {
                fos = contentResolver.openOutputStream(uri)
                fos?.use {
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    contentValues.clear()
                    contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                    contentResolver.update(uri, contentValues, null, null)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                return null
            }
        }
        return imageUri
    }

    /**
     * 🔥 [추가] 외부 Uri 이미지를 앱 내부 저장소로 영구 복사
     * 로그아웃 후에도 권한 문제 없이 이미지를 보기 위함
     */
    fun copyImageToInternalStorage(context: Context, uri: Uri): Uri? {
        return try {
            val fileName = "word_${System.currentTimeMillis()}_${UUID.randomUUID()}.jpg"
            val storageDir = File(context.filesDir, "word_images")
            if (!storageDir.exists()) storageDir.mkdirs()

            val destFile = File(storageDir, fileName)
            val inputStream = context.contentResolver.openInputStream(uri)
            val outputStream = FileOutputStream(destFile)

            inputStream?.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }
            // file:///... 형태의 Uri 반환
            Uri.fromFile(destFile)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
