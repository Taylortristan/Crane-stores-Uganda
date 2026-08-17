package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.UUID

object ImageStorageHelper {

    fun saveUriToInternalStorage(context: Context, sourceUri: Uri): String? {
        return try {
            val directory = File(context.filesDir, "product_images")
            if (!directory.exists()) {
                directory.mkdirs()
            }
            val fileName = "crane_prod_${UUID.randomUUID()}.jpg"
            val destinationFile = File(directory, fileName)

            val inputStream: InputStream? = context.contentResolver.openInputStream(sourceUri)
            val outputStream = FileOutputStream(destinationFile)

            inputStream?.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }

            destinationFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun saveBitmapToInternalStorage(context: Context, bitmap: Bitmap): String? {
        return try {
            val directory = File(context.filesDir, "product_images")
            if (!directory.exists()) {
                directory.mkdirs()
            }
            val fileName = "crane_prod_${UUID.randomUUID()}.jpg"
            val destinationFile = File(directory, fileName)

            FileOutputStream(destinationFile).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
            }
            destinationFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
