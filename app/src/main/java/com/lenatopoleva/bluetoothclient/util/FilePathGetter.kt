package com.lenatopoleva.bluetoothclient.util

import android.annotation.TargetApi
import android.content.Context
import androidx.core.content.ContentResolverCompat
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.provider.SyncStateContract.Columns.DATA


class FilePathGetter {
    @TargetApi(Build.VERSION_CODES.KITKAT)
    fun getPath(context: Context, uri: Uri): String? {

//        // DocumentProvider
//        if (DocumentsContract.isDocumentUri(context, uri)) {
//            // ExternalStorageProvider
//            if (isExternalStorageDocument(uri)) {
//                println("getPath; isExternalStorageDocument")
//                val docId = DocumentsContract.getDocumentId(uri)
//                val split = docId.split(":".toRegex()).toTypedArray()
//                val type = split[0]
//                if ("primary".equals(type, ignoreCase = true)) {
//                    return Environment.getExternalStorageDirectory().toString() + "/" + split[1]
//                }
//
//                // TODO handle non-primary volumes
//            } else if (isDownloadsDocument(uri)) {
//                println("getPath; isDownloadsDocument")
//                val id = DocumentsContract.getDocumentId(uri)
//                println("getPath; document id: $id")
//                val contentUri: Uri = ContentUris.withAppendedId(
//                    Uri.parse("content://downloads/public_downloads"), java.lang.Long.valueOf(id)
//                )
//                return getDataColumn(context, contentUri, null, null)
//            } else if (isMediaDocument(uri)) {
//                println("getPath; isMediaDocument")
//                val docId = DocumentsContract.getDocumentId(uri)
//                val split = docId.split(":".toRegex()).toTypedArray()
//                val type = split[0]
//                var contentUri: Uri? = null
//                when (type) {
//                    "image" -> contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
//                    "video" -> contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
//                    "audio" -> contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
//                }
//                val selection = "_id=?"
//                val selectionArgs = arrayOf(
//                    split[1]
//                )
//                return getDataColumn(context, contentUri, selection, selectionArgs)
//            }
//        } else if ("content".equals(uri.scheme, ignoreCase = true)) {
//            println("getPath; uri.scheme is ${uri.scheme}")
//            // Return the remote address
//            return if (isGooglePhotosUri(uri)) uri.lastPathSegment
//            else getDataColumn(context, uri, null, null)
//        } else if ("file".equals(uri.scheme, ignoreCase = true)) {
//            return uri.path
//        }
                if ("content".equals(uri.scheme, ignoreCase = true)) {
                                println("getPath; uri.scheme is ${uri.scheme}")

                    return getDataColumn(context, uri, null, null)
                }

        return null
    }

    private fun getDataColumn(
        context: Context, uri: Uri?, selection: String?,
        selectionArgs: Array<String>?
    ): String? {
        var cursor: Cursor? = null
//        val column = "_data"
//        val column = OpenableColumns.DISPLAY_NAME
        val column = null
//        val projection = arrayOf(column)
        val projection = null
        try {
            cursor = uri?.let {
               ContentResolverCompat.query(
                       context.contentResolver,
                       it,
                       projection,
                       selection,
                       selectionArgs,
                       null,
                       null
                )
            }
            println("cursor =  $cursor, cursor.moveToFirst() is ${cursor?.moveToFirst()} ")

            if (cursor != null && cursor.moveToFirst()) {
//                val nameColumnIndex: Int = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)

                val index: Int = cursor.getColumnIndexOrThrow(column)
//                val index: Int = 0
//                println("nameColumnIndex = $nameColumnIndex")
                println("file path = ${cursor.getString(index)}")
                return cursor.getString(index)
            }
        } finally {
            cursor?.close()
        }
        return null
    }


    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    private fun isExternalStorageDocument(uri: Uri): Boolean {
        return "com.android.externalstorage.documents" == uri.authority
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    private fun isDownloadsDocument(uri: Uri): Boolean {
        return "com.android.providers.downloads.documents" == uri.authority
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    private fun isMediaDocument(uri: Uri): Boolean {
        return "com.android.providers.media.documents" == uri.authority
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    private fun isGooglePhotosUri(uri: Uri): Boolean {
        return "com.google.android.apps.photos.content" == uri.authority
    }
}