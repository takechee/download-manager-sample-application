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
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.content.getSystemService
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

    private val writeExternalStoragePermissionHelper: WriteExternalStoragePermissionHelper by lazy {
        WriteExternalStoragePermissionHelperImpl(this)
    }


    // =============================================================================================
    //
    // Lifecycle
    //
    // =============================================================================================
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        writeExternalStoragePermissionHelper.setupRequestPermissionLauncher()

        registerReceiver(object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val id = intent?.extras?.getLong(DownloadManager.EXTRA_DOWNLOAD_ID) ?: return

                val cursor = downloadManager.query { setFilterById(id) }
                if (!cursor.moveToFirst()) {
                    return
                }

                val downloadedFile = DownloadedFile.from(cursor = cursor)
                DownloadDoneDialogFragment.show(supportFragmentManager, downloadedFile)
            }
        }, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))

        findViewById<Button>(R.id.downloadButton).apply {
            setOnClickListener {
                when {
                    // 権限が許可済み
                    writeExternalStoragePermissionHelper.checkSelfPermission() -> {
                        downloadFile()
                    }
                    // 権限が拒否済み
                    writeExternalStoragePermissionHelper.shouldShowRequestPermissionRationale() -> {
                        writeExternalStoragePermissionHelper.requestWriteExternalStoragePermission { isGranted ->
                            if (isGranted) {
                                downloadFile()
                            }
                        }
                    }
                    // 権限が未リクエスト状態
                    else -> writeExternalStoragePermissionHelper.requestWriteExternalStoragePermission { isGranted ->
                        if (isGranted) {
                            downloadFile()
                        }
                    }
                }
            }
        }
    }


    // =============================================================================================
    //
    // Utility
    //
    // =============================================================================================
    private fun downloadFile() {
        GlobalScope.launch { fileDownloader.download() }
    }


    // =============================================================================================
    //
    // Extension
    //
    // =============================================================================================
    private fun DownloadManager.query(factory: DownloadManager.Query.() -> Unit): Cursor {
        return query(DownloadManager.Query().apply(factory))
    }
}
