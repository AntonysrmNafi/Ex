package com.blockveil.expensetracker.util

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

/**
 * Creates an empty file under the app's private receipts folder and returns a content://
 * URI for it via FileProvider, so the system camera app can write the captured photo there
 * without needing broad storage access.
 */
fun createReceiptPhotoUri(context: Context): Uri {
    val receiptsDir = File(context.filesDir, "receipts").apply { mkdirs() }
    val file = File(receiptsDir, "receipt_${System.currentTimeMillis()}.jpg")
    return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
}
