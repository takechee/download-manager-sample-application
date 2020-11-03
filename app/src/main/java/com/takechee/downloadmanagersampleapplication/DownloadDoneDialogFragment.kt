package com.takechee.downloadmanagersampleapplication

import android.app.Dialog
import android.app.DownloadManager
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.net.toUri
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class DownloadDoneDialogFragment : DialogFragment(), DialogInterface.OnClickListener {

    private val downloadedFile: DownloadedFile by lazy {
        requireNotNull(requireArguments().getParcelable(ARGS_DOWNLOADED_FILE))
    }


    // =============================================================================================
    //
    // Lifecycle
    //
    // =============================================================================================
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.download_done_title)
            .setMessage(downloadedFile.localUri)
            .apply {
                if (downloadedFile.mediaProviderUri != null) {
                    setPositiveButton(
                        R.string.download_done_positive,
                        this@DownloadDoneDialogFragment
                    )
                }
            }
            .setNegativeButton(R.string.download_done_negative, this)
            .create()
    }


    // =============================================================================================
    //
    // UserAction
    //
    // =============================================================================================
    override fun onClick(p0: DialogInterface?, p1: Int) {
        when (p1) {
            DialogInterface.BUTTON_POSITIVE -> {
                val intent = Intent().apply {
                    action = DownloadManager.ACTION_VIEW_DOWNLOADS
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }

                context?.packageManager?.let { manager ->
                    if (intent.resolveActivity(manager) != null) {
                        startActivity(intent)
                    }
                }
                Log.e("DownloadDone", "intent=${intent}")
            }
            DialogInterface.BUTTON_NEGATIVE -> {
                p0?.dismiss()
            }
            else -> {
                // Do nothing..
            }
        }
    }


    // =============================================================================================
    //
    // Companion
    //
    // =============================================================================================
    companion object {
        private const val TAG =
            "com.takechee.downloadmanagersampleapplication.DownloadDoneDialogFragment"
        private const val ARGS_DOWNLOADED_FILE = "DOWNLOADED_FILE"

        fun show(fragmentManager: FragmentManager, downloadedFile: DownloadedFile) {
            if (fragmentManager.findFragmentByTag(TAG) != null) return
            DownloadDoneDialogFragment()
                .apply {
                    arguments = bundleOf(ARGS_DOWNLOADED_FILE to downloadedFile)
                }
                .show(fragmentManager, TAG)
        }
    }
}
