package com.lovelycatv.ai.shadowcat.app.util.common

import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

fun <T : CharSequence> T.asRequestBody(): RequestBody {
    return this.toString().toRequestBody("text/plain".toMediaTypeOrNull())
}

fun File.asRequestBody(): RequestBody {
    return this.asRequestBody("multipart/form-data".toMediaTypeOrNull())
}