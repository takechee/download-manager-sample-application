package com.takechee.downloadmanagersampleapplication

import android.app.DownloadManager
import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Parcelable
import androidx.core.content.FileProvider
import androidx.core.net.toFile
import androidx.core.net.toUri
import com.takechee.downloadmanagersampleapplication.ext.getByIndex
import com.takechee.downloadmanagersampleapplication.ext.getByIndexOrNull
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize
import java.io.File

@Parcelize
data class DownloadedFile(
    val id: Long,
    val title: String,
    val status: Int,
    val reason: Int,
    val uri: String?,
    val localUri: String?,
    val mediaProviderUriString: String?,
    val mediaType: String?,
    val bytesTotal: Int,
    val bytesDownloaded: Int,
    val lastModifiedAt: Int,
) : Parcelable {

    @IgnoredOnParcel
    val mediaProviderUri: Uri? by lazy { mediaProviderUriString?.toUri() }

    companion object {
        fun from(cursor: Cursor) = DownloadedFile(
            id = cursor.getByIndex(DownloadManager.COLUMN_ID),
            title = cursor.getByIndex(DownloadManager.COLUMN_TITLE),
            status = cursor.getByIndex(DownloadManager.COLUMN_STATUS),
            reason = cursor.getByIndex(DownloadManager.COLUMN_REASON),
            uri = cursor.getByIndexOrNull(DownloadManager.COLUMN_URI),
            localUri = cursor.getByIndexOrNull(DownloadManager.COLUMN_LOCAL_URI),
            mediaProviderUriString = cursor.getMediaUri(),
            mediaType = cursor.getByIndex(DownloadManager.COLUMN_MEDIA_TYPE),
            bytesTotal = cursor.getByIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES),
            bytesDownloaded = cursor.getByIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR),
            lastModifiedAt = cursor.getByIndex(DownloadManager.COLUMN_LAST_MODIFIED_TIMESTAMP),
        )
    }
}

private fun Cursor.getMediaUri(): String? {
    return when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.P -> {
            getByIndex<String>(DownloadManager.COLUMN_MEDIAPROVIDER_URI)
        }
        // TODO(I want to know how to get the content Uri.)
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.N -> {
            val localUri = getByIndex<String>(DownloadManager.COLUMN_LOCAL_URI).toUri()
            val path = File(localUri.path ?: return null).absolutePath
            return path
        }
        else -> getByIndex<String>(DownloadManager.COLUMN_LOCAL_FILENAME).let { pathName ->
            File(pathName).absolutePath
        }
    }
}
