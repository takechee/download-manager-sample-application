package com.takechee.downloadmanagersampleapplication

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class DownloadedFile(
    val id: Long,
    val title: String,
    val status: Int,
    val reason: Int,
    val localUri: String?,
    val mediaProviderUri: String?,
    val bytesTotal: Int,
    val bytesDownloaded: Int,
    val lastModifiedAt: Int
) : Parcelable