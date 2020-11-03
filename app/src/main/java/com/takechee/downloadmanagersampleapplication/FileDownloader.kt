package com.takechee.downloadmanagersampleapplication

import android.app.DownloadManager
import android.content.Context
import android.os.Environment
import android.webkit.URLUtil
import androidx.core.net.toUri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL
import java.net.URLDecoder

interface FileDownloader {
    suspend fun download(): Long?
}


// =============================================================================================
//
// Implements
//
// =============================================================================================
class FileDownloaderImpl constructor(
    private val url: String = "https://photo.wel-kids.jp/letter_file/download/2543",
    private val applicationContext: Context,
    private val downloadManager: DownloadManager,
) : FileDownloader {

    companion object {
        private const val MY_DIRECTORY = "DownloadSampleApp"

        private const val FILE_ENCODE_METHOD = "UTF-8"

        // example: UTF-8'' ... + 2 はカンマ2つ分
        private const val FILE_NAME_FIRST_INDEX = FILE_ENCODE_METHOD.length + 2
    }

    override suspend fun download(): Long? {
        if (!URLUtil.isValidUrl(url)) return null

        val decodedFileName = getDecodedFileName(url)

        val request = DownloadManager.Request(url.toUri())
            .setTitle(decodedFileName)
            .setDescription(applicationContext.getString(R.string.downloading_file))
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalPublicDir(
                Environment.DIRECTORY_DOWNLOADS,
                generateSubPath(decodedFileName)
            )
            .apply {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                    setRequiresCharging(false)
                }
            }
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(true)

        // return download id.
        return downloadManager.enqueue(request)
    }

    private fun generateSubPath(fileName: String): String {
        return "${MY_DIRECTORY}/${fileName}"
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    private suspend fun getDecodedFileName(url: String): String = withContext(Dispatchers.IO) {
        // example: attachment; filename*=UTF-8''"A4-2.PDF"
        val contentDisposition = URL(url).openConnection().getHeaderField("Content-Disposition")
        // example: "A4-2.PDF" → A4-2.PDF
        val encodedFileName = contentDisposition.substringWkpFileEncodeMethod()
            .replace("\"", "")
        // デコード処理
        URLDecoder.decode(encodedFileName, FILE_ENCODE_METHOD)
    }

    private fun String.substringWkpFileEncodeMethod(): String {
        return substring(lastIndexOf(FILE_ENCODE_METHOD) + FILE_NAME_FIRST_INDEX)
    }
}
