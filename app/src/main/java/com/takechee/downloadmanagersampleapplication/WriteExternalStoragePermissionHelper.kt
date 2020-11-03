package com.takechee.downloadmanagersampleapplication

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

interface WriteExternalStoragePermissionHelper {
    val hasPermission: Boolean
    fun setupRequestPermissionLauncher()
    fun checkSelfPermission(): Boolean
    fun shouldShowRequestPermissionRationale(): Boolean
    fun requestWriteExternalStoragePermission(callback: (isGranted: Boolean) -> Unit)
}


// =============================================================================================
//
// Implements
//
// =============================================================================================
internal class WriteExternalStoragePermissionHelperImpl(
    private val activity: FragmentActivity
) : WriteExternalStoragePermissionHelper {
    companion object {
        private const val PERMISSION_WRITE_EXTERNAL_STORAGE =
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    }

    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>

    private var activityResultCallback: (isGranted: Boolean) -> Unit = {}

    override var hasPermission: Boolean = false
        private set

    override fun setupRequestPermissionLauncher() {
        requestPermissionLauncher =
            activity.registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                hasPermission = isGranted
                activityResultCallback.invoke(isGranted)
            }
    }

    override fun checkSelfPermission(): Boolean {
        val permission =
            ContextCompat.checkSelfPermission(
                activity.applicationContext,
                PERMISSION_WRITE_EXTERNAL_STORAGE
            )
        hasPermission = permission == PackageManager.PERMISSION_GRANTED
        return hasPermission
    }

    override fun shouldShowRequestPermissionRationale(): Boolean {
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            activity.shouldShowRequestPermissionRationale(PERMISSION_WRITE_EXTERNAL_STORAGE)
        } else {
            false
        }
    }

    override fun requestWriteExternalStoragePermission(callback: (isGranted: Boolean) -> Unit) {
        activityResultCallback = callback
        requestPermissionLauncher.launch(PERMISSION_WRITE_EXTERNAL_STORAGE)
    }
}
