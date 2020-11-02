package com.takechee.downloadmanagersampleapplication

import android.app.DownloadManager
import android.content.Context
import android.os.Environment
import android.util.Log
import android.webkit.URLUtil
import androidx.core.net.toUri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL

interface FileDownloader {
    suspend fun download(): Long
}

class FileDownloaderImpl constructor(
    private val url: String = "",
    private val applicationContext: Context,
    private val downloadManager: DownloadManager,
) : FileDownloader {

    companion object {
        private const val MY_DIRECTORY = "DownloadSampleApp"
    }

    private fun generateSubPath(fileName: String): String {
        return "${MY_DIRECTORY}/${fileName}"
    }

    override suspend fun download(): Long {
        val fileName = URLUtil.guessFileName(url, null, null)

        Log.e("fileName", fileName)
        val request = DownloadManager.Request(url.toUri())
            .setTitle(fileName)
            .setDescription(applicationContext.getString(R.string.downloading_file))
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalPublicDir(
                Environment.DIRECTORY_DOWNLOADS,
                generateSubPath(fileName)
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

    @Suppress("BlockingMethodInNonBlockingContext")
    private suspend fun getContentDisposition(url: String): String = withContext(Dispatchers.IO) {
        URL(url).openConnection().getHeaderField("Content-Disposition")
    }
}