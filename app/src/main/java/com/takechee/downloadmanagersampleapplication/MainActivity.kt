package com.takechee.downloadmanagersampleapplication

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.database.Cursor
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.getSystemService
import androidx.core.database.getStringOrNull
import androidx.core.net.toUri
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private val downloadManager: DownloadManager by lazy {
        requireNotNull(applicationContext.getSystemService())
    }

    private val fileDownloader: FileDownloader by lazy {
        FileDownloaderImpl(
            applicationContext = applicationContext,
            downloadManager = downloadManager
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        registerReceiver(object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val id = intent?.extras?.getLong(DownloadManager.EXTRA_DOWNLOAD_ID) ?: return

                val cursor = downloadManager.query { setFilterById(id) }
                if (!cursor.moveToFirst()) {
                    return
                }

                val downloadedFile = DownloadedFile(
                    id = cursor.getLong(cursor.getColumnIndex(DownloadManager.COLUMN_ID)),
                    title = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_TITLE)),
                    status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)),
                    reason = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_REASON)),
                    localUri = cursor.getStringOrNull(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI)),
                    mediaProviderUri = cursor.getStringOrNull(cursor.getColumnIndex(DownloadManager.COLUMN_MEDIAPROVIDER_URI)),
                    bytesTotal = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)),
                    bytesDownloaded = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)),
                    lastModifiedAt = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_LAST_MODIFIED_TIMESTAMP)),
                )

                Toast.makeText(
                    this@MainActivity,
                    "Download Completed. downloadedFile: $downloadedFile",
                    Toast.LENGTH_SHORT
                ).show()
                Log.d("Download","downloadedFile: $downloadedFile")

//                val intent = Intent(Intent.ACTION_VIEW, downloadedFile.mediaProviderUri.toUri())
//                startActivity(intent)
            }
        }, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))

        findViewById<Button>(R.id.downloadButton).apply {
            setOnClickListener {
                GlobalScope.launch { fileDownloader.download() }
            }
        }
    }

    private fun DownloadManager.query(factory: DownloadManager.Query.() -> Unit): Cursor {
        return query(DownloadManager.Query().apply(factory))
    }
}