package com.lovelycatv.ai.shadowcat.app.util.android

import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.DocumentsContract
import android.provider.MediaStore

class UriUtils {
    companion object {
        @JvmStatic
        private fun getUriPath(context: Context, uri: Uri, selection: String?): String? {
            var path: String? = null
            val cursor = context.contentResolver.query(uri, null, selection, null, null)
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    val index = cursor.getColumnIndex(MediaStore.Images.Media.DATA)
                    path = cursor.getString(if (index >= 0) index else 0)
                }
                cursor.close()
            }
            return path
        }

        @JvmStatic
        fun handleUriToPath(
            context: Context,
            data: Intent,
        ): String? {
            var targetPath: String? = null
            val uri = data.data
            if (DocumentsContract.isDocumentUri(context, uri)) {
                val docId = DocumentsContract.getDocumentId(uri)
                if ("com.android.providers.media.documents" == uri!!.authority) {
                    val id =
                        docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1]
                    val selection = MediaStore.Images.Media._ID + "=" + id
                    targetPath =
                        getUriPath(context, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection)
                } else if ("com.android.providers.downloads.documents" == uri.authority) {
                    val contentUri = ContentUris.withAppendedId(
                        Uri.parse("content: //downloads/public_downloads"),
                        docId.toLong()
                    )
                    targetPath = getUriPath(context, contentUri, null)
                }
            } else if ("content".equals(uri!!.scheme, ignoreCase = true)) {
                targetPath = getUriPath(context, uri, null)
            } else if ("file".equals(uri.scheme, ignoreCase = true)) {
                targetPath = uri.path
            }
            return targetPath
        }
    }
}