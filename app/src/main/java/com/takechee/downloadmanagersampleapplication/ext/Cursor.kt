package com.takechee.downloadmanagersampleapplication.ext

import android.database.Cursor
import androidx.core.database.getStringOrNull

internal inline fun <reified T> Cursor.getByIndex(key: String): T {
    val columnIndex = getColumnIndex(key)
    return when (T::class) {
        Int::class -> getInt(columnIndex)
        Long::class -> getLong(columnIndex)
        String::class -> getString(columnIndex)
        else -> {
            val valueType = T::class.java.canonicalName
            throw IllegalArgumentException("Illegal value type $valueType for key \"$key\"")
        }
    } as T
}

internal inline fun <reified T> Cursor.getByIndexOrNull(key: String): T? {
    val columnIndex = getColumnIndex(key)
    return when (T::class) {
        String::class -> getStringOrNull(columnIndex)
        else -> {
            val valueType = T::class.java.canonicalName
            throw IllegalArgumentException("Illegal value type $valueType for key \"$key\"")
        }
    } as T?
}
