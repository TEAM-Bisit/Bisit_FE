package com.example.bisit.util

import android.content.Context
import android.net.Uri
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream

/**
 * Uri → MultipartBody.Part 변환 유틸
 *
 * @param context Context
 * @param uri 선택한 이미지 Uri
 * @param partName 서버에서 요구하는 파트 이름 (ex: "photo")
 */
fun uriToMultipart(
    context: Context,
    uri: Uri,
    partName: String
): MultipartBody.Part {

    val contentResolver = context.contentResolver
    val inputStream = contentResolver.openInputStream(uri)
        ?: throw IllegalArgumentException("Uri InputStream을 열 수 없습니다.")

    // 캐시 디렉토리에 임시 파일 생성
    val tempFile = File.createTempFile("upload_", ".jpg", context.cacheDir)

    FileOutputStream(tempFile).use { output ->
        inputStream.use { input ->
            input.copyTo(output)
        }
    }

    val requestBody =
        tempFile.asRequestBody("image/*".toMediaTypeOrNull())

    return MultipartBody.Part.createFormData(
        partName,
        tempFile.name,
        requestBody
    )
}
